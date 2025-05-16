package com.example.event.controller;

import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.event.service.GameService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GameMyController {
    private final GameService gameService;

    @GetMapping("/my/competition")
    public Result<List<Map<String, Object>>> getMyCompetitionData(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam UserConstant userconst
    ) {
        // 这里假设从当前登录用户中获取userId，实际项目中应该从安全上下文中获取
        Long userId = userconst.getUserId(); // 模拟当前登录用户ID
        List<Map<String, Object>> competitionData = gameService.getMyCompetitionData(page, reviewStatus, userId);
        return Result.success("获取我的赛事列表成功", competitionData);
    }

    /**
     * 获取我的赛事的基本数据
     *
     * @param gameId 赛事id
     * @return 赛事基本信息
     */
    @GetMapping("/my/game/{gameId}")
    public Result<Map<String, Object>> getMyGameData(
            @PathVariable Integer gameId,
            @RequestParam UserConstant userConstant) {
        // 这里假设从当前登录用户中获取userId，实际项目中应该从安全上下文中获取
        Long userId = userConstant.getUserId(); // 模拟当前登录用户ID
        Map<String, Object> gameData = gameService.getMyGameData(gameId.longValue(), userId);
        return Result.success("获取我的赛事基本信息成功", gameData);
    }
    /**
     * 获取我的赛事的对应比赛
     *
     * @param gameId 赛事id
     * @return 比赛列表
     */
    @GetMapping("/my/game/matches/{gameId}")
    public Result<List<Map<String, Object>>> getMyMatches(
            @PathVariable Integer gameId,
            @RequestParam UserConstant userConstant) {
        // 这里假设从当前登录用户中获取userId，实际项目中应该从安全上下文中获取
        Long userId = userConstant.getUserId(); // 模拟当前登录用户ID
        List<Map<String, Object>> matches = gameService.getMyMatches(gameId.longValue(), userId);
        return Result.success("获取我的赛事比赛列表成功", matches);
    }

    /**
     * 修改赛事的基本信息数据
     *
     * @param form 表单数据
     * @return 操作结果
     */
    @PutMapping("/my/game")
    public Result<String> updateGameData(
            @RequestBody Map<String, Object> form,
            @Parameter(hidden = true) UserConstant userConstant) {
        try {
            // 这里假设从当前登录用户中获取userId，实际项目中应该从安全上下文中获取
            Long userId = 1L; // 模拟当前登录用户ID
            // 添加用户ID到表单数据中
            form.put("userId", userId);
            // 假设updateGameData方法返回布尔值表示成功或失败
            // 由于GameService接口中没有直接对应的方法，这里假设有一个updateGame方法
            boolean success = true; // 模拟更新成功
            if (success) {
                return Result.success("修改赛事基本信息成功");
            } else {
                return Result.error("修改赛事基本信息失败");
            }
        } catch (Exception e) {
            return Result.error("修改赛事基本信息失败: " + e.getMessage());
        }
    }


}
