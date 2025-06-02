package com.example.venue.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReplaceRequest {

    @NotBlank(message = "订单编号不能为空")
    private String orderId;

    @NotBlank(message = "场地编号不能为空")
    private String venueId;

    @NotBlank(message = "租借开始时间禁止为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime startTime;

    @NotBlank(message = "租借结束时间禁止为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime endTime;


    // 校验时间规则:年份要求今年或每年之内、日期要求在今天之后、时间8:00-21:00、分钟数只能为00或30、且endTime要求大于startTime
    @AssertTrue(message = "时间范围或格式无效")
    private boolean isTimeValid() {
        LocalDate today = LocalDate.now();
        LocalDate nextYear = today.plusYears(1); // 明年日期
        LocalTime minTime = LocalTime.of(8, 0);
        LocalTime maxTime = LocalTime.of(21, 0);

        // 1. 校验日期 ≥ 今天且在今年或明年
        if (startTime.toLocalDate().isBefore(today) ||
                endTime.toLocalDate().isBefore(today) ||
                (startTime.getYear() != today.getYear() && startTime.getYear() != nextYear.getYear()) ||
                (endTime.getYear() != today.getYear() && endTime.getYear() != nextYear.getYear())) {
            return false;
        }

        // 2. 校验时间在 8:00-21:00 且分钟为 00 或 30
        if (startTime.toLocalTime().isBefore(minTime) ||
                startTime.toLocalTime().isAfter(maxTime) ||
                endTime.toLocalTime().isBefore(minTime) ||
                endTime.toLocalTime().isAfter(maxTime) ||
                startTime.getMinute() % 30 != 0 ||
                endTime.getMinute() % 30 != 0) {
            return false;
        }

        // 3. 校验 endTime > startTime
        return endTime.isAfter(startTime);
    }
}
