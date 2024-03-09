package com.cpbpc.dailydevotion;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFReader {

    public static void main(String[] args) {
//        String pdfFilePath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources//Daily+Remembrancer+International+Edition.pdf";
        String pdfFilePath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/24_apr-to-jun.pdf";

        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();

            for (int page = 1; page <= document.getNumberOfPages(); ++page) {
                System.out.println("****************");
                pdfTextStripper.setStartPage(page);
                pdfTextStripper.setEndPage(page);

                String pageText = pdfTextStripper.getText(document);

                // Process pageText line by line
                String[] lines = pageText.split(System.lineSeparator());
                for (String line : lines) {
                    System.out.println(line);
                }
                System.out.println("****************");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
