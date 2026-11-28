-- 433 回滚：还原 357/386 变更前授权（375 未动，无需回滚）
SET NAMES utf8mb4;

-- 1) 357 物联网设备监控：删 39，恢复护士长(10)、护理部主任(29)
DELETE rm
FROM sys_role_menu rm
WHERE rm.menu_id = 3570000000000000101
  AND rm.role_id = 39;

INSERT IGNORE INTO sys_role_menu
    (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'sql433', NOW(), 'sql433', NOW(), 1, 1
FROM (
    SELECT 4330000000000000301 AS id, 10 AS role_id, 3570000000000000101 AS menu_id UNION ALL
    SELECT 4330000000000000302, 29, 3570000000000000101
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = seed.role_id AND rm.menu_id = seed.menu_id);

-- 2) 386 告警中心：删 39（1、10、29 保持不变）
DELETE rm
FROM sys_role_menu rm
WHERE rm.menu_id = 3860000000000000101
  AND rm.role_id = 39;

-- 执行后清 Redis：his:perm:role:*
-- 回查：
-- SELECT rm.menu_id, rm.role_id, r.role_name FROM sys_role_menu rm
--   JOIN sys_role r ON r.id = rm.role_id
--  WHERE rm.menu_id IN (3570000000000000101, 3860000000000000101) ORDER BY rm.menu_id, rm.role_id;
-- 预期（变更前）：357 = 1、10、29；386 = 1、10、29
