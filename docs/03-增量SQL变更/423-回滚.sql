-- ============================================================
-- 423 回滚：恢复 422 铺底时的原患者（杨萌 14 / 王语琪 13）
-- ============================================================
SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 产科二病区 02 床 还原
UPDATE biz_admission
   SET patient_id = 2126220191405772802, update_by = 'sql423-rollback', update_time = NOW()
 WHERE admission_no = 'ZY2026SIM0027';
UPDATE sys_bed
   SET patient_id = 2126220191405772802, update_by = 'sql423-rollback', update_time = NOW()
 WHERE bed_id = 8620000000000001632;

-- 2. 妇科二病区 02 床 还原
UPDATE biz_admission
   SET patient_id = 2126218507472408577, update_by = 'sql423-rollback', update_time = NOW()
 WHERE admission_no = 'ZY2026SIM0043';
UPDATE sys_bed
   SET patient_id = 2126218507472408577, update_by = 'sql423-rollback', update_time = NOW()
 WHERE bed_id = 8620000000000001612;

COMMIT;
