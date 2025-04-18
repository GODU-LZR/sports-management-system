package com.example.equipment.controller.amin;


import com.example.common.response.Result;
import com.example.equipment.dto.EquipmentDTO;
import com.example.equipment.service.impl.EquipmentServiceImpl;
import com.example.equipment.vo.EquipmentVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@Slf4j
public class EquipmentController {

    @Autowired
    private EquipmentServiceImpl equipmentService;

    /**
     * 新增器材
     * @param equipmentDTO
     * @return
     */
    @PostMapping("/addEquipment")
    @Operation(summary = "添加器材")
    @PreAuthorize("hasRole('ADMIN')")
    public Result addEquipment(@RequestBody EquipmentDTO equipmentDTO)
    {
        log.info("前端接收到的器材数据为:{}",equipmentDTO);

        equipmentService.addEquipment(equipmentDTO);

        return Result.success();
    }

    /**
     * 分页查询获取器材列表
     * @return
     */
    @GetMapping("/getEquipment")
    public Result  getEquipment( @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize)
    {


        return Result.success();
    }


    /**
     * 修改器材信息
     * @param equipmentDTO
     * @return
     */
    public Result updateEquipment(@RequestBody EquipmentDTO equipmentDTO)
    {
        log.info("修改器材的信息为:{}",equipmentDTO);

        equipmentService.updateEquipment(equipmentDTO);

        return Result.success();
    }


    /**
     * 根据器材id删除
     * @param EquipmentId
     * @return
     */
    @DeleteMapping("/deleteEquipment")
    public Result deleteEquipment(@PathVariable Long EquipmentId){

        equipmentService.deleteEquipment(EquipmentId);

        return Result.success();
    }

}
