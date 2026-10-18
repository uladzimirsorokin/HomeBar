package com.sorokinuladzimir.homebarassistant.net

/**
 * TheCocktailDB image URLs.
 *
 * Ingredient thumbnails are available at `/images/ingredients/{Name}.png`
 * (optionally with `/small`, `/medium`, `/large`).
 */
object CocktailDbImages {

    private const val INGREDIENT_ROOT = "https://www.thecocktaildb.com/images/ingredients/"

    fun ingredient(name: String?): String? =
        name?.takeIf { it.isNotBlank() }?.let { INGREDIENT_ROOT + it.replace(" ", "%20") + ".png" }
}
