package com.cpbpc.stt;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobResponse;
import software.amazon.awssdk.services.transcribe.model.LanguageCode;
import software.amazon.awssdk.services.transcribe.model.Media;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobResponse;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus;

import java.util.UUID;

public class TranscribeTest {

    public static void main(String[] args) throws InterruptedException {
        // Specify the AWS region
        Region region = Region.AP_SOUTHEAST_1; // Replace with your desired region

        // Set up the Transcribe client
        TranscribeClient transcribeClient = TranscribeClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        // Specify the transcription job parameters
        String transcriptionJobName = UUID.randomUUID().toString();
        String languageCode = LanguageCode.EN_US.toString(); // Replace with the desired language code
        String mediaFileUri = "s3://cpbpctts/devotion/January/1_morning.mp3"; // Replace with the S3 URI of your audio file
        String outputBucketName = "cpbpc-stt"; // Replace with the desired S3 bucket for the transcription output

        // Build the StartTranscriptionJobRequest
        StartTranscriptionJobRequest transcriptionJobRequest = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(transcriptionJobName)
                .languageCode(languageCode)
                .media(Media.builder().mediaFileUri(mediaFileUri).build())
                .outputBucketName(outputBucketName)
                .outputKey("george.json")
                .build();

        // Start the transcription job
        StartTranscriptionJobResponse response = transcribeClient.startTranscriptionJob(transcriptionJobRequest);

        System.out.println("Transcription Job Name: " + response.transcriptionJob().transcriptionJobName());
        System.out.println("Transcription Job Status: " + response.transcriptionJob().transcriptionJobStatus());

        boolean isDone = false;
        while( !isDone ){
            GetTranscriptionJobRequest getTranscriptionJobRequest = GetTranscriptionJobRequest.builder()
                    .transcriptionJobName(transcriptionJobName)
                    .build();
            GetTranscriptionJobResponse getTranscriptionJobResponse = transcribeClient.getTranscriptionJob(getTranscriptionJobRequest);
            TranscriptionJobStatus status = getTranscriptionJobResponse.transcriptionJob().transcriptionJobStatus();
            if( TranscriptionJobStatus.COMPLETED.equals(status) ){
                isDone=true;
                continue;
            }
            System.out.println("Transcription Job Status: " + status);
            Thread.sleep(3000);
        }

        // Close the Transcribe client
        transcribeClient.close();
    }

}
