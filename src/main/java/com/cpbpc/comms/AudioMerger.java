package com.cpbpc.comms;

import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AudioMerger {

    public static File mergeTo(List<File> inputs, String filePath, String outputName ) throws IOException {
        if(!StringUtils.endsWith(filePath, "/") ){
            filePath += "/";
        }

        File result = new File(filePath + outputName);

        try{
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
            throw e;
        }

        return result;
    }
    
    public static void main(String[] args) throws IOException, SQLException {
        AppProperties.loadConfig("src/main/resources/app-bibleplan-english.properties");
        DBUtil.initStorage(AppProperties.getConfig());
        VerseIntf verseRegex = ThreadStorage.getVerse();

        String verses = "Gen 4-6";
        List<String> result = verseRegex.analyseVerse(verses);
        int start = 0;
        int end = 0;

        if( PunctuationTool.containHyphen(result.get(1)) ){
            String hyphen = PunctuationTool.getHyphen(result.get(1));
            String[] inputs = StringUtils.split(result.get(1), hyphen);
            start = Integer.valueOf(inputs[0]);
            end = Integer.valueOf(inputs[1]);
        }else{
            start = end = Integer.valueOf(result.get(1));
        }

        File local_audio_directory = new File(AppProperties.getConfig().getProperty("local_audio_path"));
        if( !local_audio_directory.exists() ){
            local_audio_directory.mkdirs();
            return;
        }

        String book = result.get(0);
        List<File> toBeMerged = new ArrayList<>();
        for( int i = start; i<=end; i++ ){
            File book_directory = new File( local_audio_directory.getAbsolutePath()+"/"+book+"/"+i );
            if( !book_directory.exists() ){
                continue;
            }
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

        String finalName = StringUtils.replace(verses, " ", "_");
        AudioMerger.mergeTo( toBeMerged, AppProperties.getConfig().getProperty("local_merged_path"), finalName+".mp3" );
    }
}

