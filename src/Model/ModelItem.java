package Model;

import java.util.ArrayList;

/**
 * Represents any class that has the properties of an item, meaning that it is a child of ModelObject,
 * and can be purchased and served through OnDemand.
 */
public abstract class ModelItem extends ModelObject{

    private String description;
    private String price;
    private int cookTime;

    protected ModelItem(String id){
        super(id);
    }

    public void setPrice(String amount){
        price=amount;
    }

    public void setCookTime(int seconds){
        cookTime=seconds;
    }

    public void setDescription(String description){
        this.description=description;
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
