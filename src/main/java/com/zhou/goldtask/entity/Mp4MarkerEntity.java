package com.zhou.goldtask.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mp4MarkerEntity {
    private Double time;
    private Long createdAt;
}
