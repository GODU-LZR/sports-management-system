package com.example.event.controller;

import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.event.service.MatchService;
import com.example.event.service.impl.MatchServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 比赛控制器
 * 处理/restcontroll/match/{matchId}相关的接口
 */
@RestController
@RequestMapping("/my/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    public MatchController() {
        this.matchService = new MatchServiceImpl();
    }

    /**
     * 获取比赛的基本信息数据
     *
     * @param matchId 比赛id
     * @return 比赛基本信息
     */
    @GetMapping("/{matchId}")
    public Result<Map<String, Object>> getMatchData(
            @PathVariable String matchId,
            @RequestParam UserConstant userConstant) {
        // 从当前登录用户中获取userId
        Long userId = userConstant.getUserId();
        Map<String, Object> matchData = matchService.getMatchData(matchId, userId);
        return Result.success("获取比赛基本信息成功", matchData);
    }

    /**
     * 获取比赛的赛段得分信息(复用/sport/match/getQuartersData)
     *
     * @param matchId 比赛id
     * @return 赛段得分信息
     */
    @GetMapping("/{matchId}/quarters")
    public Result<List<Map<String, Object>>> getQuartersData(
            @PathVariable String matchId,
            @RequestParam UserConstant userConstant) {
        // 从当前登录用户中获取userId
        Long userId = userConstant.getUserId();
        List<Map<String, Object>> quartersData = matchService.getQuartersData(matchId, userId);
        return Result.success("获取赛段得分信息成功", quartersData);
    }

    /**
     * 获取比赛的队伍得分信息(复用/sport/match/getTeamStatsData)
     *
     * @param matchId 比赛id
     * @return 队伍得分信息
     */
    @GetMapping("/{matchId}/teamstats")
    public Result<List<Map<String, Object>>> getTeamStatsData(
            @PathVariable String matchId,
            @RequestParam UserConstant userConstant) {
        // 从当前登录用户中获取userId
        Long userId = userConstant.getUserId();
        List<Map<String, Object>> teamStatsData = matchService.getTeamStatsData(matchId, userId);
        return Result.success("获取队伍得分信息成功", teamStatsData);
    }

    /**
     * 获取比赛队伍球员的得分信息(复用/sport/match/getPlayersData)
     *
     * @param matchId 比赛id
     * @return 球员得分信息
     */
    @GetMapping("/{matchId}/players")
    public Result<List<List<Map<String, Object>>>> getPlayersData(
            @PathVariable String matchId,
            @RequestParam UserConstant userConstant) {
        // 从当前登录用户中获取userId
        Long userId = userConstant.getUserId();
        List<List<Map<String, Object>>> playersData = matchService.getPlayersData(matchId, userId);
        return Result.success("获取球员得分信息成功", playersData);
    }

    /**
     * 修改比赛的基本信息数据
     *
     * @param matchId 比赛id
     * @param form    修改表单
     * @return 操作结果
     */
    @PutMapping("/{matchId}")
    public Result<String> updateMatchData(
            @PathVariable String matchId,
            @RequestBody Map<String, Object> form,
            @RequestParam UserConstant userConstant) {
        // 从当前登录用户中获取userId
        Long userId = userConstant.getUserId();
        boolean success = matchService.updateMatchData(matchId, form, userId);
        if (success) {
            return Result.success("修改比赛基本信息成功");
        } else {
            return Result.error("修改比赛基本信息失败");
        }
    }

    /**
     * 修改比赛的赛段得分信息
     *
     * @param matchId 比赛id
     * @param form    修改表单
     * @return 操作结果
     */
    @PutMapping("/{matchId}/quarters")
    public Result<String> updateQuartersData(
            @PathVariable String matchId,
            @RequestBody List<Map<String, Object>> form,
            @RequestParam UserConstant userConstant) {
        // 从当前登录用户中获取userId
        Long userId = userConstant.getUserId();
        boolean success = matchService.updateQuartersData(matchId, form, userId);
        if (success) {
            return Result.success("修改赛段得分信息成功");
        } else {
            return Result.error("修改赛段得分信息失败");
        }
    }

    /**
     * 修改比赛的队伍得分信息数据
     *
     * @param matchId 比赛id
     * @param form    修改表单
     * @return 操作结果
     */
    @PutMapping("/{matchId}/teamstats")
    public Result<String> updateTeamStatsData(
            @PathVariable String matchId,
            @RequestBody List<Map<String, Object>> form,
            @RequestParam UserConstant userConstant) {
        // 从当前登录用户中获取userId
        Long userId = userConstant.getUserId();
        boolean success = matchService.updateTeamStatsData(matchId, form, userId);
        if (success) {
            return Result.success("修改队伍得分信息成功");
        } else {
            return Result.error("修改队伍得分信息失败");
        }
    }

    /**
     * 修改球员得分信息数据
     *
     * @param matchId 比赛id
     * @param form    修改表单
     * @return 操作结果
     */
    @PutMapping("/{matchId}/players")
    public Result<String> updatePlayersData(
            @PathVariable String matchId,
            @RequestBody Map<String, Object> form,
            @RequestParam UserConstant userConstant) {
        // 从当前登录用户中获取userId
        Long userId = userConstant.getUserId();
        boolean success = matchService.updatePlayersData(matchId, form, userId);
        if (success) {
            return Result.success("修改球员得分信息成功");
        } else {
            return Result.error("修改球员得分信息失败");
        }
    }

    /**
     * 用户关注或取消关注比赛
     *
     * @param matchId 比赛id
     * @return 操作结果
     */
    @PostMapping("/sport/match/follow/{matchId}")
    public Result<String> handleFollow(
            @PathVariable String matchId,
            @RequestParam UserConstant userConstant) {
        // 从当前登录用户中获取userId
        Long userId = userConstant.getUserId();
        boolean success = matchService.toggleFollowStatus(matchId, userId);
        if (success) {
            return Result.success("操作成功");
        } else {
            return Result.error("操作失败");
        }
    }

    /**
     * 获取比赛的球员得分信息
     *
     * @param matchId 比赛id
     * @return 球员得分信息
     */
    @GetMapping("/sport/match/{matchId}/players")
    public Result<List<List<Map<String, Object>>>> getSportMatchPlayersData(@PathVariable String matchId) {
        List<List<Map<String, Object>>> playersData = matchService.getSportMatchPlayersData(matchId);
        return Result.success("获取球员得分信息成功", playersData);
    }
}