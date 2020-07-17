package cn.beanbang.aliyunoss;


import cn.beanbang.aliyunoss.util.AliyunOSSConfig;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@SpringBootTest
public class OssTest {

    @Autowired
    AliyunOSSConfig ossConfig;

    @Test
    void testList() {

        OSS ossClient = ossConfig.getOssClient();
        String bucketName = ossConfig.getBucketName();

        ObjectListing objectListing = ossClient.listObjects(bucketName);

        for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            log.info(" - {} (size = {} )",
                    objectSummary.getKey(), objectSummary.getSize());
        }

        ossClient.shutdown();
    }

    @Test
    void testUpload() {
        OSS ossClient = ossConfig.getOssClient();
        String bucketName = ossConfig.getBucketName();
        String objectName = "test/Hello.txt";

        String content = "Hello OSS!";

        ossClient.putObject(bucketName, objectName,
                new ByteArrayInputStream(content.getBytes()));

        ossClient.shutdown();
    }

    @Test
    void testDownload() throws IOException {
        OSS ossClient = ossConfig.getOssClient();
        String bucketName = ossConfig.getBucketName();
        String objectName = "test/Hello.txt";

        OSSObject ossObject = ossClient.getObject(bucketName, objectName);

        InputStream content = ossObject.getObjectContent();
        if (content != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                System.out.println(line);
            }
        }

        content.close();
        ossClient.shutdown();
    }

    @Test
    void multipartUpload() throws Exception {

        // 创建OSSClient实例。
        OSS ossClient = ossConfig.getOssClient();
        String bucketName = ossConfig.getBucketName();
        String objectName = "test/upload.zip";
        File localFile = ResourceUtils.getFile("classpath:upload.zip");

        // 创建InitiateMultipartUploadRequest对象。
        InitiateMultipartUploadRequest request =
                new InitiateMultipartUploadRequest(bucketName, objectName);

        // 初始化分片。
        InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = upresult.getUploadId();

        // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        List<PartETag> partETags = new ArrayList<PartETag>();
        // 计算文件有多少个分片。
        final long partSize = 1 * 1024 * 1024L;   // 1MB
        final File sampleFile = localFile;
        long fileLength = sampleFile.length();
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }

        long startTime = System.currentTimeMillis();

        // 遍历分片上传。
        for (int i = 0; i < partCount; i++) {
            log.info("{}/{}", i, partCount);
            long startPos = i * partSize;
            long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
            InputStream instream = new FileInputStream(sampleFile);
            // 跳过已经上传的分片。
            instream.skip(startPos);
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(objectName);
            uploadPartRequest.setUploadId(uploadId);
            uploadPartRequest.setInputStream(instream);
            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
            uploadPartRequest.setPartSize(curPartSize);
            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
            uploadPartRequest.setPartNumber(i + 1);
            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
            partETags.add(uploadPartResult.getPartETag());
        }

        long time = System.currentTimeMillis() - startTime;
        log.info("total time: {} s", (float)time/1000);

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

        CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);

        ossClient.shutdown();
    }

    @Test
    void concurrentUpload() throws Exception{
        // 创建OSSClient实例。
        OSS ossClient = ossConfig.getOssClient();
        String bucketName = ossConfig.getBucketName();
        String objectName = "test/upload.zip";
        File localFile = ResourceUtils.getFile("classpath:upload.zip");

        // 创建InitiateMultipartUploadRequest对象。
        InitiateMultipartUploadRequest request =
                new InitiateMultipartUploadRequest(bucketName, objectName);

        // 初始化分片。
        InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = upresult.getUploadId();

        // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        List<PartETag> partETags = new ArrayList<PartETag>();
        // 计算文件有多少个分片。
        final long partSize = 1 * 1024 * 1024L;   // 1MB
        final File sampleFile = localFile;
        long fileLength = sampleFile.length();
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(15);
        List<Future<UploadPartResult>> uploadResults = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // 遍历分片上传。
        for (int i = 0; i < partCount; i++) {
            log.info("{}/{}", i, partCount);
            long startPos = i * partSize;
            long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
            InputStream instream = new FileInputStream(sampleFile);
            // 跳过已经上传的分片。
            instream.skip(startPos);
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(objectName);
            uploadPartRequest.setUploadId(uploadId);
            uploadPartRequest.setInputStream(instream);
            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
            uploadPartRequest.setPartSize(curPartSize);
            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
            uploadPartRequest.setPartNumber(i + 1);

            /*Callable<UploadPartResult> callableTask = () -> {
                return ossClient.uploadPart(uploadPartRequest);
            };*/

            Runnable task = () -> {
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                partETags.add(uploadPartResult.getPartETag());
                log.info("finished");
            };

//            uploadResults.add(executorService.submit(callableTask));
            executorService.submit(task);

            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
//            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
//            partETags.add(uploadPartResult.getPartETag());
        }

        executorService.shutdown();
        while (!executorService.isTerminated()){
            Thread.sleep(100);
        }

        /*for (Future<UploadPartResult> resultFuture : uploadResults){
            while (true){
                if (resultFuture.isDone() && !resultFuture.isCancelled()){
                    partETags.add(resultFuture.get().getPartETag());
                    log.info("finished");
                    break;
                }
                else {
                    Thread.sleep(100);
                }
            }
        }*/

        long time = System.currentTimeMillis() - startTime;
        log.info("total time: {} s", (float)time/1000);

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

        CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);

        ossClient.shutdown();
    }
}
