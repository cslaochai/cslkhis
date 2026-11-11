-- ============================================================
-- 20261110_药事管理菜单分组重组
-- 日期：2026-11-10
-- 目的：把 500「药事管理」下 21 个扁平叶子菜单（含 1 个隐藏的 516 处方公示）
--       按「药品生命周期」收进 6 个二级目录，解决 20 项平铺难扫、同链入口被打散的问题。
-- 依据：联网核对《医院信息平台应用功能指引》+ 卫宁/创业慧康/东软等厂商实践；
--       与本地存量范本 310（目录重组）/311（菜单合并）/20261110-医技医辅菜单合并 同口径。
-- 原则（与 310/311 一致）：只调整归属与排序，不改页面、不改路由、不改组件、不改权限码，
--       因此前端 v-perm 与服务端 @PreAuthorize 都不受影响；目录节点 component=NULL 不挂页面。
-- 避坑（来自菜单四坑）：
--   ① 目录 path 必须带前导斜杠的绝对路径；② 父节点必须是目录（500 本身就是目录，合规）；
--   ③ 祖先链必须完整——故 6 个新目录必须显式授予相关角色，否则子菜单进不了菜单树；
--   ④ admin 是显式授权制（sys_role_menu 必须显式给 admin），新目录必须给 admin。
-- 前置：无前端改动，纯菜单树调整。
-- 回滚：见文件末尾「回滚段」。
-- ============================================================

-- A 新增 6 个二级目录（parent_id 全部指向 500，menu_type=1 目录，component=NULL）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29800, '药品供应链', 500, 10, 1, '/pharmacy-supply', NULL, 'pharmacy.groupSupply', 'ShoppingCart', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '药事管理分组-按药品生命周期', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29800);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29801, '药房调剂', 500, 20, 1, '/pharmacy-dispensing', NULL, 'pharmacy.groupDispensing', 'Box', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '药事管理分组-按药品生命周期', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29801);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29802, '静脉用药配置中心', 500, 30, 1, '/pharmacy-pivas', NULL, 'pharmacy.groupPivas', 'Syringe', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '药事管理分组-按药品生命周期', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29802);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29803, '临床药学与处方管理', 500, 40, 1, '/pharmacy-clinical', NULL, 'pharmacy.groupClinical', 'Stamp', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '药事管理分组-按药品生命周期', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29803);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29804, '抗菌药物管理', 500, 50, 1, '/pharmacy-antibiotic', NULL, 'pharmacy.groupAntibiotic', 'Collection', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '药事管理分组-按药品生命周期', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29804);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29805, '合理用药与药事服务', 500, 60, 1, '/pharmacy-rational', NULL, 'pharmacy.groupRational', 'Notebook', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '药事管理分组-按药品生命周期', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29805);

-- B 把 21 个叶子菜单改挂到对应二级目录，并按组内顺序重排 sort_order（不改组件/路径/权限码）
UPDATE sys_menu
SET parent_id = CASE id
        WHEN 507 THEN 29800 WHEN 505 THEN 29800 WHEN 506 THEN 29800 WHEN 512 THEN 29800
        WHEN 513 THEN 29800 WHEN 501 THEN 29800 WHEN 510 THEN 29800 WHEN 514 THEN 29800
        WHEN 502 THEN 29801 WHEN 508 THEN 29801 WHEN 511 THEN 29801 WHEN 504 THEN 29801
        WHEN 509 THEN 29802
        WHEN 503 THEN 29803 WHEN 515 THEN 29803 WHEN 519 THEN 29803 WHEN 516 THEN 29803
        WHEN 517 THEN 29804 WHEN 2922 THEN 29804 WHEN 518 THEN 29804
        WHEN 1107 THEN 29805
        ELSE parent_id END,
    sort_order = CASE id
        WHEN 507 THEN 10 WHEN 505 THEN 20 WHEN 506 THEN 30 WHEN 512 THEN 40
        WHEN 513 THEN 50 WHEN 501 THEN 60 WHEN 510 THEN 70 WHEN 514 THEN 80
        WHEN 502 THEN 10 WHEN 508 THEN 20 WHEN 511 THEN 30 WHEN 504 THEN 40
        WHEN 509 THEN 10
        WHEN 503 THEN 10 WHEN 515 THEN 20 WHEN 519 THEN 30 WHEN 516 THEN 40
        WHEN 517 THEN 10 WHEN 2922 THEN 20 WHEN 518 THEN 30
        WHEN 1107 THEN 10
        ELSE sort_order END,
    update_time = NOW()
WHERE id IN (501,502,503,504,505,506,507,508,509,510,511,512,513,514,515,516,517,518,519,1107,2922);

-- C 授权补齐（祖先链完整性 + admin 显式授权）
--   每个二级目录授予：① admin（role_id=1，系统管理员）② 当前已拥有该目录下任一子菜单的所有角色。
--   这样不会给某角色开出“空目录”，也不会破坏已有可见性。
--   写法要点（踩坑修正）：用 UNION（非 UNION ALL）批内去重——admin 兼子菜单拥有者时，
--   第二段 DISTINCT 会把 (1,目录) 再吐一次，UNION ALL 不去重→同一语句两条 (1,目录)→撞 uk_role_menu。
--   id 用 @rm_id 自增（兼容 5.7/8.0），NOT EXISTS 防存量重复；整段幂等可重跑。
SET @rm_id := (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 29800 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29800 FROM sys_role_menu rm WHERE rm.menu_id IN (507,505,506,512,513,501,510,514)
    UNION SELECT 1 AS role_id, 29801 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29801 FROM sys_role_menu rm WHERE rm.menu_id IN (502,508,511,504)
    UNION SELECT 1 AS role_id, 29802 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29802 FROM sys_role_menu rm WHERE rm.menu_id IN (509)
    UNION SELECT 1 AS role_id, 29803 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29803 FROM sys_role_menu rm WHERE rm.menu_id IN (503,515,519,516)
    UNION SELECT 1 AS role_id, 29804 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29804 FROM sys_role_menu rm WHERE rm.menu_id IN (517,2922,518)
    UNION SELECT 1 AS role_id, 29805 AS menu_id
    UNION SELECT DISTINCT rm.role_id, 29805 FROM sys_role_menu rm WHERE rm.menu_id IN (1107)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- D 验证查询（确认分组后树结构正确，可单独执行）
-- SELECT m.id, m.menu_name, m.parent_id, m.sort_order, m.is_visible
-- FROM sys_menu m
-- WHERE m.parent_id IN (500, 29800, 29801, 29802, 29803, 29804, 29805)
-- ORDER BY m.parent_id, m.sort_order;

-- ============================================================
-- 回滚段：先把 21 个叶子退回 500，再删 6 个目录，最后清对应角色授权
-- ============================================================
-- UPDATE sys_menu SET parent_id = 500, sort_order = 7,  update_time = NOW() WHERE id = 501;  -- 药品库存(原 7)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 1,  update_time = NOW() WHERE id = 502;  -- 药房发药(原 1)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 20, update_time = NOW() WHERE id = 503;  -- 处方审核(原 20)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 6,  update_time = NOW() WHERE id = 504;  -- 麻精药品专册(原 6)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 8,  update_time = NOW() WHERE id = 505;  -- 采购订单(原 8)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 9,  update_time = NOW() WHERE id = 506;  -- 入库单(原 9)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 40, update_time = NOW() WHERE id = 507;  -- 供应商管理(原 40)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 2,  update_time = NOW() WHERE id = 508;  -- 住院摆药(原 2)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 3,  update_time = NOW() WHERE id = 509;  -- 静配中心(原 3)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 4,  update_time = NOW() WHERE id = 510;  -- 药房盘点(原 4)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 5,  update_time = NOW() WHERE id = 511;  -- 中药代煎(原 5)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 10, update_time = NOW() WHERE id = 512;  -- 药品调拨(原 10)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 11, update_time = NOW() WHERE id = 513;  -- 供应商退货(原 11)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 12, update_time = NOW() WHERE id = 514;  -- 药品追溯码(原 12)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 21, update_time = NOW() WHERE id = 515;  -- 处方点评(原 21)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 90, update_time = NOW() WHERE id = 516;  -- 处方公示(原 90,隐藏)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 41, update_time = NOW() WHERE id = 517;  -- 抗菌药物分级目录(原 41)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 23, update_time = NOW() WHERE id = 518;  -- 抗菌药物使用监测(原 23)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 22, update_time = NOW() WHERE id = 519;  -- I类切口预防用药点评(原 22)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 42, update_time = NOW() WHERE id = 1107; -- 合理用药知识库(原 42)
-- UPDATE sys_menu SET parent_id = 500, sort_order = 24, update_time = NOW() WHERE id = 2922; -- 抗菌药物处方权管理(原 24)
-- DELETE FROM sys_menu WHERE id IN (29800,29801,29802,29803,29804,29805);
-- DELETE FROM sys_role_menu WHERE menu_id IN (29800,29801,29802,29803,29804,29805);
