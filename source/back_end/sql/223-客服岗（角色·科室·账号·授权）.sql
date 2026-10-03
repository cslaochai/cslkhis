-- =============================================================================
-- 223 · 客服岗（角色 / 归属科室 / 账号 / 授权）
--
-- 【为什么必须配这一刀】
-- sql/221 把患者留言升级成了工单（受理 → 回复 → 办结 → 患者确认，流转全留痕），
-- sql/222 给了工单受理的菜单 —— 但**只授给了管理员（role_id=1）**。
-- 管理员是配置岗，不是接单岗：让他接单等于「工单有人能开、没人负责」，
-- 患者端那条闭环还是断的（患者看到自己的单永远是「待受理」）。
-- 这一刀把「谁接单」这件事落到具体岗位和具体人身上。
--
-- 【岗位口径：不新造 staff_type】
-- sys_role.staff_type 是封闭枚举（1医生 2护理 3医技 4药学 5收费 6行政其他），
-- 客服不新增类别，归 **6 行政其他**（sql/195 排班岗位口径：岗位=人事类别，落 staff_type，
-- 不另造岗位字典、不用 emp_type）。客服不排班、不排班号源，staff_type 只作归类。
--
-- 【授权范围：给工单 + 语料，不给词典】
--   1130/1131/1132 患者常见问题（patient:faq:*）  —— 客服是语料生产者：患者问什么、怎么答，
--                                                   每天接单的人最清楚，必须能自己补 FAQ。
--   1136/1137 工单受理（service:ticket:*）        —— 接单与处理，本刀的核心。
--   1133/1134/1135 检验项目白话词典（lab:plain:*）—— **不给**。白话表述是医学内容
--                                                   （"血清钾"怎么讲），写错就是误导患者，
--                                                   维持管理员（医学审核）口径。
--
-- 【data_scope 为什么是 1（全部数据）】
-- 工单按"患者"归口，不按科室归口 —— 客服接的是全院患者提的单，
-- 若按本科室过滤，A 科室客服看不到 B 科室患者的单，等于漏单。
-- 科室数据权限是静态门面（DeptScopeGuard 由 Service 显式调用），
-- 工单受理接口不调用它，data_scope=1 与实现一致。
--
-- 【执行后必做】
-- 权限集合走 Redis：his:perm:role:{roleCode} TTL 10min。
-- SQL 旁路插菜单/授权后必须删缓存，否则点了新菜单被守卫弹回首页（G5b 已踩）：
--   redis-cli --scan --pattern 'his:perm:role:*' | xargs -r redis-cli DEL
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 0. 归属科室：客户服务中心（挂行政后勤部 19580075 下）
--    客服必须挂在实体科室上：sys_employee_post.dept_id 是登录后 /auth/info.deptId 的来源，
--    没有科室 = 登录后拿不到 deptId，Header 显示不出归属，工单受理人也无法落到具体人。
--    dept_code 沿用行政后勤部号段 100014xx（已有 10001401~10001410），取 10001411。
-- -----------------------------------------------------------------------------
INSERT INTO `sys_department`
  (`id`,`dept_code`,`dept_name`,`dept_type`,`parent_id`,`sort_order`,`dept_desc`,
   `is_open`,`status`,`create_by`,`create_time`,`del_flag`,`remark`)
VALUES
  (19580095,'10001411','客户服务中心',5,19580075,11,'患者转人工工单受理与患者服务咨询',
   1,1,'sql223',NOW(),0,'sql/223 客服岗归属科室')
  AS new ON DUPLICATE KEY UPDATE
  `dept_name` = new.`dept_name`,
  `dept_type` = new.`dept_type`,
  `parent_id` = new.`parent_id`,
  `dept_desc` = new.`dept_desc`,
  `update_time` = NOW(),
  `del_flag` = 0;

-- -----------------------------------------------------------------------------
-- 1. 角色：客服专员（role_code=10034，接 10033 行政人员之后）
--    role_code 是登录/切角色的唯一口径（CurrentUser.roles 来自 sys_employee_post → sys_role）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_role`
  (`id`,`role_code`,`role_name`,`role_type`,`staff_type`,`data_scope`,`sort_order`,
   `status`,`create_by`,`create_time`,`del_flag`,`remark`)
VALUES
  (24,'10034','客服专员',1,6,1,24,1,'sql223',NOW(),0,
   '患者转人工工单受理 + 客服台语料维护（工单接单岗，非配置岗）')
  AS new ON DUPLICATE KEY UPDATE
  `role_name` = new.`role_name`,
  `staff_type` = new.`staff_type`,
  `data_scope` = new.`data_scope`,
  `sort_order` = new.`sort_order`,
  `status` = new.`status`,
  `update_time` = NOW(),
  `del_flag` = 0;

-- -----------------------------------------------------------------------------
-- 2. 授权：客服专员（role_id=24）拿到工单受理 + 常见问题维护
--    sys_role_menu.id 不是自增，必须显式给：这里用 5000+menu_id 派生，
--    避开 sql/217 的 2000+ 段、sql/219 的 3000+ 段、sql/222 的 4000+ 段
--
--    ⚠ **祖先目录必须一并授权**（700 患者中心），否则菜单树为空：
--    SysMenuServiceImpl#userMenus 拿到的平铺结果交给 buildMenuTree(menus, 0L) 组装，
--    它只从 parent_id=0 的行往下挂 —— 目录没授权 → 顶层无节点 → 子菜单永远挂不上去，
--    现象是「权限集合里 service:ticket:list 都有，接口调得通，但侧边栏一个菜单都没有」。
--    sql/217、219、222 没踩到是因为管理员本来就持有 700；新角色必须自己带。
--
--    另外带上 100/102（门户 → 消息待办，portal:messages:view）：
--    Header 每 5 秒轮询 /system/message/unread/count 画未读徽标，这个接口是
--    类级 @PreAuthorize("hasAuthority('portal:messages:view')")，
--    院内岗位（10012~10033）人手一份，新角色不给 → 登录后控制台每 5 秒一条 403。
--    客服是院内岗位、也要收院内通知（工单超时提醒将来就走这里），给。
-- -----------------------------------------------------------------------------
INSERT INTO `sys_role_menu` (`id`,`role_id`,`menu_id`,`create_by`,`create_time`)
SELECT 5000 + m.id, 24, m.id, 'sql223', NOW()
FROM `sys_menu` m
WHERE m.id IN (100, 102, 700, 1130, 1131, 1132, 1136, 1137)
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 24 AND rm.menu_id = m.id
  );

-- -----------------------------------------------------------------------------
-- 3. 客服员工（2 名：交接和互查都要有第二个人，单人岗一旦请假工单就没人接）
--    id 段 8900000000000070xxx：避开雪花 2.1e18、医生 890000000000004xxxx、
--    药师 890000000000005xxxx、数据权限探针 895000000000006xxxx
--    emp_type=6（其他，列注释口径；岗位以 sys_role.staff_type 为准）
--    gender 走员工口径 his_gender_sys：0女/1男（≠患者 1男/2女）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_employee`
  (`id`,`emp_code`,`emp_name`,`emp_type`,`gender`,`hire_date`,`phone`,
   `dept_id`,`dept_name`,`position`,`status`,`create_by`,`create_time`,`del_flag`,`remark`)
VALUES
  (8900000000000070001,'CS001','客服-张静',6,0,CURDATE(),'13800007001',
   19580095,'客户服务中心','客服专员',1,'sql223',NOW(),0,'sql/223 客服岗种子账号'),
  (8900000000000070002,'CS002','客服-李娜',6,0,CURDATE(),'13800007002',
   19580095,'客户服务中心','客服专员',1,'sql223',NOW(),0,'sql/223 客服岗种子账号')
  AS new ON DUPLICATE KEY UPDATE
  `emp_name` = new.`emp_name`,
  `emp_type` = new.`emp_type`,
  `dept_id` = new.`dept_id`,
  `dept_name` = new.`dept_name`,
  `position` = new.`position`,
  `status` = new.`status`,
  `update_time` = NOW(),
  `del_flag` = 0;

-- -----------------------------------------------------------------------------
-- 4. 登录账号：cs01 / cs02，密码与 admin 一致（直接取 admin 的哈希，不自造 bcrypt）
--    user_type=1（系统用户，院内员工；患者小程序是 3，由 sys_user.user_type 判定）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_user`
  (`id`,`user_name`,`password`,`real_name`,`emp_id`,`user_type`,`status`,
   `create_by`,`create_time`,`del_flag`,`remark`)
VALUES
  (8900000000000070001,'cs01',(SELECT p.`password` FROM (SELECT `password` FROM `sys_user` WHERE `user_name`='admin') p),
   '客服-张静',8900000000000070001,1,1,'sql223',NOW(),0,'sql/223 客服岗种子账号（密码同 admin）'),
  (8900000000000070002,'cs02',(SELECT p.`password` FROM (SELECT `password` FROM `sys_user` WHERE `user_name`='admin') p),
   '客服-李娜',8900000000000070002,1,1,'sql223',NOW(),0,'sql/223 客服岗种子账号（密码同 admin）')
  AS new ON DUPLICATE KEY UPDATE
  `real_name` = new.`real_name`,
  `emp_id`   = new.`emp_id`,
  `user_type` = new.`user_type`,
  `status`   = new.`status`,
  `update_time` = NOW(),
  `del_flag` = 0;

-- -----------------------------------------------------------------------------
-- 5. 岗位绑定（sys_employee_post）：没有这一行 = 登录后 roles 为空 = 登录被拒
--    （CurrentUser.roles 由 sys_employee_post → sys_role 得来，不是 sys_user 的字段）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_employee_post`
  (`id`,`employee_id`,`role_id`,`dept_id`,`is_primary`,`effective_date`,
   `create_by`,`create_time`,`update_time`)
VALUES
  (8900000000000071001,8900000000000070001,24,19580095,1,CURDATE(),'sql223',NOW(),NOW()),
  (8900000000000071002,8900000000000070002,24,19580095,1,CURDATE(),'sql223',NOW(),NOW())
  AS new ON DUPLICATE KEY UPDATE
  `role_id`  = new.`role_id`,
  `dept_id`  = new.`dept_id`,
  `is_primary` = new.`is_primary`,
  `update_time` = NOW();

-- =============================================================================
-- 验证（执行完逐条跑，expect 列是期望值）
-- =============================================================================
SELECT 'V1 客服角色存在且启用' AS item,
       COUNT(*) AS cnt, '1' AS expect
FROM `sys_role` WHERE `role_code`='10034' AND `status`=1 AND `del_flag`=0
UNION ALL
SELECT 'V2 客服岗授权菜单数（100/102 + 700 目录 + 1130/1131/1132/1136/1137）',
       COUNT(*), '8'
FROM `sys_role_menu` rm JOIN `sys_role` r ON r.`id`=rm.`role_id`
WHERE r.`role_code`='10034' AND rm.`menu_id` IN (100,102,700,1130,1131,1132,1136,1137)
UNION ALL
SELECT 'V2c 消息待办已授权（不给就每 5 秒一条 403）',
       COUNT(*), '1'
FROM `sys_role_menu` rm JOIN `sys_role` r ON r.`id`=rm.`role_id`
WHERE r.`role_code`='10034' AND rm.`menu_id`=102
UNION ALL
SELECT 'V2b 祖先目录 700 已授权（不补这条侧边栏会是空的）',
       COUNT(*), '1'
FROM `sys_role_menu` rm JOIN `sys_role` r ON r.`id`=rm.`role_id`
WHERE r.`role_code`='10034' AND rm.`menu_id`=700
UNION ALL
SELECT 'V3 客服没被授词典权限（不给 lab:plain:*）',
       COUNT(*), '0'
FROM `sys_role_menu` rm JOIN `sys_role` r ON r.`id`=rm.`role_id`
WHERE r.`role_code`='10034' AND rm.`menu_id` IN (1133,1134,1135)
UNION ALL
SELECT 'V4 客服员工在职数',
       COUNT(*), '2'
FROM `sys_employee` WHERE `id` BETWEEN 8900000000000070001 AND 8900000000000070002
  AND `status`=1 AND `del_flag`=0
UNION ALL
SELECT 'V5 账号启用且 emp_id 与员工对得上',
       COUNT(*), '2'
FROM `sys_user` u JOIN `sys_employee` e ON e.`id`=u.`emp_id`
WHERE u.`user_name` IN ('cs01','cs02') AND u.`status`=1 AND u.`user_type`=1
UNION ALL
SELECT 'V6 岗位绑定数（is_primary=1）',
       COUNT(*), '2'
FROM `sys_employee_post` p JOIN `sys_role` r ON r.`id`=p.`role_id`
WHERE p.`employee_id` BETWEEN 8900000000000070001 AND 8900000000000070002
  AND r.`role_code`='10034' AND p.`is_primary`=1
UNION ALL
SELECT 'V7 客服权限集合含 service:ticket:handle',
       COUNT(*), '1'
FROM `sys_role_menu` rm
  JOIN `sys_role` r ON r.`id`=rm.`role_id`
  JOIN `sys_menu` m ON m.`id`=rm.`menu_id`
WHERE r.`role_code`='10034' AND m.`permission`='service:ticket:handle' AND m.`del_flag`=0;
