package com.cpbpc.comms;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.StorageClass;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class AWSUtil {

    private static java.util.logging.Logger logger = Logger.getLogger(AWSUtil.class.getName());

    private static S3Client s3Client = null;
    static{
        if( s3Client == null ){
            s3Client = S3Client.builder()
                    .region(Region.of(AppProperties.getConfig().getProperty("region")))
//                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .credentialsProvider(ProfileCredentialsProvider.create("cpbpc"))
                    .build();
        }
    }

    public static S3Client getS3Client(){
        return s3Client;
    }
    
    private static List<Tag> saveToS3(String content, String bucketName, String objectKey, String audioKey, int count) {
        return saveToS3(content, bucketName, objectKey, "", "", audioKey, count);
    }

    private static List<Tag> saveToS3(String content, String bucketName, String objectKey, String month, String date, String audioKey, int count) {
        List<Tag> tags = new ArrayList<>();
        //            InputStream inputStream = new StringInputStream(content);
        // Upload the file to S3
//            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, createS3ObjMetadata());

        if( !StringUtils.isEmpty(month) && !StringUtils.isEmpty(date) ){
            tags.add(createS3Tag("publish_date", month + "_" + genDatePath(date, count)));
        }
        tags.add(createS3Tag("voice_id", AppProperties.getConfig().getProperty("voice_id")));
        tags.add(createS3Tag("category", URLDecoder.decode(AppProperties.getConfig().getProperty("content_category"))));
        tags.add(createS3Tag("audio_key", audioKey));
//            if( AppProperties.getConfig().containsKey("name_prefix") ){
//                tags.add(createS3Tag("name_prefix", AppProperties.getConfig().getProperty("name_prefix")));
//            }
        tags.add(createS3Tag("output_bucket", AppProperties.getConfig().getProperty("output_bucket")));
        tags.add(createS3Tag("output_format", AppProperties.getConfig().getProperty("output_format")));
        tags.add(createS3Tag("output_prefix", AppProperties.getConfig().getProperty("output_prefix")));
        tags.add(createS3Tag("engine", AppProperties.getConfig().getProperty("engine")));

//            if( AppProperties.getConfig().containsKey("pl_script_bucket") ){
//                tags.add(createS3Tag("pl_script_bucket", AppProperties.getConfig().getProperty("pl_script_bucket")));
//                tags.add(createS3Tag("pl_script", StringUtils.replace(StringUtils.remove(objectKey, " "),
//                                                                            AppProperties.getConfig().getProperty("script_format"),
//                                                                            AppProperties.getConfig().getProperty("pl_format"))));
//            }

        Tagging tagging = Tagging.builder().tagSet(tags).build();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .cacheControl("no-store, no-cache, must-revalidate")
                .expires(Instant.EPOCH)
                .bucket(bucketName)
                .key(objectKey)
                .contentType("text/plain")
                .storageClass(StorageClass.INTELLIGENT_TIERING)
                .tagging(tagging)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(content));
        return tags;

    }

    public static Tag createS3Tag(String key, String value) {
        return Tag.builder().key(key).value(value).build();
    }

    public static void putBibleScriptToS3(String content, String book, String chapter, String verseNum) throws IOException, InterruptedException {

        book = StringUtils.replace(book, " ", "");
        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
        String prefix = AppProperties.getConfig().getProperty("script_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        String nameToBe = chapter+"/"+verseNum;
        String objectType = AppProperties.getConfig().getProperty("script_format");
        String objectKey = prefix + book + "/" + nameToBe + "." + objectType;
        String audioKey =  AppProperties.getConfig().getProperty("output_prefix") + book + "/" + nameToBe + "."
                + AppProperties.getConfig().getProperty("output_format");

        Thread.sleep(100);

        saveToS3(content, bucketName, objectKey, audioKey, 0);

    }

    public static String returnPLObjectKey(String fileName){
        String prefix = AppProperties.getConfig().getProperty("pl_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        String objectType = AppProperties.getConfig().getProperty("pl_format");
        String objectKey = prefix + fileName + "." + objectType;

        return objectKey;
    }

    public static void putBiblePLScriptToS3(String content, String fileName, String audioKey) {
        String bucketName = AppProperties.getConfig().getProperty("pl_script_bucket");
        String objectKey = returnPLObjectKey(fileName);
//        saveToS3(content, bucketName, objectKey, audioKey);
        List<Tag> tags = new ArrayList<>();

        tags.add(createS3Tag("audio_key", audioKey));
        tags.add(createS3Tag("output_bucket", AppProperties.getConfig().getProperty("audio_merged_bucket")));
        tags.add(createS3Tag("output_format", AppProperties.getConfig().getProperty("audio_merged_format")));
        tags.add(createS3Tag("output_prefix", AppProperties.getConfig().getProperty("audio_merged_prefix")));

        Tagging tagging = Tagging.builder().tagSet(tags).build();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .cacheControl("no-store, no-cache, must-revalidate")
                .expires(Instant.EPOCH)
                .bucket(bucketName)
                .key(objectKey)
                .contentType("text/plain")
                .storageClass(StorageClass.INTELLIGENT_TIERING)
                .tagging(tagging)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromString(content));
    }

    public static void putPLScriptToS3(String content, String month, String date) {

        String bucketName = AppProperties.getConfig().getProperty("pl_script_bucket");
        String prefix = AppProperties.getConfig().getProperty("pl_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

//        String publishMonth = publishDate_str.split("-")[0] + "_" + publishDate_str.split("-")[1];
//        String publishDate = publishDate_str.split("-")[2];
        String publishDate_str = month.replaceAll("-", "").replace("_", "")+date;
        String nameToBe = AppProperties.getConfig().getProperty("name_prefix") + publishDate_str.replaceAll("-", "");
        String objectType = AppProperties.getConfig().getProperty("pl_format");
        String objectKey = prefix + month + "/" + date + "/" + nameToBe + "." + objectType;
        String audioKey =  AppProperties.getConfig().getProperty("output_prefix")
                + month + "/"
                + nameToBe + "."
                + AppProperties.getConfig().getProperty("output_format");

        saveToS3(content, bucketName, objectKey, month, date, audioKey, 0);

    }

    public static List<Tag> putScriptToS3(String objectName, String content, String month, String date, String timing) {

        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
        String prefix = AppProperties.getConfig().getProperty("script_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

//        String publishMonth = publishDate_str.split("-")[0] + "_" + publishDate_str.split("-")[1];
//        String publishDate = publishDate_str.split("-")[2];
        String objectType = AppProperties.getConfig().getProperty("script_format");
        String objectKey = prefix + month + "/" + date + "/" + StringUtils.lowerCase(timing) + "/" + objectName + "." + objectType;
        String audioKey =  AppProperties.getConfig().getProperty("output_prefix")
                + month + "/"
                + date + "/"
                + StringUtils.lowerCase(timing) + "/"
                + objectName + "."
                + AppProperties.getConfig().getProperty("output_format");

        return saveToS3(content, bucketName, objectKey, month, date, audioKey, 0);

    }

    public static List<Tag> putScriptToS3(String objectName, String content, String month, String date, int count) {

        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
        String prefix = AppProperties.getConfig().getProperty("script_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

//        String publishMonth = publishDate_str.split("-")[0] + "_" + publishDate_str.split("-")[1];
//        String publishDate = publishDate_str.split("-")[2];
        String objectType = AppProperties.getConfig().getProperty("script_format");
        String objectKey = prefix + month + "/" + genDatePath(date, count) + "/" + objectName + "." + objectType;
        String audioKey =  AppProperties.getConfig().getProperty("output_prefix")
                + month + "/"
                + genDatePath(date, count) + "/"
                + objectName + "."
                + AppProperties.getConfig().getProperty("output_format");

        return saveToS3(content, bucketName, objectKey, month, date, audioKey, count);

    }

//    public static List<Tag> putScriptToS3(String content, String publishDate_str) {
//
//        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
//        String prefix = AppProperties.getConfig().getProperty("script_prefix");
//        if (!prefix.endsWith("/")) {
//            prefix += "/";
//        }
//
//        String publishMonth = publishDate_str.split("-")[0] + "_" + publishDate_str.split("-")[1];
//        String nameToBe = AppProperties.getConfig().getProperty("name_prefix") + publishDate_str.replaceAll("-", "");
//        String objectType = AppProperties.getConfig().getProperty("script_format");
//        String objectKey = prefix + publishMonth + "/" + nameToBe + "." + objectType;
//        String audioKey =  AppProperties.getConfig().getProperty("output_prefix")
//                            + publishMonth + "/"
//                            + nameToBe + "."
//                            + AppProperties.getConfig().getProperty("output_format");
//
//        return saveToS3(content, bucketName, objectKey, publishDate_str, audioKey);
//
//    }

//    public static void putScriptToS3(String content, String publishDate_str, String timing) throws IOException {
//
//        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
//        String prefix = AppProperties.getConfig().getProperty("script_prefix");
//        if (!prefix.endsWith("/")) {
//            prefix += "/";
//        }
//
//        String publishMonth = StringUtils.trim(publishDate_str.split(" ")[0]);
//        String publishDate = StringUtils.trim(publishDate_str.split(" ")[1]);
//        String nameToBe = publishDate.replaceAll(" ", "")+"_"+ StringUtils.lowerCase(timing);
//        String objectType = AppProperties.getConfig().getProperty("script_format");
//        String objectKey = prefix + publishMonth + "/" + nameToBe + "." + objectType;
//        String audioKey =  AppProperties.getConfig().getProperty("output_prefix")
//                + publishMonth + "/"
//                + nameToBe + "."
//                + AppProperties.getConfig().getProperty("output_format");
//
//        saveToS3(content, bucketName, objectKey, publishDate_str, audioKey);
//    }

    public static String toPolly(String content){
        return "<speak><prosody rate='" + AppProperties.getConfig().getProperty("speech_speed")
                + "' volume='" + AppProperties.getConfig().getProperty("speech_volume") + "'>"
                + content
                + "</prosody></speak>";
    }

    public static void copyS3Objects(String bucketName, String outputPrefix, String outputFormat, String local_destination) {

        List<S3Object> objects = listS3Objects(bucketName, outputPrefix);
        for( S3Object object: objects ){
            String fileName = object.key();
            logger.info("object key " + fileName);
            if( !org.apache.commons.lang3.StringUtils.endsWith(fileName, outputFormat) ){
                continue;
            }

            downloadS3Object(bucketName, fileName, local_destination+fileName);
        }
        

    }

    public static void downloadS3Object(String bucketName, String objectKey, String localFilePath){

        ResponseInputStream<GetObjectResponse> s3Object = null;
        try {
            File localFile = new File(localFilePath);
            if( !localFile.exists() ){
                localFile.createNewFile();
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                                .bucket(bucketName)
                                                .key(objectKey)
                                                .build();
            s3Object = s3Client.getObject(getObjectRequest);

            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                IOUtils.copy(s3Object, fos);
                logger.info("Object downloaded successfully to: " + localFilePath);
            }
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }finally{
            if( s3Object != null ){
                try {
                    s3Object.close();
                } catch (IOException e) {
                    logger.info(ExceptionUtils.getStackTrace(e));
                }
            }
        }

    }
    public static String readS3Object(String bucketName, String objectKey) {
        ResponseInputStream<GetObjectResponse> s3Object = null;

        try {
            // Create the GetObjectRequest to fetch the object from S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // Fetch the object from S3
            s3Object = s3Client.getObject(getObjectRequest);

            // Convert the InputStream to a String
            return IOUtils.toString(s3Object, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.info("Error reading S3 object: " + ExceptionUtils.getStackTrace(e));
        } finally {
            // Safely close the stream
            if (s3Object != null) {
                try {
                    s3Object.close();
                } catch (IOException e) {
                    logger.info("Error closing S3 object stream: " + e.getMessage());
                }
            }
        }

        return StringUtils.EMPTY;
    }
    public static void uploadS3Object(String bucketName, String prefix, String objectKey, File localFile, List<Tag> tags){

        try {
            if (!localFile.exists() || !localFile.isFile()) {
                logger.info(localFile.getAbsolutePath() + " does not exist or is not a file, skipping upload.");
                return;
            }

            // Create the PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(prefix + objectKey)
                    .storageClass(StorageClass.INTELLIGENT_TIERING)  // Setting storage class
                    .tagging(Tagging.builder().tagSet(tags).build())  // Setting tags
                    .cacheControl("no-store, no-cache, must-revalidate")
                    .expires(Instant.EPOCH)
                    .build();

            // Upload the file using the S3 client
            try (FileInputStream fis = new FileInputStream(localFile)) {
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fis, localFile.length()));
            }

            logger.info("Object uploaded successfully to S3 bucket: " + localFile.getName());
            Thread.sleep(1000); // If required, add a sleep time to ensure consistency

        } catch (Exception e) {
            logger.info("Error uploading object to S3: " + e.getMessage());
        }
    }
    public static void uploadS3Object(String bucketName, String prefix, String objectKey, String content, List<Tag> tags){

        try {
            if (content == null || content.isEmpty()) {
                logger.info("Content is empty, skipping upload.");
                return;
            }

            // Ensure the prefix ends with a "/"
            if (!prefix.endsWith("/")) {
                prefix += "/";
            }

            // Build the PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(prefix + objectKey)
                    .storageClass(StorageClass.INTELLIGENT_TIERING)  // Setting storage class
                    .tagging(Tagging.builder().tagSet(tags).build())  // Setting tags
                    .cacheControl("no-store, no-cache, must-revalidate")
                    .expires(Instant.EPOCH)
                    .build();

            // Upload the content as an InputStream
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

            // Upload the content to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(byteArrayInputStream, content.length()));

            logger.info("Object uploaded successfully to S3 bucket: " + bucketName);
            Thread.sleep(1000);  // Optional sleep to ensure consistency

        } catch (Exception e) {
            logger.info("Error uploading object to S3: " + e.getMessage());
        }
    }

    public static List<S3Object> listS3Objects(String bucketName, String prefix) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.contents(); // returns List<S3Object>
        } catch (Exception e) {
            // log the exception if needed
            return null;
        }
    }

    public static void purgeBucket(String outputBucket, String outputPrefix) {
        List<S3Object> summaries = listS3Objects(outputBucket, outputPrefix); // Updated to use SDK v2 return type
        if (summaries == null) {
            return;
        }

        for (S3Object summary : summaries) {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(outputBucket)
                    .key(summary.key())
                    .build();

            s3Client.deleteObject(request);
        }
    }
    public static void purgeObject(String bucketName, String objectKey) {
        System.out.println("object to be deleted : " + bucketName+"/"+objectKey);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                                            .bucket(bucketName)
                                            .key(objectKey)
                                            .build();

        s3Client.deleteObject(request);
    }

    public static void waitUntilObjectReady(String bucketName, String prefix, String objectKey, Date timeToUpload) throws InterruptedException {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            HeadObjectResponse headResponse = s3Client.headObject(headRequest);

            long lastModified = headResponse.lastModified().toEpochMilli();
            long contentLength = headResponse.contentLength();

            if (lastModified >= timeToUpload.getTime() && contentLength > 0) {
                return;
            }

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                Thread.sleep(3000);
                waitUntilObjectReady(bucketName, prefix, objectKey, timeToUpload);
            } else {
                logger.info("S3Exception: " + e.awsErrorDetails().errorMessage());
            }
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
        }
    }

    public static void emptyTargetFolder(String month, String date, String timing) {

        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
        String prefix = AppProperties.getConfig().getProperty("script_prefix")+month+"/"+date+"/" + StringUtils.lowerCase(timing) + "/";
        purgeBucket( bucketName, prefix );

        bucketName = AppProperties.getConfig().getProperty("output_bucket");
        prefix = AppProperties.getConfig().getProperty("output_prefix")+month+"/"+date+"/" + StringUtils.lowerCase(timing) + "/";
        purgeBucket( bucketName, prefix );

        bucketName = AppProperties.getConfig().getProperty("audio_merged_bucket");
        prefix = AppProperties.getConfig().getProperty("audio_merged_prefix")+month+"/"+date+"/" + StringUtils.lowerCase(timing) + "/";
        purgeBucket( bucketName, prefix );
    }

    private static String genDatePath (String date, int count){
        if( count == 0 ){
            return date;
        }

        return date+"-"+count;
    }
    public static void emptyTargetFolder(String month, String date, int count) {

        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
        String prefix = AppProperties.getConfig().getProperty("script_prefix")+month+"/"+genDatePath(date, count)+"/";
        purgeBucket( bucketName, prefix );

        bucketName = AppProperties.getConfig().getProperty("output_bucket");
        prefix = AppProperties.getConfig().getProperty("output_prefix")+month+"/"+genDatePath(date, count)+"/";
        purgeBucket( bucketName, prefix );

        bucketName = AppProperties.getConfig().getProperty("audio_merged_bucket");
        prefix = AppProperties.getConfig().getProperty("audio_merged_prefix")+month+"/"+genDatePath(date, count)+"/";
        purgeBucket( bucketName, prefix );
    }
}
