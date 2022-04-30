package Model;

/**
 * Represents a menu that can contain MenuCategories
 */
public class Menu extends ModelObject{
    public String description=null;

    protected Menu(String id){
        super(id);
    }

    @Override
    public String toString(){
        return "Menu(" + super.toString() + ")";
    }
}
