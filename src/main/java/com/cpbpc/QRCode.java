package com.cpbpc;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class QRCode {

    public static void main(String[] args) {
        String data = "https://d3q5injsllmv6q.cloudfront.net/acm/eng";
        String filePath = "2025-ACM-Eng.png";

        try {
            // Generate QR code and save it as an image
            generateQRCodeImage(data, 350, 350, filePath);
            System.out.println("QR Code generated and saved to: " + filePath);
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }

    public static void generateQRCodeImage(String data, int width, int height, String filePath) throws Exception {
        // Set QR code properties
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.MARGIN, 1);

        // Generate QR code
        BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);

        // Convert BitMatrix to BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, (bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB()));
            }
        }

        // Write the image to a file
        ImageIO.write(image, "PNG", new File(filePath));
    }
}

