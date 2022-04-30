package Model;

/**
 * Represents a menu that can contain MenuCategories
 */
public class Menu extends ModelObject{
    protected Menu(String id, String name){
        super(id, name);
    }

    @Override
    public String toString(){
        return "Menu(" + super.toString() + ")";
    }
}
