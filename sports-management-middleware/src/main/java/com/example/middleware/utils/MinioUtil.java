package com.example.middleware.utils;

import com.example.middleware.pojo.UploadResult;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioUtil {
    private static MinioUtil instance;

    @Value(value = "minio.expiretime")
    private String expiretime;
    @Autowired
    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 获取上传连接
     **/
    public static UploadResult getUploadUrl(String bucketName, String objectName) {
        try {
            String url = instance.minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(3600)
                            .build()
            );
          UploadResult uploadR= new UploadResult();
          uploadR.setUrl(url);
          return uploadR;
        } catch (Exception e) {
            log.error("获取上传URL失败: ", e);
            throw new RuntimeException("获取上传URL失败");
        }
    }

    public static String getUrlByKey(String buckerName, String ObjectName) {
        try {
            String url = instance.minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(buckerName)
                            .object(ObjectName)
                            .expiry(Integer.valueOf(instance.expiretime), TimeUnit.SECONDS)
                            .build()
            );
            return url;
        } catch (Exception e) {
            log.error("获取下载URL失败: ", e);
            throw new RuntimeException("获取下载URL失败");
        }
    }
}
