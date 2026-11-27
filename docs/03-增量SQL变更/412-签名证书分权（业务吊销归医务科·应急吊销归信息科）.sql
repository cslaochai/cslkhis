-- ============================================================
-- 412 签名证书分权：业务吊销（医务科）+ 应急吊销（信息科）
-- 日期：2026-11-27
-- 背景：吊销权 29625（sign:cert:revoke）自 sql309 起只授医务科（role26），
--       信息科（role33）持 29621 页面 + 29623 办理，但遇到 UKey 丢失、私钥/PIN 泄露、
--       介质损坏等安全事件时无权即时吊销，只能等医务科处理，安全窗口被拉长。
-- 口径：技术管理归信息科、业务性吊销决定权归医务科；
--       信息科新增「应急吊销」独立码，只做安全事件处置，事后由系统自动派单给医务科补备案。
-- 内容：
--   1) sys_sign_cert 增列 revoke_type（1-业务吊销 2-应急吊销），吊销留痕区分谁为什么吊销；
--   2) 新增按钮 menu_id=29630「签名证书-应急吊销」（parent 29621，menu_type=3）；
--   3) 授 role 33（信息科管理员 10043）应急码；授 role 1（系统管理员 10012）应急码并补业务码 29625
--      （超管必须两码齐全，否则服务层会把「只有应急码」判成应急吊销）。
-- 幂等：菜单按 id/permission 判重，授权按 (role_id, menu_id) 判重；重放不产生重复行。
-- 执行后必须清 Redis 权限缓存：DEL his:perm:role:10043 his:perm:role:10012
--   （TTL 10 分钟，不清则最多 10 分钟后自愈）。
-- 回滚：见同目录 412-回滚.sql。
-- ============================================================
SET NAMES utf8mb4;
START TRANSACTION;

-- ---------- 0. 前置核对（应输出 0 行 / 现有两行） ----------
-- SELECT COUNT(*) AS menu_29630_should_be_0 FROM sys_menu WHERE id = 29630;
-- SELECT COUNT(*) AS col_should_be_0 FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_sign_cert' AND COLUMN_NAME = 'revoke_type';
-- SELECT m.id, m.menu_name, m.permission FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id = 33 AND m.id IN (29621, 29623, 29625);

-- ---------- 1. 吊销类型留痕列 ----------
ALTER TABLE `sys_sign_cert`
  ADD COLUMN `revoke_type` tinyint NULL COMMENT '吊销类型（1-业务吊销 2-应急吊销）' AFTER `revoke_by_name`;

-- ---------- 2. 应急吊销按钮（仅信息科口径，故与业务吊销分码） ----------
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `sort_order`, `menu_type`, `path`, `component`, `menu_key`, `icon`,
                        `permission`, `is_frame`, `is_cache`, `is_visible`, `status`,
                        `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `remark`,
                        `create_by_id`, `update_by_id`)
SELECT 29630, '签名证书-应急吊销', 29621, 20, 3, NULL, NULL, 'sign.cert:revoke:emergency', NULL,
       'sign:cert:revoke:emergency', 0, 1, 0, 1,
       'sql412', NOW(), 'sql412', NOW(), 0, '信息科安全事件即时吊销（UKey丢失/私钥或PIN泄露/介质损坏），事后医务科补备案',
       0, 0
 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 29630)
   AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `permission` = 'sign:cert:revoke:emergency');

-- ---------- 3. 授权：信息科管理员 + 系统管理员 ----------
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 9210000000000004121, 33, 29630, 'sql412', NOW(), 'sql412', NOW(), 0, 0
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 33 AND `menu_id` = 29630)
   AND EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 29630 AND `permission` = 'sign:cert:revoke:emergency');

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 9210000000000004122, 1, 29630, 'sql412', NOW(), 'sql412', NOW(), 0, 0
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 29630)
   AND EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 29630 AND `permission` = 'sign:cert:revoke:emergency');

-- 3.1 系统管理员补业务吊销码：超管必须两码齐全，否则「只有应急码」会被服务层判成应急吊销
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 9210000000000004123, 1, 29625, 'sql412', NOW(), 'sql412', NOW(), 0, 0
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 29625)
   AND EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 29625 AND `permission` = 'sign:cert:revoke');

COMMIT;

-- ---------- 执行后回查 ----------
-- 1) 菜单与授权（应为 1 行菜单 + 2 行授权）
-- SELECT id, menu_name, parent_id, permission FROM sys_menu WHERE id = 29630;
-- SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id = 29630 ORDER BY role_id;
-- 2) 列（应为 1 行）
-- SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_sign_cert' AND COLUMN_NAME = 'revoke_type';
-- 3) 口径复核：role33 应只有 29621/29623/29630，仍无 29625；role26 保留 29625
-- SELECT rm.role_id, m.id, m.permission FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE m.id IN (29621, 29623, 29625, 29630) ORDER BY m.id, rm.role_id;
