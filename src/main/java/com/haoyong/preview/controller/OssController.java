package com.haoyong.preview.controller;

import com.haoyong.preview.common.ResultBody;
import com.haoyong.preview.service.AsyncService;
import com.haoyong.preview.util.OssUploadUtil;
import com.haoyong.preview.util.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-12-28 16:41
 **/
@RestController
@RequestMapping("/oss")
@Slf4j
public class OssController {

    @Value("${preview.temporary_preview_path}")
    private String temporary_preview_path;

    @Autowired
    OssUploadUtil ossUploadUtil;

    @Autowired
    AsyncService asyncService;

    @PostMapping("/uploadfile")
    public ResultBody<ConcurrentHashMap<String, Object>> fileUpload(@RequestParam("file") MultipartFile file) throws Exception {

        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        map.put("size",file.getSize());
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase(); //文件后缀
        map.put("suffix",suffix);
        map.put("file_name",fileName.substring(0,fileName.lastIndexOf(".")));


        String s = ossUploadUtil.uploadFile(file,suffix);


        map.put("real_path",s);
        map.put("page",0);
        map.put("preview_path","");
        if (suffix.equals("doc") || suffix.equals("docx") || suffix.equals("pptx") || suffix.equals("ppt") ) {

            String targpath = temporary_preview_path+UUIDUtil.getUUID()+".pdf";


            File file1 = new File(file.getOriginalFilename());
            FileUtils.copyInputStreamToFile(file.getInputStream(),file1);
            Future<Integer> integerFuture = asyncService.fileToPdf(file1,targpath);
            map.put("page",integerFuture.get());

            File file2 = new File(targpath);
            String pdf = ossUploadUtil.uploadFile(file2, "pdf");
            map.put("preview_path",pdf);
        }


        return ResultBody.success(map);

    }



}
