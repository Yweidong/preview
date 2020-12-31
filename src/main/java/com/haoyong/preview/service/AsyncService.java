package com.haoyong.preview.service;

import java.io.File;
import java.util.concurrent.Future;

public interface AsyncService {

    Future<Integer> fileToPdf(File file,String targpath);
}
