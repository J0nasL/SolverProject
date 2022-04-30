package Model;

/**
 * Represents an item that can contain OptionGroups
 */
public class MenuItem extends ModelItem{

    protected MenuItem(String id){
        super(id);
    }

    @Override
    public String toString(){
        return "MenuItem(" + super.toString() + ")";
    }
}
