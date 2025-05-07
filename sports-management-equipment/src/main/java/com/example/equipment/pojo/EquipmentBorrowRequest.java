package com.example.equipment.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentBorrowRequest {
    /**
     *这是一个器材借用申请 的实体类
     */
    private Long requestId;   //申请表id

    private Long userId;     //用户Id

    private Long equipmentId;  //申请器材的Id

    private Integer quantity;    //申请器材 的数量

    private LocalDateTime startTime;   //借用开始时间

    private LocalDateTime endTime;   //借用结束时间

    private Integer status;    //提交申请后的状态   用0123分别表示‘审核中’、‘已通过’、‘已拒绝’、‘已归还’

    private LocalDateTime createTime;

    private LocalDateTime modifiedTime;

    private Integer isRevoked;    //是否撤销申请

    private Long cancellerId;
}
