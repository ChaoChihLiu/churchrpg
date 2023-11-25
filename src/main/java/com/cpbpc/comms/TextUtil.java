package com.cpbpc.comms;

import org.apache.commons.lang3.StringUtils;

public class TextUtil {

    public static String removeHtmlTag(String input){
        if (StringUtils.isEmpty(input)) {
            return "";
        }

        return input.replaceAll("<[^>]*>|&nbsp;|&zwnj;|&raquo;|&laquo;|&gt;", " ");
//                .replaceAll("&nbsp;", " ")  ;
    }

}
