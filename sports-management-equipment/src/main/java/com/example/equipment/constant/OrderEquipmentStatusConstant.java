package com.example.equipment.constant;

// 订单明细项中具体器材的状态常量
public enum OrderEquipmentStatusConstant {

    UNUSED(1, "未使用"), // 订单明细创建后的初始状态
    PENDING_BORROW(2, "待借出"), // 已分配给订单，等待用户扫码领取
    ILLEGAL_PICKUP(3, "非法领取"), // 用户尝试扫码领取，但状态或权限不符
    BORROWED(4, "已借出"), // 用户已扫码领取该器材
    RETURNED(5, "已归还"), // 器材已完成检查，标记为已归还
    CANCELLED(6, "已取消"), // 该明细项被取消 (例如：订单取消导致明细项取消)
    LOST(7, "丢失"), // 器材丢失
    DAMAGED(8, "损坏"); // 归还时发现损坏，或使用过程中损坏


    private final Integer id;
    private final String description;

    OrderEquipmentStatusConstant(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    // 修正：这里应该返回 Integer 类型
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
        return null;
    }

    // 根据ID获取描述 (方便在 Service 中使用)
    public static String getDescriptionById(Integer id) {
        OrderEquipmentStatusConstant status = getById(id);
        return status != null ? status.getDescription() : "未知状态";
    }
}
