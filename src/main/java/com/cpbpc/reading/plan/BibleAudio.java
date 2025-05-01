package com.cpbpc.reading.plan;

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
import software.amazon.awssdk.services.s3.model.Tag;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.AWSUtil.createS3Tag;
import static com.cpbpc.comms.PunctuationTool.containHyphen;
import static com.cpbpc.comms.PunctuationTool.getHyphen;
import static com.cpbpc.comms.PunctuationTool.replacePauseTag;
import static com.cpbpc.comms.TextUtil.returnChapterWord;

public class BibleAudio {
    private static Logger logger = Logger.getLogger(BibleAudio.class.getName());
    private static Boolean isTest = true;

    public BibleAudio(){
    }

    private static Properties appProperties = AppProperties.getConfig();

    public static void main(String args[]) throws IOException, InvalidFormatException, SQLException, InterruptedException {
        AppProperties.loadConfig(System.getProperty("app.properties",
                                                    "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-bibleplan-chinese.properties"));
        initStorage();
        isTest = Boolean.valueOf((String)appProperties.getOrDefault("isTest", "true"));

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
            if( result.isEmpty() ){
                continue;
            }
            List<String> verse_to_merged = new ArrayList<>();
            verse_to_merged.add(result.get(0)+"|"+result.get(1));

            Map<String, String> singleChapters = new HashMap<>();

            String book = result.get(0);
            String content = grabBibleVerse(book, result.get(1), chapterBreak);
            String chapterWord = TextUtil.returnChapterWord(book);

            String pl_script = "";

            int startChapter = 0;
            String scriptBucket = AppProperties.getConfig().getProperty("script_bucket");
            String scriptPrefix = AppProperties.getConfig().getProperty("script_prefix");
            if (!scriptPrefix.endsWith("/")) {
                scriptPrefix += "/";
            }
            if( content.contains(chapterBreak) && PunctuationTool.containHyphen(result.get(1)) ){
                String hyphen = PunctuationTool.getHyphen(result.get(1));
                String[] list = StringUtils.split(result.get(1), hyphen);
                startChapter = Integer.valueOf(StringUtils.replace(StringUtils.trim(list[0]), chapterWord, ""));
                int endChapter = Integer.valueOf(StringUtils.replace(StringUtils.trim(list[1]), chapterWord, ""));
                String[] chapterContents = content.split(chapterBreak);

                purgeObject(scriptBucket, scriptPrefix+StringUtils.remove(book, " ")+"/"+verse+".audioMerge");
                int count = 0;
                for( int i = startChapter; i<=endChapter; i++ ){
                    purgeObject(scriptBucket, scriptPrefix+StringUtils.remove(book, " ")+"/"+extractBook(verse)+i+".audioMerge");
                    purgeBucket(appProperties.getProperty("output_bucket"), appProperties.getProperty("output_prefix")+StringUtils.remove(book, " ")+"/"+i);
                    sendToS3( chapterContents[count], book, i );
                    verseCount.put(book + "," + i, StringUtils.split(chapterContents[count], System.lineSeparator()).length+1);

                    singleChapters.put( extractBook(verse)+i, result.get(0)+"|"+i );

                    count++;

                }
                pl_script = StringUtils.join(chapterContents, System.lineSeparator());
            }//end of if
            else{
                int chapterNum = Integer.valueOf(StringUtils.replace(StringUtils.trim(result.get(1)), chapterWord, ""));
                purgeObject(scriptBucket, scriptPrefix+StringUtils.remove(book, " ")+"/"+verse+".audioMerge");
                purgeBucket(scriptBucket, scriptPrefix+StringUtils.remove(book, " ")+"/"+chapterNum);
                String input = content.replace(chapterBreak, "");
                pl_script = input;
                sendToS3(input, book, chapterNum);
                verseCount.put(book + "," + chapterNum, StringUtils.split(input, System.lineSeparator()).length+1);
            }

//            pl_script = PunctuationTool.replacePauseTag(pl_script, "");
            int sleepCounter = 1;
            while( !isTest && !isAllAudioDone(verseCount) ){
                Thread.sleep(sleepCounter*10*1000);
                sleepCounter++;
            }

            List<Tag> tags = new ArrayList<>();
            tags.add(createS3Tag("output_bucket", appProperties.getProperty("output_bucket")));
            tags.add(createS3Tag("output_prefix", appProperties.getProperty("output_prefix")));
            tags.add(createS3Tag("output_format", appProperties.getProperty("output_format")));
            tags.add(createS3Tag("audio_merged_bucket", appProperties.getProperty("audio_merged_bucket")));
            tags.add(createS3Tag("audio_merged_prefix", appProperties.getProperty("audio_merged_prefix")));
            tags.add(createS3Tag("audio_merged_format", appProperties.getProperty("audio_merged_format")));

//            String fileName = StringUtils.trim(result.get(0))
//                    +"_"+
//                    StringUtils.trim(StringUtils.remove(StringUtils.remove(result.get(1), " "), chapterWord));
            String fileName = verse;

            AWSUtil.uploadS3Object( appProperties.getProperty("script_bucket"),
                    appProperties.getProperty("script_prefix")+StringUtils.remove(book, " "),
                    fileName+".audioMerge",
                    StringUtils.remove(joinVerses(verse_to_merged), " "),
                    tags
            );

            breakToSingleChapter(singleChapters, tags, book);

//            String objectKey = appProperties.getProperty("audio_merged_prefix")
//                                + fileName
//                                +"."+
//                                appProperties.getProperty("audio_merged_format");

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

    private static void purgeObject(String outputBucket, String objectKey) {
        if( isTest ){
            return;
        }
        AWSUtil.purgeObject(outputBucket, objectKey);
    }

    private static void purgeBucket( String bucketName, String chapterPrefix ) {
//        AWSUtil.purgeBucket(appProperties.getProperty("output_bucket"), appProperties.getProperty("output_prefix")+book+"/"+i);
        if( isTest ){
            return;
        }
        AWSUtil.purgeBucket(bucketName, chapterPrefix);
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
            pattern = Pattern.compile("[1-3]{0,1}[A-Za-z]+");
        }

        Matcher matcher = pattern.matcher(verse);
        if (matcher.find()) {
            return matcher.group();
        }

        return "";
    }

    private static void breakToSingleChapter(Map<String, String> chapters, List<Tag> tags, String book) {

        Set<Map.Entry<String, String>> entries = chapters.entrySet();
        for( Map.Entry<String, String> entry : entries ){
            AWSUtil.uploadS3Object( appProperties.getProperty("script_bucket"),
                    appProperties.getProperty("script_prefix")+StringUtils.remove(book, " "),
                    entry.getKey()+".audioMerge",
                    StringUtils.remove(entry.getValue(), " ")+",",
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

    private static boolean isThisChapterDone(String book, int chapter, int numberOfVerse)  {
        logger.info("output prefix " + appProperties.getProperty("output_prefix")+StringUtils.remove(book, " ")
                +"/"+chapter+"/");
//        List<S3ObjectSummary> summaries = AWSUtil.listS3Objects(appProperties.getProperty("output_bucket"),
//                                                                appProperties.getProperty("output_prefix")+StringUtils.remove(book, " ")+"/"+chapter+"/");

//        int count = 0;
//        for( S3ObjectSummary summary : summaries ){
//            if( StringUtils.endsWith(summary.getKey(), appProperties.getProperty("output_format")) ){
//                count++;
//            }
//        }
//
//        return count == numberOfVerse;
        Date uploadTime = new Date();
        String prefix = appProperties.getProperty("output_prefix")+StringUtils.remove(book, " ")+"/"+chapter+"/";
        try {
            for (int i = 0; i < numberOfVerse; i++) {
                String objectKey = prefix + i + ".mp3";
                logger.info("prefix " + prefix);
                logger.info("object key " + objectKey);
                AWSUtil.waitUntilObjectReady(appProperties.getProperty("output_bucket"), prefix, objectKey, uploadTime);
            }
        }catch (InterruptedException e){
            logger.info(e.getMessage());
            return false;
        }

        return true;
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
        String toBe = replacePauseTag(PunctuationTool.replaceBiblePunctuationWithBreakTag(content));
        toBe = abbreIntf.convert(toBe);
        toBe = phoneticIntf.convert(toBe);

        String title = breakNewLine(wrapTTS(PunctuationTool.pause(800) + generateTitleAudio(book, chapterNum) + PunctuationTool.pause(800)));

        if( isTest ){
            return;
        }
        AWSUtil.putBibleScriptToS3(phoneticIntf.convert(title), book, String.valueOf(chapterNum), "0");

        String[] verses = StringUtils.split(toBe, System.lineSeparator());
        int verseNum = 1;
        for( String verse : verses ){
            String script = "";
            if( !StringUtils.equalsIgnoreCase(appProperties.getProperty("engine"), "long-form") ){
                script = breakNewLine(wrapTTS(verse));
//                script = breakNewLine(wrapTTS(verse));
            }else{
                script = breakNewLine(wrapTTS(verse));
            }
//            System.out.println(script);
            AWSUtil.putBibleScriptToS3(script, book, String.valueOf(chapterNum), String.valueOf(verseNum));
            verseNum++;
            Thread.sleep(3000);
        }
    }

    private static String breakNewLine(String input) {
        return input.replaceAll("<break", System.lineSeparator()+"<break");
//        return input;
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

    private static String grabBibleVerse(String book, String verse, String chapterBreak) throws IOException {
        if( AppProperties.isChinese() ){
            return BibleVerseGrab.grab(book, verse, chapterBreak, false);
        }
        return com.cpbpc.rpgv2.en.BibleVerseGrab.grab(book, verse, chapterBreak, false);
    }
    
}
