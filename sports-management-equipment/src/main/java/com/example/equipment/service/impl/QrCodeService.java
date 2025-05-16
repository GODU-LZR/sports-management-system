package com.example.equipment.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class QrCodeService {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    private static final String CHARSET = "UTF-8";

    // 定义不同的预处理策略
    private final List<PreprocessingStrategy> preprocessingStrategies;

    // 定义一个内部类来封装预处理函数和描述
    private static class PreprocessingStrategy {
        private final String name;
        private final Function<BufferedImage, BufferedImage> processor;

        public PreprocessingStrategy(String name, Function<BufferedImage, BufferedImage> processor) {
            this.name = name;
            this.processor = processor;
        }

        public String getName() {
            return name;
        }

        public Function<BufferedImage, BufferedImage> getProcessor() {
            return processor;
        }
    }


    public QrCodeService() {
        // 初始化预处理策略列表
        preprocessingStrategies = new ArrayList<>();

        // 策略 1: 无预处理 (直接尝试)
        preprocessingStrategies.add(new PreprocessingStrategy("No Preprocessing", img -> img));

        // 策略 2: 灰度化 + 默认对比度增强 + 默认放大
        preprocessingStrategies.add(new PreprocessingStrategy("Grayscale + Default Contrast + Default Upscale", img -> {
            BufferedImage processed = img;
            try {
                processed = grayscaleImage(processed);
                processed = enhanceContrast(processed, 1.5f, -20.0f); // 默认对比度/亮度
                processed = upscaleImage(processed, 2.0); // 默认放大
            } catch (Exception e) {
                System.err.println("QrCodeService: 策略 2 预处理失败: " + e.getMessage());
                // 失败时返回上一步骤的结果，如果第一步就失败则返回原图
            }
            return processed;
        }));

        // 策略 3: 灰度化 + 更高对比度增强 + 默认放大
        preprocessingStrategies.add(new PreprocessingStrategy("Grayscale + Higher Contrast + Default Upscale", img -> {
            BufferedImage processed = img;
            try {
                processed = grayscaleImage(processed);
                processed = enhanceContrast(processed, 2.0f, -30.0f); // 更高对比度/更暗偏移
                processed = upscaleImage(processed, 2.0); // 默认放大
            } catch (Exception e) {
                System.err.println("QrCodeService: 策略 3 预处理失败: " + e.getMessage());
            }
            return processed;
        }));

        // 策略 4: 灰度化 + 默认对比度增强 + 更大放大
        preprocessingStrategies.add(new PreprocessingStrategy("Grayscale + Default Contrast + Larger Upscale", img -> {
            BufferedImage processed = img;
            try {
                processed = grayscaleImage(processed);
                processed = enhanceContrast(processed, 1.5f, -20.0f); // 默认对比度/亮度
                processed = upscaleImage(processed, 3.0); // 更大放大
            } catch (Exception e) {
                System.err.println("QrCodeService: 策略 4 预处理失败: " + e.getMessage());
            }
            return processed;
        }));

        // 策略 5: 灰度化 + 仅放大 (不调整对比度/亮度)
        preprocessingStrategies.add(new PreprocessingStrategy("Grayscale + Only Upscale", img -> {
            BufferedImage processed = img;
            try {
                processed = grayscaleImage(processed);
                processed = upscaleImage(processed, 2.0); // 默认放大
            } catch (Exception e) {
                System.err.println("QrCodeService: 策略 5 预处理失败: " + e.getMessage());
            }
            return processed;
        }));

        // 策略 6: 灰度化 + 尝试全局阈值化 (基于博客思路)
        // 注意：HybridBinarizer 通常优于全局阈值，但作为一种尝试
        preprocessingStrategies.add(new PreprocessingStrategy("Grayscale + Global Threshold (Low)", img -> {
            BufferedImage processed = img;
            try {
                processed = grayscaleImage(processed);
                processed = applyGlobalThreshold(processed, 80); // 较低阈值，可能适合较亮的图片
            } catch (Exception e) {
                System.err.println("QrCodeService: 策略 6 预处理失败: " + e.getMessage());
            }
            return processed;
        }));

        preprocessingStrategies.add(new PreprocessingStrategy("Grayscale + Global Threshold (Mid)", img -> {
            BufferedImage processed = img;
            try {
                processed = grayscaleImage(processed);
                processed = applyGlobalThreshold(processed, 120); // 中等阈值
            } catch (Exception e) {
                System.err.println("QrCodeService: 策略 7 预处理失败: " + e.getMessage());
            }
            return processed;
        }));

        preprocessingStrategies.add(new PreprocessingStrategy("Grayscale + Global Threshold (High)", img -> {
            BufferedImage processed = img;
            try {
                processed = grayscaleImage(processed);
                processed = applyGlobalThreshold(processed, 160); // 较高阈值，可能适合较暗的图片
            } catch (Exception e) {
                System.err.println("QrCodeService: 策略 8 预处理失败: " + e.getMessage());
            }
            return processed;
        }));


        // 你可以根据需要添加更多策略，例如：
        // - 尝试不同的 CONTRAST_SCALE_FACTOR 和 CONTRAST_OFFSET 组合
        // - 尝试不同的 UPSCALE_FACTOR
        // - 尝试锐化 (如果需要，可以实现一个 simpleSharpen 方法)
        // - 尝试组合：例如 先全局阈值再放大
    }


    public byte[] generateEquipmentQrCode(String equipmentId, String baseUrl) throws WriterException, IOException {
        String finalBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String separator = finalBaseUrl.contains("?") ? "&" : "?";
        String qrContent = finalBaseUrl + separator + "equipmentId=" + equipmentId;
        return generateQrCodeImage(qrContent, QR_CODE_WIDTH, QR_CODE_HEIGHT);
    }

    public byte[] generateQrCodeForUrl(String url) throws WriterException, IOException {
        return generateQrCodeImage(url, QR_CODE_WIDTH, QR_CODE_HEIGHT);
    }

    private byte[] generateQrCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        hints.put(EncodeHintType.MARGIN, 4);

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    public String decodeQrCode(byte[] qrCodeImageBytes) throws IOException, NotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(qrCodeImageBytes);
        BufferedImage originalImage = ImageIO.read(byteArrayInputStream);
        if (originalImage == null) {
            throw new IOException("无法从字节数组解码图像，可能不是有效的图片格式。");
        }

        // 创建解码提示 Map (TRY_HARDER 提示通常有助于处理轻微的畸变和模糊)
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        // 限定只识别 QR_CODE，基于博客思路
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        // hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE); // PURE_BARCODE 适用于纯净背景，可能不适合有器材作为背景的图片，故不建议启用


        // --- START: 多轮解码尝试 ---
        for (int i = 0; i < preprocessingStrategies.size(); i++) {
            PreprocessingStrategy strategy = preprocessingStrategies.get(i);
            String strategyName = strategy.getName();
            Function<BufferedImage, BufferedImage> preprocessingFn = strategy.getProcessor();

            BufferedImage processedImage = originalImage; // 从原始图片开始处理

            try {
                // 应用当前的预处理策略
                processedImage = preprocessingFn.apply(originalImage);

                // 确保处理后的图片不是 null，并且有有效的尺寸
                if (processedImage == null || processedImage.getWidth() <= 0 || processedImage.getHeight() <= 0) {
                    System.err.println("QrCodeService.decodeQrCode: 策略 " + (i + 1) + " (" + strategyName + ") 预处理返回无效图片。");
                    continue; // 跳过本次尝试
                }


                // 使用 HybridBinarizer，它通常在光照不均的图片上表现更好
                LuminanceSource source = new BufferedImageLuminanceSource(processedImage);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                MultiFormatReader multiFormatReader = new MultiFormatReader();
                Result result = multiFormatReader.decode(bitmap, hints);

                if (result != null) {
                    System.out.println("QrCodeService.decodeQrCode: 成功使用策略 " + (i + 1) + " (" + strategyName + ") 解码。");
                    return result.getText(); // 解码成功，立即返回结果
                }
            } catch (NotFoundException e) {
                // 当前策略解码失败，继续下一次尝试
                // System.err.println("QrCodeService.decodeQrCode: 策略 " + (i + 1) + " (" + strategyName + ") 解码失败: " + (e.getMessage() == null ? "无详细信息" : e.getMessage()));
                // 对于 NotFoundException，只记录不详细输出，避免日志过多
            } catch (Exception e) {
                // 其他处理异常，记录并继续下一次尝试
                System.err.println("QrCodeService.decodeQrCode: 策略 " + (i + 1) + " (" + strategyName + ") 处理或解码时发生异常: " + e.getMessage());
            }
        }
        // --- END: 多轮解码尝试 ---

        // 所有策略都尝试失败
        // 使用静态方法获取 NotFoundException 实例
        throw NotFoundException.getNotFoundInstance();
    }

    /**
     * 灰度化图片
     * @param originalImage 原始图片
     * @return 灰度图片
     */
    private BufferedImage grayscaleImage(BufferedImage originalImage) {
        if (originalImage == null) return null;
        // 如果已经是灰度图，直接返回
        if (originalImage.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return originalImage;
        }
        // 创建一个新的 BufferedImage，类型为灰度
        BufferedImage grayImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(originalImage, 0, 0, null);
        g.dispose();
        return grayImage;
    }

    /**
     * 增强图片对比度和调整亮度
     * @param originalImage 原始图片
     * @param scaleFactor 对比度增强系数 (>1 增强)
     * @param offset 亮度偏移 (<0 变暗, >0 变亮)
     * @return 处理后的图片
     */
    private BufferedImage enhanceContrast(BufferedImage originalImage, float scaleFactor, float offset) {
        if (originalImage == null) return null;
        // RescaleOp 适用于调整亮度或对比度
        RescaleOp op = new RescaleOp(scaleFactor, offset, null);
        // 应用操作，创建一个新的 BufferedImage
        // 确保输出图片类型与输入兼容，尤其是处理灰度图时
        int outputType = originalImage.getType() == BufferedImage.TYPE_BYTE_GRAY ? BufferedImage.TYPE_BYTE_GRAY : (originalImage.getType() == 0 ? BufferedImage.TYPE_INT_RGB : originalImage.getType());
        // 创建一个兼容的 BufferedImage
        BufferedImage enhancedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), outputType);
        op.filter(originalImage, enhancedImage);
        return enhancedImage;
    }

    /**
     * 放大图片
     * @param originalImage 原始图片
     * @param scaleFactor 放大系数
     * @return 放大后的图片
     */
    private BufferedImage upscaleImage(BufferedImage originalImage, double scaleFactor) {
        if (originalImage == null) return null;
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int scaledWidth = (int) (originalWidth * scaleFactor);
        int scaledHeight = (int) (originalHeight * scaleFactor);

        if (scaledWidth <= 0 || scaledHeight <= 0) {
            // 避免因 scaleFactor 过小导致尺寸为零或负数
            return originalImage;
        }

        // 创建一个新的 BufferedImage，类型与原图兼容或使用标准类型
        // 如果原图是灰度图，放大后也保持灰度
        int outputType = originalImage.getType() == BufferedImage.TYPE_BYTE_GRAY ? BufferedImage.TYPE_BYTE_GRAY : (originalImage.getType() == 0 ? BufferedImage.TYPE_INT_RGB : originalImage.getType());
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, outputType);


        Graphics2D g = scaledImage.createGraphics();

        // 设置渲染质量，可选，但可以改善放大效果
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // 双线性插值
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 使用 AffineTransform 进行缩放绘制
        AffineTransform at = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
        g.drawImage(originalImage, at, null);

        g.dispose(); // 释放 Graphics2D 资源

        return scaledImage;
    }

    /**
     * 应用全局阈值化
     * @param originalImage 灰度图片
     * @param threshold 阈值 (0-255)
     * @return 二值化图片
     */
    private BufferedImage applyGlobalThreshold(BufferedImage originalImage, int threshold) {
        if (originalImage == null) return null;
        // 确保是灰度图，如果不是先灰度化
        BufferedImage grayImage = grayscaleImage(originalImage);

        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        // 创建一个新的 BufferedImage，类型为二值图 (TYPE_BYTE_BINARY 或 TYPE_BYTE_GRAY 配合颜色模型)
        // 使用 TYPE_BYTE_GRAY 更简单，设置颜色模型为黑白
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = binaryImage.createGraphics();
        g.drawImage(grayImage, 0, 0, null); // 复制灰度图内容
        g.dispose();

        // 手动遍历像素应用阈值
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // getRGB 返回的是 ARGB 整数，对于灰度图，R, G, B 分量是相同的
                int gray = new Color(binaryImage.getRGB(x, y)).getRed(); // 获取灰度值
                if (gray < threshold) {
                    binaryImage.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    binaryImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        return binaryImage;
    }

    // TODO: 如果需要，可以添加一个 simpleSharpen 方法，使用 ConvolveOp 和简单的锐化矩阵
    // private BufferedImage simpleSharpen(BufferedImage originalImage) { ... }
}
