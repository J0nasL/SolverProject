package Model;

/**
 * Represents an option group that can contain OptionItems
 */
public class OptionGroup extends ModelObject{

    protected OptionGroup(String id){
        super(id);
    }

    @Override
    public String toString(){
        return "OptionGroup(" + super.toString() + ")";
    }
}
