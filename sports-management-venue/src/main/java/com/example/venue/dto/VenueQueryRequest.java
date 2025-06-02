package com.example.venue.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * 前端发送到后端用于查询场地列表的请求体DTO
 */
@Data // 自动生成 Getter, Setter, equals, hashCode, toString 方法
@NoArgsConstructor // 自动生成无参构造函数 (通常是反序列化 JSON 所必需的)
@AllArgsConstructor // 自动生成包含所有字段的构造函数
@Builder
public class VenueQueryRequest {

    /**
     * 页码:从1开始
     */
    @Min(value = 1, message = "请填写正确的页码")
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    private Integer page = 1;

    /**
     * 场地ID
     */
    private String venueId;

    /**
     * 场地描述 (模糊查询用)
     */
    @Size(max = 50, message = "场地位置描述长度过长") // 示例：添加长度验证
    private String position;

    /**
     * 状态 (数字类型)
     */
    private Integer state;

    public void modifyPage(){
        this.page = (this.page - 1) * 10;
    }
}

