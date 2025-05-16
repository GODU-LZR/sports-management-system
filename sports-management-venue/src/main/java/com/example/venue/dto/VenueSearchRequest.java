package com.example.venue.dto;

import lombok.Data;

@Data // 使用 Lombok 的 @Data 注解
public class VenueSearchRequest {

    private String key; // 搜索关键字
    private String location; // 当前的地理位置，格式为 "纬度, 经度"，例如 "31.03463, 121.61245"

    // @Data 注解会自动生成 getter, setter, equals, hashCode, toString 方法，无需手动编写
}