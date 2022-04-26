package CLI;

import API.API;
import Model.*;
import Storage.Storage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CLI implements Listener<ModelObject, String>{

    public static API api;
    private static Storage storage;
    private static final boolean DEBUG=true;

    public static void main(String[] args) {
        System.out.println("Started CLI");
        CLI inst = new CLI();

        if (DEBUG) {
            System.out.println("DEBUG mode is ON");
            inst.debug();
        } else {
            inst.controller();
        }
        Choice.closeReader();
    }

    private CLI() {
        storage = Storage.getInstance();
        String idStr;
        if (storage.keyExists("id_str")) {
            idStr = storage.load("id_str");
        } else {
            idStr = Choice.getLine("Enter id:");
            storage.save("id_str", idStr);
        }
        String token = API.getToken(idStr);
        if (token == null) {
            token = API.getToken(idStr);
        }
        if (token!=null) {
            System.out.println("Got access token");
            //API.getIDs(token);
            //TODO store ids for later?
            api = API.getInstance(token);
        } else {
            System.out.println("Could not get access token");
        }
    }

    private void controller() {
        while (true) {

            Vendor v = showVendorOptions();
            if (v == null) {
                break;
            }
            System.out.println("Showing data for " + v.getName());

            //TODO find a way to merge vendor objects
            //from multiple method calls
            Vendor data=api.getVendorMain(v.getID());
            if (data != null) {
                data=api.getVendorConcepts(data.getID());
                System.out.println(data);
            }

            //for now, break
            break;

        }
    }

    private void debug(){
        Vendor v = ModelFactory.makeVendor("2164","Midnight Oil", new ArrayList<>());
        //todo the results of these calls are being thrown out
        //need to merge these into the existing vendor object
        api.getVendorMain(v.getID());
        api.getVendorConcepts(v.getID());
        Menu m=v.getCurrentMenu();
        if(m!=null) {
            menuPrint(m);
        }
    }

    /**
     * Gets a list of vendors and their statuses, lets the user choose one
     *
     * @return chosen vendors
     */
    private Vendor showVendorOptions() {
        int numberOffset = 1;
        //TODO: save vendor info and query each vendor individually to remove the need for a call to locations
        ArrayList<Vendor> vendors = api.getLocations();
        if (vendors == null) {
            return null;
        }

        vendors.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        ArrayList<Vendor> openVendors = new ArrayList<>();
        ArrayList<Vendor> closedVendors = new ArrayList<>();
        for (Vendor vendor : vendors) {
            if (vendor.isOpen()) {
                openVendors.add(vendor);
            } else {
                closedVendors.add(vendor);
            }
        }
        System.out.println("Open vendors:");
        vendorPrint(openVendors, numberOffset);
        System.out.println("\nClosed vendors:");
        vendorPrint(closedVendors, numberOffset + openVendors.size());

        while (true) {
            int res = Choice.chooseInt("\nChoose a vendor (" + Choice.ERROR_INT + " to exit):");
            if (res == Choice.ERROR_INT) {
                return null;
            }
            res -= numberOffset;
            if (res < openVendors.size()) {
                return openVendors.get(res);
            } else if (res < openVendors.size() + closedVendors.size()) {
                res -= openVendors.size();
                return closedVendors.get(res);
            }
        }
    }

    private void vendorPrint(ArrayList<Vendor> vendors, int numberOffset) {
        if (vendors.size() == 0) {
            System.out.println("none");
        } else {
            for (int i = 0; i < vendors.size(); i++) {
                System.out.println(i + numberOffset + ": " + vendors.get(i).getName());
            }
        }
    }

    private void menuPrint(Menu m){
        System.out.println("Menu categories:");
        for (ModelObject cat: m.getChildren()) {
            MenuCategory category= (MenuCategory) cat;
            System.out.println("\t"+category);
        }
    }

    private void listenerTest(ModelObject m) {
        m.addListener(this);
        m.testChange();
    }


    /**
     * This just tests whether the model factory arguments are correct
     * If there are no errors then everything is fine
     */
    @Deprecated
    private void modelTest() {
        MenuItem i = ModelFactory.makeMenuItem("0", "Itm");
        ArrayList<MenuItem> items = new ArrayList<>(List.of(i));

        MenuCategory j = ModelFactory.makeMenuCategory("1", "Cat", items);

        Menu m = ModelFactory.makeMenu("2", "Menu", new ArrayList<>(List.of(j)));

        Vendor v = ModelFactory.makeVendor("4", "Vendor", new ArrayList<>(List.of(m)));

        System.out.println(v);
    }


    @Override
    public void update(ModelObject object, String s) {
        System.out.println("Model was updated: \""+s+"\"");
    }
}
