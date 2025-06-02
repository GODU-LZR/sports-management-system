package com.example.venue.vo;

import com.example.venue.pojo.mysql.Venue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenuePage {
    private Integer total; // 总页数
    private List<Venue> data; // 当前页的场地列表
}
