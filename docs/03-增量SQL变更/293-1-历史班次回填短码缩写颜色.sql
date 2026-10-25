-- 293-1 历史班次回填短码/缩写/颜色/夜班津贴标记
-- 背景：293 加列后，历史 21 条班次的 shift_code / short_name / color 全为 NULL，
--      排班总览图没色块、下拉没短码。按班次名称关键词首版打标，跑前请人工核对。
-- 规则：
--   · 含「夜/晚/前夜/后夜/总值班」→ 夜班（红，短码 N/E，计夜班津贴）
--   · 含「上午/白班/全天」且不含「晚」→ 早班（绿，短码 AM/D）
--   · 含「下午/中班/晚间门诊」→ 午班（橙，短码 PM）
--   · 含「休/听/备/帮」→ 休息类（灰，短码 R，不计工时）
-- 注意：只打 NULL 行，已手工配置过的行不动。

-- 1) 夜班类：红底「夜」，短码按前夜/后夜/总值班细分
UPDATE `biz_shift`
SET `short_name` = '夜',
    `color` = '#F56C6C',
    `is_night` = 1,
    `count_night_allowance` = 1
WHERE `del_flag` = 0
  AND `short_name` IS NULL
  AND (`shift_name` LIKE '%夜%' OR `shift_name` LIKE '%前夜%' OR `shift_name` LIKE '%后夜%'
       OR `shift_name` LIKE '%总值班%');

-- 2) 上午/白班/全天类：绿底「早」「全」
UPDATE `biz_shift`
SET `short_name` = CASE WHEN `shift_name` LIKE '%全天%' THEN '全' ELSE '早' END,
    `color` = '#67C23A',
    `shift_code` = CASE WHEN `shift_name` LIKE '%全天%' THEN 'D' ELSE 'AM' END
WHERE `del_flag` = 0
  AND `short_name` IS NULL
  AND (`shift_name` LIKE '%上午%' OR `shift_name` LIKE '%白班%' OR `shift_name` LIKE '%全天%'
       OR (`shift_name` LIKE '%门诊%' AND `shift_name` NOT LIKE '%晚%' AND `shift_name` NOT LIKE '%下午%'));

-- 3) 下午/中班/晚间门诊类：橙底「午」
UPDATE `biz_shift`
SET `short_name` = '午',
    `color` = '#E6A23C',
    `shift_code` = 'PM'
WHERE `del_flag` = 0
  AND `short_name` IS NULL
  AND (`shift_name` LIKE '%下午%' OR `shift_name` LIKE '%中班%' OR `shift_name` LIKE '%晚间%');

-- 4) 休息/听班/备班/帮班类：灰底「休/听/备」，不计工时
UPDATE `biz_shift`
SET `short_name` = CASE
        WHEN `shift_name` LIKE '%听%' THEN '听'
        WHEN `shift_name` LIKE '%备%' THEN '备'
        ELSE '休' END,
    `color` = '#909399',
    `count_work_hours` = 0
WHERE `del_flag` = 0
  AND `short_name` IS NULL
  AND (`shift_name` LIKE '%休%' OR `shift_name` LIKE '%听%'
       OR `shift_name` LIKE '%备%' OR `shift_name` LIKE '%帮%');

-- 5) 跑完回查：还剩哪些行没打上标（人工补）
-- SELECT id, shift_name, start_time, end_time, short_name, color FROM biz_shift
-- WHERE del_flag = 0 AND short_name IS NULL;
