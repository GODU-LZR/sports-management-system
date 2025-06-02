package com.example.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * (管理员)前端发送到后端用于修改场地的请求体DTO
 */
@Data // 自动生成 Getter, Setter, equals, hashCode, toString 方法
@NoArgsConstructor // 自动生成无参构造函数 (通常是反序列化 JSON 所必需的)
@AllArgsConstructor // 自动生成包含所有字段的构造函数
public class VenueUpdateRequest {

    // 场地编号
    @NotBlank(message = "场地编号不能为空")
    private String venueId;

    // 场地类型
    @NotBlank(message = "场地的赛事类型不能为空")
    private String sportId;

    // 场地名称
    @NotBlank(message = "场地的名称不能为空")
    private String name;

    // 场地位置
    @NotBlank(message = "场地位置不能为空")
    private String position;

    // 场地价格
    @NotNull(message = "场地价格不能为空")
    @Min(value = 0, message = "场地价格有误")
    private Double value;

    // 经度
    @NotNull(message = "场地的位置经度不能为空")
    private Double longitude;

    // 纬度
    @NotNull(message = "场地的位置纬度不能为空")
    private Double latitude;

    // 场地状态(0:已下架, 1:可租借)
    @NotNull(message = "场地的状态不能为空")
    @Range(min = 0, max = 1, message = "场地的状态值有误")
    private Integer state;

    // 管理员修改者的用户编号
    private String updatedId;
}
