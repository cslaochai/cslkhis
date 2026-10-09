-- ============================================================
-- 领域 14 手术麻醉与日间手术（本域 14 表 + 上游参照 7 表 / 64 条关系）
-- 由 workspace/_er/refresh.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


-- biz_operation_apply  手术申请单
CREATE TABLE `biz_operation_apply` (
  `id` bigint NOT NULL COMMENT '手术申请单ID（雪花）',
  `apply_no` varchar(32) NOT NULL COMMENT '手术申请单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `admission_no` varchar(32) COMMENT '入院号（快照）',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别（快照）（1-男 2-女）',
  `age` int COMMENT '年龄（快照）',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(64) COMMENT '申请科室名称（快照）',
  `apply_ward_name` varchar(64) COMMENT '申请时所在病区名称（快照）',
  `apply_bed_no` varchar(32) COMMENT '申请时床号（快照）',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(64) COMMENT '申请医生姓名（快照）',
  `apply_time` datetime COMMENT '申请时间',
  `planned_operation_code` varchar(32) COMMENT '拟施手术编码',
  `planned_operation_name` varchar(200) NOT NULL COMMENT '拟施手术名称',
  `operation_level` tinyint COMMENT '手术级别（1-一级 2-二级 3-三级 4-四级）',
  `incision_level` tinyint COMMENT '切口等级（0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类）',
  `anesthesia_type` tinyint COMMENT '麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）',
  `preop_diagnosis` varchar(500) NOT NULL COMMENT '术前诊断',
  `operation_reason` varchar(500) NOT NULL COMMENT '手术指征/理由',
  `is_emergency` tinyint NOT NULL DEFAULT 0 COMMENT '是否急诊手术（0-否 1-是）',
  `is_main` tinyint NOT NULL DEFAULT 1 COMMENT '是否主要手术',
  `operation_room` varchar(64) COMMENT '手术间',
  `planned_start_time` datetime COMMENT '计划开始时间',
  `planned_end_time` datetime COMMENT '计划结束时间',
  `surgeon_id` bigint COMMENT '主刀医师ID（员工ID）',
  `surgeon_name` varchar(64) COMMENT '主刀医师姓名（快照）',
  `assistant_name` varchar(200) COMMENT '助手姓名',
  `anesthetist_id` bigint COMMENT '麻醉医师ID（员工ID）',
  `anesthetist_name` varchar(64) COMMENT '麻醉医师姓名（快照）',
  `schedule_doctor_id` bigint COMMENT '排台操作人ID（员工ID）',
  `schedule_doctor_name` varchar(64) COMMENT '排台操作人姓名（快照）',
  `schedule_time` datetime COMMENT '排台时间',
  `schedule_remark` varchar(500) COMMENT '排台备注',
  `preop_check_items` varchar(200) COMMENT '术前核对要点码',
  `preop_note` varchar(1000) COMMENT '术前核对补充说明',
  `preop_check_doctor_id` bigint COMMENT '术前核对人ID（员工ID）',
  `preop_check_doctor_name` varchar(64) COMMENT '术前核对人姓名（快照）',
  `preop_check_time` datetime COMMENT '术前核对时间',
  `actual_operation_code` varchar(32) COMMENT '实际手术编码',
  `actual_operation_name` varchar(200) COMMENT '实际手术名称',
  `operation_start_time` datetime COMMENT '实际开始时间（切皮）',
  `operation_end_time` datetime COMMENT '实际结束时间（关腹/关胸）',
  `blood_loss` int COMMENT '术中出血量（ml）',
  `intraop_findings` varchar(2000) COMMENT '术中所见',
  `intraop_procedure` varchar(2000) COMMENT '手术经过/操作步骤',
  `postop_note` varchar(1000) COMMENT '术后处理与注意事项',
  `specimen_sent` varchar(200) COMMENT '标本送检',
  `finish_doctor_id` bigint COMMENT '完成录入人ID（员工ID）',
  `finish_doctor_name` varchar(64) COMMENT '完成录入人姓名（快照）',
  `finish_time` datetime COMMENT '手术完成时间',
  `operation_id` bigint COMMENT '回写病案首页手术明细ID',
  `record_id` bigint COMMENT '回写住院病历ID',
  `operation_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-待排期 1-已排期 2-术前核对完成 3-已完成 4-已取消）',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `cancel_doctor_id` bigint COMMENT '取消人ID（员工ID）',
  `cancel_doctor_name` varchar(64) COMMENT '取消人姓名（快照）',
  `cancel_time` datetime COMMENT '取消时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术申请单';

-- biz_operation_safety_check  手术安全核查单
CREATE TABLE `biz_operation_safety_check` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `check_no` varchar(32) NOT NULL COMMENT '核查单号',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号（快照）',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `operation_name` varchar(200) COMMENT '手术名称（快照，拟施）',
  `operation_room` varchar(64) COMMENT '手术间（快照）',
  `phase` tinyint NOT NULL COMMENT '核查时段（1-麻醉诱导前 2-手术开始前 3-患者离开手术室前）',
  `items` varchar(300) NOT NULL COMMENT '核查项码值',
  `note` varchar(1000) COMMENT '异常说明',
  `surgeon_id` bigint NOT NULL COMMENT '手术医师',
  `surgeon_name` varchar(64) COMMENT '手术医师姓名（快照）',
  `surgeon_sign_time` datetime COMMENT '手术医师签名时间',
  `anesthetist_id` bigint NOT NULL COMMENT '麻醉医师员工ID',
  `anesthetist_name` varchar(64) COMMENT '麻醉医师姓名（快照）',
  `anesthetist_sign_time` datetime COMMENT '麻醉医师签名时间',
  `nurse_id` bigint NOT NULL COMMENT '手术室护士（器械/巡回）',
  `nurse_name` varchar(64) COMMENT '手术室护士姓名（快照）',
  `nurse_sign_time` datetime COMMENT '手术室护士签名时间',
  `recorder_id` bigint COMMENT '录入人ID',
  `recorder_name` varchar(64) COMMENT '录入人姓名（快照）',
  `check_time` datetime COMMENT '核查完成时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`),
  UNIQUE KEY `uk_check_apply_phase` (`apply_id`, `phase`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术安全核查单';

-- biz_operation_count  手术清点主单
CREATE TABLE `biz_operation_count` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `count_no` varchar(32) NOT NULL COMMENT '清点单号',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号（快照）',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `operation_room` varchar(64) COMMENT '手术间（快照）',
  `planned_operation_name` varchar(200) COMMENT '手术名称（快照）',
  `instrument_nurse_id` bigint COMMENT '器械（洗手）',
  `instrument_nurse_name` varchar(64) COMMENT '器械护士姓名（快照）',
  `circulate_nurse_id` bigint COMMENT '巡回护士ID（员工ID）',
  `circulate_nurse_name` varchar(64) COMMENT '巡回护士姓名（快照）',
  `before_nurse_id` bigint COMMENT '术前清点核对人ID',
  `before_nurse_name` varchar(64) COMMENT '术前清点核对人姓名（快照）',
  `before_time` datetime COMMENT '术前清点时间',
  `before_result` tinyint COMMENT '术前清点结果（1-一致 2-不一致）',
  `closure_nurse_id` bigint COMMENT '关体前清点核对人ID（员工ID）',
  `closure_nurse_name` varchar(64) COMMENT '关体前核对人姓名（快照）',
  `closure_time` datetime COMMENT '关体前清点时间',
  `closure_result` tinyint COMMENT '关体前清点结果（1-一致 2-不一致）',
  `final_nurse_id` bigint COMMENT '关体后清点核对人ID（员工ID）',
  `final_nurse_name` varchar(64) COMMENT '关体后核对人姓名（快照）',
  `final_time` datetime COMMENT '关体后清点时间',
  `final_result` tinyint COMMENT '关体后清点结果（1-一致 2-不一致）',
  `phase` tinyint NOT NULL DEFAULT 0 COMMENT '当前阶段（0-未开始 1-术前完成 2-关体前完成 3-关体后完成）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-清点中 1-三轮一致完成 2-存在差异待处理 3-异常终止）',
  `discrepancy_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否存在清点差异（0-无 1-有）',
  `diff_note` varchar(1000) COMMENT '差异说明与处理过程（差了什么、怎么处理、结论如何必须写清）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_count_apply` (`apply_id`),
  UNIQUE KEY `uk_count_no` (`count_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术清点主单';

-- biz_operation_count_item  手术清点明细
CREATE TABLE `biz_operation_count_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `count_id` bigint NOT NULL COMMENT '清点单ID',
  `seq_no` int NOT NULL DEFAULT 1 COMMENT '行号',
  `item_category` tinyint NOT NULL COMMENT '类别（纱布/纱垫）（1-器械 2-敷料 3-缝针 4-刀片 5-其他）',
  `item_name` varchar(100) NOT NULL COMMENT '名称（如：止血钳 / 纱布块 / 圆针）',
  `spec` varchar(100) COMMENT '规格',
  `before_qty` int COMMENT '术前数量',
  `closure_qty` int COMMENT '关体前数量',
  `final_qty` int COMMENT '关体后数量',
  `remark` varchar(200) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术清点明细';

-- biz_anesthesia_visit  麻醉术前访视单
CREATE TABLE `biz_anesthesia_visit` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `visit_no` varchar(32) NOT NULL COMMENT '访视单号',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别（快照）（1-男 2-女）',
  `age` int COMMENT '年龄（快照）',
  `diagnosis` varchar(500) COMMENT '术前诊断（快照）',
  `planned_operation_code` varchar(32) COMMENT '拟施手术编码（快照）',
  `planned_operation_name` varchar(200) COMMENT '拟施手术名称（快照）',
  `operation_level` tinyint COMMENT '手术级别（快照）',
  `anesthesia_type` tinyint COMMENT '拟施麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）',
  `is_emergency` tinyint NOT NULL DEFAULT 0 COMMENT '是否急诊手术（0-否 1-是）',
  `asa_grade` tinyint COMMENT 'ASA 分级（1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ 5-Ⅴ）',
  `asa_emergency` tinyint NOT NULL DEFAULT 0 COMMENT 'ASA E（急诊）（0-否 1-是）',
  `mallampati` tinyint COMMENT 'Mallampati 气道分级（1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ）',
  `mouth_open_cm` decimal(4,1) COMMENT '张口度（cm）',
  `neck_mobility` tinyint COMMENT '颈部活动度（1-正常 2-受限 3-强直）',
  `difficult_airway` tinyint NOT NULL DEFAULT 0 COMMENT '预计困难气道（0-否 1-是）',
  `airway_note` varchar(500) COMMENT '气道评估补充说明',
  `past_anesthesia_history` varchar(1000) COMMENT '既往麻醉史与不良反应史',
  `allergy_history` varchar(500) COMMENT '过敏史（药物/食物/消毒剂）',
  `medication_history` varchar(1000) COMMENT '长期用药史（抗凝药/降压药/激素等必须写）',
  `smoke_drink` varchar(200) COMMENT '吸烟饮酒史',
  `npo_status` tinyint COMMENT '禁食禁饮（0-未禁食 1-已按要求禁食 2-急诊饱胃）',
  `height_cm` decimal(5,1) COMMENT '身高（cm）',
  `weight_kg` decimal(5,1) COMMENT '体重（kg）',
  `exam_summary` varchar(1000) COMMENT '辅助检查摘要（血常规/凝血/ECG/胸片/电解质等）',
  `anesthesia_plan` varchar(1000) COMMENT '麻醉计划',
  `monitoring_plan` varchar(500) COMMENT '监测计划（有创血压/CVP/BIS/体温等）',
  `risk_assessment` varchar(1000) COMMENT '风险评估',
  `backup_plan` varchar(500) COMMENT '备选方案',
  `conclusion` tinyint COMMENT '访视结论（1-可施行麻醉 2-暂缓手术 3-需会诊）',
  `conclusion_note` varchar(1000) COMMENT '结论说明',
  `visit_status` tinyint NOT NULL DEFAULT 0 COMMENT '访视状态（0-草稿 1-已完成）',
  `visit_doctor_id` bigint COMMENT '访视麻醉医师ID（员工ID）',
  `visit_doctor_name` varchar(64) COMMENT '访视麻醉医师姓名（快照）',
  `visit_time` datetime COMMENT '访视时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_visit_apply` (`apply_id`),
  UNIQUE KEY `uk_visit_no` (`visit_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='麻醉术前访视单';

-- biz_anesthesia_record  麻醉记录单
CREATE TABLE `biz_anesthesia_record` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `record_no` varchar(32) NOT NULL COMMENT '麻醉记录单号',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号（快照）',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别（快照）（1-男 2-女）',
  `age` int COMMENT '年龄（快照）',
  `visit_id` bigint COMMENT '来源术前访视单ID',
  `anesthesia_type` tinyint COMMENT '麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）',
  `asa_grade` tinyint COMMENT 'ASA 分级',
  `anesthetist_id` bigint COMMENT '麻醉医师ID（员工ID）',
  `anesthetist_name` varchar(64) COMMENT '麻醉医师姓名（快照）',
  `assistant_anesthetist_name` varchar(200) COMMENT '麻醉助手姓名',
  `anesthesia_method_detail` varchar(500) COMMENT '麻醉方法描述',
  `airway_device` tinyint COMMENT '气道管理方式（0-无 1-气管插管 2-喉罩 3-面罩 4-其他）',
  `airway_device_spec` varchar(100) COMMENT '气道器具规格',
  `ventilation_mode` tinyint COMMENT '通气方式（1-自主呼吸 2-辅助通气 3-控制通气）',
  `enter_room_time` datetime COMMENT '入手术室时间',
  `anesthesia_start_time` datetime COMMENT '麻醉开始时间',
  `operation_start_time` datetime COMMENT '手术开始时间（切皮）',
  `operation_end_time` datetime COMMENT '手术结束时间（关腹/关胸）',
  `anesthesia_end_time` datetime COMMENT '麻醉结束时间（停药）',
  `leave_room_time` datetime COMMENT '出手术室时间',
  `crystalloid` int COMMENT '晶体液入量（ml）',
  `colloid` int COMMENT '胶体液入量（ml）',
  `blood_transfusion` int COMMENT '异体输血量（ml）',
  `autotransfusion` int COMMENT '自体血回输量（ml）',
  `urine_output` int COMMENT '术中尿量（ml）',
  `blood_loss` int COMMENT '术中出血量（ml）',
  `adverse_event_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否发生麻醉不良事件（0-无 1-有）',
  `adverse_event_note` varchar(1000) COMMENT '不良事件经过与处理',
  `anesthesia_effect` tinyint COMMENT '麻醉效果（1-满意 2-欠佳 3-失败改麻醉方式）',
  `postop_disposition` tinyint COMMENT '术后去向（1-回病房 2-入PACU 3-入ICU）',
  `record_status` tinyint NOT NULL DEFAULT 0 COMMENT '记录状态（0-记录中 1-已提交 2-已审核）',
  `submit_doctor_id` bigint COMMENT '提交人ID（员工ID）',
  `submit_doctor_name` varchar(64) COMMENT '提交人姓名（快照）',
  `submit_time` datetime COMMENT '提交时间',
  `audit_doctor_id` bigint COMMENT '审核人ID（员工ID）',
  `audit_doctor_name` varchar(64) COMMENT '审核人姓名（快照）',
  `audit_time` datetime COMMENT '审核时间',
  `charge_status` tinyint NOT NULL DEFAULT 0 COMMENT '计费状态（0-未计费 1-已计费 2-计费异常）',
  `fee_no` varchar(32) COMMENT '记账单号',
  `charged_amount` decimal(12,2) COMMENT '本次计入金额（元）',
  `charge_fail_reason` varchar(500) COMMENT '计费失败原因',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_apply` (`apply_id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='麻醉记录单';

-- biz_anesthesia_med  麻醉用药记录
CREATE TABLE `biz_anesthesia_med` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `record_id` bigint NOT NULL COMMENT '麻醉记录ID',
  `med_time` datetime NOT NULL COMMENT '给药时刻',
  `med_phase` tinyint COMMENT '用药阶段（1-诱导 2-维持 3-苏醒）',
  `drug_code` varchar(32) COMMENT '药品编码',
  `drug_name` varchar(200) NOT NULL COMMENT '药品名称',
  `dose` decimal(10,2) COMMENT '剂量',
  `unit` varchar(20) COMMENT '剂量单位（mg / ug / ml）',
  `route` tinyint COMMENT '给药途径（1-静脉推注 2-静脉泵注 3-静脉滴注 4-吸入 5-肌注 6-椎管内 7-局麻浸润 8-其他）',
  `remark` varchar(200) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='麻醉用药记录';

-- biz_anesthesia_vital  麻醉期间生命体征
CREATE TABLE `biz_anesthesia_vital` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `record_id` bigint NOT NULL COMMENT '麻醉记录ID',
  `sample_time` datetime NOT NULL COMMENT '采样时刻',
  `systolic` int COMMENT '收缩压（mmHg）',
  `diastolic` int COMMENT '舒张压（mmHg）',
  `heart_rate` int COMMENT '心率（次/分）',
  `respiration` int COMMENT '呼吸频率（次/分）',
  `temperature` decimal(4,1) COMMENT '体温（℃）',
  `spo2` int COMMENT '脉搏血氧饱和度（%）',
  `etco2` int COMMENT '呼气末二氧化碳分压（mmHg）',
  `remark` varchar(200) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vital_time` (`record_id`, `sample_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='麻醉期间生命体征';

-- biz_anesthesia_pacu  PACU 复苏记录
CREATE TABLE `biz_anesthesia_pacu` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `pacu_no` varchar(32) NOT NULL COMMENT '复苏单号',
  `record_id` bigint NOT NULL COMMENT '麻醉记录ID',
  `record_no` varchar(32) COMMENT '麻醉记录单号（快照）',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别（快照）（1-男 2-女）',
  `age` int COMMENT '年龄（快照）',
  `enter_time` datetime COMMENT '入 PACU 时间',
  `leave_time` datetime COMMENT '出 PACU 时间',
  `nurse_id` bigint COMMENT '复苏护士ID（员工ID）',
  `nurse_name` varchar(64) COMMENT '复苏护士姓名（快照）',
  `anesthetist_id` bigint COMMENT '负责麻醉医师ID（员工ID）',
  `anesthetist_name` varchar(64) COMMENT '负责麻醉医师姓名（快照）',
  `score_activity` tinyint COMMENT '肌力/活动（0-无 1-两肢可动 2-四肢可动）',
  `score_respiration` tinyint COMMENT '呼吸（0-需辅助通气 1-呼吸浅 2-深呼吸可咳嗽）',
  `score_circulation` tinyint COMMENT '血压（0-±50mmHg以上波动 1-±20~50 2-±20）',
  `score_consciousness` tinyint COMMENT '意识（0-无反应 1-可唤醒 2-完全清醒）',
  `score_spo2` tinyint COMMENT '氧合（0-吸氧下<90% 1-吸氧下>90% 2-空气下>92%）',
  `aldrete_total` tinyint COMMENT 'Aldrete 总分',
  `awareness` tinyint COMMENT '清醒程度（1-完全清醒 2-嗜睡可唤醒 3-未清醒）',
  `complication_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否发生并发症（0-无 1-有）',
  `complication_note` varchar(1000) COMMENT '并发症经过与处理',
  `oxygen_therapy` varchar(200) COMMENT '氧疗方式',
  `analgesia` varchar(200) COMMENT '镇痛方式',
  `disposition` tinyint COMMENT '出室去向（1-回病房 2-转ICU 3-继续留观）',
  `leave_criteria_met` tinyint COMMENT '是否满足出室标准（0-否 1-是）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-在室 1-已出室）',
  `charge_status` tinyint NOT NULL DEFAULT 0 COMMENT '计费状态（0-未计费 1-已计费 2-计费失败）',
  `fee_no` varchar(32) COMMENT '记账单号',
  `charged_amount` decimal(12,2) COMMENT '本次计入金额（元）',
  `charge_fail_reason` varchar(500) COMMENT '计费失败原因',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pacu_record` (`record_id`),
  UNIQUE KEY `uk_pacu_no` (`pacu_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PACU 复苏记录';

-- biz_anesthesia_followup  麻醉术后随访单
CREATE TABLE `biz_anesthesia_followup` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `followup_no` varchar(32) NOT NULL COMMENT '随访单号',
  `record_id` bigint NOT NULL COMMENT '麻醉记录ID',
  `record_no` varchar(32) COMMENT '麻醉记录单号（快照）',
  `apply_id` bigint COMMENT '手术申请单ID（快照）',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别（快照）（1-男 2-女）',
  `age` int COMMENT '年龄（快照）',
  `followup_time` datetime NOT NULL COMMENT '随访时间',
  `round_no` tinyint NOT NULL DEFAULT 1 COMMENT '随访轮次（1-术后即刻 2-24h 3-48h及以后）',
  `pain_score` int COMMENT '疼痛评分 NRS 0~10',
  `recovery` tinyint COMMENT '麻醉恢复情况（1-良好 2-一般 3-差）',
  `adverse_items` varchar(200) COMMENT '麻醉并发症码值',
  `adverse_note` varchar(1000) COMMENT '并发症经过描述',
  `handling` varchar(1000) COMMENT '处理措施与转归',
  `followup_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-草稿 1-已完成）',
  `followup_doctor_id` bigint COMMENT '随访麻醉医师ID（员工ID）',
  `followup_doctor_name` varchar(64) COMMENT '随访麻醉医师姓名（快照）',
  `finish_time` datetime COMMENT '随访完成时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_followup_no` (`followup_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='麻醉术后随访单';

-- biz_operation_charge_item  手术麻醉计费明细
CREATE TABLE `biz_operation_charge_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号（快照）',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `source_type` tinyint NOT NULL COMMENT '收费来源（预留）（1-麻醉记录 2-PACU复苏 3-手术）',
  `source_id` bigint NOT NULL COMMENT '来源单据ID',
  `source_no` varchar(32) COMMENT '来源单据号（快照）',
  `item_code` varchar(32) NOT NULL COMMENT '收费项目编码',
  `item_name` varchar(200) COMMENT '收费项目名称（快照）',
  `item_type` tinyint COMMENT '项目类型',
  `spec` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '计价单位',
  `quantity` decimal(12,2) COMMENT '数量',
  `price` decimal(12,2) COMMENT '单价',
  `amount` decimal(12,2) COMMENT '金额',
  `charge_status` tinyint NOT NULL DEFAULT 0 COMMENT '计费状态（0-未计费 1-已计费 2-计费失败）',
  `fee_record_id` bigint COMMENT '记账行ID',
  `fee_no` varchar(32) COMMENT '记账单号',
  `fail_reason` varchar(500) COMMENT '失败原因（未发现项目 / 金额异常 / 收费模块缺席）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_charge_src` (`source_type`, `source_id`, `item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术麻醉计费明细';

-- biz_day_surgery_item  日间手术准入目录
CREATE TABLE `biz_day_surgery_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `item_code` varchar(32) NOT NULL COMMENT '术式编码',
  `item_name` varchar(128) NOT NULL COMMENT '术式名称',
  `dept_id` bigint COMMENT '适用科室ID',
  `dept_name` varchar(128) COMMENT '适用科室名称（快照）',
  `max_stay_hours` int NOT NULL DEFAULT 48 COMMENT '最长滞留小时数',
  `operation_level` tinyint NOT NULL DEFAULT 2 COMMENT '手术级别',
  `anesthesia_type` tinyint COMMENT '麻醉方式（1-局部麻醉 2-椎管内麻醉 3-全身麻醉 4-神经阻滞 5-其他）',
  `standard_fee` decimal(10,2) COMMENT '标准费用',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dsitem_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日间手术准入目录';

-- biz_day_surgery_apply  日间手术登记单
CREATE TABLE `biz_day_surgery_apply` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `apply_no` varchar(32) NOT NULL COMMENT '登记单号',
  `item_id` bigint NOT NULL COMMENT '准入术式ID',
  `item_code` varchar(32) COMMENT '术式编码（快照）',
  `item_name` varchar(128) COMMENT '术式名称（快照）',
  `max_stay_hours` int COMMENT '最长滞留小时数',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `dept_id` bigint COMMENT '手术科室ID',
  `dept_name` varchar(128) COMMENT '手术科室名称（快照）',
  `doctor_id` bigint COMMENT '手术医生ID（员工ID）',
  `doctor_name` varchar(64) COMMENT '手术医生姓名',
  `plan_surgery_date` date NOT NULL COMMENT '计划手术日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待评估 2-评估通过 3-已安排 4-术后观察 5-已出院 6-已取消 7-已转住院）',
  `eval_result` tinyint COMMENT '术前评估结论（1-通过 2-不通过）',
  `eval_by` varchar(64) COMMENT '评估人',
  `eval_time` datetime COMMENT '评估时间',
  `eval_remark` varchar(500) COMMENT '评估意见/禁忌筛查结果',
  `surgery_time` datetime COMMENT '手术开始时间',
  `operating_room` varchar(64) COMMENT '手术间',
  `seq_no` int COMMENT '台次',
  `anesthesia_type` tinyint COMMENT '实际麻醉方式',
  `surgeon` varchar(64) COMMENT '主刀医生姓名',
  `arrange_by` varchar(64) COMMENT '安排人',
  `arrange_time` datetime COMMENT '安排时间',
  `surgery_end_time` datetime COMMENT '手术结束时间',
  `leave_type` tinyint COMMENT '离院方式（1-按时离院 2-转普通住院 3-非计划再入院）',
  `discharge_time` datetime COMMENT '离院时间',
  `discharge_by` varchar(64) COMMENT '离院登记人',
  `discharge_remark` varchar(500) COMMENT '出院评估结论/医嘱交代',
  `transfer_admission_id` bigint COMMENT '转住院的住院ID',
  `transfer_remark` varchar(500) COMMENT '转住院原因',
  `follow_count` int NOT NULL DEFAULT 0 COMMENT '随访次数',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dsapply_no` (`apply_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日间手术登记单';

-- biz_day_surgery_follow  日间手术随访台账
CREATE TABLE `biz_day_surgery_follow` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `apply_id` bigint NOT NULL COMMENT '登记单ID',
  `follow_type` tinyint NOT NULL DEFAULT 1 COMMENT '随访方式（1-电话 2-门诊 3-上门 4-线上）',
  `result` tinyint NOT NULL COMMENT '随访结果（1-无异常 2-有异常已处置 3-有异常再就诊 4-失联）',
  `content` varchar(500) COMMENT '随访内容',
  `operator_id` bigint COMMENT '随访人（员工ID）',
  `operator` varchar(64) COMMENT '随访人姓名',
  `follow_time` datetime COMMENT '随访时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日间手术随访台账';

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

-- biz_inpatient_operation  病案首页手术明细
CREATE TABLE `biz_inpatient_operation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `apply_id` bigint COMMENT '来源手术申请单ID',
  `seq_no` int NOT NULL DEFAULT 1 COMMENT '序号',
  `is_main` tinyint DEFAULT 0 COMMENT '是否主要手术（0-否 1-是）',
  `operation_code` varchar(32) COMMENT '手术操作编码',
  `operation_name` varchar(200) COMMENT '手术操作名称',
  `operation_date` datetime COMMENT '手术日期',
  `operation_level` tinyint COMMENT '手术级别（1-一级 2-二级 3-三级 4-四级）',
  `incision_level` tinyint COMMENT '切口等级（0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类）',
  `anesthesia_type` tinyint COMMENT '麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）',
  `surgeon_id` bigint COMMENT '主刀医师ID',
  `surgeon_name` varchar(50) COMMENT '主刀医师姓名',
  `assistant_name` varchar(200) COMMENT '助手姓名',
  `operation_basis` varchar(500) COMMENT '手术依据',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病案首页手术明细';

-- biz_inpatient_record  住院病历文书
CREATE TABLE `biz_inpatient_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '病历文书号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别（快照）（1-男 2-女）',
  `age` int COMMENT '年龄（快照）',
  `age_unit` tinyint COMMENT '年龄单位（快照）（1-岁 2-月 3-天）',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(64) COMMENT '科室名称（快照）',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称（快照）',
  `bed_no` varchar(32) COMMENT '床号（快照）',
  `record_type` tinyint NOT NULL COMMENT '文书类型',
  `record_title` varchar(200) COMMENT '文书标题',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `chief_complaint` varchar(500) COMMENT '主诉',
  `present_illness` text COMMENT '现病史',
  `past_history` text COMMENT '既往史',
  `personal_history` text COMMENT '个人史（含婚育、烟酒、职业）',
  `family_history` text COMMENT '家族史',
  `allergy_history` varchar(500) COMMENT '过敏史',
  `temperature` decimal(4,1) COMMENT '体温（℃）',
  `pulse` int COMMENT '脉搏（次/分）',
  `respiration` int COMMENT '呼吸（次/分）',
  `systolic_pressure` int COMMENT '收缩压（mmHg）',
  `diastolic_pressure` int COMMENT '舒张压（mmHg）',
  `height` decimal(5,1) COMMENT '身高（cm）',
  `weight` decimal(5,1) COMMENT '体重（kg）',
  `general_condition` varchar(500) COMMENT '一般情况（神志/发育/营养/体位/面容）',
  `skin_mucosa` varchar(500) COMMENT '皮肤黏膜',
  `head_neck` varchar(500) COMMENT '头颈部',
  `chest_lung` varchar(500) COMMENT '胸部及肺',
  `heart` varchar(500) COMMENT '心脏',
  `abdomen` varchar(500) COMMENT '腹部',
  `spine_limbs` varchar(500) COMMENT '脊柱四肢',
  `nervous_system` varchar(500) COMMENT '神经系统',
  `specialist_exam` text COMMENT '专科检查',
  `auxiliary_exam` text COMMENT '辅助检查',
  `diagnosis_name` varchar(500) COMMENT '诊断名称',
  `diagnosis_code` varchar(100) COMMENT '诊断编码',
  `treatment_plan` text COMMENT '诊疗计划 / 处理意见',
  `course_note` text COMMENT '病程记录正文',
  `record_status` tinyint NOT NULL DEFAULT 1 COMMENT '文书状态（1-草稿 2-已提交 3-已归档）',
  `doctor_id` bigint COMMENT '书写医生ID',
  `doctor_name` varchar(64) COMMENT '书写医生姓名',
  `submit_time` datetime COMMENT '提交时间',
  `archive_time` datetime COMMENT '归档时间',
  `archive_by` bigint COMMENT '归档人ID（员工ID）',
  `archive_by_name` varchar(64) COMMENT '归档人姓名',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '签名状态（0-未签名 1-已签名 2-签名已失效）',
  `sign_id` bigint COMMENT '当前有效签名ID',
  `signed_time` datetime COMMENT '最近一次签名时刻',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院病历文书';

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
ALTER TABLE `biz_anesthesia_followup` ADD CONSTRAINT `fk_biz_anesthesia_followup_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_anesthesia_record` (`id`);
ALTER TABLE `biz_anesthesia_followup` ADD CONSTRAINT `fk_biz_anesthesia_followup_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_anesthesia_followup` ADD CONSTRAINT `fk_biz_anesthesia_followup_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_anesthesia_followup` ADD CONSTRAINT `fk_biz_anesthesia_followup_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_anesthesia_followup` ADD CONSTRAINT `fk_biz_anesthesia_followup_followup_doctor_id` FOREIGN KEY (`followup_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_anesthesia_med` ADD CONSTRAINT `fk_biz_anesthesia_med_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_anesthesia_record` (`id`);
ALTER TABLE `biz_anesthesia_pacu` ADD CONSTRAINT `fk_biz_anesthesia_pacu_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_anesthesia_record` (`id`);
ALTER TABLE `biz_anesthesia_pacu` ADD CONSTRAINT `fk_biz_anesthesia_pacu_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_anesthesia_pacu` ADD CONSTRAINT `fk_biz_anesthesia_pacu_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_anesthesia_pacu` ADD CONSTRAINT `fk_biz_anesthesia_pacu_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_anesthesia_pacu` ADD CONSTRAINT `fk_biz_anesthesia_pacu_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_anesthesia_pacu` ADD CONSTRAINT `fk_biz_anesthesia_pacu_anesthetist_id` FOREIGN KEY (`anesthetist_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_anesthesia_record` ADD CONSTRAINT `fk_biz_anesthesia_record_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_anesthesia_record` ADD CONSTRAINT `fk_biz_anesthesia_record_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_anesthesia_record` ADD CONSTRAINT `fk_biz_anesthesia_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_anesthesia_record` ADD CONSTRAINT `fk_biz_anesthesia_record_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_anesthesia_visit` (`id`);
ALTER TABLE `biz_anesthesia_record` ADD CONSTRAINT `fk_biz_anesthesia_record_anesthetist_id` FOREIGN KEY (`anesthetist_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_anesthesia_record` ADD CONSTRAINT `fk_biz_anesthesia_record_submit_doctor_id` FOREIGN KEY (`submit_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_anesthesia_record` ADD CONSTRAINT `fk_biz_anesthesia_record_audit_doctor_id` FOREIGN KEY (`audit_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_anesthesia_visit` ADD CONSTRAINT `fk_biz_anesthesia_visit_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_anesthesia_visit` ADD CONSTRAINT `fk_biz_anesthesia_visit_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_anesthesia_visit` ADD CONSTRAINT `fk_biz_anesthesia_visit_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_anesthesia_visit` ADD CONSTRAINT `fk_biz_anesthesia_visit_visit_doctor_id` FOREIGN KEY (`visit_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_anesthesia_vital` ADD CONSTRAINT `fk_biz_anesthesia_vital_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_anesthesia_record` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_item_id` FOREIGN KEY (`item_id`) REFERENCES `biz_day_surgery_item` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_transfer_admission_id` FOREIGN KEY (`transfer_admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_day_surgery_follow` ADD CONSTRAINT `fk_biz_day_surgery_follow_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_day_surgery_apply` (`id`);
ALTER TABLE `biz_day_surgery_follow` ADD CONSTRAINT `fk_biz_day_surgery_follow_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_day_surgery_item` ADD CONSTRAINT `fk_biz_day_surgery_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_surgeon_id` FOREIGN KEY (`surgeon_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_anesthetist_id` FOREIGN KEY (`anesthetist_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_schedule_doctor_id` FOREIGN KEY (`schedule_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_preop_check_doctor_id` FOREIGN KEY (`preop_check_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_finish_doctor_id` FOREIGN KEY (`finish_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_operation_id` FOREIGN KEY (`operation_id`) REFERENCES `biz_inpatient_operation` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inpatient_record` (`id`);
ALTER TABLE `biz_operation_apply` ADD CONSTRAINT `fk_biz_operation_apply_cancel_doctor_id` FOREIGN KEY (`cancel_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_charge_item` ADD CONSTRAINT `fk_biz_operation_charge_item_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_operation_charge_item` ADD CONSTRAINT `fk_biz_operation_charge_item_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_operation_charge_item` ADD CONSTRAINT `fk_biz_operation_charge_item_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_operation_charge_item` ADD CONSTRAINT `fk_biz_operation_charge_item_fee_record_id` FOREIGN KEY (`fee_record_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_operation_count` ADD CONSTRAINT `fk_biz_operation_count_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_operation_count` ADD CONSTRAINT `fk_biz_operation_count_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_operation_count` ADD CONSTRAINT `fk_biz_operation_count_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_operation_count` ADD CONSTRAINT `fk_biz_operation_count_instrument_nurse_id` FOREIGN KEY (`instrument_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_count` ADD CONSTRAINT `fk_biz_operation_count_circulate_nurse_id` FOREIGN KEY (`circulate_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_count` ADD CONSTRAINT `fk_biz_operation_count_before_nurse_id` FOREIGN KEY (`before_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_count` ADD CONSTRAINT `fk_biz_operation_count_closure_nurse_id` FOREIGN KEY (`closure_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_count` ADD CONSTRAINT `fk_biz_operation_count_final_nurse_id` FOREIGN KEY (`final_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_count_item` ADD CONSTRAINT `fk_biz_operation_count_item_count_id` FOREIGN KEY (`count_id`) REFERENCES `biz_operation_count` (`id`);
ALTER TABLE `biz_operation_safety_check` ADD CONSTRAINT `fk_biz_operation_safety_check_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_operation_safety_check` ADD CONSTRAINT `fk_biz_operation_safety_check_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_operation_safety_check` ADD CONSTRAINT `fk_biz_operation_safety_check_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_operation_safety_check` ADD CONSTRAINT `fk_biz_operation_safety_check_surgeon_id` FOREIGN KEY (`surgeon_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_safety_check` ADD CONSTRAINT `fk_biz_operation_safety_check_anesthetist_id` FOREIGN KEY (`anesthetist_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_safety_check` ADD CONSTRAINT `fk_biz_operation_safety_check_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_operation_safety_check` ADD CONSTRAINT `fk_biz_operation_safety_check_recorder_id` FOREIGN KEY (`recorder_id`) REFERENCES `sys_employee` (`id`);
