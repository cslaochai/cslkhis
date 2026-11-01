-- 312: 签名证书接入 CA 在线申请（csssl RA）
-- 为什么加这几列：
--   1. 证书签发从「线下送 CA」升级为「在线提交 CA 申请」：生成密钥对后 HIS 直接调用
--      csssl 对外 API 提交申请（externalRequestId = cert_no，断网重试幂等），
--      CA 审核签发后 HIS 刷新状态并自动下载证书链回填生效。
--   2. ca_apply_no / ca_apply_status 保存 CA 申请单号与状态，页面据此展示「待审核 / 已签发 / 被驳回」。
--      FAILED 是 HIS 本地提交失败（CA 不可达、凭证错误、模板算法不匹配），CA 侧不存在该状态。
--   3. ca_apply_attempt 记录提交次数：首次用 cert_no，驳回或失败后重提用 cert_no-2、cert_no-3，
--      避免同一 externalRequestId 被 CA 幂等重放成上一张被驳回的单。
--   4. ca_last_message 保存最近一次状态说明/失败原因，页面直接展示；写入前截断到列宽。

ALTER TABLE `sys_sign_cert`
    ADD COLUMN `ca_apply_no` varchar(32) DEFAULT NULL COMMENT 'CA在线申请单号' AFTER `ca_subject_dn`,
  ADD COLUMN `ca_apply_status` varchar(16) DEFAULT NULL COMMENT 'CA申请单状态（PENDING-待审核 APPROVED-已批准待签发 ISSUED-已签发 REJECTED-已驳回 CANCELLED-已撤销 FAILED-提交失败）' AFTER `ca_apply_no`,
  ADD COLUMN `ca_apply_attempt` int NOT NULL DEFAULT '0' COMMENT 'CA申请提交次数' AFTER `ca_apply_status`,
  ADD COLUMN `ca_applied_at` datetime DEFAULT NULL COMMENT '最近一次提交CA时间' AFTER `ca_apply_attempt`,
  ADD COLUMN `ca_last_message` varchar(500) DEFAULT NULL COMMENT 'CA最近一次状态说明或提交失败原因' AFTER `ca_applied_at`;

-- issued_mode 取值扩展：3-CA在线签发（原 1-人工签发 2-系统自动签发）
ALTER TABLE `sys_sign_cert`
    MODIFY COLUMN `issued_mode` tinyint NOT NULL DEFAULT '1' COMMENT '签发方式（1-人工签发 2-系统自动签发 3-CA在线签发）';
