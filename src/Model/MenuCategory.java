package Model;

public class MenuCategory extends ModelObject{
    protected MenuCategory(String id, String name){
        super(id, name);
    }

    @Override
    public String toString(){
        return "MenuCategory(" + super.toString() + ")";
    }
}
