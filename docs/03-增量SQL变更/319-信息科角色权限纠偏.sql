-- ============================================================================
-- 319-信息科管理员（role 33）权限纠偏：收回吊销处置，补回消息待办
-- ============================================================================
-- 背景：313 给 role 33 的授权是 100/101/102/800/29621/29623 六行
--   （门户三件 + 组织与资源 + 签名证书 + 办理按钮），铺底数据也是这个口径。
--   但 dev 库实际变成 100/101/800/29621/29623/29625——
--   102（消息待办）被删、29625（签名证书-吊销处置）被加，
--   痕迹是角色管理页面手工编辑保存（全量重写 sys_role_menu）所致。
--
--   29625（sign:cert:revoke）按分权硬约束只能授医务科（role 26，sql309），
--   信息科「办理权」与医务科「处置权」必须分离，本次收回；
--   102 消息待办是登录后的基础工作入口，补回。
--
-- 幂等：先删后插，重复执行结果一致。
-- ⚠ 执行后清权限/身份缓存：
--     redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli DEL
--     redis-cli --scan --pattern 'his:user:identity:*' | xargs redis-cli DEL
-- 回滚：手工在角色管理页重授（不推荐，会再次破坏分权）。
-- ============================================================================

-- ---------------- 前置核对（期望：有 29625 越权行，缺 102） ----------------
SELECT rm.menu_id, m.menu_name, m.permission
FROM sys_role_menu rm LEFT JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.role_id = 33 ORDER BY rm.menu_id;

-- ---------------- 纠偏：删越权 29625，补 102 ----------------
DELETE FROM sys_role_menu WHERE role_id = 33 AND menu_id = 29625;

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892611012000000003, 33, 102, 'sql319', NOW(), 'sql319', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 33 AND menu_id = 102);

-- ---------------- 回查（期望 6 行：100/101/102/800/29621/29623，无 29625） ----------------
SELECT rm.menu_id, m.menu_name, m.permission
FROM sys_role_menu rm LEFT JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.role_id = 33 ORDER BY rm.menu_id;
