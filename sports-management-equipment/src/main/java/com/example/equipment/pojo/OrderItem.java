package com.example.equipment.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal; // Recommended for currency

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("equipment_borrow_detail") // 确保表名拼写正确
public class OrderItem { // 将类名改为更符合表名的 Detail 或 OrderDetail

    @TableId(value = "detail_id", type = IdType.ASSIGN_ID)
    private Long detailId; // 明细项唯一标识，对应 detail_id

    private Long orderId; // 关联的订单ID

    private Long equipmentId; // 关联的具体器材ID

    @TableField("item_status_id")
    private Integer itemStatusId; // 该具体器材在订单中的状态

    private LocalDateTime actualReturnTime; // 该器材实际归还时间

    private String damageInfo; // 损坏描述

    private BigDecimal itemAmount; // 单个器材的金额 (租金)

    // createTime, borrowTime, conditionUponBorrow, conditionUponReturn, updateTime, isDeleted
    // 这些字段在新的 detail 表结构中不存在，移除它们以保持同步。
    // 如果您确实需要在 detail 表中记录这些信息，请修改数据库表结构。

    // categoryId, equipmentName, quantity, unitPrice, durationHours
    // 这些字段通常不直接存储在 detail 表中，而是通过 equipment_id 关联查询获取，或在创建时计算。
    // 移除它们以保持与数据库表的同步。

}
