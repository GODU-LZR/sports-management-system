package com.example.venue.dto;

import lombok.Data;

@Data // 使用 Lombok 的 @Data 注解
public class VenueSearchRequest {

    // 场地类型
    private String sportId;

    // 初始价格,价格区间为[value, value+10]
    private Double value;

    // 最远距离
    private Double distance;

    // 经度
    private Double longitude;

    // 纬度
    private Double latitude;
}