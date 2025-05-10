-- 关闭外键检查（确保迁移兼容性）
SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 创建表结构（无外键版本）
-- --------------------------------------------------------
-- 球队表
-- --------------------------------------------------------
DROP TABLE IF EXISTS `event_team`;
CREATE TABLE `event_team` (
                              `team_id` bigint NOT NULL AUTO_INCREMENT,
                              `team_name` varchar(50) NOT NULL,
                              `conference` varchar(20) DEFAULT NULL,
                              `rank_position` int DEFAULT NULL,
                              `salary_cap` decimal(12,2) DEFAULT NULL,
                              PRIMARY KEY (`team_id`),
                              UNIQUE KEY `team_name_UNIQUE` (`team_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 比赛表
-- --------------------------------------------------------
DROP TABLE IF EXISTS `event_match`;
CREATE TABLE `event_match` (
                               `match_id` varchar(32) NOT NULL,
                               `home_team_id` bigint NOT NULL,
                               `away_team_id` bigint NOT NULL,
                               `match_time` datetime DEFAULT NULL,
                               PRIMARY KEY (`match_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- 比赛分节得分表
-- --------------------------------------------------------
DROP TABLE IF EXISTS `event_match_quarters`;
CREATE TABLE `event_match_quarters` (
                                        `match_id` varchar(32) NOT NULL,
                                        `team_id` bigint NOT NULL,
                                        `q1` int DEFAULT '0',
                                        `q2` int DEFAULT '0',
                                        `q3` int DEFAULT '0',
                                        `q4` int DEFAULT '0',
                                        PRIMARY KEY (`match_id`,`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 其他表结构类似创建...

-- 2. 插入示例数据
-- --------------------------------------------------------
-- 球队数据
-- --------------------------------------------------------
INSERT INTO `event_team`
(`team_name`, `conference`, `rank_position`, `salary_cap`)
VALUES
    ('Memphis Grizzlies', 'Western', 8, 122000000.00),
    ('Golden State Warriors', 'Western', 7, 192000000.00);

-- --------------------------------------------------------
-- 比赛数据（显式指定team_id保持关联）
-- --------------------------------------------------------
INSERT INTO `event_match`
(`match_id`, `home_team_id`, `away_team_id`, `match_time`)
VALUES
    ('M20231128001', 1001, 1002, '2023-11-28 19:30:00');

-- --------------------------------------------------------
-- 分节得分数据（使用已知team_id）
-- --------------------------------------------------------
INSERT INTO `event_match_quarters`
(`match_id`, `team_id`, `q1`, `q2`, `q3`, `q4`)
VALUES
    ('M20231128001', 1001, 25, 30, 36, 25),
    ('M20231128001', 1002, 31, 36, 27, 27);

-- --------------------------------------------------------
-- 球队统计（保持与插入顺序一致）
-- --------------------------------------------------------
INSERT INTO `event_team_statistics`
(`match_id`, `team_id`, `total_points`, `rebounds`, `assists`, `fg_percent`, `three_pt_percent`)
VALUES
    ('M20231128001', 1001, 116, 50, 22, 48.8, 46.2),
    ('M20231128001', 1002, 121, 39, 29, 45.9, 34.9);

-- 3. 重建索引（可选）
ALTER TABLE `event_team` ADD INDEX `idx_conference` (`conference`);
ALTER TABLE `event_match` ADD INDEX `idx_match_time` (`match_time`);

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- 4. 数据验证查询
SELECT
    t.team_name,
    mq.*,
    (mq.q1 + mq.q2 + mq.q3 + mq.q4) AS total_points
FROM event_match_quarters mq
         JOIN event_team t USING (team_id)
WHERE match_id = 'M20231128001';


-- 修改表名（需按顺序执行避免外键依赖）
ALTER TABLE event_team RENAME TO event_basketball_team;
ALTER TABLE event_match RENAME TO event_basketball_match;
ALTER TABLE event_match_quarters RENAME TO event_basketball_match_quarters;
ALTER TABLE event_team_statistics RENAME TO event_basketball_team_statistics;
ALTER TABLE event_shot_chart RENAME TO event_basketball_shot_chart;

-- 更新外键约束名称（若有需要）
ALTER TABLE event_basketball_match
DROP FOREIGN KEY event_match_ibfk_1,  -- 原外键名
ADD CONSTRAINT fk_basketball_match_home_team
FOREIGN KEY (home_team_id) REFERENCES event_basketball_team(team_id);