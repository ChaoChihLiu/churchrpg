package com.cpbpc.telegram;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

public class TelegramBot {
    private Logger logger = Logger.getLogger(TelegramBot.class.getName());

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        TelegramBot bot = new TelegramBot();
        GoogleCalendar googleCalendar = new GoogleCalendar();
//        bot.createRPGMsg("<strong>test html code here:</strong><a href='https://calvarypandan.sg/resources/rpg/calendar/eventdetail/68932/86/gideon-iv'><strong>Adult RPG</strong></a>");
//        bot.createRPGMsg("<b>TEST HTML</b>");
        bot.createRPGMsg(googleCalendar.getContent());
    }

    public void createRPGMsg(String input) {

        if (null == input || input.trim().length() <= 0) {
            logger.info("There is no input");
            return;
        }

        input = input.replaceAll("</p>", System.lineSeparator()).replaceAll("<p>", "");
        input = input.replaceAll("<[^>]*>", "");

        logger.info("after " + input);
        input = URLEncoder.encode("Red, Pray and Grow for Adults", StandardCharsets.UTF_8)
                + input
                + URLEncoder.encode("<a href='http://bit.ly/3Dtpbo9'>\uD83D\uDCDDhttp://bit.ly/3Dtpbo9</a>", StandardCharsets.UTF_8);
        String urlParameters = "chat_id=%s&parse_mode=html&text=%s";
        String chatId = "-892643296"; //RPG Test
//        String chatId = "-1001789969129"; //myTest
        String postMsg = String.format(urlParameters, chatId, input);

        byte[] postData = postMsg.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        logger.info("data length: " + postDataLength);
        String request = "https://api.telegram.org/bot5779840317:AAE84-drclh_Utfa4QCqfm07i0egvBP0P1M/sendMessage";
        try {
            URL url = new URL(request);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
                wr.flush();

                System.out.println(conn.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
