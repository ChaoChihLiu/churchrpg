package com.cpbpc.stt;

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
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAISTTTest {

    public static void main(String[] args) throws IOException, SQLException {
//        System.out.println(OpenAIUtil.suggestedPinyin("当从"));
//        System.out.println(OpenAIUtil.suggestedIntonation("yīng xǔ zhī dì"));

        AppProperties.loadConfig(System.getProperty("app.properties",
                "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-bibleplan-chinese.properties"));
        DBUtil.initStorage(AppProperties.getConfig());

        VerseIntf verseRegex = ThreadStorage.getVerse();
        PhoneticIntf phonetics = ThreadStorage.getPhonetics();

        String original = FileUtils.readFileToString(new File("src/main/resources/chinese-polly-sample.txt"), "UTF-8");
        original = NumberConverter.toChineseNumber(ZhConverterUtil.toSimple(
                TextUtil.removeMultiSpace(
                        PunctuationTool.removePunctuation(
                                verseRegex.convert(
                                        TextUtil.removeLinkBreak(
                                                TextUtil.removeHtmlTag(
                                                        phonetics.reversePhoneticCorrection(original), "")))))));
        System.out.println("original: " + original);

        Gson gson = new Gson();
        Map result = gson.fromJson(OpenAIUtil.speechToText("src/main/resources/crpg20231210.mp3", "zh"), HashMap.class);

        String fromAI = ZhConverterUtil.toSimple(String.valueOf(result.get("text")));
        fromAI = TextUtil.removeMultiSpace(PunctuationTool.removePunctuation(fromAI));
        fromAI = fromAI.replaceAll("圣经 经文第", "圣经经文第");
        System.out.println("from AI : " + fromAI);

        Patch<String> patch = DiffUtils.diff(Arrays.asList(StringUtils.split(original, " ")), Arrays.asList(StringUtils.split(fromAI, " ")));
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            System.out.println(delta);
        }

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(false)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();

        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList(StringUtils.split(original, " ")),
                Arrays.asList(StringUtils.split(fromAI, " ")));

        System.out.println("|original|new|");
        System.out.println("|--------|---|");
        for (DiffRow row : rows) {
            System.out.println("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
        }
    }

}
