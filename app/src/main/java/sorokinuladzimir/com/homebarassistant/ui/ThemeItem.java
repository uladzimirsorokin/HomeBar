package sorokinuladzimir.com.homebarassistant.ui;

public class ThemeItem {

    private int id;
    private int color;
    private boolean isSelected;
    private String name;

    public ThemeItem(int id, int color, boolean isSelected, String name) {
        this.id = id;
        this.color = color;
        this.isSelected = isSelected;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
