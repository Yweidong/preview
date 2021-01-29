package com.haoyong.preview.util;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.haoyong.preview.Enum.CommonEnum;
import com.haoyong.preview.config.AliOssConfig;
import com.haoyong.preview.exce.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
    private ThreadPoolTaskExecutor taskExecutor;
    private SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);


    public OssUploadUtil(AliOssConfig aliOssConfig, OSSClient ossClient, @Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.aliOssConfig = aliOssConfig;
        this.ossClient = ossClient;
        this.taskExecutor = taskExecutor;
    }

    /**
     *
     *分片上传
     */
    public String burstFile(File file,String suffix,String part_path){
        try {
            String objectName = getUploadName(part_path, suffix);
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(aliOssConfig.getBucketName(),objectName );
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
            String uploadId = upresult.getUploadId();
            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
            List<PartETag> partETags =  new ArrayList<PartETag>();
            // 计算文件有多少个分片。
            final long partSize = 3 * 1024 * 1024L;

            long fileLength = file.length();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }

            CountDownLatch latch = new CountDownLatch(partCount);
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                InputStream instream = new FileInputStream(file);
                // 跳过已经上传的分片。
                instream.skip(startPos);

                int finalI = i;
                Runnable run =  new Runnable() {

                   @Override
                   public void run() {
                       UploadPartRequest uploadPartRequest = new UploadPartRequest();
                       uploadPartRequest.setBucketName(aliOssConfig.getBucketName());
                       uploadPartRequest.setKey(objectName);
                       uploadPartRequest.setUploadId(uploadId);
                       uploadPartRequest.setInputStream(instream);
                       // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                       uploadPartRequest.setPartSize(curPartSize);
                       // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
                       uploadPartRequest.setPartNumber( finalI + 1);
                       // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                       UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                       // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
                       partETags.add(uploadPartResult.getPartETag());
                       latch.countDown();
                   }
               };
               taskExecutor.execute(run);


            }
            latch.await();
            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(aliOssConfig.getBucketName(), objectName, uploadId, partETags);


            // 完成上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            return completeMultipartUploadResult.getLocation();
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    //断点续传
    public String breakPointFile(String locafilepath,String suffix,String part_path) throws Throwable {
        String objectName = getUploadName(part_path, suffix);
        UploadFileRequest request = new UploadFileRequest(aliOssConfig.getBucketName(), objectName);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(suffix);

        request.setUploadFile(locafilepath);
        request.setBucketName(aliOssConfig.getBucketName());
        request.setTaskNum(10);
        request.setPartSize(1 * 1024 * 1024);
        request.setEnableCheckpoint(true);
        request.setObjectMetadata(meta);
        UploadFileResult uploadFileResult = ossClient.uploadFile(request);
        CompleteMultipartUploadResult uploadResult = uploadFileResult.getMultipartUploadResult();
        return uploadResult.getLocation();
    }

    public String uploadFile(File file,String suffix,String part_path) {

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
//            return  getShortUrl(url.toString().replaceFirst(aliOssConfig.getBucketUrl(),aliOssConfig.getUrlPrefix()));
            return getShortUrl(url.toString());
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
        String contentType = null;
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
            case "pdf":contentType = "application/pdf";
                break;
            default: contentType = "application/octet-stream";
                break;
        }
        return contentType;
    }

}
