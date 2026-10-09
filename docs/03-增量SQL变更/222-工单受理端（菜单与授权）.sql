-- =============================================================================
-- 222 · 工单受理端（菜单与授权）
--
-- 【为什么必须配这一刀】
-- sql/216 的留言只有患者端：患者能提，但没有人接 —— 那是**半截闭环**，
-- 留言落库了却没人对它负责，患者也不知道有没有人看。
-- sql/221 把留言升级成工单（受理/回复/办结/患者确认 + 流转留痕），
-- 这一刀给院内一个认领入口。没有它，工单状态永远停在「待受理」。
--
-- 【菜单挂哪】
-- 挂在「患者中心」(700) 下，与患者常见问题 (1130)、白话词典 (1133) 同级，sort_order=44。
-- 它服务的是患者提的单，不是系统配置。
--
-- 【权限码为什么是 service:ticket:*】
-- 患者端接口只认 PATIENT authority（user_type=3 自动注入），院内接口认权限码 ——
-- 同一张表两侧共用，鉴权各走各的，患者永远拿不到 service:ticket:*。
--
-- 【执行后必做】
-- 权限集合走 Redis：his:perm:role:{roleCode} TTL 10min。
-- SQL 旁路插菜单后必须删缓存，否则点了新菜单被守卫弹回首页（G5b 已踩）：
--   redis-cli --scan --pattern 'his:perm:role:*' | xargs -r redis-cli DEL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 菜单（1136）+ 按钮（1137）
--    menu_key 的唯一索引不覆盖 del_flag，删菜单一律物理删（不能软删）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_menu`
  (`id`,`menu_name`,`parent_id`,`sort_order`,`menu_type`,`path`,`component`,`menu_key`,`icon`,`permission`,
   `is_frame`,`is_cache`,`is_visible`,`status`,`create_by`,`create_time`,`del_flag`,`remark`)
VALUES
  (1136,'工单受理',700,44,2,'/system/serviceTicket','system/service/ServiceTicketView','service.ticket','Service','service:ticket:list',
   0,0,1,1,'sql222',NOW(),0,'患者转人工工单的受理端（biz_service_message + biz_service_ticket_log）'),
  (1137,'受理与处理',1136,1,3,'','','service.ticket.handle','','service:ticket:handle',
   0,0,1,1,'sql222',NOW(),0,'工单受理/回复/办结/关闭/内部备注')
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
-- 2. 授权给管理员角色（role_id=1）
--    sys_role_menu.id 不是自增，必须显式给：用 4000+menu_id 派生，
--    避开 sql/217 的 2000+ 段与 sql/219 的 3000+ 段
-- -----------------------------------------------------------------------------
INSERT INTO `sys_role_menu` (`id`,`role_id`,`menu_id`,`create_by`,`create_time`)
SELECT 4000 + m.id, 1, m.id, 'sql222', NOW()
FROM `sys_menu` m
WHERE m.id IN (1136, 1137)
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = m.id
  );
