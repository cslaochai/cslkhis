-- 348 流程共享页多岗位授权
-- 日期：2026-11-20
-- 同一业务单据的不同环节应由多个岗位看到：会诊（申请/应邀/管理）、日间手术（申请/排程/管理）、
-- 死亡与病危（医生开单/护士执行/医务科监管）、病历质控（科室自查/院级质控）、
-- 资金与对账（收费/审计）、体检与线上问诊（门诊办/客服）。含祖先目录自动补齐。

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
 (9210000000000000745, 27, 302, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000746, 27, 300, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000747, 27, 307, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000748, 27, 29812, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000749, 27, 315, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000750, 27, 1300, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000751, 27, 316, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000752, 27, 29810, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000753, 27, 318, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000754, 27, 29813, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000755, 27, 604, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000756, 27, 600, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000757, 26, 307, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000758, 26, 29812, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000759, 26, 300, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000760, 26, 315, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000761, 26, 1300, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000762, 26, 316, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000763, 26, 318, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000764, 26, 29813, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000765, 26, 604, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000766, 26, 600, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000767, 26, 2387, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000768, 3, 319, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000769, 3, 413, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000770, 3, 400, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000771, 3, 608, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000772, 3, 29700, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000773, 10, 319, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000774, 10, 608, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000775, 33, 702, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000776, 33, 700, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000777, 17, 2320, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000778, 17, 4103, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000779, 17, 1000, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000780, 17, 2367, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000781, 4, 2367, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000782, 4, 2340, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000783, 4, 4105, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000784, 22, 2950, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000785, 24, 905, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000786, 24, 1400, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000787, 24, 2929, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000788, 24, 1300, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000789, 2, 1107, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000790, 2, 29805, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000791, 2, 500, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000792, 23, 2936, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0),
 (9210000000000000793, 23, 2983, 'menu-govern-348', NOW(), 'menu-govern-348', NOW(), 0, 0);

-- 回滚
-- DELETE FROM sys_role_menu WHERE create_by='menu-govern-348';
