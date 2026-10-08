-- ============================================================
-- 领域 19 收费四层（记账·结算·支付·票据）（本域 17 表 + 上游参照 4 表 / 47 条关系）
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


-- biz_fee_record  费用记账流水
CREATE TABLE `biz_fee_record` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `fee_no` varchar(32) NOT NULL COMMENT '记账流水号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `encounter_type` tinyint NOT NULL COMMENT '就诊类型（1-门诊 2-住院）',
  `encounter_id` bigint NOT NULL COMMENT '就诊标识',
  `encounter_no` varchar(32) COMMENT '就诊标识单号',
  `dept_id` bigint COMMENT '费用归属科室',
  `dept_name` varchar(100) COMMENT '科室名称',
  `doctor_id` bigint COMMENT '开单/执行人员工ID',
  `doctor_name` varchar(50) COMMENT '开单人姓名',
  `item_type` tinyint NOT NULL COMMENT '项目类型（1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗 8-耗材）',
  `item_code` varchar(32) COMMENT '项目/药品编码',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `catalog_type` tinyint COMMENT '医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）',
  `price` decimal(10,4) NOT NULL COMMENT '单价',
  `quantity` decimal(10,2) NOT NULL COMMENT '数量',
  `amount` decimal(12,2) NOT NULL COMMENT '金额=单价×数量',
  `fee_status` tinyint NOT NULL DEFAULT 1 COMMENT '记账状态（1-待结算 2-已锁定 3-已结算 4-已红冲）',
  `source_type` tinyint NOT NULL COMMENT '费用来源',
  `source_id` bigint COMMENT '来源单据ID（处方明细ID/申请ID/医嘱ID…）',
  `source_no` varchar(64) COMMENT '来源单据号',
  `orig_fee_id` bigint COMMENT '红冲双向指针',
  `refunded_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计已冲金额',
  `book_time` datetime NOT NULL COMMENT '记账时间',
  `book_by_id` bigint COMMENT '记账人员工ID',
  `book_by_name` varchar(64) COMMENT '记账人姓名',
  `bill_id` bigint COMMENT '所属结算账单ID',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fee_no` (`fee_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用记账流水';

-- biz_settlement_bill  结算账单
CREATE TABLE `biz_settlement_bill` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `bill_no` varchar(32) NOT NULL COMMENT '账单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `encounter_type` tinyint NOT NULL COMMENT '就诊类型（1-门诊 2-住院）',
  `encounter_id` bigint NOT NULL COMMENT '就诊标识',
  `encounter_no` varchar(32) COMMENT '就诊标识单号',
  `bill_type` tinyint NOT NULL DEFAULT 2 COMMENT '账单类型（1-挂号费结算 2-门诊诊间结算 3-住院中途结算 4-出院结算）',
  `fee_count` int NOT NULL DEFAULT 0 COMMENT '纳入本账单的记账行数',
  `total_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '应收合计',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '院内优惠/抹零',
  `settlement_mode` tinyint NOT NULL DEFAULT 1 COMMENT '结算方式（1-自费 2-医保）',
  `insurance_type` varchar(32) COMMENT '医保类型',
  `pool_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '医保统筹支付',
  `account_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '医保个人账户支付',
  `self_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '个人自费',
  `payable_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '患者应缴 = total - discount - pool - account',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '已收合计',
  `refund_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '本账单已退合计',
  `bill_status` tinyint NOT NULL DEFAULT 1 COMMENT '账单状态（1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）',
  `bill_date` date NOT NULL COMMENT '账务归属日',
  `bill_time` datetime COMMENT '结算生成时间',
  `bill_by_id` bigint COMMENT '结算人员工ID',
  `bill_by_name` varchar(64) COMMENT '结算人姓名',
  `pay_time` datetime COMMENT '收讫时间',
  `void_by_id` bigint COMMENT '作废操作人',
  `void_by_name` varchar(64) COMMENT '作废操作人姓名',
  `void_time` datetime COMMENT '作废时间',
  `void_reason` varchar(200) COMMENT '作废原因（必填）',
  `orig_bill_id` bigint COMMENT '红冲指针',
  `close_reason` varchar(200) COMMENT '结清说明',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_no` (`bill_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算账单';

-- biz_settlement_bill_item  结算账单行
CREATE TABLE `biz_settlement_bill_item` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `bill_id` bigint NOT NULL COMMENT '账单ID',
  `bill_no` varchar(32) NOT NULL COMMENT '账单号',
  `fee_record_id` bigint NOT NULL COMMENT '来源记账行ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `encounter_type` tinyint NOT NULL COMMENT '就诊类型',
  `encounter_id` bigint NOT NULL COMMENT '就诊标识',
  `dept_id` bigint COMMENT '费用归属科室',
  `dept_name` varchar(100) COMMENT '科室名称',
  `item_type` tinyint NOT NULL COMMENT '项目类型',
  `item_code` varchar(32) COMMENT '项目编码',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `price` decimal(10,4) NOT NULL COMMENT '单价',
  `quantity` decimal(10,2) NOT NULL COMMENT '数量',
  `amount` decimal(12,2) NOT NULL COMMENT '应收金额',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '行级分摊优惠',
  `pool_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '行级医保统筹',
  `account_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '行级医保个账',
  `self_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '行级个人自付',
  `catalog_type` tinyint COMMENT '医保目录类别（1-甲 2-乙 3-丙）',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_fee` (`bill_id`, `fee_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算账单行';

-- biz_payment_txn  支付资金流水
CREATE TABLE `biz_payment_txn` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `txn_no` varchar(32) NOT NULL COMMENT '支付流水号',
  `bill_id` bigint COMMENT '结算账单ID',
  `bill_no` varchar(32) COMMENT '账单号快照',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `encounter_type` tinyint NOT NULL COMMENT '就诊类型',
  `encounter_id` bigint NOT NULL COMMENT '就诊标识',
  `direction` tinyint NOT NULL DEFAULT 1 COMMENT '资金方向（1-收款 2-退款）',
  `pay_method` tinyint NOT NULL COMMENT '支付方式（1-现金 2-微信 3-支付宝 4-医保个人账户 5-院内余额 6-银行卡 7-转账）',
  `amount` decimal(12,2) NOT NULL COMMENT '金额',
  `txn_status` tinyint NOT NULL DEFAULT 1 COMMENT '流水状态（1-成功 2-已冲正）',
  `orig_txn_id` bigint COMMENT '退款/冲正指向的原收款流水ID',
  `source_type` tinyint NOT NULL COMMENT '流水来源',
  `refund_method` tinyint COMMENT '退费方式（1-原路退回 2-现金退回 3-余额退回）',
  `channel_txn_no` varchar(64) COMMENT '渠道流水号',
  `cashier_id` bigint NOT NULL COMMENT '收银/退款人员工ID',
  `cashier_name` varchar(50) COMMENT '操作人姓名',
  `txn_time` datetime NOT NULL COMMENT '交易时间',
  `txn_date` date NOT NULL COMMENT '交易归属日',
  `cashier_settlement_id` bigint COMMENT '所属交班单ID',
  `insurance_cancelled` tinyint NOT NULL DEFAULT 0 COMMENT '本次退费是否已撤销医保报盘（0-不涉及 1-已发）',
  `reason` varchar(500) COMMENT '退款/冲正原因',
  `apply_id` bigint COMMENT '来源退费申请ID',
  `apply_no` varchar(32) COMMENT '来源退费申请号',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志',
  `remark` varchar(500),
  `receipt_no` varchar(32) COMMENT '收据号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_txn_no` (`txn_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付资金流水';

-- biz_pay_order  患者端统一支付单
CREATE TABLE `biz_pay_order` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `pay_no` varchar(32) NOT NULL COMMENT '支付单号',
  `biz_type` tinyint NOT NULL COMMENT '业务类型（1-门诊缴费 2-挂号费 3-住院押金）',
  `biz_id` bigint NOT NULL COMMENT '业务单ID（收费单ID/挂号单ID/入院ID）',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(64) COMMENT '患者姓名',
  `amount` decimal(10,2) NOT NULL COMMENT '金额（元）',
  `channel` tinyint NOT NULL DEFAULT 1 COMMENT '支付渠道',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态（0-待支付 1-已支付 2-已关闭 3-已退款）',
  `out_trade_no` varchar(64) COMMENT '渠道交易号',
  `pay_time` datetime COMMENT '支付时间',
  `refund_time` datetime COMMENT '退款时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者端统一支付单';

-- biz_fund_account  资金账户
CREATE TABLE `biz_fund_account` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `owner_type` tinyint NOT NULL COMMENT '账户主体（1-患者 2-住院就诊次）',
  `owner_id` bigint NOT NULL COMMENT '主体ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `balance` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `total_recharge` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计充值',
  `total_consume` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计扣用',
  `account_status` tinyint NOT NULL DEFAULT 1 COMMENT '账户状态（1-正常 2-冻结）',
  `last_txn_time` datetime COMMENT '最后一笔流水时间',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_owner` (`owner_type`, `owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金账户';

-- biz_fund_account_txn  资金账户流水
CREATE TABLE `biz_fund_account_txn` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `txn_no` varchar(32) NOT NULL COMMENT '账户流水号',
  `account_id` bigint NOT NULL COMMENT '账户ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `owner_type` tinyint NOT NULL COMMENT '账户主体',
  `owner_id` bigint NOT NULL COMMENT '主体ID（冗余）',
  `txn_type` tinyint NOT NULL COMMENT '流水类型',
  `amount` decimal(12,2) NOT NULL COMMENT '变动金额',
  `balance_after` decimal(12,2) NOT NULL COMMENT '本笔后余额快照',
  `admission_id` bigint COMMENT '入院ID',
  `bill_id` bigint COMMENT '关联账单ID',
  `payment_txn_id` bigint COMMENT '关联支付流水ID',
  `pay_method` tinyint COMMENT '充值/退款走的渠道',
  `channel_txn_no` varchar(64) COMMENT '渠道流水号',
  `operator_id` bigint COMMENT '操作人员工ID',
  `operator_name` varchar(50) COMMENT '操作人姓名',
  `txn_time` datetime NOT NULL COMMENT '发生时间',
  `txn_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-成功 2-已冲正）',
  `orig_txn_id` bigint COMMENT '冲正指向的原流水ID',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '流水不提供删除接口',
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_acct_txn_no` (`txn_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金账户流水';

-- biz_refund_apply  退费申请单
CREATE TABLE `biz_refund_apply` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `refund_apply_no` varchar(32) NOT NULL COMMENT '退费申请号',
  `bill_id` bigint COMMENT '原结算账单ID',
  `bill_no` varchar(32) COMMENT '结算账单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `refund_type` tinyint NOT NULL COMMENT '退费类型（1-退药 2-退检查 3-退检验 4-退治疗 5-全部退费）',
  `refund_reason` varchar(500) NOT NULL COMMENT '退费原因',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '退费金额',
  `apply_status` tinyint NOT NULL DEFAULT 1 COMMENT '申请状态（1-待审核 2-审核通过 3-审核驳回 4-已退费 5-已作废）',
  `apply_by` varchar(64) COMMENT '申请人',
  `apply_time` datetime COMMENT '申请时间',
  `auditor_id` bigint COMMENT '审核人ID',
  `auditor_name` varchar(50) COMMENT '审核人姓名',
  `audit_time` datetime COMMENT '审核时间',
  `audit_remark` varchar(500) COMMENT '审核意见',
  `refund_by` varchar(64) COMMENT '退费人',
  `refund_time` datetime COMMENT '退费时间',
  `cancel_by` varchar(64) COMMENT '作废人姓名',
  `cancel_time` datetime COMMENT '作废时间',
  `cancel_reason` varchar(200) COMMENT '作废原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_apply_no` (`refund_apply_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退费申请单';

-- biz_invoice  发票
CREATE TABLE `biz_invoice` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `invoice_no` varchar(32) NOT NULL COMMENT '发票号',
  `invoice_type` tinyint NOT NULL DEFAULT 1 COMMENT '发票类型（1-普通发票 2-电子发票 3-数电发票）',
  `charge_id` bigint COMMENT '旧收费单ID',
  `bill_id` bigint COMMENT '结算账单ID',
  `bill_no` varchar(32) COMMENT '结算账单号',
  `orig_invoice_id` bigint COMMENT '红冲链',
  `charge_no` varchar(32) COMMENT '收费单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `total_amount` decimal(10,2) NOT NULL COMMENT '发票金额',
  `invoice_status` tinyint NOT NULL DEFAULT 1 COMMENT '发票状态（1-已开具 2-已打印 3-已作废 4-已红冲换开）',
  `invoice_time` datetime COMMENT '开票时间',
  `print_time` datetime COMMENT '打印时间',
  `void_time` datetime COMMENT '作废时间',
  `void_reason` varchar(200) COMMENT '作废原因',
  `electronic_url` varchar(500) COMMENT '电子发票地址',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invoice_no` (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票';

-- biz_cashier_settlement  收费员班结单
CREATE TABLE `biz_cashier_settlement` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `settlement_no` varchar(32) NOT NULL COMMENT '交班单号',
  `cashier_id` bigint NOT NULL COMMENT '收费员工号',
  `cashier_name` varchar(50) NOT NULL COMMENT '收费员姓名',
  `shift_type` tinyint NOT NULL DEFAULT 3 COMMENT '班次（1-白班 2-夜班 3-其他）',
  `period_begin` datetime NOT NULL COMMENT '统计区间起',
  `period_end` datetime NOT NULL COMMENT '统计区间止',
  `day_settlement_id` bigint COMMENT '所属院级日结单ID',
  `charge_count` int NOT NULL DEFAULT 0 COMMENT '收费笔数',
  `charge_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '收费金额',
  `refund_count` int NOT NULL DEFAULT 0 COMMENT '退费笔数',
  `refund_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '退费金额',
  `net_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '净额 = 收费金额 - 退费金额',
  `cash_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '现金',
  `wechat_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '微信',
  `alipay_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '支付宝',
  `insurance_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '医保',
  `balance_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `unknown_pay_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '支付方式为空/未知的金额',
  `pool_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '医保统筹额',
  `invoice_count` int NOT NULL DEFAULT 0 COMMENT '本时段开票张数',
  `invoice_void_count` int NOT NULL DEFAULT 0 COMMENT '本时段作废张数',
  `handin_cash` decimal(12,2) COMMENT '实交现金',
  `cash_diff` decimal(12,2) COMMENT '现金差异 = 实交现金 - 系统现金',
  `diff_reason` varchar(500) COMMENT '差异说明',
  `settle_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已交班待日结 2-已日结 3-已审核）',
  `audit_by` varchar(64) COMMENT '审核人',
  `audit_time` datetime COMMENT '审核时间',
  `audit_remark` varchar(500) COMMENT '审核意见',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收费员班结单';

-- biz_day_settlement  院级日结单
CREATE TABLE `biz_day_settlement` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `settlement_no` varchar(32) NOT NULL COMMENT '日结单号',
  `settle_date` date NOT NULL COMMENT '日结日期',
  `shift_count` int NOT NULL DEFAULT 0 COMMENT '纳入的班结单数',
  `charge_count` int NOT NULL DEFAULT 0 COMMENT '收费笔数',
  `bill_count` int NOT NULL DEFAULT 0 COMMENT '当日结算账单张数',
  `charge_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '收费金额',
  `refund_count` int NOT NULL DEFAULT 0 COMMENT '退费笔数',
  `refund_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '退费金额',
  `net_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '净额 = 收费 - 退费',
  `cash_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '现金',
  `wechat_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '微信',
  `alipay_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '支付宝',
  `insurance_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '医保',
  `balance_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `unknown_pay_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '支付方式未知金额',
  `pool_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '医保统筹记账额',
  `invoice_count` int NOT NULL DEFAULT 0 COMMENT '开票张数',
  `invoice_void_count` int NOT NULL DEFAULT 0 COMMENT '作废张数',
  `detail_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '独立复算',
  `dept_count` int NOT NULL DEFAULT 0 COMMENT '有科室归属的科室数',
  `dept_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '已归属科室的明细金额合计',
  `unattributed_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '无科室归属的明细金额合计',
  `unassigned_count` int NOT NULL DEFAULT 0 COMMENT '未纳入任何班结单的已收费笔数',
  `unassigned_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '未纳入班结的金额',
  `reconcile_status` tinyint NOT NULL DEFAULT 1 COMMENT '对账结论（1-已平 2-有差异）',
  `diff_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '最大差异金额',
  `diff_detail` text COMMENT '差异明细',
  `settle_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待审核 2-已审核）',
  `settle_by` varchar(64) COMMENT '日结人',
  `settle_time` datetime COMMENT '日结时间',
  `audit_by` varchar(64) COMMENT '审核人',
  `audit_time` datetime COMMENT '审核时间',
  `audit_remark` varchar(500) COMMENT '审核意见',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settle_date` (`settle_date`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院级日结单';

-- biz_pay_channel_bill  支付渠道对账流水
CREATE TABLE `biz_pay_channel_bill` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `channel` tinyint NOT NULL COMMENT '支付渠道（2-微信 3-支付宝 6-银行卡）',
  `bill_date` date NOT NULL COMMENT '账单日期',
  `channel_trade_no` varchar(64) NOT NULL COMMENT '渠道流水号',
  `trade_time` datetime NOT NULL COMMENT '渠道交易时间',
  `amount` decimal(10,2) NOT NULL COMMENT '渠道侧金额',
  `import_way` tinyint NOT NULL DEFAULT 1 COMMENT '来源（1-渠道拉取 2-手工登记）',
  `local_txn_no` varchar(64) COMMENT '勾对的本地支付流水号',
  `local_txn_id` bigint COMMENT '勾对的本地支付流水ID',
  `txn_direction` tinyint COMMENT '勾对流水方向快照（1-收款 2-退款）',
  `match_status` tinyint NOT NULL DEFAULT 0 COMMENT '勾对状态（0-待勾对 1-已勾对 2-长款 3-短款）',
  `match_time` datetime COMMENT '勾对时间',
  `matched_by_id` bigint COMMENT '勾对人员工ID',
  `matched_by_name` varchar(50) COMMENT '勾对人姓名',
  `diff_amount` decimal(10,2) COMMENT '勾对差额（渠道-本地）',
  `handle_remark` varchar(500) COMMENT '长款/短款处理说明',
  `import_batch_no` varchar(40) COMMENT '导入批次号',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_txn` (`local_txn_no`),
  UNIQUE KEY `uk_channel_trade` (`channel`, `channel_trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付渠道对账流水';

-- biz_inpatient_settlement  住院结算单
CREATE TABLE `biz_inpatient_settlement` (
  `id` bigint NOT NULL COMMENT '结算单ID',
  `settlement_no` varchar(32) NOT NULL COMMENT '结算单号（唯一）',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `charge_count` int NOT NULL DEFAULT 0 COMMENT '本次结算涵盖的费用单数量',
  `total_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '住院总费用',
  `insurance_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '统筹支付',
  `patient_pay_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '患者应付',
  `prepay_balance` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '结算时预交金余额',
  `refund_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '应退患者',
  `arrears_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '欠费',
  `settle_status` tinyint NOT NULL DEFAULT 1 COMMENT '结算状态（1-已结清 2-欠费 3-已作废）',
  `settle_mode` tinyint COMMENT '结算方式（1-自费 2-医保）',
  `insurance_type` varchar(32) COMMENT '医保类型',
  `settle_time` datetime COMMENT '结算时间',
  `settle_by` bigint COMMENT '结算人（员工ID）',
  `settle_by_name` varchar(50) COMMENT '结算人姓名',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_inpatient_settlement_no` (`settlement_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院结算单';

-- biz_prepay  住院预交金流水
CREATE TABLE `biz_prepay` (
  `id` bigint NOT NULL COMMENT '预交金流水ID',
  `prepay_no` varchar(32) NOT NULL COMMENT '预交金单号（唯一）',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `prepay_type` tinyint NOT NULL COMMENT '流水类型（1-充值 2-退款）',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `balance_after` decimal(10,2) NOT NULL COMMENT '本笔之后的余额快照',
  `pay_method` tinyint NOT NULL DEFAULT 1 COMMENT '支付方式（1-现金 2-微信 3-支付宝 4-银行卡 5-转账）',
  `receipt_no` varchar(50) COMMENT '票据号',
  `pay_time` datetime NOT NULL COMMENT '收/退时间',
  `operator_id` bigint COMMENT '操作人',
  `operator_name` varchar(50) COMMENT '操作人姓名',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_prepay_no` (`prepay_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院预交金流水';

-- biz_arrears_policy  住院欠费管控策略
CREATE TABLE `biz_arrears_policy` (
  `id` bigint NOT NULL COMMENT '策略ID',
  `warn_line` decimal(12,2) COMMENT '预警线（元）',
  `stop_line` decimal(12,2) COMMENT '停费线（元）',
  `stop_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '停费管控开关（0-关 1-开）',
  `stop_classes` varchar(64) NOT NULL DEFAULT '2,3,4' COMMENT '被拦截的医嘱类别（2-检查 3-检验 4-治疗）',
  `remark` varchar(500) COMMENT '备注',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院欠费管控策略';

-- biz_stat_daily  日统计汇总
CREATE TABLE `biz_stat_daily` (
  `stat_id` bigint NOT NULL COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `dept_id` bigint COMMENT '科室ID',
  `visit_count` int NOT NULL DEFAULT 0 COMMENT '门诊量',
  `charge_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '收费金额',
  `prescription_count` int NOT NULL DEFAULT 0 COMMENT '处方数',
  `refund_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '退费金额',
  `drug_ratio` decimal(5,2) COMMENT '药品占比(%)',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`stat_id`),
  UNIQUE KEY `uk_stat_date_dept` (`stat_date`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日统计汇总';

-- biz_stat_dept  科室统计汇总
CREATE TABLE `biz_stat_dept` (
  `stat_id` bigint NOT NULL COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `visit_count` int NOT NULL DEFAULT 0 COMMENT '门诊量',
  `charge_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '科室收入',
  `drug_ratio` decimal(5,2) COMMENT '药品占比(%)',
  `avg_visit_time` int COMMENT '平均就诊时长(分钟)',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`stat_id`),
  UNIQUE KEY `uk_stat_date_dept` (`stat_date`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室统计汇总';

-- biz_admission  入院记录
CREATE TABLE `biz_admission` (
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `admission_no` varchar(32) NOT NULL COMMENT '入院记录号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint COMMENT '就诊次ID',
  `regist_id` bigint COMMENT '来源挂号ID',
  `regist_no` varchar(32) COMMENT '来源挂号号',
  `admission_order_id` bigint COMMENT '来源住院证ID',
  `admit_dept_id` bigint COMMENT '入院科室ID',
  `dept_id` bigint COMMENT '入院科室ID',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `bed_id` bigint NOT NULL COMMENT '床位ID',
  `admit_doctor_id` bigint NOT NULL COMMENT '入院医生ID',
  `admit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入院时间',
  `admit_way` tinyint COMMENT '入院途径（1-门诊 2-急诊 3-转院 4-其他）',
  `diagnosis` varchar(500) COMMENT '入院诊断',
  `admit_diagnosis_code` varchar(32) COMMENT '入院诊断ICD编码',
  `admit_diagnosis_name` varchar(200) COMMENT '入院诊断名称',
  `discharge_time` datetime COMMENT '出院时间',
  `admit_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-已出院 1-在院）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`admission_id`),
  UNIQUE KEY `uk_admission_no` (`admission_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入院记录';

-- biz_patient  患者基本信息
CREATE TABLE `biz_patient` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `master_id` bigint COMMENT '主索引',
  `merge_status` tinyint NOT NULL DEFAULT 0 COMMENT '主索引状态（0-正常 1-已并入主档）',
  `merge_time` datetime COMMENT '并入主档的时间',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL COMMENT '性别（1-男 2-女 9-未知）',
  `birth_date` date COMMENT '出生日期',
  `age` int COMMENT '年龄',
  `id_card` varchar(18) COMMENT '身份证号',
  `phone` varchar(20) COMMENT '手机号码',
  `contact_name` varchar(50) COMMENT '联系人姓名',
  `contact_phone` varchar(20) COMMENT '联系人电话',
  `contact_relation` varchar(20) COMMENT '联系人关系（父母、配偶、子女等）',
  `address` varchar(200) COMMENT '家庭住址',
  `nation` varchar(20) COMMENT '民族',
  `occupation` varchar(50) COMMENT '职业',
  `marital_status` tinyint DEFAULT 0 COMMENT '婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）',
  `blood_type` varchar(10) COMMENT '血型（A/B/O/AB）',
  `allergy_history` text COMMENT '过敏史',
  `medical_history` text COMMENT '既往病史',
  `patient_type` tinyint DEFAULT 1 COMMENT '患者类型（1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他）',
  `medical_insurance_no` varchar(50) COMMENT '医保卡号',
  `medical_insurance_type` varchar(50) COMMENT '医保类型',
  `card_type` tinyint DEFAULT 1 COMMENT '卡片类型（1-就诊卡 2-身份证 3-医保卡）',
  `card_no` varchar(50) COMMENT '卡片号码',
  `balance` decimal(10,2) DEFAULT 0.00 COMMENT '账户余额',
  `total_expense` decimal(10,2) DEFAULT 0.00 COMMENT '累计消费金额',
  `visit_count` int DEFAULT 0 COMMENT '就诊次数',
  `last_visit_time` datetime COMMENT '最后就诊时间',
  `last_visit_dept` bigint COMMENT '最后就诊科室',
  `last_visit_doctor` bigint COMMENT '最后就诊医生',
  `last_visit_dept_name` varchar(100) COMMENT '最近就诊科室名',
  `last_visit_doctor_name` varchar(50) COMMENT '最近接诊医生名',
  `first_visit_time` datetime COMMENT '首次就诊时间',
  `first_visit_dept_id` bigint COMMENT '首次就诊科室ID',
  `first_visit_dept_name` varchar(100) COMMENT '首次就诊科室名',
  `first_visit_doctor_id` bigint COMMENT '首次接诊医生ID',
  `first_visit_doctor_name` varchar(50) COMMENT '首次接诊医生名',
  `photo` varchar(200) COMMENT '患者照片',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_no` (`patient_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者基本信息';

-- sys_department  科室
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_code` varchar(32) NOT NULL COMMENT '科室编码（唯一）',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `dept_type` tinyint NOT NULL DEFAULT 1 COMMENT '科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他）',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父科室ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `dept_icon` varchar(200) COMMENT '科室图标',
  `dept_desc` varchar(500) COMMENT '科室描述',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `location` varchar(200) COMMENT '科室位置',
  `is_open` tinyint DEFAULT 1 COMMENT '是否开诊（0-否 1-是）',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室';

-- sys_employee  员工
CREATE TABLE `sys_employee` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `emp_code` varchar(32) NOT NULL COMMENT '员工编号（唯一）',
  `emp_name` varchar(50) NOT NULL COMMENT '员工姓名',
  `emp_type` tinyint NOT NULL DEFAULT 1 COMMENT '员工类型（1-医生 2-护士 3-收费员 4-药剂师 5-管理员 6-其他）',
  `gender` tinyint DEFAULT 1 COMMENT '性别',
  `birth_date` date COMMENT '出生日期',
  `hire_date` date COMMENT '入职日期',
  `id_card` varchar(18) COMMENT '身份证号',
  `phone` varchar(20) COMMENT '手机号码',
  `email` varchar(100) COMMENT '电子邮箱',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(200) COMMENT '科室ID',
  `title` varchar(50) COMMENT '职称',
  `position` varchar(50) COMMENT '职位',
  `specialty` varchar(200) COMMENT '专业特长',
  `education` varchar(50) COMMENT '学历',
  `avatar` varchar(200) COMMENT '头像地址',
  `is_expert` tinyint DEFAULT 0 COMMENT '是否专家（0-否 1-是）',
  `expert_price` decimal(10,2) DEFAULT 0.00 COMMENT '专家号价格',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_code` (`emp_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工';

-- ---------------- 参照关系（E-R 连线） ----------------
ALTER TABLE `biz_cashier_settlement` ADD CONSTRAINT `fk_biz_cashier_settlement_cashier_id` FOREIGN KEY (`cashier_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_cashier_settlement` ADD CONSTRAINT `fk_biz_cashier_settlement_day_settlement_id` FOREIGN KEY (`day_settlement_id`) REFERENCES `biz_day_settlement` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_orig_fee_id` FOREIGN KEY (`orig_fee_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_book_by_id` FOREIGN KEY (`book_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_fund_account` ADD CONSTRAINT `fk_biz_fund_account_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_account_id` FOREIGN KEY (`account_id`) REFERENCES `biz_fund_account` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_pay_channel_bill` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_payment_txn_id` FOREIGN KEY (`payment_txn_id`) REFERENCES `biz_payment_txn` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_orig_txn_id` FOREIGN KEY (`orig_txn_id`) REFERENCES `biz_payment_txn` (`id`);
ALTER TABLE `biz_inpatient_settlement` ADD CONSTRAINT `fk_biz_inpatient_settlement_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_settlement` ADD CONSTRAINT `fk_biz_inpatient_settlement_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inpatient_settlement` ADD CONSTRAINT `fk_biz_inpatient_settlement_settle_by` FOREIGN KEY (`settle_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_invoice` ADD CONSTRAINT `fk_biz_invoice_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_invoice` ADD CONSTRAINT `fk_biz_invoice_orig_invoice_id` FOREIGN KEY (`orig_invoice_id`) REFERENCES `biz_invoice` (`id`);
ALTER TABLE `biz_invoice` ADD CONSTRAINT `fk_biz_invoice_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_pay_channel_bill` ADD CONSTRAINT `fk_biz_pay_channel_bill_local_txn_id` FOREIGN KEY (`local_txn_id`) REFERENCES `biz_payment_txn` (`id`);
ALTER TABLE `biz_pay_channel_bill` ADD CONSTRAINT `fk_biz_pay_channel_bill_matched_by_id` FOREIGN KEY (`matched_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_pay_order` ADD CONSTRAINT `fk_biz_pay_order_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_orig_txn_id` FOREIGN KEY (`orig_txn_id`) REFERENCES `biz_payment_txn` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_cashier_id` FOREIGN KEY (`cashier_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_cashier_settlement_id` FOREIGN KEY (`cashier_settlement_id`) REFERENCES `biz_cashier_settlement` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_refund_apply` (`id`);
ALTER TABLE `biz_prepay` ADD CONSTRAINT `fk_biz_prepay_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_prepay` ADD CONSTRAINT `fk_biz_prepay_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_prepay` ADD CONSTRAINT `fk_biz_prepay_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_refund_apply` ADD CONSTRAINT `fk_biz_refund_apply_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_refund_apply` ADD CONSTRAINT `fk_biz_refund_apply_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_refund_apply` ADD CONSTRAINT `fk_biz_refund_apply_auditor_id` FOREIGN KEY (`auditor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_settlement_bill` ADD CONSTRAINT `fk_biz_settlement_bill_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_settlement_bill` ADD CONSTRAINT `fk_biz_settlement_bill_bill_by_id` FOREIGN KEY (`bill_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_settlement_bill` ADD CONSTRAINT `fk_biz_settlement_bill_void_by_id` FOREIGN KEY (`void_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_settlement_bill` ADD CONSTRAINT `fk_biz_settlement_bill_orig_bill_id` FOREIGN KEY (`orig_bill_id`) REFERENCES `biz_pay_channel_bill` (`id`);
ALTER TABLE `biz_settlement_bill_item` ADD CONSTRAINT `fk_biz_settlement_bill_item_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_settlement_bill_item` ADD CONSTRAINT `fk_biz_settlement_bill_item_fee_record_id` FOREIGN KEY (`fee_record_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_settlement_bill_item` ADD CONSTRAINT `fk_biz_settlement_bill_item_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_settlement_bill_item` ADD CONSTRAINT `fk_biz_settlement_bill_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_stat_daily` ADD CONSTRAINT `fk_biz_stat_daily_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `biz_stat_dept` (`stat_id`);
ALTER TABLE `biz_stat_dept` ADD CONSTRAINT `fk_biz_stat_dept_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
