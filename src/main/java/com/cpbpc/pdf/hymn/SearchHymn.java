package com.cpbpc.pdf.hymn;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;

import java.util.List;
import java.util.stream.Collectors;
public class SearchHymn {
    public static void main(String[] args) {
        String bucketName = "openhymnal";
        String prefix = "1_";
        String suffix = ".jpg";

        // Create an Amazon S3 client
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_1)   // Set your desired region
                .withCredentials(new ProfileCredentialsProvider())  // Uses AWS credentials from your AWS CLI profile
                .build();

        try {
            // Request to list objects
            ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                    .withBucketName(bucketName);

            // List objects
            ListObjectsV2Result result = s3Client.listObjectsV2(listObjectsRequest);

            // Get the list of object keys
            List<String> allKeys = result.getObjectSummaries()
                    .stream()
                    .map(s -> s.getKey())
                    .collect(Collectors.toList());

            // Filter keys based on partial match
            List<String> matchingKeys = allKeys.stream()
                    .filter(key -> key.startsWith(prefix) && key.endsWith(suffix))
                    .collect(Collectors.toList());

            // Print the matching keys
            if (matchingKeys.isEmpty()) {
                System.out.println("No objects found with the specified criteria.");
            } else {
                System.out.println("Matching object keys:");
                matchingKeys.forEach(System.out::println);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
