package com.example.event.Enum;

public enum GameRole {
    PLAYER(1, "球员", true, false), // isParticipant, canManage
    COACH(2, "教练", true, true),
    REFEREE(3, "裁判", false, true),
    SUBSTITUTE(4, "替补", true, false),
    MANAGER(5, "经理", false, true),
    VOLUNTEER(6, "志愿者", false, false),
    SPECTATOR(7, "观众", false, false);

    private final int code;
    private final String description;
    private final boolean isParticipant; // 是否是参与比赛的人员
    private final boolean canManage;     // 是否具有管理权限

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
        throw new IllegalArgumentException("Invalid MatchRole code: " + code);
    }
}
