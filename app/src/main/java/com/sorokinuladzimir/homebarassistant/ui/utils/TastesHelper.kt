package com.sorokinuladzimir.homebarassistant.ui.utils

import com.sorokinuladzimir.homebarassistant.db.entity.Taste
import com.sorokinuladzimir.homebarassistant.net.entity.TasteDto

object TastesHelper {

    fun tastesToString(tastes: List<Taste>?): String =
        tastes?.joinToString(", ") { it.text.orEmpty() }.orEmpty()

    fun tastesDtoToString(tastes: List<TasteDto>?): String =
        tastes?.joinToString(", ") { it.text.orEmpty() }.orEmpty()

    fun toTastesList(selectedIngredients: List<Int>, tastesArray: Array<String>): List<Taste> =
        selectedIngredients.map { Taste(tastesArray[it]) }

    fun getAsIntegerList(tastes: List<Taste>?, tastesArray: Array<String>): List<Int> {
        if (tastes == null) return emptyList()
        return tastes.mapNotNull { taste ->
            tastesArray.indexOf(taste.text).takeIf { it >= 0 }
        }
    }
}
