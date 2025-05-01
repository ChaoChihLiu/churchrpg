package com.cpbpc.pdf.hymn;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.AmazonTextractException;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.S3Object;
import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AppProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PDFTextract {

    private static int start=665;
    private static int end=693;
    static final String s3BucketName = "churchhymn-textract";

    public static void main(String[] args) {
//        uploadHymnPages();
        extractText();
    }

    private static void extractText() {
        // Set up AWS credentials
        AmazonTextract textractClient = AmazonTextractClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_1) // Specify your region
                .withCredentials(new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials()))
                .build();

        for (int i = start; i <= end; i++) {
            // Document to be processed (stored in S3)
            S3Object s3Object = new S3Object()
                    .withBucket(s3BucketName)
                    .withName("hymn_pages/page_" + i + ".pdf");

            Document document = new Document().withS3Object(s3Object);

            // Request for text detection
            DetectDocumentTextRequest request = new DetectDocumentTextRequest().withDocument(document);
            try {
                // Call Textract API
                DetectDocumentTextResult result = textractClient.detectDocumentText(request);

                // Process the detected blocks of text
                List<Block> blocks = result.getBlocks();
                StringBuffer buffer = new StringBuffer("page" + i);
                buffer.append(System.lineSeparator());

                for (Block block : blocks) {
                    // Detect text lines
                    if (block.getBlockType().equals("LINE")) {
                        buffer.append(block.getText()).append(" ");
                    }

                    // Detect tables
                    else if (block.getBlockType().equals("TABLE")) {
                        buffer.append("Detected Table:").append(System.lineSeparator());
                        List<Block> childBlocks = block.getRelationships().get(0).getIds().stream()
                                .map(id -> blocks.stream().filter(b -> b.getId().equals(id)).findFirst().orElse(null))
                                .toList();

                        // Iterate through child blocks to find cells
                        for (Block childBlock : childBlocks) {
                            if (childBlock != null && childBlock.getBlockType().equals("CELL")) {
                                buffer.append("Cell Text: ").append(childBlock.getText()).append(System.lineSeparator());
                            }
                        }
                    }
                }

                buffer.append(System.lineSeparator()).append(StringUtils.rightPad("", 10, "-")).append(System.lineSeparator());
                try (FileWriter writer = new FileWriter("detected_text_output.txt", true)) { // 'true' enables append mode
                    writer.append(buffer);
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }
                System.out.println("Text detection results appended to file.");
            } catch (AmazonTextractException e) {
                e.printStackTrace();
            }
        }
    }

    private static void uploadHymnPages() {

        AppProperties.loadConfig(System.getProperty("app.properties",
                "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-hymn.properties"));
//        Connection conn = DBUtil.createConnection(AppProperties.getConfig());

        // Path to your PDF file
        String pdfFilePath = (String)AppProperties.getConfig().getOrDefault("pdf_path", "src/main/resources/openHymnal2014.06.pdf");

//        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//                .withRegion(Regions.AP_SOUTHEAST_1)  // Set your desired region
//                .withCredentials(new ProfileCredentialsProvider())  // Uses AWS credentials from your AWS CLI profile
//                .build();
        S3Client s3Client = AWSUtil.getS3Client();

        try {
            PDDocument document = PDDocument.load(new File(pdfFilePath));
            for (int page = start; page <= end; page++) {
                PDDocument singlePageDoc = new PDDocument();

                // Extract the current page and add it to the new document
                PDPage currentPage = document.getPage(page - 1);  // 0-based index
                singlePageDoc.addPage(currentPage);

                // Save the single page to a temporary file
                File tempFile = new File("page_" + page + ".pdf");
                singlePageDoc.save(tempFile);

                // Upload the single page PDF to S3
                String s3ObjectKey = "hymn_pages/page_" + page + ".pdf";
//                s3Client.putObject(new PutObjectRequest(s3BucketName, s3ObjectKey, tempFile));
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                        .bucket(s3BucketName)
                                                        .key(s3ObjectKey)
                                                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromFile(tempFile));

                System.out.println("Uploaded page " + page + " to S3 as " + s3ObjectKey);

                // Clean up temporary file and close the single page document
                singlePageDoc.close();
                tempFile.delete();
            }


            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
