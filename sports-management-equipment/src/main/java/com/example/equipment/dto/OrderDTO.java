package com.example.equipment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    /**
     * 用于创建订单 的实体类
     */

    private Integer payMent;

    private String equipmentName;  //器材名称

    private Integer rentalQuantity;  //租借数量

    private LocalDateTime returnTime;
}
