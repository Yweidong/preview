package com.haoyong.preview.controller;

import com.haoyong.preview.common.ResultBody;
import com.haoyong.preview.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;


/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-12-28 16:41
 **/
@RestController
@RequestMapping("/oss")
@CrossOrigin
public class OssController {

    @Autowired
    IndexService indexService;

    /**
     * @param file 文件
     * @param video_root_path  视频存储路径
     * @param doc_root_path 原始文档存储路径
     * @param preview_root_path  预览文档存储路径
     */

    @PostMapping("/uploadfile")
    public ResultBody<Map<String, Object>> fileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("video_root_path") String video_root_path,
            @RequestParam("doc_root_path") String doc_root_path,
            @RequestParam("preview_root_path") String preview_root_path
            ) throws Exception {


        Map<String, Object> map = indexService.uploadFileOperation(
                file,
                video_root_path,
                doc_root_path,
                preview_root_path
                );

        return ResultBody.success(map);

    }



}
