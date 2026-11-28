-- ============================================================
-- 422 回滚：撤销全院住院存量铺底（create_by='sql422'）
-- 顺序：先按 admission 释放床位 -> 删除 admission -> 重算病区占用数。
-- ============================================================
SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 释放本次占用的床位
UPDATE sys_bed b
SET b.bed_status = 1, b.patient_id = NULL,
    b.update_by = 'sql422-rollback', b.update_time = NOW()
WHERE b.bed_id IN (
    SELECT a.bed_id FROM biz_admission a
    WHERE a.create_by = 'sql422' AND a.bed_id IS NOT NULL AND a.bed_id > 0
);

-- 2. 删除本次铺底的入院记录
DELETE FROM biz_admission WHERE create_by = 'sql422';

-- 3. 重算病区占用数
UPDATE sys_ward w
SET occupied_beds = (
      SELECT COUNT(*) FROM sys_bed b
      WHERE b.ward_id = w.ward_id AND b.del_flag = 0 AND b.bed_status = 2),
    update_by = 'sql422-rollback', update_time = NOW();

COMMIT;
