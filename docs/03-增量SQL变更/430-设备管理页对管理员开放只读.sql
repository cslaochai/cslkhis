-- 430-设备管理页对系统管理员开放只读（补 902 页面授权）
--
-- 背景：902「设备管理」的页面授权一直只有 39 设备与供应室管理员（428 执行后临床四个角色也收回），
--      管理员(role 1) 从来没有这行 —— 所以「物资设备」下少一个「设备管理」入口。
--      管理员的定位是「能看见全院在发生什么」，不是「替设备科干活」：页面给看，按钮不给。
--
-- 为什么补了也只读：设备管理页的新增/编辑/删除/保养/计量按钮，以及台账里的「共享池 加入/移出」,
--      全挂 2125 asset:equipment:add（入池改的是 sys_equipment.rentable，属资产自身属性），
--      管理员没有这行授权（role 1 的 asset 码里没有 equipment:add / equipment:delete），
--      补 902 只加页面可见性，不会把写操作带出来。
--
-- 执行后清 Redis 权限缓存：his:perm:role:*

-- 1) 补 902 页面授权给 role 1
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8920614300000000001, 1, 902, 'sql430', NOW(), 'sql430', NOW(), 1, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.id = 8920614300000000001 OR (rm.role_id = 1 AND rm.menu_id = 902));

-- 2) 回查：902 现在授给谁
-- SELECT rm.role_id, r.role_name FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id AND r.del_flag = 0
-- WHERE rm.menu_id = 902 ORDER BY rm.role_id;
-- 预期：1 系统管理员、39 设备与供应室管理员

-- 3) 回查：管理员仍然没有设备写的按钮码（只读成立）
-- SELECT rm.role_id, m.permission FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
-- WHERE rm.role_id = 1 AND m.permission IN ('asset:equipment:add', 'asset:equipment:delete');
-- 预期：0 行
