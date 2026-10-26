-- =====================================================================
-- 20261026 删除「门诊工位派岗」菜单及其子按钮
-- ---------------------------------------------------------------------
-- 背景：门诊工位派岗(2960) 的功能已拆并（公共工位字典 tab 回归排班字典管理 2939；
--       派岗/借调登记并入排班值守中心既有流程），该菜单入口不再需要。
--       本菜单 2026-10-25 曾被恢复（见 20261025_护士长收回排班三菜单与恢复工位派岗.sql），
--       现按最新口径彻底删除。
--
-- 删除对象：
--   2960  门诊工位派岗          menu_type=2（二级菜单，parent 2945 排班值守中心）
--   29601 门诊工位-派岗/新增    menu_type=3（按钮）
--   29602 门诊工位-撤销/删除    menu_type=3（按钮）
-- 角色授权牵连：role 1（系统管理员/10012）、role 26（医务科/10036）—— 一并清。
--
-- ⚠️ 物理删除原因：sys_menu.menu_key 上 uk_menu_key 为 UNIQUE 索引且不覆盖 del_flag，
--   软删会占住 key，同名/同 key 菜单日后插不回。故走 DELETE。
--
-- 幂等：DELETE 带 IN 条件，重复执行零变化。
--
-- 回滚：见下方「回滚段」（把三行菜单 + 六条授权插回即复原）。
-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min，不自愈场景必须清），
--    或重启后端。
-- =====================================================================

-- ===================== 回滚段（执行 DELETE 前保留，需要时取消注释） =====================
-- INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission, is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
--   VALUES (2960, '门诊工位派岗', 2945, 9, 2, '/opd-station', 'schedule/OpdStationView', 'org.opdStation:list', NULL, 'opd:station:list', 0, 0, 1, 1, 'admin', '2026-10-19 12:46:39', 'admin', '2026-10-25 15:43:50', 0, '公共工位字典 + 按日派岗/借调登记', 1, 1);
-- INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission, is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
--   VALUES (29601, '门诊工位-派岗/新增', 2960, 1, 3, NULL, NULL, 'org.opdStation:add', NULL, 'opd:station:add', 0, 1, 0, 1, 'admin', '2026-10-19 12:46:39', 'admin', '2026-10-19 12:46:39', 0, '工位字典新增/修改与派岗登记同走 upsert', 1, 1);
-- INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission, is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
--   VALUES (29602, '门诊工位-撤销/删除', 2960, 2, 3, NULL, NULL, 'org.opdStation:delete', NULL, 'opd:station:delete', 0, 1, 0, 1, 'admin', '2026-10-19 12:46:39', 'admin', '2026-10-19 12:46:39', 0, '撤销派岗 / 删除工位字典行（物理删）', 1, 1);
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2960);
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (26, 2960);
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 29601);
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (26, 29601);
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 29602);
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (26, 29602);
-- ===========================================================================================

-- 1) 物理删除菜单行（主菜单 + 两个子按钮）
DELETE FROM sys_menu WHERE id IN (2960, 29601, 29602);

-- 2) 级联删除角色授权（menu_key 不再占用，无需保留悬空授权）
DELETE FROM sys_role_menu WHERE menu_id IN (2960, 29601, 29602);

-- ===================== 自检 =====================
-- 应返回 0 行：菜单已彻底消失（path 与 menu_key 双查）
-- SELECT * FROM sys_menu WHERE id IN (2960,29601,29602) OR menu_key IN ('org.opdStation:list','org.opdStation:add','org.opdStation:delete');

-- 应返回 0 行：无孤儿授权
-- SELECT COUNT(*) c FROM sys_role_menu rm LEFT JOIN sys_menu m ON m.id=rm.menu_id WHERE m.id IS NULL;

-- 排班值守中心(2945) 下剩余子项（确认其它菜单不受影响）
-- SELECT id, menu_name, menu_type FROM sys_menu WHERE parent_id = 2945 AND del_flag = 0 ORDER BY sort_order;
