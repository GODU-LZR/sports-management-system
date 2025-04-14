package com.example.event.template;

/**
 * 赛事管理模板
 */
public abstract class EventManagementTemplate {

    // 模板方法，定义了赛事管理的基本流程
    public final void manageEvent() {
        createEvent();
        registerTeamsOrPlayers();
        arrangeSchedule();
        conductMatches();
        recordResults();
        generateStatistics();
        awardCeremony();
    }

    // 以下方法可以由子类重写
    protected abstract void createEvent();

    protected abstract void registerTeamsOrPlayers();

    protected abstract void arrangeSchedule();

    protected void conductMatches() {
        // 默认实现
        System.out.println("比赛进行中...");
    }

    protected abstract void recordResults();

    protected abstract void generateStatistics();

    protected void awardCeremony() {
        // 默认实现
        System.out.println("颁奖仪式...");
    }
}

