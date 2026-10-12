-- 前台导诊（role_code 10018）补「收费结算窗口」与「结算账单查询」两个菜单
-- 为什么：前台挂号代收挂号费的扫码收款弹框要打两组接口——
--   /charge/qrPay/*（出码 / 查单 / 关单）→ 权限码 finance:cashier:list（菜单 1001）
--   /charge/settlementBill/getDetailById（弹框内费用明细）→ 权限码 finance:bill:list（菜单 2363）
--   两个码原先只有 系统管理员(1) / 收费员(4) / 医保结算员(16) 持有，
--   前台导诊点「收款」必 403（弹框能开，出码失败）。
-- 为什么连父目录 1000（财务结算）一起授：侧边栏菜单树只认 parent_id=0 为根
--   （SysMenuService.buildMenuTree 从 0 起递归），只授子菜单 1001/2363 时父节点不在
--   授权集合里，整棵子树不渲染 —— 等价于没授；1000 本身无权限码，补齐只是让树长出来。
-- 前置：sys_role_menu 的唯一键 uk_role_menu(role_id, menu_id) 不含 del_flag，
--   删除即物理删；本文件用 ON DUPLICATE KEY UPDATE 兜重复执行（id 段为本文件专属，与既有行无碰撞）。
-- 执行后必须清 Redis：DEL his:perm:role:7（不清则 10 分钟内仍是旧权限集合）

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610120000000001, 7, 1000, 'role7-finance-20261012', 1, NOW(), 'role7-finance-20261012', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610120000000002, 7, 1001, 'role7-finance-20261012', 1, NOW(), 'role7-finance-20261012', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_by_id, create_time, update_by, update_by_id, update_time)
VALUES (8920610120000000003, 7, 2363, 'role7-finance-20261012', 1, NOW(), 'role7-finance-20261012', 1, NOW())
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);

-- 回查：应 3 行 —— 1000（目录，无权限码）/ 1001（finance:cashier:list）/ 2363（finance:bill:list）
SELECT rm.menu_id, m.menu_name, m.menu_type, m.permission
FROM sys_role_menu rm
         JOIN sys_menu m ON m.id = rm.menu_id
WHERE rm.role_id = 7 AND rm.menu_id IN (1000, 1001, 2363)
ORDER BY rm.menu_id;
