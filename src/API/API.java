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

    public String[][] getConfig() {
        return getConfig(token);
    }

    public static API getInstance(String token) {
        String[][] res=getConfig(token);
        if (res!=null) {
            String[] ids = res[0];
            return new API(token, ids[0], ids[1]);
        }
        return null;
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
        return ParseJson.parseLocations(response);
    }

    public ArrayList<Vendor> getLocationsIndividually(String[] vendorIDs) {
        //TODO change this to access concepts, because main page has no isOpen
        //if the result is an error, the vendor is not open
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<RunnableConnection> connections = new ArrayList<>();

        for (String vendorID : vendorIDs) {
            RunnableConnection c = new RunnableConnection(RunnableConnection.method.POST, ConnectionURI.getURI(uri.locationMain + "/" + vendorID), headers,
                    HttpRequest.BodyPublishers.noBody());
            Thread t = new Thread(c);
            connections.add(c);
            threads.add(t);
            t.start();
        }
        ArrayList<Vendor> vendors = new ArrayList<>();
        try {
            for (int i = 0; i < threads.size(); i++) {
                threads.get(i).join();
                vendors.add(ParseJson.parseMain(connections.get(i).response));
            }
            return vendors;
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        //i forgot why this comment is here
        return ParseJson.parseMain(response);
    }

    /**
     * Retrieves information from the vendor concepts page
     * Gets availability, name, menus and its children, and the current menuID
     */
    public Vendor getVendorConcepts(String vendorID) {
        HttpResponse<String> response = connection.post(ConnectionURI.getURI(uri.locationConcepts + vendorID), headers,
                HttpRequest.BodyPublishers.ofString("{}"));

        return ParseJson.parseVendorConcept(response,vendorID);
    }

    private JSONArray getPassableMenuData(String vendorID){
        //TODO this is an unnecessary extra call to concept page, find a way to reduce calls
        HttpResponse<String> response = connection.post(ConnectionURI.getURI(uri.locationConcepts + vendorID), headers,
                HttpRequest.BodyPublishers.ofString("{}"));

        if(response.statusCode()==Connection.OK_STATUS) {
            return new JSONArray(response.body());
        }
        return null;
    }

    public String getCurMenuID(String vendorID){
        JSONObject data=getPassableMenuData(vendorID).getJSONObject(0);
        if (data==null){
            return null;
        }

        String menuLocationID=data.getString("id");

        JSONArray menus=data.getJSONArray("menus");
        JSONArray schedules=data.getJSONArray("schedule");
        JSONObject body=new JSONObject();
        body.put("menus",menus);
        body.put("schedule",schedules);
        //TODO figure out how to put JSON directly into body without having
        //to convert to string, since it will probably be converted back to JSON by HttpClient
        String bodyStr=body.toString();
        //4 other key,value pairs to consider adding if the server returns a 500 error

        HttpResponse<String> response = connection.post(ConnectionURI.getURI(uri.locationConcepts + vendorID + uri.menuAddon + menuLocationID), headers,
                HttpRequest.BodyPublishers.ofString(bodyStr));

        return ParseJson.parseMenuID(response);
    }

    public void getItems(String vendorID, Menu m){
        //TODO refactor all instance methods to take Vendor instead of vendorID?

    }

}
