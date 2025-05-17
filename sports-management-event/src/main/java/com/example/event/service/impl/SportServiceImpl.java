package com.example.event.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.example.event.DTO.vo.GameVO;
import com.example.event.DTO.vo.SportVO;
import com.example.event.dao.Game;
import com.example.event.dao.Sport;
import com.example.event.mapper.GameMapper;
import com.example.event.mapper.SportMapper;
import com.example.event.service.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 体育项目服务实现类
 */
@Service
@RequiredArgsConstructor
public class SportServiceImpl implements SportService {

    private final SportMapper sportMapper;
    private final GameMapper gameMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取带赛事列表的体育项目数据
     * @return 体育项目VO列表，每个项目包含其关联的赛事列表
     */
    @Override
    public List<SportVO> getSportWithGames() {
        // 获取带赛事的体育项目数据
        List<Map<String, Object>> sportWithGames = sportMapper.getSportWithGames();

        // 转换为前端需要的格式
        Map<Long, SportVO> sportMap = new HashMap<>();

        for (Map<String, Object> item : sportWithGames) {
            Long sportId = ((Number) item.get("sport_id")).longValue();
            String sportName = (String) item.get("name");
            Long gameId = item.get("game_id") != null ? ((Number) item.get("game_id")).longValue() : null;
            String gameName = (String) item.get("game_name");

            // 如果sportMap中不存在该体育项目，则创建一个新的SportVO对象
            if (!sportMap.containsKey(sportId)) {
                SportVO sportVO = new SportVO();
                sportVO.setSportId(sportId);
                sportVO.setName(sportName);
                sportVO.setGames(new ArrayList<>());
                sportMap.put(sportId, sportVO);
            }

            // 如果gameId不为空，则添加到对应体育项目的games列表中
            if (gameId != null && gameName != null) {
                GameVO gameVO = convertFrom(item); // 使用新的转换方法
                sportMap.get(sportId).getGames().add(gameVO);
            }
        }

        // 返回结果
        return new ArrayList<>(sportMap.values());
    }

    /**
     * 获取不带赛事列表的体育项目数据
     * @return 体育项目VO列表
     */
    @Override
    public List<SportVO> getSportList() {
        // 获取不带赛事列表的体育项目数据
        List<Sport> sportList = sportMapper.getSportList();

        // 转换为前端需要的格式
        return sportList.stream().map(sport -> {
            SportVO sportVO = new SportVO();
            BeanUtils.copyProperties(sport, sportVO);
            return sportVO;
        }).collect(Collectors.toList());
    }
    private static String formatLocalDateTime(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else if (obj instanceof String) {
            return (String) obj; // 如果已经是字符串格式
        }
        return null;
    }

    /**
     * 获取赛事分页数据
     * @param sportId 体育项目ID
     * @param page 页码
     * @param filter 过滤条件(name:赛事名称, state:赛事状态, registerTime:报名时间, time:比赛时间)
     * @return 包含分页信息的Map(records:赛事列表, total:总数, pages:总页数, current:当前页)
     */
    @Override
    public Map<String, Object> getCompetitionData(Integer sportId, Integer page, Map<String, Object> filter) {
        // 处理过滤条件
        String name = filter != null && filter.containsKey("name") ? (String) filter.get("name") : null;
        String state = filter != null && filter.containsKey("state") ? (String) filter.get("state") : null;

        // 处理时间过滤条件
        LocalDateTime registerTime = null;
        if (filter != null && filter.containsKey("registerTime")) {
            String registerTimeStr = (String) filter.get("registerTime");
            if (registerTimeStr != null && !registerTimeStr.isEmpty()) {
                registerTime = LocalDateTime.parse(registerTimeStr, DATE_TIME_FORMATTER);
            }
        }

        LocalDateTime time = null;
        if (filter != null && filter.containsKey("time")) {
            String timeStr = (String) filter.get("time");
            if (timeStr != null && !timeStr.isEmpty()) {
                time = LocalDateTime.parse(timeStr, DATE_TIME_FORMATTER);
            }
        }

        // 调用GameMapper获取分页数据
        IPage<Game> gameIPage = gameMapper.getCompetitionData(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, 10),
                sportId.longValue(),
                name,
                state,
                registerTime,
                time
        );

        // 转换为前端需要的格式
        List<Map<String, Object>> records = gameIPage.getRecords().stream()
                .map(this::convertGameToMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", gameIPage.getTotal());
        result.put("pages", gameIPage.getPages());
        result.put("current", gameIPage.getCurrent());

        return result;
    }

    @Override
    public Integer getCompetitionCount(Integer sportId, Map<String, Object> filter) {
        return gameMapper.getCompetitionCount(sportId.longValue());
    }

    public static GameVO convertFrom(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        GameVO vo = new GameVO();

        // 设置 gameId
        if (map.get("game_id") != null) {
            vo.setGameId(((Number) map.get("game_id")).longValue());
        }

        // 设置 name
        vo.setName((String) map.get("game_name")); // 注意字段名是否正确，比如"game_name"

        // 设置 sport
        vo.setSport((String) map.get("sport"));

        // 设置负责人
        vo.setResponsiblePeople((String) map.get("responsible_people"));

        // 设置电话
        vo.setPhone((String) map.get("phone"));

        // 设置时间字段
        vo.setRegisterStartTime(formatLocalDateTime(map.get("register_start_time")));
        vo.setRegisterEndTime(formatLocalDateTime(map.get("register_end_time")));
        vo.setStartTime(formatLocalDateTime(map.get("start_time")));
        vo.setEndTime(formatLocalDateTime(map.get("end_time")));

        // 设置备注和模式
        vo.setNote((String) map.get("note"));
        vo.setMode((Integer) map.get("mode"));

        return vo;
    }

    // 辅助方法：处理 LocalDateTime 或 String 类型的时间字段


    /**
     * 将Game实体转换为Map
     */
    private Map<String, Object> convertGameToMap(Game game) {
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", game.getGameId());
        map.put("name", game.getName());
        map.put("sportId", game.getSportId());
        map.put("responsiblePeople", game.getResponsiblePeople());
        map.put("registerStartTime", game.getRegisterStartTime());
        map.put("registerEndTime", game.getRegisterEndTime());
        map.put("startTime", game.getStartTime());
        map.put("endTime", game.getEndTime());


        // 计算赛事状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(game.getRegisterStartTime())) {
            map.put("state", "不可报名");
        } else if (now.isAfter(game.getRegisterStartTime()) && now.isBefore(game.getRegisterEndTime())) {
            map.put("state", "可报名");
        } else if (now.isAfter(game.getRegisterEndTime()) && now.isBefore(game.getStartTime())) {
            map.put("state", "未开始");
        } else if (now.isAfter(game.getStartTime()) && now.isBefore(game.getEndTime())) {
            map.put("state", "正在举行");
        } else {
            map.put("state", "已结束");
        }

        return map;
    }
}