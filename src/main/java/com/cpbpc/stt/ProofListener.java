package com.cpbpc.stt;

import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.NumberConverter;
import com.cpbpc.comms.OpenAIUtil;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.TextUtil;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.PhoneticIntf;
import com.cpbpc.rpgv2.VerseIntf;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProofListener {

    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        AppProperties.loadConfig(System.getProperty("app.properties",
                "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-proof-listener-chinese.properties"));
        DBUtil.initStorage(AppProperties.getConfig());

        String publish_date = System.getProperty("publish.date");
        String publish_month = System.getProperty("publish.month");

        if( !StringUtils.isEmpty(publish_date) ){
            AppProperties.getConfig().setProperty("publish_date", publish_date);
        }

        if( !StringUtils.isEmpty(publish_month) ){
            AppProperties.getConfig().setProperty("publish_month", publish_month);
        }

        ProofListener pl = new ProofListener();
        pl.process();
    }

    private void process() throws IOException, InterruptedException {
        if( AppProperties.getConfig().containsKey("publish_date") ){
            String date = AppProperties.getConfig().getProperty("publish_date", "2024-01-14");
            proofListen1Day(date);
        }
        if( AppProperties.getConfig().containsKey("publish_month") ){
            String yearMonth = AppProperties.getConfig().getProperty("publish_month", "2024-01");
            proofListen1Month(yearMonth);
        }
    }

    private void proofListen1Month(String yearMonth) throws IOException, InterruptedException {
        String year = StringUtils.split(yearMonth, "-")[0];
        String month = StringUtils.split(yearMonth, "-")[1];

        LocalDate currentDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 01);
        LocalDate lastDayOfMonth = YearMonth.from(currentDate).atEndOfMonth();

        int lastDay = lastDayOfMonth.getDayOfMonth();
        for( int i =1; i<=lastDay; i++ ){
            String date_str = String.valueOf(i);
            if( i<10 ){
                date_str = "0"+i;
            }
            Thread.sleep(20*1000);
            proofListen1Day(yearMonth+"-"+date_str);
        }
    }

    private void proofListen1Day(String date) throws IOException {

        String script = loadPollyScript(date);
        String audioFilePath = loadAudio(date);

        String language = AppProperties.getConfig().getProperty("language");
        Gson gson = new Gson();
        Map result = gson.fromJson(OpenAIUtil.speechToText(audioFilePath, language), HashMap.class);
        String fromAI = ZhConverterUtil.toSimple(String.valueOf(result.get("text")));
        fromAI = TextUtil.removeMultiSpace(PunctuationTool.removePunctuation(fromAI));
        fromAI = fromAI.replaceAll("圣经 经文第", "圣经经文第");

        VerseIntf verseRegex = ThreadStorage.getVerse();
        PhoneticIntf phonetics = ThreadStorage.getPhonetics();
        script = NumberConverter.toChineseNumber(ZhConverterUtil.toSimple(
                TextUtil.removeMultiSpace(
                        PunctuationTool.removePunctuation(
                                        TextUtil.removeLinkBreak(
                                                TextUtil.removeHtmlTag(
                                                        phonetics.reversePhoneticCorrection(script), ""))))));
        System.out.println("******Start of "+date+"******");
        System.out.println("original: " + script);
        System.out.println("from AI : " + fromAI);

        Patch<String> patch = DiffUtils.diff(Arrays.asList(StringUtils.split(script, " ")), Arrays.asList(StringUtils.split(fromAI, " ")));
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            String oringal = StringUtils.remove(PunctuationTool.removePunctuation(StringUtils.join(delta.getSource().getLines())), " ");
            String aiRead =  StringUtils.remove(PunctuationTool.removePunctuation(StringUtils.join(delta.getTarget().getLines())), " ");

            if( StringUtils.equals(TextUtil.convertToPinyin(oringal), TextUtil.convertToPinyin(aiRead)) ){
                continue;
            }

            System.out.println("----------");
            System.out.println("original: " + StringUtils.join(delta.getSource().getLines()));
            System.out.println("from AI : " + StringUtils.join(delta.getTarget().getLines()));
        }
        System.out.println("******End of "+date+"******");

    }

    private String loadAudio(String publishDate) {
        String year = publishDate.split("-")[0];
        String month = publishDate.split("-")[1];

        String fileName = AppProperties.getConfig().getProperty("file_name_prefix")
                +StringUtils.remove(publishDate, "-")+"."
                +AppProperties.getConfig().getProperty("audio_format");
        String bucketName = AppProperties.getConfig().getProperty("audio_bucket");
        String objectKey = AppProperties.getConfig().getProperty("audio_prefix")
                +year+"_"+month+"/"
                +fileName;

        File dir = new File( AppProperties.getConfig().getProperty("local_audio_path") );
        if( !dir.exists() ){
            dir.mkdirs();
        }

        String local_file_path = dir.getAbsolutePath()+"/"+fileName;
        AWSUtil.downloadS3Object( bucketName, objectKey, local_file_path);

        return local_file_path;
    }

    private String loadPollyScript(String publishDate) throws IOException {
        String year = publishDate.split("-")[0];
        String month = publishDate.split("-")[1];

        String fileName = AppProperties.getConfig().getProperty("file_name_prefix")
                +StringUtils.remove(publishDate, "-")+"."
                +AppProperties.getConfig().getProperty("script_format");
        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
        String objectKey = AppProperties.getConfig().getProperty("script_prefix")
                            +year+"_"+month+"/"
                            +fileName;

        File dir = new File( AppProperties.getConfig().getProperty("local_script_path") );
        if( !dir.exists() ){
            dir.mkdirs();
        }

        String local_file_path = dir.getAbsolutePath()+"/"+fileName;
        AWSUtil.downloadS3Object( bucketName, objectKey, local_file_path);

        return IOUtils.toString(new FileReader(local_file_path));
    }


}
