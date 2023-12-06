package com.cpbpc.rpgv2.en;

import com.cpbpc.rpgv2.ConfigObj;
import com.cpbpc.rpgv2.PhoneticIntf;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Phonetics implements PhoneticIntf {

    private static final Map<String, ConfigObj> phonetic = new LinkedHashMap<>();
    private static Logger logger = Logger.getLogger(Phonetics.class.getName());

    public void put(String shortForm, String completeForm, boolean isPaused) {
        phonetic.put(shortForm, new ConfigObj(shortForm, completeForm, isPaused));
    }

    public String convert(String content) {

        if (null == phonetic || phonetic.isEmpty()) {
            return content;
        }

        String p = generatePattern();
        logger.info(p);
        Pattern r = Pattern.compile(p);
        Matcher matcher = r.matcher(content);

        List<String> finds = new ArrayList<>();
        while (matcher.find()) {
            finds.add(matcher.group(1));
        }
        logger.info("what is my finds : " + finds.toString());

        String replaced = content;
        int start = 0;
        for (String key : finds) {
            String completeForm = lookupCompleteForm(key);
            logger.info("complete form " + completeForm);
            if (phonetic.get(key).getPaused()) {
                replaced = replaced.replace(key, completeForm + "[pause]");
            } else {
                replaced = replaced.replace(key, completeForm);
            }
        }

        return replaced;
    }

    @Override
    public Map<String, ConfigObj> getPhoneticMap() {
        return phonetic;
    }

    @Override
    public String reversePhoneticCorrection(String text) {
        if( org.apache.commons.lang3.StringUtils.isEmpty(text) ){
            return "";
        }
        String result = text;
        Collection<ConfigObj> values = phonetic.values();
        for( ConfigObj value : values ){
            String fix = value.getFullWord();
            if( !StringUtils.contains(result, fix) ){
                continue;
            }
            result = result.replaceAll(fix, value.getShortForm());
        }

        return result;
    }

    private static String lookupCompleteForm(String founded) {

        for (Map.Entry<String, ConfigObj> entry : phonetic.entrySet()) {

            String newFounded = founded.replace(".", "");
            String key = entry.getKey().replace(".", "");

            if (newFounded.equals(key)) {
                return entry.getValue().getFullWord();
            }
        }

        return "";
    }

    private static String generatePattern() {

//        return "([\\s{1,}|,{1,}](e\\.{0,}g\\.{0,}|i\\.{0,}e\\.{0,}|etc\\.{0,}))([\\s{1,}|,{1,}])";
        StringBuilder builder = new StringBuilder("(");

        Set<String> keySet = phonetic.keySet();
        for (String key : keySet) {
//            String newKey = key.trim().replace(".", "\\.{0,}");
            String newKey = key.replace(".", "\\.");
            builder.append(newKey).append("|");
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }

        builder.append(")");
        return builder.toString();
    }
}
