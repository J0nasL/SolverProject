package API;

import Model.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpResponse;
import java.util.ArrayList;

public class ParseJson{

    /**
     * Parses data shared by the locations and vendor main api pages
     *
     * @param vendorData object representing a vendor
     * @return array of [vendor name, vendor id, menu id]
     */
    private static String[] parseCommonData(JSONObject vendorData){
        JSONArray conceptInfo=vendorData.getJSONArray("conceptInfo");
        JSONObject concept=conceptInfo.getJSONObject(0);
        String name=concept.getString("onDemandDisplayText"); //get name
        String menuID=concept.getString("id"); //get menu id

        String vendorID=vendorData.getString("displayProfileId"); //get id

        //when there is a custom name, change it to that
        //TODO: decide which name takes priority
        //currently the custom name wins when enabled
        JSONObject customLocation=(JSONObject) vendorData.get("customizeLocation");
        boolean customizeName=customLocation.getBoolean("featureEnabled");
        if (customizeName){
            name=customLocation.getString("locationName");
        }

        return new String[]{name, vendorID, menuID};
    }

    /**
     * Finds a child object with an ID matching the one provided, or creates one and adds it to the list of children
     *
     * @param objectID ID string of this object
     * @param parent   a parent class extending ModelObject
     * @param model    an enum representing the class of the child.
     *                 This is used to construct a new child instance if needed.
     * @return the corresponding child object, which may have just been initialized
     */
    private static ModelObject getIDMatch(String objectID, ModelObject parent, ModelFactory.models model){
        ModelObject targetObject=null;
        for (Object o: parent.children){
            ModelObject c=(ModelObject) o;
            if (c.id.equals(objectID)){
                targetObject=c;
            }
        }
        if (targetObject==null){
            targetObject=ModelFactory.makeSomeObject(objectID, model);
            parent.children.add(targetObject);
        }
        return targetObject;
    }

    public static void parseLocations(HttpResponse<String> response, ArrayList<Vendor> vendors){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONArray json=new JSONArray(response.body());

            //anonymous method to contain vendor objects
            //TODO make a class above Vendor to contain vendor objects
            ModelObject container=new ModelObject(""){
                private void forceBuildChildren(@NotNull API api, ModelObject parent){

                }
            };
            container.children.addAll(vendors);

            //for each vendor:
            for (Object i: json){
                JSONObject cur_vendor=(JSONObject) i;

                String[] conceptData=parseCommonData(cur_vendor);
                String name=conceptData[0];
                String vendorID=conceptData[1];
                String menuLocationID=conceptData[2];

                JSONObject displayOptions=cur_vendor.getJSONObject("displayOptions");
                String terminalID=displayOptions.getString("onDemandTerminalId");
                String profitCenterID=displayOptions.getString("profit-center-id");


                //make a vendor object
                Vendor targetVendor=(Vendor) getIDMatch(vendorID, container, ModelFactory.models.Vendor);

                //get whether this vendor is currently available
                JSONObject availableAt=cur_vendor.getJSONObject("availableAt");
                boolean isAvailable=availableAt.getBoolean("availableNow");

                //TODO take a look at refactoring this
                boolean conceptsAvailable=false; //assume closed if unable to parse
                if (availableAt.has("conceptsAvailableNow")){
                    conceptsAvailable=availableAt.getBoolean("conceptsAvailableNow");
                } else{
                    System.out.println("Unable to parse concept for " + name + " (online ordering closed for break)");
                }
                //boolean isAvailable=cur_vendor.getBoolean("storeAvailableNow"); //this is always true for some reason TODO I did a spelling fix, see if it changes

                //TODO: figure out how to handle available vs concepts available

                targetVendor.setName(name);
                targetVendor.setOpen(isAvailable && conceptsAvailable);
                targetVendor.menuLocationID=menuLocationID;
                //TODO check if these are needed. if so, see if other pages give this info
                targetVendor.terminalID=terminalID;
                targetVendor.profitCenterID=profitCenterID;
            }
            vendors.clear();
            for (ModelObject o:container.children){
                Vendor v=(Vendor) o;
                vendors.add(v);
            }
        }
    }

    public static void parseMain(HttpResponse<String> response, Vendor vendor){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONObject body=new JSONObject(response.body());

            String[] conceptData=parseCommonData(body);
            String name=conceptData[0];
            String vendorID=conceptData[1];
            String menuLocID=conceptData[2];

            JSONObject displayOptions=body.getJSONObject("displayOptions");
            String profitCenterID=displayOptions.getString("profit-center-id");
            String terminalID=displayOptions.getString("onDemandTerminalId");

            //TODO check if property isAsapOrderDisabled relates to high demand

            vendor.setName(name);
            vendor.menuLocationID=menuLocID;
            vendor.profitCenterID=profitCenterID;
            vendor.terminalID=terminalID;
        }
    }

    public static void parseVendorConcept(HttpResponse<String> response, Vendor vendor){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONArray body=new JSONArray(response.body());

            //TODO see if any of this is in common with concept data, parse together
            JSONObject info=body.getJSONObject(0);
            String currentMenuLocID=info.getString("id");

            String vendorName=info.getJSONObject("conceptOptions").getString("displayText");
            boolean isOpen=info.getBoolean("availableNow");

            JSONObject conceptOptions=info.getJSONObject("conceptOptions");
            String profitCenterID=conceptOptions.getString("profitCenterId");

            JSONArray menuJson=info.getJSONArray("menus");
            //make Menus
            for (Object i: menuJson){
                JSONObject menuJSON=(JSONObject) i;

                String description=menuJSON.getString("description");
                String menuID=menuJSON.getString("id");
                String menuName=menuJSON.getString("name");

                Menu targetMenu=(Menu) getIDMatch(menuID, vendor, ModelFactory.models.Menu);

                JSONArray categoryJson=menuJSON.getJSONArray("categories");
                //make MenuCategories
                for (Object j: categoryJson){
                    JSONObject categoryItem=(JSONObject) j;

                    String categoryID=categoryItem.getString("categoryId"); //this is sometimes empty
                    if(categoryID.isEmpty()){
                        if(categoryItem.has("id")){
                            String cID=categoryItem.getString("id"); //this sometimes is not present
                            assert !categoryID.isEmpty();
                            categoryID=cID;
                        } else {
                            categoryID=menuName;
                        }
                    }
                    //TODO figure out why too few categories are showing up

                    String categoryName=categoryItem.getString("name");

                    //TODO:
                    //what does categoryOptions: {} do?
                    //what does itemIdToItemPropertiesMap: {} do?

                    MenuCategory targetCategory=(MenuCategory) getIDMatch(categoryID, targetMenu, ModelFactory.models.MenuCategory);

                    JSONArray items=categoryItem.getJSONArray("items");
                    //make MenuItems
                    for (Object k: items){
                        String itemID=k.toString();

                        MenuItem targetItem=(MenuItem) getIDMatch(itemID, targetCategory, ModelFactory.models.MenuItem);

                    }

                    targetCategory.setName(categoryName);

                }

                targetMenu.setName(menuName);
                targetMenu.setDescription(description);
            }

            vendor.setOpen(isOpen);
            vendor.menuLocationID=currentMenuLocID;
            vendor.profitCenterID=profitCenterID;
            vendor.setName(vendorName);
        }
    }

    public static void parseMenuID(HttpResponse<String> response, Vendor vendor){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONArray body=new JSONArray(response.body());
            JSONObject info=body.getJSONObject(0);

            String menuID=info.getString("id");
            vendor.setCurrentMenuID(menuID);

        }
    }

    public static void parseMenuItems(HttpResponse<String> response, MenuCategory category){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONArray body=new JSONArray(response.body());

            for (int i=0; i < body.length(); i++){
                JSONObject object=body.getJSONObject(i);

                //TODO figure out which of these are important
                //very important to make sure items will be sent back to the server as they should be
                //otherwise it might result in incorrect items added to the cart or receipt
                //TODO what is the field attributes for?

                //ids
                String itemID=object.getString("id"); //the id that is given by the concepts item page
                if(object.has("kitchenVideoId")){
                    String videoID=object.getString("kitchenVideoId");
                    assert (itemID.equals(videoID));
                }
                String iID=object.getString("itemId"); //a different id

                //names
                String displayText=object.getString("displayText"); //User-readable name
                String name=object.getString("name"); //Employee-readable name
                //item name shorthands for the receipt
                String videoLabel=object.getString("kitchenVideoLabel");
                String kpText=object.getString("kpText");
                String receiptText=object.getString("receiptText");
                assert (videoLabel.equals(displayText) && videoLabel.equals(kpText) && videoLabel.equals(receiptText));


                //descriptions
                String desc=null;
                if (object.has("description")){
                    desc=object.getString("description");
                    String longDesc=object.getString("longDescription");
                    assert (desc.equals(longDesc));
                }

                //cost
                JSONObject priceInfo=object.getJSONObject("price");
                String price=object.getString("amount");

                //cook time
                int cookTime=object.getInt("kitchenCookTimeSeconds");

                //is available
                boolean isDeleted=object.getBoolean("isDeleted");
                assert (!isDeleted);

                MenuItem targetItem=(MenuItem) getIDMatch(itemID, category, ModelFactory.models.MenuItem);

                //contains ids of OptionGroups
                JSONArray childGroups=object.getJSONArray("childGroups");

                for (Object obj: childGroups){
                    JSONObject groupJSON=(JSONObject) obj;
                    String groupID=groupJSON.getString("id");

                    OptionGroup targetGroup=(OptionGroup) getIDMatch(groupID, targetItem, ModelFactory.models.OptionGroup);
                }

                targetItem.setName(displayText);
                targetItem.setCookTime(cookTime);
                targetItem.setPrice(price);
                targetItem.setDescription(desc);
            }
        }
    }

    public static void parseItemOptions(HttpResponse<String> response, MenuItem menuItem){
        //todo make this parse the whole item? is that a good idea?
        //calling parseMenuItems() would be easy

        if (response.statusCode()==Connection.OK_STATUS){
            JSONObject body=new JSONObject(response.body());

            //This is where the OptionGroup is stored
            JSONArray childGroups=body.getJSONArray("childGroups");

            for (Object o: childGroups){
                JSONObject groupJson=(JSONObject) o;

                //ids
                String groupID=groupJson.getString("id");
                String groupType=groupJson.getString("groupType"); //TODO make an enum for this
                String gID=groupJson.getString("groupId"); //some 3-digit number, not sure what this is for

                //names
                String groupName=groupJson.getString("name");
                String groupDisplayName=groupJson.getString("displayName");
                String terminalPrompt=groupJson.getString("terminalPrompt");
                assert groupName.equals(terminalPrompt) &&
                        terminalPrompt.equals(groupDisplayName);

                String groupDesc=null;
                if(groupJson.has("description")){
                    groupDesc=groupJson.getString("description");
                }

                int groupMinimum=groupJson.getInt("minimum");
                int groupMaximum=groupJson.getInt("maximum");

                OptionGroup targetGroup=(OptionGroup) getIDMatch(groupID, menuItem, ModelFactory.models.OptionGroup);
                targetGroup.setName(groupName);
                targetGroup.maximum=groupMaximum;
                targetGroup.minimum=groupMinimum;


                //this is where OptionItems are stored
                JSONArray childItems=groupJson.getJSONArray("childItems");

                for (Object obj: childItems){
                    JSONObject itemJson=(JSONObject) obj;

                    //ids
                    String itemID=itemJson.getString("id");
                    String iID=itemJson.getString("itemId"); //some 3-digit number, I don'y know what for

                    //names
                    String itemName=itemJson.getString("name");
                    String itemDisplayText=itemJson.getString("displayText");
                    String itemKitchenDisplayText=itemJson.getString("kitchenDisplayText");
                    String itemKitchenVideoLabel=itemJson.getString("kitchenVideoLabel");
                    String itemKpText=itemJson.getString("kpText");
                    String itemReceiptText=itemJson.getString("receiptText");
                    assert (itemName.equals(itemDisplayText) &&
                            itemName.equals(itemKitchenDisplayText) &&
                            itemName.equals(itemKitchenVideoLabel) &&
                            itemName.equals(itemKpText) &&
                            itemName.equals(itemReceiptText)
                    );

                    int itemCookTime=itemJson.getInt("kitchenCookTimeSeconds");

                    String itemType=itemJson.getString("itemType"); //TODO make enum

                    JSONObject priceObj=itemJson.getJSONObject("price");
                    String itemPrice=priceObj.getString("amount");

                    OptionItem targetItem=(OptionItem) getIDMatch(itemID, targetGroup, ModelFactory.models.OptionItem);

                    targetItem.setPrice(itemPrice);
                    targetItem.setCookTime(itemCookTime);
                    targetItem.setName(itemName);
                }

                targetGroup.setName(groupName);
                targetGroup.setDescription(groupDesc);
            }
        }
    }

    public static void parseCartAdd(HttpResponse<String> response){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONObject body=new JSONObject(response.body());

            //TODO figure out what of this is useful
            JSONObject details= body.getJSONObject("orderDetails");

            String orderID=details.getString("orderID");
            String orderNumber=details.getString("orderState");
            String orderState=details.getString("orderNumber");
            assert orderState=="OPEN";

        }
    }
}
