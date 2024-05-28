package com.cpbpc.rpgv2.en;

import com.cpbpc.comms.ConfigObj;
import com.cpbpc.comms.PhoneticIntf;
import com.cpbpc.comms.PunctuationTool;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
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

        /*
        ([\s|&nbsp;]{1,}overlived[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Machpelah[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Lahairoi[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Chessed[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Jidlaph[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Mamre[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Gihon[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Pison[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Jehovahjireh[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Girgashites[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}girgashites[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Methusael[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Habakkuk[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}villany[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Malachi[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Phichol[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}covert[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}triest[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Cainan[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}sware[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}lieth[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Jabal[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Jubal[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Laban[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Raca[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Irad[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Enos[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Hazo[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}err[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}ERR[\s|&nbsp;]{1,}|[\s|&nbsp;]{1,}Nun[\s|&nbsp;]{1,})
         */

        List<String> patterns = generatePattern();
        String replaced = content;
        for( String p : patterns ){
//            logger.info(p);
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
                if (phonetic.get(key) != null && phonetic.get(key).getPaused()) {
                    replaced = replaced.replace(" " +key+" ", " " + completeForm + " " + "[pause]") ;
                    replaced = replaceWithAllowedPunc(replaced, key,  completeForm, true);

                    replaced = replaced.replace(" " +StringUtils.capitalize(key)+" ", " " + completeForm + " " + "[pause]") ;
                    replaced = replaceWithAllowedPunc(replaced, StringUtils.capitalize(key),  completeForm, true);
                } else {
                    replaced = replaced.replace(" " +key+" ", " " + completeForm + " ")  ;
                    replaced = replaceWithAllowedPunc(replaced, key,  completeForm, false);

                    replaced = replaced.replace(" " +StringUtils.capitalize(key)+" ", " " + completeForm + " ")  ;
                    replaced = replaceWithAllowedPunc(replaced, StringUtils.capitalize(key),  completeForm, false);
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
            replaced = replaced.replace(" " +key+punc, completed);
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

    /*

    ((&nbsp;|\s){1,}Nahor's(&nbsp;|\s){1,}|(&nbsp;|\s){1,}Chedorlaomer(&nbsp;|\s){1,})

     */

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

        List<List<String>> keySet = splitSet(phonetic.keySet());

        for (List<String> keys : keySet) {
            StringBuilder builder = new StringBuilder("(");
            for( String key: keys ){
                String newKey = key.replace(".", "\\.");
                builder.append("(&nbsp;|\\s){0,}").append(newKey).append("(&nbsp;|\\s){0,}|")
                ;
            }
            if (builder.toString().endsWith("|")) {
                builder.delete(builder.length() - 1, builder.length());
            }
            builder.append(")");
            patterns.add(builder.toString());
        }

        return patterns;
    }

}
