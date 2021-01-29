package com.haoyong.preview.service.impl;

import com.haoyong.preview.service.AsyncService;
import com.haoyong.preview.service.IndexService;
import com.haoyong.preview.util.FileDateUtil;
import com.haoyong.preview.util.OssUploadUtil;
import com.haoyong.preview.util.UUIDUtil;
import org.jodconverter.DocumentConverter;
import org.jodconverter.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2021-01-04 09:49
 **/
@Service
public class IndexServiceImpl implements IndexService {

    @Value("${preview.temporary_preview_path}")
    private String temporary_preview_path;

    @Value("${file.locationTemp}")
    private String locationTemp;

    @Autowired
    OssUploadUtil ossUploadUtil;

    @Autowired
    AsyncService asyncService;

    @Resource(name = "jodConverter")
    DocumentConverter documentConverter;

    /**
     * @param file 文件

     * @param video_root_path  视频存储路径
     * @param doc_root_path 原始文档存储路径
     * @param preview_root_path  预览文档存储路径
     */
    @Override
    public Map<String, Object> uploadFileOperation(
            MultipartFile file,
            String video_root_path,
            String doc_root_path,
            String preview_root_path
    ) throws OfficeException, IOException, ExecutionException, InterruptedException {

        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        map.put("size",file.getSize());
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase(); //文件后缀
        map.put("suffix",suffix);
        map.put("file_name",fileName.substring(0,fileName.lastIndexOf(".")));

        String part_path = "";
        String s  = "";
        //文件路径前缀
//        String part_path = suffix.equals("mp4")?contentType("video",video_root_path,doc_root_path,preview_root_path)
//                :contentType("resource",video_root_path,doc_root_path,preview_root_path);
        //临时文件目录
        StringBuffer stringBuffer = new StringBuffer(locationTemp)
                .append(System.currentTimeMillis())
                .append("_").append(file.getOriginalFilename());
        File file1 = new File(stringBuffer.toString());
        //上传的文件放置到临时目录下
        if (!file1.getParentFile().exists()) {
            file1.mkdirs();
        }
        //copy文件
        file.transferTo(file1);


        map.put("page",0);//页数
        map.put("time",0);
        map.put("preview_path","");
        if (suffix.equals("doc") || suffix.equals("docx") || suffix.equals("pptx") || suffix.equals("ppt") ) {
            part_path = contentType("resource",video_root_path,doc_root_path,preview_root_path);
            s = ossUploadUtil.uploadFile(file1,suffix,part_path);
            map.put("real_path",s);

            StringBuffer targpath = new StringBuffer(temporary_preview_path)
                                    .append(UUIDUtil.getUUID())
                                    .append(".pdf");

//            File file1 = new File(file.getOriginalFilename());
//            FileUtils.copyInputStreamToFile(file.getInputStream(),file1);

            File file3 = new File(targpath.toString());
            documentConverter.convert(file1)
                    .to(file3)
                    .execute();

            Future<Integer> integerFuture = asyncService.fileToPdf(file1,targpath.toString());
            map.put("page",integerFuture.get());
            part_path = contentType("preview",video_root_path,doc_root_path,preview_root_path);
            String pdf = ossUploadUtil.uploadFile(file3, "pdf",part_path);
            map.put("preview_path",pdf);
            asyncService.deleteTemporaryFile(targpath.toString());

        } else if (suffix.equals("mp4")) {

            part_path = contentType("video",video_root_path,doc_root_path,preview_root_path);
            Future<Long> avTime = asyncService.getAVTime(file1);

            s = ossUploadUtil.burstFile(file1,suffix,part_path);

//                s = ossUploadUtil.breakPointFile(stringBuffer.toString(),suffix,part_path);

            map.put("real_path",s);
            map.put("time", avTime.get());
            asyncService.deleteTemporaryFile(stringBuffer.toString());
        }


        return map;
    }

    private static String contentType(String type,String video_path,String doc_path,String preview_path){
        switch (type) {
            case "resource":
                return doc_path + "/" + FileDateUtil.dateFilePath();

            case "preview":
                return  preview_path +  "/" + FileDateUtil.dateFilePath();

            case "video":
                return video_path + "/" + FileDateUtil.dateFilePath();

            default:
                return doc_path + "/"  + FileDateUtil.dateFilePath();

        }
    }
}
