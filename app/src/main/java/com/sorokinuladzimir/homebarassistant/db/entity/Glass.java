package com.sorokinuladzimir.homebarassistant.db.entity;

import java.io.Serializable;

public class Glass implements Serializable {

    private int glassId;

    private String glassName;

    public int getGlassId() {
        return glassId;
    }

    public void setGlassId(int glassId) {
        this.glassId = glassId;
    }

    public String getGlassName() {
        return glassName;
    }

    public void setGlassName(String glassName) {
        this.glassName = glassName;
    }

}
