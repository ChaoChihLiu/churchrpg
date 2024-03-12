package com.cpbpc.pdf.rpg.zh;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Composer {
    private final static VelocityEngine velocityEngine = new VelocityEngine();

    public void toHtml( ArticleParser parser ){
        velocityEngine.init();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("date", parser.readDate());
        dataMap.put("topicVerses", parser.readTopicVerses());
        dataMap.put("focusScriptures", parser.readFocusScripture());
        dataMap.put("title", parser.getTitle());
        dataMap.put("paragraphs", parser.readParagraphs());
        dataMap.put("endMap", parser.readEnd());

        VelocityContext context = new VelocityContext(dataMap);
        Template template = velocityEngine.getTemplate("src/main/resources/template/rpg-zh.vm");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        // Print the merged output
        System.out.println(writer.toString());
    }

    public static void main(String[] args){
        velocityEngine.init();

        // Create a Map and populate it with data
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("name", "John Doe");
        dataMap.put("age", 30);
        dataMap.put("city", "New York");

        // Pass the Map to Velocity context
        VelocityContext context = new VelocityContext(dataMap);

        // Get template and merge with the context
        Template template = velocityEngine.getTemplate("src/main/resources/template/rpg-zh.vm");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        // Print the merged output
        System.out.println(writer.toString());
    }

}
