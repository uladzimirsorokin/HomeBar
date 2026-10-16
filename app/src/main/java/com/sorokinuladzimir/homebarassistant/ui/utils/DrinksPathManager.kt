package com.sorokinuladzimir.homebarassistant.ui.utils

class DrinksPathManager private constructor(builder: Builder) {

    val path: String = buildPath(
        builder.drinkId, builder.glassType, builder.taste, builder.rating,
        builder.color, builder.ingredientType, builder.skill,
        builder.isCarbonated, builder.isAlcoholic
    )

    private fun buildPath(
        drinkId: String?, glassType: String?, taste: String?,
        rating: String?, color: String?, ingredientType: String?,
        skill: String?, isCarbonated: String?, isAlcoholic: String?
    ): String {
        val conditionsPath = StringBuilder()
        if (drinkId != null) conditionsPath.append("/").append(drinkId)
        if (glassType != null) conditionsPath.append("/").append(glassType)
        if (taste != null) conditionsPath.append("/").append(taste)
        if (rating != null) conditionsPath.append("/").append(rating)
        if (color != null) conditionsPath.append("/").append(color)
        if (ingredientType != null) conditionsPath.append("/").append(ingredientType)
        if (skill != null) conditionsPath.append("/").append(skill)
        if (isCarbonated != null) conditionsPath.append("/").append(isCarbonated)
        if (isAlcoholic != null) conditionsPath.append("/").append(isAlcoholic)
        return conditionsPath.toString()
    }

    class Builder {

        var drinkId: String? = null
        var glassType: String? = null
        var taste: String? = null
        var rating: String? = null
        var color: String? = null
        var ingredientType: String? = null
        var skill: String? = null
        var isCarbonated: String? = null
        var isAlcoholic: String? = null

        fun setDrinkId(drinkId: String?): Builder = apply {
            this.drinkId = drinkId
        }

        fun setGlassType(glassType: String?): Builder = apply {
            if (glassType != null) this.glassType = "servedin/$glassType"
        }

        fun setTaste(tastes: Array<String>?): Builder = apply {
            if (tastes != null) this.taste = "tasting/" + tastes.joinToString("/and/")
        }

        fun setRating(min: Int, max: Int): Builder = apply {
            this.rating = "rating/gte$min/and/lte$max"
        }

        fun setColor(color: String?): Builder = apply {
            if (color != null) this.color = "colored/$color"
        }

        fun setIngredient(ingredients: Array<String>?): Builder = apply {
            if (ingredients != null) {
                this.ingredientType = "withtype/" + ingredients.joinToString("/and/")
            }
        }

        fun setSkill(skill: Array<String>?): Builder = apply {
            this.skill = "skill/" + skill?.joinToString("/or/").orEmpty()
        }

        fun setCarbonated(carbonated: Boolean): Builder = apply {
            this.isCarbonated = if (carbonated) "carbonated" else "not/carbonated"
        }

        fun setAlcoholic(alcoholic: Boolean): Builder = apply {
            this.isAlcoholic = if (alcoholic) "alcoholic" else "not/alcoholic"
        }

        fun build(): DrinksPathManager = DrinksPathManager(this)
    }
}
