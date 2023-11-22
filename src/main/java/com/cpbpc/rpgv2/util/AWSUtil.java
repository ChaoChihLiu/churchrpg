package com.cpbpc.rpgv2.util;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.util.StringInputStream;
import com.cpbpc.rpgv2.AppProperties;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static com.amazonaws.regions.Regions.AP_SOUTHEAST_1;

public class AWSUtil {

    private static final AmazonS3 s3Client = AmazonS3Client.builder().withRegion(AP_SOUTHEAST_1)
            .withCredentials(new DefaultAWSCredentialsProviderChain())
            .build();
//    private static final AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretKey);
//    private static final AmazonS3 s3Client = AmazonS3Client.builder().withRegion(AP_SOUTHEAST_1)
//        .credentialsProvider(() -> awsCredentials)
//        .build();

    public static void putScriptToS3(String content, String publishDate_str) throws UnsupportedEncodingException {

        String bucketName = AppProperties.getProperties().getProperty("script_bucket");
        String prefix = AppProperties.getProperties().getProperty("script_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        String publishMonth = publishDate_str.split("-")[0] + "_" + publishDate_str.split("-")[1];
        String nameToBe = AppProperties.getProperties().getProperty("name_prefix") + publishDate_str.replaceAll("-", "");
        String objectType = AppProperties.getProperties().getProperty("script_format");
        String objectKey = prefix + publishMonth + "/" + nameToBe + "." + objectType;
        String audioKey =  AppProperties.getProperties().getProperty("output_prefix")
                            + publishMonth + "/"
                            + nameToBe + "."
                            + AppProperties.getProperties().getProperty("output_format");

        try {
            InputStream inputStream = new StringInputStream(content);
            // Upload the file to S3
            ObjectMetadata metadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);

            List<Tag> tags = new ArrayList<>();
            tags.add(new Tag("publish_date", publishDate_str));
            tags.add(new Tag("voice_id", AppProperties.getProperties().getProperty("voice_id")));
            tags.add(new Tag("category", URLDecoder.decode(AppProperties.getProperties().getProperty("content_category"))));
            tags.add(new Tag("audio_key", audioKey));
            tags.add(new Tag("name_prefix", AppProperties.getProperties().getProperty("name_prefix")));
            tags.add(new Tag("output_bucket", AppProperties.getProperties().getProperty("output_bucket")));
            tags.add(new Tag("output_prefix", AppProperties.getProperties().getProperty("output_prefix")));

            putObjectRequest.setStorageClass(StorageClass.IntelligentTiering);
            putObjectRequest.setTagging(new ObjectTagging(tags));

            s3Client.putObject(putObjectRequest);
        } catch (Exception e) {
            throw e;
        }
    }

}
