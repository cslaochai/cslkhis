-- =====================================================================
-- 281 护士长(10) 补「门诊排班-审核发布」按钮权限 outpatient:schedule:audit(2980)
-- ---------------------------------------------------------------------
-- 背景：
--   门诊排班发布态（G-15）审核发布按钮挂 v-perm='outpatient:schedule:audit'，
--   后端 /schedule/auditPublish、/schedule/auditWeek 也以同一 @PreAuthorize 收口。
--   此前该码只授给 系统管理员(1)/门诊办(25)/科主任(27)；护士长(10) 已持有
--   门诊排班页面(804)+增改删按钮(2120/2121/2122)与护理审核(2981)，唯独缺
--   门诊审核码 2980 → 看得到黄色「待审核」卡片却没有「审核发布」按钮。
--   本次按现场测试口径补授 2980。
--
-- 幂等：sys_role_menu 有 uk_role_menu(role_id,menu_id)，INSERT IGNORE 重复执行零变化。
-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min）或退出重登，旧权限集合才会刷新。
--
-- 说明：这是测试授权补码，不代表「护士长=门诊固定审核人」的正式组织口径；
--      科室固定审核人/审核岗建模另议（见排班审核岗设计）。
-- =====================================================================

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES (8920610140000000007, 10, 2980, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- 回查：护士长应同时持有 804(门诊排班页面) 与 2980(审核发布按钮)
-- SELECT m.id, m.menu_name, m.permission
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id = 10 AND m.id IN (804, 2980) ORDER BY m.id;
