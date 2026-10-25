-- ============================================================================
-- 296-急诊排班菜单独立 + 急诊值班表挪门户（全院公开）
-- ============================================================================
-- 背景：biz_schedule 一张表混载四个域（门诊号源 / 护理 / 急诊值班 / 岗位出勤），
--   员工排班页不设防导致医生/护理/急诊行全混在一起（排班对象还能选到医生）。
--   本轮按域拆页：门诊排班 / 护理排班 / 急诊排班（新） / 员工排班（退回岗位出勤本位）。
--   急诊值班表（29605）从「排班值守中心」挪到「门户」并授权全部角色——
--   值班表全院公开是《医疗质量安全核心制度》要求，不该藏在排班目录里。
--
-- 菜单 ID 段：29611 急诊排班（主菜单）+ 29612-29615（增/改/删/审核发布按钮）。
--   权限码复用 staff:schedule:*（与员工排班同一批排班员：admin/医务科/科主任，
--   审核加门诊办）——域拆分是数据隔离，不是人员隔离，不新增权限码。
--
-- ⚠ 运维注意：
--   1) 执行后必须清权限 Redis 缓存，否则菜单/授权变更 10 分钟内不生效：
--      redis-cli --scan --pattern 'his:perm:role:*' | xargs redis-cli DEL
--      redis-cli --scan --pattern 'his:user:identity:*' | xargs redis-cli DEL
--   2) 本文件幂等：菜单按 ID 物理删再插（sys_menu.menu_key 是 UNIQUE 且不含 del_flag，
--      软删会占键位，必须物理删）；sort_order 写死目标值（不依赖现值）；
--      角色授权 INSERT IGNORE（uk_role_menu 复合键）+ id 显式雪花号段。
--   3) sys_role_menu.id 无库默认值，INSERT 必须显式给（雪花号段 8920610252*，
--      与存量 8920610251* 不撞）。
-- ============================================================================

-- ---------- 1. 急诊值班（29605）挪到门户（100），授权全部角色 ----------
-- 门户下排序：101 工作台(1) / 102 消息待办(2) / 29605 急诊值班(3)
UPDATE sys_menu SET parent_id = 100, sort_order = 3
WHERE id = 29605 AND menu_key = 'org.erDuty';

DELETE FROM sys_role_menu WHERE menu_id = 29605;
SET @rm_id = 8920610252000000000;
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, id, 29605 FROM sys_role WHERE del_flag = 0 AND status = 1;

-- ---------- 2. 急诊排班新菜单（插在护理排班 331 之后、员工排班 2935 之前） ----------
-- 2.1 排班值守中心(2945)下排序写死目标值（幂等：重跑不累加）
UPDATE sys_menu SET sort_order = CASE id
    WHEN 2938 THEN 1   -- 排班总览
    WHEN 804  THEN 2   -- 门诊排班
    WHEN 331  THEN 3   -- 护理排班
    WHEN 29611 THEN 4  -- 急诊排班（新）
    WHEN 2935 THEN 5   -- 员工排班
    WHEN 2937 THEN 6   -- 人力配置标准
    WHEN 29604 THEN 6  -- 门诊排班审核
    WHEN 806  THEN 7   -- 全院总值班
    WHEN 2936 THEN 8   -- 值班点位配置
    WHEN 2939 THEN 9   -- 排班字典管理
    WHEN 2960 THEN 9   -- 门诊工位派岗
    WHEN 29609 THEN 10 -- 排班历史查询
END
WHERE parent_id = 2945 AND del_flag = 0;

-- 2.2 幂等清场（物理删：menu_key UNIQUE 不含 del_flag）
DELETE FROM sys_role_menu WHERE menu_id IN (29611, 29612, 29613, 29614, 29615);
DELETE FROM sys_menu WHERE id IN (29611, 29612, 29613, 29614, 29615);

-- 2.3 主菜单：复制员工排班(2935)行骨架（is_frame/is_cache/create_by 等零猜测），改差异字段
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
SELECT 29611, 2945, '急诊排班', 4, menu_type, '/er-schedule', 'views/schedule/ErScheduleView.vue',
       'org:erSchedule', 'Calendar', 'staff:schedule:list',
       is_frame, is_cache, is_visible, status, create_by, NOW(), 0
FROM sys_menu WHERE id = 2935;

-- 2.4 四个按钮（复制岗位出勤按钮行骨架，parent 指向新菜单，menu_key 换域前缀）
INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
SELECT 29612, 29611, '急诊排班-新增', sort_order, menu_type, path, component, 'org.erSchedule:add',
       icon, permission, is_frame, is_cache, is_visible, status, create_by, NOW(), 0
FROM sys_menu WHERE id = 29351;

INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
SELECT 29613, 29611, '急诊排班-修改', sort_order, menu_type, path, component, 'org.erSchedule:edit',
       icon, permission, is_frame, is_cache, is_visible, status, create_by, NOW(), 0
FROM sys_menu WHERE id = 29352;

INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
SELECT 29614, 29611, '急诊排班-删除', sort_order, menu_type, path, component, 'org.erSchedule:delete',
       icon, permission, is_frame, is_cache, is_visible, status, create_by, NOW(), 0
FROM sys_menu WHERE id = 29353;

INSERT INTO sys_menu (id, parent_id, menu_name, sort_order, menu_type, path, component, menu_key, icon,
                      permission, is_frame, is_cache, is_visible, status, create_by, create_time, del_flag)
SELECT 29615, 29611, '急诊排班-审核发布', sort_order, menu_type, path, component, 'org.erSchedule:audit',
       icon, permission, is_frame, is_cache, is_visible, status, create_by, NOW(), 0
FROM sys_menu WHERE id = 29603;

-- 2.5 授权：主菜单+增改删 复制员工排班家族（2935/29351/29352/29353 的授权角色），
--     审核发布按钮复制 29603 的授权角色（admin/医务科，科主任不能审）
SET @rm_id = 8920610252000000100;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29611 FROM sys_role_menu rm WHERE rm.menu_id = 2935;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29612 FROM sys_role_menu rm WHERE rm.menu_id = 29351;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29613 FROM sys_role_menu rm WHERE rm.menu_id = 29352;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29614 FROM sys_role_menu rm WHERE rm.menu_id = 29353;
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT @rm_id := @rm_id + 1, rm.role_id, 29615 FROM sys_role_menu rm WHERE rm.menu_id = 29603;

-- ---------- 回查 ----------
-- 门户下应有：101 工作台 / 102 消息待办 / 29605 急诊值班
-- SELECT id, menu_name, parent_id, sort_order FROM sys_menu WHERE parent_id = 100 AND del_flag = 0 ORDER BY sort_order;
-- 排班值守中心下应有：…331 护理排班(3) / 29611 急诊排班(4) / 2935 员工排班(5)…
-- SELECT id, menu_name, parent_id, sort_order FROM sys_menu WHERE parent_id = 2945 AND del_flag = 0 ORDER BY sort_order;
-- 急诊值班授权角色数 = 启用角色总数；急诊排班授权 = admin/医务科/科主任
-- SELECT menu_id, COUNT(*) FROM sys_role_menu WHERE menu_id IN (29605, 29611, 29612, 29613, 29614, 29615) GROUP BY menu_id;
