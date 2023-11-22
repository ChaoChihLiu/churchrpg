package com.cpbpc.gmail;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

public class JakartaEmail {
    public static void main(String[] args) {
        //provide recipient's email ID
        String to = "chaochihliu@gmail.com";
        //provide sender's email ID
        String from = "calvarypandan.it@gmail.com";
        //provide Mailtrap's username
        final String username = "calvarypandan.it@gmail.com";
        //provide Mailtrap's password
//        final String password = "crgnerqlcguovsyh";
        final String password = "qbzfbktlcgegaxgn";
        //provide Mailtrap's host address
        String host = "smtp.gmail.com";
        //configure Mailtrap's SMTP server details
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");
        //create the Session object
        Authenticator authenticator = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        Session session = Session.getInstance(props, authenticator);
        try {
            //create a MimeMessage object
            Message message = new MimeMessage(session);
            //set From email field
            message.setFrom(new InternetAddress(from));
            //set To email field
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            //set email subject field
            message.setSubject("Welcome to worship service on Lord's day");
            //set the content of the email message
            message.setContent(readTemplate(), "text/html");
            //send the email message
            Transport.send(message);
            System.out.println("Email Message Sent Successfully");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readTemplate() throws IOException {

//        URL resource = JakartaEmail.class.getResource("mailtemplate.html");
//        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
//        InputStream is = classloader.getResourceAsStream("mailtemplate.html");
//        return IOUtils.resourceToString("main/mailtemplate.html", StandardCharsets.UTF_8, JakartaEmail.class.getClassLoader());
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("attendancelink.html");
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);
        return writer.toString();
    }
}