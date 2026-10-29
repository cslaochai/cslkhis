-- =====================================================================
-- 20261028 手术室护士授予「手术器械清点」菜单（pid 300 住院管理下）
-- ---------------------------------------------------------------------
-- 背景：sql298 建了「手术室护士」角色(32/10042)，但只授了手术排期(306)、麻醉工作站(309)，
--       器械清点(2924) 及其按钮 2926/2927 只授给了 1-系统管理员 / 2-医生 / 3-护士 / 10-护士长。
--       结果是真正做清点的巡回/器械护士登上自己的岗位后，侧栏里没有这个页面 —— 只能借病房
--       护士账号做，器械清点记录上的签名人就不是台上的人。
--       2026-10-28 起二级及以上手术「不建清点单不许登记完成」（guardCount），这一页对 32
--       角色由「可有可无」变成必经环节，必须授。
--
-- 幂等：按 (role_id, menu_id) 判重插入，重复执行不产生重复行。
-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:10042，或重启后端（TTL 10min 不自愈）。
-- 回滚：DELETE FROM sys_role_menu WHERE role_id = 32 AND menu_id IN (2924, 2926, 2927);
-- =====================================================================

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8926102820000000100, 32, 2924, 'sql305', NOW(), 'sql305', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 32 AND menu_id = 2924);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8926102820000000101, 32, 2926, 'sql305', NOW(), 'sql305', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 32 AND menu_id = 2926);

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 8926102820000000102, 32, 2927, 'sql305', NOW(), 'sql305', NOW(), 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 32 AND menu_id = 2927);

-- 回查（期望 3 行）
-- SELECT role_id, menu_id FROM sys_role_menu WHERE role_id = 32 AND menu_id IN (2924, 2926, 2927);
