-- =============================================================
-- 387 门诊部科室节点 + 前台导诊岗位归属纠偏
--
-- 为什么：
--   1) 组织树缺「门诊部」这个职能科室节点（院办/医务科/护理部都有，门诊部没有），
--      导致「前台导诊」「门诊办」这类门诊服务岗无处可挂。
--   2) 演示账号 许妍（897001426）的导诊岗位落在「院办」（19580076），
--      是铺底时按人事备案科室顺手放的，不是设计——角色本身不绑科室，
--      科室来自 sys_employee_post 的 (员工×角色×科室) 三元组。
--   3) 真实医院口径（华西医院门诊部导医招聘：岗位类型=辅助岗、所属部门=门诊部；
--      青岛市黄岛区人民医院：门诊部“下设…挂号室、导医”）都是归门诊部，
--      既不是院办，也不是护理部编制。
--
-- 本次做什么：
--   A. 新增科室「门诊部」（职能科室，挂行政后勤部，与院办/医务科/护理部同级）
--   B. 许妍的导诊岗位行 dept_id 院办 → 门诊部
--   C. 同步 sys_employee 的主岗位快照（dept_id/dept_name），否则快照与岗位漂移
--
-- 本次不做什么（刻意）：
--   - 康蓉(897001319 @ 普通体检科)、郑静文(897001323 @ VIP体检中心) 不动：
--     体检中心导检归体检中心，挪到门诊部反而是错的。
--   - sys_role 不动：role 上没有科室列，staff_type=6（行政其他/辅助岗）保持，
--     改 2(护理) 会让导诊进护理排班取人池（BizScheduleServiceImpl:446
--     .in(staffType, NURSE, NURSING_ASSISTANT)）与护理人力编制统计。
--   - 「门诊办」角色(25) 无 SQL 可改：角色不挂科室，配人时选门诊部即可。
--   - 「客户服务中心」(19580095) 不挪：它做的是工单受理与患者咨询，
--     与门诊一站式服务台不是一件事，挪动会改客服模块的科室口径。
--
-- 前置：
--   - sys_department 唯一键 uk_dept_code；本文件 id 段 19580096 与编码 10001412
--     已核对 dev 库 2026-11-20 导出，均未占用；INSERT 带 id/code 双键判重，可重复执行。
--   - sys_employee_post 唯一键 uk_emp_role_dept(employee,role,dept) 不含 del_flag，
--     本表只改不删；改 dept_id 后新三元组不与他人冲突（该员工角色7仅一行）。
--
-- 生效：许妍的登录 token 里带的是旧科室，执行后需重新登录（或切换岗位）才刷新。
-- =============================================================

-- -------------------------------------------------------------
-- 一、新增科室：门诊部（职能科室，挂行政后勤部，sort 12）
--     dept_type=5（其他/职能，与院办、医务科、护理部同档）
-- -------------------------------------------------------------
INSERT INTO sys_department
    (id, dept_code, dept_name, dept_type, parent_id, sort_order, dept_icon, dept_desc,
     contact_phone, location, dept_leader_id, is_open, status,
     create_by, create_time, update_by, update_time, del_flag, remark, create_by_id, update_by_id)
SELECT *
FROM (
    SELECT 19580096 AS id, '10001412' AS dept_code, '门诊部' AS dept_name, '5' AS dept_type,
           19580075 AS parent_id, 12 AS sort_order, NULL AS dept_icon, '门诊服务与就诊秩序管理' AS dept_desc,
           NULL AS contact_phone, '门诊楼1楼' AS location, NULL AS dept_leader_id, 1 AS is_open, 1 AS status,
           'sql387' AS create_by, NOW() AS create_time, 'sql387' AS update_by, NOW() AS update_time,
           0 AS del_flag, '职能科室：导医/导诊/预检分诊/一站式服务台归属' AS remark,
           1 AS create_by_id, 1 AS update_by_id
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_department d WHERE d.id = seed.id OR d.dept_code = seed.dept_code);

-- -------------------------------------------------------------
-- 二、许妍（897001426）的导诊岗位：院办 → 门诊部
--     post id=897002426（role_id=7 前台导诊，is_primary=1）
-- -------------------------------------------------------------
UPDATE sys_employee_post
SET dept_id     = 19580096,
    update_by   = 'sql387',
    update_time = NOW(),
    update_by_id = 1
WHERE id = 897002426
  AND employee_id = 897001426
  AND role_id = 7;

-- -------------------------------------------------------------
-- 三、同步 sys_employee 主岗位快照
--     该两列的唯一写入方是 EmployeePostService.replacePosts；
--     本次是数据迁移，必须同步，否则「科室树」与「员工档案」两处口径不一致。
-- -------------------------------------------------------------
UPDATE sys_employee
SET dept_id     = 19580096,
    dept_name   = '门诊部',
    update_by   = 'sql387',
    update_time = NOW(),
    update_by_id = 1
WHERE id = 897001426;

-- -------------------------------------------------------------
-- 回查 1：门诊部节点应存在且挂在行政后勤部下
-- -------------------------------------------------------------
SELECT d.id, d.dept_code, d.dept_name, d.dept_type, d.parent_id, p.dept_name AS parent_name
FROM sys_department d
         LEFT JOIN sys_department p ON p.id = d.parent_id
WHERE d.id = 19580096;

-- 回查 2：许妍岗位行应落在门诊部（应为 1 行）
SELECT ep.id, ep.employee_id, e.emp_name, ep.role_id, r.role_name, ep.dept_id, d.dept_name, ep.is_primary
FROM sys_employee_post ep
         JOIN sys_employee e ON e.id = ep.employee_id
         JOIN sys_role r ON r.id = ep.role_id
         JOIN sys_department d ON d.id = ep.dept_id
WHERE ep.employee_id = 897001426;

-- 回查 3：员工主岗位快照与岗位行一致（两列应同为 19580096 / 门诊部）
SELECT id, emp_name, dept_id, dept_name FROM sys_employee WHERE id = 897001426;

-- 回查 4：三人里只应许妍被改（康蓉/郑静文仍分别在各体检科）
SELECT ep.employee_id, e.emp_name, ep.role_id, ep.dept_id, d.dept_name
FROM sys_employee_post ep
         JOIN sys_employee e ON e.id = ep.employee_id
         JOIN sys_department d ON d.id = ep.dept_id
WHERE ep.role_id = 7
ORDER BY ep.employee_id;

-- -------------------------------------------------------------
-- 回滚
-- -------------------------------------------------------------
-- UPDATE sys_employee_post SET dept_id = 19580076, update_by = 'sql387-rollback', update_time = NOW(), update_by_id = 1
-- WHERE id = 897002426 AND employee_id = 897001426 AND role_id = 7;
-- UPDATE sys_employee SET dept_id = 19580076, dept_name = '院办', update_by = 'sql387-rollback', update_time = NOW(), update_by_id = 1
-- WHERE id = 897001426;
-- DELETE FROM sys_department WHERE id = 19580096 AND dept_code = '10001412';
