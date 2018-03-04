package sorokinuladzimir.com.homebarassistant.db.entity;

import java.io.Serializable;

public class Taste implements Serializable {

    public String id;

    public String text;

    public Taste(String text) {
        this.text = text;
    }

    public Taste() {

    }
}
