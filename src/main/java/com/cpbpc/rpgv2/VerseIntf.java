package com.cpbpc.rpgv2;

import java.io.IOException;
import java.util.Map;

public interface VerseIntf {

    public void put(String shortForm, String completeForm, boolean isPaused);

    public String convert(String content) throws IOException;

    public Map<String, ConfigObj> getVerseMap();
}
