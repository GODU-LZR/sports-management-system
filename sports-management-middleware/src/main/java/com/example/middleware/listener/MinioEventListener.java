package com.example.middleware.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.middleware.mapper.FileRecordMapper;
import com.example.middleware.pojo.FileUploadRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MinioEventListener {

    @Autowired
    private FileRecordMapper fileRecordMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 监听MinIO的对象创建事件
     * 
     * @param message RabbitMQ消息内容，包含MinIO事件信息
     */
    @RabbitListener(queues = "minio_notice")
    public void handleMinioEvent(String message) {
        try {
            log.info("收到MinIO事件: {}", message);
            
            // 解析JSON消息
            JsonNode rootNode = objectMapper.readTree(message);
            String eventName = rootNode.path("EventName").asText();
            
            // 只处理对象创建事件
            if ("s3:ObjectCreated:Put".equals(eventName)) {
                JsonNode recordsNode = rootNode.path("Records");
                if (recordsNode.isArray() && recordsNode.size() > 0) {
                    JsonNode firstRecord = recordsNode.get(0);
                    
                    // 获取桶名和对象名
                    String bucketName = firstRecord.path("s3").path("bucket").path("name").asText();
                    String objectKey = firstRecord.path("s3").path("object").path("key").asText();
                    long fileSize = firstRecord.path("s3").path("object").path("size").asLong();
                    String contentType = firstRecord.path("s3").path("object").path("contentType").asText();
                    log.info("文件上传完成 - 桶: {}, 对象: {}, 大小: {}, 类型: {}", 
                            bucketName, objectKey, fileSize, contentType);
                    // 更新数据库中的文件记录状态
                    updateFileRecord(bucketName, objectKey, fileSize, contentType);
                }
            }
        } catch (Exception e) {
            log.error("处理MinIO事件失败", e);
        }
    }
    
    /**
     * 更新文件记录状态
     * 
     * @param bucketName MinIO存储桶名称
     * @param objectName MinIO对象名称
     * @param fileSize 文件大小
     * @param contentType 文件内容类型
     */
    private void updateFileRecord(String bucketName, String objectName, long fileSize, String contentType) {
        try {
            // 根据桶名和对象名查询文件记录
            LambdaQueryWrapper<FileUploadRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FileUploadRecord::getBucketName, bucketName)
                    .eq(FileUploadRecord::getObjectName, objectName);
            
            FileUploadRecord record = fileRecordMapper.selectOne(queryWrapper);
            
            if (record != null) {
                // 更新文件记录状态为成功
                record.setStatus(FileUploadRecord.FileUploadStatus.SUCCESS);
                record.setFileSize(fileSize);
                record.setFileContentType(contentType);
                record.setUpdatedAt(LocalDateTime.now());
                
                fileRecordMapper.updateById(record);
                log.info("文件记录状态已更新为SUCCESS, ID: {}", record.getId());
            } else {
                log.warn("未找到对应的文件记录 - 桶: {}, 对象: {}", bucketName, objectName);
            }
        } catch (Exception e) {
            log.error("更新文件记录状态失败", e);
        }
    }
}