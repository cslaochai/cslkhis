-- =====================================================================
-- 386 告警中心（设备事件查询与处理闭环）
--
-- 依赖：his-ms-iot 模块 biz_iot_event（MQTT 事件落库），接口 /iot/event/listPage、/iot/event/handle。
-- 目的：在监控大屏入口下补一个告警闭环页，设备科/护士长按状态/级别/时间筛选事件，
--       对未处理事件做「已处理 / 忽略」登记。前端文案以后端 VO 为准，不维护第二份字典。
-- 手工执行：需判重（menu_key 唯一、角色授权唯一），可重复跑。
-- 跑完后权限缓存（his:perm:role:*，TTL 10 分钟）不会自动失效，
--   要立即生效请重启服务或删掉对应缓存键。
-- =====================================================================

-- ① 菜单：告警中心（父目录 377 监控大屏，排在设备监控之后）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark,
                      create_by_id, update_by_id)
SELECT 3860000000000000101, '告警中心', 3770000000000000101, 7, 2, '/iot-events',
       'iot/IotEventView', 'asset.iot.event', 'Bell',
       'asset:iot:event:list', 0, 0, 1, 1,
       'admin', NOW(), 'admin', NOW(), 0, '设备告警事件查询与处理闭环（biz_iot_event）',
       0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.menu_key = 'asset.iot.event');

-- ② 角色授权：系统管理员 / 护士长 / 护理部主任（与设备监控页 357 同口径）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT seed.id, seed.role_id, seed.menu_id, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    SELECT 3860000000000000201 AS id, 1 AS role_id, 3860000000000000101 AS menu_id UNION ALL
    SELECT 3860000000000000202, 10, 3860000000000000101 UNION ALL
    SELECT 3860000000000000203, 29, 3860000000000000101
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = seed.role_id AND rm.menu_id = seed.menu_id);

-- =====================================================================
-- 回查（跑完手工核对）
-- =====================================================================
-- SELECT id, menu_name, parent_id, menu_type, path, component, permission, is_visible, status
--   FROM sys_menu WHERE id = 3860000000000000101;
-- SELECT rm.role_id, r.role_name, rm.menu_id
--   FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id
--  WHERE rm.menu_id = 3860000000000000101 ORDER BY rm.role_id;
