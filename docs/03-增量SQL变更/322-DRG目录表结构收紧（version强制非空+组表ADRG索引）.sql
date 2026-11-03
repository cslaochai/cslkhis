-- ============================================================
-- 322 DRG 目录表结构收紧（version 强制非空 + 组表补 ADRG 索引）
-- 依据：agents.md「可空列上的 DEFAULT NULL 是冗余噪音」+ 引擎口径「一律 WHERE version=? 精确查」
-- 目标库：hn_biz_his  @ 192.168.88.132:3306
--
-- 问题 1（version 可空）：
--   MySQL 唯一键含 NULL 时不做唯一约束，同码同版本可插两条；更致命的是引擎所有查询都是
--   `WHERE version = #{version}`，version 为 NULL 的行永远查不到，表现为「这个组永远不命中」
--   且全程无报错。改 NOT NULL 后，漏赋 version 的导入会直接 1364 报错（fail loud）。
--   执行前已核对：5 张表 version 为 NULL 的行数均为 0。
--
-- 问题 2（组表缺 ADRG 反查索引）：
--   biz_drg_adrg 有 idx_adrg_mdc，但 biz_drg_group 只有 uk_drg_code(drg_code,...)，
--   「某 ADRG 下有哪些细分组」只能全表扫。补 idx_group_adrg。
--
-- 本目录 5 张表为纯配置表，重灌一律物理删（DELETE），不走软删（见 233 头部约定），
-- 故唯一键中的 del_flag 保留（符合「唯一键带 del_flag 或注释写死物理删」二选一）。
--
-- 可重复执行：MODIFY / ADD INDEX 重复跑会报「已存在」，属预期；本文件只跑一次。
-- ============================================================
SET NAMES utf8mb4;

ALTER TABLE `biz_drg_mdc`
  MODIFY COLUMN `version` varchar(32) NOT NULL COMMENT '分组方案版本';

ALTER TABLE `biz_drg_adrg`
  MODIFY COLUMN `version` varchar(32) NOT NULL COMMENT '分组方案版本';

ALTER TABLE `biz_drg_set`
  MODIFY COLUMN `version` varchar(32) NOT NULL COMMENT '分组方案版本';

ALTER TABLE `biz_drg_ccmcc`
  MODIFY COLUMN `version` varchar(32) NOT NULL COMMENT '分组方案版本（2.0/3.0）';

ALTER TABLE `biz_drg_group`
  MODIFY COLUMN `version` varchar(32) NOT NULL COMMENT '版本号';

ALTER TABLE `biz_drg_group`
  ADD INDEX `idx_group_adrg` (`adrg_code`, `version`, `del_flag`);
