-- 400 回滚：恢复不含日期的唯一键
ALTER TABLE `sys_employee_post` DROP INDEX `uk_emp_role_dept_date`;
ALTER TABLE `sys_employee_post` ADD UNIQUE KEY `uk_emp_role_dept` (`employee_id`, `role_id`, `dept_id`);
