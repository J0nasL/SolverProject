package Model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents an item added to a virtual shopping cart. This is not a part of the ModelObject hierarchy.
 * The purpose of CartItem is to represent an item and its selected options, that way if options in the ModelObject hierarchy
 * are selected or deselected later, CartItem keeps an unchanging record of which options were selected at the time of its initialization.
 * Think of this as similar in purpose to an array deep-copy.
 */
public class CartItem{
    public final MenuItem item;
    public final HashMap<OptionGroup, ArrayList<OptionItem>> savedChildren=new HashMap<>();

    /**
     * Create a cart item using the provided item
     * This will store the item and a record any currently selected options for later
     * Note that any groups without selected items will not be stored by this instance
     *
     * @param menuItem The item corresponding to this cart item
     */
    public CartItem(MenuItem menuItem){
        this.item=menuItem;

        //for every group in this item's children,
        //add groups with at least one selected option in them
        //to the <group, list<option>> map
        for (ModelObject obj: item.children){
            OptionGroup group=(OptionGroup) obj;

            ArrayList<OptionItem> selectedChildren=new ArrayList<>();

            for (ModelObject obj2: group.children){
                OptionItem option=(OptionItem) obj2;

                if (option.isSelected()){
                    selectedChildren.add(option);
                }
            }

            if (!selectedChildren.isEmpty()){
                savedChildren.put(group, selectedChildren);
            }
        }
    }

    @Override
    public String toString(){
        StringBuilder b=new StringBuilder();
        String res;

        if (savedChildren.isEmpty()){
            res="";

        } else{
            boolean firstGroup=true;
            for (OptionGroup group: savedChildren.keySet()){
                b.append("<").append(group).append(", [");

                if (firstGroup){
                    firstGroup=false;
                } else{
                    b.append(", ");
                }

                boolean firstItem=true;

                for (ModelItem item: savedChildren.get(group)){
                    if (firstItem){
                        firstItem=false;
                    } else{
                        b.append(", ");
                    }
                    b.append(item);
                }
                b.append("]").append(">");
            }
            res=b.toString();
        }
        return "CartItem{item:" + item.toString() + ", savedChildren: HashMap[" + res + "]" + "}";
    }

    /**
     * Returns a human-readable string of selected options and their parent groups, either in the format of
     * <pre><code>group:
     * option1
     * option2
     * ...</code></pre>
     * or, if compact,
     * <pre><code>group: option1, option2, ...</code></pre>
     *
     * @param compact whether to collapse each group and set of child options into one line
     * @return string representation of selected objects
     */
    public String getContents(boolean compact){

        //TODO sort these
        StringBuilder b=new StringBuilder();

        for (OptionGroup group: savedChildren.keySet()){
            b.append(group.getName());//.append(": ");

            boolean firstItem=false;
            for (OptionItem item: savedChildren.get(group)){
                if (compact){
                    if(firstItem){
                        firstItem=false;
                    } else {
                        b.append(", ");
                    }
                    b.append(item.getName());
                } else{
                    b.append("\n\t");
                    b.append(item.getName());
                }
            }
            b.append("\n");
        }
        return b.toString();
    }
}
