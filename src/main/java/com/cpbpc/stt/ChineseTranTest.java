package com.cpbpc.stt;

import com.github.houbb.opencc4j.util.ZhConverterUtil;

public class ChineseTranTest {

    public static void main(String[] args){
        System.out.println(ZhConverterUtil.toSimple("蒙召"));
        System.out.println(ZhConverterUtil.toTraditional("参"));
//        System.out.println(TextUtil.convertToPinyin("歌林多前書"));
//        System.out.println(TextUtil.convertToPinyin("侍"));
    }

}
