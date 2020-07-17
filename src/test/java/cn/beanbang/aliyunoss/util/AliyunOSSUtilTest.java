package cn.beanbang.aliyunoss.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class AliyunOSSUtilTest {

    @Autowired
    AliyunOSSUtil ossUtil;

    @Test
    void listFiles() {
        ossUtil.listFiles();
    }

    @Test
    void upload() {
        String objectName = "test/Hello.txt";
        String content = "Hello OSS!";

        ossUtil.upload(objectName, new ByteArrayInputStream(content.getBytes()));
    }

    @Test
    void download() {
        String objectName = "test/Hello.txt";
        File file = new File("Hello.txt");

        ossUtil.download(objectName, file);
    }

    @Test
    void read() {
        String objectName = "test/Hello.txt";
        ossUtil.read(objectName);
    }

    @Test
    void concurrentUpload() throws Exception{
        String objectName = "test/upload.zip";
        File localFile = ResourceUtils.getFile("classpath:upload.zip");
        long partSize = 1024*1024L;
        ossUtil.concurrentUpload(objectName, localFile, partSize, 20);
    }

    @Test
    void resumableDownload() throws Throwable{
        String objectName = "test/upload.zip";
        long partSize = 1024*1024L;
        String fileName = "download.zip";
        ossUtil.resumableDownload(objectName, partSize, 10, fileName);
    }
}