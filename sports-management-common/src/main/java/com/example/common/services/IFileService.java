package com.example.common.services;

public interface IFileService {
    /**
     * 创建文件上传URL
     * @param bucketName MinIO存储桶名称
     * @param userId 用户ID
     * @param fileName 原始文件名
     * @return 上传URL
     */
/**
 * 短时复用同一上传url，
 * 短时间是重复的fileid记录
 * */
    <T> T createUploadUrl(String bucketName, String userId, String fileName);
// 指定桶名，原文件名无意义时使用
    <T> T createUploadUrl(String bucketName, String userId);
    //    默认桶images的上传,且原文件名无意义时使用
    <T> T createUploadUrl( String userId);
/**
 * 永不复用url，
 * 适用于总是新的图片
 */
    <T>T createUploadUrlUnCycle(String bucketName, String userId, String fileName);
    //    默认桶images的上传
    <T>T createUploadUrlUnCycle( String bucketName, String userId);
    <T>T createUploadUrlUnCycle( String userId);

/**
 *   根据文件id拿到用于查看的url
 */

    <T> T getViewPicURL(String fileId);
/**
 *     根据文件id拿到用于覆盖的上传url适用于头像
 */

<T> T getCoverPicURL(String fileId);
}