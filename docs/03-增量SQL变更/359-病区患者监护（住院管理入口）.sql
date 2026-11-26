-- =====================================================================
-- 359 病区患者监护（住院管理入口）
--
-- 目的：护士站的人从「住院管理」进得来，而不是只在「物资设备」目录下找。
--       与 358 指向同一路由 /iot-ward、同一权限码 asset:iot:ward，只是挂在不同父目录：
--         物资设备（900）  → 设备视角入口（358）
--         住院管理（300）  → 护士站视角入口（359）
-- 手工执行：需判重（menu_key 唯一、角色授权唯一），可重复跑。
-- 跑完后权限缓存（his:perm:role:*，TTL 10 分钟）不会自动失效，
--   要立即生效请重启服务或删掉对应缓存键。
-- =====================================================================

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3590000000000000101, '病区患者监护', 300, 4, 2, '/iot-ward',
       'iot/WardMonitorView', 'ipd.iot.ward', 'Monitor',
       'asset:iot:ward', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '住院管理下的护士站入口：床位卡 + 床旁设备实时指标 + 患者趋势（只读）',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'ipd.iot.ward');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    SELECT 3590000000000000201 AS id, 1 AS role_id, 3590000000000000101 AS menu_id UNION ALL
    SELECT 3590000000000000202, 10, 3590000000000000101 UNION ALL
    SELECT 3590000000000000203, 29, 3590000000000000101
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = seed.role_id AND rm.menu_id = seed.menu_id);
