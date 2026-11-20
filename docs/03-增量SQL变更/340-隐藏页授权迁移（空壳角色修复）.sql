-- ============================================================
-- 340 隐藏页授权迁移（空壳角色修复）P0
-- 日期：2026-11-20
-- 背景：
--   336-339 一批菜单治理脚本把「已并入」的子页面设成 is_visible=0（保留记录回滚），
--   但 sys_role_menu 里的授权没有跟着迁到宿主页，导致部分角色「有授权、无可见页面」。
--   实测：放射诊断医师(20) 5 条授权里 2 条隐藏、3 条门户 → 进系统 0 个业务页面。
-- 口径：
--   * 只做「新增授权」：把隐藏子页的授权补到宿主页，隐藏页授权原样保留。
--   * 不删除任何现有授权（隐藏页权限码仍被后端校验时删了会 403，参照 339 对 910 的处理）。
--   * 只迁「职责匹配、不产生越权」的 3 条；其余见文末「刻意未迁」。
-- 受影响角色：15 病案质控员、20 放射诊断医师、26 医务科
-- 新增行数：3
-- ============================================================

-- A1 放射诊断医师(20)：414 放射诊断工作站（已并入）→ 401 检查工作站
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000000620, 20, 401, 'menu-migrate-340', NOW(), 'menu-migrate-340', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 20 AND menu_id = 401);

-- A2 病案质控员(15)：603 病历审核（已并入）→ 602 病历查阅与审核
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000000621, 15, 602, 'menu-migrate-340', NOW(), 'menu-migrate-340', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 15 AND menu_id = 602);

-- A3 医务科(26)：618 公卫上报-其他上报（已并入）→ 613 公卫上报
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000000622, 26, 613, 'menu-migrate-340', NOW(), 'menu-migrate-340', NOW(), 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 26 AND menu_id = 613);

-- B 回查（期望 3 行，且 role 20 首次出现非门户可见页）
-- SELECT rm.role_id, r.role_name, m.id, m.menu_name, m.is_visible
--   FROM sys_role_menu rm
--   JOIN sys_role r ON r.id = rm.role_id
--   JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE (rm.role_id, rm.menu_id) IN ((20,401),(15,602),(26,613))
--  ORDER BY rm.role_id;

-- C 空壳角色回查（期望：放射诊断医师不再为 0；患者19/行政23 仍为 0，属 P2 待定）
-- SELECT rm.role_id, r.role_name,
--        SUM(CASE WHEN m.is_visible = 1 AND m.menu_type = 2 THEN 1 ELSE 0 END) AS visible_pages
--   FROM sys_role_menu rm
--   JOIN sys_role r ON r.id = rm.role_id
--   JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE m.menu_type = 2 AND m.del_flag = 0
--  GROUP BY rm.role_id, r.role_name
--  ORDER BY visible_pages ASC, rm.role_id;

-- D 清缓存（SQL 改库不会自动失效，必须清或重登）：
--    redis-cli -h 192.168.88.132 -a 123456 --scan --pattern 'his:perm:role:*' | xargs -r redis-cli -h 192.168.88.132 -a 123456 del
--    redis-cli -h 192.168.88.132 -a 123456 --scan --pattern 'his:user:identity:*' | xargs -r redis-cli -h 192.168.88.132 -a 123456 del

-- ============================================================
-- 刻意未迁（有授权但宿主页会越权，或职责不匹配）：
--   12 检查技师：403 标本管理 → 402 检验工作站。检查技师拿到 LIS 全部功能属越权；
--                其已有检查工作站(401)，标本状态可在检查侧查询。
--   17 审计统计员 / 18 院领导：605 全院数据质量监测 → 604 病历质控。
--                604 是质控工作台（可打分/发整改），给审计/领导是越权；
--                全院质量看板应走 BI/报表，不在本次迁移范围。
--   333 院内 VTE 监测、516 处方公示、102 消息中心、910 DIP 分组模拟：
--                非「已并入」语义（102 已被统一待办替代、910 是分组模拟页签），不迁。
-- ============================================================

-- 回滚：删掉本次新增的 3 行即可
-- DELETE FROM sys_role_menu WHERE id IN (9210000000000000620, 9210000000000000621, 9210000000000000622);
