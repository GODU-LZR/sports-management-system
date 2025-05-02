package com.example.equipment.controller.amin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.EquipmentDTO;
import com.example.equipment.dto.utilDTO.EquipmentPageQuery;
import com.example.equipment.service.impl.EquipmentServiceImpl;
import com.example.equipment.vo.EquipmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result addEquipment(@RequestBody EquipmentDTO equipmentDTO,@Parameter(hidden = true) UserConstant currentUser)
    {
        log.info("前端接收到的器材数据为:{}",equipmentDTO);

        equipmentService.addEquipment(equipmentDTO,currentUser);

        return Result.success();
    }

    /**
     * 分页查询获取器材列表
     * @return
     */
    @GetMapping("/getEquipment")
    public Result<IPage<EquipmentVO>> getUsers(EquipmentPageQuery query)
    {

            // 将 DTO 直接传递给 Service 层
            IPage<EquipmentVO> pageResult = equipmentService.PageSelect(query);
            // 使用你的 Result 类封装成功结果
            return Result.success(pageResult);

            // 捕获异常，并使用你的 GlobalExceptionHandler 或手动记录日志
            // 返回失败的 Result
            // 这里的错误信息可以更友好，或者使用枚举状态码
            // 例如：return Result.error(ResultCodeEnum.SYSTEM_ERROR);
//            return Result.error("Failed");

    }


    /**
     * 修改器材信息
     * @param equipmentDTO
     * @return
     */
    @PutMapping("/updateEquipment")
    public Result updateEquipment(@RequestBody EquipmentDTO equipmentDTO,@Parameter(hidden = true) UserConstant currentUser)
    {
        log.info("修改器材的信息为:{}",equipmentDTO);

        equipmentService.updateEquipment(equipmentDTO,currentUser);

        return Result.success();
    }


    /**
     * 根据器材id删除
     * @param equipmentid
     * @return
     */
    @DeleteMapping("/deleteEquipment")
    public Result deleteEquipment(@PathVariable Long equipmentid,@Parameter(hidden = true) UserConstant currentUser){

        equipmentService.deleteEquipment(equipmentid,currentUser);

        return Result.success();
    }

}
