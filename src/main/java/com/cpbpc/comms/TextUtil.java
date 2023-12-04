package com.cpbpc.comms;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.StringUtils;

public class TextUtil {

    public static String returnChapterWord(String book){
        if( AppProperties.isChinese() ){
            if( StringUtils.equals(book, "詩篇")
                    || StringUtils.equals(book, "詩")
                    || StringUtils.equals(book, ZhConverterUtil.toSimple("詩篇"))
                    || StringUtils.equals(book, ZhConverterUtil.toSimple("詩"))){
                return "篇";
            }
            return "章";
        }
        if( AppProperties.isEnglish() ){
            if( StringUtils.equalsIgnoreCase(book, "psalm") || StringUtils.equalsIgnoreCase(book, "psalms") ){
                return "";
            }
            return "chapter";
        }
        return "";
    }

    public static String removeHtmlTag(String input){
        if (StringUtils.isEmpty(input)) {
            return "";
        }

        return input.replaceAll("<[^>]*>|&nbsp;|&zwnj;|&raquo;|&laquo;|&gt;", " ");
//                .replaceAll("&nbsp;", " ")  ;
    }

}
