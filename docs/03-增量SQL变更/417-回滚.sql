-- 417 回滚：撤掉演示账号扩充所建的岗位、账号、员工（按 create_by='sql417' 留痕识别，只删本脚本建的）
DELETE FROM `sys_employee_post` WHERE create_by = 'sql417';
DELETE FROM `sys_user`          WHERE create_by = 'sql417';
DELETE FROM `sys_employee`      WHERE create_by = 'sql417';

-- 校验：以下三句均应为 0
-- SELECT COUNT(*) FROM sys_employee_post WHERE create_by='sql417';
-- SELECT COUNT(*) FROM sys_user          WHERE create_by='sql417';
-- SELECT COUNT(*) FROM sys_employee      WHERE create_by='sql417';
