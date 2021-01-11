package com.haoyong.preview.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @program: preview
 * @description: 存储路径设置
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2021-01-04 10:13
 **/
public class FileDateUtil {

    private static final SimpleDateFormat simpleDateFormat;

    static {
        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
    }
    public static String dateFilePath() {

       return simpleDateFormat.format(System.currentTimeMillis());
    }



}
