package com.example.venue.pojo.mysql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime; // 推荐使用 Java 8+ 的日期时间 API

/**
 * 场地信息 POJO 类
 * 对应数据库中的 venue 表
 */
@Data
@NoArgsConstructor // 自动生成无参构造函数
@AllArgsConstructor // 自动生成包含所有字段的构造函数
public class Venue {

    // 场地编号
    private String venueId;

    // 图片路径
    private String url;

    // 场地类型
    private String sportId;

    // 场地类型名称
    private String sportName;

    // 场地名称
    private String name;

    // 场地位置
    private String position;

    // 场地价格
    private Double value;

    // 经度
    private Double longitude;

    // 纬度
    private Double latitude;

    // 场地状态(0:已下架, 1:可租借)
    private Integer state;

    // 创建时间
    private LocalDateTime createdTime;

    // 创建者的用户id
    private String createdId;

    // 更新时间
    private LocalDateTime updatedTime;

    // 创建者的用户id
    private String updatedId;

    // 删除状态(0:未删除, 1:已删除)
    private Integer isDeleted;
}