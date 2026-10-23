-- 07-预约挂号·排班·分诊·叫号 铺底导入（依赖顺序见文件名，同域内互不依赖）
-- 用法：本目录下执行  mysql --default-character-set=utf8mb4 库名 < 00_import.sql
SET NAMES utf8mb4;

SOURCE ./biz_clinic_source_slot_template.sql;
SOURCE ./biz_clinic_source_template.sql;
SOURCE ./biz_opd_station.sql;
SOURCE ./biz_revisit_fee_policy.sql;
SOURCE ./biz_staff_plan_rule.sql;
SOURCE ./biz_triage_rule.sql;
