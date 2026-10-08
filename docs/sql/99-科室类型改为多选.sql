-- 科室类型字段从单值 tinyint 改为多值 varchar（逗号分隔）
-- 执行时间：2026-10-08
-- 影响范围：sys_department.dept_type

ALTER TABLE sys_department 
  MODIFY COLUMN dept_type VARCHAR(20) NOT NULL DEFAULT '1' 
  COMMENT '科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔';

-- 新增科室负责人字段
ALTER TABLE sys_department 
  ADD COLUMN dept_leader_id BIGINT DEFAULT NULL COMMENT '科室负责人（sys_employee.id)' AFTER location;

-- 验证：检查转换后数据是否正常
SELECT id, dept_name, dept_type, dept_leader_id FROM sys_department LIMIT 10;
