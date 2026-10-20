-- =====================================================================
-- 20261020 急诊排班审核收口：急诊科室指定审核人 + 前置核对
-- ---------------------------------------------------------------------
-- 背景：
--   岗位出勤审核闭环的代码与 SQL 已就绪（283 增量加 sys_department.schedule_auditor_id
--   列；284 增量加 29603 审核按钮 + 29604 排班审批中心 + 1/10/26/27 四角色授权；
--   后端 submit/auditPublish/reject/recall/submitWeek/auditWeek 接口与前端审核按钮、
--   审批中心页面齐全）。本文件只补一件事：
--   急诊内科(19580048)/急诊外科(19580049)/急诊儿科(19580051) 三科的
--   schedule_auditor_id 目前为空（急诊科主任岗位尚未挂人）→ 急诊班排成草稿后
--   除系统管理员外无人能审核发布，值班无法生效。
--
-- 执行顺序：先确认 283、284 已执行（见"四、前置核对"），再执行本文件。
--
-- 审核人选定：暂设医务科谭一鸣（8900000000000080001，data_scope=1 全院）。
--   等急诊科主任挂岗（sys_employee_post role 27 × 急诊三科）后，
--   改设主任的 employee_id 即可（一行 UPDATE，见"三、换人"）。
--
-- 幂等：UPDATE 带 WHERE schedule_auditor_id IS NULL 收口，重复执行零变化。
-- =====================================================================

-- =====================================================================
-- 一、急诊三科指定审核人（必执行，值可换）
-- =====================================================================
UPDATE sys_department
SET schedule_auditor_id = 8900000000000080001,   -- 医务科 谭一鸣（可改）
    update_by           = 'admin',
    update_by_id        = 1,
    update_time         = NOW()
WHERE id IN ('19580048', '19580049', '19580051')
  AND schedule_auditor_id IS NULL;

-- =====================================================================
-- 二、回查（急诊三科应显示 schedule_auditor_id=8900000000000080001）
-- =====================================================================
-- SELECT id, dept_name, dept_leader_id, schedule_auditor_id
--   FROM sys_department
--  WHERE id IN ('19580048', '19580049', '19580051');

-- =====================================================================
-- 三、换人（急诊科主任挂岗后执行：把 8900000000000080001 换成主任 employee_id）
-- =====================================================================
-- UPDATE sys_department
-- SET schedule_auditor_id = <主任employee_id>,
--     update_by = 'admin', update_by_id = 1, update_time = NOW()
-- WHERE id IN ('19580048', '19580049', '19580051');

-- =====================================================================
-- 四、前置核对（283/284 是否已执行，未执行先补跑再执行本文件）
-- =====================================================================
-- 4.1 审核按钮与审批中心菜单必须存在（permission 都是 staff:schedule:audit）
-- SELECT id, menu_name, menu_type, permission
--   FROM sys_menu WHERE id IN (29603, 29604);
--
-- 4.2 schedule_auditor_id 列必须存在（283 增量）
-- SELECT COLUMN_NAME FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_department'
--    AND COLUMN_NAME = 'schedule_auditor_id';
--
-- 4.3 四角色应持有审核按钮/审批中心（1 超管 / 10 护士长 / 26 医务科 / 27 科主任）
-- SELECT rm.role_id, m.id, m.menu_name
--   FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
--  WHERE rm.role_id IN (1,10,26,27) AND m.id IN (29603, 29604)
--  ORDER BY rm.role_id, m.id;

-- ⚠️ 执行后清 Redis 权限缓存 his:perm:role:*（TTL 10min）或退出重登，新按钮/菜单才会出现。
