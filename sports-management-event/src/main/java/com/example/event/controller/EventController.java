//package com.example.event.controller;
//
//import com.example.common.response.Result;
//import com.example.event.entity.SportEvent;
//import com.example.event.entity.BasketballEvent;
//import com.example.event.service.EventService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 赛事管理接口
// */
//@RestController
//@RequestMapping("/api/events")
//@Tag(name = "EventController", description = "赛事管理接口")
//public class EventController {
//
//    @Autowired
//    private EventService<SportEvent> eventService;
//
//    @PostMapping
//    @Operation(summary = "创建赛事")
//    public Result<SportEvent> createEvent(@RequestBody SportEvent event) {
//        SportEvent createdEvent = eventService.createEvent(event);
//        return Result.success(createdEvent);
//    }
//
//    @DeleteMapping("/{id}")
//    @Operation(summary = "删除赛事")
//    public Result<Void> deleteEvent(@PathVariable @Parameter(description = "赛事ID") Long id) {
//        boolean result = eventService.deleteEvent(id);
//        return result ? Result.success() : Result.error("删除赛事失败");
//    }
//
//    @PutMapping("/{id}")
//    @Operation(summary = "更新赛事")
//    public Result<SportEvent> updateEvent(@PathVariable @Parameter(description = "赛事ID") Long id, @RequestBody SportEvent event) {
//        event.setId(id); // 确保ID一致
//        SportEvent updatedEvent = eventService.updateEvent(event);
//        return Result.success(updatedEvent);
//    }
//
//    @GetMapping("/{id}")
//    @Operation(summary = "根据ID获取赛事详情")
//    public Result<SportEvent> getEventById(@PathVariable @Parameter(description = "赛事ID") Long id) {
//        SportEvent event = eventService.getEventById(id);
//        return event != null ? Result.success(event) : Result.error("赛事不存在");
//    }
//
//    @PostMapping("/{id}/start")
//    @Operation(summary = "开始赛事")
//    public Result<Void> startEvent(@PathVariable @Parameter(description = "赛事ID") Long id) {
//        boolean result = eventService.startEvent(id);
//        return result ? Result.success() : Result.error("开始赛事失败");
//    }
//
//    @PostMapping("/{id}/end")
//    @Operation(summary = "结束赛事")
//    public Result<Void> endEvent(@PathVariable @Parameter(description = "赛事ID") Long id) {
//        boolean result = eventService.endEvent(id);
//        return result ? Result.success() : Result.error("结束赛事失败");
//    }
//
//    @GetMapping("/list")
//    @Operation(summary = "获取赛事列表")
//    public Result<List<SportEvent>> getEventList() {
//        // 这里需要在Service中实现获取赛事列表的方法
//        return Result.success(null);
//    }
//
//    @GetMapping("/search")
//    @Operation(summary = "搜索赛事")
//    public Result<List<SportEvent>> searchEvents(
//            @RequestParam(required = false) @Parameter(description = "赛事名称") String name,
//            @RequestParam(required = false) @Parameter(description = "赛事类型") String eventType,
//            @RequestParam(required = false) @Parameter(description = "赛事状态") Integer status) {
//        // 这里需要在Service中实现搜索赛事的方法
//        return Result.success(null);
//    }
//
//    @PostMapping("/{id}/register")
//    @Operation(summary = "赛事报名")
//    public Result<Void> registerForEvent(
//            @PathVariable @Parameter(description = "赛事ID") Long id,
//            @RequestParam @Parameter(description = "参与者ID") Long participantId,
//            @RequestParam @Parameter(description = "参与者类型（个人/团队）") String participantType) {
//        // 这里需要在Service中实现赛事报名的方法
//        return Result.success();
//    }
//
//    @PostMapping("/{id}/schedule")
//    @Operation(summary = "安排赛程")
//    public Result<Void> arrangeSchedule(@PathVariable @Parameter(description = "赛事ID") Long id) {
//        // 这里需要在Service中实现安排赛程的方法
//        return Result.success();
//    }
//
//    @PostMapping("/{id}/result")
//    @Operation(summary = "记录比赛结果")
//    public Result<Void> recordResult(
//            @PathVariable @Parameter(description = "赛事ID") Long id,
//            @RequestParam @Parameter(description = "比赛ID") Long matchId,
//            @RequestBody Object result) {
//        // 这里需要在Service中实现记录比赛结果的方法
//        return Result.success();
//    }
//
//    @GetMapping("/{id}/statistics")
//    @Operation(summary = "获取赛事统计数据")
//    public Result<Object> getEventStatistics(@PathVariable @Parameter(description = "赛事ID") Long id) {
//        // 这里需要在Service中实现获取赛事统计数据的方法
//        return Result.success(null);
//    }
//}