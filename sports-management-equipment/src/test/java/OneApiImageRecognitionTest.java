// OkHttp 相关的 imports (显式导入)
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody; // 确保这个也导入了

// JUnit 5 相关的 imports
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Jackson 相关的 imports
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

// Java 标准库 imports
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
// import java.util.Objects; // 如果确实没有用到，可以注释掉或删除

public class OneApiImageRecognitionTest {

    // API 配置常量
    private static final String BASE_URL = "https://api.taobeiv.cn";
    // 警告：请勿在生产代码中硬编码API密钥。考虑使用环境变量或配置文件。
    private static final String API_KEY = "sk-7eixaeaxVvjkWsUkkKgCvLA7yPzeIoxKeQcwv1sHgauzNiaX";
    private static final String MODEL_ID = "gemini-2.0-flash-thinking-exp-01-21"; // 根据需要确认或修改模型ID

    // Jackson ObjectMapper 用于处理JSON
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT); // 配置为美化输出

    /**
     * 从 classpath 加载资源文件（例如图片）并返回其字节数组。
     *
     * @param resourcePath classpath下的资源路径 (例如 "/equipmentPicture/1.png")
     * @return 文件的字节数组
     * @throws IOException 如果资源未找到或读取错误
     */
    private byte[] loadImageBytesFromClasspath(String resourcePath) throws IOException {
        InputStream inputStream = OneApiImageRecognitionTest.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("无法在classpath下找到资源: " + resourcePath +
                    ". 请确认文件位于 src/main/resources" + resourcePath +
                    " 或 src/test/resources" + resourcePath + " (对于Maven项目结构). " +
                    "当前尝试的ClassLoader是: " + OneApiImageRecognitionTest.class.getClassLoader());
        }
        try (InputStream is = inputStream) {
            return is.readAllBytes();
        }
    }

    @Test
    void testEquipmentDamageRecognitionWithLocalImage() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        String base64Image;
        String imageMimeType = "image/png"; // 根据你的图片类型确认 (e.g., image/jpeg, image/png)
        String localImagePath = "/static/equipmentPicture/1.png"; // 确保这个路径正确

        try {
            byte[] imageBytes = loadImageBytesFromClasspath(localImagePath);
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
            System.out.println("本地图片 '" + localImagePath + "' 已加载并编码为Base64. Base64字符串长度: " + base64Image.length());
        } catch (IOException e) {
            System.err.println("加载或编码本地图片 '" + localImagePath + "' 失败: " + e.getMessage());
            e.printStackTrace();
            fail("加载或编码本地图片失败: " + e.getMessage(), e);
            return;
        }

        String imageDataUrl = String.format("data:%s;base64,%s", imageMimeType, base64Image);

        // --- 使用 Jackson 构建 JSON 请求体 ---
        ObjectNode jsonPayloadNode = objectMapper.createObjectNode();
        jsonPayloadNode.put("model", MODEL_ID);
        jsonPayloadNode.put("stream", false); // 如果需要流式输出，改为 true

        ArrayNode messagesNode = objectMapper.createArrayNode();
        ObjectNode userMessageNode = objectMapper.createObjectNode();
        userMessageNode.put("role", "user");

        ArrayNode contentArrayNode = objectMapper.createArrayNode();

        // 添加文本内容
        ObjectNode textContentNode = objectMapper.createObjectNode();
        textContentNode.put("type", "text");
        textContentNode.put("text", "请详细描述这张图片中器材的损毁情况、程度以及可能的原因。");
        contentArrayNode.add(textContentNode);

        // 添加图片内容
        ObjectNode imageUrlContentNode = objectMapper.createObjectNode();
        imageUrlContentNode.put("type", "image_url");
        ObjectNode imageUrlNode = objectMapper.createObjectNode();
        imageUrlNode.put("url", imageDataUrl);
        imageUrlContentNode.set("image_url", imageUrlNode);
        contentArrayNode.add(imageUrlContentNode);

        userMessageNode.set("content", contentArrayNode);
        messagesNode.add(userMessageNode);

        jsonPayloadNode.set("messages", messagesNode);

        // *** 关键修改：增加 max_tokens 的值 ***
        // 根据模型的响应和任务复杂度，可能需要进一步调整这个值
        jsonPayloadNode.put("max_tokens", 2000); // 从 800 增加到 2000 (或更高)

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(jsonPayloadNode);
            System.out.println("生成的 JSON 请求体:\n" + jsonPayload);
        } catch (Exception e) {
            System.err.println("构建 JSON 请求体失败: " + e.getMessage());
            e.printStackTrace();
            fail("构建 JSON 请求体失败: " + e.getMessage(), e);
            return;
        }
        // --- Jackson 构建结束 ---


        MediaType mediaTypeApplicationJson = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonPayload, mediaTypeApplicationJson);

        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/chat/completions")
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Accept", "application/json")
                .build();

        System.out.println("正在发送请求到: " + request.url());

        try (Response response = client.newCall(request).execute()) {
            String responseBodyString = "";
            ResponseBody body = response.body();
            if (body != null) {
                responseBodyString = body.string();
            }

            System.out.println("响应状态码: " + response.code());
            System.out.println("原始响应体:\n" + responseBodyString);

            // 检查状态码是否成功 (2xx 系列)
            assertTrue(response.isSuccessful(),
                    "API调用失败，状态码不是 2xx。状态码: " + response.code() + ", 消息: " + response.message() + "\n响应体: " + responseBodyString);

            if (!responseBodyString.isEmpty()) {
                try {
                    // 解析并格式化响应体，以便查看实际内容
                    Object jsonResponse = objectMapper.readValue(responseBodyString, Object.class);
                    System.out.println("格式化后的响应体:\n" + objectMapper.writeValueAsString(jsonResponse));

                    // TODO: 根据实际响应结构，解析出模型的回答内容并进行断言
                    // 例如，如果响应结构是固定的，你可以解析 choices[0].message.content
                    // ObjectMapper mapper = new ObjectMapper();
                    // JsonNode root = mapper.readTree(responseBodyString);
                    // String modelContent = root.path("choices").get(0).path("message").path("content").asText();
                    // System.out.println("模型回答内容: " + modelContent);
                    // assertFalse(modelContent.isEmpty(), "模型返回的回答内容为空");

                } catch (Exception e) {
                    System.err.println("无法将响应体解析或格式化为JSON: " + e.getMessage());
                    // 根据需要决定是否将此视为失败
                    // fail("无法解析响应体: " + e.getMessage(), e);
                }
            } else {
                System.out.println("API调用成功，但响应体为空。");
                // 如果预期有响应体，这里可能需要 fail
                // fail("API调用成功但响应体为空");
            }

        } catch (IOException e) {
            System.err.println("API调用时发生IO异常: " + e.getMessage());
            e.printStackTrace();
            fail("API调用时发生IO异常: " + e.getMessage(), e);
        }
    }
}
