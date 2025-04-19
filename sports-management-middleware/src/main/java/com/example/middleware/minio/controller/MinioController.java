package com.example.middleware.minio.controller;

import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.common.services.IFileService;
import com.example.middleware.minio.pojo.UploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/pic")
@Tag(name = "文件服务", description = "文件上传、查看相关接口")
public class MinioController {

    @DubboReference(version = "1.0.0", check = false)
    private IFileService fileService;


    // 已有方法
    @GetMapping("/view/{fileId}")
    @Operation(summary = "获取图片URL", description = "根据文件ID获取图片查看URL")
    public Result getPic(@PathVariable("fileId") String fileid) {
        String url = fileService.getViewPicURL(fileid);
        return Result.success(url);
    }

    @GetMapping("/upload/avatar")
    @Operation(summary = "获取头像上传URL", description = "获取用户头像上传URL")
    public Result getAvatarUploadUrl(@Parameter(hidden = true) UserConstant currentUser) {
        UploadResult u = fileService.createUploadUrl("images", String.valueOf(currentUser.getUserId()), "avatar");
        return Result.success(u);
    }

    // 新增方法验证
    @GetMapping("/cover/{fileId}")
    @Operation(summary = "获取封面图片URL", description = "根据文件ID获取封面图片URL")
    public Result getCoverPic(@PathVariable("fileId") String fileId) {
        String url = fileService.getCoverPicURL(fileId);
        return Result.success(url);
    }
    
    @GetMapping("/upload/simple")
    @Operation(summary = "简单上传URL", description = "只提供用户ID获取上传URL")
    public Result getSimpleUploadUrl(@Parameter(hidden = true) UserConstant currentUser) {
        UploadResult result = fileService.createUploadUrl(String.valueOf(currentUser.getUserId()));
        return Result.success(result);
    }
    
    @GetMapping("/upload/bucket/{bucketName}")
    @Operation(summary = "指定存储桶上传URL", description = "指定存储桶和用户ID获取上传URL")
    public Result getBucketUploadUrl(
            @PathVariable("bucketName") String bucketName,
            @Parameter(hidden = true) UserConstant currentUser) {
        UploadResult result = fileService.createUploadUrl(bucketName,String.valueOf(currentUser.getUserId()));
        return Result.success(result);
    }
    
    @GetMapping("/upload/uncycle/simple")
    @Operation(summary = "简单非循环上传URL", description = "只提供用户ID获取非循环上传URL")
    public Result getSimpleUncycleUploadUrl(@Parameter(hidden = true) UserConstant currentUser) {
        UploadResult result = fileService.createUploadUrlUnCycle(String.valueOf(currentUser.getUserId()));
        return Result.success(result);
    }
    
    @GetMapping("/upload/uncycle/bucket/{bucketName}")
    @Operation(summary = "指定存储桶非循环上传URL", description = "指定存储桶和用户ID获取非循环上传URL")
    public Result getBucketUncycleUploadUrl(
            @PathVariable("bucketName") String bucketName,@Parameter(hidden = true) UserConstant currentUser
          ) {
        UploadResult result = fileService.createUploadUrlUnCycle(bucketName, String.valueOf(currentUser.getUserId()));
        return Result.success(result);
    }
    
    @GetMapping("/upload/uncycle/full/{bucketName}//{fileName}")
    @Operation(summary = "完整非循环上传URL", description = "指定存储桶、用户ID和文件名获取非循环上传URL")
    public Result getFullUncycleUploadUrl(
            @PathVariable("bucketName") String bucketName,

            @PathVariable("fileName") String fileName,
            @Parameter(hidden = true) UserConstant currentUser) {
        UploadResult result = fileService.createUploadUrlUnCycle(bucketName, String.valueOf(currentUser.getUserId()), fileName);
        return Result.success(result);
    }
}
