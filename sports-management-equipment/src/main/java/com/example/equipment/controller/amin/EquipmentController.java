package com.example.equipment.controller.amin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.AssessDamageRequest;
import com.example.equipment.dto.AssessDamageResult;
import com.example.equipment.dto.EquipmentDTO;
import com.example.equipment.dto.utilDTO.EquipmentPageQuery;
import com.example.equipment.pojo.JudgeDamage;
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

        Long equipmentId = equipmentService.addEquipment(equipmentDTO, currentUser);

        return Result.success(equipmentId);
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

    @PutMapping("/judgeDamage")
    public Result judgeEquipmentDamage(@RequestBody JudgeDamage judgeDamage)
    {

        equipmentService.UpdateEquipmentDamage(judgeDamage);

        return Result.success();
    }

    /**
     * 评估器材损毁程度接口
     * 接收带有器材二维码和器材本身的图片Base64，识别二维码，调用AI评估损毁。
     * 需要用户登录 (假设通过拦截器或AOP获取UserConstant)
     * @param request 包含图片Base64的请求体
     * @param user 当前操作用户 (假设已注入)
     * @return 评估结果 (包含损毁程度等信息)
     */
    @PostMapping("/assessDamage")
    public Result<AssessDamageResult> assessDamage(@RequestBody AssessDamageRequest request, @Parameter(hidden = true) UserConstant user) {
        if (user == null || user.getUserId() == null) {
            return Result.error("用户未登录或用户信息无效。");
        }
        log.info("收到器材损毁评估请求，用户ID: {}", user.getUserId());

        AssessDamageResult result = equipmentService.assessDamage(request, user);

        if (result.isSuccess()) {
            return Result.success(result.getMessage(), result);
        } else {
            // 对于失败情况，根据结果中的 message 返回错误
            return Result.error(result.getMessage(), result); // 将结果对象也返回，前端可以根据 relative 字段判断具体错误类型
        }
    }

}
