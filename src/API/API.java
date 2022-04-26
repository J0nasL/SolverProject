package API;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class API {

    private static final Connection connection = new Connection();
    private static ConnectionURI uri;

    private final String token;
    private final String[] headers;

    public API(String token, String businessID, String contextID) {
        uri = new ConnectionURI(businessID, contextID);
        this.token = token;
        headers = new String[]{"Authorization", "Bearer " + token};
        /*try {
            testConnection();
        } catch (APIException e) {
            System.out.println("Cannot connect to host! Host=" + ConnectionURI.HOSTNAME);
        }*/
    }

    //Statics

    public static void testConnection() throws APIException {
        if (connection.get(ConnectionURI.getURI(ConnectionURI.SITE_DOWN), new String[]{}).statusCode() != Connection.OK_STATUS) {
            throw new APIException("Cannot connect to host " + ConnectionURI.ON_DEMAND);
        }
    }

    public static String getToken(String id_str) {
        String[] headers = new String[]{"X-NewRelic-ID", id_str};
        HttpResponse<String> response = connection.put(ConnectionURI.getURI(ConnectionURI.LOGIN), headers);

        if (response.statusCode() == Connection.OK_STATUS) {
            Map<String, List<String>> respHeaders = response.headers().map();
            //String refresh_token = respHeaders.get("refresh-token").get(0);
            return respHeaders.get("access-token").get(0);
        }
        return null;
    }

    /**
     * Gets data from the api /config file, which contains a lot of important information
     *
     * @return String[][] containing { {businessID,contextID}, String[] vendorIDs }
     */
    public static String[][] getConfig(String token) {
        String[] headers = new String[]{"Authorization", "Bearer " + token};
        HttpResponse<String> response = connection.get(ConnectionURI.getURI(ConnectionURI.CONFIG), headers);

        if (response.statusCode() == Connection.OK_STATUS) {

            JSONObject json = new JSONObject(response.body());
            String businessID = json.get("contextID").toString();
            String contextID = json.get("tenantID").toString();
            JSONArray storeList = (JSONArray) json.get("storeList");
            JSONObject item = (JSONObject) storeList.get(0);
            JSONArray vendors = (JSONArray) item.get("displayProfileId");

            String[] vendorNative = new String[vendors.length()];
            for (int i = 0; i < vendors.length(); i++) {
                vendorNative[i] = vendors.getString(i);
            }

            return new String[][]{new String[]{businessID, contextID}, vendorNative};
        }
        return null;
    }

    public static API getInstance(String token) {
        String[] res = getConfig(token)[0];
        return new API(token, res[0], res[1]);
    }

    //Instance methods

    public String[] getVendorIDs() {
        return getConfig(token)[1];
    }

    /**
     * Slow, takes ~7 seconds to return
     *
     * @return Array of vendors with no children
     */
    public ArrayList<Vendor> getLocations() {
        HttpResponse<String> response = connection.get(ConnectionURI.getURI(uri.locations), headers);

        if (response.statusCode() == Connection.OK_STATUS) {
            JSONArray json = new JSONArray(response.body());

            ArrayList<Vendor> vendors = new ArrayList<>();
            for (Object i : json) {
                //for each vendor:
                JSONObject cur_vendor = (JSONObject) i;

                String[] conceptData = parseCommonData(cur_vendor);
                String name = conceptData[0];
                String vendorID = conceptData[1];
                String menuID = conceptData[2];

                Menu cur_menu = ModelFactory.makeMenu(menuID, null, new ArrayList<>());
                ArrayList<Menu> menus = new ArrayList<>();
                menus.add(cur_menu);

                //get whether this vendor is currently available
                JSONObject availableAt = cur_vendor.getJSONObject("availableAt");
                boolean isAvailable = availableAt.getBoolean("availableNow");

                boolean conceptsAvailable=false; //assume closed if unable to parse
                if (availableAt.has("conceptsAvailableNow")){
                    conceptsAvailable = availableAt.getBoolean("conceptsAvailableNow");
                } else {
                    System.out.println("Unable to parse concept for "+name+":\n"+availableAt.toString());
                }
                //boolean isAvailable=cur_vendor.getBoolean("storeAvailabeNow"); //this is always true for some reason

                //TODO: figure out how to handle available vs concepts available
                Vendor v = ModelFactory.makeVendor(vendorID, name, menus);
                v.setOpen(isAvailable && conceptsAvailable);
                vendors.add(v);
            }
            return vendors;
        }
        return null;
    }

    /**
     * Retrieves information from the vendor main api page
     * Gets vendor name and the current menu id
     * Note: Does not set isOpen property
     *
     * @return new Vendor instance
     */
    public Vendor getVendorMain(String vendorID) {
        HttpResponse<String> response = connection.post(ConnectionURI.getURI(uri.locationMain + "/" + vendorID), headers,
                HttpRequest.BodyPublishers.noBody());
        //TODO

        if (response.statusCode() == Connection.OK_STATUS) {
            JSONObject body = new JSONObject(response.body());

            String[] conceptData = parseCommonData(body);
            String name = conceptData[0];
            //String vendorID=conceptData[1]; //vendor id is already provided
            String menuID = conceptData[2];

            //TODO check if property isAsapOrderDisabled relates to high demand

            Vendor vendor = ModelFactory.makeVendor(vendorID, name, new ArrayList<>());
            vendor.setName(name);
            vendor.setCurrentMenuID(menuID);

            return vendor;
        }
        return null;
    }

    /**
     * Retrieves information from the vendor concepts page
     * Gets availability, name, menus and its children, and the current menuID
     */
    public Vendor getVendorConcepts(String vendorID) {
        HttpResponse<String> response = connection.post(ConnectionURI.getURI(uri.locationConcepts + vendorID), headers,
                HttpRequest.BodyPublishers.ofString("{}"));

        if (response.statusCode() == Connection.OK_STATUS) {
            JSONArray body = new JSONArray(response.body());

            JSONObject info = body.getJSONObject(0);
            String currentMenuID = info.getString("id");

            String vendorName = info.getJSONObject("conceptOptions").getString("displayText");
            boolean isOpen = info.getBoolean("availableNow");

            //make Schedules
            //TODO is there any reason to bother with schedules since isOpen and isAvailable exist?
            //ArrayList<MenuSchedule> schedules = new ArrayList<>();

            //make Menus
            JSONArray menuJson = info.getJSONArray("menus");
            ArrayList<Menu> menus = new ArrayList<>();
            for (Object i : menuJson) {
                JSONObject menu = (JSONObject) i;
                String description = menu.getString("description"); //TODO store this?
                String id = menu.getString("id");
                String name = menu.getString("name");


                //make MenuCategories
                JSONArray categoryJson = menu.getJSONArray("categories");
                ArrayList<MenuCategory> categories = new ArrayList<>();
                for (Object j : categoryJson) {
                    JSONObject categoryItem = (JSONObject) j;

                    String categoryID = categoryItem.getString("categoryId");
                    String categoryName = categoryItem.getString("name");
                    //TODO:
                    //what does categoryOptions: {} do?
                    //what does isAvailableToGuests: true do?
                    //what does itemIdToItemPropertiesMap: {} do?

                    //make MenuItems
                    ArrayList<MenuItem> menuItems = new ArrayList<>();
                    JSONArray items = categoryItem.getJSONArray("items");
                    for (Object k : items) {
                        String itemID = k.toString();
                        menuItems.add(ModelFactory.makeMenuItem(itemID, null));
                    }
                    categories.add(ModelFactory.makeMenuCategory(categoryID, categoryName, menuItems));
                }

                menus.add(ModelFactory.makeMenu(id, name, categories));
            }
            Vendor v = ModelFactory.makeVendor(vendorID, vendorName, menus);
            v.setOpen(isOpen);
            v.menuLocationID=currentMenuID;
            return v;
        }
        return null;
    }

    public void getMenu(Vendor vendor){
        //get the current menu from the current location id
        //OnDemand is weird because it has a separate id to go to in order to get the current menu
        //but this id varies by vendor
        String menuID=vendor.menuLocationID;
        HttpResponse<String> response = connection.post(ConnectionURI.getURI(uri.locationConcepts + vendor.getID() + uri.menuAddon +menuID), headers,
                HttpRequest.BodyPublishers.ofString("{}"));

    }


    /**
     * Parses data shared by the locations and vendor main api pages
     *
     * @param vendorData object representing a vendor
     * @return array of [vendor name, vendor id, menu id]
     */
    private String[] parseCommonData(JSONObject vendorData) {
        JSONArray conceptInfo = vendorData.getJSONArray("conceptInfo");
        JSONObject concept = conceptInfo.getJSONObject(0);
        String name = concept.getString("onDemandDisplayText"); //get name
        String menuID = concept.getString("id"); //get menu id

        String vendorID = vendorData.getString("displayProfileId"); //get id

        //when there is a custom name, change it to that
        //TODO: decide which name takes priority
        //currently the custom name wins when enabled
        JSONObject customLocation = (JSONObject) vendorData.get("customizeLocation");
        boolean customizeName = customLocation.getBoolean("featureEnabled");
        if (customizeName) {
            name = customLocation.getString("locationName");
        }

        return new String[]{name, vendorID, menuID};
    }

}
