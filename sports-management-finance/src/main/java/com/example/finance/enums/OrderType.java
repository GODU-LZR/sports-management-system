package com.example.finance.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

/**
 * 订单类型枚举
 * 用于区分不同业务类型的订单
 */
@Getter
public enum OrderType implements IEnum<String> {
    
    VENUE_RENTAL(1, "场地租借"),
    EQUIPMENT_RENTAL(2, "器材租借"),
    EVENT_REGISTRATION(3, "赛事报名"),
    COURSE_ENROLLMENT(4, "教学课程"),
    MIXED(5, "混合订单"); // 包含多种商品类型的订单
    
    private final int code;
    private final String description;
    
    OrderType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据code获取订单类型
     * @param code 类型代码
     * @return 订单类型枚举
     */
    public static OrderType fromCode(int code) {
        for (OrderType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的订单类型代码: " + code);
    }

    @Override
    public String getValue() {
        return description;
    }
}