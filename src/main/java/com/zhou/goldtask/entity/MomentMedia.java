package com.zhou.goldtask.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomentMedia {
    private String fileId;
    private String kind;
    private String contentType;
    private String name;
    /** 列表用的小图。为空时客户端退回原图。 */
    private String thumbFileId;
}
