package com.cpbpc.rpgv3.zh;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.TextUtil;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Claude35SonnetChat {

    // Step 1: Create the Bedrock client
    private static BedrockRuntimeClient bedrockClient = null;
    static {
        AppProperties.loadConfig(System.getProperty("app.properties", "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-aiagent-rpg.properties"));
        bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(AppProperties.getConfig().getProperty("region")))
//                .credentialsProvider(DefaultCredentialsProvider.create())
                .credentialsProvider(ProfileCredentialsProvider.create("cpbpc"))
                .build();
    }
    private static Logger logger = Logger.getLogger(Claude35SonnetChat.class.getName());
    private static int starting_page = Integer.parseInt(AppProperties.getConfig().getProperty("starting_page"));
    private static int ending_page = Integer.parseInt(AppProperties.getConfig().getProperty("ending_page"));
    private final static VelocityEngine velocityEngine = new VelocityEngine();
    private static final boolean isTest = false;

    public static void main(String[] args) throws Exception {
        DBUtil.initStorage(AppProperties.getConfig());
        splitPDFtoImg();
        Map<String, MeditationEntry> entries = claude();
//        generateAudio(entries);
    }

    private static void generateAudio(Map<String, MeditationEntry> entries) throws IOException, SQLException {
        List<File> htmlFiles = listHtmlFiles( AppProperties.getConfig().getProperty("rpg_img_folder") );
        for ( File html : htmlFiles ){
            String htmlContent = IOUtils.toString( new FileReader(html));
            String date = html.getParentFile().getName();
            MeditationEntry entry = entries.get(date);
            String alias = URLDecoder.decode(AppProperties.getConfig().getProperty("alias"));
            int count = 0;
            if( StringUtils.equals("evening", TextUtil.getChieseTiming(entry.getDate())) ){
                count ++;
            }
            if( !isTest ){
                updateDB( entry.getTitle(), htmlContent, date );
            }
//            Article article = new Article(date, htmlContent, entry.getTitle(), alias, count);
//            RPGToAudio worker = new RPGToAudio(0, 0);
//            worker.handleRequest(article);
        }
    }

    private static String UPDATE_RPG_CHINESE = "UPDATE cpbpc_jevents_vevdetail cjv\n" +
            "JOIN (\n" +
            "    SELECT a.evdet_id\n" +
            "    FROM cpbpc_jevents_vevdetail a\n" +
            "    LEFT JOIN cpbpc_jevents_vevent cj ON cj.ev_id = a.evdet_id\n" +
            "    LEFT JOIN cpbpc_categories cc ON cc.id = cj.catid  \n" +
            "    WHERE cc.id = ?\n" +
            "    AND LENGTH(a.description) = 0\n" +
            "    AND a.summary = ?\n" +
            ") AS subquery ON cjv.evdet_id = subquery.evdet_id\n" +
            "SET cjv.description = ?, cjv.summary = ?"
            ;
    private static void updateDB(String title, String html, String date) throws SQLException {
        Connection conn = DBUtil.createConnection(AppProperties.getConfig());
        PreparedStatement ps = conn.prepareStatement(UPDATE_RPG_CHINESE);
        ps.setString( 1, AppProperties.getConfig().getProperty("content_category") );
        ps.setString( 2, date );
        ps.setString( 3, html );
        ps.setString( 4, title );

        ps.executeUpdate();

    }

    private static List<File> listHtmlFiles(String folderPath) {
        return listHtmlFiles(new File(folderPath));
    }

    private static List<File> listHtmlFiles(File folder) {
        List<File> files = new ArrayList<>();

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                List list = listHtmlFiles(file);
                files.addAll(list);
            } else if (file.getName().toLowerCase().endsWith(".html")) {
                files.add(file);
            }
        }
        return files;
    }


    private static void splitPDFtoImg() {
        String pdfFilePath = AppProperties.getConfig().getProperty("pdf_path");
        String outputDir = AppProperties.getConfig().getProperty("rpg_img_folder");

        try {
            File dir = new File(outputDir);
            if (dir.exists()) deleteFolder(dir.toPath());
            if (!dir.exists()) dir.mkdirs();

            PDDocument document = PDDocument.load(new File(pdfFilePath));
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            int pageCount = document.getNumberOfPages();
            ending_page = Math.min(ending_page, pageCount - 1);
            PDFTextStripper textStripper = new PDFTextStripper();
            for (int page = starting_page; page <= ending_page; page++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page-1, 300);
                String fileName = outputDir + "/page_" + (page) + ".png";
                ImageIO.write(bim, "png", new File(fileName));
                logger.info("Saved: " + fileName);

                textStripper.setStartPage(page);
                textStripper.setEndPage(page);
                String pageText = textStripper.getText(document);
                String plainTextFileName = outputDir + "/page_" + (page) + ".txt";
                OutputStream outputStream = new FileOutputStream(plainTextFileName);
                IOUtils.write(pageText, outputStream, StandardCharsets.UTF_8);
            }
            document.close();
           logger.info("Pages " + starting_page + " to " + ending_page + " converted to images successfully!");
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
    }

    private static void deleteFolder(Path directory) {

        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder()) // delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.info("Failed to delete: " + path + " (" + e.getMessage() + ")");
                        }
                    });
        } catch (IOException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
    }

    private static Map<String, MeditationEntry> claude() {

        Map<String, MeditationEntry> entries = new HashMap<>();
        try {
            File sampleFile = convertPDFtoImage(AppProperties.getConfig().getProperty("sample_pdf"));
            String sampleBase64 = convertToBase64(sampleFile);

            try {
                Path folderPath = Path.of(AppProperties.getConfig().getProperty("rpg_img_folder"));
                DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderPath);

                for (Path path : directoryStream) {
                    try {
                        if (Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".png")) {
                            String inputBase64 = convertToBase64(path.toFile());
                            String pngPath = path.toString().toLowerCase();
                            String plainTextPath = StringUtils.replace(pngPath, "png", "txt");
                            String pdfContent = IOUtils.toString(new FileReader(new File(plainTextPath)));

                            MeditationEntry entry = claudeAnalyse(sampleBase64, inputBase64, pdfContent);
                            String htmlContent = saveHtml(entry);

                            String year = AppProperties.getConfig().getProperty("year");
                            String formattedDate = TextUtil.convertChineseDate(entry.getDate());
                            entries.put(year + "-" + formattedDate, entry);
                            String date = year + "-" + formattedDate;

                            if (!isTest) {
                                updateDB(entry.getTitle(), htmlContent, date);
                            }
                        }
                    } catch (Exception e) {
                        logger.info(ExceptionUtils.getStackTrace(e));
                    }
                }
            } catch (IOException e) {
                logger.info(ExceptionUtils.getStackTrace(e));
            }

            return entries;
        }catch(Exception e){
            logger.info(ExceptionUtils.getStackTrace(e));
        }
        return entries;
    }

    private static String saveHtml(MeditationEntry entry) {

        velocityEngine.init();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("date", entry.getDate());
        dataMap.put("topicVerses", entry.getTopicVerses());
        dataMap.put("focusScriptures", List.of(StringUtils.split(entry.getFocusedVerse(), System.lineSeparator())));
        dataMap.put("title", entry.getTitle());
        dataMap.put("paragraphs", entry.getParagraphs());

        Map<String, String> endMap = new HashMap<>();
        if( !StringUtils.isEmpty(entry.getMeditation()) ){
            endMap.put("默想:", entry.getMeditation());
        }

        if( !StringUtils.isEmpty(entry.getMemorisation()) ){
            endMap.put("背诵:", entry.getMemorisation());
        }

        endMap.put("祷告:", entry.getPrayer());
        dataMap.put("endMap", endMap);

        VelocityContext context = new VelocityContext(dataMap);
        Template template = velocityEngine.getTemplate("src/main/resources/template/rpg-zh.vm");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        String htmlFileName = AppProperties.getConfig().getProperty("year") + "-" +
                TextUtil.convertChineseDate(entry.getDate());
        String timing = TextUtil.getChieseTiming(entry.getDate());
        if( !StringUtils.isEmpty(timing) ){
            htmlFileName += "-"+TextUtil.chieseTimingToEnglish(timing);
        }
        htmlFileName += ".html";

        String htmlFolder = AppProperties.getConfig().getProperty("year") + "-" +
                                TextUtil.convertChineseDate(entry.getDate());
        File folder = new File( AppProperties.getConfig().getProperty("rpg_img_folder") + "/" + htmlFolder );
        if( !folder.exists() ){
            folder.mkdirs();
        }
        File htmlFile = new File(
                AppProperties.getConfig().getProperty("rpg_img_folder") + "/" + htmlFolder + "/" + htmlFileName
        );
        String html = writer.toString();
        try (FileWriter fileWriter = new FileWriter(htmlFile)) {
            fileWriter.write(html);
        } catch (IOException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }

        return html;
    }

    private static int counter = 0;
    private static MeditationEntry claudeAnalyse(String sampleBase64, String inputBase64, String pdfContent) throws IOException {
        return claudeAnalyse(sampleBase64, inputBase64, pdfContent, "");
    }
    private static MeditationEntry claudeAnalyse(String sampleBase64, String inputBase64, String pdfContent, String exceptionMsg) throws IOException {
        if( counter == 3 ){
            return null;
        }
        
        List<Map<String, Object>> messages = new ArrayList<>();
        List<Map<String, Object>> content = new ArrayList<>();
        if( StringUtils.isEmpty(exceptionMsg) ){
            content.add(Map.of(
                    "type", "image",
                    "source", Map.of(
                            "type", "base64",
                            "media_type", "image/png",
                            "data", sampleBase64
                    )
            ));
            content.add(Map.of(
                    "type", "text",
                    "text", "This is sample.pdf."
            ));
        }

// The content list can include both image and text blocks
        content = createClaudeContent(content, inputBase64, pdfContent);

        // Add the user message
        messages.add(Map.of(
                "role", "user",
                "content", content
        ));
// Step 3: Build the Claude request payload
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("anthropic_version", "bedrock-2023-05-31");
        requestPayload.put("system", "You are a helpful and concise assistant that explains complex topics in Chinese and English.");
        requestPayload.put("max_tokens", 1000);
        requestPayload.put("temperature", 0.7);
        requestPayload.put("messages", messages);

        // Convert to JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonInput = objectMapper.writeValueAsString(requestPayload);

        // Step 4: Invoke the Claude model
        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(AppProperties.getConfig().getProperty("ai_model"))
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromString(jsonInput, StandardCharsets.UTF_8))
                .build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);
        String responseJson = response.body().asUtf8String();

        // Step 5: Parse and print Claude's reply
        String reply = "";
        try{

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode contentArray = root.get("content");
            if (contentArray == null || !contentArray.isArray() || contentArray.size() <= 0) {
                logger.info("No content available in the response.");
                return null;
            }

            // Get the first element in the 'content' array
            JsonNode firstContent = contentArray.get(0);

            // Extract the 'text' field from the first content element
            reply = firstContent.get("text").asText();

            logger.info("Claude's Reply:\n");
            logger.info(reply);

            ObjectMapper mapper = new ObjectMapper();
            MeditationEntry entry = mapper.readValue(reply, MeditationEntry.class);

            // Print to verify
            logger.info("Title: " + entry.getTitle());
            logger.info("Date: " + entry.getDate());
            logger.info("only Date: " + TextUtil.getChieseDate(entry.getDate()));
            logger.info("only timing: " + TextUtil.getChieseTiming(entry.getDate()));
            logger.info("Topic Verses: " + entry.getTopicVerses());
            logger.info("Focused Verse: " + entry.getFocusedVerse());
            logger.info("Meditation: " + entry.getMeditation());
            logger.info("Prayer: " + entry.getPrayer());
            logger.info("Paragraphs: " + entry.getParagraphs());

            counter = 0;
            return entry;

        }catch ( JacksonException e){
            String msg = ExceptionUtils.getStackTrace(e);
            logger.info(msg);
            counter++;
            return claudeFix(reply, msg);
        }
        
    }
    private static MeditationEntry claudeFix(String reply, String exception) throws IOException {
        List<Map<String, Object>> messages = new ArrayList<>();
        List<Map<String, Object>> content = new ArrayList<>();

        content.add(Map.of(
                "type", "text",
                "text", "previous response: " + reply
        ));

        content.add(Map.of(
                "type", "text",
                "text", "no...I cannot parse response in form of json, here is the exception: " + exception + ", please try again and return only result without your feedback"
        ));

        messages.add(Map.of(
                "role", "user",
                "content", content
        ));
// Step 3: Build the Claude request payload
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("anthropic_version", "bedrock-2023-05-31");
        requestPayload.put("system", "You are a helpful and concise assistant that explains complex topics in Chinese and English.");
        requestPayload.put("max_tokens", 1000);
        requestPayload.put("temperature", 0.7);
        requestPayload.put("messages", messages);

        // Convert to JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonInput = objectMapper.writeValueAsString(requestPayload);

        // Step 4: Invoke the Claude model
        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(AppProperties.getConfig().getProperty("ai_model"))
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromString(jsonInput, StandardCharsets.UTF_8))
                .build();

        InvokeModelResponse response = bedrockClient.invokeModel(request);
        String responseJson = response.body().asUtf8String();

        // Step 5: Parse and print Claude's reply
        String result = "";
        try{

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode contentArray = root.get("content");
            if (contentArray == null || !contentArray.isArray() || contentArray.size() <= 0) {
                logger.info("No content available in the response.");
                return null;
            }

            // Get the first element in the 'content' array
            JsonNode firstContent = contentArray.get(0);

            // Extract the 'text' field from the first content element
            reply = firstContent.get("text").asText();

            logger.info("Claude's Reply:\n");
            logger.info(reply);

            ObjectMapper mapper = new ObjectMapper();
            MeditationEntry entry = mapper.readValue(reply, MeditationEntry.class);

            // Print to verify
            logger.info("Title: " + entry.getTitle());
            logger.info("Date: " + entry.getDate());
            logger.info("only Date: " + TextUtil.getChieseDate(entry.getDate()));
            logger.info("only timing: " + TextUtil.getChieseTiming(entry.getDate()));
            logger.info("Topic Verses: " + entry.getTopicVerses());
            logger.info("Focused Verse: " + entry.getFocusedVerse());
            logger.info("Meditation: " + entry.getMeditation());
            logger.info("Prayer: " + entry.getPrayer());
            logger.info("Paragraphs: " + entry.getParagraphs());

            counter = 0;
            return entry;

        }catch ( JacksonException e){
            String msg = ExceptionUtils.getStackTrace(e);
            logger.info(msg);
        }

        return null;
    }

    private static List<Map<String, Object>> createClaudeContent(List<Map<String, Object>> content, String inputBase64, String plainText) {
        
        content.add(Map.of(
                "type", "image",
                "source", Map.of(
                        "type", "base64",
                        "media_type", "image/png",
                        "data", inputBase64
                )
        ));
        content.add(Map.of(
                "type", "text",
                "text", "This is input.pdf."
        ));

        content.add(Map.of(
                "type", "text",
                "text", "this is plaintext for input.pdf:\n" + plainText
        ));

        content.add(Map.of(
                "type", "text",
                "text", """
                            look at this document and my instruction closely, from this sample.pdf, you can see:
                            date: 七月一日，礼拜二
                            topicVerses: 路得记一章1-3节, 箴言四章20-27节
                            focusedVerse:  “你要保守你心，胜过保守一切，
                            因为一生的果效是由心发出。”
                            
                            title : 士师时代 (1)
                            meditation:  默想： 神的护理要求我们在祂的旨意下负责任地过活。
                            prayer: 祷告： 天父，请帮助我相信祢的护理和祢对我人生的计划。
                            paragraphs:\s
                            士师时代向我们展示了神的子民以色列历史上的一段时期，当时这
                            个民族在属灵的黑暗中摸索，没有方向，她的罪使她与神隔离，并
                            屡次导致她跌倒。这个时期凸显了人性的脆弱和堕落。
                            路得记以士师时代为背景，向我们呈现了生活的现实和画面。在
                            这些画面中，它彰显了神在其子民生命中最黑暗的时刻的护理之
                            光。这本书向我们展示了生活的境况和现实、神的护理、在不确定
                            的处境和日常生活的困难之中的信心。与士师记一样，路得记也着
                            眼于紧急的情况。但与士师记从国家角度看待紧急情况不同，路得
                            记是从个人角度关注紧急情况。和士师记一样，路得记彰显了神的
                            恩典、怜悯和忍耐。但与士师记不同的是，士师记显示了神对一个
                            国家的恩典、怜悯和忍耐，而路得记则彰显了神对家庭和个人的恩
                            典、怜悯和忍耐。
                            当我们试图探讨“神在日常生活中的护理”时，我们首先要定义“护
                            理”的含义。 《威斯敏斯特信仰告白》指出：“伟大的神创造万物，
                            真实藉着祂最有智慧、最圣洁的护理，根据祂无谬的预知，按着
                            「祂自己的意思所定，不受拦阻，不会改变」的计划，保持、指
                            导、处理、掌管一切受造物，包括他们的行动与事物，从最大的
                            到最小的，使自己荣耀的智慧、能力、正义、善良、怜悯得著称
                            赞。”
                            “ 当士师秉政的时候……” （得1:1a）。路得的故事以士师时代为背
                            景。尽管百姓中缺乏公义和敬虔的权威和榜样，但是这不能成为一
                            个人不履行他的责任和活出敬虔的借口。当我们明白神的护理时，
                            无论环境如何，我们必需承担起生命的责任。
                            
                            can you follow this logic and analyse input.pdf and extract words I need accordingly? 
                            Also, must fulfill the instructions followed:
                            1. return only result without your feedback 
                            2. in json form, which is valid for java programming
                            3. all parts of content, including paragraphs, title, must escape inner double quotes (") as \\" inside strings and make it as programmable json format 
                            4. key inside json reply must follow camel style
                            5. topicVerses and paragraphs must be array
                            6. value of meditation must exclude '默想： '
                            7. value of prayer must exclude '祷告： '
                            8. sometimes '默想：' is not in the article, instead, '背诵：' is used, in this case, key-value in json should be "memorisation": "XXXXX"
                            9. value of memorisation must exclude '背诵： '
                            10. date and timing, you may read '傍晚' wrongly, get '停晚' instead, double check it, it may be '傍晚'
                            11. keep line break for focused verses
                            12. focused verse always comes with double quotes (“) 
                            13. bible book name, sometimes you may read '利未记' wrongly, get '利末记' instead, double check it, it should be '利未记'
                            14. sometimes you may read '耶和华' wrongly, get '那和华' instead, double check it, it should be '耶和华'
                            15. after you parse text from input.pdf, double check accuracy again plaintext for input.pdf, sometimes you change words, it should never happen.
                        """
        ));
        
        return content;
    }

    private static String convertToBase64(File imgFile) throws IOException {

        byte[] imageBytes = Files.readAllBytes(Path.of(imgFile.toURI()));
        String base64String = Base64.getEncoder().encodeToString(imageBytes);
        return base64String;
    }

    private static File convertPDFtoImage(String filePath) {
        String outputDir = AppProperties.getConfig().getProperty("sample_img");

        try {
            File dir = new File(outputDir);
            if (!dir.exists()) dir.mkdirs();

            File inputFile = new File(filePath);
            PDDocument document = PDDocument.load(inputFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            int pageCount = document.getNumberOfPages();
            String fileName = "";
            for (int page = 0; page < pageCount; ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300); // 300 DPI for good quality
                fileName = outputDir + "/" + inputFile.getName() + ".png";
                ImageIO.write(bim, "png", new File(fileName));
                logger.info("Saved: " + fileName);
            }
            document.close();
            logger.info("PDF converted to PNG images successfully!");

            return new File(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
