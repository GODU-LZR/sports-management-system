package com.example.equipment.vo;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("equipment")
public class EquipmentVO {

    private Long createId;  //创建者Id

    private Long modifiedId;  //修改者id

    private Long equipmentId; //器材Id

    private String categoryId;  //器材分类

    private String pictureUrl;   // 器材图片


  private String specification;

  private Integer status;

    private Integer isDeleted;   //是否已删除

    private LocalDateTime createTime;   //创建时间

    private LocalDateTime modifiedTime;   //修改时间


}
