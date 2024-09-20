package com.cpbpc.pdf.hymn;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import kotlin.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PDFReader {

//    static int start = 11;
//    static int end = 11;

    static int start = 403;
    static int end = 403;
//    static int start = 593;
//    static int end = 593;
//    static int start = 11;
//    static int end = 652;

    static List<String> groups = new ArrayList<>();
    static{
        groups.add("Praise and Thanksgiving");
        groups.add("The Lord's Day");
        groups.add("Creation and Providence");
        groups.add("Christ's Glory, Name and Praise");
        groups.add("Christ's Birth");

        groups.add("Christ's Life and Ministry");
        groups.add("Christ's Suffering and Death");
        groups.add("Christ's Resurrection");
        groups.add("Christ's Second Coming");
        groups.add("Invitation, Comfort and Assurance");

        groups.add("THE WORD OF GOD");
        groups.add("The Gospel Call");
        groups.add("The New Birth");
        groups.add("God's Grace");
        groups.add("Testimony");

        groups.add("Acceptance");
        groups.add("Assurance and Trust");
        groups.add("Fellowship with Christ");
        groups.add("Thankfulness, Joy and Gladness");
        groups.add("Sanctification and Holiness");

        groups.add("Repentance and Restoration");
        groups.add("Commitment and Consecration");
        groups.add("Zeal and Service");
        groups.add("Pilgrimage and Guidance");
        groups.add("Comfort and Encouragement");

        groups.add("Prayer and Intercession");
        groups.add("Spiritual Warfare and Victory");
        groups.add("Marriage, Home and Family");
        groups.add("The Nature of the Church");
        groups.add("Missions and Evangelism");

        groups.add("Death and Eternal Life");
        groups.add("Hymns for Children");
        groups.add("Choruses");
    }

    public static boolean hasRepeatingPattern(String str) {
        int n = str.length();

        // Loop over possible lengths of the repeating substring
        for (int len = 1; len <= n / 2; len++) {
            // Check if the string length is divisible by the candidate substring length
            if (n % len == 0) {
                // Get the candidate substring
                String substring = str.substring(0, len);

                // Build a new string by repeating the candidate substring
                StringBuilder repeated = new StringBuilder();
                for (int i = 0; i < n / len; i++) {
                    repeated.append(substring);
                }

                // Check if the built string matches the original string
                if (repeated.toString().equals(str)) {
                    return true; // Found a repeating pattern
                }
            }
        }

        return false; // No repeating pattern found
    }

    public static String getRepeatedWord(String str) {
        int n = str.length();

        // Loop over possible lengths of the repeating substring
        for (int len = 1; len <= n / 2; len++) {
            // Check if the string length is divisible by the candidate substring length
            if (n % len == 0) {
                // Get the candidate substring
                String substring = str.substring(0, len);

                // Build a new string by repeating the candidate substring
                StringBuilder repeated = new StringBuilder();
                for (int i = 0; i < n / len; i++) {
                    repeated.append(substring);
                }

                // Check if the built string matches the original string
                if (repeated.toString().equals(str)) {
                    return substring; // Found the repeating pattern
                }
            }
        }

        return ""; // No repeating pattern found
    }

    private static boolean isTitleStarted = false;

    public static <S3Client> void main(String[] args) throws SQLException {

        AppProperties.loadConfig(System.getProperty("app.properties",
                "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-hymn.properties"));

        // Path to your PDF file
        String pdfFilePath = (String)AppProperties.getConfig().getOrDefault("pdf_path", "src/main/resources/openHymnal2014.06.pdf");
        String outputDirPath = (String)AppProperties.getConfig().getOrDefault("output_path", "/Users/liuchaochih/Documents/GitHub/churchrpg/hymn_output");
        createHymnFolder(outputDirPath);

        final StringBuffer buffer = new StringBuffer();
        MathContext mc = new MathContext(3, RoundingMode.HALF_UP);
        try {
            // Load the PDF document
            PDDocument document = PDDocument.load(new File(pdfFilePath));

            // Create PDFTextStripper object
            PDFTextStripper pdfStripper = new PDFTextStripper() {

                private static boolean isBoldFont(TextPosition textPosition) {
                    return textPosition.getFont().getFontDescriptor().isForceBold() ||
                            (textPosition.getFont().getFontDescriptor().getFontWeight() >= 700);
                }

                private static boolean isItalicFont(TextPosition textPosition) {
                    return textPosition.getFont().getFontDescriptor().isItalic();
                }

                @Override
                protected void processTextPosition(org.apache.pdfbox.text.TextPosition text) {
//                    BigDecimal fontSize = BigDecimal.valueOf(text.getFontSize());
//                    fontSize = fontSize.round(mc);
//                    System.out.println("text: " + text + ", fontSize: " + fontSize);
//                    System.out.println("text: " + text + ", isBold: " + isBoldFont(text));
//                    if(fontSize.compareTo(BigDecimal.valueOf(15.0)) == 0
//                            || fontSize.compareTo(BigDecimal.valueOf(14.0)) == 0){
//                        buffer.append(text);
//                    }
//                    if(fontSize.compareTo(BigDecimal.valueOf(15.0)) == -1
//                            && fontSize.compareTo(BigDecimal.valueOf(13.5)) == 1){
//                        buffer.append(text);
//                    }
//                    if( isBoldFont(text) ){
//                        System.out.println("break here: " + text.getFont().getFontDescriptor().getFontFamily());
//                    }
                    if( isBoldFont(text)
                            && (text.getFont().getFontDescriptor().getFontFamily().equals("Plantin MT Pro")
                                || text.getFont().getFontDescriptor().getFontFamily().equals("Times New Roman")
                                || text.getFont().getFontDescriptor().getFontFamily().equals("Times")) ){
//                    if( isBoldFont(text)
//                            && (text.getFont().getFontDescriptor().getFontFamily().equals("Plantin MT Pro")) ){
//                        System.out.println("text: " + text + ", Ascent: " + text.getFont().getFontDescriptor().getAscent());
                        if( isTitleStarted == false ){
                            isTitleStarted = true;
                        }
                        if( isTitleStarted == true ){
                            buffer.append(text);
                        }
                    }else{
                        if( isTitleStarted == true ){
                            isTitleStarted = false;
                            buffer.append(System.lineSeparator());
                        }
                    }

                    super.processTextPosition(text);
                }

            };

            //organise hymn and its pages
//            PDFRenderer pdfRenderer = new PDFRenderer(document);
            Map<String, List<Integer>> map = new HashMap();
            List<Integer> copyrightsPages = new ArrayList<>();
            String fileContent = IOUtils.toString(new FileInputStream(new File("detected_text_output.txt")), StandardCharsets.UTF_8);
            List<String> hymns = Arrays.asList(fileContent.split(StringUtils.rightPad("", 10, "-")));
            String pervious_title = "";
            for (int page = start; page <= end; page++) {
//            for (int page = 636; page <= 636; page++) {
                buffer.delete(0, buffer.toString().length());

                pdfStripper.setStartPage(page);
                pdfStripper.setEndPage(page);

                // Extract text from the current page
                String text = pdfStripper.getText(document);
                List<String> titles = splitTitle(buffer.toString());

                if( titles.size() <= 0 ){
                    List<Integer> list = map.getOrDefault(pervious_title, new ArrayList<>());
                    list.add(page);
                    map.put(pervious_title, list);
                    continue;
                }

                for( String title: titles ){
                    pervious_title = title;
                    List<Integer> list = map.getOrDefault(title, new ArrayList<Integer>());
                    list.add(page);
                    map.put(title, list);

                    int fountNumber = findHymnNumber(title, page, hymns);
                    boolean hasCopyrights = checkCopyrights(fountNumber, title, text);
                    if( hasCopyrights ){
                        copyrightsPages.add(page);
                    }
                }
            }

            //lost: Sweet the Moments, Rich in Blessing (371),Over the Sunset Mountains (551)
            for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());

                convertImage(entry.getKey(), entry.getValue(), copyrightsPages, document, hymns);
                insertToDB( entry.getKey(), entry.getValue(),document, hymns, "churchhymnal" );
            }
            System.out.println("copyrights: " + copyrightsPages.size());
            // Close the document
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            if( conn != null ){
//                conn.close();
//            }
        }
    }

    private static String removeNoise(String input) {

        return input.replaceAll("\\p{Punct}", "")
                    .replaceAll(" ", "")
                ;
    }


    private static Pair<Integer, String> convertImage(String hymnName,
                                                      List<Integer> pages,
                                                      List<Integer> copyrightsPages,
                                                      PDDocument document,
                                                      List<String> hymns) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        String outputDirPath = (String)AppProperties.getConfig().getOrDefault("output_path", "/Users/liuchaochih/Documents/GitHub/churchrpg/hymn_output");
        
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_1)  // Set your desired region
                .withCredentials(new ProfileCredentialsProvider())  // Uses AWS credentials from your AWS CLI profile
                .build();

        int currentHymnNumber = 0;
        String lyrics = "";
        StringBuffer buffer = new StringBuffer();
        for (int page: pages) {

            int fountNumber = findHymnNumber(hymnName, page, hymns);
            if( fountNumber > 0 ){
                currentHymnNumber = fountNumber;
            }
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page-1, 300); // 300 DPI is a good quality

            String hymnPath = outputDirPath + "/" ;
            String imgKeyName = currentHymnNumber + "_" + replaceWhitespace(hymnName) + "/" + "page_" + (page) + ".jpg";
            String txtKeyName = currentHymnNumber + "_" + replaceWhitespace(hymnName) + "/" + "page_" + (page) + ".txt";
            createHymnFolder(hymnPath + currentHymnNumber + "_" + replaceWhitespace(hymnName) + "/");
            File outputImage = new File(hymnPath + imgKeyName);
            File outputText = new File(hymnPath + txtKeyName);

            ImageIO.write(bufferedImage, "JPEG", outputImage);
            String temp = findLyric(hymnName, page, hymns);
            if( !StringUtils.isEmpty(temp) ){
                lyrics = temp;
                buffer.append(lyrics);
            }

            FileOutputStream fo = new FileOutputStream(outputText);
            IOUtils.write(lyrics, fo, StandardCharsets.UTF_8);
            IOUtils.closeQuietly(fo);

            if( copyrightsPages.contains(page) ){
                System.out.println("page " + page + " has copyrights");
                return new Pair<Integer, String>(currentHymnNumber, buffer.toString());
            }
//            addWatermark( hymnPath + currentHymnNumber + "_" + hymnName + "/", outputImage, "Calvary Pandan Use Only" );

            PutObjectRequest imgPutReq = new PutObjectRequest((String)AppProperties.getConfig().get("bucket_name"), imgKeyName, outputImage);
            s3Client.putObject(imgPutReq);
            PutObjectRequest txtPutReq = new PutObjectRequest((String)AppProperties.getConfig().get("bucket_name"), txtKeyName, outputText);
            s3Client.putObject(txtPutReq);

        }
        return new Pair<Integer, String>(currentHymnNumber, buffer.toString());
    }

    private static String findLyric(String hymnName, int page, List<String> hymns) {
        String result = "";
        for( String hymnLyric: hymns ){
            if( StringUtils.startsWith(StringUtils.trim(hymnLyric), "page"+page+System.lineSeparator()) ){
                result = StringUtils.trim(hymnLyric);
            }
        }

        if( !StringUtils.isEmpty(result) ){
            return result;
        }

        for( String hymnLyric: hymns ){
            if( StringUtils.contains(StringUtils.trim(hymnLyric.replaceAll(" ", "")).toLowerCase(),
                    hymnName.replaceAll(" ", "").toLowerCase()) ){
                result = StringUtils.trim(hymnLyric);
            }
        }


        return result;
    }

    private static int findHymnNumber(String hymnName, int page, List<String> hymns) {

        for( String hymnLyric: hymns ){
            if( !StringUtils.startsWith(StringUtils.trim(hymnLyric), "page"+page) ){
                continue;
            }

            String hymnNameRegex = hymnName.replaceAll("’", "'")
                                            .replaceAll("\\?", "\\\\?")
                                            .replaceAll(" ", "\\\\s{0,}")
                                            .toLowerCase();
            StringBuffer regex = new StringBuffer("(\\d+)\\s*");
            regex.append(hymnNameRegex)
                    .append("|")
                    .append(hymnNameRegex)
                    .append("\\s*(\\d+)")
            ;
            //String regex = "(\\d+)\\s*Saviour, More than Life to Me|Saviour, More than Life to Me\\s*(\\d+)";
            System.out.println("regex: " + regex.toString());

            // Compile the pattern
            Pattern pattern = Pattern.compile(regex.toString());
            Matcher matcher = pattern.matcher(StringUtils.trim(hymnLyric).toLowerCase());

            // Find and print the matched number
            if (matcher.find()) {
                String numberBefore = matcher.group(1);
                String numberAfter = matcher.group(2);

                if (numberBefore != null) {
                    System.out.println("Number before "+hymnName+": " + numberBefore);
                    return Integer.parseInt(numberBefore);
                } else if (numberAfter != null) {
                    System.out.println("Number after "+hymnName+": " + numberAfter);
                    return Integer.parseInt(numberAfter);
                } else {
                    System.out.println("No number found.");
                }
            } else {
//                System.out.println("No match found.");
            }
        }
        return 0;
    }

    static Pattern pattern = Pattern.compile("©\\s*(\\d{4})");
    static int currentYear = LocalDate.now().getYear();
    static File copyrightsCSV = new File("copyrights.csv");
    private static boolean checkCopyrights(int hymnNum, String hymnName, String text) throws IOException {
        if( !StringUtils.contains(text, "©") ){
            return false;
        }

        FileWriter copyRightsWrite = new FileWriter(copyrightsCSV, true);
        String copyrightsStat = getCopyrightsWords(text, "©", text.indexOf("©")+1);
        IOUtils.write(hymnNum + ",\"" + hymnName + "\",\"" + copyrightsStat + "\"" + System.lineSeparator(), copyRightsWrite);
        IOUtils.closeQuietly(copyRightsWrite);

        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int position = matcher.end();
            int year = Integer.parseInt(matcher.group(1));

            if((year+70) >= currentYear){
                return true;
            }

        } else {
            return true;
        }

        return false;
    }

    private static String getCopyrightsWords(String text, String input, int start) {

        StringBuffer buffer = new StringBuffer(input);
        for( int i = start; i<=text.length(); i++ ){
            char nextChar = text.charAt(i);
            if( nextChar == '.' ){
                break;
            }

            buffer.append(nextChar);
        }

        return buffer.toString();
    }

    private static List<String> splitTitle(String input) {


        String[] temp = StringUtils.split(input, System.lineSeparator());
        Set<String> set2 = new LinkedHashSet<>(Arrays.asList(temp));

        List<String> out = new ArrayList<>();
        for( String str : set2 ){
            if( StringUtils.equalsIgnoreCase(str, "See the Con-q'ror Mounts in Triumph") ){
                continue;
            }
            if( StringUtils.equalsIgnoreCase("Vienna, 77.77Jesus, Lord, We Look to Thee", str) ){
                str = "Jesus, Lord, We Look to Thee";
            }

            if(
                    StringUtils.equalsIgnoreCase("fine", str.trim())
                        || StringUtils.equalsIgnoreCase("D.C.", str.trim())
                        || StringUtils.equalsIgnoreCase("D.S.", str.trim())
                        || StringUtils.equalsIgnoreCase("D.S. al Fine", str.trim())
                        || StringUtils.equalsIgnoreCase("CODA", str.trim())
            ){
                continue;
            }
            if(hasRepeatingPattern(str.trim())){
                out.add(getRepeatedWord(str.replaceAll("’", "'").trim()));
            }else{
                out.add(str.replaceAll("’", "'").trim());
            }
        }

        return out;
    }

    private static final String INSERT_STATEMENT = "INSERT INTO cpbpc_hymn " +
                                                    "(category, content, title, seq_no, `group`) " +
                                                    "VALUES(?, ?, ?, ?, ?) "
//                                                    + "ON DUPLICATE KEY UPDATE content = ?, seq_no=?, `group`=?"
            ;
    private static void insertToDB(String hymnName,
                                   List<Integer> pages,
                                   PDDocument document,
                                   List<String> hymns,
                                   String category) throws SQLException {
        Connection conn = DBUtil.createConnection(AppProperties.getConfig());

        PreparedStatement stat = null;
        int currentHymnNumber = 0;
        String lyrics = "";
        StringBuffer buffer = new StringBuffer();
        try{
            for( int page: pages ){
                String temp = findLyric(hymnName, page, hymns);
                if( !StringUtils.isEmpty(temp) ){
                    lyrics = temp;
                    buffer.append(lyrics);
                }
                int fountNumber = findHymnNumber(hymnName, page, hymns);
                if( fountNumber > 0 ){
                    currentHymnNumber = fountNumber;
                }

                String replaced = buffer.toString().replaceAll("[^\\p{ASCII}]", "")
                        .replaceAll(" ", "")
                        .replaceAll(System.lineSeparator(), "")
                        .replaceAll("\\p{Punct}", "")
                        .replaceAll("'", "''")
                        ;

                String group = findHymnGroup(buffer.toString());

                stat = conn.prepareStatement(INSERT_STATEMENT);

                stat.setString(1, category);
                stat.setString(2, replaced);
                stat.setString(3, hymnName.replaceAll("'", "''"));
                stat.setInt(4, currentHymnNumber);
                stat.setString(5, group);
//            stat.setString(6, replaced);
//            stat.setInt(7, number);
//            stat.setString(8, group);

                System.out.println("stat.toString(): " + stat.toString());

                stat.execute();
            }
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        } finally {
            if( stat != null ) {
                stat.close();
            }
            conn.close();
        }

    }

    private static String findHymnGroup(String text) {

        for( String group : groups ){

            if( StringUtils.contains(text.replaceAll("’", "'").replaceAll(" ", "").toLowerCase(),
                                    group.replaceAll("’", "'").replaceAll(" ", "").toLowerCase()) ){
                return group;
            }

        }

        return "";
    }

    private static void createHymnFolder(String path) {
        File dir = new File(path);
        if( !dir.exists() ){
            dir.mkdirs();
        }
    }

    private static String replaceWhitespace(String title) {
        return title.replaceAll(" ", "_" );
    }

    private static void addWatermark(String outputDirPath, File jpgFile, String watermarkText){
        int fontSize = 30; // Reduced font size
        // Margin from edges
        int margin = 100; // Increased margin
        // Spacing between watermarks
        int horizontalSpacing = 300; // Increased spacing
        int verticalSpacing = 300;   // Increased spacing
        
        try {
            // Load the JPEG image
            BufferedImage image = ImageIO.read(jpgFile);

            // Create graphics object from the image
            Graphics2D g2d = (Graphics2D) image.getGraphics();
            // Enable anti-aliasing for smoother text
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Set watermark font and color
            Font font = new Font("Arial", Font.BOLD, fontSize);
            g2d.setFont(font);
            g2d.setColor(new Color(255, 0, 0, 30)); // Red color with transparency

            // Rotation angle
            double rotationAngle = Math.toRadians(45); // 45 degrees in radians

            // Calculate number of rows and columns
            int numRows = (image.getHeight() - 2 * margin) / verticalSpacing + 1;
            int numCols = (image.getWidth() - 2 * margin) / horizontalSpacing + 1;

            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++) {
                    int x = margin + col * horizontalSpacing;
                    int y = margin + row * verticalSpacing;

                    // Save current transformation matrix
                    AffineTransform oldTransform = g2d.getTransform();

                    // Rotate around the center of the watermark
                    g2d.rotate(rotationAngle, x + fontSize / 2, y + fontSize / 2);

                    // Draw watermark text
                    g2d.drawString(watermarkText, x, y);

                    // Restore the original transformation matrix
                    g2d.setTransform(oldTransform);
                }
            }

            // Clean up graphics
            g2d.dispose();

            // Save the watermarked image
            File outputFile = new File(outputDirPath + jpgFile.getName());
            ImageIO.write(image, "JPEG", outputFile);

            System.out.println("Watermarked image saved to " + outputFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
