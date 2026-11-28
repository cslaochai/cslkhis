-- ============================================================
-- 438 回滚：撤销审计留痕表分区与归档
-- 说明：REMOVE PARTITIONING 只摘分区定义、不动数据；分区里的行会合并回单表。
--       主键恢复为 (id)；trace_id 与 sys_audit_archive_index 直接删。
--       del_flag 恢复默认 0（历史行本来就该全是 0，回滚后 MP 逻辑删除查询照旧可用）。
-- 注意：若已执行过归档任务（老分区被 DROP 掉），被删的数据只在归档文件里，
--       回滚无法把它们搬回库——如需恢复，用归档台账里的 file_path 反向导入。
-- ============================================================
SET NAMES utf8mb4;

-- ---------- 1. 摘分区 ----------
ALTER TABLE `sys_oper_log` REMOVE PARTITIONING;
ALTER TABLE `sys_field_change_log` REMOVE PARTITIONING;

-- ---------- 2. 主键恢复为单列 id ----------
ALTER TABLE `sys_oper_log`
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`id`);

ALTER TABLE `sys_field_change_log`
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`id`);

-- ---------- 3. 恢复被删的列与索引 ----------
ALTER TABLE `sys_oper_log`
  DROP COLUMN `trace_id`,
  ADD COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  ADD KEY `idx_business_type` (`business_type`),
  ADD KEY `idx_status` (`status`);

ALTER TABLE `sys_field_change_log`
  DROP COLUMN `trace_id`,
  ADD COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  ADD KEY `idx_fc_field` (`biz_type`, `field_name`);

-- ---------- 4. 删归档台账 ----------
DROP TABLE IF EXISTS `sys_audit_archive_index`;

-- ---------- 5. 收尾核对 ----------
-- SELECT TABLE_NAME, COUNT(*) AS partition_cnt FROM information_schema.PARTITIONS
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('sys_oper_log','sys_field_change_log')
--    AND PARTITION_NAME IS NOT NULL GROUP BY TABLE_NAME;   -- 期望无结果（0 行）
