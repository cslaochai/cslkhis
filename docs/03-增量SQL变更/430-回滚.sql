-- 430 回滚：撤回站点菜单授权与站点表，任务单站点 id 列保留（列删了会丢存量回填结果）
SET NAMES utf8mb4;

-- 1) 菜单与按钮：先删授权再删菜单
DELETE FROM sys_role_menu
WHERE menu_id IN (4300000000000000101, 4300000000000000102, 4300000000000000103);

DELETE FROM sys_menu
WHERE id IN (4300000000000000101, 4300000000000000102, 4300000000000000103)
   OR parent_id = 4300000000000000101;

-- 2) 站点表（确认没有站点被其它业务引用后再删）
-- DROP TABLE IF EXISTS biz_robot_station;

-- 3) 任务单站点 id 列：不删。回滚只把指向撤销的数据置空不影响历史展示，
--    真要删列执行：
-- ALTER TABLE biz_robot_dispatch_order
--     DROP KEY idx_robot_dispatch_to_station,
--     DROP COLUMN apply_station_id,
--     DROP COLUMN from_station_id,
--     DROP COLUMN to_station_id;

-- 4) 台账停靠位置回滚
-- UPDATE biz_iot_device SET location = 'B1 中心药房充电桩'
-- WHERE device_type = 'robot' AND del_flag = 0 AND location = '1F 住院药房充电桩';

-- 执行后清 Redis 权限缓存：his:perm:role:*
