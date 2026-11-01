-- 20261101 病案归档支持「纸质签名核验」替代凭证
-- 背景：医师无电子签名证书时按医院制度走纸质病历手写签名，由病案管理科人工核验。
--       纸质核验是替代凭证，绝不回写 biz_medical_record.sign_status（避免伪造电子签名）。
--       归档放行条件改为「电子签名已签 或 纸质签名已核验」，并把签名方式固化到归档行。

ALTER TABLE `biz_medical_record_archive`
  ADD COLUMN `paper_verify_status` tinyint NOT NULL DEFAULT '0' COMMENT '纸质签名核验状态（0-未核验 1-已核验）' AFTER `seal_time`,
  ADD COLUMN `paper_cert_no` varchar(64) DEFAULT NULL COMMENT '纸质签名件编号（纸质病历号/档案号）' AFTER `paper_verify_status`,
  ADD COLUMN `paper_verify_by_id` bigint DEFAULT NULL COMMENT '纸质签名核验人ID' AFTER `paper_cert_no`,
  ADD COLUMN `paper_verify_by_name` varchar(50) DEFAULT NULL COMMENT '纸质签名核验人姓名' AFTER `paper_verify_by_id`,
  ADD COLUMN `paper_verify_time` datetime DEFAULT NULL COMMENT '纸质签名核验时间' AFTER `paper_verify_by_name`,
  ADD COLUMN `paper_verify_remark` varchar(500) DEFAULT NULL COMMENT '纸质签名核验说明' AFTER `paper_verify_time`,
  ADD COLUMN `sign_method` tinyint NOT NULL DEFAULT '0' COMMENT '归档签名方式（0-未确定 1-电子签名 2-纸质签名核验）' AFTER `paper_verify_remark`;

-- 回滚：
-- ALTER TABLE `biz_medical_record_archive`
--   DROP COLUMN `paper_verify_status`, DROP COLUMN `paper_cert_no`, DROP COLUMN `paper_verify_by_id`,
--   DROP COLUMN `paper_verify_by_name`, DROP COLUMN `paper_verify_time`, DROP COLUMN `paper_verify_remark`,
--   DROP COLUMN `sign_method`;
