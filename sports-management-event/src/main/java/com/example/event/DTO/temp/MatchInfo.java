package com.example.event.DTO.temp;

import com.example.event.dao.GameRoleRecord;
import com.example.event.dao.Match;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Data
@Builder
public class MatchInfo {
    private Match match;
    public List<GameRoleRecord> gameRoleRecordList;

}
