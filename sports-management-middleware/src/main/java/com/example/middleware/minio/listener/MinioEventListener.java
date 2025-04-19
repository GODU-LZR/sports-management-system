package com.example.middleware.minio.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.utils.RedisUtil;
import com.example.middleware.minio.mapper.FileRecordMapper;
import com.example.middleware.minio.pojo.FileUploadRecord;
import com.example.middleware.minio.MinioUtil;
import com.example.middleware.utils.RedisKEY;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



@Slf4j
@Component
public class MinioEventListener {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FileRecordMapper fileRecordMapper;

    @Autowired
    private ObjectMapper objectMapper;

//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;

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
                    // 从 JSON 中提取所需字段
                    String minioEndpoint = rootNode.path("Records").get(0).path("responseElements").path("x-minio-origin-endpoint").asText();
                    String objectKey = firstRecord.path("s3").path("object").path("key").asText();

                    long fileSize = firstRecord.path("s3").path("object").path("size").asLong();
                    String contentType = firstRecord.path("s3").path("object").path("contentType").asText();
                    log.debug("文件上传完成 - 桶: {}, 对象: {}, 大小: {}, 类型: {}",
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
     * @param bucketName  MinIO存储桶名称
     * @param objectName  MinIO对象名称
     * @param fileSize    文件大小
     * @param contentType 文件内容类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFileRecord(String bucketName, String objectName, long fileSize, String contentType) {
        try {
            // 拼接图片地址
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

                String url= MinioUtil.buildImageUrl(bucketName,objectName);
                redisUtil.set(RedisKEY.PIC_VIEW_URL_KEY+extractLeadingNumbers(objectName),url);
                // 通知前端文件上传完成
                notifyFrontend(record);
            } else {
                log.warn("未找到对应的文件记录 - 桶: {}, 对象: {}", bucketName, objectName);
            }
        } catch (Exception e) {
            log.error("更新文件记录状态失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new RuntimeException("更新文件记录失败", e);

        }
    }

    /**
     * 通知前端文件上传完成
     *
     * @param record 文件上传记录
     */
    private void notifyFrontend(FileUploadRecord record) {
        try {
            // 构建通知消息
//            Map<String, Object> message = new HashMap<>();
//            message.put("type", "FILE_UPLOAD_COMPLETE");
//            message.put("fileId", record.getFileId());
//            message.put("fileName", record.getFileName());
//            message.put("status", record.getStatus().toString());
//            message.put("fileSize", record.getFileSize());
//            message.put("contentType", record.getFileContentType());
//            message.put("timestamp", System.currentTimeMillis());

//            // 发送WebSocket消息到特定用户
//            String destination = "/queue/file-upload/" + record.getUserId();
//            messagingTemplate.convertAndSend(destination, message);

            log.info("已通知用户 {} 文件 {} 上传完成", record.getUserId(), record.getFileId());
        } catch (Exception e) {
            log.error("通知前端文件上传完成失败", e);
        }
    }

    public static String extractLeadingNumbers(String s) {
        /**
         * 从字符串中提取开头的数字部分。
         *
         * @param s 输入字符串。
         * @return 如果找到数字，则返回数字字符串；否则返回 null。
         */
        Pattern pattern = Pattern.compile("^\\d+");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return null;
        }
    }
}