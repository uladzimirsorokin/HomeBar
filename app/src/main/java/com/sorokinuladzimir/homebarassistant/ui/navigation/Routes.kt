package com.sorokinuladzimir.homebarassistant.ui.navigation

import android.net.Uri

object Routes {

    const val FOUND_DRINKS = "found_drinks?conditions={conditions}"
    const val SEARCH_FILTER = "search_filter"
    const val DRINKS_LIST = "drinks_list"
    const val INGREDIENTS_LIST = "ingredients_list"
    const val REMOTE_DRINK = "remote_drink/{drinkId}"
    const val LOCAL_DRINK = "local_drink/{extra_id}?editable={editable}"
    const val ADD_DRINK = "add_drink/{extra_id}"
    const val ADD_DRINK_INGREDIENTS = "add_drink_ingredients"
    const val LOCAL_INGREDIENT = "local_ingredient/{extra_id}?editable={editable}"
    const val ADD_INGREDIENT = "add_ingredient/{extra_id}"
    const val ABOUT = "about"
    const val SETTINGS = "settings"

    const val ARG_CONDITIONS = "conditions"
    const val ARG_REMOTE_ID = "drinkId"
    const val ARG_ID = "extra_id"
    const val ARG_EDITABLE = "editable"

    const val START = DRINKS_LIST

    fun foundDrinks(conditions: String? = null): String =
        "found_drinks?$ARG_CONDITIONS=${Uri.encode(conditions ?: "")}"

    fun remoteDrink(id: String): String = "remote_drink/${Uri.encode(id)}"

    fun localDrink(id: Long, editable: Boolean): String =
        "local_drink/$id?$ARG_EDITABLE=$editable"

    fun addDrink(id: Long): String = "add_drink/$id"

    fun localIngredient(id: Long, editable: Boolean): String =
        "local_ingredient/$id?$ARG_EDITABLE=$editable"

    fun addIngredient(id: Long): String = "add_ingredient/$id"
}
