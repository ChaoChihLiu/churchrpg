package com.cpbpc.pdf.rpg.zh;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import com.cpbpc.comms.NumberConverter;
import com.cpbpc.comms.RomanNumeral;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class PDFReader {

    private static Logger logger = Logger.getLogger(PDFReader.class.getName());

    private static final boolean isTest = false;
    private static final int starting_page = 2;

    private String pdfPath = "";
    public PDFReader(String path){
        this.pdfPath = pdfPath;
    }
    
    public static void main(String[] args) throws IOException {
 
        AppProperties.loadConfig(System.getProperty("app.properties", "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-pdf-rpg.properties"));
        String pdfFilePath = AppProperties.getConfig().getProperty("pdf_path");
        try {
            DBUtil.initStorage(AppProperties.getConfig());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        

        List<String> chineseDateRange = createChineseDateRange(AppProperties.getConfig().getProperty("date_from"),
                                                    AppProperties.getConfig().getProperty("date_to"));
        List<String> dateRange = createDateRange(AppProperties.getConfig().getProperty("date_from"),
                                                        AppProperties.getConfig().getProperty("date_to"));
//        List<String>
        if( chineseDateRange.isEmpty() ){
            logger.info("wrong data range, change date_from and date_to");
            return;
        }


        final StringBuffer buffer = new StringBuffer();
        final boolean[] withinBracket = {false};
        PDFTextStripper textStripper = new PDFTextStripper() {

            private static boolean isBoldFont(TextPosition textPosition) {
                return textPosition.getFont().getFontDescriptor().isForceBold() ||
                        (textPosition.getFont().getFontDescriptor().getFontWeight() >= 700);
            }

            private static boolean isItalicFont(TextPosition textPosition) {
                return textPosition.getFont().getFontDescriptor().isItalic();
            }

            @Override
            protected void processTextPosition(org.apache.pdfbox.text.TextPosition text) {

                float fontSize = text.getWidth();

                if(withinBracket[0] && RomanNumeral.isRomanNumeral(text.getUnicode())){
                    buffer.append(text.getUnicode());
                }

                if( Math.round(fontSize) == 14
//                                && (!StringUtils.equals("(", text.getUnicode()) && !StringUtils.equals(")", text.getUnicode()))
//                                && (!StringUtils.equals("（", text.getUnicode()) && !StringUtils.equals("）", text.getUnicode()))
                ){
                    buffer.append(text.getUnicode());
                    if( StringUtils.equals("(", text.getUnicode()) || StringUtils.equals("（", text.getUnicode()) ){
                        withinBracket[0] = true;
                    }
                    if( StringUtils.equals(")", text.getUnicode()) || StringUtils.equals("）", text.getUnicode()) ){
                        withinBracket[0] = false;
                    }
                }

                if(     (
                            Math.round(fontSize) == 5
                            &&
                            (StringUtils.equals("(", text.getUnicode()) || StringUtils.equals(")", text.getUnicode())
                                    || StringUtils.equals("（", text.getUnicode()) || StringUtils.equals("）", text.getUnicode())
                                    || StringUtils.equals("?", text.getUnicode()) || StringUtils.equals("？", text.getUnicode())
                            )
                        )
                        ||
                        (
                                Math.round(fontSize) == 3
                                        &&
                                        ( StringUtils.equals("!", text.getUnicode()) || StringUtils.equals("！", text.getUnicode())
                                        )
                        )
                        || StringUtils.equals(" ", text.getUnicode())
                ){
                    buffer.append(text.getUnicode());
                }

//                        List<List<TextPosition>> textPositionList = getCharactersByArticle();
//
//                        // Iterate through text positions
//                        for (List<TextPosition> textPositions: textPositionList) {
//                            for( TextPosition textPosition: textPositions){
//                                // Check if the font is bold
//                                if (isBoldFont(textPosition)) {
//                                    System.out.println("Bold text: " + textPosition.getUnicode());
//                                }
//                                if (textPosition.getFont().getFontDescriptor().isItalic()) {
//                                    System.out.println("Text in italics: " + textPosition.getUnicode());
//                                }
//                            }
//                        }

                super.processTextPosition(text);
            }

//                    @Override
//                    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
//                        // Iterate through each text position
//                        for (TextPosition textPosition : textPositions) {
//                            if (isBoldFont(textPosition)) {
//                                System.out.println("Bold text: " + textPosition.getUnicode());
//                            }
//                            if (isItalicFont(textPosition)) {
//                                System.out.println("Italic text: " + textPosition.getUnicode());
//                            }
//                        }
//                        super.writeString(text, textPositions);
//                    }
        };
        
        Composer composer = new Composer();
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {

            for (int page = starting_page; page <= document.getNumberOfPages(); ++page) {
                buffer.delete(0, buffer.toString().length());
                withinBracket[0] = false;

                textStripper.setStartPage(page);
                textStripper.setEndPage(page);

                String pageText = textStripper.getText(document);
                if(StringUtils.equals(StringUtils.trim(pageText), "Notes") ){
                    break;
                }

                String title = getCompleteTitle(pageText, StringUtils.trim(buffer.toString()));

                ArticleParser parser = new ArticleParser(pageText, title);
                String chineseDate = getDateRange(chineseDateRange, parser.readDate());
                if( StringUtils.isEmpty(chineseDate) ){
                    continue;
                }

                String html = composer.toHtml(parser);
//                pageText = textStripper.getText(document);
                logger.info("original: \n" + pageText);
                logger.info("-------------------");
                logger.info("output: \n" + html);

                String date = dateRange.get(chineseDateRange.indexOf(chineseDate));
                if( !isTest ){
                    updateDB( title, html, date );
                }
                
            }
        } catch (IOException | SQLException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
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

    private static String getDateRange(List<String> dateRange, String readDate) {
        if( dateRange.isEmpty() || StringUtils.isEmpty(readDate) ){
            return "";
        }

        for( String date : dateRange ){
            if( StringUtils.startsWith(readDate, date) ){
                return date;
            }
        }

        return "";
    }

    private static List<String> createChineseDateRange(String dateFrom, String dateTo) {
        List<String> list = new ArrayList<>();
        if( StringUtils.isEmpty(dateFrom)
                || StringUtils.isEmpty(dateTo) ){
            return list;
        }

        String[] dateFrom_array = StringUtils.split(dateFrom, "-");
        String from_year_str = dateFrom_array[0];
        String from_month_str = removeZeroPrefix(dateFrom_array[1]);
        String from_date_str = removeZeroPrefix(dateFrom_array[2]);

        String[] dateTo_array = StringUtils.split(dateTo, "-");
        String to_year_str = dateTo_array[0];
        String to_month_str = removeZeroPrefix(dateTo_array[1]);
        String to_date_str = removeZeroPrefix(dateTo_array[2]);
        if( !StringUtils.equals(from_year_str, to_year_str)
                || !StringUtils.equals(from_month_str, to_month_str) ){
            return list;
        }

        int from = Integer.valueOf(from_date_str);
        int to = Integer.valueOf(to_date_str);
        for( int i = from; i<=to; i++ ){
//            list.add( NumberConverter.toChineseNumber(Integer.valueOf(from_month_str)) + "月" + NumberConverter.toChineseNumber(i) + "日" );
            list.add( NumberConverter.toChineseNumber(Integer.valueOf(from_month_str)) + "月" + NumberConverter.toChineseNumber(i) );
        }

        Collections.reverse(list);
        return list;
    }

    private static List<String> createDateRange(String dateFrom, String dateTo) {
        List<String> list = new ArrayList<>();
        if( StringUtils.isEmpty(dateFrom)
                || StringUtils.isEmpty(dateTo) ){
            return list;
        }

        String[] dateFrom_array = StringUtils.split(dateFrom, "-");
        String from_year_str = dateFrom_array[0];
        String from_month_str = removeZeroPrefix(dateFrom_array[1]);
        String from_date_str = removeZeroPrefix(dateFrom_array[2]);

        String[] dateTo_array = StringUtils.split(dateTo, "-");
        String to_year_str = dateTo_array[0];
        String to_month_str = removeZeroPrefix(dateTo_array[1]);
        String to_date_str = removeZeroPrefix(dateTo_array[2]);
        if( !StringUtils.equals(from_year_str, to_year_str)
                || !StringUtils.equals(from_month_str, to_month_str) ){
            return list;
        }

        int from = Integer.valueOf(from_date_str);
        int to = Integer.valueOf(to_date_str);
        for( int i = from; i<=to; i++ ){
            String date_num = String.valueOf(i);
            if( i<10 ){
                date_num = "0"+i;
            }

            list.add( from_year_str + "-" + from_month_str + "-" + date_num );
        }

        Collections.reverse(list);
        return list;
    }

    private static String convertToChineseDate(String input) {
        if( StringUtils.isEmpty(input) ){
            return "";
        }
        
        String[] array = StringUtils.split(input, "-");
        String year_str = array[0];
        String month_str = removeZeroPrefix(array[1]);
        String date_str = removeZeroPrefix(array[2]);


        return NumberConverter.toChineseNumber(Integer.valueOf(month_str)) + "月" + NumberConverter.toChineseNumber(Integer.valueOf(date_str)) + "日";
    }

    private static String removeZeroPrefix(String input) {
        if( StringUtils.isEmpty(input) ){
            return "";
        }

//        if( StringUtils.startsWith(input, "0") ){
//            return StringUtils.remove(input, "0");
//        }

        return input;
    }

    private static String getCompleteTitle(String text, String input) {

        String title = StringUtils.substring(input, 0, (int)(input.length()*0.5));
//        Pattern title_pattern = Pattern.compile("\\R{1,}("+title+")");
//        Pattern title_pattern = Pattern.compile(escapeSpecialChar(title));
//        Matcher matcher = title_pattern.matcher(text);
//        int titlePosition = 0;
//        while( matcher.find() ){
//            titlePosition = matcher.start() + title.length();
//            break;
//        }
//
//        for( int i = titlePosition+1; i<=text.length(); i++ ){
//            char nextChar = text.toCharArray()[i];
//
//            if( StringUtils.equals(System.lineSeparator(), String.valueOf(nextChar)) ){
//                break;
//            }
//            title += nextChar;
//
//        }

        return title;
    }

}
