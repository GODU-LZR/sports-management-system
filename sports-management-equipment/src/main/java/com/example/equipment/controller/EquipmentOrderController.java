package com.example.equipment.controller;


import com.example.common.response.Result;
import com.example.equipment.dto.EquipmentDTO;
import com.example.equipment.service.impl.EquipmentOrderServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentOrderController {

//    @Autowired
    private EquipmentOrderServiceImpl equipmentOrderService;
    /**
     * 新建一个器材订单
     */
    public Result createEquipmentOrder(@RequestBody EquipmentDTO equipmentDTO)
    {


        return Result.success();
    }
}
