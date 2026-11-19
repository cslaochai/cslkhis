-- ============================================================================
-- 335 门户入口区授权补齐（工作台 / 统一待办 / 急诊值班）
--
-- 口径（用户明确要求）：门户下这三个菜单是所有登录用户都必须有的固定入口，不按权限控制。
--   代码层已兜底：SysMenuServiceImpl#mergeWithPortalEntries 把门户(100)下 is_visible=1 的
--   可见菜单不论角色授权一律并入 userMenus（新增角色无需再跑 SQL 也能看见）。
--   本文件把数据侧补齐，目的只有一个：让「角色管理→菜单权限」的勾选状态与运行结果一致，
--   否则管理员会看到「急诊值班未勾选但用户能用」这种对不上的界面。
--
-- 现状：101 工作台 35/35、29654 统一待办 35/35；29605 急诊值班缺 5 个角色。
-- 本文件只补 29605，不碰 101 / 29654 / 102 / 2001。
--
-- ⚠ 执行后清角色权限缓存：node workspace/_clear_perm_cache.mjs
-- 回滚：按回查 2.2 的 role_id 列表逐条删除即可（本文件只新增、不改已有行）。
-- ============================================================================

-- ---------- 1. 补齐 29605 到全部启用角色 ----------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu) + 100
           + ROW_NUMBER() OVER (ORDER BY r.id),
       r.id, 29605, 'sql335', NOW(), 'sql335', NOW(), 1, 1
FROM sys_role r
WHERE r.del_flag = 0
  AND r.status = 1
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = r.id AND b.menu_id = 29605);

-- ---------- 2. 回查 ----------
-- 2.1 三个入口菜单的授权数（期望都等于启用角色数 35）
SELECT menu_id, COUNT(*) AS granted_roles
FROM sys_role_menu
WHERE menu_id IN (101, 29605, 29654)
GROUP BY menu_id
ORDER BY menu_id;

-- 2.2 仍缺任一入口菜单的角色（期望 0 行）
SELECT r.id, r.role_name
FROM sys_role r
WHERE r.del_flag = 0
  AND r.status = 1
  AND (NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = r.id AND b.menu_id = 101)
    OR NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = r.id AND b.menu_id = 29605)
    OR NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = r.id AND b.menu_id = 29654))
ORDER BY r.id;
