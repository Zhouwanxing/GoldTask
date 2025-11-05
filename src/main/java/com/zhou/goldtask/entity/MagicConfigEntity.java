package com.zhou.goldtask.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@ToString
@Document("my_magic_config")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MagicConfigEntity {
    private String file_path;
    private String file_content;
    
}