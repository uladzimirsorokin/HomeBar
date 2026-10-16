package com.sorokinuladzimir.homebarassistant.net.entity

/**
 * Maps a TheCocktailDB record to the app's [DrinkEntity].
 *
 * Fields that have no equivalent in TheCocktailDB (`color`, `rating`, `isCarbonated`) are
 * left at their defaults; `strTags` is surfaced as tastes and the ingredient list is derived
 * from the `strIngredientN`/`strMeasureN` pairs.
 */
object CocktailDbDrinkMapper {

    fun map(dto: CocktailDbDrink): DrinkEntity = DrinkEntity(
        id = dto.id,
        name = dto.name,
        description = dto.instructions,
        color = null,
        rating = 0,
        isAlcoholic = dto.alcoholic.equals("Alcoholic", ignoreCase = true),
        isCarbonated = false,
        servedIn = dto.glass?.let { Glass(text = it) },
        tastes = dto.tags
            ?.split(",")
            ?.mapNotNull { it.trim().takeIf(String::isNotEmpty) }
            ?.map { TasteDto(text = it) },
        imageUrl = dto.imageUrl,
        ingredients = dto.ingredientLines()
    )
}
