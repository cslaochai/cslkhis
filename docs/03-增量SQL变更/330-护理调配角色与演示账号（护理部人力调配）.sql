-- 330 护理调配角色与演示账号（护理部人力调配）
--
-- 业务口径：护理部对全院护理人力有直接配备与调整权（卫健委《医院实施优质护理服务工作标准（试行）》），
-- 但现有角色里没有「能改岗位 + 能跨科室排班」的岗位：
--   护士长(10) 只能排本科室；科护士长(30) 只审核；护理部主任(29) 只备案；
--   科室排班员(28) 只有门诊排班权。
-- 本角色补的就是这条链路：把护士今天派到某科做分诊/输液，落成「岗位段 + 排班事实」。

SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 角色：护理调配员（全院数据范围，护理类别）
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `role_type`, `staff_type`, `data_scope`,
                        `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`,
                        `del_flag`, `remark`, `create_by_id`, `update_by_id`)
SELECT 34, '10044', '护理调配员', 1, 2, 1, 41, 1, 'admin', NOW(), 'admin', NOW(), 0,
       '护理部人力调配岗：改护士岗位归属（含生效/失效日期）+ 跨科室排班（不改编制）', 1, 1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 34 OR `role_code` = '10044');

-- 2. 菜单权限
--    门户(100/101) + 组织与资源/员工管理(800/803/2118)  —— 改岗位段
--    排班值守中心(2945) + 护理排班(331/2650/2651/2652) —— 病区矩阵排人
--    员工排班(2935/29351/29352/29353) + 排班总览(2938) —— 门诊/科室单元排人
--    编辑也走 :add，不额外造权限码
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`,
                             `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT x.menu_id_seq, 34, x.menu_id, 'admin', NOW(), 'admin', NOW(), 1, 1
  FROM (SELECT 8920610340000000001 AS menu_id_seq, 100 AS menu_id
        UNION ALL SELECT 8920610340000000002, 101
        UNION ALL SELECT 8920610340000000003, 800
        UNION ALL SELECT 8920610340000000004, 803
        UNION ALL SELECT 8920610340000000005, 2118
        UNION ALL SELECT 8920610340000000006, 2945
        UNION ALL SELECT 8920610340000000007, 331
        UNION ALL SELECT 8920610340000000008, 2650
        UNION ALL SELECT 8920610340000000009, 2651
        UNION ALL SELECT 8920610340000000010, 2652
        UNION ALL SELECT 8920610340000000011, 2935
        UNION ALL SELECT 8920610340000000012, 29351
        UNION ALL SELECT 8920610340000000013, 29352
        UNION ALL SELECT 8920610340000000014, 29353
        UNION ALL SELECT 8920610340000000015, 2938) x
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 34 AND rm.menu_id = x.menu_id);

-- 3. 员工：沈清（护理部）
INSERT INTO `sys_employee` (`id`, `emp_code`, `emp_name`, `emp_type`, `gender`, `birth_date`, `hire_date`,
                            `id_card`, `phone`, `email`, `dept_id`, `dept_name`, `title`, `position`,
                            `specialty`, `education`, `expert_price`, `status`,
                            `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`,
                            `remark`, `create_by_id`, `update_by_id`)
SELECT 8999000000000092101, 'E537002', '沈清', 2, 2, '1990-05-18', '2015-07-01',
       '430726199005180020', '13800000088', 'shenqing@cslk-his.local', 19580078, '护理部', NULL,
       '护理人力调配', '护理人力调配/跨科支援排班', '本科', 0, 1,
       'admin', NOW(), 'admin', NOW(), 0, '排班域演示·护理部人力调配员', 1, 1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_employee` WHERE `id` = 8999000000000092101 OR `emp_code` = 'E537002');

-- 4. 账号：shenqing / 123456（与演示账号同一口令，密码串直接沿用 admin 的 BCrypt）
INSERT INTO `sys_user` (`id`, `user_name`, `password`, `real_name`, `emp_id`, `user_type`, `status`,
                        `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`,
                        `remark`, `create_by_id`, `update_by_id`)
SELECT 8999000000000092102, 'shenqing', '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca',
       '沈清', 8999000000000092101, 1, 1, 'admin', NOW(), 'admin', NOW(), 0,
       '排班域演示·护理部人力调配员', 1, 1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `id` = 8999000000000092102 OR `user_name` = 'shenqing');

-- 5. 岗位：护理调配员 × 护理部（主岗位）
INSERT INTO `sys_employee_post` (`id`, `employee_id`, `role_id`, `dept_id`, `is_primary`,
                                 `effective_date`, `expire_date`, `create_by`, `create_time`,
                                 `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8999000000000092103, 8999000000000092101, 34, 19580078, 1, '2015-07-01', NULL,
       'admin', NOW(), 'admin', NOW(), 1, 1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_employee_post` WHERE `id` = 8999000000000092103);

COMMIT;

-- 回查（三道键判重 + 权限落地）
SELECT '角色' AS what, id, role_code, role_name, data_scope, staff_type FROM `sys_role` WHERE id = 34;
SELECT '菜单数' AS what, COUNT(*) AS cnt FROM `sys_role_menu` WHERE role_id = 34;
SELECT '账号' AS what, u.user_name, u.real_name, e.emp_code, p.role_id, p.dept_id, p.is_primary
  FROM `sys_user` u
  JOIN `sys_employee` e ON e.id = u.emp_id
  JOIN `sys_employee_post` p ON p.employee_id = e.id
 WHERE u.user_name = 'shenqing';
