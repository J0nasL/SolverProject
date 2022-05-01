package Model;

import API.API;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an option group that can contain OptionItems
 */
public class OptionGroup extends ModelObject{
    public int minimum;
    public int maximum;

    protected OptionGroup(String id){
        super(id);
    }

    public boolean isMandatory(){
        return minimum > 0;
    }

    public String getChoiceText(){
        String details="Choose ";
        if(minimum==maximum){
            details+=+minimum;
        } else {
            details+=minimum+" to "+maximum;
        }
        details+=":";
        return details;
    }

    @Override
    public String getName(){
        return super.getName() + (isMandatory() ? "*" : "");
    }

    @Override
    public String toString(){
        return "OptionGroup(" + super.toString() + ")";
    }
}
