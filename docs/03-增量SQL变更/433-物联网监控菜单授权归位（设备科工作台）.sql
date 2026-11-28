-- =====================================================================
-- 433 物联网监控菜单授权归位（设备科工作台 + 告警中心跨角色闭环）
--
-- 背景：
--   357 物联网设备监控的页面注释口径是「设备科/运维台账总览」（DeviceMonitorView L130），
--   初始授权却挂了护士长(10)/护理部主任(29)，临床管理岗不看全院设备台账总览。
--   386 告警中心是跨角色闭环台（设备科处理设备故障类，护理管理岗确认离位/床旁设备类），
--   初始授权漏授设备与供应室管理员(39)。
--   375 资产定位与维保现状 1/39 已正确，本次不动。
--
-- 授权目标：
--   357 物联网设备监控（只读台账总览）：1 系统管理员、39 设备与供应室管理员；收回 10、29。
--   375 资产定位与维保：1、39（保持现状，仅列入回查）。
--   386 告警中心（设备事件闭环）：1、39、10、29（设备科 + 护理管理岗双角色）。
--
-- 说明：三个菜单均无 menu_type=3 按钮码子菜单，页面授权即全部授权，无需同步收按钮码；
--       377 父目录按子菜单并集可见，10/29 在父目录下仍有患者监护/床位安全，父目录授权不动。
-- 执行后清 Redis 权限缓存：his:perm:role:*（TTL 10 分钟，不重启需手动清键）。
-- =====================================================================

-- 1) 357 物联网设备监控：收回护士长(10)、护理部主任(29)，补设备与供应室管理员(39)
DELETE rm
FROM sys_role_menu rm
WHERE rm.menu_id = 3570000000000000101
  AND rm.role_id IN (10, 29);

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                                  create_by_id, update_by_id)
SELECT 4330000000000000201 AS id, 39 AS role_id, 3570000000000000101 AS menu_id,
       'sql433', NOW(), 'sql433', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = 39 AND rm.menu_id = 3570000000000000101);

-- 2) 386 告警中心：补设备与供应室管理员(39)；1、10、29 保留
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                                  create_by_id, update_by_id)
SELECT 4330000000000000202 AS id, 39 AS role_id, 3860000000000000101 AS menu_id,
       'sql433', NOW(), 'sql433', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = 39 AND rm.menu_id = 3860000000000000101);

-- =====================================================================
-- 回查（跑完手工核对）
-- =====================================================================
-- SELECT m.id, m.menu_name, rm.role_id, r.role_name
--   FROM sys_role_menu rm
--   JOIN sys_role r ON r.id = rm.role_id
--   JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.menu_id IN (3570000000000000101, 3750000000000000101, 3860000000000000101)
--  ORDER BY rm.menu_id, rm.role_id;
-- 预期：
--   357 物联网设备监控：1 系统管理员、39 设备与供应室管理员
--   375 资产定位与维保：1 系统管理员、39 设备与供应室管理员
--   386 告警中心：1 系统管理员、10 护士长、29 护理部主任、39 设备与供应室管理员
