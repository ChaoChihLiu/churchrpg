package com.cpbpc.rpgv2.en;

import com.cpbpc.comms.AbbreIntf;
import com.cpbpc.comms.ConfigObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Abbreviation implements AbbreIntf {

    private static final Map<String, ConfigObj> abbre = new HashMap();
    private static Logger logger = Logger.getLogger(Abbreviation.class.getName());

    private static String lookupCompleteForm(String founded) {

        for (Map.Entry<String, ConfigObj> entry : abbre.entrySet()) {

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

        Set<String> keySet = abbre.keySet();
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

    public void put(String shortForm, String completeForm, boolean isPaused) {
        abbre.put(shortForm, new ConfigObj(shortForm, completeForm, isPaused));
    }

    public String convert(String content) {

        if (null == abbre || abbre.isEmpty()) {
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
            if (abbre.get(key).getPaused()) {
                replaced = replaced.replace(key, completeForm + "<break time=\"200ms\"/>");
            } else {
                replaced = replaced.replace(key, completeForm);
            }
        }

        return replaced;
    }

    @Override
    public Map<String, ConfigObj> getAbbreMap() {
        return abbre;
    }
}
