package com.example.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplaceVenueDto {

    // 场地编号
    private String sportId;

    // 场地租借开始时间
    private LocalDateTime startTime;

    // 场地租借结束时间
    private LocalDateTime endTime;
}
