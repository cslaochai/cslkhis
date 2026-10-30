-- ============================================================================
-- 309-证书吊销拆独立权限 sign:cert:revoke（只授医务科）
-- ============================================================================
-- 背景：吊销原来混在 sign:cert:edit（按钮 29623「签名证书-办理吊销」）里，
--   能办证的管理员就能随手吊销医生证书，吊销直接改变医生结诊链路（无签结诊 →
--   病历待补签、归档/发药被拦），是处置权不是办理权，不能跟 keygen/bind 同权。
--
-- 改动：
--   1) 后端 SignatureController /emr/signCert/revoke 从 hasAuthority(sign:cert:edit)
--      改为 hasAuthority(sign:cert:revoke)（同批代码改动）。
--   2) 新增按钮 29625「签名证书-吊销处置」（挂 29621 签名证书下），permission=sign:cert:revoke。
--   3) 29623 按钮改名「签名证书-办理」（keygen/bind 仍走 sign:cert:edit）。
--   4) 授权：只授医务科（sys_role.role_id=26，role_code 10036）；admin 不授——
--      分权原则：处置权不进超管日常权柄，需要时由医务科账号操作。
--   5) 配套：无签结诊的后台预警接收人 = 持有 sign:cert:revoke 的人员
--      （EmrServiceImpl.sendUnsignedSignAlert 经 SystemFacadeService.listEmployeesByPermission 圈定），
--      即预警天然只发医务科，无需另配接收人。
--
-- ⚠ 运维注意（同 308）：
--   执行后必须清权限 Redis 缓存（或重启后端），否则旧权限仍在：
--     redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli DEL
--     redis-cli --scan --pattern 'his:user:identity:*' | xargs redis-cli DEL
--   本文件幂等：29625 按 ID 物理删再插；29623 改名用 UPDATE（可重复执行）。
-- ============================================================================

-- ---------- 1. 新按钮：签名证书-吊销处置（幂等清场后重插） ----------
DELETE FROM sys_role_menu WHERE menu_id = 29625;
DELETE FROM sys_menu WHERE id = 29625;

INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
VALUES (29625, 29621, '签名证书-吊销处置', 2, 3, NULL, NULL, 'sign.cert.revoke',
        NULL, 'sign:cert:revoke', 0, 1, 0, 1, 'admin', NOW(), 0);

-- ---------- 2. 29623 按钮摘掉「吊销」字样（keygen/bind 仍在此权限下） ----------
UPDATE sys_menu SET menu_name = '签名证书-办理' WHERE id = 29623 AND menu_name = '签名证书-办理吊销';

-- ---------- 3. 授权：只授医务科（role_id=26），admin 不授 ----------
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
VALUES (8926103030000000001, 26, 29625);

-- ---------- 回查 ----------
-- SELECT id, menu_name, permission FROM sys_menu WHERE id IN (29623, 29625) AND del_flag = 0;
-- SELECT rm.role_id, r.role_name FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id
--   WHERE rm.menu_id = 29625;  -- 应只有一行：26 医务科
