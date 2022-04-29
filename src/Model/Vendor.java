package Model;

/**
 * Represents a vendor with at least one Menu
 */
public class Vendor extends ModelObject {

    private Boolean isOpen=null;
    private String currentMenuID = null;
    public String menuLocationID=null;
    public String terminalID=null;
    public String profitCenterID=null;


    protected Vendor(String id, String name) {
        super(id, name);
    }

    public Boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public String getCurrentMenuID() {
        return currentMenuID;
    }

    public void setCurrentMenuID(String menuID) {
        this.currentMenuID = menuID;
    }

    /**
     * Get the current menu. Returns null if currentMenuID is unset or there are no Menu children.
     *
     * @return current menu instance
     */
    public Menu getCurrentMenu() {
        if (currentMenuID != null) {
            return getMenu(currentMenuID);
        }
        return null;
    }

    private Menu getMenu(String menuID) {
        if (hasChildren()) {
            for (ModelObject m : getChildren()) {
                if (m instanceof Menu){
                    if (m.getID().equals(menuID)) {
                        return (Menu) m;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Vendor(isOpen(): " + isOpen + ", currentMenuID:" + currentMenuID + ", menuLocationID:"+ menuLocationID + ", " + super.toString() + ")";
    }

    @Override
    public void mergeModel(ModelObject o1) {
        assert (o1 instanceof Vendor);
        Vendor v=(Vendor)o1;
        super.mergeModel(o1);
        //merge isOpen
        if(this.isOpen==null){
            this.isOpen=v.isOpen;
        }
        //merge currentMenuID
        if(this.currentMenuID==null){
            this.currentMenuID=v.currentMenuID;
        }
        //merge menuLocationID
        if(this.menuLocationID==null){
            this.menuLocationID=v.menuLocationID;
        }
        if(this.terminalID==null){
            this.terminalID=v.terminalID;
        }
        if(this.profitCenterID==null){
            this.profitCenterID=v.profitCenterID;
        }
    }
}
