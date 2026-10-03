-- =============================================================================
-- 212 · 人力需求层与缺口视图（P2-G06，蓝图 §5.2）
--
-- 【为什么必须建这一层】
-- 现在排班只有「排了什么」，没有「需要什么」。于是：
--   · 门诊按出诊计划排医生，护理那边凭经验排人 —— 两条链各排各的，这就是"脱节"的根；
--   · 校验只能说「这个人连上 3 个夜班不行」，说不出「这个病区今天缺 2 个人」；
--   · 月底想复盘"为什么总加班"，没有任何"需求"这个分母可以除。
-- 需求层就是给排班一个**分母**：有了它，"在岗 5 人"才有意义（需求 6 人 → 缺 1）。
--
-- 【第一版口径（写在注释里，业务核定后改这里）】
--   1. 住院护理：Σ(在院患者 × 该护理等级的每患者日工时) ÷ 班次时长 8h → 向上取整
--      等级工时（小时/患者日）：特级 6.0 / 一级 3.5 / 二级 1.7 / 三级 0.6
--      说明：行业里通常叫 NHPPD（护理时数/患者日，一般 2.5~6.0）。这里**不额外再乘一个
--      科室级 NHPPD 系数**，而是把工时定额直接挂在护理等级上 —— 好处是等级一改需求立刻变，
--      不用同时维护两张表；代价是没有科室差异（ICU 和康复科用同一套定额），
--      这是第一版的妥协，等护理部核定后按科室/等级出一张系数表再替换。
--   2. 门诊护理：有出诊 → 分诊 1 人 + 跟诊 CEIL(出诊医生数 / 2)
--      治疗室按处置量那条**第一版不铺**：库里没有处置量数据，凭空给系数等于编需求。
--   3. 门诊医生：需求 = 当日出诊医生数（出诊计划本身就是医生需求的来源）。
--   4. 住院医生第一版不派生：住院医生排的是值班点位（sql/202/203 已铺 234 个点位），
--      需求口径与"在院患者数"无关，硬凑一个数只会误导。
--
-- 【两个刻意的克制】
--   · 只铺 period_code=0（全天）、shift_id=0（不限班次）的行。
--     一旦按班次拆需求，缺口视图就得按班次配对，而现在的护理排班是按天排的，
--     拆了反而是把口径搞复杂。period_code/shift_id 两列留在表里，等真的按午别排班再开。
--   · 住院需求按「当前在院快照」铺满 14 天，**没有做出入院预测**。
--     calc_basis 里写清楚，免得有人拿第 14 天的数当真。
--
-- 【手工调整不被覆盖】
-- 护士长可以改成 demand_source=3（手工调整）。重跑本脚本时 ON DUPLICATE KEY UPDATE
-- 会跳过 source=3 的行 —— 派生永远不能盖掉人拍板的数。
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 需求表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_staff_demand (
  id              bigint       NOT NULL COMMENT '主键（雪花）',
  demand_date     date         NOT NULL COMMENT '需求日期',
  org_type        tinyint      NOT NULL COMMENT '排班单元类型（1-科室 2-病区 3-全院）',
  org_id          bigint       NOT NULL DEFAULT 0 COMMENT '排班单元ID（全院级为0）',
  org_name        varchar(128) NULL COMMENT '排班单元名称（快照）',
  period_code     tinyint      NOT NULL DEFAULT 0 COMMENT '时段（0-全天 1-上午 2-下午 3-夜间）',
  shift_id        bigint       NOT NULL DEFAULT 0 COMMENT '班次ID（0-不限班次）',
  staff_type      tinyint      NOT NULL COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政）',
  required_count  int          NOT NULL DEFAULT 0 COMMENT '需求人数',
  required_level  tinyint      NULL COMMENT '能级下限（0-不限；依赖 G-01，未做前一律 NULL）',
  demand_source   tinyint      NOT NULL COMMENT '来源（1-门诊出诊派生 2-住院患者派生 3-手工调整）',
  source_biz_id   bigint       NULL COMMENT '来源业务ID（出诊计划ID/病区ID）',
  calc_basis      varchar(500) NULL COMMENT '测算依据（怎么算出来的，写给人看的）',
  status          tinyint      NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-生效）',
  create_by       varchar(64)  NULL,
  create_time     datetime     NULL,
  update_by       varchar(64)  NULL,
  update_time     datetime     NULL,
  del_flag        tinyint      NOT NULL DEFAULT 0,
  remark          varchar(500) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_demand (demand_date, org_type, org_id, period_code, shift_id, staff_type),
  KEY idx_demand_date (demand_date, org_type, staff_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人力需求（需求层：排班的驱动源与分母）';

-- -----------------------------------------------------------------------------
-- 2. 缺口视图：需求 − 在岗
--
--    在岗只数 duty_status=1（上班）：休息/请假/停诊的人不算在岗，这是缺口的定义。
--    shift_id=0 的需求行 = 不限班次，统计时不按班次过滤 —— 与该行的口径对齐。
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_staff_demand_gap AS
SELECT d.demand_date, d.org_type, d.org_id, d.org_name, d.period_code, d.shift_id,
       d.staff_type, d.demand_source, d.calc_basis,
       d.required_count,
       COUNT(s.id) AS scheduled_count,
       d.required_count - COUNT(s.id) AS gap_count
  FROM biz_staff_demand d
  LEFT JOIN biz_staff_schedule s
         ON s.schedule_date = d.demand_date AND s.org_type = d.org_type
        AND s.org_id = d.org_id AND s.staff_type = d.staff_type
        AND s.duty_status = 1 AND s.del_flag = 0
        AND (d.shift_id = 0 OR s.shift_id = d.shift_id)
 WHERE d.del_flag = 0 AND d.status = 1
 GROUP BY d.id;

-- -----------------------------------------------------------------------------
-- 3. 派生：住院护理需求（有在院患者**且病区主数据在册**的病区 × 未来 14 天）
--
--    需求 = MAX(患者工时派生, 该病区核定的护理下限, 1)
--
--    为什么必须取 MAX：病区护理不是纯按患者数配的。一个病区哪怕只剩 1 个患者，
--    白班/前夜/后夜三班也得有人顶，这是**最低运营配置**，跟患者数无关。
--    实测呼吸内科病区在院 4 人 → 按工时只算出 1 人，而实际在岗 6 人、标准下限 5 人 ——
--    只按患者算会把"今天缺 4 个人"显示成"富余 5 个"，那需求层就是在骗人。
--    核定的下限本来就在 biz_staff_plan_rule.min_staff 里（sql/204/206），直接拿来用，
--    需求层和标准表在这一刻才真的咬在一起。
--
--    ⚠ 主表是 sys_ward（**全部启用病区**），不是 biz_admission：
--    只铺"今天有患者的病区"会让其余 38 个病区在缺口面板上整片空白 —— 护士长切过去看到
--    一片「—」，第一反应是系统坏了。病区今天 0 患者时患者派生为 0，需求就等于核定下限
--    （三班还得有人顶），这才是对的。**每个启用病区每天都必须有一条需求**。
--
--    为什么以 sys_ward 为准而不是 biz_admission：在院患者里有 13 个挂在 ward_id 不在
--    sys_ward 的"病区"上（历史/测试脏数据，见 G-27），那些病区连归哪个科室、下限多少
--    都不知道，派生的需求没有归属 —— 不派生，问题单独立项给业务清数据。
-- -----------------------------------------------------------------------------
SET @rn := (SELECT COALESCE(MAX(id), 896800000000000000) FROM biz_staff_demand WHERE id < 896800000000900000);

INSERT INTO biz_staff_demand (id, demand_date, org_type, org_id, org_name, period_code, shift_id,
                              staff_type, required_count, required_level, demand_source, source_biz_id,
                              calc_basis, status, create_by, create_time, del_flag, remark)
SELECT
  @rn := @rn + 1,
  x.dt, 2, x.ward_id, x.ward_name, 0, 0,
  2,
  GREATEST(x.derived, x.floor_cnt, 1),
  NULL, 2, x.ward_id,
  CONCAT('在院 ', x.patients, ' 人 × 等级工时（特级6.0/一级3.5/二级1.7/三级0.6 h）= ', x.hours,
         'h ÷ 8h = ', x.derived, ' 人；病区核定护理下限 ', x.floor_cnt,
         ' 人（三班覆盖的最低运营配置，与患者数无关）→ 取 MAX = ',
         GREATEST(x.derived, x.floor_cnt, 1), ' 人'),
  1, 'sql/212', NOW(), 0, 'sql/212 住院派生：按当前在院快照铺 14 天，未做出入院预测'
FROM (
  WITH RECURSIVE cal(dt) AS (
    SELECT CURDATE()
    UNION ALL
    SELECT dt + INTERVAL 1 DAY FROM cal WHERE dt < CURDATE() + INTERVAL 13 DAY
  )
  SELECT c.dt                       AS dt,
         w.ward_id                  AS ward_id,
         MAX(w.ward_name)           AS ward_name,
         -- 用 admission_id 计数：0 患者的病区这里必须是 0，用 COUNT(*) 会算成 1
         COUNT(a.admission_id)      AS patients,
         IFNULL(ROUND(SUM(CASE a.nursing_level WHEN 1 THEN 6.0 WHEN 2 THEN 3.5 WHEN 3 THEN 1.7 WHEN 4 THEN 0.6 ELSE 1.7 END), 1), 0) AS hours,
         -- SUM 在 0 患者时为 NULL，而 GREATEST 遇 NULL 直接返回 NULL → 必须兜住
         IFNULL(CEIL(SUM(CASE a.nursing_level WHEN 1 THEN 6.0 WHEN 2 THEN 3.5 WHEN 3 THEN 1.7 WHEN 4 THEN 0.6 ELSE 1.7 END) / 8), 0) AS derived,
         IFNULL(MAX(r.min_staff), 0) AS floor_cnt
    FROM cal c
    JOIN sys_ward w ON w.status = 1
    LEFT JOIN biz_admission a
           ON a.del_flag = 0 AND a.admit_status = 1 AND a.ward_id = w.ward_id
    LEFT JOIN biz_staff_plan_rule r
           ON r.del_flag = 0 AND r.status = 1 AND r.org_type = 2
          AND r.org_id = w.ward_id AND r.staff_type = 2 AND r.shift_id = 0
   GROUP BY c.dt, w.ward_id
) x
ON DUPLICATE KEY UPDATE
  required_count = IF(biz_staff_demand.demand_source = 3, biz_staff_demand.required_count, VALUES(required_count)),
  calc_basis     = IF(biz_staff_demand.demand_source = 3, biz_staff_demand.calc_basis, VALUES(calc_basis)),
  org_name       = VALUES(org_name),
  update_by      = 'sql/212', update_time = NOW();

-- -----------------------------------------------------------------------------
-- 4. 派生：门诊护理需求（有出诊的科室 × 出诊日）
--    分诊 1 人/科室 + 跟诊 1 人/2 名出诊医生，再与科室核定的护理下限取 MAX（同住院侧口径）。
--    room_id 全库基本为空，不按诊室算。
--
--    只派生**有护理编制**的科室：一个护士都没有的科室谈不上护理需求。
--
--    ⚠ 关于「有病区的科室要不要算门诊护理需求」——算。急诊内科这类科室既有病区又有门诊，
--    分诊台/治疗室的护理人力是真实存在的，不因为它挂着病区就消失。
--    但护理排班单元下拉（sql/209）目前把「有病区的科室」排除在门诊单元之外，
--    于是这类需求**算得出来、暂时排不进去**。这不是需求层该藏起来的事：
--    需求层的本职就是暴露「需要但没人」的缺口，藏起来只会让缺口永远没人看见。
--    已登记 G-29，等 P4 做护理单元主数据（方案 B：病区与门诊各自独立成单元）时一并收口。
-- -----------------------------------------------------------------------------
SET @rn2 := (SELECT COALESCE(MAX(id), 896800000000100000) FROM biz_staff_demand WHERE id < 896800000000900000);

INSERT INTO biz_staff_demand (id, demand_date, org_type, org_id, org_name, period_code, shift_id,
                              staff_type, required_count, required_level, demand_source, source_biz_id,
                              calc_basis, status, create_by, create_time, del_flag, remark)
SELECT
  @rn2 := @rn2 + 1,
  x.dt, 1, x.dept_id, x.dept_name, 0, 0,
  2,
  GREATEST(x.derived, x.floor_cnt, 1),
  NULL, 1, x.dept_id,
  CONCAT('出诊医生 ', x.doctors, ' 人：分诊 1 + 跟诊 CEIL(', x.doctors, '/2) = ', x.derived,
         ' 人；科室核定护理下限 ', x.floor_cnt, ' 人 → 取 MAX = ',
         GREATEST(x.derived, x.floor_cnt, 1), ' 人'),
  1, 'sql/212', NOW(), 0, 'sql/212 门诊派生：分诊 1 人/科室 + 跟诊 1 人/2 医生；治疗室按处置量的部分待处置量数据齐了再补'
FROM (
  SELECT s.schedule_date                       AS dt,
         s.dept_id                             AS dept_id,
         MAX(s.dept_name)                      AS dept_name,
         COUNT(DISTINCT s.doctor_id)           AS doctors,
         1 + CEIL(COUNT(DISTINCT s.doctor_id) / 2) AS derived,
         IFNULL(MAX(r.min_staff), 0)           AS floor_cnt
    FROM biz_schedule s
    LEFT JOIN biz_staff_plan_rule r
           ON r.del_flag = 0 AND r.status = 1 AND r.org_type = 1
          AND r.org_id = s.dept_id AND r.staff_type = 2 AND r.shift_id = 0
   WHERE s.del_flag = 0 AND s.status = 1
     AND s.schedule_date BETWEEN CURDATE() AND CURDATE() + INTERVAL 13 DAY
     -- 只派生有护理编制的科室；有病区的不排除（见本段上方关于 G-29 的说明）
     AND EXISTS (SELECT 1 FROM sys_employee e
                   JOIN sys_employee_post p ON p.employee_id = e.id
                   JOIN sys_role r2 ON r2.id = p.role_id
                  WHERE e.del_flag = 0 AND e.status = 1 AND e.dept_id = s.dept_id
                    AND r2.del_flag = 0 AND r2.status = 1 AND r2.staff_type = 2)
   GROUP BY s.schedule_date, s.dept_id
) x
ON DUPLICATE KEY UPDATE
  required_count = IF(biz_staff_demand.demand_source = 3, biz_staff_demand.required_count, VALUES(required_count)),
  calc_basis     = IF(biz_staff_demand.demand_source = 3, biz_staff_demand.calc_basis, VALUES(calc_basis)),
  org_name       = VALUES(org_name),
  update_by      = 'sql/212', update_time = NOW();

-- -----------------------------------------------------------------------------
-- 5. 派生：门诊医生需求（= 当日出诊医生数）
--    医生的需求本来就来自出诊计划，铺这一行是为了让「门诊 × 医生」也有分母可比对。
-- -----------------------------------------------------------------------------
SET @rn3 := (SELECT COALESCE(MAX(id), 896800000000200000) FROM biz_staff_demand WHERE id < 896800000000900000);

INSERT INTO biz_staff_demand (id, demand_date, org_type, org_id, org_name, period_code, shift_id,
                              staff_type, required_count, required_level, demand_source, source_biz_id,
                              calc_basis, status, create_by, create_time, del_flag, remark)
SELECT
  @rn3 := @rn3 + 1,
  s.schedule_date, 1, s.dept_id, MAX(s.dept_name), 0, 0,
  1,
  COUNT(DISTINCT s.doctor_id),
  NULL, 1, s.dept_id,
  CONCAT('出诊计划 ', COUNT(*), ' 条，医生 ', COUNT(DISTINCT s.doctor_id), ' 人：需求 = 出诊医生数'),
  1, 'sql/212', NOW(), 0, 'sql/212 门诊派生：医生需求直接等于出诊医生数（出诊计划即需求来源）'
FROM biz_schedule s
WHERE s.del_flag = 0 AND s.status = 1
  AND s.schedule_date BETWEEN CURDATE() AND CURDATE() + INTERVAL 13 DAY
GROUP BY s.schedule_date, s.dept_id
ON DUPLICATE KEY UPDATE
  required_count = IF(biz_staff_demand.demand_source = 3, biz_staff_demand.required_count, VALUES(required_count)),
  calc_basis     = IF(biz_staff_demand.demand_source = 3, biz_staff_demand.calc_basis, VALUES(calc_basis)),
  org_name       = VALUES(org_name),
  update_by      = 'sql/212', update_time = NOW();

-- -----------------------------------------------------------------------------
-- 6. 收口：派生行不留「过期」和「无归属」两种垃圾
--    · 过期：派生窗口是「今天起 14 天」，昨天那行是拿今天的快照算出来的，留着是假历史；
--    · 无归属：ward_id 不在 sys_ward 的病区（G-27 的脏数据），INNER JOIN 已经不派生了，
--      但改口径之前跑出来的旧行还在，一并清掉。手工调整行（source=3）一条都不动。
-- -----------------------------------------------------------------------------
DELETE FROM biz_staff_demand
 WHERE del_flag = 0 AND demand_source IN (1, 2) AND demand_date < CURDATE();

DELETE FROM biz_staff_demand
 WHERE del_flag = 0 AND demand_source = 2 AND org_type = 2
   AND NOT EXISTS (SELECT 1 FROM sys_ward w WHERE w.ward_id = biz_staff_demand.org_id AND w.status = 1);

-- -----------------------------------------------------------------------------
-- 自检（verify-sql.mjs 取最后一个 marker 之后的 SELECT）
--   用法：node workspace/verify-sql.mjs sql/212-*.sql "-- VERIFY"
-- -----------------------------------------------------------------------------
-- VERIFY
SELECT 'V1 biz_staff_demand 表已建' AS item, COUNT(*) AS cnt, '1' AS expect FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_staff_demand'
UNION ALL SELECT 'V2 v_staff_demand_gap 视图已建', COUNT(*), '1' FROM information_schema.VIEWS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'v_staff_demand_gap'
UNION ALL SELECT 'V3 需求总行数', COUNT(*), '>0' FROM biz_staff_demand WHERE del_flag = 0
UNION ALL SELECT 'V4 住院护理派生行数（49 个启用病区 × 14 天）', COUNT(*), '686' FROM biz_staff_demand WHERE del_flag = 0 AND demand_source = 2 AND staff_type = 2
UNION ALL SELECT 'V4b 今天没有需求行的启用病区数（0 患者的病区也不能空白）', (SELECT COUNT(*) FROM sys_ward w WHERE w.status = 1 AND NOT EXISTS (SELECT 1 FROM biz_staff_demand d WHERE d.del_flag = 0 AND d.demand_source = 2 AND d.staff_type = 2 AND d.org_id = w.ward_id AND d.demand_date = CURDATE())), '0'
UNION ALL SELECT 'V5 门诊护理派生行数', COUNT(*), '>0' FROM biz_staff_demand WHERE del_flag = 0 AND demand_source = 1 AND staff_type = 2
UNION ALL SELECT 'V6 门诊医生派生行数', COUNT(*), '>0' FROM biz_staff_demand WHERE del_flag = 0 AND demand_source = 1 AND staff_type = 1
UNION ALL SELECT 'V7 需求人数小于 1 的行（不允许）', COUNT(*), '0' FROM biz_staff_demand WHERE del_flag = 0 AND required_count < 1
UNION ALL SELECT 'V8 唯一键撞车（同键多行）', COUNT(*), '0' FROM (SELECT demand_date, org_type, org_id, period_code, shift_id, staff_type FROM biz_staff_demand WHERE del_flag = 0 GROUP BY demand_date, org_type, org_id, period_code, shift_id, staff_type HAVING COUNT(*) > 1) x
UNION ALL SELECT 'V9 视图有数据', COUNT(*), '>0' FROM v_staff_demand_gap
UNION ALL SELECT 'V10 缺口 = 需求 − 在岗 恒等式成立', COUNT(*), '0' FROM v_staff_demand_gap WHERE gap_count <> required_count - scheduled_count
UNION ALL SELECT 'V11 有缺口的行（需求 > 在岗）', COUNT(*), '*' FROM v_staff_demand_gap WHERE gap_count > 0
UNION ALL SELECT 'V12 住院侧今天的需求合计（人）', COALESCE(SUM(required_count), 0), '>=19' FROM biz_staff_demand WHERE del_flag = 0 AND demand_source = 2 AND demand_date = CURDATE()
-- 注意：verify-sql.mjs 会把最后一行末尾的 ");" 削掉，所以末句不要以右括号收尾
UNION ALL SELECT 'V13 每条需求都写清了测算依据', COUNT(*), '0' FROM biz_staff_demand WHERE del_flag = 0 AND IFNULL(calc_basis, '') = ''
UNION ALL SELECT 'V14 被挡在派生外的在院患者（ward_id 不在 sys_ward，G-27 脏数据）', COUNT(*), '*' FROM biz_admission a LEFT JOIN sys_ward w ON w.ward_id = a.ward_id AND w.status = 1 WHERE a.del_flag = 0 AND a.admit_status = 1 AND w.ward_id IS NULL;
