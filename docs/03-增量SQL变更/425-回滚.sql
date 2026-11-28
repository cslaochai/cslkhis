-- 425-回滚.sql
-- 撤销「共享调配/机器人调度菜单收编」：两个菜单恢复侧栏显示，撤掉补给临床角色的设备管理页授权。

UPDATE sys_menu
SET is_visible = 1,
    update_by  = 'admin',
    update_time = NOW()
WHERE id IN (3800000000000000101, 4210000000000000101);

DELETE FROM sys_role_menu
WHERE menu_id = 902
  AND role_id IN (3, 10, 29, 30);
