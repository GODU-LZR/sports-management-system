package com.example.equipment.constant;

// 订单明细项中具体器材的状态常量
public enum OrderEquipmentStatusConstant {

    PENDING_BORROW(1, "待借出"), // 已分配给订单，等待用户领取
    BORROWED(2, "已借出"), // 用户已领取该器材
    RETURNED(3, "已归还"), // 用户已归还该器材
    DAMAGED(4, "损坏"), // 归还时发现损坏
    LOST(5, "丢失"), // 器材丢失
    CANCELLED(6, "已取消"); // 该明细项被取消 (例如：订单取消导致明细项取消)

    private final Integer id;
    private final String description;

    OrderEquipmentStatusConstant(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    // 根据ID获取枚举常量
    public static OrderEquipmentStatusConstant getById(Integer id) {
        if (id == null) {
            return null;
        }
        for (OrderEquipmentStatusConstant status : values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        // 可以选择返回 null 或抛出异常
        // throw new IllegalArgumentException("Invalid OrderEquipmentStatusConstant id: " + id);
        return null;
    }
}
