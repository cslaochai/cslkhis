-- 368 医用气体压力监测（物资设备入口）
-- 授权：系统管理员(1)、设备与供应室管理员(39)、护士长(10)、手术室护士(32)

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3680000000000000101, '医用气体监测', 900, 13, 2, '/iot-gas',
       'iot/GasMonitorView', 'asset.iot.gas', 'Monitor',
       'asset:iot:gas', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '中心供氧/负压/压缩空气终端压力与故障告警',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'asset.iot.gas');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3680000000000000201, 1, 3680000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 3680000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3680000000000000202, 39, 3680000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 39 AND rm.menu_id = 3680000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3680000000000000203, 10, 3680000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 10 AND rm.menu_id = 3680000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3680000000000000204, 32, 3680000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 32 AND rm.menu_id = 3680000000000000101);
