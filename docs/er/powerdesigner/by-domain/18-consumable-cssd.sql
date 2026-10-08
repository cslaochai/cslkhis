-- ============================================================
-- 领域 18 耗材与消毒供应（CSSD）（本域 8 表 + 上游参照 6 表 / 15 条关系）
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


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
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材批次库存';

-- biz_consumable_stock_log  耗材出入库流水
CREATE TABLE `biz_consumable_stock_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `stock_id` bigint NOT NULL COMMENT '库存批次ID',
  `consumable_id` bigint NOT NULL COMMENT '耗材ID',
  `batch_no` varchar(50) COMMENT '批号',
  `change_type` tinyint NOT NULL COMMENT '变动类型（1-入库 2-领用出库 3-退回入库 4-其他出库 5-盘盈 6-盘亏）',
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
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材出入库流水';

-- biz_consumable_consume  耗材科室领用台账
CREATE TABLE `biz_consumable_consume` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `consume_no` varchar(32) NOT NULL COMMENT '领用单号',
  `consumable_id` bigint NOT NULL COMMENT '耗材ID',
  `consumable_name` varchar(100) COMMENT '耗材名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `quantity` decimal(10,2) NOT NULL COMMENT '领用数量',
  `dept_id` bigint COMMENT '领用科室ID',
  `dept_name` varchar(100) COMMENT '领用科室名称',
  `purpose` varchar(200) COMMENT '用途',
  `consume_time` datetime COMMENT '领用时间',
  `operator_name` varchar(50) COMMENT '经办人',
  `stock_before` decimal(10,2) COMMENT '领用前该耗材全部批次合计',
  `stock_after` decimal(10,2) COMMENT '领用后该耗材全部批次合计',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材科室领用台账';

-- biz_consumable_trace  高值耗材使用溯源
CREATE TABLE `biz_consumable_trace` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `trace_no` varchar(32) NOT NULL COMMENT '院内追溯码',
  `udi_code` varchar(255) NOT NULL COMMENT 'UDI 原文',
  `udi_di` varchar(32) COMMENT '解析-产品标识',
  `udi_serial` varchar(64) COMMENT '解析-序列号',
  `udi_batch` varchar(64) COMMENT '解析-批号',
  `udi_expiry_date` date COMMENT '解析-有效期',
  `consumable_id` bigint NOT NULL COMMENT '耗材ID',
  `consumable_code` varchar(32) COMMENT '耗材编码',
  `consumable_name` varchar(100) COMMENT '耗材名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `reg_cert_no` varchar(100) COMMENT '注册证号',
  `retail_price` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '计费单价快照',
  `stock_id` bigint NOT NULL COMMENT '出库批次ID',
  `batch_no` varchar(50) COMMENT '批号',
  `supplier` varchar(200) COMMENT '供应商',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `visit_type` tinyint NOT NULL DEFAULT 1 COMMENT '就诊类型（1-门诊 2-住院）',
  `regist_id` bigint COMMENT '门诊挂号ID',
  `admission_id` bigint COMMENT '住院ID',
  `dept_id` bigint COMMENT '使用科室ID',
  `dept_name` varchar(100) COMMENT '使用科室名称',
  `usage_time` datetime COMMENT '使用时间',
  `operator_name` varchar(50) COMMENT '登记人',
  `charge_status` tinyint NOT NULL DEFAULT 0 COMMENT '计费状态（0-未计费 1-已计费 2-计费失败）',
  `fee_no` varchar(64) COMMENT '记账单号',
  `fee_record_id` bigint COMMENT '记账行ID',
  `charge_fail_reason` varchar(500) COMMENT '未计费/计费失败原因',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '记录状态（1-使用中 2-已作废）',
  `void_time` datetime COMMENT '作废时间',
  `void_reason` varchar(200) COMMENT '作废原因',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trace_no` (`trace_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='高值耗材使用溯源';

-- biz_cssd_pack_template  CSSD 器械包模板
CREATE TABLE `biz_cssd_pack_template` (
  `id` bigint NOT NULL COMMENT '器械包模板ID',
  `template_code` varchar(32) NOT NULL COMMENT '包编码',
  `pack_name` varchar(128) NOT NULL COMMENT '器械包名称',
  `sterilize_method` tinyint NOT NULL DEFAULT 1 COMMENT '默认灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CSSD 器械包模板';

-- biz_cssd_pack_template_item  CSSD 器械包模板明细
CREATE TABLE `biz_cssd_pack_template_item` (
  `id` bigint NOT NULL COMMENT '明细ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `item_name` varchar(128) NOT NULL COMMENT '器械/耗材名称',
  `spec` varchar(64) COMMENT '规格',
  `unit` varchar(16) NOT NULL DEFAULT '件' COMMENT '计量单位',
  `quantity` int NOT NULL COMMENT '基数（数量）',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CSSD 器械包模板明细';

-- biz_cssd_pack  CSSD 器械包
CREATE TABLE `biz_cssd_pack` (
  `id` bigint NOT NULL COMMENT '器械包ID',
  `pack_no` varchar(32) NOT NULL COMMENT '器械包条码',
  `pack_name` varchar(128) NOT NULL COMMENT '器械包名称',
  `dept_id` bigint COMMENT '申领/归属科室ID',
  `dept_name` varchar(100) COMMENT '申领/归属科室名称',
  `sterilize_method` tinyint NOT NULL DEFAULT 1 COMMENT '灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '包状态（1-已回收 2-清洗中 3-已打包 4-灭菌中 5-待发放 6-已发放）',
  `sterilizer_no` varchar(32) COMMENT '最近灭菌锅次',
  `batch_no` varchar(32) COMMENT '灭菌批次号',
  `last_node_time` datetime COMMENT '最近流转时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_cssd_pack_no` (`pack_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CSSD 器械包';

-- biz_cssd_trace  CSSD 追溯节点
CREATE TABLE `biz_cssd_trace` (
  `id` bigint NOT NULL COMMENT '追溯节点ID',
  `pack_id` bigint NOT NULL COMMENT '器械包ID',
  `pack_no` varchar(32) COMMENT '器械包条码',
  `node_type` tinyint NOT NULL COMMENT '节点类型（1-回收 2-清洗 3-打包 4-灭菌 5-储存 6-发放）',
  `node_time` datetime NOT NULL COMMENT '节点时间',
  `operator_name` varchar(50) COMMENT '操作人',
  `sterilizer_no` varchar(32) COMMENT '灭菌锅次',
  `batch_no` varchar(32) COMMENT '灭菌批次号',
  `result` tinyint NOT NULL DEFAULT 1 COMMENT '节点结果（1-合格 2-不合格）',
  `remark` varchar(500) COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CSSD 追溯节点';

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
  `bill_no` varchar(32) COMMENT '账单号',
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

-- sys_consumable  耗材字典
CREATE TABLE `sys_consumable` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `consumable_code` varchar(32) NOT NULL COMMENT '耗材编码（唯一）',
  `consumable_name` varchar(100) NOT NULL COMMENT '耗材名称',
  `category` tinyint DEFAULT 5 COMMENT '类别（1-卫生材料 2-注射穿刺 3-医用敷料 4-防护用品 5-其他）',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位（包、支、盒、个等）',
  `manufacturer` varchar(200) COMMENT '生产厂家',
  `retail_price` decimal(10,2) DEFAULT 0.00 COMMENT '零售价',
  `is_high_value` tinyint NOT NULL DEFAULT 0 COMMENT '是否高值耗材（0-普通 1-高值）',
  `udi_di` varchar(32) COMMENT '产品级UDI-DI',
  `reg_cert_no` varchar(100) COMMENT '医疗器械注册证/备案号',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consumable_code` (`consumable_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材字典';

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

-- ---------------- 参照关系（E-R 连线） ----------------
ALTER TABLE `biz_consumable_consume` ADD CONSTRAINT `fk_biz_consumable_consume_consumable_id` FOREIGN KEY (`consumable_id`) REFERENCES `sys_consumable` (`id`);
ALTER TABLE `biz_consumable_consume` ADD CONSTRAINT `fk_biz_consumable_consume_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_consumable_stock` ADD CONSTRAINT `fk_biz_consumable_stock_consumable_id` FOREIGN KEY (`consumable_id`) REFERENCES `sys_consumable` (`id`);
ALTER TABLE `biz_consumable_stock_log` ADD CONSTRAINT `fk_biz_consumable_stock_log_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `biz_consumable_stock` (`id`);
ALTER TABLE `biz_consumable_stock_log` ADD CONSTRAINT `fk_biz_consumable_stock_log_consumable_id` FOREIGN KEY (`consumable_id`) REFERENCES `sys_consumable` (`id`);
ALTER TABLE `biz_consumable_trace` ADD CONSTRAINT `fk_biz_consumable_trace_consumable_id` FOREIGN KEY (`consumable_id`) REFERENCES `sys_consumable` (`id`);
ALTER TABLE `biz_consumable_trace` ADD CONSTRAINT `fk_biz_consumable_trace_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `biz_consumable_stock` (`id`);
ALTER TABLE `biz_consumable_trace` ADD CONSTRAINT `fk_biz_consumable_trace_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_consumable_trace` ADD CONSTRAINT `fk_biz_consumable_trace_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_consumable_trace` ADD CONSTRAINT `fk_biz_consumable_trace_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_consumable_trace` ADD CONSTRAINT `fk_biz_consumable_trace_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_consumable_trace` ADD CONSTRAINT `fk_biz_consumable_trace_fee_record_id` FOREIGN KEY (`fee_record_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_cssd_pack` ADD CONSTRAINT `fk_biz_cssd_pack_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_cssd_pack_template_item` ADD CONSTRAINT `fk_biz_cssd_pack_template_item_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_cssd_pack_template` (`id`);
ALTER TABLE `biz_cssd_trace` ADD CONSTRAINT `fk_biz_cssd_trace_pack_id` FOREIGN KEY (`pack_id`) REFERENCES `biz_cssd_pack` (`id`);
