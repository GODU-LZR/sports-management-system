package com.example.middleware.service.impl;

import com.example.common.services.IFileService;
import com.example.middleware.mapper.FileRecordMapper;
import com.example.middleware.pojo.FileUploadRecord;
import com.example.middleware.pojo.UploadResult;
import com.example.middleware.utils.MinioUtil;
import com.example.middleware.utils.SnowflakeIdGeneratorM;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DubboService(version = "1.0.0",interfaceClass =com.example.common.services.IFileService.class)
@Service
@Slf4j
public class FileServiceImpl implements IFileService {
@Autowired
private FileRecordMapper fileRecordMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T> T createUploadUrl(String bucketName, String userId, String fileName) {
        // 创建自定义返回结果对象
        String fileId= SnowflakeIdGeneratorM.nextIdStr();
        String objectName=fileId+bucketName+userId;
        UploadResult uploadR = MinioUtil.getUploadUrl(bucketName, objectName);


        uploadR.setFileRecordID(fileId);
        FileUploadRecord record=new FileUploadRecord();
        record.setFileId(fileId);
        record.setObjectName(objectName);
        record.setBucketName(bucketName);
        record.setFileName(fileName);
        record.setFileContentType("application/");
        record.setUserId(userId);

        // 将结果转换为泛型T并返回
        fileRecordMapper.insert(record);
        return (T) uploadR;
    }

    @Override
    public <T> T getPicURL(String bucketName, String objectName) {
        // 创建自定义返回结果对象

    return null;
    }
}
