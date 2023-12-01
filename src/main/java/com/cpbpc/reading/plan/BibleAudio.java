package com.cpbpc.reading.plan;

import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class BibleAudio {

    private XSSFWorkbook xssfWorkbook;
    public BibleAudio(XSSFWorkbook workbook){
        this.xssfWorkbook = workbook;
    }

    private static Properties appProperties = AppProperties.getConfig();

    public static void main(String args[]) throws IOException, InvalidFormatException {
        String propPath = System.getProperty("app.properties", "src/main/resources/app-bibleplan-chinese.properties");
        FileInputStream in = null;
        try {
            in = new FileInputStream(propPath);
            appProperties.load(in);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        File file = new File( appProperties.getProperty("reading_plan") );
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        BibleAudio bibleAudio = new BibleAudio(workbook);
        Map<String, String> contents = bibleAudio.convert();
        Set<Map.Entry<String, String>> entries = contents.entrySet();
        for( Map.Entry<String, String> entry : entries ){
            String content = entry.getValue();
            String date = entry.getKey();
            if(StringUtils.isEmpty(content) ){
                continue;
            }
            String pollyScript = AWSUtil.toPolly(content);
            System.out.println(pollyScript);
            AWSUtil.putScriptToS3(content, date);
        }
    }

    private Map<String, String> convert() {
        Map<String, String> result = new HashMap<>();
        if( xssfWorkbook == null ){
            return result;
        }

        VerseIntf verseRegExp = ThreadStorage.getVerse();

        return result;
    }

}
