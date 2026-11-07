package com.ddz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 配置。
 * <p>
 * 刻意<b>不</b>使用 Jackson 通用序列化器，理由有三：
 * <ol>
 *   <li>房间态、消息体都是自建结构，用 JSON 字符串存更可控，反序列化时不会出现多态类型信息写入
 *       {@code @class} 字段被下游解析器拒绝的问题；</li>
 *   <li>序列化格式一旦上线就不能随便改，全字符串方案迁移成本最低；</li>
 *   <li>Spring Boot 4 已切到 Jackson 3，若用 {@code GenericJackson2JsonRedisSerializer}
 *       会被拖回 Jackson 2，版本冲突风险高。</li>
 * </ol>
 * Key 与 Value 一律 UTF-8 字符串，用 {@code redis-cli} 可读，排错成本最低。
 */
@Configuration
public class RedisConfig {

    /**
     * 业务统一使用的 RedisTemplate。Bean 名显式取为 {@code ddzRedisTemplate}，
     * 避免覆盖 Spring Boot 自动配置的 {@code redisTemplate}（{@code <Object,Object>}）。
     */
    @Bean("ddzRedisTemplate")
    public RedisTemplate<String, String> ddzRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        RedisSerializer<String> stringSerializer = RedisSerializer.string();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
