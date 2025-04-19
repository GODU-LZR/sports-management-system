package com.example.middleware.minio.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.services.IFileService;
import com.example.common.utils.RedisUtil;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.middleware.minio.MinioUtil;
import com.example.middleware.minio.mapper.FileRecordMapper;
import com.example.middleware.minio.pojo.FileUploadRecord;
import com.example.middleware.minio.pojo.UploadResult;
import com.example.middleware.utils.RedisKEY;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DubboService(version = "1.0.0", interfaceClass = com.example.common.services.IFileService.class)
@Service
@Slf4j
public class FileServiceImpl implements IFileService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    private FileRecordMapper fileRecordMapper;
    @Autowired
    SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> T createUploadUrl(String bucketName, String userId, String fileName) {
        // 创建自定义返回结果对象
        UploadResult uploadR = redisUtil.getFromJson(RedisKEY.PIC_UPLOAD_URL_KEY+userId, UploadResult.class);
       if (uploadR==null){
           uploadR=createUploadUrlUnCycle(bucketName,userId,fileName);
       }
        boolean r = redisUtil.set(RedisKEY.PIC_UPLOAD_URL_KEY+userId, uploadR, 3600);
        return (T) uploadR;
    }

    @Override
    public <T> T createUploadUrl(String bucketName, String userId) {
       return createUploadUrl(bucketName,userId,"kong");
    }

    @Override
    public <T> T createUploadUrl(String userId) {
        return createUploadUrl("images",userId,"kong");
    }


//   ---------------------------------------------------------------------------


    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> T createUploadUrlUnCycle(String bucketName, String userId, String fileName) {
        UploadResult uploadR=new UploadResult();
        String fileId =String.valueOf( snowflakeIdGenerator.nextId());
        String objectName = fileId + bucketName + userId;
        uploadR = MinioUtil.getUploadUrl(bucketName, objectName);
        uploadR.setFileRecordID(fileId);
        FileUploadRecord record = new FileUploadRecord();
        record.setFileId(fileId);
        record.setObjectName(objectName);
        record.setBucketName(bucketName);
        record.setFileName(fileName);
        record.setFileContentType("application/");
        record.setUserId(userId);
        // 将结果转换为泛型T并返回
        try {
            fileRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("无法插入数据库");
            return null;
        }
        return null;
    }
    @Override
    public <T> T createUploadUrlUnCycle(String bucketName,String userId) {
        return createUploadUrlUnCycle(bucketName,userId,"kong");
    }

    @Override
    public <T> T createUploadUrlUnCycle(String userId) {
        return createUploadUrlUnCycle("images",userId,"kong");
    }




//--------------------------------------------------------------------------------------------------
    @Override
    public <T> T getViewPicURL(String fileId) {
        String viewURL = (String) redisUtil.get(RedisKEY.PIC_VIEW_URL_KEY + fileId);
        FileUploadRecord fileUploadRecord = null;
        if (viewURL == null) {
            fileUploadRecord = fileRecordMapper.selectOne(new QueryWrapper<FileUploadRecord>()
                    .eq("file_id", fileId));
            if (fileUploadRecord != null) {
                viewURL = MinioUtil.buildImageUrl(fileUploadRecord.getBucketName(), fileUploadRecord.getObjectName());
                redisUtil.set(RedisKEY.PIC_VIEW_URL_KEY + fileId, viewURL, 100);
            } else {
                redisUtil.set(RedisKEY.PIC_VIEW_URL_KEY + fileId, " ", 600);
            }
        }
        return (T) viewURL;
    }

    @Override
    public <T> T getCoverPicURL(String fileId) {
        FileUploadRecord fileUploadRecord = fileRecordMapper.selectOne(new QueryWrapper<FileUploadRecord>()
                .eq("file_id", fileId));
        if(fileUploadRecord!=null){
          UploadResult ur=  MinioUtil.getUploadUrl(fileUploadRecord.getBucketName(),fileUploadRecord.getObjectName());
          return (T) ur.getUrl();
        }
        return  null;
    }
}
