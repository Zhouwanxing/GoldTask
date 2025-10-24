package com.zhou.goldtask.entity;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErPageFormEntity {
    private Integer areaMin;
    private Integer areaMax;
    private Integer priceMin;
    private Integer priceMax;
    private String sortKey;
    private Integer sortValue;
    private Integer showFloor;
    private Integer showLike;
}