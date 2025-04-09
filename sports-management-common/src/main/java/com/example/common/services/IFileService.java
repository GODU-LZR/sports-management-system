package com.example.common.services;

public interface IFileService {
    /**
     * 创建文件上传URL
     * @param bucketName MinIO存储桶名称
     * @param userId 用户ID
     * @param fileName 原始文件名
     * @return 上传URL
     */
    <T> T createUploadUrl(String bucketName, String userId, String fileName);

    <T> T getPicURL(String bucketName, String objectName);
}