import com.amazonaws.util.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogParser {

    private void show() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("/Users/liuchaochih/Downloads/performance-2023-11-15-10-14-00.log")));
        String line = null;

        String regex = "\\s+pcpsportaldb\\s+(\\w+)\\s+(\\d+)\\s+(.*)";
        Pattern pattern = Pattern.compile(regex);

//        List<String> type = new ArrayList<>();
        Map<String, Long> temp = new HashMap<>();
        List<SqlTime> result = new ArrayList<>();
        while( (line = reader.readLine()) != null ){
            Matcher matcher = pattern.matcher(line);
            if( matcher.find() ){
//                System.out.println(line);
                if(!StringUtils.isEmpty(matcher.group(3))
                        && !StringUtils.startsWith(matcher.group(3), "NULL")
                        && !StringUtils.contains(matcher.group(3), "show full processlist") ){
                    //System.out.println(matcher.group(2) + " : " +  matcher.group(3));

                    temp.put(matcher.group(3), Long.valueOf(matcher.group(2)));
                }
            }
        }

        Set<Map.Entry<String, Long>> entries = temp.entrySet();
        for( Map.Entry<String, Long> entry: entries ){
            SqlTime sqltime = new SqlTime(entry.getKey(), entry.getValue());
            result.add(sqltime);
        }

        Collections.sort(result, Comparator.comparingLong(SqlTime::getTime));

        System.out.println("time"+"\t"+"sql");
        for( SqlTime entry : result ){
            System.out.println(entry);
//            System.out.println("sql: " + StringUtils.truncate(entry.getKey(), 10));
        }

    }


    public static void main(String args[]) throws IOException {
        LogParser parser = new LogParser();
        parser.show();
    }

}

class SqlTime{

    public SqlTime(String sql, Long time){
        this.sql = sql;
        this.time = time;
    }

    private String sql;
    private Long time;

    public String getSql(){
        return sql;
    }
    public Long getTime(){
        return time;
    }

    public String toString(){
        return time + "\t" + sql;
    }

}
