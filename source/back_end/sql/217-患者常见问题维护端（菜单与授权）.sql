-- =============================================================================
-- 217 · 患者常见问题维护端（菜单与授权）
--
-- 【为什么必须配这一刀】
-- sql/216 铺了患者端客服台（sys_faq 56 条语料）。语料是**人工维护**的：
-- 患者每点一次「没帮助」都是在说这条答案该改。没有维护入口，改一条答案要找 DBA 写 SQL ——
-- 那这张表三个月内必然变成没人敢碰也没人更新的死数据，患者端客服台跟着一起烂。
-- 所以维护入口不是"锦上添花"，是客服台能不能长期活下去的前提。
--
-- 【菜单挂哪】
-- 挂在「患者中心」(700) 下：FAQ 服务的是患者，不是系统配置。
-- 与患者标签 (704) 同级，sort_order=42 排在最后。
--
-- 【权限码为什么是 patient:faq:*】
-- 患者端接口只认 PATIENT authority（user_type=3 自动注入），院内接口认权限码 ——
-- 两侧共用一张表，但鉴权各走各的，患者永远拿不到 patient:faq:*。
--
-- 【执行后必做】
-- 权限集合走 Redis：his:perm:role:{roleCode} TTL 10min。
-- SQL 旁路插菜单后必须删缓存，否则点了新菜单被守卫弹回首页（G5b 已踩）：
--   redis-cli --scan --pattern 'his:perm:role:*' | xargs -r redis-cli DEL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 菜单（1130）+ 按钮（1131/1132）
--    menu_key 的唯一索引不覆盖 del_flag，删菜单一律物理删（不能软删）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_menu`
  (`id`,`menu_name`,`parent_id`,`sort_order`,`menu_type`,`path`,`component`,`menu_key`,`icon`,`permission`,
   `is_frame`,`is_cache`,`is_visible`,`status`,`create_by`,`create_time`,`del_flag`,`remark`)
VALUES
  (1130,'患者常见问题',700,42,2,'/system/patientFaq','system/faq/FaqView','patient.faq','Notebook','patient:faq:list',
   0,0,1,1,'sql217',NOW(),0,'患者端客服台语料维护（sys_faq）'),
  (1131,'新增或修改',1130,1,3,'','','patient.faq.upsert','','patient:faq:upsert',
   0,0,1,1,'sql217',NOW(),0,'患者常见问题新增或修改'),
  (1132,'删除',1130,2,3,'','','patient.faq.delete','','patient:faq:delete',
   0,0,1,1,'sql217',NOW(),0,'患者常见问题删除（物理删）')
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
-- 2. 授权给管理员角色（role_id=1，与患者标签 704 同一角色）
-- -----------------------------------------------------------------------------
-- sys_role_menu.id 不是自增，必须显式给：这里用 2000+menu_id 派生，天然不撞已有行
INSERT INTO `sys_role_menu` (`id`,`role_id`,`menu_id`,`create_by`,`create_time`)
SELECT 2000 + m.id, 1, m.id, 'sql217', NOW()
FROM `sys_menu` m
WHERE m.id IN (1130, 1131, 1132)
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.id
  );

-- -----------------------------------------------------------------------------
-- 3. 旁路写库后必须失效权限缓存，否则新菜单点了被弹回首页
--    （Redis 里没有 CLI 时由脚本 _flush_perm_cache.mjs 处理）
-- -----------------------------------------------------------------------------
