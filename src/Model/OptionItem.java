package Model;

import API.API;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an option item
 */
public class OptionItem extends ModelItem{
    public static String SELECTED_STR="✓";
    public static String UNAVAILABLE_STR=" (Removed)";
    private boolean selected=false; //whether the user has chosen this option

    public void select(){
        selected=!selected;
    }

    public boolean isSelected(){
        return selected;
    }
    @Override
    public String getName(){
        return super.getName()+(selected?SELECTED_STR:"")+(!getAvailable()?UNAVAILABLE_STR:"");
    }

    protected OptionItem(String id){
        super(id);
    }

    @Override
    public String toString(){
        return "OptionItem(selected:" + selected + ", " + super.toString() + ")";
    }
}
