// 假设你的测试类在类似这样的包下，例如：
// package com.example.equipment.service.impl;
// 或者 package com.example.equipment.tests;
// 请确保你的测试类有正确的包声明

import com.example.equipment.EquipmentApplication; // <--- 1. 确保此 import 语句存在
import com.example.equipment.service.impl.QrCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EquipmentApplication.class) // <--- 2. 确保指定了主应用类
public class SimplifiedQrCodeServiceTest {

    @Autowired // <--- 3. 建议移除 required = false
    private QrCodeService qrCodeService;

    private final String CATGPT_BASE_URL = "https://catgpt.help";
    private final String OUTPUT_DIRECTORY = "qr_codes_output"; // 二维码保存的子目录

    @BeforeEach
    void setUp() throws IOException {
        assertNotNull(qrCodeService, "QrCodeService 未被注入。请确保它是一个Spring Bean (例如使用@Service注解)，并且被主应用类扫描到。");
        Path outputDirPath = Paths.get(OUTPUT_DIRECTORY);
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
            System.out.println("创建目录: " + outputDirPath.toAbsolutePath());
        }
    }

    /**
     * 测试一：传入ID生成二维码，目标catgpt.help?id={id}，并保存二维码文件。
     * (这个测试保持不变，因为它之前是成功的)
     */
    @Test
    void testGenerateQrCodeWithIdForCatGpt() throws Exception {
        String equipmentId = "MYDEVICE007";
        String expectedQrContent = CATGPT_BASE_URL + "?id=" + equipmentId;

        byte[] qrCodeBytes = qrCodeService.generateQrCodeForUrl(expectedQrContent);

        assertNotNull(qrCodeBytes, "生成的二维码字节数组不应为null");
        assertTrue(qrCodeBytes.length > 0, "生成的二维码字节数组不应为空");

        String decodedText = qrCodeService.decodeQrCode(qrCodeBytes);
        assertEquals(expectedQrContent, decodedText, "解码后的二维码内容与预期URL不符。");
        System.out.println("测试一: 成功为ID '" + equipmentId + "' 生成二维码，内容: " + decodedText);

        String fileName = "catgpt_with_id_" + equipmentId + ".png";
        Path filePath = Paths.get(OUTPUT_DIRECTORY, fileName);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(qrCodeBytes);
        }
        System.out.println("测试一: 二维码已保存到: " + filePath.toAbsolutePath());
        assertTrue(Files.exists(filePath), "二维码文件 '" + fileName + "' 未成功保存。");
    }

    /**
     * 测试二 (新版)：先生成二维码并保存到文件，然后从文件读取并尝试解码。
     */
    @Test
    void testGenerateSaveThenScanQrCodeForCatGptAndPrint() throws Exception {
        String expectedQrContent = CATGPT_BASE_URL; // 目标URL: "https://catgpt.help"
        String tempImageFileName = "qr_for_scan_catgpt_direct.png"; // 用于保存和后续读取的文件名
        Path tempImageFilePath = Paths.get(OUTPUT_DIRECTORY, tempImageFileName);

        // 步骤 1: 生成二维码字节数据
        byte[] generatedQrBytes = qrCodeService.generateQrCodeForUrl(expectedQrContent);
        assertNotNull(generatedQrBytes, "步骤1: 生成的二维码字节数据不应为null");
        assertTrue(generatedQrBytes.length > 0, "步骤1: 生成的二维码字节数据不应为空");

        // 步骤 2: 将生成的字节数据保存为图片文件
        try (FileOutputStream fos = new FileOutputStream(tempImageFilePath.toFile())) {
            fos.write(generatedQrBytes);
        }
        System.out.println("测试二 (新版) - 步骤2: 二维码图片已生成并保存到: " + tempImageFilePath.toAbsolutePath());
        assertTrue(Files.exists(tempImageFilePath), "步骤2: 二维码文件 '" + tempImageFileName + "' 未能成功保存。");

        // 步骤 3: 从刚才保存的图片文件中读取字节数据
        byte[] bytesReadFromFile;
        try {
            bytesReadFromFile = Files.readAllBytes(tempImageFilePath);
        } catch (IOException e) {
            System.err.println("测试二 (新版) - 步骤3: 从文件读取二维码失败: " + tempImageFilePath.toAbsolutePath());
            throw e;
        }
        assertNotNull(bytesReadFromFile, "步骤3: 从文件读取的二维码字节数据不应为null");
        assertTrue(bytesReadFromFile.length > 0, "步骤3: 从文件读取的二维码字节数据长度应大于0");

        // 步骤 4: 使用 QrCodeService 解码从文件读取的字节数据
        String decodedTextFromFile = null;
        try {
            decodedTextFromFile = qrCodeService.decodeQrCode(bytesReadFromFile);
            assertEquals(expectedQrContent, decodedTextFromFile, "步骤4: 从文件解码后的二维码内容与预期URL不符。");
        } catch (com.google.zxing.NotFoundException e) {
            System.err.println("测试二 (新版) - 步骤4 解码失败: ZXing NotFoundException。请手动检查图片文件：" + tempImageFilePath.toAbsolutePath());
            // 打印一些关于读取的字节的信息可能有助于调试，但通常意义不大
            System.err.println("读取到的字节数: " + bytesReadFromFile.length);
            throw e; // 重新抛出异常，使测试失败
        }

        // 步骤 5: 如果解码成功，在控制台打印
        System.out.println("测试二 (新版) - 步骤5: 成功从文件解码二维码，内容 (目标网站): " + decodedTextFromFile);
        // 断言解码内容与预期一致 (虽然上面try块里已经有了，这里可以再加一个总的断言)
        assertEquals(expectedQrContent, decodedTextFromFile, "最终验证：解码内容不匹配。");
    }
}