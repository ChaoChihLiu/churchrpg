package com.cpbpc.rpg;

public class AbbreviationObj {

    String shortForm;
    String fullWord;
    boolean isPaused = false;

    public AbbreviationObj(String shortForm, String fullWord, boolean isPaused) {
        this.shortForm = shortForm;
        this.fullWord = fullWord;
        this.isPaused = isPaused;
    }
}
