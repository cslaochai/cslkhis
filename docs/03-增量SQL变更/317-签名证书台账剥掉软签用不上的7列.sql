-- ============================================================================
-- 317-签名证书台账（sys_sign_cert）剥掉软签用不上的 7 列
-- ============================================================================
-- 背景：
--   sys_sign_cert 的表结构是按「本地自签 + 外部 CA 回执 + 硬件 UKey」三条路一起铺的，
--   实际只有软签（本地托管私钥 + PIN 确认）在跑。逐列扫过读取点后，7 列 HIS 侧根本不读，
--   或只是另一列的派生副本，留着会让「证书台账」出现两套真相（改 key_algo 时
--   digest_algo/sign_algo 不会跟着变，签名用的是现算值，台账显示的是旧值）。
--
-- 删除的 7 列与理由：
--   digest_algo / sign_algo  摘要与签名算法由 key_algo 现算（SignCryptoUtil.digestAlgoFor /
--                            signAlgoFor），签名与验签都取现算值，落库列没有任何读取点，
--                            只是签发当时写进去的副本。
--   revoke_by                吊销操作人 ID；台账另有 revoke_by_name + create_by_id/update_by_id
--                            + 操作日志，按人名与审计链都能追回「谁吊销的」。
--   ca_subject_dn            送 CA 的 Subject DN 由「姓名 + 科室」按固定模板现拼（与提交 CA 的
--                            报文同一个方法），证书签发后主体直接从 cert_pem 解析。
--   ca_issuer                签发 CA 名称，cert_pem 里就有 issuer，页面无独立展示需求。
--   ca_apply_attempt         申请重提次数；重提只需要「换个外部请求号」，改用证书号 + 提交时刻
--                            拼号，不计数。
--   ca_applied_at            最近一次提交 CA 时间，仅详情页展示，CA 申请单号与状态说明已够定位。
--
-- 保留（外部 CA 留痕，与 CA 侧数据对齐的唯一凭据）：
--   cert_pem      CA 签发的证书原文，公钥/主体/有效期/issuer 全部由它解析，是留痕本体。
--   ca_serial     CA 证书序列号，与 CA 系统指认「同一张证书」的凭据，本系统无法重算。
--   ca_apply_no / ca_apply_status / ca_last_message
--                 HIS 拿它去 CA 查单、把驳回原因展示给经办人，是读取点不是留痕。
--
-- 顺带清掉 AI 表结构元数据里对应的 3 行（sys_ai_qa_column），否则 AI 问答还会按已删列
--   生成 SQL。
--
-- ⚠ 执行顺序：先发已改好的后端（实体已删这 7 个字段），再执行本脚本；
--   反过来执行时旧版后端读这些列会报 Unknown column。
-- ⚠ 删列不可逆，执行前先备份：
--   CREATE TABLE sys_sign_cert_bak317 AS SELECT * FROM sys_sign_cert;
-- ============================================================================

-- ---------------- 前置核对：7 列还在吗 ----------------
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_sign_cert'
  AND COLUMN_NAME IN ('digest_algo', 'sign_algo', 'revoke_by', 'ca_subject_dn',
                      'ca_apply_attempt', 'ca_applied_at', 'ca_issuer')
ORDER BY COLUMN_NAME;

ALTER TABLE `sys_sign_cert`
    DROP COLUMN `digest_algo`,
    DROP COLUMN `sign_algo`,
    DROP COLUMN `revoke_by`,
    DROP COLUMN `ca_subject_dn`,
    DROP COLUMN `ca_apply_attempt`,
    DROP COLUMN `ca_applied_at`,
    DROP COLUMN `ca_issuer`;

DELETE
FROM sys_ai_qa_column
WHERE table_name = 'sys_sign_cert'
  AND column_name IN ('digest_algo', 'sign_algo', 'revoke_by');

-- ---------------- 回查（期望：剩余列里查不到上面 7 个名字，且 ca_* 留痕列都在） ----------------
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'sys_sign_cert'
ORDER BY ORDINAL_POSITION;

SELECT COUNT(*) AS dropped_meta_rows
FROM sys_ai_qa_column
WHERE table_name = 'sys_sign_cert'
  AND column_name IN ('digest_algo', 'sign_algo', 'revoke_by');

-- ---------------- 回滚 ----------------
-- ALTER TABLE `sys_sign_cert`
--     ADD COLUMN `digest_algo` varchar(16) NOT NULL DEFAULT 'SHA256' COMMENT '摘要算法' AFTER `key_algo`,
--     ADD COLUMN `sign_algo` varchar(32) NOT NULL DEFAULT 'SHA256withRSA' COMMENT '签名算法' AFTER `digest_algo`,
--     ADD COLUMN `revoke_by` bigint COMMENT '吊销操作人员工ID' AFTER `revoke_time`,
--     ADD COLUMN `ca_subject_dn` varchar(255) DEFAULT NULL COMMENT 'CA签发申请单主体DN（线下送CA时用）' AFTER `pin_iterations`,
--     ADD COLUMN `ca_apply_attempt` int NOT NULL DEFAULT '0' COMMENT 'CA申请提交次数' AFTER `ca_apply_status`,
--     ADD COLUMN `ca_applied_at` datetime DEFAULT NULL COMMENT '最近一次提交CA时间' AFTER `ca_apply_attempt`,
--     ADD COLUMN `ca_issuer` varchar(128) COMMENT '签发CA名称（外部CA模式回执）' AFTER `ca_serial`;
-- 元数据 3 行按本文件 git 历史（改名前的 sys_ai_qa_column.sql）重新 INSERT 即可。
