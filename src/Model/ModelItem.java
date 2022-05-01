package Model;

import java.util.ArrayList;

/**
 * Represents any class that has the properties of an item, meaning that it is a child of ModelObject,
 * and can be purchased and served through OnDemand.
 */
public abstract class ModelItem extends ModelObject{

    private String price;
    private int cookTime;
    protected ModelItem(String id){
        super(id);
    }
    public static String SELECTED_STR="✓";
    private boolean selected=false; //whether the user has chosen this option

    public void select(){
        selected=!selected;
    }

    public boolean isSelected(){
        return selected;
    }

    @Override
    public String getName(){
        return super.getName()+(selected?SELECTED_STR:"");
    }

    public void setPrice(String amount){
        price=amount;
    }

    public String getPrice(){return price;}

    public void setCookTime(int seconds){
        cookTime=seconds;
    }

    @Override
    public boolean isAtomic(){
        return true;
    }

    @Override
    public String toString(){
        return "ModelItem(price:"+price+", cookTime:"+cookTime+ ", "+ super.toString() + ")";
    }



}
