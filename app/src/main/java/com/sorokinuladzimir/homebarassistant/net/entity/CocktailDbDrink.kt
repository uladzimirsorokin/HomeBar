package com.sorokinuladzimir.homebarassistant.net.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A cocktail record from TheCocktailDB. The free endpoints return the same shape with
 * different subsets of fields populated: `search.php`/`lookup.php` return full records,
 * while `filter.php` returns only `idDrink`/`strDrink`/`strDrinkThumb` (summaries).
 */
@Serializable
class CocktailDbDrink(
    @SerialName("idDrink") val id: String? = null,
    @SerialName("strDrink") val name: String? = null,
    @SerialName("strCategory") val category: String? = null,
    @SerialName("strAlcoholic") val alcoholic: String? = null,
    @SerialName("strGlass") val glass: String? = null,
    @SerialName("strInstructions") val instructions: String? = null,
    @SerialName("strTags") val tags: String? = null,
    @SerialName("strDrinkThumb") val imageUrl: String? = null,
    @SerialName("strIngredient1") val ingredient1: String? = null,
    @SerialName("strIngredient2") val ingredient2: String? = null,
    @SerialName("strIngredient3") val ingredient3: String? = null,
    @SerialName("strIngredient4") val ingredient4: String? = null,
    @SerialName("strIngredient5") val ingredient5: String? = null,
    @SerialName("strIngredient6") val ingredient6: String? = null,
    @SerialName("strIngredient7") val ingredient7: String? = null,
    @SerialName("strIngredient8") val ingredient8: String? = null,
    @SerialName("strIngredient9") val ingredient9: String? = null,
    @SerialName("strIngredient10") val ingredient10: String? = null,
    @SerialName("strIngredient11") val ingredient11: String? = null,
    @SerialName("strIngredient12") val ingredient12: String? = null,
    @SerialName("strIngredient13") val ingredient13: String? = null,
    @SerialName("strIngredient14") val ingredient14: String? = null,
    @SerialName("strIngredient15") val ingredient15: String? = null,
    @SerialName("strMeasure1") val measure1: String? = null,
    @SerialName("strMeasure2") val measure2: String? = null,
    @SerialName("strMeasure3") val measure3: String? = null,
    @SerialName("strMeasure4") val measure4: String? = null,
    @SerialName("strMeasure5") val measure5: String? = null,
    @SerialName("strMeasure6") val measure6: String? = null,
    @SerialName("strMeasure7") val measure7: String? = null,
    @SerialName("strMeasure8") val measure8: String? = null,
    @SerialName("strMeasure9") val measure9: String? = null,
    @SerialName("strMeasure10") val measure10: String? = null,
    @SerialName("strMeasure11") val measure11: String? = null,
    @SerialName("strMeasure12") val measure12: String? = null,
    @SerialName("strMeasure13") val measure13: String? = null,
    @SerialName("strMeasure14") val measure14: String? = null,
    @SerialName("strMeasure15") val measure15: String? = null
) {

    fun ingredientLines(): List<IngredientDto> {
        val names = listOf(
            ingredient1, ingredient2, ingredient3, ingredient4, ingredient5,
            ingredient6, ingredient7, ingredient8, ingredient9, ingredient10,
            ingredient11, ingredient12, ingredient13, ingredient14, ingredient15
        )
        val measures = listOf(
            measure1, measure2, measure3, measure4, measure5,
            measure6, measure7, measure8, measure9, measure10,
            measure11, measure12, measure13, measure14, measure15
        )
        return names.mapIndexedNotNull { index, name ->
            name?.takeIf { it.isNotBlank() }?.let {
                IngredientDto(name = it, measure = measures.getOrNull(index)?.trim())
            }
        }
    }
}
