package Model;

/**
 * Represents an option item
 */
public class OptionItem extends ModelItem{
    protected OptionItem(String id, String name){
        super(id, name);
    }

    @Override
    public String toString(){
        return "OptionItem(" + super.toString() + ")";
    }
}
