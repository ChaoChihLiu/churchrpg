package com.cpbpc.gmail;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class ReadEmails {
    public static void main(String[] args) throws Exception {
        String host = "imap.gmail.com";
        final String username = "calvarypandan.it@gmail.com";
        final String password = "qbzfbktlcgegaxgn";

        // Set up properties for the mail session.
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", host);
        properties.put("mail.imaps.port", "993");
        properties.put("mail.imaps.ssl.enable", "true");

        // Create a mail session and connect to the server.
        Session session = Session.getInstance(properties);
        Store store = session.getStore("imaps");
        store.connect(host, username, password);

        // Open the inbox folder.
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        // Get the messages in the inbox folder.
        Message[] messages = inbox.getMessages();
        for (Message message : messages) {
            String subject = message.getSubject();
            Address[] from = message.getFrom();
            String body = getTextFromMessage(message);

            // Process the email data as needed.
            System.out.println("Subject: " + subject);
            System.out.println("From: " + from[0]);
            System.out.println("Body: " + body);
        }

        // Close the folder and store.
        inbox.close(false);
        store.close();
    }

    // Helper method to extract the text content from a message.
    private static String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("multipart/*")) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }
}

