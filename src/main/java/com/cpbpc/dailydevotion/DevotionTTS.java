package com.cpbpc.dailydevotion;

import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AbbreIntf;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.OpenAIUtil;
import com.cpbpc.comms.PhoneticIntf;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.pause;

public class DevotionTTS {

    private static Logger logger = Logger.getLogger(DevotionTTS.class.getName());

    public static void main(String args[]) throws IOException {

        try {
            AppProperties.loadConfig(System.getProperty("app.properties",
                    "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-devotion.properties"));
            DBUtil.initStorage(AppProperties.getConfig());

            AbbreIntf abbr = ThreadStorage.getAbbreviation();
            VerseIntf verse = ThreadStorage.getVerse();
            PhoneticIntf phonetic = ThreadStorage.getPhonetics();

            PdfReader reader = new PdfReader(AppProperties.getConfig().getProperty("input_pdf_path"));
            String month = AppProperties.getConfig().getProperty("month");
            for (int i = 2; i <= reader.getNumberOfPages(); i++) {
                StringBuilder text = new StringBuilder();
                String raw = PdfTextExtractor.getTextFromPage(reader, i);
                raw = PunctuationTool.changeFullCharacter(raw);
                logger.info("original : " + raw);
                OpenAIUtil.textToSpeech(raw, "echo");
//                String result = phonetic.convert(abbr.convert(verse.convert(PunctuationTool.changeFullCharacter(RegExUtils.replaceAll(raw, System.lineSeparator(), " ")))));
//                result = PunctuationTool.replacePunctuationWithBreakTag(result).replaceAll("<break", System.lineSeparator() + "<break");
                Parser parser = new Parser(raw);
                if( !parser.date.contains(month) ){
                    break;
                }

                String book = StringUtils.split(parser.mainVerse, " ")[0];
                String verses = StringUtils.split(parser.mainVerse, " ")[1];

                String result = text.append( parser.date )
                                    .append(PunctuationTool.pause(200))
                                    .append(parser.moment)
                                    .append(PunctuationTool.pause(1600))
                                    .append(parser.quotedVerse)
                                    .append(pause(800))
                                    .append("The Bible passage is from ")
                                    .append(verse.convert(parser.mainVerse))
                                    .append(pause(800))
                                    .append("This devotion is entitled")
                                    .append(PunctuationTool.pause(1600))
                                    .append(parser.theme)
                                    .append(PunctuationTool.pause(400))
//                                    .append(replacePauseTag(replacePunctuationWithPause(phonetic.convert(BibleVerseScraper.scrap(book, verses)))))
                                    .append(phonetic.convert(abbr.convert(verse.convert(PunctuationTool.changeFullCharacter(RegExUtils.replaceAll(parser.content, System.lineSeparator(), " "))))))
                        .toString()
                ;
                result = PunctuationTool.replacePunctuationWithBreakTag(result).replaceAll("<break", System.lineSeparator() + "<break");
                result = AWSUtil.toPolly(result);
                logger.info("modified : " + result);
//                AWSUtil.putScriptToS3(result, parser.date, parser.moment);

                if( i == 3 ){
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
    protected String quotedVerse = "";
    protected String mainVerse = "";
    protected String theme = "";
    protected String content = "";

    public Parser(String input){
        this.raw = input;
        date = parseDate();
        moment = parseMoment();
        quotedVerse = parseQuotedVerse();
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
    private String parseQuotedVerse() {
        return parseText(quotePattern);
    }

    private VerseIntf verse = ThreadStorage.getVerse();
    private String parseMainVerse() {
        List<String> sentences = List.of(raw.split(System.lineSeparator()));
        if( StringUtils.isEmpty(quotedVerse) ){
            quotedVerse = parseQuotedVerse();
        }
        return StringUtils.trim(StringUtils.replace(sentences.get(2), "\""+ quotedVerse +"\"", ""));
    }


}
