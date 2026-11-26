-- 371 病区床位安全（护理管理入口，住院护士站视角）
-- 授权：系统管理员(1)、护士(3)、护士长(10)、护理部主任(29)、科护士长(30)
-- 说明：床垫是固定安装设备，页面以"床位 + 在院患者"为主键，不按设备清单展示。

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3710000000000000101, '病区床位安全', 330, 1, 2, '/iot-bed',
       'iot/BedSafetyView', 'nursing.bedSafety', 'Monitor',
       'nursing:bedSafety:list', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '智能床垫：在床/离床、心率呼吸、翻身活动与床位告警',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'nursing.bedSafety');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3710000000000000201, 1, 3710000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 3710000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3710000000000000202, 3, 3710000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 3 AND rm.menu_id = 3710000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3710000000000000203, 10, 3710000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 10 AND rm.menu_id = 3710000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3710000000000000204, 29, 3710000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 29 AND rm.menu_id = 3710000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3710000000000000205, 30, 3710000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 30 AND rm.menu_id = 3710000000000000101);
