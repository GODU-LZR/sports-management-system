package com.example.venue.exception; // 确保在主启动类的子包下

import com.example.venue.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class VenueExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException ex) {
        // 从异常中提取第一个字段错误信息
        String errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("参数有误,请填写正确参数!");
        log.error(errorMsg);
        return Result.validateError(errorMsg); // 返回具体的校验错误信息
    }

    // 2. 拦截其他所有异常（返回统一错误格式）
    @ExceptionHandler(Exception.class)
    public Result<?> handleOtherExceptions(Exception ex) {
        log.error("系统异常: ", ex); // 打印异常堆栈（实际项目需接入日志系统）
        return Result.error("系统发生内部错误,请重试!");
    }
}

