package com.example.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDTO {

    private Long requestId;  //请求Id

    private Integer quantity;  //审核的数量

    private Integer status;   //前端发送过来的 状态

    private Long userId;    //Service层可以填入当前用户Id


}
