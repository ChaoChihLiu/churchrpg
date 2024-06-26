package com.cpbpc.comms;

public class Article {

    private String startDate;
    private String timing;
    private String content;
    private String title;
    private String category;

    private int counter = 0;

    public Article(String startDate, String desc, String title, String category, int counter) {
        this.startDate = startDate;
        this.content = desc;
        this.title = title;
        this.category = category;
        this.counter = counter;
    }

    public Article(String startDate, String timing, String desc, String title, String category, int counter) {
        this.startDate = startDate;
        this.timing = timing;
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

    public String getTiming(){
        return timing;
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
