package com.example.equipment.service.impl;

import com.example.equipment.dto.AiDamageResponse;
import com.fasterxml.jackson.core.JsonProcessingException; // 导入 JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import okhttp3.*; // 导入 OkHttp 的所有相关类，包括 okhttp3.MediaType
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AiService {

    // 从配置文件读取AI API配置
    @Value("${ai.api.base-url}")
    private String aiApiBaseUrl;
    @Value("${ai.api.key}")
    private String aiApiKey;
    @Value("${ai.api.model}")
    private String aiApiModel;
    @Value("${ai.api.max-tokens:2000}") // 默认2000
    private int aiApiMaxTokens;
    @Value("${ai.api.retry-attempts:3}") // 默认重试3次
    private int retryAttempts;
    @Value("${ai.api.retry-delay-ms:1000}") // 默认重试间隔1秒
    private long retryDelayMs;


    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    // 使用 OkHttp 的 MediaType
    private static final okhttp3.MediaType MEDIA_TYPE_JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");


    public AiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS) // 增加读取超时时间，AI响应可能较慢
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 调用AI模型评估器材损毁程度和相关性
     *
     * @param uploadedImageBase64 用户上传的图片Base64
     * @param originalImageUrl    入库时的图片URL
     * @return AI模型返回的解析结果
     * @throws IOException 如果HTTP请求失败
     * @throws RuntimeException 如果AI返回非预期结果或解析失败
     */
    public AiDamageResponse callAiForDamageAssessment(String uploadedImageBase64, String originalImageUrl) throws IOException {

        // 构建AI指令，强制返回JSON，并包含relative和conditionScore
        // 强调只返回JSON，并给出明确的JSON结构示例
        // *** 修改这里的 prompt 文本 ***
        String prompt = String.format(
                "对比提供的两张图片。第一张图片是器材**入库时**的原始照片。第二张图片是器材**归还时**拍摄的照片。\n" +
                        "你的任务是评估第二张图片中器材相对于第一张图片所示状态的当前状况。\n" +
                        "请提供一个介于0到100之间的整数作为器材的完好程度评分，其中100表示器材状况完美（看起来与原始照片完全相同或更好），0表示器材完全损坏或无法使用。\n" +
                        "同时，判断两张图片是否显示的是同一件器材。如果看起来是同一件器材，将`relative`字段设置为1。如果看起来是不同的器材，将`relative`设置为0。\n" +
                        "请**使用中文**提供观察到的损毁的简要描述，填充到`description`字段中。\n" + // <-- 强调使用中文
                        "**最关键的是，你的回复必须且只能是一个JSON对象。** JSON对象必须严格遵循以下结构：`{\"relative\": int, \"conditionScore\": int, \"description\": \"string\"}`。不要在JSON之前或之后包含任何其他文本或解释。确保`relative`的值是1或0，`conditionScore`的值介于0到100之间。"
        );
        // *** prompt 文本修改结束 ***


        // 构建JSON请求体
        ObjectNode jsonPayloadNode = objectMapper.createObjectNode();
        jsonPayloadNode.put("model", aiApiModel);
        jsonPayloadNode.put("stream", false);
        jsonPayloadNode.put("max_tokens", aiApiMaxTokens);

        ArrayNode messagesNode = objectMapper.createArrayNode();
        ObjectNode userMessageNode = objectMapper.createObjectNode();
        userMessageNode.put("role", "user");

        ArrayNode contentArrayNode = objectMapper.createArrayNode();

        // 添加文本内容 (Prompt)
        ObjectNode textContentNode = objectMapper.createObjectNode();
        textContentNode.put("type", "text");
        textContentNode.put("text", prompt);
        contentArrayNode.add(textContentNode);

        // 添加第一张图片内容 (Original Image URL)
        ObjectNode originalImageUrlContentNode = objectMapper.createObjectNode();
        originalImageUrlContentNode.put("type", "image_url");
        ObjectNode originalImageUrlNode = objectMapper.createObjectNode();
        originalImageUrlNode.put("url", originalImageUrl);
        originalImageUrlContentNode.set("image_url", originalImageUrlNode);
        contentArrayNode.add(originalImageUrlContentNode);

        // 添加第二张图片内容 (Uploaded Image Base64)
        ObjectNode uploadedImageContentNode = objectMapper.createObjectNode();
        uploadedImageContentNode.put("type", "image_url");
        ObjectNode uploadedImageUrlNode = objectMapper.createObjectNode();
        // Base64 data URL format: data:<mime type>;base64,<data>
        // Assuming uploaded image is PNG, you might need to infer this or add it to the request DTO
        uploadedImageUrlNode.put("url", "data:image/png;base64," + uploadedImageBase64);
        uploadedImageContentNode.set("image_url", uploadedImageUrlNode);
        contentArrayNode.add(uploadedImageContentNode);


        userMessageNode.set("content", contentArrayNode);
        messagesNode.add(userMessageNode);

        jsonPayloadNode.set("messages", messagesNode);

        String jsonPayload = jsonPayloadNode.toString();
        log.debug("AI Request Payload: {}", jsonPayload);

        // 使用 OkHttp 的 RequestBody.create 方法，它接受 String 和 okhttp3.MediaType
        RequestBody requestBody = RequestBody.create(jsonPayload, MEDIA_TYPE_JSON);


        Request request = new Request.Builder()
                .url(aiApiBaseUrl + "/v1/chat/completions")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + aiApiKey)
                .addHeader("Accept", "application/json")
                .build();

        IOException lastException = null;

        // Declare rawAiContent outside the try block
        String rawAiContent = null; // Initialize to null

        for (int i = 0; i < retryAttempts; i++) {
            try (Response response = client.newCall(request).execute()) {
                String responseBodyString = "";
                ResponseBody body = response.body();
                if (body != null) {
                    responseBodyString = body.string();
                }
                log.debug("AI Response Status: {}", response.code());
                log.debug("AI Response Body: {}", responseBodyString);

                if (response.isSuccessful()) {
                    // Attempt to parse the JSON response
                    try {
                        // The actual content generated by AI is usually nested, e.g., choices[0].message.content
                        ObjectNode root = (ObjectNode) objectMapper.readTree(responseBodyString);
                        // Assign to the variable declared outside the try block
                        rawAiContent = root.path("choices").get(0).path("message").path("content").asText();

                        if (rawAiContent == null || rawAiContent.trim().isEmpty()) {
                            throw new RuntimeException("AI返回内容为空");
                        }

                        log.debug("Raw AI Content: {}", rawAiContent);

                        // --- START: Extraction Logic for Markdown Code Block ---
                        String jsonContent = rawAiContent.trim();
                        String startMarker = "```json\n";
                        String endMarker = "\n```";

                        int startIndex = jsonContent.indexOf(startMarker);
                        int endIndex = jsonContent.lastIndexOf(endMarker); // Use lastIndexOf in case of multiple blocks

                        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                            // Found markdown block, extract the content inside
                            jsonContent = jsonContent.substring(startIndex + startMarker.length(), endIndex).trim();
                            log.debug("Extracted JSON from markdown block: {}", jsonContent);
                        } else {
                            // No markdown block found, assume the content is pure JSON
                            log.debug("No markdown block found, assuming raw content is pure JSON.");
                            // jsonContent is already trimmed rawAiContent
                        }
                        // --- END: Extraction Logic ---


                        // Now attempt to parse the extracted (or assumed pure) JSON string
                        // Use readValue directly on the extracted string
                        AiDamageResponse aiResponse = objectMapper.readValue(jsonContent, AiDamageResponse.class);

                        // Basic validation of AI response fields
                        if (aiResponse.getRelative() == null || aiResponse.getConditionScore() == null || aiResponse.getDescription() == null) {
                            // Include the content that failed to parse in the error message
                            throw new RuntimeException("AI返回的JSON结构不完整或字段缺失: " + jsonContent);
                        }
                        if (aiResponse.getRelative() != 0 && aiResponse.getRelative() != 1) {
                            log.warn("AI返回的relative值非预期 (不是0或1): {}", aiResponse.getRelative());
                            // Decide how strict to be. Maybe treat as unrelated if not 1?
                            // For now, let's just log and proceed if it's not null.
                        }
                        if (aiResponse.getConditionScore() < 0 || aiResponse.getConditionScore() > 100) {
                            log.warn("AI返回的conditionScore值超出范围 (0-100): {}", aiResponse.getConditionScore());
                            // Clamp the value for robustness.
                            aiResponse.setConditionScore(Math.max(0, Math.min(100, aiResponse.getConditionScore())));
                        }

                        return aiResponse; // Success, return the parsed response

                    } catch (JsonProcessingException e) { // Catch JSON specific parsing errors
                        log.error("解析AI响应JSON失败: {}", e.getMessage());
                        // Now rawAiContent is in scope. Add null check for safety.
                        lastException = new IOException("解析AI响应失败: " + e.getMessage() + ". Raw AI Content: " + (rawAiContent != null ? rawAiContent : "N/A"), e);
                    } catch (Exception e) { // Catch other potential errors during processing
                        log.error("处理AI响应时发生错误: {}", e.getMessage());
                        // rawAiContent might be null here if the error happened before assignment
                        lastException = new IOException("处理AI响应时发生错误: " + e.getMessage() + ". Raw AI Content: " + (rawAiContent != null ? rawAiContent : "N/A"), e);
                    }
                } else {
                    // HTTP error response
                    log.warn("AI API返回错误状态码: {} - {}", response.code(), response.message());
                    // Retry on server errors (5xx) or rate limits (429)
                    if (response.code() >= 500 || response.code() == 429) {
                        lastException = new IOException("AI API返回可重试错误状态码: " + response.code());
                    } else {
                        // Non-retryable client errors (4xx other than 429)
                        // Throw immediately if it's a client error that won't be fixed by retrying
                        throw new IOException("AI API返回不可重试错误状态码: " + response.code() + " - " + responseBodyString);
                    }
                }
            } catch (IOException e) {
                log.error("调用AI API发生IO异常: {}", e.getMessage());
                lastException = e; // Store the exception
            }

            // If loop continues, it's a retryable error or parsing failed
            if (i < retryAttempts - 1) {
                log.info("重试调用AI API (第 {}/{} 次)...", i + 1, retryAttempts);
                try {
                    Thread.sleep(retryDelayMs); // Wait before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("AI API重试被中断", ie);
                }
            }
        }

        // If loop finishes without returning, all retries failed
        throw new IOException("调用AI API重试失败，已达到最大尝试次数", lastException);
    }

    // You might want to add configuration properties in application.properties or application.yml
    // Example application.properties:
    /*
    ai.api.base-url=https://api.taobeiv.cn
    ai.api.key=sk-YOUR_API_KEY_HERE
    ai.api.model=gemini-2.0-flash-thinking-exp-01-21
    ai.api.max-tokens=2000
    ai.api.retry-attempts=3
    ai.api.retry-delay-ms=1000
     */
}
