-- 398 回滚：恢复护理岗被回收的清点按钮与空目录（按新 id 段补插，不保留原 id）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (9210000000000001701, 3,  2926),
  (9210000000000001702, 3,  2927),
  (9210000000000001703, 3,  400),
  (9210000000000001704, 3,  29810),
  (9210000000000001705, 10, 2926),
  (9210000000000001706, 10, 2927),
  (9210000000000001707, 10, 29810)
ON DUPLICATE KEY UPDATE update_time = NOW();
