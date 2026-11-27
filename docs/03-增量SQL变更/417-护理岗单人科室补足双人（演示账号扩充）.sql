-- 417-护理岗单人科室补足双人（演示登录账号扩充）
-- 背景：登录页测试账号由 workspace/_gen_login_cred_accounts.mjs 从 dev 库导出。
--       盘点发现部分无床位的门诊/医技/平台科室护理岗仅 1 人，无法演示双人顶岗/交接班；
--       麻醉科手术室护士仅 1 人，手术排台候选过薄。
-- 口径：只补护理岗，每个单人科室补到 ≥2；麻醉科补手术室护士。住院病区现有 3 人及以上，不动。
-- 角色：3-护士 / 9-门诊分诊护士 / 32-手术室护士；口令统一 123456（BCrypt 串沿用 admin）。
-- 幂等：按主键 + 业务唯一键（emp_code / user_name）NOT EXISTS 跳过；create_by='sql417' 留痕。
-- 回滚：417-回滚.sql
-- 校验：见文件末尾回查语句；补完重跑 node workspace/_gen_login_cred_accounts.mjs 刷新凭据。

SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 员工档案
INSERT INTO `sys_employee`
  (`id`, `emp_code`, `emp_name`, `emp_type`, `gender`, `birth_date`, `hire_date`,
   `id_card`, `phone`, `email`, `dept_id`, `dept_name`, `title`, `position`,
   `specialty`, `education`, `expert_price`, `status`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`,
   `remark`, `create_by_id`, `update_by_id`)
SELECT v.* FROM (
  SELECT 8999000000000093001 AS id, 'E930001' AS emp_code, '周静雯' AS emp_name, 2 AS emp_type, 2 AS gender,
         '1992-03-15' AS birth_date, '2016-08-01' AS hire_date, '430726199203150021' AS id_card,
         '13800009301' AS phone, 'zhoujingwen@cslk-his.local' AS email, 19580041 AS dept_id, '康复医学科' AS dept_name,
         '203' AS title, '15' AS position, '康复护理、功能锻炼与理疗配合' AS specialty, '3' AS education,
         0 AS expert_price, 1 AS status, 'sql417' AS create_by, NOW() AS create_time, 'sql417' AS update_by, NOW() AS update_time,
         0 AS del_flag, '演示账号扩充·康复医学科护理岗补足双人' AS remark, 1 AS create_by_id, 1 AS update_by_id
  UNION ALL
  SELECT 8999000000000093002, 'E930002', '唐晓梅', 2, 2, '1994-07-22', '2018-07-01', '430726199407220028',
         '13800009302', 'tangxiaomei@cslk-his.local', 19580061, '放射治疗科',
         '203', '15', '放疗摆位配合、照射护理与皮肤反应观察', '3',
         0, 1, 'sql417', NOW(), 'sql417', NOW(), 0, '演示账号扩充·放射治疗科护理岗补足双人', 1, 1
  UNION ALL
  SELECT 8999000000000093003, 'E930003', '林晓慧', 2, 2, '1996-01-08', '2019-09-01', '430726199601080026',
         '13800009303', 'linxiaohui@cslk-his.local', 19580068, '发热门诊',
         '106', '15', '发热预检分诊、流行病学筛查与院感防控', '3',
         0, 1, 'sql417', NOW(), 'sql417', NOW(), 0, '演示账号扩充·发热门诊分诊岗补足双人', 1, 1
  UNION ALL
  SELECT 8999000000000093004, 'E930004', '黄丽君', 2, 2, '1993-11-30', '2017-08-01', '430726199311300029',
         '13800009304', 'huanglijun@cslk-his.local', 19580069, '肠道门诊',
         '105', '15', '肠道传染病分诊、报卡与标本送检配合', '3',
         0, 1, 'sql417', NOW(), 'sql417', NOW(), 0, '演示账号扩充·肠道门诊分诊岗补足双人', 1, 1
  UNION ALL
  SELECT 8999000000000093005, 'E930005', '何静怡', 2, 2, '1995-05-19', '2020-07-01', '430726199505190024',
         '13800009305', 'hejingyi@cslk-his.local', 19580072, '普通体检科',
         '203', '15', '体检导检、生命体征测量与采血配合', '3',
         0, 1, 'sql417', NOW(), 'sql417', NOW(), 0, '演示账号扩充·普通体检科护理岗补足双人', 1, 1
  UNION ALL
  SELECT 8999000000000093006, 'E930006', '罗海燕', 2, 2, '1991-09-25', '2015-07-01', '430726199109250027',
         '13800009306', 'luohaiyan@cslk-his.local', 19580074, '预防保健科',
         '203', '15', '预防接种、慢病随访与健康宣教护理', '3',
         0, 1, 'sql417', NOW(), 'sql417', NOW(), 0, '演示账号扩充·预防保健科护理岗补足双人', 1, 1
  UNION ALL
  SELECT 8999000000000093007, 'E930007', '高淑敏', 2, 2, '1989-04-12', '2013-07-01', '430726198904120022',
         '13800009307', 'gaoshumin@cslk-his.local', 19580039, '麻醉科',
         '203', '15', '手术巡回/器械配合、麻醉恢复期(PACU)护理', '3',
         0, 1, 'sql417', NOW(), 'sql417', NOW(), 0, '演示账号扩充·麻醉科手术室护士补足排台候选', 1, 1
  UNION ALL
  SELECT 8999000000000093008, 'E930008', '邓雪梅', 2, 2, '1992-10-06', '2016-09-01', '430726199210060025',
         '13800009308', 'dengxuemei@cslk-his.local', 19580039, '麻醉科',
         '203', '15', '手术巡回/器械配合、麻醉恢复期(PACU)护理', '3',
         0, 1, 'sql417', NOW(), 'sql417', NOW(), 0, '演示账号扩充·麻醉科手术室护士补足排台候选', 1, 1
) v
WHERE NOT EXISTS (SELECT 1 FROM `sys_employee` e WHERE e.id = v.id OR e.emp_code = v.emp_code);

-- 2. 登录账号（口令 123456，BCrypt 串沿用 admin 同款）
INSERT INTO `sys_user`
  (`id`, `user_name`, `password`, `real_name`, `emp_id`, `user_type`, `status`,
   `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`,
   `remark`, `create_by_id`, `update_by_id`)
SELECT v.uid, v.uname, '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca',
       v.real_name, v.emp, 1, 1, 'sql417', NOW(), 'sql417', NOW(), 0,
       '演示账号扩充·护理岗补足双人', 1, 1
FROM (
  SELECT 8999000000000093101 AS uid, 'zhoujingwen' AS uname, '周静雯' AS real_name, 8999000000000093001 AS emp
  UNION ALL SELECT 8999000000000093102, 'tangxiaomei', '唐晓梅', 8999000000000093002
  UNION ALL SELECT 8999000000000093103, 'linxiaohui', '林晓慧', 8999000000000093003
  UNION ALL SELECT 8999000000000093104, 'huanglijun', '黄丽君', 8999000000000093004
  UNION ALL SELECT 8999000000000093105, 'hejingyi',   '何静怡', 8999000000000093005
  UNION ALL SELECT 8999000000000093106, 'luohaiyan',  '罗海燕', 8999000000000093006
  UNION ALL SELECT 8999000000000093107, 'gaoshumin',  '高淑敏', 8999000000000093007
  UNION ALL SELECT 8999000000000093108, 'dengxuemei', '邓雪梅', 8999000000000093008
) v
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` u WHERE u.id = v.uid OR u.user_name = v.uname);

-- 3. 主岗位（每人恰一条 is_primary=1）
INSERT INTO `sys_employee_post`
  (`id`, `employee_id`, `role_id`, `dept_id`, `is_primary`,
   `effective_date`, `expire_date`, `create_by`, `create_time`,
   `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT v.pid, v.emp, v.role, v.dept, 1, v.hire, NULL, 'sql417', NOW(), 'sql417', NOW(), 1, 1
FROM (
  SELECT 8999000000000093201 AS pid, 8999000000000093001 AS emp, 3  AS role, 19580041 AS dept, '2016-08-01' AS hire
  UNION ALL SELECT 8999000000000093202, 8999000000000093002, 3,  19580061, '2018-07-01'
  UNION ALL SELECT 8999000000000093203, 8999000000000093003, 9,  19580068, '2019-09-01'
  UNION ALL SELECT 8999000000000093204, 8999000000000093004, 9,  19580069, '2017-08-01'
  UNION ALL SELECT 8999000000000093205, 8999000000000093005, 3,  19580072, '2020-07-01'
  UNION ALL SELECT 8999000000000093206, 8999000000000093006, 3,  19580074, '2015-07-01'
  UNION ALL SELECT 8999000000000093207, 8999000000000093007, 32, 19580039, '2013-07-01'
  UNION ALL SELECT 8999000000000093208, 8999000000000093008, 32, 19580039, '2016-09-01'
) v
WHERE NOT EXISTS (SELECT 1 FROM `sys_employee_post` p WHERE p.id = v.pid);

COMMIT;

-- 回查：8 条账号 × 员工 × 主岗位，角色/科室对齐
SELECT u.user_name, u.real_name, e.emp_code, r.role_name, d.dept_name, p.is_primary
  FROM `sys_user` u
  JOIN `sys_employee` e ON e.id = u.emp_id
  JOIN `sys_employee_post` p ON p.employee_id = e.id
  LEFT JOIN `sys_role` r ON r.id = p.role_id
  LEFT JOIN `sys_department` d ON d.id = p.dept_id
 WHERE u.create_by = 'sql417'
 ORDER BY u.user_name;
