package API;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class API{

    private static final Connection connection=new Connection();
    private static final HttpRequest.BodyPublisher EMPTY_BODY=HttpRequest.BodyPublishers.ofString("{}");
    private static final HttpRequest.BodyPublisher NO_BODY=HttpRequest.BodyPublishers.noBody();
    private static ConnectionURI uri;

    private final String token;
    private final String[] headers;


    public API(String token, String businessID, String contextID){
        uri=new ConnectionURI(businessID, contextID);
        this.token=token;
        headers=new String[]{"Authorization", "Bearer " + token};
        /*try {
            testConnection();
        } catch (APIException e) {
            System.out.println("Cannot connect to host! Host=" + ConnectionURI.HOSTNAME);
        }*/
    }

    //Statics

    public static void testConnection() throws APIException{
        HttpResponse<String> response=connection.get(ConnectionURI.getURI(ConnectionURI.SITE_DOWN), new String[]{});

        if (response==null || response.statusCode()!=Connection.OK_STATUS){
            throw new APIException("Cannot connect to host " + ConnectionURI.ON_DEMAND);
        }
    }

    public static String getToken(String id_str){
        String[] headers=new String[]{"X-NewRelic-ID", id_str};
        HttpResponse<String> response=connection.put(ConnectionURI.getURI(ConnectionURI.LOGIN), headers);
        if(response==null){
            return null;
        }
        if (response.statusCode()==Connection.OK_STATUS){
            Map<String, List<String>> respHeaders=response.headers().map();
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
    public static String[][] getConfig(String token){
        String[] headers=new String[]{"Authorization", "Bearer " + token};



        HttpResponse<String> response=connection.get(ConnectionURI.getURI(ConnectionURI.CONFIG), headers);

        if(response==null){
            return null;
        }
        if (response.statusCode()==Connection.OK_STATUS){
            JSONObject json=new JSONObject(response.body());

            String businessID=json.get("contextID").toString();
            String contextID=json.get("tenantID").toString();
            JSONArray storeList=(JSONArray) json.get("storeList");
            JSONObject item=(JSONObject) storeList.get(0);
            JSONArray vendors=(JSONArray) item.get("displayProfileId");

            String[] vendorNative=new String[vendors.length()];
            for (int i=0; i < vendors.length(); i++){
                vendorNative[i]=vendors.getString(i);
            }

            return new String[][]{new String[]{businessID, contextID}, vendorNative};
        }
        return null;
    }

    public String[][] getConfig(){
        return getConfig(token);
    }

    public static API getInstance(String token){
        String[][] res=getConfig(token);
        if (res!=null){
            String[] ids=res[0];
            return new API(token, ids[0], ids[1]);
        }
        return null;
    }

    //Instance methods

    public String[] getVendorIDs(){
        String[][] config=getConfig(token);
        if(config==null){return null;}
        return config[1];
    }

    /**
     * Populates provided vendors with data from the locations document.
     * If a vendor exists in the locations document and is not provided in vendors,
     * it will be added to the list.
     * Slow, takes ~7 seconds to return.
     *
     * @param vendors Array of vendors to populate
     */
    public void getLocations(@NotNull ArrayList<Vendor> vendors){
        HttpResponse<String> response=connection.get(ConnectionURI.getURI(uri.locationsBusiness), headers);
        ParseJson.parseLocations(response, vendors);
    }

    /**
     * This method is designed as a way of avoiding access of the locations document.
     * Uses asynchronous requests to retrieve similar data in less time.
     * Only the provided list of vendors will be checked
     *
     * @param vendors Array of vendors to populate
     */
    public void getLocationsIndividually(@NotNull ArrayList<Vendor> vendors){
        //TODO change this to access concepts, because main page has no isOpen
        //if the result is an error, the vendor is not open
        ArrayList<Thread> threads=new ArrayList<>();
        ArrayList<RunnableConnection> connections=new ArrayList<>();

        for (Vendor vendor: vendors){
            RunnableConnection c=new RunnableConnection(
                    RunnableConnection.method.POST,
                    ConnectionURI.getURI(uri.locationMain + "/" + vendor.id),
                    headers,
                    NO_BODY);
            Thread t=new Thread(c);
            connections.add(c);
            threads.add(t);
            t.start();
        }

        try{
            for (int i=0; i < threads.size(); i++){
                threads.get(i).join();
                ParseJson.parseMain(connections.get(i).response, vendors.get(i));
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * Retrieves information from the vendor main api page
     * Gets vendor name and the current menu id
     * Note: Does not set isOpen property
     */
    public void getVendorMain(Vendor vendor){
        HttpResponse<String> response=connection.post(ConnectionURI.getURI(uri.locationMain + "/" + vendor.id), headers,
                NO_BODY); //body
        //TODO
        //i forgot why this comment is here
        ParseJson.parseMain(response,vendor);
    }

    /**
     * Retrieves information from the vendor concepts page
     * Gets availability, name, menus and their categories, itemIDs, and the current menuID
     */
    public void getVendorConcepts(Vendor vendor){
        HttpResponse<String> response=connection.post(ConnectionURI.getURI(uri.locationConcepts + vendor.id), headers,
                EMPTY_BODY);

        ParseJson.parseVendorConcept(response, vendor);
    }

    private JSONArray getPassableMenuData(Vendor vendor){
        //TODO this is an unnecessary extra call to concept page, find a way to reduce calls
        HttpResponse<String> response=connection.post(
                ConnectionURI.getURI(uri.locationConcepts + vendor.id),
                headers,
                EMPTY_BODY);

        if (response.statusCode()==Connection.OK_STATUS){
            return new JSONArray(response.body());
        }
        return null;
    }

    public void getCurMenuID(Vendor vendor){
        JSONArray array=getPassableMenuData(vendor);
        if(array==null){return;}
        JSONObject data=array.getJSONObject(0);

        String menuLocationID=data.getString("id");

        JSONArray menus=data.getJSONArray("menus");
        JSONArray schedules=data.getJSONArray("schedule");
        JSONObject body=new JSONObject();
        body.put("menus", menus);
        body.put("schedule", schedules);
        /*TODO figure out how to put JSON directly into body without having
            to convert to string, since it will probably be converted back to JSON by HttpClient*/
        String bodyStr=body.toString();
        //4 other key,value pairs to consider adding if the server returns a 500 error

        HttpResponse<String> response=connection.post(
                ConnectionURI.getURI(uri.locationConcepts + vendor.id + uri.menuAddon + menuLocationID),
                headers,
                HttpRequest.BodyPublishers.ofString(bodyStr));

        ParseJson.parseMenuID(response, vendor);
    }

    public void getItems(Vendor v, MenuCategory category){
        assert v.getCurrentMenu().children.contains(category); //make sure this category is a child of the given vendor
        //not sure why it wouldn't be, but good to error check regardless

        JSONObject body=new JSONObject();

        JSONArray itemIds=new JSONArray();
        for (ModelObject child: category.children){
            MenuItem item=(MenuItem) child;
            itemIds.put(item.id);
        }
        body.put("itemIds", itemIds);
        body.put("conceptId", v.menuLocationID);

        //These don't seem to be necessary
        //body.put("onDemandTerminalId", v`.terminalID);
        //body.put("profitCenterId", v.profitCenterID);
        //body.put("storePriceLevel", "1");
        //body.put("currencyUnit", "USD");

        HttpResponse<String> response=connection.post(ConnectionURI.getURI(uri.getItems), headers,
                HttpRequest.BodyPublishers.ofString(body.toString()));

        ParseJson.parseMenuItems(response,category);

    }

    public void getItemOptions(MenuItem item){
        //TODO i think i can skip getting the menu item if i just run this page with the item ids from concepts
        HttpResponse<String> response=connection.post(
                ConnectionURI.getURI(uri.getItemInfo + item.id),
                headers,
                EMPTY_BODY);

        ParseJson.parseItemOptions(response, item);
    }

    public void addToCart(MenuItem menuItem){

        JSONObject itemJSON=new JSONObject();
        itemJSON.put("id",menuItem.id);

        JSONObject price=new JSONObject();
        price.put("amount",menuItem.getPrice());
        itemJSON.put("price",price);

        //itemJSON.put("splInstruction",""); //TODO implement special instructions

        JSONArray modifiers=new JSONArray();

        for(ModelObject o:menuItem.children){
            OptionGroup group=(OptionGroup) o;

            for (ModelObject o1:group.children){
                OptionItem item=(OptionItem) o1;

                if(item.isSelected()){
                    JSONObject optionJSON=new JSONObject();

                    optionJSON.put("id", item.id);
                    optionJSON.put("selected", true); //is this ever false?
                    optionJSON.put("amount",item.getPrice());
                    optionJSON.put("parentGroupId",group.id);
                    //TODO any need to add item and option names or video labels?
                    modifiers.put(optionJSON);
                }
            }
        }
        itemJSON.put("selectedModifiers",modifiers);

        HttpResponse<String> response=connection.post(ConnectionURI.getURI(uri.cartAdd), headers,
                HttpRequest.BodyPublishers.ofString(itemJSON.toString()));
    }

    public void getWaitTimes(ArrayList<MenuItem> items){

        assert !items.isEmpty();

        JSONObject reqJSON=new JSONObject();
        JSONArray cartItems=new JSONArray();

        for(MenuItem item: items){

            JSONObject thisItem=new JSONObject();

            /*"childGroups": [],
            "options": [],
            "attributes": []*/
            //TODO add these fields if any of them are non-empty and affect cook time

            thisItem.put("kitchenVideoId",item.id);

            JSONArray childGroups=new JSONArray();
            thisItem.put("childGroups",childGroups);

            cartItems.put(thisItem);
        }

        reqJSON.put("varianceEnabled", true); //whether to return 2 wait times, a min and max
        reqJSON.put("variancePercentage", 10); //how much to skew the min and max away from the wait time

        reqJSON.put("cartItems",cartItems);

        System.out.println(reqJSON);

        HttpResponse<String> response=connection.post(ConnectionURI.getURI(uri.waitTimes), headers,
                HttpRequest.BodyPublishers.ofString(reqJSON.toString()));

        ParseJson.parseWaitTimes(response);

    }

    public void getRevenueCategory(){
        HttpResponse<String> response=connection.get(ConnectionURI.getURI(uri.revenueCategory), headers);

        //ParseJson.parse(response);
    }

}
