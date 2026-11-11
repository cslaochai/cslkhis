-- ============================================================
-- 20261110_药事管理菜单分组：仅补授权（C 段独立版）
-- 用途：当 A（建 6 目录）+ B（21 叶子改归属）已执行、只差授权时使用。
--       也可在跑完主文件 20261110_药事管理菜单分组重组.sql 后单独补跑，幂等。
-- 背景：6 个二级目录 29800~29805 必须显式授权，否则：
--   ① 菜单树按「员工+当前角色」经 sys_role_menu+sys_employee_post 取菜单（直查库，不走 his:perm:role 缓存）；
--   ② 目录没授权→该角色看不到→其下 21 个功能挂在没有权限的“爸爸”下成孤儿→整支被隐藏。
-- 修复点：用 UNION（非 UNION ALL）批内去重，避免 admin 兼子菜单拥有者时 (1,目录) 自重复撞 uk_role_menu。
-- ============================================================

-- 1) 给 6 个二级目录补授权：admin(role_id=1,系统管理员) + 拥有其任一子菜单的所有角色（幂等）
SET @rm_id := (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 29800 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29800 FROM sys_role_menu rm WHERE rm.menu_id IN (507,505,506,512,513,501,510,514)
    UNION SELECT 1 AS role_id, 29801 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29801 FROM sys_role_menu rm WHERE rm.menu_id IN (502,508,511,504)
    UNION SELECT 1 AS role_id, 29802 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29802 FROM sys_role_menu rm WHERE rm.menu_id IN (509)
    UNION SELECT 1 AS role_id, 29803 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29803 FROM sys_role_menu rm WHERE rm.menu_id IN (503,515,519,516)
    UNION SELECT 1 AS role_id, 29804 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29804 FROM sys_role_menu rm WHERE rm.menu_id IN (517,2922,518)
    UNION SELECT 1 AS role_id, 29805 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29805 FROM sys_role_menu rm WHERE rm.menu_id IN (1107)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- 2) 验证：admin(1) 是否拿到 6 个目录授权，以及各目录还授给了哪些角色
-- SELECT rm.role_id, m.menu_name AS 目录, COUNT(*) OVER (PARTITION BY rm.menu_id) AS 授给角色数
-- FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
-- WHERE rm.menu_id IN (29800,29801,29802,29803,29804,29805)
-- ORDER BY rm.menu_id, rm.role_id;
-- 期望：role_id=1 在 6 行都出现（admin 可见 6 个分组），且每个目录还包含原拥有子菜单的角色。

-- 3) 前端刷新：userMenus 直查库，无需清 his:perm:role 缓存；但前端菜单常登录时拉一次并缓存于 store，
--    请「重新登录」或「硬刷新(Ctrl+F5)」让左侧栏重新取 userMenus。
