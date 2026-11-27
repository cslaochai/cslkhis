-- 400 员工岗位唯一键带生效日期：支持「A 科 → B 科 → 再回 A 科」同角色同科多段。
-- 原 uk_emp_role_dept(employee_id, role_id, dept_id) 不含日期，回轮岗的第二段同科行会撞唯一键落到 500 Duplicate entry。
-- 新唯一键带 effective_date：同科多段按生效日区分；长期生效（生效日 NULL）的多行由服务端时段互斥校验兜底
-- （MySQL 唯一索引对多个 NULL 不去重）。
ALTER TABLE `sys_employee_post` DROP INDEX `uk_emp_role_dept`;
ALTER TABLE `sys_employee_post` ADD UNIQUE KEY `uk_emp_role_dept_date` (`employee_id`, `role_id`, `dept_id`, `effective_date`);
