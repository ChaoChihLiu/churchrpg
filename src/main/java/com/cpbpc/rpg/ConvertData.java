package com.cpbpc.rpg;

public class ConvertData {

    private String startDate;
    private String content;
    private String title;
    private String category;

    private int counter;

    public ConvertData(String startDate, String desc, String title, String category, int counter) {
        this.startDate = startDate;
        this.content = desc;
        this.title = title;
        this.category = category;
        this.counter = counter;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCounter() {
        return counter;
    }
}
