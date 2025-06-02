package com.example.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReasonRequest {

    @NotBlank(message = "订单编号不能为空")
    private String orderId;

    @NotBlank(message = "否决/撤销原因不能为空")
    private String reason;

    // 审核人的用户id
    private String auditId;
}
