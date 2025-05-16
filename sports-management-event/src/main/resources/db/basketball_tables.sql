-- 篮球比赛相关表结构

-- 篮球比赛表
CREATE TABLE `match_basketball` (
  `match_id` varchar(36) NOT NULL COMMENT '比赛ID',
  `game_id` bigint NOT NULL COMMENT '所属赛事ID',
  `sport` varchar(50) NOT NULL DEFAULT '篮球' COMMENT '体育项目',
  `away_team_id` bigint NOT NULL COMMENT '客队ID',
  `home_team_id` bigint NOT NULL COMMENT '主队ID',
  `away_team` varchar(100) NOT NULL COMMENT '客队名称',
  `home_team` varchar(100) NOT NULL COMMENT '主队名称',
  `away_team_score` int DEFAULT '0' COMMENT '客队得分',
  `home_team_score` int DEFAULT '0' COMMENT '主队得分',
  `venue_id` bigint DEFAULT NULL COMMENT '场地ID',
  `venue_name` varchar(100) DEFAULT NULL COMMENT '场地名称',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `phase` int DEFAULT '1' COMMENT '赛段',
  `winner` varchar(100) DEFAULT NULL COMMENT '获胜方',
  `responsible_person_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `responsible_person` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `note` text COMMENT '备注信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`match_id`),
  KEY `idx_game_id` (`game_id`),
  KEY `idx_teams` (`home_team_id`, `away_team_id`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='篮球比赛表';

-- 篮球比赛赛段得分表
CREATE TABLE `match_quarter_basketball` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `match_id` varchar(36) NOT NULL COMMENT '比赛ID',
  `team_id` bigint NOT NULL COMMENT '队伍ID',
  `team_name` varchar(100) NOT NULL COMMENT '队伍名称',
  `is_home_team` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否主队',
  `q1` int DEFAULT '0' COMMENT '第一节得分',
  `q2` int DEFAULT '0' COMMENT '第二节得分',
  `q3` int DEFAULT '0' COMMENT '第三节得分',
  `q4` int DEFAULT '0' COMMENT '第四节得分',
  `ot` int DEFAULT '0' COMMENT '加时赛得分',
  `total` int DEFAULT '0' COMMENT '总得分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_team` (`match_id`,`team_id`),
  KEY `idx_match_id` (`match_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='篮球比赛赛段得分表';

-- 篮球队伍统计数据表
CREATE TABLE `team_stats_basketball` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `match_id` varchar(36) NOT NULL COMMENT '比赛ID',
  `team_id` bigint NOT NULL COMMENT '队伍ID',
  `team_name` varchar(100) NOT NULL COMMENT '队伍名称',
  `is_home_team` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否主队',
  `pts` int DEFAULT '0' COMMENT '总得分',
  `reb` int DEFAULT '0' COMMENT '篮板',
  `ast` int DEFAULT '0' COMMENT '助攻',
  `fg_made` int DEFAULT '0' COMMENT '投篮命中数',
  `fg_att` int DEFAULT '0' COMMENT '投篮出手数',
  `fg_pct` decimal(5,2) DEFAULT '0.00' COMMENT '投篮命中率',
  `tp_made` int DEFAULT '0' COMMENT '三分命中数',
  `tp_att` int DEFAULT '0' COMMENT '三分出手数',
  `tp_pct` decimal(5,2) DEFAULT '0.00' COMMENT '三分命中率',
  `ft_made` int DEFAULT '0' COMMENT '罚球命中数',
  `ft_att` int DEFAULT '0' COMMENT '罚球出手数',
  `ft_pct` decimal(5,2) DEFAULT '0.00' COMMENT '罚球命中率',
  `stl` int DEFAULT '0' COMMENT '抢断',
  `blk` int DEFAULT '0' COMMENT '盖帽',
  `tov` int DEFAULT '0' COMMENT '失误',
  `pf` int DEFAULT '0' COMMENT '犯规',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_team` (`match_id`,`team_id`),
  KEY `idx_match_id` (`match_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='篮球队伍统计数据表';

-- 篮球球员统计数据表
CREATE TABLE `player_stats_basketball` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `match_id` varchar(36) NOT NULL COMMENT '比赛ID',
  `team_id` bigint NOT NULL COMMENT '队伍ID',
  `player_id` bigint NOT NULL COMMENT '球员ID',
  `player_name` varchar(100) NOT NULL COMMENT '球员姓名',
  `jersey_number` varchar(10) DEFAULT NULL COMMENT '球衣号码',
  `position` varchar(20) DEFAULT NULL COMMENT '位置',
  `minutes` varchar(10) DEFAULT '00:00' COMMENT '上场时间',
  `pts` int DEFAULT '0' COMMENT '得分',
  `reb` int DEFAULT '0' COMMENT '篮板',
  `ast` int DEFAULT '0' COMMENT '助攻',
  `fg_made` int DEFAULT '0' COMMENT '投篮命中数',
  `fg_att` int DEFAULT '0' COMMENT '投篮出手数',
  `fg_pct` decimal(5,2) DEFAULT '0.00' COMMENT '投篮命中率',
  `tp_made` int DEFAULT '0' COMMENT '三分命中数',
  `tp_att` int DEFAULT '0' COMMENT '三分出手数',
  `tp_pct` decimal(5,2) DEFAULT '0.00' COMMENT '三分命中率',
  `ft_made` int DEFAULT '0' COMMENT '罚球命中数',
  `ft_att` int DEFAULT '0' COMMENT '罚球出手数',
  `ft_pct` decimal(5,2) DEFAULT '0.00' COMMENT '罚球命中率',
  `off_reb` int DEFAULT '0' COMMENT '前场篮板',
  `def_reb` int DEFAULT '0' COMMENT '后场篮板',
  `stl` int DEFAULT '0' COMMENT '抢断',
  `blk` int DEFAULT '0' COMMENT '盖帽',
  `tov` int DEFAULT '0' COMMENT '失误',
  `pf` int DEFAULT '0' COMMENT '犯规',
  `plus_minus` int DEFAULT '0' COMMENT '正负值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_player` (`match_id`,`player_id`),
  KEY `idx_match_id` (`match_id`),
  KEY `idx_team_id` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='篮球球员统计数据表';

-- 篮球比赛裁判表
CREATE TABLE `match_referee_basketball` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `match_id` varchar(36) NOT NULL COMMENT '比赛ID',
  `referee_id` bigint NOT NULL COMMENT '裁判ID',
  `referee_name` varchar(50) NOT NULL COMMENT '裁判姓名',
  `referee_type` varchar(20) DEFAULT NULL COMMENT '裁判类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_referee` (`match_id`,`referee_id`),
  KEY `idx_match_id` (`match_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='篮球比赛裁判表';

-- 篮球比赛角色记录表
CREATE TABLE `match_role_record_basketball` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `match_id` varchar(36) NOT NULL COMMENT '比赛ID',
  `team_id` bigint DEFAULT NULL COMMENT '队伍ID',
  `role` int NOT NULL COMMENT '角色 (对应 GameRole 枚举的 code)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_match` (`user_id`,`match_id`),
  KEY `idx_match_id` (`match_id`),
  KEY `idx_team_id` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='篮球比赛角色记录表';

-- 篮球投篮图数据表
CREATE TABLE `shot_chart_basketball` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `match_id` varchar(36) NOT NULL COMMENT '比赛ID',
  `team_id` bigint NOT NULL COMMENT '队伍ID',
  `player_id` bigint NOT NULL COMMENT '球员ID',
  `quarter` int DEFAULT '1' COMMENT '节次',
  `time_remaining` varchar(10) DEFAULT NULL COMMENT '剩余时间',
  `x_coord` decimal(5,2) NOT NULL COMMENT 'X坐标',
  `y_coord` decimal(5,2) NOT NULL COMMENT 'Y坐标',
  `shot_type` varchar(20) DEFAULT NULL COMMENT '投篮类型',
  `shot_distance` decimal(5,2) DEFAULT NULL COMMENT '投篮距离',
  `is_made` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否命中',
  `points` int DEFAULT '2' COMMENT '得分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_match_id` (`match_id`),
  KEY `idx_team_player` (`team_id`,`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='篮球投篮图数据表';

-- 插入样例数据

-- 篮球比赛样例数据
INSERT INTO `match_basketball` (`match_id`, `game_id`, `sport`, `away_team_id`, `home_team_id`, `away_team`, `home_team`, `away_team_score`, `home_team_score`, `venue_id`, `venue_name`, `start_time`, `end_time`, `phase`, `winner`, `responsible_person_id`, `responsible_person`, `phone`, `note`) VALUES
('BB-2023-001', 1, '篮球', 123, 456, '软件1223', '软件1224', 116, 121, 4, '篮球场3号', '2023-12-15 14:00:00', '2023-12-15 15:30:00', 1, '软件1224', 4, '张管理员', '13800138000', '请各队提前30分钟到场热身，迟到15分钟视为弃权');

-- 篮球比赛赛段得分样例数据
INSERT INTO `match_quarter_basketball` (`match_id`, `team_id`, `team_name`, `is_home_team`, `q1`, `q2`, `q3`, `q4`, `total`) VALUES
('BB-2023-001', 123, '软件1223', 0, 25, 30, 36, 25, 116),
('BB-2023-001', 456, '软件1224', 1, 31, 36, 27, 27, 121);

-- 篮球队伍统计数据样例数据
INSERT INTO `team_stats_basketball` (`match_id`, `team_id`, `team_name`, `is_home_team`, `pts`, `reb`, `ast`, `fg_made`, `fg_att`, `fg_pct`, `tp_made`, `tp_att`, `tp_pct`) VALUES
('BB-2023-001', 123, '软件1223', 0, 116, 50, 39, 42, 86, 48.80, 12, 26, 46.20),
('BB-2023-001', 456, '软件1224', 1, 121, 39, 29, 45, 98, 45.90, 15, 43, 34.90);

-- 篮球球员统计数据样例数据
INSERT INTO `player_stats_basketball` (`match_id`, `team_id`, `player_id`, `player_name`, `jersey_number`, `position`, `minutes`, `pts`, `reb`, `ast`, `fg_made`, `fg_att`) VALUES
('BB-2023-001', 123, 1001, '莫兰特', '12', 'PG', '34:54', 22, 3, 3, 9, 18),
('BB-2023-001', 123, 1002, '杰克逊', '13', 'SG', '28:15', 18, 5, 7, 7, 12),
('BB-2023-001', 456, 2001, '库里', '30', 'PG', '32:45', 25, 4, 8, 8, 16),
('BB-2023-001', 456, 2002, '汤普森', '11', 'SG', '30:20', 20, 3, 2, 7, 15);

-- 篮球比赛裁判样例数据
INSERT INTO `match_referee_basketball` (`match_id`, `referee_id`, `referee_name`, `referee_type`) VALUES
('BB-2023-001', 1, '张教练', '主裁判'),
('BB-2023-001', 2, '王教练', '副裁判');

-- 篮球比赛角色记录样例数据
INSERT INTO `match_role_record_basketball` (`user_id`, `match_id`, `team_id`, `role`) VALUES
(101, 'BB-2023-001', 123, 1),  -- 球员
(102, 'BB-2023-001', 123, 2),  -- 教练
(103, 'BB-2023-001', NULL, 3), -- 裁判
(104, 'BB-2023-001', 456, 1),  -- 球员
(105, 'BB-2023-001', 456, 2);  -- 教练

-- 篮球投篮图数据样例数据
INSERT INTO `shot_chart_basketball` (`match_id`, `team_id`, `player_id`, `quarter`, `time_remaining`, `x_coord`, `y_coord`, `shot_type`, `shot_distance`, `is_made`, `points`) VALUES
('BB-2023-001', 123, 1001, 1, '08:45', 25.50, 15.75, '跳投', 18.50, 1, 2),
('BB-2023-001', 123, 1001, 2, '05:30', 35.25, 20.50, '三分', 23.75, 1, 3),
('BB-2023-001', 456, 2001, 1, '09:20', 28.75, 18.25, '三分', 24.50, 1, 3),
('BB-2023-001', 456, 2001, 3, '06:15', 15.50, 10.25, '上篮', 5.25, 1, 2);