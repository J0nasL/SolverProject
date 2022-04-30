package CLI;

import API.API;
import Model.*;
import Storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class CLI implements Listener<ModelObject, String>{

    public static API api;
    private static Storage storage;
    private static boolean DEBUG=false;
    private String VendorIDs;
    private static final int NUMBER_OFFSET=1;

    public static void main(String[] args){
        System.out.println("Started CLI");
        if(args.length==1){
            if(args[0].equals("true")){
                System.out.println("DEBUG is ON");
                DEBUG=true;
            }
        }
        CLI inst=new CLI();

        if (DEBUG){
            System.out.println("DEBUG mode is ON");
            inst.debug();
        } else{
            inst.controller();
        }
        Choice.closeReader();
    }

    private CLI(){
        storage=Storage.getInstance();
        String idStr;
        if (storage.keyExists("id_str")){
            idStr=storage.load("id_str");
        } else{
            idStr=Choice.getLine("Enter id:");
            storage.save("id_str", idStr);
        }
        String token=API.getToken(idStr);
        if (token==null){
            token=API.getToken(idStr);
        }
        if (token!=null){
            System.out.println("Got access token");
            //API.getIDs(token);
            //TODO store ids for later?
            api=API.getInstance(token);
        } else{
            System.out.println("Could not get access token");
        }
    }

    private void controller(){
        while (true){

            String[][] configInfo=api.getConfig();
            if (configInfo==null){
                break;
            }
            Vendor v=showVendorOptions(configInfo[1]);
            if (v==null){
                break;
            }
            System.out.println("Showing data for " + v.getName());

            //TODO have these calls run at the same time
            //maybe only 1 public method in the api for both?
            Vendor data=api.getVendorMain(v.getID());
            if (data!=null){
                v.mergeModel(data);
            }
            if(v.isOpen()){
                //this method will return an error code if the vendor is closed
                Vendor concept=api.getVendorConcepts(v.getID());
                if (concept!=null){
                    v.mergeModel(concept);
                }
            }
            v.setCurrentMenuID(api.getCurMenuID(v.getID()));
            MenuCategory chosenCategory=showMenuOptions(v);
            if (chosenCategory!=null){
                System.out.println("Showing categories for " + chosenCategory.getName());
                //this will also merge the items into the vendor that owns the category
                MenuCategory itemModel=api.getItems(v, chosenCategory);
                if (itemModel!=null){
                    chosenCategory.mergeModel(itemModel);
                    MenuItem chosenItem=showItemOptions(chosenCategory);
                    if (chosenItem!=null){

                        System.out.println("Chosen item: " + chosenItem.getName());

                        System.out.println(api.getItemOptions(chosenItem));
                    }
                }
            }
            //for now, break
            break;
        }
        System.out.println("Exiting.");
    }

    private void debug(){
        timeTrial();
    }

    private void timeTrial(){
        //compares speed of synchronous calls to every vendor versus one call to 1312 document
        //timing is approximately equal when individual calls are synchronous, and much faster when async
        long groupSum=0;
        long indSum=0;
        int groupTotal=0;
        int indTotal=0;
        String[] vendorIDs=api.getConfig()[1];
        for (int i=0; i < 50; i++){
            boolean isEven=i % 2==0;
            long start=System.currentTimeMillis();
            if (isEven){
                api.getLocations();
            } else{
                api.getLocationsIndividually(vendorIDs);
            }
            long end=System.currentTimeMillis();
            if (isEven){
                groupSum+=end - start;
                groupTotal+=1;
            } else{
                indSum+=end - start;
                indTotal+=1;
            }
            System.out.println("Average lumped: " + (groupSum / groupTotal) + "ms, total=" + (groupTotal));
            if (indTotal > 0){
                System.out.println("Average Individual: " + (indSum / indTotal) + "ms, total=" + (indTotal));
            }
        }
    }

    /**
     * Gets a list of vendors and their statuses, lets the user choose one
     *
     * @return chosen vendors
     */
    private Vendor showVendorOptions(String[] vendorIDs){
        int numberOffset=1;
        //TODO: save vendor info and query each vendor individually to remove the need for a call to locations
        ArrayList<Vendor> vendors=api.getLocations();
        if (vendors==null){
            return null;
        }

        vendors.sort(Vendor::compareTo);
        ArrayList<ModelObject> openVendors=new ArrayList<>();
        ArrayList<ModelObject> closedVendors=new ArrayList<>();
        for (Vendor vendor: vendors){
            if (vendor.isOpen()){
                openVendors.add(vendor);
            } else{
                closedVendors.add(vendor);
            }
        }
        System.out.println("Open vendors:");
        objectPrint(openVendors, numberOffset);
        System.out.println("\nClosed vendors:");
        objectPrint(closedVendors, numberOffset + openVendors.size());

        while (true){
            int res=Choice.chooseInt("\nChoose a vendor (" + Choice.ERROR_INT + " to exit):");
            if (res==Choice.ERROR_INT){
                return null;
            }
            res-=numberOffset;
            if (res < openVendors.size()){
                return (Vendor) openVendors.get(res);
            } else if (res < openVendors.size() + closedVendors.size()){
                res-=openVendors.size();
                return (Vendor) closedVendors.get(res);
            }
        }
    }

    private void objectPrint(ArrayList<ModelObject> objects, int numberOffset){
        if (objects.size()==0){
            System.out.println("none");
        } else{
            for (int i=0; i < objects.size(); i++){
                System.out.println(i + numberOffset + ": " + objects.get(i).getName());
            }
        }
    }

    private Integer getChoice(int numberOffset, int arraySize){
        //TODO refactor showVendorOptions to use this method
        while (true){
            int res=Choice.chooseInt("\nChoose one (" + Choice.ERROR_INT + " to exit):");
            if (res==Choice.ERROR_INT){
                return null;
            }
            res-=numberOffset;
            if (res < arraySize && res >= 0){
                System.out.println();
                return res;
            }
        }
    }

    /**
     * Gets a list of menu categories, lets the user choose one
     *
     * @return chosen category
     */
    private MenuCategory showMenuOptions(Vendor vendor){
        //TODO make numberOffset a static final global somewhere
        Menu curMenu=vendor.getCurrentMenu();
        System.out.println("Current menu: " + curMenu.getName());
        ArrayList<ModelObject> categories=curMenu.getChildren();
        categories.sort(ModelObject::compareTo);
        //TODO sort alphabetically, not by ID

        System.out.println("Categories:");
        objectPrint(categories, NUMBER_OFFSET);
        Integer choice=getChoice(NUMBER_OFFSET, categories.size());
        if (choice==null){
            return null;
        }
        return (MenuCategory) categories.get(choice);
    }

    private MenuItem showItemOptions(MenuCategory category){
        //TODO make numberOffset a static final global somewhere
        System.out.println("Current category: " + category.getName());
        ArrayList<ModelObject> items=category.getChildren();
        items.sort(ModelObject::compareTo);
        //TODO sort alphabetically, not by ID
        System.out.println("Items:");
        objectPrint(items, NUMBER_OFFSET);
        Integer choice=getChoice(NUMBER_OFFSET, items.size());
        if (choice==null){
            return null;
        }
        return (MenuItem) items.get(choice);
    }

    private void listenerTest(ModelObject m){
        m.addListener(this);
        m.testChange();
    }

    /**
     * This just tests whether the model factory arguments are correct
     * If there are no errors then everything is fine
     */
    @Deprecated
    private void modelTest(){
        //also need to test itemoption
        MenuItem i=ModelFactory.makeMenuItem("0", "Itm",null);
        ArrayList<MenuItem> items=new ArrayList<>(List.of(i));

        MenuCategory j=ModelFactory.makeMenuCategory("1", "Cat", items);

        Menu m=ModelFactory.makeMenu("2", "Menu", new ArrayList<>(List.of(j)));

        Vendor v=ModelFactory.makeVendor("4", "Vendor", new ArrayList<>(List.of(m)));

        System.out.println(v);
    }

    @Override
    public void update(ModelObject object, String s){
        System.out.println("Model was updated: \"" + s + "\"");
    }
}
