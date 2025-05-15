package com.example.common.response;

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

    // --- 成功的静态方法 ---
    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>()
                .setCode(ResultCode.SUCCESS.getCode()) // 使用枚举统一定义成功码
                .setMessage(ResultCode.SUCCESS.getMessage()) // 使用枚举统一定义成功消息
                .setData(data);
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode()); // 使用枚举统一定义成功码
        result.setMessage(message);
        result.setData(data);
        return result;
    }


    // --- 失败的静态方法 ---

    /**
     * 返回错误结果，使用默认错误码和自定义消息
     * @param message 自定义错误消息
     * @return Result<T>
     */
    public static <T> Result<T> error(String message) {
        return new Result<T>()
                .setCode(ResultCode.ERROR.getCode()) // 使用默认错误码
                .setMessage(message);
    }

    /**
     * 返回错误结果，使用指定的 IResultCode (包含 code 和 message)
     * @param resultCode 实现 IResultCode 接口的错误码枚举或对象
     * @return Result<T>
     */
    public static <T> Result<T> error(IResultCode resultCode) {
        return new Result<T>()
                .setCode(resultCode.getCode())
                .setMessage(resultCode.getMessage());
    }

    /**
     * 返回错误结果，使用指定的 code 和 message
     * @param code 自定义错误码 (Integer 类型)
     * @param message 自定义错误消息 (String 类型)
     * @return Result<T>
     */
    public static <T> Result<T> error(Integer code, String message) {
        if (code == null) {
            code = ResultCode.ERROR.getCode();
        }
        if (message == null) {
            message = "未提供具体的错误消息";
        }

        return new Result<T>()
                .setCode(code)
                .setMessage(message);
    }

    /**
     * 【新增】返回错误结果，使用默认错误码、自定义消息和数据
     * @param message 自定义错误消息
     * @param data 需要返回的数据 (例如包含 relative 标志的 AssessDamageResult)
     * @return Result<T>
     */
    public static <T> Result<T> error(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.ERROR.getCode()); // 使用默认错误码
        result.setMessage(message);
        result.setData(data); // 设置数据
        return result;
    }


    @Override
    public String toString() {
        try {
            // 每次都 new ObjectMapper 可能影响性能，考虑将其设为静态常量或通过依赖注入获取
            // private static final ObjectMapper objectMapper = new ObjectMapper();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this); // 正确序列化为 JSON
        } catch (JsonProcessingException e) {
            // 记录日志会更好
            // log.error("序列化 Result 对象失败", e);
            return super.toString(); // 序列化失败时回退到默认 toString
        }
    }
}
