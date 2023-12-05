package com.cpbpc.comms;

import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class AudioMerger {

    private static java.util.logging.Logger logger = Logger.getLogger(AudioMerger.class.getName());

    static {
        try {
            AppProperties.loadConfig(System.getProperty("app.properties",
                    "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-bibleplan-chinese.properties"));
            DBUtil.initStorage(AppProperties.getConfig());
        } catch (SQLException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
    }

    public static File mergeTo(List<File> inputs, String filePath, String outputName ) {
        if(!StringUtils.endsWith(filePath, "/") ){
            filePath += "/";
        }

        File dir = new File(filePath);
        File result = new File(filePath + outputName);
        logger.info("merge to " + filePath+outputName);
        try{
            if( !dir.exists() ){
                dir.mkdirs();
            }
            if(!result.exists()){
                result.createNewFile();
            }

            FileOutputStream sistream = new FileOutputStream(result);
            for( File file : inputs ){
                FileInputStream fistream = new FileInputStream(file);
                int temp;
                int size = 0;

                if( inputs.indexOf(file) > 0 ){
                    fistream.read(new byte[32],0,32);
                }

                temp = fistream.read();
                while( temp != -1){
                    sistream.write(temp);
                    temp = fistream.read();
                };
                fistream.close();
            }

        } catch (IOException e){
            logger.info(ExceptionUtils.getStackTrace(e));
        }

        return result;
    }
    
    public static void main(String[] args) {

        try {
            File file = new File( AppProperties.getConfig().getProperty("reading_plan") );
            List<String> verses = SpreadSheetReader.readVerseFromXlsx(file);
            logger.info("verse size " + verses.size());
            mergeMp3(verses);
        }catch (Exception e){
            logger.info(ExceptionUtils.getStackTrace(e));
        }
    }

    public static List<File> mergeMp3(List<String> verses) throws IOException {
        List<File> files = new ArrayList<>();
        for( String verse:verses ){
            files.add(mergeMp3(verse));
        }
        return files;
    }

    public static File mergeMp3(String verse) throws IOException {
        VerseIntf verseRegex = ThreadStorage.getVerse();
        List<String> result = verseRegex.analyseVerse(verse);
        int start = 0;
        int end = 0;

        String book = result.get(0);
        String chapterWord = TextUtil.returnChapterWord(book);
        if( PunctuationTool.containHyphen(result.get(1)) ){
            String hyphen = PunctuationTool.getHyphen(result.get(1));
            String[] inputs = StringUtils.split(result.get(1), hyphen);
            start = Integer.valueOf(StringUtils.replace(StringUtils.trim(inputs[0]), chapterWord, ""));
            end = Integer.valueOf(StringUtils.replace(StringUtils.trim(inputs[1]), chapterWord, ""));
        }else{
            start = end = Integer.valueOf(StringUtils.replace(StringUtils.trim(result.get(1)), chapterWord, ""));
        }

        File local_audio_directory = new File(AppProperties.getConfig().getProperty("local_audio_path"));
        if( !local_audio_directory.exists() ){
            return null;
        }
        
        List<File> toBeMerged = new ArrayList<>();
        for( int i = start; i<=end; i++ ){
            File book_directory = new File( local_audio_directory.getAbsolutePath()+"/"+AppProperties.getConfig().getProperty("output_prefix")+book+"/"+i );
            if( !book_directory.exists() ){
                book_directory.mkdirs();
            }
            AWSUtil.copyS3Objects( AppProperties.getConfig().getProperty("output_bucket"),
                    AppProperties.getConfig().getProperty("output_prefix")+book+"/"+i+"/",
                    AppProperties.getConfig().getProperty("output_format"),
                    AppProperties.getConfig().getProperty("local_audio_path"));
            List<File> list = new ArrayList<>();
            list.addAll(List.of(book_directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if( StringUtils.endsWith(name, "mp3") ){
                        return true;
                    }
                    return false;
                }
            })));
            list.sort(new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    int i1 = Integer.parseInt(StringUtils.split(f1.getName(), "\\.")[0]);
                    int i2 = Integer.parseInt(StringUtils.split(f2.getName(), "\\.")[0]);
                    if( i1 < i2 ){
                        return -1;
                    }
                    if( i1 > i2 ){
                        return 1;
                    }
                    return 0;
                }
            });//end of list sort

            toBeMerged.addAll(list);
        }//end of for loop

        String finalName = StringUtils.remove(StringUtils.replace(verse, " ", "_"), chapterWord);
        return mergeTo( toBeMerged, AppProperties.getConfig().getProperty("local_merged_path"), finalName+".mp3" );
    }
}

