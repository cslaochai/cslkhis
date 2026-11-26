-- 386 统计上报员落一个真实账号（病案室 康瑶）
-- 日期：2026-11-26
-- 背景：role41 统计上报员只有菜单授权、没有任何岗位行，等于无人可用。
--       统计上报与病案编码在多数医院同属病案统计科，先由病案室现有编码员兼任，
--       不新建账号，避免一个空角色长期挂着。
-- 做法：给康瑶（897001456，病案室 19580085）追加一条 role41 岗位，is_primary=0，
--       主岗位仍是病案编码员；顶栏「切换岗位」即可切到统计上报员视角。
-- 约束：sys_employee_post 唯一键 uk_emp_role_dept，本表无 del_flag，删除走物理删。

INSERT INTO sys_employee_post
    (id, employee_id, role_id, dept_id, is_primary, effective_date, expire_date,
     create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 9210000000000001201, 897001456, 41, 19580085, 0, '2026-11-26', NULL,
       'sql386', NOW(), 'sql386', NOW(), 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_employee_post
    WHERE employee_id = 897001456 AND role_id = 41 AND dept_id = 19580085
);

-- 回查：
-- SELECT employee_id, role_id, dept_id, is_primary FROM sys_employee_post WHERE employee_id = 897001456 ORDER BY role_id;
-- 每人恰一条 is_primary=1：
-- SELECT employee_id, COUNT(*) FROM sys_employee_post WHERE is_primary = 1 GROUP BY employee_id HAVING COUNT(*) <> 1;

-- 回滚：
-- DELETE FROM sys_employee_post WHERE id = 9210000000000001201;
