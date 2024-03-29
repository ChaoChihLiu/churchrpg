package com.cpbpc.reading.plan;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.Tag;
import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AbbreIntf;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.AzureUtil;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.PhoneticIntf;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.SpreadSheetReader;
import com.cpbpc.comms.TextUtil;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.URLShortener;
import com.cpbpc.comms.VerseIntf;
import com.cpbpc.rpgv2.zh.BibleVerseGrab;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.containHyphen;
import static com.cpbpc.comms.PunctuationTool.getHyphen;
import static com.cpbpc.comms.PunctuationTool.replacePauseTag;
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

        List<String> verses = new ArrayList<>();
        List<String> objectURLs = new ArrayList<>();
        if( appProperties.containsKey("reading_plan") ){
            File file = new File( appProperties.getProperty("reading_plan") );
            verses.addAll(SpreadSheetReader.readVerseFromXlsx(file));
        }
        if( appProperties.containsKey("day_plan") ){
            verses.addAll(List.of(StringUtils.split(URLDecoder.decode(appProperties.getProperty("day_plan")), ",")));
        }

        String chapterBreak = "_______";
        Map<String, Integer> verseCount = new HashMap<>();
        for( String verse : verses ){
            verseCount.clear();
            List<String> result = analyseVerse(verse);
            List<String> verse_to_merged = new ArrayList<>();
            verse_to_merged.add(result.get(0)+"|"+result.get(1));

            Map<String, String> singleChapters = new HashMap<>();

            String book = result.get(0);
            String content = scrapBibleVerse(book, result.get(1), chapterBreak);
            String chapterWord = TextUtil.returnChapterWord(book);

            String pl_script = "";

            int startChapter = 0;
            if( content.contains(chapterBreak) && PunctuationTool.containHyphen(result.get(1)) ){
                String hyphen = PunctuationTool.getHyphen(result.get(1));
                String[] list = StringUtils.split(result.get(1), hyphen);
                startChapter = Integer.valueOf(StringUtils.replace(StringUtils.trim(list[0]), chapterWord, ""));
                int endChapter = Integer.valueOf(StringUtils.replace(StringUtils.trim(list[1]), chapterWord, ""));
                String[] chapterContents = content.split(chapterBreak);

                int count = 0;
                for( int i = startChapter; i<=endChapter; i++ ){
                    AWSUtil.purgeBucket(appProperties.getProperty("output_bucket"), appProperties.getProperty("output_prefix")+book+"/"+i);
                    sendToS3( chapterContents[count], book, i );
                    verseCount.put(book + "," + i, StringUtils.split(chapterContents[count], System.lineSeparator()).length+1);

                    singleChapters.put( extractBook(verse)+i, result.get(0)+"|"+i );

                    count++;
                }
                pl_script = StringUtils.join(chapterContents, System.lineSeparator());
            }//end of if
            else{
                int chapterNum = Integer.valueOf(StringUtils.replace(StringUtils.trim(result.get(1)), chapterWord, ""));
                AWSUtil.purgeBucket(appProperties.getProperty("output_bucket"), appProperties.getProperty("output_prefix")+book+"/"+chapterNum);
                String input = content.replace(chapterBreak, "");
                pl_script = input;
                sendToS3(input, book, chapterNum);
                verseCount.put(book + "," + chapterNum, StringUtils.split(input, System.lineSeparator()).length+1);
            }

            pl_script = PunctuationTool.replacePauseTag(pl_script, "");
            while( !isAllAudioDone(verseCount) ){
                Thread.sleep(10*1000);
            }

            List<Tag> tags = new ArrayList<>();
            tags.add(new Tag("output_bucket", appProperties.getProperty("output_bucket")));
            tags.add(new Tag("output_prefix", appProperties.getProperty("output_prefix")));
            tags.add(new Tag("output_format", appProperties.getProperty("output_format")));
            tags.add(new Tag("audio_merged_bucket", appProperties.getProperty("audio_merged_bucket")));
            tags.add(new Tag("audio_merged_prefix", appProperties.getProperty("audio_merged_prefix")));
            tags.add(new Tag("audio_merged_format", appProperties.getProperty("audio_merged_format")));

//            String fileName = StringUtils.trim(result.get(0))
//                    +"_"+
//                    StringUtils.trim(StringUtils.remove(StringUtils.remove(result.get(1), " "), chapterWord));
            String fileName = verse;
            
            AWSUtil.uploadS3Object( appProperties.getProperty("script_bucket"),
                    appProperties.getProperty("script_prefix"),
                    fileName+".audioMerge",
                    joinVerses(verse_to_merged),
                    tags
            );

            breakToSingleChapter(singleChapters, tags);

            String objectKey = appProperties.getProperty("audio_merged_prefix")
                                + fileName
                                +"."+
                                appProperties.getProperty("audio_merged_format");

//            if( AppProperties.isChinese() ){
//                logger.info("wait this file " + objectKey);
//                AWSUtil.waitUntilObjectReady( appProperties.getProperty("audio_merged_bucket"),
//                        appProperties.getProperty("audio_merged_prefix"),
//                        objectKey,
//                        new Date());
//                AWSUtil.putBiblePLScriptToS3(pl_script, StringUtils.remove(verse, " "), objectKey);
//            }

            String url = "https://"+appProperties.getProperty("audio_merged_bucket")+".s3."+appProperties.getProperty("region")+".amazonaws.com/"+appProperties.getProperty("audio_merged_prefix")+ URLEncoder.encode(verse) +".mp3";
            if( !StringUtils.equals(appProperties.getProperty("shorten_url"), "true") ){
                objectURLs.add(url);
            }else{
                objectURLs.add(URLShortener.shorten(url));
            }
            
        }//end of for loop verses

        logger.info("all links " + StringUtils.join(objectURLs, System.lineSeparator()));

    }

    private static String joinVerses(List<String> verseToMerged) {

        String result = StringUtils.join(verseToMerged, ",");
        if( !StringUtils.endsWith(result, ",") ){
            return result+",";
        }

        return result;
    }

    private static String extractBook(String verse) {

        Pattern pattern = null;
        if( AppProperties.isChinese() ){
            pattern = Pattern.compile("[\u4e00-\u9fa5]+");
        }else{
            pattern = Pattern.compile("[A-Za-z]+");
        }

        Matcher matcher = pattern.matcher(verse);
        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    private static void breakToSingleChapter(Map<String, String> chapters, List<Tag> tags) {

        Set<Map.Entry<String, String>> entries = chapters.entrySet();
        for( Map.Entry<String, String> entry : entries ){
            AWSUtil.uploadS3Object( appProperties.getProperty("script_bucket"),
                    appProperties.getProperty("script_prefix"),
                    entry.getKey()+".audioMerge",
                    entry.getValue()+",",
                    tags
            );
        }
    }

    private static boolean isAllAudioDone(Map<String, Integer> verseCount) {

        Set<Map.Entry<String, Integer>> entrySet = verseCount.entrySet();
        for( Map.Entry<String, Integer> entry : entrySet ){
            String book = StringUtils.trim(StringUtils.split(entry.getKey(), ",")[0]);
            int chapter = Integer.parseInt(StringUtils.trim(StringUtils.split(entry.getKey(), ",")[1]));
            int numberOfVerse = entry.getValue();

            if( !isThisChapterDone(book,chapter, numberOfVerse) ){
                return false;
            }
        }


        return true;
    }

    private static boolean isThisChapterDone(String book, int chapter, int numberOfVerse) {
        List<S3ObjectSummary> summaries = AWSUtil.listS3Objects(appProperties.getProperty("output_bucket"),
                                                                appProperties.getProperty("output_prefix")+book+"/"+chapter+"/");

        int count = 0;
        for( S3ObjectSummary summary : summaries ){
            if( StringUtils.endsWith(summary.getKey(), appProperties.getProperty("output_format")) ){
                count++;
            }
        }

        return count == numberOfVerse;
    }

    private static List<String> analyseVerse(String verse) {

        VerseIntf verseRegex = ThreadStorage.getVerse();
        List<String> result = verseRegex.analyseVerse(verse);

        if( AppProperties.isChinese() ){
            String book = result.get(0);
            String verseStr = result.get(1);
            for( int i=1; i<result.size(); i++ ){
                String chapterWord = returnChapterWord(book);
                if( !containHyphen(verseStr) && !StringUtils.endsWith(result.get(i), chapterWord) ){
                    result.set(i, result.get(i)+chapterWord);
                }
                if( containHyphen(verseStr) ){
                    String hyphen = getHyphen(verseStr);
                    String[] tmp = StringUtils.split(verseStr, hyphen);
                    StringBuilder builder = new StringBuilder();
                    for( String input : tmp ){
                        if( !StringUtils.contains(input, chapterWord) ){
                            builder.append( input ).append(chapterWord);
                        }else{
                            builder.append( input );
                        }
                        builder.append(hyphen);
                    }

                    String output = builder.toString();
                    if( StringUtils.endsWith(output, hyphen) ){
                        output = StringUtils.substring(output, 0, output.length()-1);
                    }

                    result.set(i, output);
                }
            }
        }

        return result;
    }

    private static void sendToS3(String content, String book, int chapterNum) throws IOException, InterruptedException {

        PhoneticIntf phoneticIntf = ThreadStorage.getPhonetics();
        AbbreIntf abbreIntf = ThreadStorage.getAbbreviation();
        String toBe = replacePauseTag(content);
        toBe = phoneticIntf.convert(abbreIntf.convert(toBe));

        String title = breakNewLine(wrapTTS(PunctuationTool.pause(800) + generateTitleAudio(book, chapterNum) + PunctuationTool.pause(800)));

        AWSUtil.putBibleScriptToS3(title, book, String.valueOf(chapterNum), "0");

        String[] verses = StringUtils.split(toBe, System.lineSeparator());
        int verseNum = 1;
        for( String verse : verses ){
            String script = "";
            if( !StringUtils.equalsIgnoreCase(appProperties.getProperty("engine"), "long-form") ){
//                script = breakNewLine(wrapTTS(PunctuationTool.replaceBiblePunctuationWithBreakTag(verse)));
                script = breakNewLine(wrapTTS(verse));
            }else{
                Thread.sleep(3000);
                script = breakNewLine(wrapTTS(verse));
            }
//            System.out.println(script);
            AWSUtil.putBibleScriptToS3(script, book, String.valueOf(chapterNum), String.valueOf(verseNum));
            verseNum++;
        }
    }

    private static String breakNewLine(String input) {
//        return input.replaceAll("<break", System.lineSeparator()+"<break");
        return input;
    }

    private static String wrapTTS( String content ){
        String result = "";
        if( AppProperties.isAWS() ){
            result = AWSUtil.toPolly(content);
        }
        if( AppProperties.isAzure() ){
            result = AzureUtil.toTTS(content);
        }

        return result;
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
            return BibleVerseGrab.grab(book, verse, chapterBreak);
        }
        return com.cpbpc.rpgv2.en.BibleVerseGrab.grab(book, verse, chapterBreak);
    }
    
}
