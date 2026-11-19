-- ============================================================
-- 336 住院菜单按业务域归位（手术麻醉 / 营养膳食独立一级目录，随访转诊入患者中心）
-- 日期：2026-11-16
-- 目的：原 300「住院管理」下平铺 26 个二级菜单，把病区在院主线与手术室麻醉科、
--       营养科、院外协作、后台配置混在同一目录，侧栏过长难扫。本轮按业务域归位：
--       手麻、营养膳食各自独立成一级目录，随访/转诊归患者中心，字典维护归系统管理，
--       住院目录只留病区医护主线。
-- 依据：联网核对创业慧康 / 东软 / 卫宁等厂商实践——手麻(AIMS/OR)、营养膳食各自独立
--       子系统，随访/转诊属患者服务域，字典页归系统配置；与本地存量范本
--       20261110_药事管理菜单分组重组 / 20261110_财务结算菜单分组重组 同口径。
-- 原则（与分组重组先例一致）：只调整归属与排序，不改页面、不改路由、不改组件、不改权限码，
--       因此前端 v-perm 与服务端 @PreAuthorize 都不受影响；目录节点 component=NULL 不挂页面。
-- 避坑（菜单四坑 + 分组重组踩坑）：
--   ① 目录 path 必须带前导斜杠的绝对路径；② 父节点必须是目录（29810/29811 为一级目录，合规）；
--   ③ 祖先链必须完整——新目录与迁入目录必须显式授予相关角色，否则子菜单进不了菜单树；
--   ④ admin 是显式授权制（sys_role_menu 必须显式给 admin），新目录必须给 admin；
--   ⑤ 授权段「固定 admin 行 + 动态 DISTINCT 角色」必须用 UNION 不能用 UNION ALL，
--      否则 admin 兼子菜单拥有者时批内自撞 uk_role_menu(role_id,menu_id)。
-- 前置：无前端改动，纯菜单树调整（前端路由为静态表，path/component 均未变，可达性不受影响）。
-- 落地后：务必清菜单/权限缓存或重启后端（权限 Redis TTL 10min，菜单缓存同理）：
--         node workspace/_clear_perm_cache.mjs
-- 回滚：见文件末尾「回滚段」。
-- ============================================================

-- A 新增两个一级目录（menu_type=1 目录，component=NULL，permission=NULL）
-- 菜单 ID 段：29810 / 29811（29800-29805 已被药事分组重组占用，错开）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29810, '手术麻醉', 0, 35, 1, '/or-group', NULL, 'orGroup', 'Scissor', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '围术期独立子系统：手术排期 / 器械清点 / 麻醉 / 日间手术', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29810);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29811, '营养膳食', 0, 37, 1, '/nutrition-group', NULL, 'nutritionGroup', 'Food', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '临床营养与配餐独立子系统：筛查 / 膳食医嘱 / 订餐 / 营养会诊 / 指标监测', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29811);

-- B 页面归位：改挂目标目录并按组内顺序重排 sort_order（不改组件/路径/权限码）
UPDATE sys_menu
SET parent_id = CASE id
        WHEN 306   THEN 29810 WHEN 2924 THEN 29810 WHEN 309 THEN 29810
        WHEN 316   THEN 29810 WHEN 2928 THEN 29810
        WHEN 424   THEN 29811 WHEN 425 THEN 29811 WHEN 426 THEN 29811
        WHEN 427   THEN 29811 WHEN 428 THEN 29811
        WHEN 311   THEN 700   WHEN 312 THEN 700
        WHEN 2383  THEN 1100
        ELSE parent_id END,
    sort_order = CASE id
        WHEN 306   THEN 10 WHEN 2924 THEN 20 WHEN 309 THEN 30 WHEN 316 THEN 40 WHEN 2928 THEN 50
        WHEN 424   THEN 10 WHEN 425 THEN 20 WHEN 426 THEN 30 WHEN 427 THEN 40 WHEN 428 THEN 50
        WHEN 311   THEN 50 WHEN 312 THEN 51
        WHEN 2383  THEN 42
        ELSE sort_order END,
    update_time = NOW()
WHERE id IN (306, 2924, 309, 316, 2928, 424, 425, 426, 427, 428, 311, 312, 2383);

-- C 留在住院管理(300)的页面重排排序（病区在院主线 + 院内会诊 + ICU 专科 + 医嘱组套模板）
-- 301 入出院管理 / 302 住院医生站 / 303 护士工作站 / 304 住院病历 / 317 床位管理 / 308 转科管理
-- 320 患者请假离院 / 319 病危重通知 / 318 死亡证明与登记 / 310 出院带药 / 307 会诊管理
-- 314 ICU 专科监护 / 2387 医嘱组套（套餐）维护
UPDATE sys_menu
SET sort_order = CASE id
        WHEN 301  THEN 10
        WHEN 302  THEN 20
        WHEN 303  THEN 30
        WHEN 304  THEN 40
        WHEN 317  THEN 50
        WHEN 308  THEN 60
        WHEN 320  THEN 70
        WHEN 319  THEN 80
        WHEN 318  THEN 90
        WHEN 310  THEN 100
        WHEN 307  THEN 110
        WHEN 314  THEN 120
        WHEN 2387 THEN 130
    END,
    update_time = NOW()
WHERE parent_id = 300 AND del_flag = 0
  AND id IN (301, 302, 303, 304, 317, 308, 320, 319, 318, 310, 307, 314, 2387);

-- D 授权补齐（祖先链完整性 + admin 显式授权）
--   每个目录授予：① admin（role_id=1，系统管理员）② 当前已拥有该目录下任一子菜单的所有角色。
--   按钮子菜单的授权角色均为其页面持有者子集（已核对），按页面 id 反查即可覆盖。
--   UNION 批内去重；NOT EXISTS 防存量重复；@rm_id 自增，整段幂等可重跑。
SET @rm_id := (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu);

-- D1 手术麻醉(29810)：子菜单 306 / 2924 / 309 / 316 / 2928
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 29810 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 29810 FROM sys_role_menu rm
    WHERE rm.menu_id IN (306, 2924, 309, 316, 2928)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- D2 营养膳食(29811)：子菜单 424 / 425 / 426 / 427 / 428
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 29811 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 29811 FROM sys_role_menu rm
    WHERE rm.menu_id IN (424, 425, 426, 427, 428)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- D3 患者中心(700)：补持有随访/转诊但未挂 700 的角色（700 为现有目录，admin 已挂）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT DISTINCT rm.role_id, 700 AS menu_id FROM sys_role_menu rm
    WHERE rm.menu_id IN (311, 312)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- D4 系统管理(1100)：补持有医嘱基础字典但未挂 1100 的角色（兜底，当前仅 admin 持有）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT DISTINCT rm.role_id, 1100 AS menu_id FROM sys_role_menu rm
    WHERE rm.menu_id IN (2383)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- E 验证查询（可单独执行）
-- E1 各新目录 / 迁入目录下的页面清单
-- SELECT parent_id, id, menu_name, sort_order FROM sys_menu
-- WHERE parent_id IN (29810, 29811, 700, 1100) AND menu_type = 2 AND del_flag = 0
-- ORDER BY parent_id, sort_order, id;

-- E2 住院管理(300) 剩余页面（期望 13 个）
-- SELECT id, menu_name, sort_order FROM sys_menu
-- WHERE parent_id = 300 AND menu_type = 2 AND del_flag = 0 ORDER BY sort_order;

-- E3 孤儿检查：页面授权角色未挂祖先目录的（本轮迁入页面期望 0 行）
-- 注：既有遗留 616 满意度评价 / role 25(门诊办) 挂 700 未挂目录，系 310 病历目录重组遗留，不在本轮范围。
-- SELECT rm.menu_id, rm.role_id, m.parent_id AS dir_id
-- FROM sys_role_menu rm
-- JOIN sys_menu m ON m.id = rm.menu_id
-- WHERE m.parent_id IN (29810, 29811, 700, 1100)
--   AND m.menu_type = 2
--   AND NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = rm.role_id AND b.menu_id = m.parent_id);

-- E4 新目录授权角色数（应 ≥ 其下任一页面的授权角色数）
-- SELECT menu_id, COUNT(*) AS granted_roles FROM sys_role_menu
-- WHERE menu_id IN (29810, 29811) GROUP BY menu_id;

-- ============================================================
-- 回滚段：先把 13 个迁走的页面退回 300，再删两个新目录，最后清对应角色授权
-- ============================================================
-- UPDATE sys_menu SET parent_id = 300, sort_order = 5  WHERE id = 306;    -- 手术排期(原 5)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 6  WHERE id = 2924;   -- 手术器械清点(原 6)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 7  WHERE id = 309;    -- 麻醉工作站(原 7)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 21 WHERE id = 316;    -- 日间手术(原 21)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 42 WHERE id = 2928;   -- 日间手术准入目录(原 42)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 22 WHERE id = 424;    -- 营养风险筛查(原 22)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 23 WHERE id = 425;    -- 膳食医嘱执行(原 23)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 24 WHERE id = 426;    -- 订餐配送(原 24)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 25 WHERE id = 427;    -- 营养会诊(原 25)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 26 WHERE id = 428;    -- 营养指标监测(原 26)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 27 WHERE id = 311;    -- 随访回访(原 27)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 28 WHERE id = 312;    -- 双向转诊(原 28)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 40 WHERE id = 2383;   -- 医嘱基础字典(原 40)
-- DELETE FROM sys_role_menu WHERE menu_id IN (29810, 29811);
-- DELETE FROM sys_menu WHERE id IN (29810, 29811);
-- 说明：D3/D4 是给现有目录 700/1100 补挂的角色授权，页面退回 300 后这些挂接属于空目录授权，
--       不影响使用；如需彻底回滚，按「持有 311/312 的角色中原本未挂 700 者」「持有 2383 但原本未挂 1100 者」核对后删除。
