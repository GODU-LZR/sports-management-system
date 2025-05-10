package com.example.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.event.dao.Game;
import com.example.event.dao.Match;
import com.example.event.mapper.GameMapper;
import com.example.event.mapper.MatchMapper;
import com.example.event.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 赛事服务实现类
 */
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    
    private final GameMapper gameMapper;
    private final MatchMapper matchMapper;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public Map<String, Object> getGameData(Long gameId) {
        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            return null;
        }
        
        return convertGameToMap(game);
    }
    
    @Override
    public List<Map<String, Object>> getMatches(Long gameId) {
        LambdaQueryWrapper<Match> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Match::getGameId, gameId);
        List<Match> matches = matchMapper.selectList(queryWrapper);
        
        return matches.stream()
                .map(this::convertMatchToMap)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> getMyCompetitionData(Integer page, Integer reviewStatus, Long userId) {
        Page<Game> pageParam = new Page<>(page, 10); // 每页10条数据
        IPage<Game> gameIPage = gameMapper.getMyCompetitionData(pageParam, userId, reviewStatus);
        
        return gameIPage.getRecords().stream()
                .map(this::convertGameToMap)
                .collect(Collectors.toList());
    }
    
    @Override
    public Integer getMyCompetitionCount(Integer reviewStatus, Long userId) {
        return gameMapper.getMyCompetitionCount(userId, reviewStatus);
    }
    
    @Override
    public Map<String, Object> getMyGameData(Long gameId, Long userId) {
        Game game = gameMapper.selectById(gameId);
        if (game == null || !game.getCreatorId().equals(userId)) {
            return null;
        }
        
        return convertGameToMap(game);
    }
    
    @Override
    public List<Map<String, Object>> getMyMatches(Long gameId, Long userId) {
        // 先验证赛事是否属于该用户
        Game game = gameMapper.selectById(gameId);
        if (game == null || !game.getCreatorId().equals(userId)) {
            return new ArrayList<>();
        }
        
        // 获取该赛事的所有比赛
        LambdaQueryWrapper<Match> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Match::getGameId, gameId);
        List<Match> matches = matchMapper.selectList(queryWrapper);
        
        return matches.stream()
                .map(this::convertMatchToMap)
                .collect(Collectors.toList());
    }
    
    @Override
    public IPage<Game> getCompetitionData(Integer page, Long sportId, String name, String state, 
                                         LocalDateTime registerTime, LocalDateTime time) {
        Page<Game> pageParam = new Page<>(page, 10); // 每页10条数据
        return gameMapper.getCompetitionData(pageParam, sportId, name, state, registerTime, time);
    }
    
    @Override
    public int getCompetitionCount(Long sportId) {
        return gameMapper.getCompetitionCount(sportId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGame(Game game) {
        // 设置审核状态为待审核
        game.setReviewStatus(0);
        gameMapper.insert(game);
        return game.getGameId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGame(Game game) {
        // 检查赛事是否存在
        Game existingGame = gameMapper.selectById(game.getGameId());
        if (existingGame == null) {
            return false;
        }
        
        // 如果赛事已经审核通过，则不允许修改
        if (existingGame.getReviewStatus() == 1) {
            return false;
        }
        
        // 更新赛事信息
        return gameMapper.updateById(game) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGame(Long gameId) {
        // 检查赛事是否存在
        Game existingGame = gameMapper.selectById(gameId);
        if (existingGame == null) {
            return false;
        }
        
        // 如果赛事已经审核通过，则不允许删除
        if (existingGame.getReviewStatus() == 1) {
            return false;
        }
        
        // 删除赛事关联的所有比赛
        LambdaQueryWrapper<Match> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Match::getGameId, gameId);
        matchMapper.delete(queryWrapper);
        
        // 删除赛事
        return gameMapper.deleteById(gameId) > 0;
    }
    
    @Override
    public boolean updateReviewStatus(Long gameId, Integer reviewStatus) {
        Game game = new Game();
        game.setGameId(gameId);
        game.setReviewStatus(reviewStatus);
        return gameMapper.updateById(game) > 0;
    }
    
    /**
     * 将Game对象转换为Map
     */
    private Map<String, Object> convertGameToMap(Game game) {
        Map<String, Object> result = new HashMap<>();
        result.put("gameId", game.getGameId());
        result.put("name", game.getName());
        result.put("sportId", game.getSportId());
        result.put("sport", game.getSport());
        result.put("responsiblePeople", game.getResponsiblePeople());
        result.put("phone", game.getPhone());
        result.put("registerStartTime", game.getRegisterStartTime() != null ? 
                game.getRegisterStartTime().format(DATE_TIME_FORMATTER) : null);
        result.put("registerEndTime", game.getRegisterEndTime() != null ? 
                game.getRegisterEndTime().format(DATE_TIME_FORMATTER) : null);
        result.put("startTime", game.getStartTime() != null ? 
                game.getStartTime().format(DATE_TIME_FORMATTER) : null);
        result.put("endTime", game.getEndTime() != null ? 
                game.getEndTime().format(DATE_TIME_FORMATTER) : null);
        result.put("note", game.getNote());
        result.put("mode", game.getMode());
        result.put("reviewStatus", game.getReviewStatus());
        result.put("creatorId", game.getCreatorId());
        return result;
    }
    
    /**
     * 将Match对象转换为Map
     */
    private Map<String, Object> convertMatchToMap(Match match) {
        Map<String, Object> result = new HashMap<>();
        result.put("matchId", match.getMatchId());
        result.put("gameId", match.getGameId());
        result.put("sport", match.getSport());
        result.put("awayTeam", match.getAwayTeam());
        result.put("homeTeam", match.getHomeTeam());
        result.put("awayTeamScore", match.getAwayTeamScore());
        result.put("homeTeamScore", match.getHomeTeamScore());
        result.put("venueName", match.getVenueName());
        result.put("startTime", match.getStartTime() != null ? 
                match.getStartTime().format(DATE_TIME_FORMATTER) : null);
        result.put("endTime", match.getEndTime() != null ? 
                match.getEndTime().format(DATE_TIME_FORMATTER) : null);
        result.put("phase", match.getPhase());
        result.put("winner", match.getWinner());
        result.put("responsiblePerson", match.getResponsiblePerson());
        result.put("phone", match.getPhone());
        result.put("note", match.getNote());
        result.put("refereeName", match.getRefereeName());
        return result;
    }
}