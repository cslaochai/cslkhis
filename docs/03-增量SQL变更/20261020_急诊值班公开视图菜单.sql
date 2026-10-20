-- =====================================================================
-- 20261020 急诊值班公开视图：菜单 + 授权
-- ---------------------------------------------------------------------
-- 背景：
--   核心制度《医疗质量安全核心制度要点》(国卫医发〔2018〕8号) 要求值班表全院公开。
--   本文件新增「急诊值班」只读菜单（排班中心 2945 下），展示急诊内科/外科/儿科
--   当天已发布的医生值班表。后端接口 /system/staffSchedule/erDutyPublic 为
--   isAuthenticated()（任何登录职工可查），菜单权限码只控制页面入口可见性。
--
-- 授权对象：与既有排班体系一致 1 超管 / 10 护士长 / 26 医务科 / 27 科主任。
--   如需更广公开（如全院医生都可进），把 29605 授权给对应角色即可（接口不限）。
--
-- 幂等：sys_menu.id / uk_menu_key、sys_role_menu uk_role_menu(role_id,menu_id)，
--       INSERT IGNORE 重复执行零变化。
-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min）或退出重登。
-- =====================================================================

-- ---------- ① 菜单：急诊值班（只读公开视图） ----------
INSERT IGNORE INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon,
                             permission, is_frame, is_cache, is_visible, status,
                             create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
VALUES (29605, '急诊值班', 2945, 6, 2, '/er-duty', 'views/schedule/ErDutyView.vue',
        'org.erDuty', 'FirstAidKit',
        'schedule:erDuty:view', 0, 1, 1, 1,
        'admin', NOW(), 'admin', NOW(), 0, '急诊值班表（公开只读）：急诊三科当天已发布医生值班，全院可查', 1, 1);

-- ---------- ② 角色授权 ----------
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
VALUES
  (8920610200000000001,  1, 29605, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610200000000002, 10, 29605, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610200000000003, 26, 29605, 'admin', NOW(), 'admin', NOW(), 1, 1),
  (8920610200000000004, 27, 29605, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- ③ 回查 ----------
-- SELECT rm.role_id, m.id, m.menu_name, m.permission
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id IN (1,10,26,27) AND m.id = 29605
--  ORDER BY rm.role_id;
