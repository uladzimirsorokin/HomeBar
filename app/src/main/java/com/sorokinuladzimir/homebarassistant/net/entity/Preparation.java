package com.sorokinuladzimir.homebarassistant.net.entity;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Preparation {

    @SerializedName("steps")
    private ArrayList<PreparationStep> preparationSteps;

    public ArrayList<PreparationStep> getPreparationSteps() {
        return preparationSteps;
    }

    public void setPreparationSteps(ArrayList<PreparationStep> preparationSteps) {
        this.preparationSteps = preparationSteps;
    }
}
