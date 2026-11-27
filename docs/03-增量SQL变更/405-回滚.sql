-- 405 回滚：撤销 405——删回补 (33,2383)，补回 role26 九条（新 id 段 1931–1939），角色名改回

-- ① 撤销回补：2383 收回信息科管理员
DELETE FROM sys_role_menu WHERE role_id = 33 AND menu_id = 2383;

-- ② 补回 role26 摘除项（id 段 1931–1939）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (9210000000000001931, 26, 307),
  (9210000000000001932, 26, 2387),
  (9210000000000001933, 26, 2383),
  (9210000000000001934, 26, 613),
  (9210000000000001935, 26, 618),
  (9210000000000001936, 26, 29605),
  (9210000000000001937, 26, 29812),
  (9210000000000001938, 26, 1100),
  (9210000000000001939, 26, 29701)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ③ 角色名改回（role_code 不变）
UPDATE sys_role SET role_name = '门诊办', update_time = NOW() WHERE id = 25 AND role_code = '10035';
UPDATE sys_role SET role_name = '医务科', update_time = NOW() WHERE id = 26 AND role_code = '10036';
