package com.cpbpc.rpgv2.zh;

import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.rpgv2.ConfigObj;
import com.cpbpc.rpgv2.PhoneticIntf;
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

import static com.cpbpc.comms.TextUtil.containsUnicodeEscape;
import static com.cpbpc.comms.TextUtil.getChineseWord;

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

        List<String> ps = generatePattern();
        List<String> finds = new ArrayList<>();
        for( String p: ps ){
            Pattern r = Pattern.compile(p);
            Matcher matcher = r.matcher(content);
            while (matcher.find()) {
                if( !finds.contains(matcher.group(1)) ){
                    finds.add(matcher.group(1));
                }
            }
        }
        logger.info("fixing phonetics : " + finds.toString());

        String replaced = content;
        int start = 0;
        for (String key : finds) {
            String keyToFind = key;
            if(PunctuationTool.containQuestionMark(key)){
                keyToFind = PunctuationTool.escapeQuestionMark(key);
            }
            String completeForm = lookupCompleteForm(keyToFind);
            logger.info("complete form " + completeForm);
//            if (phonetic.containsKey(keyToFind) && phonetic.get(keyToFind).getPaused()) {
//                replaced = replaced.replace(key, completeForm + "[pause]");
//            } else {
//                replaced = replaced.replace(key, completeForm);
//            }
            replaced = replaced.replace(key, completeForm);
        }

        return replaced;
    }

    @Override
    public Map<String, ConfigObj> getPhoneticMap() {
        return phonetic;
    }

    @Override
    public String reversePhoneticCorrection(String text) {
        if( StringUtils.isEmpty(text) ){
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

            if( containsUnicodeEscape(key) ){
                Pattern p = Pattern.compile(key);
                Matcher matcher = p.matcher(founded);
                if( matcher.find() ){
                    String toBeReplaced = getChineseWord(key);
                    return StringUtils.replace(founded, toBeReplaced, entry.getValue().getFullWord());
                }
            }

            if (newFounded.equals(key)) {
                return entry.getValue().getFullWord();
            }
        }

        return "";
    }

    private static List<String> generatePattern() {
        List<String> patterns = new ArrayList<>();

        List<List<String>> keySet = splitSet(phonetic.keySet());
        
        for (List<String> keys : keySet) {
            StringBuilder builder = new StringBuilder("(");
            for( String key: keys ){
                String newKey = key.replace(".", "\\.");
                builder.append(newKey).append("|");
            }
            if (builder.toString().endsWith("|")) {
                builder.delete(builder.length() - 1, builder.length());
            }
            builder.append(")");
            patterns.add(builder.toString());
        }

        return patterns;
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
}
