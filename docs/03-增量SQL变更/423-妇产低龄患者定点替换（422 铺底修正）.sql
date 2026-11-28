-- ============================================================
-- 423 妇产低龄患者定点替换（422 全院住院存量铺底的边界修正）
-- 日期：2026-11-27
-- 背景：422 铺底时妇科/产科仅约束性别未约束年龄下限，落入 2 条低龄边界记录
--       （产科 14 岁、妇科 13 岁），与真实收治人群不符。本次替换为适龄女性空闲患者。
-- 口径：床位/病区/医生/诊断/入院时间均不变，仅替换 admission.patient_id 与 sys_bed.patient_id。
-- 幂等：按 admission_no 定位，且限定 create_by='sql422'；重复执行不会二次替换。
-- 回滚：见同目录 423-回滚.sql。
-- ============================================================
SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 产科二病区 02 床：杨萌(14) -> 马霞(25, 女)
UPDATE biz_admission
   SET patient_id = 2125844886132404225, update_by = 'sql423', update_time = NOW()
 WHERE admission_no = 'ZY2026SIM0027' AND create_by = 'sql422';
UPDATE sys_bed
   SET patient_id = 2125844886132404225, update_by = 'sql423', update_time = NOW()
 WHERE bed_id = 8620000000000001632;

-- 2. 妇科二病区 02 床：王语琪(13) -> 陈雅琳(41, 女)
UPDATE biz_admission
   SET patient_id = 2126219792279998467, update_by = 'sql423', update_time = NOW()
 WHERE admission_no = 'ZY2026SIM0043' AND create_by = 'sql422';
UPDATE sys_bed
   SET patient_id = 2126219792279998467, update_by = 'sql423', update_time = NOW()
 WHERE bed_id = 8620000000000001612;

COMMIT;
