package com.cpbpc.reading.plan;

import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.PhoneticIntf;
import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BibleAudio {

    private XSSFWorkbook xssfWorkbook;
    public BibleAudio(XSSFWorkbook workbook){
        this.xssfWorkbook = workbook;
    }

    private static Properties appProperties = AppProperties.getConfig();

    public static void main(String args[]) throws IOException, InvalidFormatException, SQLException, InterruptedException {
        AppProperties.loadConfig("src/main/resources/app-bibleplan-english.properties");
        
        File file = new File( appProperties.getProperty("reading_plan") );
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        BibleAudio bibleAudio = new BibleAudio(workbook);
        List<String> verses = bibleAudio.convert();
        initStorage();
        VerseIntf verseRegex = ThreadStorage.getVerse();

        String chapterBreak = "------";
        for( String verse : verses ){
            List<String> result = verseRegex.analyseVerse(verse);
            String book = result.get(0);
            String content = scrapBibleVerse(book, result.get(1), chapterBreak);

            int startChapter = 0;
            if( content.contains(chapterBreak) && PunctuationTool.containHyphen(result.get(1)) ){
                String hyphen = PunctuationTool.getHyphen(result.get(1));
                String[] list = StringUtils.split(result.get(1), hyphen);
                startChapter = Integer.valueOf(list[0]);
                int endChapter = Integer.valueOf(list[1]);
                String[] chapterContents = StringUtils.split(content, chapterBreak);
                int count = 0;
                for( int i = startChapter; i<=endChapter; i++ ){
                    sendToS3( chapterContents[count], book, i );
                    count++;
                }
            }//end of if
            else{
                String input = content.replace(chapterBreak, "");
                sendToS3(input, book, Integer.parseInt(result.get(1)));
            }

        }//end of for loop verses
    }

    private static void sendToS3(String content, String book, int chapterNum) throws IOException, InterruptedException {

        PhoneticIntf phoneticIntf = ThreadStorage.getPhonetics();
        String toBe = PunctuationTool.replacePauseTag(content, "");
        toBe = phoneticIntf.convert(toBe);

        String title = AWSUtil.toPolly(PunctuationTool.pause(800) + book +" chapter " + chapterNum + PunctuationTool.pause(800));
        AWSUtil.putBibleScriptToS3(title, book, String.valueOf(chapterNum), "0");

        String[] verses = StringUtils.split(toBe, System.lineSeparator());
        int verseNum = 1;
        for( String verse : verses ){
            Thread.sleep(1000);
            String script = AWSUtil.toPolly(PunctuationTool.replacePunctuationWithBreakTag(verse));
            AWSUtil.putBibleScriptToS3(script, book, String.valueOf(chapterNum), String.valueOf(verseNum));
            verseNum++;
        }
    }

    private static void initStorage() throws SQLException {
        DBUtil.initStorage(appProperties);
    }

    private static String scrapBibleVerse(String book, String verse, String chapterBreak) throws IOException {
        if( appProperties.getProperty("language").equals("zh") ){
            return com.cpbpc.rpgv2.zh.BibleVerseScraper.scrap(book, verse, chapterBreak);
        }
        return com.cpbpc.rpgv2.en.BibleVerseScraper.scrap(book, verse, chapterBreak);
    }

    private List<String> convert() {
        List<String> result = new ArrayList<>();
        if( xssfWorkbook == null ){
            return result;
        }

        int startRow = parseStartRowIndex();
        int endRow = parseEndRowIndex();
        int cellIndex = parseCellIndex();

//        wb.getSheetAt(0).getRow(9).getCell(4);
        XSSFSheet sheet = xssfWorkbook.getSheetAt(Integer.parseInt(appProperties.getProperty("sheet_index")));
        for( int i = startRow; i<=endRow; i++ ){
            String value = sheet.getRow(i).getCell(cellIndex).getStringCellValue();
            result.add(value);
        }

//        return result;
        return List.of("Gen 4-6");
    }

    private int parseCellIndex() {
        String range = appProperties.getProperty("range");
        return CellReference.convertColStringToIndex(StringUtils.substring(range, 0, 1));
    }

    private int parseEndRowIndex() {
        String range = appProperties.getProperty("range");
        String result = StringUtils.substring(StringUtils.split(range, "-")[1], 1);
        return Integer.parseInt(result)-1;
    }

    private int parseStartRowIndex() {
        String range = appProperties.getProperty("range");
        String result = StringUtils.substring(StringUtils.split(range, "-")[0], 1);
        return Integer.parseInt(result)-1;
    }

}
