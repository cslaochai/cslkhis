-- =====================================================================
-- 282 科室单独指定「门诊排班审核人 / 护理排班审核人」（与行政科室负责人解耦）
-- ---------------------------------------------------------------------
-- 背景：
--   此前排班审核只靠全局权限码 outpatient:schedule:audit / nursing:schedule:audit，
--   谁有码谁审全院，不绑定科室、不指定固定人。本次按「每个科室指定一名专职审核人」
--   落地：在 sys_department 上新增两个可空员工字段，门诊、护理分开指定各一人。
--   行政科室负责人 dept_leader_id 保持原样，不复用、不混淆。
--
-- 口径：
--   outpatient_auditor_id  门诊排班审核人（sys_employee.id），审本科室门诊排班
--   nursing_auditor_id     护理排班审核人（sys_employee.id），审本科室护理排班
--   系统管理员(10012)、门诊办(10035) 为全局豁免，不填也能审；其余角色必须命中
--   本科室对应审核人才能通过审核发布（后端 Service 校验，见门诊/护理审批链路）。
--
-- 幂等：重复执行前请先确认列是否已存在（MySQL 无通用 ADD COLUMN IF NOT EXISTS）。
-- =====================================================================

ALTER TABLE sys_department
    ADD COLUMN outpatient_auditor_id BIGINT NULL COMMENT '门诊排班审核人（sys_employee.id）' AFTER dept_leader_id,
    ADD COLUMN nursing_auditor_id    BIGINT NULL COMMENT '护理排班审核人（sys_employee.id）' AFTER outpatient_auditor_id;

-- 回查：三个人字段并列展示
-- SELECT id, dept_name, dept_leader_id, outpatient_auditor_id, nursing_auditor_id
--   FROM sys_department WHERE del_flag = 0 ORDER BY id;
