-- =============================================================================
-- 211 · 住院护理等级落位（P2-G07）
--
-- 为什么先做这一步：需求层（sql/212）的住院侧测算公式是
--     Σ(在院患者 × 护理等级权重) × NHPPD → 当日护理工时 → ÷ 班次时长 → 需求人数
-- 护理等级是这个公式里唯一的权重输入。现状 `biz_admission` 根本没有这一列，
-- 只有 `biz_nursing_record.nursing_level`（生命体征记录顺手带的一个字段），
-- 全库只有 1 名患者有值 —— 也就是说，需求测算现在**没有权重可乘**。
-- 不把等级落到「在院」这条主记录上，后面所有 NHPPD 测算都是假的。
--
-- 三个设计决定（都写进列注释里，避免后来的人又搞混）：
--   1. 等级落在 `biz_admission`（一次住院一条），不落 `biz_patient`。
--      同一个患者这次住进来是一级、下次是三级，等级是**这次住院**的属性。
--   2. 带 `nursing_level_source`。因为第一版绝大多数是「默认兜底」，
--      如果没有来源标记，护士长在页面上看到的 51 个「二级护理」里，
--      分不清哪些是真的评过、哪些只是没评的默认值 —— 那就等于埋了个假数据。
--   3. 只回填**在院**（admit_status=1）的患者。已出院的历史不编，
--      编出来也没人对账，只会让「谁评的」这件事说不清。
--
-- 默认值为什么是 3（二级护理）：新入院未评定前按普通护理对待，这是
-- 护理分级的常规下限口径。特级/一级必须护士长（或医嘱）显式评定才会写入，
-- 也就是 source=2/3。默认值一旦被误当成评定结果，source 列能一眼看出来。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 加列（幂等：information_schema 判断后再 PREPARE）
-- -----------------------------------------------------------------------------
SET @c1 := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_admission' AND COLUMN_NAME = 'nursing_level');
SET @d1 := IF(@c1 = 0,
    CONCAT('ALTER TABLE biz_admission ADD COLUMN nursing_level tinyint NULL ',
           'COMMENT ''护理等级（1-特级 2-一级 3-二级 4-三级，字典 his_nursing_level）'' AFTER ward_id'),
    'DO 0');
PREPARE s1 FROM @d1; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @c2 := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_admission' AND COLUMN_NAME = 'nursing_level_source');
SET @d2 := IF(@c2 = 0,
    CONCAT('ALTER TABLE biz_admission ADD COLUMN nursing_level_source tinyint NOT NULL DEFAULT 1 ',
           'COMMENT ''护理等级来源（1-默认兜底 2-护理记录带出 3-护士长评定）'' AFTER nursing_level'),
    'DO 0');
PREPARE s2 FROM @d2; EXECUTE s2; DEALLOCATE PREPARE s2;

SET @c3 := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_admission' AND COLUMN_NAME = 'nursing_level_time');
SET @d3 := IF(@c3 = 0,
    CONCAT('ALTER TABLE biz_admission ADD COLUMN nursing_level_time datetime NULL ',
           'COMMENT ''护理等级评定时间（默认兜底时为写入时间）'' AFTER nursing_level_source'),
    'DO 0');
PREPARE s3 FROM @d3; EXECUTE s3; DEALLOCATE PREPARE s3;

-- -----------------------------------------------------------------------------
-- 2. 回填①：护理记录里评过等级的，按这次住院（admission_id）取最近一次
--
--    只认 r.admission_id = a.id 的同一次住院，不按 patient_id 跨住院串味。
--    同一天多条记录取 measure_time 最大的那条（MAX(id) 兜底顺序）。
-- -----------------------------------------------------------------------------
UPDATE biz_admission a
  JOIN (
        SELECT r.admission_id, r.nursing_level, r.measure_time
          FROM biz_nursing_record r
          JOIN (SELECT admission_id, MAX(measure_time) mt
                  FROM biz_nursing_record
                 WHERE del_flag = 0 AND nursing_level IS NOT NULL
                 GROUP BY admission_id) x
            ON x.admission_id = r.admission_id AND x.mt = r.measure_time
         WHERE r.del_flag = 0 AND r.nursing_level IS NOT NULL
       ) t ON t.admission_id = a.admission_id
   SET a.nursing_level = t.nursing_level,
       a.nursing_level_source = 2,
       a.nursing_level_time = t.measure_time
 WHERE a.del_flag = 0
   AND a.admit_status = 1
   AND (a.nursing_level IS NULL OR a.nursing_level_source = 1);

-- -----------------------------------------------------------------------------
-- 3. 回填②：剩下没评过的在院患者，兜底二级护理（source=1）
-- -----------------------------------------------------------------------------
UPDATE biz_admission
   SET nursing_level = 3,
       nursing_level_source = 1,
       nursing_level_time = NOW()
 WHERE del_flag = 0
   AND admit_status = 1
   AND nursing_level IS NULL;

-- -----------------------------------------------------------------------------
-- 4. 索引：需求测算每次都是「某天 × 在院 × 病区 × 等级」，这三列一起进索引
-- -----------------------------------------------------------------------------
SET @i1 := (SELECT COUNT(*) FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_admission' AND INDEX_NAME = 'idx_admit_ward_level');
SET @d4 := IF(@i1 = 0,
    'ALTER TABLE biz_admission ADD INDEX idx_admit_ward_level (admit_status, ward_id, nursing_level)',
    'DO 0');
PREPARE s4 FROM @d4; EXECUTE s4; DEALLOCATE PREPARE s4;

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs sql/211-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 nursing_level 列已建' AS item, COUNT(*) AS cnt, '1' AS expect FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_admission' AND COLUMN_NAME = 'nursing_level'
UNION ALL SELECT 'V2 nursing_level_source 列已建', COUNT(*), '1' FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_admission' AND COLUMN_NAME = 'nursing_level_source'
UNION ALL SELECT 'V3 nursing_level_time 列已建', COUNT(*), '1' FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_admission' AND COLUMN_NAME = 'nursing_level_time'
UNION ALL SELECT 'V4 在院患者还有没等级的（必须为 0）', COUNT(*), '0' FROM biz_admission WHERE del_flag = 0 AND admit_status = 1 AND nursing_level IS NULL
UNION ALL SELECT 'V5 等级取值越界（不在 1~4，必须为 0）', COUNT(*), '0' FROM biz_admission WHERE del_flag = 0 AND nursing_level IS NOT NULL AND nursing_level NOT IN (1,2,3,4)
UNION ALL SELECT 'V6 在院患者总数', COUNT(*), '51' FROM biz_admission WHERE del_flag = 0 AND admit_status = 1
-- V7 注意：实测为 0 —— 全库唯一被评过护理等级的那位患者（biz_nursing_record 里 3 条一级护理）
-- 已经出院了，所以「在院」口径下没有一条是评出来的。这个 0 不是脚本写错，正是 G-07 的现状：
-- 护理等级根本没人维护。source=2 的回填逻辑留在这里，等护士长真的开始评了就自动生效。
UNION ALL SELECT 'V7 护理记录带出的（source=2，当前为 0：没人在院被评过级）', COUNT(*), '*' FROM biz_admission WHERE del_flag = 0 AND admit_status = 1 AND nursing_level_source = 2
UNION ALL SELECT 'V8 默认兜底的（source=1）', COUNT(*), '51' FROM biz_admission WHERE del_flag = 0 AND admit_status = 1 AND nursing_level_source = 1
UNION ALL SELECT 'V9 idx_admit_ward_level 已建（复合索引按列计 3 行）', COUNT(*), '3' FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_admission' AND INDEX_NAME = 'idx_admit_ward_level'
UNION ALL SELECT 'V10 有在院患者的病区数（需求测算的下游口径）', COUNT(*), '>0' FROM (SELECT DISTINCT ward_id FROM biz_admission WHERE del_flag = 0 AND admit_status = 1 AND ward_id IS NOT NULL) x;
