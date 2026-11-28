-- 427 回滚：把 902 / 2125 / 2126 重新授给系统管理员（角色 1）
SET NAMES utf8mb4;

INSERT IGNORE INTO sys_role_menu
    (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    SELECT 4270000000000000101 AS id, 1 AS role_id, 902 AS menu_id UNION ALL
    SELECT 4270000000000000102, 1, 2125 UNION ALL
    SELECT 4270000000000000103, 1, 2126
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = seed.role_id AND rm.menu_id = seed.menu_id);

-- 执行后清 Redis：his:perm:role:*
-- 回查：SELECT menu_id FROM sys_role_menu WHERE role_id = 1 AND menu_id IN (902,2125,2126);
