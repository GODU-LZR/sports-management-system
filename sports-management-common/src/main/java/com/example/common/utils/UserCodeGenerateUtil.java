package com.example.common.utils; // 通常 Util 类放在 utils 包下

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component; // 使用 @Component，更符合 Util 类的语义
import org.springframework.util.StringUtils;

/**
 * 用户编码生成工具类
 * 使用 Redis 的 INCR 命令生成带前缀的、顺序递增的唯一编码。
 */
@Component // 使用 @Component 代替 @Service，更符合工具类的语义
public class UserCodeGenerateUtil {

    // 日志记录器
    private static final Logger log = LoggerFactory.getLogger(UserCodeGenerateUtil.class);

    @Autowired
    private RedisUtil redisUtil; // 假设 RedisUtil 类已存在且注入

    // 从配置文件读取序列号在 Redis 中的 Key 前缀，默认为 "sequence"
    @Value("${code.generator.sequence-key-prefix:sequence}")
    private String sequenceKeyPrefix;

    // 从配置文件读取数字部分的默认填充长度，默认为 7 (例如 0000001)
    @Value("${code.generator.default-padding-length:7}") // <--- 默认值修改为 7
    private int defaultPaddingLength;

    /**
     * 为指定前缀生成下一个唯一编码 (使用默认填充长度)。
     * 例如，如果前缀是 "user"，将生成类似 "user0000001" 的编码。
     *
     * @param prefix 编码前缀 (例如 "user", "order" 等)，不能为空或空白。
     * @return 生成的唯一编码。
     * @throws IllegalArgumentException 如果前缀无效。
     * @throws RuntimeException 如果 Redis 操作失败或生成的序列号超出限制。
     */
    public String generateNextCode(String prefix) {
        // 调用重载方法，使用默认的填充长度
        return generateNextCode(prefix, defaultPaddingLength);
    }

    /**
     * 为指定前缀生成下一个唯一编码，并指定数字部分的长度。
     *
     * @param prefix        编码前缀 (例如 "user", "order" 等)，不能为空或空白。
     * @param paddingLength 数字部分的长度 (必须大于0)。例如，长度为 7 将生成 7 位数字（不足补零）。
     * @return 生成的唯一编码，格式为 "prefix" + 零填充的数字。
     * @throws IllegalArgumentException 如果前缀无效或填充长度小于等于0。
     * @throws RuntimeException 如果 Redis 操作失败或生成的序列号超出指定长度所能表示的最大值。
     */
    public String generateNextCode(String prefix, int paddingLength) {
        // 1. 校验输入参数
        if (!StringUtils.hasText(prefix)) { // 使用 Spring 的 StringUtils 检查空或空白
            throw new IllegalArgumentException("编码前缀不能为空或空白字符串。");
        }
        if (paddingLength <= 0) {
            throw new IllegalArgumentException("数字部分的填充长度必须大于0。");
        }

        // 2. 构建 Redis Key (每个前缀使用独立的序列)
        // 例如: sequence:user, sequence:order
        // 将前缀转为小写并去除首尾空格，保证 Redis Key 的一致性
        String redisKey = sequenceKeyPrefix + ":" + prefix.trim().toLowerCase();

        try {
            // 3. 调用 RedisUtil 的 incr 方法获取下一个序列号
            // incr(key, delta) 应该原子地增加 key 对应的数值，如果 key 不存在，则先设置为 0 再增加 delta。
            long sequence = redisUtil.incr(redisKey, 1); // 每次递增 1

            // 4. 检查序列号是否会超出格式化长度所允许的最大值
            // 例如，如果 paddingLength 为 7，最大值是 9999999
            long maxValue = (long) Math.pow(10, paddingLength) - 1;
            if (sequence > maxValue) {
                // 如果序列号超过了指定长度能表示的最大数，记录错误并抛出异常
                log.error("前缀 '{}' 的序列号 (Redis Key: {}) 已超过最大值 ({}), 无法使用长度 {} 进行格式化。当前序列号: {}",
                        prefix, redisKey, maxValue, paddingLength, sequence);
                // 抛出状态异常，表示当前状态无法生成符合要求的编码
                throw new IllegalStateException("前缀 '" + prefix + "' 的编码序列已超出最大值。");
            }
            // 检查序列号是否为非正数（理论上 Redis INCR 从 1 开始，不应出现，但作为防御性检查）
            if (sequence <= 0) {
                // 如果 incr 操作返回了非预期的 0 或负数，记录错误并抛出异常
                log.error("为前缀 '{}' (Redis Key: {}) 生成序列号时返回了非预期的非正数值: {}. 请检查 RedisUtil 实现或 Redis 服务状态。",
                        prefix, redisKey, sequence);
                // 抛出运行时异常，表明生成过程中出现问题
                throw new RuntimeException("未能为前缀 '" + prefix + "' 生成有效的序列号。");
            }


            // 5. 格式化编码：前缀 + 零填充的序列号
            // 例如，如果 paddingLength 是 7, formatPattern 就是 "%s%07d"
            String formatPattern = "%s%0" + paddingLength + "d";
            String generatedCode = String.format(formatPattern, prefix, sequence);

            // 记录生成的编码 (调试级别)
            log.debug("成功为前缀 '{}' 生成编码: {}", prefix, generatedCode);
            return generatedCode;

        } catch (IllegalArgumentException iae) {
            // 捕获 redisUtil.incr 可能因参数问题（如 delta < 0，虽然这里是1）抛出的异常
            log.error("调用 redisUtil.incr 时发生参数错误 (Key: '{}'): {}", redisKey, iae.getMessage(), iae);
            // 重新抛出运行时异常，包装原始异常信息
            throw new RuntimeException("因 Redis 操作参数无效导致编码生成失败。", iae);
        } catch (Exception e) {
            // 捕获 Redis 连接异常或其他来自 redisUtil 的底层错误
            log.error("使用 Redis Key '{}' 为前缀 '{}' 生成编码时出错。", redisKey, prefix, e);
            // 抛出运行时异常，包装原始异常，表明是 Redis 相关错误导致失败
            throw new RuntimeException("因 Redis 错误导致为前缀 '" + prefix + "' 生成编码失败。", e);
        }
    }
}