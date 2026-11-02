-- ============================================================================
-- 319-重复在途签名证书清理
-- ============================================================================
-- 背景：
--   办理签名证书入口（keygen）此前只拦「已持有有效证书」，不拦「已有一张待CA签发」
--   的在途证书；新建医生会自动生成一张，管理员再手工点「办理」就给同一个人攒出
--   多张「待CA签发」（实测同一员工 3 张，且各有一次 CA 申请流水，重复送单）。
--   后端已补唯一性校验（keygen 拦在途证书，报错带原证书号与申请单状态），
--   本脚本清理存量：同一员工多张待签发证书只留 id 最大的一张（最近一次申请流程），
--   其余按系统内吊销的落库口径置为已吊销（cert_status=2 + revoke_reason/time/by_name），
--   留痕可查、不物理删行。
--
-- 口径：
--   - 保留行：同员工（del_flag=0 且 cert_status=0）中 id 最大的一张；
--   - 吊销行：revoke_reason='重复申请清理：同一员工同刻只允许一张待CA签发证书（319号脚本）'，
--     revoke_by_name='数据清理脚本319'，revoke_time=执行时刻；
--   - 被吊销证书的申请流水：仅在途状态（PENDING/APPROVED）置为 CANCELLED 并写说明，
--     终态行（ISSUED/REJECTED/CANCELLED/FAILED）不动；
--   - 只处理待签发重复，不动「有效」「已吊销」行。
--
-- ⚠ CA 侧在途申请单：HIS 置 CANCELLED 后，若 CA 中心侧对应申请单仍在途，
--   需在 CA 中心手工撤单；CA 后续推送的状态回执按状态机校验，不会覆盖终态。
-- ⚠ 幂等：UPDATE 全部带 cert_status 条件，重复执行 0 行。
-- ⚠ 回滚：见文末（从备份表恢复原状态）。
-- ============================================================================

-- ---------------- 前置备份（可重复执行；回滚依据） ----------------
CREATE TABLE IF NOT EXISTS `sys_sign_cert_bak319` AS SELECT * FROM `sys_sign_cert`;
CREATE TABLE IF NOT EXISTS `sys_sign_ca_apply_bak319` AS SELECT * FROM `sys_sign_ca_apply`;

-- ---------------- 执行前核对：哪些员工有几张在途 ----------------
SELECT `emp_id`, `emp_name`, COUNT(*) AS pending_cnt,
       GROUP_CONCAT(`cert_no` ORDER BY `id` DESC SEPARATOR ' | ') AS cert_nos
FROM `sys_sign_cert`
WHERE `del_flag` = 0 AND `cert_status` = 0
GROUP BY `emp_id`, `emp_name`
HAVING COUNT(*) > 1;

-- ---------------- 清理 1：多余在途证书置为已吊销（保留每人 id 最大一张） ----------------
UPDATE `sys_sign_cert` c
JOIN (
    SELECT `emp_id`, MAX(`id`) AS keep_id
    FROM `sys_sign_cert`
    WHERE `del_flag` = 0 AND `cert_status` = 0
    GROUP BY `emp_id`
    HAVING COUNT(*) > 1
) d ON c.`emp_id` = d.`emp_id` AND c.`id` <> d.`keep_id`
SET c.`cert_status`     = 2,
    c.`revoke_reason`   = '重复申请清理：同一员工同刻只允许一张待CA签发证书（319号脚本）',
    c.`revoke_time`     = NOW(),
    c.`revoke_by_name`  = '数据清理脚本319',
    c.`update_time`     = NOW()
WHERE c.`del_flag` = 0
  AND c.`cert_status` = 0;

-- ---------------- 清理 2：被吊销证书的在途申请流水置为 CANCELLED ----------------
UPDATE `sys_sign_ca_apply` a
JOIN `sys_sign_cert` c ON c.`id` = a.`cert_id`
SET a.`apply_status` = 'CANCELLED',
    a.`ca_message`   = CONCAT('HIS侧重复申请清理（319号脚本）：证书已按重复在途吊销，原状态 ', a.`apply_status`),
    a.`update_time`  = NOW()
WHERE c.`cert_status` = 2
  AND c.`revoke_by_name` = '数据清理脚本319'
  AND a.`apply_status` IN ('PENDING', 'APPROVED');

-- ---------------- 回查（期望：每人待签发至多 1 张；被吊销行的流水无在途状态） ----------------
SELECT `emp_id`, `emp_name`, COUNT(*) AS pending_cnt
FROM `sys_sign_cert`
WHERE `del_flag` = 0 AND `cert_status` = 0
GROUP BY `emp_id`, `emp_name`
HAVING COUNT(*) > 1;

SELECT c.`cert_no`, c.`emp_name`, c.`cert_status`, c.`revoke_reason`, c.`revoke_time`
FROM `sys_sign_cert` c
WHERE c.`revoke_by_name` = '数据清理脚本319'
ORDER BY c.`id`;

SELECT a.`cert_no`, a.`apply_seq`, a.`apply_no`, a.`apply_status`, a.`ca_message`
FROM `sys_sign_ca_apply` a
JOIN `sys_sign_cert` c ON c.`id` = a.`cert_id`
WHERE c.`revoke_by_name` = '数据清理脚本319'
ORDER BY a.`cert_no`, a.`apply_seq`;

-- ---------------- 回滚（恢复被清理行的原状态；先核对备份行数再执行） ----------------
-- UPDATE `sys_sign_cert` c
-- JOIN `sys_sign_cert_bak319` b ON b.`id` = c.`id`
-- SET c.`cert_status`    = b.`cert_status`,
--     c.`revoke_reason`  = b.`revoke_reason`,
--     c.`revoke_time`    = b.`revoke_time`,
--     c.`revoke_by_name` = b.`revoke_by_name`,
--     c.`update_time`    = b.`update_time`
-- WHERE c.`revoke_by_name` = '数据清理脚本319';
--
-- UPDATE `sys_sign_ca_apply` a
-- JOIN `sys_sign_ca_apply_bak319` b ON b.`id` = a.`id`
-- SET a.`apply_status` = b.`apply_status`,
--     a.`ca_message`   = b.`ca_message`,
--     a.`update_time`  = b.`update_time`
-- WHERE a.`apply_status` = 'CANCELLED'
--   AND a.`ca_message` LIKE 'HIS侧重复申请清理（319号脚本）%';
--
-- DROP TABLE IF EXISTS `sys_sign_cert_bak319`, `sys_sign_ca_apply_bak319`;
