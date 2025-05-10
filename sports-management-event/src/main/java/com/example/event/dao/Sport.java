package com.example.event.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 体育项目实体类
 */
@Data
@TableName("sport")
public class Sport {

    /**
     * 体育项目ID
     */
    @TableId(value = "sport_id", type = IdType.AUTO)
    private Long sportId;

    /**
     * 体育项目名称
     */
    @TableField("name")
    private String name;
}