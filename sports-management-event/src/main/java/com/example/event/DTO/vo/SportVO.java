package com.example.event.DTO.vo;

import lombok.Data;

import java.util.List;

/**
 * 体育项目VO对象
 */
@Data
public class SportVO {
    
    /**
     * 体育项目ID
     */
    private Long sportId;
    
    /**
     * 体育项目名称
     */
    private String name;
    
    /**
     * 赛事列表
     */
    private List<GameVO> games;
}