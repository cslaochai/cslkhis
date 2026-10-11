-- =====================================================================
-- 20261011 排班模块：权限域拆分（A/B）+ 补充管理角色（A）
-- ---------------------------------------------------------------------
-- 背景（排班模块重构方案 §P-A/B）：
--   原 org:schedule:* 一个权限域笼统罩住「门诊排班(804) + 岗位出勤(2935)
--   + 岗位人力标准(2937) + 排班总览(2938)」四个语义完全不同的菜单。
--   谁拿到 org:schedule 就等于同时拿到「给全院排出勤」和「改医生号源」，
--   角色与菜单无法一一对应，跨域越权。
--
-- 拆分目标（对齐标准 HIS，护理域 nursing:schedule:* 本就独立，本次不动）：
--   outpatient:schedule:*  —— 门诊排班/号源（804 及其增改删按钮）
--   staff:schedule:*       —— 岗位出勤 + 岗位人力标准（2935 / 2937）
--   schedule:overview:list —— 排班总览，只读聚合（2938）
--   nursing:schedule:*     —— 护理排班（331，已独立，不动）
--
-- ⚠️ 本 SQL 只改「库」这一半。权限域是三处联动，必须同批改：
--     ① 库 sys_menu.permission（本文件）
--     ② 后端 @PreAuthorize("hasAuthority('...')")
--     ③ 前端 v-perm="'...'"
--    只改任意一个 = 鉴权串与菜单权限串不匹配 → 按钮消失 / 接口 403。
--
-- ⚠️ 执行后必须清 Redis 权限缓存 his:perm:role:*（或重启后端），
--    否则旧的 / 新的权限在 TTL 10min 内仍按旧值生效。
-- =====================================================================

-- =====================================================================
-- 一、B：权限域拆分（menu_key 是路由标识，一律不动；只改 permission 串）
-- =====================================================================

-- 1.1 门诊排班（804）及其三个按钮 → outpatient:schedule:*
UPDATE sys_menu SET permission = 'outpatient:schedule:list' WHERE id = 804  AND permission = 'org:schedule:list';
UPDATE sys_menu SET permission = 'outpatient:schedule:add'    WHERE id = 2120 AND permission = 'org:schedule:add';
UPDATE sys_menu SET permission = 'outpatient:schedule:edit'   WHERE id = 2121 AND permission = 'org:schedule:edit';
UPDATE sys_menu SET permission = 'outpatient:schedule:delete' WHERE id = 2122 AND permission = 'org:schedule:delete';

-- 1.2 岗位出勤(2935) / 岗位人力标准(2937) → staff:schedule:list
UPDATE sys_menu SET permission = 'staff:schedule:list' WHERE id = 2935 AND permission = 'org:schedule:list';
UPDATE sys_menu SET permission = 'staff:schedule:list' WHERE id = 2937 AND permission = 'org:schedule:list';

-- 1.3 排班总览(2938) → schedule:overview:list（管理层只读）
UPDATE sys_menu SET permission = 'schedule:overview:list' WHERE id = 2938 AND permission = 'org:schedule:list';

-- 1.4 总值班排班(806)/值守点位(2936) 属 org:duty:* 域，不在本次排班域拆分范围


-- =====================================================================
-- 二、A：补充三个管理角色
-- ---------------------------------------------------------------------
-- 缺口：20261010 全量收口按 permission LIKE 'org:%' 收回后，门诊排班(804)、
--       岗位出勤(2935)、岗位人力标准(2937) 实际上只剩超管(role 1)能操作，
--       院领导(18) 仅有总览(2938)、护士长(10) 仅有护理排班(331)。
--       日常排班没有对应的业务管理岗，本次补齐。
--
-- staff_type 取值说明（role.staff_type 表示「谁适合兼任这个角色」的职业口径）：
--   门诊办 / 医务科 = 6 行政其他（院级职能处室，不属某个临床职业）
--   科主任         = 1 医生（科室负责人由医师担任，与护士长=2 口径对齐）
-- data_scope 取值：1=全部数据（院级职能处室）/ 3=本部门数据（科主任只看本科室）
-- =====================================================================

INSERT IGNORE INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                            create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('25', '10035', '门诊办', 1, 6, 1, 25, 1, 'admin', 'admin', 0, '门诊排班/号源的业务主管岗（院级职能处室），data_scope=1 全院', '1', '1');

INSERT IGNORE INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                            create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('26', '10036', '医务科', 1, 6, 1, 26, 1, 'admin', 'admin', 0, '全院岗位出勤/岗位人力标准的主管岗，data_scope=1 全院', '1', '1');

INSERT IGNORE INTO sys_role (id, role_code, role_name, role_type, staff_type, data_scope, sort_order, status,
                            create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('27', '10037', '科主任', 1, 1, 3, 27, 1, 'admin', 'admin', 0, '本科室门诊排班+岗位出勤负责人，data_scope=3 本部门', '1', '1');


-- =====================================================================
-- 三、A：给新角色授予菜单（sys_role_menu 无自增，必须显式给 id）
-- ---------------------------------------------------------------------
-- 按项目约定「新增角色必带三样」：
--   ① 祖先目录一并授权 → 门户(100) / 排班中心(2945)
--   ② 消息待办(102)（Header 每 5s 轮询，缺了 header 会崩）+ 工作台(101)
--   ③ ⚠️ 真正能登录还需 sys_employee_role + sys_employee_post 岗位行
--      （没有岗位行会被拒绝登录）—— 那是员工数据，不在本脚本范围，见文档说明
--
-- 业务范围：
--   门诊办(25) → 门诊排班(804)+增改删(2120/2121/2122) + 总览(2938)
--   医务科(26) → 岗位出勤(2935) + 岗位人力标准(2937) + 总览(2938)
--   科主任(27) → 门诊排班(804)+增改删 + 岗位出勤(2935) + 总览(2938)  （本部门）
--   院领导(18) → 维持「只看总览」不变（20261011 已补 2938），不再扩权
-- =====================================================================

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id) VALUES
-- ---- 门诊办(25)：通用 100/101/102 + 排班中心 + 门诊排班域 + 总览 ----
(8920610100000000106, 25, 100,  'admin', 'admin', 1, 1),
(8920610100000000107, 25, 101,  'admin', 'admin', 1, 1),
(8920610100000000108, 25, 102,  'admin', 'admin', 1, 1),
(8920610100000000109, 25, 2945, 'admin', 'admin', 1, 1),
(8920610100000000110, 25, 804,  'admin', 'admin', 1, 1),
(8920610100000000111, 25, 2120, 'admin', 'admin', 1, 1),
(8920610100000000112, 25, 2121, 'admin', 'admin', 1, 1),
(8920610100000000113, 25, 2122, 'admin', 'admin', 1, 1),
(8920610100000000114, 25, 2938, 'admin', 'admin', 1, 1),
-- ---- 医务科(26)：通用 + 排班中心 + 岗位出勤域 + 总览 ----
(8920610100000000115, 26, 100,  'admin', 'admin', 1, 1),
(8920610100000000116, 26, 101,  'admin', 'admin', 1, 1),
(8920610100000000117, 26, 102,  'admin', 'admin', 1, 1),
(8920610100000000118, 26, 2945, 'admin', 'admin', 1, 1),
(8920610100000000119, 26, 2935, 'admin', 'admin', 1, 1),
(8920610100000000120, 26, 2937, 'admin', 'admin', 1, 1),
(8920610100000000121, 26, 2938, 'admin', 'admin', 1, 1),
-- ---- 科主任(27)：通用 + 排班中心 + 门诊排班域 + 岗位出勤 + 总览（本部门）----
(8920610100000000122, 27, 100,  'admin', 'admin', 1, 1),
(8920610100000000123, 27, 101,  'admin', 'admin', 1, 1),
(8920610100000000124, 27, 102,  'admin', 'admin', 1, 1),
(8920610100000000125, 27, 2945, 'admin', 'admin', 1, 1),
(8920610100000000126, 27, 804,  'admin', 'admin', 1, 1),
(8920610100000000127, 27, 2120, 'admin', 'admin', 1, 1),
(8920610100000000128, 27, 2121, 'admin', 'admin', 1, 1),
(8920610100000000129, 27, 2122, 'admin', 'admin', 1, 1),
(8920610100000000130, 27, 2935, 'admin', 'admin', 1, 1),
(8920610100000000131, 27, 2938, 'admin', 'admin', 1, 1);


-- =====================================================================
-- 四、Redis 权限缓存
-- ---------------------------------------------------------------------
-- 必须删 his:perm:role:*（TTL 10min），否则新权限短时不生效。
-- Redis 口令在 docker/.env（不入库），本项目无法脚本直连，由运维执行：
--   redis-cli -a <pwd> --scan --pattern 'his:perm:role:*' | xargs redis-cli -a <pwd> del
-- 或重启后端。
-- =====================================================================
