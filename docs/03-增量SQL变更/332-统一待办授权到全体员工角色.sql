-- ============================================================================
-- 332 统一待办（29654）授权到全体员工角色（A 方案配套）
--
-- 背景：331 把消息中心（102）改成「只查不办」，待办型消息行给「到待办办理」跳到 /todos。
--       （334 起统一待办改为单表 + 数据范围查询条件，该跳转链接已移除，本条背景仅作历史记录。）
--       但 29654 仍只授 role 1，其余持有 102 的角色点了会被路由守卫拦
--       （guard 按「path 在不在自己菜单树里」判权）= 死链。
--       要落实「统一待办是唯一办理台」，所有持有消息中心的员工角色必须都能进。
--
-- 授权口径：
--   授 = 持有 102（消息中心）且 role_code <> 'PATIENT' 的角色
--   不授 = 患者角色 19（PATIENT，患者端不是员工待办工作台）
--   不含 29642（workflow:task:handle）：流程待办处理权只给特定岗位，本文件不扩大审批权
--
-- 行为中性：这批角色原本就在消息中心有「处理」（portal:messages:edit 2001 已授 30 个角色），
--           本文件只是把办理入口从 102 搬到 29654，不新增任何人的办事能力。
--
-- ⚠ 执行后清角色权限缓存：node workspace/_clear_perm_cache.mjs
-- ⚠ 残留边界：患者角色 19 仍持有 102 但不授 29654。患者端（his-ms-app 小程序）不复用本页面；
--   若将来患者真在员工后台登录，消息中心的「到待办办理」点过去会被 guard 拦（无 /todos 菜单），
--   届时二选一：给该链接加「有菜单才渲染」的条件，或单独收回患者角色的 102。
-- 回滚：
--   DELETE rm FROM sys_role_menu rm WHERE rm.menu_id = 29654 AND rm.role_id <> 1;
-- ============================================================================

-- ---------- 1. 补授 29654 ----------
-- id = 当前 MAX(id) + 100 + 行号（INSERT...SELECT 先物化再写表，MAX 取插入前快照）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                           create_by_id, update_by_id)
SELECT (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu) + 100
           + ROW_NUMBER() OVER (ORDER BY rm.role_id),
       rm.role_id, 29654, 'sql332', NOW(), 'sql332', NOW(), 1, 1
FROM sys_role_menu rm
         JOIN sys_role r ON r.id = rm.role_id
WHERE rm.menu_id = 102
  AND r.role_code <> 'PATIENT'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = rm.role_id AND b.menu_id = 29654)
GROUP BY rm.role_id;

-- ---------- 2. 回查 ----------
-- 2.1 已授 29654 的角色（期望 32 行 = role 1 + 31 个员工角色）
SELECT COUNT(*) AS granted_roles
FROM sys_role_menu
WHERE menu_id = 29654;

-- 2.2 仍持有 102 但进不了办理台的角色（期望只有患者 19）
SELECT rm.role_id, r.role_name, r.role_code
FROM sys_role_menu rm
         JOIN sys_role r ON r.id = rm.role_id
WHERE rm.menu_id = 102
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = rm.role_id AND b.menu_id = 29654)
ORDER BY rm.role_id;

-- 2.3 护栏：患者角色不应被授 29654（期望 0 行）
SELECT rm.role_id
FROM sys_role_menu rm
         JOIN sys_role r ON r.id = rm.role_id
WHERE rm.menu_id = 29654
  AND r.role_code = 'PATIENT';
