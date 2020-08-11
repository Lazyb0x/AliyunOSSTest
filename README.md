# Aliyun OSS SDK Test

阿里云的对象储存容器的简单操作。

文档：<https://help.aliyun.com/document_detail/32008.html>

* Spring Boot
* 简单上传
* 简单下载
* 利用分片功能实现的多线程上传
* 断点续传下载
* 临时链接
* 日志切片

```yaml application.yml
# application.yml
aliyun:
  oss:
    endpoint: https://oss-cn-shenzhen.aliyuncs.com
    accessKeyId: <accessKeyId>
    accessKeySecret: <accessKeySecret>
    bucketName: <bucketName>
```

