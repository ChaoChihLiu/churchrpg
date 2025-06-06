package com.cpbpc.comms;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Locale;

public class NumberConverter {

    public static String ordinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }


    public static String toChineseNumber(String text){
        if(StringUtils.isEmpty(text) ){
            return "";
        }

        String result = "";
        String temp = "";
        for( char c : text.toCharArray() ){
            if( !NumberUtils.isCreatable(String.valueOf(c)) ){
                if( !StringUtils.isEmpty(temp) ){
                    result += toChineseNumber(Integer.valueOf(temp));
                    temp = "";
                }
                result += c;
                continue;
            }

            temp += c;
        }


        return result;
    }

    public static String toChineseNumber(int i) {
        Locale chineseNumbers = new Locale("C@numbers=hans");
        com.ibm.icu.text.NumberFormat formatter =
                com.ibm.icu.text.NumberFormat.getInstance(chineseNumbers);

        return formatter.format(i);
    }

    public static int toNumber(String s) {
        if (NumberUtils.isCreatable(s)) {
            return NumberUtils.createInteger(s);
        }

        String x = " 一二三四五六七八九十百";
        int l = s.length(),
                i = x.indexOf(s.charAt(l - 1)),
                j = x.indexOf(s.charAt(0));
        if (l < 2) return i; // 1-10
        if (l < 3) {
            if (i == 10) return j * 10; // 20,30,40,50,60,70,80,90
            if (i > 10) return j * 100; // 100,200,300,400,500,600,700,800,900
            return 10 + i; // 11-19
        }
        if (l < 4) return j * 10 + i; // 21-29,31-39,41-49,51-59,61-69,71-79,81-89,91-99
        if (l < 5) return j * 100 + i; // 101-109,201-209,301-309,401-409,501-509,601-609,701-709,801-809,901-909
        return j * 100 + i + x.indexOf(s.charAt(2)) * 10; // 111-119,121-129,131-139,...,971-979,981-989,991-999
    }

    // Simple Chinese number parser for 1-31
    private static final String[] CN_NUMS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
    public static int chineseToNumber(String cn) {
        if (cn.equals("十")) return 10;
        if (cn.length() == 1) { // "一"..."九"
            return indexOfChineseOTNumber(CN_NUMS, cn);
        }
        if (cn.startsWith("十")) { // "十一"..."十九"
            return 10 + indexOfChineseOTNumber(CN_NUMS, cn.substring(1));
        }
        if (cn.endsWith("十")) { // "二十","三十"
            return indexOfChineseOTNumber(CN_NUMS, cn.substring(0, cn.length() - 1)) * 10;
        }
        if (cn.contains("十")) { // "二十一"..."三十一"
            String[] parts = cn.split("十");
            int tens = indexOfChineseOTNumber(CN_NUMS, parts[0]);
            int ones = indexOfChineseOTNumber(CN_NUMS, parts[1]);
            return tens * 10 + ones;
        }
        return 0; // fallback
    }

    private static int indexOfChineseOTNumber(String[] arr, String s) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(s)) return i;
        }
        return 0;
    }

}
