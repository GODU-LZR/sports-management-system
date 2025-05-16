package com.example.event.temp.Contro;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.event.DTO.vo.SportVO;
import com.example.event.Enum.GameRole;
import com.example.event.dao.GameRoleRecord;
import com.example.event.dao.temp.Gameandteam;
import com.example.event.mapper.GameRoleRecordMapper;
import com.example.event.mapper.basketball.BasketballTeamMapper;
import com.example.event.mapper.GameandteamMapper;
import com.example.event.service.SportService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Added import
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections; // Added import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/temp/options/sport") // Base path for all methods in this controller
public class tempOpController {
    @Autowired
    private SportService sportService;
    @Autowired
    private GameRoleRecordMapper gameRoleRecordMapper;
    @Autowired
    private GameandteamMapper gameandteamMapper;
    @Autowired
    private BasketballTeamMapper basketballTeamMapper;

    // Existing methods (unchanged)
    @GetMapping("/type")
    public Result<List<SportVO>> getSport1() {
        List<SportVO> sportData = sportService.getSportWithGames();
        return Result.success(sportData);
    }

    @GetMapping("/venue")
    public Result<?> getVenue() {
        Map<String, Object> maps = new HashMap<>();
        maps.put("北京", "鸟巢");
        maps.put("广州", "霍英东体育馆");
        return Result.success(maps);
    }

    @GetMapping("/users/responsible-person") // Full path: /temp/options/sport/users/responsible-person
    public Result<List<GameRoleRecord>> getResponsible() {
        List<GameRoleRecord> gameRoleList = gameRoleRecordMapper.selectList(new QueryWrapper<GameRoleRecord>()
                .eq("role", GameRole.MANAGER.getCode())); // 使用枚举的 getCode()
        return Result.success(gameRoleList);
    }


    @GetMapping("/referees")
    public Result<?> getRefereeOptionsByKeyword() {

        List<GameRoleRecord> gameRoleList = gameRoleRecordMapper.selectList(new QueryWrapper<GameRoleRecord>()
                .eq("role", GameRole.REFEREE.getCode())); // 使用枚举的 getCode()
        return Result.success(gameRoleList);

    }


    @GetMapping("/users/player")
    public Result<?> getPlayerOptionsByKeyword() {

        List<GameRoleRecord> gameRoleList = gameRoleRecordMapper.selectList(new QueryWrapper<GameRoleRecord>()
                .eq("role", GameRole.PLAYER.getCode())); // 使用枚举的 getCode()
        return Result.success(gameRoleList);
    }

    /**
     * 1.6 GET /options/my-teams : 获取所有的我所在的队伍的选项
     * Full path resolved: /temp/options/sport/options/my-teams
     */
    @GetMapping("/my-teams")
    public Result<?> getMyTeamOptions(
            @Parameter(hidden = true) UserConstant userConstant
    ) {
        Long userid = userConstant.getUserId();

        QueryWrapper<GameRoleRecord> playerGamesQuery = new QueryWrapper<>();
        playerGamesQuery.eq("user_id", userid)
                .eq("role", GameRole.PLAYER.getCode()); // Using getCode() as in your original snippet
        List<GameRoleRecord> userPlayerRoles = gameRoleRecordMapper.selectList(playerGamesQuery);

        if (userPlayerRoles == null || userPlayerRoles.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<Long> gameIds = userPlayerRoles.stream()
                .map(GameRoleRecord::getGameId)
                .distinct() // Ensure unique game IDs
                .collect(Collectors.toList());
   if (gameIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        QueryWrapper<GameRoleRecord> teamMembersQuery = new QueryWrapper<>();
        teamMembersQuery.in("game_id", gameIds);
        List<GameRoleRecord> gameRoleList = gameRoleRecordMapper.selectList(teamMembersQuery);

        return Result.success(gameRoleList);
    }

    /**
     * 1.7 GET /options/game/{gameId}/enrolled-teams : 获取指定赛事已报名队伍选项
     * Full path resolved: /temp/options/sport/options/game/{gameId}/enrolled-teams
     *
     * @param gameId The ID of the game for which to fetch enrolled teams.
     */
    @GetMapping("/game/{gameId}/enrolled-teams")
    public Result<?> getEnrolledTeamOptionsForGame(@PathVariable Long gameId) {
        QueryWrapper<Gameandteam> tgameandteamMapper = new QueryWrapper<>();
        tgameandteamMapper.in("game_id",gameId);
         List<Gameandteam> gameandteams=  gameandteamMapper.selectList(tgameandteamMapper);
        return Result.success(gameandteams);

    }
    @Data
    class team{
        List<GameRoleRecord>gameRoleRecordList;


    }
}