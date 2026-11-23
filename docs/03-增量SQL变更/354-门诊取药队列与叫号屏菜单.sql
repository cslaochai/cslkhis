-- =====================================================================
-- 354 门诊取药队列与叫号屏菜单
--
-- 依赖：353（biz_dispense_queue 与 4 个门诊西药房取药窗口）
-- 目的：给「取药队列」页面挂菜单，让药剂师/临床药师能从药事管理 → 药房调剂进页面。
--      「取药叫号屏」不进菜单栏（设备页直接开 URL），但也建一行，
--       这样菜单树里存在该 path，守卫不会把装屏的浏览器踢回工作台。
-- 手工执行：需判重（menu_key 唯一、角色授权唯一），可重复跑。
-- 跑完后权限缓存（his:perm:role:*，TTL 10 分钟）不会自动失效，
--   要立即生效请重启服务或删掉对应缓存键。
-- =====================================================================

-- ① 菜单：取药队列
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3540000000000000101, '取药队列', 29801, 11, 2, '/dispense-queue',
       'pharmacy-window/DispenseQueueView', 'pharmacy.dispenseQueue', 'Tickets',
       'pharmacy:dispensing:list', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '签到取号、窗口叫号、过号恢复',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'pharmacy.dispenseQueue');

-- ② 菜单：取药叫号屏（隐藏，不进菜单栏）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3540000000000000102, '取药叫号屏', 29801, 12, 2, '/dispense-screen',
       'pharmacy-window/DispenseScreenView', 'pharmacy.dispenseScreen', 'Monitor',
       'pharmacy:dispensing:list', 0, 0, 0, 1,
       'admin', NOW(), 'admin', NOW(), 0, '一块屏对一个窗口，靠 URL 参数 stationCode 绑定',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'pharmacy.dispenseScreen');

-- ③ 角色授权：系统管理员 / 药剂师 / 临床药师（沿用药房发药菜单 502 的既有授权面）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    SELECT 3540000000000000201 AS id, 1 AS role_id, 3540000000000000101 AS menu_id UNION ALL
    SELECT 3540000000000000202, 5, 3540000000000000101 UNION ALL
    SELECT 3540000000000000203, 13, 3540000000000000101 UNION ALL
    SELECT 3540000000000000204, 1, 3540000000000000102 UNION ALL
    SELECT 3540000000000000205, 5, 3540000000000000102 UNION ALL
    SELECT 3540000000000000206, 13, 3540000000000000102
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = seed.role_id AND rm.menu_id = seed.menu_id);

-- =====================================================================
-- 回查（跑完手工核对）
-- =====================================================================
-- SELECT id, menu_name, parent_id, menu_type, path, component, permission, is_visible, status
--   FROM sys_menu WHERE id IN (3540000000000000101, 3540000000000000102);
-- SELECT rm.role_id, r.role_name, rm.menu_id
--   FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id
--  WHERE rm.menu_id IN (3540000000000000101, 3540000000000000102) ORDER BY rm.role_id, rm.menu_id;
