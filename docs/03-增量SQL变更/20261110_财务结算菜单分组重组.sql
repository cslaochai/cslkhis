-- ============================================================
-- 20261110_财务结算菜单分组重组
-- 日期：2026-11-10
-- 目的：把 1000「财务结算」下 21 个扁平页面菜单，按业务域收进 6 个二级目录，
--       侧边栏从 21 行 → 6 个分组头。解决 21 项平铺难扫、同类入口被打散的问题。
-- 依据：联网核对厂商（嘉讯/东软/Q医疗等）实践——收费/财务/医保/对账/价格分域，而非混在一个父菜单下平铺；
--       与本地存量范本 310（目录重组）/311（菜单合并）/20261110_药事管理菜单分组重组 同口径。
-- 原则（与 310/311/药事一致）：只调整归属与排序，不改页面、不改路由、不改组件、不改权限码，
--       因此前端 v-perm 与服务端 @PreAuthorize 都不受影响；目录节点 component=NULL 不挂页面。
-- 避坑（来自菜单四坑 + 药事分组 UNION 踩坑）：
--   ① 目录 path 必须带前导斜杠的绝对路径；② 父节点必须是目录（1000 本身就是目录，合规）；
--   ③ 祖先链必须完整——故 6 个新目录必须显式授予相关角色，否则子菜单进不了菜单树；
--   ④ admin 是显式授权制（sys_role_menu 必须显式给 admin），新目录必须给 admin；
--   ⑤ 授权段凡「固定 admin 行 + 动态 DISTINCT 角色」的写法，必须用 UNION 不能用 UNION ALL，
--      否则 admin 兼子菜单拥有者时批内自撞 uk_role_menu（Duplicate entry）。
-- 前置：无前端改动，纯菜单树调整。
-- 落地后：务必清菜单/权限缓存或重启后端（权限 Redis TTL 10min，菜单缓存同理），否则新目录/授权不生效。
-- 回滚：见文件末尾「回滚段」。
-- ============================================================

-- A 新增 6 个二级目录（parent_id 全部指向 1000，menu_type=1 目录，component=NULL）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 4101, '收费作业', 1000, 10, 1, '/fin-cashier', NULL, 'finance.groupCashier', 'Wallet', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '财务结算分组-按业务域', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 4101);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 4102, '发票管理', 1000, 20, 1, '/fin-invoice', NULL, 'finance.groupInvoice', 'Tickets', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '财务结算分组-按业务域', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 4102);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 4103, '日结与对账', 1000, 30, 1, '/fin-recon', NULL, 'finance.groupRecon', 'Coin', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '财务结算分组-按业务域', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 4103);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 4104, '流水与查询', 1000, 40, 1, '/fin-flow', NULL, 'finance.groupFlow', 'Document', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '财务结算分组-按业务域', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 4104);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 4105, '价格与策略', 1000, 50, 1, '/fin-price', NULL, 'finance.groupPrice', 'PriceTag', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '财务结算分组-按业务域', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 4105);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 4106, '医保管理', 1000, 60, 1, '/fin-insurance', NULL, 'finance.groupInsurance', 'Discount', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '财务结算分组-按业务域', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 4106);

-- B 把 21 个页面改挂到对应二级目录，并按组内顺序重排 sort_order（不改组件/路径/权限码）
UPDATE sys_menu
SET parent_id = CASE id
        WHEN 1001 THEN 4101 WHEN 305 THEN 4101 WHEN 2360 THEN 4101 WHEN 1005 THEN 4101
        WHEN 1004 THEN 4102
        WHEN 1008 THEN 4103 WHEN 2320 THEN 4103 WHEN 2367 THEN 4103
        WHEN 2363 THEN 4104 WHEN 2365 THEN 4104 WHEN 2350 THEN 4104 WHEN 313 THEN 4104
        WHEN 1007 THEN 4105 WHEN 2340 THEN 4105
        WHEN 1003 THEN 4106 WHEN 1011 THEN 4106 WHEN 1012 THEN 4106 WHEN 1010 THEN 4106
        WHEN 1006 THEN 4106 WHEN 1009 THEN 4106 WHEN 2923 THEN 4106
        ELSE parent_id END,
    sort_order = CASE id
        WHEN 1001 THEN 10 WHEN 305 THEN 20 WHEN 2360 THEN 30 WHEN 1005 THEN 40
        WHEN 1004 THEN 10
        WHEN 1008 THEN 10 WHEN 2320 THEN 20 WHEN 2367 THEN 30
        WHEN 2363 THEN 10 WHEN 2365 THEN 20 WHEN 2350 THEN 30 WHEN 313 THEN 40
        WHEN 1007 THEN 10 WHEN 2340 THEN 20
        WHEN 1003 THEN 10 WHEN 1011 THEN 20 WHEN 1012 THEN 30 WHEN 1010 THEN 40
        WHEN 1006 THEN 50 WHEN 1009 THEN 60 WHEN 2923 THEN 70
        ELSE sort_order END,
    update_time = NOW()
WHERE id IN (1001, 305, 2360, 1005, 1004, 1008, 2320, 2367, 2363, 2365, 2350, 313,
            1007, 2340, 1003, 1011, 1012, 1010, 1006, 1009, 2923);

-- C 授权补齐（祖先链完整性 + admin 显式授权）
--   每个二级目录授予：① admin（role_id=1）② 当前已拥有该目录下任一子菜单的所有角色。
--   用 UNION（非 UNION ALL）批内去重，避免 admin 兼子菜单拥有者时撞 uk_role_menu。
SET @rm_id := (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu);

-- C1 收费作业（4101）：子菜单 1001,305,2360,1005
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 4101 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 4101 FROM sys_role_menu rm WHERE rm.menu_id IN (1001, 305, 2360, 1005)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- C2 发票管理（4102）：子菜单 1004
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 4102 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 4102 FROM sys_role_menu rm WHERE rm.menu_id IN (1004)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- C3 日结与对账（4103）：子菜单 1008,2320,2367
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 4103 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 4103 FROM sys_role_menu rm WHERE rm.menu_id IN (1008, 2320, 2367)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- C4 流水与查询（4104）：子菜单 2363,2365,2350,313
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 4104 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 4104 FROM sys_role_menu rm WHERE rm.menu_id IN (2363, 2365, 2350, 313)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- C5 价格与策略（4105）：子菜单 1007,2340
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 4105 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 4105 FROM sys_role_menu rm WHERE rm.menu_id IN (1007, 2340)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- C6 医保管理（4106）：子菜单 1003,1011,1012,1010,1006,1009,2923
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 4106 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 4106 FROM sys_role_menu rm WHERE rm.menu_id IN (1003, 1011, 1012, 1010, 1006, 1009, 2923)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- D 验证查询（确认分组后树结构正确，可单独执行）
-- SELECT m.id, m.menu_name, m.parent_id, m.sort_order, m.is_visible
-- FROM sys_menu m
-- WHERE m.parent_id IN (1000, 4101, 4102, 4103, 4104, 4105, 4106)
-- ORDER BY m.parent_id, m.sort_order;

-- ============================================================
-- 回滚段：先把 21 个页面退回 1000，再删 6 个目录，最后清对应角色授权
-- ============================================================
-- UPDATE sys_menu SET parent_id = 1000, sort_order =  1, update_time = NOW() WHERE id = 1001; -- 收费结算窗口(原 1)
-- UPDATE sys_menu SET parent_id = 1000, sort_order =  2, update_time = NOW() WHERE id = 305;  -- 住院结算(原 2)
-- UPDATE sys_menu SET parent_id = 1000, sort_order =  3, update_time = NOW() WHERE id = 1005; -- 退费申请(原 3)
-- UPDATE sys_menu SET parent_id = 1000, sort_order =  4, update_time = NOW() WHERE id = 1004; -- 发票管理(原 4)
-- UPDATE sys_menu SET parent_id = 1000, sort_order =  5, update_time = NOW() WHERE id = 1008; -- 财务日结(原 5)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 10, update_time = NOW() WHERE id = 1003; -- 医保结算清单(原 10)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 11, update_time = NOW() WHERE id = 1011; -- 慢特病医保备案(原 11)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 12, update_time = NOW() WHERE id = 1012; -- 医保合规审核台账(原 12)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 13, update_time = NOW() WHERE id = 1010; -- 医保扣款与飞检处理(原 13)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 20, update_time = NOW() WHERE id = 2360; -- 费用记账(原 20)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 21, update_time = NOW() WHERE id = 2363; -- 结算账单查询(原 21)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 22, update_time = NOW() WHERE id = 2365; -- 支付流水(原 22)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 23, update_time = NOW() WHERE id = 2350; -- 退费流水(原 23)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 24, update_time = NOW() WHERE id = 2367; -- 资金账户(原 24)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 25, update_time = NOW() WHERE id = 2320; -- 支付渠道对账(原 25)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 26, update_time = NOW() WHERE id = 1007; -- 价格管理(原 26)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 40, update_time = NOW() WHERE id = 1006; -- 医保政策配置(原 40)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 41, update_time = NOW() WHERE id = 1009; -- 医保目录对照(原 41)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 42, update_time = NOW() WHERE id = 2923; -- 门诊慢特病病种目录(原 42)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 43, update_time = NOW() WHERE id = 2340; -- 复诊收费策略(原 43)
-- UPDATE sys_menu SET parent_id = 1000, sort_order = 44, update_time = NOW() WHERE id = 313;  -- 欠费管控(原 44)
-- DELETE FROM sys_menu WHERE id IN (4101, 4102, 4103, 4104, 4105, 4106);
-- DELETE FROM sys_role_menu WHERE menu_id IN (4101, 4102, 4103, 4104, 4105, 4106);
