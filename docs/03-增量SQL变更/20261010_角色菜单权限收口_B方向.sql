-- 角色菜单权限收口（B 方向：不新增角色，按现有 24 角色重新归位）
-- 生成时间 2026-10-10；作用：把跨域/越职页面连同其按钮码一起收回，补上无家的本职页
-- 前置：sys_role_menu 删除即物理删（uk_role_menu 不含 del_flag），无需软删
-- 执行后必须清 Redis：DEL his:perm:role:{1..24}（否则 10 分钟内仍是旧权限集合）

-- ===== 收回（删页面 + 该角色下这些页面的全部按钮子授权）=====

-- 角色 2 收回：医嘱基础字典、日间手术准入目录、电子签名与时间戳；患者主索引
DELETE FROM sys_role_menu WHERE role_id=2 AND menu_id IN (606,702,2088,2107,2108,2383,2384,2385,2386,2928);

-- 角色 3 收回：病区护理排班、护理质控、订餐配送、营养会诊；患者主索引
DELETE FROM sys_role_menu WHERE role_id=3 AND menu_id IN (331,334,426,427,702,2107,2108);

-- 角色 4 收回：患者主索引
DELETE FROM sys_role_menu WHERE role_id=4 AND menu_id IN (702,2107,2108);

-- 角色 5 收回：患者主索引
DELETE FROM sys_role_menu WHERE role_id=5 AND menu_id IN (702,2107,2108);

-- 角色 6 收回：患者主索引
DELETE FROM sys_role_menu WHERE role_id=6 AND menu_id IN (702,2107,2108);

-- 角色 7 收回：门诊日志；患者主索引
DELETE FROM sys_role_menu WHERE role_id=7 AND menu_id IN (207,702,2107,2108);

-- 角色 8 收回：挂号预约、分诊工作站；患者主索引
DELETE FROM sys_role_menu WHERE role_id=8 AND menu_id IN (201,202,702,2002,2003,2004,2005,2006,2107,2108);

-- 角色 9 收回：患者主索引
DELETE FROM sys_role_menu WHERE role_id=9 AND menu_id IN (702,2107,2108);

-- 角色 10 收回：订餐配送、营养会诊；患者主索引
DELETE FROM sys_role_menu WHERE role_id=10 AND menu_id IN (426,427,702,2107,2108,2663,2664,2665);

-- 角色 11 收回：患者主索引
DELETE FROM sys_role_menu WHERE role_id=11 AND menu_id IN (702,2107,2108);

-- 角色 12 收回：患者主索引
DELETE FROM sys_role_menu WHERE role_id=12 AND menu_id IN (702,2107,2108);

-- 角色 13 收回：药房盘点；患者主索引
DELETE FROM sys_role_menu WHERE role_id=13 AND menu_id IN (510,702,2107,2108);

-- 角色 15 收回：不良事件上报、院感监测、医疗纠纷与投诉、满意度评价；患者主索引
DELETE FROM sys_role_menu WHERE role_id=15 AND menu_id IN (610,614,615,616,702,2092,2093,2094,2102,2103,2104,2107,2108,2171,2172,2630,2631);

-- 角色 16 收回：患者主索引
DELETE FROM sys_role_menu WHERE role_id=16 AND menu_id IN (702,2107,2108);

-- ===== 补充（页面读授权；写按钮如需再单独授）=====
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000001,3,2180,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000002,3,2181,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000003,3,2182,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000004,3,310,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000005,3,2035,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000006,3,2036,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000007,3,2037,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000008,5,310,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000009,5,2035,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000010,5,2036,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
INSERT INTO sys_role_menu (id,role_id,menu_id,create_by,create_by_id,create_time,update_by,update_by_id,update_time) VALUES (8920610100000000011,5,2037,'role-shoukou-20261010',1,NOW(),'role-shoukou-20261010',1,NOW()) ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);
