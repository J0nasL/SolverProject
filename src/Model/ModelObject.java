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
    private String description;
    public ArrayList<ModelObject> children=new ArrayList<>();
    @NotNull
    public final String id;
    @Nullable
    private String name;


    protected ModelObject(@NotNull String id){
        this.id=id;
    }


    /**
     * Returns the name of this object
     */
    public @Nullable String getName(){
        if(name==null){
            return "removed";
        }
        return name;
    }

    /**
     * Sets the name of this object
     */
    public void setName(@Nullable String name){
        this.name=name;
    }


    /**
     * Returns a string representation of the given instance
     */
    @Override
    public String toString(){
        StringBuilder res=new StringBuilder("ModelObject(id:" + id + ", name:" + name + ", description:"+description);
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

    @Override
    public boolean equals(Object o){
        if (o instanceof ModelObject){
            ModelObject other=(ModelObject) o;
            return other.id.equals(this.id);
        }
        return false;
    }

    public int compareTo(ModelObject other){
        assert (other!=null);
        return id.compareTo(other.id);
    }

    public void setDescription(String description){
        this.description=description;
    }

    public String getDescription(){return description;}

    /**
     * Whether the containing class is the lowest on the ModelObject hierarchy.
     * The lowest class cannot contain children.
     * Referred to as atomic because it cannot be subdivided.
     */
    public boolean isAtomic(){
        return false;
    }

    public void addListener(Listener<ModelObject, String> listener){
        this.listeners.add(listener);
    }

    public void testChange(){
        notifyListeners("test change on " + this);
    }
}
