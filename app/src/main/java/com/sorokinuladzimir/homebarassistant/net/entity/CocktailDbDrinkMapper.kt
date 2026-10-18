package com.sorokinuladzimir.homebarassistant.net.entity

/**
 * Maps a TheCocktailDB record to the app's [DrinkEntity].
 *
 * Fields that have no equivalent in TheCocktailDB (`color`, `rating`, `isCarbonated`) are
 * left at their defaults; `strTags` is surfaced as tastes and the ingredient list is derived
 * from the `strIngredientN`/`strMeasureN` pairs.
 */
object CocktailDbDrinkMapper {

    private val tagPattern = Regex("^[A-Za-z][A-Za-z0-9 '\\-]{0,23}$")

    fun map(dto: CocktailDbDrink): DrinkEntity = DrinkEntity(
        id = dto.id,
        name = dto.name,
        description = dto.instructions,
        color = null,
        rating = 0,
        isAlcoholic = dto.alcoholic.equals("Alcoholic", ignoreCase = true),
        isCarbonated = false,
        servedIn = dto.glass?.let { Glass(text = it) },
        tastes = sanitizeTags(dto.tags),
        imageUrl = dto.imageUrl,
        ingredients = dto.ingredientLines()
    )

    /**
     * TheCocktailDB tags are free-form and occasionally contain spam/code (e.g. injected
     * `print(...)` snippets), so keep only short, human-readable tags.
     */
    private fun sanitizeTags(tags: String?): List<TasteDto>? {
        if (tags.isNullOrBlank()) return null
        val cleaned = tags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() && tagPattern.matches(it) }
            .distinctBy { it.lowercase() }
            .take(MAX_TAGS)
        return cleaned.takeIf { it.isNotEmpty() }?.map { TasteDto(text = it) }
    }

    private const val MAX_TAGS = 6
}
