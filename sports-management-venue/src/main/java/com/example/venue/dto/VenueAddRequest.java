package com.example.venue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * (管理员)前端发送到后端用于新增场地的请求体DTO
 */
@Data // 自动生成 Getter, Setter, equals, hashCode, toString 方法
@NoArgsConstructor // 自动生成无参构造函数 (通常是反序列化 JSON 所必需的)
@AllArgsConstructor // 自动生成包含所有字段的构造函数
public class VenueAddRequest {

    private String venueId;

    @NotBlank(message = "场地的赛事类型不能为空")
    private String sportId;

    @NotBlank(message = "场地的名称不能为空")
    private String name;

    @NotBlank(message = "场地位置不能为空")
    private String position;

    @NotNull(message = "场地价格不能为空")
    @Min(value = 0, message = "场地价格有误")
    private Double value;

    @NotNull(message = "场地的位置经度不能为空")
    private Double longitude; // ES中需映射为geo_point的lon部分

    @NotNull(message = "场地的位置纬度不能为空")
    private Double latitude;  // ES中需映射为geo_point的lat部分

    @NotNull(message = "场地的状态不能为空")
    @Range(min = 0, max = 1, message = "场地的状态只能是0或1")
    private Integer state;

    private String createdId;

    private String updatedId;
}
