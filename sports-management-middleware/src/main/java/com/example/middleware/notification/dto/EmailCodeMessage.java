package com.example.middleware.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailCodeMessage implements Serializable { // 实现 Serializable 接口是个好习惯
    private static final long serialVersionUID = 1L; // 序列化版本号

    private String email;
    private String code;
}