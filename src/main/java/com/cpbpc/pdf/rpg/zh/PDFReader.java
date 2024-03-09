package com.cpbpc.pdf.rpg.zh;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import software.amazon.awssdk.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFReader {

    private static Logger logger = Logger.getLogger(PDFReader.class.getName());

    private String pdfPath = "";
    public PDFReader(String path){
        this.pdfPath = pdfPath;
    }

    public List<ArticleParser> readContents(){
        List<ArticleParser> result = new ArrayList<>();
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            StringBuffer buffer = new StringBuffer();
            PDFTextStripper textStripper = new PDFTextStripper() {
                @Override
                protected void processTextPosition(org.apache.pdfbox.text.TextPosition text) {

                    float fontSize = text.getWidth();

                    if( Math.round(fontSize) == 14 ){
                        buffer.append(text.getUnicode());
                    }
                    super.processTextPosition(text);
                }
            };
           
            for (int page = 2; page <= document.getNumberOfPages(); ++page) {
                textStripper.setStartPage(page);
                textStripper.setEndPage(page);

                String pageText = textStripper.getText(document);
                String title = StringUtils.substring(buffer.toString(), 0, (int)(buffer.length()*0.5));
                ArticleParser parser = new ArticleParser(pageText, title);
                result.add(parser);

            }
        } catch (IOException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
        return result;
    }
    
    public static void main(String[] args) throws IOException {
//        String pdfFilePath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources//Daily+Remembrancer+International+Edition.pdf";
        String pdfFilePath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/24_apr-to-jun.pdf";

//        String input = "四月三十一日，礼拜一";
//        String regex = "(?:一|二|三|四|五|六|七|八|九|十|十一|十二)月[一|二|三|四|五|六|七|八|九|十]{1,3}日，(礼拜|星期)(?:一|二|三|四|五|六|日)";
//
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(input);
//
//        while (matcher.find()) {
//            System.out.println("Pattern found: " + matcher.group());
//        }

        String propPath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-chinese.properties";
        FileInputStream in = new FileInputStream(propPath);
        AppProperties.getConfig().load(in);

        try {
            DBUtil.initStorage(AppProperties.getConfig());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {

            for (int page = 58; page <= document.getNumberOfPages(); ++page) {
                StringBuffer buffer = new StringBuffer();
                PDFTextStripper textStripper = new PDFTextStripper() {
                    @Override
                    protected void processTextPosition(org.apache.pdfbox.text.TextPosition text) {

                        float fontSize = text.getWidth();

                        //（）
                        if( Math.round(fontSize) == 14
                                && (!StringUtils.equals("(", text.getUnicode()) && !StringUtils.equals(")", text.getUnicode()))
                                && (!StringUtils.equals("（", text.getUnicode()) && !StringUtils.equals("）", text.getUnicode()))){
                            buffer.append(text.getUnicode());
                        }
                        super.processTextPosition(text);
                    }
                };

                System.out.println("****************");
                textStripper.setStartPage(page);
                textStripper.setEndPage(page);

                String pageText = textStripper.getText(document);
                if(StringUtils.equals(StringUtils.trim(pageText), "Notes") ){
                    break;
                }

                String title = getCompleteTitle(pageText, buffer.toString());

                ArticleParser parser = new ArticleParser(pageText, title);
                parser.readDate();
                parser.readTopicVerses();
                parser.readFocusScripture();
                parser.readEnd();

                // Process pageText line by line
                String[] lines = pageText.split(System.lineSeparator());
                for (String line : lines) {
                    System.out.println(line);
                }
                System.out.println("****************");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCompleteTitle(String text, String input) {

        String title = StringUtils.substring(input, 0, (int)(input.length()*0.5));
        Pattern title_pattern = Pattern.compile("\\R{1,}("+title+")");
        Matcher matcher = title_pattern.matcher(text);
        int titlePosition = 0;
        while( matcher.find() ){
            titlePosition = matcher.start() + title.length();
            break;
        }

        for( int i = titlePosition+1; i<=text.length(); i++ ){
            char nextChar = text.toCharArray()[i];

            if( StringUtils.equals(System.lineSeparator(), String.valueOf(nextChar)) ){
                break;
            }
            title += nextChar;

        }

        return title;
    }
}
