-- 领域：18-耗材与消毒供应（CSSD）
-- 库：hn_biz_his    表数：8
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- biz_consumable_stock  耗材批次库存
-- ----------------------------
CREATE TABLE `biz_consumable_stock` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `consumable_id` bigint NOT NULL COMMENT '耗材ID',
  `batch_no` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '批号',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '有效期',
  `quantity` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '库存数量',
  `cost_price` decimal(10,2) DEFAULT '0.00' COMMENT '成本价',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '库存金额',
  `location` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '存放位置',
  `supplier` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '供应商',
  `stock_status` tinyint DEFAULT '1' COMMENT '库存状态（1-正常 2-预警 3-缺货 4-过期）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_cstock_consumable` (`consumable_id`),
  KEY `idx_cstock_expiry` (`expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='耗材批次库存';

-- ----------------------------
-- biz_consumable_stock_log  耗材出入库流水
-- ----------------------------
CREATE TABLE `biz_consumable_stock_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `stock_id` bigint NOT NULL COMMENT '库存批次ID',
  `consumable_id` bigint NOT NULL COMMENT '耗材ID',
  `batch_no` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '批号',
  `change_type` tinyint NOT NULL COMMENT '变动类型（1-入库 2-领用出库 3-退回入库 4-其他出库 5-盘盈 6-盘亏）',
  `change_quantity` decimal(10,2) NOT NULL COMMENT '变动数量',
  `quantity_before` decimal(10,2) NOT NULL COMMENT '变动前批次数量',
  `quantity_after` decimal(10,2) NOT NULL COMMENT '变动后批次数量',
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源类型',
  `source_id` bigint DEFAULT NULL COMMENT '来源单据ID',
  `source_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源单据号',
  `operator_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_clog_consumable` (`consumable_id`),
  KEY `idx_clog_stock` (`stock_id`),
  KEY `idx_clog_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='耗材出入库流水';

-- ----------------------------
-- biz_consumable_consume  耗材科室领用台账
-- ----------------------------
CREATE TABLE `biz_consumable_consume` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `consume_no` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '领用单号',
  `consumable_id` bigint NOT NULL COMMENT '耗材ID',
  `consumable_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '耗材名称',
  `specification` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',
  `quantity` decimal(10,2) NOT NULL COMMENT '领用数量',
  `dept_id` bigint DEFAULT NULL COMMENT '领用科室ID',
  `dept_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '领用科室名称',
  `purpose` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用途',
  `consume_time` datetime DEFAULT NULL COMMENT '领用时间',
  `operator_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '经办人',
  `stock_before` decimal(10,2) DEFAULT NULL COMMENT '领用前该耗材全部批次合计',
  `stock_after` decimal(10,2) DEFAULT NULL COMMENT '领用后该耗材全部批次合计',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_cconsume_no` (`consume_no`),
  KEY `idx_cconsume_consumable` (`consumable_id`),
  KEY `idx_cconsume_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='耗材科室领用台账';

-- ----------------------------
-- biz_consumable_trace  高值耗材使用溯源
-- ----------------------------
CREATE TABLE `biz_consumable_trace` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `trace_no` varchar(32) NOT NULL COMMENT '院内追溯码',
  `udi_code` varchar(255) NOT NULL COMMENT 'UDI 原文',
  `udi_di` varchar(32) DEFAULT NULL COMMENT '解析-产品标识',
  `udi_serial` varchar(64) DEFAULT NULL COMMENT '解析-序列号',
  `udi_batch` varchar(64) DEFAULT NULL COMMENT '解析-批号',
  `udi_expiry_date` date DEFAULT NULL COMMENT '解析-有效期',
  `consumable_id` bigint NOT NULL COMMENT '耗材ID',
  `consumable_code` varchar(32) DEFAULT NULL COMMENT '耗材编码',
  `consumable_name` varchar(100) DEFAULT NULL COMMENT '耗材名称',
  `specification` varchar(100) DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `reg_cert_no` varchar(100) DEFAULT NULL COMMENT '注册证号',
  `retail_price` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '计费单价快照',
  `stock_id` bigint NOT NULL COMMENT '出库批次ID',
  `batch_no` varchar(50) DEFAULT NULL COMMENT '批号',
  `supplier` varchar(200) DEFAULT NULL COMMENT '供应商',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `visit_type` tinyint NOT NULL DEFAULT '1' COMMENT '就诊类型（1-门诊 2-住院）',
  `regist_id` bigint DEFAULT NULL COMMENT '门诊挂号ID',
  `admission_id` bigint DEFAULT NULL COMMENT '住院ID',
  `dept_id` bigint DEFAULT NULL COMMENT '使用科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '使用科室名称',
  `usage_time` datetime DEFAULT NULL COMMENT '使用时间',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '登记人',
  `charge_status` tinyint NOT NULL DEFAULT '0' COMMENT '计费状态（0-未计费 1-已计费 2-计费失败）',
  `fee_no` varchar(64) DEFAULT NULL COMMENT '记账单号',
  `fee_record_id` bigint DEFAULT NULL COMMENT '记账行ID',
  `charge_fail_reason` varchar(500) DEFAULT NULL COMMENT '未计费/计费失败原因',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '记录状态（1-使用中 2-已作废）',
  `void_time` datetime DEFAULT NULL COMMENT '作废时间',
  `void_reason` varchar(200) DEFAULT NULL COMMENT '作废原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trace_no` (`trace_no`),
  KEY `idx_trace_udi_di` (`udi_di`),
  KEY `idx_trace_udi_code` (`udi_code`(64)),
  KEY `idx_trace_patient` (`patient_id`),
  KEY `idx_trace_consumable` (`consumable_id`),
  KEY `idx_trace_stock` (`stock_id`),
  KEY `idx_trace_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='高值耗材使用溯源';

-- ----------------------------
-- biz_cssd_pack_template  CSSD 器械包模板
-- ----------------------------
CREATE TABLE `biz_cssd_pack_template` (
  `id` bigint NOT NULL COMMENT '器械包模板ID',
  `template_code` varchar(32) NOT NULL COMMENT '包编码',
  `pack_name` varchar(128) NOT NULL COMMENT '器械包名称',
  `sterilize_method` tinyint NOT NULL DEFAULT '1' COMMENT '默认灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  KEY `idx_bcpt_code` (`template_code`),
  KEY `idx_bcpt_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CSSD 器械包模板';

-- ----------------------------
-- biz_cssd_pack_template_item  CSSD 器械包模板明细
-- ----------------------------
CREATE TABLE `biz_cssd_pack_template_item` (
  `id` bigint NOT NULL COMMENT '明细ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `item_name` varchar(128) NOT NULL COMMENT '器械/耗材名称',
  `spec` varchar(64) DEFAULT NULL COMMENT '规格',
  `unit` varchar(16) NOT NULL DEFAULT '件' COMMENT '计量单位',
  `quantity` int NOT NULL COMMENT '基数（数量）',
  `sort_no` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_bcpti_tpl` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CSSD 器械包模板明细';

-- ----------------------------
-- biz_cssd_pack  CSSD 器械包
-- ----------------------------
CREATE TABLE `biz_cssd_pack` (
  `id` bigint NOT NULL COMMENT '器械包ID',
  `pack_no` varchar(32) NOT NULL COMMENT '器械包条码',
  `pack_name` varchar(128) NOT NULL COMMENT '器械包名称',
  `dept_id` bigint DEFAULT NULL COMMENT '申领/归属科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '申领/归属科室名称',
  `sterilize_method` tinyint NOT NULL DEFAULT '1' COMMENT '灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '包状态（1-已回收 2-清洗中 3-已打包 4-灭菌中 5-待发放 6-已发放）',
  `sterilizer_no` varchar(32) DEFAULT NULL COMMENT '最近灭菌锅次',
  `batch_no` varchar(32) DEFAULT NULL COMMENT '灭菌批次号',
  `last_node_time` datetime DEFAULT NULL COMMENT '最近流转时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_cssd_pack_no` (`pack_no`),
  KEY `idx_bcp_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CSSD 器械包';

-- ----------------------------
-- biz_cssd_trace  CSSD 追溯节点
-- ----------------------------
CREATE TABLE `biz_cssd_trace` (
  `id` bigint NOT NULL COMMENT '追溯节点ID',
  `pack_id` bigint NOT NULL COMMENT '器械包ID',
  `pack_no` varchar(32) DEFAULT NULL COMMENT '器械包条码',
  `node_type` tinyint NOT NULL COMMENT '节点类型（1-回收 2-清洗 3-打包 4-灭菌 5-储存 6-发放）',
  `node_time` datetime NOT NULL COMMENT '节点时间',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人',
  `sterilizer_no` varchar(32) DEFAULT NULL COMMENT '灭菌锅次',
  `batch_no` varchar(32) DEFAULT NULL COMMENT '灭菌批次号',
  `result` tinyint NOT NULL DEFAULT '1' COMMENT '节点结果（1-合格 2-不合格）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_bct_pack` (`pack_id`),
  KEY `idx_bct_time` (`node_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='CSSD 追溯节点';
