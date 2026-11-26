-- ============================================================
-- 389 病历归档表补纸质签名核验列（修病案管理页 500）
-- 日期：2026-11-26
-- 根因：实体 BizMedicalRecordArchive / VO 已映射纸质核验 7 字段，
--   但 biz_medical_record_archive 表未建对应列；归档列表/详情全字段 SELECT
--   抛 Unknown column 'paper_verify_status'，兜成 500，病案管理页打不开。
-- 口径：纸质核验是电子签名的替代凭证，只登记核验事实，不回写病历 sign_status。
-- ============================================================

ALTER TABLE `biz_medical_record_archive`
  ADD COLUMN `paper_verify_status` tinyint NOT NULL DEFAULT 0 COMMENT '纸质签名核验状态（0-未核验 1-已核验）' AFTER `seal_time`,
  ADD COLUMN `paper_cert_no`       varchar(64) NULL COMMENT '纸质签名件编号（纸质病历号/档案号）' AFTER `paper_verify_status`,
  ADD COLUMN `paper_verify_by_id`  bigint NOT NULL DEFAULT 0 COMMENT '纸质签名核验人ID' AFTER `paper_cert_no`,
  ADD COLUMN `paper_verify_by_name` varchar(50) NULL COMMENT '纸质签名核验人姓名' AFTER `paper_verify_by_id`,
  ADD COLUMN `paper_verify_time`   datetime NULL COMMENT '纸质签名核验时间' AFTER `paper_verify_by_name`,
  ADD COLUMN `paper_verify_remark` varchar(500) NULL COMMENT '纸质签名核验说明' AFTER `paper_verify_time`,
  ADD COLUMN `sign_method`          tinyint NOT NULL DEFAULT 0 COMMENT '归档签名方式（0-未确定 1-电子签名 2-纸质签名核验）' AFTER `paper_verify_remark`;

-- 回查：
-- SELECT paper_verify_status, paper_cert_no, paper_verify_by_id, paper_verify_by_name,
--        paper_verify_time, paper_verify_remark, sign_method
--   FROM biz_medical_record_archive WHERE del_flag=0 LIMIT 1;

-- 回滚：
-- ALTER TABLE `biz_medical_record_archive`
--   DROP COLUMN `sign_method`, DROP COLUMN `paper_verify_remark`, DROP COLUMN `paper_verify_time`,
--   DROP COLUMN `paper_verify_by_name`, DROP COLUMN `paper_verify_by_id`, DROP COLUMN `paper_cert_no`,
--   DROP COLUMN `paper_verify_status`;
