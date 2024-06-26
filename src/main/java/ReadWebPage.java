import com.cpbpc.comms.AppProperties;

import java.util.Properties;

public class ReadWebPage {

    public static void main(String[] args) {

        Properties properties = AppProperties.readEDZXBibleMapping();


//        String url = "http://www.edzx.com/bible/read/?id=1&volume=66&chapter=1"; // Replace with the URL of the webpage you want to read
//
//        try {
//            // Create a URL object
//            URL webpage = new URL(url);
//
//            // Open a connection to the URL
//            URLConnection connection = webpage.openConnection();
//
//            // Create a BufferedReader to read the content
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//
//            // Read and print each line of the webpage content
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//
//            // Close the BufferedReader
//            reader.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
