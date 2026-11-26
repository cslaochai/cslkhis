-- ============================================================
-- 364 药房冷链监控（药事管理入口）
-- 数据全部来自已有设备（coldchain）× 时序库 × biz_iot_event，不新增业务表
-- 授权：超级管理员(1)、药师(5)、药库管理员(37)
-- ============================================================

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3640000000000000101, '药房冷链监控', 500, 9, 2, '/pharmacy-coldchain',
       'iot/ColdChainView', 'pharmacy.iot.coldchain', 'Monitor',
       'pharmacy:iot:coldchain', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '冷藏柜温湿度与超温处置（GSP 记录）',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'pharmacy.iot.coldchain');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3640000000000000201, 1, 3640000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 3640000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3640000000000000202, 5, 3640000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 5 AND rm.menu_id = 3640000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3640000000000000203, 37, 3640000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 37 AND rm.menu_id = 3640000000000000101);
