package com.cpbpc.rpgv2.en;

import com.cpbpc.comms.AbbreIntf;
import com.cpbpc.comms.ConfigObj;
import com.cpbpc.comms.PunctuationTool;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
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

    private static List<List<String>> splitSet(Set<String> set){
        String[] arrayFromSet = set.toArray(new String[0]);
        List<List<String>> list = new ArrayList<>();
        int batchSize = 100;
        for (int i = 0; i < arrayFromSet.length; i += batchSize) {
            int endIndex = Math.min(i + batchSize, arrayFromSet.length);
            String[] subArray = Arrays.copyOfRange(arrayFromSet, i, endIndex);
            list.add( List.of(subArray) );
        }

        return list;
    }

    private static List<String> generatePattern() {
        List<String> patterns = new ArrayList<>();

        List<List<String>> keySet = splitSet(abbre.keySet());

//        return "([\\s{1,}|,{1,}](e\\.{0,}g\\.{0,}|i\\.{0,}e\\.{0,}|etc\\.{0,}))([\\s{1,}|,{1,}])";
        StringBuilder builder = new StringBuilder("(");

        for (List<String> keys : keySet) {
            for (String key : keys) {
//            String newKey = key.trim().replace(".", "\\.{0,}");
                String newKey = key.replace(".", "\\.");
//            builder.append(newKey).append("|");
                builder.append("(\\(|&nbsp;|\\s){0,}").append(newKey).append("(&nbsp;|\\s|\\)){0,}|");
            }
            if (builder.toString().endsWith("|")) {
                builder.delete(builder.length() - 1, builder.length());
            }

            builder.append(")");
            patterns.add(builder.toString());
        }

        return patterns;
    }

    public void put(String shortForm, String completeForm, boolean isPaused) {
        abbre.put(shortForm, new ConfigObj(shortForm, completeForm, isPaused));
    }

    public String convert(String content) {

        if (null == abbre || abbre.isEmpty()) {
            return content;
        }

        String replaced = content;
        List<String> patterns = generatePattern();
        for( String p : patterns ){
            Pattern r = Pattern.compile(p);
            Matcher matcher = r.matcher(content);

            List<String> finds = new ArrayList<>();
            while (matcher.find()) {
                finds.add(matcher.group(1));
            }
            logger.info("what is my finds : " + finds.toString());
            for (String key : finds) {
                key = StringUtils.trim(key);
                String completeForm = lookupCompleteForm(key);
//                logger.info("complete form " + completeForm);
                if (abbre.get(key) != null && abbre.get(key).getPaused()) {
                    replaced = replaced.replace(" " +StringUtils.capitalize(key)+" ", " " + completeForm + " " + "[pause]") ;
                    replaced = replaced.replace(" " +StringUtils.lowerCase(key)+" ", " " + completeForm + " " + "[pause]") ;
                    replaced = replaceWithAllowedPunc(replaced, key,  completeForm, true);
                    replaced = replaceWordStarting(replaced, key,  completeForm, false);
                } else {
                    replaced = replaced.replace(" " +StringUtils.capitalize(key)+" ", " " + completeForm + " ")  ;
                    replaced = replaced.replace(" " +StringUtils.lowerCase(key)+" ", " " + completeForm + " ")  ;
                    replaced = replaceWithAllowedPunc(replaced, key,  completeForm, false);
                    replaced = replaceWordStarting(replaced, key,  completeForm, false);
                }
            }
        }
        return replaced;
    }
    private String replaceWithAllowedPunc(String input, String key, String replacement, boolean addPaused) {
        String replaced = input;
        List<String> allowedPuncs = PunctuationTool.getAllowedPunctuations();
        allowedPuncs.add("?");
        allowedPuncs.add("？");
        allowedPuncs.add("!");
        allowedPuncs.add("！");
        allowedPuncs.add(".");

        for( String punc : allowedPuncs ){
            String completed = " " + replacement + punc;
            if( addPaused ){
                completed += "[pause]";
            }
            replaced = replaced.replace(" " +StringUtils.capitalize(key)+punc, completed);
            replaced = replaced.replace(" " +StringUtils.lowerCase(key)+punc, completed);
        }

        return replaced;
    }
    private String replaceWordStarting(String input, String key, String replacement, boolean addPaused){
        String regex = "(?i)(^|\\.|\\?|!|\\n|\\r\\n)\\s*" + key + "\\b";
        StringBuffer result = new StringBuffer();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group().replaceFirst("(?i)" + key, replacement));
        }
        matcher.appendTail(result);

//        if( addPaused ){
//            result.append("[pause]");
//        }
        return result.toString();
    }

    @Override
    public Map<String, ConfigObj> getAbbreMap() {
        return abbre;
    }
}
