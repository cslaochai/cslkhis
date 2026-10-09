-- =============================================================================
-- 213 · 病区护理人力标准铺底（P2-G28）
--
-- 【为什么补这一刀】
-- 建需求层（sql/212）时实测发现：49 个启用病区里**只有 3 个有护理人力标准**
-- （呼吸/消化/心血管），其余 46 个一条都没有。后果有两层：
--   1. 校验层：StaffPlanRuleServiceImpl.matchRule() 匹配不到规则时直接 return null 跳过 ——
--      「没配标准」在系统里的真实含义是「这个单元没人管」，排班员把病区人全删光也不报错；
--   2. 需求层：需求 = MAX(患者工时派生, 病区核定下限, 1)，下限常年为 0 → 需求只剩患者派生值。
--      实测全科医学科病区在院 11 人只算出 3 人需求，而同规模病区实际在岗 6 人 ——
--      下限缺位直接把需求算少了。
-- 标准不是需求层的可选输入，是它的地板。地板没有，MAX 就成了摆设。
--
-- 【为什么不早铺】
-- sql/204 当时的口径是「只铺已排过班的组合，按历史最小在岗反推」，刻意不给没排过班的
-- 单元铺 —— 没有事实就没有基线。这个克制是对的，但留下了一个洞：护理排班只在 3 个病区
-- 排过，于是 46 个病区永远等不到「排过班」那一天，也就永远没有标准。
-- 需求层建起来以后，情况变了：病区的护理需求可以从**开放床位**推，不必等排班事实。
--
-- 【下限按开放床位分档】
--   30 床以上 → 5 人/天（与现有 3 条实测下限完全一致：呼吸/消化/心血管都是 30 床、下限 5）
--   20~29 床 → 4 人/天
--   10~19 床 → 3 人/天
--   10 床以下 → 2 人/天
-- 分档的锚点是那 3 条实测值，不是拍脑袋；其余三档按同比例下推。
-- 这里的「人/天」是 24 小时三班覆盖的最低运营配置（白班 + 前夜 + 后夜都得有人顶），
-- 与患者数无关 —— 病区今天一个患者没有，夜班也不能空岗。
--
-- 【不会把存量排班判违规】
-- 46 个病区在此之前一条护理排班都没有（护理排班事实只落在有标准的那 3 个病区），
-- 铺下限不会让任何既成排班突然违规。已有的 3 条一条都不动。
-- =============================================================================

SET @rn := (SELECT COALESCE(MAX(id), 896750000000100000) FROM biz_staff_plan_rule WHERE id < 896750000000900000);

INSERT INTO biz_staff_plan_rule (id, org_type, org_id, org_name, shift_id, staff_type,
                                 min_staff, max_staff, max_week_hours,
                                 max_consecutive_night_days, max_consecutive_work_days,
                                 status, create_time, update_time, del_flag, remark)
SELECT
  @rn := @rn + 1,
  2, w.ward_id, w.ward_name, 0, 2,
  CASE WHEN w.total_beds >= 30 THEN 5
       WHEN w.total_beds >= 20 THEN 4
       WHEN w.total_beds >= 10 THEN 3
       ELSE 2 END,
  0, 48.0, 2, 6,
  1, NOW(), NOW(), 0,
  CONCAT('sql/213 病区护理兜底：开放床位 ', w.total_beds,
         ' 张按档位铺下限量（30床↑5人 / 20~29床4人 / 10~19床3人 / 10床↓2人），',
         '口径=24小时三班覆盖的最低运营配置，锚点是已排班病区的实测下限，需护理部复核定稿')
FROM sys_ward w
WHERE w.status = 1
  AND NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule r
                   WHERE r.del_flag = 0 AND r.org_type = 2 AND r.org_id = w.ward_id
                     AND r.staff_type = 2 AND r.shift_id = 0);

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs sql/213-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 启用病区里还有没护理标准的（必须为 0）' AS item, COUNT(*) AS cnt, '0' AS expect FROM sys_ward w WHERE w.status = 1 AND NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule r WHERE r.del_flag = 0 AND r.org_type = 2 AND r.org_id = w.ward_id AND r.staff_type = 2 AND r.shift_id = 0)
UNION ALL SELECT 'V2 病区护理标准总数（= 启用病区数）', COUNT(*), '49' FROM biz_staff_plan_rule WHERE del_flag = 0 AND org_type = 2 AND staff_type = 2 AND shift_id = 0
UNION ALL SELECT 'V3 本次铺进来的行数', COUNT(*), '46' FROM biz_staff_plan_rule WHERE del_flag = 0 AND org_type = 2 AND staff_type = 2 AND shift_id = 0 AND remark LIKE 'sql/213%'
UNION ALL SELECT 'V4 下限小于 2 的病区（不允许）', COUNT(*), '0' FROM biz_staff_plan_rule WHERE del_flag = 0 AND org_type = 2 AND staff_type = 2 AND shift_id = 0 AND min_staff < 2
UNION ALL SELECT 'V5 存量 3 条没被改动（30 床病区下限仍是 5）', COUNT(*), '3' FROM biz_staff_plan_rule WHERE del_flag = 0 AND org_type = 2 AND staff_type = 2 AND shift_id = 0 AND min_staff = 5 AND remark NOT LIKE 'sql/213%'
UNION ALL SELECT 'V6 30 床及以上病区的下限都是 5', COUNT(*), '0' FROM biz_staff_plan_rule r JOIN sys_ward w ON w.ward_id = r.org_id WHERE r.del_flag = 0 AND r.org_type = 2 AND r.staff_type = 2 AND r.shift_id = 0 AND w.total_beds >= 30 AND r.min_staff <> 5
UNION ALL SELECT 'V7 每个病区都写了铺底依据', COUNT(*), '0' FROM biz_staff_plan_rule WHERE del_flag = 0 AND org_type = 2 AND staff_type = 2 AND shift_id = 0 AND IFNULL(remark, '') = '';
