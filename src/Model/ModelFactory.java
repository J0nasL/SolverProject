package Model;

import java.util.ArrayList;

/**
 * This class contains static methods for creating instances of classes that extend ModelObject.
 * Because the provided classes do not have public constructors,
 * this is the only way to create an instance of these objects from outside the package
 */
public abstract class ModelFactory{

    public enum models{
        Vendor,Menu,MenuCategory,MenuItem,OptionGroup,OptionItem
    }

    /**
     * Creates an instance of Vendor which can contain Menu instances
     * This object is at the top of the object hierarchy
     *
     * @return the created instance
     */
    public static Vendor makeVendor(String vendorID){
        Vendor v=new Vendor(vendorID);
        return v;
    }

    /**
     * Creates an instance of Menu which can contain MenuCategory instances
     *
     * @return the created instance
     */
    public static Menu makeMenu(String menuID){
        Menu m=new Menu(menuID);
        return m;
    }

    /**
     * Creates an instance of MenuCategory which can contain MenuItem instances
     *
     * @return the created instance
     */
    public static MenuCategory makeMenuCategory(String categoryID){
        MenuCategory c=new MenuCategory(categoryID);
        return c;
    }

    /**
     * Creates an instance of MenuItem which can contain OptionGroup instances
     *
     * @return the created instance
     */
    public static MenuItem makeMenuItem(String itemID){
        MenuItem i=new MenuItem(itemID);
        return i;
    }

    /**
     * Creates an instance of OptionGroup which can contain OptionItem instances
     *
     * @return the created instance
     */
    public static OptionGroup makeOptionGroup(String groupID){
        OptionGroup g=new OptionGroup(groupID);
        return g;
    }

    /**
     * Creates an instance of OptionItem with the provided properties
     * Note that no children are added because this is the lowest item on the hierarchy
     *
     * @return the created instance
     */
    public static OptionItem makeOptionItem(String optionID){
        OptionItem i=new OptionItem(optionID);
        return i;
    }

    //TODO: see if OptionItem needs children to represent sub-options

    private static void addChildren(ModelObject obj, ArrayList<? extends ModelObject> children){
        obj.children.addAll(children);
    }

    public static ModelObject makeSomeObject(String objectID, models model){
        switch (model){
            case Vendor:
                return makeVendor(objectID);
            case Menu:
                return makeMenu(objectID);
            case MenuCategory:
                return makeMenuCategory(objectID);
            case MenuItem:
                return makeMenuItem(objectID);
            case OptionGroup:
                return makeOptionGroup(objectID);
            case OptionItem:
                return makeOptionItem(objectID);
        }
        return null;
    }
}
