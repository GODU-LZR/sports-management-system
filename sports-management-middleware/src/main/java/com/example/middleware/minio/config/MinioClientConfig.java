package com.example.middleware.minio.config;

import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
public class MinioClientConfig {
    
    private final MinioProperties minioProperties;
    
    public MinioClientConfig(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }
    
    @Data
    @Component
    @ConfigurationProperties(prefix = "minio")
    public static class MinioProperties {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private Integer expiretime;
        private int port;
        private Integer breakpointTime;
    }

    @Bean
    public MinioClient minioClient() {
        try {
            log.debug("一个同步Minio客户端初始化");
            return MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint(),minioProperties.getPort(),false)
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
        } catch (Exception e) {
            log.error("Minio初始化失败", e);
            throw new RuntimeException(e);
        }
    }

    @Bean
    public MinioAsyncClient MinioAsyncMinioAsyncClient(){
        try{
            log.debug("一个异步Minio客户端初始化");
            MinioAsyncClient minioClient = MinioAsyncClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
            return minioClient;
        }catch (Exception e){
            throw new RuntimeException("\"-----创建异步Minio客户端失败-----");
        }
    }
    public int getExpiretime(){
        return minioProperties.expiretime;
    }
}
