package com.example.common.exception;

import com.example.common.response.Result;
import com.example.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("没有权限：{}", e.getMessage(), e);
        // 捕获权限不足异常，返回403
        return Result.error(ResultCode.FORBIDDEN); // 使用ResultCode.FORBIDDEN
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        // 捕获认证失败异常，返回401
        log.error("认证失败：{}", e.getMessage(), e);
        return Result.error(ResultCode.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return Result.error("on(系统异常，请联系管理员");
    }
    @ExceptionHandler(org.apache.dubbo.rpc.RpcException.class)
    public Result<?>handleDubboException(RpcException e){
        log.error("dubbo异常：{}", e.getMessage(), e);
        return Result.error("系统异常，请联系管理员");
    }
}