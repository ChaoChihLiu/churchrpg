package com.cpbpc;

import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.net.URLEncoder;

public class Encoder {

    public static  void main(String args[]){
        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("士1-3")));
    }

}
