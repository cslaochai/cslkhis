-- ============================================================================
-- 327-故障演练菜单（顶级目录 + 演练控制台，仅授系统管理员 role_id=1）
-- 仅留档，不要在 HIS 执行；HIS 的 29910/29911 菜单与授权已移除。
-- ============================================================================
-- 背景（2026-11-13）：
--   csa-fault 演练进程（127.0.0.1:18090）已提供 10 个故障场景，
--   现在要给系统管理员一个前端入口：侧边栏「故障演练」→「演练控制台」，
--   页面里每张卡片点击即触发对应故障。
--
-- 只授 role_id=1（role_code=10012 系统管理员）：
--   sys_menu 的可见性完全由 sys_role_menu 决定（/system/menu/userMenus
--   按 employee + 当前角色过滤），不给其他角色插关联行 → 其他角色天然看不到，
--   手敲 /fault-lab 也会被 router/guard.js 的「不在菜单即无权」收敛回工作台。
--
-- id 段：29910 / 29911 为本文件专属（插入前已核对 max(sys_menu.id)=29805，
--   且 29910/29911 均未被占用）。守卫按 id / menu_key / permission 三道键判重。
--
-- ⚠ 执行后清角色权限缓存（新增权限码 fault:lab:view 仍在旧缓存里）：
--     redis-cli -h 192.168.88.132 -a 123456 --scan --pattern 'his:perm:role:*'
--     redis-cli -h 192.168.88.132 -a 123456 --scan --pattern 'his:user:identity:*'
--   逐键 DEL；或直接重新登录（前端会重拉 /auth/info）。
--
-- 回滚：
--   DELETE FROM sys_role_menu WHERE menu_id IN (29910, 29911);
--   DELETE FROM sys_menu WHERE id IN (29910, 29911);
-- ============================================================================

-- ---------------- 1. 顶级目录「故障演练」 ----------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time,
                      update_by, update_time, del_flag, remark)
SELECT 29910, '故障演练', 0, 160, 1, '/fault-lab-group', NULL, 'faultLab', 'Cpu',
       NULL, 0, 1, 1, 1, 'sql327', NOW(), 'sql327', NOW(), 0, '故障演练沙箱入口（仅系统管理员可见）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29910)
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'faultLab')
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission = 'fault:lab:view');

-- ---------------- 2. 页面「演练控制台」 ----------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time,
                      update_by, update_time, del_flag, remark)
SELECT 29911, '演练控制台', 29910, 1, 2, '/fault-lab', 'fault-lab/FaultLabView', 'faultLab.console', 'Aim',
       'fault:lab:view', 0, 1, 1, 1, 'sql327', NOW(), 'sql327', NOW(), 0, '故障场景卡片页，点击触发故障'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29911)
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_key = 'faultLab.console')
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = '/fault-lab');

-- ---------------- 3. 只授系统管理员（role_id=1） ----------------
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 893271113000000001, 1, 29910, 'sql327', NOW(), 'sql327', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 29910);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 893271113000000002, 1, 29911, 'sql327', NOW(), 'sql327', NOW(), 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 29911);

-- ---------------- 4. 回查（期望：sys_menu 2 行；sys_role_menu 只有 role 1 的 2 行） ----------------
SELECT m.id, m.menu_name, m.parent_id, m.menu_type, m.path, m.menu_key, m.permission
FROM sys_menu m
WHERE m.id IN (29910, 29911);

SELECT rm.role_id, r.role_name, rm.menu_id, m.menu_name, m.permission
FROM sys_role_menu rm
         LEFT JOIN sys_role r ON r.id = rm.role_id
         LEFT JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.menu_id IN (29910, 29911)
ORDER BY rm.menu_id, rm.role_id;
