package com.cpbpc.reading.plan;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.NumberConverter;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.SecretUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenTelegramExcel {
    private static final Properties appProperties = AppProperties.getConfig();

    private static final String language = "chinese";
//    private static final int start = 0;
//    private static final int end = 1;
    private static final int start = 2;
    private static final int end = 2;
    private static final boolean isTest = true;

    private static final String filePath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/2-year-reading-plan.xlsx";
//    private static List<Integer> columnsToRead = Arrays.asList(1, 4, 7, 10, 13, 16);
    private static List<Integer> columnsToRead = Arrays.asList(1);

    /*
    ✝️ 彼得前书1-3章

    🗣 https://bit.ly/3svxA8d
     */
    private static final String pattern = "(https://bit\\.ly/[0-9A-za-z]+)";
    private static final String[] hyphens_unicode = PunctuationTool.getHyphensUnicode();
    private static final Map<String, String> abbre = new LinkedHashMap<>();
    private static final String query = "select \n" +
            " cjv.summary, cjv.description, cj.catid, cjr.rp_id, \n" +
            " DATE_FORMAT(cjr.startrepeat, \"%Y_%m\") as `month`, \n" +
            " DATE_FORMAT(cjr.startrepeat, \"%Y%m%d\") as `date` \n" +
            " from cpbpc_jevents_vevdetail cjv\n" +
            "     left join cpbpc_jevents_vevent cj on cj.ev_id = cjv.evdet_id\n" +
            "     left join cpbpc_categories cc on cc.id = cj.catid\n" +
            "     left join cpbpc_jevents_repetition cjr on cjr.eventdetail_id  = cjv.evdet_id\n" +
            "     where  cc.id =? \n" +
//            "     and DATE_FORMAT(cjr.startrepeat, \"%Y-%m-%d\")=? \n" +
            "     and DATE_FORMAT(cjr.startrepeat, \"%Y-%m\")=? \n" +
            "     order by cjr.startrepeat asc \n";

    public static void main(String args[]) throws IOException, SQLException {

        AppProperties.loadConfig(System.getProperty("app.properties",
                                                    "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-"+language+".properties"));
        initAbbre();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            CreationHelper creationHelper = workbook.getCreationHelper();
            CellStyle hlinkStyle = workbook.createCellStyle();
            Font hlinkFont = workbook.createFont();
            hlinkFont.setUnderline(Font.U_SINGLE);
            hlinkFont.setColor(IndexedColors.BLUE.getIndex());
            hlinkStyle.setFont(hlinkFont);
            
            // Get the first sheet
            for( int idx=start; idx<=end; idx++ ){
                Sheet sheet = workbook.getSheetAt(idx);

                // Iterate through rows
                for (Row row : sheet) {
                    if( row.getRowNum() < 3 ){
                        continue;
                    }
                    for (Cell cell : row) {
                        if( !columnsToRead.contains(cell.getColumnIndex()) ){
                            continue;
                        }
                        if( StringUtils.equalsIgnoreCase("Bible Texts", cell.getStringCellValue()) ){
                            continue;
                        }
                        switch (cell.getCellType()) {
                            case STRING:
                                String content = StringUtils.remove(cell.getStringCellValue(), " ");
                                content = genEmojiCross() + " " + grepVerses(content) + System.lineSeparator() +
                                        System.lineSeparator() +
                                        "\uD83D\uDDE3" + " " + shortenURL(genAudioLink(content), isTest);
                                System.out.print(content + "\n");
                                RichTextString richText = creationHelper.createRichTextString(content);
                                cell.setCellValue(richText);
                                break;
                            default:
//                                System.out.print("UNKNOWN\t");
                                break;
                        }
                    }
                    System.out.println(); // Newline after each row
                }
            }
            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                workbook.write(fos);
                System.out.println("Workbook updated successfully!");
            }

            audioURLs.forEach(url -> {
                System.out.println(url);
            });
        } catch (IOException e) {
            System.err.println("Error reading the Excel file: " + e.getMessage());
        }
    }

    private static String shortenURL(String link, boolean isTest) throws UnsupportedEncodingException {
        if(isTest){
            return link;
        }
        String accessKey = SecretUtil.getBitlyKey();
        String requestBody = "{\"long_url\":\"" + link + "\", \"domain\":\"bit.ly\", \"group_guid\":\"Bn7fexZnrBp\"}";
        StringEntity entity = new StringEntity(requestBody);
        HttpClient httpClient = HttpClientBuilder.create().build();

        // Create an HttpGet request with the API endpoint URL
        HttpPost request = new HttpPost("https://api-ssl.bitly.com/v4/shorten");
        request.setEntity(entity);

        // Set the Authorization header with the access key
        request.setHeader("Authorization", "Bearer " + accessKey);
        request.setHeader("Content-Type", "application/json");

        try {
            // Execute the request and get the response
            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            return extractLink(responseBody);

            // Process the response as needed
            // Here, you can extract the shortened URL from the response and use it
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String extractLink(String responseBody) {

        Pattern p = Pattern.compile(pattern);
        String result = "";
        Matcher matcher = p.matcher(responseBody);
        if (matcher.find()) {
            result = matcher.group(0);
        }

        return result;
    }

    private static List<String> audioURLs = new ArrayList<>();
    private static String genAudioLink(String filename) {
        String url = "";
        if (AppProperties.isChinese()) {
            url = genChAudioLink(filename);
            audioURLs.add(url);
            return url;
        }

        url = genEngAudioLink(filename);
        audioURLs.add(url);
        return url;
    }

    private static String genChAudioLink(String name) {
        return "https://cpbpc-bible-reading-plan.s3.ap-southeast-1.amazonaws.com/cuvs/" + URLEncoder.encode(name) + ".mp3";
    }

    private static String genEngAudioLink(String name) {
        return "https://cpbpc-bible-reading-plan.s3.ap-southeast-1.amazonaws.com/kjv/" + name + ".mp3";
    }

    private static List<String> textUrls = new ArrayList<>();
    private static String genArticleLink(Map<String, String> dataRow) {
        String url = "";
        if (AppProperties.isChinese()) {
            url = genChArticleLink(dataRow);
            textUrls.add(url);
            return url;
        }

        url = genEngArticleLink(dataRow);
        textUrls.add(url);
        return url;
    }

    //https://calvarypandan.sg/resources/rpg/calendar/eventdetail/69825/86/israel-worshipped-in-thanksgiving
    private static String genEngArticleLink(Map<String, String> dataRow) {
//        return "https://calvarypandan.sg/resources/rpg/calendar/eventdetail/" + dataRow.get("rp_id") + "/" + dataRow.get("catid") + "/" + RegExUtils.replaceAll(StringUtils.lowerCase(dataRow.get("summary")), " ", "-");
        return "https://calvarypandan.sg/resources/rpg/calendar/eventdetail/" + dataRow.get("rp_id");
    }

    //https://mandarin.calvarypandan.sg/%E8%B5%84%E6%BA%90/%E8%AF%BB,%E7%A5%B7,%E9%95%BF-%E6%97%A5%E5%8E%86/eventdetail/67101
    private static String genChArticleLink(Map<String, String> dataRow) {
//        return "https://mandarin.calvarypandan.sg/"+URLDecoder.decode("%E8%B5%84%E6%BA%90")+"/"+ URLDecoder.decode("%E8%AF%BB,%E7%A5%B7,%E9%95%BF-%E6%97%A5%E5%8E%86") +"/eventdetail/"+dataRow.get("rp_id");
        return "https://mandarin.calvarypandan.sg/%E8%B5%84%E6%BA%90/%E8%AF%BB,%E7%A5%B7,%E9%95%BF-%E6%97%A5%E5%8E%86/eventdetail/" + dataRow.get("rp_id");
    }

    private static String genEmojiCross() {

        char char1 = '\u271D';
        char char2 = '\uFE0F';

        char[] emojiModifier = {char1, char2};

        return new String(emojiModifier);
    }


    private static String grepVerses(String content) {
        String p = generateVersePattern();
        if (AppProperties.isChinese()) {
            p = generateTopicVersePattern();
        }
        Pattern versePattern = Pattern.compile(p);
        Matcher matcher = versePattern.matcher(content);

        int position = 0;
        while (matcher.find(position)) {
            String book = matcher.group(0).trim();
            return StringUtils.replace(content, book,  genFullBookName(abbre.get(book)));
        }

        return "";
    }

    private static String genFullBookName(String input) {
        String replaced = input;
        if( StringUtils.startsWithIgnoreCase(input, "first") ){
            replaced = "1" + StringUtils.removeIgnoreCase(input, "first");
        }
        if( StringUtils.startsWithIgnoreCase(input, "second") ){
            replaced = "2" + StringUtils.removeIgnoreCase(input, "second");
        }
        if( StringUtils.startsWithIgnoreCase(input, "third") ){
            replaced = "3" + StringUtils.removeIgnoreCase(input, "third");
        }

        return replaced + " ";
    }

    private static Pattern buildTitlePattern(String title) {

        StringBuilder builder = new StringBuilder("[<strong>]\\s{0,}");
        for( char c : title.toCharArray() ){

            if( c == '(' || c == ')' || c == '?' ){
                builder.append('\\');
            }

            builder.append(c);
            if( StringUtils.indexOf(title, c) == title.length()-1 ){
                break;
            }

            if( c == ' ' ){
                builder.append("[</strong>|<br\\s{0,}/>|<strong>|</p>|<p[^>]*>]{0,}");
            }
        }

        builder.append("\\s{0,}</strong>");

        return Pattern.compile(builder.toString());
    }

    private static String generateTopicVersePattern() {
        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = abbre.keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(key.toString()).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append("))");

        System.out.println(builder.toString());
        return builder.toString();
    }

    private static String appendNextCharTillCompleteVerse(String content, String ref, int startFrom) {
        if (content == null || content.trim().length() <= 0 || ref == null || ref.trim().length() <= 0) {
            return "";
        }

        content = PunctuationTool.changeFullCharacter(content);
        int position = content.indexOf(ref, startFrom) + ref.length();
        StringBuilder builder = new StringBuilder(ref);
        List<String> verseParts = new ArrayList<>();
        List<String> punctuations = new ArrayList<>();
        punctuations.addAll(List.of(":", ",", " ", ";", "节", "節", "章", "篇", "十"));
        for (String hyphen_unicode : hyphens_unicode) {
            punctuations.add(StringEscapeUtils.unescapeJava(hyphen_unicode));
        }

        verseParts.addAll(punctuations);
        for (int i = 0; i < 10; i++) {
            verseParts.add(String.valueOf(i));
            verseParts.add(NumberConverter.toChineseNumber(i));
        }
        System.out.println("verseParts : " + verseParts.toString());

        for (int i = position; i < content.length(); i++) {

            String nextChar = content.substring(i, i + 1);
            if (!verseParts.contains(nextChar)) {
                break;
            }
            builder.append(nextChar);
        }

        String result = builder.toString();
//        if( punctuations.contains(result.substring(result.length()-1, result.length())) ){
//            result = result.substring(0, result.length()-1);
//        }
        return result;
    }

    private static String generateVersePattern() {
        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = abbre.keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(key.toString()).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append("))");

        return builder.toString();
    }

    private static void initAbbre() throws SQLException {
        Connection conn = DBUtil.createConnection(appProperties);
        PreparedStatement state = conn.prepareStatement("select * from cpbpc_abbreviation order by seq_no asc, length(short_form) desc");
        ResultSet rs = state.executeQuery();

        while (rs.next()) {
            String group = rs.getString("group");
            String shortForm = rs.getString("short_form");
            String completeForm = rs.getString("complete_form");

            if ("bible".toLowerCase().equals(group.toLowerCase())) {
                abbre.put(shortForm, completeForm);
            }

        }

        conn.close();

    }
}
