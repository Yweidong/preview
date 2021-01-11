package com.haoyong.preview.service.impl;

import com.haoyong.preview.service.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @program: preview
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-12-30 11:50
 **/
@Service
@Slf4j
public class AsyncServiceImpl implements AsyncService {


    @Override
    @Async("taskExecutor")
    public Future<Integer> fileToPdf(File file,String targpath) {
        try {
            File file1 = new File(targpath);

            PDDocument load = PDDocument.load(file1);
            int numberOfPages = load.getNumberOfPages();
            if (file.exists()) {
                file.delete();
            }

            return new AsyncResult<>(numberOfPages);
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>(0);
    }

    @Override
    @Async("taskExecutor")
    public void deleteTemporaryFile(String targpath) {
        File file = new File(targpath);


        if (file.exists()) {
            file.delete();
        }
    }
}
