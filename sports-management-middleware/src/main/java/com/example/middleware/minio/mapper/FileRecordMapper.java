package com.example.middleware.minio.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.middleware.minio.pojo.FileUploadRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileRecordMapper extends BaseMapper<FileUploadRecord> {


}
