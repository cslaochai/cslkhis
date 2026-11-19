-- ============================================================
-- 337 住院菜单二级分组收拢（在院协作 / 出院与终末），组套模板下沉系统管理
-- 日期：2026-11-16
-- 目的：300「住院管理」下平铺 13 个二级页面，一线工作区与在院协作事项、出院终末事项、
--       后台模板维护混在同一层级，侧栏难扫。本轮按真实住院流程阶段收拢：
--         直属工作区：入出院 / 医生站 / 护士站 / 病历 / 床位 / ICU 专科
--         二级目录「在院协作」：会诊 / 转科 / 请假离院 / 病危重通知
--         二级目录「出院与终末」：出院带药 / 死亡证明与登记
--         后台下沉：医嘱组套模板维护 → 系统管理（与医嘱基础字典并列）
-- 依据：联网核对《电子病历系统功能规范》、上海 HIS 功能规范（会诊医嘱 / 出院带药医嘱 /
--       排他性医嘱分类）、卫宁 CIS、东软 RealOne-EMR、嘉讯护士站与成电医星住院 EMR——
--       会诊 / 转科 / 出院 / 带药 / 病危本是医生站、护士站、出入院处站内发起的动作，
--       本地暂以独立台账页承载，故先按事项阶段分组收拢，不改变页面归属的数据链路。
-- 原则（与 336 / 药事分组重组先例一致）：只调整归属、排序与目录分组，不改页面、不改路由、
--       不改组件、不改权限码；前端 v-perm 与服务端 @PreAuthorize 均不受影响；目录节点 component=NULL。
-- 边界：本轮为菜单目录层收拢，台账页保留为独立可访问页面；后续前端改造在医生站 / 护士站
--       工作区内嵌对应发起入口（复用现有弹框组件），台账页再降级为查询，不在本轮范围。
-- 避坑（菜单四坑 + 336 踩坑）：
--   ① 目录 path 必须带前导斜杠的绝对路径；② 父节点必须是目录（300/1100 均为目录，合规）；
--   ③ 祖先链必须完整——新二级目录必须显式授予相关角色，否则子菜单进不了菜单树；
--   ④ admin 为显式授权制，新目录必须给 admin；
--   ⑤ 授权段 UNION（非 UNION ALL）批内去重，避免 admin 兼子菜单持有者时撞 uk_role_menu。
-- 前置：无前端改动，纯菜单树调整（前端路由为静态表，path/component 均未变，可达性不受影响；
--       Sidebar 已支持「目录 → 子目录 → 页面」三层渲染）。
-- 落地后：清权限缓存（权限 Redis TTL 10min；菜单为进程内缓存，F5 刷新即重拉）：
--         node workspace/_clear_perm_cache.mjs
-- 回滚：见文件末尾「回滚段」。
-- ============================================================

-- A 新增两个二级目录（parent_id=300，menu_type=1，component=NULL，permission=NULL）
-- 菜单 ID 段：29812 / 29813（29810/29811 已用于手术麻醉 / 营养膳食，错开）
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29812, '在院协作', 300, 60, 1, '/ipd-collab', NULL, 'ipd.groupCollab', 'Users', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '住院分组-在院协作事项：会诊 / 转科 / 请假离院 / 病危重通知', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29812);

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time,
                      del_flag, remark, create_by_id, update_by_id)
SELECT 29813, '出院与终末', 300, 70, 1, '/ipd-discharge', NULL, 'ipd.groupDischarge', 'Box', NULL,
       0, 0, 1, 1, 'system', NOW(), 'system', NOW(), 0, '住院分组-出院阶段与终末事项：出院带药 / 死亡证明与登记', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 29813);

-- B 页面归位：改挂目标目录并按组内顺序重排 sort_order（不改组件 / 路径 / 权限码）
UPDATE sys_menu
SET parent_id = CASE id
        WHEN 307 THEN 29812 WHEN 308 THEN 29812 WHEN 320 THEN 29812 WHEN 319 THEN 29812
        WHEN 310 THEN 29813 WHEN 318 THEN 29813
        WHEN 2387 THEN 1100
        ELSE parent_id END,
    sort_order = CASE id
        WHEN 307 THEN 10 WHEN 308 THEN 20 WHEN 320 THEN 30 WHEN 319 THEN 40
        WHEN 310 THEN 10 WHEN 318 THEN 20
        WHEN 2387 THEN 46
        ELSE sort_order END,
    update_time = NOW()
WHERE id IN (307, 308, 320, 319, 310, 318, 2387);

-- C 留在住院管理(300)直属的工作区页面重排排序（协作 / 终末目录占 60 / 70，ICU 专科置 80）
UPDATE sys_menu
SET sort_order = CASE id
        WHEN 301 THEN 10
        WHEN 302 THEN 20
        WHEN 303 THEN 30
        WHEN 304 THEN 40
        WHEN 317 THEN 50
        WHEN 314 THEN 80
    END,
    update_time = NOW()
WHERE parent_id = 300 AND del_flag = 0
  AND id IN (301, 302, 303, 304, 317, 314);

-- D 授权补齐（祖先链完整性 + admin 显式授权）
--   每个新目录授予：① admin（role_id=1）② 当前已持有该目录下任一子菜单的所有角色。
--   不给出空目录，不破坏已有可见性；UNION 批内去重；NOT EXISTS 防存量重复；幂等可重跑。
SET @rm_id := (SELECT IFNULL(MAX(id), 0) FROM sys_role_menu);

-- D1 在院协作(29812)：子菜单 307 / 308 / 320 / 319
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 29812 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 29812 FROM sys_role_menu rm
    WHERE rm.menu_id IN (307, 308, 320, 319)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- D2 出院与终末(29813)：子菜单 310 / 318
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, g.menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT 1 AS role_id, 29813 AS menu_id
    UNION
    SELECT DISTINCT rm.role_id, 29813 FROM sys_role_menu rm
    WHERE rm.menu_id IN (310, 318)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- D3 系统管理(1100)：兜底补持有 2387 但未挂 1100 的角色（实测 2387 持有者 1,2 均已挂 1100，此段为空转保险）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT (@rm_id := @rm_id + 1), g.role_id, 1100 AS menu_id, 'system', NOW(), 'system', NOW(), 0, 0
FROM (
    SELECT DISTINCT rm.role_id, 1100 AS menu_id FROM sys_role_menu rm
    WHERE rm.menu_id IN (2387)
) g
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = g.role_id AND x.menu_id = g.menu_id);

-- E 验证查询（可单独执行）
-- E1 住院管理(300)直属页面 + 两个新目录子页面清单
-- SELECT parent_id, id, menu_name, sort_order, menu_type, is_visible
-- FROM sys_menu
-- WHERE parent_id IN (300, 29812, 29813, 1100) AND del_flag = 0
-- ORDER BY parent_id, sort_order, id;

-- E2 孤儿检查：页面授权角色未挂父目录（本轮迁入页面期望 0 行）
-- SELECT rm.menu_id, rm.role_id, m.parent_id AS dir_id
-- FROM sys_role_menu rm
-- JOIN sys_menu m ON m.id = rm.menu_id
-- WHERE m.parent_id IN (29812, 29813) AND m.menu_type = 2
--   AND NOT EXISTS (SELECT 1 FROM sys_role_menu b WHERE b.role_id = rm.role_id AND b.menu_id = m.parent_id);

-- E3 新目录授权角色数（应 ≥ 其下任一页面授权角色数）
-- SELECT menu_id, COUNT(*) AS granted_roles FROM sys_role_menu
-- WHERE menu_id IN (29812, 29813) GROUP BY menu_id;

-- ============================================================
-- 回滚段：先把 7 个页面退回原归属与原排序，再删两个新目录，最后清对应目录授权
-- ============================================================
-- UPDATE sys_menu SET parent_id = 300, sort_order = 110, update_time = NOW() WHERE id = 307;  -- 会诊管理(原 110)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 60,  update_time = NOW() WHERE id = 308;  -- 转科管理(原 60)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 70,  update_time = NOW() WHERE id = 320;  -- 患者请假离院(原 70)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 80,  update_time = NOW() WHERE id = 319;  -- 病危重通知(原 80)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 100, update_time = NOW() WHERE id = 310;  -- 出院带药(原 100)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 90,  update_time = NOW() WHERE id = 318;  -- 死亡证明与登记(原 90)
-- UPDATE sys_menu SET parent_id = 300, sort_order = 130, update_time = NOW() WHERE id = 2387; -- 医嘱组套维护(原 130)
-- UPDATE sys_menu SET sort_order = 120, update_time = NOW() WHERE id = 314;                  -- ICU 专科监护(原 120)
-- DELETE FROM sys_role_menu WHERE menu_id IN (29812, 29813);
-- DELETE FROM sys_menu WHERE id IN (29812, 29813);
-- 说明：D3 为 1100 补挂的空目录授权，2387 退回 300 后属于空挂接，不影响使用；如需彻底回滚，
--       按「持有 2387 但原本未挂 1100 的角色」核对后删除对应 1100 授权行。
