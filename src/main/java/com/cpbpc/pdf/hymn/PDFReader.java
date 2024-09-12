package com.cpbpc.pdf.hymn;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cpbpc.comms.AppProperties;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;



public class PDFReader {

    public static <S3Client> void main(String[] args) {

        AppProperties.loadConfig(System.getProperty("app.properties",
                "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-hymn.properties"));

        // Path to your PDF file
        String pdfFilePath = (String)AppProperties.getConfig().getOrDefault("pdf_path", "src/main/resources/openHymnal2014.06.pdf");
        String outputDirPath = (String)AppProperties.getConfig().getOrDefault("output_path", "/Users/liuchaochih/Documents/GitHub/churchrpg/hymn_output");
        createHymnFolder(outputDirPath);

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_1)  // Set your desired region
                .withCredentials(new ProfileCredentialsProvider())  // Uses AWS credentials from your AWS CLI profile
                .build();

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
                    BigDecimal fontSize = BigDecimal.valueOf(text.getFontSize());
                    fontSize = fontSize.round(mc);
//                    System.out.println("text: " + text + ", fontSize: " + fontSize);
                    if(fontSize.compareTo(BigDecimal.valueOf(15.0)) == 0
                            || fontSize.compareTo(BigDecimal.valueOf(14.0)) == 0){
                        buffer.append(text);
                    }
                    if(fontSize.compareTo(BigDecimal.valueOf(15.0)) == -1
                            && fontSize.compareTo(BigDecimal.valueOf(13.5)) == 1){
                        buffer.append(text);
                    }
                    super.processTextPosition(text);
                }

            };

            // Get the total number of pages
//            int numberOfPages = document.getNumberOfPages();

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int currentHymnNumber = 0;
            String title = "";
            // Iterate through each page and extract text
            for (int page = 22; page <= 339; page++) {
//            for (int page = 111; page <= 112; page++) {
                buffer.delete(0, buffer.toString().length());

                pdfStripper.setStartPage(page);
                pdfStripper.setEndPage(page);

                // Extract text from the current page
                String text = pdfStripper.getText(document);
                if( !StringUtils.isEmpty(buffer.toString())
                        && !StringUtils.equalsIgnoreCase(buffer.toString(), title) ){
                    currentHymnNumber++;
                    title = buffer.toString();
                    title = title.replaceAll("\\p{Punct}", "");
                    title = replaceWhitespace(title);
                }

                // Print the text for the current page
                System.out.println("Text on page " + page + ":");
                System.out.println("Title: " + title);
//                System.out.println(text);
//                System.out.println("------");

                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page-1, 300); // 300 DPI is a good quality

                String hymnPath = outputDirPath + "/" ;
                String imgKeyName = currentHymnNumber + "_" + title + "/" + "page_" + (page) + ".jpg";
                String txtKeyName = currentHymnNumber + "_" + title + "/" + "page_" + (page) + ".txt";
                createHymnFolder(hymnPath + currentHymnNumber + "_" + title + "/");
                // Define the output JPEG file path
                File outputImage = new File(hymnPath + imgKeyName);
                File outputText = new File(hymnPath + txtKeyName);

                // Write the BufferedImage to JPEG file
                ImageIO.write(bufferedImage, "JPEG", outputImage);
                IOUtils.write(text, new FileOutputStream(outputText), StandardCharsets.UTF_8);
                addWatermark( hymnPath + currentHymnNumber + "_" + title + "/", outputImage, "Calvary Pandan Use Only" );

//                java.util.List tags = new ArrayList<Tag>();
//                String titleValue = URLEncoder.encode(buffer.toString().replaceAll("\\p{Punct}", ""), StandardCharsets.UTF_8);
//                tags.add(new Tag("title", titleValue));
//                tags.add(new Tag("number", String.valueOf(currentHymnNumber)));
//
//                System.out.println("title " + titleValue);
//                System.out.println("number " + currentHymnNumber);

//                ObjectTagging objectTagging = new ObjectTagging(tags);
                PutObjectRequest imgPutReq = new PutObjectRequest("testhymn", imgKeyName, outputImage);
                s3Client.putObject(imgPutReq);
                PutObjectRequest txtPutReq = new PutObjectRequest("testhymn", txtKeyName, outputText);
                s3Client.putObject(txtPutReq);
            }

            // Close the document
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
