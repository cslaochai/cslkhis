-- =====================================================================
-- 20261025 护理排班模板（周模板 + 班次实用排班模式 + 套用到周）
-- ---------------------------------------------------------------------
-- 背景：
--   护理排班长期依赖「人工点格 / 复制上周」，没有可复用的周模板。
--   本次新增护理排班模板体系，让护士长把「APN 三班倒 / 8-8 白夜两班 / 小组排班」
--   等周固定排班沉淀成模板，一键套用到任意一周，生成真实护士周排班。
--   套用落库复用现有护理周排班底座（biz_schedule + scheduleSource=TEMPLATE），
--   与「复制上周」是同一条事实写入口（staffScheduleService.replaceDayAttendance）。
-- 范围（四张表，全在业务库 hn_biz_his）：
--   A. biz_nurse_schedule_template        模板主表（周模板：护士×星期几的固定出勤）
--   B. biz_nurse_schedule_template_item    模板明细（一行 = 某护士某星期几的出勤）
--   C. biz_nurse_schedule_template_rule    班次实用排班模式（APN/8-8/小组，脚手架参考）
--   D. biz_nurse_schedule_template_apply   套用记录（审计，只写不删）
-- 权限：复用护理排班权限域 nursing:schedule:*（list/add/edit/delete）。
--   模板内容嵌入「护理排班(331)」页的「排班模板」签，不另开菜单；
--   按钮码沿用 2650/2651/2652（nursing:schedule:add/edit/delete），无需新增。
-- 软删说明：template/item/rule 用「手动维护 del_flag」而非 @TableLogic ——
--   因为「已删除模板」统计必须能查到 del_flag=1 的行，@TableLogic 会自动过滤掉。
-- 幂等：全程 CREATE TABLE IF NOT EXISTS + 固定命名索引，可重复执行。
-- 注意：执行后清 Redis 权限缓存（his:perm:role:* / his:user:identity:*）或重启后端。
-- =====================================================================

-- =====================================================================
-- A. 模板主表
-- =====================================================================
CREATE TABLE IF NOT EXISTS `biz_nurse_schedule_template` (
  `id`           bigint       NOT NULL                COMMENT '主键ID（雪花）',
  `name`         varchar(100) NOT NULL                COMMENT '模板名称（如「内科一组周模板」）',
  `unit_type`    tinyint      NOT NULL DEFAULT '1'    COMMENT '排班单元类型（1-病区 2-门诊科室；空按1-病区）',
  `ward_id`      bigint       NOT NULL DEFAULT '0'    COMMENT '排班单元ID（unit_type=1 取病区ID，=2 取门诊科室ID）',
  `ward_name`    varchar(128) NOT NULL                COMMENT '排班单元名称（快照）',
  `week_parity`  tinyint      NOT NULL DEFAULT '0'    COMMENT '单双周（0-每周 1-单周 2-双周，按 ISO 周号奇偶）',
  `valid_from`   date              DEFAULT NULL       COMMENT '生效起始日期（空=不限）',
  `valid_until`  date              DEFAULT NULL       COMMENT '生效截止日期（空=不限）',
  `scope`        tinyint      NOT NULL DEFAULT '0'    COMMENT '共享范围（0-个人 1-权限组共享）',
  `status`       tinyint      NOT NULL DEFAULT '1'    COMMENT '状态（0-停用 1-启用）',
  `del_flag`     tinyint      NOT NULL DEFAULT '0'    COMMENT '删除标志（0-正常 1-删除；手动维护，便于已删除统计）',
  `remark`       varchar(500)    DEFAULT NULL          COMMENT '备注',
  `create_by`    varchar(64)  NOT NULL                COMMENT '创建人',
  `create_time`  datetime     NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by`    varchar(64)  NOT NULL                COMMENT '更新人',
  `update_time`  datetime     NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint       NOT NULL                COMMENT '创建人ID',
  `update_by_id` bigint       NOT NULL                COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_unit`       (`unit_type`, `ward_id`),
  KEY `idx_del_status` (`del_flag`, `status`),
  KEY `idx_scope`      (`scope`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理排班模板（周模板：护士×星期几的固定出勤）';

-- =====================================================================
-- B. 模板明细（一行 = 一个护士在某星期几的出勤）
-- =====================================================================
CREATE TABLE IF NOT EXISTS `biz_nurse_schedule_template_item` (
  `id`              bigint       NOT NULL                COMMENT '主键ID（雪花）',
  `template_id`     bigint       NOT NULL                COMMENT '所属模板ID',
  `employee_id`     bigint       NOT NULL                COMMENT '护士ID',
  `employee_name`   varchar(50)  NOT NULL                COMMENT '护士姓名（快照，报错点名用）',
  `week_day`        tinyint      NOT NULL                COMMENT '星期几（1-周一 … 7-周日）',
  `schedule_status` tinyint      NOT NULL                COMMENT '出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）',
  `shift_id`        bigint          DEFAULT NULL         COMMENT '班次ID（仅状态=1-上班时有效；非上班由服务端强制置空）',
  `del_flag`        tinyint      NOT NULL DEFAULT '0'    COMMENT '删除标志（0-正常 1-删除；手动维护）',
  `remark`          varchar(500)    DEFAULT NULL          COMMENT '备注',
  `create_by`       varchar(64)  NOT NULL                COMMENT '创建人',
  `create_time`     datetime     NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by`       varchar(64)  NOT NULL                COMMENT '更新人',
  `update_time`     datetime     NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id`    bigint       NOT NULL                COMMENT '创建人ID',
  `update_by_id`    bigint       NOT NULL                COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_template` (`template_id`),
  KEY `idx_del`     (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理排班模板明细（护士×星期几×出勤/班次）';

-- =====================================================================
-- C. 班次实用排班模式规则（APN / 8-8 / 小组，脚手架参考，非强约束）
-- =====================================================================
CREATE TABLE IF NOT EXISTS `biz_nurse_schedule_template_rule` (
  `id`            bigint       NOT NULL                COMMENT '主键ID（雪花）',
  `rule_name`     varchar(100) NOT NULL                COMMENT '模式名称（如「APN 三班倒」）',
  `rule_code`     varchar(50)  NOT NULL                COMMENT '模式编码（APN / 8-8 / GROUP，业务约定）',
  `pattern_detail` text            DEFAULT NULL         COMMENT '班次模式明细（JSON：由哪些班次组成、排班结构说明；前端只读）',
  `status`        tinyint      NOT NULL DEFAULT '1'    COMMENT '状态（0-停用 1-启用）',
  `del_flag`      tinyint      NOT NULL DEFAULT '0'    COMMENT '删除标志（0-正常 1-删除；手动维护）',
  `remark`        varchar(500)    DEFAULT NULL          COMMENT '备注',
  `create_by`     varchar(64)  NOT NULL                COMMENT '创建人',
  `create_time`   datetime     NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by`     varchar(64)  NOT NULL                COMMENT '更新人',
  `update_time`   datetime     NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id`  bigint       NOT NULL                COMMENT '创建人ID',
  `update_by_id`  bigint       NOT NULL                COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_del`   (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理排班模板规则（班次实用排班模式：APN/8-8/小组）';

-- =====================================================================
-- D. 套用记录（谁在何时把哪个模板套到哪个单元哪一周，生成/跳过多少格；只写不删）
-- =====================================================================
CREATE TABLE IF NOT EXISTS `biz_nurse_schedule_template_apply` (
  `id`              bigint       NOT NULL                COMMENT '主键ID（雪花）',
  `template_id`     bigint          DEFAULT NULL         COMMENT '套用模板ID',
  `template_name`   varchar(128)    DEFAULT NULL         COMMENT '套用模板名称（快照）',
  `unit_type`       tinyint         DEFAULT NULL         COMMENT '排班单元类型（1-病区 2-门诊科室）',
  `ward_id`         bigint          DEFAULT NULL         COMMENT '排班单元ID',
  `ward_name`       varchar(128)    DEFAULT NULL         COMMENT '排班单元名称（快照）',
  `apply_week_start` date           DEFAULT NULL         COMMENT '套用目标周起始（周一）',
  `apply_week_end`   date           DEFAULT NULL         COMMENT '套用目标周结束（周日）',
  `applied_by_id`   bigint          DEFAULT NULL         COMMENT '操作人ID',
  `applied_by_name` varchar(50)     DEFAULT NULL         COMMENT '操作人姓名',
  `generated_count` int          NOT NULL DEFAULT '0'    COMMENT '实际生成排班格数',
  `skipped_count`   int          NOT NULL DEFAULT '0'    COMMENT '跳过格数（已排/日期已过/单双周不匹配/不在生效期/班次失效等）',
  `status`          tinyint      NOT NULL DEFAULT '0'    COMMENT '状态（0-成功 1-部分跳过）',
  `remark`          varchar(500)    DEFAULT NULL          COMMENT '备注（含套用汇总说明）',
  `create_by`       varchar(64)  NOT NULL                COMMENT '创建人',
  `create_time`     datetime     NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by`       varchar(64)  NOT NULL                COMMENT '更新人',
  `update_time`     datetime     NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id`    bigint       NOT NULL                COMMENT '创建人ID',
  `update_by_id`    bigint       NOT NULL                COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_template` (`template_id`),
  KEY `idx_ward`     (`ward_id`, `apply_week_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理排班模板套用记录（审计，只写不删）';

-- =====================================================================
-- 回查
-- =====================================================================
-- 1. 四张表必须存在
-- SELECT table_name FROM information_schema.tables
--   WHERE table_schema = DATABASE() AND table_name LIKE 'biz_nurse_schedule_template%';
-- 2. 套用落库来源应与「复制上周」一致：biz_schedule.schedule_source = 2（TEMPLATE）
-- SELECT * FROM biz_schedule WHERE schedule_source = 2 LIMIT 10;
-- 3. 变更类型枚举 APPLY_TEMPLATE = 11（运行时以 NurseScheduleChangeTypeEnum 为准，
--    留痕表 biz_nurse_schedule_log.action_type 记录 11；无需单独维护字典表）
