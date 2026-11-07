-- ============================================================
-- 斗地主 Web · 数据库初始化脚本
-- 字符集：utf8mb4 / utf8mb4_unicode_ci
-- 幂等：全部使用 CREATE ... IF NOT EXISTS，可重复执行
-- 用途：
--   1) 本地手动执行：mysql -uroot -p < schema.sql
--   2) Docker 首次启动自动执行（挂载到 docker-entrypoint-initdb.d）
-- ============================================================

CREATE DATABASE IF NOT EXISTS `doudizhu`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `doudizhu`;

-- ------------------------------------------------------------
-- 用户
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(32)  NOT NULL COMMENT '登录名',
    `password_hash` VARCHAR(100) NOT NULL COMMENT 'BCrypt 哈希，长度 60，留余量',
    `nickname`      VARCHAR(32)  NOT NULL COMMENT '展示昵称',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '软删除：0=正常 1=已删除',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户';

-- ------------------------------------------------------------
-- 对局记录（一行 = 一局）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `game_record` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `room_id`      VARCHAR(32) NOT NULL COMMENT '房间号',
    `landlord_id`  BIGINT      NOT NULL COMMENT '地主 user_id',
    `winner_side`  TINYINT     NOT NULL COMMENT '1=地主胜 2=农民胜',
    `base_score`   INT         NOT NULL COMMENT '底分',
    `multiplier`   INT         NOT NULL COMMENT '总倍数 = 叫分 × 炸弹 × 春天',
    `bomb_count`   INT         NOT NULL DEFAULT 0 COMMENT '本局炸弹数（含王炸）',
    `players_json` JSON        NOT NULL COMMENT '座位、用户、身份、得分',
    `replay_json`  MEDIUMTEXT  NOT NULL COMMENT '逐手牌谱，用于回放',
    `started_at`   DATETIME    NOT NULL COMMENT '开局时间',
    `ended_at`     DATETIME    NOT NULL COMMENT '结算时间',
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    PRIMARY KEY (`id`),
    KEY `idx_room` (`room_id`),
    KEY `idx_started` (`started_at`),
    KEY `idx_landlord` (`landlord_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='对局记录';

-- ------------------------------------------------------------
-- 战绩统计（对局结束时增量更新）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `player_stat` (
    `user_id`        BIGINT   NOT NULL COMMENT '用户 id',
    `total_games`    INT      NOT NULL DEFAULT 0 COMMENT '总局数',
    `win_games`      INT      NOT NULL DEFAULT 0 COMMENT '胜局数',
    `landlord_games` INT      NOT NULL DEFAULT 0 COMMENT '当地主局数',
    `landlord_wins`  INT      NOT NULL DEFAULT 0 COMMENT '当地主胜局数',
    `max_streak`     INT      NOT NULL DEFAULT 0 COMMENT '最高连胜',
    `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='战绩统计';

-- ------------------------------------------------------------
-- AI 人格配置（后台可调，不用改代码）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ai_persona` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(32)   NOT NULL COMMENT '人格名',
    `avatar`      VARCHAR(255)  DEFAULT NULL COMMENT '头像 URL',
    `style`       VARCHAR(32)   NOT NULL COMMENT 'steady / aggressive / talkative',
    `prompt_hint` TEXT          NOT NULL COMMENT '注入 system prompt 的性格描述',
    `temperature` DECIMAL(2, 1) NOT NULL DEFAULT 0.8 COMMENT 'LLM 温度，0.0~2.0',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '软删除：0=正常 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_style` (`style`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='AI 人格配置';

-- ------------------------------------------------------------
-- 种子数据：3 种 AI 人格（幂等，重复执行不报错）
-- ------------------------------------------------------------
INSERT INTO `ai_persona` (`name`, `style`, `prompt_hint`, `temperature`)
VALUES ('稳健老张', 'steady', '你打牌极度保守，优先保炸弹、拆牌最少、能不出就不出，只有在能一手走完时才果断出牌。说话简短克制。', 0.6),
       ('莽夫小王', 'aggressive', '你打法激进，喜欢抢先手、压牌、早放炸弹，宁可拆顺子也要压住对手。说话冲、爱挑衅。', 0.9),
       ('话痨阿美', 'talkative', '你牌技中等但话很多，每次出牌都带一句俏皮台词，喜欢调侃对手的手牌张数。语气轻松活泼。', 1.0)
ON DUPLICATE KEY UPDATE `id` = `id`;
