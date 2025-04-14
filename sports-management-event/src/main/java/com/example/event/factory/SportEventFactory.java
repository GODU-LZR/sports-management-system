package com.example.event.factory;

import com.example.event.entity.BasketballEvent;

/**
 * 体育赛事工厂
 */
public class SportEventFactory {

    public static BasketballEvent createSportEvent(String sportType) {
        switch (sportType) {
            case "basketball":
                return new BasketballEvent();
//            case "football":
//                return new FootballEvent();
            // 其他运动项目
            default:
                throw new IllegalArgumentException("不支持的运动类型: " + sportType);
        }
    }
}