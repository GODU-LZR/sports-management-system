package com.example.event.Enum;

import com.baomidou.mybatisplus.annotation.EnumValue; // 1. 导入 @EnumValue 注解
import com.baomidou.mybatisplus.annotation.IEnum;

public enum GameRole implements IEnum<String> { // IEnum<String> 及其 getValue() 仍可用于其他目的
    PLAYER(1, "球员", true, false),
    COACH(2, "教练", true, true),
    REFEREE(3, "裁判", false, true),
    SUBSTITUTE(4, "替补", true, false),
    MANAGER(5, "经理", false, true),
    VOLUNTEER(6, "志愿者", false, false),
    SPECTATOR(7, "观众", false, false);

    @EnumValue // 2. 在 code 字段上添加 @EnumValue 注解
    private final int code;
    private final String description;
    private final boolean isParticipant;
    private final boolean canManage;

    GameRole(int code, String description, boolean isParticipant, boolean canManage) {
        this.code = code;
        this.description = description;
        this.isParticipant = isParticipant;
        this.canManage = canManage;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isParticipant() {
        return isParticipant;
    }

    public boolean canManage() {
        return canManage;
    }

    public static GameRole getByCode(int code) {
        for (GameRole role : values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        // 修正了原代码中的 "MatchRole" 为 "GameRole"
        throw new IllegalArgumentException("Invalid GameRole code: " + code);
    }

    @Override
    public String getValue() {
        // 当 GameRole 类型的字段被持久化时，@EnumValue 会优先于 IEnum<String> 的 getValue()
        // 此 getValue() 仍然可以用于其他需要获取 description 的场景
        return description;
    }
}