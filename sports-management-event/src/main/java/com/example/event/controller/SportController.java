package com.example.event.controller;
import com.example.common.response.Result;
import com.example.event.DTO.vo.SportVO;
import com.example.event.service.SportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 体育项目控制器
 */
@RestController
@RequestMapping("/sport")
public class SportController {

    @Autowired
    private SportService sportService;

    /**
     * 获取带赛事的赛事项目数据
     *
     * @return 带赛事的体育项目列表
     */

    @GetMapping("/type")
    public Result<List<SportVO>> getSport1() {
        List<SportVO> sportData = sportService.getSportWithGames();
        return Result.success(sportData);
    }

    /**
     * 获取不带赛事列表的赛事项目数据
     *
     * @return 体育项目列表
     */
    @GetMapping("/type/list")
    public Result<List<SportVO>> getSport2() {
        List<SportVO> sportData = sportService.getSportList();
        return Result.success(sportData);
    }

    /**
     * 获取赛事列表数据
     *
     * @param sportId 赛事项目id
     * @param page 页码
     * @param filter 过滤条件
     * @return 赛事列表数据
     */
    @GetMapping("/competition/{sportId}")
    public Result<Map<String, Object>> getCompetitionData(
            @PathVariable Integer sportId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) Map<String, Object> filter) {
        Map<String, Object> competitionData = sportService.getCompetitionData(sportId, page, filter);
        return Result.success(competitionData);
    }

    /**
     * 获取赛事列表的总条数
     *
     * @param sportId 赛事项目id
     * @return 总条数
     */
    @GetMapping("/competition/count/{sportId}")
    public Result<Integer> getCompetitionPage(@PathVariable Integer sportId) {
        Integer count = sportService.getCompetitionCount(sportId);
        return Result.success(count);
    }
}