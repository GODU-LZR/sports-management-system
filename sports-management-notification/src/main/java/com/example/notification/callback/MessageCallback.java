package com.example.notification.callback;

/**
 * 消息处理回调接口
 */
public interface MessageCallback {
    /**
     * 处理接收到的消息
     * @param message 消息内容
     * @throws Exception 处理消息时可能抛出的异常
     */
    void onMessage(String message) throws Exception;
}