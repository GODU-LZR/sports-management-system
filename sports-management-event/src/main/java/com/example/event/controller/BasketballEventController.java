package com.example.event.controller;

import com.example.common.response.Result;
import com.example.event.entity.BasketballEvent;
import com.example.event.service.EventService;
import com.example.event.service.impl.BasketballEventServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 篮球赛事管理接口
 */
@RestController
@RequestMapping("/api/events/basketball")
@Tag(name = "BasketballEventController", description = "篮球赛事管理接口")
public class BasketballEventController {

    @Autowired
    private EventService<BasketballEvent> basketballEventService;

    @PostMapping
    @Operation(summary = "创建篮球赛事")
    public Result<BasketballEvent> createEvent(@RequestBody BasketballEvent event) {
        BasketballEvent createdEvent = basketballEventService.createEvent(event);
        return Result.success(createdEvent);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除篮球赛事")
    public Result<Void> deleteEvent(@PathVariable @Parameter(description = "赛事ID") Long id) {
        boolean result = basketballEventService.deleteEvent(id);
        return result ? Result.success() : Result.error("删除篮球赛事失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新篮球赛事")
    public Result<BasketballEvent> updateEvent(@PathVariable @Parameter(description = "赛事ID") Long id, @RequestBody BasketballEvent event) {
        event.setId(id); // 确保ID一致
        BasketballEvent updatedEvent = basketballEventService.updateEvent(event);
        return Result.success(updatedEvent);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取篮球赛事详情")
    public Result<BasketballEvent> getEventById(@PathVariable @Parameter(description = "赛事ID") Long id) {
        BasketballEvent event = basketballEventService.getEventById(id);
        return event != null ? Result.success(event) : Result.error("篮球赛事不存在");
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始篮球赛事")
    public Result<Void> startEvent(@PathVariable @Parameter(description = "赛事ID") Long id) {
        boolean result = basketballEventService.startEvent(id);
        return result ? Result.success() : Result.error("开始篮球赛事失败");
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "结束篮球赛事")
    public Result<Void> endEvent(@PathVariable @Parameter(description = "赛事ID") Long id) {
        boolean result = basketballEventService.endEvent(id);
        return result ? Result.success() : Result.error("结束篮球赛事失败");
    }

    @GetMapping("/list")
    @Operation(summary = "获取篮球赛事列表")
    public Result<List<BasketballEvent>> getEventList() {
        // 调用Service获取篮球赛事列表
        List<BasketballEvent> eventList = ((BasketballEventServiceImpl) basketballEventService).getEventList();
        return Result.success(eventList);
    }

    @PostMapping("/{id}/quarters/{quarterId}/start")
    @Operation(summary = "开始某一节比赛")
    public Result<Void> startQuarter(
            @PathVariable @Parameter(description = "赛事ID") Long id,
            @PathVariable @Parameter(description = "节次ID") Integer quarterId) {
        boolean result = ((BasketballEventServiceImpl) basketballEventService).startQuarter(id, quarterId);
        return result ? Result.success() : Result.error("开始比赛节次失败");
    }

    @PostMapping("/{id}/quarters/{quarterId}/end")
    @Operation(summary = "结束某一节比赛")
    public Result<Void> endQuarter(
            @PathVariable @Parameter(description = "赛事ID") Long id,
            @PathVariable @Parameter(description = "节次ID") Integer quarterId) {
        boolean result = ((BasketballEventServiceImpl) basketballEventService).endQuarter(id, quarterId);
        return result ? Result.success() : Result.error("结束比赛节次失败");
    }

    @PostMapping("/{id}/score")
    @Operation(summary = "记录得分")
    public Result<Void> recordScore(
            @PathVariable @Parameter(description = "赛事ID") Long id,
            @RequestParam @Parameter(description = "球队ID") Long teamId,
            @RequestParam @Parameter(description = "球员ID") Long playerId,
            @RequestParam @Parameter(description = "得分类型（2分/3分/罚球）") String scoreType) {
        boolean result = ((BasketballEventServiceImpl) basketballEventService).recordScore(id, teamId, playerId, scoreType);
        return result ? Result.success() : Result.error("记录得分失败");
    }

    @PostMapping("/{id}/foul")
    @Operation(summary = "记录犯规")
    public Result<Void> recordFoul(
            @PathVariable @Parameter(description = "赛事ID") Long id,
            @RequestParam @Parameter(description = "球队ID") Long teamId,
            @RequestParam @Parameter(description = "球员ID") Long playerId,
            @RequestParam @Parameter(description = "犯规类型") String foulType) {
        boolean result = ((BasketballEventServiceImpl) basketballEventService).recordFoul(id, teamId, playerId, foulType);
        return result ? Result.success() : Result.error("记录犯规失败");
    }

    @GetMapping("/{id}/statistics/team/{teamId}")
    @Operation(summary = "获取球队统计数据")
    public Result<Object> getTeamStatistics(
            @PathVariable @Parameter(description = "赛事ID") Long id,
            @PathVariable @Parameter(description = "球队ID") Long teamId) {
        Map<String, Object> statistics = ((BasketballEventServiceImpl) basketballEventService).getTeamStatistics(id, teamId);
        return Result.success(statistics);
    }

    @GetMapping("/{id}/statistics/player/{playerId}")
    @Operation(summary = "获取球员统计数据")
    public Result<Object> getPlayerStatistics(
            @PathVariable @Parameter(description = "赛事ID") Long id,
            @PathVariable @Parameter(description = "球员ID") Long playerId) {
        Map<String, Object> statistics = ((BasketballEventServiceImpl) basketballEventService).getPlayerStatistics(id, playerId);
        return Result.success(statistics);
    }
}