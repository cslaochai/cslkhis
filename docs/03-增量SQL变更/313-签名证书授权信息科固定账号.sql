-- ============================================================================
-- 313-签名证书授权信息科固定账号（新增「信息科管理员」角色，绑蒋俊杰）
-- ============================================================================
-- 背景：sql308 把签名证书(29621, sign:cert:list) / 办理按钮(29623, sign:cert:edit)
--   只授了 role 1 系统管理员，签名证书页实际只有超管能进。证书办理（生成密钥对 /
--   CA 在线申请 / 状态刷新 / 证书回填）是信息科的日常运维职责，不该借超管账号做。
--   本次落地「信息科有固定账号管证书」：
--     1) 新增角色 33「信息科管理员」(role_code=10043)，授 门户/工作台/消息待办(登录必需)
--        + 组织与资源(800, 祖先目录) + 签名证书(29621) + 签名证书-办理(29623)。
--     2) 吊销处置(29625, sign:cert:revoke) 仍只授医务科(role 26, sql309)，不授信息科——
--        办理权与处置权分离原则不变。
--     3) 固定账号 = 蒋俊杰(jiangjunjie, employee 897001446 / user 897003446)：
--        原为 信息科·行政人员(role 23，无任何业务页面)，主岗位改挂「信息科管理员」角色，
--        登录默认岗即带签名证书页。
--
-- 权限对照（SignatureController 端点级注解）：
--   台账分页/详情/按员工查询/CA状态 ← sign:cert:list（29621 已授）
--   生成密钥对/CA申请/刷新/回填     ← sign:cert:edit（29623 已授）
--   吊销                           ← sign:cert:revoke（29625 不授，医务科）
--
-- 幂等：角色按 role_code 判存在；授权与岗位先删后插。
-- ⚠ 运维注意：执行后必须清权限 Redis 缓存（或重启后端），否则旧权限仍在：
--     redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli DEL
--     redis-cli --scan --pattern 'his:user:identity:*' | xargs redis-cli DEL
-- 回滚：DELETE FROM sys_employee_post WHERE employee_id = 897001446 AND role_id = 33;
--      DELETE FROM sys_role_menu     WHERE role_id = 33;
--      DELETE FROM sys_role          WHERE id = 33;
--      -- 并把蒋俊杰主岗位恢复为 role 23 行政人员
-- ============================================================================

-- ---------------- 1. 新角色：信息科管理员 ----------------
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 33, '10043', '信息科管理员', 1, 6, 1, 43, 1,
       'sql313', NOW(), 'sql313', NOW(), 0,
       '信息科固定岗：签名证书台账查询与办理（CA在线申请/回填）；吊销处置归医务科（分权）', 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = '10043');

-- ---------------- 2. 菜单授权（先删后插，保证幂等） ----------------
DELETE FROM sys_role_menu WHERE role_id = 33;

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (892611012000000001, 33, 100,   'sql313', NOW(), 'sql313', NOW(), 1, 1),
  (892611012000000002, 33, 101,   'sql313', NOW(), 'sql313', NOW(), 1, 1),
  (892611012000000003, 33, 102,   'sql313', NOW(), 'sql313', NOW(), 1, 1),
  (892611012000000004, 33, 800,   'sql313', NOW(), 'sql313', NOW(), 1, 1),
  (892611012000000005, 33, 29621, 'sql313', NOW(), 'sql313', NOW(), 1, 1),
  (892611012000000006, 33, 29623, 'sql313', NOW(), 'sql313', NOW(), 1, 1);

-- ---------------- 3. 固定账号绑岗：蒋俊杰 主岗位 行政人员(23) → 信息科管理员(33) ----------------
DELETE FROM sys_employee_post
WHERE employee_id = 897001446 AND role_id IN (23, 33) AND dept_id = 19580081;

INSERT INTO sys_employee_post (id, employee_id, role_id, dept_id, is_primary, effective_date, expire_date,
                               create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
  (892611012000000101, 897001446, 33, 19580081, 1, '2017-06-01', NULL, 'sql313', NOW(), 'sql313', NOW(), 1, 1);

-- ---------------- 4. 自检 ----------------
SELECT id, role_code, role_name, data_scope, status FROM sys_role WHERE id = 33;
SELECT role_id, menu_id FROM sys_role_menu WHERE role_id = 33;  -- 期望 6 行
SELECT employee_id, role_id, dept_id, is_primary FROM sys_employee_post WHERE employee_id = 897001446;  -- 期望 1 行：role 33 主岗
