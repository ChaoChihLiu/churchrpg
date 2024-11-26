package com.cpbpc.errorfix;

import com.amazonaws.internal.ExceptionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PronunciationFix {
    private static Logger logger = Logger.getLogger(PronunciationFix.class.getName());

    private static String csvfile = "test.csv";
//    private static String folder_path = "/Users/liuchaochih/Downloads/";
//    private static String folder = "kjv";
//        private static String folder = "cpbpc-tts-script/kjv";
    private static String folder = "";
    private static String folder_path = "/sftpusers/chroot/coder/";

    private static Map<String, String> fixes = new HashMap<>();
    private static Pattern phonemePattern = null;
    private static Pattern wordPattern = null;

    public static void main(String[] args) {
        String filename = csvfile;
        String target = folder_path + folder;
        if (args.length >= 1) {
            filename = args[0];
        }
        if (args.length >= 2) {
            target = folder_path + args[1];
        }

        // Read the CSV file
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2); // Split into key-value pairs
                if (parts.length == 2) {
                    fixes.put(parts[0].trim(), parts[1].trim());
                }
            }
            String phoneme_string = genPhonemePattern(fixes);
            String word_string = genWordPattern(fixes);
            phonemePattern = Pattern.compile(phoneme_string);
            wordPattern = Pattern.compile(word_string);
        } catch (IOException e) {
            logger.info(ExceptionUtils.exceptionStackTrace(e));
        }

        try{
            // Process all XML files in the target folder and subfolders
            File targetFolder = new File(target);
            if (targetFolder.exists() && targetFolder.isDirectory()) {
                processFolder(targetFolder);
            } else {
                System.err.println("Target folder does not exist or is not a directory: " + target);
            }
        } catch (IOException | InterruptedException e) {
            logger.info(ExceptionUtils.exceptionStackTrace(e));
        }
    }

    private static void processFolder(File folder) throws IOException, InterruptedException {
        // List all files and directories
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursive call for subfolders
                    processFolder(file);
                } else if (file.isFile() && file.getName().endsWith(".xml")) {
                    // Process XML file
                    processXmlFile(file);
                }
            }
        }
    }

    private static void processXmlFile(File xmlFile) throws IOException, InterruptedException {
        String original_transcript = readContent(xmlFile);
        String transcript = fixPhoneme(original_transcript, fixes, phonemePattern);
        transcript = fixPronunciation(transcript, fixes, wordPattern, false);

        if( StringUtils.equals(original_transcript, transcript) ){
            return;
        }

//        logger.info("Processed and saved: " + xmlFile.getAbsolutePath());
        // Save the modified transcript back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFile))) {
            writer.write(transcript);
            logger.info("Processed and saved: " + xmlFile.getAbsolutePath());
            Thread.sleep(1000);
        } catch (IOException | InterruptedException e) {
            logger.severe("Error saving file: " + xmlFile.getAbsolutePath());
            throw e;
        }
    }

    private static <FileInputStream> String readContent(File xmlFile) {

        Path path = Paths.get(xmlFile.getAbsolutePath());
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(path);
            String fileContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            return fileContent;
        } catch (IOException e) {
            logger.info(ExceptionUtils.exceptionStackTrace(e));
        }finally{
            IOUtils.closeQuietly(inputStream);
        }

        return "";
    }

    private static String genPhonemePattern(Map<String, String> fixes){
        if( fixes.isEmpty() ){
            return "";
        }
        //<phoneme\s+alphabet=\"ipa\"\s+ph=\"([^\"]+)\">(sin|george)</phoneme>
        StringBuilder builder = new StringBuilder("<phoneme\\s+alphabet=[\\\"|\\']ipa[\\\"|\\']\\s+ph=[\\\"|\\']([^\\\"|^\\']+)[\\\"|\\']>(");
        Set<String> keys = fixes.keySet();
        for( String key : keys ){
            builder.append(StringUtils.trim(key)).append("|");
        }

        if( StringUtils.endsWith(builder, "|") ){
            builder = new StringBuilder(builder.substring(0, builder.toString().length()-1));
        }

        builder.append(")</phoneme>");

        return builder.toString();
    }

    private static String genWordPattern(Map<String, String> fixes){
        if( fixes.isEmpty() ){
            return "";
        }
        //String regex = "(?<!<phoneme[^>]\\*>)\\b(Succoth|Etham)\\b(?!<\\/phoneme>)";
        StringBuilder builder = new StringBuilder("(?<!<phoneme[^>]\\*>)\\b(");
        Set<String> keys = fixes.keySet();
        for( String key : keys ){
            builder.append(StringUtils.trim(key)).append("|");
        }

        if( StringUtils.endsWith(builder, "|") ){
            builder = new StringBuilder(builder.substring(0, builder.toString().length()-1));
        }

        builder.append(")\\b(?!<\\/phoneme>)");

        return builder.toString();
    }

    private static String fixPronunciation(String transcript, Map<String, String> fixes, Pattern pattern, boolean isEnglish) {
        String result = transcript;

        Matcher matcher = pattern.matcher(transcript);

        Map<String, String> beReplaced = new HashMap<>();
        while( matcher.find() ){
            String word = matcher.group();
            logger.info("fixPronunciation word: " + word);

            beReplaced.put(word, fixes.get(word));
        }

        Set<Map.Entry<String, String>> entries = beReplaced.entrySet();
        for(Map.Entry<String, String> entry : entries){
            result = result.replaceAll(entry.getKey(), generatePronunciationReplacement(entry.getKey(), entry.getValue(), isEnglish));
        }

        return result;
    }

    private static String generatePronunciationReplacement(String key, String value, boolean isEnglish) {

        if( isEnglish ){
            return "<phoneme alphabet=\"ipa\" ph=\""+value+"\">"+key+"</phoneme>";
        }
        return "<phoneme alphabet=\"sapi\" ph=\""+value+"\">"+key+"</phoneme>";
    }
    private static String fixPhoneme(String transcript, Map<String, String> fixes, Pattern pattern) {
        String result = transcript;
        
        Matcher matcher = pattern.matcher(transcript);

        Map<String, String> beReplaced = new HashMap<>();
        while( matcher.find() ){
            String ipa = matcher.group(1);
            String word = matcher.group(2);
            logger.info("fixPhoneme ipa: " + ipa);
            logger.info("fixPhoneme word: " + word);

            beReplaced.put(ipa, fixes.get(word));
        }

        Set<Map.Entry<String, String>> entries = beReplaced.entrySet();
        for(Map.Entry<String, String> entry : entries){
            result = result.replaceAll(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
