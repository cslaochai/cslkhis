
-- 知识库文档历史版本表：每次保存（新建/编辑/回退）留一条快照，供版本留痕与回退
CREATE TABLE IF NOT EXISTS `sys_knowledge_doc_history` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `doc_id` BIGINT NOT NULL COMMENT '文档ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `source_type` TINYINT DEFAULT NULL COMMENT '来源类型（1-内置示例 2-手工录入 3-文件导入）',
  `version` VARCHAR(50) DEFAULT NULL COMMENT '版本号',
  `source_name` VARCHAR(100) DEFAULT NULL COMMENT '来源名称（诊疗指南/论坛/院内制度等）',
  `content` LONGTEXT NOT NULL COMMENT '内容快照',
  `change_type` TINYINT NOT NULL DEFAULT 2 COMMENT '变更类型（1-新建 2-编辑 3-回退）',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_doc_id` (`doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档历史版本';
