package com.example.event.DTO.vo;

import com.example.event.dao.Game;
import lombok.Data;

import java.time.format.DateTimeFormatter;

/**
 * 赛事VO对象
 */
@Data
public class GameVO {
    
    /**
     * 赛事ID
     */
    private Long gameId;
    
    /**
     * 赛事名称
     */
    private String name;
    
    /**
     * 体育项目
     */
    private String sport;
    
    /**
     * 负责人
     */
    private String responsiblePeople;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 报名开始时间
     */
    private String registerStartTime;
    
    /**
     * 报名结束时间
     */
    private String registerEndTime;
    
    /**
     * 赛事开始时间
     */
    private String startTime;
    
    /**
     * 赛事结束时间
     */
    private String endTime;
    
    /**
     * 备注信息
     */
    private String note;
    
    /**
     * 匹配模式
     */
    private Integer mode;
    public static GameVO convertFrom(Game game) {
        if (game == null) {
            return null;
        }

        GameVO vo = new GameVO();
        vo.setGameId(game.getGameId());
        vo.setName(game.getName());
        vo.setSport(game.getSport()); // 如果需要 sportId 可以从 game.getsportId()
        vo.setResponsiblePeople(game.getResponsiblePeople());
        vo.setPhone(game.getPhone());
        vo.setNote(game.getNote());
        vo.setMode(game.getMode());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (game.getRegisterStartTime() != null) {
            vo.setRegisterStartTime(game.getRegisterStartTime().format(formatter));
        }

        if (game.getRegisterEndTime() != null) {
            vo.setRegisterEndTime(game.getRegisterEndTime().format(formatter));
        }

        if (game.getStartTime() != null) {
            vo.setStartTime(game.getStartTime().format(formatter));
        }

        if (game.getEndTime() != null) {
            vo.setEndTime(game.getEndTime().format(formatter));
        }

        return vo;
    }
}