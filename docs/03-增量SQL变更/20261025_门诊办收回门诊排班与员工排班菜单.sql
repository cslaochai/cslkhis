-- =====================================================================
-- 20261025 排班审批中心权限收口：门诊办只审门诊排班
-- ---------------------------------------------------------------------
-- 背景（现场反馈）：
--   1. 门诊办是「门诊排班审核岗」，不是「全院岗位排班审核岗」——
--      员工排班（staff:schedule:*）的审核归医务科(26)，门诊办不该看到。
--   2. 排班审批中心(29604) 菜单原权限码 = staff:schedule:audit，
--      收回门诊办的员工审核权后，如果不改菜单权限码，门诊办连审批中心入口都看不到。
--      菜单入口应改为「登录即可见」，后端两个待审接口各自 @PreAuthorize 收口：
--        · /staff-schedule/pending  → staff:schedule:audit（医务科/护士长）
--        · /appoint/schedule/pending → outpatient:schedule:audit（门诊办/科主任）
--   3. 前端按权限动态显隐 tab：有 staff:schedule:audit 才显示「全院岗位排班」tab，
--      有 outpatient:schedule:audit 才显示「门诊排班」tab（ScheduleApproveView.vue 已改）。
--   4. 「变更日志」tab 从审批中心拿掉，挪到排班总览(2938/ScheduleOverview.vue)。
--
-- 护理排班审核：护理部排班审核角色(29/10039)，不在本审批中心范围内（护理排班页内自审）。
--
-- 本次动作：
--   A. 29604 菜单 permission 改为 NULL（登录即可见）
--   B. 门诊办(25) 收回 804/2120/2121/2122（门诊排班维护）+ 2935（员工排班页面）+ 29603（员工审核按钮）
--   C. 门诊办(25) 保留：2945 父目录 / 2938 排班总览 / 29604 审批中心入口 / 2980 门诊审核按钮
--
-- 幂等：UPDATE / DELETE 带条件，重复执行零变化。
-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min）或退出重登。
-- =====================================================================

-- A. 审批中心菜单入口：登录即可见（后端接口各自鉴权）+ 改名为「门诊排班审核」
UPDATE sys_menu SET permission = NULL, menu_name = '门诊排班审核', update_time = NOW() WHERE id = 29604;

-- B. 收回门诊办的排班维护入口（门诊排班页面+按钮、员工排班页面）
DELETE FROM sys_role_menu WHERE role_id = 25 AND menu_id IN (804, 2120, 2121, 2122, 2935);

-- B2. 收回门诊办的员工排班审核按钮权限码（staff:schedule:audit）
DELETE FROM sys_role_menu WHERE role_id = 25 AND menu_id = 29603;

-- C. 回查：门诊办(25) 在排班值守中心(2945) 下应只剩：
--    2938 排班总览 / 29604 排班审批中心（页面）/ 2980 门诊排班-审核发布（按钮）
-- SELECT m.id, m.menu_name, m.menu_type, m.permission
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id = 25 AND (m.parent_id = 2945 OR m.id = 29604 OR m.id = 2980)
--  ORDER BY m.sort_order;

-- D. Redis 权限缓存（由运维执行或重启后端）
--    redis-cli -a <pwd> --scan --pattern 'his:perm:role:*' | xargs redis-cli -a <pwd> del
