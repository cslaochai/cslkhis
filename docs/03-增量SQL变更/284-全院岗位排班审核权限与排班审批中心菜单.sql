-- =====================================================================
-- 284 全院岗位排班「审核发布」权限 + 独立「排班审批中心」菜单
-- ---------------------------------------------------------------------
-- 背景：
--   全院岗位排班（菜单 2935 员工排班）此前只有增改删三个权限码
--   （staff:schedule:add / edit / delete），没有审核码 —— 排完的班停在草稿态，
--   没人能放行（在岗聚合只统计 publish_status IN (2,4)）。
--   本次补上审核链路的权限与入口：
--     ① 按钮权限  staff:schedule:audit  →「员工排班」页里的审核发布/驳回按钮
--     ② 菜单      排班审批中心           →「待我审批」聚合页（逐条/批量审核）
--   两者共用同一个权限码 staff:schedule:audit，与护理排班的
--   331 护理排班 / 2981 病区护理排班-审核发布 同一套做法。
--
-- 授权对象（与既有 staff:schedule:* 持有者一致）：
--   1 系统管理员(10012) / 10 护士长(10021) / 26 医务科(10036) / 27 科主任(10037)
--   这四个角色已持有 2945 排班值守中心目录 + 2935 员工排班页面 + 29351/29352/29353
--   出勤增改删按钮，祖先目录一并授权，无需再补 2945。
--
-- 🔑 权限码只是「能不能进审批页/看得到按钮」，真正谁能审哪条排班由科室主档
--    sys_department.schedule_auditor_id 决定（见 283 增量），后端
--    assertScheduleAuditor(deptId) 校验；系统管理员(10012)全局豁免。
--
-- 幂等：sys_menu.id / sys_menu.menu_key 为主键与唯一键，重复执行前请先确认
--       id 29603/29604 未被占用；sys_role_menu 有 uk_role_menu(role_id,menu_id)，
--       INSERT IGNORE 重复执行零变化。
-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min）或退出重登，旧权限集合才会刷新。
-- =====================================================================

-- ---------- ① 按钮权限：员工排班-审核发布 ----------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                       permission, is_frame, is_cache, is_visible, status,
                       create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (29603, '员工排班-审核发布', 2935, 4, 3, NULL, NULL, 'org.staffSchedule:audit', NULL,
        'staff:schedule:audit', 0, 1, 0, 1,
        'admin', NOW(), 'admin', NOW(), 0, '全院岗位排班：待审核→已发布 / 驳回（科室指定审核人）', 1, 1);

-- ---------- ② 菜单：排班审批中心 ----------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                       permission, is_frame, is_cache, is_visible, status,
                       create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (29604, '排班审批中心', 2945, 5, 2, '/schedule-approve', 'views/schedule/ScheduleApproveView.vue',
        'org.scheduleApprove', 'Stamp',
        'staff:schedule:audit', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '待我审批：全院岗位排班的待审聚合（逐条/批量审核发布与驳回）', 1, 1);

-- ---------- ③ 角色授权：1 系统管理员 / 10 护士长 / 26 医务科 / 27 科主任 ----------
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
  (8920610140000000008,  1, 29603, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610140000000009,  1, 29604, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610140000000010, 10, 29603, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610140000000011, 10, 29604, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610140000000012, 26, 29603, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610140000000013, 26, 29604, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610140000000014, 27, 29603, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610140000000015, 27, 29604, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- 回查：这四个角色应同时持有 2935 员工排班 + 29603 审核按钮 + 29604 审批中心
-- SELECT rm.role_id, m.id, m.menu_name, m.permission
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id IN (1,10,26,27) AND m.id IN (2935, 29603, 29604)
--  ORDER BY rm.role_id, m.id;
