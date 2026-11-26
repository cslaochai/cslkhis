-- =====================================================================
-- 358 病区患者监护大屏菜单（护士站视角）
-- 【已废弃】按 360 撤掉：同一路由不要挂两个目录，护士站入口统一在「住院管理 → 病区患者监护」(359)。
--
-- 目的：物联网数据不再只按设备铺开，护士站以「患者/床位」为主键看床旁设备实时指标与患者趋势。
--       设备清单页（357 物联网设备监控）保留为设备科/运维视角，两者并行。
-- 手工执行：需判重（menu_key 唯一、角色授权唯一），可重复跑。
-- 跑完后权限缓存（his:perm:role:*，TTL 10 分钟）不会自动失效，
--   要立即生效请重启服务或删掉对应缓存键。
-- =====================================================================

-- ① 菜单：病区患者监护（父目录 900 物资设备，排在设备监控之后）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3580000000000000101, '病区患者监护', 900, 13, 2, '/iot-ward',
       'iot/WardMonitorView', 'asset.iot.ward', 'Odometer',
       'asset:iot:ward', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '护士站床位卡：患者信息 + 床旁设备实时指标 + 患者趋势（只读）',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'asset.iot.ward');

-- ② 角色授权：系统管理员 / 护士长 / 护理部主任
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    SELECT 3580000000000000201 AS id, 1 AS role_id, 3580000000000000101 AS menu_id UNION ALL
    SELECT 3580000000000000202, 10, 3580000000000000101 UNION ALL
    SELECT 3580000000000000203, 29, 3580000000000000101
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = seed.role_id AND rm.menu_id = seed.menu_id);

-- =====================================================================
-- 回查（跑完手工核对）
-- =====================================================================
-- SELECT id, menu_name, parent_id, menu_type, path, component, permission, is_visible, status
--   FROM sys_menu WHERE id = 3580000000000000101;
-- SELECT rm.role_id, r.role_name, rm.menu_id
--   FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id
--  WHERE rm.menu_id = 3580000000000000101 ORDER BY rm.role_id;
