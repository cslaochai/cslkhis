-- ============================================================
-- 领域 17 药房药库（采购·库存·发药·调拨·盘点·静配）（本域 22 表 + 上游参照 13 表 / 67 条关系）
-- 由 workspace/_er/refresh.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


-- biz_purchase_order  药品采购订单
CREATE TABLE `biz_purchase_order` (
  `order_id` bigint NOT NULL COMMENT '采购订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '采购订单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `order_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `total_amount` decimal(12,2) DEFAULT 0.00 COMMENT '订单总金额',
  `approval_status` tinyint NOT NULL DEFAULT 0 COMMENT '审批状态（0-待审批 1-已通过 2-已驳回）',
  `approver_id` bigint COMMENT '审批人ID',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品采购订单';

-- biz_purchase_order_detail  药品采购订单明细
CREATE TABLE `biz_purchase_order_detail` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '采购订单ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `quantity` decimal(10,2) NOT NULL COMMENT '采购数量',
  `unit_price` decimal(12,2) NOT NULL COMMENT '采购单价',
  `amount` decimal(14,2) NOT NULL COMMENT '金额 = 数量 × 单价',
  `batch_no` varchar(50) NOT NULL COMMENT '批号',
  `production_date` date COMMENT '生产日期',
  `expiry_date` date NOT NULL COMMENT '有效期',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_drug_batch` (`order_id`, `drug_id`, `batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品采购订单明细';

-- biz_drug_inbound  药品入库单
CREATE TABLE `biz_drug_inbound` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `inbound_no` varchar(32) NOT NULL COMMENT '入库单号（唯一）',
  `inbound_type` tinyint NOT NULL DEFAULT 1 COMMENT '入库类型（1-采购入库 2-退货入库 3-盘盈入库 4-其他入库）',
  `purchase_order_id` bigint COMMENT '来源采购订单ID',
  `purchase_order_no` varchar(32) COMMENT '来源采购订单号',
  `supplier` varchar(200) COMMENT '供应商',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '总金额',
  `total_quantity` decimal(10,2) DEFAULT 0.00 COMMENT '总数量',
  `inbound_status` tinyint DEFAULT 1 COMMENT '入库状态（1-待审核 2-已审核 3-已入库 4-已取消）',
  `audit_by` varchar(64) COMMENT '审核人',
  `audit_time` datetime COMMENT '审核时间',
  `inbound_by` varchar(64) COMMENT '入库人',
  `inbound_time` datetime COMMENT '入库时间',
  `cancel_by` varchar(64) COMMENT '取消人',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(200) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_no` (`inbound_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品入库单';

-- biz_drug_inbound_detail  药品入库明细
CREATE TABLE `biz_drug_inbound_detail` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `inbound_id` bigint NOT NULL COMMENT '入库单ID',
  `inbound_no` varchar(32) NOT NULL COMMENT '入库单号',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) NOT NULL COMMENT '药品编码',
  `drug_name` varchar(200) NOT NULL COMMENT '药品名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `batch_no` varchar(50) NOT NULL COMMENT '批号',
  `production_date` date COMMENT '生产日期',
  `expiry_date` date NOT NULL COMMENT '有效期',
  `quantity` decimal(10,2) NOT NULL COMMENT '入库数量',
  `cost_price` decimal(10,2) NOT NULL COMMENT '成本价',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `detail_status` tinyint DEFAULT 1 COMMENT '明细状态（1-正常 2-已入库 3-已取消）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品入库明细';

-- biz_drug_stock  药品批次库存
CREATE TABLE `biz_drug_stock` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `batch_no` varchar(50) NOT NULL COMMENT '批号',
  `production_date` date COMMENT '生产日期',
  `expiry_date` date NOT NULL COMMENT '有效期',
  `quantity` decimal(10,2) DEFAULT 0.00 COMMENT '库存数量',
  `locked_quantity` decimal(10,2) DEFAULT 0.00 COMMENT '锁定数量',
  `available_quantity` decimal(10,2) DEFAULT 0.00 COMMENT '可用数量',
  `cost_price` decimal(10,2) DEFAULT 0.00 COMMENT '成本价',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '库存金额',
  `location` varchar(100) COMMENT '存放位置',
  `stock_room` tinyint NOT NULL DEFAULT 2 COMMENT '库存地点（1-药库 2-药房）',
  `supplier` varchar(200) COMMENT '供应商',
  `supplier_id` bigint COMMENT '供应商ID',
  `stock_status` tinyint DEFAULT 1 COMMENT '库存状态（1-正常 2-预警 3-缺货 4-过期）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品批次库存';

-- biz_drug_stock_log  药品库存流水
CREATE TABLE `biz_drug_stock_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `stock_id` bigint NOT NULL COMMENT '库存批次ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `batch_no` varchar(50) COMMENT '批号',
  `change_type` tinyint NOT NULL COMMENT '变动类型（1-入库 2-发药出库 3-退药回库 4-其他出库 5-盘盈 6-盘亏）',
  `change_quantity` decimal(10,2) NOT NULL COMMENT '变动数量',
  `quantity_before` decimal(10,2) NOT NULL COMMENT '变动前批次数量',
  `quantity_after` decimal(10,2) NOT NULL COMMENT '变动后批次数量',
  `source_type` varchar(32) COMMENT '来源类型',
  `source_id` bigint COMMENT '来源单据ID',
  `source_no` varchar(64) COMMENT '来源单据号',
  `operator_name` varchar(50) COMMENT '操作人',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品库存流水';

-- biz_drug_outbound  药品出库单
CREATE TABLE `biz_drug_outbound` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `outbound_no` varchar(32) NOT NULL COMMENT '出库单号（唯一）',
  `outbound_type` tinyint NOT NULL DEFAULT 1 COMMENT '出库类型（1-发药出库 2-报损出库 3-退药出库 4-调拨出库 5-其他出库）',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '总金额',
  `total_quantity` decimal(10,2) DEFAULT 0.00 COMMENT '总数量',
  `outbound_status` tinyint DEFAULT 1 COMMENT '出库状态（1-待审核 2-已审核 3-已出库 4-已取消）',
  `audit_by` varchar(64) COMMENT '审核人',
  `audit_time` datetime COMMENT '审核时间',
  `outbound_by` varchar(64) COMMENT '出库人',
  `outbound_time` datetime COMMENT '出库时间',
  `cancel_by` varchar(64) COMMENT '取消人',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(200) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_no` (`outbound_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品出库单';

-- biz_drug_outbound_detail  药品出库明细
CREATE TABLE `biz_drug_outbound_detail` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `outbound_id` bigint NOT NULL COMMENT '出库单ID',
  `outbound_no` varchar(32) NOT NULL COMMENT '出库单号',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) NOT NULL COMMENT '药品编码',
  `drug_name` varchar(200) NOT NULL COMMENT '药品名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `batch_no` varchar(50) NOT NULL COMMENT '批号',
  `quantity` decimal(10,2) NOT NULL COMMENT '出库数量',
  `cost_price` decimal(10,2) NOT NULL COMMENT '成本价',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `detail_status` tinyint DEFAULT 1 COMMENT '明细状态（1-正常 2-已出库 3-已取消）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品出库明细';

-- biz_drug_dispensing  药品发药记录
CREATE TABLE `biz_drug_dispensing` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dispensing_no` varchar(32) NOT NULL COMMENT '发药单号',
  `prescription_id` bigint NOT NULL COMMENT '处方ID',
  `prescription_no` varchar(32) COMMENT '处方号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) COMMENT '药品编码',
  `drug_name` varchar(100) NOT NULL COMMENT '药品名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `quantity` decimal(10,2) NOT NULL COMMENT '发药数量',
  `price` decimal(10,4) COMMENT '单价',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `dispensing_status` tinyint NOT NULL DEFAULT 1 COMMENT '发药状态（1-待发药 2-已发药 3-已退药）',
  `pharmacist_id` bigint COMMENT '发药药师ID',
  `pharmacist_name` varchar(50) COMMENT '发药药师姓名',
  `dispensing_time` datetime COMMENT '发药时间',
  `stock_before` decimal(10,2) COMMENT '发药前库存',
  `stock_after` decimal(10,2) COMMENT '发药后库存',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `prescription_detail_id` bigint COMMENT '处方明细ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dispensing_no` (`dispensing_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品发药记录';

-- biz_ward_dispense  住院摆药单
CREATE TABLE `biz_ward_dispense` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `dispense_no` varchar(32) NOT NULL COMMENT '摆药单号 WD+yyyyMMdd+4位',
  `dispense_date` date NOT NULL COMMENT '摆药日期',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `ward_id` bigint NOT NULL COMMENT '病区ID（快照）',
  `ward_name` varchar(128) COMMENT '病区名称（快照）',
  `dept_id` bigint COMMENT '入院科室ID（快照）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '主单状态（1-待配药 2-配药中 3-已配药 4-已核对 5-已退药）',
  `generate_by` varchar(64) COMMENT '生成人（药房）',
  `generate_time` datetime COMMENT '生成时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dispense_no` (`dispense_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院摆药单';

-- biz_ward_dispense_item  住院摆药明细
CREATE TABLE `biz_ward_dispense_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `dispense_id` bigint NOT NULL COMMENT '摆药单ID',
  `dispense_no` varchar(32) COMMENT '摆药单号（冗余）',
  `dispense_date` date NOT NULL COMMENT '摆药日期',
  `dispense_seq` int NOT NULL DEFAULT 1 COMMENT '重摆序号',
  `order_id` bigint NOT NULL COMMENT '住院医嘱ID',
  `order_no` varchar(32) COMMENT '医嘱号（快照）',
  `admission_id` bigint NOT NULL COMMENT '入院ID（冗余）',
  `patient_id` bigint NOT NULL COMMENT '患者ID（冗余）',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `ward_id` bigint COMMENT '病区ID（快照）',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_name` varchar(128) COMMENT '药品名称（快照）',
  `item_code` varchar(64) COMMENT '医嘱项目编码（快照）',
  `item_name` varchar(128) COMMENT '医嘱项目名称（快照）',
  `spec` varchar(64) COMMENT '规格（快照）',
  `unit` varchar(32) COMMENT '单位（快照）',
  `quantity` decimal(12,2) NOT NULL COMMENT '摆药数量',
  `price` decimal(12,4) NOT NULL DEFAULT 0.0000 COMMENT '单价',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额 = quantity × price',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '明细状态（1-待配药 2-已配药 3-已核对 4-已退药）',
  `stock_before` decimal(12,2) COMMENT '配药前库存',
  `stock_after` decimal(12,2) COMMENT '配药后库存',
  `fee_record_id` bigint COMMENT '记账行ID',
  `fee_no` varchar(32) COMMENT '记账单号',
  `dispenser_id` bigint COMMENT '配药人ID',
  `dispenser_name` varchar(64) COMMENT '配药人姓名',
  `dispense_time` datetime COMMENT '配药时间',
  `checker_id` bigint COMMENT '核对人ID（员工ID）',
  `checker_name` varchar(64) COMMENT '核对人姓名',
  `check_time` datetime COMMENT '核对时间',
  `return_by` varchar(64) COMMENT '退药操作人',
  `return_time` datetime COMMENT '退药时间',
  `return_reason` varchar(255) COMMENT '退药原因（必填）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_date_seq` (`order_id`, `dispense_date`, `dispense_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院摆药明细';

-- biz_drug_transfer  药品调拨单
CREATE TABLE `biz_drug_transfer` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `transfer_no` varchar(64) NOT NULL COMMENT '调拨单号',
  `transfer_type` tinyint NOT NULL COMMENT '方向（1-药库下拨药房 2-药房退回药库）',
  `from_room` tinyint NOT NULL COMMENT '发出库位（1-药库 2-药房）',
  `to_room` tinyint NOT NULL COMMENT '接收库位',
  `reason` varchar(200) NOT NULL COMMENT '事由',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待发出 2-待接收 3-已完成 4-已作废）',
  `total_items` int NOT NULL DEFAULT 0 COMMENT '批次数',
  `total_quantity` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '申请合计数量',
  `out_quantity` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '已发出合计数量',
  `in_quantity` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '已接收合计数量',
  `total_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '合计金额',
  `out_by` varchar(64) COMMENT '发出人',
  `out_time` datetime COMMENT '发出时间',
  `in_by` varchar(64) COMMENT '接收人',
  `in_time` datetime COMMENT '接收时间',
  `cancel_by` varchar(64) COMMENT '作废操作人',
  `cancel_time` datetime COMMENT '作废时间',
  `cancel_reason` varchar(200) COMMENT '作废原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品调拨单';

-- biz_drug_transfer_item  药品调拨明细
CREATE TABLE `biz_drug_transfer_item` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `transfer_id` bigint NOT NULL COMMENT '调拨单ID',
  `stock_id` bigint NOT NULL COMMENT '发出方库存批次ID',
  `in_stock_id` bigint COMMENT '接收方库存批次ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) COMMENT '药品编码（快照）',
  `drug_name` varchar(200) COMMENT '药品名称（快照）',
  `specification` varchar(100) COMMENT '规格（快照）',
  `unit` varchar(20) COMMENT '单位（快照）',
  `batch_no` varchar(50) COMMENT '批号',
  `production_date` date COMMENT '生产日期（快照）',
  `expiry_date` date COMMENT '有效期（快照）',
  `cost_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '批次成本价',
  `apply_quantity` decimal(10,2) NOT NULL COMMENT '调拨数量',
  `locked_quantity` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '建单时该批次已锁定量',
  `out_flag` tinyint NOT NULL DEFAULT 0 COMMENT '发出标记（0-未发出 1-已发出）',
  `in_flag` tinyint NOT NULL DEFAULT 0 COMMENT '接收标记（0-未接收 1-已接收）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_stock` (`transfer_id`, `stock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品调拨明细';

-- biz_drug_supplier_return  药品供应商退货单
CREATE TABLE `biz_drug_supplier_return` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `return_no` varchar(64) NOT NULL COMMENT '退货单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `supplier_name` varchar(128) NOT NULL COMMENT '供应商名称',
  `return_reason` varchar(200) NOT NULL COMMENT '退货原因（近效期 / 质量问题 / 冷链断链 / 采购让价退货…，必填）',
  `src_ref_no` varchar(64) COMMENT '原入库单号或采购单号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待退货 2-已退货 3-已作废）',
  `total_items` int NOT NULL DEFAULT 0 COMMENT '批次数',
  `total_quantity` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '退货合计数量',
  `total_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '退货合计金额',
  `return_by` varchar(64) COMMENT '退货经办人',
  `return_time` datetime COMMENT '退货时间',
  `cancel_by` varchar(64) COMMENT '作废操作人',
  `cancel_time` datetime COMMENT '作废时间',
  `cancel_reason` varchar(200) COMMENT '作废原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_return_no` (`return_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品供应商退货单';

-- biz_drug_supplier_return_item  药品供应商退货明细
CREATE TABLE `biz_drug_supplier_return_item` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `return_id` bigint NOT NULL COMMENT '退货单ID',
  `stock_id` bigint NOT NULL COMMENT '库存批次ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) COMMENT '药品编码（快照）',
  `drug_name` varchar(200) COMMENT '药品名称（快照）',
  `specification` varchar(100) COMMENT '规格（快照）',
  `unit` varchar(20) COMMENT '单位（快照）',
  `batch_no` varchar(50) COMMENT '批号（快照）',
  `expiry_date` date COMMENT '有效期',
  `stock_room` tinyint NOT NULL DEFAULT 1 COMMENT '退货库位（1-药库 2-药房）',
  `supplier_id` bigint COMMENT '批次所属供应商ID',
  `cost_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '批次成本价',
  `quantity` decimal(10,2) NOT NULL COMMENT '退货数量',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '退货金额',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sreturn_stock` (`return_id`, `stock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品供应商退货明细';

-- biz_drug_trace  药品追溯码台账
CREATE TABLE `biz_drug_trace` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `trace_no` varchar(32) NOT NULL COMMENT '院内追溯流水号',
  `trace_code` varchar(128) NOT NULL COMMENT '追溯码原文',
  `code_type` tinyint NOT NULL DEFAULT 1 COMMENT '码制（1-GS1 2-中国药品追溯码20位 3-其他）',
  `drug_di` varchar(32) COMMENT '解析-产品标识',
  `serial_no` varchar(64) COMMENT '解析-生产序列号',
  `code_batch_no` varchar(64) COMMENT '解析-码内批号',
  `code_expiry_date` date COMMENT '解析-码内有效期',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) COMMENT '药品编码（快照）',
  `drug_name` varchar(100) COMMENT '药品名称（快照）',
  `generic_name` varchar(100) COMMENT '通用名（快照）',
  `specification` varchar(100) COMMENT '规格（快照）',
  `dosage_form` varchar(50) COMMENT '剂型（快照）',
  `unit` varchar(20) COMMENT '单位（快照）',
  `manufacturer` varchar(200) COMMENT '生产厂家（快照）',
  `approval_number` varchar(100) COMMENT '批准文号',
  `stock_id` bigint COMMENT '采集挂靠批次ID',
  `stock_batch_no` varchar(50) COMMENT '库存批号（快照）',
  `supplier` varchar(200) COMMENT '供应商（快照）',
  `supplier_id` bigint COMMENT '供应商ID',
  `source_type` tinyint NOT NULL DEFAULT 1 COMMENT '采集来源（1-入库采集 2-存量补采）',
  `inbound_id` bigint COMMENT '来源入库单ID',
  `inbound_no` varchar(64) COMMENT '来源入库单号（快照）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '码状态（1-在库 2-已发药核销 3-已作废）',
  `scan_time` datetime COMMENT '采集扫码时间',
  `operator_name` varchar(50) COMMENT '采集人',
  `dispensing_id` bigint COMMENT '发药单ID',
  `dispensing_no` varchar(64) COMMENT '发药单号（快照）',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `visit_type` tinyint COMMENT '就诊类型（1-门诊 2-住院）',
  `regist_id` bigint COMMENT '门诊挂号ID（快照）',
  `admission_id` bigint COMMENT '住院ID（快照）',
  `dept_id` bigint COMMENT '发药科室ID（快照）',
  `dept_name` varchar(100) COMMENT '发药科室名称（快照）',
  `dispense_time` datetime COMMENT '发药核销时间',
  `dispense_operator` varchar(50) COMMENT '发药核销人',
  `upload_status` tinyint NOT NULL DEFAULT 0 COMMENT '上传状态（0-待上传 1-已上传 2-上传失败）',
  `upload_batch_no` varchar(32) COMMENT '上传批次号',
  `upload_time` datetime COMMENT '上传时间',
  `upload_fail_reason` varchar(500) COMMENT '上传失败原因',
  `void_type` tinyint COMMENT '作废类型（1-退药 2-报损 3-召回）',
  `void_time` datetime COMMENT '作废时间',
  `void_reason` varchar(200) COMMENT '作废原因',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drug_trace_no` (`trace_no`),
  UNIQUE KEY `uk_drug_trace_code` (`trace_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品追溯码台账';

-- biz_stocktake  药房盘点单
CREATE TABLE `biz_stocktake` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `stocktake_no` varchar(64) NOT NULL COMMENT '盘点单号',
  `stocktake_title` varchar(200) NOT NULL COMMENT '盘点主题',
  `scope_drug_type` tinyint COMMENT '范围-药品类型（1-西药 2-中成药 3-中药饮片）',
  `scope_keyword` varchar(100) COMMENT '范围-药品名称关键字',
  `scope_desc` varchar(200) NOT NULL COMMENT '范围的人读描述',
  `snapshot_time` datetime NOT NULL COMMENT '账面快照时点',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-盘点中 2-待复核 3-已过账 4-已关单）',
  `total_items` int NOT NULL DEFAULT 0 COMMENT '参与盘点批次数',
  `counted_items` int NOT NULL DEFAULT 0 COMMENT '已录入实盘数批次数',
  `diff_items` int NOT NULL DEFAULT 0 COMMENT '有差异批次数',
  `profit_items` int NOT NULL DEFAULT 0 COMMENT '盘盈批次数',
  `loss_items` int NOT NULL DEFAULT 0 COMMENT '盘亏批次数',
  `diff_quantity` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '净差数量',
  `diff_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '净差金额',
  `submit_by` varchar(64) COMMENT '提交人',
  `submit_time` datetime COMMENT '提交时间',
  `audit_by` varchar(64) COMMENT '复核人',
  `audit_time` datetime COMMENT '复核时间',
  `audit_remark` varchar(500) COMMENT '复核意见 / 退回原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stocktake_no` (`stocktake_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药房盘点单';

-- biz_stocktake_item  药房盘点明细
CREATE TABLE `biz_stocktake_item` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `stocktake_id` bigint NOT NULL COMMENT '盘点单ID',
  `stock_id` bigint NOT NULL COMMENT '库存批次ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) COMMENT '药品编码（快照）',
  `drug_name` varchar(200) COMMENT '药品名称（快照）',
  `specification` varchar(100) COMMENT '规格（快照）',
  `unit` varchar(20) COMMENT '单位（快照）',
  `batch_no` varchar(50) COMMENT '批号（快照）',
  `production_date` date COMMENT '生产日期（快照）',
  `expiry_date` date COMMENT '有效期（快照）',
  `location` varchar(100) COMMENT '库位',
  `cost_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '成本价',
  `locked_quantity` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '快照时已锁定数量',
  `book_quantity` decimal(10,2) NOT NULL COMMENT '账面数量',
  `counted_quantity` decimal(10,2) COMMENT '实盘数量',
  `diff_quantity` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '差异数量',
  `diff_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '差异金额',
  `posted` tinyint NOT NULL DEFAULT 0 COMMENT '过账标记（0-未过账 1-已盘盈亏过账 2-无差异免过账）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '差异说明',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stocktake_stock` (`stocktake_id`, `stock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药房盘点明细';

-- biz_drug_package  药品耗材套餐
CREATE TABLE `biz_drug_package` (
  `id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `package_name` varchar(100) NOT NULL COMMENT '套餐名称',
  `package_type` tinyint DEFAULT 1 COMMENT '套餐类型（1-药品套餐 2-检查套餐 3-综合套餐）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品耗材套餐';

-- biz_drug_package_detail  药品耗材套餐明细
CREATE TABLE `biz_drug_package_detail` (
  `id` bigint NOT NULL,
  `package_id` bigint NOT NULL COMMENT '套餐ID',
  `item_type` tinyint NOT NULL COMMENT '项目类型（1-药品 2-检查 3-检验）',
  `item_id` bigint NOT NULL COMMENT '项目ID',
  `item_code` varchar(32) NOT NULL COMMENT '项目编码',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `quantity` decimal(10,2) DEFAULT 1.00 COMMENT '数量',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '单价',
  `usage_dosage` varchar(100) COMMENT '用法用量',
  `frequency` varchar(50) COMMENT '用药频次',
  `route` varchar(50) COMMENT '用药途径',
  `duration` int COMMENT '疗程天数',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品耗材套餐明细';

-- biz_pivas_batch  静配中心主单
CREATE TABLE `biz_pivas_batch` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `pivas_no` varchar(32) NOT NULL COMMENT '静配单号',
  `admix_date` date NOT NULL COMMENT '调配日期',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `ward_id` bigint NOT NULL COMMENT '病区ID（快照）',
  `ward_name` varchar(128) COMMENT '病区名称（快照）',
  `dept_id` bigint COMMENT '入院科室ID（快照）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '主单状态（1-待审方 2-待排队 3-待调配 4-待核对 5-已完成 6-全拒配）',
  `item_count` int NOT NULL DEFAULT 0 COMMENT '明细条数',
  `generate_by` varchar(64) COMMENT '生成人',
  `generate_time` datetime COMMENT '生成时间',
  `label_by` varchar(64) COMMENT '打标签（排队）',
  `label_time` datetime COMMENT '打标签时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pivas_no` (`pivas_no`),
  UNIQUE KEY `uk_adm_date` (`admission_id`, `admix_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='静配中心主单';

-- biz_pivas_item  静配中心调配明细
CREATE TABLE `biz_pivas_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `pivas_id` bigint NOT NULL COMMENT '主单ID',
  `pivas_no` varchar(32) COMMENT '静配单号（冗余）',
  `admix_date` date NOT NULL COMMENT '调配日期',
  `pivas_seq` int NOT NULL DEFAULT 1 COMMENT '重生成序号',
  `order_id` bigint NOT NULL COMMENT '住院医嘱ID',
  `order_no` varchar(32) COMMENT '医嘱号（快照）',
  `admission_id` bigint NOT NULL COMMENT '入院ID（冗余）',
  `patient_id` bigint NOT NULL COMMENT '患者ID（冗余）',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `ward_id` bigint COMMENT '病区ID（快照）',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_name` varchar(128) COMMENT '药品名称（快照）',
  `item_code` varchar(64) COMMENT '医嘱项目编码（快照）',
  `item_name` varchar(128) COMMENT '医嘱项目名称（快照）',
  `spec` varchar(64) COMMENT '规格（快照）',
  `unit` varchar(32) COMMENT '单位（快照）',
  `quantity` decimal(12,2) NOT NULL COMMENT '当日调配数量',
  `price` decimal(12,4) NOT NULL DEFAULT 0.0000 COMMENT '单价',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额 = quantity × price',
  `route` varchar(64) COMMENT '给药途径（快照，中文原文：静滴/静推/泵入…）',
  `frequency` varchar(32) COMMENT '频次（快照，qd/bid/tid…）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '明细状态（0-已拒配 1-待审方 2-已审方 3-已排队 4-已调配 5-已核对发放）',
  `queue_no` int COMMENT '排队号',
  `auditor_id` bigint COMMENT '审方药师ID（员工ID）',
  `auditor_name` varchar(64) COMMENT '审方药师姓名',
  `audit_time` datetime COMMENT '审方时间',
  `reject_reason` varchar(255) COMMENT '审方退回原因',
  `compounder_id` bigint COMMENT '调配人ID',
  `compounder_name` varchar(64) COMMENT '调配人姓名',
  `compound_time` datetime COMMENT '调配时间',
  `verifier_id` bigint COMMENT '成品核对人ID',
  `verifier_name` varchar(64) COMMENT '成品核对人姓名',
  `verify_time` datetime COMMENT '核对发放时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pivas_order_date_seq` (`order_id`, `admix_date`, `pivas_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='静配中心调配明细';

-- biz_admission  入院记录
CREATE TABLE `biz_admission` (
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `admission_no` varchar(32) NOT NULL COMMENT '入院记录号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint COMMENT '就诊次ID',
  `regist_id` bigint COMMENT '来源挂号ID',
  `regist_no` varchar(32) COMMENT '来源挂号号（快照）',
  `admission_order_id` bigint COMMENT '来源住院证ID',
  `admit_dept_id` bigint COMMENT '入院科室ID',
  `dept_id` bigint COMMENT '入院科室ID',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `nursing_level` tinyint COMMENT '护理等级（1-特级 2-一级 3-二级 4-三级，字典 his_nursing_level）',
  `nursing_level_source` tinyint NOT NULL DEFAULT 1 COMMENT '护理等级来源（1-默认兜底 2-护理记录带出 3-护士长评定）',
  `nursing_level_time` datetime COMMENT '护理等级评定时间（默认兜底时为写入时间）',
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

-- biz_appoint_info  挂号信息
CREATE TABLE `biz_appoint_info` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `regist_no` varchar(32) NOT NULL COMMENT '挂号单号（唯一）',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL COMMENT '性别（1-男 2-女 3-未知）',
  `age` int COMMENT '年龄',
  `phone` varchar(20) COMMENT '手机号码',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `room_id` bigint COMMENT '诊室ID',
  `room_name` varchar(100) COMMENT '诊室名称',
  `doctor_id` bigint COMMENT '医生ID',
  `doctor_name` varchar(50) COMMENT '医生姓名',
  `schedule_id` bigint COMMENT '排班ID',
  `slot_id` bigint COMMENT '排班时间片段ID',
  `slot_start` char(5) COMMENT '就诊时段开始快照（HH:mm）',
  `slot_end` char(5) COMMENT '就诊时段结束快照（HH:mm）',
  `regist_type` tinyint DEFAULT 1 COMMENT '挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）',
  `regist_source` tinyint DEFAULT 1 COMMENT '挂号来源（1-窗口挂号 2-自助机挂号 3-网上挂号 4-预约挂号）',
  `regist_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '挂号时间',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `arrive_time` datetime COMMENT '到达时间',
  `schedule_type` tinyint COMMENT '时间段（1-上午 2-下午 3-全天 4-凌晨）',
  `slot_time` varchar(8) COMMENT '就诊时段',
  `visit_type` tinyint NOT NULL COMMENT '就诊类型（号别）（1-初诊 2-复诊）',
  `revisit_source` tinyint COMMENT '复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊）',
  `settlement_type` tinyint DEFAULT 1 COMMENT '结算方式（1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-商业保险）',
  `medical_insurance_type` varchar(50) COMMENT '医保类型（如：在职职工、退休职工、城乡居民等）',
  `medical_insurance_no` varchar(50) COMMENT '医保卡号',
  `regist_status` tinyint DEFAULT 1 COMMENT '挂号状态（1-已挂号 2-已签到 3-已接诊 4-已就诊 5-已退号 6-已过号 7-爽约 8-未就诊）',
  `revisit_type` tinyint COMMENT '【已废置】改用 visit_type',
  `revisit_record_id` bigint COMMENT '复诊关联的病历ID',
  `refund_time` datetime COMMENT '退号时间',
  `refund_reason` varchar(200) COMMENT '退号原因',
  `bill_id` bigint COMMENT '挂号费结算账单ID',
  `bill_no` varchar(32) COMMENT '账单号（快照）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_by_id` bigint COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_by_id` bigint COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_regist_no` (`regist_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号信息';

-- biz_consumable_stock  耗材批次库存
CREATE TABLE `biz_consumable_stock` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `consumable_id` bigint NOT NULL COMMENT '耗材ID',
  `batch_no` varchar(50) NOT NULL COMMENT '批号',
  `production_date` date COMMENT '生产日期',
  `expiry_date` date COMMENT '有效期',
  `quantity` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '库存数量',
  `cost_price` decimal(10,2) DEFAULT 0.00 COMMENT '成本价',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '库存金额',
  `location` varchar(100) COMMENT '存放位置',
  `supplier` varchar(200) COMMENT '供应商',
  `stock_status` tinyint DEFAULT 1 COMMENT '库存状态（1-正常 2-预警 3-缺货 4-过期）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材批次库存';

-- biz_fee_record  费用记账流水
CREATE TABLE `biz_fee_record` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `fee_no` varchar(32) NOT NULL COMMENT '记账流水号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `encounter_type` tinyint NOT NULL COMMENT '就诊类型（1-门诊 2-住院）',
  `encounter_id` bigint NOT NULL COMMENT '就诊标识',
  `encounter_no` varchar(32) COMMENT '就诊标识单号',
  `dept_id` bigint COMMENT '费用归属科室',
  `dept_name` varchar(100) COMMENT '科室名称（快照）',
  `doctor_id` bigint COMMENT '开单/执行人员工ID',
  `doctor_name` varchar(50) COMMENT '开单人姓名（快照）',
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
  `book_by_name` varchar(64) COMMENT '记账人姓名（快照）',
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

-- biz_inpatient_order  住院医嘱主表
CREATE TABLE `biz_inpatient_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '医嘱号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `dept_id` bigint COMMENT '开立科室ID',
  `dept_name` varchar(64) COMMENT '开立科室名称（快照）',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称（快照）',
  `bed_no` varchar(32) COMMENT '床号（快照）',
  `order_type` tinyint NOT NULL COMMENT '医嘱类型（1-长期 2-临时）',
  `order_group` varchar(32) COMMENT '组套号',
  `order_class` tinyint NOT NULL COMMENT '医嘱类别',
  `item_code` varchar(64) COMMENT '项目编码（药品/检查/检验字典码）',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `spec` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `dosage` decimal(12,3) COMMENT '单次剂量',
  `dosage_unit` varchar(20) COMMENT '剂量单位',
  `route` varchar(64) COMMENT '给药途径（口服/静滴/肌注…）',
  `frequency` varchar(32) COMMENT '频次（qd/bid/tid/q8h…）',
  `quantity` decimal(10,2) NOT NULL DEFAULT 1.00 COMMENT '本次执行数量',
  `price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '本次执行金额（元）',
  `start_time` datetime NOT NULL COMMENT '医嘱开始时间',
  `plan_end_time` datetime COMMENT '计划结束时间',
  `stop_time` datetime COMMENT '实际停止时间',
  `order_time` datetime NOT NULL COMMENT '开立时间',
  `doctor_id` bigint COMMENT '开立医生ID（员工ID）',
  `doctor_name` varchar(64) COMMENT '开立医生姓名',
  `doctor_sign_id` bigint COMMENT '开立医生签名ID',
  `doctor_signed_time` datetime COMMENT '开立签名时刻',
  `verify_nurse_id` bigint COMMENT '校对护士ID（员工ID）',
  `verify_nurse_name` varchar(64) COMMENT '校对护士姓名',
  `verify_time` datetime COMMENT '校对时间',
  `nurse_sign_id` bigint COMMENT '校对护士签名ID',
  `nurse_signed_time` datetime COMMENT '校对签名时刻',
  `stop_doctor_id` bigint COMMENT '停止医嘱的医生ID',
  `stop_doctor_name` varchar(64) COMMENT '停止医嘱的医生姓名',
  `stop_reason` varchar(200) COMMENT '停止原因',
  `order_status` tinyint NOT NULL DEFAULT 1 COMMENT '医嘱状态（1-待校对 2-已校对 3-执行中 4-已完成 5-已停止 6-已作废 7-已退回）',
  `is_urgent` tinyint NOT NULL DEFAULT 0 COMMENT '是否加急（0-否 1-是）',
  `source` tinyint NOT NULL DEFAULT 1 COMMENT '医嘱来源（1-医生 2-模板 3-组套）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院医嘱主表';

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

-- biz_prescription  处方主表
CREATE TABLE `biz_prescription` (
  `id` bigint NOT NULL,
  `prescription_no` varchar(32) NOT NULL COMMENT '处方号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别',
  `age` int COMMENT '年龄',
  `regist_id` bigint NOT NULL COMMENT '挂号ID',
  `record_id` bigint COMMENT '病历ID',
  `record_no` varchar(32) COMMENT '病历号',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '医生姓名',
  `doctor_sign_id` bigint COMMENT '开方医师签名ID',
  `doctor_signed_time` datetime COMMENT '开方签名时刻',
  `prescription_type` tinyint DEFAULT 1 COMMENT '处方类型（1-西药处方 2-中成药处方 3-中药饮片处方）',
  `prescription_source` tinyint DEFAULT 1 COMMENT '处方来源（1-门诊处方 2-急诊处方 3-住院处方）',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '总金额',
  `drug_count` int DEFAULT 0 COMMENT '药品数量',
  `usage_instruction` varchar(500) COMMENT '用法说明',
  `dose_count` int COMMENT '中药饮片剂数',
  `decoct_flag` tinyint COMMENT '中药煎服方式（1-代煎 2-自煎）',
  `diagnosis` varchar(500) COMMENT '诊断',
  `prescription_status` tinyint DEFAULT 1 COMMENT '处方状态（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）',
  `payment_status` tinyint DEFAULT 0 COMMENT '缴费状态（0-未缴费 1-已缴费 2-已退费）',
  `is_long_prescription` tinyint NOT NULL DEFAULT 0 COMMENT '长处方（0-否 1-是）',
  `long_prescription_days` int COMMENT '长处方用药天数',
  `pay_time` datetime COMMENT '缴费时间',
  `pay_amount` decimal(10,2) DEFAULT 0.00 COMMENT '实付金额',
  `pay_method` tinyint COMMENT '支付方式（1-现金 2-微信 3-支付宝 4-医保卡 5-余额）',
  `submit_time` datetime COMMENT '提交时间',
  `audit_time` datetime COMMENT '审核时间',
  `audit_result` tinyint COMMENT '审核结果',
  `return_reason` varchar(500) COMMENT '最近一次审方退回原因',
  `return_time` datetime COMMENT '最近一次退回时间',
  `return_count` int NOT NULL DEFAULT 0 COMMENT '累计被退回次数',
  `audit_by` varchar(64) COMMENT '审核人',
  `audit_sign_id` bigint COMMENT '审方药师签名ID',
  `audit_signed_time` datetime COMMENT '审方签名时刻',
  `dispense_time` datetime COMMENT '发药时间',
  `dispense_by` varchar(64) COMMENT '发药人',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(200) COMMENT '取消原因',
  `refund_time` datetime COMMENT '退药时间',
  `refund_by` varchar(64) COMMENT '退药人',
  `refund_reason` varchar(200) COMMENT '退药原因',
  `is_urgent` tinyint DEFAULT 0 COMMENT '是否加急（0-否 1-是）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prescription_no` (`prescription_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方主表';

-- biz_prescription_detail  处方明细
CREATE TABLE `biz_prescription_detail` (
  `id` bigint NOT NULL,
  `prescription_id` bigint NOT NULL COMMENT '处方ID',
  `prescription_no` varchar(32) NOT NULL COMMENT '处方号',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) NOT NULL COMMENT '药品编码',
  `drug_name` varchar(200) NOT NULL COMMENT '药品名称',
  `generic_name` varchar(200) COMMENT '通用名',
  `specification` varchar(100) COMMENT '规格',
  `dosage_form` varchar(50) COMMENT '剂型',
  `manufacturer` varchar(200) COMMENT '生产厂家',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `quantity` decimal(10,2) NOT NULL COMMENT '数量',
  `price` decimal(10,4) COMMENT '单价',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `usage_dosage` varchar(100) NOT NULL COMMENT '用法用量',
  `frequency` varchar(50) NOT NULL COMMENT '用药频次',
  `route` varchar(50) NOT NULL COMMENT '用药途径',
  `duration` int COMMENT '疗程天数',
  `single_dosage` varchar(50) COMMENT '单次剂量',
  `total_dosage` decimal(10,2) COMMENT '总剂量',
  `is_skin_test` tinyint DEFAULT 0 COMMENT '是否需要皮试（0-否 1-是）',
  `skin_test_result` tinyint COMMENT '皮试结果（0-阴性 1-阳性）',
  `is_allergy` tinyint DEFAULT 0 COMMENT '是否过敏（0-否 1-是）',
  `is_combo` tinyint DEFAULT 0 COMMENT '是否组合药（0-否 1-是）',
  `combo_group` int COMMENT '组合组号',
  `is_special` tinyint DEFAULT 0 COMMENT '是否特殊用药（0-否 1-是）',
  `special_reason` varchar(200) COMMENT '特殊用药原因',
  `detail_status` tinyint DEFAULT 1 COMMENT '明细状态（1-正常 2-已发药 3-已退药）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `payment_status` tinyint DEFAULT 0 COMMENT '缴费状态（0-未缴费 1-已缴费 2-已退费）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细';

-- sys_department  科室
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_code` varchar(32) NOT NULL COMMENT '科室编码（唯一）',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `dept_type` varchar(20) NOT NULL COMMENT '科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父科室ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `dept_icon` varchar(200) COMMENT '科室图标',
  `dept_desc` varchar(500) COMMENT '科室描述',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `location` varchar(200) COMMENT '科室位置',
  `dept_leader_id` bigint COMMENT '科室负责人（sys_employee.id)',
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

-- sys_drug  药品字典
CREATE TABLE `sys_drug` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `drug_code` varchar(32) NOT NULL COMMENT '药品编码（唯一）',
  `drug_name` varchar(200) NOT NULL COMMENT '药品名称',
  `drug_type` tinyint NOT NULL DEFAULT 1 COMMENT '药品类型（1-西药 2-中成药 3-中药饮片）',
  `generic_name` varchar(200) COMMENT '通用名',
  `trade_name` varchar(200) COMMENT '商品名',
  `specification` varchar(100) COMMENT '规格',
  `dosage_form` varchar(50) COMMENT '剂型（片剂、胶囊、注射剂等）',
  `unit` varchar(20) COMMENT '单位（片、粒、支等）',
  `gram_per_unit` decimal(10,3) COMMENT '每最小库存单位含多少克',
  `manufacturer` varchar(200) COMMENT '生产厂家',
  `approval_number` varchar(100) COMMENT '批准文号',
  `barcode` varchar(50) COMMENT '条形码',
  `trace_di` varchar(32) COMMENT '药品追溯码产品标识',
  `trace_code_prefix` varchar(16) COMMENT '中国药品追溯码本体码',
  `is_trace_required` tinyint NOT NULL DEFAULT 0 COMMENT '是否要求扫码采集追溯码（0-不要求 1-必须采集）',
  `category_id` bigint COMMENT '药品分类ID',
  `category_name` varchar(100) COMMENT '药品分类名称',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '单价',
  `cost_price` decimal(10,2) DEFAULT 0.00 COMMENT '成本价',
  `retail_price` decimal(10,2) DEFAULT 0.00 COMMENT '零售价',
  `is_medical_insurance` tinyint DEFAULT 0 COMMENT '是否医保药品（0-否 1-是）',
  `medical_insurance_code` varchar(50) COMMENT '医保编码',
  `storage_condition` varchar(200) COMMENT '储存条件',
  `shelf_life` int DEFAULT 0 COMMENT '有效期（月）',
  `is_skin_test` tinyint DEFAULT 0 COMMENT '是否需要皮试（0-否 1-是）',
  `is_cold_chain` tinyint DEFAULT 0 COMMENT '是否冷链药品（0-否 1-是）',
  `special_flag` tinyint NOT NULL DEFAULT 0 COMMENT '特殊管理分类（0-普通 1-麻醉药品 2-第一类精神药品 3-第二类精神药品 4-毒性药品）',
  `antibiotic_level` tinyint NOT NULL DEFAULT 0 COMMENT '抗菌药物分级（0-非抗菌药物 1-非限制使用级 2-限制使用级 3-特殊使用级）',
  `ddd_value` decimal(10,4) COMMENT 'WHO 限定日剂量（g/日）',
  `ddd_unit_gram` decimal(12,4) COMMENT '每发药单位（盒/瓶/支）',
  `contraindication` text COMMENT '禁忌症',
  `adverse_reaction` text COMMENT '不良反应',
  `usage_dosage` text COMMENT '用法用量',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drug_code` (`drug_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品字典';

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

-- sys_supplier  供应商
CREATE TABLE `sys_supplier` (
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `supplier_code` varchar(32) NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(128) NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(32) COMMENT '联系人',
  `phone` varchar(32) COMMENT '联系电话',
  `address` varchar(256) COMMENT '地址',
  `license_no` varchar(64) COMMENT '营业执照号',
  `license_expiry` date COMMENT '资质证照有效期',
  `rating` tinyint DEFAULT 3 COMMENT '评级（1-差 2-一般 3-良好 4-优秀）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-正常）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`supplier_id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商';

-- sys_ward  病区
CREATE TABLE `sys_ward` (
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_code` varchar(32) NOT NULL COMMENT '病区编码',
  `ward_name` varchar(64) NOT NULL COMMENT '病区名称',
  `dept_id` bigint NOT NULL COMMENT '所属科室ID',
  `total_beds` int NOT NULL DEFAULT 0 COMMENT '总床位数',
  `occupied_beds` int NOT NULL DEFAULT 0 COMMENT '已占用床位数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-正常）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`ward_id`),
  UNIQUE KEY `uk_ward_code` (`ward_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病区';
-- ---------------- 参照关系（E-R 连线） ----------------
ALTER TABLE `biz_drug_dispensing` ADD CONSTRAINT `fk_biz_drug_dispensing_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_prescription` (`id`);
ALTER TABLE `biz_drug_dispensing` ADD CONSTRAINT `fk_biz_drug_dispensing_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_drug_dispensing` ADD CONSTRAINT `fk_biz_drug_dispensing_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_drug_dispensing` ADD CONSTRAINT `fk_biz_drug_dispensing_pharmacist_id` FOREIGN KEY (`pharmacist_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_drug_dispensing` ADD CONSTRAINT `fk_biz_drug_dispensing_prescription_detail_id` FOREIGN KEY (`prescription_detail_id`) REFERENCES `biz_prescription_detail` (`id`);
ALTER TABLE `biz_drug_inbound` ADD CONSTRAINT `fk_biz_drug_inbound_purchase_order_id` FOREIGN KEY (`purchase_order_id`) REFERENCES `biz_purchase_order` (`order_id`);
ALTER TABLE `biz_drug_inbound_detail` ADD CONSTRAINT `fk_biz_drug_inbound_detail_inbound_id` FOREIGN KEY (`inbound_id`) REFERENCES `biz_drug_inbound` (`id`);
ALTER TABLE `biz_drug_inbound_detail` ADD CONSTRAINT `fk_biz_drug_inbound_detail_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_drug_outbound_detail` ADD CONSTRAINT `fk_biz_drug_outbound_detail_outbound_id` FOREIGN KEY (`outbound_id`) REFERENCES `biz_drug_outbound` (`id`);
ALTER TABLE `biz_drug_outbound_detail` ADD CONSTRAINT `fk_biz_drug_outbound_detail_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_drug_package` ADD CONSTRAINT `fk_biz_drug_package_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_drug_package_detail` ADD CONSTRAINT `fk_biz_drug_package_detail_package_id` FOREIGN KEY (`package_id`) REFERENCES `biz_drug_package` (`id`);
ALTER TABLE `biz_drug_stock` ADD CONSTRAINT `fk_biz_drug_stock_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_drug_stock` ADD CONSTRAINT `fk_biz_drug_stock_supplier_id` FOREIGN KEY (`supplier_id`) REFERENCES `sys_supplier` (`supplier_id`);
ALTER TABLE `biz_drug_stock_log` ADD CONSTRAINT `fk_biz_drug_stock_log_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `biz_drug_stock` (`id`);
ALTER TABLE `biz_drug_stock_log` ADD CONSTRAINT `fk_biz_drug_stock_log_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_drug_supplier_return` ADD CONSTRAINT `fk_biz_drug_supplier_return_supplier_id` FOREIGN KEY (`supplier_id`) REFERENCES `sys_supplier` (`supplier_id`);
ALTER TABLE `biz_drug_supplier_return_item` ADD CONSTRAINT `fk_biz_drug_supplier_return_item_return_id` FOREIGN KEY (`return_id`) REFERENCES `biz_drug_supplier_return` (`id`);
ALTER TABLE `biz_drug_supplier_return_item` ADD CONSTRAINT `fk_biz_drug_supplier_return_item_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `biz_consumable_stock` (`id`);
ALTER TABLE `biz_drug_supplier_return_item` ADD CONSTRAINT `fk_biz_drug_supplier_return_item_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_drug_supplier_return_item` ADD CONSTRAINT `fk_biz_drug_supplier_return_item_supplier_id` FOREIGN KEY (`supplier_id`) REFERENCES `sys_supplier` (`supplier_id`);
ALTER TABLE `biz_drug_trace` ADD CONSTRAINT `fk_biz_drug_trace_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_drug_trace` ADD CONSTRAINT `fk_biz_drug_trace_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `biz_drug_stock` (`id`);
ALTER TABLE `biz_drug_trace` ADD CONSTRAINT `fk_biz_drug_trace_supplier_id` FOREIGN KEY (`supplier_id`) REFERENCES `sys_supplier` (`supplier_id`);
ALTER TABLE `biz_drug_trace` ADD CONSTRAINT `fk_biz_drug_trace_inbound_id` FOREIGN KEY (`inbound_id`) REFERENCES `biz_drug_inbound` (`id`);
ALTER TABLE `biz_drug_trace` ADD CONSTRAINT `fk_biz_drug_trace_dispensing_id` FOREIGN KEY (`dispensing_id`) REFERENCES `biz_drug_dispensing` (`id`);
ALTER TABLE `biz_drug_trace` ADD CONSTRAINT `fk_biz_drug_trace_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_drug_trace` ADD CONSTRAINT `fk_biz_drug_trace_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_drug_trace` ADD CONSTRAINT `fk_biz_drug_trace_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_drug_trace` ADD CONSTRAINT `fk_biz_drug_trace_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_drug_transfer_item` ADD CONSTRAINT `fk_biz_drug_transfer_item_transfer_id` FOREIGN KEY (`transfer_id`) REFERENCES `biz_drug_transfer` (`id`);
ALTER TABLE `biz_drug_transfer_item` ADD CONSTRAINT `fk_biz_drug_transfer_item_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `biz_consumable_stock` (`id`);
ALTER TABLE `biz_drug_transfer_item` ADD CONSTRAINT `fk_biz_drug_transfer_item_in_stock_id` FOREIGN KEY (`in_stock_id`) REFERENCES `biz_consumable_stock` (`id`);
ALTER TABLE `biz_drug_transfer_item` ADD CONSTRAINT `fk_biz_drug_transfer_item_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_pivas_batch` ADD CONSTRAINT `fk_biz_pivas_batch_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_pivas_batch` ADD CONSTRAINT `fk_biz_pivas_batch_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_pivas_batch` ADD CONSTRAINT `fk_biz_pivas_batch_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_pivas_batch` ADD CONSTRAINT `fk_biz_pivas_batch_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_pivas_item` ADD CONSTRAINT `fk_biz_pivas_item_pivas_id` FOREIGN KEY (`pivas_id`) REFERENCES `biz_pivas_batch` (`id`);
ALTER TABLE `biz_pivas_item` ADD CONSTRAINT `fk_biz_pivas_item_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_inpatient_order` (`id`);
ALTER TABLE `biz_pivas_item` ADD CONSTRAINT `fk_biz_pivas_item_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_pivas_item` ADD CONSTRAINT `fk_biz_pivas_item_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_pivas_item` ADD CONSTRAINT `fk_biz_pivas_item_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_pivas_item` ADD CONSTRAINT `fk_biz_pivas_item_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_pivas_item` ADD CONSTRAINT `fk_biz_pivas_item_auditor_id` FOREIGN KEY (`auditor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_pivas_item` ADD CONSTRAINT `fk_biz_pivas_item_compounder_id` FOREIGN KEY (`compounder_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_pivas_item` ADD CONSTRAINT `fk_biz_pivas_item_verifier_id` FOREIGN KEY (`verifier_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_purchase_order` ADD CONSTRAINT `fk_biz_purchase_order_supplier_id` FOREIGN KEY (`supplier_id`) REFERENCES `sys_supplier` (`supplier_id`);
ALTER TABLE `biz_purchase_order` ADD CONSTRAINT `fk_biz_purchase_order_approver_id` FOREIGN KEY (`approver_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_purchase_order_detail` ADD CONSTRAINT `fk_biz_purchase_order_detail_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_purchase_order` (`order_id`);
ALTER TABLE `biz_purchase_order_detail` ADD CONSTRAINT `fk_biz_purchase_order_detail_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_stocktake_item` ADD CONSTRAINT `fk_biz_stocktake_item_stocktake_id` FOREIGN KEY (`stocktake_id`) REFERENCES `biz_stocktake` (`id`);
ALTER TABLE `biz_stocktake_item` ADD CONSTRAINT `fk_biz_stocktake_item_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `biz_drug_stock` (`id`);
ALTER TABLE `biz_stocktake_item` ADD CONSTRAINT `fk_biz_stocktake_item_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_ward_dispense` ADD CONSTRAINT `fk_biz_ward_dispense_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_ward_dispense` ADD CONSTRAINT `fk_biz_ward_dispense_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_ward_dispense` ADD CONSTRAINT `fk_biz_ward_dispense_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_ward_dispense` ADD CONSTRAINT `fk_biz_ward_dispense_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_ward_dispense_item` ADD CONSTRAINT `fk_biz_ward_dispense_item_dispense_id` FOREIGN KEY (`dispense_id`) REFERENCES `biz_ward_dispense` (`id`);
ALTER TABLE `biz_ward_dispense_item` ADD CONSTRAINT `fk_biz_ward_dispense_item_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_inpatient_order` (`id`);
ALTER TABLE `biz_ward_dispense_item` ADD CONSTRAINT `fk_biz_ward_dispense_item_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_ward_dispense_item` ADD CONSTRAINT `fk_biz_ward_dispense_item_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_ward_dispense_item` ADD CONSTRAINT `fk_biz_ward_dispense_item_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_ward_dispense_item` ADD CONSTRAINT `fk_biz_ward_dispense_item_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_ward_dispense_item` ADD CONSTRAINT `fk_biz_ward_dispense_item_fee_record_id` FOREIGN KEY (`fee_record_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_ward_dispense_item` ADD CONSTRAINT `fk_biz_ward_dispense_item_dispenser_id` FOREIGN KEY (`dispenser_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_ward_dispense_item` ADD CONSTRAINT `fk_biz_ward_dispense_item_checker_id` FOREIGN KEY (`checker_id`) REFERENCES `sys_employee` (`id`);
