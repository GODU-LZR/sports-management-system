package com.example.equipment.pojo;

import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeDamage {

    private Long equipmentId;

    private Integer conditionScore;   //完好程度

}
