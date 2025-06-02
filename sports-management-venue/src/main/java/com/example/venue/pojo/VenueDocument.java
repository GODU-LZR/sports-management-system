package com.example.venue.pojo; // 或者 com.example.venue.document; 根据你的项目结构

import lombok.Data;
// 如果你的 location 字段在 POJO 中是分开的经纬度，可以这样定义
// import com.fasterxml.jackson.annotation.JsonProperty; // 如果需要指定 JSON 字段名

@Data
public class VenueDocument {
    private String venueId; // 对应映射中的 venueId (keyword)
    private String sport;   // 对应映射中的 sport (keyword)
    private String name;    // 对应映射中的 name (text/keyword)
    private String position;
    private float value;    // 对应映射中的 value (float)
    private Integer state; // 场地状态:0-可租借, 1-已下架

    private String location; // 对应映射中的 location (geo_point)，格式 "纬度,经度"

    public VenueDocument(String venueId, String name, String position, float value, Integer state, String sport, String location) {
        this.venueId = venueId;
        this.name = name;
        this.position = position;
        this.value = value;
        this.state = state;
        this.sport = sport;
        this.location = location;
    }
}
