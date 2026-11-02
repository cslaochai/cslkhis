-- 321 DRG 分组方案严格化：方案头 + 规则溯源 + 官方伪行显式化 + 2.0 院内推导规则修正
--
-- 背景（为什么改）：
--   1) 原 biz_drg_* 只有目录行，没有「方案头」：无法记录标准号、发布/生效日期、规则来源、
--      是否经双人复核、官方预期组数，也就无法判断库里的方案到底是完整接入还是残缺铺底。
--   2) 3.0 官方 xlsx 里每 MDC 的「?QY（其他手术歧义组）」与「?00（该 MDC 未入组占位）」
--      是官方结果码语义，不是真实病组。原实现把它们当普通行装载，会把 B000 之类占位码
--      当真实 DRG 返回；必须用 row_kind 显式区分。
--   3) 2.0 官方只发布 PDF，没有机读规则；原库内 MDC 规则是后补的简化版（MDCP 只看
--      DI_P00、MDCZ 只看 DI_Z00、MDCM/N 不看性别、先期分组排序靠后），会把新生儿、
--      多发伤、性别专属 MDC 错判。本次按官方分组器口径补成推导规则，并一律标
--      rule_source=INFERRED / verified=0，等双人复核与官方分组器回归后再置 verified=1。
--   4) 2.0 码集合缺 OP_SB1/OP_TB1/OP_WJ1/OP_XJ1/OP_YC1/OP_ZZ1 六个（官方 PDF 未给机读码表），
--      对应 6 个 ADRG 永不命中。这里不臆造码表，由引擎健康检查把缺失集合报出来。
--
-- 口径：
--   - 规则原文是唯一入组事实；rule_source 只标来源，不改变求值。
--   - verified=1 仅表示「规则来源官方且数量与官方一致」，不代表权重/支付标准已下发。
--   - 伪行不物理删除：QY 是合法结果码（sim_status=3 歧义组），?00 是「未入组」占位（返回 0000）。
--   - 本脚本为配置表迁移，重灌按 version 物理删，不走软删。
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

-- ----------------------------
-- biz_drg_scheme  分组方案头（版本 → 标准来源、规则可信度、官方预期规模）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `biz_drg_scheme` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `version` varchar(32) NOT NULL COMMENT '分组方案版本（与 biz_drg_mdc/adrg/group/set/ccmcc 的 version 同值）',
  `scheme_code` varchar(32) NOT NULL COMMENT '方案编码（如 CHS-DRG-3.0）',
  `scheme_name` varchar(100) NOT NULL COMMENT '方案名称',
  `standard_no` varchar(64) COMMENT '标准文号/发布文号',
  `publish_date` date COMMENT '官方发布日期',
  `effective_date` date COMMENT '统筹区执行日期',
  `source_file` varchar(500) COMMENT '规则载体（官方 xlsx/PDF 文件名与发布批次）',
  `rule_source` varchar(16) NOT NULL DEFAULT 'OFFICIAL' COMMENT '规则来源（OFFICIAL-官方机读规则 INFERRED-院内推导 LOCAL-本地自建）',
  `verified` tinyint NOT NULL DEFAULT '0' COMMENT '是否已复核（1-双人复核且与官方分组器回归一致 0-未复核）',
  `expected_mdc` int COMMENT '官方 MDC 真组数（不含伪行）',
  `expected_adrg` int COMMENT '官方 ADRG 真组数（不含 QY/00 伪行）',
  `expected_drg` int COMMENT '官方 DRG 真组数（不含 QY/000 伪行）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-草稿 1-启用 2-停用）',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认版本（1-是 0-否，同版本内唯一）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL DEFAULT '0' COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL DEFAULT '0' COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scheme_version` (`version`,`del_flag`),
  KEY `idx_scheme_default` (`is_default`,`status`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='DRG 分组方案头';

-- ----------------------------
-- 目录表补溯源列：MDC 补先期分组标记/判定步序/规则来源/复核标记
-- ----------------------------
ALTER TABLE `biz_drg_mdc`
  ADD COLUMN `pre_group_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否先期分组（1-是，按子 ADRG 优先判定；0-按本行规则判定）' AFTER `mdc_rule`,
  ADD COLUMN `group_step` tinyint NOT NULL DEFAULT '5' COMMENT '判定步序（1/2/3/4-先期分组，5-普通 MDC；同一 MDC 内再按 sort_no）' AFTER `pre_group_flag`,
  ADD COLUMN `rule_source` varchar(16) NOT NULL DEFAULT 'OFFICIAL' COMMENT '规则来源（OFFICIAL-官方 INFERRED-院内推导 LOCAL-本地自建）' AFTER `group_step`,
  ADD COLUMN `verified` tinyint NOT NULL DEFAULT '0' COMMENT '是否已复核（1-双人复核且回归一致 0-未复核）' AFTER `rule_source`;

-- ADRG/DRG 补行类型：区分官方伪行，禁止把 ?QY / ?00 当真实病组
ALTER TABLE `biz_drg_adrg`
  ADD COLUMN `row_kind` tinyint NOT NULL DEFAULT '0' COMMENT '行类型（0-正常入组 1-QY 其他手术歧义组 2-00 该 MDC 未入组占位）' AFTER `sort_no`;

ALTER TABLE `biz_drg_group`
  ADD COLUMN `row_kind` tinyint NOT NULL DEFAULT '0' COMMENT '行类型（0-正常入组 1-QY 歧义组 2-000 未入组占位）' AFTER `sort_no`;

-- 模拟结果状态增加「歧义组」：QY 是合法结果，但不应参与权重与盈亏统计
ALTER TABLE `biz_drg_sim_result`
  MODIFY COLUMN `sim_status` tinyint NOT NULL DEFAULT '1' COMMENT '结果（1-已入组 2-未入组 3-歧义组）';

-- ----------------------------
-- 方案头数据
-- ----------------------------
DELETE FROM `biz_drg_scheme` WHERE version IN ('chs-drg-2.0','3.0');
INSERT INTO `biz_drg_scheme`
(id, version, scheme_code, scheme_name, standard_no, publish_date, effective_date, source_file,
 rule_source, verified, expected_mdc, expected_adrg, expected_drg, status, is_default,
 create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
(30000001,'3.0','CHS-DRG-3.0','国家医保局按病组（DRG）付费 3.0 版分组方案',NULL,'2026-09-09',NULL,
 '国家医保局《按病组（DRG）付费3.0版分组方案配置信息》六表 xlsx（MDC/ADRG/DRG/集合/CC/排除）',
 'OFFICIAL',1,26,492,825,1,1,
 'admin','2026-01-01 00:00:00','admin','2026-01-01 00:00:00',0,
 '官方机读规则全量接入；QY/00 伪行保留原编码并标 row_kind；权重与支付标准由统筹区另行下发',1,1),
(20000001,'chs-drg-2.0','CHS-DRG-2.0','国家版 CHS-DRG 2.0 分组方案（院内推导规则）',NULL,NULL,NULL,
 '国家医保局 CHS-DRG 2.0 分组方案（PDF，无机读规则列）；码集合来自郴州基线库',
 'INFERRED',0,26,409,634,1,0,
 'admin','2026-01-01 00:00:00','admin','2026-01-01 00:00:00',0,
 'MDC 规则为院内按官方分组器口径推导；缺 OP_SB1/OP_TB1/OP_WJ1/OP_XJ1/OP_YC1/OP_ZZ1 六集合，健康检查会报出；复核前不得用于正式结算',1,1);

-- ----------------------------
-- 3.0 MDC：官方先期分组步序
--   1 MDCA（结构性先期，按子 ADRG 判定）
--   2 MDCP 新生儿（须无手术且出生日龄<29 天，或日龄<366 天且主诊断在 DI_PV1 且无手术）
--   3 MDCZ 多发严重创伤（两个及以上 Z 类诊断）
--   5 其余普通 MDC
-- ----------------------------
UPDATE `biz_drg_mdc`
   SET pre_group_flag = 0, group_step = 5, rule_source = 'OFFICIAL', verified = 1
 WHERE version = '3.0';

UPDATE `biz_drg_mdc`
   SET pre_group_flag = 1, group_step = 1, rule_source = 'OFFICIAL', verified = 1
 WHERE version = '3.0' AND mdc_code = 'MDCA';

UPDATE `biz_drg_mdc`
   SET pre_group_flag = 1, group_step = 2, rule_source = 'OFFICIAL', verified = 1
 WHERE version = '3.0' AND mdc_code = 'MDCP';

UPDATE `biz_drg_mdc`
   SET pre_group_flag = 1, group_step = 3, rule_source = 'OFFICIAL', verified = 1
 WHERE version = '3.0' AND mdc_code = 'MDCZ';

-- ----------------------------
-- 2.0 MDC：先期分组步序 + 院内推导规则
--   先期顺序：MDCA(结构性) → MDCP 新生儿 → MDCY HIV → MDCZ 多发伤 → 普通 MDC
--   以下规则均为 INFERRED：按官方分组器判定口径推导，未与官方分组器批量回归前 verified=0
-- ----------------------------
UPDATE `biz_drg_mdc`
   SET pre_group_flag = 0, group_step = 5, verified = 0
 WHERE version = 'chs-drg-2.0';

UPDATE `biz_drg_mdc`
   SET pre_group_flag = 1, group_step = 1, rule_source = 'OFFICIAL', verified = 0
 WHERE version = 'chs-drg-2.0' AND mdc_code = 'MDCA';

UPDATE `biz_drg_mdc`
   SET pre_group_flag = 1, group_step = 2, rule_source = 'INFERRED', verified = 0,
       mdc_rule = '((NL=0) and (XSRTL<29)) or ((NL=0) and (XSRTL>=29) and (XSRTL<366) and (ZYZD in DI_PV1))'
 WHERE version = 'chs-drg-2.0' AND mdc_code = 'MDCP';

UPDATE `biz_drg_mdc`
   SET pre_group_flag = 1, group_step = 3, rule_source = 'INFERRED', verified = 0,
       mdc_rule = 'ZYZD in DI_Y00'
 WHERE version = 'chs-drg-2.0' AND mdc_code = 'MDCY';

UPDATE `biz_drg_mdc`
   SET pre_group_flag = 1, group_step = 4, rule_source = 'INFERRED', verified = 0,
       mdc_rule = 'length(DI_Z00 ∩ {ZYZD, QTZD}) >= 2'
 WHERE version = 'chs-drg-2.0' AND mdc_code = 'MDCZ';

UPDATE `biz_drg_mdc`
   SET rule_source = 'INFERRED', verified = 0,
       mdc_rule = '(ZYZD in DI_M00) and (XB=1)'
 WHERE version = 'chs-drg-2.0' AND mdc_code = 'MDCM';

UPDATE `biz_drg_mdc`
   SET rule_source = 'INFERRED', verified = 0,
       mdc_rule = '(ZYZD in DI_N00) and (XB=2)'
 WHERE version = 'chs-drg-2.0' AND mdc_code = 'MDCN';

-- 2.0 其余普通 MDC 规则保持 ICD 集合判定，但同样属于推导结果
UPDATE `biz_drg_mdc`
   SET rule_source = 'INFERRED', verified = 0
 WHERE version = 'chs-drg-2.0' AND mdc_code NOT IN ('MDCA','MDCP','MDCY','MDCZ','MDCM','MDCN');

-- ----------------------------
-- ADRG/DRG 行类型回填
--   3.0：?QY（rule=ZYSS in OP_ALL）→ 1；空名称的 ?00 → 2；其余 0
--   2.0：官方 PDF 无伪行，全部 0
-- ----------------------------
UPDATE `biz_drg_adrg` SET row_kind = 0;
UPDATE `biz_drg_group` SET row_kind = 0;

UPDATE `biz_drg_adrg`
   SET row_kind = 1
 WHERE version = '3.0' AND RIGHT(adrg_code, 2) = 'QY';

UPDATE `biz_drg_adrg`
   SET row_kind = 2
 WHERE version = '3.0' AND (adrg_name IS NULL OR adrg_name = '') AND RIGHT(adrg_code, 2) = '00';

UPDATE `biz_drg_group`
   SET row_kind = 1
 WHERE version = '3.0' AND RIGHT(drg_code, 2) = 'QY';

UPDATE `biz_drg_group`
   SET row_kind = 2
 WHERE version = '3.0' AND (drg_name IS NULL OR drg_name = '') AND RIGHT(drg_code, 2) = '00';

-- ----------------------------
-- 字典：DRG 模拟入组状态补「歧义组」
--   不用 ON DUPLICATE KEY UPDATE：sys_dict_data 只有主键和 dict_type 普通索引，
--   id 恰好撞上别的字典行时会被静默改掉对方的标签与备注。这里按 (dict_type, dict_value) 判存在。
-- ----------------------------
INSERT INTO `sys_dict_data`
(id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
 create_by, create_time, update_by, update_time, del_flag, remark, dict_source, create_by_id, update_by_id)
SELECT 891000000000001214,'his_drg_sim_status','歧义组','3',3,NULL,'warning',0,1,
 'admin','2026-01-01 00:00:00','admin','2026-01-01 00:00:00',0,
 '命中官方 ?QY 其他手术歧义组：结果为合法歧义码，不计权重与盈亏',2,1,1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data` d
   WHERE d.dict_type = 'his_drg_sim_status' AND d.dict_value = '3' AND d.del_flag = 0
);

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;
