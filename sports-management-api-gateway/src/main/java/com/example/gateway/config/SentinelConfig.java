package com.example.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.example.common.model.Result;
import com.example.common.model.ResultCode;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;


import javax.annotation.PostConstruct;

@Configuration
public class SentinelConfig {
    
    @PostConstruct
    public void init() {
        // 自定义限流异常处理
        BlockRequestHandler blockRequestHandler = (serverWebExchange, throwable) -> {
            Result<?> result = Result.error(ResultCode.FORBIDDEN);
            result.setMessage("请求过于频繁，请稍后再试");
            
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(result);
        };
        
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}