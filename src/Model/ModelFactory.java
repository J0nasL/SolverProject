package Model;

import java.util.ArrayList;

/**
 * This class contains static methods for creating instances of classes that extend ModelObject.
 * Because the provided classes do not have public constructors,
 * this is the only way to create an instance of these objects from outside the package
 */
public abstract class ModelFactory{

    /**
     * Creates an instance of Vendor which can contain Menu instances
     * This object is at the top of the object hierarchy
     *
     * @param menus children of this object
     * @return the created instance
     */
    public static Vendor makeVendor(String vendorID, String vendorName, ArrayList<Menu> menus){
        Vendor v=new Vendor(vendorID, vendorName);
        addChildren(v, menus);
        return v;
    }

    /**
     * Creates an instance of Menu which can contain MenuCategory instances
     *
     * @param categories children of this object
     * @return the created instance
     */
    public static Menu makeMenu(String menuID, String menuName, ArrayList<MenuCategory> categories){
        Menu m=new Menu(menuID, menuName);
        addChildren(m, categories);
        return m;
    }

    /**
     * Creates an instance of MenuCategory which can contain MenuItem instances
     *
     * @param items children of this object
     * @return the created instance
     */
    public static MenuCategory makeMenuCategory(String categoryID, String categoryName, ArrayList<MenuItem> items){
        MenuCategory c=new MenuCategory(categoryID, categoryName);
        addChildren(c, items);
        return c;
    }

    /**
     * Creates an instance of MenuItem with the provided properties
     * Note that no children are added because this is the lowest item on the hierarchy
     *
     * @return the created item
     */
    public static MenuItem makeMenuItem(String itemID, String itemName){
        //TODO: add more values like price
        //note that this does not add children
        return new MenuItem(itemID, itemName);
    }

    //TODO: MenuItem needs children to represent options for the items

    private static void addChildren(ModelObject obj, ArrayList<? extends ModelObject> children){
        if (children!=null){
            for (ModelObject c: children){
                obj.addChild(c);
            }
        }
    }
}
