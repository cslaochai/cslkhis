-- ============================================================================
-- 320-签名证书CA申请记录补员工维度（emp_id / emp_name）
-- ============================================================================
-- 背景：
--   sys_sign_ca_apply 原来只挂证书（cert_id + cert_no 冗余证书编号），没有员工维度。
--   按员工全局查「申请了什么、送到 CA 没有、卡在哪」必须回 JOIN sys_sign_cert。
--   本脚本补 emp_id / emp_name 两列（落行时冗余，历史单据不随证书行改名变化），
--   并建 emp_id 索引支撑员工履历直查；配套后端 start() 落行时写入员工信息，
--   新增 /emr/signCert/empTimeline 员工证书操作时间线（申请流水+吊销事件）。
--
-- 口径：
--   - emp_id / emp_name 取落行时刻证书行上的持有人（与申请事实同时刻）；
--   - 存量行回填：从 sys_sign_cert 按 cert_id 补齐，证书已被物理删除的行补不上
--     （本系统证书只逻辑删，正常都能补上）；
--   - 前置依赖：316 已执行（表存在）。
-- ⚠ 加列对旧版后端无害（MyBatis-Plus 实体没这俩字段时照常读写），
--   执行顺序不敏感；建议照常先发代码再执行脚本。
-- ⚠ 幂等：加列用 IF NOT EXISTS 语义（MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，
--   用 information_schema 判空跳过不行——脚本语境改用「重复执行报 1060 可忽略」，
--   回填 UPDATE 带 emp_id IS NULL 条件，重复执行 0 行。
-- ============================================================================

-- ---------------- 前置核对：表在吗、缺列吗 ----------------
SELECT COLUMN_NAME, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_sign_ca_apply'
  AND COLUMN_NAME IN ('emp_id', 'emp_name');

ALTER TABLE `sys_sign_ca_apply`
    ADD COLUMN `emp_id`   bigint      COMMENT '员工ID（证书持有人，冗余落行：按员工直查申请履历）' AFTER `cert_no`,
    ADD COLUMN `emp_name` varchar(64) COMMENT '员工姓名（冗余落行，历史单据不随证书行改名变化）' AFTER `emp_id`,
    ADD KEY `idx_caapply_emp` (`emp_id`, `submitted_at`);

-- ---------------- 存量回填：按 cert_id 从证书行补员工 ----------------
UPDATE `sys_sign_ca_apply` a
JOIN `sys_sign_cert` c ON c.`id` = a.`cert_id`
SET a.`emp_id`   = c.`emp_id`,
    a.`emp_name` = c.`emp_name`,
    a.`update_time` = a.`update_time`
WHERE a.`emp_id` IS NULL;

-- ---------------- 回查（期望：emp_id 为空的行数 = 0；员工维度分布） ----------------
SELECT COUNT(*) AS 未回填行数
FROM `sys_sign_ca_apply`
WHERE `emp_id` IS NULL;

SELECT `emp_id`, `emp_name`, COUNT(*) AS 申请单数,
       SUM(`apply_status` IN ('PENDING', 'APPROVED')) AS 在途数,
       MAX(`submitted_at`) AS 最近申请
FROM `sys_sign_ca_apply`
GROUP BY `emp_id`, `emp_name`
ORDER BY 最近申请 DESC;

-- ---------------- 回滚（加列可逆；索引随列删除） ----------------
-- ALTER TABLE `sys_sign_ca_apply`
--     DROP KEY `idx_caapply_emp`,
--     DROP COLUMN `emp_id`,
--     DROP COLUMN `emp_name`;
