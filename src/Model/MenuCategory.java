package Model;

import API.API;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a menu category that can contain MenuItems
 */
public class MenuCategory extends ModelObject{
    protected MenuCategory(String id){
        super(id);
    }

    @Override
    public String toString(){
        return "MenuCategory(" + super.toString() + ")";
    }
}
