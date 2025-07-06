import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BibleTest {

    //https://www.wwbible.org/%E5%92%8C%E5%90%88%E6%9C%AC%E7%AE%80%E4%BD%93
    public static void main(String[] args) throws IOException {
        String content = readFromInternet("https://www.wwbible.org/%E5%92%8C%E5%90%88%E6%9C%AC%E7%AE%80%E4%BD%93");
        String regex = "<li class=\"book-group-item\".*?</li>";

        // Compile and match regex
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        // Print all matching tags
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    private static String readFromInternet(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
//        System.out.println("Response code: " + responseCode);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String html = response.toString();
//        System.out.println(html);

        return html;
    }
}
