package CLI;

import API.API;
import Model.*;
import Storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Objects;

public class CLI implements Listener<ModelObject, String>{

    private String VendorIDs;
    private final ArrayList<CartItem> cart=new ArrayList<>();

    private static API api;
    private static Storage storage;
    private static boolean DEBUG=false;
    private static final int NUMBER_OFFSET=1;
    private static boolean accessClosedVendor=false;
    public enum State{
        BROWSE, BACK, QUIT, CART, CHECKOUT;

    }

    private State curState=State.BROWSE;

    //TODO add option to favorite/pin modelitems to have them appear first in the list

    public static void main(String[] args){
        System.out.println("Started CLI");
        if (args.length==1){
            if (args[0].equals("true")){
                System.out.println("DEBUG is ON");
                DEBUG=true;
            }
        }
        CLI inst=new CLI();
        if(api==null){
            return;
        }

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
            System.out.println("Could not get access token");
        } else{
            System.out.println("Got access token");
            //API.getIDs(token);
            //TODO store ids for later?
            api=API.getInstance(token);
            assert api!=null;
        }
    }

    private boolean isBrowsing(){
        if (curState==State.BACK){
            curState=State.BROWSE;
            return false;
        }
        return curState==State.BROWSE;
    }

    private void controller(){
        while (true){
            switch (curState){
                case BROWSE -> browsing();
                case CHECKOUT -> checkout();
                case BACK -> browsing(); //if user hit back at vendor level
                case CART -> cart();
                case QUIT -> {
                    System.out.println("Exiting.");
                    return;
                }
            }
        }
    }

    private void browsing(){
        String[][] configInfo=api.getConfig();
        Objects.requireNonNull(configInfo);
        //TODO: save vendor info and query each vendor individually to remove the need for a call to locations
        ArrayList<Vendor> vendors=new ArrayList<>();
        api.getLocations(vendors);

        while (isBrowsing()){
            Vendor v=chooseVendor(vendors);
            if (v==null){
                continue;
            }
            api.getVendorMain(v);
            if (v.isOpen()){
                api.getVendorConcepts(v);
            }
            api.getCurMenuID(v);


            while (isBrowsing()){
                MenuCategory cat=(MenuCategory) chooseObject(v.getCurrentMenu());
                if (cat==null){
                    continue;
                }
                api.getItems(v, cat);
                while (isBrowsing()){
                    MenuItem i=(MenuItem) chooseObject(cat);
                    if (i==null){
                        continue;
                    }
                    api.getItemOptions(i);
                    while (isBrowsing()){
                        OptionGroup g=(OptionGroup) chooseObject(i);
                        if (g==null){
                            continue;
                        }
                        while (isBrowsing()){
                            OptionItem o=(OptionItem) chooseObject(g);
                            if (o==null){
                                continue;
                            }
                            o.select();
                        }
                    }
                }
            }
        }
    }

    private void cart(){
        while(curState==State.CART){
            if(cart.isEmpty()){
                System.out.println("Cart is empty\n");
                curState=State.BROWSE;
            } else{
                System.out.println("\nCart:");
                System.out.println("Select an item to remove");
                System.out.println("Enter " + Choice.CHECKOUT + " to continue to checkout");

                Integer choice=chooseCartItemIndex(cart);

                if (choice!=null){
                    //remove from cart
                    cart.remove((int)choice);
                }
            }
        }
    }

    private void checkout(){
        System.out.println("\nCheckout");
        boolean isPrompting=true;
        while(isPrompting){
            String fname=Choice.getLine("Enter first name");
            String lname=Choice.getLine("Enter first character of last name");

            if(!fname.isEmpty() && lname.length()==1){
                isPrompting=false;
            } else {
                System.out.println("Invalid name");
            }
        }
        isPrompting=true;
        while(isPrompting){
            //TODO replace with shibboleth query
            int id=Choice.chooseInt("Enter student UUID");

            if(id!=Choice.ERROR_INT && id>0){
                isPrompting=false;
            } else {
                System.out.println("Invalid UUID");
            }
        }

        //TODO continue checkout flow here

        if(curState==State.BACK){
            curState=State.CART;
        } else {
            curState=State.QUIT;
        }
    }

    private void objectPrint(ArrayList<ModelObject> objects, int numberOffset){
        if (objects.size()==0){
            System.out.println("None");
        } else{
            for (int i=0; i < objects.size(); i++){
                String name=objects.get(i).getName();
                System.out.println(i + numberOffset + ": " + name);
            }
        }
    }

    private Integer handleChoice(ModelObject parent, int startNum, int endNum){
        //TODO refactor showVendorOptions to use this method
        System.out.println();
        Choice res=Choice.getChoice();

        switch (res.curChoice){
            case ADD_CART:
                if (parent instanceof MenuItem){
                    MenuItem item=(MenuItem) parent;
                    api.addToCart(item);
                    cart.add(new CartItem(item));
                    //TODO quantity option
                    System.out.println("Added" + parent.getName() + "to cart");
                } else{
                    System.out.println("Cannot add to cart: not an item");
                }
                return null;

            case BACK:
                curState=State.BACK;
                return null;

            case HELP:
                Choice.printHelp();
                return null;

            case QUIT:
                curState=State.QUIT;
                return null;

            case CHECKOUT:
                if(curState==State.CART){
                    curState=State.CHECKOUT;
                } else {
                    System.out.println("Need to view cart before checkout");
                }
                return null;

            case VIEW_CART:
                curState=State.CART;
                return null;

            case INT:
                int choiceNum=res.intChoice;
                choiceNum-=startNum;
                if (choiceNum < endNum && res.intChoice >= startNum){
                    System.out.println();
                    return choiceNum;
                } else{
                    System.out.println(Choice.INVALID_STR);
                }
                return null;
        }
        return null;
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
        if(accessClosedVendor){
            objectPrint(closedVendors, NUMBER_OFFSET + openVendors.size());
        }

        Integer res;
        while (true){
            if(accessClosedVendor){
                res=handleChoice(null, NUMBER_OFFSET, closedVendors.size() + openVendors.size());
            } else {
                res=handleChoice(null, NUMBER_OFFSET, openVendors.size());
            }
            if (res==null){
                return null;
            }
            if (res < openVendors.size()){
                return (Vendor) openVendors.get(res);
            } else if (accessClosedVendor && res < openVendors.size() + closedVendors.size()){
                res-=openVendors.size();
                return (Vendor) closedVendors.get(res);
            }
        }
    }

    @Nullable
    private ModelObject chooseObject(ModelObject parent){
        if (parent==null){
            return null;
        }
        System.out.println("Current selection: " + parent.getName());
        if (parent.getDescription()!=null){
            System.out.println(parent.getDescription());
        }
        ArrayList<ModelObject> items=parent.children;
        if (items==null){
            return null;
        }
        items.sort(ModelObject::compareTo);
        //TODO sort alphabetically, not by ID
        if (parent instanceof OptionGroup){
            OptionGroup g=(OptionGroup) parent;
            System.out.println(g.getChoiceText());
        } else{
            System.out.println("Options:");
        }

        objectPrint(items, NUMBER_OFFSET);
        Integer choice=handleChoice(parent, NUMBER_OFFSET, items.size() + NUMBER_OFFSET);
        if (choice==null){
            return null;
        }
        return items.get(choice);
    }

    @Nullable
    private Integer chooseCartItemIndex(@NotNull ArrayList<CartItem> items){
        //this loop functions like objectPrint
        for (int i=NUMBER_OFFSET; i < items.size()+NUMBER_OFFSET-1; i++){
            CartItem item= items.get(i);
            System.out.println(i+": "+item.getContents(false));
            //System.out.println(item); //debug
        }

        return handleChoice(null, NUMBER_OFFSET, items.size() + NUMBER_OFFSET);
    }

    private void debug(){
        ArrayList<MenuItem> items=new ArrayList<>();
        items.add(ModelFactory.makeMenuItem("5f209ba1a3bbc4000def7721"));
        api.getWaitTimes(items);
        //timeTrial();
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

    private void listenerTest(ModelObject m){
        m.addListener(this);
        m.testChange();
    }

    @Override
    public void update(ModelObject object, String s){
        System.out.println("Model was updated: \"" + s + "\"");
    }
}
