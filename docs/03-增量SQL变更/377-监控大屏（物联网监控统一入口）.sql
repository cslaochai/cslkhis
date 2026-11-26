-- 377 监控大屏（把分散在各业务域的物联网监控页收拢到一个顶级入口）
-- 背景：冷链在药事管理、气体/资产在物资设备、床位在护理管理、患者监护在住院管理，
--       护士站/设备科每次找监控要跨 4 个菜单，入口不统一。
-- 方案：新建顶级目录「监控大屏」，把 6 个监控页挂到其下，原菜单只换 parent_id/sort_order，不删不改权限码。
--       子菜单原有的授权关系保持不变；父目录按"任一子菜单被授权"的角色补齐父级授权。

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3770000000000000101, '监控大屏', 0, 15, 1, '/monitor-wall', null, 'monitor.wall', 'Monitor',
       null, 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '物联网监控统一入口：患者监护/床位安全/冷链/医用气体/资产定位/设备监控',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'monitor.wall');

-- 6 个监控页挂到「监控大屏」下，顺序按护士站使用频度
UPDATE sys_menu SET parent_id = 3770000000000000101, sort_order = 1
WHERE id = 3590000000000000101;  -- 病区患者监护 /iot-ward

UPDATE sys_menu SET parent_id = 3770000000000000101, sort_order = 2
WHERE id = 3710000000000000101;  -- 病区床位安全 /iot-bed

UPDATE sys_menu SET parent_id = 3770000000000000101, sort_order = 3
WHERE id = 3640000000000000101;  -- 药房冷链监控 /pharmacy-coldchain

UPDATE sys_menu SET parent_id = 3770000000000000101, sort_order = 4
WHERE id = 3680000000000000101;  -- 医用气体监测 /iot-gas

UPDATE sys_menu SET parent_id = 3770000000000000101, sort_order = 5
WHERE id = 3750000000000000101;  -- 资产定位与维保 /iot-assets

UPDATE sys_menu SET parent_id = 3770000000000000101, sort_order = 6
WHERE id = 3570000000000000101;  -- 医疗设备监控 /iot-devices

-- 父目录授权：凡被授予任一监控子菜单的角色，都能看到「监控大屏」。
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3770000000000000201, 1, 3770000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 3770000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3770000000000000202, 3, 3770000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 3 AND rm.menu_id = 3770000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3770000000000000203, 5, 3770000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 5 AND rm.menu_id = 3770000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3770000000000000204, 10, 3770000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 10 AND rm.menu_id = 3770000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3770000000000000205, 29, 3770000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 29 AND rm.menu_id = 3770000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3770000000000000206, 30, 3770000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 30 AND rm.menu_id = 3770000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3770000000000000207, 32, 3770000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 32 AND rm.menu_id = 3770000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3770000000000000208, 37, 3770000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 37 AND rm.menu_id = 3770000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3770000000000000209, 39, 3770000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 39 AND rm.menu_id = 3770000000000000101);
