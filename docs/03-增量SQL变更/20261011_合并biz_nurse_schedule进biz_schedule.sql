-- =====================================================================
-- 2026-10-11  合并 biz_nurse_schedule 进排班底座 biz_schedule（P5）
-- =====================================================================
-- 背景：biz_nurse_schedule 是 biz_schedule 的冗余降维副本——
--   · 实库 1680 条护理行 100% 有 staff_schedule_id 指向活底座行，0 悬空；
--   · 逐字段比对（shift/start/end/工时/状态）与底座行完全一致
--     （唯一"差异"是休息行本表存 NULL、底座存 0，同一语义）；
--   · 它唯一差异化的 unit_type=2（门诊科室）零数据，ward_id 与 unit_id 全表相等；
--   · 唯一键 (employee_id, schedule_date) 一天一格，连早晚双班都表达不了。
-- 代码侧：护理读写已全部切到 biz_schedule（staff_type IN (2,7) + 排班单元），
--         实体/Mapper/副本写路径已删除。
-- 执行口径：按业务确认，副本数据不做回填核对、不做备份，直接退役。
-- 幂等性：DROP 带 IF EXISTS，可重复执行。

DROP TABLE IF EXISTS biz_nurse_schedule;

-- 执行后核对：护理排班矩阵、护理台账、月度工时、分诊在岗护士（biz_schedule 护理序列行）应与退役前一致。
SELECT COUNT(*) AS 护理序列排班行数
  FROM biz_schedule
 WHERE del_flag = 0 AND staff_type IN (2, 7);
