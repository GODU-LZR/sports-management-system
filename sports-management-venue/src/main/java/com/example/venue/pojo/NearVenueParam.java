package com.example.venue.pojo;

import lombok.Data;

@Data
public class NearVenueParam {
    private String key; // 搜索关键字
    private String location; // 当前的地理位置

    private Integer limit = 10; // 默认返回10条数据

    private Double distance; // 距离数值
    private String distanceUnit = "km";
}
