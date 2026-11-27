-- ============================================================
-- 401 报表统计目录归位：绩效与成本核算 → 财务结算；病案统计上报 → 医疗质量与病案
-- 日期：2026-11-27
-- 背景：
--   原「报表统计」目录(1200)混入两类非只读报表的业务作业页，挂错业务域：
--   · 908 绩效与成本核算：科室月度成本/结余/绩效金额核算，是绩效财务作业（带写操作），
--     归「财务结算」目录(1000)；
--   · 2159 病案统计上报：病案室月报/年报报文生成、台账、报出/作废，是病案质控域作业，
--     归「医疗质量与病案」目录(29700)。
-- 边界：
--   · 只改两条菜单的 parent_id 与排序；菜单记录、权限码、接口、组件、角色授权一律不动；
--   · 子按钮 2134（908 新增）、2160/2161/2162（2159 生成/报出/作废）挂在页面菜单下，随父迁移；
--   · 授权关系 sys_role_menu 不变，避免产生孤儿菜单；
--   · 不改权限码命名（report:perf:list / report:statReport:list），改码牵动授权与前端 v-perm，超本次范围。
-- 落地后：清角色权限/身份缓存（Redis TTL 10min，SQL 改库不自动失效）：
--         清 his:perm:role:* 与 his:user:identity:*，或重新登录（前端重拉 /auth/info 与菜单树）。
-- 回滚：见文件末尾「回滚段」。
-- ============================================================

-- A 908 绩效与成本核算：报表统计(1200) → 财务结算(1000)，排序排在医保管理(4106, sort 60)之后
UPDATE sys_menu
SET parent_id = 1000,
    sort_order = 70,
    update_time = NOW()
WHERE id = 908
  AND parent_id = 1200;

-- B 2159 病案统计上报：报表统计(1200) → 医疗质量与病案(29700)，排序排在手卫生依从性(617, sort 65)之后
UPDATE sys_menu
SET parent_id = 29700,
    sort_order = 70,
    update_time = NOW()
WHERE id = 2159
  AND parent_id = 1200;

-- C 回查（可单独执行）
-- C1 归位后归属（期望：908 parent=1000；2159 parent=29700）
-- SELECT id, menu_name, parent_id, sort_order, permission, is_visible
--   FROM sys_menu WHERE id IN (908, 2159);

-- C2 两个目录下的子菜单（期望：908 出现在 1000 子级；2159 出现在 29700 子级）
-- SELECT id, menu_name, parent_id, sort_order FROM sys_menu
--   WHERE parent_id IN (1000, 29700) ORDER BY parent_id, sort_order;

-- ============================================================
-- 回滚段
--   1) 908 退回报表统计：
-- UPDATE sys_menu SET parent_id = 1200, sort_order = 4, update_time = NOW() WHERE id = 908;
--   2) 2159 退回报表统计：
-- UPDATE sys_menu SET parent_id = 1200, sort_order = 3, update_time = NOW() WHERE id = 2159;
-- ============================================================
