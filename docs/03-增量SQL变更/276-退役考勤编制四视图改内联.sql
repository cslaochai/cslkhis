-- =============================================================
-- 276-退役考勤编制四个视图，聚合逻辑内联进 Mapper
-- 背景：v_staff_worktime / v_staff_worktime_summary / v_staff_calibration_advice /
--       v_staff_demand_gap 建库时引用旧排班事实表 biz_staff_schedule；该表已改名合并为
--       biz_schedule（排班表命名重构增量），视图未同步更新，MySQL 1356（引用无效表）失效，
--       前端 try/catch 吞错，页面长期显示空态。
-- 处置：聚合逻辑已从库视图搬入后端，页面口径不变——
--   1) v_staff_worktime            -> BizStaffAttendanceMapper.xml 的 worktimeBase（计划段 UNION ALL 无计划出勤段）
--   2) v_staff_worktime_summary    -> 同文件 summaryAgg（单元 × 日 人头/工时聚合）
--   3) v_staff_calibration_advice  -> 同文件 adviceAgg（近 14 天编制校准归因）
--   4) v_staff_demand_gap          -> BizStaffDemandMapper.selectGap 内联派生表（需求 − 在岗）
--   内联时统一把旧表名 biz_staff_schedule 修正为真实表 biz_schedule。
-- 说明：本脚本只删除失效视图对象，不删任何基表、不动业务数据；幂等，可重复执行。
-- =============================================================

DROP VIEW IF EXISTS v_staff_calibration_advice;
DROP VIEW IF EXISTS v_staff_worktime_summary;
DROP VIEW IF EXISTS v_staff_worktime;
DROP VIEW IF EXISTS v_staff_demand_gap;
