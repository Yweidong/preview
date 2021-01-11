package com.haoyong.preview.util;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.haoyong.preview.Enum.CommonEnum;
import com.haoyong.preview.config.AliOssConfig;
import com.haoyong.preview.exce.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.net.URL;
import java.util.Date;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-12-28 15:57
 **/
@Slf4j
@Component
public class OssUploadUtil {

    private AliOssConfig aliOssConfig;
    private OSSClient ossClient;
    private SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);


    public OssUploadUtil(AliOssConfig aliOssConfig, OSSClient ossClient) {
        this.aliOssConfig = aliOssConfig;
        this.ossClient = ossClient;
    }


    /**
     * @param part_path 文件路径前缀
     *
     */
    public  String uploadFile(MultipartFile file,String suffix,String part_path) {
        String fileUrl = uploadImg2Oss(file,suffix,part_path);
        String str = getFileUrl(fileUrl);
        return  str;
    }


    public String uploadFile(File file,String suffix,String part_path) throws FileNotFoundException {


        String fileUrl = uploadImg2Oss(file,suffix,part_path);
        String str = getFileUrl(fileUrl);
        return  str;
    }

    private String uploadImg2Oss(File file,String suffix,String part_path) {
//        //1、限制最大文件为20M
//        if (file.getSize() > 1024 * 1024 *20) {
//            return "图片太大";
//        }

        String name = getUploadName(part_path,suffix);

        try {
            InputStream inputStream = new FileInputStream(file);
            this.uploadFile2OSS(inputStream, name,suffix);
            return name;
        }
        catch (Exception e) {
            throw new BizException(CommonEnum.BODY_NOT_MATCH);
        }
    }

    //获取上传的文件名
    private String getUploadName(String part_path,String suffix) {
        String uuid = String.valueOf(idWorker.nextId());
        return part_path + "/" + uuid + "." + suffix;
    }

    private String uploadImg2Oss(MultipartFile file,String suffix,String part_path) {
//        //1、限制最大文件为20M
//        if (file.getSize() > 1024 * 1024 *20) {
//            return "图片太大";
//        }

        String name = getUploadName(part_path,suffix);

        try {
            InputStream inputStream = file.getInputStream();
            this.uploadFile2OSS(inputStream, name,suffix);
            return name;
        }
        catch (Exception e) {
           throw new BizException(CommonEnum.BODY_NOT_MATCH);
        }
    }

    //判断文件名是否已上传
    private boolean fileNameExists(String encryptName) {

       return ossClient.doesObjectExist(aliOssConfig.getBucketName(), encryptName);

    }

    /**
     * 通过文件名获取文完整件路径
     * @param fileUrl
     * @return 完整URL路径
     */
    public String getFileUrl(String fileUrl) {
        if (fileUrl !=null && fileUrl.length()>0) {
//            String[] split = fileUrl.split("/");
//            String url =  this.getUrl(this.filedir + split[split.length - 1]);
//            String url =  this.getUrl( split[split.length - 1]);
            String url = this.getUrl(fileUrl);
            return url;
        }
        return null;
    }

    //获取去掉参数的完整路径
    private String getShortUrl(String url) {
        String[] imgUrls = url.split("\\?");
        return imgUrls[0].trim();
    }

    // 获得url链接
    private String getUrl(String key) {
        // 设置URL过期时间为20年  3600l* 1000*24*365*20
        Date expiration = new Date(new Date().getTime() + 3600l * 1000 * 24 * 365 * 20);
        // 生成URL

        URL url = ossClient.generatePresignedUrl(aliOssConfig.getBucketName(), key, expiration);
        if (url != null) {
            return  getShortUrl(url.toString().replaceFirst(aliOssConfig.getBucketUrl(),aliOssConfig.getUrlPrefix()));
        }
        return null;
    }


    // 上传文件（指定文件名）
    private String uploadFile2OSS(InputStream instream, String fileName,String suffix) {
        String ret = "";
        try {
            //创建上传Object的Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(instream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(contentType(suffix));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            //上传文件


            PutObjectResult putResult = ossClient.putObject(
                    aliOssConfig.getBucketName(),
                    fileName,
                    instream,
                    objectMetadata
            );
            ret = putResult.getETag();

        } catch (IOException e) {
            throw new BizException(CommonEnum.BODY_NOT_MATCH);
        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                throw new BizException(CommonEnum.BODY_NOT_MATCH);
            }
        }
        return ret;
    }


    /**
     *
     * @MethodName: contentType
     * @Description: 获取文件类型
     * @param fileType
     * @return String
     */
    private static String contentType(String fileType){
        fileType = fileType.toLowerCase();
        String contentType = "";
        switch (fileType) {
            case "bmp": contentType = "image/bmp";
                break;
            case "gif": contentType = "image/gif";
                break;
            case "png":
            case "jpeg":
            case "jpg": contentType = "image/jpeg";
                break;
            case "html":contentType = "text/html";
                break;
            case "txt": contentType = "text/plain";
                break;
            case "vsd": contentType = "application/vnd.visio";
                break;
            case "ppt":
            case "pptx":contentType = "application/vnd.ms-powerpoint";
                break;
            case "doc":
            case "docx":contentType = "application/msword";
                break;
            case "xml":contentType = "text/xml";
                break;
            case "mp4":contentType = "video/mp4";
                break;
            default: contentType = "application/octet-stream";
                break;
        }
        return contentType;
    }

}
