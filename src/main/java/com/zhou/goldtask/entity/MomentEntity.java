package com.zhou.goldtask.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document("moment")
@CompoundIndexes({
        @CompoundIndex(name = "moment_user_time", def = "{'username': 1, 'createdAt': -1}")
})
public class MomentEntity {
    @Id
    @JsonProperty("_id")
    private String _id;
    private String username;
    private String content;
    private long createdAt;
    private List<MomentMedia> media = new ArrayList<MomentMedia>();
}
