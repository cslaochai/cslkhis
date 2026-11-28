-- ============================================================
-- 438 审计留痕表分区与归档
-- 日期：2026-11-28
-- 背景：sys_oper_log / sys_field_change_log 是纯追加的审计流水，按「每行一个请求 / 每行一个字段变更」
--       增长。三甲规模粗估：字段留痕 3000 万行/年，接口日志（oper_param/json_result 存整对象 JSON）
--       一年上百 GB，比字段留痕大一个量级。这两张表都缺三样东西：
--         ① 时间分区——老数据没法整段下线，只能 DELETE，删一次锁表半天；
--         ② 不可删约束——挂着 del_flag + BaseEntity 的 update_by/update_time，技术上谁都能改能"逻辑删"；
--         ③ 归档元数据——导出的文件散在文件系统上，事后说不清"哪段数据在哪、有没有被改过"。
-- 法规口径（不因本脚本改变，只补基础设施）：
--   《电子病历应用管理规范（试行）》第十四条：保存历次操作印痕、操作时间、操作人，可查询可追溯；
--   第十七条：归档后原则上不得修改，确需修改须审批并保留修改痕迹；
--   第十九条：门（急）诊病历自末次就诊起不少于 15 年，住院病历自末次出院起不少于 30 年；
--   国卫办医政函〔2025〕262 号：全流程可追溯、防篡改。
--   → 审计表是"不能删的历史"，软删也不行；下线只能走"导出 + 校验 + 删分区"，且导出留索引可查。
-- 本脚本做四件事：
--   1) 两表按月 RANGE COLUMNS 分区（p_hist 吸收历史行 + 2026-01~2028-12 + pmax 兜底）；
--   2) 两表去掉 del_flag，改 append-only（Java 侧同步去掉 @TableLogic，否则查不到会整列空白）；
--   3) 两表加 trace_id，与 TraceIdFilter 写进 MDC 的 traceId 同值，串起"接口日志 ↔ 字段留痕"；
--   4) 建 sys_audit_archive_index：归档文件的唯一台账（行数 / 字节数 / SHA-256 / 覆盖时段）。
-- 口径：
--   1) 分区键 = 业务时间列（oper_time / change_time），不是 id、不是 create_time；
--      MySQL 要求所有唯一键都含分区列，故主键改为 (id, 业务时间)。id 仍由雪花生成、全局唯一，
--      应用侧 selectById 不受影响。
--   2) p_hist 必须存在：库里已有早于 2026-01-01 的行，不给兜底分区 ALTER 会直接报 1526 失败。
--   3) pmax 必须存在：分区维护任务没跑（或漏跑）时，新数据落 pmax 而不是报错写不进。
--   4) 归档保留期：sys_oper_log 12 个月、sys_field_change_log 36 个月（Java 侧常量，见 AuditTableMaintainTask）。
--      病历类字段留痕的 15/30 年不在 MySQL 热层等，靠归档文件 + 索引满足。
--   5) 二级索引在分区表上是 local index（每分区一棵），保留查询真用得上的四个，去掉两个常量级索引。
-- 前置：分区表不允许外键。若库里有 powerdesigner 带过来的 FK，本脚本会先摘掉（无则空操作）。
-- 幂等：建表带 IF NOT EXISTS；ALTER 不幂等，重放会在「分区已存在 / 列已存在」处报错中断，
--       属预期（增量脚本按序执行一次）。
-- 回滚：见同目录 438-回滚.sql。
-- ============================================================
SET NAMES utf8mb4;

-- ---------- 0. 前置核对 ----------
-- 确认两表当前体量，评估 ALTER 窗口（alter 会重建表，锁表时间 ≈ 数据量）：
-- SELECT TABLE_NAME, TABLE_ROWS, DATA_LENGTH/1024/1024 AS data_mb
--   FROM information_schema.TABLES
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('sys_oper_log','sys_field_change_log');
-- 确认无外键（预期都是 0）：
-- SELECT TABLE_NAME, CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS
--  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME IN ('sys_oper_log','sys_field_change_log');

-- ---------- 1. 摘掉外键（分区表不允许 FK） ----------
-- sys_field_change_log 在 powerdesigner 全量脚本里带 dept_id / operator_id 两条 FK；
-- 初始化 DDL 基线里没有。这里逐个探测、有则摘，避免手工判断。
SET @fk_name := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS
                 WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_field_change_log' LIMIT 1);
SET @ddl := IF(@fk_name IS NULL, 'DO 0', CONCAT('ALTER TABLE sys_field_change_log DROP FOREIGN KEY `', @fk_name, '`'));
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk_name := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS
                 WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_field_change_log' LIMIT 1);
SET @ddl := IF(@fk_name IS NULL, 'DO 0', CONCAT('ALTER TABLE sys_field_change_log DROP FOREIGN KEY `', @fk_name, '`'));
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk_name := (SELECT CONSTRAINT_NAME FROM information_schema.REFERENTIAL_CONSTRAINTS
                 WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_oper_log' LIMIT 1);
SET @ddl := IF(@fk_name IS NULL, 'DO 0', CONCAT('ALTER TABLE sys_oper_log DROP FOREIGN KEY `', @fk_name, '`'));
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 2. sys_oper_log：列治理 + 索引瘦身 + 主键带时间 ----------
-- oper_time 置 NOT NULL：分区键为 NULL 时落 p_hist，事后按时间归档永远捞不到它。
ALTER TABLE `sys_oper_log`
  MODIFY COLUMN `oper_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  ADD COLUMN `trace_id` varchar(64) DEFAULT NULL COMMENT '请求追踪号（X-Trace-Id，与 trace 日志/MDC 同值）' AFTER `oper_time`;

-- append-only：去掉逻辑删除列（Java 侧同步去掉 @TableLogic，见 SysOperLog）
ALTER TABLE `sys_oper_log` DROP COLUMN `del_flag`;

-- 二级索引只留真用得上的：按时间查（审计默认）、按人查、按「时间+成败」筛。
-- business_type / status 各只有 2~8 个值，单列索引对分区表毫无收益，删掉减写放大。
ALTER TABLE `sys_oper_log`
  DROP INDEX `idx_business_type`,
  DROP INDEX `idx_status`,
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`id`, `oper_time`);

ALTER TABLE `sys_oper_log`
PARTITION BY RANGE COLUMNS(`oper_time`) (
    PARTITION p_hist  VALUES LESS THAN ('2026-01-01'),
    PARTITION p202601 VALUES LESS THAN ('2026-02-01'),
    PARTITION p202602 VALUES LESS THAN ('2026-03-01'),
    PARTITION p202603 VALUES LESS THAN ('2026-04-01'),
    PARTITION p202604 VALUES LESS THAN ('2026-05-01'),
    PARTITION p202605 VALUES LESS THAN ('2026-06-01'),
    PARTITION p202606 VALUES LESS THAN ('2026-07-01'),
    PARTITION p202607 VALUES LESS THAN ('2026-08-01'),
    PARTITION p202608 VALUES LESS THAN ('2026-09-01'),
    PARTITION p202609 VALUES LESS THAN ('2026-10-01'),
    PARTITION p202610 VALUES LESS THAN ('2026-11-01'),
    PARTITION p202611 VALUES LESS THAN ('2026-12-01'),
    PARTITION p202612 VALUES LESS THAN ('2027-01-01'),
    PARTITION p202701 VALUES LESS THAN ('2027-02-01'),
    PARTITION p202702 VALUES LESS THAN ('2027-03-01'),
    PARTITION p202703 VALUES LESS THAN ('2027-04-01'),
    PARTITION p202704 VALUES LESS THAN ('2027-05-01'),
    PARTITION p202705 VALUES LESS THAN ('2027-06-01'),
    PARTITION p202706 VALUES LESS THAN ('2027-07-01'),
    PARTITION p202707 VALUES LESS THAN ('2027-08-01'),
    PARTITION p202708 VALUES LESS THAN ('2027-09-01'),
    PARTITION p202709 VALUES LESS THAN ('2027-10-01'),
    PARTITION p202710 VALUES LESS THAN ('2027-11-01'),
    PARTITION p202711 VALUES LESS THAN ('2027-12-01'),
    PARTITION p202712 VALUES LESS THAN ('2028-01-01'),
    PARTITION p202801 VALUES LESS THAN ('2028-02-01'),
    PARTITION p202802 VALUES LESS THAN ('2028-03-01'),
    PARTITION p202803 VALUES LESS THAN ('2028-04-01'),
    PARTITION p202804 VALUES LESS THAN ('2028-05-01'),
    PARTITION p202805 VALUES LESS THAN ('2028-06-01'),
    PARTITION p202806 VALUES LESS THAN ('2028-07-01'),
    PARTITION p202807 VALUES LESS THAN ('2028-08-01'),
    PARTITION p202808 VALUES LESS THAN ('2028-09-01'),
    PARTITION p202809 VALUES LESS THAN ('2028-10-01'),
    PARTITION p202810 VALUES LESS THAN ('2028-11-01'),
    PARTITION p202811 VALUES LESS THAN ('2028-12-01'),
    PARTITION p202812 VALUES LESS THAN ('2029-01-01'),
    PARTITION pmax    VALUES LESS THAN (MAXVALUE)
);

-- ---------- 3. sys_field_change_log：列治理 + 索引瘦身 + 主键带时间 ----------
ALTER TABLE `sys_field_change_log`
  ADD COLUMN `trace_id` varchar(64) DEFAULT NULL COMMENT '请求追踪号（X-Trace-Id，与 sys_oper_log.trace_id 同值）' AFTER `batch_no`;

ALTER TABLE `sys_field_change_log` DROP COLUMN `del_flag`;

-- idx_fc_field(biz_type,field_name) 删：查询走 like '%x%'，索引用不上，白担写放大。
ALTER TABLE `sys_field_change_log`
  DROP INDEX `idx_fc_field`,
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`id`, `change_time`);

ALTER TABLE `sys_field_change_log`
PARTITION BY RANGE COLUMNS(`change_time`) (
    PARTITION p_hist  VALUES LESS THAN ('2026-01-01'),
    PARTITION p202601 VALUES LESS THAN ('2026-02-01'),
    PARTITION p202602 VALUES LESS THAN ('2026-03-01'),
    PARTITION p202603 VALUES LESS THAN ('2026-04-01'),
    PARTITION p202604 VALUES LESS THAN ('2026-05-01'),
    PARTITION p202605 VALUES LESS THAN ('2026-06-01'),
    PARTITION p202606 VALUES LESS THAN ('2026-07-01'),
    PARTITION p202607 VALUES LESS THAN ('2026-08-01'),
    PARTITION p202608 VALUES LESS THAN ('2026-09-01'),
    PARTITION p202609 VALUES LESS THAN ('2026-10-01'),
    PARTITION p202610 VALUES LESS THAN ('2026-11-01'),
    PARTITION p202611 VALUES LESS THAN ('2026-12-01'),
    PARTITION p202612 VALUES LESS THAN ('2027-01-01'),
    PARTITION p202701 VALUES LESS THAN ('2027-02-01'),
    PARTITION p202702 VALUES LESS THAN ('2027-03-01'),
    PARTITION p202703 VALUES LESS THAN ('2027-04-01'),
    PARTITION p202704 VALUES LESS THAN ('2027-05-01'),
    PARTITION p202705 VALUES LESS THAN ('2027-06-01'),
    PARTITION p202706 VALUES LESS THAN ('2027-07-01'),
    PARTITION p202707 VALUES LESS THAN ('2027-08-01'),
    PARTITION p202708 VALUES LESS THAN ('2027-09-01'),
    PARTITION p202709 VALUES LESS THAN ('2027-10-01'),
    PARTITION p202710 VALUES LESS THAN ('2027-11-01'),
    PARTITION p202711 VALUES LESS THAN ('2027-12-01'),
    PARTITION p202712 VALUES LESS THAN ('2028-01-01'),
    PARTITION p202801 VALUES LESS THAN ('2028-02-01'),
    PARTITION p202802 VALUES LESS THAN ('2028-03-01'),
    PARTITION p202803 VALUES LESS THAN ('2028-04-01'),
    PARTITION p202804 VALUES LESS THAN ('2028-05-01'),
    PARTITION p202805 VALUES LESS THAN ('2028-06-01'),
    PARTITION p202806 VALUES LESS THAN ('2028-07-01'),
    PARTITION p202807 VALUES LESS THAN ('2028-08-01'),
    PARTITION p202808 VALUES LESS THAN ('2028-09-01'),
    PARTITION p202809 VALUES LESS THAN ('2028-10-01'),
    PARTITION p202810 VALUES LESS THAN ('2028-11-01'),
    PARTITION p202811 VALUES LESS THAN ('2028-12-01'),
    PARTITION p202812 VALUES LESS THAN ('2029-01-01'),
    PARTITION pmax    VALUES LESS THAN (MAXVALUE)
);

-- ---------- 4. 归档索引表 ----------
-- 只存"文件在哪、多大、多少行、sha256、覆盖哪段时间"，不存数据本身。
-- 事后举证 = 索引行 + 归档文件，两者都能给人看；文件被改过，sha256 对不上。
CREATE TABLE IF NOT EXISTS `sys_audit_archive_index` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `table_name` varchar(64) NOT NULL COMMENT '源表名',
  `partition_name` varchar(64) NOT NULL COMMENT '源分区名',
  `period_start` datetime NOT NULL COMMENT '覆盖起始（含）',
  `period_end` datetime NOT NULL COMMENT '覆盖截止（不含）',
  `row_count` bigint NOT NULL COMMENT '导出行数',
  `file_path` varchar(500) NOT NULL COMMENT '归档文件绝对路径（CSV.gz）',
  `file_size` bigint NOT NULL COMMENT '文件字节数',
  `sha256` char(64) NOT NULL COMMENT '归档文件 SHA-256',
  `archived_by` varchar(64) NOT NULL COMMENT '归档执行人（系统任务固定 system:auditArchive）',
  `archived_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
  `retention_months` int NOT NULL COMMENT '归档时的热层保留月数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_archive_partition` (`table_name`,`partition_name`),
  KEY `idx_archive_time` (`archived_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审计归档文件台账';

-- ---------- 5. 收尾核对 ----------
-- 分区是否建好（期望两表各 38 个分区）：
-- SELECT TABLE_NAME, COUNT(*) AS partition_cnt FROM information_schema.PARTITIONS
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('sys_oper_log','sys_field_change_log')
--    AND PARTITION_NAME IS NOT NULL GROUP BY TABLE_NAME;
-- 主键是否带时间列：
-- SELECT TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION FROM information_schema.KEY_COLUMN_USAGE
--  WHERE TABLE_SCHEMA = DATABASE() AND CONSTRAINT_NAME = 'PRIMARY'
--    AND TABLE_NAME IN ('sys_oper_log','sys_field_change_log') ORDER BY TABLE_NAME, ORDINAL_POSITION;
