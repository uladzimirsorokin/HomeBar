package com.sorokinuladzimir.homebarassistant.ui.utils;


import android.text.TextUtils;

public final class DrinksPathManager {

    private final String drinkId;
    private final String glassType;
    private final String taste;
    private final String rating;
    private final String color;
    private final String ingredientType;
    private final String skill;
    private final String isCarbonated;
    private final String isAlcoholic;

    private final String path;

    private DrinksPathManager(Builder builder) {
        this.drinkId = builder.drinkId;
        this.glassType = builder.glassType;
        this.taste = builder.taste;
        this.rating = builder.rating;
        this.color = builder.color;
        this.ingredientType = builder.ingredientType;
        this.skill = builder.skill;
        this.isCarbonated = builder.isCarbonated;
        this.isAlcoholic = builder.isAlcoholic;

        this.path = buildPath(builder.drinkId, builder.glassType, builder.taste, builder.rating,
                builder.color, builder.ingredientType, builder.skill, builder.isCarbonated, builder.isAlcoholic);
    }

    private String buildPath(String drinkId, String glassType, String taste,
                             String rating, String color, String ingredientType,
                             String skill, String isCarbonated, String isAlcoholic) {
        StringBuilder conditionsPath = new StringBuilder();
        if (drinkId != null) {
            conditionsPath.append("/").append(drinkId);
        }
        if (glassType != null) {
            conditionsPath.append("/").append(glassType);
        }
        if (taste != null) {
            conditionsPath.append("/").append(taste);
        }

        if (rating != null) {
            conditionsPath.append("/").append(rating);
        }
        if (color != null) {
            conditionsPath.append("/").append(color);
        }
        if (ingredientType != null) {
            conditionsPath.append("/").append(ingredientType);
        }
        if (skill != null) {
            conditionsPath.append("/").append(skill);
        }
        if (isCarbonated != null) {
            conditionsPath.append("/").append(isCarbonated);
        }
        if (isAlcoholic != null) {
            conditionsPath.append("/").append(isAlcoholic);
        }
        return conditionsPath.toString();
    }

    public String getDrinkId() {
        return drinkId;
    }

    public String getGlassType() {
        return glassType;
    }

    public String getTaste() {
        return taste;
    }

    public String getRating() {
        return rating;
    }

    public String getColor() {
        return color;
    }

    public String getIngredientType() {
        return ingredientType;
    }

    public String getSkill() {
        return skill;
    }

    public String getPath() {
        return path;
    }

    /**
     * The builder class.
     */
    public static class Builder {

        private String drinkId;
        private String glassType;
        private String taste;
        private String rating;
        private String color;
        private String ingredientType;
        private String skill;
        private String isCarbonated;
        private String isAlcoholic;

        public Builder setDrinkId(String drinkId) {
            this.drinkId = drinkId;
            return this;
        }

        public Builder setGlassType(String glassType) {
            if (glassType != null) this.glassType = "servedin/" + glassType;
            return this;
        }

        public Builder setTaste(String[] tastes) {
            if (tastes != null) this.taste = "tasting/" + TextUtils.join("/and/", tastes);
            return this;
        }

        public Builder setRating(int min, int max) {
            this.rating = "rating/gte" + min + "/and/lte" + max;
            return this;
        }

        public Builder setColor(String color) {
            if (color != null) this.color = "colored/" + color;
            return this;
        }

        public Builder setIngredient(String[] ingredients) {
            if (ingredients != null)
                this.ingredientType = "withtype/" + TextUtils.join("/and/", ingredients);
            return this;
        }

        public Builder setSkill(String[] skill) {
            this.skill = "skill/" + TextUtils.join("/or/", skill);
            return this;
        }

        public Builder setCarbonated(Boolean carbonated) {
            if (carbonated) {
                this.isCarbonated = "carbonated";
            } else {
                this.isCarbonated = "not/carbonated";
            }
            return this;
        }

        public Builder setAlcoholic(Boolean alcoholic) {
            if (alcoholic) {
                this.isAlcoholic = "alcoholic";
            } else {
                this.isAlcoholic = "not/alcoholic";
            }
            return this;
        }

        public DrinksPathManager build() {
            return new DrinksPathManager(this);
        }
    }
}
