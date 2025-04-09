package com.example.middleware.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件上传记录实体类
 *
 * 该实体类用于记录和追踪文件上传的整个生命周期，包括上传状态、文件元信息和存储位置等关键信息。
 * 设计考虑：
 * 1. 可追踪性：通过记录文件ID、用户ID等信息，实现文件上传的全程追踪
 * 2. 可靠性：包含文件校验和错误信息字段，确保文件完整性
 * 3. 存储管理：记录存储位置和文件大小，便于后续的存储管理和清理
 * 4. 状态管理：使用枚举类型记录文件状态，支持完整的状态流转
 */
@Data
@TableName("file_upload_records")
@InterceptorIgnore(tenantLine = "true")
public class FileUploadRecord {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文件唯一标识，用于文件的快速检索和去重 */
    @TableField(value = "file_id", updateStrategy = FieldStrategy.NOT_NULL)
    private String fileId;

    /** 原始文件名，保留用户上传时的文件名 */
    @TableField(value = "file_name", updateStrategy = FieldStrategy.NOT_NULL)
    private String fileName;

    /** 上传用户的ID，用于权限控制和审计 */
    @TableField(value = "user_id", updateStrategy = FieldStrategy.NOT_NULL)
    private String userId;

    /** MinIO存储桶名称，用于文件的物理存储管理 */
    @TableField(value = "bucket_name", updateStrategy = FieldStrategy.NOT_NULL)
    private String bucketName;

    /** MinIO对象名称，实际存储在MinIO中的文件名 */
    @TableField(value = "object_name", updateStrategy = FieldStrategy.NOT_NULL)
    private String objectName;

    /** 文件MIME类型，用于文件类型识别和下载响应 */
    @TableField(value = "file_content_type", updateStrategy = FieldStrategy.NOT_NULL)
    private String fileContentType;

    /** 文件在存储系统中的完整路径！！！！ 暂时无用*/
    @TableField("storage_path")
    private String storagePath;

    /** 文件上传状态，使用枚举类型确保状态的规范性 */
    @TableField(value = "status", updateStrategy = FieldStrategy.NOT_NULL)
    private FileUploadStatus status = FileUploadStatus.UPLOADING;

    /** 上传失败时的错误信息 */
    @TableField("error_message")
    private String errorMessage = "";

    /** 记录创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 记录最后更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** 文件大小（字节），用于存储空间管理和下载进度计算 */
    @TableField(value = "file_size", updateStrategy = FieldStrategy.NOT_NULL)
    private Long fileSize = 0L;

    /** 文件校验和，用于验证文件完整性 */
    @TableField("checksum")
    private String checksum = "";
@Override

public String toString(){
    return fileName+fileId;
}
    /**
     * 文件上传状态枚举
     * UPLOADING: 文件正在上传中
     * PROCESSING: 文件上传完成，正在处理中（如校验、转码等）
     * SUCCESS: 文件处理完成，可以使用
     * FAILED: 文件上传或处理失败
     */
    public enum FileUploadStatus {
        UPLOADING,
        PROCESSING,
        SUCCESS,
        FAILED
    }
}