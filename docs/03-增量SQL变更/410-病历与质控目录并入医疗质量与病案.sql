-- ============================================================================
-- 410 病历与质控目录（600）并入 医疗质量与安全（29700）
-- ============================================================================
-- 背景（2026-11-27）：600「病历与质控」与 29700「医疗质量与安全」两个同级目录
--   各装几个页面，同一职能角色的质量类菜单被拆成两组（医务科 26 侧栏因此多出一个一级目录）。
--   617「手卫生依从性」在 600 下长期零授权，是孤儿页。
--
-- 改动：
--   1) 600 下 5 个页面平移到 29700，排序接在 607/608/610 之后；
--   2) 29700 改名为「医疗质量与病案」（合并后原名称覆盖不了病案管理与上报科室代码对照）；
--   3) 摘除 600 目录的全部角色授权并删除 600 菜单行（不留空目录）。
--   不动：页面权限码（permission）、路由 path、组件、后端接口、任何按钮授权。
--   影响面：仅菜单树分组。持有 600 但未持子页的角色不会丢失任何可访问页面。
--
-- 幂等：平移按 parent_id 判（重跑第二遍无行可更）；600 行删除带子页数守卫。
-- 回滚见文件末尾。
-- ============================================================================

-- ---------------- 0. 前置核对 ----------------
SELECT id, menu_name, parent_id, sort_order, menu_type, is_visible
FROM sys_menu WHERE parent_id IN (600, 29700) ORDER BY parent_id, sort_order;

SELECT rm.role_id, r.role_name, COUNT(*) AS menu_cnt
FROM sys_role_menu rm LEFT JOIN sys_role r ON r.id = rm.role_id
WHERE rm.menu_id = 600 GROUP BY rm.role_id, r.role_name ORDER BY rm.role_id;

-- 断言：600 下必须恰为 5 个页面，且 id 集合为 602/604/609/617/29626
SELECT COUNT(*) AS child_cnt,
       SUM(id IN (602, 604, 609, 617, 29626)) AS matched_cnt,
       SUM(menu_type <> 2) AS not_page_cnt
FROM sys_menu WHERE parent_id = 600;

-- ---------------- 1. 页面平移（排序：专项质控10 / 危急值30 / 医疗安全40，病案类 45-65） ----------------
UPDATE sys_menu
SET parent_id  = 29700,
    sort_order = CASE id
                     WHEN 602   THEN 45
                     WHEN 604   THEN 50
                     WHEN 29626 THEN 55
                     WHEN 609   THEN 60
                     WHEN 617   THEN 65
                 END,
    update_by   = 'sql410',
    update_time = NOW()
WHERE parent_id = 600
  AND id IN (602, 604, 609, 617, 29626);

-- ---------------- 2. 目录改名 ----------------
UPDATE sys_menu
SET menu_name   = '医疗质量与病案',
    update_by   = 'sql410',
    update_time = NOW()
WHERE id = 29700;

-- ---------------- 3. 摘 600 授权 + 删 600 行（守卫：子页必须已清空） ----------------
DELETE FROM sys_role_menu WHERE menu_id = 600;

SET @child_cnt := (SELECT COUNT(*) FROM sys_menu WHERE parent_id = 600);
DELETE FROM sys_menu WHERE id = 600 AND @child_cnt = 0;

-- ---------------- 4. 回查（期望：29700 下 8 个页面；600 不存在；600 授权 0 行） ----------------
SELECT id, menu_name, parent_id, sort_order, is_visible
FROM sys_menu WHERE parent_id = 29700 ORDER BY sort_order;
SELECT COUNT(*) AS menu600_rows FROM sys_menu WHERE id = 600;
SELECT COUNT(*) AS grant600_rows FROM sys_role_menu WHERE menu_id = 600;

-- ---------------- 回滚 ----------------
-- UPDATE sys_menu SET parent_id = 600,
--        sort_order = CASE id WHEN 602 THEN 1 WHEN 604 THEN 2 WHEN 609 THEN 3 WHEN 617 THEN 4 WHEN 29626 THEN 5 END,
--        update_by = 'rollback410', update_time = NOW()
--  WHERE parent_id = 29700 AND id IN (602, 604, 609, 617, 29626);
-- UPDATE sys_menu SET menu_name = '医疗质量与安全', update_by = 'rollback410', update_time = NOW() WHERE id = 29700;
-- INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
--                       permission, is_frame, is_cache, is_visible, status, create_by, create_time, update_by,
--                       update_time, del_flag)
-- VALUES (600, 0, '病历与质控', 70, 1, '/emr-qc', NULL, 'emr.qc', 'Document', NULL, 0, 0, 1, 1,
--         'rollback410', NOW(), 'rollback410', NOW(), 0);
-- -- 授权按执行前备份逐行回插（备份：sys_role_menu 中 menu_id=600 的 6 行，role 1/14/15/16/26/27/41 中的持有者）
-- ============================================================================
