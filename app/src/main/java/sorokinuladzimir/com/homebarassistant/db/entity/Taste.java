package sorokinuladzimir.com.homebarassistant.db.entity;

import java.io.Serializable;

public class Taste implements Serializable {

    private String id;

    private String text;

    public Taste() {

    }

    public Taste(String text) {
        this.text = text;
    }

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
}
