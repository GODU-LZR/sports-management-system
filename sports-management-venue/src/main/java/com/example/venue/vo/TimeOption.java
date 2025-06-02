package com.example.venue.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor // 自动生成无参构造函数 (通常是反序列化 JSON 所必需的)
@AllArgsConstructor // 自动生成包含所有字段的构造函数
public class TimeOption {

    // 开始时间
    @JsonFormat(pattern = "HH:mm")
    private LocalDateTime startTime;

    // 结束时间
    @JsonFormat(pattern = "HH:mm")
    private LocalDateTime endTime;
}
