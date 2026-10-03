-- =============================================================================
-- 205-护理排班回写底座行（修 G-09 单向影子 + G-10 底座错位）
--
-- 【背景】排班域施工蓝图 §4-P0。三条排班线里，门诊 biz_schedule 与值守 biz_duty_roster
--   都冗余了 staff_schedule_id 回指出勤底座 biz_staff_schedule，只有护理
--   biz_nurse_schedule **没有这一列** —— 护理写了一行事实到底座却无处回指，
--   于是「改一条找不到另一条」：改护理格子后想知道底座有没有跟着动，只能靠
--   (员工,日期,病区) 反查，而这个反查在多单元场景下会指错行（见下）。
--
-- 【G-10 错位是怎么发生的】
--   NurseScheduleServiceImpl.syncDayAttendance → replaceDayAttendance
--     = 先按 (org_type=2 病区, org_id=ward_id) 物理清旧行，再 ensureAttendance。
--   而 ensureAttendance 内部的查重键 sameShiftRow 是
--     **(employee_id, schedule_date, shift_id)** —— 不含排班单元。
--   所以当同一个护士同一天同一班次在别处（比如门诊科室 org_type=1）已有一条底座行时，
--   「清」清的是病区（没东西可清），「查」却查到门诊那条 → 直接复用它。
--   结果：护理表说她在呼吸内科病区上班，底座说她在全科医学科门诊。两条链各说各话。
--   （代码侧修复见 StaffScheduleServiceImpl.ensureForUnit；本脚本只管数据列与回填）
--
-- 【本脚本做三件事】
--   1. biz_nurse_schedule 加 staff_schedule_id 列（可空，历史行回填后仍允许空）
--   2. 存量回填：按 (员工, 日期, 病区, 班次) 反查底座那条事实并写回。
--      实测存量 1680 行**全部**能唯一命中（候选数为 0 条多值），口径可靠。
--   3. 建索引：回指列会被 JOIN 频繁用到。
--
-- 【刻意不做】
--   · 不加外键：护理格子与底座事实是**两条生命周期**，底座行可被全院排班直接删掉，
--     外键会让「全院排班删一条事实」连带删不掉护理格子、或反过来卡住。回指只是指路牌。
--   · 不设 NOT NULL：底座行的存在由业务层保证，DDL 卡死会让「底座行丢了」变成写不进去
--     而不是看得见的一组空值。（自检项 V2 盯的就是这个空值数）
--   · 不删任何存量：本脚本纯加列 + 回填，零删除。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 加列（幂等：已存在则跳过）
-- -----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                     WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'biz_nurse_schedule'
                       AND COLUMN_NAME = 'staff_schedule_id');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_nurse_schedule ADD COLUMN staff_schedule_id bigint NULL COMMENT ''关联的出勤事实 biz_staff_schedule.id（护理格子→底座的指路牌）'' AFTER del_flag',
    'DO 0');
PREPARE st_ddl FROM @ddl; EXECUTE st_ddl; DEALLOCATE PREPARE st_ddl;

-- -----------------------------------------------------------------------------
-- 2. 建索引（幂等：已存在则跳过）
-- -----------------------------------------------------------------------------
SET @idx_exists := (SELECT COUNT(*) FROM information_schema.STATISTICS
                     WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'biz_nurse_schedule'
                       AND INDEX_NAME = 'idx_staff_schedule');
SET @ddl2 := IF(@idx_exists = 0,
    'ALTER TABLE biz_nurse_schedule ADD INDEX idx_staff_schedule (staff_schedule_id)',
    'DO 0');
PREPARE st_ddl2 FROM @ddl2; EXECUTE st_ddl2; DEALLOCATE PREPARE st_ddl2;

-- -----------------------------------------------------------------------------
-- 3. 存量回填
--    匹配键：(员工, 日期, org_type=2 病区, org_id=ward_id)，班次在护理行非空的场合再收窄一层。
--    取 MIN(id) 兜底：语义上同一格只允许一条事实，多条说明历史脏，取最小那条保持幂等。
--    已回写的行不重复动（AND n.staff_schedule_id IS NULL）。
-- -----------------------------------------------------------------------------
UPDATE biz_nurse_schedule n
  JOIN (
    SELECT n2.id AS nurse_id,
           MIN(c.id) AS core_id
      FROM biz_nurse_schedule n2
      JOIN biz_staff_schedule c
        ON c.del_flag = 0
       AND c.employee_id   = n2.employee_id
       AND c.schedule_date = n2.schedule_date
       AND c.org_type      = 2
       AND c.org_id        = n2.ward_id
       AND (n2.shift_id IS NULL OR c.shift_id = n2.shift_id)
     WHERE n2.del_flag = 0
       AND n2.staff_schedule_id IS NULL
     GROUP BY n2.id
  ) x ON x.nurse_id = n.id
   SET n.staff_schedule_id = x.core_id;

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs sql/205-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 staff_schedule_id 列已建'  AS item, COUNT(*) AS cnt, '1'       AS expect FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_nurse_schedule' AND COLUMN_NAME = 'staff_schedule_id'
UNION ALL SELECT 'V2 存量护理行都已回写', COUNT(*), '0' FROM biz_nurse_schedule WHERE del_flag = 0 AND staff_schedule_id IS NULL
UNION ALL SELECT 'V3 回写的底座行真实存在', COUNT(*), '0' FROM biz_nurse_schedule n LEFT JOIN biz_staff_schedule c ON c.id = n.staff_schedule_id WHERE n.del_flag = 0 AND n.staff_schedule_id IS NOT NULL AND c.id IS NULL
UNION ALL SELECT 'V4 回写指向的是病区单元行', COUNT(*), '0' FROM biz_nurse_schedule n JOIN biz_staff_schedule c ON c.id = n.staff_schedule_id WHERE n.del_flag = 0 AND c.org_type <> 2
UNION ALL SELECT 'V5 回写指向的病区=护理格子病区', COUNT(*), '0' FROM biz_nurse_schedule n JOIN biz_staff_schedule c ON c.id = n.staff_schedule_id WHERE n.del_flag = 0 AND c.org_id <> n.ward_id
UNION ALL SELECT 'V6 出勤状态两边一致', COUNT(*), '0' FROM biz_nurse_schedule n JOIN biz_staff_schedule c ON c.id = n.staff_schedule_id WHERE n.del_flag = 0 AND c.duty_status <> n.schedule_status
UNION ALL SELECT 'V7 索引已建', COUNT(*), '1' FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_nurse_schedule' AND INDEX_NAME = 'idx_staff_schedule'
UNION ALL SELECT 'V8 A 指路牌指向的就是本格事实', COUNT(*), '1680' FROM biz_nurse_schedule n JOIN biz_staff_schedule c ON c.id = n.staff_schedule_id WHERE n.del_flag = 0 AND c.del_flag = 0 AND c.org_type = 2 AND c.org_id = n.ward_id AND c.employee_id = n.employee_id AND c.schedule_date = n.schedule_date
UNION ALL SELECT 'V9 A 已回写行数（留痕）', COUNT(*), '>=1680' FROM biz_nurse_schedule WHERE del_flag = 0 AND staff_schedule_id IS NOT NULL;
