package Model;

/**
 * Represents a vendor with at least one MenuSchedule
 */
public class Vendor extends ModelObject implements Comparable<Vendor> {

    private Boolean isOpen=null;
    private String currentMenuID = null;

    public String menuLocationID=null;


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
        return "Vendor(isOpen(): " + isOpen() + ", currentMenuID:" + currentMenuID + ", "+ super.toString() + ")";
    }

    @Override
    public int compareTo(Vendor other) {
        return getID().compareTo(other.getID());
    }
}
