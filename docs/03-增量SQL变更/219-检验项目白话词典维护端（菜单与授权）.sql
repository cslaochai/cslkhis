-- =============================================================================
-- 219 · 检验项目白话词典维护端（菜单与授权）
--
-- 【为什么必须配这一刀】
-- sql/218 铺了 sys_lab_plain_item（41 条白话，库内 39 个检验项目 100% 覆盖）。
-- 但"覆盖率 100%"是**此刻**的数字：检验科一旦新开项目，患者端报告就多一个
-- 只有数值、没有解释的条目。没有维护入口，运营无从知道该补哪一条 ——
-- 词典三个月就退化成建库那天的快照，患者端解读跟着一起烂。
-- 维护页里带「覆盖率自检」：直接列出库里出现过、但词典没配的项目名（按出现次数倒序），
-- 这才是这个页面真正的价值，增删改查只是载体。
--
-- 【菜单挂哪】
-- 挂在「患者中心」(700) 下，与患者常见问题 (1130) 同级，sort_order=43。
-- 它服务的是患者端报告解读，不是检验科业务配置。
--
-- 【权限码为什么是 lab:plain:*】
-- 患者端报告解读接口只认 PATIENT authority（user_type=3 自动注入），
-- 院内维护接口认权限码 —— 同一张表两侧共用，鉴权各走各的，患者永远拿不到 lab:plain:*。
--
-- 【执行后必做】
-- 权限集合走 Redis：his:perm:role:{roleCode} TTL 10min。
-- SQL 旁路插菜单后必须删缓存，否则点了新菜单被守卫弹回首页（G5b 已踩）：
--   redis-cli --scan --pattern 'his:perm:role:*' | xargs -r redis-cli DEL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 菜单（1133）+ 按钮（1134/1135）
--    menu_key 的唯一索引不覆盖 del_flag，删菜单一律物理删（不能软删）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_menu`
  (`id`,`menu_name`,`parent_id`,`sort_order`,`menu_type`,`path`,`component`,`menu_key`,`icon`,`permission`,
   `is_frame`,`is_cache`,`is_visible`,`status`,`create_by`,`create_time`,`del_flag`,`remark`)
VALUES
  (1133,'检验项目白话词典',700,43,2,'/system/labPlain','system/faq/LabPlainItemView','lab.plain','Reading','lab:plain:list',
   0,0,1,1,'sql219',NOW(),0,'患者端报告解读的白话词典维护（sys_lab_plain_item）'),
  (1134,'新增或修改',1133,1,3,'','','lab.plain.upsert','','lab:plain:upsert',
   0,0,1,1,'sql219',NOW(),0,'白话词条新增或修改'),
  (1135,'删除',1133,2,3,'','','lab.plain.delete','','lab:plain:delete',
   0,0,1,1,'sql219',NOW(),0,'白话词条删除（物理删，uk_item_name 不含 del_flag）')
  AS new ON DUPLICATE KEY UPDATE
  `menu_name` = new.`menu_name`,
  `parent_id` = new.`parent_id`,
  `sort_order` = new.`sort_order`,
  `path` = new.`path`,
  `component` = new.`component`,
  `icon` = new.`icon`,
  `permission` = new.`permission`,
  `status` = new.`status`,
  `update_time` = NOW(),
  `del_flag` = 0;

-- -----------------------------------------------------------------------------
-- 2. 授权给管理员角色（role_id=1，与患者常见问题 1130 同一角色）
--    sys_role_menu.id 不是自增，必须显式给：这里用 3000+menu_id 派生，
--    避开 sql/217 已经占用的 2000+menu_id 段
-- -----------------------------------------------------------------------------
INSERT INTO `sys_role_menu` (`id`,`role_id`,`menu_id`,`create_by`,`create_time`)
SELECT 3000 + m.id, 1, m.id, 'sql219', NOW()
FROM `sys_menu` m
WHERE m.id IN (1133, 1134, 1135)
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.id
  );
