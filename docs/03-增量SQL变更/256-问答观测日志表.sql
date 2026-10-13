
-- 知识库问答观测日志表：每次问答留一条记录（问题/回答/命中文档/降级/耗时），支持用户反馈（顶/踩）
CREATE TABLE IF NOT EXISTS `sys_knowledge_query_log` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `question` VARCHAR(500) NOT NULL COMMENT '问题',
  `answer` MEDIUMTEXT DEFAULT NULL COMMENT '回答（降级时为检索原文拼接）',
  `hit_docs` VARCHAR(500) DEFAULT NULL COMMENT '命中文档列表（标题分号分隔，最多 5 个，超出省略）',
  `hit_count` INT NOT NULL DEFAULT 0 COMMENT '命中块数',
  `degraded` TINYINT NOT NULL DEFAULT 0 COMMENT '是否模型降级（0-正常 1-降级）',
  `degrade_reason` VARCHAR(200) DEFAULT NULL COMMENT '降级原因',
  `latency_ms` INT NOT NULL DEFAULT 0 COMMENT '耗时（毫秒）',
  `feedback` TINYINT NOT NULL DEFAULT 0 COMMENT '用户反馈（0-未反馈 1-有用 2-无用）',
  `operator_id` BIGINT DEFAULT NULL COMMENT '提问人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '提问人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_degraded` (`degraded`),
  KEY `idx_feedback` (`feedback`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库问答观测日志';
