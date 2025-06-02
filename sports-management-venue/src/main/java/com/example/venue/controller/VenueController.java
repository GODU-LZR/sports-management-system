package com.example.venue.controller; // 假设你的 Controller 包名

import com.example.venue.dto.VenueAddRequest;
import com.example.venue.dto.VenueQueryRequest;
import com.example.venue.dto.VenueSearchRequest;
import com.example.venue.dto.VenueUpdateRequest;
import com.example.venue.pojo.es.ESVenue;
import com.example.venue.pojo.mysql.Venue;
import com.example.venue.service.ESService;
import com.example.venue.service.VenueService;
import com.example.venue.vo.Result;
import com.example.venue.vo.VenueOption;
import com.example.venue.vo.VenuePage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController // 使用 @RestController 结合 @RequestMapping 更常见，它包含了 @Controller 和 @ResponseBody
@RequestMapping("/venue") // 统一的路径前缀，例如 /api/venues
@Slf4j // <-- 在类上添加这个注解，Lombok 会自动生成 'log' 字段
public class VenueController {

    private final VenueService venueService;

    private final ESService esService;

    private final String adminId = "Admin12345678";

    // 推荐使用构造器注入依赖
    @Autowired
    public VenueController(VenueService venueService, ESService esService) {
        this.venueService = venueService;
        this.esService = esService;
    }

    // 查询场地 (你之前提供的示例方法，已完善日志和异常处理)
    @PostMapping("/query")
    public Result<VenuePage> listVenues(@RequestBody @Valid VenueQueryRequest venueQueryRequest) {
        venueQueryRequest.modifyPage();
        log.info("接收到查询场地信息请求，参数: {}", venueQueryRequest);
        VenuePage venuePage = venueService.listVenues(venueQueryRequest);
        return Result.success(venuePage);
    }

    // 新增场地
    @PostMapping
    public Result<String> addVenue(@RequestBody @Valid VenueAddRequest venueAddRequest) {
        String venueId = "venue" + System.currentTimeMillis();

        venueAddRequest.setVenueId(venueId);
        venueAddRequest.setCreatedId(this.adminId);
        venueAddRequest.setUpdatedId(this.adminId);
        log.info("接收到新增场地请求，参数: {}", venueAddRequest);
        boolean success = venueService.addVenue(venueAddRequest);
        if (success) {
            return Result.success("新增场地成功");
        }
        return Result.failure("新增场地失败，请检查输入或稍后重试");
    }

    // 修改场地
    @PutMapping
    public Result<String> updateVenue(@RequestBody @Valid VenueUpdateRequest venueUpdateRequest) {
        venueUpdateRequest.setUpdatedId(this.adminId);
        log.info("接收到修改场地请求，参数: {}", venueUpdateRequest);
        boolean success = venueService.updateVenue(venueUpdateRequest);
        if (success) {
            return Result.success("修改场地成功");
        }
        return Result.failure("修改场地失败，可能场地不存在或数据未改变");
    }

    // 删除场地
    @DeleteMapping("/{venueId}")
    public Result<String> deleteVenue(@PathVariable("venueId") String venueId) {
        if(venueId == null || venueId.isEmpty()) {
            return Result.validateError("场地参数有误");
        }
        log.info("接收到删除场地请求，场地ID: {}", venueId);
        boolean success = venueService.deleteVenue(venueId, this.adminId);
        if (success) {
            return Result.success("删除场地成功");
        }
        return Result.failure("删除场地失败，可能场地不存在或数据未改变");
    }

    // 根据关键字搜索场地选项列表。
    @GetMapping("/options")
    public Result<List<VenueOption>> listVenueOptions(@RequestParam(required = false) String key) {
        log.info("接收到查询场地选项请求，关键字: {}", key);
        List<VenueOption> options = venueService.listVenueOptions(key);
        // Service 返回 null 或空列表都是有效情况，直接返回成功
        if (options == null || options.isEmpty()) {
            log.info("查询场地选项成功，未找到匹配项。关键字: {}", key);
        }
        return Result.success(options);
    }

    // 根据一组venueId查询场地列表
    @PostMapping("/listByIds") // 映射 POST 请求到 /api/venues/listByIds 路径
    public Result<List<Venue>> listVenuesByIds(@RequestBody List<String> venueIds) {
        // 记录请求参数，注意列表可能很大，日志中打印全部可能不合适，这里仅作示例
        log.info("接收到根据ID查询场地列表请求，ID列表（共{}个）: {}", venueIds != null ? venueIds.size() : 0, venueIds);

        // 简单的参数校验：如果传入的ID列表为空或null，直接返回空列表
        if (venueIds == null || venueIds.isEmpty()) {
            return Result.validateError("参数有误,不能为空");
        }

        List<Venue> venues = venueService.listVenuesByIds(venueIds);
        if (venues == null || venues.isEmpty()) {
            return Result.failure("未查到任何数据,请重试!");
        }
        return Result.success(venues);
    }

    // es搜索场地 (Service层目前返回null，这里也做相应处理)
    @PostMapping("/search")
    public Result<List<ESVenue>> searchVenue(VenueSearchRequest venueSearchRequest) {
        log.info("接收到搜索场地选项请求，关键字: {}", venueSearchRequest);
        List<ESVenue> list = esService.searchVenue(venueSearchRequest);
        return Result.success(list);
    }
}
