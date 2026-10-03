-- =============================================================================
-- 207-班次补夜班标记与班后休息时长（G-02 / G-03）
--
-- 【问题】`biz_staff_plan_rule` 里配了三个上限字段，但一个都不生效（G-12 假闭环）：
--   · max_week_hours            单周工时上限
--   · max_consecutive_night_days 连续夜班天数上限
--   · max_consecutive_work_days  连续上班天数上限
--   前两个能不能判出來，取决于「哪条排班是夜班」—— 而 biz_shift 里**没有任何夜班标记**：
--   现有字段只有 start_time / end_time / cross_day，靠它们猜夜班必然猜错：
--     · 00:00 起的「急诊后夜班」根本不跨天（cross_day=0），按跨天判会漏
--     · 「晚间门诊」18:00-21:00 按「开始时刻≥16:00」判会被当成夜班，那是延时门诊不是值班
--   ⇒ 夜班必须是**班次字典自己维护的属性**，不是从时刻推出来的结论。
--
-- 【本脚本做三件事】
--   1. biz_shift 加 is_night（1-夜班 0-白班）
--   2. biz_shift 加 need_rest_hours（下这个班之后至少要休息几小时，0=不限制）
--   3. 存量回填 + 给夜班一个默认的岗后休息时长（16 小时）
--
-- 【回填口径（可解释、可改）】
--   夜班 = 班次名含「夜」 **或** 跨零点收班（cross_day=1）。
--   · 用名字为主，是因为班次名是护士长/科室自己写下的业务意图，比我们从时刻推更准；
--   · 跨零点兜底，是为了接住「名字里没写夜但实际通宵」的班次（如未来新增的通宵班）；
--   · 明确排除的：晚间门诊 18:00-21:00（延时服务）、急诊前夜班以外的所有门诊班次。
--   ⚠ 回填只是让存量有个起点 —— 真正的归属是班次维护页上那两个字段，
--     后续由业务在字典里自行调整，代码一律以字段为准，不再猜名字。
--
-- 【默认值从哪来】
--   need_rest_hours = 16 是「下夜班 → 次日不能排早班」这条规则的通行取法
--   （23:00 收班 + 16h ≈ 次日下午 15:00 之后才能再排），不是本系统独创。
--   非夜班一律 0：白班之后不限制会误伤正常排班。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 加列（幂等）
-- -----------------------------------------------------------------------------
SET @c1 := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_shift' AND COLUMN_NAME = 'is_night');
SET @d1 := IF(@c1 = 0,
    CONCAT('ALTER TABLE biz_shift ADD COLUMN is_night tinyint NOT NULL DEFAULT 0 ',
           'COMMENT ''是否夜班（1-夜班 0-白班）：夜班流入判定与连续夜班上限的唯一依据'' AFTER cross_day'),
    'DO 0');
PREPARE s1 FROM @d1; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @c2 := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_shift' AND COLUMN_NAME = 'need_rest_hours');
SET @d2 := IF(@c2 = 0,
    CONCAT('ALTER TABLE biz_shift ADD COLUMN need_rest_hours decimal(4,1) NOT NULL DEFAULT 0.0 ',
           'COMMENT ''下此班后最短休息小时数（0-不限制；夜班通例取16）'' AFTER is_night'),
    'DO 0');
PREPARE s2 FROM @d2; EXECUTE s2; DEALLOCATE PREPARE s2;

-- -----------------------------------------------------------------------------
-- 2. 回填夜班标记（已人工改过的行不动：is_night 保持现状，避免每次跑脚本都把人改的洗回去）
--    只对「还没标记的」即 is_night=0 的行按口径判一遍 writes 1。
-- -----------------------------------------------------------------------------
UPDATE biz_shift
   SET is_night = 1
 WHERE del_flag = 0
   AND is_night = 0
   AND (shift_name LIKE '%夜%' OR cross_day = 1);

-- -----------------------------------------------------------------------------
-- 3. 夜班的岗后休息时长
--    只填「现在还是 0」的行：有人手工调过的（比如某科室要求 24h）不被覆盖。
-- -----------------------------------------------------------------------------
UPDATE biz_shift
   SET need_rest_hours = 16.0
 WHERE del_flag = 0 AND is_night = 1 AND need_rest_hours = 0;

UPDATE biz_shift
   SET need_rest_hours = 0.0
 WHERE del_flag = 0 AND is_night = 0 AND need_rest_hours <> 0;

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs sql/207-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 is_night 列已建' AS item, COUNT(*) AS cnt, '1' AS expect FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_shift' AND COLUMN_NAME = 'is_night'
UNION ALL SELECT 'V2 need_rest_hours 列已建', COUNT(*), '1' FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_shift' AND COLUMN_NAME = 'need_rest_hours'
UNION ALL SELECT 'V3 名字含夜的都是夜班', COUNT(*), '0' FROM biz_shift WHERE del_flag = 0 AND shift_name LIKE '%夜%' AND is_night = 0
UNION ALL SELECT 'V4 跨零点的都是夜班', COUNT(*), '0' FROM biz_shift WHERE del_flag = 0 AND cross_day = 1 AND is_night = 0
UNION ALL SELECT 'V5 夜班都配了休息时长', COUNT(*), '0' FROM biz_shift WHERE del_flag = 0 AND is_night = 1 AND need_rest_hours = 0
UNION ALL SELECT 'V6 非夜班不限制休息', COUNT(*), '0' FROM biz_shift WHERE del_flag = 0 AND is_night = 0 AND need_rest_hours <> 0
UNION ALL SELECT 'V7 A 夜班条数', COUNT(*), '7' FROM biz_shift WHERE del_flag = 0 AND is_night = 1
UNION ALL SELECT 'V8 A 白班条数', COUNT(*), '14' FROM biz_shift WHERE del_flag = 0 AND is_night = 0
UNION ALL SELECT 'V9 A 存量排班里夜班行数（留痕）', COUNT(*), '>0' FROM biz_staff_schedule s JOIN biz_shift h ON h.id = s.shift_id WHERE s.del_flag = 0 AND h.is_night = 1;
