package com.example.venue.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 场地选项
 */
@Data
@NoArgsConstructor // 自动生成无参构造函数 (通常是反序列化 JSON 所必需的)
@AllArgsConstructor // 自动生成包含所有字段的构造函数
public class VenueOption {

    // 场地编号
    private String venueId;

    // 场地名称
    private String name;
}
