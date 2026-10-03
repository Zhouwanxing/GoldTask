package com.zhou.goldtask.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 一台设备的弹珠库存。sid 作 _id。
 * count 为空表示设备连上过、但还没上报过数量；这时管理员加的珠先记在 pending。
 */
@Data
@Document("marble_bank")
public class MarbleBankEntity {
    @Id
    private String id;
    private String name;
    /** 已确定的库存；null 表示还没上报。 */
    private Integer count;
    /** count 还没确定时，管理员已经加上、等设备下次上报时并进去的数量。 */
    private int pending;
    /** 最近一次生效的客户端同步序号，相同序号重试不会重复加减。 */
    private String lastSyncId;
    private long updatedAt;
}
