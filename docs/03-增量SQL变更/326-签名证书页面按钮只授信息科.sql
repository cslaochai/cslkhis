-- ============================================================================
-- 326-签名证书页面（菜单 29621）及其全部按钮只授信息科管理员（role 33）
-- ============================================================================
-- 背景（2026-11-04 定的口径）：签名证书台账页 /sign-cert 的所有操作按钮
--   （详情、进度、回填、重新调用 CA、重新签发、履历、吊销）只给信息科，
--   不再给医务科、超管及其他任何角色。
--
-- 现状（铺底 + 309 + 313 + 319 叠加后的授权）：
--   菜单 29621（sign:cert:list）  ：role 1 超管、role 33 信息科
--   按钮 29623（sign:cert:edit）  ：role 1 超管、role 33 信息科
--   按钮 29625（sign:cert:revoke）：role 26 医务科
--
-- 改动：
--   1) 删除除 role 33 之外所有角色对 29621 / 29623 / 29625 的授权；
--   2) 给 role 33 补授 29625（吊销处置），使吊销按钮同样落在信息科手里；
--   3) 29621 / 29623 / 29625 三个菜单行本身不动（permission 码不变，后端接口不动）。
--
-- ⚠ 本条覆盖 sql309 / sql319 的「办理权归信息科、处置权归医务科」分权口径：
--   按新口径处置权也归信息科，故 role 26 的 29625 一并收回。
--   后端 /emr/signCert/revoke 仍是 hasAuthority('sign:cert:revoke')，
--   信息科持有该权限后即可吊销，无需改代码。
--
-- ⚠ 执行后必须清权限/身份缓存（否则旧权限仍在内存里）：
--     redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli DEL
--     redis-cli --scan --pattern 'his:user:identity:*' | xargs redis-cli DEL
--   幂等：先删后插，可重复执行。
-- 回滚：按下方「回滚」段恢复 309/319 口径（role 1 持 29621/29623、role 26 持 29625）。
-- ============================================================================

-- ---------------- 前置核对（看现在到底谁有这三个权限） ----------------
SELECT rm.role_id, r.role_name, rm.menu_id, m.menu_name, m.permission
FROM sys_role_menu rm
         LEFT JOIN sys_role r ON r.id = rm.role_id
         LEFT JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.menu_id IN (29621, 29623, 29625)
ORDER BY rm.menu_id, rm.role_id;

-- ---------------- 0. 确保「吊销处置」按钮菜单存在（没跑过 309 的环境） ----------------
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
SELECT 29625, 29621, '签名证书-吊销处置', 2, 3, NULL, NULL, 'sign.cert.revoke', NULL,
       'sign:cert:revoke', 0, 1, 0, 1, 'sql326', NOW(), 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29625);

-- ---------------- 1. 收回：除信息科（role 33）外一律删除 ----------------
DELETE FROM sys_role_menu
WHERE menu_id IN (29621, 29623, 29625)
  AND role_id <> 33;

-- ---------------- 2. 补授：信息科管理员持有整页 + 全部按钮 ----------------
-- 29621 签名证书（页面，sign:cert:list）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892611104000000001, 33, 29621, 'sql326', NOW(), 'sql326', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 33 AND menu_id = 29621);

-- 29623 签名证书-办理（sign:cert:edit：办理/回填/重试CA/重新签发）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892611104000000002, 33, 29623, 'sql326', NOW(), 'sql326', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 33 AND menu_id = 29623);

-- 29625 签名证书-吊销处置（sign:cert:revoke）：新口径下也归信息科
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892611104000000003, 33, 29625, 'sql326', NOW(), 'sql326', NOW(), 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 33 AND menu_id = 29625);

-- ---------------- 3. 角色备注同步新口径（原备注写着「吊销归医务科」，已作废） ----------------
UPDATE sys_role
SET remark     = '信息科固定岗：签名证书台账与全部按钮（办理/回填/重新调用CA/重新签发/吊销）只授本角色',
    update_by  = 'sql326',
    update_time = NOW()
WHERE id = 33;

-- ---------------- 4. 回查（期望：只有 role 33 的 3 行） ----------------
SELECT rm.role_id, r.role_name, rm.menu_id, m.menu_name, m.permission
FROM sys_role_menu rm
         LEFT JOIN sys_role r ON r.id = rm.role_id
         LEFT JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.menu_id IN (29621, 29623, 29625)
ORDER BY rm.menu_id, rm.role_id;

-- ---------------- 回滚（恢复 sql309/sql319 口径，仅在需要时执行） ----------------
-- DELETE FROM sys_role_menu WHERE role_id = 33 AND menu_id = 29625;
-- INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
-- VALUES (892611104000000004, 1, 29621, 'rollback', NOW(), 'rollback', NOW(), 1, 1),
--        (892611104000000005, 1, 29623, 'rollback', NOW(), 'rollback', NOW(), 1, 1),
--        (892611104000000006, 26, 29625, 'rollback', NOW(), 'rollback', NOW(), 1, 1);
