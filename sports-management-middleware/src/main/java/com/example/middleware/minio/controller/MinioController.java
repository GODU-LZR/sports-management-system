package com.example.middleware.minio.controller;

import com.example.common.response.Result;
import com.example.common.services.IFileService;
import com.example.common.utils.RedisUtil;
import com.example.middleware.minio.pojo.UploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/pic")
@Tag(name = "文件服务", description = "文件上传、查看相关接口")
public class MinioController {

    @DubboReference(version = "1.0.0", check = false)
    private IFileService fileService;
    @Autowired(required = false)
    private RedisUtil redisUtil;

    // 已有方法
    @RequestMapping("/view/{fileId}")
    @Operation(summary = "获取图片URL", description = "根据文件ID获取图片查看URL")
    public Result getPic(@PathVariable("fileId") String fileid) {
        String url = fileService.getViewPicURL(fileid);
        return Result.success(url);
    }
    
    @RequestMapping("/upload/avatar/{userid}")
    @Operation(summary = "获取头像上传URL", description = "获取用户头像上传URL")
    public Result getAvatarUploadUrl(@PathVariable("userid") String userid) {
        UploadResult u = fileService.createUploadUrl("images", userid, "avatar");
        return Result.success(u);
    }

    // 新增方法验证
    @GetMapping("/cover/{fileId}")
    @Operation(summary = "获取封面图片URL", description = "根据文件ID获取封面图片URL")
    public Result getCoverPic(@PathVariable("fileId") String fileId) {
        String url = fileService.getCoverPicURL(fileId);
        return Result.success(url);
    }
    
    @GetMapping("/upload/simple/{userid}")
    @Operation(summary = "简单上传URL", description = "只提供用户ID获取上传URL")
    public Result getSimpleUploadUrl(@PathVariable("userid") String userid) {
        UploadResult result = fileService.createUploadUrl(userid);
        return Result.success(result);
    }
    
    @GetMapping("/upload/bucket/{bucketName}/{userid}")
    @Operation(summary = "指定存储桶上传URL", description = "指定存储桶和用户ID获取上传URL")
    public Result getBucketUploadUrl(
            @PathVariable("bucketName") String bucketName,
            @PathVariable("userid") String userid) {
        UploadResult result = fileService.createUploadUrl(bucketName, userid);
        return Result.success(result);
    }
    
    @GetMapping("/upload/uncycle/simple/{userid}")
    @Operation(summary = "简单非循环上传URL", description = "只提供用户ID获取非循环上传URL")
    public Result getSimpleUncycleUploadUrl(@PathVariable("userid") String userid) {
        UploadResult result = fileService.createUploadUrlUnCycle(userid);
        return Result.success(result);
    }
    
    @GetMapping("/upload/uncycle/bucket/{bucketName}/{userid}")
    @Operation(summary = "指定存储桶非循环上传URL", description = "指定存储桶和用户ID获取非循环上传URL")
    public Result getBucketUncycleUploadUrl(
            @PathVariable("bucketName") String bucketName,
            @PathVariable("userid") String userid) {
        UploadResult result = fileService.createUploadUrlUnCycle(bucketName, userid);
        return Result.success(result);
    }
    
    @GetMapping("/upload/uncycle/full/{bucketName}/{userid}/{fileName}")
    @Operation(summary = "完整非循环上传URL", description = "指定存储桶、用户ID和文件名获取非循环上传URL")
    public Result getFullUncycleUploadUrl(
            @PathVariable("bucketName") String bucketName,
            @PathVariable("userid") String userid,
            @PathVariable("fileName") String fileName) {
        UploadResult result = fileService.createUploadUrlUnCycle(bucketName, userid, fileName);
        return Result.success(result);
    }
}
