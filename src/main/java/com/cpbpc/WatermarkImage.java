package com.cpbpc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WatermarkImage {

    public static void main(String[] args) {
        // Define the directory containing subfolders and images
        String directoryPath = "/Users/liuchaochih/Downloads/cpbpc-hymn";
        File directory = new File(directoryPath);

        // Get all JPG files recursively from the directory
        List<File> jpgFiles = new ArrayList<>();
        findJpgFiles(directory, jpgFiles);

        // Apply watermark to all found JPG files
        for (File jpgFile : jpgFiles) {
            applyWatermark(jpgFile);
        }

        System.out.println("Watermark applied to all images.");
    }

    // Recursive function to find all .jpg files in the directory and its subdirectories
    private static void findJpgFiles(File directory, List<File> jpgFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJpgFiles(file, jpgFiles); // Recursively search subfolders
                } else if (file.getName().toLowerCase().endsWith(".jpg")) {
                    jpgFiles.add(file); // Add JPG files to the list
                }
            }
        }
    }

    // Function to apply watermark to a single image file
    private static void applyWatermark(File inputFile) {
        try {
            // Load the original image
            BufferedImage originalImage = ImageIO.read(inputFile);

            // Create a new BufferedImage to draw on
            BufferedImage watermarkedImage = new BufferedImage(
                    originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

            // Initialize Graphics2D
            Graphics2D g2d = (Graphics2D) watermarkedImage.getGraphics();
            g2d.drawImage(originalImage, 0, 0, null);

            // Set watermark transparency (50% opacity)
            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f); // Adjust transparency here
            g2d.setComposite(alphaChannel);

            // Set watermark text properties
            g2d.setColor(Color.RED); // Set watermark color
            g2d.setFont(new Font("Arial", Font.BOLD, 50)); // Set font size and style

            String watermarkText = "CPBPC Use Only";

            // Set rendering hints for smooth text
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Measure the text size to calculate spacing
            FontMetrics fontMetrics = g2d.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(watermarkText);
            int textHeight = fontMetrics.getHeight();

            // Create an AffineTransform for rotating and scaling
            AffineTransform originalTransform = g2d.getTransform();
            double rotationAngle = Math.toRadians(-45); // Diagonal orientation

            // Rotate the watermark text
            AffineTransform transform = new AffineTransform();
            transform.rotate(rotationAngle);

            // Apply the rotation
            g2d.setTransform(transform);

            // Determine the step size (spacing) between watermarks based on text size
            int stepX = textWidth + 100; // Add some extra space for clarity
            int stepY = textHeight + 100; // Add some extra space for clarity

            // Loop through and draw the watermark text multiple times to cover the entire image
            for (int x = -originalImage.getWidth(); x < originalImage.getWidth() * 2; x += stepX) {
                for (int y = -originalImage.getHeight(); y < originalImage.getHeight() * 2; y += stepY) {
                    g2d.drawString(watermarkText, x, y);
                }
            }

            // Reset to the original transform
            g2d.setTransform(originalTransform);

            // Dispose the Graphics object
            g2d.dispose();

            // Save the watermarked image to a new file in the same folder with "_watermarked" appended to the name
            String outputFilePath = inputFile.getParent() + "/" + inputFile.getName().replace(".jpg", "_watermarked.jpg");
            File outputFile = new File(outputFilePath);
            ImageIO.write(watermarkedImage, "jpg", outputFile);

            System.out.println("Watermark applied to: " + inputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
