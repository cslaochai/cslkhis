-- 395 回滚：恢复院领导(18) 被摘的报表工具/明细页（按新 id 段补插，不保留原 id）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (9210000000000001501, 18, 907),
  (9210000000000001502, 18, 908),
  (9210000000000001503, 18, 910),
  (9210000000000001504, 18, 1201),
  (9210000000000001505, 18, 2159)
ON DUPLICATE KEY UPDATE update_time = NOW();
