-- =============================================================================
-- 206-人力配置标准消双轨（护理页并入 biz_staff_plan_rule）
--
-- 【背景】排班域施工蓝图 §4-P0 / G-08。
--   人力配置标准在系统里有**两份**：
--     · 旧 biz_nurse_schedule_rule（18 条）—— 护理排班页读写（`NurseScheduleServiceImpl` 第 81 行）
--     · 新 biz_staff_plan_rule（61 条，`org_type=2 病区 × staff_type=2 护理` 那 18 条）
--       —— 全院岗位排班的人力闸门 `StaffPlanRuleServiceImpl.reviewAfterChange` 读它
--   两套并存、互不相认：**护理页配的下限进不了底座校验，底座配的标准护理页看不见**。
--   排班员在护理页把某班下限改成 8，排班时照样能排到只剩 1 人，系统一声不吭。
--
-- 【实测现状（2026-10-02）】
--   旧表 18 条 / 新表病区护理 18 条，**逐字段比对差异 0 行**，且旧表 create_time、update_time
--   全为 NULL —— 说明这两份是同一次初始化灌进去的，不是两拨人各配各的。
--   ⇒ 所以本脚本**不搬数据**，只做三件事：补漏、定源、标记。
--
-- 【本脚本做三件事】
--   1. 补：旧表有、新表没有的（实测 0 条，但脚本要能保证这条永远成立）→ 按同字段插进新表
--   2. 定：以后只有一个源 —— biz_staff_plan_rule。代码侧已改（`NurseScheduleServiceImpl`
--      的 ruleList / ruleUpsert / ruleDeleteById / 告警读取全部走 `StaffPlanRuleService`）
--   3. 标：旧表加 `deprecated` 列并置 1，把「这张表已经只是历史」写进表结构，
--      免得三个月后有人又对着它开发。
--
-- 【刻意不做】
--   · 不删旧表、不改旧表 status：还有外部报表/脚本可能在读它，删了就查不到当初配的什么。
--   · 不在旧表上建触发器来禁止写入：纪律靠**表结构上的标记**看得见、查得到，
--     不靠数据库里一个看不见的杀手。（写入侧的收口在代码里：护理页的三段维护已全部走 StaffPlanRuleService）
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 旧表加「已废弃」标记列（幂等）
-- -----------------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                     WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'biz_nurse_schedule_rule'
                       AND COLUMN_NAME = 'deprecated');
-- 注意：MySQL 默认把 || 当「逻辑或」，拼接只能用 CONCAT，别写成 a || b。
SET @ddl := IF(@col_exists = 0,
    CONCAT('ALTER TABLE biz_nurse_schedule_rule ADD COLUMN deprecated tinyint NOT NULL DEFAULT 0 ',
           'COMMENT ''1=已并入 biz_staff_plan_rule（sql/206 起只读，勿再写入）'' AFTER status'),
    'DO 0');
PREPARE st_ddl FROM @ddl; EXECUTE st_ddl; DEALLOCATE PREPARE st_ddl;

UPDATE biz_nurse_schedule_rule
   SET deprecated = 1,
       remark = CONCAT('[sql/206 已并入 biz_staff_plan_rule，本表只读]', IFNULL(CONCAT(' ', remark), ''))
 WHERE del_flag = 0 AND deprecated = 0;

-- -----------------------------------------------------------------------------
-- 2. 补漏：旧表有而新表没有的标准行补进新表（实测 0 条）
--    id 号段 89675… 与 sql/204 的铺底规则同号段，按现有最大值递增避免撞车。
-- -----------------------------------------------------------------------------
SET @rn := (SELECT COALESCE(MAX(id), 896750000000100000) FROM biz_staff_plan_rule WHERE id < 896750000000300000);

INSERT INTO biz_staff_plan_rule (id, org_type, org_id, org_name, shift_id, staff_type,
                                 min_staff, max_staff, max_week_hours,
                                 max_consecutive_night_days, max_consecutive_work_days,
                                 status, create_time, update_time, del_flag, remark)
SELECT
  @rn := @rn + 1,
  2, o.ward_id, o.ward_name, o.shift_id, 2,
  o.min_staff, o.max_staff, o.max_week_hours,
  o.max_consecutive_night_days, o.max_consecutive_work_days,
  o.status, NOW(), NOW(), 0,
  CONCAT('sql/206 由旧护理标准表补齐（原 id=', o.id, '）')
FROM biz_nurse_schedule_rule o
WHERE o.del_flag = 0
  AND NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule n
                   WHERE n.del_flag = 0 AND n.org_type = 2 AND n.org_id = o.ward_id
                     AND n.staff_type = 2 AND n.shift_id = o.shift_id);

-- -----------------------------------------------------------------------------
-- 3. 定值对齐：两边若出现字段不一致，以**旧表**为准（护士长最后改的是旧表那一版，
--    承认它是最近一次人工意图；新表那份多半是初始化时拷过去的）。
--    实测 0 行，写在这里是为了让"将来再漂移"能被一次执行拉回来。
-- -----------------------------------------------------------------------------
UPDATE biz_staff_plan_rule n
  JOIN biz_nurse_schedule_rule o
    ON o.del_flag = 0 AND n.del_flag = 0 AND n.org_type = 2 AND n.org_id = o.ward_id
   AND n.staff_type = 2 AND n.shift_id = o.shift_id
   SET n.min_staff = o.min_staff,
       n.max_staff = o.max_staff,
       n.max_week_hours = o.max_week_hours,
       n.max_consecutive_night_days = o.max_consecutive_night_days,
       n.max_consecutive_work_days = o.max_consecutive_work_days,
       n.status = o.status,
       n.update_time = NOW()
 WHERE o.min_staff <> n.min_staff OR o.max_staff <> n.max_staff OR o.status <> n.status
    OR COALESCE(o.max_week_hours, -1) <> COALESCE(n.max_week_hours, -1)
    OR COALESCE(o.max_consecutive_night_days, -1) <> COALESCE(n.max_consecutive_night_days, -1)
    OR COALESCE(o.max_consecutive_work_days, -1) <> COALESCE(n.max_consecutive_work_days, -1);

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs sql/206-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 旧表未标记废弃的行数' AS item, COUNT(*) AS cnt, '0' AS expect FROM biz_nurse_schedule_rule o WHERE o.del_flag = 0 AND o.deprecated <> 1
UNION ALL SELECT 'V1b A 旧表已标记废弃的行数（留痕）', COUNT(*), '18' FROM biz_nurse_schedule_rule o WHERE o.del_flag = 0 AND o.deprecated = 1
UNION ALL SELECT 'V2 旧表每条都能在新表找到', COUNT(*), '0' FROM biz_nurse_schedule_rule o WHERE o.del_flag = 0 AND NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule n WHERE n.del_flag = 0 AND n.org_type = 2 AND n.org_id = o.ward_id AND n.staff_type = 2 AND n.shift_id = o.shift_id)
UNION ALL SELECT 'V3 两边字段值不一致的行数', COUNT(*), '0' FROM biz_nurse_schedule_rule o JOIN biz_staff_plan_rule n ON n.del_flag = 0 AND n.org_type = 2 AND n.org_id = o.ward_id AND n.staff_type = 2 AND n.shift_id = o.shift_id WHERE o.del_flag = 0 AND (o.min_staff <> n.min_staff OR o.max_staff <> n.max_staff OR o.status <> n.status OR COALESCE(o.max_week_hours, -1) <> COALESCE(n.max_week_hours, -1) OR COALESCE(o.max_consecutive_night_days, -1) <> COALESCE(n.max_consecutive_night_days, -1) OR COALESCE(o.max_consecutive_work_days, -1) <> COALESCE(n.max_consecutive_work_days, -1))
UNION ALL SELECT 'V4 A 新表病区护理标准数', COUNT(*), '>=18' FROM biz_staff_plan_rule WHERE del_flag = 0 AND org_type = 2 AND staff_type = 2
UNION ALL SELECT 'V5 A 护理排班涉及病区都有标准', COUNT(*), '0' FROM (SELECT DISTINCT ward_id FROM biz_nurse_schedule WHERE del_flag = 0) w WHERE NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule n WHERE n.del_flag = 0 AND n.org_type = 2 AND n.org_id = w.ward_id AND n.staff_type = 2)
UNION ALL SELECT 'V6 A deprecated 列已建', COUNT(*), '1' FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_nurse_schedule_rule' AND COLUMN_NAME = 'deprecated'
UNION ALL SELECT 'V7 A 新标准总数（留痕）', COUNT(*), '>=61' FROM biz_staff_plan_rule WHERE del_flag = 0;
