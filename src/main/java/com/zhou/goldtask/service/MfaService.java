package com.zhou.goldtask.service;

import cn.hutool.core.codec.Base32;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.digest.otp.HOTP;
import cn.hutool.crypto.digest.otp.TOTP;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.MfaEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class MfaService {
    private static final String COLLECTION = "my_mfa";
    private static final Set<Integer> VALID_DIGITS = new HashSet<>(Arrays.asList(6, 7, 8));

    @Resource
    private MongoTemplate mongoTemplate;

    public List<JSONObject> list() {
        Query query = new Query().with(Sort.by(Sort.Direction.ASC, "createTime"));
        List<MfaEntity> entities = mongoTemplate.find(query, MfaEntity.class, COLLECTION);
        List<JSONObject> result = new ArrayList<>();
        for (MfaEntity entity : entities) {
            result.add(toView(entity));
        }
        return result;
    }

    public void saveOne(JSONObject body) {
        if (body == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        MfaEntity entity;
        String uri = body.getStr("uri");
        if (StrUtil.isNotBlank(uri)) {
            entity = parseOtpauthUri(uri);
        } else {
            entity = fromManual(body);
        }
        entity.set_id(IdUtil.objectId());
        entity.setCreateTime(System.currentTimeMillis());
        // 先校验密钥可解码且能生成验证码
        generateCode(entity);
        mongoTemplate.insert(entity, COLLECTION);
    }

    public void deleteOne(String id) {
        if (StrUtil.isBlank(id)) {
            throw new IllegalArgumentException("id 不能为空");
        }
        Query query = new Query(Criteria.where("_id").is(id));
        long n = mongoTemplate.remove(query, COLLECTION).getDeletedCount();
        if (n == 0) {
            throw new IllegalArgumentException("条目不存在");
        }
    }

    private JSONObject toView(MfaEntity entity) {
        int period = entity.getPeriod() != null && entity.getPeriod() > 0 ? entity.getPeriod() : 30;
        int digits = entity.getDigits() != null ? entity.getDigits() : 6;
        long nowSec = Instant.now().getEpochSecond();
        long remain = period - (nowSec % period);
        if (remain == 0) {
            remain = period;
        }
        String code;
        try {
            code = generateCode(entity);
        } catch (Exception e) {
            log.warn("生成 MFA 验证码失败, id={}", entity.get_id(), e);
            code = "------";
        }
        JSONObject view = new JSONObject();
        view.set("id", entity.get_id());
        view.set("_id", entity.get_id());
        view.set("issuer", StrUtil.blankToDefault(entity.getIssuer(), "未命名"));
        view.set("account", StrUtil.blankToDefault(entity.getAccount(), ""));
        view.set("code", code);
        view.set("period", period);
        view.set("remain", remain);
        view.set("digits", digits);
        return view;
    }

    private MfaEntity fromManual(JSONObject body) {
        String secret = normalizeSecret(body.getStr("secret"));
        String account = StrUtil.trim(body.getStr("account"));
        if (StrUtil.isBlank(secret)) {
            throw new IllegalArgumentException("请填写密钥");
        }
        if (!isValidBase32(secret)) {
            throw new IllegalArgumentException("密钥必须是有效的 Base32");
        }
        if (StrUtil.isBlank(account)) {
            throw new IllegalArgumentException("请填写账号");
        }
        int digits = body.getInt("digits", 6);
        int period = body.getInt("period", 30);
        String algorithm = StrUtil.blankToDefault(body.getStr("algorithm"), "SHA1").toUpperCase();
        String type = StrUtil.blankToDefault(body.getStr("type"), "totp").toLowerCase();
        return MfaEntity.builder()
                .secret(secret)
                .issuer(StrUtil.blankToDefault(StrUtil.trim(body.getStr("issuer")), "未命名"))
                .account(account)
                .digits(VALID_DIGITS.contains(digits) ? digits : 6)
                .period(period > 0 ? period : 30)
                .algorithm(algorithm)
                .type("hotp".equals(type) ? "hotp" : "totp")
                .counter(body.getLong("counter", 0L))
                .build();
    }

    public MfaEntity parseOtpauthUri(String uri) {
        String trimmed = StrUtil.trim(uri);
        if (StrUtil.isBlank(trimmed) || !trimmed.toLowerCase().startsWith("otpauth://")) {
            throw new IllegalArgumentException("URI 必须以 otpauth:// 开头");
        }
        String rest = trimmed.substring("otpauth://".length());
        int slash = rest.indexOf('/');
        if (slash < 0) {
            throw new IllegalArgumentException("URI 格式无效");
        }
        String type = rest.substring(0, slash).toLowerCase();
        if (!"totp".equals(type) && !"hotp".equals(type)) {
            throw new IllegalArgumentException("仅支持 totp / hotp 类型");
        }
        String pathAndQuery = rest.substring(slash + 1);
        String labelPart;
        String queryPart = "";
        int q = pathAndQuery.indexOf('?');
        if (q >= 0) {
            labelPart = pathAndQuery.substring(0, q);
            queryPart = pathAndQuery.substring(q + 1);
        } else {
            labelPart = pathAndQuery;
        }
        JSONObject params = parseQuery(queryPart);
        String secret = normalizeSecret(params.getStr("secret"));
        if (StrUtil.isBlank(secret)) {
            throw new IllegalArgumentException("URI 中缺少 secret");
        }
        if (!isValidBase32(secret)) {
            throw new IllegalArgumentException("secret 不是有效的 Base32");
        }
        String issuer = StrUtil.nullToDefault(params.getStr("issuer"), "");
        String account;
        String label = urlDecode(labelPart);
        int colon = label.indexOf(':');
        if (colon >= 0) {
            String labelIssuer = label.substring(0, colon).trim();
            account = label.substring(colon + 1).trim();
            if (StrUtil.isBlank(issuer)) {
                issuer = labelIssuer;
            }
        } else {
            account = label.trim();
        }
        int digits = params.getInt("digits", 6);
        int period = params.getInt("period", 30);
        String algorithm = StrUtil.blankToDefault(params.getStr("algorithm"), "SHA1").toUpperCase();
        long counter = params.getLong("counter", 0L);
        return MfaEntity.builder()
                .secret(secret)
                .issuer(StrUtil.blankToDefault(issuer, "未命名"))
                .account(StrUtil.blankToDefault(account, "未命名账号"))
                .digits(VALID_DIGITS.contains(digits) ? digits : 6)
                .period(period > 0 ? period : 30)
                .algorithm(algorithm)
                .type(type)
                .counter(counter)
                .build();
    }

    private JSONObject parseQuery(String query) {
        JSONObject params = new JSONObject();
        if (StrUtil.isBlank(query)) {
            return params;
        }
        for (String pair : query.split("&")) {
            if (StrUtil.isBlank(pair)) {
                continue;
            }
            int eq = pair.indexOf('=');
            if (eq < 0) {
                params.set(urlDecode(pair), "");
            } else {
                params.set(urlDecode(pair.substring(0, eq)), urlDecode(pair.substring(eq + 1)));
            }
        }
        return params;
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    private String generateCode(MfaEntity entity) {
        byte[] key = decodeSecret(entity.getSecret());
        int digits = entity.getDigits() != null ? entity.getDigits() : 6;
        HmacAlgorithm algorithm = resolveAlgorithm(entity.getAlgorithm());
        int code;
        if ("hotp".equalsIgnoreCase(entity.getType())) {
            long counter = entity.getCounter() != null ? entity.getCounter() : 0L;
            HOTP hotp = new HOTP(digits, algorithm, key);
            code = hotp.generate(counter);
        } else {
            int period = entity.getPeriod() != null && entity.getPeriod() > 0 ? entity.getPeriod() : 30;
            TOTP totp = new TOTP(Duration.ofSeconds(period), digits, algorithm, key);
            code = totp.generate(Instant.now());
        }
        return String.format("%0" + digits + "d", code);
    }

    private byte[] decodeSecret(String secret) {
        try {
            return Base32.decode(normalizeSecret(secret));
        } catch (Exception e) {
            throw new IllegalArgumentException("密钥 Base32 解码失败");
        }
    }

    private HmacAlgorithm resolveAlgorithm(String algorithm) {
        String alg = StrUtil.blankToDefault(algorithm, "SHA1").toUpperCase().replace("HMAC", "");
        if ("SHA256".equals(alg)) {
            return HmacAlgorithm.HmacSHA256;
        }
        if ("SHA512".equals(alg)) {
            return HmacAlgorithm.HmacSHA512;
        }
        return HmacAlgorithm.HmacSHA1;
    }

    private String normalizeSecret(String secret) {
        return StrUtil.nullToDefault(secret, "").replaceAll("\\s+", "").toUpperCase();
    }

    private boolean isValidBase32(String secret) {
        if (StrUtil.isBlank(secret) || !secret.matches("^[A-Z2-7]+=*$")) {
            return false;
        }
        return secret.replaceAll("=+$", "").length() >= 8;
    }
}
