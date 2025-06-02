package com.example.venue.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor // 生成一个 protected 的无参构造函数,用于序列化等
@AllArgsConstructor // 生成一个 private 的全参构造函数,同于静态工厂方法
public class ReplaceVenue{

    // 场地编号
    private String venueId;

    // 场地名称
    private String name;

    // 场地价格
    private Integer value;

    // 场地使用时间
    private LocalDateTime startTime;

    // 场地结束时间
    private LocalDateTime endTime;
}
