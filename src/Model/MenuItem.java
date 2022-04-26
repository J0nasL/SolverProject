package Model;

public class MenuItem extends ModelObject {
    protected MenuItem(String id, String name) {
        super(id, name);
    }

    @Override
    public String toString() {
        return "MenuItem(" + super.toString() + ")";
    }
}
