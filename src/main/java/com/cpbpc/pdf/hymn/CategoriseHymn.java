package com.cpbpc.pdf.hymn;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.DBUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoriseHymn {

    public static void main(String[] args) {
        AppProperties.loadConfig(System.getProperty("app.properties",
                "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-hymn.properties"));

        try{
//            updateScriptureIndex();
            updateTopicIndex();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void updateTopicIndex() throws IOException, SQLException {

        String content = IOUtils.toString(new FileReader("src/main/resources/hymn-topic-index.csv"));
        String[] lines = StringUtils.split(content, System.lineSeparator());
        String topic = "";
        Pattern pattern = Pattern.compile(".*\\s(\\d+)$");
        for ( String line: lines ){
            if( StringUtils.isEmpty(line) ){
                continue;
            }
            System.out.println("line : " + line);
            line = StringUtils.trim(line).replaceAll("\"", "");
            if( isTopic(line) ){
                topic = line;
                System.out.println("Topic: " + topic + " start");
                continue;
            }

            Matcher matcher = pattern.matcher(line);

            // Extract matches
            while (matcher.find()) {
                String hymnNum = matcher.group(1);

                saveTopicIndex(topic, hymnNum);
            }
        }

    }//end of updateTopicIndex

    private static void saveTopicIndex(String topic, String hymnNum) throws SQLException {
        Connection conn = DBUtil.createConnection(AppProperties.getConfig());

        try{
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO cpbpc_hymn_index " +
                    " (hymn_num, index_type, `index`) " +
                    " VALUES(?, ?, ?)" +
                    " ON DUPLICATE KEY UPDATE " +
                    " `index`= CONCAT(`index`, ',', ?) ");
            preparedStatement.setInt(1, Integer.parseInt(hymnNum));
            preparedStatement.setString(2, "Topic");
            preparedStatement.setString(3, topic);
            preparedStatement.setString(4, topic);

            preparedStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            conn.close();
        }
    }

    private static boolean isTopic(String line) {
        String regex = "^(?!.*\\d$).*";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(line);

        return matcher.find();
    }

    private static void updateScriptureIndex() throws IOException, SQLException {

        String content = IOUtils.toString(new FileReader("src/main/resources/hymn-scripture-index.csv"));
        String[] lines = StringUtils.split(content, System.lineSeparator());
        String book = "";
        Pattern pattern = Pattern.compile("(\\d+:\\d+(?:-\\d+)?|\\d+)\\s.*?(\\d+)$");
        for ( String line: lines ){
            if( StringUtils.isEmpty(line) ){
                continue;
            }
            System.out.println("line : " + line);
            line = StringUtils.trim(line).replaceAll("\"", "");
            if( isBook(line) ){
                book = convertBook(line);
                System.out.println("Book: " + book + " start");
                continue;
            }

            Matcher matcher = pattern.matcher(line);

            // Extract matches
            while (matcher.find()) {
                String ref = matcher.group(1);
                String hymnNum = matcher.group(2);

                saveScriptureIndex(book, ref, hymnNum);
            }
        }

    }//end of updateScriptureIndex

    private static String convertBook(String line) {
        String result = line;

        if( StringUtils.startsWith(line, "1")
                || StringUtils.startsWith(line, "2")
                || StringUtils.startsWith(line, "3")  ){
            result = StringUtils.replace(result, "1", "first".toUpperCase());
            result = StringUtils.replace(result, "2", "second".toUpperCase());
            result = StringUtils.replace(result, "3", "third".toUpperCase());
        }

        return result;
    }

    private static void saveScriptureIndex(String book, String ref, String hymnNum) throws SQLException {

        Connection conn = DBUtil.createConnection(AppProperties.getConfig());

        try{
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO cpbpc_hymn_index " +
                    " (hymn_num, index_type, `index`) " +
                    " VALUES(?, ?, ?)" +
                    " ON DUPLICATE KEY UPDATE " +
                    " `index`= CONCAT(`index`, ',', ?) ");
            preparedStatement.setInt(1, Integer.parseInt(hymnNum));
            preparedStatement.setString(2, "Scripture");
            preparedStatement.setString(3, book + " " +ref);
            preparedStatement.setString(4, book + " " +ref);

            preparedStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            conn.close();
        }
    }

    private static boolean isBook(String line) throws SQLException {
        Connection conn = DBUtil.createConnection(AppProperties.getConfig());

        try{
            PreparedStatement preparedStatement = conn.prepareStatement("select short_form from cpbpc_abbreviation " +
                                                                                " where `group`='bible' and short_form=?");
            preparedStatement.setString(1, line);
            ResultSet rs = preparedStatement.executeQuery();
            while( rs.next() ){
                if( !StringUtils.isEmpty(rs.getString("short_form")) ){
                    return true;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            conn.close();
        }

        return false;
    }

}//end of class
