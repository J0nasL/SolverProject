package Model;

import API.API;
import org.jetbrains.annotations.NotNull;

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
