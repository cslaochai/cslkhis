-- =====================================================================
-- 20261020 急诊班次改三班倒：消除 23:00-00:00 空窗（合规修正）
-- ---------------------------------------------------------------------
-- 背景：
--   急诊医生班次原为 白班08-16 / 前夜16-23 / 后夜00-08，23:00-00:00 有 1 小时
--   无人在岗，违反《急诊科建设与管理指南(试行)》"急诊24小时开放"硬底线。
--   本次改为行业标准三班倒：白班08-16 / 前夜16-次日00 / 后夜00-08，无缝覆盖24h。
--
-- 关键约束（来自代码，勿改写法）：
--   ① onDutyAt「此刻在岗」判定走 ShiftCoverUtil.covers：
--      start > end 即按跨零点处理（[start,24:00) ∪ [00:00,end)）。
--      因此前夜班必须存 end_time='00:00' + cross_day=1（次日收）；
--      禁止存 '24:00' —— LocalTime.parse("24:00") 解析失败返回 null，
--      排班会被判定为「不在岗」，整条班次失效。
--   ② onDutyAt 读的是 biz_schedule 行上的班次快照（start_time/end_time），
--      不是实时读 biz_shift 字典 → 只改字典只影响之后新排的班，
--      已排的未来前夜班行必须同步改快照，否则那些天仍空窗。
--
-- 幂等：三段 UPDATE 均带 WHERE end_time='23:00' 收口，重复执行零变化。
-- =====================================================================

-- =====================================================================
-- 一、班次字典：前夜班 16:00-23:00 → 16:00-次日00:00（必执行）
-- =====================================================================
UPDATE biz_shift
SET end_time         = '00:00',
    cross_day        = 1,          -- 0-不跨 → 1-次日收
    duration_minutes = 480,        -- 420(7h) → 480(8h)，与前夜/后夜同为8h
    update_by        = 'admin',
    update_by_id     = 1,
    update_time      = NOW()
WHERE id = '890000000000000008'
  AND end_time = '23:00';

-- =====================================================================
-- 二、已排班快照同步（必执行）：onDutyAt 读排班行快照，未来前夜班行不改仍空窗
--     只改 schedule_date >= 今天 的未来行，历史班次事实保持原样
-- =====================================================================
UPDATE biz_schedule
SET end_time    = '00:00',
    update_by   = 'admin',
    update_by_id = 1,
    update_time = NOW()
WHERE shift_id = '890000000000000008'
  AND end_time = '23:00'
  AND schedule_date >= CURDATE();

-- =====================================================================
-- 三、可选：急诊门诊号源快照同步
--     仅当「急诊门诊号源」仍在用（20261019 第四段停用未执行）时需要；
--     已停用或已切岗位排班取数的库可跳过本段。
-- =====================================================================
-- UPDATE biz_clinic_source
-- SET end_time = '00:00'
-- WHERE shift_id = '890000000000000008'
--   AND end_time = '23:00'
--   AND schedule_date >= CURDATE();

-- =====================================================================
-- 四、核对
-- =====================================================================
-- 4.1 班次字典：三班应无缝覆盖 24h（后夜00-08 / 白班08-16 / 前夜16-次日00）
-- SELECT id, shift_name, start_time, end_time, cross_day, is_night, duration_minutes
--   FROM biz_shift
--  WHERE id IN (890000000000000007, 890000000000000008, 890000000000000009)
--  ORDER BY start_time;

-- 4.2 已排急诊前夜班快照：未来行 end_time 应全部为 00:00
-- SELECT schedule_date, employee_name, start_time, end_time
--   FROM biz_schedule
--  WHERE shift_id = '890000000000000008'
--    AND schedule_date >= CURDATE()
--  ORDER BY schedule_date;

-- 4.3 空窗复查：任意时刻急诊科单元（19580048 急诊内科等）都应至少一条已发布在岗
-- SELECT '00-08' seg, COUNT(*) FROM biz_schedule WHERE schedule_date=CURDATE() AND shift_id='890000000000000009'
--   AND publish_status IN (2,4)
-- UNION ALL SELECT '08-16', COUNT(*) FROM biz_schedule WHERE schedule_date=CURDATE() AND shift_id='890000000000000007'
--   AND publish_status IN (2,4)
-- UNION ALL SELECT '16-24', COUNT(*) FROM biz_schedule WHERE schedule_date=CURDATE() AND shift_id='890000000000000008'
--   AND publish_status IN (2,4);
