-- =====================================================================
-- 262-门诊排班变更留痕表补快照列.sql
-- 背景：261 建的 biz_clinic_source_log 只存 schedule_id，删除类留痕的排班已物理删，
--       前端联排班表取不到 医生/科室/日期，删除审计显示成 null（谁删了什么不可读）。
--       审计表必须冗余快照列（排班后续改名/改科也不影响历史留痕的可读性）。
-- 幂等：information_schema 判列存在，重跑安全。
-- 回查：SHOW COLUMNS FROM biz_clinic_source_log 应见 schedule_date 等三列
-- =====================================================================

-- 1) 加冗余快照列（幂等）
SET @has_date = (SELECT COUNT(*) FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA='hn_biz_his' AND TABLE_NAME='biz_clinic_source_log' AND COLUMN_NAME='schedule_date');
SET @sql1 = IF(@has_date = 0, 'ALTER TABLE biz_clinic_source_log ADD COLUMN schedule_date DATE NULL COMMENT ''排班日期快照''', 'SELECT 1');
PREPARE st1 FROM @sql1; EXECUTE st1; DEALLOCATE PREPARE st1;

SET @has_dept = (SELECT COUNT(*) FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA='hn_biz_his' AND TABLE_NAME='biz_clinic_source_log' AND COLUMN_NAME='dept_name');
SET @sql2 = IF(@has_dept = 0, 'ALTER TABLE biz_clinic_source_log ADD COLUMN dept_name VARCHAR(64) NULL COMMENT ''科室名称快照''', 'SELECT 1');
PREPARE st2 FROM @sql2; EXECUTE st2; DEALLOCATE PREPARE st2;

SET @has_doc = (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA='hn_biz_his' AND TABLE_NAME='biz_clinic_source_log' AND COLUMN_NAME='doctor_name');
SET @sql3 = IF(@has_doc = 0, 'ALTER TABLE biz_clinic_source_log ADD COLUMN doctor_name VARCHAR(64) NULL COMMENT ''排班人员姓名快照''', 'SELECT 1');
PREPARE st3 FROM @sql3; EXECUTE st3; DEALLOCATE PREPARE st3;

-- 2) 存量回填：还能联到排班的补上（删除类留痕的排班已不在，保持 null，由前端容错显示 reason）
UPDATE biz_clinic_source_log l
JOIN biz_clinic_source s ON s.id = l.schedule_id AND s.del_flag = 0
SET l.schedule_date = s.schedule_date, l.dept_name = s.dept_name, l.doctor_name = s.doctor_name
WHERE l.schedule_date IS NULL;

-- 3) 回查
SELECT id, schedule_id, action_type, amount, reason, schedule_date, dept_name, doctor_name, occur_time
FROM biz_clinic_source_log ORDER BY id DESC LIMIT 10;
