package com.cpbpc.errorfix;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetractFix {

    private static final Pattern PHONEME_PATTERN = Pattern.compile(
            "<phoneme\\s+alphabet=[\"']ipa[\"']\\s+ph=[\"'][^\"']+[\"']>(.*?)</phoneme>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static String targetWord = null;

    public static void main(String[] args) throws IOException, InterruptedException {
        String folderPath = "/sftpusers/chroot/coder/"; // default
        if (args.length >= 1) {
            folderPath += args[0];
        }
        if (args.length >= 2) {
            targetWord = args[1];
        }

        File root = new File(folderPath);
        if (root.exists() && root.isDirectory()) {
            processFolder(root);
        } else {
            System.err.println("Folder not found: " + folderPath);
        }
    }

    private static void processFolder(File folder) throws IOException, InterruptedException {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                processFolder(file); // recursive
            } else if (file.isFile() && file.getName().endsWith(".xml")) {
                processXmlFile(file);
            }
        }
    }

    private static void processXmlFile(File xmlFile) throws IOException, InterruptedException {
        String originalContent = Files.readString(xmlFile.toPath(), StandardCharsets.UTF_8);

        Matcher matcher = PHONEME_PATTERN.matcher(originalContent);
        boolean changed = false;
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String word = matcher.group(1); // word inside tag
            if (targetWord == null || word.equalsIgnoreCase(targetWord)) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(word));
                changed = true;
            } else {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(buffer);

        if (changed) {
            System.out.println("==== File: " + xmlFile.getAbsolutePath() + " ====");
//            System.out.println(buffer.toString()); // Only print
            Files.writeString(xmlFile.toPath(), buffer.toString(), StandardCharsets.UTF_8);
            System.out.println("Recovered: " + xmlFile.getAbsolutePath());
            System.out.println("=============================================");
            Thread.sleep(1000);
        }
    }
    
}

