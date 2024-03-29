package com.cpbpc.rpgv2.zh;


import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.TextUtil;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.changeFullCharacter;
import static com.cpbpc.comms.TextUtil.removeHtmlTag;


public class BibleVerseGrab {
    private static Logger logger = Logger.getLogger(BibleVerseGrab.class.getName());

    private static final String[] hyphens_unicode = new String[]{"\\u002d", "\\u2010", "\\u2011", "\\u2012", "\\u2013", "\\u2015", "\\u2212"};
    private static Map<String, String> textCache = new HashMap<>();

    private static boolean containHyphen(String verseStr) {

        for (String hyphen_unicode : hyphens_unicode) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verseStr.contains(hyphen)) {
                return true;
            }
        }

        return false;
    }

    private static String getHyphen(String verseStr) {

        for (String hyphen_unicode : hyphens_unicode) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verseStr.contains(hyphen)) {
                return hyphen;
            }
        }

        return "";
    }

    static int toNumber(String s) {
        if (NumberUtils.isCreatable(s)) {
            return NumberUtils.createInteger(s);
        }

        String x = " 一二三四五六七八九十百";
        int l = s.length(),
                i = x.indexOf(s.charAt(l - 1)),
                j = x.indexOf(s.charAt(0));
        if (l < 2) return i; // 1-10
        if (l < 3) {
            if (i == 10) return j * 10; // 20,30,40,50,60,70,80,90
            if (i > 10) return j * 100; // 100,200,300,400,500,600,700,800,900
            return 10 + i; // 11-19
        }
        if (l < 4) return j * 10 + i; // 21-29,31-39,41-49,51-59,61-69,71-79,81-89,91-99
        if (l < 5) return j * 100 + i; // 101-109,201-209,301-309,401-409,501-509,601-609,701-709,801-809,901-909
        return j * 100 + i + x.indexOf(s.charAt(2)) * 10; // 111-119,121-129,131-139,...,971-979,981-989,991-999
    }

    public static void main(String[] args) throws IOException {
        String language = "chinese";
        String propPath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-"+language+".properties";
        FileInputStream in = new FileInputStream(propPath);
        AppProperties.getConfig().load(in);
        //        String verseStr = "三十二篇7-9节";
//        String verseStr = "31篇7,11节";
//        String verseStr = "三章7-9节";
        String book = "詩篇";
//        String book = "羅馬書";
//        String book = "列王纪上";
//        String verseStr = "三十二篇1节-三十六篇";
//        String verseStr = "十八章41至十九章7节";
//        String verseStr = "三十四篇7-9节";
//        String verseStr = "三十四篇";
//        String verseStr = "三十四篇-三十六篇";
        String verseStr = "一百二十七至一百二十八篇";
//        String verseStr = "十二章";
        System.out.println(grab(book, verseStr));
    }

    public static String grab(String book, String verseStr) throws IOException {
        return grab(book, verseStr, "");
    }
    public static String grab(String book, String verseStr, String chapterBreak) throws IOException {

        isFromBackup.set(Boolean.FALSE);

        verseStr = StringUtils.trim(verseStr);
        List<String> result = new ArrayList<>();
        String chapterWord = TextUtil.returnChapterWord(book);
        if ( hasMultipleChapter(verseStr, chapterWord) ) {
            String hyphen = getHyphen(verseStr);
            String[] array = splitVerses( verseStr, List.of(hyphen, "到", "至") );

            List<String> list1 = returnVerses(book, toEndVerses(array[0], chapterWord));
            result.addAll(list1);

            List<String> list2 = new ArrayList<>();
            list2.addAll(returnVerses(book, fromStartVerses(array[1], chapterWord)));

            int startingChapter = toNumber(StringUtils.split(list1.get(0), ".")[0]);
            int endingChapter = toNumber(StringUtils.split(list2.get(0), ".")[0]);
            Locale chineseNumbers = new Locale("C@numbers=hans");
            com.ibm.icu.text.NumberFormat formatter =
                    com.ibm.icu.text.NumberFormat.getInstance(chineseNumbers);
            for (int i = startingChapter + 1; i < endingChapter; i++) {
                result.addAll(returnVerses(book, formatter.format(i) + chapterWord));
            }
            result.addAll(list2);

        } else {
            result.addAll(returnVerses(book, verseStr));
        }
        return attachBibleVerses(book, result, chapterBreak);
    }

    private static String fromStartVerses(String verseStr, String chapterWord) {
        String result = verseStr;
        if( StringUtils.endsWith(result, chapterWord) ){
            result += "1-200节";
        }else if( StringUtils.endsWith(ZhConverterUtil.toSimple(result), "节") ){
            result = StringUtils.substring(verseStr, 0, verseStr.indexOf(chapterWord)+1)+"1-"+StringUtils.substring(verseStr, verseStr.indexOf(chapterWord)+1);
        }
        
        return result;
    }

    private static String toEndVerses(String verseStr, String chapterWord) {

        String result = StringUtils.replace(appendChapterWord(verseStr, chapterWord), "节", "-200节");
        if( !StringUtils.endsWith(result, chapterWord) && !StringUtils.endsWith(ZhConverterUtil.toSimple(result), "节") ){
            result += "-200节";
        }
        
        return result;
    }

    private static String appendChapterWord(String input, String chapterWord) {
        String result = input;
        if( !StringUtils.contains(result, chapterWord) ){
            return  result+chapterWord;
        }

        return result;
    }

    private static boolean hasMultipleChapter(String verseStr, String chapterWord) {

        if( StringUtils.countMatches(verseStr, chapterWord) >= 2
                && (containHyphen(verseStr) || verseStr.contains("到") || verseStr.contains("至")) ){
            return true;
        }

        int hyphenPos = indexOfHyphen(verseStr);
        int chapterWordPos = StringUtils.indexOf(verseStr, chapterWord);
        if( (containHyphen(verseStr) || verseStr.contains("到") || verseStr.contains("至"))
                && StringUtils.countMatches(verseStr, chapterWord) == 1
                && chapterWordPos > hyphenPos ){
            return true;
        }

        return false;
    }

    private static int indexOfHyphen(String verseStr) {
        if( containHyphen(verseStr) ){
            String hyphen = getHyphen(verseStr);
            return StringUtils.indexOf(verseStr, hyphen);
        }

        if( StringUtils.contains(verseStr, "到") ){
            return StringUtils.indexOf(verseStr, "到");
        }

        if( StringUtils.contains(verseStr, "至") ){
            return StringUtils.indexOf(verseStr, "至");
        }

        return -1;
    }

    private static String[] splitVerses(String verseStr, List<String> spliters) {

        for( String spliter : spliters ){
            if( StringUtils.isEmpty(spliter) ){
                continue;
            }
            if( verseStr.contains(spliter) ){
                return verseStr.split(spliter);
            }
        }

        return new String[]{};
    }

    private static String attachBibleVerses(String book, List<String> verses, String chapterBreak) throws IOException {
        StringBuffer buffer = new StringBuffer();
        String currentBookChapter = "";
        for (String verse : verses) {
            String[] array = StringUtils.split(verse, ".");
            int chapterNum = 0;
            int verseNum = 0;

            if (NumberUtils.isCreatable(StringUtils.trim(array[0]))) {
                chapterNum = NumberUtils.toInt(StringUtils.trim(array[0]));
            }
            if (array.length > 1 && NumberUtils.isCreatable(StringUtils.trim(array[1]))) {
                verseNum = NumberUtils.toInt(StringUtils.trim(array[1]));
            }

            if (!currentBookChapter.equals(book + chapterNum)) {
//                buffer.append(book).append(chapterNum);
//                if( book.equals("詩篇") ){
//                    buffer.append("篇");
//                }else{
//                    buffer.append("章");
//                }
                buffer
//                        .append("[pause]")
                        .append(System.lineSeparator());
                currentBookChapter = book + chapterNum;
            }

            if (verseNum == 0) {
                buffer.append(recurBibleVerse("", book, chapterNum, verseNum, chapterBreak));
            } else {
                String result = grabBibleVerse(book, chapterNum, verseNum);
                if (StringUtils.isEmpty(result)) {
                    continue;
                }
                buffer
//                        .append(verseNum).append("節")
//                        .append("[pause]")
                        .append(result).append(System.lineSeparator());
            }
        }

        String result = buffer.toString();
        return changeFullCharacter(result);
    }

    private static String recurBibleVerse(String grabResult, String book, int chapter, int verse, String chapterBreak) throws IOException {
        if (verse == 0) {
            return recurBibleVerse("", book, chapter, 1, chapterBreak);
        }

        StringBuffer buffer = new StringBuffer(grabResult);
        String response = grabBibleVerse(book, chapter, verse);
        if (StringUtils.isEmpty(response)) {
            if( !StringUtils.isEmpty(chapterBreak) ){
                return buffer.toString() + System.lineSeparator() + chapterBreak;
            }
            return buffer.toString();
        }
        buffer
//                .append(verse).append("節")
//                .append("[pause]")
                .append(response).append(System.lineSeparator());

        int i = verse + 1;
        return recurBibleVerse(buffer.toString(), book, chapter, i, chapterBreak);
    }

    private static ThreadLocal isFromBackup = new ThreadLocal();
    private static String grabBibleVerse(String book, int chapter, int verse) throws IOException {
        String bookChapter = book + chapter;
        String text = "";
        if (textCache.containsKey(bookChapter)) {
            text = textCache.get(bookChapter);
        } else {
            Properties bookMapping = AppProperties.readBibleMapping();
            text = readFromInternet(book, Integer.valueOf(bookMapping.getProperty(ZhConverterUtil.toSimple(book))), chapter, false);
            if( !verifyVerse(text, chapter, verse, false) ){
                text = readFromInternet(book, Integer.valueOf(bookMapping.getProperty(ZhConverterUtil.toSimple(book))), chapter, true);
                isFromBackup.set(Boolean.TRUE);
            }
            textCache.put(bookChapter, text);
        }

        String versePattern = createVersePattern(chapter, verse, (Boolean)isFromBackup.get());
        Pattern p = Pattern.compile(versePattern);
        Matcher matcher = p.matcher(text);
        String result = "";
        int groupId = 1;
        if( (Boolean)isFromBackup.get() ){
            groupId+=1;
        }
        if (matcher.find()) {
            logger.info(matcher.group(groupId));
            result = ZhConverterUtil.toSimple(StringUtils.remove(removeHtmlTag(matcher.group(groupId)), " "));
        }

        return result;
    }

    private static boolean verifyVerse(String text, int chapter, int verse, boolean isFormBackup) {
        String versePattern = createVersePattern(chapter, verse, isFormBackup);
        Pattern p = Pattern.compile(versePattern);
        Matcher matcher = p.matcher(text);
        return matcher.find();
    }

    /*
    <td>8:21</td>
   <td>
      <div class="verse_list">                                    西巴和撒慕拿说，你自己起来杀我们吧。因为人如何，力量也是如何。基甸就起来，杀了西巴和撒慕拿，夺获他们骆驼项上戴的月牙圈。                                </div>
   </td>
     */
    private static String createVersePattern(int chapterNumber, int verseNumber, boolean fromBackup) {
        if( !fromBackup ){
            return "<td>"+chapterNumber+":"+verseNumber+"</td>\\s*<td>\\s*<div\\s+class=\"verse_list\">\\s*([^<]+)\\s*</div>\\s*</td>";
        }

        if (verseNumber == 1) {
            return "(<span\\s{1,}class=\"chapternum\">" + chapterNumber + "[\\u00A0|&nbsp;]</span>)([^<>]*)(</span>)";
        }

        return "(<sup\\s{1,}class=\"versenum\">" + verseNumber + "[\\u00A0|&nbsp;]</sup>)([^<>]*)(</span>)";
    }

    private static String readFromInternet(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
//        System.out.println("Response code: " + responseCode);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String html = response.toString();
//        System.out.println(html);

        return html;
    }

    private static String readFromInternet(String book, int bookNumber, int chapterNumber, boolean fromBackup) throws IOException {

        String backup_url = "https://www.biblegateway.com/passage/?search=" + URLEncoder.encode(book+chapterNumber) + "&version="+ AppProperties.getConfig().getProperty("bible_version");
        String url = "http://www.edzx.com/bible/read/?id=1&volume="+bookNumber+"&chapter="+chapterNumber;
        String html = "";
        if( fromBackup ){
            html = readFromInternet(backup_url);
        } else{
            html = readFromInternet(url);
        }
        return html;
    }

    private static List<String> returnVerses(String book, String verseStr) {
        int chapter = 0;
        String chapterWord = TextUtil.returnChapterWord(book);
        if (verseStr.contains(chapterWord)) {
            chapter = toNumber(StringUtils.substring(verseStr, 0, verseStr.indexOf(chapterWord)));
        }
       
        String verses = "";
        if (verseStr.contains(chapterWord)) {
            verses = StringUtils.substring(verseStr, verseStr.indexOf(chapterWord) + 1);
        }
       
        if (verses.contains("节") || verses.contains("節")) {
//            int index = (verses.endsWith("节")) ? verses.indexOf("节") : (verses.endsWith("節")) ? verses.indexOf("節") : 0;
//            verses = StringUtils.substring(verses, 0, index);
            verses = StringUtils.replace(verses, "节", "").replace("節", "");
        }

        List<String> result = new ArrayList<>();
        if ((containHyphen(verses) || verses.contains("到") || verses.contains("至"))) {
            String hyphen = getHyphen(verses);
            String[] array = splitVerses( verses, List.of(hyphen, "到", "至") );
            int start = toNumber(array[0]);
            int end = toNumber(array[1]);

            for (int i = start; i <= end; i++) {
                result.add(chapter + "." + i);
            }
        } else if (verses.contains(",")) {
            String array[] = verses.split(",");
            for (String verse_ind : array) {
                result.add(chapter + "." + verse_ind);
            }
        } else {
            result.add(chapter + "." + verses);
        }

        return result;
    }

}
