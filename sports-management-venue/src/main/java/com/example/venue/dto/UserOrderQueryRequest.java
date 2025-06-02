package com.example.venue.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderQueryRequest {

    /**
     * 页码
     */
    @Min(value = 1, message = "请填写正确的页码")
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    private Integer page = 1;

    /**
     * 场地编号 (可为 null)
     */
    private String venueId;

    /**
     * 审核状态 (可为 null)
     * 0: 待审核, 1: 审核通过, 2: 审核拒绝, 3: 已取消, 4: 已完成
     */
    @Min(value = 0, message = "审核状态为无效值")
    @Max(value = 4, message = "审核状态为无效值")
    private Integer state;

    /**
     * 支付状态 (可为 null)
     * 0: 待支付, 1: 已支付
     */
    @Min(value = 0, message = "支付状态为无效值")
    @Max(value = 1, message = "支付状态为无效值")
    private Integer payState;

    private String userId;

    public void modifyPage(){
        this.page = (this.page - 1) * 10;
    }
}
