package com.cpbpc.reading.plan;

import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.SpreadSheetReader;
import com.cpbpc.comms.TextUtil;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.PhoneticIntf;
import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static com.cpbpc.comms.PunctuationTool.replacePauseTag;
import static com.cpbpc.comms.PunctuationTool.replacePunctuationWithPause;
import static com.cpbpc.comms.TextUtil.returnChapterWord;

public class BibleAudio {
    private static Logger logger = Logger.getLogger(BibleAudio.class.getName());

    public BibleAudio(){
    }

    private static Properties appProperties = AppProperties.getConfig();

    public static void main(String args[]) throws IOException, InvalidFormatException, SQLException, InterruptedException {
        AppProperties.loadConfig(System.getProperty("app.properties",
                                                    "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-bibleplan-english.properties"));

        initStorage();
        PhoneticIntf phoneticIntf = ThreadStorage.getPhonetics();

        BibleAudio bibleAudio = new BibleAudio();
        List<String> verses = new ArrayList<>();
        if( appProperties.containsKey("reading_plan") ){
            File file = new File( appProperties.getProperty("reading_plan") );
            verses.addAll(SpreadSheetReader.readVerseFromXlsx(file));
        }
        if( appProperties.containsKey("day_plan") ){
            verses.addAll(List.of(StringUtils.split(appProperties.getProperty("day_plan"), ",")));
        }

        logger.info("appProperties : " + appProperties.toString());
        logger.info("verses : " + verses.toString());

        String chapterBreak = "_______";
        for( String verse : verses ){
            List<String> result = analyseVerse(verse);
            String book = result.get(0);
            String content = phoneticIntf.convert(scrapBibleVerse(book, result.get(1), chapterBreak));
            String chapterWord = TextUtil.returnChapterWord(book);

            int startChapter = 0;
            if( content.contains(chapterBreak) && PunctuationTool.containHyphen(result.get(1)) ){
                String hyphen = PunctuationTool.getHyphen(result.get(1));
                String[] list = StringUtils.split(result.get(1), hyphen);
                startChapter = Integer.valueOf(StringUtils.replace(StringUtils.trim(list[0]), chapterWord, ""));
                int endChapter = Integer.valueOf(StringUtils.replace(StringUtils.trim(list[1]), chapterWord, ""));
                String[] chapterContents = content.split(chapterBreak);
                for( String chapter : chapterContents ){
                    System.out.println(chapter);
                }
                int count = 0;
                for( int i = startChapter; i<=endChapter; i++ ){
                    sendToS3( chapterContents[count], book, i );
                    count++;
                }
            }//end of if
            else{
                String input = content.replace(chapterBreak, "");
                sendToS3(input, book, Integer.valueOf(StringUtils.replace(StringUtils.trim(result.get(1)), chapterWord, "")));
            }

        }//end of for loop verses

//        Thread.sleep(10 * 60 * 1000);
//        AudioMerger.mergeMp3(verses);
    }

    private static List<String> analyseVerse(String verse) {

        VerseIntf verseRegex = ThreadStorage.getVerse();
        List<String> result = verseRegex.analyseVerse(verse);

        if( AppProperties.isChinese() ){
            String book = result.get(0);
            for( int i=1; i<result.size(); i++ ){
                String chapterWord = returnChapterWord(book);
                if( !StringUtils.endsWith(result.get(i), chapterWord) ){
                    result.set(i, result.get(i)+chapterWord);
                }
            }
        }

        return result;
    }

    private static void sendToS3(String content, String book, int chapterNum) throws IOException, InterruptedException {

        PhoneticIntf phoneticIntf = ThreadStorage.getPhonetics();
        String toBe = replacePauseTag(replacePunctuationWithPause(content));
        toBe = phoneticIntf.convert(toBe);

        String title = AWSUtil.toPolly(PunctuationTool.pause(800) + generateTitleAudio(book, chapterNum) + PunctuationTool.pause(800));
        AWSUtil.putBibleScriptToS3(title, book, String.valueOf(chapterNum), "0");

        String[] verses = StringUtils.split(toBe, System.lineSeparator());
        int verseNum = 1;
        for( String verse : verses ){
            String script = "";
            if( !StringUtils.equalsIgnoreCase(appProperties.getProperty("engine"), "long-form") ){
                script = AWSUtil.toPolly(PunctuationTool.replacePunctuationWithBreakTag(verse));
            }else{
                Thread.sleep(3000);
                script = AWSUtil.toPolly(verse);
            }
//            System.out.println(script);
            AWSUtil.putBibleScriptToS3(script, book, String.valueOf(chapterNum), String.valueOf(verseNum));
            verseNum++;
        }
    }

    private static String generateTitleAudio(String book, int chapterNum) {
        if( AppProperties.isChinese() ){
            return  book + chapterNum + returnChapterWord(book);
        }

        if( StringUtils.equalsIgnoreCase(book, "psalm") || StringUtils.equalsIgnoreCase(book, "psalms") ){
            return  book + chapterNum;
        }

        return  book +" chapter " + chapterNum;

    }


    private static void initStorage() throws SQLException {
        DBUtil.initStorage(appProperties);
    }

    private static String scrapBibleVerse(String book, String verse, String chapterBreak) throws IOException {
        if( AppProperties.isChinese() ){
            return com.cpbpc.rpgv2.zh.BibleVerseScraper.scrap(book, verse, chapterBreak);
        }
        return com.cpbpc.rpgv2.en.BibleVerseScraper.scrap(book, verse, chapterBreak);
    }
    
}
