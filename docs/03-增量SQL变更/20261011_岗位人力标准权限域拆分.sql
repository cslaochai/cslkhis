-- =====================================================================
-- 20261011 岗位人力标准权限域拆分：staff:schedule:* → staff:planRule:*
-- ---------------------------------------------------------------------
-- 背景：
--   「岗位出勤」(2935) 与「岗位人力标准」(2937) 原先共用同一套 staff:schedule:* 码，
--   于是 20261011「护士长排班授权」给护士长补出勤按钮码之后，她直连接口也能改人力标准
--   （菜单和按钮对她不可见，鉴权却放行）。人力标准是排班的分母（一个单元×班次×岗位
--   该配几个人），能排班不等于能改标准，两个动作必须分属两个权限域。
--
-- 三处联改（漏一处就是「按钮消失」或「接口 403」，且零报错）：
--   ① 本文件：sys_menu 页面行 2937 的 permission + 两个按钮行 + 角色授权；
--   ② 后端 StaffPlanRuleController 的三个 @PreAuthorize；
--   ③ 前端 StaffPlanRuleView.vue 的三个 v-perm。
--   本文件与 ②③ 同一批提交，不要只跑 SQL 不重启后端。
--
-- 未拆的（刻意留在 staff:schedule:*，因为它们服务的页面护士长本来就有）：
--   ShiftController（班次字典）→ 入口在门诊排班页(804)；
--   StaffDemandController 的 recalc/adjust → 入口在护理排班页(331)，是护士长的手工调需求；
--   StaffAttendanceController / StaffScheduleController → 岗位出勤本身。
--   ⚠️ 注意 demand 的 recalc/adjust 读的是人力标准算出来的需求，但它不写标准行，
--      所以不构成绕过本次拆分的口子。
--
-- 幂等：页面行 UPDATE 带 permission 前置条件；按钮行用「id OR menu_key OR permission」
--       三道键判重；授权行用「role_id+menu_id OR 本文件专属 id」判重，重复执行零变化。
-- ⚠️ 执行后必须清 Redis 权限缓存 his:perm:role:*（TTL 10min）或重启后端，否则旧集合仍生效。
-- =====================================================================

-- =====================================================================
-- 一、页面行 2937 改挂自己的 list 码
-- =====================================================================
UPDATE sys_menu
SET permission = 'staff:planRule:list', update_by = 'admin', update_time = NOW()
WHERE id = 2937
  AND permission = 'staff:schedule:list';

-- =====================================================================
-- 二、按钮码两行（父菜单 2937，串值逐字取自后端 @PreAuthorize）
-- ---------------------------------------------------------------------
-- 没有 :edit 码：新增与修改共用同一个 upsert 接口、同一个 :add 权限（与出勤同口径）。
-- =====================================================================
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 29371, '岗位人力标准-新增', 2937, 1, 3, NULL, NULL, 'org.staffPlanRule:add', NULL, 'staff:planRule:add',
       0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '新增与修改同走一个 upsert 接口', 1, 1
FROM DUAL
WHERE NOT EXISTS(SELECT 1
                 FROM sys_menu m
                 WHERE m.id = 29371
                    OR m.menu_key = 'org.staffPlanRule:add'
                    OR m.permission = 'staff:planRule:add');

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status,
                      create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT 29372, '岗位人力标准-删除', 2937, 2, 3, NULL, NULL, 'org.staffPlanRule:delete', NULL, 'staff:planRule:delete',
       0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), 0, '删除标准行（物理删）', 1, 1
FROM DUAL
WHERE NOT EXISTS(SELECT 1
                 FROM sys_menu m
                 WHERE m.id = 29372
                    OR m.menu_key = 'org.staffPlanRule:delete'
                    OR m.permission = 'staff:planRule:delete');

-- =====================================================================
-- 三、角色授权：持有页面 2937 的两个角色自动拿到全部按钮码
-- ---------------------------------------------------------------------
-- 超管(1)、医务科(26)。护士长(10)/科主任(27)/门诊办(25)/院领导(18) 都没有 2937，不授。
-- =====================================================================
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT s.nid, s.rid, s.mid, 'admin', NOW(), 'admin', NOW(), 1, 1
FROM (SELECT 1 AS rid, 29371 AS mid, 2026101140000000001 AS nid
      UNION ALL
      SELECT 1, 29372, 2026101140000000002
      UNION ALL
      SELECT 26, 29371, 2026101140000000003
      UNION ALL
      SELECT 26, 29372, 2026101140000000004) s
WHERE NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = s.rid AND rm.menu_id = s.mid);

-- =====================================================================
-- 四、回查（执行后逐条确认，key 没落进去 = 按钮永久隐藏 + 接口 403）
-- =====================================================================
-- 4.1 页面行已改码
-- SELECT id, menu_name, permission FROM sys_menu WHERE id IN (2937, 29371, 29372);
-- 4.2 两个角色的按钮码齐备
-- SELECT rm.role_id, GROUP_CONCAT(rm.menu_id ORDER BY rm.menu_id) ids
--   FROM sys_role_menu rm WHERE rm.menu_id IN (29371, 29372) GROUP BY rm.role_id;
-- 4.3 前端引用的 staff:planRule:* 必须 100% 能在 menu_type=3 里找到
-- SELECT permission FROM sys_menu WHERE menu_type = 3 AND del_flag = 0 AND permission LIKE 'staff:planRule%';
-- 4.4 全仓不能再有 staff:schedule:* 落在 2937 名下
-- SELECT id, permission FROM sys_menu WHERE parent_id = 2937 OR id = 2937;
