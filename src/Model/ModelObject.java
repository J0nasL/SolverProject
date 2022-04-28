package Model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class ModelObject{
    private final List<Listener<ModelObject, String>> listeners = new LinkedList<>();
    private ArrayList<ModelObject> children = new ArrayList<>();
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
        StringBuilder res= new StringBuilder("ModelObject(id:" + id + ", name:" + name + ", children:{");
        for (int i=0; i<children.size();i++) {
            res.append(children.get(i).toString());
            if(i!=children.size()-1){
                res.append(", ");
            }
        }
        res.append("})");
        return res.toString();
    }

    /**
     * Should be called whenever this ModelObject is updated to inform listeners
     */
    private void notifyListeners(String msg) {
        for (var listener : listeners) {
            listener.update(this, msg);
        }
    }

    /**
     * Merge data from this object into the current object
     *
     * @param o1 Instance to get data from
     */
    public void mergeModel(ModelObject o1){
        //ids must be the same
        assert (o1.id!=null && this.id!=null);
        assert (o1.id.equals(this.id));

        //inherit the shorter name
        if (o1.name!=null){
            if(o1.name.length()<this.name.length()){
                this.name=o1.name;
            }
        }
        //merge new children
        for (ModelObject child:o1.children){
            if(!this.children.contains(child)){
                addChild(child);
            }
        }
    }

    public void addListener(Listener<ModelObject, String> listener) {
        this.listeners.add(listener);
    }

    public void testChange(){
        notifyListeners("test change on "+ this);
    }
}
