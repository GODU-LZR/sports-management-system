package com.example.middleware.controller;

import com.example.common.dto.UserRoleWrapper;
import com.example.common.response.Result;
import com.example.common.services.IFileService;
import com.example.middleware.pojo.UploadResult;
import com.example.middleware.utils.SnowflakeIdGeneratorM;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@CrossOrigin
@RequestMapping("/pic")
public class MinioController {

    @DubboReference(version = "1.0.0", check = false)
    private IFileService fileService;

    @RequestMapping("/upload/{bucketName}")
    public Result getUploadUrl(
        @PathVariable("bucketName") String bucketName,
        @Parameter(hidden = true) UserRoleWrapper currentUser
        )
    {

        UploadResult u=fileService.createUploadUrl(bucketName, String.valueOf(currentUser.getUserId()), "KONG" );
        return Result.success(u);
    }

    @RequestMapping("/upload/{bucketName}/t")
    public Result getUploadUrl2(
            @PathVariable("bucketName") String bucketName
    )
    {

        UploadResult u=fileService.createUploadUrl(bucketName, "ttt", "KONG");
        return Result.success(u);
    }




}
