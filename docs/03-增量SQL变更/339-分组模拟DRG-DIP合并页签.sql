-- ============================================================
-- 339 分组模拟入口合并：DRG / DIP 两个独立菜单收敛为一个「分组模拟」页签页
-- 日期：2026-11-17
-- 背景：
--   原 907「DRG-DIP 分组模拟」与 910「DIP 分组模拟」两个页面结构同构、前后相邻，
--   侧栏看起来像两个重复菜单。实际后端是两套独立分组器（DRG 病组 / DIP 病种分值），
--   接口、表、权限码均不同，不做后端合并；仅在前端聚合为一个壳页面，顶部两个页签切换。
-- 前端配套（已改，纯组件复用，后端 Java 零改动）：
--   · 新建 source/front/src/views/drg/GroupSimView.vue：el-tabs 内嵌现有 DrgSimView / DipSimView（lazy）；
--   · 路由 /drgsim 指向 GroupSimView；删除独立 /dipsim 静态路由；
--   · DrgSimView.vue / DipSimView.vue / 各自 Edit 弹窗 / api/drg.js / api/dip.js 均原样保留。
-- 菜单变更：
--   · 907 作为聚合入口：改名「分组模拟」，component 指向聚合壳；path /drgsim、权限码 report:drg:list 不变；
--   · 910 侧栏隐藏退役（is_visible=0，不物理删除）；其授权与权限码 report:dip:list 保留——
--     聚合壳 DIP 页签调 /report/dip/* 仍由后端按该码鉴权。
-- 鉴权核对：
--   · 权限码下发 SQL（SysUserMapper.selectPermissionsByRoleCode）只过滤 del_flag=0，
--     不过滤 status / is_visible，故 910 隐藏后 report:dip:list 照常下发，DIP 页签不会 403；
--   · 907 / 910 授权角色完全一致（role_id 1 / 17 / 18 同时持有两码），无需按权限隐藏页签。
-- 范围边界：
--   · 只动 907、910 两条菜单记录；其他菜单、角色授权一律不碰；
--   · 910 不物理删除（del_flag 保持 0），保留回滚余地。
-- 落地后：清角色权限/身份缓存（Redis TTL 10min，SQL 改库不自动失效）：
--         清 his:perm:role:* 与 his:user:identity:*，或重新登录（前端重拉 /auth/info 与菜单树）。
-- 回滚：见文件末尾「回滚段」。
-- ============================================================

-- A 907 聚合入口：改名 + 组件指向聚合壳（path /drgsim、permission report:drg:list、menu_key 不变）
UPDATE sys_menu
SET menu_name = '分组模拟',
    component = 'drg/GroupSimView',
    update_time = NOW()
WHERE id = 907
  AND component = 'drg/DrgSimView';

-- B 910 侧栏隐藏退役（授权、权限码、记录均保留；仅不再作为独立入口出现）
UPDATE sys_menu
SET is_visible = 0,
    update_time = NOW()
WHERE id = 910
  AND is_visible = 1;

-- C 回查（可单独执行）
-- C1 两条菜单现状（期望：907 名称=分组模拟 / component=drg/GroupSimView / vis=1；
--                      910 vis=0，permission 仍为 report:dip:list）
-- SELECT id, menu_name, path, component, permission, menu_type, is_visible, status, del_flag
--   FROM sys_menu WHERE id IN (907, 910);

-- C2 910 授权仍在（期望 role_id 1 / 17 / 18 三行，DIP 页签鉴权可用）
-- SELECT rm.role_id, m.permission FROM sys_role_menu rm
--   JOIN sys_menu m ON m.id = rm.menu_id WHERE m.id = 910 ORDER BY rm.role_id;

-- ============================================================
-- 回滚段
--   1) 907 恢复原名与原组件：
-- UPDATE sys_menu SET menu_name = 'DRG-DIP 分组模拟', component = 'drg/DrgSimView', update_time = NOW()
--   WHERE id = 907;
--   2) 910 恢复侧栏显示：
-- UPDATE sys_menu SET is_visible = 1, update_time = NOW() WHERE id = 910;
--   3) 前端路由/壳回退：删除 GroupSimView.vue，/drgsim 指回 DrgSimView，恢复 /dipsim → DipSimView。
-- ============================================================
