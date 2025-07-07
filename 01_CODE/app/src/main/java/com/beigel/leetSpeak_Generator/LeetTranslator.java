package com.beigel.leetSpeak_Generator;

import java.util.HashMap;
import java.util.Map;

public class LeetTranslator {
    private static final Map<Character, String> SIMPLE_MAP = new HashMap<>();
    private static final Map<Character, String> EXTENDED_MAP = new HashMap<>();

    // Statische Initialisierung für bessere Performance
    static {
        initializeSimpleMap();
        initializeExtendedMap();
    }

    private static void initializeSimpleMap() {
        SIMPLE_MAP.put('A', "4");
        SIMPLE_MAP.put('B', "8");
        SIMPLE_MAP.put('C', "C");
        SIMPLE_MAP.put('D', "D");
        SIMPLE_MAP.put('E', "3");
        SIMPLE_MAP.put('F', "F");
        SIMPLE_MAP.put('G', "6");
        SIMPLE_MAP.put('H', "#");
        SIMPLE_MAP.put('I', "1");
        SIMPLE_MAP.put('J', "J");
        SIMPLE_MAP.put('K', "K");
        SIMPLE_MAP.put('L', "L");
        SIMPLE_MAP.put('M', "M");
        SIMPLE_MAP.put('N', "N");
        SIMPLE_MAP.put('O', "0");
        SIMPLE_MAP.put('P', "P");
        SIMPLE_MAP.put('Q', "Q");
        SIMPLE_MAP.put('R', "R");
        SIMPLE_MAP.put('S', "5");
        SIMPLE_MAP.put('T', "7");
        SIMPLE_MAP.put('U', "U");
        SIMPLE_MAP.put('V', "V");
        SIMPLE_MAP.put('W', "W");
        SIMPLE_MAP.put('X', "X");
        SIMPLE_MAP.put('Y', "Y");
        SIMPLE_MAP.put('Z', "2");
    }

    private static void initializeExtendedMap() {
        EXTENDED_MAP.put('A', "4");
        EXTENDED_MAP.put('B', "8");
        EXTENDED_MAP.put('C', "(");
        EXTENDED_MAP.put('D', "|)");
        EXTENDED_MAP.put('E', "3");
        EXTENDED_MAP.put('F', "|=");
        EXTENDED_MAP.put('G', "6");
        EXTENDED_MAP.put('H', "#");
        EXTENDED_MAP.put('I', "!");
        EXTENDED_MAP.put('J', "_|");
        EXTENDED_MAP.put('K', "|<");
        EXTENDED_MAP.put('L', "1");
        EXTENDED_MAP.put('M', "/\\/\\");
        EXTENDED_MAP.put('N', "|\\|");
        EXTENDED_MAP.put('O', "0");
        EXTENDED_MAP.put('P', "9");
        EXTENDED_MAP.put('Q', "0_");
        EXTENDED_MAP.put('R', "2");
        EXTENDED_MAP.put('S', "5");
        EXTENDED_MAP.put('T', "7");
        EXTENDED_MAP.put('U', "|_|");
        EXTENDED_MAP.put('V', "\\/");
        EXTENDED_MAP.put('W', "\\/\\/");
        EXTENDED_MAP.put('X', "><");
        EXTENDED_MAP.put('Y', "`/");
        EXTENDED_MAP.put('Z', "Z");
    }

    public static String translate(String input, TranslationMode mode, CustomProfile customProfile) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {
            result.append(translateChar(c, mode, customProfile));
        }

        return result.toString();
    }

    public static String translateChar(char c, TranslationMode mode, CustomProfile customProfile) {
        char upperChar = Character.toUpperCase(c);

        switch (mode) {
            case SIMPLE:
                return SIMPLE_MAP.getOrDefault(upperChar, String.valueOf(c));

            case EXTENDED:
                return EXTENDED_MAP.getOrDefault(upperChar, String.valueOf(c));

            case CUSTOM:
                if (customProfile != null) {
                    String translation = customProfile.getTranslation(String.valueOf(upperChar));
                    return translation != null ? translation : String.valueOf(c);
                }
                return SIMPLE_MAP.getOrDefault(upperChar, String.valueOf(c));

            default:
                return String.valueOf(c);
        }
    }

    public enum TranslationMode {
        SIMPLE, EXTENDED, CUSTOM
    }
}