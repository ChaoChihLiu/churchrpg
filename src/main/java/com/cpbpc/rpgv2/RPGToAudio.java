package com.cpbpc.rpgv2;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.GetSpeechSynthesisTaskRequest;
import com.amazonaws.services.polly.model.GetSpeechSynthesisTaskResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.StartSpeechSynthesisTaskRequest;
import com.amazonaws.services.polly.model.StartSpeechSynthesisTaskResult;
import com.amazonaws.services.polly.model.TaskStatus;
import com.amazonaws.services.polly.model.TextType;
import com.amazonaws.services.polly.model.VoiceId;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.Tag;
import com.cpbpc.rpgv2.util.AWSUtil;
import org.apache.commons.lang3.RegExUtils;

import org.awaitility.Duration;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
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

import static org.awaitility.Awaitility.await;

//public class com.cpbpc.rpg.RPGToAudio implements RequestHandler {
public class RPGToAudio {

    private static final Properties appProperties = AppProperties.getProperties();
    private static final AmazonS3 s3Client = AmazonS3Client.builder()
            .withCredentials(new DefaultAWSCredentialsProviderChain())
            .build();
    private static final int SYNTHESIS_TASK_TIMEOUT_SECONDS = 300;
    private static final AmazonPolly AMAZON_POLLY_CLIENT = AmazonPollyClientBuilder.defaultClient();
    //    private static final String SNS_TOPIC_ARN = "arn:aws:sns:eu-west-2:123456789012:synthesize-finish-topic";
    private static final Duration SYNTHESIS_TASK_POLL_INTERVAL = Duration.FIVE_SECONDS;
    private static final Duration SYNTHESIS_TASK_POLL_DELAY = Duration.TEN_SECONDS;
    private static final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    private final List<Pattern> quote_patterns = List.of(Pattern.compile("(\")(([^\"]|\\\\\")*)(\")"), Pattern.compile("(“)(([^“])*)(”)"));
    private long previousUsage = 0;
    private long pollyLimit = 0;
    private Logger logger = Logger.getLogger(RPGToAudio.class.getName());

    public RPGToAudio(long previousUsage, long pollyLimit) {
        this.previousUsage = previousUsage;
        this.pollyLimit = pollyLimit;
    }

    //    @Override
//    public Boolean handleRequest( Object input, Context context) {
    public Boolean handleRequest(Article convertData) throws IOException {

        if (null == convertData.getContent()) {
            logger.info("No records found");
            return false;
        }
        logger.info("original : " + convertData.getContent());

//        String content_modified = convertData.getContent();
//        content_modified = RomanNumeral.convert(content_modified);
//        String summary_modified = convertData.getTitle();
//
//        content_modified = appendTitle(summary_modified, content_modified);
//        content_modified = insertTopicAfterDate(summary_modified, content_modified);
//
//        content_modified = ("en".equals(appProperties.getProperty("language"))) ? EnVerseRegExp.convert(content_modified) : content_modified;
//
//        //"：", "，", "；"  replace 'full width text'
//        content_modified = content_modified.replaceAll("：", ":")
//                .replaceAll("，", ",")
//                .replaceAll("；", ";")
//                .replaceAll("“", "\"")
//                .replaceAll("”", "\"")
//                .replaceAll("（", "(")
//                .replaceAll("）", ")")
//        ;
//        summary_modified = summary_modified.replaceAll("：", ":")
//                .replaceAll("，", ",")
//                .replaceAll("；", ";")
//                .replaceAll("“", "\"")
//                .replaceAll("”", "\"")
//        ;
//
//        if ("zh".equals(appProperties.getProperty("language"))) {
//            //</em>“ <em>
//            content_modified = RegExUtils.replaceFirst(content_modified, "<em>\\s{0,}\"", "<em>\"引用經文[pause]");
//            content_modified = RegExUtils.replaceFirst(content_modified, "</em>\\s{0,}\"\\s{0,}<em>", "<em>\"引用經文[pause]");
//        } else {
////            Scripture in focus
//            content_modified = RegExUtils.replaceFirst(content_modified, "<em>\\s{0,}\"", "<em>\"The scripture passage in focus is[pause]");
//        }
//
//        content_modified = removeUnwantedBetweenQuotes(content_modified);
//
//        //give pause tag, then convert to ssml tag after removing all html tags  。
//        List<String> toBeReplaced_list = Arrays.asList("<td style=\"text-align: right;\">",
//                "<div>&nbsp;</div>",
//                "<p>&nbsp;</p>",
//                "</strong></span></div>",
//                "</em></span></div>",
//                "</em></td>",
//                "。<br />",
//                "。<br/>",
//                "</strong></p>",
//                "<br/>",
//                "<br />",
//                "。"
//        );
//        for (String toBeReplaced : toBeReplaced_list) {
////            content_modified = content_modified.replaceAll(toBeReplaced, "[pause]");
//            content_modified = RegExUtils.replaceAll(content_modified, toBeReplaced, "[pause]");
//        }
//        content_modified = content_modified
//                .replaceAll("THOUGHT", "[pause]THOUGHT")
//                .replaceAll("PRAYER", "[pause]PRAYER")
//                .replaceAll("默想", "[pause]默想")
//                .replaceAll("祷告", "[pause]祷告")
////                                            .replaceAll("\\.", "[pause]")
//        ;
//        //remove all html tags
//        content_modified = content_modified.replaceAll("<p>&nbsp;</p>", ",")
//                .replaceAll("<[^>]*>|&nbsp;|&zwnj;|&raquo;|&laquo;|&gt;", " ")
//                .replaceAll("&nbsp;", " ")
//        ;
//
////        content_modified = ( "en".equals(appProperties.getProperty("language")) )? com.cpbpc.rpgv2.en.Abbreviation.convert(content_modified):content_modified;
////        summary_modified = ( "en".equals(appProperties.getProperty("language")) )? com.cpbpc.rpgv2.en.Abbreviation.convert(summary_modified):summary_modified;
//        content_modified = abbre.convert(content_modified);
//        summary_modified = abbre.convert(summary_modified);
//
//        content_modified = ("en".equals(appProperties.getProperty("language"))) ? content_modified : VerseRegExp.convert(content_modified);
//        content_modified = abbre.convert(content_modified);
//        summary_modified = abbre.convert(summary_modified);
//        content_modified = content_modified.replaceAll("\\[pause\\]", "<break time=\"500ms\"/>");
//        content_modified = ("en".equals(appProperties.getProperty("language"))) ? content_modified : Phonetics.convert(content_modified);
//        ;
//        summary_modified = summary_modified.replaceAll("<[^>]*>|&nbsp;|&zwnj;|&raquo;|&laquo;|&gt;", " ")
//                .replaceAll("&nbsp;", " ")
//        ;
//        //then convert html code to plain text, e.g. &nbsp;
//        content_modified = HtmlEscape.unescapeHtml(content_modified);
//        logger.info("after unescapeHtml");
//        content_modified = content_modified.replaceAll("&", " and ");
////        logger.info( "content have html tags removed : " + content_modified );
//
//        summary_modified = RomanNumeral.convert(summary_modified);
//        summary_modified = summary_modified.replaceAll("&", " and ");
//
//        //give pauses between round bracket
//        content_modified = content_modified
//                .replaceAll("\\(", "(<break time=\"200ms\"/>")
//                .replaceAll("\\)", "<break time=\"200ms\"/>)")
//        ;

        AbstractComposer composer = initComposer(appProperties.getProperty("language"), convertData.getContent(), convertData.getTitle());
        String content_modified = composer.toPolly();

        logger.info("content : " + content_modified);
//        logger.info("content length: " + content_modified.length());

        if (AppProperties.getTotalLength() + content_modified.length() + previousUsage >= pollyLimit) {
            logger.info("reached Polly Limit");
            return false;
        }

        AppProperties.addTotalLength(content_modified.length());
        logger.info(" total length " + AppProperties.getTotalLength());

        logger.info("use.polly is " + Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "true")));
        if (Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "true")) == true) {
            logger.info("send to polly script S3 bucket!");
            AWSUtil.putScriptToS3(content_modified, convertData.getStartDate());
//            StartSpeechSynthesisTaskResult result = startPolly(content_modified);
//            String taskId = result.getSynthesisTask().getTaskId();
//
//            String finalSummary_modified = summary_modified;
//            await().with()
//                    .pollInterval(SYNTHESIS_TASK_POLL_INTERVAL)
//                    .pollDelay(SYNTHESIS_TASK_POLL_DELAY)
//                    .atMost(SYNTHESIS_TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
//                    .until(
//                            () -> {
//
//                                boolean end = getSynthesisTaskStatus(taskId).equals(TaskStatus.Completed.toString());
//                                if (end) {
//                                    changeOutputName(convertData.getStartDate(),
//                                            result.getSynthesisTask().getOutputUri(),
//                                            result.getSynthesisTask().getOutputFormat(),
//                                            finalSummary_modified,
//                                            appProperties.getProperty("voice_id"),
//                                            convertData.getCategory(),
//                                            convertData.getCounter());
//                                }
//
//                                return end;
//                            }
//                    );
        }

        //download pdf/image from s3 bucket then process with textract
        //process the records
//        for(S3EventNotification.S3EventNotificationRecord record: input.getRecords()){
//            String bucketName = record.getS3().getBucket().getName();
//            String objectKey = record.getS3().getObject().getKey();
//
//            logger.log("bucketName: " + bucketName + "\n" + "objectKey: " + objectKey);
//
//            S3Object s3Object = s3Client.getObject(bucketName, objectKey);
//            S3ObjectInputStream inputStream = s3Object.getObjectContent();
//
//            SdkBytes bytes = SdkBytes.fromInputStream(inputStream);
//            Document document = Document.builder().bytes(bytes).build();
//            List<FeatureType> list = new ArrayList();
//            list.add(FeatureType.TABLES);
//            AnalyzeDocumentRequest request = AnalyzeDocumentRequest.builder().featureTypes(list).document(document).build();
//            TextractClient client = TextractClient.builder().region(Region.AP_SOUTHEAST_1).build();
//            AnalyzeDocumentResponse response = client.analyzeDocument(request);
//
//            List<Block> blocks = response.blocks();
//            StringBuilder content = new StringBuilder();
//            for( Block block : blocks ){
//
////                logger.log( "block type " + block.blockType().toString() );
//                if( block.blockType().toString().equals("WORD") ){
//                    continue;
//                }
//
//                String word = block.text();
//                if( word == null ){
//                    continue;
//                }
//
//                String line = block.text();
//                content.append(line);
//                if( line.endsWith(".") || line.endsWith("?") || line.endsWith(":") ){
//                    content.append(System.lineSeparator());
//                }
//            }
//            logger.log(content.toString());
//
        return true;
    }

    private AbstractComposer initComposer(String language, String content, String title) {
        String packageName = "com.cpbpc.rpgv2." + language;
        AbstractArticleParser parser = initParser(language, content, title);
        try {
            Class<?> clazz = Class.forName(packageName + ".Composer");
            Constructor<?> constructor = clazz.getConstructor(AbstractArticleParser.class);
            Object obj = constructor.newInstance(parser);
            if( obj instanceof AbstractComposer ){
                return (AbstractComposer)obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private AbstractArticleParser initParser(String language, String content, String title) {

        String packageName = "com.cpbpc.rpgv2." + language;
        try {
            Class<?> clazz = Class.forName(packageName + ".ArticleParser");
            Constructor<?> constructor = clazz.getConstructor(String.class, String.class);
            Object obj = constructor.newInstance(content, title);
            if( obj instanceof AbstractArticleParser ){
                return (AbstractArticleParser)obj;
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private String appendTitle(String summmary, String content) {
        String content_modified = content;
        if ("zh".equals(appProperties.getProperty("language"))) {
            content_modified = RegExUtils.replaceFirst(content_modified, summmary, "[pause]今日靈修題目[pause]" + summmary);
        } else {
            content_modified = RegExUtils.replaceFirst(content_modified, summmary, "[pause]Today's devotional is entitled[pause]" + summmary);
        }
        return content_modified;
    }

    private String insertTopicAfterDate(String summary, String content) {
        Pattern datePattern = genDatePattern();
        String contentModified = content;

        Matcher m = datePattern.matcher(content);
        if (m.find()) {
            String target = m.group();
            if ("zh".equals(appProperties.getProperty("language"))) {
                contentModified = m.replaceFirst(target + "[pause]今日靈修題目[pause]" + summary);
            } else {
                contentModified = m.replaceFirst(target + "[pause]Today's devotional is entitled[pause]" + summary);
            }

        }

        return contentModified;
    }

    private Pattern genDatePattern() {
        if ("zh".equals(appProperties.getProperty("language"))) {
            return Pattern.compile("[一二三四五六七八九十百千零月日主礼拜，]{8,11}");
        }

        return Pattern.compile("[A-Z,\\s’]{12,22}\\d{1,2}");
    }

    private String removeUnwantedBetweenQuotes(String input) {
        String changed = input;

        Map<String, String> replacements = new HashMap<>();
        for (Pattern p : quote_patterns) {

            Matcher matcher = p.matcher(input);
            while (matcher.find()) {
                String orginal = matcher.group(2);
                String replace = orginal.replaceAll("<[^>]*>|&nbsp;|&zwnj;|&raquo;|&laquo;|&gt;", " ");
                replacements.put(orginal, replace);
            }

        }

        Set<Map.Entry<String, String>> entries = replacements.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            changed = changed.replace(entry.getKey(), entry.getValue());
        }

        return changed;
    }

    //<phoneme alphabet="x-amazon-pinyin" ph="de2">得</phoneme><phoneme alphabet="x-amazon-pinyin" ph="zhao2">着</phoneme>
    private StartSpeechSynthesisTaskResult startPolly(String content) {
        String withMarkup = "<speak><prosody rate='" + appProperties.getProperty("speech_speed") + "' volume='" + appProperties.getProperty("speech_volume") + "'>" + content + "</prosody></speak>";
        logger.info("content : " + withMarkup);
        StartSpeechSynthesisTaskRequest speechRequest = new StartSpeechSynthesisTaskRequest()
                .withOutputFormat(OutputFormat.fromValue(appProperties.getProperty("output_format")))
                .withText(withMarkup)
                .withTextType(TextType.Ssml)
//                .withOutputS3BucketName("cpbpc-rpg-audio")
//                .withOutputS3KeyPrefix("audio/")
                .withOutputS3BucketName(appProperties.getProperty("output_bucket"))
                .withOutputS3KeyPrefix(appProperties.getProperty("output_prefix"))
//                    .withSnsTopicArn(SNS_TOPIC_ARN)
                .withVoiceId(VoiceId.fromValue(appProperties.getProperty("voice_id")))
                .withEngine("neural");

        StartSpeechSynthesisTaskResult result = AMAZON_POLLY_CLIENT.startSpeechSynthesisTask(speechRequest);
        logger.info("file url : " + result.getSynthesisTask().getOutputUri());
        return result;
    }

    private void changeOutputName(String publishDate_str, String outputUri,
                                  String objectType, String nameToBe,
                                  String voiceId, String category, int count) {

        if (null == outputUri || outputUri.trim().length() <= 0) {
            return;
        }

        String publish_month = publishDate_str.split("-")[0] + "_" + publishDate_str.split("-")[1];
//        nameToBe = publishDate_str + "_" + nameToBe.replaceAll(" ", "-")
//                + "_" + voiceId
        nameToBe = appProperties.getProperty("name_prefix", "arpg") + publishDate_str.replaceAll("-", "");
        if (count > 0) {
            nameToBe += "-" + count;
        }
        ;

        String bucketName = appProperties.getProperty("output_bucket");
        String prefix = appProperties.getProperty("output_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        String[] pathInfo = outputUri.split("/");
        String objectName = pathInfo[pathInfo.length - 1];

        String destination_key = prefix + publish_month + "/" + nameToBe + "." + objectType;
        CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName,
                prefix + objectName,
                bucketName,
                destination_key);

        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("publish_date", publishDate_str));
        tags.add(new Tag("voice_id", voiceId));
        tags.add(new Tag("category", category));

        copyObjRequest.setStorageClass(StorageClass.IntelligentTiering);
        copyObjRequest.setNewObjectTagging(new ObjectTagging(tags));
        s3Client.copyObject(copyObjRequest);

        logger.info("delete this object : " + prefix + objectName);
        s3Client.deleteObject(new DeleteObjectRequest(bucketName, prefix + objectName));

    }

    private String convertToday() {

        Date today = new Date();
        return dateformat.format(today);
    }

//    private SynthesisTask getSynthesisTask(String taskId) {
//        GetSpeechSynthesisTaskRequest getSpeechSynthesisTaskRequest = new GetSpeechSynthesisTaskRequest()
//                .withTaskId(taskId);
//        GetSpeechSynthesisTaskResult result =AMAZON_POLLY_CLIENT.getSpeechSynthesisTask(getSpeechSynthesisTaskRequest);
//        return result.getSynthesisTask();
//    }

    private String getSynthesisTaskStatus(String taskId) {
        GetSpeechSynthesisTaskRequest getSpeechSynthesisTaskRequest = new GetSpeechSynthesisTaskRequest()
                .withTaskId(taskId);
        GetSpeechSynthesisTaskResult result = AMAZON_POLLY_CLIENT.getSpeechSynthesisTask(getSpeechSynthesisTaskRequest);
        logger.info("polly status : " + result.getSynthesisTask().getTaskStatus());

        if (TaskStatus.Failed.toString().equals(result.getSynthesisTask().getTaskStatus())) {
            logger.info(result.getSynthesisTask().getTaskStatusReason());
        }
        return result.getSynthesisTask().getTaskStatus();
    }

}