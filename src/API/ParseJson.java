package API;

import Model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.print.attribute.standard.JobSheets;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class ParseJson{
    public static ArrayList<Vendor> parseLocations(HttpResponse<String> response){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONArray json=new JSONArray(response.body());

            ArrayList<Vendor> vendors=new ArrayList<>();
            for (Object i: json){
                //for each vendor:
                JSONObject cur_vendor=(JSONObject) i;

                String[] conceptData=parseCommonData(cur_vendor);
                String name=conceptData[0];
                String vendorID=conceptData[1];
                String menuID=conceptData[2];

                JSONObject displayOptions=cur_vendor.getJSONObject("displayOptions");
                String terminalID=displayOptions.getString("onDemandTerminalId");
                String profitCenterID=displayOptions.getString("profit-center-id");

                Menu thisMenu=ModelFactory.makeMenu(menuID, null, null);
                ArrayList<Menu> menus=new ArrayList<>();
                menus.add(thisMenu);

                //get whether this vendor is currently available
                JSONObject availableAt=cur_vendor.getJSONObject("availableAt");
                boolean isAvailable=availableAt.getBoolean("availableNow");

                boolean conceptsAvailable=false; //assume closed if unable to parse
                if (availableAt.has("conceptsAvailableNow")){
                    conceptsAvailable=availableAt.getBoolean("conceptsAvailableNow");
                } else{
                    System.out.println("Unable to parse concept for " + name + ":\n" + availableAt);
                }
                //boolean isAvailable=cur_vendor.getBoolean("storeAvailabeNow"); //this is always true for some reason

                //TODO: figure out how to handle available vs concepts available
                Vendor v=ModelFactory.makeVendor(vendorID, name, menus);
                v.setOpen(isAvailable && conceptsAvailable);
                v.terminalID=terminalID;
                v.profitCenterID=profitCenterID;
                vendors.add(v);
            }
            return vendors;
        }
        return null;
    }

    public static Vendor parseMain(HttpResponse<String> response){
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

            Vendor vendor=ModelFactory.makeVendor(vendorID, name, null);
            vendor.setName(name);
            vendor.menuLocationID=menuLocID;
            vendor.profitCenterID=profitCenterID;
            vendor.terminalID=terminalID;

            return vendor;
        }
        return null;
    }

    public static Vendor parseVendorConcept(HttpResponse<String> response, String vendorID){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONArray body=new JSONArray(response.body());

            //TODO see if any of this is in common with concept data, parse together
            JSONObject info=body.getJSONObject(0);
            String currentMenuLocID=info.getString("id");

            String vendorName=info.getJSONObject("conceptOptions").getString("displayText");
            boolean isOpen=info.getBoolean("availableNow");

            JSONObject conceptOptions=info.getJSONObject("conceptOptions");
            String profitCenterID=conceptOptions.getString("profitCenterId");

            //make Menus
            JSONArray menuJson=info.getJSONArray("menus");
            ArrayList<Menu> menus=new ArrayList<>();
            for (Object i: menuJson){
                JSONObject menu=(JSONObject) i;
                String description=menu.getString("description"); //TODO store this?
                String id=menu.getString("id");
                String name=menu.getString("name");


                //make MenuCategories
                JSONArray categoryJson=menu.getJSONArray("categories");
                ArrayList<MenuCategory> categories=new ArrayList<>();
                for (Object j: categoryJson){
                    JSONObject categoryItem=(JSONObject) j;

                    String categoryID=categoryItem.getString("categoryId");
                    String categoryName=categoryItem.getString("name");
                    //TODO:
                    //what does categoryOptions: {} do?
                    //what does isAvailableToGuests: true do?
                    //what does itemIdToItemPropertiesMap: {} do?


                    //make MenuItems
                    ArrayList<MenuItem> menuItems=new ArrayList<>();
                    JSONArray items=categoryItem.getJSONArray("items");
                    for (Object k: items){
                        String itemID=k.toString();
                        menuItems.add(ModelFactory.makeMenuItem(itemID, null, null));
                    }
                    categories.add(ModelFactory.makeMenuCategory(categoryID, categoryName, menuItems));
                }

                menus.add(ModelFactory.makeMenu(id, name, categories));
            }
            Vendor v=ModelFactory.makeVendor(vendorID, vendorName, menus);
            v.setOpen(isOpen);
            v.menuLocationID=currentMenuLocID;
            v.profitCenterID=profitCenterID;
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

    public static String parseMenuID(HttpResponse<String> response){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONArray body=new JSONArray(response.body());
            JSONObject info=body.getJSONObject(0);

            String menuID=info.getString("id");
            return menuID;

        }
        return null;
    }

    public static ArrayList<MenuItem> parseMenuItems(HttpResponse<String> response){
        if (response.statusCode()==Connection.OK_STATUS){
            JSONArray body=new JSONArray(response.body());
            ArrayList<MenuItem> items=new ArrayList<>();

            for (int i=0; i < body.length(); i++){
                JSONObject object=body.getJSONObject(i);

                //TODO figure out which of these are important
                //very important to make sure items will be sent back to the server as they should be
                //otherwise it might result in incorrect items added to the cart or receipt
                //TODO what is the field attributes for?

                //ids
                String id=object.getString("id");
                String videoID=object.getString("kitchenVideoId");
                assert (id.equals(videoID));
                String itemID=object.getString("itemId"); //not the same as the other two

                //names
                String displayText=object.getString("displayText"); //User-readable name
                String name=object.getString("name"); //Employee-readable name
                //item name shorthands for the reciept
                String videoLabel=object.getString("kitchenVideoLabel");
                String kpText=object.getString("kpText");
                String receiptText=object.getString("receiptText");
                assert (videoLabel.equals(displayText) && videoLabel.equals(kpText) && videoLabel.equals(receiptText));


                //descriptions
                if (object.has("description")){
                    String desc=object.getString("description");
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

                //contains ids of OptionGroups
                JSONArray childGroups=object.getJSONArray("childGroups");

                ArrayList<OptionGroup> optionGroups=new ArrayList<>();

                for(Object obj:childGroups){
                    JSONObject groupJSON=(JSONObject) obj;
                    String groupID=groupJSON.getString("id");
                    optionGroups.add(ModelFactory.makeOptionGroup(groupID,null,null));
                }

                MenuItem curItem=ModelFactory.makeMenuItem(id, displayText, optionGroups);
                curItem.setCookTime(cookTime);
                curItem.setPrice(price);

                items.add(curItem);

            }
            return items;
        }
        return null;
    }

    public static ArrayList<OptionGroup> parseItemOptions(HttpResponse<String> response){
        //todo make this parse the whole item? is that a good idea?
        //calling parseMenuItems() would be easy

        if (response.statusCode()==Connection.OK_STATUS){
            JSONObject body=new JSONObject(response.body());

            //This is where the OptionGroup is stored
            JSONArray childGroups=body.getJSONArray("childGroups");

            ArrayList<OptionGroup> groups=new ArrayList<>();

            for (Object o: childGroups){
                JSONObject groupJson=(JSONObject) o;

                //ids
                String groupID=groupJson.getString("id");
                String groupType=groupJson.getString("groupType"); //TODO make an enum for this
                String gID=groupJson.getString("groupId"); //some 3 digit number, not sure what this is for

                //names
                String groupName=groupJson.getString("name");
                String groupDisplayName=groupJson.getString("displayName");
                String terminalPrompt=groupJson.getString("terminalPrompt");
                assert groupName.equals(terminalPrompt) && terminalPrompt.equals(groupDisplayName);

                int groupMaximum=groupJson.getInt("maximum");
                int groupMinimum=groupJson.getInt("minimum");


                //this is where OptionItems are stored
                JSONArray childItems=groupJson.getJSONArray("childItems");


                ArrayList<OptionItem> items=new ArrayList<>();

                for (Object obj: childItems){
                    JSONObject itemJson=(JSONObject) obj;

                    //ids
                    String itemID=itemJson.getString("id");
                    String iID=itemJson.getString("itemId"); //some 3 digit number, idk what for

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

                    OptionItem curItem=ModelFactory.makeOptionItem(itemID, itemName);
                    curItem.setPrice(itemPrice);
                    curItem.setCookTime(itemCookTime);
                    items.add(curItem);
                }

                OptionGroup curGroup=ModelFactory.makeOptionGroup(groupID, groupName, items);
                groups.add(curGroup);
            }
            return groups;
        }
        return null;
    }
}
