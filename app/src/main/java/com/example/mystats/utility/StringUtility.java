package com.example.mystats.utility;

public class StringUtility {
    public static String capitalizeFirstLetter(final String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String cleanString(String str) {
        str = str.trim();
        if (!str.isEmpty()) {
            str = capitalizeFirstLetter(str);
        }
        return str;
    }

    public static String cleanDoubleString(String str) {
        if (str.startsWith(".")) {
            str = "0" + str;
        }
        else {
            str = str.replaceFirst("^0+(?!$)", "0");
        }
        return str;
    }
}
