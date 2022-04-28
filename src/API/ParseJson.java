package API;

import Model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpResponse;
import java.util.ArrayList;

public class ParseJson {
    public static Vendor parseMain(HttpResponse<String> response){
        if (response.statusCode() == Connection.OK_STATUS) {
            JSONObject body = new JSONObject(response.body());

            String[] conceptData = parseCommonData(body);
            String name = conceptData[0];
            String vendorID=conceptData[1];
            String menuLocID = conceptData[2];

            //TODO check if property isAsapOrderDisabled relates to high demand

            Vendor vendor = ModelFactory.makeVendor(vendorID, name, null);
            vendor.setName(name);
            vendor.menuLocationID=menuLocID;

            return vendor;
        }
        return null;
    }

    public static ArrayList<Vendor> parseLocations(HttpResponse<String> response){
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

                Menu thisMenu = ModelFactory.makeMenu(menuID, null, null);
                ArrayList<Menu> menus = new ArrayList<>();
                menus.add(thisMenu);

                //get whether this vendor is currently available
                JSONObject availableAt = cur_vendor.getJSONObject("availableAt");
                boolean isAvailable = availableAt.getBoolean("availableNow");

                boolean conceptsAvailable = false; //assume closed if unable to parse
                if (availableAt.has("conceptsAvailableNow")) {
                    conceptsAvailable = availableAt.getBoolean("conceptsAvailableNow");
                } else {
                    System.out.println("Unable to parse concept for " + name + ":\n" + availableAt);
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

    public static Vendor parseVendorConcept(HttpResponse<String> response, String vendorID){
        if (response.statusCode() == Connection.OK_STATUS) {
            JSONArray body = new JSONArray(response.body());

            JSONObject info = body.getJSONObject(0);
            String currentMenuLocID = info.getString("id");

            String vendorName = info.getJSONObject("conceptOptions").getString("displayText");
            boolean isOpen = info.getBoolean("availableNow");

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
            v.menuLocationID = currentMenuLocID;
            return v;
        }
        return null;
    }

    /**
     * Parses data shared by the locations and vendor main api pages
     *
     * @param vendorData object representing a vendor
     * @return array of [vendor name, vendor id, menu id]
     */
    private static String[] parseCommonData(JSONObject vendorData) {
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

    public static String parseMenuID(HttpResponse<String> response){
        if (response.statusCode() == Connection.OK_STATUS) {
            JSONArray body = new JSONArray(response.body());
            JSONObject info= body.getJSONObject(0);

            String menuID=info.getString("id");
            return menuID;

        }
        return null;
    }

}
