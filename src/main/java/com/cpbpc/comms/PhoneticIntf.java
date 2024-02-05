package com.cpbpc.comms;

import java.util.Map;

public interface PhoneticIntf {

    public void put(String shortForm, String completeForm, boolean isPaused);

    public String convert(String content);

    public Map<String, ConfigObj> getPhoneticMap();

    public String reversePhoneticCorrection(String input);
}
