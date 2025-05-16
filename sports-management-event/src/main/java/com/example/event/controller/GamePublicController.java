package com.example.event.controller;

import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.event.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 赛事控制器
 */
@RestController
@RequiredArgsConstructor
public class GamePublicController {
    private final GameService gameService;

    /**
     * 获取赛事的基本信息
     *
     * @param gameId 赛事id
     * @return 赛事基本信息
     */
    @GetMapping("/sport/game/{gameId}")
    public Result<Map<String, Object>> getGameData(@PathVariable Integer gameId) {
        Map<String, Object> gameData = gameService.getGameData(gameId.longValue());
        return Result.success("获取赛事基本信息成功", gameData);
    }

    /**
     * 获取赛事的对应比赛列表
     *
     * @param gameId 赛事id
     * @return 比赛列表
     */
    @GetMapping("/sport/game/matches/{gameId}")
    public Result<List<Map<String, Object>>> getMatches(@PathVariable Integer gameId) {
        List<Map<String, Object>> matches = gameService.getMatches(gameId.longValue());
        return Result.success("获取比赛列表成功", matches);
    }

    /**
     * 获取我的赛事基本信息数据
     *
     * @param page         页码
     * @param reviewStatus 审核状态
     * @return 我的赛事列表
     */

    /**
     * 获取我的赛事列表的总条数
     *
     * @param reviewStatus 审核状态
     * @return 总条数
     */
    @GetMapping("/my/competition/count")
    public Result<Integer> getMyCompetitionCount(
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam(required = false) UserConstant userConstant) {
        // 这里假设从当前登录用户中获取userId，实际项目中应该从安全上下文中获取

        Integer count = gameService.getMyCompetitionCount(reviewStatus, userConstant.getUserId());
        return Result.success(count);
    }


    /**
     * 获取裁判待选选项
     *
     * @param referee 搜索关键字
     * @return 裁判选项列表
     */
    @GetMapping("/referee/options")
    public Result<List<Map<String, Object>>> getRefereeOptions(
            @RequestParam(required = false) String referee) {
        // 假设有一个获取裁判选项的方法
        List<Map<String, Object>> options = new ArrayList<>();
        // 模拟数据
        if (referee == null || referee.isEmpty()) {
            // 返回所有裁判选项
            Map<String, Object> option1 = new HashMap<>();
            option1.put("id", 1);
            option1.put("name", "张三");
            options.add(option1);

            Map<String, Object> option2 = new HashMap<>();
            option2.put("id", 2);
            option2.put("name", "李四");
            options.add(option2);
        } else {
            // 根据关键字搜索裁判
            Map<String, Object> option = new HashMap<>();
            option.put("id", 1);
            option.put("name", referee + "_裁判");
            options.add(option);
        }
        return Result.success("获取裁判选项成功", options);
    }

    /**
     * 获取场地待选选项
     *
     * @param venue 搜索关键字
     * @return 场地选项列表
     */
    @GetMapping("/venue/options")
    public Result<List<Map<String, Object>>> getVenueOptions(@RequestParam(required = false) String venue) {
        // 假设有一个获取场地选项的方法
        List<Map<String, Object>> options = new ArrayList<>();
        // 模拟数据
        if (venue == null || venue.isEmpty()) {
            // 返回所有场地选项
            Map<String, Object> option1 = new HashMap<>();
            option1.put("id", 1);
            option1.put("name", "体育馆A");
            options.add(option1);

            Map<String, Object> option2 = new HashMap<>();
            option2.put("id", 2);
            option2.put("name", "体育馆B");
            options.add(option2);
        } else {
            // 根据关键字搜索场地
            Map<String, Object> option = new HashMap<>();
            option.put("id", 1);
            option.put("name", venue + "_场地");
            options.add(option);
        }
        return Result.success("获取场地选项成功", options);
    }

    /**
     * 获取队伍待选选项
     *
     * @param team 搜索关键字
     * @return 队伍选项列表
     */
    @GetMapping("/team/options")
    public Result<List<Map<String, Object>>> getTeamOptions(@RequestParam(required = false) String team) {
        // 假设有一个获取队伍选项的方法
        List<Map<String, Object>> options = new ArrayList<>();
        // 模拟数据
        if (team == null || team.isEmpty()) {
            // 返回所有队伍选项
            Map<String, Object> option1 = new HashMap<>();
            option1.put("id", 1);
            option1.put("name", "红队");
            options.add(option1);

            Map<String, Object> option2 = new HashMap<>();
            option2.put("id", 2);
            option2.put("name", "蓝队");
            options.add(option2);
        } else {
            // 根据关键字搜索队伍
            Map<String, Object> option = new HashMap<>();
            option.put("id", 1);
            option.put("name", team + "_队");
            options.add(option);
        }
        return Result.success("获取队伍选项成功", options);
    }

    /**
     * 获取负责人待选选项
     *
     * @param responsiblePerson 搜索关键字
     * @return 负责人选项列表
     */
    @GetMapping("/responsible/options")
    public Result<List<Map<String, Object>>> getResponsiblePersonOptions(@RequestParam(required = false) String responsiblePerson) {
        // 假设有一个获取负责人选项的方法
        List<Map<String, Object>> options = new ArrayList<>();
        // 模拟数据
        if (responsiblePerson == null || responsiblePerson.isEmpty()) {
            // 返回所有负责人选项
            Map<String, Object> option1 = new HashMap<>();
            option1.put("id", 1);
            option1.put("name", "王五");
            options.add(option1);

            Map<String, Object> option2 = new HashMap<>();
            option2.put("id", 2);
            option2.put("name", "赵六");
            options.add(option2);
        } else {
            // 根据关键字搜索负责人
            Map<String, Object> option = new HashMap<>();
            option.put("id", 1);
            option.put("name", responsiblePerson + "_负责人");
            options.add(option);
        }
        return Result.success("获取负责人选项成功", options);
    }
}