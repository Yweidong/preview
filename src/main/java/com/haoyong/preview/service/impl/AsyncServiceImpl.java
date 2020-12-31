package com.haoyong.preview.service.impl;

import com.haoyong.preview.service.AsyncService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.jodconverter.DocumentConverter;
import org.jodconverter.office.OfficeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
public class AsyncServiceImpl implements AsyncService {
    @Resource
    DocumentConverter documentConverter;

    @Override
    @Async("taskExecutor")
    public Future<Integer> fileToPdf(File file,String targpath) {
        try {
            File file1 = new File(targpath);
            documentConverter.convert(file)
                    .to(file1)
                    .execute();
            PDDocument load = PDDocument.load(file1);
            int numberOfPages = load.getNumberOfPages();
            file.delete();
            return new AsyncResult<>(numberOfPages);
        } catch (OfficeException e) {
            e.printStackTrace();
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>(0);
    }
}
