package com.cpbpc.remembrance;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.AbbreIntf;
import com.cpbpc.rpgv2.PhoneticIntf;
import com.cpbpc.rpgv2.VerseIntf;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class PDFReader {


    public static void main(String args[]) throws IOException {

        try {
            String propPath = System.getProperty("app.properties");
            FileInputStream in = new FileInputStream(propPath);
            AppProperties.getConfig().load(in);
            DBUtil.initStorage(AppProperties.getConfig());

            AbbreIntf abbr = ThreadStorage.getAbbreviation();
            VerseIntf verse = ThreadStorage.getVerse();
            PhoneticIntf phonetic = ThreadStorage.getPhonetics();

            PdfReader reader = new PdfReader("/Users/liuchaochih/Downloads/Daily+Remembrancer+International+Edition.pdf");
            StringBuilder text = new StringBuilder();
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                String content = PdfTextExtractor.getTextFromPage(reader, i);
                System.out.println("original : " + content);
                content = phonetic.convert(abbr.convert(verse.convert(content)));
                System.out.println("modified : " + content);

                text.append(System.lineSeparator()).append(content);
                if( i == 10 ){
                    break;
                }
            }
//            System.out.println("Text from PDF:\n" + text.toString());
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

}
