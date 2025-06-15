package com.cpbpc.rpgv3.zh;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeditationEntry {

    public static void main(String args[]) throws JsonProcessingException {

//        String input = """
//                {
//                    "date": "七月二日，礼拜三",
//                    "topicVerses": [
//                        "路得记一章1-3节",
//                        "利未记二十六章14-20节"
//                    ],
//                    "focusedVerse": "你们要白白地劳力，因为你们的地不出土产，其上的树木也不结果子。",
//                    "title": "士师时代 (2)",
//                    "meditation": "护理之工将生命中的每一刻都置于神的计划之中。",
//                    "prayer": "天父，帮助我看到祢在我生命中的护理和引导之手。",
//                    "paragraphs": [
//                        "马太·亨利清楚地讲述了神的护理对人的要求：\\"除了养向神，没有可以逃避祂的地方；除了养向祂的怜悯，没有可以躲避祂公义的地方。\\"",
//                        "\\"……因中遭遇饥荒\\"（得1:1）。这节经文反映了以色列国的衰败！他们背离了自己的使命，改变了思想和目的，并且相信自己的智慧和想法，忽视了神明确的指示。环境、暂时的优势和当前的利益占据了主导的地位。他们的思想成了他们的权威，他们不遵循神的话。消灭迦南人的命令，能确保他们的独立性并有助于他们的圣洁。这能带来属灵上的益处，使他们摆脱迦南生活方式的诱惑。他们没有从属灵的角度来看待事情，而是从属世的角度来看待它。他们并不认为谁交会败坏善行。他们选择了妥协而不是分别。没有任何今世的利益值得人违背神的话语和旨意所带来的属灵毁灭。我们的愿望必须是明白神的话语并遵行祂所启示的旨意。",
//                        "当他们离开神的话语而追随自己的想法，并且拒绝神的计划而为自己开辟一条不同的道路时，他们的勤奋和努力就徒劳无益。粮仓（伯利恒的字面意思）遭遇粮荒。尽管田地仍然肥沃，土地仍然是\\"好土地\\"，但是神预定了一场管教性的饥荒，目的是让祂的儿女回到祂的身边。饥荒中仍存在神的护理，就像当神\\"将所储靠的粮食全行断绝\\"（诗105:16），或在大卫统治期间（撒下21:1；24:13），以及所罗门在就殿的祈祷（代下6:22-39）（约沙法在历代志下20:6-9中复述了这一祈告）中所体现的那样。"
//                    ]
//                }
//                """;
//
//        ObjectMapper mapper = new ObjectMapper();
//        MeditationEntry entry = mapper.readValue(input, MeditationEntry.class);
//
//        // Print to verify
//        System.out.println("Title: " + entry.getTitle());
//        System.out.println("Date: " + entry.getDate());
//        System.out.println("Topic Verses: " + entry.getTopicVerses());
//        System.out.println("Focused Verse: " + entry.getFocusedVerse());
//        System.out.println("Meditation: " + entry.getMeditation());
//        System.out.println("Prayer: " + entry.getPrayer());
//        System.out.println("Paragraphs: " + entry.getParagraphs());
        String input = "六月二十九日，主日傍晚";
        Pattern pattern = Pattern.compile("((?:一|二|三|四|五|六|七|八|九|十|十一|十二)月(?:[一二三四五六七八九十]{1,3})日)[，、]?(?:[^一二三四五六七八九十早晚]*)(早晨|傍晚)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            System.out.println("Date: " + matcher.group(1));
            System.out.println("Time: " + matcher.group(2));
        }

    }

    private String date;
    private List<String> topicVerses;
    private String focusedVerse;
    private String title;
    private String meditation;
    private String prayer;
    private String memorisation;
    private List<String> paragraphs;
    
    // Getters and setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<String> getTopicVerses() { return topicVerses; }
    public void setTopicVerses(List<String> topic_verses) { this.topicVerses = topic_verses; }

    public String getFocusedVerse() { return focusedVerse; }
    public void setFocusedVerse(String focused_verse) { this.focusedVerse = focused_verse; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMeditation() { return meditation; }
    public void setMeditation(String meditation) { this.meditation = meditation; }

    public String getPrayer() { return prayer; }
    public void setPrayer(String prayer) { this.prayer = prayer; }

    public String getMemorisation() {
        return memorisation;
    }

    public void setMemorisation(String memorisation) {
        this.memorisation = memorisation;
    }

    public List<String> getParagraphs() { return paragraphs; }
    public void setParagraphs(List<String> paragraphs) { this.paragraphs = paragraphs; }

}
