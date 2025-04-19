package com.example.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.event.decorator.BasketballStatisticsDecorator;
import com.example.event.decorator.BasketballTournamentDecorator;
import com.example.event.entity.BasketballEvent;
import com.example.event.entity.BasketballMatchRecord;
import com.example.event.entity.BasketballPlayer;
import com.example.event.entity.BasketballTeam;
import com.example.event.mapper.BasketballEventMapper;
import com.example.event.mapper.BasketballMatchRecordMapper;
import com.example.event.mapper.BasketballPlayerMapper;
import com.example.event.mapper.BasketballTeamMapper;
import com.example.event.service.EventService;
import com.example.event.strategy.BasketballScoringStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 篮球赛事服务实现
 */
@Service
@RequiredArgsConstructor
public class BasketballEventServiceImpl implements EventService<BasketballEvent> {
    
    @Autowired
    private BasketballEventMapper basketballEventMapper;
    
    @Autowired
    private BasketballTeamMapper basketballTeamMapper;
    
    @Autowired
    private BasketballPlayerMapper basketballPlayerMapper;
    
    @Autowired
    private BasketballMatchRecordMapper basketballMatchRecordMapper;
    
    @Autowired(required = false)
    private BasketballScoringStrategy basketballScoringStrategy;
    
    @Override
    @Transactional
    public BasketballEvent createEvent(BasketballEvent event) {
        // 设置默认值
        if (event.getQuartersPerMatch() == null) {
            event.setQuartersPerMatch(4); // 默认4节
        }
        if (event.getMinutesPerQuarter() == null) {
            event.setMinutesPerQuarter(12); // 默认每节12分钟
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        event.setCreateTime(now);
        event.setUpdateTime(now);
        event.setStatus(0); // 未开始
        event.setIsDeleted(0); // 未删除
        
        // 保存到数据库
        basketballEventMapper.insert(event);
        
        return event;
    }

    @Override
    @Transactional
    public BasketballEvent updateEvent(BasketballEvent event) {
        // 更新时间
        event.setUpdateTime(LocalDateTime.now());
        
        // 更新到数据库
        basketballEventMapper.updateById(event);
        
        return event;
    }

    @Override
    @Transactional
    public boolean deleteEvent(Long eventId) {
        // 逻辑删除
        BasketballEvent event = new BasketballEvent();
        event.setId(eventId);
        event.setIsDeleted(1); // 已删除
        event.setUpdateTime(LocalDateTime.now());
        
        // 更新到数据库
        return basketballEventMapper.updateById(event) > 0;
    }

    @Override
    public BasketballEvent getEventById(Long eventId) {
        // 从数据库查询
        return basketballEventMapper.selectById(eventId);
    }

    @Override
    @Transactional
    public boolean startEvent(Long eventId) {
        BasketballEvent event = getEventById(eventId);
        if (event == null) {
            return false;
        }
        
        // 更新赛事状态为进行中
        event.setStatus(1); // 进行中
        event.setStartTime(LocalDateTime.now());
        
        // 更新到数据库
        return basketballEventMapper.updateById(event) > 0;
    }

    @Override
    @Transactional
    public boolean endEvent(Long eventId) {
        BasketballEvent event = getEventById(eventId);
        if (event == null) {
            return false;
        }
        
        // 更新赛事状态为已结束
        event.setStatus(2); // 已结束
        event.setEndTime(LocalDateTime.now());
        
        // 更新到数据库
        return basketballEventMapper.updateById(event) > 0;
    }
    
    /**
     * 获取篮球赛事列表
     */
    public List<BasketballEvent> getEventList() {
        // 构建查询条件
        LambdaQueryWrapper<BasketballEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasketballEvent::getIsDeleted, 0); // 未删除的
        queryWrapper.orderByDesc(BasketballEvent::getCreateTime); // 按创建时间降序
        
        // 查询数据库
        return basketballEventMapper.selectList(queryWrapper);
    }
    
    /**
     * 开始某一节比赛
     */
    @Transactional
    public boolean startQuarter(Long eventId, Integer quarterId) {
        // 获取赛事信息
        BasketballEvent event = getEventById(eventId);
        if (event == null || event.getStatus() != 1) { // 赛事必须处于进行中状态
            return false;
        }
        
        // 获取比赛记录
        LambdaQueryWrapper<BasketballMatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasketballMatchRecord::getEventId, eventId);
        queryWrapper.eq(BasketballMatchRecord::getStatus, 1); // 进行中的比赛
        BasketballMatchRecord matchRecord = basketballMatchRecordMapper.selectOne(queryWrapper);
        
        if (matchRecord == null) {
            return false;
        }
        
        // 更新当前节次
        matchRecord.setCurrentQuarter(quarterId);
        matchRecord.setUpdateTime(LocalDateTime.now());
        
        // 更新到数据库
        return basketballMatchRecordMapper.updateById(matchRecord) > 0;
    }
    
    /**
     * 结束某一节比赛
     */
    @Transactional
    public boolean endQuarter(Long eventId, Integer quarterId) {
        // 获取赛事信息
        BasketballEvent event = getEventById(eventId);
        if (event == null || event.getStatus() != 1) { // 赛事必须处于进行中状态
            return false;
        }
        
        // 获取比赛记录
        LambdaQueryWrapper<BasketballMatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasketballMatchRecord::getEventId, eventId);
        queryWrapper.eq(BasketballMatchRecord::getStatus, 1); // 进行中的比赛
        BasketballMatchRecord matchRecord = basketballMatchRecordMapper.selectOne(queryWrapper);
        
        if (matchRecord == null || matchRecord.getCurrentQuarter() != quarterId) {
            return false;
        }
        
        // 如果是最后一节，并且比分相同，则进入加时赛
        if (quarterId == event.getQuartersPerMatch() && matchRecord.getHomeScore().equals(matchRecord.getAwayScore())) {
            // 设置加时赛
            matchRecord.setCurrentQuarter(event.getQuartersPerMatch() + 1); // 加时赛
        } else if (quarterId == event.getQuartersPerMatch()) {
            // 最后一节结束，比赛结束
            matchRecord.setStatus(2); // 已结束
            matchRecord.setCurrentQuarter(null);
        } else {
            // 准备下一节
            matchRecord.setCurrentQuarter(quarterId + 1);
        }
        
        matchRecord.setUpdateTime(LocalDateTime.now());
        
        // 更新到数据库
        return basketballMatchRecordMapper.updateById(matchRecord) > 0;
    }
    
    /**
     * 记录得分
     */
    @Transactional
    public boolean recordScore(Long eventId, Long teamId, Long playerId, String scoreType) {
        // 获取赛事信息
        BasketballEvent event = getEventById(eventId);
        if (event == null || event.getStatus() != 1) { // 赛事必须处于进行中状态
            return false;
        }
        
        // 获取比赛记录
        LambdaQueryWrapper<BasketballMatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasketballMatchRecord::getEventId, eventId);
        queryWrapper.eq(BasketballMatchRecord::getStatus, 1); // 进行中的比赛
        BasketballMatchRecord matchRecord = basketballMatchRecordMapper.selectOne(queryWrapper);
        
        if (matchRecord == null) {
            return false;
        }
        
        // 计算得分
        int scoreValue = 0;
        switch (scoreType) {
            case "2分":
                scoreValue = 2;
                break;
            case "3分":
                scoreValue = 3;
                break;
            case "罚球":
                scoreValue = 1;
                break;
            default:
                return false;
        }
        
        // 更新比赛记录中的得分
        if (teamId.equals(matchRecord.getHomeTeamId())) {
            matchRecord.setHomeScore(matchRecord.getHomeScore() + scoreValue);
        } else if (teamId.equals(matchRecord.getAwayTeamId())) {
            matchRecord.setAwayScore(matchRecord.getAwayScore() + scoreValue);
        } else {
            return false;
        }
        
        matchRecord.setUpdateTime(LocalDateTime.now());
        
        // 更新比赛记录
        basketballMatchRecordMapper.updateById(matchRecord);
        
        return true;
    }
    
    /**
     * 记录犯规
     */
    @Transactional
    public boolean recordFoul(Long eventId, Long teamId, Long playerId, String foulType) {
        // 获取赛事信息
        BasketballEvent event = getEventById(eventId);
        if (event == null || event.getStatus() != 1) { // 赛事必须处于进行中状态
            return false;
        }
        
        // 获取比赛记录
        LambdaQueryWrapper<BasketballMatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasketballMatchRecord::getEventId, eventId);
        queryWrapper.eq(BasketballMatchRecord::getStatus, 1); // 进行中的比赛
        BasketballMatchRecord matchRecord = basketballMatchRecordMapper.selectOne(queryWrapper);
        
        if (matchRecord == null) {
            return false;
        }
        
        // 记录犯规信息
        // 这里可以添加犯规记录的逻辑
        
        return true;
    }
    
    /**
     * 获取球队统计数据
     */
    public Map<String, Object> getTeamStatistics(Long eventId, Long teamId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取赛事信息
        BasketballEvent event = getEventById(eventId);
        if (event == null) {
            return statistics;
        }
        
        // 获取球队信息
        BasketballTeam team = basketballTeamMapper.selectById(teamId);
        if (team == null) {
            return statistics;
        }
        
        // 获取比赛记录
        LambdaQueryWrapper<BasketballMatchRecord> matchQueryWrapper = new LambdaQueryWrapper<>();
        matchQueryWrapper.eq(BasketballMatchRecord::getEventId, eventId);
        matchQueryWrapper.and(wrapper -> wrapper.eq(BasketballMatchRecord::getHomeTeamId, teamId)
                .or().eq(BasketballMatchRecord::getAwayTeamId, teamId));
        List<BasketballMatchRecord> matchRecords = basketballMatchRecordMapper.selectList(matchQueryWrapper);
        
        // 基本信息
        statistics.put("teamId", team.getId());
        statistics.put("teamName", team.getName());
        statistics.put("logo", team.getLogo());
        statistics.put("coach", team.getCoach());
        
        // 比赛统计
        int totalMatches = matchRecords.size();
        int wins = 0;
        int totalPoints = 0;
        
        for (BasketballMatchRecord match : matchRecords) {
            boolean isHomeTeam = match.getHomeTeamId().equals(teamId);
            int teamScore = isHomeTeam ? match.getHomeScore() : match.getAwayScore();
            int opponentScore = isHomeTeam ? match.getAwayScore() : match.getHomeScore();
            
            totalPoints += teamScore;
            
            // 如果比赛已结束，计算胜负
            if (match.getStatus() == 2) {
                if (teamScore > opponentScore) {
                    wins++;
                }
            }
        }
        
        statistics.put("totalMatches", totalMatches);
        statistics.put("wins", wins);
        statistics.put("losses", totalMatches - wins);
        statistics.put("winRate", totalMatches > 0 ? (double) wins / totalMatches : 0);
        statistics.put("totalPoints", totalPoints);
        statistics.put("avgPointsPerGame", totalMatches > 0 ? (double) totalPoints / totalMatches : 0);
        
        // 获取球队球员列表
        LambdaQueryWrapper<BasketballPlayer> playerQueryWrapper = new LambdaQueryWrapper<>();
        playerQueryWrapper.eq(BasketballPlayer::getTeamId, teamId);
        playerQueryWrapper.eq(BasketballPlayer::getIsDeleted, 0);
        List<BasketballPlayer> players = basketballPlayerMapper.selectList(playerQueryWrapper);
        
        statistics.put("playerCount", players.size());
        statistics.put("players", players);
        
        return statistics;
    }
    
    /**
     * 获取球员统计数据
     */
    public Map<String, Object> getPlayerStatistics(Long eventId, Long playerId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取赛事信息
        BasketballEvent event = getEventById(eventId);
        if (event == null) {
            return statistics;
        }
        
        // 获取球员信息
        BasketballPlayer player = basketballPlayerMapper.selectById(playerId);
        if (player == null) {
            return statistics;
        }
        
        // 获取球队信息
        BasketballTeam team = basketballTeamMapper.selectById(player.getTeamId());
        
        // 基本信息
        statistics.put("playerId", player.getId());
        statistics.put("playerName", player.getName());
        statistics.put("jerseyNumber", player.getJerseyNumber());
        statistics.put("position", player.getPosition());
        statistics.put("teamId", player.getTeamId());
        statistics.put("teamName", team != null ? team.getName() : null);
        statistics.put("photo", player.getPhoto());
        
        // 获取比赛记录
        LambdaQueryWrapper<BasketballMatchRecord> matchQueryWrapper = new LambdaQueryWrapper<>();
        matchQueryWrapper.eq(BasketballMatchRecord::getEventId, eventId);
        matchQueryWrapper.and(wrapper -> wrapper.eq(BasketballMatchRecord::getHomeTeamId, player.getTeamId())
                .or().eq(BasketballMatchRecord::getAwayTeamId, player.getTeamId()));
        List<BasketballMatchRecord> matchRecords = basketballMatchRecordMapper.selectList(matchQueryWrapper);
        
        // 比赛统计
        statistics.put("totalMatches", matchRecords.size());
        
        // 模拟球员统计数据
        statistics.put("totalPoints", 0);
        statistics.put("avgPointsPerGame", 0.0);
        statistics.put("totalRebounds", 0);
        statistics.put("totalAssists", 0);
        statistics.put("totalSteals", 0);
        statistics.put("totalBlocks", 0);
        statistics.put("fieldGoalPercentage", 0.0);
        statistics.put("threePointPercentage", 0.0);
        statistics.put("freeThrowPercentage", 0.0);
        
        return statistics;
    }
    
    /**
     * 篮球特有方法：安排小组赛
     */
    public boolean arrangeGroupStage(Long eventId) {
        BasketballEvent event = getEventById(eventId);
        if (event == null) {
            return false;
        }
        
        // 使用装饰器模式添加比赛安排功能
        BasketballTournamentDecorator tournamentDecorator = new BasketballTournamentDecorator(event);
        
        // 模拟队伍ID列表
        // List<Long> teamIds = getEventTeams(eventId);
        
        // 安排小组赛
        // int groupCount = event.getTeamCount() / 4; // 假设每组4支队伍
        // return tournamentDecorator.arrangeGroupStage(groupCount, teamIds);
        
        return true;
    }
    
    /**
     * 篮球特有方法：记录比赛统计数据
     */
    public boolean recordMatchStatistics(Long matchId, Long playerId, String statisticType, Integer value) {
        // 获取比赛所属的赛事
        // Long eventId = getEventIdByMatchId(matchId);
        // BasketballEvent event = getEventById(eventId);
        BasketballEvent event = new BasketballEvent(); // 模拟
        
        if (event == null) {
            return false;
        }
        
        // 使用装饰器模式添加统计数据功能
        BasketballStatisticsDecorator statisticsDecorator = new BasketballStatisticsDecorator(event);
        
        // 根据统计类型记录数据
        switch (statisticType) {
            case "score":
                statisticsDecorator.recordPlayerScore(playerId, value);
                break;
            case "rebound":
                statisticsDecorator.recordRebound(playerId);
                break;
            case "assist":
                statisticsDecorator.recordAssist(playerId);
                break;
            case "steal":
                statisticsDecorator.recordSteal(playerId);
                break;
            case "block":
                statisticsDecorator.recordBlock(playerId);
                break;
            default:
                return false;
        }
        
        // 保存统计数据到数据库
        // saveStatistics(statisticsDecorator, matchId);
        
        return true;
    }
    
    /**
     * 篮球特有方法：获取球员统计数据
     */
    public Integer getPlayerStatistics(Long eventId, Long playerId, String statisticType) {
        BasketballEvent event = getEventById(eventId);
        if (event == null) {
            return 0;
        }
        
        // 使用装饰器模式添加统计数据功能
        BasketballStatisticsDecorator statisticsDecorator = new BasketballStatisticsDecorator(event);
        
        // 从数据库加载统计数据
        // loadStatistics(statisticsDecorator, eventId);
        
        // 根据统计类型获取数据
        switch (statisticType) {
            case "score":
                return statisticsDecorator.getPlayerTotalScore(playerId);
            case "rebound":
                return statisticsDecorator.getPlayerRebounds().getOrDefault(playerId, 0);
            case "assist":
                return statisticsDecorator.getPlayerAssists().getOrDefault(playerId, 0);
            case "steal":
                return statisticsDecorator.getPlayerSteals().getOrDefault(playerId, 0);
            case "block":
                return statisticsDecorator.getPlayerBlocks().getOrDefault(playerId, 0);
            default:
                return 0;
        }
    }
    
    /**
     * 篮球特有方法：计算比赛得分
     */
    public boolean calculateMatchScore(Long matchId) {
        // 使用策略模式计算得分
        return basketballScoringStrategy.calculateScore(matchId);
    }
    

}