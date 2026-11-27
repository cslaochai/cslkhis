-- =====================================================================
-- 20261127 分诊护士拆门诊与急诊两个角色 —— 回滚
-- ---------------------------------------------------------------------
-- 回滚到「role 9 分诊护士 同时持门诊+急诊」的现状（含 2011 的历史漂移）。
-- 顺序：岗位改回 → 收回 role 44 授权 → 恢复 role 9 急诊授权 → role 9 定名 → 删角色。
-- 幂等：每步都可重复执行。
-- =====================================================================

-- ---------- ① 急诊三科岗位改回 role 9 ----------
UPDATE sys_employee_post
   SET role_id = 9,
       update_by = 'admin',
       update_time = NOW(),
       update_by_id = 1
 WHERE role_id = 44
   AND dept_id IN (19580048, 19580049, 19580051);

-- ---------- ② 收回 role 44 授权 ----------
DELETE FROM sys_role_menu WHERE role_id = 44;

-- ---------- ③ 恢复 role 9 急诊授权（原 ID 段逐字复原） ----------
INSERT INTO sys_role_menu
  (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`,
   `create_by_id`, `update_by_id`)
SELECT v.id, 9, v.menu_id, 'admin', NOW(), 'admin', NOW(), 1, 1
FROM (
            SELECT 8920610200000000101 AS id, 205  AS menu_id
  UNION ALL SELECT 8920610200000000102, 2010
  UNION ALL SELECT 8920610230000000001, 2011
  UNION ALL SELECT 8920610230000000301, 2607
) v
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 9 AND rm.menu_id = v.menu_id
);

-- ---------- ④ role 9 定名复原 ----------
UPDATE sys_role
   SET role_name = '分诊护士',
       remark = '急诊预检分诊岗：负责预检分诊、分级判定、急诊挂号与就诊秩序、门诊日志；不执行医嘱。',
       update_by = 'admin',
       update_time = NOW()
 WHERE id = 9 AND role_code = '10020';

-- ---------- ⑤ 删 role 44（仅在无岗位、无授权时才删，避免误删现场） ----------
DELETE FROM sys_role
 WHERE id = 44
   AND role_code = '10054'
   AND NOT EXISTS (SELECT 1 FROM sys_employee_post ep WHERE ep.role_id = 44)
   AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 44);

-- 回滚后：清 Redis his:perm:role:*；受影响人员重新登录。
