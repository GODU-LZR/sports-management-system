package com.example.event.Interceptor;

import com.example.event.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MatchIdValidationInterceptor implements HandlerInterceptor {

    @Autowired
    private MatchService matchService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String uri = request.getRequestURI(); // /api/v1/match/1001/user/888
        String[] parts = uri.split("/");

        // 简单提取 matchId 位置（这个位置根据你的路径结构可能需要调整）
        String matchId = null;
        for (int i = 0; i < parts.length; i++) {
            if ("match".equals(parts[i]) && i + 1 < parts.length) {
                matchId = parts[i + 1];
                break;
            }
        }

        if (matchId != null && !matchService.existsById()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"比赛ID不存在: " + matchId + "\"}");
            return false;
        }

        return true;
    }
}
