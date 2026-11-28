-- 425-设备管理与共享调配·机器人调度菜单收编.sql
--
-- 背景：用户反馈「设备共享调配」「机器人调度」单列成一级菜单不合适——它们业务上都属于「设备管理」，
-- 现在两者已并入设备管理页的 Tab（共享调配 / 机器人调度），侧栏菜单不再单列，只保留一个「设备管理」入口。
--
-- 做法：两条菜单 is_visible 置 0（从侧栏消失，但菜单行与按钮码原样保留，权限判定不受影响），
--      并把「设备管理」页(902)授给原先只有共享调配/调度入口的临床角色（3 护士 / 10 护士长 / 29 护理部主任 / 30 科护士长），
--      否则她们没有 902 就进不了这个页面，收编等于把入口删掉。
--
-- 回查：SELECT id, menu_name, is_visible FROM sys_menu WHERE id IN (902, 3800000000000000101, 4210000000000000101);
--       SELECT rm.role_id, r.role_name FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id WHERE rm.menu_id = 902 ORDER BY rm.role_id;

UPDATE sys_menu
SET is_visible = 0,
    update_by  = 'admin',
    update_time = NOW()
WHERE id IN (3800000000000000101, 4210000000000000101)
  AND del_flag = 0;

-- 设备管理页给临床角色（仅页面权限，不含按钮码：护士看不到新增/停用，派单/申请按钮仍走各自老码）
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time,
                                  create_by_id, update_by_id)
SELECT seed.id, seed.role_id, 902, 'admin', NOW(), 'admin', NOW(), 0, 0
FROM (
    SELECT 4250000000000000101 AS id, 3 AS role_id UNION ALL
    SELECT 4250000000000000102, 10 UNION ALL
    SELECT 4250000000000000103, 29 UNION ALL
    SELECT 4250000000000000104, 30
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = seed.role_id AND rm.menu_id = 902);
