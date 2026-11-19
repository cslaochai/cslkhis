-- ============================================================================
-- 334 统一待办（29654）授权到全部角色
--
-- 背景：用户要求「统一待办要给所有角色都展示」。332 只铺了「持有 102 的员工角色」，
--       仍有 3 个角色进不去：19 患者 / 34 护理调配员 / 35 医务调配员
--       （34、35 是后来新建的调配员角色，从未持有 102）。
--
-- 本次口径 = 全部启用角色（sys_role.del_flag = 0 且 status = 1），不再按「持有 102」推导。
--   行为中性：页面可见 ≠ 能办事。待办里的「处理」按钮仍按 2001（portal:messages:edit）与
--   29642（workflow:task:handle）上锁，本文件不新增任何人的处置权限。
--   患者角色 19 从此也能看到「统一待办」菜单：患者端在小程序（his-ms-app）不复用本页；
--   若患者真在员工后台登录，他名下通常没有消息，列表为空，不会看到别人的数据。
--
-- 不含 29642（流程待办处理权）：流程待办来源只在持 29642 的角色下出现，本次不扩大审批权。
--
-- ⚠ 执行后清角色权限缓存：node workspace/_clear_perm_cache.mjs
-- 回滚：
--   DELETE rm FROM sys_role_menu rm
--    WHERE rm.menu_id = 29654 AND rm.role_id IN (19, 34, 35);
-- ============================================================================

-- ---------- 1. 给所有未授 29654 的启用角色补授 ----------
-- id = 当前 MAX(id) + 100 + 行号（子查询取插入前快照，与 332 同一写法）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu) + 100
           + ROW_NUMBER() OVER (ORDER BY r.id),
       r.id, 29654, 'sql334', NOW(), 'sql334', NOW(), 1, 1
FROM sys_role r
WHERE r.del_flag = 0
  AND r.status = 1
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = r.id AND b.menu_id = 29654);

-- ---------- 2. 回查 ----------
-- 2.1 已授 29654 的角色数（期望 = 启用角色总数 35）
SELECT COUNT(*) AS granted_roles FROM sys_role_menu WHERE menu_id = 29654;

-- 2.2 启用但还没授 29654 的角色（期望 0 行）
SELECT r.id, r.role_name, r.role_code
FROM sys_role r
WHERE r.del_flag = 0
  AND r.status = 1
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = r.id AND b.menu_id = 29654)
ORDER BY r.id;

-- 2.3 护栏：处理权限没被一起放开（29642 授权数应保持 1，2001 授权数应保持 31）
SELECT (SELECT COUNT(*) FROM sys_role_menu WHERE menu_id = 29642) AS granted_29642,
       (SELECT COUNT(*) FROM sys_role_menu WHERE menu_id = 2001) AS granted_2001;
