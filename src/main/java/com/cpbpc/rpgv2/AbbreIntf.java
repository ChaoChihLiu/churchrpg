package com.cpbpc.rpgv2;

import java.util.Map;

public interface AbbreIntf {

    public void put(String shortForm, String completeForm, boolean isPaused);

    public String convert(String content);

    public Map<String, ConfigObj> getAbbreMap();
}
