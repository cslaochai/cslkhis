-- =====================================================================
-- 20261026 医务科收回「门诊排班审核」菜单入口
-- ---------------------------------------------------------------------
-- 背景：门诊排班审核（outpatient:schedule:audit）归门诊办(25)/科主任(27)/超管(1)，
--       医务科(26) 的审核范围是员工排班(staff:schedule:audit)/急诊排班/总值班(org:duty:audit)，
--       不含门诊排班。29604 菜单 permission=NULL（登录即可见），不收回授权则医务科
--       也能进页面——但页面上按钮 v-perm='outpatient:schedule:audit' 全隐藏、接口 403，
--       是一个「能进不能动」的半残入口，按业务口径直接隐藏。
--
-- 动作：删除 医务科(26) × 29604（门诊排班审核）的授权行。
--
-- 幂等：DELETE 带 role_id + menu_id 双条件，重复执行零变化。
-- ⚠️ 执行后必须清 Redis 权限缓存（TTL 10min）或退出重登，否则旧快照仍显示该菜单：
--      redis-cli -a <pwd> --scan --pattern 'his:perm:role:*'      | xargs redis-cli -a <pwd> del
--      redis-cli -a <pwd> --scan --pattern 'his:user:identity:*'  | xargs redis-cli -a <pwd> del
--      或重启后端。
-- =====================================================================

DELETE FROM sys_role_menu WHERE role_id = 26 AND menu_id = 29604;

-- ===================== 回查（应返回 0 行） =====================
-- SELECT rm.role_id, m.id, m.menu_name
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id = 26 AND m.id = 29604;
