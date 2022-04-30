package Model;

import API.API;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class ModelObject implements Comparable<ModelObject>{
    private final List<Listener<ModelObject, String>> listeners=new LinkedList<>();
    public ArrayList<ModelObject> children=new ArrayList<>();
    @NotNull public final String id;
    @Nullable private String name;

    protected ModelObject(@NotNull String id){
        this.id=id;
    }

    /**
     * Returns the name of this object
     */
    public @Nullable String getName(){
        return name;
    }

    /**
     * Sets the name of this object
     */
    public void setName(@Nullable String name){
        this.name=name;
    }

    /**
     * Model view controller? Never heard of it.
     * This method is implemented by every subclass of ModelObject
     * Each subclass makes whatever calls to the provided api instance it needs
     * in order to build its list of children. Note that this is not recursive.
     *
     * @param api API instance
     */
    //TODO
    public /*abstract*/ void forceBuildChildren(@NotNull API api)/*;*/{}

    /**
     * Does the thing, RECURSIVELY
     *
     * @param api API instance
     */
    public void forceBuildRecursive(@NotNull API api){
        forceBuildChildren(api);
        if(children!=null){
            for (ModelObject child: children){
                forceBuildRecursive(api);
            }
        }
    }

    /**
     * Returns a string representation of the given instance
     */
    @Override
    public String toString(){
        StringBuilder res=new StringBuilder("ModelObject(id:" + id + ", name:" + name);
        if (children!=null){
            res.append(", children:{");
            for (int i=0; i < children.size(); i++){
                res.append(children.get(i).toString());

                if (i!=children.size() - 1){
                    res.append(", ");
                }
            }
            res.append("}");
        }
        res.append(")");
        return res.toString();
    }

    /**
     * Should be called whenever this ModelObject is updated to inform listeners
     */
    private void notifyListeners(String msg){
        for (var listener: listeners){
            listener.update(this, msg);
        }
    }

    /**
     * Merge data from this object into the current object
     *
     * @param o1 Instance to get data from
     */
    public void mergeModel(ModelObject o1){
        //TODO remove this, it's too complicated
        Objects.requireNonNull(o1);
        //ids must be the same
        assert (o1.id.equals(this.id));

        //inherit the shorter name
        if (o1.name!=null){
            if (this.name==null || o1.name.length() < this.name.length()){
                this.name=o1.name;
            }
        }
        //merge new children
        if (o1.children!=null){
            for (ModelObject otherChild: o1.children){
                //whether a child with the same ID exists in this object's children
                boolean childFound=false;
                if(children!=null){
                    for (ModelObject child: this.children){
                        if (otherChild.id.equals(child.id)){
                            child.mergeModel(otherChild);
                            childFound=true;
                            break;
                        }
                    }
                }
                if (!childFound){
                    this.children.add(otherChild);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof ModelObject){
            ModelObject other=(ModelObject) o;
            return other.id.equals(this.id);
        }
        return false;
    }

    public int compareTo(ModelObject other){
        assert (other!=null);
        return id.compareTo(other.id);
    }

    public void addListener(Listener<ModelObject, String> listener){
        this.listeners.add(listener);
    }

    public void testChange(){
        notifyListeners("test change on " + this);
    }

    /**
     * Whether the containing class is the lowest on the ModelObject hierarchy.
     * The lowest class cannot contain children.
     * Referred to as atomic because it cannot be subdivided.
     */
    public boolean isAtomic(){
        return false;
    }
}
