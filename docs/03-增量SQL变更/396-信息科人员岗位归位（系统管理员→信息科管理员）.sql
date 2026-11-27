-- ============================================================================
-- 396 信息科人员岗位归位：role 1 系统管理员 → role 33 信息科管理员
-- 日期：2026-11-26
-- ============================================================================
-- 背景：
--   role 1 系统管理员(10012, data_scope=1 全院) 是兜底超管角色，按基线只应保留种子
--   超管 admin(employee_id=1, 院本部 1958001) 一人一科室。实测发现信息科 3 人被误挂
--   role 1 主岗位，与蒋俊杰(role 33 信息科管理员)同岗不同角色：
--     · 897001443 李瑞 lirui  —— 兼岗 role 39 设备与供应室管理员@设备科(19580082, 非主)，保留
--     · 897001444 彭慧 penghui
--     · 897001445 容欣 rongxin
--   本次把 3 人的主岗位由 role 1@信息科(19580081) 迁到 role 33@信息科(19580081)，
--   对齐蒋俊杰；role 33 菜单授权不补，与蒋俊杰现有信息科运维栈一致。
--
-- 口径（复刻 313 信息科固定账号绑岗）：
--   · 岗位表 sys_employee_post 无 del_flag，摘除走物理 DELETE。
--   · 唯一键 uk_emp_role_dept(employee_id, role_id, dept_id)；先 INSERT 新岗（复用原岗
--     effective_date/expire_date），再 DELETE 原 role 1 岗，避免日期漂移。
--   · 新岗 is_primary=1（主岗位）；员工快照 sys_employee.dept_id/dept_name 本就是信息科，
--     科室不变，无需回填。
--   · id 段 892611012000000102–104，接续 313 的 101。
--
-- 注意：
--   · 迁移后这 3 人权限从超管全量降到 role 33 信息科运维栈；业务域页面本就不该信息科访问。
--   · 岗位=角色×科室是鉴权直接 join 的事实来源，角色集合变了必须清身份缓存：
--     按 employee_id 清 his:user:identity:<username>（3 人账号 lirui/penghui/rongxin），
--     本次由人工自清 Redis，脚本不代跑。
-- ============================================================================

-- ---------- 1. 先插 role 33 主岗（复用原岗生效/失效日期） ----------
INSERT INTO sys_employee_post
  (id, employee_id, role_id, dept_id, is_primary, effective_date, expire_date,
   create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892611012000000102, employee_id, 33, dept_id, 1, effective_date, expire_date,
       'sql396', NOW(), 'sql396', NOW(), 1, 1
FROM sys_employee_post
WHERE employee_id = 897001443 AND role_id = 1 AND dept_id = 19580081;

INSERT INTO sys_employee_post
  (id, employee_id, role_id, dept_id, is_primary, effective_date, expire_date,
   create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892611012000000103, employee_id, 33, dept_id, 1, effective_date, expire_date,
       'sql396', NOW(), 'sql396', NOW(), 1, 1
FROM sys_employee_post
WHERE employee_id = 897001444 AND role_id = 1 AND dept_id = 19580081;

INSERT INTO sys_employee_post
  (id, employee_id, role_id, dept_id, is_primary, effective_date, expire_date,
   create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 892611012000000104, employee_id, 33, dept_id, 1, effective_date, expire_date,
       'sql396', NOW(), 'sql396', NOW(), 1, 1
FROM sys_employee_post
WHERE employee_id = 897001445 AND role_id = 1 AND dept_id = 19580081;

-- ---------- 2. 删原 role 1 主岗（物理删；李瑞 role 39 兼岗不动） ----------
DELETE FROM sys_employee_post
WHERE employee_id IN (897001443, 897001444, 897001445)
  AND role_id = 1 AND dept_id = 19580081;

-- ---------- 3. 回查 ----------
-- 3.1 三人岗位明细：李瑞应 2 岗（33 主 + 39 非主），彭慧/容欣应 1 岗（33 主）
-- SELECT employee_id, role_id, dept_id, is_primary FROM sys_employee_post
--   WHERE employee_id IN (897001443,897001444,897001445) ORDER BY employee_id, is_primary DESC;
-- 3.2 role 1 仅剩 admin(employee_id=1) 院本部一岗
-- SELECT employee_id, role_id, dept_id, is_primary FROM sys_employee_post WHERE role_id=1;

-- ---------- 回滚 ----------
-- 1) 删新 role 33 岗：
-- DELETE FROM sys_employee_post WHERE create_by='sql396' AND role_id=33 AND dept_id=19580081;
-- 2) 恢复原 role 1 主岗（id/生效日取自删除前快照）：
-- INSERT INTO sys_employee_post (id, employee_id, role_id, dept_id, is_primary, effective_date, expire_date,
--   create_by, create_time, update_by, update_time, create_by_id, update_by_id) VALUES
--   (897002443, 897001443, 1, 19580081, 1, '2001-02-01', NULL, 'admin', NOW(), 'system', NOW(), 1, 1),
--   (897002444, 897001444, 1, 19580081, 1, '2012-05-01', NULL, 'admin', NOW(), 'system', NOW(), 1, 1),
--   (897002445, 897001445, 1, 19580081, 1, '2015-07-01', NULL, 'admin', NOW(), 'system', NOW(), 1, 1);
