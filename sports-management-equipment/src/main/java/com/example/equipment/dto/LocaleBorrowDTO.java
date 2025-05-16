package com.example.equipment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocaleBorrowDTO {

//    private LocalDateTime now;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long equipmentId;

}
