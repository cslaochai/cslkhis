-- =============================================================
-- 353-门诊取药队列 + 药房发药窗口（补「患者到窗口取药」这一小层）
-- 背景：
--   门诊发药现有 biz_drug_dispensing 只有三态（1-待发药 2-已发药 3-已退药），
--   缴费即待发药、药师点了就已发药，中间没有「患者是否到场」这一步。
--   于是药师无法区分「缴了费人没来」和「还没缴费」，叫号屏也无号可叫。
--   本增量补两层：
--     1) biz_opd_station 加「6-发药窗口」档位 + 铺 4 个取药窗口（复用既有工位字典与派岗，不新建窗口表）；
--     2) biz_dispense_queue 门诊取药队列（处方级：取号 / 签到 / 叫号 / 整单发药完成）。
-- 口径（与需求确认一致）：
--   · 药房无自动发药机，药师在窗口一人完成取药+核对+交付 → 不设「配药」独立阶段，
--     故队列表无 prepare_by / prepare_time / prepare_mode 三列（那三列的前提是后台机器调剂）。
--   · 窗口复用 biz_opd_station（门诊公共工位字典）+ biz_opd_station_assignment（派岗）：
--     药房窗口与输液室/采血室同类（白天常规服务岗、公共、按天按岗派人），277 号增量即为该场景建表，
--     required_staff_type 本就有「4-药学」档位。窗口是固定 4 个的有限资源，派岗即可落到具体窗口。
--   · 号池全局唯一（uk_dpq_date_no）：签到发一个全药房当日号，叫号时动态分配给空闲窗口，
--     避免分窗口独立号池造成某窗口堵死。window_id 在叫号时才写入。
--   · 队列是处方级（整单叫号），发药行是明细级（一药一行）；两者不建直接外键，
--     按 prescription_id 归集，配药进度用 dispensed_item_count / prescription_item_count 算。
--   · 同一处方同一日只排一次队（uk_dpq_prescription_date）；当日退药重发次日可再排。
-- 唯一键均不含 del_flag → 本表清理走物理删（软删行会占住唯一键）。
-- 幂等：建表 IF NOT EXISTS；窗口按 station_code 判重；列注释可重复 ALTER。
-- =============================================================

-- -------------------------------------------------------------
-- 一、工位类别补齐「6-发药窗口」码值（同步：OpdStationKindEnum、列注释、铺底数据）
-- -------------------------------------------------------------
ALTER TABLE `biz_opd_station`
    MODIFY COLUMN `station_kind` tinyint NOT NULL DEFAULT 0
    COMMENT '工位类别（1-总导诊 2-门诊分诊台 3-输液室 4-注射室 5-采血室 6-发药窗口 9-其他）';

ALTER TABLE `biz_opd_station_assignment`
    MODIFY COLUMN `station_kind` tinyint NOT NULL DEFAULT 0
    COMMENT '工位类别快照（1总导诊 2分诊台 3输液室 4注射室 5采血室 6发药窗口 9其他）';

-- -------------------------------------------------------------
-- 二、铺 4 个门诊西药房取药窗口（按编码判重，可重复执行）
-- id 段 35300000000000001xx，与 277 段（277...1xx）及雪花 id 错开；sort_no 接在既有 10-50 之后用 61-64
-- -------------------------------------------------------------
INSERT INTO biz_opd_station
    (id, station_code, station_name, station_kind, required_staff_type, capacity,
     sort_no, status, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT * FROM (
    SELECT 3530000000000000101 AS id, 'OP-DRUG-1' AS station_code, '门诊西药房1号取药窗口' AS station_name, 6 AS station_kind, 4 AS required_staff_type, 1 AS capacity, 61 AS sort_no, 1 AS status, 'admin' AS create_by, NOW() AS create_time, 'admin' AS update_by, NOW() AS update_time, 0 AS del_flag, '患者刷证取药叫号' AS remark UNION ALL
    SELECT 3530000000000000102, 'OP-DRUG-2', '门诊西药房2号取药窗口', 6, 4, 1, 62, 1, 'admin', NOW(), 'admin', NOW(), 0, '患者刷证取药叫号' UNION ALL
    SELECT 3530000000000000103, 'OP-DRUG-3', '门诊西药房3号取药窗口', 6, 4, 1, 63, 1, 'admin', NOW(), 'admin', NOW(), 0, '患者刷证取药叫号' UNION ALL
    SELECT 3530000000000000104, 'OP-DRUG-4', '门诊西药房4号取药窗口', 6, 4, 1, 64, 1, 'admin', NOW(), 'admin', NOW(), 0, '患者刷证取药叫号'
) seed
WHERE NOT EXISTS (SELECT 1 FROM biz_opd_station s WHERE s.station_code = seed.station_code);

-- -------------------------------------------------------------
-- 三、门诊取药队列（处方级：取号 → 签到 → 叫号 → 整单发药完成）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_dispense_queue` (
    `id`                    bigint      NOT NULL COMMENT '主键ID（雪花）',
    `queue_date`            date        NOT NULL COMMENT '取药日期',
    `queue_no`              varchar(16) NOT NULL COMMENT '取药号（当日全药房流水）',
    `prescription_id`       bigint      NOT NULL COMMENT '处方ID',
    `prescription_no`       varchar(32) NULL COMMENT '处方号（快照）',
    `prescription_item_count` int       NOT NULL DEFAULT 0 COMMENT '处方明细条数',
    `dispensed_item_count`  int         NOT NULL DEFAULT 0 COMMENT '已发药明细条数（配药进度）',
    `patient_id`            bigint      NOT NULL COMMENT '患者ID',
    `patient_no`            varchar(32) NULL COMMENT '患者号（快照）',
    `patient_name`          varchar(50) NULL COMMENT '患者姓名（快照）',
    `window_id`             bigint      NULL COMMENT '发药窗口ID',
    `window_name`           varchar(64) NULL COMMENT '发药窗口名称（快照）',
    `queue_status`          tinyint     NOT NULL DEFAULT 1 COMMENT '队列状态（1-待取药 2-已发药 3-已过号 4-已取消）',
    `sign_in_time`          datetime    NOT NULL COMMENT '到面签到时间（刷身份证/就诊卡）',
    `sign_in_type`          tinyint     NULL COMMENT '签到方式（1-身份证 2-就诊卡 3-扫码 4-手工）',
    `call_count`            int         NOT NULL DEFAULT 0 COMMENT '叫号次数',
    `call_time`             datetime    NULL COMMENT '最近叫号时间',
    `dispense_time`         datetime    NULL COMMENT '整单发药完成时间',
    `cancel_reason`         varchar(200) NULL COMMENT '过号或取消原因',
    `create_by`             varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id`          bigint      NULL COMMENT '创建人ID',
    `create_time`           datetime    NOT NULL DEFAULT (now()) COMMENT '创建时间',
    `update_by`             varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id`          bigint      NULL COMMENT '更新人ID',
    `update_time`           datetime    NOT NULL DEFAULT (now()) COMMENT '更新时间',
    `del_flag`              tinyint     NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
    `remark`                varchar(500) NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dpq_date_no` (`queue_date`, `queue_no`),
    UNIQUE KEY `uk_dpq_prescription_date` (`prescription_id`, `queue_date`),
    KEY `idx_dpq_status_signin` (`queue_status`, `sign_in_time`),
    KEY `idx_dpq_window` (`window_id`, `queue_status`),
    KEY `idx_dpq_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门诊取药队列';

-- =============================================================
-- 四、回查（执行后逐条确认）
-- =============================================================
-- SELECT id, station_code, station_name, station_kind, required_staff_type, sort_no, status
--   FROM biz_opd_station WHERE station_kind = 6 ORDER BY sort_no;
-- SHOW CREATE TABLE biz_dispense_queue;
-- SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS
--   WHERE TABLE_SCHEMA = 'hn_biz_his' AND TABLE_NAME = 'biz_opd_station' AND COLUMN_NAME = 'station_kind';
