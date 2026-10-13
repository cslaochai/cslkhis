
-- 知识库离线评测：评测用例集 + 每次运行汇总 + 每用例明细（用于算 recall@k / MRR，改切块参数/embedding 后跑回归对比）
CREATE TABLE IF NOT EXISTS `sys_knowledge_eval_case` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `question` VARCHAR(500) NOT NULL COMMENT '评测问题',
  `expected_doc_ids` VARCHAR(1000) NOT NULL COMMENT '期望命中文档ID（分号分隔）',
  `expected_doc_titles` VARCHAR(1000) DEFAULT NULL COMMENT '期望命中文档标题（分号分隔，展示用）',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '说明',
  `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除（0-正常 1-删除）',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库评测用例';

CREATE TABLE IF NOT EXISTS `sys_knowledge_eval_run` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `case_count` INT NOT NULL DEFAULT 0 COMMENT '评测用例数',
  `hit_case_count` INT NOT NULL DEFAULT 0 COMMENT '命中期望文档的用例数',
  `recall_at_k` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '召回率（%）',
  `mrr` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT '平均倒数排名（MRR，0-1）',
  `avg_latency_ms` INT NOT NULL DEFAULT 0 COMMENT '平均检索耗时（毫秒）',
  `config_snapshot` VARCHAR(200) DEFAULT NULL COMMENT '评测时配置快照（切块参数/embedding）',
  `status` TINYINT NOT NULL DEFAULT 2 COMMENT '状态（2-完成 3-失败）',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '运行时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库评测运行汇总';

CREATE TABLE IF NOT EXISTS `sys_knowledge_eval_run_detail` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `run_id` BIGINT NOT NULL COMMENT '运行ID',
  `case_id` BIGINT NOT NULL COMMENT '用例ID',
  `question` VARCHAR(500) NOT NULL COMMENT '评测问题',
  `expected_doc_ids` VARCHAR(1000) NOT NULL COMMENT '期望命中文档ID（分号分隔）',
  `expected_doc_titles` VARCHAR(1000) DEFAULT NULL COMMENT '期望命中文档标题（分号分隔）',
  `hit_doc_titles` VARCHAR(1000) DEFAULT NULL COMMENT '实际命中文档标题（前5个，分号分隔）',
  `hit_count` INT NOT NULL DEFAULT 0 COMMENT '实际命中块数',
  `hit_rank` INT NOT NULL DEFAULT 0 COMMENT '期望文档最高命中位次（1起，0=未命中）',
  `hit_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否命中期望文档（1-命中 0-未命中）',
  PRIMARY KEY (`id`),
  KEY `idx_run_id` (`run_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库评测运行明细';
