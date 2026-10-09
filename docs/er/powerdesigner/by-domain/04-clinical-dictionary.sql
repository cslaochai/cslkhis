-- ============================================================
-- 领域 04 临床字典与项目目录（药品·耗材·诊疗项目·诊断编码）（本域 21 表 + 上游参照 2 表 / 10 条关系）
-- 由 workspace/_er/refresh.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


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

-- sys_drug_price_history  药品价格变动史
CREATE TABLE `sys_drug_price_history` (
  `history_id` bigint NOT NULL COMMENT '历史记录ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `old_price` decimal(10,2) NOT NULL COMMENT '原价',
  `new_price` decimal(10,2) NOT NULL COMMENT '新价',
  `change_reason` varchar(256) COMMENT '调价原因',
  `operator_id` bigint COMMENT '操作人ID',
  `change_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调价时间',
  PRIMARY KEY (`history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品价格变动史';

-- sys_price_change_history  项目价格变更史
CREATE TABLE `sys_price_change_history` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `item_type` varchar(20) NOT NULL COMMENT '项目类型',
  `item_id` bigint NOT NULL COMMENT '项目ID',
  `item_code` varchar(50) COMMENT '项目编码',
  `item_name` varchar(100) COMMENT '项目名称（冗余）',
  `old_price` decimal(10,2) COMMENT '原价',
  `new_price` decimal(10,2) COMMENT '新价',
  `change_reason` varchar(200) COMMENT '调价原因',
  `operator_id` bigint COMMENT '操作人ID（员工ID）',
  `operator_name` varchar(50) COMMENT '操作人姓名',
  `change_time` datetime NOT NULL COMMENT '调价时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目价格变更史';

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
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consumable_code` (`consumable_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材字典';

-- sys_inspection_item  检查项目字典
CREATE TABLE `sys_inspection_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `item_code` varchar(32) NOT NULL COMMENT '项目编码（唯一）',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `item_type` tinyint NOT NULL DEFAULT 1 COMMENT '项目类型（1-放射检查 2-超声检查 3-心电图 4-内镜检查 5-其他）',
  `dept_id` bigint COMMENT '检查科室ID',
  `body_part` varchar(200) COMMENT '检查部位',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '检查价格',
  `duration` int DEFAULT 0 COMMENT '检查时长（分钟）',
  `preparation` varchar(500) COMMENT '检查前准备',
  `contraindication` varchar(500) COMMENT '检查禁忌',
  `report_template` text COMMENT '报告模板',
  `is_emergency` tinyint DEFAULT 0 COMMENT '是否支持急诊（0-否 1-是）',
  `is_appointment` tinyint DEFAULT 1 COMMENT '是否需要预约（0-否 1-是）',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查项目字典';

-- sys_laboratory_item  检验项目字典
CREATE TABLE `sys_laboratory_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `item_code` varchar(32) NOT NULL COMMENT '项目编码（唯一）',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `item_type` tinyint NOT NULL DEFAULT 1 COMMENT '项目类型（1-血液检验 2-尿液检验 3-生化检验 4-免疫检验 5-微生物检验 6-其他）',
  `dept_id` bigint COMMENT '检验科室ID',
  `specimen_type` varchar(50) COMMENT '标本类型（血液、尿液、粪便等）',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '检验价格',
  `duration` int DEFAULT 0 COMMENT '出报告时间（小时）',
  `reference_value` varchar(200) COMMENT '参考值范围',
  `unit` varchar(50) COMMENT '单位',
  `is_emergency` tinyint DEFAULT 0 COMMENT '是否支持急诊（0-否 1-是）',
  `is_fasting` tinyint DEFAULT 0 COMMENT '是否需要空腹（0-否 1-是）',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验项目字典';

-- sys_laboratory_item_detail  检验项目组套明细
CREATE TABLE `sys_laboratory_item_detail` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `laboratory_item_id` bigint NOT NULL COMMENT '检验大项目ID',
  `item_code` varchar(32) NOT NULL COMMENT '明细项目编码',
  `item_name` varchar(200) NOT NULL COMMENT '明细项目名称',
  `unit` varchar(50) COMMENT '单位',
  `reference_range` varchar(100) COMMENT '参考范围',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验项目组套明细';

-- sys_treatment_item  治疗项目字典
CREATE TABLE `sys_treatment_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `item_code` varchar(32) NOT NULL COMMENT '项目编码（唯一）',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `item_type` tinyint NOT NULL DEFAULT 1 COMMENT '项目类型（1-注射 2-输液 3-换药 4-拆线 5-其他）',
  `dept_id` bigint COMMENT '执行科室ID',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '治疗价格',
  `duration` int DEFAULT 0 COMMENT '治疗时长（分钟）',
  `usage_method` varchar(200) COMMENT '使用方法',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治疗项目字典';

-- sys_diagnosis  诊断字典
CREATE TABLE `sys_diagnosis` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `diagnosis_code` varchar(32) NOT NULL COMMENT '诊断编码',
  `diagnosis_name` varchar(200) NOT NULL COMMENT '诊断名称',
  `diagnosis_type` tinyint DEFAULT 1 COMMENT '诊断类型（1-西医诊断 2-中医诊断）',
  `category_name` varchar(100) COMMENT '诊断分类名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父诊断ID',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `is_common` tinyint DEFAULT 0 COMMENT '是否常用诊断（0-否 1-是）',
  `is_notifiable` tinyint DEFAULT 0 COMMENT '是否传染病（0-否 1-是）',
  `disease_stage` varchar(100) COMMENT '疾病分期',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_diagnosis_code` (`diagnosis_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诊断字典';

-- sys_icd10  ICD-10 诊断编码
CREATE TABLE `sys_icd10` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `icd_code` varchar(20) NOT NULL COMMENT 'ICD编码',
  `icd_name` varchar(200) NOT NULL COMMENT '疾病名称',
  `icd_category` varchar(100) COMMENT '分类',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-正常）',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ICD-10 诊断编码';

-- sys_icd9cm3  ICD-9-CM-3 手术编码
CREATE TABLE `sys_icd9cm3` (
  `id` bigint NOT NULL COMMENT '主键',
  `op_code` varchar(32) NOT NULL COMMENT '手术操作编码',
  `op_name` varchar(300) NOT NULL COMMENT '手术操作名称',
  `op_category` tinyint COMMENT '章节',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-正常）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_op_code` (`op_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ICD-9-CM-3 手术编码';

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

-- sys_patient_tag  患者标签
CREATE TABLE `sys_patient_tag` (
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `short_name` varchar(2) COMMENT '标签缩写用于展示',
  `tag_color` varchar(20) COMMENT '标签颜色',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者标签';

-- sys_infectious_disease  法定传染病目录
CREATE TABLE `sys_infectious_disease` (
  `id` bigint NOT NULL COMMENT '主键',
  `disease_code` varchar(16) NOT NULL COMMENT '病种编码',
  `disease_name` varchar(50) NOT NULL COMMENT '病种名称',
  `infectious_class` tinyint NOT NULL COMMENT '传染病类别',
  `deadline_hours` int NOT NULL COMMENT '报卡时限',
  `icd10` varchar(16) COMMENT '参考 ICD-10 编码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  `remark` varchar(255),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_infectious_disease_code` (`disease_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='法定传染病目录';

-- sys_single_disease  单病种质控目录
CREATE TABLE `sys_single_disease` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `disease_code` varchar(32) NOT NULL COMMENT '病种编码',
  `disease_name` varchar(100) NOT NULL COMMENT '病种名称',
  `icd10_prefix` varchar(200) NOT NULL COMMENT '纳入 ICD-10 前缀',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_disease_code` (`disease_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单病种质控目录';

-- sys_drg_group  DRG 分组与权重
CREATE TABLE `sys_drg_group` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `drg_code` varchar(32) NOT NULL COMMENT 'DRG 组编码',
  `drg_name` varchar(200) COMMENT 'DRG 组名称',
  `mdc_code` varchar(8) COMMENT 'MDC 主要诊断大类',
  `adrg_code` varchar(16) COMMENT 'ADRG 编码',
  `weight` decimal(10,4) COMMENT '权重',
  `pay_standard` decimal(10,2) COMMENT '病组支付标准（元）',
  `source` varchar(64) COMMENT '来源',
  `version` varchar(32) COMMENT '版本号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_drg_code` (`drg_code`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DRG 分组与权重';

-- sys_checkup_package  体检套餐
CREATE TABLE `sys_checkup_package` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `package_name` varchar(100) NOT NULL COMMENT '套餐名称',
  `package_code` varchar(32) COMMENT '套餐编码',
  `gender_limit` tinyint NOT NULL DEFAULT 0 COMMENT '适用性别（0-不限 1-男 2-女）',
  `price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '套餐价格（元）',
  `description` varchar(500) COMMENT '套餐说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_name` (`package_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检套餐';

-- sys_checkup_package_item  体检套餐项目
CREATE TABLE `sys_checkup_package_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `package_id` bigint NOT NULL COMMENT '套餐ID',
  `item_name` varchar(100) NOT NULL COMMENT '项目名称',
  `item_type` tinyint NOT NULL DEFAULT 1 COMMENT '项目类别（问诊/体格）（1-检验 2-检查 3-一般）',
  `ref_standard` varchar(200) COMMENT '参考范围/标准',
  `amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '单项金额（元）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检套餐项目';

-- biz_shift  班次字典
CREATE TABLE `biz_shift` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `shift_name` varchar(50) NOT NULL COMMENT '班次名称',
  `start_time` varchar(10) NOT NULL COMMENT '开始时间（HH:mm）',
  `end_time` varchar(10) NOT NULL COMMENT '结束时间（HH:mm）',
  `cross_day` tinyint NOT NULL DEFAULT 0 COMMENT '是否跨零点（0-不跨 1-次日收）',
  `is_night` tinyint NOT NULL DEFAULT 0 COMMENT '是否夜班（1-夜班 0-白班）：夜班流入判定与连续夜班上限的唯一依据',
  `need_rest_hours` decimal(4,1) NOT NULL DEFAULT 0.0 COMMENT '下此班后最短休息小时数（0-不限制；夜班通例取16）',
  `late_grace_minutes` int NOT NULL DEFAULT 15 COMMENT '迟到宽限（分钟）：签到晚于班次开始超过这个数才算迟到',
  `duration_minutes` int NOT NULL DEFAULT 0 COMMENT '时长（分钟）',
  `dept_id` bigint COMMENT '适用科室ID',
  `schedule_type` tinyint COMMENT '班次类型（1-上午 2-下午 3-全天 4-凌晨）',
  `use_scope` tinyint NOT NULL DEFAULT 1 COMMENT '班次适用域（1-门诊 2-病区护理排班）',
  `apply_staff_type` tinyint COMMENT '适用岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-全部岗位通用）',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次字典';

-- sys_drug_interaction  药物相互作用知识库
CREATE TABLE `sys_drug_interaction` (
  `id` bigint NOT NULL COMMENT '主键',
  `component_a` varchar(50) NOT NULL COMMENT '成分关键字A',
  `component_b` varchar(50) NOT NULL COMMENT '成分关键字B',
  `pair_key` varchar(120) NOT NULL COMMENT '成分对归一化键',
  `severity` tinyint NOT NULL COMMENT '严重度（1-禁忌 2-慎用）',
  `interaction_desc` varchar(500) NOT NULL COMMENT '相互作用后果',
  `suggestion` varchar(500) COMMENT '处理建议（换药/减量/监测什么指标）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pair_key` (`pair_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药物相互作用知识库';

-- sys_drug_dose_limit  药品剂量上限知识库
CREATE TABLE `sys_drug_dose_limit` (
  `id` bigint NOT NULL COMMENT '主键',
  `component` varchar(50) NOT NULL COMMENT '成分关键字',
  `dose_unit` varchar(10) NOT NULL COMMENT '剂量单位（，只有 g/mg/ug 三值可比）',
  `max_single_dose` decimal(12,4) COMMENT '单次最大量',
  `max_daily_dose` decimal(12,4) COMMENT '每日最大量',
  `note` varchar(200) COMMENT '口径说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dose_component` (`component`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品剂量上限知识库';

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
ALTER TABLE `biz_shift` ADD CONSTRAINT `fk_biz_shift_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_checkup_package_item` ADD CONSTRAINT `fk_sys_checkup_package_item_package_id` FOREIGN KEY (`package_id`) REFERENCES `sys_checkup_package` (`id`);
ALTER TABLE `sys_diagnosis` ADD CONSTRAINT `fk_sys_diagnosis_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `sys_diagnosis` (`id`);
ALTER TABLE `sys_drug_price_history` ADD CONSTRAINT `fk_sys_drug_price_history_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `sys_drug_price_history` ADD CONSTRAINT `fk_sys_drug_price_history_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_inspection_item` ADD CONSTRAINT `fk_sys_inspection_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_laboratory_item` ADD CONSTRAINT `fk_sys_laboratory_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_laboratory_item_detail` ADD CONSTRAINT `fk_sys_laboratory_item_detail_laboratory_item_id` FOREIGN KEY (`laboratory_item_id`) REFERENCES `sys_laboratory_item` (`id`);
ALTER TABLE `sys_price_change_history` ADD CONSTRAINT `fk_sys_price_change_history_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_treatment_item` ADD CONSTRAINT `fk_sys_treatment_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
