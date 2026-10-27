-- ============================================================================
-- 20261027 急诊排班审核独立菜单（医务科专用审核界面）+ 医务科收回急诊排班编辑页
-- ============================================================================
-- 背景：296-急诊排班菜单独立.sql 里 29611（急诊排班页）的授权是「复制员工排班家族」
--   来的，医务科(role 26) 因此持有了完整的急诊排班编辑页（页面+增改删 29611~29614 +
--   审核按钮 29615）。业务口径：急诊排班 = 科主任编制，医务科只做「备案审核」，
--   不该看到编辑界面（页面上编辑/删除按钮点了也是 403，纯困惑）。
--
-- 本变更：
--   A. 新菜单 29627「急诊排班审核」（2945 排班值守中心下，紧随 29611）：
--      页面 views/schedule/ErScheduleAuditView.vue，数据源 /system/staffSchedule/pendingApproval
--      （后端按「科室主档 schedule_auditor_id = 当前登录人」收口，只要求 staff:schedule:audit），
--      授权 admin + 医务科(26)。
--   B. 医务科(26) 收回 29611~29615（急诊排班页 + 增/改/删/审核按钮）：
--      收回后 role 26 不再持有 staff:schedule:list/add/edit/delete，
--      审核权限不受影响（29604 排班审批中心 + 新 29627 都带 staff:schedule:audit）。
--      科主任(role 27) 的 29611~29614 不动 —— 他们是编制人。
--
-- ⚠ 运维注意：
--   1) 执行后必须清权限 Redis 缓存，否则 10 分钟内不生效：
--      redis-cli -a 123456 --scan --pattern 'his:perm:role:*' | xargs redis-cli -a 123456 DEL
--      redis-cli -a 123456 --scan --pattern 'his:user:identity:*' | xargs redis-cli -a 123456 DEL
--   2) 幂等：sys_menu 按 ID 物理删再插（menu_key UNIQUE 不含 del_flag，软删占键位）；
--      sys_role_menu 按 (role_id, menu_id) 组合精确删再插（INSERT IGNORE 会被已存在组合
--      静默跳过）；id 显式给（sys_role_menu.id 无库默认值），雪花号段 8920610270* 与存量
--      8920610251*/8920610252* 不撞。
-- ============================================================================

-- ---------- A. 新菜单 29627 急诊排班审核 ----------
DELETE FROM sys_role_menu WHERE menu_id = 29627;
DELETE FROM sys_menu WHERE id = 29627;

INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time,
                      update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (29627, '急诊排班审核', 2945, 5, 2, '/er-schedule-audit', 'views/schedule/ErScheduleAuditView.vue',
        'org:erScheduleAudit', 'CircleCheck', 'staff:schedule:audit', 0, 0, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0,
        '医务科对急诊三科待审核排班的审核发布/驳回（待我审批队列，独立于科主任的急诊排班编辑页）', 1, 1);

DELETE FROM sys_role_menu WHERE role_id IN (1, 26) AND menu_id = 29627;
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES (8920610270000000001, 1, 29627, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (8920610270000000002, 26, 29627, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- B. 医务科收回急诊排班编辑族（页面+增改删+按钮，29615 权限已由 29627 覆盖） ----------
DELETE FROM sys_role_menu WHERE role_id = 26 AND menu_id IN (29611, 29612, 29613, 29614, 29615);

-- ---------- B2. 医务科只留「排班总览 + 急诊排班审核」 ----------
-- 首轮执行后实查（/system/role/getMenuIds?roleId=26）发现：role 26 从 20261011
-- 建角色起就直持 员工排班页(2935)+增改删按钮(29351~29353)+审核按钮(29603)，以及
-- 人力配置标准(2937 族)、排班字典管理(2939 族)、听班呼叫(29616 族)。
-- 业务口径（20261027 用户最终定）：医务科 = 排班总览(2938) + 急诊排班审核(29627)
-- + 急诊值班公开页(29605，全院只读)，其余排班域全部收回。
-- ⚠ 必须整族收回（页面+按钮一起）：saveRoleMenu/授权语义按「按钮带回父页面」，
--   只删页面不删按钮会自动补回。
DELETE FROM sys_role_menu WHERE role_id = 26 AND menu_id IN (
    2935, 29351, 29352, 29353, 29603,          -- 员工排班页 + 增改删 + 审核按钮
    2937, 29371, 29372,                         -- 人力配置标准页 + 按钮
    2939, 29391, 29392, 29393,                  -- 排班字典管理页 + 按钮
    29616, 29617, 29618, 29619);                -- 听班呼叫页 + 发起/应答/取消按钮

-- ---------- 回查 ----------
-- 1) 菜单在：SELECT id, menu_name, parent_id, sort_order, permission FROM sys_menu WHERE id = 29627;
-- 2) role 26 排班域只剩：2938(总览) + 29605(急诊值班公开) + 29627(急诊排班审核) + 2945/2982(目录)
--    SELECT menu_id FROM sys_role_menu WHERE role_id = 26 AND menu_id >= 29000 ORDER BY menu_id;
-- 3) role 26 权限码不含 add/edit/delete/list：
--    SELECT DISTINCT m.permission FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--    WHERE rm.role_id = 26 AND m.permission LIKE 'staff:schedule%';
--    预期只有 staff:schedule:audit
