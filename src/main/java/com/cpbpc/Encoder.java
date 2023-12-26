package com.cpbpc;

import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class Encoder {

    public static  void main(String args[]){
        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("歌林多前書")));
        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("歌林多後書")));

        System.out.println(URLDecoder.decode("%E6%9E%97%E5%89%8D"));
    }

}
