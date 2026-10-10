-- 233 DRG/DIP 3.0 分组方案接入：把「DRG 行上挂前缀匹配键」的一跳模型，改为官方「规则原文 + 集合编号 → ICD 精确码」的两跳模型
-- 数据来源：国家医保局《按病组（DRG）付费3.0版分组方案配置信息》（2026-09-09 公开发布的 xlsx，MDC/ADRG/DRG/集合/CC/排除表 六表）
-- 官方包实测不含权重与支付标准（六表列名仅 编码/名称/规则/所属/排序），weight、pay_standard 由统筹区医保局下发，本迁移一律留 NULL。
-- 本目录三张分组表与 sys_drg_set 为纯配置表，无留档价值，重灌一律物理删（DELETE），不走软删。

-- ----------------------------
-- sys_drg_mdc  DRG 主要诊断大类（MDC）目录与入组规则
-- ----------------------------
CREATE TABLE `sys_drg_mdc` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `mdc_code` varchar(8) NOT NULL COMMENT 'MDC 编码',
  `mdc_name` varchar(100) DEFAULT NULL COMMENT 'MDC 名称',
  `mdc_rule` varchar(2000) DEFAULT NULL COMMENT 'MDC 入组规则原文',
  `sort_no` int DEFAULT NULL COMMENT '排序',
  `version` varchar(32) DEFAULT NULL COMMENT '分组方案版本',
  `source` varchar(64) DEFAULT NULL COMMENT '来源',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mdc_code` (`mdc_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 主要诊断大类目录';

-- ----------------------------
-- sys_drg_adrg  DRG 核心疾病诊断相关组（ADRG）目录与入组规则
-- ----------------------------
CREATE TABLE `sys_drg_adrg` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `adrg_code` varchar(16) NOT NULL COMMENT 'ADRG 编码',
  `adrg_name` varchar(200) DEFAULT NULL COMMENT 'ADRG 名称',
  `adrg_rule` varchar(2000) DEFAULT NULL COMMENT 'ADRG 入组规则原文',
  `mdc_code` varchar(8) DEFAULT NULL COMMENT '所属 MDC 编码',
  `sort_no` int DEFAULT NULL COMMENT 'MDC 内排序',
  `version` varchar(32) DEFAULT NULL COMMENT '分组方案版本',
  `source` varchar(64) DEFAULT NULL COMMENT '来源',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_adrg_code` (`adrg_code`,`del_flag`),
  KEY `idx_adrg_mdc` (`mdc_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 核心组目录';

-- ----------------------------
-- sys_drg_set  分组规则引用的 ICD 码集合（规则里的匹配键事实，集合编号 → 精确码）
-- ----------------------------
CREATE TABLE `sys_drg_set` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `set_code` varchar(24) NOT NULL COMMENT '集合编号',
  `icd_code` varchar(32) NOT NULL COMMENT 'ICD 编码',
  `icd_name` varchar(200) DEFAULT NULL COMMENT 'ICD 名称',
  `set_type` varchar(4) NOT NULL COMMENT '类型（OP-手术操作 DI-诊断 EX-CC/MCC 排除组）',
  `version` varchar(32) DEFAULT NULL COMMENT '分组方案版本',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_set_icd` (`set_code`,`icd_code`,`del_flag`),
  KEY `idx_set_icd_code` (`icd_code`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 分组码集合';

-- ----------------------------
-- sys_drg_group 改列：drg_rule 为入组唯一事实，删掉能从规则推导或官方未提供的维度列
-- ----------------------------
ALTER TABLE `sys_drg_group`
  ADD COLUMN `drg_rule` varchar(2000) DEFAULT NULL COMMENT 'DRG 细分组规则原文' AFTER `adrg_code`,
  ADD COLUMN `sort_no` int DEFAULT NULL COMMENT 'ADRG 内排序' AFTER `drg_rule`;

ALTER TABLE `sys_drg_group`
  DROP COLUMN `group_type`,
  DROP COLUMN `cc_mcc_flag`,
  DROP COLUMN `gender_limit`,
  DROP COLUMN `age_tier`,
  DROP COLUMN `pre_group_flag`,
  DROP COLUMN `surgery_attr`,
  DROP COLUMN `base_disease_flag`,
  DROP COLUMN `diag_match`,
  DROP COLUMN `oper_match`;

-- ----------------------------
-- sys_drg_ccmcc 改列：补排除组编号（CC 表的「排除表」列，指向 sys_drg_set 的 EX_ 集合）
-- ----------------------------
ALTER TABLE `sys_drg_ccmcc`
  ADD COLUMN `excl_group` varchar(24) DEFAULT NULL COMMENT 'CC/MCC 排除组编号' AFTER `cc_level`;

-- 排除表改由 sys_drg_set（set_type=EX）+ sys_drg_ccmcc.excl_group 表达，原按主诊断逐对展开的表不再需要
DROP TABLE IF EXISTS `sys_drg_exclusion`;

-- ----------------------------
-- ICD 字典补贯标口径标记：官方集合用的是医保版贯标码（带 x00x 扩展），现字典另有一套历史缩写码
-- ----------------------------
ALTER TABLE `sys_icd10`
  ADD COLUMN `code_std` tinyint NOT NULL DEFAULT '0' COMMENT '编码口径（1-医保版贯标码 0-历史缩写码）' AFTER `icd_category`;

ALTER TABLE `sys_icd9cm3`
  ADD COLUMN `code_std` tinyint NOT NULL DEFAULT '0' COMMENT '编码口径（1-医保版贯标码 0-历史缩写码）' AFTER `op_category`;
