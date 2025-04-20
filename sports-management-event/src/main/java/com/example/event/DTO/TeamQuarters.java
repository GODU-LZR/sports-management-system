package com.example.event.DTO;

/**
 * 单队分节得分
 */
public class TeamQuarters {
    public String team;
    public int one;
    public int two;
    public int three;
    public int four;

    public TeamQuarters(String team, int one, int two, int three, int four) {
        this.team = team;
        this.one = one;
        this.two = two;
        this.three = three;
        this.four = four;
    }
}
