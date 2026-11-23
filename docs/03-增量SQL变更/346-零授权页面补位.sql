-- 346 零授权页面补位
-- 日期：2026-11-20
--   905 体检管理 / 2930 体检套餐 -> 门诊办(25)
--   909 医保付费结算 -> 医保结算员(16)
--   2383 医嘱基础字典 -> 医务科(26)
--   29621 签名证书 -> 医务科(26)
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
 (9210000000000000715, 25, 905, 'menu-govern-346', NOW(), 'menu-govern-346', NOW(), 0, 0),
 (9210000000000000716, 25, 2930, 'menu-govern-346', NOW(), 'menu-govern-346', NOW(), 0, 0),
 (9210000000000000717, 16, 909, 'menu-govern-346', NOW(), 'menu-govern-346', NOW(), 0, 0),
 (9210000000000000718, 26, 2383, 'menu-govern-346', NOW(), 'menu-govern-346', NOW(), 0, 0),
 (9210000000000000719, 26, 29621, 'menu-govern-346', NOW(), 'menu-govern-346', NOW(), 0, 0);

-- 回滚
-- DELETE FROM sys_role_menu WHERE create_by='menu-govern-346';
