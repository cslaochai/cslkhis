-- =====================================================================
-- 2026-10-19  急诊当班数据源切岗位排班：核对与可选收口
-- =====================================================================
-- 背景：急诊「谁在岗」此前从门诊号源排班（biz_clinic_source）取数，语义错位
--       且跨零点夜班（schedule_date 归开始日）在今天凌晨取不到，急诊会掉待派单池。
--       本次改动把急诊消费端（EmergencyServiceImpl.onDutySchedules / pickOnDutySchedule /
--       dutySelectList / 交班班次名）统一切到岗位排班底座 biz_schedule 的急诊科室单元
--       （org_type=1 + 急诊科 id），经 StaffScheduleService.onDutyAt 按「昨天+今天」两日
--       捞行 + ShiftCoverUtil 判覆盖，凌晨 01:00 能命中「昨天 22:00 开班」的夜班。
--
-- 探查结论（为什么本脚本没有建班次、没有做数据迁移）：
--   · 急诊三班次已存在：890000000000000007 白班 08:00-16:00 / 008 前夜 16:00-23:00 /
--     009 后夜 00:00-08:00（schedule_type 3/2/4），岗位排班写入口 requireUsableShift
--     只查「启用 + apply_staff_type 匹配」，不限班次册，现有班次可直接选中；
--   · biz_schedule 急诊科室单元行已存在（门诊排班与岗位排班本就双写，铺底即如此）；
--   · publish_status 已由 20261019_G15排班发布态.sql 把存量行统一置为 2（已发布），
--     满足 onDutyAt 的生效判定（∈ {2 已发布, 4 已变更}）。
--   因此本次 SQL 只做「核对 + 可选收口」，主体改动在后端代码（已改并编译通过）。
--
-- 执行口径：
--   · 第 1~3 段为核对 SELECT，只读，可随时执行；
--   · 第 4 段为可选收口 UPDATE，默认可不执行（先核对 3 的号源行数再决定）。
-- 幂等性：核对段只读；收口段 status<>0 条件 + del_flag=0 条件，可重复执行。
-- 执行前提：后端已换新 jar（含本次 EmergencyServiceImpl 改动）并重启。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 一、核对：急诊科岗位排班行是否满足「急诊当班」命中条件
--     onDutyAt(科室单元, 急诊科id, 医生) 要求：
--       org_type=1 科室单元 · staff_type=1 医生 · duty_status=1 上班 ·
--       publish_status ∈ {2 已发布, 4 已变更} · del_flag=0
--     期望：能看到急诊内/外/儿科已发布的医生排班行；行数为 0 时请先在
--           「岗位出勤」页给急诊科按急诊班次排班并发布。
-- ---------------------------------------------------------------------
SELECT s.id, s.schedule_date, s.week_day, s.org_type, s.org_id, s.org_name,
       s.employee_id, s.employee_name, s.staff_type, s.shift_id,
       s.start_time, s.end_time, s.duty_status, s.attend_mode, s.clinic_flag,
       s.publish_status
  FROM biz_schedule s
 WHERE s.del_flag = 0
   AND s.org_type = 1
   AND s.org_name LIKE '%急诊%'
   AND s.staff_type = 1
   AND s.duty_status = 1
   AND s.publish_status IN (2, 4)
 ORDER BY s.schedule_date DESC, s.org_name, s.start_time;

-- ---------------------------------------------------------------------
-- 二、核对：急诊三班次定义（白班 08:00-16:00 / 前夜 16:00-23:00 / 后夜 00:00-08:00）
-- ---------------------------------------------------------------------
SELECT id, shift_name, start_time, end_time, cross_day, is_night,
       schedule_type, use_scope, apply_staff_type, status
  FROM biz_shift
 WHERE id IN (890000000000000007, 890000000000000008, 890000000000000009)
   AND del_flag = 0;

-- ---------------------------------------------------------------------
-- 三、核对：急诊科门诊号源残留（收口前先看有多少）
--     这些行由旧「门诊排班」双写派生，急诊挂号本就不走号源，保留无害，
--     仅影响门诊排班页/总览展示；若确认无需对外再执行第 4 段。
-- ---------------------------------------------------------------------
SELECT dept_id, dept_name, COUNT(*) AS 号源行数,
       SUM(status = 1) AS 启用行数, SUM(publish_status IN (2, 4)) AS 已发布行数
  FROM biz_clinic_source
 WHERE del_flag = 0 AND dept_name LIKE '%急诊%'
 GROUP BY dept_id, dept_name;

-- =====================================================================
-- 四、【可选】停用急诊科门诊号源（收口双写，保留历史数据）
--     幂等：status<>0 + del_flag=0 条件，可重复执行。
--     不影响：急诊挂号（直录不走号源）、岗位排班、急诊当班取数（已切 biz_schedule）。
--     影响：门诊排班页不再展示/维护急诊科号源（急诊科出诊计划不对外放号）。
--     若过渡期仍需在门诊排班页给急诊科排班，请跳过本段。
-- =====================================================================
-- UPDATE biz_clinic_source
--    SET status = 0, update_by = 'admin', update_by_id = 1, update_time = NOW()
--  WHERE del_flag = 0 AND dept_name LIKE '%急诊%' AND status <> 0;

-- 执行后回查：四段核对均应符合预期；再以一次凌晨（00:00-08:00 后夜班覆盖时段）
-- 的急诊登记验证自动派单命中后夜班医生（EmergencyAssignTypeEnum.AUTO_BY_SCHEDULE）。

-- =====================================================================
-- 五、【必执行】急诊三班次改「全院通用」册（use_scope 1 → 4）
-- =====================================================================
-- 原因：岗位出勤页的班次下拉只放 4-全院通用册（StaffScheduleEdit.vue 的
--       SHIFT_USE_SCOPE_GENERAL=4），急诊三班次原为 1-门诊册，岗位出勤页选不到，
--       急诊科无法在岗位排班里排班（这正是旧设计把急诊挂在门诊排班页的原因）。
--       改为通用册后：岗位出勤页可选中；门诊排班页仍可选（usableByOutpatient
--       放行 门诊册 + 通用册），不破坏门诊侧兼容。
-- 影响：仅班次的可选范围扩大，班次时间/夜班/工时等定义不动；
--       岗位排班写入口 requireUsableShift 不校验班次册，无联动风险。
-- 幂等：use_scope<>4 条件，可重复执行。
-- =====================================================================
UPDATE biz_shift
   SET use_scope = 4, update_by = 'admin', update_by_id = 1, update_time = NOW()
 WHERE del_flag = 0
   AND id IN (890000000000000007, 890000000000000008, 890000000000000009)
   AND use_scope <> 4;

-- 回查：三行 use_scope 均应为 4。
--   SELECT id, shift_name, use_scope FROM biz_shift
--    WHERE id IN (890000000000000007, 890000000000000008, 890000000000000009);
