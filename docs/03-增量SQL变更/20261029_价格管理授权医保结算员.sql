-- ============================================================================
-- 20261029 价格管理（政策价表）授权给医保结算员（role 16）
-- ============================================================================
-- 背景：诊查费价格已价表化（sys_charge_policy_price），维护入口在前端
--   「财务结算 → 价格管理 → 政策价」页签：
--     页面 1007  perm finance:price:list   （查看价格管理）
--     按钮 2146 perm finance:price:edit   （调价 / 启停政策价，即时生效并写调价历史）
--   此前仅 admin(1) 持有 1007 / 2146，业务角色无法维护政策价。
--   按 20261029 用户定：物价维护权下放给「医保结算员」(role 16)；
--   窗口「收费员」(role 4) 不持有（定价权不放在收费窗口，role 4 仅持财务日结 2147）。
--   目录 1000（财务结算）role 16 已持有（sys_role_menu 16009），无需重复授权。
--
-- 本变更：role 16 新增 1007（价格管理页，list）+ 2146（价格管理-修改，edit）；
--   admin(1) 维持原授权不变。
--
-- ⚠ 运维注意：
--   1) 执行后必须清权限 Redis 缓存，否则 10 分钟内不生效：
--      redis-cli -a 123456 --scan --pattern 'his:perm:role:*' | xargs redis-cli -a 123456 DEL
--      redis-cli -a 123456 --scan --pattern 'his:user:identity:*' | xargs redis-cli -a 123456 DEL
--   2) 幂等：按 (role_id, menu_id) 组合精确删再插（INSERT IGNORE 会被已存在组合
--      静默跳过）；id 显式给（sys_role_menu.id 无库默认值），雪花号段 8920610290*
--      与存量 8920610270* / 8920610251* / 8920610252* 及 8900000000000090* 不撞。
-- ============================================================================

DELETE FROM sys_role_menu WHERE role_id = 16 AND menu_id IN (1007, 2146);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES (8920610290000000001, 16, 1007, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (8920610290000000002, 16, 2146, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- 回查 ----------
-- 1) role 16 已持有价格管理页 + 按钮（目录 1000 原有）：
--    SELECT m.id, m.menu_name, m.permission FROM sys_role_menu rm
--      JOIN sys_menu m ON m.id = rm.menu_id
--     WHERE rm.role_id = 16 AND rm.menu_id IN (1000, 1007, 2146)
--     ORDER BY rm.menu_id;
--    预期：1000 财务结算(目录) + 1007 价格管理(list) + 2146 价格管理-修改(edit)
-- 2) 收费员(4) 不持有价格管理：
--    SELECT COUNT(*) FROM sys_role_menu WHERE role_id = 4 AND menu_id IN (1007, 2146);
--    预期：0
-- 3) 目录链路完整（祖先 1000 已授，侧边栏可见）：
--    SELECT id, menu_name, parent_id, menu_type FROM sys_menu WHERE id IN (1000, 1007, 2146);
