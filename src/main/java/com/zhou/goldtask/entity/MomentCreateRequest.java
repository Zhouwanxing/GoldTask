package com.zhou.goldtask.entity;

import lombok.Data;

import java.util.List;

@Data
public class MomentCreateRequest {
    private String content;
    private List<String> fileIds;
}
