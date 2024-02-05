package com.cpbpc.comms;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface VerseIntf {

    public void put(String shortForm, String completeForm, boolean isPaused);

    public String convert(String content);
    public String convert(String content, boolean addPause);
    public String convert(String content, Pattern pattern);

    public Map<String, ConfigObj> getVerseMap();

    public String appendNextCharTillCompleteVerse(String content, String target, int position, int anchorPoint);
    public Pattern getVersePattern() ;

    public List<String> analyseVerse(String line);

    public List<String> analyseVerse(String line, Pattern pattern);
}
