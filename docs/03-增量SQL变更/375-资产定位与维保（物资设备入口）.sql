-- 375 资产定位与维保（物资设备入口）
-- 授权：系统管理员(1)、设备与供应室管理员(39)
-- 说明：资产定位面向设备科与护士站"找设备"，维保状态来自设备台账（equipmentId 关联 sys_equipment）。

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3750000000000000101, '资产定位与维保', 900, 14, 2, '/iot-assets',
       'iot/AssetTrackingView', 'asset.iot.asset', 'Monitor',
       'asset:iot:asset', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '在用设备实时位置、离位告警、信标电量与维保到期提醒',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'asset.iot.asset');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3750000000000000201, 1, 3750000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 3750000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3750000000000000202, 39, 3750000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 39 AND rm.menu_id = 3750000000000000101);
