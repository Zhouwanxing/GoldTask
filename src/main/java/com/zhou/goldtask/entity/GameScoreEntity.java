package com.zhou.goldtask.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 小游戏成绩。同一设备同一游戏只留一条最高分记录,排行榜即各设备最高分的全网排名。
 */
@Data
@Builder
@ToString
@Document("game_score")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GameScoreEntity {
    @Id
    private String _id;
    /** 游戏标识:twenty48 / block / matrix。 */
    private String game;
    /** 设备标识(iOS identifierForVendor)。 */
    private String sid;
    /** 设备名,排行榜展示用。 */
    private String name;
    /** 成绩。2048/方块消除是分数,记忆矩阵是关卡数。 */
    private int score;
    /** 最近一次刷新纪录的时间戳(毫秒),同分时先到的排前。 */
    private long updatedAt;
}
