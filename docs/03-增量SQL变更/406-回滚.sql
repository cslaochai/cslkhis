-- 406 回滚：撤销 406——删回补 (25,2041)，补回 role26 的 312 页面与 700 空目录（id 段 1941–1942）

-- ① 撤销回补：双向转诊-新增按钮收回门诊办管理员
DELETE FROM sys_role_menu WHERE role_id = 25 AND menu_id = 2041;

-- ② 补回 role26 摘除项
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (9210000000000001941, 26, 312),
  (9210000000000001942, 26, 700)
ON DUPLICATE KEY UPDATE update_time = NOW();
