package com.sorokinuladzimir.homebarassistant;

public class Constants {

    private Constants() {
        throw new UnsupportedOperationException();
    }

    public static class Uri {
        public static final String ABSOLUT_DRINKS_ROOT = "http://addb.absolutdrinks.com";
        public static final String ABSOLUT_DRINKS_IMAGE_ROOT = "http://assets.absolutdrinks.com/drinks/600x800/";
        public static final String ABSOLUT_INGREDIENTS_IMAGE_ROOT = "http://assets.absolutdrinks.com/";

        private Uri() {
        }
    }

    public static class Keys {
        public static final String ABSOLUT_API_KEY = "e94231991e5d42c58fbd02e0d60fe11b";

        private Keys() {
        }
    }

    public static class Strings {
        public static final String ALBUM_NAME = "HomeBar";
        public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

        private Strings() {
        }
    }

    public static class Values {
        public static final int DEFAULT_ITEM_AMOUNT = 5;
        public static final int DEFAULT_IMAGE_SIZE = 750;

        private Values() {
        }
    }

    public static class Extra {
        public static final String MIN_RATING = "minRating";
        public static final String MAX_RATING = "maxRating";
        public static final String GLASS_ID = "glassId";
        public static final String TASTE_ID = "tasteId";
        public static final String INGREDIENT_ID = "ingredientId";
        public static final String CARBONATED = "isCarbonated";
        public static final String ALCOHOLIC = "isAlcoholic";
        public static final String SKILL = "skill";
        public static final String COLOR = "color";
        public static final String COCKTAIL = "cocktail";
        public static final String REQUEST_CONDITIONS = "request conditions";
        public static final String APP_THEME = "appTheme";
        public static final String API_KEY = "apiKey";
        public static final String LANG = "lang";
        public static final String AMOUNT = "amount";
        public static final String UNIT = "unit";
        public static final String ALLOW_DELETE = "allowDelete";
        public static final String TITLE = "title";
        public static final String SELECTION = "selection";
        public static final String CONDITIONS = "conditions";
        public static final String EDITABLE = "editable";
        public static final String EXTRA_ID = "extraId";

        private Extra() {
        }
    }


}
