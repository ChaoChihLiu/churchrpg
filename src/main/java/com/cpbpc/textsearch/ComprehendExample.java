package com.cpbpc.textsearch;


import com.cpbpc.comms.TextUtil;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectKeyPhrasesRequest;
import software.amazon.awssdk.services.comprehend.model.DetectKeyPhrasesResponse;
import software.amazon.awssdk.services.comprehend.model.KeyPhrase;

import java.util.List;

public class ComprehendExample {
    public static void main(String[] args) {
        // Set the region and create a credentials provider
        Region region = Region.US_EAST_1; // Change as needed
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();

        // Create Comprehend client
        ComprehendClient comprehendClient = ComprehendClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        // Sample text
        String text = TextUtil.removeHtmlTag(getText());

        // Create request
        DetectKeyPhrasesRequest request = DetectKeyPhrasesRequest.builder()
                .text(text)
                .languageCode("en")
                .build();

        // Call comprehend to detect key phrases
        DetectKeyPhrasesResponse response = comprehendClient.detectKeyPhrases(request);

        // Get and print key phrases
        List<KeyPhrase> keyPhrases = response.keyPhrases();
        for (KeyPhrase keyPhrase : keyPhrases) {
            System.out.println("Key Phrase: " + keyPhrase.text() + ", Score: " + keyPhrase.score());
        }

        // Close the client
        comprehendClient.close();
    }

    private static String getText() {

        return "<div><span style=\"font-size: 12pt;\"><em>MONDAY, JULY 6</em></span></div>\n" +
                "<div><span style=\"font-size: 12pt;\"><strong>Acts 1:5</strong></span></div>\n" +
                "<p><span style=\"font-size: 12pt;\">John 14:16-18</span></p>\n" +
                "<p>&nbsp;</p>\n" +
                "<div><span style=\"font-size: 12pt;\"><em>“…for he dwelleth with you,</em></span></div>\n" +
                "<p><span style=\"font-size: 12pt;\"><em>and shall be in you.”</em></span></p>\n" +
                "<p>&nbsp;</p>\n" +
                "<div><span style=\"font-size: 12pt;\"><strong>PROMISED BAPTISM</strong></span></div>\n" +
                "<div>&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\">Many Christians today seek for a “second” baptism, a post-conversion&nbsp;experience to confirm that they are truly born-again believers. They say,&nbsp;“I was baptised by the Holy Spirit and after a series of tutoring I was able&nbsp;to speak in tongues!” However, the book of Acts makes no mention of&nbsp;the baptism of the Holy Spirit, for the Spirit of God does not baptise but&nbsp;Christ.</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\"><span style=\"text-decoration: underline;\">Baptism with water</span>: John the Baptist’s main mission was to announce the&nbsp;coming of the Messiah. In John 1:29, he pointed to Jesus as the “<em>Lamb&nbsp;of God, which taketh away the sin of the world.</em>” His was the baptism of&nbsp;repentance for the remission of sins.</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\"><span style=\"text-decoration: underline;\">Baptism with the Holy Spirit</span>: The promise was for the disciples to be&nbsp;baptised with the Holy Ghost (Acts 1:5) which is described as the Holy&nbsp;Spirit coming upon the believer. It was further clarified in Acts 2:4 as a&nbsp;filling with the Holy Spirit.</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\">The disciples of the Lord Jesus Christ were already indwelt with the Spirit&nbsp;since they were already believers. Although Acts 1:5 appears to have the&nbsp;idea of a second baptism or a second blessing, the baptism mentioned&nbsp;among the disciples of Christ was about the filling of the Holy Spirit. Acts&nbsp;2:4 tells us that they were “<em>filled</em>” with the Holy Spirit. “This baptismal&nbsp;filling of the Spirit was not for salvation, but for service” (Khoo). The&nbsp;purpose of Jesus Christ’s promise to send the Holy Spirit was to instruct&nbsp;them in the doctrines they were to proclaim to the people and that they&nbsp;might be strengthened to withstand the conflicts which they would face.&nbsp;It is not a second baptism nor the gibberish and ecstatic utterances of&nbsp;the so-called “tongue-speaking” of today’s Charismatics. Rather, it is the&nbsp;Holy Spirit who would enable Christians to witness both near and far.</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\">Have you truly believed in the Lord Jesus Christ? Are you filled with&nbsp;the Spirit of God? Are you able to speak forth the gospel of Jesus&nbsp;Christ to others?</span></div>\n" +
                "<div style=\"text-align: justify;\">&nbsp;</div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\"><strong>THOUGHT:</strong> Am I baptised with the Holy Ghost?</span></div>\n" +
                "<div style=\"text-align: justify;\"><span style=\"font-size: 12pt;\"><strong>PRAYER:</strong> Father, please fill me with Thy Holy Spirit and let Him abide&nbsp;with me forever.</span></div>";

    }
}

