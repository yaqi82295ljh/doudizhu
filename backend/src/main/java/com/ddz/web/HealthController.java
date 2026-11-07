package com.ddz.web;

import com.ddz.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查。M0 阶段的验收入口：前端能调通 {@code /api/ping} 即视为链路打通。
 */
@Tag(name = "00. 健康检查", description = "M0 验收用：确认服务自身与依赖可用")
@RestController
@RequestMapping("/api")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    public HealthController(JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Operation(summary = "服务存活探测", description = "无需鉴权，前端联调的第一个接口")
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "doudizhu");
        body.put("status", "up");
        body.put("time", OffsetDateTime.now().toString());
        return Result.ok(body);
    }

    @Operation(summary = "依赖连通性自检", description = "分别探测 MySQL 与 Redis，返回 up/down，探测失败不抛异常")
    @GetMapping("/ping/deps")
    public Result<Map<String, Object>> deps() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mysql", probeMysql());
        body.put("redis", probeRedis());
        return Result.ok(body);
    }

    private String probeMysql() {
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return (one != null && one == 1) ? "up" : "unknown";
        } catch (Exception e) {
            log.warn("MySQL 探测失败: {}", e.getMessage());
            return "down";
        }
    }

    private String probeRedis() {
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            return "PONG".equalsIgnoreCase(connection.ping()) ? "up" : "unknown";
        } catch (Exception e) {
            log.warn("Redis 探测失败: {}", e.getMessage());
            return "down";
        }
    }
}
