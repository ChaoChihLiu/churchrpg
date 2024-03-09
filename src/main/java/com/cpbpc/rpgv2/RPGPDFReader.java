package com.cpbpc.rpgv2;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class RPGPDFReader {

    static ThreadLocal title = new ThreadLocal<StringBuffer>();
    static ThreadLocal hasTitle = new ThreadLocal<Boolean>();

    public static void main(String[] args) throws IOException {
        String pdfFilePath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/24_apr-to-jun.pdf";

        // Create a PDFTextStripper object
        PDFTextStripper textStripper = new PDFTextStripper() {
            @Override
            protected void processTextPosition(org.apache.pdfbox.text.TextPosition text) {
                // Access font size information
                float fontSize = text.getWidth();

                if( Math.round(fontSize) == 14 ){
                    ((StringBuffer)title.get()).append(text.getUnicode());
                }

                // Print text with font size
//                if( text.getUnicode().equals("我") ){
//                    System.out.println("Font Size: " + fontSize + ", Text: " + text.getUnicode());
//                }

                super.processTextPosition(text);
            }
        };

        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {

            for (int page = 1; page <= document.getNumberOfPages(); ++page) {
                hasTitle.set(Boolean.FALSE);
                title.set(new StringBuffer());

                textStripper.setStartPage(page);
                textStripper.setEndPage(page);
                System.out.println( "page " + page );
                String pageText = textStripper.getText(document);
                System.out.println(pageText);
//                System.out.println(StringUtils.substring(((StringBuffer)title.get()).toString(), 0, ((StringBuffer)title.get()).toString().length()/2));
//                String[] paragraphs = pageText.split("\\r\\n");
//
//                // Display paragraphs
//                for (String paragraph : paragraphs) {
//                    System.out.println(paragraph);
//                    System.out.println("-----"); // Separator for clarity
//                }

                // Process pageText line by line
//                String[] lines = pageText.split(System.lineSeparator());
//                for (String line : lines) {
//                    System.out.println("count : " + line.length());
//                    System.out.println(line);
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
