package Model;

/**
 * Represents an option group that can contain OptionItems
 */
public class OptionGroup extends ModelItem{

    protected OptionGroup(String id, String name){
        super(id, name);
    }

    @Override
    public String toString(){
        return "OptionGroup(" + super.toString() + ")";
    }
}
