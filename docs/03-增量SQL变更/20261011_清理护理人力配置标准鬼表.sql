-- 20261011 清理护理人力配置标准鬼表 biz_nurse_schedule_rule
-- 依据（代码核实）：
--   1. sql/206 起该表已废弃，写入停止，标准并入统一的 biz_staff_plan_rule（护理用 staff_type=2 表达）。
--   2. 全后端 grep 确认无任何 service 注入/调用 BizNurseScheduleRuleMapper；
--      BizNurseScheduleRule 实体、BizNurseScheduleRuleMapper 仅自身引用；NurseScheduleServiceImpl
--      读取护理人力标准实际来自 biz_staff_plan_rule（toRules(wardId, List<BizStaffPlanRule>)），与死表无关。
--   3. 纯死代码：建表 DDL、种子数据、00_import_all.sql 导入行均已从仓库移除（见本目录同次提交）。
-- 本 SQL 只负责清洗【已上线数据库】里残留的该表。
-- 注意：DROP 前请确认该表确无业务读路径（出院/护理统计若仍有直查需先改）。

DROP TABLE IF EXISTS `biz_nurse_schedule_rule`;
