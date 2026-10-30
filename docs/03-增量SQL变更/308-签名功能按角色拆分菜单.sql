-- ============================================================================
-- 308-签名功能按角色拆分菜单（签名中心 → 签名验签 / 签名证书 / 日志审计只读）
-- ============================================================================
-- 背景：原「系统管理 → 电子签名与时间戳」（606）一张页面混装了四类人群的工作：
--   医务/质控要验签作废、人事/管理员要办证书、审计只看不动、科室要查员工证书。
--   本轮按角色拆成三处 + 一处只读入口：
--     1) 签名验签（29620，病历与质量安全 600 下）：概览卡片 + 签名记录列表，
--        可执行验签 / 作废 / 补签（按钮 29622，sign:verify:edit）。
--     2) 签名证书（29621，组织与资源 800 下）：证书台账，可办理 / 回填 / 吊销
--        （按钮 29623，sign:cert:edit），无统计卡。
--     3) 日志审计（1108）加「签名记录」只读 tab：按钮 29624（sign:audit:list），
--        只能看详情，不能操作。
--     4) 员工管理行内「签名设置」只读弹框：读 sign:cert:list 或 org:employee:list。
--
-- 配套后端改动（同一批提交）：SignatureController 类级注解拆到端点级——
--   查询端点 hasAnyAuthority(sign:verify:list / sign:audit:list / pharmacy:prescriptionAudit:list)；
--   summary 只认 sign:verify:list；verify/sign/invalidate 认 sign:verify:edit；
--   verifyByBiz 另放行 pharmacy:prescriptionAudit:list（药房审方在处方详情里验签）；
--   证书查询端点 hasAnyAuthority(sign:cert:list / org:employee:list)；
--   keygen/bind/revoke 认 sign:cert:edit；新增 GET /emr/signCert/getByEmp。
--
-- 权限码映射：sign:center:list → 三拆；sign:center:edit → sign:verify:edit / sign:cert:edit。
--
-- 授权规则（动态复制，不写死角色）：
--   29620/29621 ← 原持 606 的角色；29622/29623 ← 原持 2088 的角色；
--   29624 ← 原持 1108（日志审计）的角色。
--
-- ⚠ 运维注意：
--   1) 执行后必须清权限 Redis 缓存（或重启后端），否则旧权限仍在：
--      redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli DEL
--      redis-cli --scan --pattern 'his:user:identity:*' | xargs redis-cli DEL
--   2) 本文件幂等：菜单按 ID 物理删再插（menu_key UNIQUE 不含 del_flag，软删占键位）；
--      授权 INSERT IGNORE（uk_role_menu 复合键）+ id 显式雪花号段 8926103020*。
-- ============================================================================

-- ---------- 1. 旧菜单 606 + 按钮 2088 物理清场 ----------
DELETE FROM sys_role_menu WHERE menu_id IN (606, 2088);
DELETE FROM sys_menu WHERE id IN (606, 2088);

-- ---------- 2. 新菜单（幂等清场后重插） ----------
DELETE FROM sys_role_menu WHERE menu_id IN (29620, 29621, 29622, 29623, 29624);
DELETE FROM sys_menu WHERE id IN (29620, 29621, 29622, 29623, 29624);

-- 2.1 签名验签：病历与质量安全(600)下，排序接在 616(36) 之后
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
VALUES (29620, 600, '签名验签', 37, 2, '/signature-verify', 'sign/SignatureVerifyView', 'sign.verify',
        'Stamp', 'sign:verify:list', 0, 0, 1, 1, 'admin', NOW(), 0);

-- 2.2 签名证书：组织与资源(800)下，排序接在 802 诊室管理(41) 之后
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
VALUES (29621, 800, '签名证书', 42, 2, '/sign-cert', 'sign/SignatureCertView', 'sign.cert',
        'Key', 'sign:cert:list', 0, 0, 1, 1, 'admin', NOW(), 0);

-- 2.3 操作按钮：验签/作废/补签（挂 29620）、证书办理/回填/吊销（挂 29621）、
--     日志审计只读 tab（挂 1108）
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
VALUES (29622, 29620, '签名验签-验签作废', 1, 3, NULL, NULL, 'sign.verify.edit',
        NULL, 'sign:verify:edit', 0, 1, 0, 1, 'admin', NOW(), 0);

INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
VALUES (29623, 29621, '签名证书-办理吊销', 1, 3, NULL, NULL, 'sign.cert.edit',
        NULL, 'sign:cert:edit', 0, 1, 0, 1, 'admin', NOW(), 0);

INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
VALUES (29624, 1108, '日志审计-签名记录', 1, 3, NULL, NULL, 'sign.audit.list',
        NULL, 'sign:audit:list', 0, 1, 0, 1, 'admin', NOW(), 0);

-- ---------- 3. 授权（动态复制原持有人；admin 兜底全量） ----------
SET @rm_id = 8926103020000000000;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29620 FROM sys_role_menu rm WHERE rm.menu_id = 606;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29621 FROM sys_role_menu rm WHERE rm.menu_id = 606;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29622 FROM sys_role_menu rm WHERE rm.menu_id = 2088;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29623 FROM sys_role_menu rm WHERE rm.menu_id = 2088;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29624 FROM sys_role_menu rm WHERE rm.menu_id = 1108;

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, 1, m.menu_id
FROM (SELECT 29620 menu_id UNION SELECT 29621 UNION SELECT 29622 UNION SELECT 29623 UNION SELECT 29624) m;

-- ---------- 回查 ----------
-- SELECT id, menu_name, parent_id, sort_order, permission FROM sys_menu
--   WHERE id IN (29620,29621,29622,29623,29624) AND del_flag = 0;
-- SELECT menu_id, COUNT(*) FROM sys_role_menu
--   WHERE menu_id IN (29620,29621,29622,29623,29624) GROUP BY menu_id;
-- SELECT COUNT(*) FROM sys_menu WHERE id IN (606, 2088);  -- 应为 0
