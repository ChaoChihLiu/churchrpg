package com.cpbpc.remembrance;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.AbbreIntf;
import com.cpbpc.rpgv2.PhoneticIntf;
import com.cpbpc.rpgv2.VerseIntf;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            for (int i = 3; i <= reader.getNumberOfPages(); i++) {
                StringBuilder text = new StringBuilder();
                String raw = PdfTextExtractor.getTextFromPage(reader, i);
                raw = PunctuationTool.changeFullCharacter(raw);
                System.out.println("original : " + raw);
//                String result = phonetic.convert(abbr.convert(verse.convert(PunctuationTool.changeFullCharacter(RegExUtils.replaceAll(raw, System.lineSeparator(), " ")))));
//                result = PunctuationTool.replacePunctuationWithBreakTag(result).replaceAll("<break", System.lineSeparator() + "<break");
                Parser parser = new Parser(raw);

                String result = text.append( parser.date )
                                    .append(PunctuationTool.pause(800))
                                    .append(parser.moment)
                                    .append(PunctuationTool.pause(800))
                                    .append(parser.title)
                                    .append(PunctuationTool.pause(400))
                                    .append(verse.convert(parser.mainVerse))
                                    .append(PunctuationTool.pause(800))
                                    .append(parser.theme)
                                    .append(phonetic.convert(abbr.convert(verse.convert(PunctuationTool.changeFullCharacter(RegExUtils.replaceAll(parser.content, System.lineSeparator(), " "))))))
                        .toString()
                ;
                result = PunctuationTool.replacePunctuationWithBreakTag(result).replaceAll("<break", System.lineSeparator() + "<break");

                System.out.println("modified : " + result);

                if( i == 10 ){
                    break;
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

}

class Parser{
    protected String raw = "";
    protected String date = "";
    protected String moment = "";
    protected String title = "";
    protected String mainVerse = "";
    protected String theme = "";
    protected String content = "";

    public Parser(String input){
        this.raw = input;
        date = parseDate();
        moment = parseMoment();
        title = parseTitle();
        mainVerse = parseMainVerse();
        theme = parseTheme();
        content = parseContent();
    }

    private String parseMoment() {

        if( StringUtils.isEmpty(date) ){
            date = parseDate();
        }

        String input = StringUtils.replace(raw, date+System.lineSeparator(), "");
        if( input.startsWith("MORNING") ){
            return "MORNING";
        }
        if( input.startsWith("EVENING") ){
            return "EVENING";
        }
        return "";

    }

    private String parseContent() {
        String result = "";

        if( StringUtils.isEmpty(theme) ){
            theme = parseTheme();
        }
        result = StringUtils.substring(raw, StringUtils.indexOf(raw, theme)+theme.length());

        String reversed = StringUtils.reverse(result);
        int index = 0;
        for( char c : reversed.toCharArray() ){
            index++;
            if(!NumberUtils.isCreatable(String.valueOf(c))){
                break;
            }
        }
        result = StringUtils.reverse( StringUtils.substring(reversed, index) );
        
        return result;
    }

    private String parseTheme() {
        String result = "";
        if( StringUtils.isEmpty(mainVerse) ){
            mainVerse = parseMainVerse();
        }

        String input = StringUtils.trim(StringUtils.substring(raw, StringUtils.indexOf(raw, mainVerse) + mainVerse.length()));
        for( char c : input.toCharArray() ){

            if( c == ' '
                    || PunctuationTool.getAllowedPunctuations().contains(String.valueOf(c))
                    || System.lineSeparator().equals(String.valueOf(c)) ){
                result += c;
                continue;
            }
            if( Character.isUpperCase(c) ){
                result += c;
                continue;
            }
            break;
        }

        return StringUtils.trim(result);
    }

    private Pattern monthPattern = Pattern.compile("((January|February|March|April|May|June|July|August|September|October|November|December)\\s{0,}\\d{1,2})");
    private String parseDate() {
        return parseText(monthPattern);
    }

    private String parseText( Pattern pattern ){
        Matcher matcher = pattern.matcher(raw);
        if( matcher.find() ){
            return matcher.group(1);
        }

        return "";
    }

    private Pattern quotePattern = Pattern.compile("\"([^\"]*)\"");
    private String parseTitle() {
        return parseText(quotePattern);
    }

    private VerseIntf verse = ThreadStorage.getVerse();
    private String parseMainVerse() {
        List<String> sentences = List.of(raw.split(System.lineSeparator()));
        if( StringUtils.isEmpty(title) ){
            title = parseTitle();
        }
        return StringUtils.trim(StringUtils.replace(sentences.get(2), "\""+title+"\"", ""));
    }


}