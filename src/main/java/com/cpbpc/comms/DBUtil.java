package com.cpbpc.comms;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    public static Connection createConnection(Properties appProperties) throws SQLException {

        try {
            Connection conn = DriverManager.getConnection(appProperties.getProperty("db_url"),
                    appProperties.getProperty("db_username"),
                    appProperties.getProperty("db_password"));

            return conn;
        } catch (Exception e) {
            throw e;
        }
    }
    public static void initStorage(Properties appProperties) throws SQLException {
        initStorage(createConnection(appProperties));
    }

    private static void initStorage(Connection conn) throws SQLException {
        AbbreIntf abbr = ThreadStorage.getAbbreviation();
        PhoneticIntf phonetic = ThreadStorage.getPhonetics();
        VerseIntf verse = ThreadStorage.getVerse();

        PreparedStatement state = conn.prepareStatement("select * from cpbpc_abbreviation where isEnabled=1 and cloud_sys=? order by seq_no desc, length(short_form) desc");
        if( AppProperties.getConfig().containsKey("cloud_sys")
                && !StringUtils.isEmpty(AppProperties.getConfig().getProperty("cloud_sys")) ){
            state.setString(1, AppProperties.getConfig().getProperty("cloud_sys"));
        }else{
            state.setString(1, "aws");
        }

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

}
