-- ============================================================================
-- 415 医保付费（907/909/910）从 报表统计(1200) 迁入 财务结算→医保管理(4106)
-- ============================================================================
-- 背景（2026-11-27）：907「医保付费」聚合壳（DRG/DIP 分组模拟 + 付费测算 + 特例单议）
--   长期挂在「报表统计(1200)」下，但它不是只读报表：
--     · 后端归属 his-ms-charge（财务结算域），与收费/结算/医保台账同模块；
--     · 页面含批量模拟、重跑、特例单议 草稿→提交→审核通过/驳回/撤回 等写操作与流程；
--     · 同域业务页（结算清单/政策配置/目录对照/扣款飞检/慢特病备案/合规台账）
--       本就归在「医保管理(4106)」，唯独付费测算被甩到报表目录。
--
-- 改动：
--   1) 907（聚合壳，可见）、909（医保付费结算，已并入壳隐藏）、910（DIP 分组模拟，
--      已并入壳隐藏）三行 parent_id 1200 → 4106，排序接在目录对照(60)之后；
--   2) 更新 907 备注，标注归属医保管理域。
--   不动：权限码 permission、路由 path、组件 component、menu_key、后端接口、
--         任何角色授权（sys_role_menu 按 menu_id 关联，目录平移不丢授权）。
--   遗留：report:drg:list / report:dip:list 前缀为早期按报表起的旧码，本次不收口，
--         避免与后端注解、前端 hasPerm、角色授权多处同步出错；后续单立脚本统一改 finance: 前缀。
--
-- 幂等：按 parent_id + id 判（重跑第二遍无行可更）。
-- 回滚见文件末尾。
-- ============================================================================

-- ---------------- 0. 前置核对 ----------------
SELECT id, menu_name, parent_id, sort_order, menu_type, is_visible, permission
FROM sys_menu WHERE id IN (907, 909, 910) ORDER BY id;

SELECT id, menu_name, parent_id, sort_order
FROM sys_menu WHERE parent_id = 4106 ORDER BY sort_order;

-- ---------------- 1. 目录平移（医保管理内排序：目录对照60 → 医保付费65，隐藏跟随 66/67） ----------------
UPDATE sys_menu
SET parent_id  = 4106,
    sort_order = CASE id
                     WHEN 907 THEN 65
                     WHEN 909 THEN 66
                     WHEN 910 THEN 67
                 END,
    update_by   = 'sql415',
    update_time = NOW()
WHERE parent_id = 1200
  AND id IN (907, 909, 910);

-- ---------------- 2. 907 备注更新 ----------------
UPDATE sys_menu
SET remark    = '医保管理域：聚合壳，DRG / DIP 分组模拟 + 医保结算（付费测算 + 特例单议）页签；909/910 已并入本壳，记录与权限码保留',
    update_by = 'sql415',
    update_time = NOW()
WHERE id = 907;

-- ---------------- 3. 回查（期望：4106 下含 907/909/910；1200 下不再有这三行） ----------------
SELECT id, menu_name, parent_id, sort_order, is_visible, permission
FROM sys_menu WHERE parent_id = 4106 ORDER BY sort_order;

SELECT id, menu_name, parent_id FROM sys_menu WHERE id IN (907, 909, 910);

SELECT COUNT(*) AS left_in_report
FROM sys_menu WHERE parent_id = 1200 AND id IN (907, 909, 910);

-- ---------------- 回滚 ----------------
-- UPDATE sys_menu
-- SET parent_id = 1200,
--     sort_order = CASE id WHEN 907 THEN 20 WHEN 909 THEN 22 WHEN 910 THEN 21 END,
--     update_by  = 'rollback415', update_time = NOW()
-- WHERE parent_id = 4106 AND id IN (907, 909, 910);
-- UPDATE sys_menu
-- SET remark = '聚合壳：DRG / DIP 分组 + 医保结算页签；909 已并入',
--     update_by = 'rollback415', update_time = NOW()
-- WHERE id = 907;
-- ============================================================================
