-- =============================================================================
-- 214 · 实际出勤层（P3-G17/G18/G19）
--
-- 【闭环到了第 3 步，却没有任何落点】
-- 排班四层是：编制标准 → 需求测算 → 排班落地 → **执行回填**。
-- P0~P2 把前两层半做完了：标准能配、需求能算、排班能排、还能和需求对出缺口。
-- 但「排的那个人到底来了没有、来了多久」全库**一张表都没有** —— 实测：
--   - 全库没有任何考勤/打卡表（只有 biz_attending_relation 住院管床、
--     biz_infection_monitor_daily 院感打卡，都不是人的出勤）；
--   - biz_staff_schedule 里 duty_status=1（应上班）的行有 1472 条，
--     其中过去 7 天 211 条 —— 这 211 条执行得怎么样，系统一个字都说不出来。
-- 没有实际，缺口就永远停在「计划层面的缺口」：页面上写"缺 2 人"，
-- 但真实原因是今天派的人没来、还是编制本身就不够，分不清。闭环转不起来。
--
-- 【这一刀做什么】
--   1. biz_staff_attendance：实际出勤事实（签到/签退/实际工时/替了谁的班）
--   2. biz_shift.late_grace_minutes：迟到宽限（判定要可配，不能是魔法数字）
--   3. v_staff_worktime：行级对照（一条计划 ↔ 一条实际，给出差异类型）
--   4. v_staff_worktime_summary：单元×日 汇总（计划人数/实到/缺勤/工时差）
--   5. v_staff_calibration_advice：G-19 校准建议（把执行结果喂回第 ① 层标准）
--
-- 【一条必须守住的界限：系统不许自己判"缺勤"】
-- 没有打卡数据时，「排了班、没有出勤记录」在系统里只能叫 **未回填**，
-- 不能叫缺勤 —— 人可能调班了、可能去支援别的单元了、可能打卡机坏了。
-- 缺勤（attendance_status=4）永远只能由科室/护士长**显式确认**产生，
-- 或者由考勤机导入明确给出。系统在没有任何证据时宁可承认"我不知道"。
-- 这条线一破，工时/绩效的数据就全是脏的，而且没人会发现。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 班次加「迟到宽限」：判定参数必须可配，写死 15 分钟在 SQL 和 Java 两边各一份
--    迟早会对不上。放班次字典里，SQL 视图和 Java 判定读同一个数。
-- -----------------------------------------------------------------------------
--    MySQL 没有 ADD COLUMN IF NOT EXISTS（那是 MariaDB），沿用 sql/207 的幂等写法。
SET @c0 := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_shift' AND COLUMN_NAME = 'late_grace_minutes');
SET @d0 := IF(@c0 = 0,
    CONCAT('ALTER TABLE biz_shift ADD COLUMN late_grace_minutes int NOT NULL DEFAULT 15 ',
           'COMMENT ''迟到宽限（分钟）：签到晚于班次开始超过这个数才算迟到'' AFTER need_rest_hours'),
    'DO 0');
PREPARE s0 FROM @d0; EXECUTE s0; DEALLOCATE PREPARE s0;

UPDATE biz_shift
   SET late_grace_minutes = 15
 WHERE del_flag = 0 AND IFNULL(late_grace_minutes, 0) = 0;

-- -----------------------------------------------------------------------------
-- 2. 实际出勤事实表
--
--    关于唯一键为什么不跟"人×日"：一个人同一天可以在**两个单元**出勤
--    （上午本科室 + 下午去别的病区支援），也可以在同一个单元上两个班次
--    （主班 + 加班）。业务上这三个都是不同的事实，不能被互相吃掉。
--    所以收口到「人 × 日 × 单元 × 班次」，与底座 biz_staff_schedule 的
--    uk_emp_date_shift 同一族口径。
--
--    staff_schedule_id 沿用已有的约定：**指路牌，不是外键** —— 三个业务线
--    （门诊/护理/值守）共享底座事实，但外键会让其中一条线的删改牵连别人。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_staff_attendance (
  id                 bigint       NOT NULL COMMENT '主键（雪花）',
  staff_schedule_id  bigint       NULL     COMMENT '关联的排班事实ID（biz_staff_schedule.id）；空=无计划的出勤（加班/支援/替班）',
  employee_id        bigint       NOT NULL COMMENT '员工ID',
  employee_name      varchar(50)  NULL     COMMENT '姓名',
  emp_code           varchar(32)  NULL     COMMENT '工号',
  schedule_date      date         NOT NULL COMMENT '出勤日期（归属哪一天；夜班签退跨到次日也算这天）',
  org_type           tinyint      NOT NULL COMMENT '实际出勤单元类型（1-科室 2-病区 3-全院）',
  org_id             bigint       NOT NULL DEFAULT 0 COMMENT '实际出勤单元ID（全院级为0）',
  org_name           varchar(128) NULL     COMMENT '单元名称',
  shift_id           bigint       NOT NULL DEFAULT 0 COMMENT '班次ID（0-无班次，如自由工时的加班）',
  staff_type         tinyint      NULL     COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政）',
  check_in           datetime     NULL     COMMENT '签到时间（NULL=没签到：缺勤确认或手工登记的工时）',
  check_out          datetime     NULL     COMMENT '签退时间（NULL=还没签退或缺勤）',
  actual_minutes     int          NULL     COMMENT '实际工时（分钟）：打卡则算，无打卡由科室确认后手工填',
  planned_minutes    int          NOT NULL DEFAULT 0 COMMENT '计划工时（分钟）：biz_staff_schedule.work_minutes 的快照',
  overtime_minutes   int          NOT NULL DEFAULT 0 COMMENT '超时工时（分钟）：GREATEST(0, 实际-计划)',
  attendance_status  tinyint      NOT NULL DEFAULT 1 COMMENT '出勤状态（1-正常 2-迟到 3-早退 4-缺勤 5-替班 6-加班 7-支援）',
  substitute_for     bigint       NULL     COMMENT '替了谁的班（employee_id）',
  confirm_status     tinyint      NOT NULL DEFAULT 0 COMMENT '科室确认（0-待确认 1-已确认 2-有异议）',
  confirm_by         varchar(64)  NULL     COMMENT '确认人',
  confirm_time       datetime     NULL     COMMENT '确认时间',
  data_source        tinyint      NOT NULL DEFAULT 1 COMMENT '数据来源（1-人工登记 2-考勤机导入 3-系统判定）',
  status             tinyint      NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-生效）',
  create_by          varchar(64)  NULL     COMMENT '创建人',
  create_time        datetime     NULL     COMMENT '创建时间',
  update_by          varchar(64)  NULL     COMMENT '更新人',
  update_time        datetime     NULL     COMMENT '更新时间',
  del_flag           tinyint      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  remark             varchar(500) NULL     COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_attend (employee_id, schedule_date, org_type, org_id, shift_id),
  KEY idx_unit_date (org_type, org_id, schedule_date),
  KEY idx_date (schedule_date),
  KEY idx_ssid (staff_schedule_id)
) COMMENT='实际出勤（闭环第3步：计划 vs 实际的对照落点）';

-- -----------------------------------------------------------------------------
-- 3. v_staff_worktime：行级对照（一行 = 一条应出勤的计划，或一条无计划的出勤）
--
--    差异类型 diff_type：
--      1-正常        有排已到，工时差在 ±15 分钟内
--      2-迟到        签到晚于（班次开始 + 宽限）
--      3-早退        签退早于（班次结束 - 0 宽限）
--      4-工时不足    到了但工时明显少于计划（非迟到早退能解释的，如中途离岗）
--      5-超时/加班   实际工时明显多于计划（同一条计划上的延长）
--      6-缺勤        科室已确认的缺勤（系统不会自己判，见文件头）
--      7-未回填      排了班、日子已到（含今天）、没有任何出勤记录 —— **不是缺勤，是没数**
--      8-无计划出勤  没排班却来上班了（加班/支援/替班）
--      9-待出勤      日期还没到，谈有没有出勤没有意义
--
--    为什么 7 和 6 必须分开：这两件事的管理动作完全不同。
--    缺勤要扣绩效，未回填要催科室登记 —— 混在一起会让护士长在名单里
--    看到一堆"缺勤"，逐个查完发现一半只是没打卡，从此再也不看这个表。
--
--    为什么单独要有 9 这一档：没有它，未来的日子既不满足「a.id IS NULL 且日子已过」，
--    就会掉进最后 ELSE 的「正常」—— **后天的人会显示成已实到**（UI 实测撞过：
--    10-03 / 10-04 显示「实到 6/6」）。这不是显示层的毛病：它会顺着 present_head
--    流进工时汇总与达成率，一个还没到来的日子能把本周达成率抬到 100%。
--    今天归 7 不归 9 —— 排了班今天就该来，今天没记录就是该催的，不是"还没到"。
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_staff_worktime AS
SELECT
  CAST(s.id AS CHAR)                       AS plan_id,
  CAST(a.id AS CHAR)                       AS attend_id,
  s.employee_id                            AS employee_id,
  s.employee_name                          AS employee_name,
  s.schedule_date                          AS work_date,
  s.org_type                               AS org_type,
  s.org_id                                 AS org_id,
  s.org_name                               AS org_name,
  s.staff_type                             AS staff_type,
  s.shift_id                               AS shift_id,
  COALESCE(sh.shift_name, '未排班次')       AS shift_name,
  s.duty_status                            AS duty_status,
  s.work_minutes                           AS planned_minutes,
  a.check_in                               AS check_in,
  a.check_out                              AS check_out,
  a.actual_minutes                         AS actual_minutes,
  a.attendance_status                      AS attendance_status,
  a.confirm_status                         AS confirm_status,
  CASE
    WHEN a.id IS NULL AND s.schedule_date > CURDATE()          THEN 9   -- 日期还没到：谈出勤没有意义
    WHEN a.id IS NULL                                          THEN 7   -- 有排未到：未回填（今天也算，排了班今天就该来）
    WHEN a.attendance_status = 4                               THEN 6   -- 已确认缺勤
    WHEN a.attendance_status IN (5, 6, 7)                      THEN 8   -- 替班/加班/支援（无计划的出勤）
    WHEN a.check_in IS NOT NULL AND sh.id IS NOT NULL
         AND a.check_in > DATE_ADD(CONCAT(s.schedule_date, ' ', sh.start_time),
                                   INTERVAL sh.late_grace_minutes MINUTE)
                                                               THEN 2   -- 迟到
    WHEN a.check_out IS NOT NULL AND sh.id IS NOT NULL
         AND a.check_out < CASE WHEN sh.cross_day = 1
                                THEN DATE_ADD(CONCAT(s.schedule_date, ' ', sh.end_time), INTERVAL 1 DAY)
                                ELSE CONCAT(s.schedule_date, ' ', sh.end_time) END
                                                               THEN 3   -- 早退
    WHEN COALESCE(a.actual_minutes, 0) < s.work_minutes - 15   THEN 4   -- 工时不足
    WHEN COALESCE(a.actual_minutes, 0) > s.work_minutes + 15   THEN 5   -- 超时/加班
    ELSE 1
  END                                      AS diff_type,
  CASE
    WHEN a.id IS NULL AND s.schedule_date > CURDATE()          THEN '待出勤（日期未到）'
    WHEN a.id IS NULL                                          THEN '未回填（有排班无出勤记录，需科室登记）'
    WHEN a.attendance_status = 4                               THEN '缺勤（科室已确认）'
    WHEN a.attendance_status = 5                               THEN '替班'
    WHEN a.attendance_status = 6                               THEN '加班'
    WHEN a.attendance_status = 7                               THEN '支援（跨单元出勤）'
    WHEN a.check_in IS NOT NULL AND sh.id IS NOT NULL
         AND a.check_in > DATE_ADD(CONCAT(s.schedule_date, ' ', sh.start_time),
                                   INTERVAL sh.late_grace_minutes MINUTE)
                                                               THEN '迟到'
    WHEN a.check_out IS NOT NULL AND sh.id IS NOT NULL
         AND a.check_out < CASE WHEN sh.cross_day = 1
                                THEN DATE_ADD(CONCAT(s.schedule_date, ' ', sh.end_time), INTERVAL 1 DAY)
                                ELSE CONCAT(s.schedule_date, ' ', sh.end_time) END
                                                               THEN '早退'
    WHEN COALESCE(a.actual_minutes, 0) < s.work_minutes - 15   THEN '工时不足'
    WHEN COALESCE(a.actual_minutes, 0) > s.work_minutes + 15   THEN '超时/加班'
    ELSE '正常'
  END                                      AS diff_reason,
  COALESCE(a.actual_minutes, 0) - s.work_minutes AS diff_minutes
FROM biz_staff_schedule s
LEFT JOIN biz_staff_attendance a
       ON a.del_flag = 0 AND a.staff_schedule_id = s.id
LEFT JOIN biz_shift sh ON sh.id = s.shift_id AND sh.del_flag = 0
WHERE s.del_flag = 0
  AND s.duty_status = 1            -- 只有"上班"的计划才需要对照；休息/请假/培训本来就不用来

UNION ALL

-- 无计划却来上班的（加班/支援/替班）：这类出勤在 biz_staff_schedule 里没有对应行，
-- 上面那段 LEFT JOIN 永远照不出来，必须单独给一头，否则这部分人力是隐形的。
SELECT
  NULL                                     AS plan_id,
  CAST(a.id AS CHAR)                       AS attend_id,
  a.employee_id                            AS employee_id,
  a.employee_name                          AS employee_name,
  a.schedule_date                          AS work_date,
  a.org_type                               AS org_type,
  a.org_id                                 AS org_id,
  a.org_name                               AS org_name,
  a.staff_type                             AS staff_type,
  a.shift_id                               AS shift_id,
  COALESCE(sh.shift_name, '未排班次')       AS shift_name,
  NULL                                     AS duty_status,
  0                                        AS planned_minutes,
  a.check_in                               AS check_in,
  a.check_out                              AS check_out,
  a.actual_minutes                         AS actual_minutes,
  a.attendance_status                      AS attendance_status,
  a.confirm_status                         AS confirm_status,
  8                                        AS diff_type,
  CASE a.attendance_status
    WHEN 5 THEN '替班'
    WHEN 6 THEN '加班（无排班）'
    WHEN 7 THEN '支援（跨单元出勤）'
    ELSE '无计划的出勤'
  END                                      AS diff_reason,
  COALESCE(a.actual_minutes, 0)            AS diff_minutes
FROM biz_staff_attendance a
LEFT JOIN biz_shift sh ON sh.id = a.shift_id AND sh.del_flag = 0
WHERE a.del_flag = 0
  AND a.schedule_date < CURDATE() + INTERVAL 1 DAY
  AND NOT EXISTS (SELECT 1 FROM biz_staff_schedule s
                   WHERE s.del_flag = 0 AND s.id = a.staff_schedule_id);

-- -----------------------------------------------------------------------------
-- 4. v_staff_worktime_summary：单元 × 日 汇总（给排班员看的一屏）
--    三个"人数"口径必须分开写清楚，否则一定被误读：
--      plan_head      多少人排了上班
--      present_head   实际到岗（不含缺勤确认与未回填）
--      unrecorded_head 排了、日子也过了、但一条出勤记录都没有 —— **该催的清单**
--      absent_head    科室已确认缺勤
--      extra_head     没排班却来上班的
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_staff_worktime_summary AS
SELECT
  w.work_date                                        AS work_date,
  w.org_type                                         AS org_type,
  w.org_id                                           AS org_id,
  MAX(w.org_name)                                    AS org_name,
  w.staff_type                                       AS staff_type,
  COUNT(DISTINCT CASE WHEN w.plan_id IS NOT NULL THEN w.employee_id END) AS plan_head,
  COUNT(DISTINCT CASE WHEN w.diff_type IN (1,2,3,4,5) THEN w.employee_id END) AS present_head,
  COUNT(DISTINCT CASE WHEN w.diff_type = 6 THEN w.employee_id END)           AS absent_head,
  COUNT(DISTINCT CASE WHEN w.diff_type = 7 THEN w.employee_id END)           AS unrecorded_head,
  COUNT(DISTINCT CASE WHEN w.diff_type = 8 THEN w.employee_id END)           AS extra_head,
  SUM(w.planned_minutes)                             AS planned_minutes,
  SUM(COALESCE(w.actual_minutes, 0))                 AS actual_minutes,
  SUM(COALESCE(w.actual_minutes, 0)) - SUM(w.planned_minutes) AS diff_minutes,
  CASE WHEN SUM(w.planned_minutes) = 0 THEN NULL
       ELSE ROUND(100.0 * SUM(COALESCE(w.actual_minutes, 0)) / SUM(w.planned_minutes), 1)
  END                                                AS fulfill_rate
FROM v_staff_worktime w
GROUP BY w.work_date, w.org_type, w.org_id, w.staff_type;

-- -----------------------------------------------------------------------------
-- 5. v_staff_calibration_advice：G-19 差异归因 → 标准校准建议（闭环的第 4 步）
--
--    闭环能转起来的标志不是"出了张报表"，是**执行结果能改下一轮的编制标准**：
--      连着两个人加班顶班 → 说明编制不够 → 建议上调 min_staff；
--      长期实际到岗远低于编制、又没出事 → 说明编制虚高 → 建议核减。
--    这里只给建议，不自动改 biz_staff_plan_rule —— 编制是护理部的权，
--    系统给数、人做决定，这条边界不能越。
--
--    最重要的一档是 **数据不足**：过去 14 天里"未回填"太多（>30%）时，
--    任何"上调/核减"的结论都是拿半个事实糊弄人 —— 直接说不知道，
--    并把"请先补 N 条出勤记录"写进建议里。当前全库 0 条出勤，所以现在跑出来
--    必然是这一档，这是诚实的。
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_staff_calibration_advice AS
SELECT
  s.org_type                                          AS org_type,
  s.org_id                                            AS org_id,
  MAX(s.org_name)                                     AS org_name,
  s.staff_type                                        AS staff_type,
  COUNT(*)                                            AS days_covered,
  SUM(s.plan_head)                                    AS plan_head_days,
  SUM(s.present_head)                                 AS present_head_days,
  SUM(s.unrecorded_head)                              AS unrecorded_head_days,
  SUM(s.absent_head)                                  AS absent_head_days,
  SUM(s.extra_head)                                   AS extra_head_days,
  SUM(s.planned_minutes)                              AS planned_minutes,
  SUM(s.actual_minutes)                               AS actual_minutes,
  SUM(s.diff_minutes)                                 AS diff_minutes,
  ROUND(AVG(g.required_count), 1)                     AS avg_required,
  ROUND(AVG(s.present_head), 1)                       AS avg_present,
  CASE WHEN SUM(s.plan_head) = 0 THEN 0
       ELSE ROUND(100.0 * SUM(s.unrecorded_head) / SUM(s.plan_head), 1)
  END                                                 AS unrecorded_rate,
  CASE
    WHEN SUM(s.plan_head) = 0                                   THEN 4   -- 数据不足：这段时间压根没排班
    WHEN SUM(s.unrecorded_head) > 0.3 * SUM(s.plan_head)        THEN 4   -- 数据不足：出勤登记不全
    WHEN ROUND(AVG(g.required_count), 1) > ROUND(AVG(s.present_head), 1)
         AND SUM(s.diff_minutes) > 0                            THEN 1   -- 建议上调
    WHEN AVG(g.required_count) * 2 < AVG(s.present_head)
         AND SUM(s.diff_minutes) < 0                            THEN 3   -- 建议核减
    ELSE 2                                                               -- 编制合适
  END                                                 AS advice_type,
  CASE
    WHEN SUM(s.plan_head) = 0
      THEN '近 14 天该单元无「上班」排班，无法评估编制'
    WHEN SUM(s.unrecorded_head) > 0.3 * SUM(s.plan_head)
      THEN CONCAT('出勤登记不全（未回填 ', SUM(s.unrecorded_head),
                  ' 人次 / 应出勤 ', SUM(s.plan_head),
                  ' 人次），先补登出勤再谈编制校准')
    WHEN ROUND(AVG(g.required_count), 1) > ROUND(AVG(s.present_head), 1) AND SUM(s.diff_minutes) > 0
      THEN CONCAT('实到长期低于需求（需求均 ', ROUND(AVG(g.required_count), 1),
                  ' 人 < 实到均 ', ROUND(AVG(s.present_head), 1),
                  ' 人）且存在超时工时 ', SUM(s.diff_minutes),
                  ' 分钟，疑似由加班填补缺口，建议上调护理编制下限并复核班次载荷')
    WHEN AVG(g.required_count) * 2 < AVG(s.present_head) AND SUM(s.diff_minutes) < 0
      THEN CONCAT('实到长期高于需求一倍以上（需求均 ', ROUND(AVG(g.required_count), 1),
                  ' 人、实到均 ', ROUND(AVG(s.present_head), 1),
                  ' 人）且工时富余，建议核减或调整到人少的班次')
    ELSE '实到与需求基本吻合，编制维持现状'
  END                                                 AS advice_text,
  CASE
    WHEN SUM(s.plan_head) = 0 THEN NULL
    WHEN SUM(s.unrecorded_head) > 0.3 * SUM(s.plan_head) THEN NULL
    WHEN ROUND(AVG(g.required_count), 1) > ROUND(AVG(s.present_head), 1) AND SUM(s.diff_minutes) > 0
      THEN CEIL(AVG(g.required_count))
    ELSE NULL
  END                                                 AS suggest_min_staff
FROM v_staff_worktime_summary s
LEFT JOIN v_staff_demand_gap g
       ON g.demand_date = s.work_date AND g.org_type = s.org_type
      AND g.org_id = s.org_id AND g.staff_type = s.staff_type
WHERE s.work_date BETWEEN CURDATE() - INTERVAL 14 DAY AND CURDATE() - INTERVAL 1 DAY
GROUP BY s.org_type, s.org_id, s.staff_type;

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs source/back_end/sql/214-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 biz_staff_attendance 表已建' AS item, COUNT(*) AS cnt, '1' AS expect FROM information_schema.TABLES WHERE table_schema = DATABASE() AND table_name = 'biz_staff_attendance'
-- 注：information_schema.STATISTICS 一个复合索引**按列计一行**，uk_attend 是
--     人×日×单元×班次 4 列 + 表级一行 = 5，不是查出一堆索引名（sql/209 踩过同个坑）。
UNION ALL SELECT 'V2 唯一键 uk_attend 存在（人×日×单元×班次，4列+表级共5行）', COUNT(*), '5' FROM information_schema.STATISTICS WHERE table_schema = DATABASE() AND table_name = 'biz_staff_attendance' AND index_name = 'uk_attend'
UNION ALL SELECT 'V3 biz_shift 迟到宽限已列且全部有值', COUNT(*), '0' FROM biz_shift WHERE del_flag = 0 AND IFNULL(late_grace_minutes, 0) = 0
UNION ALL SELECT 'V4 v_staff_worktime 视图已建', COUNT(*), '1' FROM information_schema.VIEWS WHERE table_schema = DATABASE() AND table_name = 'v_staff_worktime'
UNION ALL SELECT 'V5 v_staff_worktime_summary 视图已建', COUNT(*), '1' FROM information_schema.VIEWS WHERE table_schema = DATABASE() AND table_name = 'v_staff_worktime_summary'
UNION ALL SELECT 'V6 v_staff_calibration_advice 视图已建', COUNT(*), '1' FROM information_schema.VIEWS WHERE table_schema = DATABASE() AND table_name = 'v_staff_calibration_advice'
UNION ALL SELECT 'V7 对照主集：应上班且有班次可对照的计划行', COUNT(*), '1472' FROM biz_staff_schedule s JOIN biz_shift sh ON sh.id = s.shift_id AND sh.del_flag = 0 WHERE s.del_flag = 0 AND s.duty_status = 1
UNION ALL SELECT 'V8 其中过去日期、尚无出勤记录的（应>0，P3前就是全缺口）', COUNT(*), '>=1' FROM biz_staff_schedule s JOIN biz_shift sh ON sh.id = s.shift_id AND sh.del_flag = 0 WHERE s.del_flag = 0 AND s.duty_status = 1 AND s.schedule_date < CURDATE() AND NOT EXISTS (SELECT 1 FROM biz_staff_attendance a WHERE a.del_flag = 0 AND a.staff_schedule_id = s.id)
UNION ALL SELECT 'V9 当前实际出勤行数（应为 0：全库无打卡源，不许伪造）', COUNT(*), '0' FROM biz_staff_attendance WHERE del_flag = 0
UNION ALL SELECT 'V10 对照视图里出现差异类型 7（未回填）的行数', COUNT(*), '>=1' FROM v_staff_worktime WHERE diff_type = 7
UNION ALL SELECT 'V11 系统自判缺勤的行数（必须为 0：缺勤只能人确认）', COUNT(*), '0' FROM v_staff_worktime WHERE diff_type = 6
UNION ALL SELECT 'V11b 未来日期全部落在「9-待出勤」（不许混进实到）', COUNT(*), '0' FROM v_staff_worktime WHERE diff_type <> 9 AND work_date > CURDATE()
UNION ALL SELECT 'V12 汇总视图能对出过去 14 天有过排班的单元数', COUNT(*), '>=1' FROM v_staff_worktime_summary WHERE work_date BETWEEN CURDATE() - INTERVAL 14 DAY AND CURDATE() - INTERVAL 1 DAY
UNION ALL SELECT 'V13 校准建议此刻只应给「数据不足」档', COUNT(*), '0' FROM v_staff_calibration_advice WHERE advice_type <> 4
UNION ALL SELECT 'V14 每条校准建议都写了人话依据', COUNT(*), '0' FROM v_staff_calibration_advice WHERE IFNULL(advice_text, '') = '';
