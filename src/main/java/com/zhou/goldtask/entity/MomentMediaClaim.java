package com.zhou.goldtask.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** 附件占用：一个 GridFS 文件最多被一条朋友圈引用。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("moment_media_claim")
public class MomentMediaClaim {
    @Id
    private String _id;
    @Indexed(unique = true)
    private String fileId;
    private String username;
}
