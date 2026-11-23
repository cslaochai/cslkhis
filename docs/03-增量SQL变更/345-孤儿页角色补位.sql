-- 345 孤儿页角色补位
-- 日期：2026-11-20
--   312 双向转诊 -> 医生(2)/门诊办(25)/医务科(26)
--   805 技术授权 -> 医务科(26)
--   29620 签名验签 -> 医务科(26)
--   801 科室管理 -> 医务科(26)
--   802 诊室管理 -> 门诊办(25)
--   2928 日间手术准入目录 -> 医务科(26)/手术室护士(32)
--   1107 合理用药知识库 -> 临床药师(13)
--   2939 排班字典管理 -> 科室排班员(28)
--   2940 AI运营问数 -> 院领导(18)/审计统计员(17)
INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
 (9210000000000000702, 2, 312, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000703, 25, 312, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000704, 26, 312, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000705, 26, 805, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000706, 26, 29620, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000707, 26, 801, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000708, 25, 802, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000709, 26, 2928, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000710, 32, 2928, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000711, 13, 1107, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000712, 28, 2939, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000713, 18, 2940, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0),
 (9210000000000000714, 17, 2940, 'menu-govern-345', NOW(), 'menu-govern-345', NOW(), 0, 0);

-- 回滚
-- DELETE FROM sys_role_menu WHERE create_by='menu-govern-345';
