-- ============================================================================
-- 318-签名证书台账（sys_sign_cert）剥掉 CA 申请三列
-- ============================================================================
-- 背景：
--   316 已把 CA 申请从「证书行上覆盖记最后一次」搬进 sys_sign_ca_apply 流水表
--   （一次申请一行，提交/审核/签发/回填时刻与回执说明全留痕），证书行的
--   ca_apply_no / ca_apply_status / ca_last_message 从此只剩「最新一条的副本」
--   这一个身份——同一事实两处落库，列表与详情各读一处就会出现两套真相。
--
--   本脚本把这三列删掉：证书表回归「一张证书的事实」，
--   申请单号 / 状态 / 回执说明一律由后端按 cert_id 取流水表最新一条对外。
--   连同 317 删掉的 4 列（ca_subject_dn / ca_issuer / ca_apply_attempt / ca_applied_at），
--   证书表上的 ca_* 申请过程列至此清零；保留的 cert_pem / ca_serial 是
--   「CA 签发的这张证书」本身的事实，不是申请过程。
--
-- 口径变化（后端已同步改造，见同批代码）：
--   列表 / 详情的「CA 申请」列 = sys_sign_ca_apply 该证书 id 最大一行；
--   线下人工回填后不再有「清状态码」的双写 hack —— 证书状态说证书的事实，
--   申请列说最近一次申请的事实，线上失败转线下回填的完整过程看详情时间线。
--
-- ⚠ 执行顺序：先发已改好的后端（实体已删这 3 个字段，读取全部走流水表），
--   再执行本脚本；反过来执行时旧版后端读写这些列会报 Unknown column。
-- ⚠ 前置依赖：316 必须已执行（本脚本不再迁移数据，流水表里没有的历史找不回来）。
-- ⚠ 删列不可逆，执行前先备份：
--   CREATE TABLE sys_sign_cert_bak318 AS SELECT * FROM sys_sign_cert;
-- ============================================================================

-- ---------------- 前置核对：3 列还在吗、流水表有数据吗 ----------------
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_sign_cert'
  AND COLUMN_NAME IN ('ca_apply_no', 'ca_apply_status', 'ca_last_message')
ORDER BY COLUMN_NAME;

-- 期望：三列上有值的证书行，都能在流水表找到对应行（316 迁移已兜底）
SELECT c.`id`, c.`cert_no`, c.`ca_apply_no`, c.`ca_apply_status`
FROM `sys_sign_cert` c
WHERE (c.`ca_apply_no` IS NOT NULL OR c.`ca_apply_status` IS NOT NULL)
  AND NOT EXISTS (SELECT 1 FROM `sys_sign_ca_apply` a WHERE a.`cert_id` = c.`id`);

ALTER TABLE `sys_sign_cert`
    DROP COLUMN `ca_apply_no`,
    DROP COLUMN `ca_apply_status`,
    DROP COLUMN `ca_last_message`;

-- AI 表结构元数据同步清掉（按信息_schema自动同步的库可能留有这三行，幂等）
DELETE
FROM sys_ai_qa_column
WHERE table_name = 'sys_sign_cert'
  AND column_name IN ('ca_apply_no', 'ca_apply_status', 'ca_last_message');

-- ---------------- 回查（期望：3 列查不到，cert_pem / ca_serial 仍在） ----------------
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_sign_cert'
ORDER BY ORDINAL_POSITION;

SELECT COUNT(*) AS dropped_meta_rows
FROM sys_ai_qa_column
WHERE table_name = 'sys_sign_cert'
  AND column_name IN ('ca_apply_no', 'ca_apply_status', 'ca_last_message');

-- ---------------- 回滚（只恢复列结构，列里的最后状态从流水表最新一条可回填） ----------------
-- ALTER TABLE `sys_sign_cert`
--     ADD COLUMN `ca_apply_no` varchar(32) DEFAULT NULL COMMENT 'CA在线申请单号' AFTER `pin_iterations`,
--     ADD COLUMN `ca_apply_status` varchar(16) DEFAULT NULL COMMENT 'CA申请单状态（PENDING-待审核 APPROVED-已批准待签发 ISSUED-已签发 REJECTED-已驳回 CANCELLED-已撤销 FAILED-提交失败）' AFTER `ca_apply_no`,
--     ADD COLUMN `ca_last_message` varchar(500) DEFAULT NULL COMMENT 'CA最近一次状态说明或提交失败原因' AFTER `ca_apply_status`;
-- UPDATE `sys_sign_cert` c
-- JOIN `sys_sign_ca_apply` a ON a.`cert_id` = c.`id`
--     AND a.`id` = (SELECT MAX(t.`id`) FROM `sys_sign_ca_apply` t WHERE t.`cert_id` = c.`id`)
-- SET c.`ca_apply_no` = a.`apply_no`,
--     c.`ca_apply_status` = a.`apply_status`,
--     c.`ca_last_message` = a.`ca_message`;
