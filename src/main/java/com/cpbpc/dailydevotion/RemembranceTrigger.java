package com.cpbpc.dailydevotion;

import com.amazonaws.internal.ExceptionUtils;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.Article;
import com.cpbpc.comms.DBUtil;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemembranceTrigger implements RequestHandler {

    private static final Properties appProperties = AppProperties.getConfig();
    //    private static AWSLambda lambda = null;
    private static final String SEARCH_CONTENT_BY_ID = " select cc.id, cc.alias, cjv.* " +
            "    from cpbpc_jevents_vevdetail cjv " +
            "    left join cpbpc_jevents_vevent cj on cj.ev_id = cjv.evdet_id " +
            "    left join cpbpc_categories cc on cc.id = cj.catid " +
            "    where cc.id = ? " +
            "    and cjv.summary like ? " +
            "    order by cjv.evdet_id asc "
            ;

    static {
        AppProperties.resetTotalLength();
    }

    private Logger logger = Logger.getLogger(RemembranceTrigger.class.getName());

    public static void main(String[] args) {

        if( AppProperties.isChinese() ){
            Locale.setDefault(new Locale("zh", "CN"));
        }

        AppProperties.loadConfig(System.getProperty("app.properties", "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-devotion.properties"));
        RemembranceTrigger trigger = new RemembranceTrigger();
        trigger.handleRequest(null, null);

    }

    @Override
    public Boolean handleRequest(Object input, Context context) {
        
        Connection conn = null;
        try {

            conn = DBUtil.createConnection(appProperties);

            if (null == conn) {
                logger.info("cannot create connection to db");
                return false;
            }
            DBUtil.initStorage(appProperties);
            
            PreparedStatement state = generateQueryStatement(conn, SEARCH_CONTENT_BY_ID);
            logger.info(" sql: " + state.toString());

            ResultSet rs = state.executeQuery();

            int count = 0;
            while (rs.next()) {
                convertAudio(new Article(getDate(rs.getString("summary")),
                                getTiming(rs.getString("summary")),
                                rs.getString("description"),
                                rs.getString("summary"),
                                rs.getString("alias"),
                                count));
            }

            return true;
        } catch (SQLException | InterruptedException | UnsupportedEncodingException | ExecutionException e) {
            logger.info(ExceptionUtils.exceptionStackTrace(e));
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

    private static final Pattern date_pattern = Pattern.compile("\\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{1,2})\\s+(Morning|Evening)\\b");
    private String getDate(String summary) {

        Matcher matcher = date_pattern.matcher(summary);
        if( matcher.find() ){
            String matched = matcher.group(0);
            String timing = matcher.group(2);

            return matched.replace(timing, "").trim();
        }

        return "";
    }

    private String getTiming(String summary) {
        Matcher matcher = date_pattern.matcher(summary);
        if( matcher.find() ){
            String timing = matcher.group(2);
            return timing.trim();
        }

        return "";
    }
    private PreparedStatement generateQueryStatement(Connection conn, String sql) throws SQLException {

        PreparedStatement state = conn.prepareStatement(sql);
        state.setInt(1, Integer.parseInt(appProperties.getProperty("content_category")));
        state.setString(2, appProperties.getProperty("month")+"%");
        return state;
    }
    
    private void convertAudio(Article data)
            throws UnsupportedEncodingException, ExecutionException, InterruptedException {

        try {
            RemembranceToAudio worker = new RemembranceToAudio();
            worker.handleRequest(data);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        }
    }
    
}
