-- 397 回滚：恢复护理岗被摘跨域页面（按新 id 段补插，不保留原 id）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (9210000000000001601, 3,  413),
  (9210000000000001602, 3,  2924),
  (9210000000000001603, 10, 2924)
ON DUPLICATE KEY UPDATE update_time = NOW();
