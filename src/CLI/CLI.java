package CLI;

import API.API;
import Model.*;
import Storage.Storage;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CLI implements Listener<ModelObject, String>{

    public static API api;
    private static Storage storage;
    private static boolean DEBUG=false;
    private String VendorIDs;
    private static final int NUMBER_OFFSET=1;

    public static void main(String[] args){
        System.out.println("Started CLI");
        if (args.length==1){
            if (args[0].equals("true")){
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
        String[][] configInfo=api.getConfig();
        Objects.requireNonNull(configInfo);
        //TODO: save vendor info and query each vendor individually to remove the need for a call to locations
        ArrayList<Vendor> vendors=new ArrayList<>();
        api.getLocations(vendors);

        while (true){
            Vendor v=chooseVendor(vendors);
            if (v==null){
                break;
            } else{
                loop(v);
            }
        }


        /*while (true){


            Vendor v=chooseVendor(vendors);
            if (v==null){
                break;
            }
            System.out.println("Showing data for " + v.getName());

            //TODO have these calls run at the same time
            //maybe only 1 public method in the api for both?
            Vendor data=api.getVendorMain(v.id);
            if (data!=null){
                v.mergeModel(data);
            }
            if(v.isOpen()){
                //this method will return an error code if the vendor is closed
                Vendor concept=api.getVendorConcepts(v.id);
                if (concept!=null){
                    v.mergeModel(concept);
                }
            }
            v.setCurrentMenuID(api.getCurMenuID(v.id));
            MenuCategory chosenCategory=chooseMenu(v);
            if (chosenCategory!=null){
                System.out.println("Showing categories for " + chosenCategory.getName());
                //this will also merge the items into the vendor that owns the category
                MenuCategory itemModel=api.getItems(v, chosenCategory);
                if (itemModel!=null){
                    chosenCategory.mergeModel(itemModel);
                    MenuItem chosenItem=chooseItem(chosenCategory);
                    if (chosenItem!=null){

                        System.out.println("Chosen item: " + chosenItem.getName());

                        System.out.println(api.getItemOptions(chosenItem));
                    }
                }
            }
            //for now, break
            break;
        }*/
        System.out.println("Exiting.");
    }

    private void loop(ModelObject m){
        ModelObject res=chooseObject(m);
        if (res!=null){
            if (res.children!=null){
                loop(res);
            }
            else{
                //TODO
            }
        }
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
                api.getLocations(new ArrayList<>());
            } else{
                api.getLocationsIndividually(new ArrayList<>());
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
     * Gets a list of vendors and their statuses, lets the user choose one
     *
     * @return chosen vendors
     */
    private Vendor chooseVendor(ArrayList<Vendor> vendors){
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
        objectPrint(openVendors, NUMBER_OFFSET);
        System.out.println("\nClosed vendors:");
        objectPrint(closedVendors, NUMBER_OFFSET + openVendors.size());

        while (true){
            Integer res=getChoice(NUMBER_OFFSET, closedVendors.size() + openVendors.size());
            if (res==null){
                return null;
            }
            if (res < openVendors.size()){
                return (Vendor) openVendors.get(res);
            } else if (res < openVendors.size() + closedVendors.size()){
                res-=openVendors.size();
                return (Vendor) closedVendors.get(res);
            }
        }
    }

    private ModelObject chooseObject(ModelObject parent){
        System.out.println("Current selection: " + parent.getName());
        ArrayList<ModelObject> items=parent.children;
        if (items==null){
            return null;
        }
        items.sort(ModelObject::compareTo);
        //TODO sort alphabetically, not by ID
        System.out.println("Options:");
        objectPrint(items, NUMBER_OFFSET);
        Integer choice=getChoice(NUMBER_OFFSET, items.size());
        if (choice==null){
            return null;
        }
        return items.get(choice);
    }

    /**
     * Gets a list of menu categories, lets the user choose one
     *
     * @return chosen category
     */
    private MenuCategory chooseMenu(Vendor vendor){
        return (MenuCategory) chooseObject(vendor);
    }

    private MenuItem chooseItem(MenuCategory category){
        return (MenuItem) chooseObject(category);
    }

    private OptionGroup chooseOptionGroup(MenuItem item){
        return (OptionGroup) chooseObject(item);
    }

    private void listenerTest(ModelObject m){
        m.addListener(this);
        m.testChange();
    }

    @Override
    public void update(ModelObject object, String s){
        System.out.println("Model was updated: \"" + s + "\"");
    }
}
