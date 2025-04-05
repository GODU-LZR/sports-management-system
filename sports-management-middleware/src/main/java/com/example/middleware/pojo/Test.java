package com.example.middleware.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Test  {
    // 用户ID，默认为null表示未分配ID
    private Long id = null;
    // 用户名，默认为"未命名用户"
    private String username = "未命名用户";
    // 密码，默认为空字符串
    private String password = "xxxxx";
    // 邮箱，默认为空字符串
    private String email = "xxxx";
    // 头像URL，默认使用系统默认头像
    private String avatarUrl = "/default/avatar.png";
    // 删除标志，0表示未删除
    private Integer isDeleted = 0;

    // 创建时间，默认为当前时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime = LocalDateTime.now();

    // 修改时间，默认为当前时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedTime = LocalDateTime.now();
    
    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", isDeleted=" + isDeleted +
                ", createTime=" + createTime +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}
