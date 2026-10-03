-- =============================================================================
-- 208-值守排班回指出勤事实（修 G-24 悬空回指）
--
-- 【问题】P0 收口扫描时实测：`biz_duty_roster` 现存 27 行值守排班，
--   **`staff_schedule_id` 全部指向不存在的行**（89678… 号段，初始化时硬编的假 id）。
--   后果是值守这条线跟共享底座根本没咬上：
--     · 「全院岗位排班」里看不到总值班的人 —— 今天谁在总值班，只有值守页知道；
--     · 同一个人同一时刻被排到别处（比如同时在门诊出诊），底座的互斥判定查不到值守这一条；
--     · 未来 P3 的实际出勤、工时归集要以 staff_schedule_id 为锚，锚是空的，整段挂不上。
--
-- 【为什么不直接删掉这些值守行】
--   它们是真实的排班记录（2026-09-28 ~ 10-01 的总值班），删了等于把历史抹掉。
--   正确做法是给它们补上本该存在的那条出勤事实 —— 事实层缺什么补什么，不是业务层让步。
--
-- 【本脚本做两件事】
--   1. 对每条缺少事实的值守行，按「人 × 日期 × 班次」补一条 biz_staff_schedule：
--      · 排班单元取**值班点位**的 org_type/org_id（值守挂的是责任位所在的单元，不是值班人原科室）
--      · 岗位类别取点位要求的 required_staff_type；点位没要求时退到员工的岗位角色
--      · 出勤态=1 上班，响应形态取点位的 attend_mode（一线在岗 / 二三线听班）
--   2. 把新落的事实 id 回写进 biz_duty_roster.staff_schedule_id
--
-- 【刻意不做】
--   · 不动 dept_id 之外的任何值守快照列：那是当时的样子，改了历史就失真。
--   · 不改写已存在且有效的事实：只补缺失的那一层，不重排任何人的班。
--   · 不给已有合法回指的行重挂：幂等，重复执行不产生新行。
--
-- ⚠ 注：biz_staff_schedule 的唯一键是 uk_emp_date_shift(employee_id, schedule_date, shift_id)，
--      不含排班单元 —— 同一人同一天同一班次在系统里只能有一条事实，
--      所以「一个人在同一时段被排到两个单元」在建表层就写不出来。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 补缺失的出勤事实
-- -----------------------------------------------------------------------------
SET @rn := 896790000000000000;

INSERT INTO biz_staff_schedule (id, schedule_date, week_day, org_type, org_id, org_name,
                                dept_id, dept_name, employee_id, emp_code, employee_name,
                                staff_type, shift_id, start_time, end_time,
                                duty_status, attend_mode, clinic_flag, work_minutes,
                                schedule_source, create_time, update_time, del_flag, remark)
SELECT
  @rn := @rn + 1,
  d.duty_date,
  DAYOFWEEK(d.duty_date) - 1,                                   -- MySQL DAYOFWEEK 周日=1，转成周一=1
  p.org_type, p.org_id,
  CASE p.org_type WHEN 1 THEN (SELECT x.dept_name FROM sys_department x WHERE x.id = p.org_id)
                  WHEN 2 THEN (SELECT w.ward_name FROM sys_ward w WHERE w.ward_id = p.org_id)
                  ELSE '全院' END,
  CASE p.org_type WHEN 1 THEN p.org_id
                  WHEN 2 THEN COALESCE((SELECT w.dept_id FROM sys_ward w WHERE w.ward_id = p.org_id), 0)
                  ELSE 0 END,
  CASE p.org_type WHEN 1 THEN (SELECT x.dept_name FROM sys_department x WHERE x.id = p.org_id)
                  WHEN 2 THEN COALESCE((SELECT x.dept_name FROM sys_department x
                                         WHERE x.id = (SELECT w.dept_id FROM sys_ward w WHERE w.ward_id = p.org_id)), '')
                  ELSE '全院' END,
  d.employee_id, e.emp_code, e.emp_name,
  COALESCE(p.required_staff_type, pt.staff_type, 1),
  d.shift_id, d.start_time, d.end_time,
  1,                                                            -- 上班
  COALESCE(p.attend_mode, 1),
  0,
  COALESCE(h.duration_minutes, 0),
  1,                                                            -- 手工来源（历史数据无从区分）
  NOW(), NOW(), 0,
  'sql/208 由值守排班补建的出勤事实（原 staff_schedule_id 指向不存在的行）'
-- ⚠ 注：biz_staff_schedule 的唯一键是 uk_emp_date_shift(employee_id, schedule_date, shift_id)，
--      不含排班单元 —— 同一人同一天同一班次在系统里只能有一条事实。
--      所以这里**按这条键去重**后再补：存量里存在「同一人同一天同时接了总值班主班与副班」
--      这种排班本身有问题的数据（如 9302 在 2026-09-29），两条值守行只能共用一条出勤事实 ——
--      主班/副班是值守层的角色概念，事实层只表达「这个人这天这个班在岗」。
--      先在这里用 GROUP BY 收口，避免撞重复键把整个迁移挂掉；那条数据的角色冲突另案跟进。
FROM (SELECT MIN(d.id) AS rid
        FROM biz_duty_roster d
        JOIN biz_duty_post p ON p.id = d.post_id
        JOIN sys_employee e  ON e.id = d.employee_id
       WHERE d.del_flag = 0
         AND NOT EXISTS (SELECT 1 FROM biz_staff_schedule c
                          WHERE c.del_flag = 0 AND c.employee_id = d.employee_id
                            AND c.schedule_date = d.duty_date AND c.shift_id = d.shift_id)
       GROUP BY d.employee_id, d.duty_date, d.shift_id) k
JOIN biz_duty_roster d       ON d.id = k.rid
JOIN biz_duty_post p         ON p.id = d.post_id
JOIN sys_employee e          ON e.id = d.employee_id
LEFT JOIN biz_shift h        ON h.id = d.shift_id
LEFT JOIN (SELECT ep.employee_id, MIN(r.staff_type) AS staff_type
             FROM sys_employee_post ep JOIN sys_role r ON r.id = ep.role_id
            GROUP BY ep.employee_id) pt ON pt.employee_id = d.employee_id;

-- -----------------------------------------------------------------------------
-- 2. 回写 staff_schedule_id（悬空的那 27 行；已经挂对的不动）
--    优先用「早就存在的事实」，没有则用上一步刚补的。
-- -----------------------------------------------------------------------------
UPDATE biz_duty_roster d
  JOIN biz_staff_schedule c
    ON c.del_flag = 0 AND c.employee_id = d.employee_id
   AND c.schedule_date = d.duty_date AND c.shift_id = d.shift_id
   SET d.staff_schedule_id = c.id
 WHERE d.del_flag = 0
   AND (d.staff_schedule_id IS NULL
        OR d.staff_schedule_id = 0
        OR NOT EXISTS (SELECT 1 FROM biz_staff_schedule x WHERE x.id = d.staff_schedule_id));

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs sql/208-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 悬空回指的行数' AS item, COUNT(*) AS cnt, '0' AS expect FROM biz_duty_roster d WHERE d.del_flag = 0 AND NOT EXISTS (SELECT 1 FROM biz_staff_schedule c WHERE c.id = d.staff_schedule_id)
UNION ALL SELECT 'V2 值守排班已全部挂上底座', COUNT(*), '27' FROM biz_duty_roster d JOIN biz_staff_schedule c ON c.id = d.staff_schedule_id WHERE d.del_flag = 0
UNION ALL SELECT 'V3 挂上去的都是上班事实', COUNT(*), '0' FROM biz_duty_roster d JOIN biz_staff_schedule c ON c.id = d.staff_schedule_id WHERE d.del_flag = 0 AND c.duty_status <> 1
UNION ALL SELECT 'V4 事实落在点位所属单元', COUNT(*), '0' FROM biz_duty_roster d JOIN biz_staff_schedule c ON c.id = d.staff_schedule_id JOIN biz_duty_post p ON p.id = d.post_id WHERE d.del_flag = 0 AND (c.org_type <> p.org_type OR c.org_id <> p.org_id)
UNION ALL SELECT 'V5 日期班次对得上', COUNT(*), '0' FROM biz_duty_roster d JOIN biz_staff_schedule c ON c.id = d.staff_schedule_id WHERE d.del_flag = 0 AND (c.schedule_date <> d.duty_date OR c.shift_id <> d.shift_id)
UNION ALL SELECT 'V6 A 值守行数（留痕）', COUNT(*), '>=27' FROM biz_duty_roster WHERE del_flag = 0
UNION ALL SELECT 'V7 A 底座总行数（留痕，不应被重复拉高）', COUNT(*), '>=1874' FROM biz_staff_schedule WHERE del_flag = 0;
