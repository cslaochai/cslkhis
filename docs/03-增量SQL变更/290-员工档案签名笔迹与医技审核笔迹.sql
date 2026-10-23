-- ============================================================
-- 290-员工档案签名笔迹与医技报告签名PDF
-- 背景：员工档案预维护签名笔迹（手写板绘制，base64 PNG），
--       结诊处方签名、检查检验报告审核签名时直接引用，不再每次手写。
--       检查/检验审核通过后自动生成「报告签名 PDF」：
--         * PDF 内含报告医师/审核医师签名区（审核医师手写笔迹来自员工档案）；
--         * 对 PDF 文件字节做 SHA-256 + 审核医师证书签名（文件级数字签名），
--           签名元数据落报告记录表，供下载验签。
--       结诊提交后生成处方签 PDF（含医师手写笔迹 + 聚合支付码）。
-- 目标库：hn_biz_his @ 192.168.88.132:3306
-- ============================================================
SET NAMES utf8mb4;

-- 员工档案：签名笔迹（base64 PNG）
ALTER TABLE `sys_employee` ADD COLUMN `sign_image` longtext COMMENT '签名笔迹（base64 PNG，结诊/报告审核签名引用）' AFTER `avatar`;

-- 病历：处方签PDF路径（结诊提交时生成）
ALTER TABLE `biz_medical_record` ADD COLUMN `rx_pdf_path` varchar(300) DEFAULT NULL COMMENT '处方签PDF路径（结诊时生成）' AFTER `guide_pdf_path`;

-- 检验记录：签名报告PDF元数据（文件级数字签名）
ALTER TABLE `biz_laboratory_record`
    ADD COLUMN `report_pdf_path` varchar(300) DEFAULT NULL COMMENT '签名报告PDF路径（审核通过时生成）' AFTER `audit_signed_time`,
    ADD COLUMN `report_pdf_hash` varchar(64) DEFAULT NULL COMMENT '报告PDF文件SHA-256' AFTER `report_pdf_path`,
    ADD COLUMN `report_pdf_sign` text DEFAULT NULL COMMENT '报告PDF文件签名值（Base64）' AFTER `report_pdf_hash`,
    ADD COLUMN `report_pdf_cert_id` bigint DEFAULT NULL COMMENT '报告PDF签名证书ID' AFTER `report_pdf_sign`,
    ADD COLUMN `report_pdf_signed_time` datetime DEFAULT NULL COMMENT '报告PDF签名时刻' AFTER `report_pdf_cert_id`;

-- 检查记录：签名报告PDF元数据（文件级数字签名）
ALTER TABLE `biz_inspection_record`
    ADD COLUMN `report_pdf_path` varchar(300) DEFAULT NULL COMMENT '签名报告PDF路径（审核通过时生成）' AFTER `audit_signed_time`,
    ADD COLUMN `report_pdf_hash` varchar(64) DEFAULT NULL COMMENT '报告PDF文件SHA-256' AFTER `report_pdf_path`,
    ADD COLUMN `report_pdf_sign` text DEFAULT NULL COMMENT '报告PDF文件签名值（Base64）' AFTER `report_pdf_hash`,
    ADD COLUMN `report_pdf_cert_id` bigint DEFAULT NULL COMMENT '报告PDF签名证书ID' AFTER `report_pdf_sign`,
    ADD COLUMN `report_pdf_signed_time` datetime DEFAULT NULL COMMENT '报告PDF签名时刻' AFTER `report_pdf_cert_id`;
