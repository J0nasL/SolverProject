package Model;

import API.API;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an option item
 */
public class OptionItem extends ModelItem{

    protected OptionItem(String id){
        super(id);
    }

    @Override
    public String toString(){
        return "OptionItem(" + super.toString() + ")";
    }
}
