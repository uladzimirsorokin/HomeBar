package sorokinuladzimir.com.homebarassistant.ui.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IngredientParcer {

    private static final String REGEX_AMOUNT = "(^[0-9]+(/[0-9]+)? ([0-9]+/[0-9]+)?)(.*)\\[(.*)";
    private static final String REGEX_AMOUNT2 = "^[0-9]+(\\/[0-9]+)?( [0-9]+\\/[0-9]+)?";

    private static final String REGEX_AMOUNT3 = "[^0-9](.*)\\[(.*)]";

    private static final String REGEX_UNIT = " (\\w*?) ";

    private static final String REGEX_NAME = "\\[(.*?)\\]";

    public static String parseName(String name){
        Pattern pattern = Pattern.compile(REGEX_NAME);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find())       {
           return matcher.group(1);
        }

        return name;
    }

    public static String parseUnit(String name){
        Pattern pattern = Pattern.compile(REGEX_UNIT);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find())       {
            return matcher.group(1);
        }

        return "";
    }

    public static String parseAmount(String name){
        Pattern pattern = Pattern.compile(REGEX_AMOUNT);
        Matcher matcher = pattern.matcher(name);
        if (matcher.find())       {
            for(int i = 0; i < matcher.groupCount(); i++){
                String group = matcher.group(i);
                if (group != null) group.length();
            }
            return matcher.group(1);
        }

        return "";
        //return name.replaceAll(REGEX_AMOUNT, "");
    }
}