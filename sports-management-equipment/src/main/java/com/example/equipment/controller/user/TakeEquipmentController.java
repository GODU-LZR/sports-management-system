package com.example.equipment.controller.user;


import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.LocaleBorrowDTO;
import com.example.equipment.dto.UserOperateEquipmentDTO;
import com.example.equipment.service.impl.TakeEquipmentServiceImpl;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class TakeEquipmentController {


    @Autowired
    TakeEquipmentServiceImpl takeEquipmentService;


    @PutMapping("/takeEquipment")
    public Result TakeEquipment(UserOperateEquipmentDTO takeEquipmentDTO, @Parameter(hidden = true) UserConstant currentUser)
    {

        try{

//            takeEquipmentService.TakeEquipment(takeEquipmentDTO,currentUser);

        }catch (IllegalArgumentException e)
        {
            return Result.error(e.getMessage());
        }

        return Result.success();
    }


    @PutMapping("/OutboundReserveEquipment")
    public Result OutboundEquipment(Long equipmentId,@Parameter(hidden = true) UserConstant currentUser)
    {

        try{

            takeEquipmentService.OutboundReserveEquipment(equipmentId,currentUser);

        }catch (IllegalArgumentException e)
        {
            return Result.error(e.getMessage());
        }

        return Result.success();
    }

    @PutMapping("localeBorrowEquipment")
    public Result localeOutboundEquipment(@RequestBody LocaleBorrowDTO borrowDTO,@Parameter(hidden = true) UserConstant currentUser)
    {

        try
        {
            takeEquipmentService.LocaleOutboundEquipment(borrowDTO,currentUser);
        }catch (IllegalArgumentException e)
        {
            return Result.error(e.getMessage());
        }


        return Result.success();
    }


    @PutMapping("/returnEquipment")
    public Result ReturnEquipment(Long equipmentId,@Parameter(hidden = true) UserConstant currentUser){

        takeEquipmentService.returnEquipment(equipmentId,currentUser);

        return Result.success();
    }





}
