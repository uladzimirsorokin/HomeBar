package sorokinuladzimir.com.homebarassistant.net.entity;

import java.io.Serializable;

/**
 * Created by 1 on 10/18/2016.
 */

public class Glass implements Serializable {

    //6 разновидностей стаканов
    //пока засунул в ресурсы, есть запрос+картинка приходит
    private String text;

    private String name;

    private int image;

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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
