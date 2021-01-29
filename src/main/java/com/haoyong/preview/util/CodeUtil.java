package com.haoyong.preview.util;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2021-01-12 18:26
 **/
public class CodeUtil {
    public static String getRandomCode() {
        String random=(int)((Math.random()*9+1)*100000)+"";

        return random;
    }


}
