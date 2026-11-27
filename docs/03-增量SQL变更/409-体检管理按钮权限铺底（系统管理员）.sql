-- ============================================================
-- 409 体检管理按钮权限铺底：系统管理员补 新增/修改/删除 三个按钮码
-- 日期：2026-11-27
-- 背景：体检套餐页（sys_menu 2930）与体检管理页（905）已授 role_id=1（角色 10012 系统管理员），
--       但按钮码 2131/2132/2133 只授给了 门诊办管理员(25) / 信息科管理员(43)。
--       表现：系统管理员登录后「新增套餐」「编辑」按钮被 v-perm 隐藏，保存接口
--       checkup:manage:add 直接 403 —— 体检套餐实际上只有这两个非系统管理员角色能维护。
-- 口径：AGENTS §8.1「凡被授某页面的角色自动获该页全部按钮码」。本文件只做该项对齐，
--       不新增/修改任何菜单，不改角色数据范围。
-- 内容：role_id=1 补 menu_id 2131(checkup:manage:add)、2132(checkup:manage:edit)、2133(checkup:manage:delete)。
-- 幂等：按 (role_id, menu_id) 判重，重放不产生重复行；id 取本文件专属段 9210000000000009001~9003。
-- 执行后必须清 Redis 权限缓存：DEL his:perm:role:10012（TTL 10 分钟，不清则最多 10 分钟后自愈）。
-- 回滚：DELETE FROM sys_role_menu WHERE id IN (9210000000000009001,9210000000000009002,9210000000000009003);
--
-- ⚠ 2026-11-27 已按业务口径撤销（本文件不再保留授予结果）：
--   系统管理员(10012)定位为系统配置角色，体检业务数据只读；体检套餐的新增/编辑/删除
--   由「体检中心前台」(10043) 与「门诊办管理员」(10035) 承担。dev 库已 DELETE 掉本次
--   插入的三行并清 his:perm:role:10012 / his:user:identity:admin。本文件保留仅作记录，
--   重放会重新把三行按钮码授给系统管理员，不要重放。
-- ============================================================
SET NAMES utf8mb4;
START TRANSACTION;

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`)
SELECT 9210000000000009001, 1, 2131, 'sql:409', NOW()
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2131)
   AND EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2131 AND `permission` = 'checkup:manage:add');

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`)
SELECT 9210000000000009002, 1, 2132, 'sql:409', NOW()
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2132)
   AND EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2132 AND `permission` = 'checkup:manage:edit');

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`)
SELECT 9210000000000009003, 1, 2133, 'sql:409', NOW()
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 2133)
   AND EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2133 AND `permission` = 'checkup:manage:delete');

COMMIT;

-- ---------- 执行后回查（三条都应为 3 行） ----------
-- SELECT m.id, m.permission FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id = 1 AND m.permission LIKE 'checkup:manage:%' ORDER BY m.id;
-- SELECT CAST(id AS CHAR) id, role_id, menu_id FROM sys_role_menu WHERE id IN (9210000000000009001, 9210000000000009002, 9210000000000009003);
