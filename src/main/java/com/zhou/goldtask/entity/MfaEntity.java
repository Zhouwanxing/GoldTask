package com.zhou.goldtask.entity;

import com.zhou.goldtask.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("my_mfa")
@Document("my_mfa")
public class MfaEntity {
    @Id
    private String _id;
    /** Base32 密钥，仅服务端使用，列表接口不返回 */
    private String secret;
    private String issuer;
    private String account;
    private Integer digits;
    private Integer period;
    private String algorithm;
    /** totp / hotp */
    private String type;
    /** HOTP 计数器 */
    private Long counter;
    private Long createTime;
}
