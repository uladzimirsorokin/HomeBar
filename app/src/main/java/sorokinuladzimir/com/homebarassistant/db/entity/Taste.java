package sorokinuladzimir.com.homebarassistant.db.entity;

import java.io.Serializable;

/**
 * Created by 1 on 10/18/2016.
 */

public class Taste implements Serializable {

    private String id;
    private String text;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
