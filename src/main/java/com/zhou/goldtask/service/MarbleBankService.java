package com.zhou.goldtask.service;

import com.zhou.goldtask.controller.WebSocketServer;
import com.zhou.goldtask.entity.MarbleBankEntity;
import com.zhou.goldtask.entity.MarbleDeviceDto;
import com.zhou.goldtask.repository.MarbleBankRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 弹珠：库存 + 全局获胜概率，同在 marble_bank 集合。
 * 设备行以 sid 为 id；配置行 id 固定 __config__，只用 winChance 字段。
 * 在线设备变更后立刻经 WebSocket 下发；离线设备下次进游戏再拉。
 */
@Service
public class MarbleBankService {
    private static final int MAX_COUNT = 1_000_000_000;
    private static final int MAX_ADD = 1_000_000;
    private static final int DEFAULT_WIN_CHANCE = 30;
    private static final int MAX_WIN_CHANCE = 100;
    /** 全局配置行的固定 id，不可能撞设备 UUID。 */
    private static final String CONFIG_ID = "__config__";
    /** 分段锁数量：固定 64 把，避免每来一台设备就常驻一把锁。 */
    private static final int LOCK_STRIPES = 64;

    @Resource
    private MarbleBankRepository marbleBankRepository;

    private final Object[] locks = new Object[LOCK_STRIPES];

    public MarbleBankService() {
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Object();
        }
    }

    // MARK: - 获胜概率（全局配置行）

    public int getWinChance() {
        MarbleBankEntity entity = marbleBankRepository.findById(CONFIG_ID).orElse(null);
        return entity != null && entity.getWinChance() != null ? entity.getWinChance() : DEFAULT_WIN_CHANCE;
    }

    /** 保存新概率并广播给全部在线设备。返回落库的值，方便管理页直接采用。 */
    public int saveWinChance(Integer winChance) {
        if (winChance == null || winChance < 0 || winChance > MAX_WIN_CHANCE) {
            throw new IllegalArgumentException("获胜概率要在 0 到 100 之间");
        }
        synchronized (lock(CONFIG_ID)) {
            MarbleBankEntity entity = marbleBankRepository.findById(CONFIG_ID).orElse(null);
            if (entity == null) {
                entity = new MarbleBankEntity();
                entity.setId(CONFIG_ID);
                entity.setName("全局配置");
            }
            entity.setWinChance(winChance);
            entity.setUpdatedAt(System.currentTimeMillis());
            marbleBankRepository.save(entity);
        }
        WebSocketServer.pushMarbleConfig(winChance);
        return winChance;
    }

    // MARK: - 设备库存

    /** WebSocket 上线时登记设备，让管理页能看到还没上报库存的机器。 */
    public void touch(String sid, String name) {
        String id = cleanSid(sid);
        if (id.isEmpty() || CONFIG_ID.equals(id)) {
            return;
        }
        synchronized (lock(id)) {
            MarbleBankEntity entity = marbleBankRepository.findById(id).orElse(null);
            String cleaned = cleanName(name);
            long now = System.currentTimeMillis();
            if (entity == null) {
                entity = new MarbleBankEntity();
                entity.setId(id);
                entity.setName(cleaned);
                entity.setUpdatedAt(now);
                marbleBankRepository.save(entity);
                return;
            }
            if (!cleaned.equals(entity.getName())) {
                entity.setName(cleaned);
                entity.setUpdatedAt(now);
                marbleBankRepository.save(entity);
            }
        }
    }

    public List<MarbleDeviceDto> list() {
        return marbleBankRepository.findAll().stream()
                .filter(entity -> !CONFIG_ID.equals(entity.getId()))
                .map(this::toDto)
                .sorted(Comparator.comparing(MarbleDeviceDto::isOnline).reversed()
                        .thenComparing(MarbleDeviceDto::getName, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    public MarbleDeviceDto get(String sid) {
        String id = requireSid(sid);
        synchronized (lock(id)) {
            return toDto(id, marbleBankRepository.findById(id).orElse(null));
        }
    }

    /** 管理员给某台设备加减珠。库存未上报时先记 pending，并下发本次调整的数量。 */
    public MarbleDeviceDto add(String sid, Integer delta) {
        String id = requireSid(sid);
        if (delta == null || delta == 0 || delta > MAX_ADD || delta < -MAX_ADD) {
            throw new IllegalArgumentException("加减珠数量不合法");
        }
        MarbleDeviceDto dto;
        boolean pending;
        int pushed;
        synchronized (lock(id)) {
            MarbleBankEntity entity = loadOrCreate(id);
            if (entity.getCount() == null) {
                entity.setPending(clamp(entity.getPending() + (long) delta));
                pending = true;
                pushed = delta;
            } else {
                entity.setCount(clamp(entity.getCount() + (long) delta));
                pending = false;
                pushed = entity.getCount();
            }
            entity.setUpdatedAt(System.currentTimeMillis());
            marbleBankRepository.save(entity);
            dto = toDto(entity);
        }
        if (pending) {
            WebSocketServer.pushMarbleDelta(id, pushed);
        } else {
            WebSocketServer.pushMarbleBank(id, pushed);
        }
        return dto;
    }

    public MarbleDeviceDto sync(SyncCommand command) {
        if (command == null || command.getOp() == null) {
            throw new IllegalArgumentException("参数不合法");
        }
        if ("init".equals(command.getOp())) {
            return init(command);
        }
        if ("delta".equals(command.getOp())) {
            return delta(command);
        }
        throw new IllegalArgumentException("参数不合法");
    }

    private MarbleDeviceDto init(SyncCommand command) {
        String id = requireSid(command.getSid());
        int reported = command.getCount() == null ? 0 : clamp(command.getCount());
        int applied = command.getAppliedPending() == null ? 0 : clamp(command.getAppliedPending());
        synchronized (lock(id)) {
            MarbleBankEntity entity = loadOrCreate(id);
            if (entity.getCount() == null) {
                int extra = Math.max(0, entity.getPending() - applied);
                entity.setCount(clamp(reported + (long) extra));
                entity.setPending(0);
            }
            entity.setName(cleanName(command.getName()));
            entity.setUpdatedAt(System.currentTimeMillis());
            marbleBankRepository.save(entity);
            return toDto(entity);
        }
    }

    private MarbleDeviceDto delta(SyncCommand command) {
        String id = requireSid(command.getSid());
        String syncId = command.getSyncId() == null ? "" : command.getSyncId().trim();
        if (syncId.isEmpty() || syncId.length() > 64 || command.getDelta() == null) {
            throw new IllegalArgumentException("参数不合法");
        }
        long delta = command.getDelta();
        if (delta > MAX_COUNT || delta < -MAX_COUNT) {
            throw new IllegalArgumentException("参数不合法");
        }
        synchronized (lock(id)) {
            MarbleBankEntity entity = marbleBankRepository.findById(id).orElse(null);
            if (entity == null || entity.getCount() == null) {
                throw new IllegalArgumentException("弹珠数还没上报");
            }
            if (!syncId.equals(entity.getLastSyncId()) && delta != 0) {
                entity.setCount(clamp(entity.getCount() + delta));
                entity.setLastSyncId(syncId);
            }
            entity.setName(cleanName(command.getName()));
            entity.setUpdatedAt(System.currentTimeMillis());
            marbleBankRepository.save(entity);
            return toDto(entity);
        }
    }

    private MarbleBankEntity loadOrCreate(String id) {
        MarbleBankEntity entity = marbleBankRepository.findById(id).orElse(null);
        if (entity != null) {
            return entity;
        }
        entity = new MarbleBankEntity();
        entity.setId(id);
        entity.setName("未知设备");
        entity.setUpdatedAt(System.currentTimeMillis());
        return entity;
    }

    private MarbleDeviceDto toDto(MarbleBankEntity entity) {
        return toDto(entity.getId(), entity);
    }

    private MarbleDeviceDto toDto(String sid, MarbleBankEntity entity) {
        MarbleDeviceDto dto = new MarbleDeviceDto();
        dto.setSid(sid);
        if (entity == null) {
            dto.setName("未知设备");
            dto.setCount(0);
            dto.setPending(0);
            dto.setKnown(false);
            dto.setOnline(WebSocketServer.isOnline(sid));
            return dto;
        }
        boolean known = entity.getCount() != null;
        dto.setName(entity.getName() == null ? "未知设备" : entity.getName());
        dto.setCount(known ? entity.getCount() : 0);
        dto.setPending(entity.getPending());
        dto.setKnown(known);
        dto.setOnline(WebSocketServer.isOnline(sid));
        return dto;
    }

    private Object lock(String sid) {
        return locks[(sid.hashCode() & 0x7fffffff) % locks.length];
    }

    private static String requireSid(String sid) {
        String id = cleanSid(sid);
        if (id.isEmpty() || id.length() > 80 || CONFIG_ID.equals(id)) {
            throw new IllegalArgumentException("设备不合法");
        }
        return id;
    }

    private static String cleanSid(String sid) {
        return sid == null ? "" : sid.trim();
    }

    private static String cleanName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "未知设备";
        }
        String trimmed = name.trim().replaceAll("\\s+", " ");
        return trimmed.length() > 64 ? trimmed.substring(0, 64) : trimmed;
    }

    private static int clamp(long value) {
        if (value < 0) {
            return 0;
        }
        return value > MAX_COUNT ? MAX_COUNT : (int) value;
    }

    /** 设备上报：init 用本机数量建账，delta 用差值并带 syncId 防重。 */
    @Data
    public static class SyncCommand {
        private String sid;
        private String name;
        private String op;
        private Integer count;
        private Integer delta;
        private Integer appliedPending;
        private String syncId;
    }
}
