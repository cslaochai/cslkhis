-- =====================================================================
-- 285 排班变更留痕允许「整周级」记录（staff_schedule_id 可空）
-- ---------------------------------------------------------------------
-- 背景：
--   全院岗位排班的审批闭环新增了「整周提交审核 / 整周审核发布」两个动作。
--   整周动作一次搬动本单元整周（周一～周日）的若干排班行，**没有唯一的单行**可挂，
--   于是留痕只能落一条「周级」记录：staff_schedule_id 为空，amount 记搬动行数，
--   reason 记周区间。这与护理排班域的既有做法一致（护理的周级留痕也允许不带单行）。
--
--   原表定义 staff_schedule_id BIGINT NOT NULL，插入整周级留痕会直接
--   「Field 'staff_schedule_id' doesn't have a default value」→ 整周提交/整周发布
--   整个事务回滚（实测 500，且草稿→待审核的迁移被一起回滚掉）。
--
-- 影响面：
--   仅放宽可空性，不改列名/类型/索引（idx_change_schedule 仍可用）。
--   既有换班/代班/停班/加减号/出诊变更等单行留痕照旧写入具体 id，语义不变；
--   读侧 ScheduleChangeLogServiceImpl.toVOs 对 staff_schedule_id 为空的行
--   本来就按「无单行快照」处理（scheduleDate/orgName 留空），无需改代码。
--
-- 幂等：MySQL 无通用 ALTER ... IF EXISTS，重复执行前请先确认列是否已可空。
-- =====================================================================

ALTER TABLE biz_clinic_source_change_log
    MODIFY COLUMN staff_schedule_id BIGINT NULL COMMENT '员工排班ID（整周级审批记录为空）',
    MODIFY COLUMN action_type TINYINT NOT NULL COMMENT '变更类型（1-换班 2-代班 3-停班 4-加号 5-减号 6-出诊变更 7-提交审核 8-审核发布 9-驳回 10-整周提交 11-整周审核发布 12-撤回 13-进入调班）';

-- 回查：列已可空
-- SELECT column_name, is_nullable, column_comment
--   FROM information_schema.COLUMNS
--  WHERE table_schema = 'hn_biz_his' AND table_name = 'biz_clinic_source_change_log'
--    AND column_name IN ('staff_schedule_id', 'action_type');
