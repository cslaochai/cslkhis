-- ============================================================
-- 247-AI运营问数敏感字段配置.sql
-- 前置：245/246 已执行（sys_ai_qa_column 已建且含 column_type）
-- 说明：敏感字段集中维护在列级配置表 sys_ai_qa_column：
--   sensitive_type 0-普通 1-脱敏（出参打码） 2-禁止出库（SQL 层拦截，结果不出库）
--   mask_type 脱敏方式：phone-手机号 idcard-身份证 email-邮箱 middle-姓名/中间打码
-- 幂等：加列用 information_schema 判断；铺底 UPDATE 按 表.列 定位
-- ============================================================

-- 1. 幂等加列
SET @db = DATABASE();
SET @sql = (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='sys_ai_qa_column' AND COLUMN_NAME='sensitive_type'),
        'SELECT 1',
        'ALTER TABLE sys_ai_qa_column ADD COLUMN sensitive_type tinyint NOT NULL DEFAULT 0 COMMENT ''敏感类型（0-普通 1-脱敏 2-禁止出库）'' AFTER is_enabled'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='sys_ai_qa_column' AND COLUMN_NAME='mask_type'),
        'SELECT 1',
        'ALTER TABLE sys_ai_qa_column ADD COLUMN mask_type varchar(16) NULL COMMENT ''脱敏方式（phone-手机号 idcard-身份证 email-邮箱 middle-姓名/中间打码）'' AFTER sensitive_type'
    )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. 铺底：白名单 11 张启用表内的敏感列（患者手机号禁止出库；患者姓名/患者号出参脱敏）
--    手机号：检验记录携带患者手机号 → 禁止出库（结果层拦截）
UPDATE sys_ai_qa_column SET sensitive_type=2, mask_type='phone', update_time=NOW()
WHERE del_flag=0 AND table_name='biz_laboratory_record' AND column_name='phone';

-- 患者姓名 / 患者号：可统计可查，但出参打码（mask_type=middle，SensitiveMaskUtils.maskMiddle）
UPDATE sys_ai_qa_column SET sensitive_type=1, mask_type='middle', update_time=NOW()
WHERE del_flag=0 AND column_name IN ('patient_name','patient_no')
  AND table_name IN ('biz_admission','biz_visit','biz_fee_record','biz_settlement_bill','biz_payment_txn',
                     'biz_prescription','biz_laboratory_record','biz_operation_apply');
