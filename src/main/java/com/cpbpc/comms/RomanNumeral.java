package com.cpbpc.comms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum RomanNumeral {
    I(1), IV(4), V(5), IX(9), X(10),
    XL(40), L(50), XC(90), C(100),
    CD(400), D(500), CM(900), M(1000);

    //    private static final String pattern_str = "\\(([IiVvXxLlCcDdMm]{1,10})\\)";
    private static final String pattern_str = "\\(([IVXLCDM]{1,10})\\)";
    private static final Pattern p = Pattern.compile(pattern_str);
    private static Logger logger = Logger.getLogger(RomanNumeral.class.getName());
    private int value;

    RomanNumeral(int value) {
        this.value = value;
    }

    public static List<RomanNumeral> getReverseSortedValues() {
        return Arrays.stream(values())
                .sorted(Comparator.comparing((RomanNumeral e) -> e.value).reversed())
                .collect(Collectors.toList());
    }

    public static String convert(String input) {

        Matcher m = p.matcher(input);

        List<String> finds = new ArrayList<>();
        while (m.find()) {
//            input = m.replaceFirst( " Part " + romanToArabic(m.group(1)) );
            finds.add(m.group(0).trim());
        }
        logger.info("what is my finds : " + finds.toString());

        String replaced = input;
        for (String key : finds) {
            logger.info("key is " + key);
            logger.info("replace is " + " Part " + romanToArabic(key.replace("(", "").replace(")", "")));
            replaced = replaced.replaceFirst(key.replace("(", "\\(").replace(")", "\\)"),
                    " Part " + romanToArabic(key.replace("(", "").replace(")", "")));
        }

        return replaced;
    }

    public static int romanToArabic(String input) {
        String romanNumeral = input.toUpperCase();
        int result = 0;

        List<RomanNumeral> romanNumerals = RomanNumeral.getReverseSortedValues();

        int i = 0;

        while ((romanNumeral.length() > 0) && (i < romanNumerals.size())) {
            RomanNumeral symbol = romanNumerals.get(i);
            if (romanNumeral.startsWith(symbol.name())) {
                result += symbol.getValue();
                romanNumeral = romanNumeral.substring(symbol.name().length());
            } else {
                i++;
            }
        }

        if (romanNumeral.length() > 0) {
            throw new IllegalArgumentException(input + " cannot be converted to a Roman Numeral");
        }

        return result;
    }

    public static String arabicToRoman(int number) {
        if ((number <= 0) || (number > 4000)) {
            throw new IllegalArgumentException(number + " is not in range (0,4000]");
        }

        List<RomanNumeral> romanNumerals = RomanNumeral.getReverseSortedValues();

        int i = 0;
        StringBuilder sb = new StringBuilder();

        while ((number > 0) && (i < romanNumerals.size())) {
            RomanNumeral currentSymbol = romanNumerals.get(i);
            if (currentSymbol.getValue() <= number) {
                sb.append(currentSymbol.name());
                number -= currentSymbol.getValue();
            } else {
                i++;
            }
        }

        return sb.toString();
    }

    public int getValue() {
        return value;
    }
}