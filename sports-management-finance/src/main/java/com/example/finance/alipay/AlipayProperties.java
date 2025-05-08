package com.example.finance.alipay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {
    private String serverUrl;
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String format;
    private String charset;
    private String signType;


    @Override
    public String toString() {
        return "AlipayProperties{" +
                "alipayPublicKey='" + alipayPublicKey + '\'' +
                ", serverUrl='" + serverUrl + '\'' +
                ", appId='" + appId + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", format='" + format + '\'' +
                ", charset='" + charset + '\'' +
                ", signType='" + signType + '\'' +
                '}';
    }
}