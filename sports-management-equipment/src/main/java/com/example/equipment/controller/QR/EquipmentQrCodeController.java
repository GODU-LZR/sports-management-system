package com.example.equipment.controller.QR;

import com.example.common.response.Result;
import com.example.common.response.ResultCode;
import com.example.equipment.dto.GenerateQrCodeRequestDTO; // 引入新建的DTO
import com.example.equipment.service.impl.QrCodeService; // 引入二维码服务
import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 引入用于读取配置的注解
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64; // 引入Base64编码工具

@RestController
@RequestMapping("/qrcode") // 统一的二维码相关接口路径
@Slf4j
@Tag(name = "EquipmentQrCodeController", description = "器材二维码生成接口")
public class EquipmentQrCodeController {

    @Autowired
    private QrCodeService qrCodeService;


    @Value("${frontend.base-url:http://124.71.58.72:3010/}") // 提供一个默认值以防配置缺失
    private String frontendBaseUrl;

    /**
     * 根据器材ID生成二维码，二维码扫描后携带器材ID跳转到前端页面。
     *
     * @param requestDTO 包含器材ID的请求体对象
     * @return Result 对象，其中 data 字段为二维码图片的Base64编码字符串。
     */
    @PostMapping("/generate")
    @Operation(summary = "生成器材二维码",
            description = "根据器材ID生成一个二维码，扫描该二维码将跳转到前端指定页面并携带器材ID参数。返回Base64编码的PNG图片数据。")
    public Result<String> generateEquipmentQrCode(@RequestBody GenerateQrCodeRequestDTO requestDTO) {
        log.info("收到生成器材二维码请求: equipmentId={}", requestDTO != null ? requestDTO.getEquipmentId() : "null");

        if (requestDTO == null || requestDTO.getEquipmentId() == null) {
            log.warn("生成二维码请求失败: 器材ID为空");
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "器材ID不能为空。");
        }

        Long equipmentId = requestDTO.getEquipmentId();

        try {
            // 调用服务层方法生成二维码字节数组
            // 使用前面定义的 frontendBaseUrl
            byte[] qrCodeBytes = qrCodeService.generateEquipmentQrCode(String.valueOf(equipmentId), frontendBaseUrl);

            if (qrCodeBytes == null || qrCodeBytes.length == 0) {
                log.error("生成二维码字节数组为空或null, equipmentId={}", equipmentId);
                 return Result.error(ResultCode.ERROR.getCode(), "生成二维码失败，返回数据为空。");
            }

            // 将字节数组编码为Base64字符串
            String base64QrCode = Base64.getEncoder().encodeToString(qrCodeBytes);
            log.info("成功生成并编码器材二维码，equipmentId={}", equipmentId);

            // 返回成功结果，数据为Base64字符串
            return Result.success(base64QrCode, "二维码生成成功。");

        } catch (WriterException e) {
            log.error("生成二维码时发生WriterException异常, equipmentId={}", equipmentId, e);
            return Result.error(ResultCode.ERROR.getCode(), "生成二维码图片数据失败。");
        } catch (IOException e) {
            log.error("生成二维码时发生IOException异常, equipmentId={}", equipmentId, e);
            return Result.error(ResultCode.ERROR.getCode(), "处理二维码图片数据失败。");
        } catch (Exception e) {
            log.error("生成器材二维码时发生未预料的错误, equipmentId={}", equipmentId, e);
            return Result.error(ResultCode.ERROR.getCode(), "生成二维码失败，请稍后重试。");
        }
    }
}
