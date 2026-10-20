-- =====================================================================
-- 283 科室单独指定「全院岗位排班审核人」（与行政科室负责人、门诊/护理审核人解耦）
-- ---------------------------------------------------------------------
-- 背景：
--   282 已落地门诊、护理两套排班各指定一名专职审核人。本次按同一模式，在全院
--   岗位排班域（biz_schedule / StaffScheduleController 的提交-审核发布链路）也引入
--   「每个科室指定一名专职审核人」：在 sys_department 上新增一个可空员工字段。
--   行政科室负责人 dept_leader_id、门诊/护理审核人保持原样，不复用、不混淆。
--
--   全院岗位排班覆盖医生、技师、药师、行政等非护理岗位，审核人是本科室该域的
--   指定负责人（如科主任、区护士长或行政主管），与护理排班审核人可不同人。
--
-- 口径：
--   schedule_auditor_id   全院岗位排班审核人（sys_employee.id），审本科室全院岗位排班
--   系统管理员(10012) 为全局豁免，不填也能审；其余角色必须命中本科室该审核人才能
--   通过审核发布（后端 assertScheduleAuditor(deptId) 校验）。
--   注：orgType=3（全院级）的排班记录没有归属科室，仅系统管理员可审核发布。
--
-- 幂等：MySQL 无通用 ADD COLUMN IF NOT EXISTS，重复执行前请先确认列是否已存在。
-- =====================================================================

ALTER TABLE sys_department
    ADD COLUMN schedule_auditor_id BIGINT NULL COMMENT '全院岗位排班审核人（sys_employee.id）'
        AFTER nursing_auditor_id;

-- 回查：四个人字段并列展示
-- SELECT id, dept_name, dept_leader_id, outpatient_auditor_id, nursing_auditor_id, schedule_auditor_id
--   FROM sys_department WHERE del_flag = 0 ORDER BY id;
