
drop table sys_ai_prompt_template;

-- 提示词模板表：支持在线编辑提示词，版本管理
CREATE TABLE IF NOT EXISTS `sys_ai_prompt_template` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `template_code` VARCHAR(64) NOT NULL COMMENT '模板编码',
  `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
  `version` VARCHAR(32) NOT NULL DEFAULT '1.0' COMMENT '版本号',
  `content` MEDIUMTEXT NOT NULL COMMENT '模板正文',
  `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '是否当前生效（1-是 0-否）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注/修改说明',
  `create_by_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_version` (`template_code`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI提示词模板';
