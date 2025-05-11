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
@NoArgsConstructor
@AllArgsConstructor
@TableName("equipment_borrow_order") // 确保表名拼写正确
public class Order { // 类名可以保留 Order 或改为 EquipmentBorrowOrder

    // orderId -> order_id (自动映射)
    @TableId(type = IdType.ASSIGN_ID)
    private Long orderId; // 订单唯一标识

    // userId -> user_id (自动映射)
    private Long userId; // 下单用户ID

    // requestId -> request_id (自动映射)
    private Long requestId; // 关联的借用请求ID

    // startTime -> request_start_time
    @TableField("request_start_time")
    private LocalDateTime requestStartTime; // 请求的借用开始时间

    // endTime -> request_end_time
    @TableField("request_end_time")
    private LocalDateTime requestEndTime; // 请求的借用结束时间

    // createTime -> create_time (自动映射)
    private LocalDateTime createTime; // 订单创建时间

    // borrowTime -> borrow_time (自动映射)
    private LocalDateTime borrowTime; // 实际借出时间

    // returnTime -> expected_return_time
    // 将 returnTime 改为 expectedReturnTime 以更清晰地对应数据库字段
    @TableField("expected_return_time")
    private LocalDateTime expectedReturnTime; // 预期归还时间

    // totalAmount -> total_amount (自动映射)
    // 使用 BigDecimal 更好
    private BigDecimal totalAmount; // 订单总金额

    // status -> order_status_id
    @TableField("order_status_id")
    private Integer orderStatusId; // 订单状态

    // updateTime -> update_time (自动映射)
    private LocalDateTime updateTime; // 最后更新时间

    // payTime, paidAmount, paymentMethod, transactionId, notes, isDeleted
    // 这些字段在新的 order 表结构中不存在，移除它们以保持同步。
    // 如果您确实需要在 order 表中记录这些信息，请修改数据库表结构。
}
