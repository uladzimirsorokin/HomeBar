package com.sorokinuladzimir.homebarassistant.net.entity;

import java.io.Serializable;

public class Glass implements Serializable {

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
