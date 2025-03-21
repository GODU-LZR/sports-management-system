package com.example.common.model;

/**
 * 结果状态码接口
 */
public interface IResultCode {
    /**
     * 获取状态码
     * @return 状态码
     */
    Integer getCode();

    /**
     * 获取状态信息
     * @return 状态信息
     */
    String getMessage();
}