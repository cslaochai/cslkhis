-- 331 医务调配角色（复用谭一鸣账号）
--
-- 医生侧与护士侧对称的缺口：医务科(26) 只有「人力配置标准 + 审核发布」，没有 list/add/edit，
-- 也不带 org:employee 权限；科主任(27)/科室排班员(28) 的 data_scope 都是本科室，排不了外本科室的人。
-- 结果就是「张三这个月在 A 科、下个月在 B 科」在排班层根本落不下去。
--
-- 本角色补这条链路：改医师岗位段（含生效/失效日期）+ 跨科室排门诊号源/急诊值班。
-- 审核发布仍归门诊办(25, outpatient:schedule:audit) 与医务科(26, 29627/2982)，调配员只排不自批。
--
-- 不挂进 sys_employee/sys_user：复用谭一鸣（8900000000000080001）现有账号，
-- 新增一条「医务调配员 × 医务科」非主岗位，登录后在顶栏切岗位即可。

SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 角色：医务调配员（全院数据范围，行政/管理类）
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `role_type`, `staff_type`, `data_scope`,
                        `sort_order`, `status`, `create_by`, `create_time`, `update_by`, `update_time`,
                        `del_flag`, `remark`, `create_by_id`, `update_by_id`)
SELECT 35, '10045', '医务调配员', 1, 6, 1, 42, 1, 'admin', NOW(), 'admin', NOW(), 0,
       '医务科人力调配岗：改医师岗位归属（含生效/失效日期）+ 跨科室排门诊/急诊班（不改编制）', 1, 1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 35 OR `role_code` = '10045');

-- 2. 菜单权限
--    门户(100/101) + 组织与资源/员工管理(800/803/2118)     —— 改医师岗位段
--    排班值守中心(2945) + 门诊排班(804/2120/2121/2122)     —— 或诊号源
--    急诊排班(29611/29612/29613/29614)                     —— 急诊值班（员工排班域不收医生，见 assertScope）
--    排班总览(2938)
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`,
                             `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT x.menu_id_seq, 35, x.menu_id, 'admin', NOW(), 'admin', NOW(), 1, 1
  FROM (SELECT 8920610350000000001 AS menu_id_seq, 100 AS menu_id
        UNION ALL SELECT 8920610350000000002, 101
        UNION ALL SELECT 8920610350000000003, 800
        UNION ALL SELECT 8920610350000000004, 803
        UNION ALL SELECT 8920610350000000005, 2118
        UNION ALL SELECT 8920610350000000006, 2945
        UNION ALL SELECT 8920610350000000007, 804
        UNION ALL SELECT 8920610350000000008, 2120
        UNION ALL SELECT 8920610350000000009, 2121
        UNION ALL SELECT 8920610350000000010, 2122
        UNION ALL SELECT 8920610350000000011, 29611
        UNION ALL SELECT 8920610350000000012, 29612
        UNION ALL SELECT 8920610350000000013, 29613
        UNION ALL SELECT 8920610350000000014, 29614
        UNION ALL SELECT 8920610350000000015, 2938) x
 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 35 AND rm.menu_id = x.menu_id);

-- 3. 岗位：谭一鸣 × 医务调配员 × 医务科（非主岗位，主岗位仍是医务科26，顶栏切岗位使用）
INSERT INTO `sys_employee_post` (`id`, `employee_id`, `role_id`, `dept_id`, `is_primary`,
                                 `effective_date`, `expire_date`, `create_by`, `create_time`,
                                 `update_by`, `update_time`, `create_by_id`, `update_by_id`)
SELECT 8999000000000092104, 8900000000000080001, 35, 19580077, 0, '2026-11-16', NULL,
       'admin', NOW(), 'admin', NOW(), 1, 1
 WHERE NOT EXISTS (SELECT 1 FROM `sys_employee_post` WHERE `id` = 8999000000000092104
                       OR (`employee_id` = 8900000000000080001 AND `role_id` = 35));

COMMIT;

-- 回查
SELECT '角色' AS what, id, role_code, role_name, data_scope, staff_type FROM `sys_role` WHERE id = 35;
SELECT '菜单数' AS what, COUNT(*) AS cnt FROM `sys_role_menu` WHERE role_id = 35;
SELECT '岗位' AS what, p.employee_id, u.user_name, p.role_id, p.dept_id, p.is_primary, p.effective_date
  FROM `sys_employee_post` p JOIN `sys_employee` e ON e.id = p.employee_id
  JOIN `sys_user` u ON u.emp_id = e.id
 WHERE p.employee_id = 8900000000000080001;
