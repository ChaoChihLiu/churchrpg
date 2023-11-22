package com.cpbpc.rpgv2;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cpbpc.rpgv2.util.ThreadStorage;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class RPGTrigger implements RequestHandler {

    private static final Properties appProperties = AppProperties.getProperties();
    //    private static AWSLambda lambda = null;
    private static final String SEARCH_CONTENT_BY_ID = " select DATE_FORMAT(cjr.startrepeat , \"%Y-%m-%d\") as startrepeat, cc.alias, cjv.* " +
            " from cpbpc_jevents_vevdetail cjv " +
            " left join cpbpc_jevents_vevent cj on cj.ev_id = cjv.evdet_id  " +
            " left join cpbpc_categories cc on cc.id = cj.catid " +
            " left join cpbpc_jevents_repetition cjr on cjr.eventdetail_id  = cjv.evdet_id  " +
            " where ";
    private static final SimpleDateFormat CURRENT_MONTH = new SimpleDateFormat("yyyy-MM");

    private static int TIME_OUT = 5 * 60 * 1000;

    static {
        AppProperties.resetTotalLength();
    }

    private Logger logger = Logger.getLogger(RPGTrigger.class.getName());

    public static void main(String[] args) {

//        System.setProperty("app.properties", "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-english.properties");
//        System.setProperty("publish.date", "2023-12-01");
////        System.setProperty("publish.month", "");
//        System.setProperty("use.polly", "true");

        String propPath = System.getProperty("app.properties");
        FileInputStream in = null;
        try {
            in = new FileInputStream(propPath);
            appProperties.load(in);

            if (null != System.getProperty("publish.date") && System.getProperty("publish.date").trim().length() > 0) {
                appProperties.setProperty("publish.date", System.getProperty("publish.date"));
            }
            if (null != System.getProperty("publish.month") && System.getProperty("publish.month").trim().length() > 0) {
                appProperties.setProperty("publish.month", System.getProperty("publish.month"));
            }
            appProperties.setProperty("use.polly", "false");
            if (null != System.getProperty("use.polly") && Boolean.valueOf(System.getProperty("use.polly")) == true) {
                appProperties.setProperty("use.polly", System.getProperty("use.polly"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        RPGTrigger trigger = new RPGTrigger();
        trigger.handleRequest(null, null);

    }

    @Override
    public Boolean handleRequest(Object input, Context context) {

//        lambda = AWSLambdaClient.builder().withRegion(Regions.AP_SOUTHEAST_1).withClientConfiguration(createClientConfig()).build();

        Connection conn = null;
        try {

            conn = createConnection();

            if (null == conn) {
                logger.info("cannot create connection to db");
                return false;
            }
            initStorage(conn);

            int process_id = retrieveLastProcessId(conn);
            logger.info("processid  " + process_id);
            Long currentUsage = retrievePollyUsageCurrentMonth(conn);
            logger.info("currentUsage  " + currentUsage);
            if (null == currentUsage) {
                currentUsage = 0L;
            }
            if (currentUsage >= Long.parseLong(appProperties.getProperty("polly_limit"))) {
                logger.info("reached Polly Limit!");
                return false;
            }

            PreparedStatement state = null;
            logger.info(" last process id is " + process_id);
            String sql = SEARCH_CONTENT_BY_ID + " cc.title in ( '" + URLDecoder.decode(appProperties.getProperty("content_category")) + "' ) ";
            if (!appProperties.getOrDefault("publish.date", "0").equals("0")) {
                sql += " and DATE_FORMAT(cjr.startrepeat, \"%Y-%m-%d\")=? ";
            } else if (!appProperties.getOrDefault("publish.month", "0").equals("0")) {
                sql += " and DATE_FORMAT(cjr.startrepeat, \"%Y-%m\")=? ";
            } else {
                sql += " and cjv.evdet_id>? ";
            }
            logger.info(" sql: " + sql);
            state = conn.prepareStatement(sql);
            if (!appProperties.getOrDefault("publish.date", "0").equals("0")) {
                state.setString(1, appProperties.getProperty("publish.date"));
            } else if (!appProperties.getOrDefault("publish.month", "0").equals("0")) {
                state.setString(1, appProperties.getProperty("publish.month"));
            } else {
                state.setInt(1, process_id);
                logger.info(" state: " + state.toString());
            }

            ResultSet rs = state.executeQuery();
            int current_process_id = 0;

            String previousDate = "";
            int count = 0;
            while (rs.next()) {
                logger.info(rs.getString("startrepeat"));
                if (previousDate.equals(rs.getString("startrepeat"))) {
                    count++;
                } else {
                    count = 0;
                }
                previousDate = rs.getString("startrepeat");

                convertAudio(new Article(rs.getString("startrepeat"),
                                rs.getString("description"),
                                rs.getString("summary"),
                                rs.getString("alias"),
                                count),
                        currentUsage);
                current_process_id = rs.getInt("evdet_id");
            }

            if ((appProperties.getOrDefault("publish.date", "0").equals("0")
                    || appProperties.getOrDefault("publish.month", "0").equals("0"))
                    && current_process_id > 0
                    && Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "true"))) {
                updateProcessId(current_process_id, conn);
            }
            if (Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "true"))) {
                updatePollyUsageCurrentMonth(AppProperties.getTotalLength(), conn);
            }

            return true;
        } catch (SQLException | InterruptedException | UnsupportedEncodingException | ExecutionException e) {
            logger.info(e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException throwables) {

            }
            return true;
        }

    }

    private void updatePollyUsageCurrentMonth(long totalLength, Connection conn) throws SQLException {

        Long currentUsage = retrievePollyUsageCurrentMonth(conn);
        PreparedStatement state = null;
        if (null == currentUsage) {
            state = conn.prepareStatement("insert into cpbpc_system_config (`key`, value) value (?, ?) ");
            state.setString(1, currentMonth() + "_polly_limit");
            state.setLong(2, totalLength);
        } else {
            state = conn.prepareStatement("update cpbpc_system_config set value=" + (currentUsage + totalLength) + " where `key`='" + currentMonth() + "_polly_limit'");
        }
        state.executeUpdate();
    }

    private String currentMonth() {
        return CURRENT_MONTH.format(new java.util.Date());
    }

    private Long retrievePollyUsageCurrentMonth(Connection conn) throws SQLException {

        PreparedStatement state = conn.prepareStatement("select * from cpbpc_system_config where `key`='" + currentMonth() + "_polly_limit'");
        ResultSet rs = state.executeQuery();

        while (rs.next()) {
            return Long.parseLong(rs.getString("value"));
        }

        return null;

    }

    private void initStorage(Connection conn) throws SQLException {
        AbbreIntf abbr = ThreadStorage.getAbbreviation();
        PhoneticIntf phonetic = ThreadStorage.getPhonetics();
        VerseIntf verse = ThreadStorage.getVerse();

        PreparedStatement state = conn.prepareStatement("select * from cpbpc_abbreviation order by seq_no asc, length(short_form) desc");
        ResultSet rs = state.executeQuery();

        while (rs.next()) {
            String group = rs.getString("group");
            String shortForm = rs.getString("short_form");
            String completeForm = rs.getString("complete_form");
            String isPaused = rs.getString("is_paused");

            if ("bible".toLowerCase().equals(group.toLowerCase())) {
                verse.put(shortForm, completeForm, (isPaused.equals("1")) ? true : false);
            } else if ("pronunciation".toLowerCase().equals(group.toLowerCase())) {
                phonetic.put(shortForm, completeForm, (isPaused.equals("1")) ? true : false);
            } else {
                abbr.put(shortForm, completeForm, (isPaused.equals("1")) ? true : false);
            }

        }

    }

    private ClientConfiguration createClientConfig() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSocketTimeout(TIME_OUT);
        clientConfiguration.setClientExecutionTimeout(TIME_OUT);
        clientConfiguration.setConnectionTimeout(TIME_OUT);
        clientConfiguration.setRequestTimeout(TIME_OUT);

        return clientConfiguration;
    }

    private void updateProcessId(int current_process_id, Connection conn) {

        try {
            PreparedStatement state = conn.prepareStatement("update cpbpc_system_config set value=? where `key`=?");
            state.setInt(1, current_process_id);
            state.setString(2, "rpg_process_id");
            logger.info("current_process_id is " + current_process_id);
            state.executeUpdate();
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }

    }

    private void convertAudio(Article data,
                              long currentUsage)
            throws UnsupportedEncodingException, ExecutionException, InterruptedException {

        try {
            RPGToAudio worker = new RPGToAudio(currentUsage, Long.parseLong(appProperties.getProperty("polly_limit")));
            worker.handleRequest(data);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        }
    }

    private int retrieveLastProcessId(Connection conn) throws SQLException {

        PreparedStatement state = conn.prepareStatement("select * from cpbpc_system_config where `key`=?");
        state.setString(1, "rpg_process_id");
        ResultSet rs = state.executeQuery();
        if (rs.next()) {
            return Integer.parseInt(rs.getString("value"));
        }

        return 0;
    }

    private Connection createConnection() {

        try {
            Connection conn = DriverManager.getConnection(appProperties.getProperty("db_url"),
                    appProperties.getProperty("db_username"),
                    appProperties.getProperty("db_password"));

            return conn;
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        return null;

    }

}
