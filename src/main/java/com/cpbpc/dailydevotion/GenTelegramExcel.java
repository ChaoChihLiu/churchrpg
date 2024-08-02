package com.cpbpc.dailydevotion;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.SecretUtil;
import com.cpbpc.comms.TextUtil;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.DBUtil.initStorage;
import static com.cpbpc.comms.TextUtil.capitalizeEveryWord;

public class GenTelegramExcel {
    private static final Properties appProperties = AppProperties.getConfig();

    private static final String year = "2024";
    private static final String month = "10";

    private static final boolean isTest = true;

    /*

        🌅 Morning     <—- Sent at 6 am
        💭 The Lord thy God is a merciful God
        📖 Deuteronomy 4:31

        🗣 https://bit.ly/zz38ewd     <—- MP3

        📝 https://bit.ly/jhr69as     <—- Text on website


        🌃 Evening     <—- Sent at 5 pm
        💭 Who forgiveth all thine iniquities
        📖 Psalm 103:3

        🗣 https://bit.ly/8jgdaj     <—- MP3

        📝 https://bit.ly/ja7trwh     <—- Text on website
     */
    private static final String pattern = "(https://bit\\.ly/[0-9A-za-z]+)";
    private static final String[] hyphens_unicode = PunctuationTool.getHyphensUnicode();
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
                                                    "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-devotion.properties"));
        initStorage(AppProperties.getConfig());
        
        List<Map<String, String>> rows = fetchData();


        // Create a new Excel workbook
        Workbook workbook = new XSSFWorkbook();
        CreationHelper creationHelper = workbook.getCreationHelper();
        // Create a new Excel sheet within the workbook
        Sheet sheet = workbook.createSheet("Sheet 1");
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        // Create rows and cells and set values
        for (Map<String, String> dataRow : rows) {
            int rowNumber = rows.indexOf(dataRow);
            Row row = sheet.createRow(rowNumber);

            String audioLink = shortenURL(genAudioLink(dataRow), isTest);
            String articleLink = shortenURL(genArticleLink(dataRow), isTest);

            Cell cell = row.createCell(0);
            RichTextString richText = creationHelper.createRichTextString(dataRow.get("date"));
            cell.setCellValue(richText);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cell.setCellStyle(cellStyle);

            cell = row.createCell(1);
            richText = creationHelper.createRichTextString(
                            getTimingEmoji(getTiming(dataRow)) + " " + getTiming(dataRow) + "\n" +
                            "\uD83D\uDCAD" + " " + getTopic(dataRow) + "\n" +
                            "\uD83D\uDCD6" + "“" + getTheme(dataRow) + "” (" + grepThemeVerses(dataRow) + ")\n" +
                            "\n" +
                            "\uD83D\uDDE3" + " " + audioLink + "\n" +
                            "\n" +
                            "\uD83D\uDCDD" + " " + articleLink
            );

            CellStyle hlinkStyle = workbook.createCellStyle();
            Font hlinkFont = workbook.createFont();
            hlinkFont.setUnderline(Font.U_SINGLE);
            hlinkFont.setColor(IndexedColors.BLUE.getIndex());
            hlinkStyle.setFont(hlinkFont);

            cell.setCellValue(richText);
            cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cell.setCellStyle(cellStyle);

            Hyperlink articleHyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
            articleHyperlink.setAddress(genArticleLink(dataRow));
            cell = row.createCell(2);
            cell.setCellValue(genArticleLink(dataRow));
            cell.setHyperlink(articleHyperlink);
            cell.setCellStyle(hlinkStyle);

            Hyperlink audioHyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
            audioHyperlink.setAddress(genAudioLink(dataRow));
            cell = row.createCell(3);
            cell.setCellValue(genAudioLink(dataRow));
            cell.setHyperlink(audioHyperlink);
            cell.setCellStyle(hlinkStyle);
        }

        System.out.println( "text url:" );
        System.out.println(StringUtils.join(textUrls, System.lineSeparator()));
        System.out.println( "audio url:" );
        System.out.println(StringUtils.join(audioURLs, System.lineSeparator()));

//        // Save the workbook to a file or stream
        try (FileOutputStream fileOut = new FileOutputStream("daily_remembrance.xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTimingEmoji(String timing) {

        if( StringUtils.equalsIgnoreCase(timing, "morning") ){
            return "\uD83C\uDF05";
        }
        if( StringUtils.equalsIgnoreCase(timing, "evening") ){
            return "\uD83C\uDF03";
        }

        return "";
    }

    private static String grepThemeVerses(Map<String, String> dataRow) {
        if( dataRow.isEmpty() ){
            return "";
        }

        if( !dataRow.containsKey("summary") ){
            return "";
        }

        String summary = dataRow.get("summary");
        VerseIntf verseIntf = ThreadStorage.getVerse();
        List<String> list = verseIntf.analyseVerse(summary);
        if( list.isEmpty() ){
            return "";
        }

        return convertBookName(list.get(0)) + " " + list.get(1);
    }

    private static String convertBookName(String book) {
        String result = book;

        if( StringUtils.startsWithIgnoreCase(book, "first") ){
            return StringUtils.replaceIgnoreCase(book, "first", "1");
        }
        if( StringUtils.startsWithIgnoreCase(book, "second") ){
            return StringUtils.replaceIgnoreCase(book, "second", "2");
        }
        if( StringUtils.startsWithIgnoreCase(book, "third") ){
            return StringUtils.replaceIgnoreCase(book, "third", "3");
        }


        return result;
    }

    private static final Pattern topicPattern = Pattern.compile("[A-Z\\s\\?,;:']+");
    private static String getTopic(Map<String, String> dataRow) {
        if( dataRow.isEmpty() ){
            return "";
        }

        if( !dataRow.containsKey("description") ){
            return "";
        }

        String content = dataRow.get("description");
        Matcher matcher = topicPattern.matcher(content);
        if( matcher.find() ){
            return capitalizeEveryWord(StringUtils.lowerCase(matcher.group()));
        }

        return "";
    }

    private static final Pattern themePattern = Pattern.compile("\\\"([^\\\"]*)\\\"");
    private static String getTheme(Map<String, String> dataRow) {
        if( dataRow.isEmpty() ){
            return "";
        }

        if( !dataRow.containsKey("summary") ){
            return "";
        }

        String summary = dataRow.get("summary");
        Matcher matcher = themePattern.matcher(summary);
        if( matcher.find() ){
            return matcher.group(1);
        }

        return "";
    }

    private static String getTiming(Map<String, String> dataRow) {

        if( dataRow.isEmpty() ){
            return "";
        }

        if( !dataRow.containsKey("summary") ){
            return "";
        }

        String summary = dataRow.get("summary");
        if(StringUtils.containsAnyIgnoreCase(summary, "Morning")){
            return "Morning";
        }
        if(StringUtils.containsAnyIgnoreCase(summary, "Evening")){
            return "Evening";
        }

        return "";
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
    private static String genAudioLink(Map<String, String> dataRow) {
        String url = "";
        url = genEngAudioLink(dataRow);
        audioURLs.add(url);
        return url;
    }

    //https://cpbpc-tts.s3.ap-southeast-1.amazonaws.com/remembrance/October/dr_October_28_Morning.mp3
    private static String genEngAudioLink(Map<String, String> dataRow) {
        return "https://cpbpc-tts.s3.ap-southeast-1.amazonaws.com/remembrance/"+getMonth(dataRow)+"/dr_"+getMonth(dataRow)+"_"+getDate(dataRow)+"_"+getTiming(dataRow)+".mp3";
    }

    private static String getMonth(Map<String, String> dataRow) {
        if( dataRow.isEmpty() ){
            return "";
        }

        if( !dataRow.containsKey("summary") ){
            return "";
        }

        String summary = dataRow.get("summary");
        Pattern pattern = TextUtil.getDatePattern();
        Matcher matcher = pattern.matcher(summary);
        if( matcher.find() ){
            String monthDate = matcher.group(0);
            String date = matcher.group(1);
            return StringUtils.trim(StringUtils.remove(monthDate, date));
        }

        return "";
    }
    private static String getDate(Map<String, String> dataRow) {
        if( dataRow.isEmpty() ){
            return "";
        }

        if( !dataRow.containsKey("summary") ){
            return "";
        }

        String summary = dataRow.get("summary");
        Pattern pattern = TextUtil.getDatePattern();
        Matcher matcher = pattern.matcher(summary);
        if( matcher.find() ){
            String date = matcher.group(1);
            return StringUtils.trim(date);
        }

        return "";
    }

    private static List<String> textUrls = new ArrayList<>();
    private static String genArticleLink(Map<String, String> dataRow) {
        String url = "";

        url = genEngArticleLink(dataRow);
        textUrls.add(url);
        return url;
    }

    //https://calvarypandan.sg/resources/remembrancer/remembrancer-calendar/eventdetail/28288
    private static String genEngArticleLink(Map<String, String> dataRow) {
        return "https://calvarypandan.sg/resources/remembrancer/remembrancer-calendar/eventdetail/" + dataRow.get("rp_id");
    }
    
    private static List<Map<String, String>> fetchData() throws SQLException {
        List<Map<String, String>> list = new ArrayList<>();

        String category = URLDecoder.decode(appProperties.getProperty("content_category"));
        Connection conn = DBUtil.createConnection(appProperties);
        PreparedStatement stat = conn.prepareStatement(query);
        stat.setString(1, category);
        stat.setString(2, year + "-" + month);
//        stat.setString(2, "2024-02-10");

        ResultSet rs = stat.executeQuery();
        while (rs.next()) {
            Map row = new HashMap();
            list.add(row);

            row.put("summary", rs.getString("summary"));
            row.put("description", rs.getString("description"));
            row.put("catid", rs.getString("catid"));
            row.put("rp_id", rs.getString("rp_id"));
            row.put("month", rs.getString("month"));
            row.put("date", rs.getString("date"));
        }

        conn.close();

        return list;
    }


}
