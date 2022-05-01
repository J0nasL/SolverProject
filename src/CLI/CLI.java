package CLI;

import API.API;
import Model.*;
import Storage.Storage;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.Option;
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
            assert api!=null;
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
        boolean browse=true;

        while (browse){
            Vendor v=chooseVendor(vendors);
            if (v==null){
                break;
            }
            api.getVendorMain(v);
            api.getVendorConcepts(v);
            api.getCurMenuID(v);


            while (browse){
                MenuCategory cat=(MenuCategory) chooseObject(v.getCurrentMenu());
                if (cat==null){
                    break;
                }
                api.getItems(v, cat);
                while (browse){
                    MenuItem i=(MenuItem) chooseObject(cat);
                    if (i==null){
                        break;
                    }
                    api.addToCart(i); //for testing
                    api.getItemOptions(i);
                    while (browse){
                        OptionGroup g=(OptionGroup) chooseObject(i);
                        if(g==null){
                            break;
                        }
                        while (browse){
                            OptionItem o=(OptionItem) chooseObject(g);
                            if(o==null){
                                break;
                            }
                            o.select();
                            //browse=false;
                        }
                    }
                }
            }
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
                String name=objects.get(i).getName();
                System.out.println(i + numberOffset + ": " + name);
            }
        }
    }

    @Nullable
    private Integer getChoice(int numberOffset, int arraySize, int lowBound){
        //TODO refactor showVendorOptions to use this method
        System.out.println();
        while (true){
            int res=Choice.chooseInt("Choose (" + Choice.ERROR_INT + " to exit):");
            if (res==Choice.ERROR_INT){
                return null;
            }
            res-=numberOffset;
            lowBound-=numberOffset;
            if (res < arraySize && res >= lowBound){
                System.out.println();
                return res;
            }
            System.out.println("Invalid selection");
        }
    }

    /**
     * Gets a list of vendors and their statuses, lets the user choose one
     *
     * @return chosen vendors
     */
    @Nullable
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
            Integer res=getChoice(NUMBER_OFFSET, closedVendors.size() + openVendors.size(), NUMBER_OFFSET);
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

    @Nullable
    private ModelObject chooseObject(ModelObject parent){
        boolean canSelect=parent instanceof ModelItem;
        return chooseObject(parent,canSelect);
    }

    @Nullable
    private ModelObject chooseObject(ModelObject parent, boolean canSelect){
        if(parent==null){
            return null;
        }
        System.out.println("Current selection: " + parent.getName());
        if(parent.getDescription()!=null){
            System.out.println(parent.getDescription());
        }
        ArrayList<ModelObject> items=parent.children;
        if (items==null){
            return null;
        }
        items.sort(ModelObject::compareTo);
        //TODO sort alphabetically, not by ID
        if(parent instanceof OptionGroup){
            OptionGroup g=(OptionGroup) parent;
            System.out.println(g.getChoiceText());
        } else{
            System.out.println("Options:");
        }

        int low=NUMBER_OFFSET;
        if(canSelect){
            //show an option for 0
            System.out.println((NUMBER_OFFSET - 1) + ": Select current item");
            low-=1;
        }
        objectPrint(items, NUMBER_OFFSET);
        Integer choice=getChoice(NUMBER_OFFSET, items.size(),low);
        if (choice==null){
            return null;
        }
        if(canSelect && choice==NUMBER_OFFSET-2){
            //if the user
            ModelItem selectable=(ModelItem) parent;
            selectable.select();
            return null;
        }
        return items.get(choice);
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
