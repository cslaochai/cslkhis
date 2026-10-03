-- =============================================================================
-- 204-人力配置标准铺开（补齐裸排单元与岗位类别）
--
-- 【问题】sql/200 只把「病区护理岗」的老规则迁进了 biz_staff_plan_rule（18 条，
-- 覆盖 3 个病区 × 护理）。实测 biz_staff_schedule 里 10 个「单元 × 岗位类别」组合
-- 中 **7 个没有标准**，涉及排班 194 行（10.3%）。
--   而 StaffPlanRuleServiceImpl.matchRule() 匹配不到规则时**直接 return null 跳过校验** ——
--   「没配标准」在系统里的真实含义是「这个单元没人管」，不是「这个单元不需要管」。
--   排班员可以随意把一个病区的护士全删光，系统一声不吭。
--
-- 【本脚本铺两类】
--   A. 已排过班的组合（有数据支撑）：min_staff 取该组合**历史每日在岗人数的最小值**，
--      max_staff 留 0（不限）。取最小值是为了**存量排班一条都不违规** —— 标准一铺就把
--      现有排班全判违规，等于逼着所有人立刻重排，那是添乱不是治理。
--      ⚠ 这是按存量反推的**基线**，不是业务核定值，remark 里写清楚让人复核。
--   B. 有住院医师值班点位的科室（39 个）：医生岗兜底 min=1。
--      一天至少得有一个医生在岗 —— 这是常识底线，不是拍脑袋。
--
-- 【刻意不做】
--   · 不给「没排过班也没点位」的单元铺：没有事实就没有基线，凭空填个数等于编数据。
--   · 不填 max_staff：上限只做提示不做拦截，且现有 18 条也全是 0，保持一致。
--   · max_week_hours / 连班上限只填在 shift_id=0（单元级）那条上 —— 与现有规则同口径，
--     分班次的行留空（工时是人的属性，按单元算，不按班次拆）。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- A. 已排过班的组合：按历史最小在岗铺下限
--    只铺 shift_id=0（单元级口径）：matchRule 的匹配顺序是先找班次级再回落单元级，
--    单元级的兜底确保"这个组合至少有人管"，班次级的细分交给业务后续配。
--    id：'89675' + LPAD(org_id,10,'0') + staff_type —— org_id 最长 19 位会截断，但实测
--    排班涉及的单元 id 都在 8~19 位之间；为绝对安全，19 位的（雪花病区 id）用下面的
--    CONCAT 直接拼会撞车，所以这里统一改用「行号递增」的稳妥做法见 B 段。
-- -----------------------------------------------------------------------------
SET @rn := (SELECT COALESCE(MAX(id), 896750000000000100) FROM biz_staff_plan_rule WHERE id < 896750000000090000);

INSERT INTO biz_staff_plan_rule (id, org_type, org_id, org_name, shift_id, staff_type,
                                 min_staff, max_staff, max_week_hours,
                                 max_consecutive_night_days, max_consecutive_work_days,
                                 status, create_time, update_time, del_flag, remark)
SELECT
  @rn := @rn + 1,
  x.org_type, x.org_id, x.org_name, 0, x.staff_type,
  x.min_on, 0, 48.0, 2, 6,
  1, NOW(), NOW(), 0,
  CONCAT('sql/204 按存量实测下限反推（历史每日在岗 ', x.min_on, '~', x.max_on, ' 人），需业务复核后定稿')
FROM (
  SELECT t.org_type, t.org_id,
         CASE t.org_type WHEN 1 THEN (SELECT d.dept_name FROM sys_department d WHERE d.id = t.org_id)
                         WHEN 2 THEN (SELECT w.ward_name FROM sys_ward w WHERE w.ward_id = t.org_id)
                         ELSE '全院' END org_name,
         t.staff_type, MIN(t.c) min_on, MAX(t.c) max_on
  FROM (SELECT org_type, org_id, staff_type, schedule_date, COUNT(*) c
        FROM biz_staff_schedule
        WHERE del_flag = 0 AND duty_status = 1
        GROUP BY org_type, org_id, staff_type, schedule_date) t
  GROUP BY t.org_type, t.org_id, t.staff_type
) x
WHERE NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule r
                   WHERE r.del_flag = 0 AND r.org_type = x.org_type AND r.org_id = x.org_id
                     AND r.staff_type = x.staff_type AND r.shift_id = 0);

-- -----------------------------------------------------------------------------
-- B. 有住院医师值班点位的科室：医生岗兜底 min=1
--    这些科室已经铺了一线/二线/三线共 6 个值班位（sql/203），一旦排班就会落在
--    「科室 + 医生」这个组合上。先给条底线，免得值班刚排上就处于无人监管状态。
-- -----------------------------------------------------------------------------
SET @rn2 := (SELECT COALESCE(MAX(id), 896750000000050000) FROM biz_staff_plan_rule WHERE id < 896750000000090000);

INSERT INTO biz_staff_plan_rule (id, org_type, org_id, org_name, shift_id, staff_type,
                                 min_staff, max_staff, max_week_hours,
                                 max_consecutive_night_days, max_consecutive_work_days,
                                 status, create_time, update_time, del_flag, remark)
SELECT
  @rn2 := @rn2 + 1,
  1, d.id, d.dept_name, 0, 1,
  1, 0, 48.0, 2, 6,
  1, NOW(), NOW(), 0,
  'sql/204 临床科室医生岗底线：一天至少 1 名医生在岗（配合 sql/203 住院医师值班点位）'
FROM sys_department d
WHERE d.del_flag = 0 AND d.status = 1
  AND EXISTS (SELECT 1 FROM biz_duty_post p
               WHERE p.duty_scope = 6 AND p.del_flag = 0 AND p.status = 1 AND p.org_id = d.id)
  AND NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule r
                   WHERE r.del_flag = 0 AND r.org_type = 1 AND r.org_id = d.id
                     AND r.staff_type = 1 AND r.shift_id = 0);

-- -----------------------------------------------------------------------------
-- 3. 自检（跑完必须全绿）
--    ⚠ 自检段里不要写 SET @var：verify-sql.mjs 拿 marker 之后的整段去 query，
--      多一条 SET 就多返回一个 OkPacket，rowsOf() 下钻时把它当数据行，整段判废。
-- -----------------------------------------------------------------------------
SELECT 'X1_已排班组合无标准数' item, COUNT(*) cnt, 0 expect
FROM (
  SELECT t.org_type, t.org_id, t.staff_type
  FROM biz_staff_schedule t
  WHERE t.del_flag = 0 AND t.duty_status = 1
    AND (t.remark IS NULL OR t.remark NOT LIKE 'VERIFY%')
  GROUP BY t.org_type, t.org_id, t.staff_type
) x
WHERE NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule r
                   WHERE r.del_flag = 0 AND r.status = 1 AND r.org_type = x.org_type
                     AND r.org_id = x.org_id AND r.staff_type = x.staff_type)

UNION ALL SELECT 'X2_标准总数', COUNT(*), '>=25' FROM biz_staff_plan_rule WHERE del_flag = 0

-- 只校验本次新铺的规则：存量 18 条里有 1 条（呼吸内科病区 min=5）**本身就高于存量实测**
-- （该病区 70 天数据里最低一天只有 4 名护士在岗）。那是 sql/200 迁进来的老账，
-- 本脚本不替业务改标准值、也不替他补排班，只在 X9 单独留痕报出来。
UNION ALL SELECT 'X3_新铺下限不高于存量实测', COUNT(*), 0
FROM biz_staff_plan_rule r
WHERE r.del_flag = 0 AND r.status = 1 AND r.shift_id = 0 AND r.min_staff > 0
  AND r.remark LIKE 'sql/204%'
  AND r.min_staff > COALESCE((
    SELECT MIN(t.c) FROM (SELECT org_id, staff_type, schedule_date, COUNT(*) c
                          FROM biz_staff_schedule
                          WHERE del_flag = 0 AND duty_status = 1
                            AND (remark IS NULL OR remark NOT LIKE 'VERIFY%')
                          GROUP BY org_id, staff_type, schedule_date) t
    WHERE t.org_id = r.org_id AND t.staff_type = r.staff_type), r.min_staff)

UNION ALL SELECT 'X4_规则单元名非空', COUNT(*), 0
FROM biz_staff_plan_rule WHERE del_flag = 0 AND (org_name IS NULL OR org_name = '')

UNION ALL SELECT 'X5_值班科室都有医生岗标准', COUNT(*), 0
FROM (SELECT DISTINCT p.org_id FROM biz_duty_post p
       WHERE p.duty_scope = 6 AND p.del_flag = 0 AND p.status = 1) d
WHERE NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule r
                   WHERE r.del_flag = 0 AND r.status = 1 AND r.org_type = 1
                     AND r.org_id = d.org_id AND r.staff_type = 1 AND r.shift_id = 0)

UNION ALL SELECT 'X6_规则键不重复', COUNT(*), 0
FROM (SELECT org_type, org_id, staff_type, shift_id FROM biz_staff_plan_rule
      WHERE del_flag = 0 GROUP BY 1,2,3,4 HAVING COUNT(*) > 1) y

UNION ALL SELECT 'X7_存量18条未受影响', COUNT(*), 18
FROM biz_staff_plan_rule
WHERE del_flag = 0 AND (remark IS NULL OR remark NOT LIKE 'sql/204%')

UNION ALL SELECT 'X9_存量规则高于实测(留痕)', COUNT(*), '*'
FROM biz_staff_plan_rule r
WHERE r.del_flag = 0 AND r.status = 1 AND r.shift_id = 0 AND r.min_staff > 0
  AND (r.remark IS NULL OR r.remark NOT LIKE 'sql/204%')
  AND r.min_staff > COALESCE((SELECT MIN(t.c) FROM (SELECT org_id, staff_type, schedule_date, COUNT(*) c
                              FROM biz_staff_schedule WHERE del_flag = 0 AND duty_status = 1
                                AND (remark IS NULL OR remark NOT LIKE 'VERIFY%')
                              GROUP BY org_id, staff_type, schedule_date) t
                              WHERE t.org_id = r.org_id AND t.staff_type = r.staff_type), r.min_staff)

-- 最后一项刻意写成不带子查询的简单查询：verify-sql.mjs 切自检段时会把结尾的 `);` 一并剥掉，
-- 若最后一项以 `)` 收尾（子查询/NOT EXISTS 的闭括号会被当成语句结尾吃掉），剥完就语法残缺。
UNION ALL SELECT 'X8_启用中的标准', COUNT(*), '>0'
FROM biz_staff_plan_rule WHERE del_flag = 0 AND status = 1;
