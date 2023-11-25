package com.cpbpc.telegram;

import com.cpbpc.comms.DBUtil;
import com.cpbpc.rpgv2.AppProperties;
import org.apache.commons.lang3.RegExUtils;
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
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenTelegramExcel {
    private static final Properties appProperties = AppProperties.getProperties();
    private static final String propPath = "./src/main/resources/app-english.properties";
    private static final String theme = "The Book of Joshua";
    private static final String writer = "Rev Dr Michael Koech";

    /*
    ✝️ 你们在基督里是完整的
    💭 不要害怕
    📖 彼得前书三章13-16节,诗篇25篇1-9节
    ✍🏻 阮贤牧师
    🗣 https://bit.ly/3svxA8d
    📝 https://bit.ly/47R6ad6
     */
    private static final String pattern = "(https://bit\\.ly/[0-9A-za-z]+)";
    private static final String[] hyphens_unicode = new String[]{"\\u002d", "\\u2010", "\\u2011", "\\u2012", "\\u2013", "\\u2015", "\\u2212"};
    private static final Map<String, String> abbre = new HashMap();
    private static final String query = "select \n" +
            " cjv.summary, cjv.description, cj.catid, cjr.rp_id, \n" +
            " DATE_FORMAT(cjr.startrepeat, \"%Y_%m\") as `month`, \n" +
            " DATE_FORMAT(cjr.startrepeat, \"%Y%m%d\") as `date` \n" +
            " from cpbpc_jevents_vevdetail cjv\n" +
            "     left join cpbpc_jevents_vevent cj on cj.ev_id = cjv.evdet_id\n" +
            "     left join cpbpc_categories cc on cc.id = cj.catid\n" +
            "     left join cpbpc_jevents_repetition cjr on cjr.eventdetail_id  = cjv.evdet_id\n" +
            "     where  cc.title =? \n" +
            "     and DATE_FORMAT(cjr.startrepeat, \"%Y-%m\")=? \n" +
            "     order by cjr.startrepeat asc \n";

    public static void main(String args[]) throws IOException, SQLException {

        loadProperties();
        initAbbre();

        LocalDate currentDate = LocalDate.of(2023, 12, 1);
        LocalDate lastDayOfMonth = YearMonth.from(currentDate).atEndOfMonth();

        List<Map<String, String>> rows = fetchData(lastDayOfMonth.getYear(), lastDayOfMonth.getMonthValue());


        // Create a new Excel workbook
        Workbook workbook = new XSSFWorkbook();
        CreationHelper creationHelper = workbook.getCreationHelper();
        // Create a new Excel sheet within the workbook
        Sheet sheet = workbook.createSheet("Sheet 1");
        // Create rows and cells and set values
        for (Map<String, String> dataRow : rows) {
            int rowNumber = rows.indexOf(dataRow);
            Row row = sheet.createRow(rowNumber);
            Cell cell = row.createCell(0);

            RichTextString richText = creationHelper.createRichTextString(
                    genEmojiCross() + " " + theme + "\n" +
                            "\uD83D\uDCAD" + " " + capitalize(StringUtils.lowerCase(dataRow.get("summary"))) + "\n" +
                            "\uD83D\uDCD6" + " " + grepThemeVerses(dataRow) + "\n" +
                            genEmojiWritingHand() + " " + writer + "\n" +
                            "\uD83D\uDDE3" + " " + shortenURL(genAudioLink(dataRow)) + "\n" +
                            "\n" +
                            "\uD83D\uDCDD" + " " + shortenURL(genArticleLink(dataRow))
            );
            cell.setCellValue(richText);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cell.setCellStyle(cellStyle);
        }

//        // Save the workbook to a file or stream
        try (FileOutputStream fileOut = new FileOutputStream("example.xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String capitalize(String summary) {

        String result = "";
        String[] strs = StringUtils.split(summary, " ");

        for (String str : strs) {
            result += " " + StringUtils.capitalize(str);
        }

        return result;
    }

    private static String shortenURL(String link) throws UnsupportedEncodingException {
//        return link;
        String accessKey = "";
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

    private static String genAudioLink(Map<String, String> dataRow) {
        if (appProperties.getProperty("language").equals("zh")) {
            return genChAudioLink(dataRow);
        }

        return genEngAudioLink(dataRow);
    }

    private static String genChAudioLink(Map<String, String> dataRow) {
        return "https://cpbpc-rpg-audio.s3.ap-southeast-1.amazonaws.com/rpg-chinese/" + dataRow.get("month") + "/crpg" + dataRow.get("date") + ".mp3";
    }

    private static String genEngAudioLink(Map<String, String> dataRow) {
        return "https://cpbpc-rpg-audio.s3.ap-southeast-1.amazonaws.com/rpg/" + dataRow.get("month") + "/arpg" + dataRow.get("date") + ".mp3";
    }

    private static String genArticleLink(Map<String, String> dataRow) {
        if (appProperties.getProperty("language").equals("zh")) {
            return genChArticleLink(dataRow);
        }

        return genEngArticleLink(dataRow);
    }

    //https://calvarypandan.sg/resources/rpg/calendar/eventdetail/69825/86/israel-worshipped-in-thanksgiving
    private static String genEngArticleLink(Map<String, String> dataRow) {
        return "https://calvarypandan.sg/resources/rpg/calendar/eventdetail/" + dataRow.get("rp_id") + "/" + dataRow.get("catid") + "/" + RegExUtils.replaceAll(StringUtils.lowerCase(dataRow.get("summary")), " ", "-");
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

    private static String genEmojiWritingHand() {

        char writingHand = '\u270D';
        char highSurrogate = '\uD83C'; // High surrogate pair
        char lowSurrogate = '\uDFFB';  // Low surrogate pair

        char[] emojiModifier = {writingHand, highSurrogate, lowSurrogate};

        return new String(emojiModifier);
    }

    private static String grepThemeVerses(Map<String, String> dataRow) {
        List<String> result = new ArrayList<>();
        String content = dataRow.get("description");
        int anchorPoint = content.indexOf("<p>&nbsp;</p>");

        String p = generateVersePattern();
        if (appProperties.getProperty("language").equals("zh")) {
            p = generateTopicVersePattern();
        }
        Pattern versePattern = Pattern.compile(p);
        Matcher matcher = versePattern.matcher(content);

        int position = 0;
        while (matcher.find(position)) {
            String bookNChapter = matcher.group(0).trim();
            position = matcher.start() + bookNChapter.length();
            if (position >= anchorPoint) {
                break;
            }

            String completeVerse = appendNextCharTillCompleteVerse(content, bookNChapter, 0);
            result.add(completeVerse);
        }

        return StringUtils.join(result, "; ");
    }

    private static String generateTopicVersePattern() {
        //雅各书一章1节    使徒行传十二章1-2节  哥林多后书6章14-7章1节
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
        builder.append(")\\s{0,}[0-9一二三四五六七八九十百千零]{1,}\\s{0,}[章|篇])");

        System.out.println(builder.toString());
        return builder.toString();
    }

    private static String appendNextCharTillCompleteVerse(String content, String ref, int startFrom) {
        if (content == null || content.trim().length() <= 0 || ref == null || ref.trim().length() <= 0) {
            return "";
        }

        int position = content.indexOf(ref, startFrom) + ref.length();
        StringBuilder builder = new StringBuilder(ref);
        List<String> verseParts = new ArrayList<>();
        List<String> punctuations = new ArrayList<>();
        punctuations.addAll(List.of(":", ",", " ", ";", "节", "節", "章"));
        for (String hyphen_unicode : hyphens_unicode) {
            punctuations.add(StringEscapeUtils.unescapeJava(hyphen_unicode));
        }

        verseParts.addAll(punctuations);
        for (int i = 0; i < 10; i++) {
            verseParts.add(String.valueOf(i));
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
        builder.append(")[.]{0,1}\\s{0,}[0-9]{1,3})");

        return builder.toString();
    }

    private static void loadProperties() throws IOException {
        FileInputStream in = null;
        in = new FileInputStream(propPath);
        appProperties.load(in);
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

    private static List<Map<String, String>> fetchData(int year, int month) throws SQLException {
        List<Map<String, String>> list = new ArrayList<>();

        String category = URLDecoder.decode(appProperties.getProperty("content_category"));
        Connection conn = DBUtil.createConnection(appProperties);
        PreparedStatement stat = conn.prepareStatement(query);
        stat.setString(1, category);
        stat.setString(2, year + "-" + month);

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

    /*
    select DATE_FORMAT(cjr.startrepeat , "%Y-%m-%d") as startrepeat, cc.alias, cjv.*
    from cpbpc_jevents_vevdetail cjv
    left join cpbpc_jevents_vevent cj on cj.ev_id = cjv.evdet_id
    left join cpbpc_categories cc on cc.id = cj.catid
    left join cpbpc_jevents_repetition cjr on cjr.eventdetail_id  = cjv.evdet_id
    where  cc.title in ( '读祷长' )
    and DATE_FORMAT(cjr.startrepeat, "%Y-%m-%d")=?
     */

    /*
     select DATE_FORMAT(cjr.startrepeat , "%Y-%m-%d") as startrepeat, cc.alias, cjv.*
     from cpbpc_jevents_vevdetail cjv
     left join cpbpc_jevents_vevent cj on cj.ev_id = cjv.evdet_id
     left join cpbpc_categories cc on cc.id = cj.catid
     left join cpbpc_jevents_repetition cjr on cjr.eventdetail_id  = cjv.evdet_id
     where  cc.title in ( 'RPG Adult' )
     and DATE_FORMAT(cjr.startrepeat, "%Y-%m-%d")=?
     */

}
