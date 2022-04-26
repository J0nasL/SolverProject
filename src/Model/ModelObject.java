package Model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class ModelObject {
    private final List<Listener<ModelObject, String>> listeners = new LinkedList<>();
    private ArrayList<ModelObject> children = new ArrayList<ModelObject>();
    private boolean hasChildren = false;
    private final String id;
    private String name;

    protected ModelObject(String id, String name){
        this.id=id;
        this.name=name;
    }

    /**
     * Returns the name of this object
     */
    public String getName(){
        return name;
    }

    /**
     * Sets the name of this object
     */
    public void setName(String name){this.name=name;}

    /**
     * Returns the ID of this object
     */
    public String getID(){
        return id;
    }

    /**
     * Returns whether this item has been initialized with children
     */
    public boolean hasChildren() {
        return hasChildren;
    }

    /**
     * Returns an ArrayList of this object's children
     */
    public ArrayList<ModelObject> getChildren() {
        return children;
    }

    /**
     * Adds a child to this object
     */
    protected void addChild(ModelObject object) {
        //should this be a public method?
        children.add(object);
        hasChildren = true;
    }

    /**
     * Returns a string representation of the given instance
     */
    @Override
    public String toString(){
        String res="ModelObject(id:"+id+", name:"+name+", children:{";
        for (int i=0; i<children.size();i++) {
            res += children.get(i).toString();
            if(i!=children.size()-1){
                res+=", ";
            }
        }
        res+="})";
        return res;
    }

    /**
     * Should be called whenever this ModelObject is updated to inform listeners
     */
    private void notifyListeners(String msg) {
        for (var listener : listeners) {
            listener.update(this, msg);
        }
    }

    public void addListener(Listener<ModelObject, String> listener) {
        this.listeners.add(listener);
    }

    public void testChange(){
        notifyListeners("test change on "+this.toString());
    }
}
