package com.cpbpc;

public class URLGen {

    public static void main(String args[]){
//        String input = "https://cpbpc-tts.s3.ap-southeast-1.amazonaws.com/remembrance/January/dr_January_14_Evening.mp3";

        for( int i = 1; i<=31; i++ ){
            String num = ""+i;
            if( i<10 ){
                num = "0"+i ;
            }
//            System.out.println( "https://cpbpc-tts.s3.ap-southeast-1.amazonaws.com/remembrance/August/dr_August_"+i+"_Morning.mp3" );
//            System.out.println( "https://cpbpc-tts.s3.ap-southeast-1.amazonaws.com/remembrance/August/dr_August_"+i+"_Evening.mp3" );
            System.out.println( "https://cpbpc-rpg-audio.s3.ap-southeast-1.amazonaws.com/rpg/2024_07/arpg202407"+num+".mp3" );
        }

//        String test = "rpg-chinese/2024_03/11/1_start.mp3";
//        System.out.println(test.substring(0, test.lastIndexOf("/")));

//        int rate = Integer.parseInt("75%".replace("%", ""));
//        System.out.println(rate-100);

//        String content = "毁灭或奖赏的能 力是无限的. \n" + "祷告 : 我需要, 每时每刻我都需要";
//        String cleanedText = content.replaceAll("(\\p{IsHan})\\s+(?=\\p{IsHan})", "$1");
//        System.out.println(cleanedText);
    }

}
