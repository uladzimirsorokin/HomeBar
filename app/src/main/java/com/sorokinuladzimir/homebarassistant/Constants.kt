package com.sorokinuladzimir.homebarassistant

object Constants {

    object Uri {
        const val ABSOLUT_DRINKS_ROOT = BuildConfig.ABSOLUT_DRINKS_ROOT
        const val ABSOLUT_DRINKS_IMAGE_ROOT = BuildConfig.ABSOLUT_DRINKS_IMAGE_ROOT
        const val ABSOLUT_INGREDIENTS_IMAGE_ROOT = BuildConfig.ABSOLUT_INGREDIENTS_IMAGE_ROOT
    }

    object Keys {
        const val ABSOLUT_API_KEY = BuildConfig.ABSOLUT_API_KEY
    }

    object Strings {
        const val ALBUM_NAME = "HomeBar"
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"
    }

    object Values {
        const val DEFAULT_ITEM_AMOUNT = 5
        const val DEFAULT_IMAGE_SIZE = 750
    }

    object Extra {
        const val MIN_RATING = "minRating"
        const val MAX_RATING = "maxRating"
        const val GLASS_ID = "glassId"
        const val TASTE_ID = "tasteId"
        const val INGREDIENT_ID = "ingredientId"
        const val CARBONATED = "isCarbonated"
        const val ALCOHOLIC = "isAlcoholic"
        const val SKILL = "skill"
        const val COLOR = "color"
        const val COCKTAIL = "cocktail"
        const val REQUEST_CONDITIONS = "request conditions"
        const val APP_THEME = "appTheme"
        const val API_KEY = "apiKey"
        const val LANG = "lang"
        const val AMOUNT = "amount"
        const val UNIT = "unit"
        const val ALLOW_DELETE = "allowDelete"
        const val TITLE = "title"
        const val SELECTION = "selection"
        const val CONDITIONS = "conditions"
        const val EDITABLE = "editable"
        const val EXTRA_ID = "extraId"
        const val DIALOG = "dialog"
    }
}
