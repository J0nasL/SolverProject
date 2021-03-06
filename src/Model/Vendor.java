package Model;

import API.API;

/**
 * Represents a vendor with at least one Menu
 */
public class Vendor extends ModelObject{

    private Boolean isOpen=null;
    private String currentMenuID=null;
    public String menuLocationID=null;
    //TODO are these needed?
    public String terminalID=null;
    public String profitCenterID=null;


    protected Vendor(String id){
        super(id);
    }

    public Boolean isOpen(){
        return isOpen;
    }

    public void setOpen(boolean isOpen){
        this.isOpen=isOpen;
    }

    public String getCurrentMenuID(){
        return currentMenuID;
    }

    public void setCurrentMenuID(String menuID){
        this.currentMenuID=menuID;
    }

    /**
     * Get the current menu. Returns null if currentMenuID is unset or there are no Menu children.
     *
     * @return current menu instance
     */
    public Menu getCurrentMenu(){
        if (currentMenuID!=null){
            return getMenu(currentMenuID);
        }
        return null;
    }

    private Menu getMenu(String menuID){
        if (children!=null){
            for (ModelObject m: children){
                if (m instanceof Menu){
                    if (m.id.equals(menuID)){
                        return (Menu) m;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString(){
        return "Vendor(isOpen: " + isOpen + ", currentMenuID:" + currentMenuID + ", menuLocationID:" + menuLocationID + ", " + super.toString() + ")";
    }

}
