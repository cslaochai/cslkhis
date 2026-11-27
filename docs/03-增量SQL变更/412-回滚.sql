-- 412 回滚：撤销应急吊销码与吊销类型列（先确认无证书行使用应急吊销档位）
-- 日期：2026-11-27
SET NAMES utf8mb4;
START TRANSACTION;

-- 0. 前置：revoke_type=2 的行必须为 0，否则回滚会丢失「应急吊销」事实
-- SELECT COUNT(*) AS emergency_rows FROM sys_sign_cert WHERE revoke_type = 2;

DELETE FROM `sys_role_menu` WHERE `menu_id` = 29630;
DELETE FROM `sys_role_menu` WHERE `id` = 9210000000000004123 AND `role_id` = 1 AND `menu_id` = 29625;
DELETE FROM `sys_menu` WHERE `id` = 29630 AND `permission` = 'sign:cert:revoke:emergency';

ALTER TABLE `sys_sign_cert` DROP COLUMN `revoke_type`;

COMMIT;

-- 执行后必须清 Redis：DEL his:perm:role:10043 his:perm:role:10012
