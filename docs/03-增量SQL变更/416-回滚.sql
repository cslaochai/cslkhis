-- 416 回滚：撤掉门诊多科室仿真补的门诊分诊护士岗位（只删本脚本建的，按 create_by 留痕识别）
DELETE FROM sys_employee_post
 WHERE role_id = 9 AND create_by = 'sql416';

-- 校验：SELECT COUNT(*) FROM sys_employee_post WHERE create_by='sql416';  -- 应为 0
