-- =============================================================================
-- 200 - 排班核心表（全院岗位出勤事实 + 值守点位 + 人力标准 + 变更留痕）
--
-- 【要解决的问题】
-- 排班事实散在三张表，各自回答不同的问题，彼此之间没有任何互斥校验：
--  1) biz_schedule（sql/195 已扩成全院岗位）：号源/诊室/费用是它独有的事实，
--     但 status 是**号源态**（停诊/正常/已满/过期），表达不了「这个人今天休息/请假」，
--     于是「今日在岗」只能靠 status=1 猜；
--  2) biz_nurse_schedule：status 是**出勤态**（上班/休息/请假/培训/停班），一人一天一条，
--     工时与人力标准校验是门诊侧没有的下游；
--  3) biz_duty_roster：唯一键 (duty_date, shift_type, role_type) **不含人**，
--     语义是「一个责任位一天一位」，与「一个人的班」是两个东西。
-- 后果：同一个护士可以在 1)、2) 两张表里被排两次、时间重叠都不报错（跨表互斥从来没实现过）。
--
-- 【本文件做什么】
--  1. 新建 biz_staff_schedule —— 「谁 · 哪天 · 在哪个单元 · 什么班 · 出不出勤」的唯一事实表。
--     三张老表的「人与时间」都收敛到它；号源、工时、责任位留在各自的扩展表。
--  2. biz_schedule 加 staff_schedule_id 绑到核心表，成为「出诊计划与号源宿主」的扩展表。
--     此后只有 clinic_flag=1 的班才有 biz_schedule 行。
--  3. biz_shift 加 apply_staff_type（谁能选这条班次），use_scope 扩 4-全院通用并铺 3 条通用班次。
--     原 use_scope 按「哪张表」分册，新增岗位类别就要改码值；按岗位分才能给医技/窗口/行政用。
--  4. 新建 biz_duty_post（值守点位定义），biz_duty_roster 加 shift_id + post_id + staff_schedule_id，
--     把「位」与「人」拆开：位先存在，人由核心表进位；时刻改由班次带出，不再由 Java 常量硬算。
--     旧列（shift_type/role_type/start_time/end_time/employee_*/dept_*）全部保留 ——
--     值班日志、急诊升级、currentAt 都在读它们。
--  5. biz_nurse_schedule_rule 泛化为 biz_staff_plan_rule（单元 × 班次 × 岗位 × 人数上下限）。
--  6. 新建 biz_schedule_change_log：换班/代班/停班/加减号统一留痕，
--     取代 biz_duty_roster.substitute_* 与 biz_schedule.remark 拼字符串两种土办法。
--  7. 存量迁移：三张老表的行灌进核心表并回填绑定列。
--
-- 【刻意不做】
--  - 不删 biz_nurse_schedule / biz_duty_roster 的任何列，也不搬它们的行数：
--    护理的周工时统计与病区矩阵、值守的交接日志都在读旧列，删除是独立的一步。
--  - 不动 biz_schedule_slot（号源时段）：段只服务出诊班，主表绑上核心表后它自然跟着走。
--  - 不铺「非医生岗位的排班数据」：要先有人事岗位与单元的真实归属，属下一轮的事。
--
-- 【删除口径】biz_staff_schedule / biz_staff_plan_rule / biz_duty_post /
--   biz_schedule_change_log 的唯一键都不含 del_flag —— 删除一律物理删。
-- =============================================================================

SET NAMES utf8mb4;
SET @db := DATABASE();

-- -----------------------------------------------------------------------------
-- 1. 核心事实表：员工排班
--    shift_id=0 表达「无班次」（休息/请假没有班次，但不能没有这一行 —— 排了休息才是事实），
--    与本仓「0 = 合计/不限」的哨兵口径一致。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_staff_schedule (
  id                 BIGINT        NOT NULL COMMENT '主键ID（雪花）',
  schedule_date      DATE          NOT NULL COMMENT '排班日期',
  week_day           TINYINT       NOT NULL COMMENT '星期（1-周一 7-周日）',
  org_type           TINYINT       NOT NULL DEFAULT 1 COMMENT '排班单元类型（1-科室 2-病区 3-全院）',
  org_id             BIGINT        NOT NULL DEFAULT 0 COMMENT '排班单元ID（全院级为0）',
  org_name           VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '排班单元名称（快照）',
  dept_id            BIGINT        NOT NULL DEFAULT 0 COMMENT '科室ID（全院级为0）',
  dept_name          VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '科室名称（快照）',
  employee_id        BIGINT        NOT NULL COMMENT '员工ID',
  emp_code           VARCHAR(32)   DEFAULT NULL COMMENT '工号（快照）',
  employee_name      VARCHAR(50)   NOT NULL DEFAULT '' COMMENT '姓名（快照）',
  employee_post_id   BIGINT        DEFAULT NULL COMMENT '员工岗位ID（人 × 科室 × 角色）',
  staff_type         TINYINT       NOT NULL DEFAULT 1 COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  shift_id           BIGINT        NOT NULL DEFAULT 0 COMMENT '标准班次ID（0-无班次）',
  start_time         VARCHAR(5)    DEFAULT NULL COMMENT '开始时间（HH:mm，班次快照）',
  end_time           VARCHAR(5)    DEFAULT NULL COMMENT '结束时间（HH:mm，班次快照，早于开始时间属次日）',
  duty_status        TINYINT       NOT NULL DEFAULT 1 COMMENT '出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）',
  attend_mode        TINYINT       NOT NULL DEFAULT 1 COMMENT '响应形态（1-坐班 2-听班 3-留院值班）',
  clinic_flag        TINYINT       NOT NULL DEFAULT 0 COMMENT '是否出诊（0-否 1-是）',
  work_minutes       INT           NOT NULL DEFAULT 0 COMMENT '工时（分钟）',
  schedule_source    TINYINT       NOT NULL DEFAULT 1 COMMENT '生成来源（1-手工 2-模板 3-复制周期 4-换班）',
  template_id        BIGINT        DEFAULT NULL COMMENT '来源排班周模板ID',
  create_by          VARCHAR(64)   DEFAULT NULL COMMENT '创建人',
  create_time        DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by          VARCHAR(64)   DEFAULT NULL COMMENT '更新人',
  update_time        DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag           TINYINT       NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  remark             VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_emp_date_shift (employee_id, schedule_date, shift_id),
  KEY idx_org_date (org_type, org_id, schedule_date, duty_status),
  KEY idx_dept_date_staff (dept_id, schedule_date, staff_type, duty_status),
  KEY idx_emp_date (employee_id, schedule_date),
  KEY idx_date_status (schedule_date, duty_status),
  KEY idx_shift_id (shift_id),
  KEY idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工排班';
-- ⚠ uk_emp_date_shift 不含 del_flag：本表删除走物理删（软删留下的行继续占键，
--   「改了再排同一天同一班」必然 Duplicate entry）。

-- -----------------------------------------------------------------------------
-- 2. 人力配置标准（泛化自病区护理的 biz_nurse_schedule_rule）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_staff_plan_rule (
  id                            BIGINT       NOT NULL COMMENT '主键ID（雪花）',
  org_type                      TINYINT      NOT NULL DEFAULT 1 COMMENT '排班单元类型（1-科室 2-病区 3-全院）',
  org_id                        BIGINT       NOT NULL DEFAULT 0 COMMENT '排班单元ID（全院级为0）',
  org_name                      VARCHAR(128) DEFAULT NULL COMMENT '排班单元名称（快照）',
  shift_id                      BIGINT       NOT NULL DEFAULT 0 COMMENT '标准班次ID（0-该单元全部班次）',
  staff_type                    TINYINT      NOT NULL COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  min_staff                     TINYINT      NOT NULL DEFAULT 0 COMMENT '最低在岗人数',
  max_staff                     TINYINT      NOT NULL DEFAULT 0 COMMENT '最高在岗人数',
  max_week_hours                DECIMAL(5,1) DEFAULT NULL COMMENT '单周工时上限',
  max_consecutive_night_days    TINYINT      DEFAULT NULL COMMENT '连续夜班天数上限',
  max_consecutive_work_days     TINYINT      DEFAULT NULL COMMENT '连续上班天数上限',
  status                        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  create_by                     VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
  create_time                   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by                     VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
  update_time                   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag                      TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  remark                        VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_plan_rule (org_type, org_id, shift_id, staff_type),
  KEY idx_plan_org (org_id, staff_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人力配置标准';
-- ⚠ uk_plan_rule 不含 del_flag：配置页「整体替换」必须物理删。

-- -----------------------------------------------------------------------------
-- 3. 值守点位定义（把「位」从「人」里剥出来）
--    一个点位 = 一个班次 × 一个班内角色，例如「总值班白班主班」；一天一位一人。
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_duty_post (
  id                  BIGINT       NOT NULL COMMENT '主键ID（雪花）',
  post_code           VARCHAR(32)  NOT NULL COMMENT '点位编码',
  post_name           VARCHAR(64)  NOT NULL COMMENT '点位名称',
  duty_scope          TINYINT      NOT NULL DEFAULT 1 COMMENT '责任范围（1-全院行政 2-急诊 3-感染 4-总务 5-信息）',
  org_type            TINYINT      NOT NULL DEFAULT 3 COMMENT '排班单元类型（1-科室 2-病区 3-全院）',
  org_id              BIGINT       NOT NULL DEFAULT 0 COMMENT '排班单元ID（全院级为0）',
  role_type           TINYINT      NOT NULL DEFAULT 1 COMMENT '班内角色（1-主班 2-副班）',
  shift_id            BIGINT       NOT NULL COMMENT '标准班次ID',
  required_staff_type TINYINT      DEFAULT NULL COMMENT '应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-不限）',
  phone               VARCHAR(32)  DEFAULT NULL COMMENT '点位值班电话',
  sort_no             INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  status              TINYINT      NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  create_by           VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
  create_time         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by           VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
  update_time         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag            TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  remark              VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_duty_post_code (post_code),
  KEY idx_duty_post_org (org_type, org_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='值班点位';
-- ⚠ uk_duty_post_code 不含 del_flag：删除走物理删。

-- -----------------------------------------------------------------------------
-- 4. 排班变更留痕
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS biz_schedule_change_log (
  id                BIGINT       NOT NULL COMMENT '主键ID（雪花）',
  staff_schedule_id BIGINT       NOT NULL COMMENT '员工排班ID',
  action_type       TINYINT      NOT NULL COMMENT '变更类型（1-换班 2-代班 3-停班 4-加号 5-减号 6-出诊变更）',
  from_employee_id  BIGINT       DEFAULT NULL COMMENT '原值班人',
  to_employee_id    BIGINT       DEFAULT NULL COMMENT '实际值班人',
  from_shift_id     BIGINT       DEFAULT NULL COMMENT '原班次ID',
  to_shift_id       BIGINT       DEFAULT NULL COMMENT '新班次ID',
  amount            INT          DEFAULT NULL COMMENT '变更数量',
  reason            VARCHAR(200) DEFAULT NULL COMMENT '变更原因',
  occur_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  create_by         VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
  create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_by         VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
  update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  del_flag          TINYINT      NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  remark            VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_change_schedule (staff_schedule_id, occur_time),
  KEY idx_change_emp (to_employee_id, occur_time),
  KEY idx_change_date (action_type, occur_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='排班变更记录';

-- -----------------------------------------------------------------------------
-- 5. biz_schedule 绑定核心表（幂等 ALTER）
-- -----------------------------------------------------------------------------
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_schedule' AND COLUMN_NAME = 'staff_schedule_id');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE biz_schedule ADD COLUMN staff_schedule_id bigint DEFAULT NULL COMMENT ''员工排班ID'' AFTER id',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @has_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_schedule' AND INDEX_NAME = 'idx_schedule_staff_schedule');
-- 普通索引不是唯一键：一条出勤事实可以挂多条出诊计划（同一人同一窗口的历史多科室排班，
-- 唯一键 uk_schedule_window 带着 dept_id，本来就允许），绑成唯一键会让这些行改一次排班就撞重复键。
SET @sql := IF(@has_idx = 0,
  'ALTER TABLE biz_schedule ADD KEY idx_schedule_staff_schedule (staff_schedule_id)',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- -----------------------------------------------------------------------------
-- 5b. 班次归位：历史行的 shift_id 要么空、要么指向不存在的班次，一律按「时间来认」
--     实测（2026-10-02，hn_biz_his）：275 条有效出诊行里——
--       · 96 条 shift_id 为 NULL；
--       · 126 条 shift_id 指向不存在的班次（多写一个 0：8900000000000000007 vs 真实
--         890000000000000007，急诊白/前夜/后夜三批各 42 条，是历史铺数脚本写坏的）；
--       · 起止时间还存在 HH:mm 与 HH:mm:ss 两种格式（57 条带 :00 尾巴），
--         按整串等值去认班次一条都认不到，直接灌进核心表的 VARCHAR(5) 还会 Data too long。
--     四级识别，从上往下取第一个命中，保证每条行最终都有班次：
--       0) 已有且有效的 shift_id —— 原样保留，不覆盖任何还认得出的历史选择；
--       1) 同册精确匹配：同 use_scope 且起止时刻（取 HH:mm）完全相同；
--       2) 跨册精确匹配：时刻相同即可（例如 08:00-17:30 只有护理主班才有）；
--       3) 完全覆盖：出诊窗口落在某个班次的时段内，取同册里时段最长的一个
--          （08:30-12:00 → 上午门诊，13:30-17:00 → 全天门诊，15:00-16:00 → 全天门诊）；
--       4) 重叠最多：没有任何班次装得下时（例如 00:00-23:59 这种历史全天夹具），
--          取与出诊时段重叠分钟数最多的班次，并在 remark 留痕。
--     第 4 级是近似：核心表从此只有一种班次口径，宁可把一次怪窗口归到最接近的班，
--     也不留 shift_id=0 的「上班但不知道上什么班」这种含混状态
--     （核心表的 shift_id=0 只留给休息/请假——他们本来就没有班次）。
--     覆盖/重叠两级都排除跨零点班次：分两种时区 zhài 算会算出负区间。
-- -----------------------------------------------------------------------------
UPDATE biz_schedule s
   SET s.shift_id = COALESCE(
        -- 0) 原本就是有效的班次
        (SELECT id FROM biz_shift WHERE id = s.shift_id),
        -- 1) 同册精确
        (SELECT sh.id FROM biz_shift sh
          WHERE sh.del_flag = 0 AND sh.use_scope = 1
            AND LEFT(sh.start_time,5) = LEFT(s.start_time,5)
            AND LEFT(sh.end_time,5)   = LEFT(s.end_time,5)
          ORDER BY sh.id LIMIT 1),
        -- 2) 跨册精确
        (SELECT sh.id FROM biz_shift sh
          WHERE sh.del_flag = 0
            AND LEFT(sh.start_time,5) = LEFT(s.start_time,5)
            AND LEFT(sh.end_time,5)   = LEFT(s.end_time,5)
          ORDER BY sh.use_scope, sh.id LIMIT 1),
        -- 3) 完全覆盖：班次时段把出诊窗口整个包住，取同册时段最长者
        (SELECT sh.id FROM biz_shift sh
          WHERE sh.del_flag = 0 AND sh.status = 1
            AND LEFT(sh.start_time,5) < LEFT(sh.end_time,5)          -- 排除跨零点
            AND LEFT(sh.start_time,5) <= LEFT(s.start_time,5)
            AND LEFT(sh.end_time,5)   >= LEFT(s.end_time,5)
            AND LEFT(s.start_time,5)  <  LEFT(s.end_time,5)
          ORDER BY sh.use_scope, sh.duration_minutes DESC, sh.id LIMIT 1),
        -- 4) 兜底：重叠分钟数最多的班次，必留痕
        (SELECT sh.id FROM biz_shift sh
          WHERE sh.del_flag = 0 AND sh.status = 1
            AND LEFT(sh.start_time,5) < LEFT(sh.end_time,5)
          ORDER BY sh.use_scope,
                   GREATEST(0, TIMESTAMPDIFF(MINUTE,
                     GREATEST(TIME(CONCAT(LEFT(sh.start_time,5),':00')),
                              TIME(CONCAT(LEFT(s.start_time,5),':00'))),
                     LEAST(TIME(CONCAT(LEFT(sh.end_time,5),':00')),
                           TIME(CONCAT(LEFT(s.end_time,5),':00'))))) DESC,
                   sh.id LIMIT 1))
 WHERE s.del_flag = 0 AND s.schedule_date IS NOT NULL;

-- 第 4 级近似叠认的那些行，把原窗口写进 remark，将来清理或重录时能一眼认出来
UPDATE biz_schedule s
  JOIN biz_shift sh ON sh.id = s.shift_id
   SET s.remark = CONCAT('[SQL200-近似班次] 原窗口 ', s.start_time, '-', s.end_time,
                         ' 无标准班次覆盖，按重叠最多归入「', sh.shift_name, '」。', IFNULL(s.remark, ''))
 WHERE s.del_flag = 0 AND s.schedule_date IS NOT NULL
   AND (LEFT(sh.start_time,5) <> LEFT(s.start_time,5) OR LEFT(sh.end_time,5) <> LEFT(s.end_time,5))
   AND s.remark NOT LIKE '[SQL200-%';

-- 星期统一成 1-周一 7-周日：本列原先按「周日起算」注释、按 Java 的 %7 写入（周日落成 0），
-- 注释与数据两头都不对；核心表按 1-周一 起算，这一列此后由核心表带出，先把存量与注释改成同一口径。
ALTER TABLE biz_schedule MODIFY COLUMN week_day tinyint NOT NULL COMMENT '星期（1-周一 2-周二 3-周三 4-周四 5-周五 6-周六 7-周日）';

UPDATE biz_schedule s
   SET s.week_day = CASE WHEN DAYOFWEEK(s.schedule_date) = 1 THEN 7 ELSE DAYOFWEEK(s.schedule_date) - 1 END
 WHERE s.del_flag = 0 AND s.schedule_date IS NOT NULL
   AND s.week_day <> CASE WHEN DAYOFWEEK(s.schedule_date) = 1 THEN 7 ELSE DAYOFWEEK(s.schedule_date) - 1 END;

-- 岗位类别列注释漏了 1-医生（默认值就是 1），按默认值排班的行反而在注释里查不到自己
ALTER TABLE biz_schedule MODIFY COLUMN staff_type tinyint NOT NULL DEFAULT 1
    COMMENT '排班对象岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）';

-- -----------------------------------------------------------------------------
-- 6. biz_shift 补齐 Java 实体在用的列；统一排序规则；加岗位适用列；
--    use_scope 扩 4-全院通用并铺 3 条通用班次
-- -----------------------------------------------------------------------------
-- 6a. cross_day：Java 实体 BizShift / ShiftVO / ShiftUpsertDTO 三处都在读写它，
--     但库里这张表从来没加上过（本仓 sql 目录在 09-30 那次 git reset --hard 时被连带清掉，
--     目录从未进过版本库），本文件下面第 6d/7/12 段都要往这一列写值，缺列会直接 Unknown column。
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_shift' AND COLUMN_NAME = 'cross_day');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE biz_shift ADD COLUMN cross_day tinyint NOT NULL DEFAULT 0 COMMENT ''是否跨零点（0-不跨 1-次日收）'' AFTER end_time',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 6b. 排序规则：本库 290 张表用 utf8mb4_0900_ai_ci，唯独 biz_shift 是 utf8mb4_general_ci。
--     两张表的字符列一做 = 比较就抛 Illegal mix of collations —— 第 5b 段按时间认班次、
--     后续任何 biz_shift JOIN biz_schedule / biz_nurse_schedule 都会炸，先统一到主流。
SET @need_conv := (SELECT COUNT(*) FROM information_schema.TABLES
                    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_shift'
                      AND TABLE_COLLATION <> 'utf8mb4_0900_ai_ci');
SET @sql := IF(@need_conv = 1,
  'ALTER TABLE biz_shift CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 6c. 岗位适用列
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_shift' AND COLUMN_NAME = 'apply_staff_type');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE biz_shift ADD COLUMN apply_staff_type tinyint DEFAULT NULL COMMENT ''适用岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-全部岗位通用）'' AFTER use_scope',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 已有三册的岗位归属显式写死：门诊册只给医生，护理册只给护理，值守册不限岗位
UPDATE biz_shift SET apply_staff_type = 1 WHERE use_scope = 1 AND del_flag = 0 AND apply_staff_type IS NULL;
UPDATE biz_shift SET apply_staff_type = 2 WHERE use_scope = 2 AND del_flag = 0 AND apply_staff_type IS NULL;

-- 值守两条班次是本文件后面（第 3 段点位、第 7 段回填、第 12 段迁移）唯一引用的班次，
-- 这里补一条幂等铺底：库里已有同 id 或同名的班次就不动，避免第 7/12 段回填不到 shift_id。
INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, duration_minutes, dept_id,
                       schedule_type, use_scope, apply_staff_type, status, create_by, remark)
SELECT x.id, x.shift_name, x.start_time, x.end_time, x.cross_day, x.duration_minutes, NULL,
       NULL, 3, NULL, 1, 'sql200', x.remark
FROM (
            SELECT 890000000000000021 id, '总值班白班' shift_name, '08:00' start_time, '18:00' end_time,
                   0 cross_day, 600 duration_minutes, '全院总值班白班（值守册）' remark
  UNION ALL SELECT 890000000000000022, '总值班夜班', '18:00', '08:00',
                   1, 840, '全院总值班夜班（跨零点，归值班开始日）'
) x
WHERE NOT EXISTS (SELECT 1 FROM biz_shift s WHERE s.id = x.id OR (s.shift_name = x.shift_name AND s.del_flag = 0));

-- 值守夜班时长纠正：18:00~次日08:00 是 14 小时，铺底时按 12 小时写死了。
-- 工时统计与在岗人数按 duration_minutes 聚合，这一列错着，周工时就会系统性少报每人每天 2 小时。
UPDATE biz_shift SET duration_minutes = 840 WHERE id = 890000000000000022 AND duration_minutes <> 840;

INSERT INTO biz_shift (id, shift_name, start_time, end_time, cross_day, duration_minutes, dept_id,
                       schedule_type, use_scope, apply_staff_type, status, create_by, remark)
SELECT x.id, x.shift_name, x.start_time, x.end_time, x.cross_day, x.duration_minutes, NULL,
       NULL, 4, NULL, 1, 'sql200', x.remark
FROM (
            SELECT 896730000000000101 id, '全院白班' shift_name, '08:00' start_time, '17:30' end_time,
                   0 cross_day, 570 duration_minutes, '全院通用班次：行政/窗口/医技的常白班' remark
  UNION ALL SELECT 896730000000000102, '全院中班', '13:00', '21:00',
                   0, 480, '全院通用班次：午后至夜间的补位班'
  UNION ALL SELECT 896730000000000103, '全院夜班', '21:00', '08:00',
                   1, 660, '全院通用班次：跨零点，归开始日'
) x
WHERE NOT EXISTS (SELECT 1 FROM biz_shift s WHERE s.id = x.id OR (s.shift_name = x.shift_name AND s.del_flag = 0));

-- -----------------------------------------------------------------------------
-- 7. biz_duty_roster 加班次/点位与核心表绑定（幂等 ALTER）
--    shift_id 补在这一段：这张表原先只有 shift_type（1-白 2-夜）加两个手填时刻，
--    起止时间由 Java 里的 DAY_START_HOUR/NIGHT_START_HOUR 常量硬算，班次字典里那两条值守班次
--    反而没人引用 —— 时刻改一次要发版，字典改了不影响存量行。加列后时间一律由班次带出。
-- -----------------------------------------------------------------------------
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_duty_roster' AND COLUMN_NAME = 'shift_id');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE biz_duty_roster ADD COLUMN shift_id bigint DEFAULT NULL COMMENT ''标准班次ID''',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 值守白班/夜班两条班次的固定 id（sql/199 铺底）；缺行时本段留 NULL，由自检 T17 兜出来
UPDATE biz_duty_roster SET shift_id = CASE WHEN shift_type = 2 THEN 890000000000000022 ELSE 890000000000000021 END
 WHERE del_flag = 0 AND shift_id IS NULL
   AND EXISTS (SELECT 1 FROM biz_shift b WHERE b.id = CASE WHEN shift_type = 2 THEN 890000000000000022 ELSE 890000000000000021 END);

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_duty_roster' AND COLUMN_NAME = 'post_id');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE biz_duty_roster ADD COLUMN post_id bigint DEFAULT NULL COMMENT ''值班点位ID'' AFTER shift_id',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_duty_roster' AND COLUMN_NAME = 'staff_schedule_id');
SET @sql := IF(@has_col = 0,
  'ALTER TABLE biz_duty_roster ADD COLUMN staff_schedule_id bigint DEFAULT NULL COMMENT ''员工排班ID'' AFTER post_id',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @has_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
                  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'biz_duty_roster' AND INDEX_NAME = 'uk_duty_post_date');
SET @sql := IF(@has_idx = 0,
  'ALTER TABLE biz_duty_roster ADD UNIQUE KEY uk_duty_post_date (post_id, duty_date)',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
-- uk 建在可空列上：存量行 post_id 为 NULL 时 MySQL 视之为互不相等，回填完成后「一天一位一人」才真正生效。

-- -----------------------------------------------------------------------------
-- 7b. 排班生成来源全仓统一成一套码值
--     护理那条线原先自己编了 1-手工 2-复制上周，与核心表的 1-手工 2-模板 3-复制周期 4-换班 撞码：
--     同一个「2」在两张表里是两个意思，Java 侧就只能留两个枚举，而 §13 要求同一含义全仓只有一个枚举。
--     这里把存量数据搬到核心表的码值上，此后两边共用 StaffScheduleSourceEnum。
-- -----------------------------------------------------------------------------
UPDATE biz_nurse_schedule SET schedule_source = 3 WHERE schedule_source = 2;

ALTER TABLE biz_nurse_schedule MODIFY COLUMN schedule_source tinyint NOT NULL DEFAULT 1
    COMMENT '生成来源（1-手工 2-模板 3-复制周期 4-换班）';

-- 值守点位铺底：与 sql/169 的四条责任位（白/夜 × 主/副）一一对应
INSERT INTO biz_duty_post (id, post_code, post_name, duty_scope, org_type, org_id, role_type, shift_id,
                           required_staff_type, sort_no, status, create_by, remark)
SELECT x.id, x.post_code, x.post_name, 1, 3, 0, x.role_type, x.shift_id, NULL, x.sort_no, 1, 'sql200', x.remark
FROM (
            SELECT 896740000000000101 id, 'DUTY_DAY_MAIN' post_code, '总值班白班主班' post_name,
                   1 role_type, 890000000000000021 shift_id, 10 sort_no, '全院总值班白班责任位' remark
  UNION ALL SELECT 896740000000000102, 'DUTY_DAY_VICE', '总值班白班副班', 2, 890000000000000021, 11,
                   '全院总值班白班副位'
  UNION ALL SELECT 896740000000000103, 'DUTY_NIGHT_MAIN', '总值班夜班主班', 1, 890000000000000022, 20,
                   '全院总值班夜班责任位（跨零点，归值班开始日）'
  UNION ALL SELECT 896740000000000104, 'DUTY_NIGHT_VICE', '总值班夜班副班', 2, 890000000000000022, 21,
                   '全院总值班夜班副位'
) x
WHERE NOT EXISTS (SELECT 1 FROM biz_duty_post p WHERE p.id = x.id OR p.post_code = x.post_code);

-- -----------------------------------------------------------------------------
-- 8. 字典铺底（Java 枚举是权威，这里同步 sys_dict_type / sys_dict_data）
-- -----------------------------------------------------------------------------
INSERT INTO sys_dict_type (id, dict_type, dict_name, status, remark, create_by, create_time, del_flag)
SELECT x.id, x.dict_type, x.dict_name, 1, x.remark, 'sql200', NOW(), 0
FROM (
            SELECT 896710000000000101 id, 'his_org_unit_type' dict_type, '排班单元类型' dict_name, '1-科室 2-病区 3-全院' remark
  UNION ALL SELECT 896710000000000102, 'his_duty_status', '出勤状态', '1-上班 2-休息 3-请假 4-培训 5-停班'
  UNION ALL SELECT 896710000000000103, 'his_attend_mode', '值班响应形态', '1-坐班 2-听班 3-留院值班'
  UNION ALL SELECT 896710000000000104, 'his_staff_schedule_source', '排班生成来源', '1-手工 2-模板 3-复制周期 4-换班'
  UNION ALL SELECT 896710000000000105, 'his_schedule_change_type', '排班变更类型', '1-换班 2-代班 3-停班 4-加号 5-减号 6-出诊变更'
  UNION ALL SELECT 896710000000000106, 'his_duty_scope', '值守责任范围', '1-全院行政 2-急诊 3-感染 4-总务 5-信息'
) x
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type t WHERE t.dict_type = x.dict_type AND t.del_flag = 0);

INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, status, create_by,
                           create_time, del_flag, remark, dict_source)
SELECT x.id, x.dict_type, x.dict_label, x.dict_value, x.dict_sort, 1, 'sql200', NOW(), 0, x.remark, 1
FROM (
  SELECT 896720000000000101 id, 'his_shift_scope' dict_type, '全院通用' dict_label, '4' dict_value, 4 dict_sort,
         '全院岗位出勤班次册（医技/窗口/行政通用）' remark
  UNION ALL SELECT 896720000000000111, 'his_org_unit_type', '科室', '1', 1, 'biz_staff_schedule.org_type'
  UNION ALL SELECT 896720000000000112, 'his_org_unit_type', '病区', '2', 2, 'biz_staff_schedule.org_type'
  UNION ALL SELECT 896720000000000113, 'his_org_unit_type', '全院', '3', 3, 'biz_staff_schedule.org_type'
  UNION ALL SELECT 896720000000000121, 'his_duty_status', '上班', '1', 1, 'biz_staff_schedule.duty_status'
  UNION ALL SELECT 896720000000000122, 'his_duty_status', '休息', '2', 2, 'biz_staff_schedule.duty_status'
  UNION ALL SELECT 896720000000000123, 'his_duty_status', '请假', '3', 3, 'biz_staff_schedule.duty_status'
  UNION ALL SELECT 896720000000000124, 'his_duty_status', '培训', '4', 4, 'biz_staff_schedule.duty_status'
  UNION ALL SELECT 896720000000000125, 'his_duty_status', '停班', '5', 5, 'biz_staff_schedule.duty_status'
  UNION ALL SELECT 896720000000000131, 'his_attend_mode', '坐班', '1', 1, 'biz_staff_schedule.attend_mode'
  UNION ALL SELECT 896720000000000132, 'his_attend_mode', '听班', '2', 2, 'biz_staff_schedule.attend_mode'
  UNION ALL SELECT 896720000000000133, 'his_attend_mode', '留院值班', '3', 3, 'biz_staff_schedule.attend_mode'
  UNION ALL SELECT 896720000000000141, 'his_staff_schedule_source', '手工', '1', 1, 'biz_staff_schedule.schedule_source'
  UNION ALL SELECT 896720000000000142, 'his_staff_schedule_source', '模板', '2', 2, 'biz_staff_schedule.schedule_source'
  UNION ALL SELECT 896720000000000143, 'his_staff_schedule_source', '复制周期', '3', 3, 'biz_staff_schedule.schedule_source'
  UNION ALL SELECT 896720000000000144, 'his_staff_schedule_source', '换班', '4', 4, 'biz_staff_schedule.schedule_source'
  UNION ALL SELECT 896720000000000151, 'his_schedule_change_type', '换班', '1', 1, 'biz_schedule_change_log.action_type'
  UNION ALL SELECT 896720000000000152, 'his_schedule_change_type', '代班', '2', 2, 'biz_schedule_change_log.action_type'
  UNION ALL SELECT 896720000000000153, 'his_schedule_change_type', '停班', '3', 3, 'biz_schedule_change_log.action_type'
  UNION ALL SELECT 896720000000000154, 'his_schedule_change_type', '加号', '4', 4, 'biz_schedule_change_log.action_type'
  UNION ALL SELECT 896720000000000155, 'his_schedule_change_type', '减号', '5', 5, 'biz_schedule_change_log.action_type'
  UNION ALL SELECT 896720000000000156, 'his_schedule_change_type', '出诊变更', '6', 6, 'biz_schedule_change_log.action_type'
  UNION ALL SELECT 896720000000000161, 'his_duty_scope', '全院行政', '1', 1, 'biz_duty_post.duty_scope'
  UNION ALL SELECT 896720000000000162, 'his_duty_scope', '急诊', '2', 2, 'biz_duty_post.duty_scope'
  UNION ALL SELECT 896720000000000163, 'his_duty_scope', '感染', '3', 3, 'biz_duty_post.duty_scope'
  UNION ALL SELECT 896720000000000164, 'his_duty_scope', '总务', '4', 4, 'biz_duty_post.duty_scope'
  UNION ALL SELECT 896720000000000165, 'his_duty_scope', '信息', '5', 5, 'biz_duty_post.duty_scope'
) x
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data d
                   WHERE d.dict_type = x.dict_type AND d.dict_value = x.dict_value AND d.del_flag = 0);

-- -----------------------------------------------------------------------------
-- 9. 人力标准迁移：病区护理规则 → 通用规则（病区合计行 shift_id=0 一并带过来）
-- -----------------------------------------------------------------------------
INSERT INTO biz_staff_plan_rule (id, org_type, org_id, org_name, shift_id, staff_type, min_staff, max_staff,
                                 max_week_hours, max_consecutive_night_days, max_consecutive_work_days,
                                 status, create_by, create_time, del_flag, remark)
SELECT 896750000000000000 + ROW_NUMBER() OVER (ORDER BY r.id),
       2, r.ward_id, r.ward_name, r.shift_id, 2, r.min_staff, r.max_staff,
       r.max_week_hours, r.max_consecutive_night_days, r.max_consecutive_work_days,
       r.status, 'sql200', NOW(), 0, r.remark
FROM biz_nurse_schedule_rule r
WHERE r.del_flag = 0
  AND NOT EXISTS (SELECT 1 FROM biz_staff_plan_rule p
                   WHERE p.org_type = 2 AND p.org_id = r.ward_id AND p.shift_id = r.shift_id AND p.staff_type = 2);

-- -----------------------------------------------------------------------------
-- 10. 存量迁移（一）：门诊与全院岗位排班 biz_schedule → 核心表
--     · shift_id / week_day 已在第 5 段统一成核心表口径，这里直接照搬；
--     · 停诊行仍按在岗落（号源停用 ≠ 人不在岗，见第 10 段内的注）；
--     · id 段 89676：基数取本段当前最大值，重跑不会撞主键。
--     · 同一人同日同班跨科室重复的行只落一条事实：核心表的 uk 是 (人, 日, 班)，不带科室，
--       而 biz_schedule 的 uk 带 dept_id —— 实测有 43 组「同一人被同时排进急诊内/外/儿科」，
--       即 SELECT 出来的行自己就会重复。NOT EXISTS 只能挡住目标表里已存在的行，
--       挡不住结果集内部自撞（原写法首次执行必然 ER_DUP_ENTRY），
--       所以先按 uk 开窗去一条代表行，其余的出诊计划照样绑到这条事实上
--       （第 5 段的绑定索引是普通索引，一条事实挂多条出诊计划是被允许的设计）。
--     · 时间快照取**班次的**起止，不取出诊行的：出诊行有时间存成 HH:mm:ss 的，
--       灌进核心表 VARCHAR(5) 在严格模式下会 Data too long；且既然班次已经认出来了，
--       核心表记的就是「上了哪个班」，班次的时刻才是唯一口径。
-- -----------------------------------------------------------------------------
SET @base := (SELECT COALESCE(MAX(id), 896760000000000000) FROM biz_staff_schedule
               WHERE id BETWEEN 896760000000000000 AND 896769999999999999);

INSERT INTO biz_staff_schedule (id, schedule_date, week_day, org_type, org_id, org_name, dept_id, dept_name,
                                employee_id, emp_code, employee_name, employee_post_id, staff_type,
                                shift_id, start_time, end_time, duty_status, attend_mode, clinic_flag,
                                work_minutes, schedule_source, template_id,
                                create_by, create_time, update_time, del_flag, remark)
SELECT @base + ROW_NUMBER() OVER (ORDER BY x.id),
       x.schedule_date, x.week_day,
       1, x.dept_id, x.dept_name, x.dept_id, x.dept_name,
       x.doctor_id, e.emp_code, x.doctor_name, p.id, x.staff_type,
       x.eff_shift,
       COALESCE(sh2.start_time, LEFT(x.start_time,5)),
       COALESCE(sh2.end_time,   LEFT(x.end_time,5)),
       -- 停诊行也按「在岗」落：停诊只是号源池停用，人可能还在科里；
       -- 把停诊落成停班会让在岗名单少一个本来能接到电话的人。要表达「今天不来」请在排班里改成请假/休息。
       1,
       1,
       CASE WHEN x.staff_type = 1 THEN 1 ELSE 0 END,
       COALESCE(sh2.duration_minutes, TIMESTAMPDIFF(MINUTE,
              CONCAT('1970-01-01 ', LEFT(x.start_time,5)), CONCAT('1970-01-01 ', LEFT(x.end_time,5))), 0),
       1, NULL,
       x.create_by, x.create_time, x.update_time, 0, x.remark
FROM (
  SELECT y.*,
         ROW_NUMBER() OVER (PARTITION BY y.doctor_id, y.schedule_date, y.eff_shift ORDER BY y.id) rn
    FROM (SELECT s.*, COALESCE(s.shift_id, 0) eff_shift
            FROM biz_schedule s
           WHERE s.del_flag = 0 AND s.schedule_date IS NOT NULL) y
) x
LEFT JOIN biz_shift sh2 ON sh2.id = x.eff_shift
LEFT JOIN sys_employee e ON e.id = x.doctor_id
LEFT JOIN sys_employee_post p ON p.employee_id = x.doctor_id AND p.dept_id = x.dept_id AND p.is_primary = 1
WHERE x.rn = 1
  AND NOT EXISTS (SELECT 1 FROM biz_staff_schedule t
                   WHERE t.employee_id = x.doctor_id AND t.schedule_date = x.schedule_date
                     AND t.shift_id = x.eff_shift);

UPDATE biz_schedule s
  JOIN biz_staff_schedule t
    ON t.employee_id = s.doctor_id
   AND t.schedule_date = s.schedule_date
   AND t.shift_id = COALESCE(s.shift_id, 0)
 SET s.staff_schedule_id = t.id
 WHERE s.del_flag = 0 AND s.schedule_date IS NOT NULL AND s.staff_schedule_id IS NULL;

-- -----------------------------------------------------------------------------
-- 11. 存量迁移（二）：病区护理排班 biz_nurse_schedule → 核心表（org_type=2）
--     schedule_status 与 duty_status 码值逐字相同（1-5），原样带过来；
--     休息/请假行本来就没有班次，shift_id 落 0。
--     · 同上：核心表 uk 不带病区，这里开窗是为了「同一人同日同班被重复写入两次」的历史脏行
--       不至于让迁移直接失败（实测本病区侧 1680 行无一重复，这层是防御性的）。
-- -----------------------------------------------------------------------------
SET @base := (SELECT COALESCE(MAX(id), 896770000000000000) FROM biz_staff_schedule
               WHERE id BETWEEN 896770000000000000 AND 896779999999999999);

INSERT INTO biz_staff_schedule (id, schedule_date, week_day, org_type, org_id, org_name, dept_id, dept_name,
                                employee_id, emp_code, employee_name, employee_post_id, staff_type,
                                shift_id, start_time, end_time, duty_status, attend_mode, clinic_flag,
                                work_minutes, schedule_source, template_id,
                                create_by, create_time, update_time, del_flag, remark)
SELECT @base + ROW_NUMBER() OVER (ORDER BY n.id),
       n.schedule_date, n.week_day,
       2, n.ward_id, n.ward_name, n.dept_id, n.dept_name,
       n.employee_id, n.emp_code, n.nurse_name, p.id, 2,
       COALESCE(n.shift_id, 0),
       LEFT(n.start_time,5), LEFT(n.end_time,5),   -- 本列是 varchar(10)，核心表 varchar(5)：截断
       n.schedule_status, 1, 0,
       n.work_minutes,
       -- 来源码值已在第 7b 段统一到核心表口径，这里逐字搬
       n.schedule_source, NULL,
       n.create_by, n.create_time, n.update_time, 0, n.remark
FROM (SELECT n.*, ROW_NUMBER() OVER (PARTITION BY n.employee_id, n.schedule_date,
                                        COALESCE(n.shift_id, 0) ORDER BY n.id) rn
        FROM biz_nurse_schedule n WHERE n.del_flag = 0) n
LEFT JOIN sys_employee_post p ON p.employee_id = n.employee_id AND p.dept_id = n.dept_id AND p.is_primary = 1
WHERE n.rn = 1
  AND NOT EXISTS (SELECT 1 FROM biz_staff_schedule t
                   WHERE t.employee_id = n.employee_id AND t.schedule_date = n.schedule_date
                     AND t.shift_id = COALESCE(n.shift_id, 0));

-- -----------------------------------------------------------------------------
-- 12. 存量迁移（三）：全院总值班 biz_duty_roster → 核心表（org_type=3，科室归全院）
--     换过班的行按**实际值班人**落事实（substitute_* 是这个人真的上了这个班）。
--     · 同上加窗口去重：代班会让「张三替李四上白班」与「张三自己白班」合成同一个人同一班，
--       实测本值守侧 28 行无一重复，这层同样是防御性的。
-- -----------------------------------------------------------------------------
SET @base := (SELECT COALESCE(MAX(id), 896780000000000000) FROM biz_staff_schedule
               WHERE id BETWEEN 896780000000000000 AND 896789999999999999);

INSERT INTO biz_staff_schedule (id, schedule_date, week_day, org_type, org_id, org_name, dept_id, dept_name,
                                employee_id, emp_code, employee_name, employee_post_id, staff_type,
                                shift_id, start_time, end_time, duty_status, attend_mode, clinic_flag,
                                work_minutes, schedule_source, template_id,
                                create_by, create_time, update_time, del_flag, remark)
SELECT @base + ROW_NUMBER() OVER (ORDER BY d.id),
       d.duty_date,
       CASE WHEN DAYOFWEEK(d.duty_date) = 1 THEN 7 ELSE DAYOFWEEK(d.duty_date) - 1 END,
       3, 0, '全院', 0, '全院',
       COALESCE(d.substitute_emp_id, d.employee_id), e.emp_code,
       COALESCE(d.substitute_emp_name, d.employee_name), p.id, COALESCE(r.staff_type, 6),
       d.eff_shift,
       COALESCE(sh.start_time, d.start_time), COALESCE(sh.end_time, d.end_time),
       CASE WHEN d.status = 0 THEN 5 ELSE 1 END, 3, 0,
       COALESCE(sh.duration_minutes, CASE WHEN d.shift_type = 2 THEN 840 ELSE 600 END), 1, NULL,
       d.create_by, d.create_time, d.update_time, 0, d.remark
FROM (SELECT d.*,
             COALESCE(d.shift_id, CASE WHEN d.shift_type = 2 THEN 890000000000000022
                                       ELSE 890000000000000021 END) eff_shift,
             ROW_NUMBER() OVER (PARTITION BY COALESCE(d.substitute_emp_id, d.employee_id), d.duty_date,
                                  COALESCE(d.shift_id, CASE WHEN d.shift_type = 2 THEN 890000000000000022
                                                            ELSE 890000000000000021 END)
                                ORDER BY d.id) rn
        FROM biz_duty_roster d WHERE d.del_flag = 0) d
LEFT JOIN biz_shift sh ON sh.id = d.eff_shift
LEFT JOIN sys_employee e ON e.id = COALESCE(d.substitute_emp_id, d.employee_id)
LEFT JOIN sys_employee_post p ON p.employee_id = COALESCE(d.substitute_emp_id, d.employee_id)
                       AND p.dept_id = d.dept_id AND p.is_primary = 1
LEFT JOIN sys_role r ON r.id = p.role_id
WHERE d.rn = 1
  AND NOT EXISTS (SELECT 1 FROM biz_staff_schedule t
                   WHERE t.employee_id = COALESCE(d.substitute_emp_id, d.employee_id)
                     AND t.schedule_date = d.duty_date
                     AND t.shift_id = d.eff_shift);

UPDATE biz_duty_roster d
   SET d.post_id = CASE WHEN d.shift_type = 2 AND d.role_type = 2 THEN 896740000000000104
                        WHEN d.shift_type = 2 THEN 896740000000000103
                        WHEN d.role_type = 2 THEN 896740000000000102
                        ELSE 896740000000000101 END
 WHERE d.del_flag = 0 AND d.post_id IS NULL;

UPDATE biz_duty_roster d
  JOIN biz_staff_schedule t
    ON t.employee_id = COALESCE(d.substitute_emp_id, d.employee_id)
   AND t.schedule_date = d.duty_date
   AND t.shift_id = COALESCE(d.shift_id, CASE WHEN d.shift_type = 2 THEN 890000000000000022 ELSE 890000000000000021 END)
 SET d.staff_schedule_id = t.id
 WHERE d.del_flag = 0 AND d.staff_schedule_id IS NULL;

-- -----------------------------------------------------------------------------
-- 13. 自检（跑完必须全绿）
-- -----------------------------------------------------------------------------
SELECT 'T1_核心表有数据' item, COUNT(*) cnt, '>0' expect FROM biz_staff_schedule WHERE del_flag = 0
UNION ALL SELECT 'T2_门诊行全部绑定', COUNT(*), 0 FROM biz_schedule WHERE del_flag = 0 AND schedule_date IS NOT NULL AND staff_schedule_id IS NULL
UNION ALL SELECT 'T3_值守行全部有点位', COUNT(*), 0 FROM biz_duty_roster WHERE del_flag = 0 AND post_id IS NULL
-- ⚠ 这两项原本写死 3 / 4（"当时库里就这些"）。sql/202 起通用册又加了 2 条医师值班班次、
-- sql/203 又铺了 234 个临床点位，写死的快照必然失真。断言要锁**本脚本铺的那部分**，
-- 不能锁"全表总数" —— 后者会随后续迁移漂移，每次都假报警。
UNION ALL SELECT 'T4_通用班次不少于3条', COUNT(*), '>=3' FROM biz_shift WHERE use_scope = 4 AND del_flag = 0
UNION ALL SELECT 'T5_行政点位4条', COUNT(*), 4 FROM biz_duty_post WHERE del_flag = 0 AND duty_scope = 1
UNION ALL SELECT 'T6_字典组6个', COUNT(*), 6 FROM sys_dict_type WHERE dict_type IN ('his_org_unit_type','his_duty_status','his_attend_mode','his_staff_schedule_source','his_schedule_change_type','his_duty_scope') AND del_flag = 0
UNION ALL SELECT 'T7_人×日×班唯一', COUNT(*), 0 FROM (SELECT employee_id, schedule_date, shift_id FROM biz_staff_schedule WHERE del_flag = 0 GROUP BY 1,2,3 HAVING COUNT(*) > 1) x
UNION ALL SELECT 'T8_出勤状态码值合法', COUNT(*), 0 FROM biz_staff_schedule WHERE del_flag = 0 AND duty_status NOT BETWEEN 1 AND 5
UNION ALL SELECT 'T9_星期口径1-7', COUNT(*), 0 FROM biz_staff_schedule WHERE del_flag = 0 AND week_day NOT BETWEEN 1 AND 7
UNION ALL SELECT 'T10_全院级行科室为0', COUNT(*), 0 FROM biz_staff_schedule WHERE del_flag = 0 AND org_type = 3 AND dept_id <> 0
UNION ALL SELECT 'T11_出诊标志只给医生', COUNT(*), 0 FROM biz_staff_schedule WHERE del_flag = 0 AND clinic_flag = 1 AND staff_type <> 1
UNION ALL SELECT 'T12_上班行都有班次', COUNT(*), 0 FROM biz_staff_schedule WHERE del_flag = 0 AND duty_status = 1 AND shift_id = 0
UNION ALL SELECT 'T13_人力标准迁移数=规则数',
       (SELECT COUNT(*) FROM biz_staff_plan_rule WHERE del_flag = 0 AND org_type = 2),
       (SELECT COUNT(*) FROM biz_nurse_schedule_rule WHERE del_flag = 0)
-- 排除 VERIFY200 夹具：verify-200-staff-schedule.mjs 会造「出诊事实」但不一定配出诊计划
-- （它测的是换班/拆人这类动作，不是门诊放号）。不排除的话自检结果取决于"清理跑没跑"。
UNION ALL SELECT 'T14_出诊事实都有出诊计划行', COUNT(*), 0
       FROM biz_staff_schedule t
       WHERE t.del_flag = 0 AND t.clinic_flag = 1 AND t.org_type = 1
         AND (t.remark IS NULL OR t.remark NOT LIKE 'VERIFY200-%')
         AND NOT EXISTS (SELECT 1 FROM biz_schedule s WHERE s.del_flag = 0 AND s.staff_schedule_id = t.id)
UNION ALL SELECT 'T15_值守夜班时长已纠正', COUNT(*), 1 FROM biz_shift WHERE id = 890000000000000022 AND duration_minutes = 840
UNION ALL SELECT 'T16_门诊星期口径已统一', COUNT(*), 0 FROM biz_schedule
       WHERE del_flag = 0 AND schedule_date IS NOT NULL
         AND week_day <> CASE WHEN DAYOFWEEK(schedule_date) = 1 THEN 7 ELSE DAYOFWEEK(schedule_date) - 1 END
UNION ALL SELECT 'T17_值守行都有班次', COUNT(*), 0 FROM biz_duty_roster WHERE del_flag = 0 AND shift_id IS NULL
UNION ALL SELECT 'T18_值守班次引用有效', COUNT(*), 0
       FROM biz_duty_roster d LEFT JOIN biz_shift s ON s.id = d.shift_id
       WHERE d.del_flag = 0 AND d.shift_id IS NOT NULL AND s.id IS NULL
UNION ALL SELECT 'T19_值守行全部绑定', COUNT(*), 0 FROM biz_duty_roster WHERE del_flag = 0 AND staff_schedule_id IS NULL;
