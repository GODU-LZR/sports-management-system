package com.example.venue.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderQueryRequest {

    // 页码
    @Min(value = 1, message = "请填写正确的页码")
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    private Integer page = 1;

    // 审核状态
    @Min(value = 0, message = "审核状态为无效值")
    @Max(value = 4, message = "审核状态为无效值")
    private Integer state;

    // 订单编号
    private String orderId;

    // 场地编号
    private String venueId;

    // 下单时间
    @Size(min = 2, message = "下单时间范围有误")
    private List<LocalDateTime> orderTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public void setOrderTime(List<LocalDateTime> orderTime) {
        this.orderTime = orderTime; // 需要前端传格式正确的字符串数组
    }

    public void modifyPage(){
        this.page = (this.page - 1) * 10;
    }
}
