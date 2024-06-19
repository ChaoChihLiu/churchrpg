package com.cpbpc.stt;

import com.cpbpc.comms.OpenAIUtil;

import java.io.IOException;
import java.sql.SQLException;

public class OpenAISTTTest {

    public static void main(String[] args) throws IOException, SQLException {
//        String text = "January 1. Morning. Look unto me. The Bible passage is from Isaiah chapter 45 verse 22. This devotion is entitled. Every man looks to the new year with hope. Another new year dawns on planet earth, and we are still exposed to sorrow, Satan, and disappointment. Sin still lives in us, and a thousand things are ready to distress us. But our God says. Look unto me. Look unto me as the source of happiness, the giver of grace, and your friend. Look unto me in every trial, for all you want, and in every place. Look unto me today. I have blessings to bestow. I am waiting to be gracious. I am your Father in Jesus. Believe that I am deeply interested in your present and eternal welfare. That all I have promised I will perform. That I am with you to bless you. I cannot be unconcerned about anything that affects you, and I pledge to make all things work together for your good. You have looked to self, to others, in times past. But you have only met with trouble and disappointment. Now look unto me alone, to me for all. Lift up the iron heart to me today, and every day through the year, and walk before me in peace and holiness. Prove me now herewith if I will not make you holy, useful, and happy. Try me, and find my word of promise true, true to the very letter. Only look unto me. Look to Him, till His wondrous love thy every thought control. It's vast. Constraining power prove. All body. Spirit. Soul.";

//        String result = OpenAIUtil.speechToText("/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/CBS-q2.m4a", "en");
//        Gson gson = new Gson();
//        Map<String, String> map = gson.fromJson(result, HashMap.class);
//        System.out.println(map.get("text"));
//        StringBuffer text = new StringBuffer("question: ");
//        text.append( "Do you see yourself as a confident person, or not? if You do , where does your confidence come from, God or yourself?" );
//        text.append("answer: ");
//        text.append(map.get("text"));
////
////        OpenAIUtil.summarise(map.get("text"));
//        try{
//            OpenAIUtil.summarise(text.toString());
////            OpenAIUtil.summarise(text);
//        }catch (Exception e){
//            e.printStackTrace();
//        }


        System.out.println(OpenAIUtil.suggestedPinyin("当从"));
////        System.out.println(OpenAIUtil.suggestedIntonation("yīng xǔ zhī dì"));
//
//        AppProperties.loadConfig(System.getProperty("app.properties",
//                "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-bibleplan-chinese.properties"));
//        DBUtil.initStorage(AppProperties.getConfig());
//
//        VerseIntf verseRegex = ThreadStorage.getVerse();
//        PhoneticIntf phonetics = ThreadStorage.getPhonetics();
//
//        String original = FileUtils.readFileToString(new File("src/main/resources/chinese-polly-sample.txt"), "UTF-8");
//        original = NumberConverter.toChineseNumber(ZhConverterUtil.toSimple(
//                TextUtil.removeMultiSpace(
//                        PunctuationTool.removePunctuation(
//                                verseRegex.convert(
//                                        TextUtil.removeLinkBreak(
//                                                TextUtil.removeHtmlTag(
//                                                        phonetics.reversePhoneticCorrection(original), "")))))));
//        System.out.println("original: " + original);
//
//        Gson gson = new Gson();
//        Map result = gson.fromJson(OpenAIUtil.speechToText("src/main/resources/crpg20231210.mp3", "zh"), HashMap.class);
//
//        String fromAI = ZhConverterUtil.toSimple(String.valueOf(result.get("text")));
//        fromAI = TextUtil.removeMultiSpace(PunctuationTool.removePunctuation(fromAI));
//        fromAI = fromAI.replaceAll("圣经 经文第", "圣经经文第");
//        System.out.println("from AI : " + fromAI);
//
//        Patch<String> patch = DiffUtils.diff(Arrays.asList(StringUtils.split(original, " ")), Arrays.asList(StringUtils.split(fromAI, " ")));
//        for (AbstractDelta<String> delta : patch.getDeltas()) {
//            System.out.println(delta);
//        }
//
//        DiffRowGenerator generator = DiffRowGenerator.create()
//                .showInlineDiffs(false)
//                .inlineDiffByWord(true)
//                .oldTag(f -> "~")
//                .newTag(f -> "**")
//                .build();
//
//        List<DiffRow> rows = generator.generateDiffRows(
//                Arrays.asList(StringUtils.split(original, " ")),
//                Arrays.asList(StringUtils.split(fromAI, " ")));
//
//        System.out.println("|original|new|");
//        System.out.println("|--------|---|");
//        for (DiffRow row : rows) {
//            System.out.println("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
//        }
    }

}
