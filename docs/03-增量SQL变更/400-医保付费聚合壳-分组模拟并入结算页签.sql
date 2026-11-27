-- ============================================================
-- 400 「医保付费」聚合壳：分组模拟 + 医保付费结算收敛为一个多页签入口
-- 日期：2026-11-27
-- 背景：
--   报表统计目录下 907 分组模拟（DRG/DIP 分组器）与 909 医保付费结算两个页面同属医保付费域，
--   侧栏并列成两条菜单。后端仍是两套独立功能（分组测算 / 结算盈亏），接口、表、权限码互不合并；
--   仅在前端聚合为 GroupSimView 壳，DRG / DIP / 医保结算三个页签切换。
-- 前端配套（已改，纯组件复用，后端 Java 零改动）：
--   · GroupSimView.vue 三页签：DrgSimView / DipSimView / MedpaySettleView（lazy），按权限码显隐页签；
--   · 路由 /drgsim 指向壳；/medpaysettle 重定向到 /drgsim?tab=settle；
--   · 三个子组件、各自接口、权限码均原样保留。
-- 菜单变更：
--   · 907 作为聚合入口：改名「医保付费」，path /drgsim、component drg/GroupSimView、
--     权限码 report:drg:list 不变；
--   · 909 侧栏隐藏退役（is_visible=0，不物理删除）；授权与权限码 medpay:settle:list 保留——
--     壳内「医保结算」页签调医保结算接口仍由后端按该码鉴权。
-- 鉴权对齐：
--   · 壳入口由 907 菜单可见性控制，页签内接口按各自权限码鉴权；
--   · 角色 16（医保结算员）原持 909 但不持 907，退役 909 后将看不到入口；补授 907 菜单，
--     使其从壳进入结算页签（DRG/DIP 页签因无对应码由前端隐藏）；
--   · 角色 17（审计统计员）持 907/910、不持 909，结算页签前端隐藏，不补授 909。
-- 范围边界：
--   · 只动 907、909 两条菜单记录与 16→907 一条授权；其他菜单、角色授权一律不碰；
--   · 909 不物理删除（del_flag 保持 0），保留回滚余地。
-- 落地后：清角色权限/身份缓存（Redis TTL 10min，SQL 改库不自动失效）：
--         清 his:perm:role:* 与 his:user:identity:*，或重新登录（前端重拉 /auth/info 与菜单树）。
-- 回滚：见文件末尾「回滚段」。
-- ============================================================

-- A 907 聚合入口：改名「医保付费」（component / path / permission 不变）
UPDATE sys_menu
SET menu_name = '医保付费',
    update_time = NOW()
WHERE id = 907
  AND component = 'drg/GroupSimView';

-- B 909 侧栏隐藏退役（授权、权限码、记录均保留；仅不再作为独立入口出现）
UPDATE sys_menu
SET is_visible = 0,
    update_time = NOW()
WHERE id = 909
  AND is_visible = 1;

-- C 授权对齐：医保结算员(16) 补授壳入口 907（结算页签仍由 909 码鉴权）
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001601, 16, 907, 'admin', NOW(), 'admin', NOW(), 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu WHERE role_id = 16 AND menu_id = 907
);

-- D 回查（可单独执行）
-- D1 两条菜单现状（期望：907 名称=医保付费 / vis=1 / permission=report:drg:list；
--                      909 vis=0，permission 仍为 medpay:settle:list）
-- SELECT id, menu_name, parent_id, path, component, permission, menu_type, is_visible, status, del_flag
--   FROM sys_menu WHERE id IN (907, 909);

-- D2 授权现状（期望：907 = 1 / 16 / 17；909 = 1 / 16；910 = 1 / 17）
-- SELECT rm.menu_id, GROUP_CONCAT(rm.role_id ORDER BY rm.role_id) roles
--   FROM sys_role_menu rm WHERE rm.menu_id IN (907, 909, 910) GROUP BY rm.menu_id;

-- ============================================================
-- 回滚段
--   1) 907 恢复原名：
-- UPDATE sys_menu SET menu_name = '分组模拟', update_time = NOW() WHERE id = 907;
--   2) 909 恢复侧栏显示：
-- UPDATE sys_menu SET is_visible = 1, update_time = NOW() WHERE id = 909;
--   3) 摘除 16→907 补授：
-- DELETE FROM sys_role_menu WHERE role_id = 16 AND menu_id = 907 AND id = 9210000000000001601;
--   4) 前端路由/壳回退：恢复 /medpaysettle → MedpaySettleView，删除壳内结算页签。
-- ============================================================
