-- =============================================================================
-- 209-护理排班单元扩到门诊（G-11）
--
-- 【问题】护理排班只能排病区：iyoose 下拉里全是病区（49 个），门诊科室一个都没有。
--   实测：出诊「科室 × 日期」组合 54 个，**没有一个在同科室同日有护士在岗** ——
--   门诊护理（分诊、跟诊、治疗处置）在系统里没有落脚的地方，
--   想给门诊配护士只能绕道「全院岗位排班」排一条裸出勤（没有班次搭配、没有层级、没有工时统计）。
--
-- 【国外的 ir】：不是。"护理排班"被建模成了"**病区**护理排班"——排班单元只有 ward_id 一种取值，
--   这才是它跟门诊脱节的根因。单元是主线概念，不能只有一种。
--
-- 【本脚本做二件事】
--   1. biz_nurse_schedule 加 unit_type / unit_id：1-病区（取 sys_ward.ward_id）、2-门诊科室（取 sys_department.id）
--   2. 存量回填为 unit_type=1 / unit_id=ward_id（ward_id 列保留兼容，病区场景继续写）
--
-- 【为什么不加第三个挊モデル（sys_nursing_unit 主数据表）】
--   标准 HIS 会单独建「护理单元」主数据（类型=病区/门诊/急诊/手术室），那是 P4 的做法：
--   它要铺主数据、要改所有引用，代价大。这里先用「类型 + id」二元组把**口子打开**，
--   让业务先生长出门诊护理的排法，等真的需要"护理单元"这个概念时再收口到主数据表。
--
-- 【刻意不做】
--   · 不改 uk_nurse_date(employee_id, schedule_date)：**一个护士一天仍然只有一格**。
--     她同一天要么在病区、要么在门诊，不存在"上午门诊下午病区"两条格子 ——
--     护士的编制归属是单一的（调用mentions 里 is also 限制了跨病区，要先调档案），
--     排两格只会让"这天她到底在哪"变成两个答案。
--   · 不删 ward_id：存量 1680 行和当前前端都在用它，二元组上位之后才谈收口。
--   · 不铺门诊护理的人力标准：那是 sql/210（G-13）的事，本脚本只管排班单元的形态。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 加列（幂等）
-- -----------------------------------------------------------------------------
SET @c1 := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_nurse_schedule' AND COLUMN_NAME = 'unit_type');
SET @d1 := IF(@c1 = 0,
    CONCAT('ALTER TABLE biz_nurse_schedule ADD COLUMN unit_type tinyint NOT NULL DEFAULT 1 ',
           'COMMENT ''排班单元类型（1-病区 2-门诊科室）'' AFTER dept_name'),
    'DO 0');
PREPARE s1 FROM @d1; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @c2 := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_nurse_schedule' AND COLUMN_NAME = 'unit_id');
SET @d2 := IF(@c2 = 0,
    CONCAT('ALTER TABLE biz_nurse_schedule ADD COLUMN unit_id bigint NULL ',
           'COMMENT ''排班单元ID（unit_type=1 取 sys_ward.ward_id，=2 取 sys_department.id）'' AFTER unit_type'),
    'DO 0');
PREPARE s2 FROM @d2; EXECUTE s2; DEALLOCATE PREPARE s2;

-- -----------------------------------------------------------------------------
-- 2. 存量回填：全部是病区场景
-- -----------------------------------------------------------------------------
UPDATE biz_nurse_schedule
   SET unit_type = 1,
       unit_id = ward_id
 WHERE del_flag = 0 AND (unit_id IS NULL OR unit_id = 0 OR unit_type IS NULL OR unit_type = 0);

-- -----------------------------------------------------------------------------
-- 3. 索引：矩阵的每次渲染都是「某单元 × 某月」，这两列必须进索引
-- -----------------------------------------------------------------------------
SET @i1 := (SELECT COUNT(*) FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_nurse_schedule' AND INDEX_NAME = 'idx_unit_date');
SET @d3 := IF(@i1 = 0,
    'ALTER TABLE biz_nurse_schedule ADD INDEX idx_unit_date (unit_type, unit_id, schedule_date)',
    'DO 0');
PREPARE s3 FROM @d3; EXECUTE s3; DEALLOCATE PREPARE s3;

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs sql/209-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 unit_type 列已建' AS item, COUNT(*) AS cnt, '1' AS expect FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_nurse_schedule' AND COLUMN_NAME = 'unit_type'
UNION ALL SELECT 'V2 unit_id 列已建', COUNT(*), '1' FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_nurse_schedule' AND COLUMN_NAME = 'unit_id'
UNION ALL SELECT 'V3 存量护理行已回填单元', COUNT(*), '0' FROM biz_nurse_schedule WHERE del_flag = 0 AND (unit_type IS NULL OR unit_type = 0 OR unit_id IS NULL OR unit_id = 0)
UNION ALL SELECT 'V4 病区行的 unit_id 与 ward_id 一致', COUNT(*), '0' FROM biz_nurse_schedule WHERE del_flag = 0 AND unit_type = 1 AND unit_id <> ward_id
UNION ALL SELECT 'V5 idx_unit_date 已建（复合索引按列计 3 行）', COUNT(*), '3' FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_nurse_schedule' AND INDEX_NAME = 'idx_unit_date'
UNION ALL SELECT 'V6 A 存量护理行数', COUNT(*), '1680' FROM biz_nurse_schedule WHERE del_flag = 0
UNION ALL SELECT 'V7 A 有护士编制的门诊科室数（G-13 要铺标准的范围）', COUNT(*), '>0' FROM (SELECT DISTINCT e.dept_id FROM sys_employee e JOIN sys_employee_post p ON p.employee_id = e.id JOIN sys_role r ON r.id = p.role_id WHERE e.del_flag = 0 AND e.status = 1 AND r.staff_type = 2 AND e.dept_id NOT IN (SELECT w.dept_id FROM sys_ward w WHERE w.status = 1)) x;
