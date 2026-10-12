-- =====================================================================
-- 20261011 排班菜单授权：护士长补齐「门诊排班/岗位出勤/排班总览」 + 新增医务科可登录账号
-- ---------------------------------------------------------------------
-- 背景：
--   ① 20261011「排班权限域拆分」只落到 list 这一档。后端 ShiftController /
--      StaffScheduleController / StaffAttendanceController / StaffPlanRuleController，
--      以及前端 StaffScheduleView、StaffPlanRuleView 的按钮一直在用
--      staff:schedule:add / :edit / :delete 三个码，但 sys_menu 里从来没有对应行
--      → 任何角色（含超管）都拿不到 → 「整周复制/新增/修改/删除」按钮永久隐藏、
--        接口一律 403，而现场现象是「按钮不见了」，零报错。本次补齐三个按钮行。
--   ② 护士长(10021) 此前只持有护理排班(331)，排班中心另外 6 个页面一个都没有；
--        本次按业务口径补 门诊排班(804)+增改删按钮、岗位出勤(2935)+三个按钮、
--        排班总览(2938)。岗位人力标准(2937) 仍留在医务科，不授予护士长。
--        护士长 data_scope=3（本部门），所以拿到的是本科室的排班与出勤，不是全院。
--   ③ 医务科(10036) 建了角色和菜单授权，但没有任何人挂这个岗位 → 登录不了。
--        本次补一个可登录账号（员工档 + 账号 + 岗位行三件套）。
--
-- 幂等：sys_menu 有 uk_menu_key、sys_role_menu 有 uk_role_menu(role_id,menu_id)、
--       sys_employee 有 uk_emp_code、sys_user 有 uk_user_name、
--       sys_employee_post 有 uk_emp_role_dept，一律 INSERT IGNORE + 固定 id，重复执行零变化。
-- ⚠️ 执行后必须清 Redis 权限缓存 his:perm:role:*（TTL 10min）或重启后端，否则旧集合仍生效。
-- =====================================================================

-- =====================================================================
-- 一、补「岗位出勤」三个按钮码（父菜单 2935，码值逐字取自后端 @PreAuthorize）
-- =====================================================================
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                             permission, is_frame, is_cache, is_visible, status,
                             create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (29351, '岗位出勤-新增', 2935, 1, 3, NULL, NULL, 'org.staffSchedule:add', NULL, 'staff:schedule:add', 0, 1, 0, 1,
        'admin', NOW(), 'admin', NOW(), 0, '整周复制与新增出勤行', 1, 1),
       (29352, '岗位出勤-修改', 2935, 2, 3, NULL, NULL, 'org.staffSchedule:edit', NULL, 'staff:schedule:edit', 0, 1, 0, 1,
        'admin', NOW(), 'admin', NOW(), 0, '改岗/换班与出勤登记维护', 1, 1),
       (29353, '岗位出勤-删除', 2935, 3, 3, NULL, NULL, 'org.staffSchedule:delete', NULL, 'staff:schedule:delete', 0, 1, 0, 1,
        'admin', NOW(), 'admin', NOW(), 0, '删除出勤行', 1, 1);

-- =====================================================================
-- 二、角色授权
-- ---------------------------------------------------------------------
-- 护士长(10)   → 门诊排班(804)+按钮(2120/2121/2122)、岗位出勤(2935)+按钮(29351-29353)、排班总览(2938)
-- 医务科(26)   → 岗位出勤的三个按钮（页面 2935/2937 早已授予，只缺按钮码）
-- 科主任(27)   → 岗位出勤的三个按钮（同上，页面 2935 已授予）
-- 超管(1)      → 岗位出勤的三个按钮（超管本就应当持有全部菜单行）
--
-- ⚠️ 本文件留下的口径缺口（已在同日的「岗位人力标准权限域拆分.sql」收口）：
--    岗位出勤(2935) 与 岗位人力标准(2937) 当时共用同一套 staff:schedule:* 码，
--    护士长拿到这三个码后，直连接口也能改人力标准（菜单与按钮对她不可见）。
--    隔离动作是「2937 那套码拆成 staff:planRule:* + 后端 @PreAuthorize + 前端 v-perm」三处联改，
--    单改本文件会变成「鉴权串与菜单权限串不匹配」，所以另起一个文件同批做完。
-- =====================================================================
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES (2026101130000000001, 10, 804, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000002, 10, 2120, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000003, 10, 2121, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000004, 10, 2122, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000005, 10, 2935, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000006, 10, 29351, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000007, 10, 29352, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000008, 10, 29353, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000009, 10, 2938, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000010, 26, 29351, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000011, 26, 29352, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000012, 26, 29353, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000013, 27, 29351, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000014, 27, 29352, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000015, 27, 29353, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000016, 1, 29351, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000017, 1, 29352, 'admin', NOW(), 'admin', NOW(), 1, 1),
       (2026101130000000018, 1, 29353, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- =====================================================================
-- 三、医务科(10036) 可登录账号：谭一鸣 / tanyiming / 123456
-- ---------------------------------------------------------------------
-- 三件套缺一即登录失败：账号 status=1 + 有角色 + 在 sys_employee_post 上有该角色的科室岗位。
-- 岗位落在 医务科(19580077)，is_primary=1；口令是其它账号同一个 bcrypt(123456)。
-- =====================================================================
INSERT IGNORE INTO sys_employee (id, emp_code, emp_name, emp_type, gender, birth_date, hire_date, id_card, phone, email,
                                 dept_id, dept_name, title, position, specialty, education, avatar, is_expert, expert_price,
                                 status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (8900000000000080001, 'YW001', '谭一鸣', 6, 1, '1979-06-15', '2010-07-01', '430103197906152015', '13800008001',
        'tanyiming@cslk-his.local', '19580077', '医务科', '副主任科员', '医务科管理岗', '医疗质量与人力调配管理', '本科',
        NULL, 0, 0.00, 1, 'admin', NOW(), 'admin', NOW(), 0, '医务科测试账号（岗位出勤/人力标准/排班总览）', 1, 1);

INSERT IGNORE INTO sys_user (id, user_name, password, real_name, emp_id, user_type, patient_id, openid, avatar,
                             last_login_time, last_login_ip, login_count, password_update_time, status,
                             create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (8900000000000080001, 'tanyiming', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '谭一鸣',
        8900000000000080001, 1, NULL, NULL, NULL, NULL, NULL, 0, '2026-06-08 01:30:00', 1,
        'admin', NOW(), 'admin', NOW(), 0, '医务科测试账号，口令 123456', 1, 1);

INSERT IGNORE INTO sys_employee_post (id, employee_id, role_id, dept_id, is_primary, effective_date, expire_date,
                                      create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES (8900000000000081001, 8900000000000080001, 26, '19580077', 1, '2026-10-11', NULL,
        'admin', NOW(), 'admin', NOW(), 1, 1);

-- =====================================================================
-- 四、回查
-- =====================================================================
-- 4.1 三个按钮码必须存在且 menu_key 唯一
-- SELECT id, menu_name, menu_key, permission FROM sys_menu WHERE id IN (29351, 29352, 29353);
-- 4.2 护士长应持有排班中心 4 个页面 + 6 个按钮
-- SELECT m.id, m.menu_name, m.permission FROM sys_role_menu rm
--   JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id = 10 AND m.parent_id IN (2945, 804, 2935) ORDER BY m.id;
-- 4.3 前端引用的 staff:schedule:* 必须 100% 能在 menu_type=3 里找到
-- SELECT permission FROM sys_menu WHERE menu_type = 3 AND del_flag = 0 AND permission LIKE 'staff:schedule%';
-- 4.4 医务科账号三件套齐备
-- SELECT u.user_name, u.real_name, u.status, e.emp_code, p.role_id, p.dept_id, p.is_primary
--   FROM sys_user u JOIN sys_employee e ON e.id = u.emp_id
--   JOIN sys_employee_post p ON p.employee_id = e.id
--  WHERE u.user_name = 'tanyiming';
