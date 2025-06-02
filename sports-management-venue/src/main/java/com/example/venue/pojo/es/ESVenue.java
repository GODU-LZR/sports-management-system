package com.example.venue.pojo.es;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.geo.GeoPoint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ESVenue {

    private String venueId;

    private String name;

    private GeoPoint location; // 使用ES原生类型
}
