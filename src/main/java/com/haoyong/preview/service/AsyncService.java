package com.haoyong.preview.service;

import java.io.File;
import java.util.concurrent.Future;

public interface AsyncService {

    Future<Integer> fileToPdf(File file,String targpath);//文件转pdf

    void deleteTemporaryFile(String targpath); //删除临时文件
}
