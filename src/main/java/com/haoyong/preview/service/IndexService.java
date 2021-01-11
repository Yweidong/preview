package com.haoyong.preview.service;

import org.jodconverter.office.OfficeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2021-01-04 09:47
 **/
public interface IndexService {

    Map<String,Object> uploadFileOperation(
            MultipartFile file,
            String column_id,
            String video_root_path,
            String doc_root_path,
            String preview_root_path
    ) throws OfficeException, IOException, ExecutionException, InterruptedException;
}
