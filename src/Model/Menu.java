package Model;

import API.API;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a menu that can contain MenuCategories
 */
public class Menu extends ModelObject{

    protected Menu(String id){
        super(id);
    }

    @Override
    public String toString(){
        return "Menu(" + super.toString() + ")";
    }
}
