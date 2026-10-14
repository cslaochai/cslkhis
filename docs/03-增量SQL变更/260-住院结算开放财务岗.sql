-- 住院结算（菜单 305）对财务岗开放：补齐本页面自己的按钮码并授权
-- 为什么：住院结算页原先只有 系统管理员(1) 持有 305，收费员(4)/医保结算员(16) 侧边栏画不出这一项，
--   只能看到「收费结算窗口」(1001)。而后端注释早已声明设计意图是「住院结算员只挂 305 菜单即可」，
--   属铺底漏项 —— 全仓增量 SQL 从未出现过 ipd:settlement 这个权限码。
-- 为什么还要新建按钮码：305 名下原先一个 menu_type=3 节点都没有，而该页两个动作是写接口
--   （收预交金 / 办理出院结算，自费与医保同一个入口，靠 settleMode 1-自费 2-医保 分流），
--   原先只认 ipd:inpatient:add / ipd:inpatient:edit —— 这两个码挂在「入出院管理」(301) 名下，
--   授给财务岗等于借医护页面的码，日后想单独收回住院结算的写权限会连带影响入出院管理。
--   故建 305 专属码 ipd:settlement:add / ipd:settlement:edit，后端改 hasAnyAuthority 两侧都认。
-- 报盘链路不在本文件内：医保住院结算的 2304 上传 / 2305 撤销走 /charge/settlement/*，
--   权限码 finance:insurance:edit（菜单 1003），医保结算员(16) 已持有，收费员(4) 按岗位分工不放开。
-- 前置：menu_key 有唯一键 uk_menu_key，sys_menu 主键 id；授权表唯一键 uk_role_menu(role_id, menu_id) 不含 del_flag，
--   删除即物理删，故授权用 ON DUPLICATE KEY UPDATE 兜重复执行。
-- id 段：菜单用 3051/3052（本文件专属，紧邻宿主菜单 305）；授权用 8920610140000000xxx（2026-10-14 专属段）。
-- 执行后必须清 Redis：DEL his:perm:role:4 his:perm:role:16（不清则 10 分钟内仍是旧权限集合）

-- 1) 305 的两个按钮码（三道键判重：id / menu_key / permission）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time, del_flag,
                      create_by_id, update_by_id)
SELECT 3051, '住院结算-收预交金', 305, 1, 3, NULL, NULL, 'ipd.settlement:add', NULL, 'ipd:settlement:add',
       0, 1, 0, 1, 'settle-perm-20261014', NOW(), 'settle-perm-20261014', NOW(), 0,
       1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m
                  WHERE m.id = 3051 OR m.menu_key = 'ipd.settlement:add' OR m.permission = 'ipd:settlement:add');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time, del_flag,
                      create_by_id, update_by_id)
SELECT 3052, '住院结算-出院结算', 305, 2, 3, NULL, NULL, 'ipd.settlement:edit', NULL, 'ipd:settlement:edit',
       0, 1, 0, 1, 'settle-perm-20261014', NOW(), 'settle-perm-20261014', NOW(), 0,
       1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m
                  WHERE m.id = 3052 OR m.menu_key = 'ipd.settlement:edit' OR m.permission = 'ipd:settlement:edit');

-- 2) 授权：收费员(4) + 医保结算员(16) 各得 305（读）/ 3051（收预交金）/ 3052（出院结算）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610140000000001, 4, 305, 'settle-perm-20261014', 1, NOW(), 'settle-perm-20261014', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610140000000002, 4, 3051, 'settle-perm-20261014', 1, NOW(), 'settle-perm-20261014', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610140000000003, 4, 3052, 'settle-perm-20261014', 1, NOW(), 'settle-perm-20261014', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610140000000004, 16, 305, 'settle-perm-20261014', 1, NOW(), 'settle-perm-20261014', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610140000000005, 16, 3051, 'settle-perm-20261014', 1, NOW(), 'settle-perm-20261014', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610140000000006, 16, 3052, 'settle-perm-20261014', 1, NOW(), 'settle-perm-20261014', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

-- 3) 回查：按钮应 2 行（3051/3052，parent=305）
SELECT id, menu_name, parent_id, menu_type, menu_key, permission, status, del_flag
FROM sys_menu
WHERE id IN (3051, 3052);

-- 4) 回查：授权应 6 行
SELECT r.role_code, rm.menu_id, m.menu_name, m.permission
FROM sys_role_menu rm
         JOIN sys_role r ON r.id = rm.role_id
         JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.menu_id IN (305, 3051, 3052)
ORDER BY r.role_code, rm.menu_id;
