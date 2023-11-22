package com.cpbpc.rpgv2;

public class ConfigObj {

    private String shortForm;
    private String fullWord;
    private boolean isPaused = false;

    public ConfigObj(String shortForm, String fullWord, boolean isPaused) {
        this.shortForm = shortForm;
        this.fullWord = fullWord;
        this.isPaused = isPaused;
    }

    public String getFullWord() {
        return fullWord;
    }

    public boolean getPaused() {
        return isPaused;
    }

    public String getShortForm() {
        return shortForm;
    }
}
