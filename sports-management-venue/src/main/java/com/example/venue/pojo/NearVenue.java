package com.example.venue.pojo;

import lombok.Data;

@Data
public class NearVenue {
    private String venueId; // 场地id
    private String name; // 场地名称
    private String position; // 场地位置描述
    private String location; // 地理位置"31.03463, 121.61245"
    private Integer value; // 价格

    // 添加一个字段来存储计算出的距离
    private Double calculatedDistance; // 计算出的距离 (单位与排序单位一致)

}
