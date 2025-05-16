package com.example.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.equipment.dto.*;
import com.example.equipment.dto.utilDTO.EquipmentPageQuery;
import com.example.equipment.mapper.CategoryMapper;
import com.example.equipment.mapper.EquipmentMapper;
import com.example.equipment.pojo.Equipment;
import com.example.equipment.pojo.JudgeDamage;
import com.example.equipment.service.EquipmentService;
import com.example.equipment.vo.EquipmentVO;
import com.google.zxing.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.random;


@Service
@Slf4j
public class EquipmentServiceImpl  extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private QrCodeService qrCodeService; // 注入QrCodeService

    @Autowired
    private AiService aiService; // 注入AiService


    /**
     * 新增器材方法
     * @param equipmentDTO
     */
    @Override
    public Long addEquipment(EquipmentDTO equipmentDTO, UserConstant user) {

        Long equipmentId = snowflakeIdGenerator.nextId();

        Equipment equipment = new Equipment();
        //拷贝属性
        BeanUtils.copyProperties(equipmentDTO,equipment);

        equipment.setEquipmentId(equipmentId);  //生成雪花id

        equipment.setCreateTime(LocalDateTime.now());

        equipment.setCreateId(user.getUserId());  //获取当前用户Id

        log.info("Service层添加器材的信息:{}",equipment);


        equipmentMapper.addEquipment(equipment);

        categoryMapper.AddTotal(equipment.getCategoryId());

        return equipmentId;

    }

    /**
     * 修改器材信息1
     * @param equipmentDTO
     */
    @Override
    public void updateEquipment(EquipmentDTO equipmentDTO,UserConstant userConstant) {

        Equipment equipment = new Equipment();

        BeanUtils.copyProperties(equipmentDTO,equipment);

        //更新修改时间
        equipment.setModifiedTime(LocalDateTime.now());

        //设置修改用户Id
        equipment.setModifiedId(userConstant.getUserId());

        equipmentMapper.updateEquipment(equipment);

    }


    /**
     * 根据Id删除器材
     * @param equipmentId
     */
    @Override
    public void deleteEquipment(Long equipmentId,UserConstant userConstant) {

        Long userId = userConstant.getUserId();
        if (equipmentId !=null){
//          Equipment equipment = equipmentMapper.find_IS_used(equipmentId);

            equipmentMapper.delete(equipmentId,userId);

//            categoryMapper.reduceEquipment(equipmentId);

        }

    }

    /**
     * 分页查询器材列表 (返回 EquipmentVO)
     * 直接调用 Mapper 中自定义的 selectEquipmentVOPage 方法
     * @param query 查询条件和分页信息
     * @return 分页结果 IPage<EquipmentVO>
     */
    @Override
    public IPage<EquipmentVO> PageSelect(EquipmentPageQuery query) {

        // 1. 创建分页对象
        // MyBatis-Plus 会根据这个对象中的 current 和 size 进行分页
        // 泛型参数 EquipmentVO 匹配了 Mapper 方法的返回类型 IPage<EquipmentVO>
        IPage<EquipmentVO> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 2. 直接调用 EquipmentMapper 中自定义的 selectEquipmentVOPage 方法
        // 这个方法已经在 Mapper XML 或 @SelectProvider 中写好了 JOIN 和 WHERE 条件逻辑
        // 它接收 IPage<EquipmentVO> 和 EquipmentPageQuery DTO
        // Mapper 方法会负责根据 query DTO 中的条件（如 specification）构建 WHERE 子句
        log.info("Service层执行分页查询，查询条件:{}", query);
        IPage<EquipmentVO> resultPage = equipmentMapper.selectEquipmentVOPage(page, query);

        log.info("Service层分页查询结果:{}", resultPage.getRecords().size()); // 记录查询到的条数
        return resultPage;
    }

    @Override
    public void UpdateEquipmentDamage(JudgeDamage judgeDamage) {

        equipmentMapper.updateEquipmentCondition(judgeDamage);

    }

    /**
     * 评估器材损毁程度，包括二维码识别和AI图片对比
     * @param request 包含用户上传图片Base64的请求DTO
     * @param user 当前操作用户
     * @return 损毁评估结果
     */
    @Override
    @Transactional // 确保更新数据库在事务中
    public AssessDamageResult assessDamage(AssessDamageRequest request, UserConstant user) {
        AssessDamageResult result = new AssessDamageResult();
        result.setSuccess(false); // 默认失败

        String base64Image = request.getBase64Image();
        if (base64Image == null || base64Image.trim().isEmpty()) {
            result.setMessage("上传的图片数据为空。");
            return result;
        }

        byte[] imageBytes;
        try {
            // 解码Base64字符串
            imageBytes = Base64.getDecoder().decode(base64Image);
            if (imageBytes.length == 0) {
                result.setMessage("解码后的图片数据为空。");
                return result;
            }
        } catch (IllegalArgumentException e) {
            log.error("Base64图片解码失败: {}", e.getMessage());
            result.setMessage("无效的图片数据格式。");
            return result;
        }


        String qrContent = null;
        Long equipmentId = null;

        // 1. 二维码识别
        try {
            qrContent = qrCodeService.decodeQrCode(imageBytes);
            log.info("二维码解码内容: {}", qrContent);

            // 从二维码内容中提取 equipmentId
            // 假设二维码内容格式为: baseUrl?equipmentId=XYZ 或 baseUrl&equipmentId=XYZ
            if (qrContent != null && !qrContent.trim().isEmpty()) {
                String idParam = "equipmentId=";
                int startIndex = qrContent.indexOf(idParam);
                if (startIndex != -1) {
                    String idString = qrContent.substring(startIndex + idParam.length());
                    // 提取到第一个 & 或 # 或末尾
                    int endIndex = idString.indexOf('&');
                    if (endIndex == -1) endIndex = idString.indexOf('#');
                    if (endIndex != -1) {
                        idString = idString.substring(0, endIndex);
                    }
                    try {
                        equipmentId = Long.parseLong(idString);
                        result.setEquipmentId(equipmentId); // 设置结果中的器材ID
                        log.info("从二维码内容中提取到器材ID: {}", equipmentId);
                    } catch (NumberFormatException e) {
                        log.error("从二维码内容 '{}' 提取的ID '{}' 不是有效的数字: {}", qrContent, idString, e.getMessage());
                        result.setMessage("二维码内容格式错误，无法提取器材ID。");
                        return result;
                    }
                } else {
                    log.warn("二维码内容 '{}' 中未找到 'equipmentId=' 参数。", qrContent);
                    result.setMessage("二维码内容格式错误，未找到器材ID信息。");
                    return result;
                }
            } else {
                result.setMessage("未能识别二维码内容。请确保图片清晰且二维码完整。");
                return result;
            }

        } catch (NotFoundException e) {
            log.warn("二维码识别失败: {}", e.getMessage());
            result.setMessage("未能识别二维码。请确保图片清晰且二维码完整。");
            return result;
        } catch (IOException e) {
            log.error("读取图片或解码二维码时发生IO错误: {}", e.getMessage());
            result.setMessage("处理图片时发生错误。");
            return result;
        } catch (Exception e) {
            log.error("二维码识别过程中发生未知错误: {}", e.getMessage());
            result.setMessage("二维码识别过程中发生未知错误。");
            return result;
        }

        if (equipmentId == null) {
            // Should not happen if previous steps are correct, but as a safeguard
            result.setMessage("未能获取有效的器材ID。");
            return result;
        }


        // 2. 查询入库图片URL
        String originalImageUrl = equipmentMapper.getPictureUrlById(equipmentId);
        if (originalImageUrl == null || originalImageUrl.trim().isEmpty()) {
            log.warn("器材ID {} 未找到对应的入库图片URL。", equipmentId);
            result.setMessage("未找到该器材的入库图片信息，无法进行损毁评估。");
            return result;
        }
        log.info("查询到器材ID {} 的入库图片URL: {}", equipmentId, originalImageUrl);

        // 3. 调用AI模型进行评估
        AiDamageResponse aiResponse;
        try {
            // 将用户上传的图片Base64和入库图片URL传递给AI服务
            aiResponse = aiService.callAiForDamageAssessment(base64Image, originalImageUrl);
            log.info("AI评估结果: relative={}, conditionScore={}, description={}",
                    aiResponse.getRelative(), aiResponse.getConditionScore(), aiResponse.getDescription());

        } catch (IOException e) {
            log.error("调用AI服务失败: {}", e.getMessage());
            result.setMessage("调用AI服务失败，请稍后重试。");
            return result;
        } catch (RuntimeException e) {
            log.error("AI服务返回非预期结果或解析失败: {}", e.getMessage());
            result.setMessage("AI服务返回结果异常，请联系管理员。");
            return result;
        } catch (Exception e) {
            log.error("调用AI服务发生未知错误: {}", e.getMessage());
            result.setMessage("调用AI服务发生未知错误。");
            return result;
        }


        // 4. 处理AI评估结果
        if (aiResponse.getRelative() == null || aiResponse.getRelative() == 0) {
            // AI判断图片与原始图片不相关
            result.setMessage("上传的图片与器材不符。请确保拍摄的是带有二维码的器材本身。");
            result.setRelative(0); // 明确设置相关性标志
            // 不更新数据库
            return result;
        } else {
            // AI判断图片相关
            result.setRelative(1); // 明确设置相关性标志
            Integer conditionScore = aiResponse.getConditionScore();
            String damageDescription = aiResponse.getDescription();

            // 5. 更新器材损毁程度
            if (conditionScore != null) {
                JudgeDamage judgeDamage = new JudgeDamage();
                judgeDamage.setEquipmentId(equipmentId);
                // Clamp score to be safe, although AI service already attempts this
                judgeDamage.setConditionScore(Math.max(0, Math.min(100, conditionScore)));

                try {
                    equipmentMapper.updateEquipmentCondition(judgeDamage);
                    log.info("器材ID {} 的损毁程度已更新为 {}", equipmentId, judgeDamage.getConditionScore());

                    result.setSuccess(true);
                    result.setConditionScore(judgeDamage.getConditionScore());
                    result.setDamageDescription(damageDescription); // 包含AI的描述
                    result.setMessage("器材损毁评估完成，损毁程度已更新。");

                } catch (Exception e) {
                    log.error("更新器材损毁程度失败 (ID: {}): {}", equipmentId, e.getMessage());
                    result.setMessage("更新器材损毁程度失败。");
                    // Keep success as false
                }
            } else {
                // Should not happen if AI response parsing is correct, but as a safeguard
                result.setMessage("AI评估结果中未包含有效的损毁程度。");
                // Keep success as false
            }
            return result;
        }
    }

    /**
     * 现场扫码借取/归还器材接口实现
     *
     * @param request 包含器材二维码图片的Base64字符串
     * @param user    当前操作用户
     * @return 操作结果消息 (借取成功/归还成功/失败原因)
     */
    @Override
    @Transactional // 确保数据库更新的原子性
    public Result<String> scanAndHandle(ScanRequest request, UserConstant user) {
        log.info("收到扫码借取/归还请求，用户ID: {}", user.getUserId());

        if (user == null || user.getUserId() == null) {
            return Result.error("用户未登录或用户信息无效。");
        }

        String base64Image = request.getBase64Image();
        if (base64Image == null || base64Image.trim().isEmpty()) {
            return Result.error("上传的图片数据为空。");
        }

        byte[] imageBytes;
        try {
            // 解码Base64字符串，处理可能的Data URL前缀
            if (base64Image.contains(",")) {
                base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
            }
            imageBytes = Base64.getDecoder().decode(base64Image);
            if (imageBytes.length == 0) {
                return Result.error("解码后的图片数据为空。");
            }
        } catch (IllegalArgumentException e) {
            log.error("扫码借还 - Base64图片解码失败: {}", e.getMessage());
            return Result.error("无效的图片数据格式。");
        }

        String qrContent = null;
        Long equipmentId = null;

        // 1. 二维码识别
        try {
            qrContent = qrCodeService.decodeQrCode(imageBytes);
            log.info("扫码借还 - 二维码解码内容: {}", qrContent);

            // 从二维码内容中提取 equipmentId (复用 assessDamage 中的解析逻辑)
            if (qrContent != null && !qrContent.trim().isEmpty()) {
                String idParam = "equipmentId=";
                int startIndex = qrContent.indexOf(idParam);
                if (startIndex != -1) {
                    String idString = qrContent.substring(startIndex + idParam.length());
                    int endIndex = idString.indexOf('&');
                    if (endIndex == -1) endIndex = idString.indexOf('#');
                    if (endIndex != -1) {
                        idString = idString.substring(0, endIndex);
                    }
                    try {
                        equipmentId = Long.parseLong(idString);
                        log.info("扫码借还 - 从二维码内容中提取到器材ID: {}", equipmentId);
                    } catch (NumberFormatException e) {
                        log.error("扫码借还 - 从二维码内容 '{}' 提取的ID '{}' 不是有效的数字: {}", qrContent, idString, e.getMessage());
                        return Result.error("二维码内容格式错误，无法提取器材ID。");
                    }
                } else {
                    log.warn("扫码借还 - 二维码内容 '{}' 中未找到 'equipmentId=' 参数。", qrContent);
                    return Result.error("二维码内容格式错误，未找到器材ID信息。");
                }
            } else {
                return Result.error("未能识别二维码内容。请确保图片清晰且二维码完整。");
            }

        } catch (NotFoundException e) {
            log.warn("扫码借还 - 二维码识别失败: {}", e.getMessage());
            return Result.error("未能识别二维码。请确保图片清晰且二维码完整。");
        } catch (IOException e) {
            log.error("扫码借还 - 读取图片或解码二维码时发生IO错误: {}", e.getMessage());
            return Result.error("处理图片时发生错误。");
        } catch (Exception e) {
            log.error("扫码借还 - 二维码识别过程中发生未知错误: {}", e.getMessage());
            return Result.error("二维码识别过程中发生未知错误。");
        }

        if (equipmentId == null) {
            return Result.error("未能获取有效的器材ID。");
        }

        // 2. 根据器材ID查询器材信息
        Equipment equipment = equipmentMapper.selectEquipmentById(equipmentId);

        if (equipment == null) {
            log.warn("扫码借还 - 根据ID {} 未找到器材信息或器材已删除。", equipmentId);
            return Result.error("未找到该器材的信息或该器材已报废/删除。");
        }

        Long currentModifiedId = equipment.getModifiedId();
        Long currentUserId = user.getUserId();

        // 3. 判断 modifiedId 并执行借取或归还操作
        try {
            if (currentModifiedId == null) {
                // modifiedId 为 null，表示器材当前可用，执行借取操作
                log.info("扫码借还 - 器材ID {} 当前可用，用户 {} 尝试借取。", equipmentId, currentUserId);
                equipmentMapper.updateEquipmentModifiedInfo(equipmentId, currentUserId);
                log.info("扫码借还 - 器材ID {} 借取成功，modifiedId 更新为 {}", equipmentId, currentUserId);

                // ！！！ 添加：借出成功，对应种类库存减一
                categoryMapper.BorrowEqp(equipmentId);
                log.info("扫码借还 - 器材ID {} 对应种类库存已减一。", equipmentId);

                // 可以选择同步更新 status 字段为 0 (已租用)，如果你的业务逻辑需要的话
                 equipmentMapper.setEquipment_Status_To_0(equipmentId);
                return Result.success("器材借取成功！");

            } else {
                // modifiedId 不为 null，表示器材已被借用
                if (Objects.equals(currentModifiedId, currentUserId)) {
                    // modifiedId 与当前用户ID相等，表示是当前用户归还器材
                    log.info("扫码借还 - 器材ID {} 当前被用户 {} 借用，用户 {} 尝试归还。", equipmentId, currentModifiedId, currentUserId);
                    equipmentMapper.updateEquipmentModifiedInfo(equipmentId, null); // 将 modifiedId 设置为 null
                    log.info("扫码借还 - 器材ID {} 归还成功，modifiedId 更新为 null", equipmentId);

                    // ！！！ 添加：归还成功，对应种类库存加一
                    categoryMapper.ReturnEqp(equipmentId);
                    log.info("扫码借还 - 器材ID {} 对应种类库存已加一。", equipmentId);

                    // 可以选择同步更新 status 字段为 1 (可用)，如果你的业务逻辑需要的话
                     equipmentMapper.setEquipment_Status_To_1(equipmentId);
                    return Result.success("器材归还成功！");

                } else {
                    // modifiedId 与当前用户ID不相等，表示器材被其他用户借用
                    log.warn("扫码借还 - 器材ID {} 当前被用户 {} 借用，用户 {} 尝试操作。", equipmentId, currentModifiedId, currentUserId);
                    // 根据需求，不返回具体是哪个用户，只返回被其他用户租用
                    return Result.error("该器材已被其他用户借用，无法操作。");
                }
            }
        } catch (Exception e) {
            log.error("扫码借还 - 处理器材ID {} 时发生数据库更新异常: {}", equipmentId, e.getMessage());
            // 事务会自动回滚，包括对 equipment 和 equipment_category 表的更新
            return Result.error("操作失败，请稍后重试或联系管理员。");
        }
    }

}
