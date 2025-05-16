package com.example.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.equipment.dto.AssessDamageRequest;
import com.example.equipment.dto.AssessDamageResult;
import com.example.equipment.dto.EquipmentDTO;
import com.example.equipment.dto.ScanRequest;
import com.example.equipment.dto.utilDTO.EquipmentPageQuery;
import com.example.equipment.pojo.JudgeDamage;
import com.example.equipment.vo.EquipmentVO;

public interface EquipmentService {
    Long addEquipment(EquipmentDTO equipmentDTO, UserConstant userConstant);

    void updateEquipment(EquipmentDTO equipmentDTO,UserConstant userConstant);

    void deleteEquipment(Long equipmentId,UserConstant userConstant);

    IPage<EquipmentVO> PageSelect(EquipmentPageQuery query);

    void UpdateEquipmentDamage(JudgeDamage judgeDamage);

    /**
     * 评估器材损毁程度，包括二维码识别和AI图片对比
     * @param request 包含用户上传图片Base64的请求DTO
     * @param user 当前操作用户
     * @return 损毁评估结果
     */
    AssessDamageResult assessDamage(AssessDamageRequest request, UserConstant user);

    /**
     * 现场扫码借取或归还器材
     * @param request 包含器材二维码图片的Base64字符串
     * @param user 当前操作用户
     * @return 操作结果消息 (借取成功/归还成功/失败原因)
     */
    Result<String> scanAndHandle(ScanRequest request, UserConstant user);
}
