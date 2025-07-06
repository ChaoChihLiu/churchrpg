import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class TestPDFReader {

    public static void main(String[] args) {
        // Path to your PDF file
        String pdfFilePath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/OpenHymnal2014.06.pdf";
        String outputDirPath = "/Users/liuchaochih/Documents/GitHub/churchrpg/hymn_output";
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
                protected void processTextPosition(TextPosition text) {
                    BigDecimal fontSize = BigDecimal.valueOf(text.getFontSize());
                    fontSize = fontSize.round(mc);
//                    System.out.println("text: " + text + ", fontSize: " + fontSize);
                    if(fontSize.compareTo(BigDecimal.valueOf(15.0)) == 0
                            || fontSize.compareTo(BigDecimal.valueOf(14.4)) == 0 ){
                        buffer.append(text);
                    }
                    super.processTextPosition(text);
                }

            };

            // Get the total number of pages
//            int numberOfPages = document.getNumberOfPages();

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int currentHymnNumber = 0;
            // Iterate through each page and extract text
//            for (int page = 22; page <= 339; page++) {
            for (int page = 22; page <= 26; page++) {
                buffer.delete(0, buffer.toString().length());

                pdfStripper.setStartPage(page);
                pdfStripper.setEndPage(page);

                // Extract text from the current page
                String text = pdfStripper.getText(document);
                String title = buffer.toString();
//                if( !StringUtils.isEmpty(buffer.toString()) ){
//                    currentHymnNumber++;
//                    title = buffer.toString();
//                }

                // Print the text for the current page
                System.out.println("Text on page " + page + ":");
                System.out.println("Title: " + title);
                System.out.println(text);

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
