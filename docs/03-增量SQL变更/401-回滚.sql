-- 401 回滚：撤销体检前台拆分，恢复 role7/24 原状
-- ① 岗位回滚：两名体检接待 43→7
UPDATE sys_employee_post SET role_id = 7, update_by = 'menu-govern-401', update_time = NOW()
WHERE employee_id IN (897001319, 897001323) AND role_id = 43;

-- ② 删除新角色授权
DELETE FROM sys_role_menu WHERE role_id = 43;

-- ③ 删除新角色
DELETE FROM sys_role WHERE id = 43;

-- ④ 回补 role7 财务结算/收费作业、role24 健康管理空目录
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (9210000000000001811, 7, 1000),
  (9210000000000001812, 7, 4101),
  (9210000000000001813, 24, 1400)
ON DUPLICATE KEY UPDATE update_time = NOW();
