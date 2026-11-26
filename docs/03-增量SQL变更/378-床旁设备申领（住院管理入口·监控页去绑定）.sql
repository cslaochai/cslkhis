-- 378 床旁设备申领（住院管理入口）【已废弃，勿再执行；由 380 设备共享调配替代】
-- 废弃原因：设备申领属于设备科共享调配，不是住院护士给患者选泵即绑定；380 已删除本文件新增的
--           菜单 3780000000000000101 及其按钮/授权，并把绑定来源 3 改为「共享调配」（见 381）。
-- 背景：输液泵绑定/解绑原挂在「监控大屏 · 病区患者监护」卡片上，监控页变成可操作页面，
--       与「监控只读、业务动作在临床工作流里」的口径冲突。
-- 方案：在住院管理下新增「床旁设备申领」，护士按在院患者申领床旁输液泵并绑定；
--       监控大屏只读展示设备与体征，不再提供绑定/解绑按钮。

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3780000000000000101, '床旁设备申领', 300, 55, 2, '/bedside-device', 'inpatient/BedsideDeviceView',
       'ipd.bedsideDevice', 'FirstAidKit', 'ipd:bedsideDevice:list', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '在院患者床旁输液泵申领、绑定与回收',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.path = '/bedside-device');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3780000000000000102, '床旁设备-申领', 3780000000000000101, 1, 3, null, null,
       'ipd.bedsideDevice:apply', null, 'ipd:bedsideDevice:apply', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '申领设备并绑定到在院患者',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.permission = 'ipd:bedsideDevice:apply');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3780000000000000103, '床旁设备-回收', 3780000000000000101, 2, 3, null, null,
       'ipd.bedsideDevice:recycle', null, 'ipd:bedsideDevice:recycle', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '治疗结束/换泵时回收设备',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.permission = 'ipd:bedsideDevice:recycle');

-- 授权：护士站相关角色（系统管理员/护士/护士长/护理部主任/科护士长）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000201, 1, 3780000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 3780000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000202, 3, 3780000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 3 AND rm.menu_id = 3780000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000203, 10, 3780000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 10 AND rm.menu_id = 3780000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000204, 29, 3780000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 29 AND rm.menu_id = 3780000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000205, 30, 3780000000000000101, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 30 AND rm.menu_id = 3780000000000000101);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000206, 1, 3780000000000000102, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 3780000000000000102);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000207, 3, 3780000000000000102, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 3 AND rm.menu_id = 3780000000000000102);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000208, 10, 3780000000000000102, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 10 AND rm.menu_id = 3780000000000000102);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000209, 29, 3780000000000000102, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 29 AND rm.menu_id = 3780000000000000102);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000210, 30, 3780000000000000102, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 30 AND rm.menu_id = 3780000000000000102);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000211, 1, 3780000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 3780000000000000103);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000212, 3, 3780000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 3 AND rm.menu_id = 3780000000000000103);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000213, 10, 3780000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 10 AND rm.menu_id = 3780000000000000103);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000214, 29, 3780000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 29 AND rm.menu_id = 3780000000000000103);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT 3780000000000000215, 30, 3780000000000000103, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 30 AND rm.menu_id = 3780000000000000103);
