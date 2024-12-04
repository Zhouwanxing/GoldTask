package com.zhou.goldtask.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Mp4LikeDto {
    private String path;
    private int page;

    @JsonProperty("isShowBest")
    private boolean isShowBest;
}