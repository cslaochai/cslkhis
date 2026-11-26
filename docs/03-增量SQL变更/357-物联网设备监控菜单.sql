-- =====================================================================
-- 357 物联网设备监控菜单
--
-- 依赖：his-ms-iot 模块（MQTT 订阅落库 + TDengine 查询），见
--       docs/00-开发文档/物联网设备数据存储设计.md、356 时序库 DDL。
-- 目的：给「物联网设备监控」页面挂菜单（物资设备 → 物联网设备监控），
--       让设备科/护士长/护理部能从侧栏进页面看设备在线状态与实时指标。
-- 手工执行：需判重（menu_key 唯一、角色授权唯一），可重复跑。
-- 跑完后权限缓存（his:perm:role:*，TTL 10 分钟）不会自动失效，
--   要立即生效请重启服务或删掉对应缓存键。
-- =====================================================================

-- ① 菜单：物联网设备监控（父目录 900 物资设备）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3570000000000000101, '物联网设备监控', 900, 12, 2, '/iot-devices',
       'iot/DeviceMonitorView', 'asset.iot', 'Monitor',
       'asset:iot:list', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '设备在线状态与实时指标（数据源 TDengine，只读）',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'asset.iot');

-- ② 角色授权：系统管理员 / 护士长 / 护理部主任
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    SELECT 3570000000000000201 AS id, 1 AS role_id, 3570000000000000101 AS menu_id UNION ALL
    SELECT 3570000000000000202, 10, 3570000000000000101 UNION ALL
    SELECT 3570000000000000203, 29, 3570000000000000101
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = seed.role_id AND rm.menu_id = seed.menu_id);

-- =====================================================================
-- 回查（跑完手工核对）
-- =====================================================================
-- SELECT id, menu_name, parent_id, menu_type, path, component, permission, is_visible, status
--   FROM sys_menu WHERE id = 3570000000000000101;
-- SELECT rm.role_id, r.role_name, rm.menu_id
--   FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id
--  WHERE rm.menu_id = 3570000000000000101 ORDER BY rm.role_id;
