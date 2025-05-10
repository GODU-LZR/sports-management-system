package com.example.middleware.minio;

import com.example.middleware.minio.pojo.UploadResult;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioUtil {
    private static MinioUtil instance;

    @Value(value = "minio.expiretime")
    private String expiretime;
    @Value(value = "minio.endpoint")
    private String endpoint;
    @Value(value = "minio.port")
    private String port;
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
            UploadResult uploadR = new UploadResult();
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

    /**
     * 构建图片URL
     *
     * @param bucketName 存储桶名称
     * @param objectKey  对象键名
     * @return 图片访问URL
     * @throws Exception 异常信息
     */
    public static String buildImageUrl(String bucketName, String objectKey)  {
        if (bucketName == null || bucketName.isEmpty() || objectKey == null || objectKey.isEmpty()) {
            log.error("构建图片URL失败: 存储桶名称或对象键名为空");
            throw new IllegalArgumentException("存储桶名称或对象键名不能为空");
        }
        String minioServerUrl = "http://124.71.58.72:9000";



        // 构建完整的图片URL
        try{
        URI uri = new URI(minioServerUrl + "/" + bucketName + "/" + objectKey);
        String url = uri.toString();

        log.info("图片URL构建成功: {}", url);
        return url;
        }catch (Exception e){
            throw new RuntimeException(e);
        }

}

}
