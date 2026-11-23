-- ============================================================
-- 342 患者/行政补最小可见页 + 物价员/统计上报员角色
-- 日期：2026-11-20
-- 内容：
--   A 患者(19)：补患者中心最小集（健康档案/满意度/常见问题/白话词典）
--   B 行政人员(23)：补总值班日志/排班总览/满意度/工单
--   C 新增 40 物价员、41 统计上报员，并授对应页面
-- 新增授权行：24（其中 6 行为原有，实际新增 24 条里的 18 条为新增，见明细）
-- 说明：患者(19)/行政(23) 原有的 门户/工作台/统一待办 授权未重复插入。
-- ============================================================

-- C1 新增角色
INSERT INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES
 (40, '10050', '物价员', 2, 5, 1, 47, 1, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, '医疗服务价格维护与收费策略', 0, 0),
 (41, '10051', '统计上报员', 2, 6, 1, 48, 1, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, '病案统计与运营报表上报', 0, 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- A/B/C2 授权（仅列实际新增的 24 行）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
 (9210000000000000651, 19, 700,  'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000652, 19, 705,  'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000653, 19, 616,  'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000654, 19, 1130, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000655, 19, 1133, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000656, 23, 800,  'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000657, 23, 2920, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000658, 23, 2945, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000659, 23, 2938, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000660, 23, 616,  'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000661, 23, 1136, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000662, 40, 100,  'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000663, 40, 101,  'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000664, 40, 29654,'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000665, 40, 1000, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000666, 40, 4105, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000667, 40, 1007, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000668, 40, 2340, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000669, 41, 100,  'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000670, 41, 101,  'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000671, 41, 29654,'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000672, 41, 1200, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000673, 41, 2159, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0),
 (9210000000000000674, 41, 1201, 'menu-govern-342', NOW(), 'menu-govern-342', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 回查
-- SELECT rm.role_id, r.role_name, m.menu_name FROM sys_role_menu rm
--   JOIN sys_role r ON r.id=rm.role_id JOIN sys_menu m ON m.id=rm.menu_id
--  WHERE rm.role_id IN (19,23,40,41) ORDER BY rm.role_id, m.parent_id;

-- 回滚
-- DELETE FROM sys_role_menu WHERE create_by='menu-govern-342';
-- DELETE FROM sys_role WHERE id IN (40,41);
