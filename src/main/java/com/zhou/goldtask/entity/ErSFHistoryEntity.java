package com.zhou.goldtask.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@ToString
@Document("my_ersf_history")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ErSFHistoryEntity {
    @Id
    private String _id;
    private double price;
    private String time;
    private String homeId;
}