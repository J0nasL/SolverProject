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
     * Creates an instance of MenuItem which can contain OptionGroup instances
     *
     * @param options children of this object
     * @return the created instance
     */
    public static MenuItem makeMenuItem(String itemID, String itemName, ArrayList<OptionGroup> options){
        MenuItem i=new MenuItem(itemID, itemName);
        addChildren(i,options);
        return i;
    }

    /**
     * Creates an instance of OptionGroup which can contain OptionItem instances
     *
     * @param options children of this object
     * @return the created instance
     */
    public static OptionGroup makeOptionGroup(String groupID, String groupName, ArrayList<OptionItem> options){
        OptionGroup g=new OptionGroup(groupID,groupName);
        addChildren(g,options);
        return g;
    }

    /**
     * Creates an instance of OptionItem with the provided properties
     * Note that no children are added because this is the lowest item on the hierarchy
     *
     * @return the created instance
     */
    public static OptionItem makeOptionItem(String optionID, String optionName){
        //note that this does not add children
        OptionItem i=new OptionItem(optionID,optionName);
        return i;
    }

    //TODO: see if OptionItem needs children to represent sub-options



    private static void addChildren(ModelObject obj, ArrayList<? extends ModelObject> children){
        if (children!=null){
            for (ModelObject c: children){
                obj.addChild(c);
            }
        }
    }
}
