package com.example.venue.vo;

import lombok.AllArgsConstructor;
import lombok.Data; // 需要引入Lombok依赖
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

// 定义常用的业务状态码
class ResultCode {
    public static final Integer SUCCESS = 200;          // 成功
    public static final Integer FAILURE = 500;          // 通用失败（例如系统内部错误）
    public static final Integer VALIDATE_ERROR = 400;   // 参数校验失败
    public static final Integer UNAUTHORIZED = 401;     // 未认证（未登录）
    public static final Integer FORBIDDEN = 403;        // 无权限（已登录但无权访问）
    public static final Integer NOTFOUND = 404;        // 资源不存在
    // ... 可以根据业务需要添加更多状态码
}

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 生成一个 protected 的无参构造函数,用于序列化等
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 生成一个 private 的全参构造函数,同于静态工厂方法
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    // --- 静态工厂方法 ---

    /**
     * 返回成功结果 (状态码: 200)
     *
     * @param data 数据载荷
     * @param <T>  数据类型
     * @return 成功结果对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, "操作成功", data);
    }

    /**
     * 返回成功结果（无数据载荷） (状态码: 200)
     *
     * @param <T> 数据类型（通常为 Void）
     * @return 成功结果对象
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS, "操作成功", null);
    }

    /**
     * 返回通用失败结果（例如系统内部错误） (状态码: 500)
     *
     * @param message 失败消息
     * @param <T>     数据类型（通常为 Void）
     * @return 失败结果对象
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(ResultCode.FAILURE, message, null);
    }

    /**
     * 返回参数校验失败结果 (状态码: 400)
     *
     * @param message 失败消息 (通常是具体的校验错误信息)
     * @param <T>     数据类型（通常为 Void）
     * @return 失败结果对象
     */
    public static <T> Result<T> validateError(String message) {
        return new Result<>(ResultCode.VALIDATE_ERROR, message, null);
    }

    /**
     * 返回未认证/未登录结果 (状态码: 401)
     *
     * @param <T> 数据类型（通常为 Void）
     * @return 失败结果对象
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(ResultCode.UNAUTHORIZED, "请先登录", null);
    }

    /**
     * 返回无权限结果 (状态码: 403)
     *
     * @param <T> 数据类型（通常为 Void）
     * @return 失败结果对象
     */
    public static <T> Result<T> forbidden() {
        return new Result<>(ResultCode.FORBIDDEN, "您没有权限访问", null);
    }

    /**
     * 返回资源不存在结果 (状态码: 404)
     *
     * @param message 失败消息 (可选，可以更具体说明哪个资源不存在)
     * @param <T> 数据类型（通常为 Void）
     * @return 失败结果对象
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(ResultCode.NOTFOUND, message, null);
    }

    /**
     * 返回带自定义状态码和消息的失败结果
     * 用于上面未涵盖的特定业务错误码
     *
     * @param message 失败消息
     * @param <T>     数据类型（通常为 Void）
     * @return 失败结果对象
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.FAILURE, message, null);
    }
}
