-- ============================================================================
-- 20261027 急诊内科分诊护士测试账号（汤晓岚 / tangxiaolan）
-- ============================================================================
-- 背景：急诊挂号链路演示需要「急诊护士工作站」本部账号。急诊登记（菜单 205/2010，
--   perm opd:emergency:add）授权 role 9（分诊护士），现有 role 9 测试账号林心怡
--   （linxinyi）是心内科门诊分诊夹具、被分诊验证脚本按该身份引用，不改派，新增一人。
-- 三件套：sys_employee + sys_user + sys_employee_post（role 9 + dept 19580048 急诊内科）。
-- 权限零新增：急诊护士工作站 205/2010/2601/2607 与分诊复评 2607 等 role 9 已有授权，
--   新账号随角色直接继承，不动 sys_menu / sys_role_menu。
-- 口令：全库统一 123456（bcrypt 串复用林心怡行同款哈希）。
-- 幂等：按 id 物理删再插（账号行无留档价值）；user_name 唯一键兜底再清一次。
-- ⚠ 执行后无需清权限 Redis：新用户不存在 his:user:identity:{username} 旧缓存。
-- 号段：8999000000000091xx，与林心怡夹具 8999000000000090xx 及其他雪花段不重叠。
-- ============================================================================

DELETE FROM sys_employee_post WHERE id = '8999000000000091003';
DELETE FROM sys_user          WHERE id = '8999000000000091002';
DELETE FROM sys_employee      WHERE id = '8999000000000091001';
DELETE FROM sys_user          WHERE user_name = 'tangxiaolan';

INSERT INTO `sys_employee` (`id`, `emp_code`, `emp_name`, `emp_type`, `gender`, `birth_date`, `hire_date`,
                            `id_card`, `phone`, `email`, `dept_id`, `dept_name`, `title`, `position`,
                            `specialty`, `education`, `avatar`, `sign_image`, `is_expert`, `is_registered`,
                            `is_fixed`, `expert_price`, `status`, `create_by`, `create_time`, `update_by`,
                            `update_time`, `del_flag`, `remark`, `create_by_id`, `update_by_id`)
VALUES
(8999000000000091001, 'E737001', '汤晓岚', 2, 2, '1996-08-21', '2021-06-01',
 '430726199608212026', '13900001357', 'tangxiaolan@cslk-his.local', 19580048, '急诊内科',
 '203', '15', '急诊预检分级、抢救配合', '3', NULL, NULL, 0, NULL, NULL, '0.00',
 1, 'admin', NOW(), 'admin', NOW(), 0, '测试夹具·急诊内科分诊护士', 1, 1);

INSERT INTO `sys_user` (`id`, `user_name`, `password`, `real_name`, `emp_id`, `user_type`, `patient_id`,
                        `openid`, `avatar`, `last_login_time`, `last_login_ip`, `login_count`,
                        `password_update_time`, `status`, `create_by`, `create_time`, `update_by`,
                        `update_time`, `del_flag`, `remark`, `create_by_id`, `update_by_id`)
VALUES
(8999000000000091002, 'tangxiaolan',
 '$2a$10$wnMHh1IGlx.hjaj6A97Ose7POcAJv4osIBPjvtkrgf1QCS.FLT1ca', '汤晓岚',
 8999000000000091001, 1, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1,
 'admin', NOW(), 'admin', NOW(), 0, '测试夹具·急诊内科分诊护士', 1, 1);

INSERT INTO `sys_employee_post` (`id`, `employee_id`, `role_id`, `dept_id`, `is_primary`,
                                 `effective_date`, `expire_date`, `create_by`, `create_time`,
                                 `update_by`, `update_time`, `create_by_id`, `update_by_id`)
VALUES
(8999000000000091003, 8999000000000091001, 9, 19580048, 1,
 NULL, NULL, 'admin', NOW(), 'admin', NOW(), 1, 1);

-- ---------- 回查 ----------
-- SELECT e.emp_code, e.emp_name, u.user_name, p.role_id, p.dept_id
--   FROM sys_employee e
--   JOIN sys_user u ON u.emp_id = e.id AND u.del_flag = 0
--   JOIN sys_employee_post p ON p.employee_id = e.id AND p.del_flag = 0
--  WHERE e.id = 8999000000000091001;
-- 预期：E737001 / 汤晓岚 / tangxiaolan / 9（分诊护士）/ 19580048（急诊内科）
-- role 9 应已有急诊护士工作站授权：SELECT COUNT(*) FROM sys_role_menu WHERE role_id = 9 AND menu_id IN (205, 2010);
