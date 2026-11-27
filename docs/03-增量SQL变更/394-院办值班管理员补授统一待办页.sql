-- ============================================================
-- 394 院办值班管理员(36) 补授统一待办页 29654
-- 日期：2026-11-26
-- 背景：29654 为全员通用待办页（父目录 100 门户，36 已授 100），
--       36 漏授导致院办值班岗工作台无统一待办入口。
-- 回滚：见 394-回滚.sql
-- ============================================================

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES (9210000000000001401, 36, 29654, 'menu-govern-394', NOW(), 'menu-govern-394', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 回查：SELECT role_id, menu_id FROM sys_role_menu WHERE role_id=36 AND menu_id=29654;
-- 清缓存：Redis 删除 his:perm:role:* 与 his:user:identity:*
