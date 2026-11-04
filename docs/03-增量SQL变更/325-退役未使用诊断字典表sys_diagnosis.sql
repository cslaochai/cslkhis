-- =====================================================================
-- 325 退役未使用诊断字典表 sys_diagnosis
-- ---------------------------------------------------------------------
-- 背景：
--   sys_diagnosis（诊断字典，82 行人工挑选的常用诊断铺底）自建模以来从未
--   接入任何代码，属于死表：
--   1. 后端全部 Java/Mapper XML 零引用，无 entity/mapper/service/controller，
--      git 历史中亦从未出现（git log -S"sys_diagnosis" -- source 为空）；
--   2. 前端、小程序零引用，无诊断字典维护页或下拉接口；
--   3. 无任何业务表通过外键/字段引用它；
--   4. AI 运营问数曾登记该表（sys_ai_qa_schema），但 is_enabled=0，从未启用。
--
--   系统实际使用的诊断字典是 sys_icd10（医生站/急诊/住院病历/DRG·DIP 的
--   /system/icd10/selectList 与 AI ICD 推荐能力），传染病标记由
--   sys_infectious_disease 承担；"常用诊断"能力从未实现。
--
-- 改动：
--   1. 清理 AI 运营问数中该表的表/列登记；
--   2. DROP 该表；
--   3. 同步移除初始化 DDL 建表语句、铺底数据 sys_diagnosis.sql 与
--      00_import 引用、E-R 图（er-by-domain / powerdesigner / viewer）。
--
-- 执行说明：
--   无代码依赖，可独立执行；如需留底可先备份：
--     CREATE TABLE sys_diagnosis_bak_325 AS SELECT * FROM sys_diagnosis;
-- =====================================================================

-- 1) 清理 AI 运营问数登记
DELETE FROM `sys_ai_qa_column` WHERE `table_name` = 'sys_diagnosis';
DELETE FROM `sys_ai_qa_schema` WHERE `table_name` = 'sys_diagnosis';

-- 2) 删除表
DROP TABLE IF EXISTS `sys_diagnosis`;
