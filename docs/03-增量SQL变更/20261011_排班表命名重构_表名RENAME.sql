-- ============================================================
-- 排班表命名重构 — 数据库表名 RENAME
-- 日期：2026-10-11
-- 说明：配合代码侧实体/类改名（见 _rename_schedule_tables.py）与文档种子名改名。
--       将"门诊号源体系"与"排班主体(底座)"的表名按语义归正：
--         biz_schedule*          -> biz_clinic_source*   （门诊号源表及其从表/变更日志）
--         biz_staff_schedule     -> biz_schedule         （排班表主体，全角色出勤事实）
--       护理排班表 biz_nurse_schedule 保持不变。
-- 注意：本脚本为一次性 DDL，需从【旧表名】环境执行；
--       已执行或目标名已存在的环境，请用 _run_rename_tables.py 做幂等修复。
-- ============================================================

SET FOREIGN_KEY_CHECKS=0;

RENAME TABLE `biz_schedule`              TO `biz_clinic_source`,
             `biz_schedule_slot`         TO `biz_clinic_source_slot`,
             `biz_schedule_slot_template` TO `biz_clinic_source_slot_template`,
             `biz_schedule_template`     TO `biz_clinic_source_template`,
             `biz_schedule_change_log`   TO `biz_clinic_source_change_log`;

RENAME TABLE `biz_staff_schedule`        TO `biz_schedule`;

SET FOREIGN_KEY_CHECKS=1;
