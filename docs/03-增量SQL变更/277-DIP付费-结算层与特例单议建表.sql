-- =============================================================
-- 277-DIP付费-结算层与特例单议建表.sql（P4 结算层）
-- 说明：
--   1) biz_medpay_settle：按病种付费（DRG/DIP）结算结果，一张出院结算账单一条，
--      按 (settle_bill_id, pay_mode, version) 幂等覆盖。DRG 走「权重×费率」、
--      DIP 走「分值(基准×系数)×点值」，应付/盈亏未下放参数时为 NULL（不编造）。
--   2) biz_case_appeal：特例单议（病例申诉），DRG/DIP 共用一张，按 pay_mode 区分；
--      官方口径：特例单议占结算病例 DRG ≤5%、DIP 2027 起 ≤1%。
--   3) sys_config 新增 drg.rate（元/权重）；275 已落 medpay.mode / dip.default_version / dip.point_value。
--      本脚本写入的 drg.rate 为演示值，仅当当前为空/未配置时写入，正式接入统筹区费率不覆盖。
--   4) 菜单 909「医保付费结算」，权限 medpay:settle:list，路由 /medpaysettle，
--      组件 drg/MedpaySettleView，角色 1/17/18（镜像 907/908 的授权）。
-- id 段：27700000000000001xx（表）/ 27700000000000009xx（菜单与角色授权）。
-- 可重复执行：建表 IF NOT EXISTS，菜单/授权按唯一键守卫。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;
START TRANSACTION;

-- -------------------------------------------------------------
-- 1. biz_medpay_settle  按病种付费结算结果（DRG/DIP 共用，pay_mode 区分）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_medpay_settle`
(
    `id`               bigint        NOT NULL COMMENT '主键ID',
    `settle_bill_id`   bigint        NOT NULL COMMENT '结算账单ID（biz_settlement_bill.id，bill_type=4 出院结算）',
    `bill_no`          varchar(64)   COMMENT '账单号（快照）',
    `summary_id`       bigint        COMMENT '病案首页ID（biz_inpatient_summary.id）',
    `admission_id`     bigint        COMMENT '住院ID（biz_inpatient_summary.admission_id）',
    `patient_name`     varchar(50)   COMMENT '患者姓名（快照）',
    `pay_mode`         varchar(16)   NOT NULL COMMENT '付费方式（DRG / DIP，按 medpay.mode 路由时快照）',
    `version`          varchar(32)   NOT NULL COMMENT '分组方案版本（DRG=chs-drg-2.0/3.0；DIP=2.0/3.0）',
    `group_code`       varchar(32)   COMMENT '组码/病种码（DRG=drg_code；DIP=disease_code；未入组为空）',
    `group_name`       varchar(200)  COMMENT '组名/病种名',
    `group_type`       tinyint       COMMENT 'DIP 病种类型（1-核心病种 2-综合病种；DRG 为 NULL）',
    `weight`           decimal(10,4) COMMENT 'DRG 权重（官方包未含时为 NULL）',
    `rate`             decimal(10,6) COMMENT 'DRG 费率（元/权重，统筹区公布，快照）',
    `base_score`       decimal(10,4) COMMENT 'DIP 基准分值',
    `coeff`            decimal(6,4)  COMMENT 'DIP 病例组合系数（辅助目录命中系数，未命中 1.0000）',
    `score`            decimal(10,4) COMMENT 'DIP 结算分值 = 基准分值 × 系数',
    `point_value`      decimal(10,6) COMMENT 'DIP 点值（元/分值，快照）',
    `insurance_amount` decimal(10,2) COMMENT '医保应付（DRG=权重×费率；DIP=分值×点值；参数未下发为 NULL）',
    `actual_amount`    decimal(10,2) COMMENT '实际住院费用（结算账单 total_amount）',
    `profit_amount`    decimal(10,2) COMMENT '盈亏 = 医保应付 - 实际费用（任一侧为 NULL 则为 NULL）',
    `calc_status`      tinyint       NOT NULL COMMENT '结算状态（1-已入组计算 2-未入组）',
    `rule_note`        varchar(300)  COMMENT '命中路径说明',
    `settle_time`      datetime      COMMENT '结算计算时间',
    `create_by`        varchar(64)   NOT NULL COMMENT '创建人',
    `create_by_id`     bigint        COMMENT '创建人ID',
    `create_time`      datetime      NOT NULL COMMENT '创建时间',
    `update_by`        varchar(64)   NOT NULL COMMENT '更新人',
    `update_by_id`     bigint        COMMENT '更新人ID',
    `update_time`      datetime      NOT NULL COMMENT '更新时间',
    `del_flag`         tinyint       NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`           varchar(500)  COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_medpay_settle` (`settle_bill_id`, `pay_mode`, `version`, `del_flag`),
    KEY `idx_summary` (`summary_id`, `version`),
    KEY `idx_pay_mode` (`pay_mode`, `calc_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '按病种付费（DRG/DIP）结算结果';

-- -------------------------------------------------------------
-- 2. biz_case_appeal  特例单议（病例申诉，DRG/DIP 共用）
--    官方口径：特例单议占结算病例 DRG ≤5%、DIP 2027 起 ≤1%（医保发〔2025〕18号配套办法）。
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `biz_case_appeal`
(
    `id`             bigint        NOT NULL COMMENT '主键ID',
    `appeal_no`      varchar(32)   NOT NULL COMMENT '特例单议申请单号',
    `settle_id`      bigint        NOT NULL COMMENT '结算结果ID（biz_medpay_settle.id）',
    `settle_bill_id` bigint        COMMENT '结算账单ID（快照）',
    `summary_id`     bigint        COMMENT '病案首页ID（快照）',
    `patient_name`   varchar(50)   COMMENT '患者姓名（快照）',
    `pay_mode`       varchar(16)   COMMENT '付费方式（DRG / DIP，快照）',
    `group_code`     varchar(32)   COMMENT '组码/病种码（快照）',
    `group_name`     varchar(200)  COMMENT '组名/病种名（快照）',
    `reason_type`    tinyint       NOT NULL COMMENT '申请类型（1-费用极高 2-重大新技术 3-危急重症 4-罕见病 5-其他）',
    `reason_desc`    varchar(500)  COMMENT '申请理由',
    `profit_amount`  decimal(10,2) COMMENT '申请时盈亏（快照）',
    `status`         tinyint       NOT NULL COMMENT '状态（1-草稿 2-已提交 3-审核通过 4-审核驳回 5-已撤回）',
    `apply_by`       varchar(64)   COMMENT '申请人',
    `apply_by_id`    bigint        COMMENT '申请人ID',
    `apply_time`     datetime      COMMENT '提交时间',
    `review_by`      varchar(64)   COMMENT '审核人',
    `review_by_id`   bigint        COMMENT '审核人ID',
    `review_time`    datetime      COMMENT '审核时间',
    `review_remark`  varchar(500)  COMMENT '审核意见',
    `create_by`      varchar(64)   NOT NULL COMMENT '创建人',
    `create_by_id`   bigint        COMMENT '创建人ID',
    `create_time`    datetime      NOT NULL COMMENT '创建时间',
    `update_by`      varchar(64)   NOT NULL COMMENT '更新人',
    `update_by_id`   bigint        COMMENT '更新人ID',
    `update_time`    datetime      NOT NULL COMMENT '更新时间',
    `del_flag`       tinyint       NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`         varchar(500)  COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_appeal_no` (`appeal_no`, `del_flag`),
    KEY `idx_settle` (`settle_id`),
    KEY `idx_status` (`status`, `pay_mode`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '按病种付费特例单议（病例申诉）';

-- -------------------------------------------------------------
-- 3. sys_config：DRG 费率（演示值，仅空时写入；正式接入统筹区费率后覆盖）
-- -------------------------------------------------------------
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark,
                        create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2770000000000000001', 'DRG 结算费率（元/权重）', 'drg.rate', '12000.000000', 1, 0,
       '演示费率（元/权重）：12000 为演示样例，正式接入以统筹区公布为准',
       'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'drg.rate'
                  AND config_value IS NOT NULL AND config_value <> '');

-- -------------------------------------------------------------
-- 4. 菜单 909「医保付费结算」+ 角色授权（1/17/18，镜像 275 的 908 写法）
-- -------------------------------------------------------------
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id, create_time, update_time)
SELECT '909', '医保付费结算', '1200', 22, 2, '/medpaysettle', 'drg/MedpaySettleView', 'report.medpaysettle', 'icon-suanfa',
       'medpay:settle:list', 0, 0, 1, 1,
       'admin', 'admin', 0, 'P4 结算层：出院结算账单按 DRG/DIP 计算医保应付与盈亏 + 特例单议', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = '909' OR menu_key = 'report.medpaysettle' OR permission = 'medpay:settle:list');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2770000000000000901', '1', '909', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '1' AND menu_id = '909');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2770000000000000902', '17', '909', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '17' AND menu_id = '909');

INSERT INTO sys_role_menu (id, role_id, menu_id, create_by, update_by, create_by_id, update_by_id, create_time, update_time)
SELECT '2770000000000000903', '18', '909', 'admin', 'admin', '1', '1', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = '18' AND menu_id = '909');

COMMIT;

-- 回查（人工核对用，勿删）：
-- SELECT COUNT(*) FROM biz_case_appeal WHERE del_flag = 0;
-- SELECT config_key, config_value FROM sys_config WHERE config_key IN ('drg.rate', 'medpay.mode', 'dip.point_value');
-- 验证链路：菜单 909「医保付费结算」→ 批量计算（默认最近 50 张出院结算账单）→ 结果表出现应付/盈亏；
--           超支（盈亏<0）行可发起特例单议 → 提交 → 审核通过/驳回。
