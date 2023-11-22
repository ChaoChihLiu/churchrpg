package com.cpbpc;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;

public class PDFReader {

    public static void main(String args[]) throws IOException {
        try {
            PdfReader reader = new PdfReader("/Users/liuchaochih/Downloads/Daily+Remembrancer+International+Edition.pdf");
            StringBuilder text = new StringBuilder();
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                text.append(System.lineSeparator()).append(PdfTextExtractor.getTextFromPage(reader, i));
            }
            System.out.println("Text from PDF:\n" + text.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
