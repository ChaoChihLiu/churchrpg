package com.cpbpc.pdf.hymn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicXMLFilter {

    public static void main(String[] args) {
        String mxlFilePath = "/Users/liuchaochih/Documents/GitHub/cpbpc-convert-hymn/cpbpc-hymn/15_Praise_to_the_Lord,_The_Almighty/page_25.musicxml";
        String filteredMxlFilePath = filterMeasures(mxlFilePath);
        System.out.println("Filtered MusicXML file created at: " + filteredMxlFilePath);
    }

    public static String filterMeasures(String mxlFilePath) {
        StringBuilder xmlContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(mxlFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                xmlContent.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Error reading the MusicXML file: " + e.getMessage());
            return null;
        }

        String xmlString = xmlContent.toString();

        // Regex pattern to find measures with 'new-system="yes"'
        Pattern measurePattern = Pattern.compile("<measure[^>]*number=\"(\\d+)\"[^>]*>(.*?)<print[^>]*new-system=\"yes\"[^>]*>(.*?)</measure>", Pattern.DOTALL);
        Matcher matcher = measurePattern.matcher(xmlString);

        StringBuilder filteredXml = new StringBuilder();
        int measureCount = 0;

        // Replace every second measure with new-system="yes"
        while (matcher.find()) {
            measureCount++;
            if (measureCount % 2 == 0) {
                // Skip this measure by replacing it with an empty string
                continue;
            }
            // Append the matched measure to the filtered output
            filteredXml.append(matcher.group()).append(System.lineSeparator());
        }

        // Handle remaining measures that do not have new-system="yes"
        String remainingMeasures = xmlString.replaceAll(measurePattern.pattern(), "");
        filteredXml.append(remainingMeasures);

        // Write the filtered content to a new MusicXML file
        String filteredFilePath = mxlFilePath.replace(".musicxml", "_filtered.musicxml");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filteredFilePath))) {
            bw.write(filteredXml.toString());
        } catch (IOException e) {
            System.err.println("Error writing the filtered MusicXML file: " + e.getMessage());
            return null;
        }

        return filteredFilePath;
    }
}
