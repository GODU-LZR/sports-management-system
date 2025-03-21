package com.example.user.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 用户ID（雪花算法生成）
     */
    private Long id;

    /**
     * 用户编号（系统生成，唯一）
     */
    private String userCode;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 邮箱（唯一）
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 账号状态：0-正常，1-封禁15天，2-封禁30天，3-永久封禁
     */
    private Integer status;

    /**
     * 封禁结束时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime banEndTime;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer isDeleted;
}
