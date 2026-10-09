-- 领域：19-收费四层（记账·结算·支付·票据）
-- 库：hn_biz_his    表数：17
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- biz_fee_record  费用记账流水
-- ----------------------------
CREATE TABLE `biz_fee_record`
(
    `id`              bigint         NOT NULL COMMENT '主键（雪花）',
    `fee_no`          varchar(32)    NOT NULL COMMENT '记账流水号',
    `patient_id`      bigint         NOT NULL COMMENT '患者ID',
    `patient_no`      varchar(32)             DEFAULT NULL COMMENT '患者号（快照）',
    `patient_name`    varchar(50)    NOT NULL COMMENT '患者姓名（快照）',
    `encounter_type`  tinyint        NOT NULL COMMENT '就诊类型（1-门诊 2-住院）',
    `encounter_id`    bigint         NOT NULL COMMENT '就诊标识',
    `encounter_no`    varchar(32)             DEFAULT NULL COMMENT '就诊标识单号',
    `dept_id`         bigint                  DEFAULT NULL COMMENT '费用归属科室',
    `dept_name`       varchar(100)            DEFAULT NULL COMMENT '科室名称（快照）',
    `doctor_id`       bigint                  DEFAULT NULL COMMENT '开单/执行人员工ID',
    `doctor_name`     varchar(50)             DEFAULT NULL COMMENT '开单人姓名（快照）',
    `item_type`       tinyint        NOT NULL COMMENT '项目类型（1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗 8-耗材）',
    `item_code`       varchar(32)             DEFAULT NULL COMMENT '项目/药品编码',
    `item_name`       varchar(200)   NOT NULL COMMENT '项目名称',
    `specification`   varchar(100)            DEFAULT NULL COMMENT '规格',
    `unit`            varchar(20)             DEFAULT NULL COMMENT '单位',
    `catalog_type`    tinyint                 DEFAULT NULL COMMENT '医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）',
    `price`           decimal(10, 4) NOT NULL COMMENT '单价',
    `quantity`        decimal(10, 2) NOT NULL COMMENT '数量',
    `amount`          decimal(12, 2) NOT NULL COMMENT '金额=单价×数量',
    `fee_status`      tinyint        NOT NULL DEFAULT '1' COMMENT '记账状态（1-待结算 2-已锁定 3-已结算 4-已红冲）',
    `source_type`     tinyint        NOT NULL COMMENT '费用来源',
    `source_id`       bigint                  DEFAULT NULL COMMENT '来源单据ID（处方明细ID/申请ID/医嘱ID…）',
    `source_no`       varchar(64)             DEFAULT NULL COMMENT '来源单据号',
    `orig_fee_id`     bigint                  DEFAULT NULL COMMENT '红冲双向指针',
    `refunded_amount` decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '累计已冲金额',
    `book_time`       datetime       NOT NULL COMMENT '记账时间',
    `book_by_id`      bigint                  DEFAULT NULL COMMENT '记账人员工ID',
    `book_by_name`    varchar(64)             DEFAULT NULL COMMENT '记账人姓名（快照）',
    `bill_id`         bigint                  DEFAULT NULL COMMENT '所属结算账单ID',
    `create_by`       varchar(64)    NOT NULL,
    `create_by_id`    bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`       varchar(64)    NOT NULL,
    `update_by_id`    bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`        tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500)            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fee_no` (`fee_no`),
    KEY               `idx_fee_patient` (`patient_id`,`book_time`),
    KEY               `idx_fee_encounter` (`encounter_type`,`encounter_id`),
    KEY               `idx_fee_settle` (`fee_status`,`bill_id`),
    KEY               `idx_fee_dept_time` (`dept_id`,`book_time`),
    KEY               `idx_fee_source` (`source_type`,`source_id`),
    KEY               `idx_fee_book_time` (`book_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='费用记账流水';

-- ----------------------------
-- biz_settlement_bill  结算账单
-- ----------------------------
CREATE TABLE `biz_settlement_bill`
(
    `id`              bigint         NOT NULL COMMENT '主键（雪花）',
    `bill_no`         varchar(32)    NOT NULL COMMENT '账单号',
    `patient_id`      bigint         NOT NULL COMMENT '患者ID',
    `patient_no`      varchar(32)             DEFAULT NULL COMMENT '患者号（快照）',
    `patient_name`    varchar(50)    NOT NULL COMMENT '患者姓名（快照）',
    `encounter_type`  tinyint        NOT NULL COMMENT '就诊类型（1-门诊 2-住院）',
    `encounter_id`    bigint         NOT NULL COMMENT '就诊标识',
    `encounter_no`    varchar(32)             DEFAULT NULL COMMENT '就诊标识单号（快照）',
    `bill_type`       tinyint        NOT NULL DEFAULT '2' COMMENT '账单类型（1-挂号费结算 2-门诊诊间结算 3-住院中途结算 4-出院结算）',
    `fee_count`       int            NOT NULL DEFAULT '0' COMMENT '纳入本账单的记账行数',
    `total_amount`    decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '应收合计',
    `discount_amount` decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '院内优惠/抹零',
    `settlement_mode` tinyint        NOT NULL DEFAULT '1' COMMENT '结算方式（1-自费 2-医保）',
    `insurance_type`  varchar(32)             DEFAULT NULL COMMENT '医保类型',
    `pool_amount`     decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '医保统筹支付',
    `account_amount`  decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '医保个人账户支付',
    `self_amount`     decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '个人自费',
    `payable_amount`  decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '患者应缴 = total - discount - pool - account',
    `paid_amount`     decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '已收合计',
    `refund_amount`   decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '本账单已退合计',
    `bill_status`     tinyint        NOT NULL DEFAULT '1' COMMENT '账单状态（1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）',
    `bill_date`       date           NOT NULL COMMENT '账务归属日',
    `bill_time`       datetime                DEFAULT NULL COMMENT '结算生成时间',
    `bill_by_id`      bigint                  DEFAULT NULL COMMENT '结算人员工ID',
    `bill_by_name`    varchar(64)             DEFAULT NULL COMMENT '结算人姓名（快照）',
    `pay_time`        datetime                DEFAULT NULL COMMENT '收讫时间',
    `void_by_id`      bigint                  DEFAULT NULL COMMENT '作废操作人',
    `void_by_name`    varchar(64)             DEFAULT NULL COMMENT '作废操作人姓名（快照）',
    `void_time`       datetime                DEFAULT NULL COMMENT '作废时间',
    `void_reason`     varchar(200)            DEFAULT NULL COMMENT '作废原因（必填）',
    `orig_bill_id`    bigint                  DEFAULT NULL COMMENT '红冲指针',
    `close_reason`    varchar(200)            DEFAULT NULL COMMENT '结清说明',
    `create_by`       varchar(64)    NOT NULL,
    `create_by_id`    bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`       varchar(64)    NOT NULL,
    `update_by_id`    bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`        tinyint        NOT NULL DEFAULT '0',
    `remark`          varchar(500)            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bill_no` (`bill_no`),
    KEY               `idx_bill_patient` (`patient_id`,`bill_date`),
    KEY               `idx_bill_encounter` (`encounter_type`,`encounter_id`),
    KEY               `idx_bill_status_date` (`bill_status`,`bill_date`),
    KEY               `idx_bill_date` (`bill_date`),
    KEY               `idx_bill_by` (`bill_by_id`,`bill_time`),
    KEY               `idx_bill_pay_time` (`pay_time`,`bill_status`),
    KEY               `idx_orig_bill` (`orig_bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='结算账单';

-- ----------------------------
-- biz_settlement_bill_item  结算账单行
-- ----------------------------
CREATE TABLE `biz_settlement_bill_item`
(
    `id`              bigint         NOT NULL COMMENT '主键（雪花）',
    `bill_id`         bigint         NOT NULL COMMENT '账单ID',
    `bill_no`         varchar(32)    NOT NULL COMMENT '账单号（快照）',
    `fee_record_id`   bigint         NOT NULL COMMENT '来源记账行ID',
    `patient_id`      bigint         NOT NULL COMMENT '患者ID',
    `encounter_type`  tinyint        NOT NULL COMMENT '就诊类型',
    `encounter_id`    bigint         NOT NULL COMMENT '就诊标识',
    `dept_id`         bigint                  DEFAULT NULL COMMENT '费用归属科室',
    `dept_name`       varchar(100)            DEFAULT NULL COMMENT '科室名称（快照）',
    `item_type`       tinyint        NOT NULL COMMENT '项目类型',
    `item_code`       varchar(32)             DEFAULT NULL COMMENT '项目编码（快照）',
    `item_name`       varchar(200)   NOT NULL COMMENT '项目名称（快照）',
    `specification`   varchar(100)            DEFAULT NULL COMMENT '规格（快照）',
    `unit`            varchar(20)             DEFAULT NULL COMMENT '单位（快照）',
    `price`           decimal(10, 4) NOT NULL COMMENT '单价（快照）',
    `quantity`        decimal(10, 2) NOT NULL COMMENT '数量（快照）',
    `amount`          decimal(12, 2) NOT NULL COMMENT '应收金额',
    `discount_amount` decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '行级分摊优惠',
    `pool_amount`     decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '行级医保统筹',
    `account_amount`  decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '行级医保个账',
    `self_amount`     decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '行级个人自付',
    `catalog_type`    tinyint                 DEFAULT NULL COMMENT '医保目录类别（1-甲 2-乙 3-丙）',
    `create_by`       varchar(64)    NOT NULL,
    `create_by_id`    bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`       varchar(64)    NOT NULL,
    `update_by_id`    bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`        tinyint        NOT NULL DEFAULT '0',
    `remark`          varchar(500)            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bill_fee` (`bill_id`,`fee_record_id`),
    KEY               `idx_bitem_patient` (`patient_id`),
    KEY               `idx_bitem_fee` (`fee_record_id`),
    KEY               `idx_bitem_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='结算账单行';

-- ----------------------------
-- biz_payment_txn  支付资金流水
-- ----------------------------
CREATE TABLE `biz_payment_txn`
(
    `id`                    bigint         NOT NULL COMMENT '主键（雪花）',
    `txn_no`                varchar(32)    NOT NULL COMMENT '支付流水号',
    `bill_id`               bigint                  DEFAULT NULL COMMENT '结算账单ID',
    `bill_no`               varchar(32)             DEFAULT NULL COMMENT '账单号快照',
    `patient_id`            bigint         NOT NULL COMMENT '患者ID',
    `patient_no`            varchar(32)             DEFAULT NULL COMMENT '患者号（快照）',
    `patient_name`          varchar(50)    NOT NULL COMMENT '患者姓名（快照）',
    `encounter_type`        tinyint        NOT NULL COMMENT '就诊类型',
    `encounter_id`          bigint         NOT NULL COMMENT '就诊标识',
    `direction`             tinyint        NOT NULL DEFAULT '1' COMMENT '资金方向（1-收款 2-退款）',
    `pay_method`            tinyint        NOT NULL COMMENT '支付方式（1-现金 2-微信 3-支付宝 4-医保个人账户 5-院内余额 6-银行卡 7-转账）',
    `amount`                decimal(12, 2) NOT NULL COMMENT '金额',
    `txn_status`            tinyint        NOT NULL DEFAULT '1' COMMENT '流水状态（1-成功 2-已冲正）',
    `orig_txn_id`           bigint                  DEFAULT NULL COMMENT '退款/冲正指向的原收款流水ID',
    `source_type`           tinyint        NOT NULL COMMENT '流水来源',
    `refund_method`         tinyint                 DEFAULT NULL COMMENT '退费方式（1-原路退回 2-现金退回 3-余额退回）',
    `channel_txn_no`        varchar(64)             DEFAULT NULL COMMENT '渠道流水号',
    `cashier_id`            bigint         NOT NULL COMMENT '收银/退款人员工ID',
    `cashier_name`          varchar(50)             DEFAULT NULL COMMENT '操作人姓名',
    `txn_time`              datetime       NOT NULL COMMENT '交易时间',
    `txn_date`              date           NOT NULL COMMENT '交易归属日',
    `cashier_settlement_id` bigint                  DEFAULT NULL COMMENT '所属交班单ID',
    `insurance_cancelled`   tinyint        NOT NULL DEFAULT '0' COMMENT '本次退费是否已撤销医保报盘（0-不涉及 1-已发）',
    `reason`                varchar(500)            DEFAULT NULL COMMENT '退款/冲正原因',
    `apply_id`              bigint                  DEFAULT NULL COMMENT '来源退费申请ID',
    `apply_no`              varchar(32)             DEFAULT NULL COMMENT '来源退费申请号（快照）',
    `create_by`             varchar(64)    NOT NULL,
    `create_by_id`          bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`             varchar(64)    NOT NULL,
    `update_by_id`          bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`           datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`              tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志',
    `remark`                varchar(500)            DEFAULT NULL,
    `receipt_no`            varchar(32)             DEFAULT NULL COMMENT '收据号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_txn_no` (`txn_no`),
    KEY                     `idx_txn_bill` (`bill_id`,`direction`),
    KEY                     `idx_txn_patient` (`patient_id`,`txn_time`),
    KEY                     `idx_txn_cashier_time` (`cashier_id`,`txn_time`),
    KEY                     `idx_txn_date` (`txn_date`,`direction`),
    KEY                     `idx_txn_channel` (`channel_txn_no`),
    KEY                     `idx_txn_orig` (`orig_txn_id`),
    KEY                     `idx_txn_settlement` (`cashier_settlement_id`),
    KEY                     `idx_txn_apply` (`apply_id`),
    KEY                     `idx_txn_encounter` (`encounter_type`,`encounter_id`,`direction`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付资金流水';

-- ----------------------------
-- biz_pay_order  患者端统一支付单
-- ----------------------------
CREATE TABLE `biz_pay_order`
(
    `id`           bigint         NOT NULL COMMENT '主键（雪花）',
    `pay_no`       varchar(32)    NOT NULL COMMENT '支付单号',
    `biz_type`     tinyint        NOT NULL COMMENT '业务类型（1-门诊缴费 2-挂号费 3-住院押金）',
    `biz_id`       bigint         NOT NULL COMMENT '业务单ID（收费单ID/挂号单ID/入院ID）',
    `patient_id`   bigint                  DEFAULT NULL COMMENT '患者ID',
    `patient_name` varchar(64)             DEFAULT NULL COMMENT '患者姓名',
    `amount`       decimal(10, 2) NOT NULL COMMENT '金额（元）',
    `channel`      tinyint        NOT NULL DEFAULT '1' COMMENT '支付渠道',
    `pay_status`   tinyint        NOT NULL DEFAULT '0' COMMENT '支付状态（0-待支付 1-已支付 2-已关闭 3-已退款）',
    `out_trade_no` varchar(64)             DEFAULT NULL COMMENT '渠道交易号',
    `pay_time`     datetime                DEFAULT NULL COMMENT '支付时间',
    `refund_time`  datetime                DEFAULT NULL COMMENT '退款时间',
    `create_by`    varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id` bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id` bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`       varchar(500)            DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pay_no` (`pay_no`),
    KEY            `idx_biz` (`biz_type`,`biz_id`),
    KEY            `idx_patient` (`patient_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='患者端统一支付单';

-- ----------------------------
-- biz_fund_account  资金账户
-- ----------------------------
CREATE TABLE `biz_fund_account`
(
    `id`             bigint         NOT NULL COMMENT '主键（雪花）',
    `owner_type`     tinyint        NOT NULL COMMENT '账户主体（1-患者 2-住院就诊次）',
    `owner_id`       bigint         NOT NULL COMMENT '主体ID',
    `patient_id`     bigint         NOT NULL COMMENT '患者ID',
    `patient_no`     varchar(32)             DEFAULT NULL COMMENT '患者号（快照）',
    `patient_name`   varchar(50)             DEFAULT NULL COMMENT '患者姓名（快照）',
    `balance`        decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '余额',
    `version`        bigint         NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
    `total_recharge` decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '累计充值',
    `total_consume`  decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '累计扣用',
    `account_status` tinyint        NOT NULL DEFAULT '1' COMMENT '账户状态（1-正常 2-冻结）',
    `last_txn_time`  datetime                DEFAULT NULL COMMENT '最后一笔流水时间',
    `create_by`      varchar(64)    NOT NULL,
    `create_by_id`   bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`      varchar(64)    NOT NULL,
    `update_by_id`   bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`       tinyint        NOT NULL DEFAULT '0',
    `remark`         varchar(500)            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account_owner` (`owner_type`,`owner_id`),
    KEY              `idx_account_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资金账户';

-- ----------------------------
-- biz_fund_account_txn  资金账户流水
-- ----------------------------
CREATE TABLE `biz_fund_account_txn`
(
    `id`             bigint         NOT NULL COMMENT '主键（雪花）',
    `txn_no`         varchar(32)    NOT NULL COMMENT '账户流水号',
    `account_id`     bigint         NOT NULL COMMENT '账户ID',
    `patient_id`     bigint         NOT NULL COMMENT '患者ID',
    `owner_type`     tinyint        NOT NULL COMMENT '账户主体',
    `owner_id`       bigint         NOT NULL COMMENT '主体ID（冗余）',
    `txn_type`       tinyint        NOT NULL COMMENT '流水类型',
    `amount`         decimal(12, 2) NOT NULL COMMENT '变动金额',
    `balance_after`  decimal(12, 2) NOT NULL COMMENT '本笔后余额快照',
    `admission_id`   bigint                  DEFAULT NULL COMMENT '入院ID',
    `bill_id`        bigint                  DEFAULT NULL COMMENT '关联账单ID',
    `payment_txn_id` bigint                  DEFAULT NULL COMMENT '关联支付流水ID',
    `pay_method`     tinyint                 DEFAULT NULL COMMENT '充值/退款走的渠道',
    `channel_txn_no` varchar(64)             DEFAULT NULL COMMENT '渠道流水号',
    `operator_id`    bigint                  DEFAULT NULL COMMENT '操作人员工ID',
    `operator_name`  varchar(50)             DEFAULT NULL COMMENT '操作人姓名（快照）',
    `txn_time`       datetime       NOT NULL COMMENT '发生时间',
    `txn_status`     tinyint        NOT NULL DEFAULT '1' COMMENT '状态（1-成功 2-已冲正）',
    `orig_txn_id`    bigint                  DEFAULT NULL COMMENT '冲正指向的原流水ID',
    `create_by`      varchar(64)    NOT NULL,
    `create_by_id`   bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`      varchar(64)    NOT NULL,
    `update_by_id`   bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`       tinyint        NOT NULL DEFAULT '0' COMMENT '流水不提供删除接口',
    `remark`         varchar(500)            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_acct_txn_no` (`txn_no`),
    KEY              `idx_acct_txn_account` (`account_id`,`txn_time`),
    KEY              `idx_acct_txn_patient` (`patient_id`,`txn_time`),
    KEY              `idx_acct_txn_admission` (`admission_id`,`txn_time`),
    KEY              `idx_acct_txn_payment` (`payment_txn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资金账户流水';

-- ----------------------------
-- biz_refund_apply  退费申请单
-- ----------------------------
CREATE TABLE `biz_refund_apply`
(
    `id`              bigint         NOT NULL COMMENT '主键ID',
    `refund_apply_no` varchar(32)    NOT NULL COMMENT '退费申请号',
    `bill_id`         bigint                  DEFAULT NULL COMMENT '原结算账单ID',
    `bill_no`         varchar(32)             DEFAULT NULL COMMENT '结算账单号',
    `patient_id`      bigint         NOT NULL COMMENT '患者ID',
    `patient_no`      varchar(32)             DEFAULT NULL COMMENT '患者号',
    `patient_name`    varchar(50)             DEFAULT NULL COMMENT '患者姓名',
    `refund_type`     tinyint        NOT NULL COMMENT '退费类型（1-退药 2-退检查 3-退检验 4-退治疗 5-全部退费）',
    `refund_reason`   varchar(500)   NOT NULL COMMENT '退费原因',
    `refund_amount`   decimal(10, 2) NOT NULL COMMENT '退费金额',
    `apply_status`    tinyint        NOT NULL DEFAULT '1' COMMENT '申请状态（1-待审核 2-审核通过 3-审核驳回 4-已退费 5-已作废）',
    `apply_by`        varchar(64)             DEFAULT NULL COMMENT '申请人',
    `apply_time`      datetime                DEFAULT NULL COMMENT '申请时间',
    `auditor_id`      bigint                  DEFAULT NULL COMMENT '审核人ID',
    `auditor_name`    varchar(50)             DEFAULT NULL COMMENT '审核人姓名',
    `audit_time`      datetime                DEFAULT NULL COMMENT '审核时间',
    `audit_remark`    varchar(500)            DEFAULT NULL COMMENT '审核意见',
    `refund_by`       varchar(64)             DEFAULT NULL COMMENT '退费人',
    `refund_time`     datetime                DEFAULT NULL COMMENT '退费时间',
    `cancel_by`       varchar(64)             DEFAULT NULL COMMENT '作废人姓名（快照）',
    `cancel_time`     datetime                DEFAULT NULL COMMENT '作废时间',
    `cancel_reason`   varchar(200)            DEFAULT NULL COMMENT '作废原因',
    `create_by`       varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`    bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`    bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500)            DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_apply_no` (`refund_apply_no`),
    KEY               `idx_charge_id` (`bill_id`),
    KEY               `idx_patient_id` (`patient_id`),
    KEY               `idx_apply_status` (`apply_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退费申请单';

-- ----------------------------
-- biz_invoice  发票
-- ----------------------------
CREATE TABLE `biz_invoice`
(
    `id`              bigint         NOT NULL COMMENT '主键ID',
    `invoice_no`      varchar(32)    NOT NULL COMMENT '发票号',
    `invoice_type`    tinyint        NOT NULL DEFAULT '1' COMMENT '发票类型（1-普通发票 2-电子发票 3-数电发票）',
    `charge_id`       bigint                  DEFAULT NULL COMMENT '旧收费单ID',
    `bill_id`         bigint                  DEFAULT NULL COMMENT '结算账单ID',
    `bill_no`         varchar(32)             DEFAULT NULL COMMENT '结算账单号（快照）',
    `orig_invoice_id` bigint                  DEFAULT NULL COMMENT '红冲链',
    `charge_no`       varchar(32)             DEFAULT NULL COMMENT '收费单号',
    `patient_id`      bigint         NOT NULL COMMENT '患者ID',
    `patient_no`      varchar(32)             DEFAULT NULL COMMENT '患者号',
    `patient_name`    varchar(50)             DEFAULT NULL COMMENT '患者姓名',
    `total_amount`    decimal(10, 2) NOT NULL COMMENT '发票金额',
    `invoice_status`  tinyint        NOT NULL DEFAULT '1' COMMENT '发票状态（1-已开具 2-已打印 3-已作废 4-已红冲换开）',
    `invoice_time`    datetime                DEFAULT NULL COMMENT '开票时间',
    `print_time`      datetime                DEFAULT NULL COMMENT '打印时间',
    `void_time`       datetime                DEFAULT NULL COMMENT '作废时间',
    `void_reason`     varchar(200)            DEFAULT NULL COMMENT '作废原因',
    `electronic_url`  varchar(500)            DEFAULT NULL COMMENT '电子发票地址',
    `create_by`       varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`    bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`    bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`        tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    `remark`          varchar(500)            DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invoice_no` (`invoice_no`),
    KEY               `idx_charge_id` (`charge_id`),
    KEY               `idx_patient_id` (`patient_id`),
    KEY               `idx_invoice_status` (`invoice_status`),
    KEY               `idx_invoice_bill` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发票';

-- ----------------------------
-- biz_cashier_settlement  收费员班结单
-- ----------------------------
CREATE TABLE `biz_cashier_settlement`
(
    `id`                 bigint         NOT NULL COMMENT '主键ID',
    `settlement_no`      varchar(32)    NOT NULL COMMENT '交班单号',
    `cashier_id`         bigint         NOT NULL COMMENT '收费员工号',
    `cashier_name`       varchar(50)    NOT NULL COMMENT '收费员姓名',
    `shift_type`         tinyint        NOT NULL DEFAULT '3' COMMENT '班次（1-白班 2-夜班 3-其他）',
    `period_begin`       datetime       NOT NULL COMMENT '统计区间起',
    `period_end`         datetime       NOT NULL COMMENT '统计区间止',
    `day_settlement_id`  bigint                  DEFAULT NULL COMMENT '所属院级日结单ID',
    `charge_count`       int            NOT NULL DEFAULT '0' COMMENT '收费笔数',
    `charge_amount`      decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '收费金额',
    `refund_count`       int            NOT NULL DEFAULT '0' COMMENT '退费笔数',
    `refund_amount`      decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '退费金额',
    `net_amount`         decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '净额 = 收费金额 - 退费金额',
    `cash_amount`        decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '现金',
    `wechat_amount`      decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '微信',
    `alipay_amount`      decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '支付宝',
    `insurance_amount`   decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '医保',
    `balance_amount`     decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '余额',
    `unknown_pay_amount` decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '支付方式为空/未知的金额',
    `pool_amount`        decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '医保统筹额',
    `invoice_count`      int            NOT NULL DEFAULT '0' COMMENT '本时段开票张数',
    `invoice_void_count` int            NOT NULL DEFAULT '0' COMMENT '本时段作废张数',
    `handin_cash`        decimal(12, 2)          DEFAULT NULL COMMENT '实交现金',
    `cash_diff`          decimal(12, 2)          DEFAULT NULL COMMENT '现金差异 = 实交现金 - 系统现金',
    `diff_reason`        varchar(500)            DEFAULT NULL COMMENT '差异说明',
    `settle_status`      tinyint        NOT NULL DEFAULT '1' COMMENT '状态（1-已交班待日结 2-已日结 3-已审核）',
    `audit_by`           varchar(64)             DEFAULT NULL COMMENT '审核人',
    `audit_time`         datetime                DEFAULT NULL COMMENT '审核时间',
    `audit_remark`       varchar(500)            DEFAULT NULL COMMENT '审核意见',
    `create_by`          varchar(64)    NOT NULL,
    `create_by_id`       bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`          varchar(64)    NOT NULL,
    `update_by_id`       bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`           tinyint        NOT NULL DEFAULT '0',
    `remark`             varchar(500)            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_settlement_no` (`settlement_no`),
    KEY                  `idx_cashier_end` (`cashier_id`,`period_end`),
    KEY                  `idx_day_settlement` (`day_settlement_id`),
    KEY                  `idx_settle_status` (`settle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收费员班结单';

-- ----------------------------
-- biz_day_settlement  院级日结单
-- ----------------------------
CREATE TABLE `biz_day_settlement`
(
    `id`                  bigint         NOT NULL COMMENT '主键ID',
    `settlement_no`       varchar(32)    NOT NULL COMMENT '日结单号',
    `settle_date`         date           NOT NULL COMMENT '日结日期',
    `shift_count`         int            NOT NULL DEFAULT '0' COMMENT '纳入的班结单数',
    `charge_count`        int            NOT NULL DEFAULT '0' COMMENT '收费笔数',
    `bill_count`          int            NOT NULL DEFAULT '0' COMMENT '当日结算账单张数',
    `charge_amount`       decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '收费金额',
    `refund_count`        int            NOT NULL DEFAULT '0' COMMENT '退费笔数',
    `refund_amount`       decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '退费金额',
    `net_amount`          decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '净额 = 收费 - 退费',
    `cash_amount`         decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '现金',
    `wechat_amount`       decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '微信',
    `alipay_amount`       decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '支付宝',
    `insurance_amount`    decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '医保',
    `balance_amount`      decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '余额',
    `unknown_pay_amount`  decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '支付方式未知金额',
    `pool_amount`         decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '医保统筹记账额',
    `invoice_count`       int            NOT NULL DEFAULT '0' COMMENT '开票张数',
    `invoice_void_count`  int            NOT NULL DEFAULT '0' COMMENT '作废张数',
    `detail_amount`       decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '独立复算',
    `dept_count`          int            NOT NULL DEFAULT '0' COMMENT '有科室归属的科室数',
    `dept_amount`         decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '已归属科室的明细金额合计',
    `unattributed_amount` decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '无科室归属的明细金额合计',
    `unassigned_count`    int            NOT NULL DEFAULT '0' COMMENT '未纳入任何班结单的已收费笔数',
    `unassigned_amount`   decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '未纳入班结的金额',
    `reconcile_status`    tinyint        NOT NULL DEFAULT '1' COMMENT '对账结论（1-已平 2-有差异）',
    `diff_amount`         decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '最大差异金额',
    `diff_detail`         text COMMENT '差异明细',
    `settle_status`       tinyint        NOT NULL DEFAULT '1' COMMENT '状态（1-待审核 2-已审核）',
    `settle_by`           varchar(64)             DEFAULT NULL COMMENT '日结人',
    `settle_time`         datetime                DEFAULT NULL COMMENT '日结时间',
    `audit_by`            varchar(64)             DEFAULT NULL COMMENT '审核人',
    `audit_time`          datetime                DEFAULT NULL COMMENT '审核时间',
    `audit_remark`        varchar(500)            DEFAULT NULL COMMENT '审核意见',
    `create_by`           varchar(64)    NOT NULL,
    `create_by_id`        bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`           varchar(64)    NOT NULL,
    `update_by_id`        bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`         datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`            tinyint        NOT NULL DEFAULT '0',
    `remark`              varchar(500)            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_settle_date` (`settle_date`),
    UNIQUE KEY `uk_settlement_no` (`settlement_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='院级日结单';

-- ----------------------------
-- biz_pay_channel_bill  支付渠道对账流水
-- ----------------------------
CREATE TABLE `biz_pay_channel_bill`
(
    `id`               bigint         NOT NULL COMMENT '主键（雪花）',
    `channel`          tinyint        NOT NULL COMMENT '支付渠道（2-微信 3-支付宝 6-银行卡）',
    `bill_date`        date           NOT NULL COMMENT '账单日期',
    `channel_trade_no` varchar(64)    NOT NULL COMMENT '渠道流水号',
    `trade_time`       datetime       NOT NULL COMMENT '渠道交易时间',
    `amount`           decimal(10, 2) NOT NULL COMMENT '渠道侧金额',
    `import_way`       tinyint        NOT NULL DEFAULT '1' COMMENT '来源（1-渠道拉取 2-手工登记）',
    `local_txn_no`     varchar(64)             DEFAULT NULL COMMENT '勾对的本地支付流水号',
    `local_txn_id`     bigint                  DEFAULT NULL COMMENT '勾对的本地支付流水ID',
    `txn_direction`    tinyint                 DEFAULT NULL COMMENT '勾对流水方向快照（1-收款 2-退款）',
    `match_status`     tinyint        NOT NULL DEFAULT '0' COMMENT '勾对状态（0-待勾对 1-已勾对 2-长款 3-短款）',
    `match_time`       datetime                DEFAULT NULL COMMENT '勾对时间',
    `matched_by_id`    bigint                  DEFAULT NULL COMMENT '勾对人员工ID',
    `matched_by_name`  varchar(50)             DEFAULT NULL COMMENT '勾对人姓名（快照）',
    `diff_amount`      decimal(10, 2)          DEFAULT NULL COMMENT '勾对差额（渠道-本地）',
    `handle_remark`    varchar(500)            DEFAULT NULL COMMENT '长款/短款处理说明',
    `import_batch_no`  varchar(40)             DEFAULT NULL COMMENT '导入批次号',
    `create_by`        varchar(64)    NOT NULL,
    `create_by_id`     bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by`        varchar(64)    NOT NULL,
    `update_by_id`     bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`      datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `del_flag`         tinyint        NOT NULL DEFAULT '0',
    `remark`           varchar(500)            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_channel_trade` (`channel`,`channel_trade_no`),
    UNIQUE KEY `uk_bill_txn` (`local_txn_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付渠道对账流水';

-- ----------------------------
-- biz_inpatient_settlement  住院结算单
-- ----------------------------
CREATE TABLE `biz_inpatient_settlement`
(
    `id`                 bigint         NOT NULL COMMENT '结算单ID',
    `settlement_no`      varchar(32)    NOT NULL COMMENT '结算单号（唯一）',
    `admission_id`       bigint         NOT NULL COMMENT '入院ID',
    `patient_id`         bigint         NOT NULL COMMENT '患者ID',
    `patient_no`         varchar(32)             DEFAULT NULL COMMENT '患者号',
    `patient_name`       varchar(50)             DEFAULT NULL COMMENT '患者姓名',
    `charge_count`       int            NOT NULL DEFAULT '0' COMMENT '本次结算涵盖的费用单数量',
    `total_amount`       decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '住院总费用',
    `insurance_amount`   decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '统筹支付',
    `patient_pay_amount` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '患者应付',
    `prepay_balance`     decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '结算时预交金余额',
    `refund_amount`      decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '应退患者',
    `arrears_amount`     decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '欠费',
    `settle_status`      tinyint        NOT NULL DEFAULT '1' COMMENT '结算状态（1-已结清 2-欠费 3-已作废）',
    `settle_mode`        tinyint                 DEFAULT NULL COMMENT '结算方式（1-自费 2-医保）',
    `insurance_type`     varchar(32)             DEFAULT NULL COMMENT '医保类型',
    `settle_time`        datetime                DEFAULT NULL COMMENT '结算时间',
    `settle_by`          bigint                  DEFAULT NULL COMMENT '结算人（员工ID）',
    `settle_by_name`     varchar(50)             DEFAULT NULL COMMENT '结算人姓名',
    `remark`             varchar(500)            DEFAULT NULL COMMENT '备注',
    `create_by`          varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`       bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`       bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`           tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_inpatient_settlement_no` (`settlement_no`),
    KEY                  `idx_biz_ips_admission` (`admission_id`),
    KEY                  `idx_biz_ips_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='住院结算单';

-- ----------------------------
-- biz_prepay  住院预交金流水
-- ----------------------------
CREATE TABLE `biz_prepay`
(
    `id`            bigint         NOT NULL COMMENT '预交金流水ID',
    `prepay_no`     varchar(32)    NOT NULL COMMENT '预交金单号（唯一）',
    `admission_id`  bigint         NOT NULL COMMENT '入院ID',
    `patient_id`    bigint         NOT NULL COMMENT '患者ID',
    `patient_no`    varchar(32)             DEFAULT NULL COMMENT '患者号',
    `patient_name`  varchar(50)             DEFAULT NULL COMMENT '患者姓名',
    `prepay_type`   tinyint        NOT NULL COMMENT '流水类型（1-充值 2-退款）',
    `amount`        decimal(10, 2) NOT NULL COMMENT '金额',
    `balance_after` decimal(10, 2) NOT NULL COMMENT '本笔之后的余额快照',
    `pay_method`    tinyint        NOT NULL DEFAULT '1' COMMENT '支付方式（1-现金 2-微信 3-支付宝 4-银行卡 5-转账）',
    `receipt_no`    varchar(50)             DEFAULT NULL COMMENT '票据号',
    `pay_time`      datetime       NOT NULL COMMENT '收/退时间',
    `operator_id`   bigint                  DEFAULT NULL COMMENT '操作人',
    `operator_name` varchar(50)             DEFAULT NULL COMMENT '操作人姓名',
    `remark`        varchar(500)            DEFAULT NULL COMMENT '备注',
    `create_by`     varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`  bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`  bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`      tinyint        NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_prepay_no` (`prepay_no`),
    KEY             `idx_biz_prepay_admission` (`admission_id`,`pay_time`),
    KEY             `idx_biz_prepay_patient` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='住院预交金流水';

-- ----------------------------
-- biz_arrears_policy  住院欠费管控策略
-- ----------------------------
CREATE TABLE `biz_arrears_policy`
(
    `id`           bigint      NOT NULL COMMENT '策略ID',
    `warn_line`    decimal(12, 2)       DEFAULT NULL COMMENT '预警线（元）',
    `stop_line`    decimal(12, 2)       DEFAULT NULL COMMENT '停费线（元）',
    `stop_enabled` tinyint     NOT NULL DEFAULT '0' COMMENT '停费管控开关（0-关 1-开）',
    `stop_classes` varchar(64) NOT NULL DEFAULT '2,3,4' COMMENT '被拦截的医嘱类别（2-检查 3-检验 4-治疗）',
    `remark`       varchar(500)         DEFAULT NULL COMMENT '备注',
    `update_by`    varchar(64) NOT NULL COMMENT '更新人',
    `update_by_id` bigint               DEFAULT NULL COMMENT '更新人ID',
    `update_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`    varchar(64) NOT NULL COMMENT '创建人',
    `create_by_id` bigint               DEFAULT NULL COMMENT '创建人ID',
    `create_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='住院欠费管控策略';

-- ----------------------------
-- biz_stat_daily  日统计汇总
-- ----------------------------
CREATE TABLE `biz_stat_daily`
(
    `stat_id`            bigint         NOT NULL COMMENT '统计ID',
    `stat_date`          date           NOT NULL COMMENT '统计日期',
    `dept_id`            bigint                  DEFAULT NULL COMMENT '科室ID',
    `visit_count`        int            NOT NULL DEFAULT '0' COMMENT '门诊量',
    `charge_amount`      decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '收费金额',
    `prescription_count` int            NOT NULL DEFAULT '0' COMMENT '处方数',
    `refund_amount`      decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '退费金额',
    `drug_ratio`         decimal(5, 2)           DEFAULT NULL COMMENT '药品占比(%)',
    `remark`             varchar(500)            DEFAULT NULL COMMENT '备注',
    `create_by`          varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`       bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`          varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`       bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`        datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`stat_id`),
    UNIQUE KEY `uk_stat_date_dept` (`stat_date`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='日统计汇总';

-- ----------------------------
-- biz_stat_dept  科室统计汇总
-- ----------------------------
CREATE TABLE `biz_stat_dept`
(
    `stat_id`        bigint         NOT NULL COMMENT '统计ID',
    `stat_date`      date           NOT NULL COMMENT '统计日期',
    `dept_id`        bigint         NOT NULL COMMENT '科室ID',
    `visit_count`    int            NOT NULL DEFAULT '0' COMMENT '门诊量',
    `charge_amount`  decimal(12, 2) NOT NULL DEFAULT '0.00' COMMENT '科室收入',
    `drug_ratio`     decimal(5, 2)           DEFAULT NULL COMMENT '药品占比(%)',
    `avg_visit_time` int                     DEFAULT NULL COMMENT '平均就诊时长(分钟)',
    `remark`         varchar(500)            DEFAULT NULL COMMENT '备注',
    `create_by`      varchar(64)    NOT NULL COMMENT '创建人',
    `create_by_id`   bigint                  DEFAULT NULL COMMENT '创建人ID',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`      varchar(64)    NOT NULL COMMENT '更新人',
    `update_by_id`   bigint                  DEFAULT NULL COMMENT '更新人ID',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`stat_id`),
    UNIQUE KEY `uk_stat_date_dept` (`stat_date`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='科室统计汇总';
