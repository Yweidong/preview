package com.haoyong.preview.util;



import com.haoyong.preview.exce.BizException;
import lombok.extern.slf4j.Slf4j;
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.MultimediaObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;

/**
 * @program: preview
 * @description: 音视频获取时长，大小
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2021-01-20 16:41
 **/
@Slf4j
public class AudioVideoUtil {



    //获取时长
    public static long getDurationSecond(File file) {
//        String locapath = stringBuffer.toString();
//        File file = new File(locapath);
        try {

            //解析文件
            MultimediaObject object = new MultimediaObject(file);
            MultimediaInfo multimediaInfo = object.getInfo();

            long duration = multimediaInfo.getDuration();

            return duration / 1000;
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        } finally {
            file.delete();
        }

    }

    //获取视频的大小
    public static Long getVideoSize(StringBuffer stringBuffer) {
        FileChannel fc = null;
        String locapath = stringBuffer.toString();
        File file = new File(locapath);
        try {
            //上传的文件放置到临时目录下
            if (!file.getParentFile().exists()) {
                file.mkdirs();
            }

            FileInputStream inputStream = new FileInputStream(file);
            fc = inputStream.getChannel();
            BigDecimal filesize = new BigDecimal(fc.size());
            BigDecimal divide = filesize.divide(new BigDecimal(1024 * 1024), 2, RoundingMode.HALF_UP);
            return  divide.longValue();
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        } finally {
            file.delete();
            if (fc != null) {
                try {
                    fc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

    }
}
