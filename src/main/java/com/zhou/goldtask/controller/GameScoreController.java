package com.zhou.goldtask.controller;

import cn.dev33.satoken.util.SaResult;
import com.zhou.goldtask.entity.GameScoreEntity;
import com.zhou.goldtask.repository.GameScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 小游戏成绩排行。无登录态,靠设备 sid 标识;上报只保留每设备每游戏的最高分。
 */
@RestController
@RequestMapping("/page/game/score")
@CrossOrigin
@Slf4j
public class GameScoreController {
    private static final List<String> GAMES = Arrays.asList("twenty48", "block", "matrix");
    private static final int MAX_SCORE = 100_000_000;
    private static final int TOP_N = 2;

    @Resource
    private GameScoreRepository gameScoreRepository;

    /** 上报成绩:破本机纪录才有意义;无论是否刷新纪录,都返回该游戏最新前两名。 */
    @PostMapping("/report")
    public SaResult report(@RequestBody GameScoreEntity body) {
        String game = body.getGame();
        if (game == null || !GAMES.contains(game)
                || body.getSid() == null || body.getSid().isEmpty()
                || body.getScore() < 0 || body.getScore() > MAX_SCORE) {
            return SaResult.error("参数不合法");
        }
        GameScoreEntity existing = gameScoreRepository.findByGameAndSid(game, body.getSid());
        if (existing == null) {
            body.set_id(null);
            body.setUpdatedAt(System.currentTimeMillis());
            if (body.getName() == null || body.getName().isEmpty()) {
                body.setName("未知设备");
            }
            gameScoreRepository.save(body);
            log.info("游戏成绩首报: game={}, name={}, score={}", game, body.getName(), body.getScore());
        } else if (body.getScore() > existing.getScore()) {
            existing.setScore(body.getScore());
            existing.setName(body.getName());
            existing.setUpdatedAt(System.currentTimeMillis());
            gameScoreRepository.save(existing);
            log.info("游戏成绩刷新: game={}, name={}, score={}", game, body.getName(), body.getScore());
        }
        return SaResult.data(loadTop(game));
    }

    /** 该游戏全网前两名(每设备只计最高分)。 */
    @GetMapping("/top")
    public SaResult top(@RequestParam String game) {
        if (!GAMES.contains(game)) {
            return SaResult.error("未知游戏");
        }
        return SaResult.data(loadTop(game));
    }

    private List<GameScoreEntity> loadTop(String game) {
        return gameScoreRepository.findTop(game, PageRequest.of(0, TOP_N));
    }
}
