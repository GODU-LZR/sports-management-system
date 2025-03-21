package com.example.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>()
                .setCode(ResultCode.SUCCESS.getCode())
                .setMessage(ResultCode.SUCCESS.getMessage())
                .setData(data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<T>()
                .setCode(ResultCode.ERROR.getCode())
                .setMessage(message);
    }

    public static <T> Result<T> error(IResultCode resultCode) {
        return new Result<T>()
                .setCode(resultCode.getCode())
                .setMessage(resultCode.getMessage());
    }

    @Override
    public String toString() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this); // 正确序列化为 JSON
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }
}