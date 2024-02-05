package com.cpbpc.comms;

import java.util.Map;

public interface AbbreIntf {

    public void put(String shortForm, String completeForm, boolean isPaused);

    public String convert(String content);

    public Map<String, ConfigObj> getAbbreMap();
}
