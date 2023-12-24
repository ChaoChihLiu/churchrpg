package com.cpbpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Mp3Merger {


    public static void main(String args[]){
        try {
            File file1 = new File("/Users/liuchaochih/Downloads/arpg20240110-1.mp3");
            FileInputStream fistream1 = new FileInputStream(file1);
            File finalfile = new File(file1.getParent()+"/arpg20240110.mp3");
            if(!finalfile.exists())
            {
                finalfile.createNewFile();
            }
            FileOutputStream sistream = new FileOutputStream(finalfile);
            int temp;
            int size = 0;
            temp = fistream1.read();
            while( temp != -1)
            {
                sistream.write(temp);
                temp = fistream1.read();
            };
            fistream1.close();
            FileInputStream fistream2 = new FileInputStream("/Users/liuchaochih/Downloads/arpg20240110-2.mp3");
            fistream2.read(new byte[32],0,32);
            temp = fistream2.read();
            while( temp != -1)
            {
                sistream.write(temp);
                temp = fistream2.read();
            };
            fistream2.close();
            sistream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
