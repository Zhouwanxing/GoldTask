package com.zhou.goldtask.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@ToString
@Document("my_gold")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GoldEntity {
    @Id
    private String _id;
    private int zdf = 0;
    private int zss = 0;
    private int ccb = 0;
}