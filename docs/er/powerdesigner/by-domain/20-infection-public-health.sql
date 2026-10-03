-- ============================================================
-- 领域 20 院感·公卫·医疗安全与死亡登记（本域 13 表 + 上游参照 11 表 / 47 条关系）
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


-- biz_infection_case  院感病例报告卡
CREATE TABLE `biz_infection_case` (
  `id` bigint NOT NULL COMMENT '主键',
  `case_no` varchar(32) NOT NULL COMMENT '病例编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者编号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别',
  `age` int COMMENT '年龄（快照）',
  `visit_type` tinyint NOT NULL COMMENT '就诊类型',
  `regist_id` bigint COMMENT '门诊就诊ID',
  `inp_id` bigint COMMENT '住院记录ID',
  `dept_id` bigint COMMENT '发现科室ID（快照）',
  `dept_name` varchar(100) COMMENT '发现科室（快照）',
  `case_source` tinyint NOT NULL COMMENT '感染来源',
  `infection_site` varchar(8) NOT NULL COMMENT '感染部位',
  `infection_diag` varchar(200) NOT NULL COMMENT '感染诊断',
  `pathogen` varchar(100) COMMENT '病原菌',
  `specimen` varchar(100) COMMENT '标本来源',
  `infect_date` date NOT NULL COMMENT '感染/诊断日期',
  `case_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `leak_flag` tinyint NOT NULL DEFAULT 0 COMMENT '漏报标志',
  `report_by` bigint NOT NULL COMMENT '上报人ID',
  `report_name` varchar(50) NOT NULL COMMENT '上报人姓名（快照）',
  `report_time` datetime NOT NULL COMMENT '上报时间',
  `audit_name` varchar(50) COMMENT '核实人',
  `audit_time` datetime COMMENT '核实时间',
  `audit_remark` varchar(200) COMMENT '核实意见',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  `remark` varchar(255),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_infection_case_no` (`case_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院感病例报告卡';

-- biz_infection_monitor  院感目标性监测登记
CREATE TABLE `biz_infection_monitor` (
  `id` bigint NOT NULL COMMENT '主键',
  `monitor_no` varchar(32) NOT NULL COMMENT '监测编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者编号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `monitor_type` tinyint NOT NULL COMMENT '监测类型',
  `dept_id` bigint COMMENT '监测科室ID',
  `dept_name` varchar(100) COMMENT '监测科室（快照）',
  `insert_date` date NOT NULL COMMENT '置入日期',
  `remove_date` date COMMENT '拔除日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `infection_flag` tinyint NOT NULL DEFAULT 0 COMMENT '感染确认',
  `infection_date` date COMMENT '感染日期',
  `infection_site` varchar(8) COMMENT '感染部位',
  `infection_diag` varchar(200) COMMENT '感染诊断',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  `remark` varchar(255),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_infection_monitor_no` (`monitor_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院感目标性监测登记';

-- biz_infection_monitor_daily  监测每日打卡
CREATE TABLE `biz_infection_monitor_daily` (
  `id` bigint NOT NULL COMMENT '主键',
  `monitor_id` bigint NOT NULL COMMENT '监测登记ID',
  `monitor_date` date NOT NULL COMMENT '监测日期',
  `recorder_id` bigint NOT NULL COMMENT '记录人ID',
  `recorder_name` varchar(50) NOT NULL COMMENT '记录人姓名（快照）',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  `remark` varchar(255),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测每日打卡';

-- biz_hand_hygiene_obs  手卫生依从性观察记录
CREATE TABLE `biz_hand_hygiene_obs` (
  `id` bigint NOT NULL COMMENT '主键',
  `obs_date` date NOT NULL COMMENT '观察日期',
  `dept_id` bigint NOT NULL COMMENT '被观察科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '被观察科室（快照）',
  `obs_object` tinyint NOT NULL COMMENT '观察对象',
  `opportunity_count` int NOT NULL COMMENT '手卫生时机数',
  `comply_count` int NOT NULL COMMENT '实际执行数',
  `observer_id` bigint NOT NULL COMMENT '观察人ID',
  `observer_name` varchar(50) NOT NULL COMMENT '观察人姓名（快照）',
  `obs_time` datetime NOT NULL COMMENT '观察登记时间',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  `remark` varchar(255),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手卫生依从性观察记录';

-- biz_infectious_report  传染病报告卡
CREATE TABLE `biz_infectious_report` (
  `id` bigint NOT NULL COMMENT '主键',
  `report_no` varchar(32) NOT NULL COMMENT '报卡编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者编号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别',
  `age` int COMMENT '年龄（快照）',
  `regist_id` bigint COMMENT '门诊就诊ID',
  `inp_id` bigint COMMENT '住院记录ID',
  `visit_dept_id` bigint COMMENT '发现/就诊科室ID（快照）',
  `visit_dept_name` varchar(100) COMMENT '发现/就诊科室（快照）',
  `disease_id` bigint NOT NULL COMMENT '病种ID',
  `disease_code` varchar(16) NOT NULL COMMENT '病种编码（快照）',
  `disease_name` varchar(50) NOT NULL COMMENT '病种名称（快照）',
  `infectious_class` tinyint NOT NULL COMMENT '传染病类别（快照，1甲/2乙/3丙）',
  `icd10` varchar(16) COMMENT 'ICD-10（快照）',
  `report_deadline` datetime NOT NULL COMMENT '报卡时限',
  `clinical_desc` varchar(500) COMMENT '临床摘要',
  `report_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `report_count` int NOT NULL DEFAULT 1 COMMENT '报卡次数',
  `report_by` bigint NOT NULL COMMENT '填卡医生ID',
  `report_by_name` varchar(50) NOT NULL COMMENT '填卡医生姓名（快照）',
  `report_time` datetime NOT NULL COMMENT '填卡时间',
  `audit_by_name` varchar(50) COMMENT '审核人姓名',
  `audit_time` datetime COMMENT '审核时间',
  `audit_opinion` varchar(200) COMMENT '审核意见',
  `return_reason` varchar(200) COMMENT '退报原因',
  `notify_time` datetime COMMENT '最近一次超时催报时间',
  `direct_time` datetime COMMENT '直报时间',
  `direct_payload` text COMMENT '直报报文',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  `remark` varchar(255),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_infectious_report_no` (`report_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传染病报告卡';

-- biz_public_health_report  公卫上报表
CREATE TABLE `biz_public_health_report` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `report_no` varchar(32) NOT NULL COMMENT '上报编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `record_id` bigint COMMENT '病历ID',
  `report_type` tinyint NOT NULL COMMENT '上报类型（1-传染病 2-死因监测 3-慢性病 4-其他）',
  `report_content` varchar(1000) NOT NULL COMMENT '上报内容',
  `diagnosis` varchar(200) COMMENT '诊断',
  `diagnosis_code` varchar(32) COMMENT '诊断编码',
  `report_status` tinyint NOT NULL DEFAULT 1 COMMENT '上报状态（1-待审核 2-审核通过 3-审核驳回）',
  `report_by` varchar(64) COMMENT '上报人',
  `report_time` datetime COMMENT '上报时间',
  `audit_by` varchar(64) COMMENT '审核人',
  `audit_time` datetime COMMENT '审核时间',
  `audit_remark` varchar(500) COMMENT '审核意见',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公卫上报表';

-- biz_adverse_event  不良事件上报
CREATE TABLE `biz_adverse_event` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `event_no` varchar(32) NOT NULL COMMENT '事件编号 AE+yyyyMMdd+4位',
  `event_type` tinyint NOT NULL COMMENT '事件类型',
  `event_level` tinyint NOT NULL COMMENT '事件等级',
  `acquired_flag` tinyint NOT NULL DEFAULT 1 COMMENT '来源（1-院内获得 2-入院带入）',
  `occur_dept_id` bigint NOT NULL COMMENT '发生科室 sys_department.id',
  `occur_dept_name` varchar(64) COMMENT '发生科室名称（快照）',
  `occur_ward_id` bigint COMMENT '发生病区ID',
  `occur_ward_name` varchar(128) COMMENT '发生病区名称（快照）',
  `occur_time` datetime NOT NULL COMMENT '发生时间',
  `patient_id` bigint COMMENT '关联患者',
  `patient_name` varchar(64) COMMENT '患者姓名（快照，可空）',
  `visit_id` bigint COMMENT '关联就诊 biz_regist_info.id（可空）',
  `title` varchar(100) NOT NULL COMMENT '事件摘要',
  `description` text NOT NULL COMMENT '事件详细经过',
  `immediate_action` varchar(500) COMMENT '即时处置措施',
  `reporter_id` bigint NOT NULL COMMENT '上报人员工ID',
  `reporter_name` varchar(64) COMMENT '上报人姓名（快照）',
  `report_time` datetime NOT NULL COMMENT '上报时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已上报待处理 2-处理中 3-已整改 4-已结案）',
  `handler_id` bigint COMMENT '处理人员工ID',
  `handler_name` varchar(64) COMMENT '处理人姓名（快照）',
  `handle_remark` varchar(500) COMMENT '处理意见（D）',
  `handle_time` datetime COMMENT '处理时间',
  `rectify_by_id` bigint COMMENT '整改人员工ID',
  `rectify_by_name` varchar(64) COMMENT '整改人姓名（快照）',
  `rectify_measures` varchar(500) COMMENT '整改措施（C）',
  `rectify_time` datetime COMMENT '整改时间',
  `close_by_id` bigint COMMENT '结案人员工ID',
  `close_by_name` varchar(64) COMMENT '结案人姓名（快照）',
  `verify_remark` varchar(500) COMMENT '验证结论（A）',
  `close_time` datetime COMMENT '结案时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `create_time` datetime COMMENT '创建时间',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_no` (`event_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不良事件上报';

-- biz_medical_waste  医疗废物登记
CREATE TABLE `biz_medical_waste` (
  `id` bigint NOT NULL COMMENT '医废登记ID',
  `waste_no` varchar(32) NOT NULL COMMENT '医废交接单号',
  `waste_type` tinyint NOT NULL COMMENT '医废类别（1-感染性 2-损伤性 3-病理性 4-药物性 5-化学性）',
  `weight_kg` decimal(8,2) COMMENT '重量（kg）',
  `dept_id` bigint COMMENT '产生科室ID',
  `dept_name` varchar(100) COMMENT '产生科室名称',
  `collect_time` datetime NOT NULL COMMENT '收集时间',
  `collector_name` varchar(50) COMMENT '收集人',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已登记 2-已交接 3-已处置）',
  `handover_name` varchar(50) COMMENT '交接人',
  `handover_time` datetime COMMENT '交接时间',
  `disposal_company` varchar(128) COMMENT '处置公司',
  `disposal_time` datetime COMMENT '处置时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_medical_waste_no` (`waste_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗废物登记';

-- biz_death_certificate  死亡医学证明书
CREATE TABLE `biz_death_certificate` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `cert_no` varchar(32) NOT NULL COMMENT '证明编号',
  `admission_id` bigint NOT NULL COMMENT '住院记录ID',
  `discharge_id` bigint COMMENT '死亡出院记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '死者姓名（快照）',
  `gender` tinyint COMMENT '性别（1-男 2-女 3-未知）',
  `nation` varchar(20) COMMENT '民族（快照）',
  `birth_date` date COMMENT '出生日期（快照）',
  `age` int COMMENT '死亡年龄',
  `id_card` varchar(18) COMMENT '身份证号',
  `occupation` varchar(50) COMMENT '职业（快照）',
  `marital_status` tinyint COMMENT '婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）',
  `death_time` datetime NOT NULL COMMENT '死亡时间',
  `death_place` tinyint NOT NULL COMMENT '死亡地点（1-医院 2-来院途中 3-家中 4-民政管理机构 5-其他机构 9-未指明）',
  `death_dept_id` bigint COMMENT '死亡科室ID',
  `death_dept_name` varchar(100) COMMENT '死亡科室名称（快照）',
  `death_ward_name` varchar(64) COMMENT '死亡病区名称（快照）',
  `death_bed_no` varchar(16) COMMENT '死亡床位号（快照）',
  `clinical_diagnosis` varchar(500) NOT NULL COMMENT '死亡诊断',
  `underlying_icd_code` varchar(32) COMMENT '根本死因ICD-10编码',
  `underlying_icd_name` varchar(200) COMMENT '根本死因名称（快照）',
  `past_history` varchar(500) COMMENT '既往病史',
  `autopsy_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否尸检（0-否 1-是）',
  `autopsy_result` varchar(500) COMMENT '尸检结论/病理诊断',
  `relative_name` varchar(50) COMMENT '死者近亲属姓名',
  `relative_relation` varchar(20) COMMENT '与死者关系',
  `relative_phone` varchar(20) COMMENT '近亲属联系电话',
  `physician_id` bigint COMMENT '填表医师ID',
  `physician_name` varchar(50) NOT NULL COMMENT '填表医师姓名',
  `fill_time` datetime COMMENT '填表时间',
  `reviewer_id` bigint COMMENT '审核人ID',
  `reviewer_name` varchar(50) COMMENT '审核人姓名',
  `review_time` datetime COMMENT '审核时间',
  `review_opinion` varchar(500) COMMENT '审核意见',
  `cert_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-草稿 2-已审核 3-已开具 4-已作废）',
  `issue_time` datetime COMMENT '签发（出具/盖章）',
  `printer_name` varchar(64) COMMENT '最后打印人',
  `print_count` int NOT NULL DEFAULT 0 COMMENT '打印次数',
  `last_print_time` datetime COMMENT '最后打印时间',
  `void_reason` varchar(500) COMMENT '作废原因',
  `void_by` varchar(64) COMMENT '作废经办人',
  `void_time` datetime COMMENT '作废时间',
  `orig_cert_id` bigint COMMENT '重开来源证明ID',
  `report_status` tinyint NOT NULL DEFAULT 1 COMMENT '死因监测上报状态（1-未上报 2-已上报 3-上报失败）',
  `report_deadline` datetime COMMENT '上报时限',
  `report_time` datetime COMMENT '上报时间',
  `report_no` varchar(64) COMMENT '上报回执编号/区域死因监测编号',
  `report_error` varchar(500) COMMENT '上报失败原因',
  `report_payload` text COMMENT '上报报文',
  `notify_time` datetime COMMENT '最近一次超时催报时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cert_no` (`cert_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='死亡医学证明书';

-- biz_death_certificate_cause  死亡证明死因链
CREATE TABLE `biz_death_certificate_cause` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `cert_id` bigint NOT NULL COMMENT '死亡证明ID',
  `part` tinyint NOT NULL COMMENT '部分（1-Ⅰ部分死因链 2-Ⅱ部分其他疾病）',
  `seq_no` tinyint NOT NULL COMMENT '行序',
  `icd_code` varchar(32) COMMENT 'ICD-10编码',
  `icd_name` varchar(200) NOT NULL COMMENT '疾病或情况名称',
  `interval_text` varchar(50) COMMENT '发病至死亡间隔',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cert_part_seq` (`cert_id`, `part`, `seq_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='死亡证明死因链';

-- biz_death_registration  住院死亡登记簿
CREATE TABLE `biz_death_registration` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `register_no` varchar(32) NOT NULL COMMENT '死亡登记号',
  `admission_id` bigint NOT NULL COMMENT '住院记录ID',
  `cert_id` bigint COMMENT '死亡证明ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '死者姓名（快照）',
  `death_time` datetime NOT NULL COMMENT '死亡时间',
  `death_dept_id` bigint COMMENT '死亡科室ID',
  `death_dept_name` varchar(100) COMMENT '死亡科室名称（快照）',
  `death_bed_no` varchar(16) COMMENT '死亡床位号（快照）',
  `death_type` tinyint NOT NULL COMMENT '死亡类型（1-疾病死亡 2-非疾病死亡）',
  `police_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否已报公安/司法（0-否 1-是）',
  `police_org` varchar(100) COMMENT '受理公安机关',
  `police_case_no` varchar(64) COMMENT '公安受理/案件编号',
  `police_report_time` datetime COMMENT '报案时间',
  `forensic_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否由法医出具/检验（0-否 1-是）',
  `body_disposal` tinyint COMMENT '尸体处理方式（1-殡仪馆接运 2-家属自行处理 3-病理解剖 4-其他）',
  `body_unit` varchar(100) COMMENT '遗体接运/接收单位',
  `body_transport_time` datetime COMMENT '遗体移出时间',
  `relative_name` varchar(50) COMMENT '办理人/近亲属姓名',
  `relative_relation` varchar(20) COMMENT '与死者关系',
  `relative_phone` varchar(20) COMMENT '联系电话',
  `received_copies` varchar(50) COMMENT '家属已领取联次（1-记录联 2-户籍联 3-殡葬联 4-家属联）',
  `receive_time` datetime COMMENT '领取时间',
  `dispute_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否存在医疗纠纷/患方异议（0-否 1-是）',
  `dispute_desc` varchar(500) COMMENT '纠纷/异议情况',
  `register_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-草稿 2-已登记 3-已作废）',
  `registrar_id` bigint COMMENT '登记人ID（值班医师/病区护士/防保科）',
  `registrar_name` varchar(50) COMMENT '登记人姓名',
  `register_time` datetime COMMENT '登记（确认）',
  `void_reason` varchar(500) COMMENT '作废原因',
  `void_time` datetime COMMENT '作废时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_register_no` (`register_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院死亡登记簿';

-- biz_dispute_case  医疗纠纷投诉主单
CREATE TABLE `biz_dispute_case` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `case_no` varchar(32) NOT NULL COMMENT '单据编号',
  `case_type` tinyint NOT NULL COMMENT '类型（1-服务投诉 2-医疗纠纷 3-医疗损害争议 4-其他）',
  `source_type` tinyint NOT NULL COMMENT '来源（1-来电 2-来访 3-来信 4-政务热线 5-上级交办 6-院内发现 7-其他）',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '等级（1-一般 2-较大 3-重大）',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `admission_id` bigint COMMENT '关联住院ID',
  `dept_id` bigint COMMENT '被投诉科室ID',
  `dept_name` varchar(128) COMMENT '被投诉科室名称（快照）',
  `involved_staff` varchar(255) COMMENT '涉及人员',
  `complainant` varchar(64) COMMENT '投诉人姓名（可为患者本人/家属/其他）',
  `complainant_rel` varchar(32) COMMENT '与患者关系（1-本人 2-家属 3-代理人 4-其他）',
  `complainant_tel` varchar(32) COMMENT '投诉人联系电话',
  `occur_time` datetime COMMENT '事件发生时间',
  `occur_place` varchar(128) COMMENT '事件发生地点',
  `content` varchar(1000) NOT NULL COMMENT '投诉/纠纷内容',
  `demand` varchar(500) COMMENT '投诉人诉求',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待受理 2-调查中 3-处理中 4-已结案 5-已撤销）',
  `need_seal` tinyint NOT NULL DEFAULT 0 COMMENT '是否需封存病历（0-否 1-是）',
  `seal_status` tinyint NOT NULL DEFAULT 0 COMMENT '封存状态（0-未申请 1-已封存 2-待归档后封存）',
  `archive_id` bigint COMMENT '已封存病案ID',
  `seal_time` datetime COMMENT '封存时间',
  `deal_type` tinyint COMMENT '处理途径（1-院内协商 2-医调委调解 3-行政调解 4-司法鉴定 5-诉讼 6-其他）',
  `duty_type` tinyint COMMENT '责任认定（1-无责 2-轻微责任 3-次要责任 4-主要责任 5-完全责任）',
  `compensation` decimal(12,2) COMMENT '赔偿/补偿金额',
  `conclusion` varchar(1000) COMMENT '调查结论/处理结果',
  `register_by` varchar(64) COMMENT '登记人',
  `register_time` datetime COMMENT '登记时间',
  `accept_by` varchar(64) COMMENT '受理人',
  `accept_time` datetime COMMENT '受理时间',
  `close_by` varchar(64) COMMENT '结案人',
  `close_time` datetime COMMENT '结案时间',
  `revoke_reason` varchar(500) COMMENT '撤销原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_case_no` (`case_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗纠纷投诉主单';

-- biz_dispute_flow  纠纷投诉处理台账
CREATE TABLE `biz_dispute_flow` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `case_id` bigint NOT NULL COMMENT '主单ID',
  `action` varchar(64) NOT NULL COMMENT '动作（受理/调查/协商/回复投诉人/封存病历/结案/撤销…）',
  `from_status` tinyint COMMENT '动作前状态',
  `to_status` tinyint COMMENT '动作后状态',
  `content` varchar(1000) COMMENT '处理说明',
  `operator_id` bigint COMMENT '操作人（员工ID）',
  `operator` varchar(64) COMMENT '操作人姓名',
  `operate_time` datetime COMMENT '操作时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纠纷投诉处理台账';

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

-- biz_discharge  出院记录
CREATE TABLE `biz_discharge` (
  `discharge_id` bigint NOT NULL COMMENT '出院ID',
  `discharge_no` varchar(32) NOT NULL COMMENT '出院记录号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `discharge_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '出院时间',
  `discharge_doctor_id` bigint COMMENT '出院医生ID',
  `discharge_diagnosis` varchar(500) COMMENT '出院诊断',
  `discharge_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-正常 2-转科 3-自动出院）',
  `remark` varchar(500) COMMENT '备注',
  `discharge_way` tinyint COMMENT '离院方式（1-医嘱离院 2-医嘱转院 3-医嘱转社区 4-非医嘱离院 5-死亡 9-其他）',
  `discharge_diagnosis_code` varchar(32) COMMENT '出院诊断ICD编码',
  `death_flag` tinyint DEFAULT 0 COMMENT '死亡标志（0-否 1-是）',
  `discharge_summary` varchar(1000) COMMENT '出院小结 / 出院带药医嘱',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`discharge_id`),
  UNIQUE KEY `uk_discharge_no` (`discharge_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出院记录';

-- biz_medical_record  门诊病历
CREATE TABLE `biz_medical_record` (
  `id` bigint NOT NULL,
  `record_no` varchar(32) NOT NULL COMMENT '病历号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL COMMENT '性别',
  `age` int COMMENT '年龄',
  `regist_id` bigint NOT NULL COMMENT '挂号ID',
  `regist_no` varchar(32) NOT NULL COMMENT '挂号单号',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `visit_type` tinyint DEFAULT 1 COMMENT '就诊类型（1-初诊 2-复诊）',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '医生姓名',
  `chief_complaint` text COMMENT '主诉',
  `present_illness` text COMMENT '现病史',
  `past_history` text COMMENT '既往史',
  `personal_history` text COMMENT '个人史',
  `family_history` text COMMENT '家族史',
  `allergy_history` text COMMENT '过敏史',
  `temperature` varchar(10) COMMENT '体温（℃）',
  `pulse` varchar(10) COMMENT '脉搏（次/分）',
  `respiration` varchar(10) COMMENT '呼吸（次/分）',
  `systolic_pressure` varchar(10) COMMENT '收缩压（mmHg）',
  `diastolic_pressure` varchar(10) COMMENT '舒张压（mmHg）',
  `general_condition` text COMMENT '一般情况',
  `skin_mucosa` text COMMENT '皮肤黏膜',
  `head_neck` text COMMENT '头颈部',
  `chest_lung` text COMMENT '胸肺',
  `heart` text COMMENT '心脏',
  `abdomen` text COMMENT '腹部',
  `spine_limbs` text COMMENT '脊柱四肢',
  `nervous_system` text COMMENT '神经系统',
  `specialist_exam` text COMMENT '专科检查',
  `auxiliary_exam` text COMMENT '辅助检查',
  `diagnosis` text COMMENT '诊断',
  `diagnosis_code` varchar(100) COMMENT '诊断编码',
  `diagnosis_name` varchar(500) COMMENT '诊断名称',
  `treatment_plan` text COMMENT '处理意见',
  `record_status` tinyint DEFAULT 1 COMMENT '病历状态（1-草稿 2-已提交 3-已归档 4-已作废）',
  `review_status` tinyint DEFAULT 0 COMMENT '审核状态（0-待提交 1-待审核 2-审核通过 3-审核驳回）',
  `review_by` varchar(64) COMMENT '审核人',
  `review_time` datetime COMMENT '审核时间',
  `review_remark` varchar(500) COMMENT '审核意见',
  `guide_pdf_path` varchar(512) COMMENT '患者引导单文件路径',
  `submit_time` datetime COMMENT '提交时间',
  `archive_time` datetime COMMENT '归档时间',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '签名状态（0-未签名 1-已签名 2-签名已失效）',
  `sign_id` bigint COMMENT '当前有效签名ID',
  `signed_time` datetime COMMENT '最近一次签名时刻',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊病历';

-- biz_medical_record_archive  病历归档
CREATE TABLE `biz_medical_record_archive` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `archive_no` varchar(32) NOT NULL COMMENT '归档编号',
  `record_id` bigint NOT NULL COMMENT '病历ID',
  `record_no` varchar(32) COMMENT '病历号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `regist_id` bigint COMMENT '挂号ID',
  `visit_date` date COMMENT '就诊日期',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(50) COMMENT '科室名称',
  `doctor_id` bigint COMMENT '医生ID',
  `doctor_name` varchar(50) COMMENT '医生姓名',
  `diagnosis` varchar(500) COMMENT '诊断',
  `archive_status` tinyint NOT NULL DEFAULT 1 COMMENT '归档状态（1-待归档 2-已归档 3-已封存）',
  `archive_time` datetime COMMENT '归档时间',
  `seal_time` datetime COMMENT '封存时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_archive_no` (`archive_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历归档';

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

-- biz_patient_past_disease  既往疾病史
CREATE TABLE `biz_patient_past_disease` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `disease_name` varchar(200) NOT NULL COMMENT '疾病名称',
  `disease_code` varchar(50) COMMENT '疾病编码',
  `diagnosis_date` date COMMENT '诊断日期',
  `diagnosis_dept` varchar(100) COMMENT '诊断科室',
  `treatment_plan` text COMMENT '治疗方案',
  `current_status` varchar(50) COMMENT '当前控制情况（已治愈/控制良好/未控制/随访中）',
  `relapse_count` int DEFAULT 0 COMMENT '复发次数',
  `last_followup_date` date COMMENT '最近随访日期',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='既往疾病史';

-- biz_visit  就诊次
CREATE TABLE `biz_visit` (
  `visit_id` bigint NOT NULL COMMENT '就诊次ID',
  `visit_no` varchar(32) NOT NULL COMMENT '就诊次编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `start_time` datetime NOT NULL COMMENT '就诊开始时间',
  `end_time` datetime COMMENT '就诊结束时间',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '本次就诊总费用',
  `visit_status` tinyint NOT NULL DEFAULT 1 COMMENT '就诊状态（0-已取消 1-进行中 2-已完成）',
  `regist_ids` varchar(500) COMMENT '关联的挂号ID列表',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`visit_id`),
  UNIQUE KEY `uk_visit_no` (`visit_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊次';

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
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_occur_dept_id` FOREIGN KEY (`occur_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_occur_ward_id` FOREIGN KEY (`occur_ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_visit` (`visit_id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_reporter_id` FOREIGN KEY (`reporter_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_handler_id` FOREIGN KEY (`handler_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_rectify_by_id` FOREIGN KEY (`rectify_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_close_by_id` FOREIGN KEY (`close_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_death_certificate` ADD CONSTRAINT `fk_biz_death_certificate_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_death_certificate` ADD CONSTRAINT `fk_biz_death_certificate_discharge_id` FOREIGN KEY (`discharge_id`) REFERENCES `biz_discharge` (`discharge_id`);
ALTER TABLE `biz_death_certificate` ADD CONSTRAINT `fk_biz_death_certificate_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_death_certificate` ADD CONSTRAINT `fk_biz_death_certificate_death_dept_id` FOREIGN KEY (`death_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_death_certificate` ADD CONSTRAINT `fk_biz_death_certificate_physician_id` FOREIGN KEY (`physician_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_death_certificate` ADD CONSTRAINT `fk_biz_death_certificate_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_death_certificate` ADD CONSTRAINT `fk_biz_death_certificate_orig_cert_id` FOREIGN KEY (`orig_cert_id`) REFERENCES `biz_death_certificate` (`id`);
ALTER TABLE `biz_death_certificate_cause` ADD CONSTRAINT `fk_biz_death_certificate_cause_cert_id` FOREIGN KEY (`cert_id`) REFERENCES `biz_death_certificate` (`id`);
ALTER TABLE `biz_death_registration` ADD CONSTRAINT `fk_biz_death_registration_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_death_registration` ADD CONSTRAINT `fk_biz_death_registration_cert_id` FOREIGN KEY (`cert_id`) REFERENCES `biz_death_certificate` (`id`);
ALTER TABLE `biz_death_registration` ADD CONSTRAINT `fk_biz_death_registration_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_death_registration` ADD CONSTRAINT `fk_biz_death_registration_death_dept_id` FOREIGN KEY (`death_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_death_registration` ADD CONSTRAINT `fk_biz_death_registration_registrar_id` FOREIGN KEY (`registrar_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_dispute_case` ADD CONSTRAINT `fk_biz_dispute_case_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_dispute_case` ADD CONSTRAINT `fk_biz_dispute_case_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_dispute_case` ADD CONSTRAINT `fk_biz_dispute_case_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_dispute_case` ADD CONSTRAINT `fk_biz_dispute_case_archive_id` FOREIGN KEY (`archive_id`) REFERENCES `biz_medical_record_archive` (`id`);
ALTER TABLE `biz_dispute_flow` ADD CONSTRAINT `fk_biz_dispute_flow_case_id` FOREIGN KEY (`case_id`) REFERENCES `biz_dispute_case` (`id`);
ALTER TABLE `biz_dispute_flow` ADD CONSTRAINT `fk_biz_dispute_flow_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_hand_hygiene_obs` ADD CONSTRAINT `fk_biz_hand_hygiene_obs_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_hand_hygiene_obs` ADD CONSTRAINT `fk_biz_hand_hygiene_obs_observer_id` FOREIGN KEY (`observer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_infection_case` ADD CONSTRAINT `fk_biz_infection_case_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_infection_case` ADD CONSTRAINT `fk_biz_infection_case_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_infection_case` ADD CONSTRAINT `fk_biz_infection_case_inp_id` FOREIGN KEY (`inp_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_infection_case` ADD CONSTRAINT `fk_biz_infection_case_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_infection_case` ADD CONSTRAINT `fk_biz_infection_case_report_by` FOREIGN KEY (`report_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_infection_monitor` ADD CONSTRAINT `fk_biz_infection_monitor_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_infection_monitor` ADD CONSTRAINT `fk_biz_infection_monitor_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_infection_monitor_daily` ADD CONSTRAINT `fk_biz_infection_monitor_daily_monitor_id` FOREIGN KEY (`monitor_id`) REFERENCES `biz_infection_monitor` (`id`);
ALTER TABLE `biz_infection_monitor_daily` ADD CONSTRAINT `fk_biz_infection_monitor_daily_recorder_id` FOREIGN KEY (`recorder_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_infectious_report` ADD CONSTRAINT `fk_biz_infectious_report_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_infectious_report` ADD CONSTRAINT `fk_biz_infectious_report_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_infectious_report` ADD CONSTRAINT `fk_biz_infectious_report_inp_id` FOREIGN KEY (`inp_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_infectious_report` ADD CONSTRAINT `fk_biz_infectious_report_visit_dept_id` FOREIGN KEY (`visit_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_infectious_report` ADD CONSTRAINT `fk_biz_infectious_report_disease_id` FOREIGN KEY (`disease_id`) REFERENCES `biz_patient_past_disease` (`id`);
ALTER TABLE `biz_infectious_report` ADD CONSTRAINT `fk_biz_infectious_report_report_by` FOREIGN KEY (`report_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_medical_waste` ADD CONSTRAINT `fk_biz_medical_waste_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_public_health_report` ADD CONSTRAINT `fk_biz_public_health_report_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_public_health_report` ADD CONSTRAINT `fk_biz_public_health_report_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
