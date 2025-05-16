package com.example.venue.config; // 你的配置包名

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient; // 导入旧的高层级客户端类
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary; // 如果有多个 RestHighLevelClient Bean，可以使用 @Primary

import java.util.Arrays;
import java.util.Objects;

/**
 * Elasticsearch 旧 High-Level REST Client (elasticsearch-rest-high-level-client) 的配置类。
 * 需要保留 elasticsearch-rest-high-level-client 和 elasticsearch-rest-client 依赖。
 *
 * 此版本修改了 RestHighLevelClient 的构建方式，直接传入 RestClientBuilder，
 * 以尝试解决 "需要的类型: RestClientBuilder 提供的类型: RestClient" 编译错误。
 */
@Configuration
public class ElasticsearchClientConfig {

    // 从 application.properties 读取配置
    @Value("${spring.elasticsearch.uris}")
    private String uris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    // 读取连接超时和 Socket 超时 (这些是 Spring Boot 自动配置的标准属性名)
    @Value("${spring.elasticsearch.connection-timeout:5000}") // 默认 5 秒
    private int connectionTimeoutMillis;

    @Value("${spring.elasticsearch.socket-timeout:60000}") // 默认 60 秒
    private int socketTimeoutMillis;


    /**
     * 配置并创建 RestHighLevelClient Bean。
     * 直接使用 RestClientBuilder 构建 RestHighLevelClient。
     */
    @Bean
    // @Primary // 如果Spring上下文中可能有其他RestHighLevelClient Bean，可以使用此注解标记首选
    public RestHighLevelClient elasticsearchClient() {
        // 解析 URIs 字符串，支持多个主机，格式如 "http://host1:port1,http://host2:port2"
        String[] uriArray = uris.split(",");
        HttpHost[] httpHosts = Arrays.stream(uriArray)
                .map(uri -> {
                    try {
                        // 简单的 URI 解析，假设是 http://host:port 或 https://host:port
                        String trimmedUri = uri.trim();
                        if (trimmedUri.isEmpty()) return null;

                        String scheme = trimmedUri.startsWith("https") ? "https" : "http";
                        String hostPort = trimmedUri.replace("http://", "").replace("https://", "");
                        String[] parts = hostPort.split(":");
                        String host = parts[0];
                        // 如果端口未指定，根据协议使用默认端口
                        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : (scheme.equals("https") ? 443 : 80);
                        return new HttpHost(host, port, scheme);
                    } catch (Exception e) {
                        // 可以在这里添加日志记录，警告解析失败的URI
                        System.err.println("Failed to parse Elasticsearch URI: " + uri + ". Error: " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull) // 过滤掉解析失败的 null 值
                .toArray(HttpHost[]::new);

        if (httpHosts.length == 0) {
            throw new IllegalArgumentException("Invalid or empty Elasticsearch URIs configured: " + uris);
        }

        // 配置基本认证 (用户名和密码)
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        // 构建 RestClientBuilder
        RestClientBuilder builder = RestClient.builder(httpHosts)
                // 配置请求超时等设置
                .setRequestConfigCallback(requestConfigBuilder -> {
                    // 设置连接超时和 Socket 超时 (毫秒)
                    return requestConfigBuilder
                            .setConnectTimeout(connectionTimeoutMillis) // 从 properties 读取连接超时
                            .setSocketTimeout(socketTimeoutMillis); // 从 properties 读取 Socket 超时
                })
                // 配置底层的 HttpClient
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    // 设置默认的认证提供者
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    // 禁用嗅探 (Sniffing) 是可选的，如果遇到节点发现问题可以尝试禁用
                    // httpClientBuilder.disableSniffing(); // 注意：这个方法在某些旧版本或特定配置下可能不存在或行为不同
                    // 如果使用 HTTPS 且需要信任证书，可以在这里配置 SSLContext
                    // httpClientBuilder.setSSLContext(...)
                    return httpClientBuilder;
                })
                // 配置失败监听器
                .setFailureListener(new RestClient.FailureListener() {
                    @Override
                    public void onFailure(org.elasticsearch.client.Node node) {
                        // 处理节点失败，这里可以使用日志框架记录
                        System.err.println("Elasticsearch node failure: " + node.getHost());
                    }
                });

        // **修改这里：直接使用 RestClientBuilder 构建 RestHighLevelClient**
        // 原来的代码是:
        // RestClient restClient = builder.build();
        // return new RestHighLevelClient(restClient);

        // 修改为直接传入 builder:
        return new RestHighLevelClient(builder);
    }

    // 注意：如果添加了这个自定义配置类并定义了 RestHighLevelClient Bean，
    // Spring Boot 针对 RestHighLevelClient 的默认自动配置会失效。
    // 所有相关的配置（如连接信息、认证、超时等）都需要在这个类中手动完成。
    // 确保你的 application.properties 中有 spring.elasticsearch.uris, username, password,
    // 以及可选的 connection-timeout 和 socket-timeout 属性。
    // 同时确保 pom.xml 中正确引入了 elasticsearch-rest-high-level-client 和 elasticsearch-rest-client 7.17.6 依赖，
    // 并且没有引入新的 elasticsearch-java 客户端或其他冲突版本的依赖。
}
