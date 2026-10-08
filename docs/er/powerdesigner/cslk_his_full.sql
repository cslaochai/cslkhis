-- ============================================================
-- cslk_his 全库 E-R 反查脚本：302 表 / 831 条关系
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================

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

-- biz_admission_order  入院通知单
CREATE TABLE `biz_admission_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '住院证号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `phone` varchar(20) COMMENT '联系电话',
  `id_card` varchar(18) COMMENT '身份证号',
  `regist_id` bigint COMMENT '来源挂号ID',
  `regist_no` varchar(32) COMMENT '来源挂号号',
  `visit_id` bigint COMMENT '来源就诊次ID',
  `source_dept_id` bigint COMMENT '开证科室ID',
  `source_dept_name` varchar(100) COMMENT '开证科室名称',
  `source_doctor_id` bigint COMMENT '开证医生ID',
  `source_doctor_name` varchar(50) COMMENT '开证医生姓名',
  `apply_dept_id` bigint COMMENT '拟收治科室ID',
  `apply_dept_name` varchar(100) COMMENT '拟收治科室名称',
  `diagnosis_code` varchar(32) COMMENT '拟诊ICD编码',
  `diagnosis_name` varchar(200) COMMENT '拟诊名称',
  `diagnosis_note` varchar(500) COMMENT '病情与收治说明',
  `insurance_type` varchar(50) COMMENT '医保类型',
  `medical_insurance_no` varchar(50) COMMENT '医保卡号',
  `order_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待收治 2-已收治 3-已作废 4-已过期）',
  `expect_admit_time` datetime COMMENT '预计入院时间',
  `order_time` datetime NOT NULL COMMENT '开证时间',
  `valid_until` datetime COMMENT '有效期至',
  `admission_id` bigint COMMENT '收治后回填的入院ID',
  `admit_time` datetime COMMENT '实际收治时间',
  `admit_dept_id` bigint COMMENT '实际收治科室ID',
  `cancel_reason` varchar(200) COMMENT '作废原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ao_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入院通知单';

-- biz_adverse_event  不良事件上报
CREATE TABLE `biz_adverse_event` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `event_no` varchar(32) NOT NULL COMMENT '事件编号 AE+yyyyMMdd+4位',
  `event_type` tinyint NOT NULL COMMENT '事件类型',
  `event_level` tinyint NOT NULL COMMENT '事件等级',
  `acquired_flag` tinyint NOT NULL DEFAULT 1 COMMENT '来源（1-院内获得 2-入院带入）',
  `occur_dept_id` bigint NOT NULL COMMENT '发生科室 sys_department.id',
  `occur_dept_name` varchar(64) COMMENT '发生科室名称',
  `occur_ward_id` bigint COMMENT '发生病区ID',
  `occur_ward_name` varchar(128) COMMENT '发生病区名称',
  `occur_time` datetime NOT NULL COMMENT '发生时间',
  `patient_id` bigint COMMENT '关联患者',
  `patient_name` varchar(64) COMMENT '患者姓名（快照，可空）',
  `visit_id` bigint COMMENT '关联就诊 biz_regist_info.id（可空）',
  `title` varchar(100) NOT NULL COMMENT '事件摘要',
  `description` text NOT NULL COMMENT '事件详细经过',
  `immediate_action` varchar(500) COMMENT '即时处置措施',
  `reporter_id` bigint NOT NULL COMMENT '上报人员工ID',
  `reporter_name` varchar(64) COMMENT '上报人姓名',
  `report_time` datetime NOT NULL COMMENT '上报时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已上报待处理 2-处理中 3-已整改 4-已结案）',
  `handler_id` bigint COMMENT '处理人员工ID',
  `handler_name` varchar(64) COMMENT '处理人姓名',
  `handle_remark` varchar(500) COMMENT '处理意见（D）',
  `handle_time` datetime COMMENT '处理时间',
  `rectify_by_id` bigint COMMENT '整改人员工ID',
  `rectify_by_name` varchar(64) COMMENT '整改人姓名',
  `rectify_measures` varchar(500) COMMENT '整改措施（C）',
  `rectify_time` datetime COMMENT '整改时间',
  `close_by_id` bigint COMMENT '结案人员工ID',
  `close_by_name` varchar(64) COMMENT '结案人姓名',
  `verify_remark` varchar(500) COMMENT '验证结论（A）',
  `close_time` datetime COMMENT '结案时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `create_time` datetime COMMENT '创建时间',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_no` (`event_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不良事件上报';

-- biz_alert  预警记录
CREATE TABLE `biz_alert` (
  `alert_id` bigint NOT NULL COMMENT '预警ID',
  `alert_no` varchar(32) NOT NULL COMMENT '预警编号',
  `rule_id` bigint COMMENT '规则ID',
  `alert_type` varchar(32) NOT NULL COMMENT '预警类型',
  `alert_content` varchar(1000) NOT NULL COMMENT '预警内容',
  `alert_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-未处理 1-已处理 2-已忽略）',
  `notify_user_id` bigint COMMENT '通知用户ID',
  `notify_time` datetime COMMENT '通知时间',
  `read_time` datetime COMMENT '阅读时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`alert_id`),
  UNIQUE KEY `uk_alert_no` (`alert_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录';

-- biz_anesthesia_followup  麻醉术后随访单
CREATE TABLE `biz_anesthesia_followup` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `followup_no` varchar(32) NOT NULL COMMENT '随访单号',
  `record_id` bigint NOT NULL COMMENT '麻醉记录ID',
  `record_no` varchar(32) COMMENT '麻醉记录单号',
  `apply_id` bigint COMMENT '手术申请单ID',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `followup_time` datetime NOT NULL COMMENT '随访时间',
  `round_no` tinyint NOT NULL DEFAULT 1 COMMENT '随访轮次（1-术后即刻 2-24h 3-48h及以后）',
  `pain_score` int COMMENT '疼痛评分 NRS 0~10',
  `recovery` tinyint COMMENT '麻醉恢复情况（1-良好 2-一般 3-差）',
  `adverse_items` varchar(200) COMMENT '麻醉并发症码值',
  `adverse_note` varchar(1000) COMMENT '并发症经过描述',
  `handling` varchar(1000) COMMENT '处理措施与转归',
  `followup_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-草稿 1-已完成）',
  `followup_doctor_id` bigint COMMENT '随访麻醉医师ID（员工ID）',
  `followup_doctor_name` varchar(64) COMMENT '随访麻醉医师姓名',
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

-- biz_anesthesia_pacu  PACU 复苏记录
CREATE TABLE `biz_anesthesia_pacu` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `pacu_no` varchar(32) NOT NULL COMMENT '复苏单号',
  `record_id` bigint NOT NULL COMMENT '麻醉记录ID',
  `record_no` varchar(32) COMMENT '麻醉记录单号',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `enter_time` datetime COMMENT '入 PACU 时间',
  `leave_time` datetime COMMENT '出 PACU 时间',
  `nurse_id` bigint COMMENT '复苏护士ID（员工ID）',
  `nurse_name` varchar(64) COMMENT '复苏护士姓名',
  `anesthetist_id` bigint COMMENT '负责麻醉医师ID（员工ID）',
  `anesthetist_name` varchar(64) COMMENT '负责麻醉医师姓名',
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
  UNIQUE KEY `uk_pacu_no` (`pacu_no`),
  UNIQUE KEY `uk_pacu_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PACU 复苏记录';

-- biz_anesthesia_record  麻醉记录单
CREATE TABLE `biz_anesthesia_record` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `record_no` varchar(32) NOT NULL COMMENT '麻醉记录单号',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `visit_id` bigint COMMENT '来源术前访视单ID',
  `anesthesia_type` tinyint COMMENT '麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）',
  `asa_grade` tinyint COMMENT 'ASA 分级',
  `anesthetist_id` bigint COMMENT '麻醉医师ID（员工ID）',
  `anesthetist_name` varchar(64) COMMENT '麻醉医师姓名',
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
  `submit_doctor_name` varchar(64) COMMENT '提交人姓名',
  `submit_time` datetime COMMENT '提交时间',
  `audit_doctor_id` bigint COMMENT '审核人ID（员工ID）',
  `audit_doctor_name` varchar(64) COMMENT '审核人姓名',
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

-- biz_anesthesia_visit  麻醉术前访视单
CREATE TABLE `biz_anesthesia_visit` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `visit_no` varchar(32) NOT NULL COMMENT '访视单号',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `diagnosis` varchar(500) COMMENT '术前诊断',
  `planned_operation_code` varchar(32) COMMENT '拟施手术编码',
  `planned_operation_name` varchar(200) COMMENT '拟施手术名称',
  `operation_level` tinyint COMMENT '手术级别',
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
  `visit_doctor_name` varchar(64) COMMENT '访视麻醉医师姓名',
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

-- biz_antibiotic_alias  抗菌药物品名别名
CREATE TABLE `biz_antibiotic_alias` (
  `id` bigint NOT NULL COMMENT '主键',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) COMMENT '药品编码',
  `drug_name` varchar(200) COMMENT '药品目录名',
  `alias_name` varchar(200) NOT NULL COMMENT '别名',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_antibiotic_alias` (`alias_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抗菌药物品名别名';

-- biz_antibiotic_auth  抗菌药物处方权授权
CREATE TABLE `biz_antibiotic_auth` (
  `id` bigint NOT NULL COMMENT '主键',
  `auth_no` varchar(32) NOT NULL COMMENT '授权编号',
  `doctor_id` bigint NOT NULL COMMENT '医师ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '医师姓名',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `title` varchar(50) COMMENT '职称',
  `auth_level` tinyint NOT NULL COMMENT '授权级别（1-非限制使用级 2-限制使用级 3-特殊使用级）',
  `auth_basis` varchar(100) COMMENT '授权依据',
  `auth_date` date NOT NULL COMMENT '授权日期',
  `expire_date` date NOT NULL COMMENT '有效期至',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-有效 2-暂停 3-取消）',
  `authorizer` varchar(50) COMMENT '授权人',
  `authorize_org` varchar(100) COMMENT '授权部门',
  `revoke_reason` varchar(500) COMMENT '暂停/取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_antibiotic_auth_doctor` (`doctor_id`, `auth_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抗菌药物处方权授权';

-- biz_antibiotic_incision_review  I 类切口预防用药点评
CREATE TABLE `biz_antibiotic_incision_review` (
  `id` bigint NOT NULL COMMENT '主键',
  `review_no` varchar(32) NOT NULL COMMENT '点评编号',
  `operation_apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号',
  `admission_id` bigint COMMENT '入院ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_name` varchar(100) COMMENT '手术科室',
  `operation_name` varchar(200) COMMENT '手术名称',
  `operation_code` varchar(32) COMMENT '手术编码 ICD-9-CM-3',
  `operation_time` datetime COMMENT '手术开始时间',
  `surgeon_name` varchar(64) COMMENT '主刀医师',
  `incision_level` tinyint NOT NULL DEFAULT 1 COMMENT '切口等级',
  `drug_id` bigint COMMENT '预防用药药品ID',
  `drug_name` varchar(200) COMMENT '预防用药名称',
  `antibiotic_level` tinyint COMMENT '预防用药分级（快照：1/2/3）',
  `indication_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否有预防用药指征（0-无 1-有）',
  `timing_type` tinyint COMMENT '给药时机',
  `course_hours` int COMMENT '预防用药总时长',
  `combo_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否联合用药（0-否 1-是）',
  `combo_reason` varchar(500) COMMENT '联合用药理由',
  `consult_flag` tinyint NOT NULL DEFAULT 0 COMMENT '特殊使用级是否有抗菌药物管理工作组会诊同意（0-无 1-有）',
  `review_result` tinyint COMMENT '点评结论（1-合理 2-不合理）',
  `problem_types` varchar(200) COMMENT '问题码',
  `review_opinion` varchar(500) COMMENT '点评意见',
  `reviewer_id` bigint COMMENT '点评人员工ID',
  `reviewer_name` varchar(50) COMMENT '点评人姓名',
  `review_time` datetime COMMENT '点评时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_incision_apply` (`operation_apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='I 类切口预防用药点评';

-- biz_antibiotic_stats  抗菌药物使用监测指标
CREATE TABLE `biz_antibiotic_stats` (
  `id` bigint NOT NULL COMMENT '主键',
  `stat_month` char(7) NOT NULL COMMENT '统计月份',
  `scope_type` tinyint NOT NULL DEFAULT 1 COMMENT '统计范围（1-全院 2-科室）',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `op_rx_count` int NOT NULL DEFAULT 0 COMMENT '门急诊处方总数（处方状态 3/4，源 1/2）',
  `op_abx_rx_count` int NOT NULL DEFAULT 0 COMMENT '含抗菌药物的门急诊处方数',
  `op_usage_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '门诊抗菌药物使用率（%）',
  `ip_discharge_count` int NOT NULL DEFAULT 0 COMMENT '同期出院患者数',
  `ip_abx_patient_count` int NOT NULL DEFAULT 0 COMMENT '出院患者中使用抗菌药物的人数',
  `ip_usage_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '住院抗菌药物使用率（%）',
  `patient_days` int NOT NULL DEFAULT 0 COMMENT '收治患者人天数',
  `ddds` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '抗菌药物累计 DDD 数',
  `aud` decimal(8,2) NOT NULL DEFAULT 0.00 COMMENT '使用强度 AUD',
  `abx_treat_count` int NOT NULL DEFAULT 0 COMMENT '使用抗菌药物的住院患者数',
  `micro_submit_count` int NOT NULL DEFAULT 0 COMMENT '其中送检微生物标本的患者数',
  `micro_submit_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '微生物标本送检率（%）',
  `unmatched_order_count` int NOT NULL DEFAULT 0 COMMENT '未匹配到抗菌药物目录的住院药品医嘱数',
  `generate_by` varchar(64) COMMENT '生成人',
  `generate_time` datetime COMMENT '生成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_antibiotic_stats` (`stat_month`, `scope_type`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抗菌药物使用监测指标';

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

-- biz_archive_borrow  病案借阅复印
CREATE TABLE `biz_archive_borrow` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `borrow_no` varchar(32) NOT NULL COMMENT '单号 BR+yyyyMMdd+4位',
  `borrow_type` tinyint NOT NULL COMMENT '类型（1-借阅 2-复印）',
  `archive_id` bigint NOT NULL COMMENT '归档记录 biz_medical_record_archive.id',
  `record_no` varchar(64) COMMENT '病历号',
  `patient_name` varchar(64) COMMENT '患者姓名',
  `dept_name` varchar(64) COMMENT '病历所属科室',
  `applicant_id` bigint NOT NULL COMMENT '申请人员工ID',
  `applicant_name` varchar(64) COMMENT '申请人姓名',
  `purpose` varchar(500) NOT NULL COMMENT '借阅/复印用途（病历讨论/医保核查/司法取证/科研等）',
  `expect_return_date` date COMMENT '应归还日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待审核 2-已借出 3-已归还 4-已拒绝 5-已复印）',
  `audit_by_id` bigint COMMENT '审核人员工ID',
  `audit_by_name` varchar(64) COMMENT '审核人姓名',
  `audit_remark` varchar(500) COMMENT '审核意见',
  `audit_time` datetime COMMENT '审核时间',
  `lend_time` datetime COMMENT '借出时间',
  `return_time` datetime COMMENT '归还时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `create_time` datetime COMMENT '创建时间',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_borrow_no` (`borrow_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病案借阅复印';

-- biz_archive_code_task  病案编码任务池
CREATE TABLE `biz_archive_code_task` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `task_no` varchar(32) NOT NULL COMMENT '任务号 CT+yyyyMMdd+4位',
  `archive_id` bigint NOT NULL COMMENT '归档记录 biz_medical_record_archive.id',
  `record_no` varchar(64) COMMENT '病历号',
  `patient_name` varchar(64) COMMENT '患者姓名',
  `dept_name` varchar(64) COMMENT '病历所属科室',
  `diagnosis` varchar(500) COMMENT '病历诊断',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待编码 2-已提交 3-已完成 4-已退修）',
  `coder_id` bigint COMMENT '编码人员工ID',
  `coder_name` varchar(64) COMMENT '编码员姓名',
  `assign_time` datetime COMMENT '分配时间',
  `main_icd_code` varchar(20) COMMENT '主诊断 ICD-10 编码',
  `main_icd_name` varchar(200) COMMENT '主诊断名称',
  `other_icd_text` varchar(500) COMMENT '其他诊断/手术 ICD',
  `submit_time` datetime COMMENT '提交编码时间',
  `audit_by_id` bigint COMMENT '审核人员工ID',
  `audit_by_name` varchar(64) COMMENT '审核人姓名',
  `audit_remark` varchar(500) COMMENT '审核意见',
  `audit_time` datetime COMMENT '审核时间',
  `return_count` int NOT NULL DEFAULT 0 COMMENT '累计退修次数',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `create_time` datetime COMMENT '创建时间',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病案编码任务池';

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

-- biz_bed_allocate  床位调配台账
CREATE TABLE `biz_bed_allocate` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `allocate_no` varchar(32) NOT NULL COMMENT '调配单号',
  `bed_id` bigint NOT NULL COMMENT '床位ID',
  `bed_no` varchar(16) COMMENT '床位号',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称',
  `own_dept_id` bigint COMMENT '床位归属科室ID',
  `own_dept_name` varchar(100) COMMENT '床位归属科室名称',
  `use_dept_id` bigint COMMENT '实际使用科室ID',
  `use_dept_name` varchar(100) COMMENT '实际使用科室名称',
  `wait_id` bigint COMMENT '来源等床记录ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `alloc_type` tinyint NOT NULL DEFAULT 1 COMMENT '调配类型（1-本科室预留 2-跨科调配 3-急诊占床）',
  `alloc_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已预留 2-已转入院 3-已释放 4-已作废）',
  `operator_id` bigint COMMENT '操作人ID',
  `operator_name` varchar(50) COMMENT '操作人姓名',
  `operate_time` datetime NOT NULL COMMENT '操作时间',
  `release_time` datetime COMMENT '释放时间',
  `release_reason` varchar(200) COMMENT '释放/作废原因',
  `admission_id` bigint COMMENT '转入院后的入院ID',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bed_allocate_no` (`allocate_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='床位调配台账';

-- biz_bed_wait  等床队列
CREATE TABLE `biz_bed_wait` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `wait_no` varchar(32) NOT NULL COMMENT '等待号',
  `admission_order_id` bigint COMMENT '来源住院证ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `phone` varchar(20) COMMENT '联系电话',
  `apply_dept_id` bigint COMMENT '拟收治科室ID',
  `apply_dept_name` varchar(100) COMMENT '拟收治科室名称',
  `expect_ward_id` bigint COMMENT '期望病区ID',
  `bed_type` varchar(32) NOT NULL DEFAULT 'normal' COMMENT '需求床型',
  `priority` tinyint NOT NULL DEFAULT 1 COMMENT '优先级（1-普通 2-急 3-危重）',
  `gender_limit` tinyint NOT NULL DEFAULT 0 COMMENT '性别限制（0-不限 1-限男床 2-限女床）',
  `isolation_flag` tinyint NOT NULL DEFAULT 0 COMMENT '隔离需求（0-否 1-是）',
  `expect_admit_date` date COMMENT '预计入院日期',
  `diagnosis_name` varchar(200) COMMENT '拟诊名称',
  `wait_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-等待中 1-已安排床位 2-已收治 3-已取消）',
  `register_time` datetime NOT NULL COMMENT '登记排队时间',
  `assigned_bed_id` bigint COMMENT '已安排的床位ID',
  `assigned_bed_no` varchar(16) COMMENT '已安排床位号',
  `assigned_ward_id` bigint COMMENT '已安排床位所在病区ID',
  `assigned_ward_name` varchar(64) COMMENT '已安排病区名称',
  `assigned_dept_id` bigint COMMENT '已安排床位所属科室ID',
  `assigned_dept_name` varchar(100) COMMENT '已安排床位所属科室名称',
  `assigned_time` datetime COMMENT '安排床位时间',
  `assigned_by` varchar(64) COMMENT '安排人',
  `admission_id` bigint COMMENT '收治后回填的入院ID',
  `admit_time` datetime COMMENT '实际收治时间',
  `cancel_reason` varchar(200) COMMENT '取消原因',
  `cancel_time` datetime COMMENT '取消时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bed_wait_no` (`wait_no`),
  UNIQUE KEY `uk_bed_wait_order` (`admission_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='等床队列';

-- biz_blood_crossmatch  交叉配血记录
CREATE TABLE `biz_blood_crossmatch` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `match_no` varchar(32) NOT NULL COMMENT '配血编号',
  `apply_no` varchar(32) COMMENT '用血申请单号',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `patient_blood_type` tinyint COMMENT '患者血型（1-A 2-B 3-O 4-AB）',
  `patient_rh_type` tinyint COMMENT '患者 Rh（1-阳性 2-阴性）',
  `bag_no` varchar(50) NOT NULL COMMENT '血袋号',
  `bag_blood_type` tinyint COMMENT '血袋血型',
  `bag_rh_type` tinyint COMMENT '血袋 Rh',
  `component_type` tinyint COMMENT '血液成分',
  `volume` int DEFAULT 0 COMMENT '血量（ml）',
  `method` tinyint DEFAULT 2 COMMENT '配血方法（1-盐水法 2-凝聚胺法 3-抗人球蛋白法 4-微柱凝胶法）',
  `result` tinyint DEFAULT 1 COMMENT '配血结果（1-相合 2-不相合 3-可疑凝集）',
  `conclusion` varchar(500) COMMENT '配血结论',
  `status` tinyint DEFAULT 1 COMMENT '状态（1-待配血 2-已配血 3-已复核 4-已作废）',
  `operator` varchar(64) COMMENT '配血人',
  `match_time` datetime COMMENT '配血时间',
  `verifier` varchar(64) COMMENT '复核人',
  `verify_time` datetime COMMENT '复核时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_match_no` (`match_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交叉配血记录';

-- biz_blood_inventory  血库血袋库存
CREATE TABLE `biz_blood_inventory` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `bag_no` varchar(50) NOT NULL COMMENT '血袋号（唯一）',
  `blood_type` tinyint NOT NULL COMMENT '血型（1-A 2-B 3-O 4-AB）',
  `rh_type` tinyint DEFAULT 1 COMMENT 'Rh 血型（1-阳性 2-阴性）',
  `component_type` tinyint NOT NULL COMMENT '血液成分（1-全血 2-红细胞悬液 3-洗涤红细胞 4-冰冻血浆 5-血小板 6-冷沉淀）',
  `volume` int DEFAULT 0 COMMENT '血量（ml）',
  `unit_amount` decimal(6,2) DEFAULT 0.00 COMMENT '单位（U）',
  `collect_date` date COMMENT '采集日期',
  `expire_date` date COMMENT '失效日期',
  `source_type` tinyint DEFAULT 1 COMMENT '血液来源（1-血站 2-自体储血 3-互助献血）',
  `source_name` varchar(100) COMMENT '来源单位 / 献血人',
  `donor_no` varchar(50) COMMENT '献血码',
  `abo_verify` tinyint DEFAULT 0 COMMENT '血型复核（0-未复核 1-已复核）',
  `storage_loc` varchar(100) COMMENT '存放位置',
  `status` tinyint DEFAULT 1 COMMENT '状态（1-在库 2-已预留 3-已发血 4-已报废 5-已退回）',
  `inbound_by` varchar(64) COMMENT '入库人',
  `inbound_time` datetime COMMENT '入库时间',
  `outbound_by` varchar(64) COMMENT '出库人',
  `outbound_time` datetime COMMENT '出库时间',
  `apply_no` varchar(32) COMMENT '关联用血申请单号',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bag_no` (`bag_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血库血袋库存';

-- biz_blood_stock_log  血库出入库流水
CREATE TABLE `biz_blood_stock_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `bag_no` varchar(50) NOT NULL COMMENT '血袋号',
  `biz_type` tinyint NOT NULL COMMENT '业务类型（1-入库 2-发血 3-退回 4-报废 5-预留 6-取消预留）',
  `from_status` tinyint COMMENT '变更前状态',
  `to_status` tinyint COMMENT '变更后状态',
  `apply_no` varchar(32) COMMENT '关联用血申请单号',
  `reason` varchar(500) COMMENT '原因',
  `operator` varchar(64) COMMENT '操作人',
  `operate_time` datetime COMMENT '操作时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血库出入库流水';

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

-- biz_checkup_record  体检登记
CREATE TABLE `biz_checkup_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '体检编号',
  `patient_id` bigint NOT NULL COMMENT '体检人ID',
  `patient_name` varchar(50) NOT NULL COMMENT '体检人姓名',
  `gender` tinyint COMMENT '性别（2-女 9-未知）',
  `age` int COMMENT '年龄',
  `phone` varchar(20) COMMENT '联系电话',
  `person_type` tinyint NOT NULL DEFAULT 1 COMMENT '体检对象（1-个人 2-团体）',
  `package_id` bigint NOT NULL COMMENT '套餐ID',
  `package_name` varchar(100) NOT NULL COMMENT '套餐名称',
  `total_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '应收金额',
  `checkup_date` date NOT NULL COMMENT '体检日期',
  `record_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已登记 2-检查中 3-已完成 4-已出报告）',
  `conclusion` varchar(1000) COMMENT '总检结论',
  `doctor_name` varchar(50) COMMENT '总检医师',
  `report_time` datetime COMMENT '报告时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检登记';

-- biz_checkup_result  体检结果明细
CREATE TABLE `biz_checkup_result` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_id` bigint NOT NULL COMMENT '体检登记ID',
  `item_name` varchar(100) NOT NULL COMMENT '项目名称',
  `item_type` tinyint NOT NULL DEFAULT 1 COMMENT '项目类别（1-检验 2-检查 3-一般）',
  `ref_standard` varchar(200) COMMENT '参考范围',
  `result_value` varchar(500) COMMENT '结果值/所见',
  `abnormal_flag` tinyint NOT NULL DEFAULT 0 COMMENT '异常标志（0-正常 1-异常 2-待查）',
  `summary_text` varchar(500) COMMENT '单项小结/建议',
  `checker_name` varchar(50) COMMENT '检查/检验医师',
  `result_time` datetime COMMENT '结果录入时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检结果明细';

-- biz_chronic_record  慢病建档
CREATE TABLE `biz_chronic_record` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `record_no` varchar(32) NOT NULL COMMENT '档案编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `disease_code` varchar(32) NOT NULL COMMENT '慢病编码',
  `disease_name` varchar(100) NOT NULL COMMENT '慢病名称',
  `doctor_id` bigint NOT NULL COMMENT '认定医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '认定医生姓名',
  `dept_id` bigint COMMENT '认定科室ID',
  `dept_name` varchar(100) COMMENT '认定科室名称',
  `confirm_status` tinyint NOT NULL DEFAULT 0 COMMENT '认定状态（0-待认定 1-已认定 2-已取消）',
  `confirm_time` datetime COMMENT '认定时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='慢病建档';

-- biz_clinical_rule_check  临床规则校验记录
CREATE TABLE `biz_clinical_rule_check` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `check_no` varchar(32) NOT NULL COMMENT '校验编号',
  `record_id` bigint NOT NULL COMMENT '病历ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `rule_type` tinyint NOT NULL COMMENT '规则类型（1-配伍禁忌 2-检验诊断关联性 3-用药合理性）',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_content` varchar(500) COMMENT '规则内容',
  `check_result` tinyint NOT NULL DEFAULT 1 COMMENT '校验结果（0-不通过 1-通过）',
  `error_level` tinyint COMMENT '错误级别（1-警告 2-错误 3-严重）',
  `error_detail` varchar(1000) COMMENT '错误详情',
  `suggestion` varchar(500) COMMENT '处理建议',
  `check_status` tinyint NOT NULL DEFAULT 1 COMMENT '处理状态（1-待处理 2-已处理 3-已忽略）',
  `check_by` varchar(64) COMMENT '校验人',
  `check_time` datetime COMMENT '校验时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临床规则校验记录';

-- biz_compliance_audit  医保合规审核单
CREATE TABLE `biz_compliance_audit` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `audit_no` varchar(32) NOT NULL COMMENT '审核单号',
  `settlement_id` bigint NOT NULL COMMENT '结算清单ID',
  `regist_id` bigint COMMENT '就诊锚点',
  `audit_type` tinyint NOT NULL DEFAULT 1 COMMENT '审核类型（1-结算前自查 2-批量筛查 3-医保反馈复核）',
  `risk_level` tinyint NOT NULL DEFAULT 0 COMMENT '风险等级（0-未发现 1-提示 2-关注 3-高危）',
  `risk_score` int NOT NULL DEFAULT 0 COMMENT '风险分',
  `hit_count` int NOT NULL DEFAULT 0 COMMENT '命中规则数',
  `pass_count` int NOT NULL DEFAULT 0 COMMENT '通过规则数',
  `na_count` int NOT NULL DEFAULT 0 COMMENT '不适用规则数',
  `drg_code` varchar(32) COMMENT 'DRG分组编码',
  `drg_weight` decimal(10,4) COMMENT 'DRG权重',
  `pay_standard` decimal(10,2) COMMENT '病组支付标准（元）',
  `actual_cost` decimal(10,2) COMMENT '实际总费用（元）',
  `cost_ratio` decimal(10,4) COMMENT '费用倍率=实际/支付标准',
  `conclusion` varchar(500) COMMENT '审核结论',
  `audit_by` varchar(64) COMMENT '审核人',
  `audit_time` datetime COMMENT '审核时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ca_audit_no` (`audit_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保合规审核单';

-- biz_compliance_audit_item  医保合规审核明细
CREATE TABLE `biz_compliance_audit_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `audit_id` bigint NOT NULL COMMENT '审核ID',
  `rule_code` varchar(16) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_group` char(1) NOT NULL COMMENT '规则分组',
  `result` tinyint NOT NULL COMMENT '结果（1-命中 2-通过 3-不适用）',
  `risk_level` tinyint NOT NULL DEFAULT 0 COMMENT '风险等级（1-提示 2-关注 3-高危）',
  `target_type` tinyint NOT NULL DEFAULT 0 COMMENT '对象（0-清单级 1-诊断 2-手术操作）',
  `target_id` bigint COMMENT '对象ID',
  `target_code` varchar(32) COMMENT '对象编码',
  `target_name` varchar(200) COMMENT '对象名称',
  `evidence` varchar(1000) COMMENT '判定依据',
  `suggestion` varchar(500) COMMENT '整改建议',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保合规审核明细';

-- biz_consultation  会诊申请记录
CREATE TABLE `biz_consultation` (
  `consultation_id` bigint NOT NULL COMMENT '会诊ID',
  `consultation_no` varchar(32) NOT NULL COMMENT '会诊编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint COMMENT '就诊次ID',
  `admission_id` bigint COMMENT '入院ID',
  `from_dept_id` bigint NOT NULL COMMENT '申请科室ID',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(64) COMMENT '申请医生姓名',
  `to_dept_id` bigint NOT NULL COMMENT '会诊科室ID',
  `consult_type` tinyint NOT NULL DEFAULT 2 COMMENT '会诊范围（1-科内 2-科间 3-全院）',
  `consult_category` tinyint NOT NULL DEFAULT 1 COMMENT '会诊类别（1-普通科间 2-营养 3-药学 4-其他）',
  `is_urgent` tinyint NOT NULL DEFAULT 0 COMMENT '是否急会诊（0-普通 1-急会诊）',
  `reason` varchar(500) COMMENT '会诊理由',
  `doctor_id` bigint NOT NULL COMMENT '会诊医生ID',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `consult_time` datetime COMMENT '会诊时间',
  `consult_status` tinyint NOT NULL COMMENT '会诊状态（0-待应答 1-已完成 2-已取消 3-已应答）',
  `accept_time` datetime COMMENT '会诊方接诊时间',
  `accept_doctor_id` bigint COMMENT '接诊医生ID（员工ID）',
  `accept_doctor_name` varchar(64) COMMENT '接诊医生姓名',
  `finish_time` datetime COMMENT '会诊完成时间',
  `record_id` bigint COMMENT '回写的住院病历ID',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `conclusion` varchar(1000) COMMENT '会诊结论',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`consultation_id`),
  UNIQUE KEY `uk_consultation_no` (`consultation_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会诊申请记录';

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

-- biz_critical_notice  病危重通知回执
CREATE TABLE `biz_critical_notice` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `notice_no` varchar(32) NOT NULL COMMENT '通知单号',
  `admission_id` bigint NOT NULL COMMENT '住院记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `patient_no` varchar(32) COMMENT '患者编号',
  `gender` tinyint COMMENT '性别（1-男 2-女 3-未知）',
  `age` int COMMENT '年龄',
  `dept_id` bigint COMMENT '开单科室ID',
  `dept_name` varchar(100) COMMENT '开单科室名称',
  `ward_name` varchar(64) COMMENT '病区名称',
  `bed_no` varchar(16) COMMENT '床位号',
  `admission_no` varchar(32) COMMENT '住院号',
  `notice_type` tinyint NOT NULL COMMENT '通知类别（1-病危 2-病重）',
  `consciousness_status` tinyint NOT NULL DEFAULT 1 COMMENT '患者神志（1-清醒 2-嗜睡 3-意识模糊 4-昏迷 9-其他）',
  `clinical_diagnosis` varchar(500) NOT NULL COMMENT '目前诊断',
  `condition_desc` varchar(1000) NOT NULL COMMENT '病情及危险因素',
  `warning_matters` varchar(1000) NOT NULL COMMENT '可能的病情变化与预警事项',
  `doctor_measures` varchar(1000) COMMENT '医方已采取/拟采取的诊治措施与配合要求',
  `notify_time` datetime NOT NULL COMMENT '告知时间',
  `doctor_id` bigint COMMENT '告知医师ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '告知医师姓名',
  `witness_doctor_id` bigint COMMENT '见证医师ID',
  `witness_doctor_name` varchar(50) COMMENT '见证医师姓名（可空）',
  `signer_name` varchar(50) COMMENT '签收人姓名',
  `signer_relation` tinyint COMMENT '签收人与患者关系',
  `signer_id_card` varchar(20) COMMENT '签收人证件号',
  `signer_phone` varchar(20) COMMENT '签收人联系电话',
  `signer_signature` mediumtext COMMENT '签收人手写签名',
  `acknowledge_time` datetime COMMENT '签收时间',
  `notice_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-草稿 2-已签发 3-已签收 4-已作废）',
  `issue_time` datetime COMMENT '签发',
  `printer_name` varchar(64) COMMENT '最后打印人',
  `print_count` int NOT NULL DEFAULT 0 COMMENT '回执打印次数',
  `last_print_time` datetime COMMENT '最后打印时间',
  `void_reason` varchar(500) COMMENT '作废原因',
  `void_by` varchar(64) COMMENT '作废经办人',
  `void_time` datetime COMMENT '作废时间',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '电子签名状态（0-未签名 1-已签名 2-签名已作废）',
  `sign_id` bigint COMMENT '当前有效签名ID',
  `signed_time` datetime COMMENT '签名时刻',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_no` (`notice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病危重通知回执';

-- biz_critical_value  检验危急值
CREATE TABLE `biz_critical_value` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `critical_no` varchar(32) NOT NULL COMMENT '危急值号',
  `record_id` bigint COMMENT '检验记录ID',
  `record_no` varchar(32) COMMENT '检验记录号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `item_code` varchar(32) COMMENT '检验项目编码',
  `item_name` varchar(200) NOT NULL COMMENT '检验项目名称',
  `result_value` varchar(200) NOT NULL COMMENT '危急值结果值',
  `result_unit` varchar(50) COMMENT '结果单位',
  `reference_range` varchar(100) COMMENT '参考范围',
  `critical_type` tinyint NOT NULL COMMENT '危急值类型（1-偏低 2-偏高）',
  `threshold_text` varchar(100) COMMENT '阈值说明',
  `critical_desc` varchar(300) NOT NULL COMMENT '危急值描述',
  `report_dept_id` bigint COMMENT '报告科室ID',
  `report_dept_name` varchar(100) COMMENT '报告科室',
  `report_by` varchar(64) COMMENT '报告人',
  `report_time` datetime NOT NULL COMMENT '报告时间',
  `deadline_time` datetime COMMENT '处置时限',
  `notify_status` tinyint NOT NULL DEFAULT 0 COMMENT '通知状态（0-未通知 1-已通知）',
  `notify_time` datetime COMMENT '通知时间',
  `escalate_status` tinyint NOT NULL DEFAULT 0 COMMENT '超时升级状态（0-未升级 1-已升级）',
  `escalate_time` datetime COMMENT '升级时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '闭环状态（1-待接收 2-已接收 3-已处置 4-已作废）',
  `receive_by` varchar(64) COMMENT '接收人',
  `receive_time` datetime COMMENT '接收时间',
  `handle_by` varchar(64) COMMENT '处置人',
  `handle_time` datetime COMMENT '处置时间',
  `handle_measure` varchar(500) COMMENT '处置措施',
  `source` varchar(16) NOT NULL DEFAULT 'RULE' COMMENT '来源',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_critical_no` (`critical_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验危急值';

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

-- biz_day_surgery_apply  日间手术登记单
CREATE TABLE `biz_day_surgery_apply` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `apply_no` varchar(32) NOT NULL COMMENT '登记单号',
  `item_id` bigint NOT NULL COMMENT '准入术式ID',
  `item_code` varchar(32) COMMENT '术式编码',
  `item_name` varchar(128) COMMENT '术式名称',
  `max_stay_hours` int COMMENT '最长滞留小时数',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '手术科室ID',
  `dept_name` varchar(128) COMMENT '手术科室名称',
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

-- biz_day_surgery_item  日间手术准入目录
CREATE TABLE `biz_day_surgery_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `item_code` varchar(32) NOT NULL COMMENT '术式编码',
  `item_name` varchar(128) NOT NULL COMMENT '术式名称',
  `dept_id` bigint COMMENT '适用科室ID',
  `dept_name` varchar(128) COMMENT '适用科室名称',
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

-- biz_death_certificate  死亡医学证明书
CREATE TABLE `biz_death_certificate` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `cert_no` varchar(32) NOT NULL COMMENT '证明编号',
  `admission_id` bigint NOT NULL COMMENT '住院记录ID',
  `discharge_id` bigint COMMENT '死亡出院记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '死者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 3-未知）',
  `nation` varchar(20) COMMENT '民族',
  `birth_date` date COMMENT '出生日期',
  `age` int COMMENT '死亡年龄',
  `id_card` varchar(18) COMMENT '身份证号',
  `occupation` varchar(50) COMMENT '职业',
  `marital_status` tinyint COMMENT '婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）',
  `death_time` datetime NOT NULL COMMENT '死亡时间',
  `death_place` tinyint NOT NULL COMMENT '死亡地点（1-医院 2-来院途中 3-家中 4-民政管理机构 5-其他机构 9-未指明）',
  `death_dept_id` bigint COMMENT '死亡科室ID',
  `death_dept_name` varchar(100) COMMENT '死亡科室名称',
  `death_ward_name` varchar(64) COMMENT '死亡病区名称',
  `death_bed_no` varchar(16) COMMENT '死亡床位号',
  `clinical_diagnosis` varchar(500) NOT NULL COMMENT '死亡诊断',
  `underlying_icd_code` varchar(32) COMMENT '根本死因ICD-10编码',
  `underlying_icd_name` varchar(200) COMMENT '根本死因名称',
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
  `patient_name` varchar(50) NOT NULL COMMENT '死者姓名',
  `death_time` datetime NOT NULL COMMENT '死亡时间',
  `death_dept_id` bigint COMMENT '死亡科室ID',
  `death_dept_name` varchar(100) COMMENT '死亡科室名称',
  `death_bed_no` varchar(16) COMMENT '死亡床位号',
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

-- biz_dept_cost_month  科室月度成本
CREATE TABLE `biz_dept_cost_month` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `cost_month` char(7) NOT NULL COMMENT '核算月份',
  `labor_cost` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '人力成本（元）',
  `drug_cost` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '药品成本（元）',
  `material_cost` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '耗材成本（元）',
  `depreciation` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '设备折旧（元）',
  `other_cost` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '其他成本（元）',
  `total_cost` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '成本合计',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_month` (`dept_id`, `cost_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室月度成本';

-- biz_diag_template  常用诊断模板
CREATE TABLE `biz_diag_template` (
  `id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `icd_code` varchar(20) NOT NULL COMMENT 'ICD-10编码',
  `icd_name` varchar(200) NOT NULL COMMENT '诊断名称',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常用诊断模板';

-- biz_dialysis_machine  透析机位台账
CREATE TABLE `biz_dialysis_machine` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `machine_no` varchar(32) NOT NULL COMMENT '机位号',
  `room_name` varchar(64) COMMENT '透析分区',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-可用 2-维修 3-停用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_machine_no` (`machine_no`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='透析机位台账';

-- biz_dialysis_patient  透析患者档案
CREATE TABLE `biz_dialysis_patient` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `dialysis_no` varchar(32) NOT NULL COMMENT '透析号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `phone` varchar(32) COMMENT '联系电话',
  `first_dialysis_date` date NOT NULL COMMENT '首次透析日期',
  `cause` varchar(255) COMMENT '原发病/进入透析原因',
  `access_type` tinyint NOT NULL COMMENT '血管通路（1-自体内瘘 2-人工血管 3-中心静脉导管 4-动静脉外露）',
  `access_site` varchar(128) COMMENT '通路部位',
  `dialysis_freq` tinyint NOT NULL DEFAULT 3 COMMENT '透析频次（1-每周1次 2-每周2次 3-每周3次 4-每周≥4次）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '档案状态（1-在透 2-暂停 3-退出）',
  `exit_reason` varchar(255) COMMENT '暂停/退出原因（转腹透/移植/死亡/失访等，截到 200）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dp_no` (`dialysis_no`),
  UNIQUE KEY `uk_dp_patient` (`patient_id`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='透析患者档案';

-- biz_dialysis_prescription  透析处方
CREATE TABLE `biz_dialysis_prescription` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `archive_id` bigint NOT NULL COMMENT '透析档案ID',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `dry_weight` decimal(6,2) NOT NULL COMMENT '干体重 kg',
  `duration_min` int NOT NULL DEFAULT 240 COMMENT '单次透析时长（分钟）',
  `blood_flow` int NOT NULL DEFAULT 220 COMMENT '血流量 mL/min',
  `dialyzer` tinyint NOT NULL DEFAULT 3 COMMENT '透析器（1-低通量纤维素膜 2-低通量合成膜 3-高通量合成膜）',
  `anticoagulant` tinyint NOT NULL DEFAULT 1 COMMENT '抗凝方式（1-普通肝素 2-低分子肝素 3-枸橼酸钠 4-无肝素）',
  `anticoag_dose` varchar(64) COMMENT '抗凝剂量描述',
  `target_ultra_ml` decimal(8,1) COMMENT '目标超滤量 ml',
  `start_date` date NOT NULL COMMENT '处方生效日期',
  `end_date` date COMMENT '处方停用日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-有效 2-已停用）',
  `doctor_id` bigint COMMENT '开立医生（员工ID）',
  `doctor_name` varchar(64) COMMENT '开立医生姓名',
  `stop_reason` varchar(255) COMMENT '停用原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='透析处方';

-- biz_dialysis_session  透析单
CREATE TABLE `biz_dialysis_session` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `session_no` varchar(32) NOT NULL COMMENT '透析单号',
  `dialysis_date` date NOT NULL COMMENT '透析日期',
  `time_slot` tinyint NOT NULL DEFAULT 1 COMMENT '时段（1-上午 2-下午 3-夜间）',
  `machine_id` bigint NOT NULL COMMENT '机位ID',
  `machine_no` varchar(32) COMMENT '机位号',
  `archive_id` bigint NOT NULL COMMENT '透析档案ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `prescription_id` bigint NOT NULL COMMENT '使用的透析处方ID',
  `dry_weight` decimal(6,2) COMMENT '干体重 kg',
  `duration_min` int COMMENT '处方透析时长分钟',
  `blood_flow` int COMMENT '处方血流量',
  `dialyzer` tinyint COMMENT '透析器',
  `anticoagulant` tinyint COMMENT '抗凝方式',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已排班 2-透析中 3-已完成 4-已取消）',
  `before_weight` decimal(6,2) COMMENT '透前体重 kg',
  `access_check` varchar(255) COMMENT '通路评估',
  `on_time` datetime COMMENT '上机时间',
  `on_by` varchar(64) COMMENT '上机人',
  `after_weight` decimal(6,2) COMMENT '透后体重 kg',
  `actual_duration_min` int COMMENT '实际透析时长分钟',
  `ultra_ml` decimal(8,1) COMMENT '实际超滤量 ml =（透前-透后）',
  `off_time` datetime COMMENT '下机时间',
  `off_by` varchar(64) COMMENT '下机人',
  `adverse_type` tinyint COMMENT '不良反应类型（，空=无）',
  `adverse_desc` varchar(255) COMMENT '不良反应处置描述',
  `cancel_reason` varchar(255) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  `slot_key` varchar(64) COMMENT '机位时段占用键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_no` (`session_no`),
  UNIQUE KEY `uk_session_slot` (`slot_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='透析单';

-- biz_diet_plan  膳食方案
CREATE TABLE `biz_diet_plan` (
  `id` bigint NOT NULL COMMENT '主键',
  `diet_no` varchar(32) NOT NULL COMMENT '膳食方案编号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(100) COMMENT '病区名称',
  `bed_no` varchar(20) COMMENT '床号',
  `order_id` bigint COMMENT '来源医嘱ID',
  `order_no` varchar(32) COMMENT '来源医嘱号',
  `source` tinyint NOT NULL DEFAULT 1 COMMENT '来源（1-医嘱校对派生 2-营养师手工登记）',
  `diet_code` varchar(32) NOT NULL COMMENT '饮食类型码',
  `diet_category` tinyint NOT NULL COMMENT '饮食类别（1-基本饮食 2-治疗饮食 3-诊断试验饮食 4-营养支持）',
  `diet_name` varchar(100) NOT NULL COMMENT '饮食名称',
  `route` tinyint NOT NULL DEFAULT 1 COMMENT '给食途径（1-口服 2-管饲）',
  `feed_way` varchar(100) COMMENT '管饲/输注方式说明',
  `calorie_target` int COMMENT '每日热量目标 kcal',
  `protein_target` int COMMENT '每日蛋白目标 g',
  `fluid_target` int COMMENT '每日液体量 ml',
  `meal_types` varchar(32) COMMENT '供应餐次',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `stop_time` datetime COMMENT '停止时间',
  `plan_status` tinyint NOT NULL DEFAULT 1 COMMENT '方案状态（1-执行中 2-已停止 3-已作废）',
  `confirm_status` tinyint NOT NULL DEFAULT 0 COMMENT '营养科接收状态（0-待接收 1-已接收 2-已退回）',
  `confirm_time` datetime COMMENT '接收/退回时间',
  `confirmer_id` bigint COMMENT '接收人',
  `confirmer_name` varchar(50) COMMENT '接收人姓名',
  `reject_reason` varchar(500) COMMENT '退回原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_diet_plan_no` (`diet_no`),
  UNIQUE KEY `uk_diet_plan_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='膳食方案';

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

-- biz_discharge_drug  出院带药单
CREATE TABLE `biz_discharge_drug` (
  `id` bigint NOT NULL COMMENT '带药单ID',
  `order_no` varchar(32) NOT NULL COMMENT '带药单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `drug_id` bigint COMMENT '药品ID',
  `drug_name` varchar(128) NOT NULL COMMENT '药品名称',
  `spec` varchar(64) COMMENT '规格',
  `dosage` varchar(64) COMMENT '每次剂量/用法用量描述',
  `unit` varchar(32) COMMENT '单位',
  `quantity` decimal(10,2) NOT NULL COMMENT '带药数量',
  `usage_text` varchar(200) COMMENT '用药医嘱',
  `days` int COMMENT '用药天数',
  `remark` varchar(500) COMMENT '备注',
  `dispense_status` tinyint NOT NULL DEFAULT 1 COMMENT '发药状态（1-待发药 2-已发药）',
  `dispense_by` bigint COMMENT '发药人（员工ID）',
  `dispense_name` varchar(50) COMMENT '发药人姓名',
  `dispense_time` datetime COMMENT '发药时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_discharge_drug_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出院带药单';

-- biz_dispute_case  医疗纠纷投诉主单
CREATE TABLE `biz_dispute_case` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `case_no` varchar(32) NOT NULL COMMENT '单据编号',
  `case_type` tinyint NOT NULL COMMENT '类型（1-服务投诉 2-医疗纠纷 3-医疗损害争议 4-其他）',
  `source_type` tinyint NOT NULL COMMENT '来源（1-来电 2-来访 3-来信 4-政务热线 5-上级交办 6-院内发现 7-其他）',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '等级（1-一般 2-较大 3-重大）',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `admission_id` bigint COMMENT '关联住院ID',
  `dept_id` bigint COMMENT '被投诉科室ID',
  `dept_name` varchar(128) COMMENT '被投诉科室名称',
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

-- biz_drg_sim_result  DRG 分组模拟结果
CREATE TABLE `biz_drg_sim_result` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `summary_id` bigint NOT NULL COMMENT '病案首页ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `main_diag_code` varchar(32) COMMENT '分组时使用的主诊断编码',
  `main_diag_name` varchar(200) COMMENT '主诊断名称',
  `is_surgery` tinyint NOT NULL DEFAULT 0 COMMENT '是否手术',
  `inpatient_days` int COMMENT '住院天数',
  `drg_code` varchar(32) NOT NULL COMMENT '入组编码',
  `drg_name` varchar(200) COMMENT '组名称',
  `mdc_code` varchar(8) COMMENT 'MDC 大类',
  `weight` decimal(10,4) COMMENT '权重 RW',
  `pay_standard` decimal(10,2) COMMENT '病组支付标准（元）',
  `actual_amount` decimal(10,2) COMMENT '实际住院费用',
  `profit_amount` decimal(10,2) COMMENT '盈亏 = 支付标准 - 实际费用',
  `sim_status` tinyint NOT NULL DEFAULT 1 COMMENT '结果（1-已入组 2-未入组）',
  `rule_note` varchar(300) COMMENT '命中规则说明',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_summary` (`summary_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DRG 分组模拟结果';

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
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品库存流水';

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
  `drug_code` varchar(32) COMMENT '药品编码',
  `drug_name` varchar(200) COMMENT '药品名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `batch_no` varchar(50) COMMENT '批号',
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
  `drug_code` varchar(32) COMMENT '药品编码',
  `drug_name` varchar(100) COMMENT '药品名称',
  `generic_name` varchar(100) COMMENT '通用名',
  `specification` varchar(100) COMMENT '规格',
  `dosage_form` varchar(50) COMMENT '剂型',
  `unit` varchar(20) COMMENT '单位',
  `manufacturer` varchar(200) COMMENT '生产厂家',
  `approval_number` varchar(100) COMMENT '批准文号',
  `stock_id` bigint COMMENT '采集挂靠批次ID',
  `stock_batch_no` varchar(50) COMMENT '库存批号',
  `supplier` varchar(200) COMMENT '供应商',
  `supplier_id` bigint COMMENT '供应商ID',
  `source_type` tinyint NOT NULL DEFAULT 1 COMMENT '采集来源（1-入库采集 2-存量补采）',
  `inbound_id` bigint COMMENT '来源入库单ID',
  `inbound_no` varchar(64) COMMENT '来源入库单号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '码状态（1-在库 2-已发药核销 3-已作废）',
  `scan_time` datetime COMMENT '采集扫码时间',
  `operator_name` varchar(50) COMMENT '采集人',
  `dispensing_id` bigint COMMENT '发药单ID',
  `dispensing_no` varchar(64) COMMENT '发药单号',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `visit_type` tinyint COMMENT '就诊类型（1-门诊 2-住院）',
  `regist_id` bigint COMMENT '门诊挂号ID',
  `admission_id` bigint COMMENT '住院ID',
  `dept_id` bigint COMMENT '发药科室ID',
  `dept_name` varchar(100) COMMENT '发药科室名称',
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
  UNIQUE KEY `uk_drug_trace_code` (`trace_code`),
  UNIQUE KEY `uk_drug_trace_no` (`trace_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品追溯码台账';

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
  `drug_code` varchar(32) COMMENT '药品编码',
  `drug_name` varchar(200) COMMENT '药品名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `batch_no` varchar(50) COMMENT '批号',
  `production_date` date COMMENT '生产日期',
  `expiry_date` date COMMENT '有效期',
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

-- biz_duty_log  总值班值班日志
CREATE TABLE `biz_duty_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `duty_date` date NOT NULL COMMENT '值班日期',
  `shift_type` tinyint NOT NULL COMMENT '班次 1-白班 2-夜班（1-白班 2-夜班）',
  `roster_id` bigint COMMENT '所属排班行 biz_duty_roster.id',
  `employee_id` bigint NOT NULL COMMENT '值班人',
  `employee_name` varchar(64) COMMENT '值班人姓名',
  `log_type` tinyint NOT NULL DEFAULT 1 COMMENT '记录类型 1-值班事件 2-遗留事项 3-巡查记录（1-值班事件 2-遗留事项 3-巡查记录）',
  `happen_time` datetime COMMENT '事件发生时间',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` varchar(2000) COMMENT '事件经过',
  `handle_result` varchar(1000) COMMENT '处理情况',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0-待处理 1-已处理 2-已交班（0-待处理 1-已处理 2-已交班）',
  `handover_emp_id` bigint COMMENT '接班人',
  `handover_emp_name` varchar(64) COMMENT '接班人姓名',
  `handover_time` datetime COMMENT '交班时间',
  `ack_time` datetime COMMENT '接班人签收时间',
  `create_by` varchar(64) COMMENT '记录人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='总值班值班日志';

-- biz_duty_roster  全院总值班排班
CREATE TABLE `biz_duty_roster` (
  `id` bigint NOT NULL COMMENT '主键',
  `duty_date` date NOT NULL COMMENT '值班日期',
  `shift_type` tinyint NOT NULL DEFAULT 1 COMMENT '班次（1-白班 2-夜班 00-次日08）',
  `role_type` tinyint NOT NULL DEFAULT 1 COMMENT '班内角色（1-主班 2-副班）',
  `employee_id` bigint NOT NULL COMMENT '值班人',
  `employee_name` varchar(50) COMMENT '值班人姓名',
  `dept_id` bigint COMMENT '值班人原属科室ID',
  `dept_name` varchar(100) COMMENT '值班人原属科室名称',
  `phone` varchar(32) COMMENT '值班联系电话',
  `start_time` varchar(5) COMMENT '班次开始时间（HH:mm）',
  `end_time` varchar(5) COMMENT '班次结束时间（HH:mm）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-有效 0-停用）',
  `substitute_emp_id` bigint COMMENT '临时换班后的实际值班人',
  `substitute_emp_name` varchar(50) COMMENT '换班后实际值班人姓名',
  `substitute_time` datetime COMMENT '换班时间',
  `substitute_reason` varchar(200) COMMENT '换班原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_duty_date_shift_role` (`duty_date`, `shift_type`, `role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全院总值班排班';

-- biz_ecg_holter  Holter 动态心电
CREATE TABLE `biz_ecg_holter` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `record_id` bigint NOT NULL COMMENT '检查记录ID',
  `waveform_id` bigint COMMENT '波形ID',
  `wear_start_time` datetime COMMENT '开始佩戴时间',
  `wear_end_time` datetime COMMENT '结束佩戴时间',
  `total_beats` int COMMENT '总心搏数',
  `avg_hr` int COMMENT '平均心率（次/分）',
  `max_hr` int COMMENT '最快心率（次/分）',
  `max_hr_time` varchar(16) COMMENT '最快心率时刻（HH:mm）',
  `min_hr` int COMMENT '最慢心率（次/分）',
  `min_hr_time` varchar(16) COMMENT '最慢心率时刻（HH:mm）',
  `afib_flag` tinyint COMMENT '是否检出房颤（0-否 1-是）',
  `afib_beats` int COMMENT '房颤心搏数',
  `svc_count` int COMMENT '室上性早搏总数',
  `pvc_count` int COMMENT '室性早搏总数',
  `vt_count` int COMMENT '室性心动过速阵数',
  `pause_count` int COMMENT '停搏',
  `longest_pause_ms` int COMMENT '最长停搏时长',
  `st_episode_count` int COMMENT 'ST段异常发作阵数',
  `hourly_hr_json` text COMMENT '24小时逐时平均心率',
  `analysis_by` varchar(64) COMMENT '分析人',
  `analysis_time` datetime COMMENT '分析时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Holter 动态心电';

-- biz_ecg_measure  心电测量参数
CREATE TABLE `biz_ecg_measure` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `record_id` bigint NOT NULL COMMENT '检查记录ID',
  `waveform_id` bigint COMMENT '波形ID',
  `hr` int COMMENT '心率（次/分）',
  `pr_ms` int COMMENT 'PR间期',
  `qrs_ms` int COMMENT 'QRS时限',
  `qt_ms` int COMMENT 'QT间期（ms）',
  `qtc_ms` int COMMENT 'QTc校正间期',
  `p_axis` int COMMENT 'P电轴（°）',
  `qrs_axis` int COMMENT 'QRS电轴',
  `t_axis` int COMMENT 'T电轴（°）',
  `rhythm_text` varchar(100) COMMENT '节律描述',
  `measure_by` varchar(64) COMMENT '测量人',
  `measure_time` datetime COMMENT '测量时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心电测量参数';

-- biz_ecg_template  心电报告模板
CREATE TABLE `biz_ecg_template` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_code` varchar(32) NOT NULL COMMENT '模板编码',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `ecg_type` tinyint COMMENT '适用心电类型',
  `finding_tpl` text COMMENT '心电图所见模板',
  `conclusion_tpl` text COMMENT '心电图诊断模板',
  `suggestion_tpl` text COMMENT '建议模板',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ecg_tpl_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心电报告模板';

-- biz_ecg_waveform  心电波形采集
CREATE TABLE `biz_ecg_waveform` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `wave_no` varchar(32) NOT NULL COMMENT '波形号',
  `record_id` bigint NOT NULL COMMENT '检查记录ID',
  `record_no` varchar(32) COMMENT '检查记录号',
  `apply_id` bigint COMMENT '检查申请单ID（冗余）',
  `apply_no` varchar(32) COMMENT '申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(64) COMMENT '患者姓名',
  `ecg_type` tinyint NOT NULL DEFAULT 1 COMMENT '心电类型（1-常规静息心电图 2-24小时动态心电图）',
  `wave_data` longtext COMMENT '波形数据',
  `device_no` varchar(64) COMMENT '采集设备号',
  `collect_by` varchar(64) COMMENT '采集人',
  `collect_time` datetime COMMENT '采集时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wave_no` (`wave_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心电波形采集';

-- biz_emergency  急诊记录
CREATE TABLE `biz_emergency` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `emergency_no` varchar(32) NOT NULL COMMENT '急诊号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int DEFAULT 0 COMMENT '年龄',
  `phone` varchar(20) COMMENT '联系电话',
  `chief_complaint` text COMMENT '主诉',
  `triage_level` tinyint NOT NULL DEFAULT 3 COMMENT '分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）',
  `zone` varchar(20) DEFAULT '绿区' COMMENT '区域（红区/黄区/绿区）',
  `green_channel` varchar(50) COMMENT '绿色通道（胸痛中心/卒中中心/创伤中心/无）',
  `dept_id` bigint COMMENT '接诊科室ID',
  `dept_name` varchar(100) COMMENT '接诊科室',
  `doctor_id` bigint COMMENT '接诊医生ID',
  `doctor_name` varchar(50) COMMENT '接诊医生',
  `assign_type` tinyint NOT NULL DEFAULT 0 COMMENT '派单方式',
  `unassigned_reason` varchar(200) COMMENT '未派单原因',
  `target_see_minutes` int COMMENT '该分诊级别的应接诊时限',
  `vital_signs` text COMMENT '生命体征',
  `diagnosis` text COMMENT '初步诊断',
  `treatment` text COMMENT '处理措施',
  `emergency_status` tinyint DEFAULT 1 COMMENT '急诊状态（1-候诊 2-诊治中 3-留观 4-转住院 5-离院 6-死亡）',
  `observation_bed` varchar(20) COMMENT '留观床位号',
  `observation_ward_id` bigint COMMENT '留观病区ID',
  `observation_bed_id` bigint COMMENT '留观床位ID',
  `observation_start_time` datetime COMMENT '开始留观时间',
  `observation_end_time` datetime COMMENT '结束留观时间（转住院/离院/死亡时写入）',
  `admission_id` bigint COMMENT '转住院产生的入院记录ID',
  `admission_time` datetime COMMENT '入急诊时间',
  `diagnosis_time` datetime COMMENT '开始诊治时间',
  `finish_time` datetime COMMENT '结束时间',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emergency_no` (`emergency_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='急诊记录';

-- biz_emergency_handover  急诊交班单
CREATE TABLE `biz_emergency_handover` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `handover_no` varchar(32) NOT NULL COMMENT '交班单号',
  `dept_id` bigint NOT NULL COMMENT '交班科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '交班科室名称',
  `from_emp_id` bigint NOT NULL COMMENT '交出人员工ID',
  `from_emp_name` varchar(50) NOT NULL COMMENT '交出人姓名',
  `take_emp_id` bigint NOT NULL COMMENT '接班人员工ID',
  `take_emp_name` varchar(50) NOT NULL COMMENT '接班人姓名',
  `shift_name` varchar(32) COMMENT '班次名',
  `period_begin` datetime NOT NULL COMMENT '本班区间起',
  `period_end` datetime NOT NULL COMMENT '本班区间止',
  `pending_count` int NOT NULL DEFAULT 0 COMMENT '本次移交未闭环人数（定格）',
  `pool_count` int NOT NULL DEFAULT 0 COMMENT '其中交班前无人指派的条数',
  `overdue_count` int NOT NULL DEFAULT 0 COMMENT '其中候诊已超时的条数（定格）',
  `observation_count` int NOT NULL DEFAULT 0 COMMENT '其中留观中的条数（定格）',
  `obs_over_limit_count` int NOT NULL DEFAULT 0 COMMENT '其中留观已超时限的条数（定格）',
  `remark` varchar(500) COMMENT '整单交代备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_handover_no` (`handover_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='急诊交班单';

-- biz_emergency_handover_item  急诊交班明细
CREATE TABLE `biz_emergency_handover_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `handover_id` bigint NOT NULL COMMENT '交班单ID',
  `emergency_id` bigint NOT NULL COMMENT '急诊记录ID',
  `emergency_no` varchar(32) NOT NULL COMMENT '急诊号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `triage_level` tinyint COMMENT '分诊级别',
  `emergency_status` tinyint NOT NULL COMMENT '交班时该患者的急诊状态（1-候诊 2-诊治中 3-留观）',
  `from_doctor_id` bigint COMMENT '交班时的负责医生ID',
  `from_doctor_name` varchar(50) COMMENT '交班时的负责医生姓名',
  `take_doctor_id` bigint NOT NULL COMMENT '接续责任人',
  `take_doctor_name` varchar(50) NOT NULL COMMENT '接续责任人姓名',
  `disposition` varchar(100) NOT NULL COMMENT '去向/处置交代',
  `handover_note` varchar(300) COMMENT '逐条补充交代（过敏史/管路/家属联系方式等，截到 300）',
  `wait_minutes` bigint COMMENT '候诊已等多久',
  `obs_hours` int COMMENT '已留观小时数',
  `overdue_level` tinyint NOT NULL DEFAULT 0 COMMENT '超时档位定格（0-未超时 1-超时 2-严重超时）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0-未删除 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_handover_item` (`handover_id`, `emergency_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='急诊交班明细';

-- biz_emr_signature  电子签名证据
CREATE TABLE `biz_emr_signature` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `sign_no` varchar(32) NOT NULL COMMENT '签名流水号',
  `biz_type` tinyint NOT NULL COMMENT '签名对象类型',
  `biz_id` bigint NOT NULL COMMENT '签名对象ID',
  `biz_no` varchar(64) COMMENT '对象单号',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '对象所属科室ID',
  `dept_name` varchar(64) COMMENT '对象所属科室名称',
  `sign_scene` tinyint NOT NULL COMMENT '签名场景',
  `chain_no` int NOT NULL DEFAULT 1 COMMENT '同对象第几次签名',
  `prev_sign_id` bigint COMMENT '前一次签名ID',
  `prev_digest` varchar(128) COMMENT '前一次签名摘要',
  `signer_id` bigint NOT NULL COMMENT '签名人员工ID',
  `signer_name` varchar(64) NOT NULL COMMENT '签名人姓名',
  `signer_dept_id` bigint COMMENT '签名人科室ID',
  `signer_dept_name` varchar(64) COMMENT '签名人科室名称',
  `signer_title` varchar(64) COMMENT '签名人职称',
  `cert_id` bigint NOT NULL COMMENT '所用证书ID',
  `cert_no` varchar(32) NOT NULL COMMENT '所用证书编号',
  `digest_algo` varchar(16) NOT NULL COMMENT '摘要算法',
  `sign_algo` varchar(32) NOT NULL COMMENT '签名算法',
  `content_digest` varchar(128) NOT NULL COMMENT '被签内容摘要',
  `sign_value` text NOT NULL COMMENT '签名值',
  `content_snapshot` mediumtext COMMENT '被签内容快照',
  `signed_time` datetime NOT NULL COMMENT '签名时刻',
  `time_source` tinyint NOT NULL DEFAULT 1 COMMENT '时间来源（1-本机时钟 2-院内授时服务器 3-第三方TSA）',
  `tsa_serial` varchar(64) COMMENT '第三方时间戳序列号',
  `tsa_time` datetime COMMENT 'TSA 授时时刻',
  `tsa_token` text COMMENT '时间戳令牌',
  `sign_status` tinyint NOT NULL DEFAULT 1 COMMENT '签名状态（1-有效 2-已作废）',
  `verify_status` tinyint NOT NULL DEFAULT 0 COMMENT '最近一次验签结果（0-未校验 1-通过 2-失败）',
  `verify_time` datetime COMMENT '最近一次验签时间',
  `verify_count` int NOT NULL DEFAULT 0 COMMENT '累计验签次数',
  `invalid_reason` varchar(200) COMMENT '作废原因',
  `invalid_time` datetime COMMENT '作废时间',
  `invalid_by` bigint COMMENT '作废操作人员工ID',
  `invalid_by_name` varchar(64) COMMENT '作废操作人姓名',
  `client_ip` varchar(64) COMMENT '签名来源 IP（留痕）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sign_no` (`sign_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子签名证据';

-- biz_endoscopy_record  内镜检查记录
CREATE TABLE `biz_endoscopy_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '内镜检查号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `visit_date` date COMMENT '就诊日期',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(100) COMMENT '申请科室',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(50) COMMENT '申请医生',
  `clinical_diagnosis` varchar(500) COMMENT '临床诊断',
  `endo_type` tinyint DEFAULT 1 COMMENT '内镜类型（1-胃镜 2-肠镜 3-支气管镜 4-膀胱镜 5-宫腔镜 6-喉镜 7-ERCP 8-胶囊内镜）',
  `anesthesia_method` tinyint DEFAULT 1 COMMENT '麻醉方式（1-无麻醉 2-表面麻醉 3-静脉麻醉 4-全身麻醉）',
  `body_part` varchar(200) COMMENT '检查部位 / 到达范围',
  `exam_purpose` varchar(500) COMMENT '检查目的',
  `bowel_prep_score` tinyint COMMENT '肠道准备质量 Boston 评分',
  `hp_result` tinyint DEFAULT 0 COMMENT '幽门螺杆菌（0-未查 1-阴性 2-阳性）',
  `findings` text COMMENT '内镜所见',
  `diagnosis` text COMMENT '内镜诊断',
  `suggestion` varchar(1000) COMMENT '建议',
  `biopsy_flag` tinyint DEFAULT 0 COMMENT '是否活检（0-否 1-是）',
  `biopsy_part` varchar(200) COMMENT '活检部位',
  `biopsy_count` int DEFAULT 0 COMMENT '活检块数',
  `pathology_order_no` varchar(32) COMMENT '关联病理号',
  `endoscopist` varchar(64) COMMENT '内镜医师',
  `execute_time` datetime COMMENT '检查时间',
  `status` tinyint DEFAULT 1 COMMENT '状态（1-已登记 2-已签到 3-检查中 4-已出报告 5-已审核 6-已发布 7-已取消）',
  `report_by` varchar(64) COMMENT '报告医师',
  `report_time` datetime COMMENT '报告时间',
  `audit_by` varchar(64) COMMENT '审核医师',
  `audit_time` datetime COMMENT '审核时间',
  `publish_by` varchar(64) COMMENT '发布人',
  `publish_time` datetime COMMENT '发布时间',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内镜检查记录';

-- biz_equipment_maintain  设备维保记录
CREATE TABLE `biz_equipment_maintain` (
  `id` bigint NOT NULL COMMENT '维保记录ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_code` varchar(32) COMMENT '设备编码',
  `equipment_name` varchar(200) COMMENT '设备名称',
  `maintain_type` tinyint NOT NULL COMMENT '维保类型（1-保养 2-维修 3-巡检）',
  `maintain_date` date NOT NULL COMMENT '维保日期',
  `next_maintain_date` date COMMENT '下次维保日期',
  `cost` decimal(12,2) COMMENT '费用（元）',
  `fault_desc` varchar(500) COMMENT '故障描述',
  `handle_result` varchar(500) COMMENT '处理结果',
  `maintain_result` tinyint NOT NULL DEFAULT 1 COMMENT '维保结果（1-正常 2-异常）',
  `handler_name` varchar(50) COMMENT '维保人',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维保记录';

-- biz_equipment_metering  设备计量记录
CREATE TABLE `biz_equipment_metering` (
  `id` bigint NOT NULL COMMENT '计量记录ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_code` varchar(32) COMMENT '设备编码',
  `equipment_name` varchar(200) COMMENT '设备名称',
  `metering_type` tinyint NOT NULL COMMENT '计量类型（1-强检 2-校准）',
  `metering_date` date NOT NULL COMMENT '计量日期',
  `valid_until` date NOT NULL COMMENT '有效期至',
  `metering_result` tinyint NOT NULL DEFAULT 1 COMMENT '计量结果（1-合格 2-不合格）',
  `cert_no` varchar(64) COMMENT '证书编号',
  `agency` varchar(128) COMMENT '检定/校准机构',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备计量记录';

-- biz_exam_appointment  检查预约单
CREATE TABLE `biz_exam_appointment` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `appt_no` varchar(32) NOT NULL COMMENT '预约单号',
  `active_flag` tinyint DEFAULT 1 COMMENT '有效标记',
  `apply_id` bigint NOT NULL COMMENT '检查申请单ID',
  `apply_no` varchar(32) COMMENT '申请单号',
  `prev_apply_status` tinyint COMMENT '预约前申请状态',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(100) COMMENT '申请科室',
  `doctor_id` bigint COMMENT '申请医生ID',
  `doctor_name` varchar(50) COMMENT '申请医生',
  `item_id` bigint COMMENT '检查项目ID',
  `item_code` varchar(32) COMMENT '项目编码',
  `item_name` varchar(200) COMMENT '项目名称',
  `body_part` varchar(200) COMMENT '检查部位',
  `exam_minutes` int COMMENT '本次占用时长',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `device_code` varchar(32) COMMENT '设备编码',
  `device_name` varchar(100) COMMENT '设备名称',
  `exam_dept_id` bigint COMMENT '检查科室ID',
  `exam_dept_name` varchar(100) COMMENT '检查科室名称',
  `room_name` varchar(100) COMMENT '检查室',
  `exam_date` date NOT NULL COMMENT '检查日期',
  `start_time` char(5) NOT NULL COMMENT '开始时间（HH:mm）',
  `end_time` char(5) NOT NULL COMMENT '结束时间（HH:mm）',
  `is_emergency` tinyint DEFAULT 0 COMMENT '是否急诊（0-否 1-是）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已预约 2-已到检 3-已完成 4-已取消 5-爽约）',
  `book_by` varchar(64) COMMENT '预约操作人',
  `book_time` datetime COMMENT '预约操作时间',
  `arrive_time` datetime COMMENT '到检时间',
  `finish_time` datetime COMMENT '检查完成时间',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `noshow_time` datetime COMMENT '爽约判定时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_appt_active` (`apply_id`, `active_flag`),
  UNIQUE KEY `uk_exam_appt_no` (`appt_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查预约单';

-- biz_exam_device  检查设备档位
CREATE TABLE `biz_exam_device` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_code` varchar(32) NOT NULL COMMENT '预约设备编码',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `device_type` tinyint NOT NULL COMMENT '设备类别（1-CT 2-MR 3-DR 4-超声 5-心电 6-内镜 7-其他）',
  `equipment_id` bigint COMMENT '设备台账ID',
  `dept_id` bigint COMMENT '检查科室ID',
  `dept_name` varchar(100) COMMENT '检查科室名称',
  `room_name` varchar(100) COMMENT '检查室/机房位置',
  `am_start` char(5) NOT NULL DEFAULT '08:00' COMMENT '上午开放开始（HH:mm）',
  `am_end` char(5) NOT NULL DEFAULT '12:00' COMMENT '上午开放结束（HH:mm）',
  `pm_start` char(5) COMMENT '下午开放开始（HH:mm）',
  `pm_end` char(5) COMMENT '下午开放结束（HH:mm）',
  `slot_minutes` int NOT NULL DEFAULT 30 COMMENT '号源粒度',
  `parallel_count` int NOT NULL DEFAULT 1 COMMENT '单格子并行号数',
  `ahead_days` int NOT NULL DEFAULT 7 COMMENT '可提前预约天数',
  `max_slot_minutes` int NOT NULL DEFAULT 240 COMMENT '可占号最长时长（分钟）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-开放预约 2-暂停预约）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_device_code` (`device_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查设备档位';

-- biz_exam_device_item  设备可开展项目
CREATE TABLE `biz_exam_device_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `item_id` bigint NOT NULL COMMENT '检查项目ID',
  `item_code` varchar(32) COMMENT '项目编码',
  `item_name` varchar(200) COMMENT '项目名称',
  `exam_minutes` int COMMENT '该设备做该项目的时长（分钟）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_device_item` (`device_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备可开展项目';

-- biz_exam_film  检查胶片用量
CREATE TABLE `biz_exam_film` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `film_no` varchar(32) NOT NULL COMMENT '胶片单号',
  `record_id` bigint NOT NULL COMMENT '检查记录ID',
  `record_no` varchar(32) COMMENT '检查记录号',
  `apply_id` bigint COMMENT '检查申请单ID',
  `apply_no` varchar(32) COMMENT '申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(64) COMMENT '患者姓名',
  `visit_date` date COMMENT '就诊日期',
  `item_code` varchar(32) COMMENT '检查项目编码',
  `item_name` varchar(200) COMMENT '检查项目名称',
  `body_part` varchar(100) COMMENT '检查部位',
  `modality` tinyint COMMENT '影像模态（，快照）',
  `spec_id` bigint NOT NULL COMMENT '胶片规格ID',
  `spec_code` varchar(32) COMMENT '规格编码',
  `spec_name` varchar(100) NOT NULL COMMENT '规格名称',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `unit` varchar(20) COMMENT '计价单位',
  `quantity` int NOT NULL COMMENT '胶片张数',
  `amount` decimal(12,2) NOT NULL COMMENT '金额 = 单价 × 张数',
  `film_status` tinyint NOT NULL DEFAULT 1 COMMENT '胶片状态（1-已登记 2-已打印 3-已发放 4-已作废）',
  `charge_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否已记账（0-未记账 1-已记账）',
  `fee_id` bigint COMMENT '记账流水ID',
  `fee_no` varchar(32) COMMENT '记账流水号',
  `print_by` varchar(64) COMMENT '打印人',
  `print_time` datetime COMMENT '打印时间',
  `deliver_by` varchar(64) COMMENT '发放人',
  `deliver_time` datetime COMMENT '发放时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_film_no` (`film_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查胶片用量';

-- biz_exam_image  检查影像帧
CREATE TABLE `biz_exam_image` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `biz_type` tinyint NOT NULL DEFAULT 1 COMMENT '影像来源单据类型',
  `apply_id` bigint NOT NULL COMMENT '申请单ID',
  `apply_no` varchar(64) COMMENT '申请单号',
  `record_id` bigint COMMENT '执行记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(64) COMMENT '患者姓名',
  `item_name` varchar(200) COMMENT '检查/检验项目名称',
  `body_part` varchar(100) COMMENT '检查部位',
  `modality` tinyint COMMENT '影像模态（1-CT 2-MR 3-DR 4-超声 5-心电 6-内镜 7-其他）',
  `seq` int NOT NULL DEFAULT 1 COMMENT '本申请单内的帧序号',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_url` varchar(500) NOT NULL COMMENT '访问路径',
  `file_size` bigint COMMENT '文件字节数',
  `mime_type` varchar(64) COMMENT '文件MIME类型',
  `source` tinyint NOT NULL DEFAULT 1 COMMENT '来源（1-工作站上传 2-模拟）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '上传人',
  `create_time` datetime COMMENT '上传时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查影像帧';

-- biz_exam_slot  检查设备号源时段
CREATE TABLE `biz_exam_slot` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `slot_date` date NOT NULL COMMENT '号源日期',
  `seq` int COMMENT '段序',
  `start_time` char(5) NOT NULL COMMENT '段开始时间（HH:mm）',
  `end_time` char(5) NOT NULL COMMENT '段结束时间（HH:mm）',
  `total_source` int NOT NULL DEFAULT 1 COMMENT '段号源总数',
  `used_source` int NOT NULL DEFAULT 0 COMMENT '段已占号数',
  `available_source` int NOT NULL DEFAULT 1 COMMENT '段剩余号源',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '段状态（0-停用锁号 1-正常）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_slot` (`device_id`, `slot_date`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查设备号源时段';

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

-- biz_film_spec  胶片规格价目
CREATE TABLE `biz_film_spec` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `spec_code` varchar(32) NOT NULL COMMENT '规格编码',
  `spec_name` varchar(100) NOT NULL COMMENT '规格名称',
  `unit_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `unit` varchar(20) NOT NULL DEFAULT '张' COMMENT '计价单位',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spec_code` (`spec_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='胶片规格价目';

-- biz_followup_task  随访任务
CREATE TABLE `biz_followup_task` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_no` varchar(32) NOT NULL COMMENT '任务编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `phone` varchar(20) COMMENT '联系电话',
  `diagnosis` varchar(200) COMMENT '诊断',
  `dept_id` bigint COMMENT '随访所属科室ID',
  `dept_name` varchar(128) COMMENT '科室名称',
  `followup_type` tinyint NOT NULL COMMENT '随访类型（1-复诊提醒 2-慢病随访 3-用药指导 4-术后随访）',
  `followup_content` varchar(500) COMMENT '随访内容',
  `followup_time` datetime NOT NULL COMMENT '计划随访时间',
  `followup_status` tinyint NOT NULL DEFAULT 1 COMMENT '随访状态（1-待随访 2-随访中 3-已完成 4-已取消）',
  `executor_id` bigint COMMENT '执行人ID',
  `executor_name` varchar(50) COMMENT '执行人姓名',
  `execute_time` datetime COMMENT '执行时间',
  `execute_result` varchar(500) COMMENT '执行结果',
  `revisit_record_id` bigint COMMENT '复诊引用的原病历ID',
  `revisit_appoint_id` bigint COMMENT '由本任务生成的复诊挂号ID',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访任务';

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

-- biz_hand_hygiene_obs  手卫生依从性观察记录
CREATE TABLE `biz_hand_hygiene_obs` (
  `id` bigint NOT NULL COMMENT '主键',
  `obs_date` date NOT NULL COMMENT '观察日期',
  `dept_id` bigint NOT NULL COMMENT '被观察科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '被观察科室',
  `obs_object` tinyint NOT NULL COMMENT '观察对象',
  `opportunity_count` int NOT NULL COMMENT '手卫生时机数',
  `comply_count` int NOT NULL COMMENT '实际执行数',
  `observer_id` bigint NOT NULL COMMENT '观察人ID',
  `observer_name` varchar(50) NOT NULL COMMENT '观察人姓名',
  `obs_time` datetime NOT NULL COMMENT '观察登记时间',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  `remark` varchar(255),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手卫生依从性观察记录';

-- biz_icu_monitor  ICU 监护记录单
CREATE TABLE `biz_icu_monitor` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `stay_id` bigint NOT NULL COMMENT '入科记录ID',
  `record_time` datetime NOT NULL COMMENT '记录时刻',
  `temperature` decimal(4,1) COMMENT '体温 ℃',
  `pulse` int COMMENT '脉搏 次/分',
  `respiratory` int COMMENT '呼吸 次/分',
  `sbp` int COMMENT '收缩压 mmHg',
  `dbp` int COMMENT '舒张压 mmHg',
  `spo2` int COMMENT '血氧饱和度（%）',
  `gcs_eye` tinyint COMMENT 'GCS 睁眼 1~4',
  `gcs_verbal` tinyint COMMENT 'GCS 语言 1~5',
  `gcs_motor` tinyint COMMENT 'GCS 运动 1~6',
  `gcs_total` int COMMENT 'GCS 总分',
  `pupil` varchar(128) COMMENT '瞳孔',
  `cvp` decimal(5,1) COMMENT '中心静脉压 cmH2O',
  `vent_mode` tinyint COMMENT '呼吸支持（1-鼻导管 2-无创 3-有创 4-脱机）',
  `fio2` int COMMENT '吸氧浓度（%）',
  `peep` decimal(4,1) COMMENT '呼气末正压（cmH2O）',
  `intake_ml` decimal(8,1) COMMENT '入量 ml',
  `output_ml` decimal(8,1) COMMENT '出量 ml',
  `fluid_balance` decimal(8,1) COMMENT '液体平衡 ml = 入量-出量',
  `urine_ml` int COMMENT '尿量 ml',
  `has_airway` tinyint NOT NULL DEFAULT 0 COMMENT '人工气道/气管插管 0-无 1-有（0-无 1-有）',
  `has_cvc` tinyint NOT NULL DEFAULT 0 COMMENT '中心静脉导管 0-无 1-有（0-无 1-有）',
  `has_arterial` tinyint NOT NULL DEFAULT 0 COMMENT '动脉置管 0-无 1-有（0-无 1-有）',
  `has_catheter` tinyint NOT NULL DEFAULT 0 COMMENT '导尿管 0-无 1-有（0-无 1-有）',
  `has_drain` tinyint NOT NULL DEFAULT 0 COMMENT '引流管 0-无 1-有（0-无 1-有）',
  `condition_desc` varchar(500) COMMENT '病情观察',
  `handling` varchar(500) COMMENT '处置/干预',
  `recorder_id` bigint COMMENT '记录人（员工ID）',
  `recorder_name` varchar(64) COMMENT '记录人姓名',
  `record_date` date COMMENT '记录日期',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_icu_monitor_time` (`stay_id`, `record_time`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ICU 监护记录单';

-- biz_icu_stay  ICU 入出科登记
CREATE TABLE `biz_icu_stay` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `stay_no` varchar(32) NOT NULL COMMENT '入科单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `from_dept_id` bigint COMMENT '入科来源科室ID',
  `from_dept_name` varchar(128) COMMENT '入科来源科室名称',
  `ward_id` bigint NOT NULL COMMENT 'ICU 病区ID',
  `ward_name` varchar(128) COMMENT 'ICU 病区名称',
  `bed_id` bigint NOT NULL COMMENT 'ICU 床位ID',
  `bed_no` varchar(16) COMMENT 'ICU 床位号',
  `care_level` tinyint NOT NULL DEFAULT 1 COMMENT '监护等级（1-特级 2-I级 3-II级）',
  `in_time` datetime NOT NULL COMMENT '入科时间',
  `in_diag` varchar(255) COMMENT '入科诊断/原因',
  `in_gcs` int COMMENT '入科 GCS 评分',
  `in_by` varchar(64) COMMENT '入科登记人',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-在科 2-已出科）',
  `out_time` datetime COMMENT '出科时间',
  `out_dest` tinyint COMMENT '转出去向（1-普通病房 2-专科病房 3-手术室 4-转院 5-死亡 6-自动离院）',
  `out_reason` varchar(255) COMMENT '出科情况/转归说明',
  `out_gcs` int COMMENT '出科 GCS 评分',
  `out_by` varchar(64) COMMENT '出科登记人',
  `monitor_count` int NOT NULL DEFAULT 0 COMMENT '监护记录条数',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_icu_stay_no` (`stay_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ICU 入出科登记';

-- biz_infection_case  院感病例报告卡
CREATE TABLE `biz_infection_case` (
  `id` bigint NOT NULL COMMENT '主键',
  `case_no` varchar(32) NOT NULL COMMENT '病例编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者编号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别',
  `age` int COMMENT '年龄',
  `visit_type` tinyint NOT NULL COMMENT '就诊类型',
  `regist_id` bigint COMMENT '门诊就诊ID',
  `inp_id` bigint COMMENT '住院记录ID',
  `dept_id` bigint COMMENT '发现科室ID',
  `dept_name` varchar(100) COMMENT '发现科室',
  `case_source` tinyint NOT NULL COMMENT '感染来源',
  `infection_site` varchar(8) NOT NULL COMMENT '感染部位',
  `infection_diag` varchar(200) NOT NULL COMMENT '感染诊断',
  `pathogen` varchar(100) COMMENT '病原菌',
  `specimen` varchar(100) COMMENT '标本来源',
  `infect_date` date NOT NULL COMMENT '感染/诊断日期',
  `case_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `leak_flag` tinyint NOT NULL DEFAULT 0 COMMENT '漏报标志',
  `report_by` bigint NOT NULL COMMENT '上报人ID',
  `report_name` varchar(50) NOT NULL COMMENT '上报人姓名',
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
  `patient_no` varchar(32) NOT NULL COMMENT '患者编号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `monitor_type` tinyint NOT NULL COMMENT '监测类型',
  `dept_id` bigint COMMENT '监测科室ID',
  `dept_name` varchar(100) COMMENT '监测科室',
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
  `recorder_name` varchar(50) NOT NULL COMMENT '记录人姓名',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记',
  `remark` varchar(255),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测每日打卡';

-- biz_infectious_report  传染病报告卡
CREATE TABLE `biz_infectious_report` (
  `id` bigint NOT NULL COMMENT '主键',
  `report_no` varchar(32) NOT NULL COMMENT '报卡编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者编号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别',
  `age` int COMMENT '年龄',
  `regist_id` bigint COMMENT '门诊就诊ID',
  `inp_id` bigint COMMENT '住院记录ID',
  `visit_dept_id` bigint COMMENT '发现/就诊科室ID',
  `visit_dept_name` varchar(100) COMMENT '发现/就诊科室',
  `disease_id` bigint NOT NULL COMMENT '病种ID',
  `disease_code` varchar(16) NOT NULL COMMENT '病种编码',
  `disease_name` varchar(50) NOT NULL COMMENT '病种名称',
  `infectious_class` tinyint NOT NULL COMMENT '传染病类别（快照，1甲/2乙/3丙）',
  `icd10` varchar(16) COMMENT 'ICD-10',
  `report_deadline` datetime NOT NULL COMMENT '报卡时限',
  `clinical_desc` varchar(500) COMMENT '临床摘要',
  `report_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `report_count` int NOT NULL DEFAULT 1 COMMENT '报卡次数',
  `report_by` bigint NOT NULL COMMENT '填卡医生ID',
  `report_by_name` varchar(50) NOT NULL COMMENT '填卡医生姓名',
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

-- biz_infusion_round  输液巡视记录
CREATE TABLE `biz_infusion_round` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `exec_id` bigint NOT NULL COMMENT '执行行ID',
  `order_id` bigint NOT NULL COMMENT '医嘱ID（冗余）',
  `admission_id` bigint NOT NULL COMMENT '入院ID（冗余）',
  `round_time` datetime NOT NULL COMMENT '巡视时间',
  `drip_rate` int COMMENT '滴速（滴/分）',
  `remaining_volume` int COMMENT '余量（ml）',
  `round_nurse_id` bigint COMMENT '巡视护士ID（员工ID）',
  `round_nurse_name` varchar(64) COMMENT '巡视护士姓名',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='输液巡视记录';

-- biz_infusion_seat  输液室座位
CREATE TABLE `biz_infusion_seat` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `seat_no` varchar(32) NOT NULL COMMENT '座位号',
  `area` varchar(50) NOT NULL DEFAULT '普通区' COMMENT '区域（成人区/儿童区/隔离区等）',
  `seat_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-空闲 2-占用 3-停用）',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seat_no` (`seat_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='输液室座位';

-- biz_inpatient_diagnosis  病案首页诊断明细
CREATE TABLE `biz_inpatient_diagnosis` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `seq_no` int NOT NULL DEFAULT 1 COMMENT '序号',
  `diag_type` tinyint NOT NULL DEFAULT 2 COMMENT '诊断类型（1-主要诊断 2-其他诊断）',
  `icd_code` varchar(32) COMMENT 'ICD-10 编码',
  `icd_name` varchar(200) COMMENT '诊断名称',
  `admit_condition` tinyint COMMENT '入院病情（1-有 2-临床未确定 3-情况不明 4-无）',
  `cc_level` varchar(8) COMMENT '并发症合并症级别',
  `diagnosis_basis` varchar(500) COMMENT '诊断依据',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病案首页诊断明细';

-- biz_inpatient_leave  住院请假登记
CREATE TABLE `biz_inpatient_leave` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `leave_no` varchar(32) NOT NULL COMMENT '请假单号',
  `admission_id` bigint NOT NULL COMMENT '住院记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `patient_no` varchar(32) COMMENT '患者编号',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `dept_id` bigint COMMENT '申请时点所在科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `ward_name` varchar(64) COMMENT '病区名称',
  `bed_no` varchar(16) COMMENT '床位号',
  `admission_no` varchar(32) COMMENT '住院号',
  `leave_type` tinyint NOT NULL COMMENT '请假类别（1-临时外出当日往返 2-离院过夜 9-其他）',
  `reason` varchar(500) NOT NULL COMMENT '请假事由（必填）',
  `destination` varchar(200) NOT NULL COMMENT '去向',
  `companion_name` varchar(50) NOT NULL COMMENT '随行/联系人姓名（必填）',
  `companion_relation` tinyint COMMENT '随行人与患者关系',
  `companion_phone` varchar(20) NOT NULL COMMENT '随行人联系电话（必填）',
  `expected_leave_time` datetime NOT NULL COMMENT '预计离院时间',
  `expected_return_time` datetime NOT NULL COMMENT '预计返回时间',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `apply_by` varchar(64) COMMENT '申请人',
  `doctor_advice` varchar(1000) COMMENT '医师意见',
  `approve_time` datetime COMMENT '审批时间',
  `doctor_id` bigint COMMENT '审批医师ID',
  `doctor_name` varchar(50) COMMENT '审批医师姓名',
  `reject_reason` varchar(500) COMMENT '拒绝理由',
  `confirm_name` varchar(50) COMMENT '患方确认人姓名',
  `confirm_relation` tinyint COMMENT '确认人与患者关系',
  `confirm_phone` varchar(20) COMMENT '确认人联系电话',
  `confirm_signature` mediumtext COMMENT '患方手写签名',
  `confirm_time` datetime COMMENT '患方签署时间',
  `actual_leave_time` datetime COMMENT '实际离院时间',
  `actual_return_time` datetime COMMENT '实际返回时间',
  `return_note` varchar(500) COMMENT '返回情况备注',
  `return_by` varchar(64) COMMENT '销假经办人',
  `leave_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待审批 2-已批准 3-已离院 4-已返回 5-已拒绝 6-已取消）',
  `overdue_contact_result` tinyint COMMENT '超期联系结果（1-联系上并约定返回 2-联系不上 3-家属）',
  `overdue_contact_note` varchar(500) COMMENT '超期处置备注',
  `overdue_contact_time` datetime COMMENT '超期处置时间',
  `overdue_contact_by` varchar(64) COMMENT '超期处置人',
  `report_to` tinyint COMMENT '上报对象（1-主管医师 2-病区护士长 3-医务科）',
  `printer_name` varchar(64) COMMENT '最后打印人',
  `print_count` int NOT NULL DEFAULT 0 COMMENT '承诺书打印次数',
  `last_print_time` datetime COMMENT '最后打印时间',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `cancel_by` varchar(64) COMMENT '取消经办人',
  `cancel_time` datetime COMMENT '取消时间',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '电子签名状态（0-未签名 1-已签名 2-签名已作废）',
  `sign_id` bigint COMMENT '当前有效签名ID',
  `signed_time` datetime COMMENT '签名时刻',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_leave_no` (`leave_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院请假登记';

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

-- biz_inpatient_order  住院医嘱主表
CREATE TABLE `biz_inpatient_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '医嘱号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '开立科室ID',
  `dept_name` varchar(64) COMMENT '开立科室名称',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称',
  `bed_no` varchar(32) COMMENT '床号',
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

-- biz_inpatient_order_exec  医嘱执行记录
CREATE TABLE `biz_inpatient_order_exec` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '医嘱ID',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint COMMENT '患者ID（冗余）',
  `exec_seq` int NOT NULL DEFAULT 1 COMMENT '本条医嘱的第几次执行',
  `plan_date` date NOT NULL COMMENT '计划日期',
  `plan_time` datetime NOT NULL COMMENT '计划执行时间',
  `exec_time` datetime COMMENT '实际执行时间',
  `exec_nurse_id` bigint COMMENT '执行护士ID（员工ID）',
  `exec_nurse_name` varchar(64) COMMENT '执行护士姓名',
  `exec_status` tinyint NOT NULL DEFAULT 1 COMMENT '执行状态（1-待执行 2-已执行 3-已跳过 4-已退回）',
  `exec_note` varchar(255) COMMENT '执行备注 / 跳过原因',
  `infusion_start_time` datetime COMMENT '输液开始时间',
  `drip_rate` int COMMENT '开始滴速（滴/分）',
  `infusion_end_time` datetime COMMENT '输液结束时间',
  `adverse_flag` tinyint NOT NULL DEFAULT 0 COMMENT '输液不良反应（0-无 1-有）',
  `adverse_note` varchar(255) COMMENT '不良反应描述',
  `fee_record_id` bigint COMMENT '本次执行生成的记账行ID',
  `fee_no` varchar(32) COMMENT '本次执行生成的记账单号',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ioe_order_plan_date` (`order_id`, `plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医嘱执行记录';

-- biz_inpatient_order_template  医嘱模板
CREATE TABLE `biz_inpatient_order_template` (
  `id` bigint NOT NULL COMMENT '模板ID（雪花）',
  `doctor_id` bigint COMMENT '归属医生',
  `doctor_name` varchar(64) COMMENT '医生姓名',
  `dept_id` bigint COMMENT '创建时科室ID',
  `scope` tinyint NOT NULL DEFAULT 1 COMMENT '共享范围（1-个人 2-科室 3-全院）',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `order_type` tinyint NOT NULL DEFAULT 2 COMMENT '默认医嘱类型（1-长期 2-临时）',
  `item_count` int NOT NULL DEFAULT 0 COMMENT '明细条数',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注/适用场景说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医嘱模板';

-- biz_inpatient_order_template_item  医嘱模板明细
CREATE TABLE `biz_inpatient_order_template_item` (
  `id` bigint NOT NULL COMMENT '明细ID（雪花）',
  `template_id` bigint NOT NULL COMMENT '模板主表ID',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `order_class` tinyint NOT NULL COMMENT '医嘱类别',
  `item_code` varchar(64) COMMENT '项目编码（药品/检查/检验字典码，套用时按它回查现价与下拉选中态）',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `spec` varchar(100) COMMENT '规格',
  `unit` varchar(32) COMMENT '单位',
  `dosage` decimal(12,4) COMMENT '单次剂量',
  `dosage_unit` varchar(32) COMMENT '剂量单位',
  `route` varchar(64) COMMENT '给药途径',
  `frequency` varchar(32) COMMENT '频次',
  `quantity` decimal(12,2) NOT NULL DEFAULT 1.00 COMMENT '数量',
  `price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '录入时单价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医嘱模板明细';

-- biz_inpatient_record  住院病历文书
CREATE TABLE `biz_inpatient_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '病历文书号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `age_unit` tinyint COMMENT '年龄单位（1-岁 2-月 3-天）',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(64) COMMENT '科室名称',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称',
  `bed_no` varchar(32) COMMENT '床号',
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

-- biz_inpatient_record_log  住院文书修改日志
CREATE TABLE `biz_inpatient_record_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `doc_type` tinyint NOT NULL COMMENT '单据类型（1-住院病历文书 2-护理文书）',
  `record_id` bigint NOT NULL COMMENT '单据ID',
  `record_no` varchar(50) COMMENT '单据号',
  `record_type` tinyint COMMENT '文书类型码',
  `user_id` bigint COMMENT '操作人ID（员工ID）',
  `user_name` varchar(64) COMMENT '操作人姓名',
  `operation` varchar(50) COMMENT '操作',
  `field_name` varchar(100) COMMENT '变更字段',
  `old_value` text COMMENT '变更前值',
  `new_value` text COMMENT '变更后值',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院文书修改日志';

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

-- biz_inpatient_summary  住院病案首页
CREATE TABLE `biz_inpatient_summary` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `age_unit` tinyint COMMENT '年龄单位（1-岁 2-月 3-天）',
  `id_card` varchar(18) COMMENT '身份证号',
  `admit_dept_id` bigint COMMENT '入院科别ID',
  `admit_dept_name` varchar(64) COMMENT '入院科别名称',
  `medical_insurance_no` varchar(32) COMMENT '医保卡号',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(50) COMMENT '科室名称',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称',
  `bed_no` varchar(16) COMMENT '床位号',
  `admit_time` datetime COMMENT '入院时间',
  `discharge_time` datetime COMMENT '出院时间',
  `inpatient_days` int COMMENT '实际住院天数',
  `admit_way` tinyint COMMENT '入院途径（1-门诊 2-急诊 3-转院 4-其他）',
  `discharge_way` tinyint COMMENT '离院方式（1-医嘱离院 2-医嘱转院 3-医嘱转社区 4-非医嘱离院 5-死亡 9-其他）',
  `death_flag` tinyint DEFAULT 0 COMMENT '死亡标志（0-否 1-是）',
  `autopsy_flag` tinyint DEFAULT 0 COMMENT '死亡患者尸检（0-否 1-是）',
  `readmit_31d` tinyint COMMENT '31日内再入院（0-否 1-是）',
  `is_surgery` tinyint DEFAULT 0 COMMENT '是否手术（0-否 1-是）',
  `is_transfusion` tinyint DEFAULT 0 COMMENT '是否输血（0-否 1-是）',
  `is_rescue` tinyint DEFAULT 0 COMMENT '是否抢救（0-否 1-是）',
  `is_critical` tinyint DEFAULT 0 COMMENT '是否危重（0-否 1-是）',
  `main_diagnosis_code` varchar(32) COMMENT '主要诊断编码',
  `main_diagnosis_name` varchar(200) COMMENT '主要诊断名称',
  `total_amount` decimal(12,2) COMMENT '住院总费用',
  `western_drug_amount` decimal(12,2) COMMENT '西药费',
  `chinese_drug_amount` decimal(12,2) COMMENT '中成药费',
  `herbal_amount` decimal(12,2) COMMENT '中药饮片费',
  `exam_amount` decimal(12,2) COMMENT '检查费',
  `lab_amount` decimal(12,2) COMMENT '检验费',
  `treatment_amount` decimal(12,2) COMMENT '治疗费',
  `operation_amount` decimal(12,2) COMMENT '手术费',
  `material_amount` decimal(12,2) COMMENT '耗材费',
  `bed_amount` decimal(12,2) COMMENT '床位费',
  `nursing_amount` decimal(12,2) COMMENT '护理费',
  `other_amount` decimal(12,2) COMMENT '其他费用',
  `summary_status` tinyint NOT NULL DEFAULT 1 COMMENT '首页状态（1-草稿 2-已提交 3-已归档）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_summary_admission` (`admission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院病案首页';

-- biz_inpatient_transfer  住院转科轨迹
CREATE TABLE `biz_inpatient_transfer` (
  `id` bigint NOT NULL COMMENT '转科记录ID（雪花）',
  `transfer_no` varchar(32) NOT NULL COMMENT '转科单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `admission_no` varchar(32) COMMENT '入院号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `from_dept_id` bigint COMMENT '转出科室ID',
  `from_dept_name` varchar(64) COMMENT '转出科室名称',
  `from_ward_id` bigint COMMENT '转出病区ID',
  `from_ward_name` varchar(64) COMMENT '转出病区名称',
  `from_bed_id` bigint COMMENT '转出床位ID',
  `from_bed_no` varchar(32) COMMENT '转出床位号',
  `to_dept_id` bigint NOT NULL COMMENT '转入科室ID',
  `to_dept_name` varchar(64) COMMENT '转入科室名称',
  `to_ward_id` bigint NOT NULL COMMENT '转入病区ID',
  `to_ward_name` varchar(64) COMMENT '转入病区名称',
  `to_bed_id` bigint NOT NULL COMMENT '转入床位ID',
  `to_bed_no` varchar(32) COMMENT '转入床位号',
  `transfer_type` tinyint NOT NULL DEFAULT 1 COMMENT '转科类型（1-普通转科 2-急诊转科 3-转入ICU 4-ICU转出）',
  `transfer_reason` varchar(500) NOT NULL COMMENT '转科原因',
  `hospital_days` int COMMENT '发起转科时该次住院的已住院天数',
  `stop_orders_count` int NOT NULL DEFAULT 0 COMMENT '接收时随之停止的长期医嘱条数',
  `order_remark` varchar(500) COMMENT '医嘱处置说明',
  `apply_doctor_id` bigint COMMENT '转出方发起医生ID',
  `apply_doctor_name` varchar(64) COMMENT '转出方发起医生姓名',
  `receive_doctor_id` bigint COMMENT '转入方接收医生ID（员工ID）',
  `receive_doctor_name` varchar(64) COMMENT '转入方接收医生姓名',
  `record_id` bigint COMMENT '回写的住院病历ID',
  `apply_time` datetime COMMENT '发起时间',
  `receive_time` datetime COMMENT '接收时间',
  `transfer_status` tinyint NOT NULL DEFAULT 0 COMMENT '转科状态（0-待接收 1-已完成 2-已取消）',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院转科轨迹';

-- biz_inspection_apply  检查申请单
CREATE TABLE `biz_inspection_apply` (
  `id` bigint NOT NULL,
  `apply_no` varchar(32) NOT NULL COMMENT '申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别',
  `age` int COMMENT '年龄',
  `regist_id` bigint NOT NULL COMMENT '挂号ID',
  `record_id` bigint COMMENT '病历ID',
  `record_no` varchar(32) COMMENT '病历号',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `dept_id` bigint NOT NULL COMMENT '申请科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '申请科室名称',
  `doctor_id` bigint NOT NULL COMMENT '申请医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '申请医生姓名',
  `inspection_item_id` bigint NOT NULL COMMENT '检查项目ID',
  `inspection_item_code` varchar(32) NOT NULL COMMENT '检查项目编码',
  `inspection_item_name` varchar(200) NOT NULL COMMENT '检查项目名称',
  `inspection_dept_id` bigint COMMENT '检查科室ID',
  `inspection_dept_name` varchar(100) COMMENT '检查科室名称',
  `body_part` varchar(200) COMMENT '检查部位',
  `inspection_purpose` varchar(500) COMMENT '检查目的',
  `clinical_diagnosis` varchar(500) COMMENT '临床诊断',
  `disease_summary` text COMMENT '病史摘要',
  `special_requirements` varchar(500) COMMENT '特殊要求',
  `is_emergency` tinyint DEFAULT 0 COMMENT '是否急诊（0-否 1-是）',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '检查费用',
  `apply_status` tinyint DEFAULT 1 COMMENT '申请状态（1-已提交 2-已缴费 6-已取消）',
  `appointment_time` datetime COMMENT '预约时间',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '签名状态（0-未签名 1-已签名 2-签名已失效）',
  `sign_id` bigint COMMENT '当前有效签名ID',
  `signed_time` datetime COMMENT '最近一次签名时刻',
  `submit_time` datetime COMMENT '提交时间',
  `pay_time` datetime COMMENT '缴费时间',
  `complete_time` datetime COMMENT '完成时间',
  `report_id` bigint COMMENT '报告ID',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(200) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查申请单';

-- biz_inspection_record  检查记录
CREATE TABLE `biz_inspection_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '检查记录号（唯一）',
  `apply_id` bigint NOT NULL COMMENT '申请单ID',
  `apply_no` varchar(32) NOT NULL COMMENT '申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `phone` varchar(20) COMMENT '手机号码',
  `visit_date` date COMMENT '就诊日期',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(100) COMMENT '申请科室',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(50) COMMENT '申请医生',
  `inspection_item_id` bigint NOT NULL COMMENT '检查项目ID',
  `inspection_item_code` varchar(32) NOT NULL COMMENT '检查项目编码',
  `inspection_item_name` varchar(200) NOT NULL COMMENT '检查项目名称',
  `inspection_dept_id` bigint COMMENT '检查科室ID',
  `inspection_dept_name` varchar(100) COMMENT '检查科室名称',
  `body_part` varchar(200) COMMENT '检查部位',
  `inspection_purpose` varchar(500) COMMENT '检查目的',
  `clinical_diagnosis` varchar(500) COMMENT '临床诊断',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '检查费用',
  `appointment_time` datetime COMMENT '预约时间',
  `check_in_time` datetime COMMENT '签到时间',
  `execute_time` datetime COMMENT '执行时间',
  `execute_by` varchar(64) COMMENT '执行人',
  `result_description` text COMMENT '检查描述',
  `result_conclusion` text COMMENT '检查结论',
  `report_sign_id` bigint COMMENT '报告医师签名ID',
  `report_signed_time` datetime COMMENT '报告签名时刻',
  `result_image` varchar(500) COMMENT '检查影像路径',
  `record_status` tinyint DEFAULT 1 COMMENT '记录状态（1-已登记 2-已签到 3-检查中 4-已出结果 5-已审核 6-已发布 7-已取消）',
  `audit_by` varchar(64) COMMENT '审核人（初审）',
  `audit_sign_id` bigint COMMENT '审核医师签名ID',
  `audit_signed_time` datetime COMMENT '审核签名时刻',
  `audit_time` datetime COMMENT '审核时间',
  `audit2_by` varchar(64) COMMENT '复审人',
  `audit2_time` datetime COMMENT '复审时间',
  `report_time` datetime COMMENT '报告发布时间',
  `report_by` varchar(64) COMMENT '报告发布人',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(200) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查记录';

-- biz_inspection_template  检查申请模板
CREATE TABLE `biz_inspection_template` (
  `id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `inspection_item_id` bigint NOT NULL COMMENT '检查项目ID',
  `inspection_item_code` varchar(32) NOT NULL COMMENT '检查项目编码',
  `inspection_item_name` varchar(200) NOT NULL COMMENT '检查项目名称',
  `body_part` varchar(100) COMMENT '检查部位',
  `inspection_purpose` varchar(200) COMMENT '检查目的',
  `is_emergency` tinyint DEFAULT 0 COMMENT '是否急诊（0-否 1-是）',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查申请模板';

-- biz_insurance_catalog_rule  医保目录报销规则
CREATE TABLE `biz_insurance_catalog_rule` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `rule_no` varchar(32) NOT NULL COMMENT '规则编号',
  `item_code` varchar(32) NOT NULL COMMENT '项目编码',
  `item_name` varchar(200) COMMENT '项目名称',
  `catalog_type` tinyint NOT NULL COMMENT '医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）',
  `encounter_type` tinyint NOT NULL COMMENT '就诊类型（1-门诊 2-住院）',
  `insurance_type` varchar(32) COMMENT '医保类型（职工/居民/公费等；NULL=通用规则，所有医保类型共用）',
  `self_pay_ratio` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '自付比例（%）',
  `deductible` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '起付线（元）',
  `ceiling` decimal(10,2) NOT NULL DEFAULT 999999.99 COMMENT '封顶线（元）',
  `pool_ratio` decimal(5,2) NOT NULL DEFAULT 0.00 COMMENT '统筹报销比例（%）',
  `limit_flags` int NOT NULL DEFAULT 0 COMMENT '限制标志位掩码',
  `effective_date` date NOT NULL COMMENT '生效日期（含）',
  `expire_date` date COMMENT '失效日期',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_catalog_encounter` (`item_code`, `catalog_type`, `encounter_type`, `insurance_type`, `effective_date`),
  UNIQUE KEY `uk_rule_no` (`rule_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保目录报销规则';

-- biz_insurance_report  医保报盘报文台账
CREATE TABLE `biz_insurance_report` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `settlement_id` bigint NOT NULL COMMENT '医保结算清单ID',
  `settlement_no` varchar(64) COMMENT '结算清单号',
  `report_type` tinyint NOT NULL COMMENT '报文类型（1-上传 2-撤销）',
  `msg_type` varchar(8) NOT NULL COMMENT '医保接口编号',
  `trade_no` varchar(64) NOT NULL COMMENT 'HIS 侧流水号',
  `orig_trade_no` varchar(64) COMMENT '撤销报文回指的原上传 trade_no',
  `receipt_no` varchar(64) COMMENT '医保端回执编号',
  `payload` longtext NOT NULL COMMENT '出参报文全文',
  `reply_payload` text COMMENT '回执报文全文（JSON）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '报文状态（0-待发送 1-回执成功 2-回执失败 3-已被撤销）',
  `err_msg` varchar(500) COMMENT '失败原因',
  `send_time` datetime COMMENT '发出时间',
  `reply_time` datetime COMMENT '回执时间',
  `bill_date` date COMMENT '账期日',
  `total_amount` decimal(12,2) COMMENT '冗余',
  `insurance_pay` decimal(12,2) COMMENT '冗余',
  `personal_pay` decimal(12,2) COMMENT '冗余',
  `self_pay` decimal(12,2) COMMENT '冗余',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_no` (`trade_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保报盘报文台账';

-- biz_insurance_settlement  医保结算清单
CREATE TABLE `biz_insurance_settlement` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `settlement_no` varchar(32) NOT NULL COMMENT '结算清单号',
  `bill_id` bigint NOT NULL COMMENT '结算账单ID',
  `bill_no` varchar(32) COMMENT '结算账单号',
  `encounter_type` tinyint COMMENT '就诊类型（1-门诊 2-住院）',
  `encounter_id` bigint COMMENT '就诊标识',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `id_card` varchar(18) COMMENT '身份证号',
  `medical_insurance_no` varchar(32) COMMENT '医保卡号',
  `regist_id` bigint COMMENT '挂号ID快照',
  `visit_type` varchar(20) COMMENT '就诊类型（初诊/复诊）',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(50) COMMENT '科室名称',
  `doctor_id` bigint COMMENT '医生ID',
  `doctor_name` varchar(50) COMMENT '医生姓名',
  `diagnosis` varchar(500) COMMENT '诊断',
  `diagnosis_code` varchar(32) COMMENT '诊断编码',
  `diagnosis_name` varchar(200) COMMENT '诊断名称',
  `total_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '医疗总费用 = 账单应收合计 - 院内优惠',
  `drug_amount` decimal(10,2) DEFAULT 0.00 COMMENT '药品费',
  `inspection_amount` decimal(10,2) DEFAULT 0.00 COMMENT '检查费',
  `laboratory_amount` decimal(10,2) DEFAULT 0.00 COMMENT '检验费',
  `treatment_amount` decimal(10,2) DEFAULT 0.00 COMMENT '治疗费',
  `material_amount` decimal(10,2) DEFAULT 0.00 COMMENT '材料费',
  `other_amount` decimal(10,2) DEFAULT 0.00 COMMENT '其他费用',
  `settlement_type` tinyint COMMENT '结算方式（1-自费 2-医保）',
  `insurance_type` varchar(32) COMMENT '医保类型（城镇职工/城镇居民/新农合等）',
  `coverage_ratio` decimal(5,2) COMMENT '统筹报销比例（%）',
  `insurance_pay` decimal(10,2) DEFAULT 0.00 COMMENT '医保统筹支付 = 账单 pool_amount',
  `personal_pay` decimal(10,2) DEFAULT 0.00 COMMENT '个人账户支付 = 本账单 pay_method=4 的成功收款流水合计',
  `self_pay` decimal(10,2) DEFAULT 0.00 COMMENT '患者自付 = 现金',
  `settlement_status` tinyint NOT NULL DEFAULT 1 COMMENT '清单状态（1-待结算 2-已结算 3-已上传 4-已审核 5-已作废）',
  `upload_time` datetime COMMENT '上传时间',
  `audit_status` tinyint COMMENT '审核状态（0-待审核 1-审核通过 2-审核驳回）',
  `audit_time` datetime COMMENT '审核时间',
  `audit_remark` varchar(500) COMMENT '审核意见',
  `drg_code` varchar(32) COMMENT 'DRG分组编码',
  `drg_weight` decimal(10,4) COMMENT 'DRG权重',
  `estimated_cost` decimal(10,2) COMMENT '预估费用',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_isb_bill` (`bill_id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保结算清单';

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

-- biz_lab_result  检验结果
CREATE TABLE `biz_lab_result` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_id` bigint NOT NULL COMMENT '检验记录ID',
  `record_no` varchar(32) NOT NULL COMMENT '检验记录号',
  `laboratory_item_id` bigint NOT NULL COMMENT '检验项目ID',
  `laboratory_item_code` varchar(32) NOT NULL COMMENT '检验项目编码',
  `laboratory_item_name` varchar(200) NOT NULL COMMENT '检验项目名称',
  `result_value` varchar(200) COMMENT '检验结果值',
  `result_unit` varchar(50) COMMENT '结果单位',
  `reference_range` varchar(100) COMMENT '参考范围',
  `abnormal_flag` tinyint DEFAULT 0 COMMENT '异常标志（0-正常 1-偏高 2-偏低 3-异常）',
  `abnormal_desc` varchar(200) COMMENT '异常描述',
  `judge_note` varchar(200) COMMENT '异常判定说明',
  `result_type` tinyint DEFAULT 1 COMMENT '结果类型（1-定量 2-定性 3-文字描述）',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验结果';

-- biz_laboratory_apply  检验申请单
CREATE TABLE `biz_laboratory_apply` (
  `id` bigint NOT NULL,
  `apply_no` varchar(32) NOT NULL COMMENT '申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别',
  `age` int COMMENT '年龄',
  `regist_id` bigint NOT NULL COMMENT '挂号ID',
  `record_id` bigint COMMENT '病历ID',
  `record_no` varchar(32) COMMENT '病历号',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `dept_id` bigint NOT NULL COMMENT '申请科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '申请科室名称',
  `doctor_id` bigint NOT NULL COMMENT '申请医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '申请医生姓名',
  `laboratory_item_id` bigint NOT NULL COMMENT '检验项目ID',
  `laboratory_item_code` varchar(32) NOT NULL COMMENT '检验项目编码',
  `laboratory_item_name` varchar(200) NOT NULL COMMENT '检验项目名称',
  `laboratory_dept_id` bigint COMMENT '检验科室ID',
  `laboratory_dept_name` varchar(100) COMMENT '检验科室名称',
  `specimen_type` varchar(50) NOT NULL COMMENT '标本类型',
  `specimen_source` varchar(100) COMMENT '标本来源',
  `laboratory_purpose` varchar(500) COMMENT '检验目的',
  `clinical_diagnosis` varchar(500) COMMENT '临床诊断',
  `disease_summary` text COMMENT '病史摘要',
  `is_fasting` tinyint DEFAULT 0 COMMENT '是否空腹（0-否 1-是）',
  `is_emergency` tinyint DEFAULT 0 COMMENT '是否急诊（0-否 1-是）',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '检验费用',
  `apply_status` tinyint DEFAULT 1 COMMENT '申请状态（1-已提交 2-已缴费 6-已取消）',
  `submit_time` datetime COMMENT '提交时间',
  `sign_status` tinyint NOT NULL DEFAULT 0 COMMENT '签名状态（0-未签名 1-已签名 2-签名已失效）',
  `sign_id` bigint COMMENT '当前有效签名ID',
  `signed_time` datetime COMMENT '最近一次签名时刻',
  `pay_time` datetime COMMENT '缴费时间',
  `sample_time` datetime COMMENT '采样时间',
  `sample_by` varchar(64) COMMENT '采样人',
  `complete_time` datetime COMMENT '完成时间',
  `report_id` bigint COMMENT '报告ID',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(200) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验申请单';

-- biz_laboratory_record  检验记录
CREATE TABLE `biz_laboratory_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '检验记录号（唯一）',
  `apply_id` bigint NOT NULL COMMENT '申请单ID',
  `apply_no` varchar(32) NOT NULL COMMENT '申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `phone` varchar(20) COMMENT '手机号码',
  `visit_date` date COMMENT '就诊日期',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(100) COMMENT '申请科室',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(50) COMMENT '申请医生',
  `laboratory_item_id` bigint NOT NULL COMMENT '检验项目ID',
  `laboratory_item_code` varchar(32) NOT NULL COMMENT '检验项目编码',
  `laboratory_item_name` varchar(200) NOT NULL COMMENT '检验项目名称',
  `laboratory_dept_id` bigint COMMENT '检验科室ID',
  `laboratory_dept_name` varchar(100) COMMENT '检验科室名称',
  `specimen_type` varchar(50) NOT NULL COMMENT '标本类型（血液、尿液、粪便等）',
  `specimen_no` varchar(50) COMMENT '标本编号',
  `specimen_status` tinyint DEFAULT 1 COMMENT '标本状态（1-待采集 2-已采集 3-已接收 4-检测中 5-已完成 6-已退回）',
  `sample_time` datetime COMMENT '采样时间',
  `sample_by` varchar(64) COMMENT '采样人',
  `receive_time` datetime COMMENT '接收时间',
  `receive_by` varchar(64) COMMENT '接收人',
  `execute_time` datetime COMMENT '检测时间',
  `execute_by` varchar(64) COMMENT '检测人',
  `price` decimal(10,2) DEFAULT 0.00 COMMENT '检验费用',
  `diagnosis` text COMMENT '检验结论/诊断',
  `report_sign_id` bigint COMMENT '报告医师签名ID',
  `report_signed_time` datetime COMMENT '报告签名时刻',
  `suggestions` text COMMENT '建议',
  `record_status` tinyint DEFAULT 1 COMMENT '记录状态（1-已登记 2-已采样 3-已接收 4-检测中 5-已出结果 6-已审核 7-已发布 8-已取消）',
  `audit_by` varchar(64) COMMENT '审核人（初审）',
  `audit_sign_id` bigint COMMENT '审核医师签名ID',
  `audit_signed_time` datetime COMMENT '审核签名时刻',
  `audit_time` datetime COMMENT '审核时间',
  `audit2_by` varchar(64) COMMENT '复审人',
  `audit2_time` datetime COMMENT '复审时间',
  `report_time` datetime COMMENT '报告发布时间',
  `report_by` varchar(64) COMMENT '报告发布人',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(200) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验记录';

-- biz_laboratory_template  检验申请模板
CREATE TABLE `biz_laboratory_template` (
  `id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `laboratory_item_id` bigint NOT NULL COMMENT '检验项目ID',
  `laboratory_item_code` varchar(32) NOT NULL COMMENT '检验项目编码',
  `laboratory_item_name` varchar(200) NOT NULL COMMENT '检验项目名称',
  `sample_type` varchar(50) COMMENT '标本类型',
  `inspection_purpose` varchar(200) COMMENT '检验目的',
  `is_emergency` tinyint DEFAULT 0 COMMENT '是否急诊（0-否 1-是）',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验申请模板';

-- biz_lis_eqa_compare  室间质评仪器间比对
CREATE TABLE `biz_lis_eqa_compare` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `plan_id` bigint NOT NULL COMMENT '质评批次ID',
  `plan_no` varchar(32) COMMENT '质评批次号（冗余）',
  `item_code` varchar(32) COMMENT '检验项目编码',
  `item_name` varchar(200) COMMENT '检验项目名称',
  `sample_seq` tinyint COMMENT '第几个样品',
  `sample_no` varchar(64) COMMENT '盲样编号',
  `instrument_a` varchar(100) COMMENT 'A 组仪器',
  `method_a` varchar(100) COMMENT 'A 组方法学',
  `value_a` decimal(18,4) COMMENT 'A 组测定值',
  `instrument_b` varchar(100) COMMENT 'B 组仪器',
  `method_b` varchar(100) COMMENT 'B 组方法学',
  `value_b` decimal(18,4) COMMENT 'B 组测定值',
  `diff_value` decimal(18,4) COMMENT '互差绝对值 |A-B|',
  `diff_rate` decimal(10,2) COMMENT '相对互差% = |A-B| /(A+B)',
  `allow_rate` decimal(10,2) COMMENT '允许互差%',
  `allow_source` tinyint DEFAULT 1 COMMENT '允许限来源（1-由该批次 2-无）',
  `status` tinyint DEFAULT 1 COMMENT '比对结论（1-可接受 2-超差）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_compare` (`plan_id`, `sample_seq`, `item_code`, `instrument_a`, `instrument_b`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='室间质评仪器间比对';

-- biz_lis_eqa_plan  室间质评批次
CREATE TABLE `biz_lis_eqa_plan` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `plan_no` varchar(32) NOT NULL COMMENT '质评批次号',
  `plan_year` smallint NOT NULL COMMENT '质评年度',
  `batch_no` tinyint NOT NULL COMMENT '本年度第几批（1-上半年 2-下半年）',
  `org_name` varchar(200) NOT NULL COMMENT '组织方（国家/省/市临床检验中心 或 第三方质评机构）',
  `plan_name` varchar(200) COMMENT '质评计划名称',
  `item_count` int DEFAULT 0 COMMENT '本次参加项目数',
  `sample_count` int DEFAULT 0 COMMENT '本次下发盲样数',
  `receive_date` date COMMENT '盲样接收日期',
  `receive_by` varchar(64) COMMENT '盲样接收人',
  `report_deadline` date COMMENT '结果上报截止日期',
  `return_date` date COMMENT '成绩回报日期',
  `status` tinyint DEFAULT 1 COMMENT '批次状态（1-待收样 2-检测中 3-已上报 4-已回报 5-已归档）',
  `pt_score` decimal(6,2) COMMENT 'PT 得分',
  `pass_flag` tinyint COMMENT '本次是否合格（1-合格 0-不合格）',
  `fail_count` int DEFAULT 0 COMMENT '判定为「不合格」的盲样项数',
  `archive_by` varchar(64) COMMENT '归档人',
  `archive_time` datetime COMMENT '归档时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_no` (`plan_no`),
  UNIQUE KEY `uk_year_batch_org` (`plan_year`, `batch_no`, `org_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='室间质评批次';

-- biz_lis_eqa_sample  室间质评盲样
CREATE TABLE `biz_lis_eqa_sample` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `plan_id` bigint NOT NULL COMMENT '质评批次ID',
  `plan_no` varchar(32) COMMENT '质评批次号（冗余）',
  `sample_no` varchar(64) NOT NULL COMMENT '盲样编号',
  `sample_seq` tinyint NOT NULL COMMENT '第几个样品',
  `item_id` bigint COMMENT '检验项目ID',
  `item_code` varchar(32) COMMENT '检验项目编码',
  `item_name` varchar(200) NOT NULL COMMENT '检验项目名称',
  `instrument_name` varchar(100) NOT NULL DEFAULT '' COMMENT '检测仪器',
  `method_name` varchar(100) COMMENT '检测方法学',
  `receive_date` date COMMENT '盲样接收日期',
  `receive_by` varchar(64) COMMENT '盲样接收人',
  `test_value` decimal(18,4) COMMENT '本室测定值',
  `test_by` varchar(64) COMMENT '检测人',
  `test_time` datetime COMMENT '检测时间',
  `overdue_flag` tinyint DEFAULT 0 COMMENT '是否逾期上报',
  `target_value` decimal(18,4) COMMENT '回报靶值/组均值',
  `group_sd` decimal(18,4) COMMENT '回报组标准差',
  `tea` decimal(8,2) COMMENT '允许总误差 TEa（%）',
  `target_min` decimal(18,4) COMMENT '可接受范围下限',
  `target_max` decimal(18,4) COMMENT '可接受范围上限',
  `sdi` decimal(10,3) COMMENT '标准差指数（SDI）',
  `bias_rate` decimal(10,2) COMMENT '偏倚（%）',
  `judge_mode` tinyint DEFAULT 0 COMMENT '判定口径（0-无法判定 1-SDI 2-允许总误差 3-可接受范围）',
  `result_status` tinyint DEFAULT 0 COMMENT '判定结果（0-未判定 1-满意 2-尚可 3-不合格）',
  `status` tinyint DEFAULT 0 COMMENT '流转状态（0-待检测 1-已检测 2-已上报 3-已回报）',
  `handle_status` tinyint DEFAULT 0 COMMENT '整改状态（0-无需整改 1-待整改 2-已整改）',
  `handle_cause` varchar(500) COMMENT '不合格原因分析',
  `handle_measure` varchar(500) COMMENT '纠正措施',
  `handle_by` varchar(64) COMMENT '整改人',
  `handle_time` datetime COMMENT '整改时间',
  `review_by` varchar(64) COMMENT '整改复核人',
  `review_time` datetime COMMENT '复核时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sample` (`plan_id`, `sample_seq`, `item_code`, `instrument_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='室间质评盲样';

-- biz_lis_qc_plan  室内质控计划
CREATE TABLE `biz_lis_qc_plan` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `plan_no` varchar(32) NOT NULL COMMENT '质控计划编号',
  `item_id` bigint COMMENT '检验项目ID',
  `item_code` varchar(32) COMMENT '检验项目编码',
  `item_name` varchar(200) NOT NULL COMMENT '检验项目名称',
  `instrument_no` varchar(50) COMMENT '仪器编号',
  `instrument_name` varchar(100) COMMENT '仪器名称',
  `qc_level` tinyint DEFAULT 2 COMMENT '质控水平（1-低值 2-中值 3-高值）',
  `control_name` varchar(200) COMMENT '质控品名称',
  `control_lot_no` varchar(50) COMMENT '质控品批号',
  `manufacturer` varchar(100) COMMENT '生产厂家',
  `mean_value` decimal(18,4) COMMENT '靶值（均值）',
  `sd_value` decimal(18,4) COMMENT '标准差 SD',
  `cv_limit` decimal(8,2) COMMENT '允许 CV 上限（%）',
  `expire_date` date COMMENT '质控品效期',
  `status` tinyint DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_no` (`plan_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='室内质控计划';

-- biz_lis_qc_record  室内质控记录
CREATE TABLE `biz_lis_qc_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `plan_id` bigint NOT NULL COMMENT '质控计划ID',
  `plan_no` varchar(32) COMMENT '质控计划编号',
  `item_code` varchar(32) COMMENT '检验项目编码',
  `item_name` varchar(200) COMMENT '检验项目名称',
  `instrument_name` varchar(100) COMMENT '仪器名称',
  `qc_level` tinyint COMMENT '质控水平（1-低值 2-中值 3-高值）',
  `qc_date` date COMMENT '质控日期',
  `qc_time` datetime COMMENT '质控时间',
  `result_value` decimal(18,4) COMMENT '质控测定值',
  `z_score` decimal(10,3) COMMENT 'Z 值',
  `status` tinyint DEFAULT 1 COMMENT '质控状态（1-在控 2-警告 3-失控）',
  `violated_rules` varchar(100) COMMENT '命中的 Westgard 规则',
  `operator` varchar(64) COMMENT '操作人',
  `handle_status` tinyint DEFAULT 0 COMMENT '失控处理（0-无需处理 1-待处理 2-已处理）',
  `handle_cause` varchar(500) COMMENT '失控原因分析',
  `handle_measure` varchar(500) COMMENT '纠正措施',
  `handle_by` varchar(64) COMMENT '处理人',
  `handle_time` datetime COMMENT '处理时间',
  `review_by` varchar(64) COMMENT '复核人',
  `review_time` datetime COMMENT '复核时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='室内质控记录';

-- biz_meal_order  住院订餐配送
CREATE TABLE `biz_meal_order` (
  `id` bigint NOT NULL COMMENT '主键',
  `meal_no` varchar(32) NOT NULL COMMENT '订餐单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(100) COMMENT '病区名称',
  `bed_no` varchar(20) COMMENT '床号',
  `diet_plan_id` bigint COMMENT '来源膳食方案ID',
  `diet_code` varchar(32) COMMENT '饮食类型码',
  `diet_name` varchar(100) COMMENT '饮食名称',
  `meal_date` date NOT NULL COMMENT '就餐日期',
  `meal_type` tinyint NOT NULL COMMENT '餐次（1-早餐 2-午餐 3-晚餐 4-加餐）',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '份数',
  `dish_content` varchar(200) COMMENT '配餐内容/食谱',
  `deliver_status` tinyint NOT NULL DEFAULT 0 COMMENT '配餐状态（0-待配餐 1-已配餐 2-已配送 3-已签收 4-已取消）',
  `prepare_time` datetime COMMENT '配餐完成时间',
  `deliver_time` datetime COMMENT '配送出仓时间',
  `deliver_by_id` bigint COMMENT '配送人（员工ID）',
  `deliver_by_name` varchar(50) COMMENT '配送人姓名',
  `sign_time` datetime COMMENT '签收时间',
  `sign_by` varchar(50) COMMENT '签收人（患者/家属/护士姓名）',
  `cancel_time` datetime COMMENT '退订时间',
  `cancel_reason` varchar(500) COMMENT '退订原因（停餐/出院/拒餐/转科等，必填）',
  `source` tinyint NOT NULL DEFAULT 1 COMMENT '来源（1-按膳食方案批量生成 2-手工加订）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meal_no` (`meal_no`),
  UNIQUE KEY `uk_meal_order` (`admission_id`, `meal_date`, `meal_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='住院订餐配送';

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

-- biz_medical_record_log  门诊病历修改日志
CREATE TABLE `biz_medical_record_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_id` bigint NOT NULL COMMENT '病历ID',
  `record_no` varchar(50) COMMENT '病历号',
  `user_id` bigint COMMENT '操作人ID',
  `user_name` varchar(64) COMMENT '操作人姓名',
  `operation` varchar(50) COMMENT '操作类型',
  `field_name` varchar(100) COMMENT '修改字段',
  `old_value` text COMMENT '修改前值',
  `new_value` text COMMENT '修改后值',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊病历修改日志';

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

-- biz_medicaltech_execution  医技执行记录
CREATE TABLE `biz_medicaltech_execution` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `execution_no` varchar(32) NOT NULL COMMENT '执行单号',
  `apply_type` tinyint NOT NULL COMMENT '申请类型（1-检查 2-检验）',
  `apply_id` bigint NOT NULL COMMENT '申请单ID',
  `apply_no` varchar(32) COMMENT '申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `item_id` bigint COMMENT '项目ID',
  `item_code` varchar(32) COMMENT '项目编码',
  `item_name` varchar(100) NOT NULL COMMENT '项目名称',
  `execution_status` tinyint NOT NULL DEFAULT 1 COMMENT '执行状态（1-待执行 2-执行中 3-已完成 4-已审核）',
  `executor_id` bigint COMMENT '执行人ID',
  `executor_name` varchar(50) COMMENT '执行人姓名',
  `execute_time` datetime COMMENT '执行时间',
  `complete_time` datetime COMMENT '完成时间',
  `reviewer_id` bigint COMMENT '审核人ID',
  `reviewer_name` varchar(50) COMMENT '审核人姓名',
  `review_time` datetime COMMENT '审核时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_execution_no` (`execution_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医技执行记录';

-- biz_narcotic_register  麻精药品专册
CREATE TABLE `biz_narcotic_register` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `register_no` varchar(32) NOT NULL COMMENT '专册登记号',
  `prescription_id` bigint COMMENT '处方ID',
  `prescription_no` varchar(32) COMMENT '处方号',
  `dispensing_id` bigint COMMENT '发药记录ID',
  `dispensing_no` varchar(32) COMMENT '发药单号',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `id_card` varchar(32) COMMENT '身份证号',
  `dept_id` bigint COMMENT '开方科室ID',
  `dept_name` varchar(100) COMMENT '开方科室名称',
  `diagnosis` varchar(500) COMMENT '临床诊断',
  `drug_id` bigint COMMENT '药品ID',
  `drug_code` varchar(32) COMMENT '药品编码',
  `drug_name` varchar(200) COMMENT '药品名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `special_flag` tinyint NOT NULL COMMENT '特殊管理分类（1-麻醉 2-第一类精神 3-第二类精神 4-毒性）',
  `dosage_form` varchar(50) COMMENT '剂型（用于判定限量档位：注射剂/控缓释/其他）',
  `quantity` decimal(10,2) NOT NULL COMMENT '发药数量',
  `batch_no` varchar(200) COMMENT '批号',
  `duration` int COMMENT '核定的处方天数',
  `limit_days` int COMMENT '规则允许的最大天数',
  `daily_dosage` decimal(10,2) COMMENT '核定日用量',
  `doctor_id` bigint COMMENT '开方医师ID',
  `doctor_name` varchar(50) COMMENT '开方医师姓名',
  `audit_by` varchar(64) COMMENT '审方药师',
  `dispense_by_id` bigint COMMENT '发药人ID',
  `dispense_by` varchar(50) COMMENT '发药人姓名',
  `dispense_time` datetime COMMENT '发药时间',
  `checker_id` bigint COMMENT '复核人ID',
  `checker_name` varchar(50) COMMENT '复核人姓名',
  `check_time` datetime COMMENT '复核时间',
  `ampoule_status` tinyint NOT NULL DEFAULT 0 COMMENT '空安瓿回收状态（0-不适用 1-待回收 2-已回收）',
  `ampoule_issued` decimal(10,2) COMMENT '发出安瓿数',
  `ampoule_returned` decimal(10,2) COMMENT '回收空安瓿数',
  `ampoule_destroyed` decimal(10,2) COMMENT '剩余液销毁量',
  `return_by` varchar(50) COMMENT '回收登记人',
  `return_time` datetime COMMENT '回收登记时间',
  `return_remark` varchar(500) COMMENT '回收/销毁说明',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_narco_register_no` (`register_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='麻精药品专册';

-- biz_nurse_schedule  病区护理排班
CREATE TABLE `biz_nurse_schedule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(128) COMMENT '科室名称',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `week_day` tinyint NOT NULL COMMENT '星期（1-周一 7-周日）',
  `employee_id` bigint NOT NULL COMMENT '护士ID',
  `emp_code` varchar(32) COMMENT '工号',
  `nurse_name` varchar(50) COMMENT '护士姓名',
  `nurse_title` varchar(50) COMMENT '职称',
  `shift_id` bigint COMMENT '班次ID',
  `shift_name` varchar(50) COMMENT '班次名称',
  `start_time` varchar(10) COMMENT '开始时间 HH',
  `end_time` varchar(10) COMMENT '结束时间 HH',
  `work_minutes` int NOT NULL DEFAULT 0 COMMENT '工时',
  `schedule_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-上班 2-休息 3-请假 4-培训 5-停班）',
  `schedule_source` tinyint NOT NULL DEFAULT 1 COMMENT '来源（1-手工 2-复制上周）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nurse_date` (`employee_id`, `schedule_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病区护理排班';

-- biz_nurse_schedule_rule  护理人力配置标准
CREATE TABLE `biz_nurse_schedule_rule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称',
  `shift_id` bigint NOT NULL DEFAULT 0 COMMENT '班次ID',
  `shift_name` varchar(50) COMMENT '班次名称',
  `min_staff` tinyint NOT NULL DEFAULT 0 COMMENT '最低在岗人数',
  `max_staff` tinyint NOT NULL DEFAULT 0 COMMENT '最高在岗人数',
  `max_week_hours` decimal(5,1) COMMENT '单周工时上限',
  `max_consecutive_night_days` tinyint COMMENT '连续夜班天数上限',
  `max_consecutive_work_days` tinyint COMMENT '连续上班天数上限',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ward_shift_rule` (`ward_id`, `shift_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理人力配置标准';

-- biz_nursing_assessment  护理评估单
CREATE TABLE `biz_nursing_assessment` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `assess_no` varchar(32) NOT NULL COMMENT '评估单号 AS+yyyyMMdd+4位',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称',
  `bed_no` varchar(32) COMMENT '床号',
  `assess_type` tinyint NOT NULL COMMENT '评估类型（1-压疮Braden 2-跌倒Morse 3-疼痛NRS）',
  `total_score` int NOT NULL COMMENT '总分',
  `risk_level` tinyint NOT NULL COMMENT '风险等级（1-低风险 2-中风险 3-高风险 4-极高风险）',
  `items_json` text COMMENT '评分明细 JSON',
  `assess_time` datetime NOT NULL COMMENT '评估时间',
  `assess_nurse_id` bigint COMMENT '评估护士ID（员工ID）',
  `assess_nurse_name` varchar(64) COMMENT '评估护士姓名',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assess_no` (`assess_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理评估单';

-- biz_nursing_qc_check  护理质量检查单
CREATE TABLE `biz_nursing_qc_check` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `check_no` varchar(32) NOT NULL COMMENT '检查单号 QC+yyyyMM+病区序号+类别',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(128) COMMENT '科室名称',
  `check_month` char(7) NOT NULL COMMENT '检查月份 yyyy-MM',
  `check_date` date NOT NULL COMMENT '现场检查日期',
  `category` tinyint NOT NULL COMMENT '检查类别',
  `inspector_id` bigint COMMENT '检查人员工ID',
  `inspector_name` varchar(50) COMMENT '检查人姓名',
  `sample_count` int NOT NULL DEFAULT 0 COMMENT '抽查总例数',
  `qualified_count` int NOT NULL DEFAULT 0 COMMENT '合格总例数',
  `qualified_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '合格率%=合格例数/抽查例数*100',
  `full_score` decimal(7,1) NOT NULL DEFAULT 0.0 COMMENT '应得分',
  `total_score` decimal(7,1) NOT NULL DEFAULT 0.0 COMMENT '实得分',
  `score_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '得分率%=实得分/应得分*100',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-草稿 2-已确认）',
  `summary` varchar(500) COMMENT '本轮小结',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_ward_month_cat` (`ward_id`, `check_month`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理质量检查单';

-- biz_nursing_qc_check_item  护理质量检查明细
CREATE TABLE `biz_nursing_qc_check_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `check_id` bigint NOT NULL COMMENT '检查单ID',
  `item_id` bigint NOT NULL COMMENT '检查项ID',
  `item_code` varchar(32) COMMENT '项目编码',
  `item_name` varchar(128) COMMENT '项目名称',
  `category` tinyint NOT NULL COMMENT '检查类别',
  `checked_num` int NOT NULL DEFAULT 0 COMMENT '抽查例数',
  `qualified_num` int NOT NULL DEFAULT 0 COMMENT '合格例数',
  `full_score` decimal(5,1) NOT NULL DEFAULT 0.0 COMMENT '本项应得分',
  `score` decimal(5,1) NOT NULL DEFAULT 0.0 COMMENT '本项实得分=应得分*合格/抽查',
  `problem` varchar(500) COMMENT '存在问题',
  `cause_analysis` varchar(500) COMMENT '原因分析',
  `rectify_measure` varchar(500) COMMENT '整改措施',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_item` (`check_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理质量检查明细';

-- biz_nursing_qc_indicator  护理质控指标台账
CREATE TABLE `biz_nursing_qc_indicator` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(128) COMMENT '科室名称',
  `stat_month` char(7) NOT NULL COMMENT '统计月份 yyyy-MM',
  `indicator_code` varchar(32) NOT NULL COMMENT '指标编码',
  `indicator_name` varchar(64) NOT NULL COMMENT '指标名称',
  `unit` varchar(16) NOT NULL COMMENT '单位',
  `numerator` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '分子',
  `denominator` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '分母',
  `rate_value` decimal(10,4) COMMENT '指标值',
  `target_value` decimal(10,4) COMMENT '目标值',
  `reached_flag` tinyint COMMENT '是否达标（1-达标 0-未达标）',
  `source_type` tinyint NOT NULL COMMENT '事实来源（1-检查表 2-不良事件+住院事实）',
  `report_status` tinyint NOT NULL DEFAULT 1 COMMENT '上报状态（1-未上报 2-已上报）',
  `calc_time` datetime COMMENT '最近一次重算时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_indicator` (`ward_id`, `stat_month`, `indicator_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理质控指标台账';

-- biz_nursing_record  护理文书
CREATE TABLE `biz_nursing_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '护理文书号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(64) COMMENT '科室名称',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称',
  `bed_no` varchar(32) COMMENT '床号',
  `nursing_type` tinyint NOT NULL COMMENT '文书类型（1-三测单 2-护理记录单 3-生命体征监测）',
  `measure_time` datetime NOT NULL COMMENT '测量/记录时间',
  `shift` tinyint COMMENT '班次（1-白班 2-小夜班 3-大夜班）',
  `temperature` decimal(4,1) COMMENT '体温（℃）',
  `pulse` int COMMENT '脉搏（次/分）',
  `respiration` int COMMENT '呼吸（次/分）',
  `systolic_pressure` int COMMENT '收缩压（mmHg）',
  `diastolic_pressure` int COMMENT '舒张压（mmHg）',
  `spo2` int COMMENT '血氧饱和度（%）',
  `stool_count` int COMMENT '大便次数（次/日）',
  `urine_volume` int COMMENT '尿量（ml）',
  `intake_volume` int COMMENT '入量（ml）',
  `output_volume` int COMMENT '出量（ml）',
  `nursing_level` tinyint COMMENT '护理级别（1-特级护理 2-一级护理 3-二级护理 4-三级护理）',
  `nursing_content` text COMMENT '护理措施与病情观察记录正文',
  `nurse_id` bigint COMMENT '记录护士ID（员工ID）',
  `nurse_name` varchar(64) COMMENT '记录护士姓名',
  `record_status` tinyint NOT NULL DEFAULT 1 COMMENT '文书状态（1-草稿 2-已提交 3-已归档）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nr_admission_type_time` (`admission_id`, `nursing_type`, `measure_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理文书';

-- biz_nutrition_screen  营养风险筛查记录
CREATE TABLE `biz_nutrition_screen` (
  `id` bigint NOT NULL COMMENT '主键',
  `screen_no` varchar(32) NOT NULL COMMENT '筛查编号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(100) COMMENT '病区名称',
  `bed_no` varchar(20) COMMENT '床号',
  `screen_type` tinyint NOT NULL DEFAULT 1 COMMENT '量表（1-NRS2002 2-PG-SGA 3-MNA）',
  `impair_score` tinyint COMMENT 'NRS2002 营养状态受损评分 0~3（1-体重下降 2-GI手术 3-骨髓移植等）',
  `severity_score` tinyint COMMENT 'NRS2002 疾病严重程度评分 0~3（1-髋骨骨折 2-腹部大手术 3-颅脑损伤）',
  `age_score` tinyint COMMENT 'NRS2002 年龄评分',
  `height_cm` decimal(5,1) COMMENT '身高 cm',
  `weight_kg` decimal(6,2) COMMENT '体重 kg',
  `bmi` decimal(5,2) COMMENT 'BMI',
  `weight_loss_percent` decimal(5,1) COMMENT '近 3 个月体重下降百分比（%）',
  `total_score` int NOT NULL DEFAULT 0 COMMENT '量表总分',
  `risk_flag` tinyint NOT NULL DEFAULT 0 COMMENT '营养风险',
  `screen_source` tinyint NOT NULL DEFAULT 1 COMMENT '筛查时机（1-入院48小时内 2-病情变化复筛 3-术后复筛 4-定期复筛）',
  `next_screen_date` date COMMENT '下次筛查日期',
  `items_json` text COMMENT '分项明细 JSON',
  `screen_time` datetime NOT NULL COMMENT '筛查时间',
  `screener_id` bigint COMMENT '筛查人（员工ID）',
  `screener_name` varchar(50) COMMENT '筛查人姓名',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nutrition_screen_no` (`screen_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营养风险筛查记录';

-- biz_nutrition_stats  营养膳食月度指标
CREATE TABLE `biz_nutrition_stats` (
  `id` bigint NOT NULL COMMENT '主键',
  `stat_month` char(7) NOT NULL COMMENT '统计月份',
  `scope_type` tinyint NOT NULL DEFAULT 1 COMMENT '统计范围（1-全院 2-科室）',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `discharge_count` int NOT NULL DEFAULT 0 COMMENT '同期出院患者数',
  `screened_count` int NOT NULL DEFAULT 0 COMMENT '其中出院前做过 NRS2002 筛查的患者数',
  `screen_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '营养风险筛查率（%）',
  `risk_count` int NOT NULL DEFAULT 0 COMMENT '筛查阳性',
  `risk_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '筛查阳性率（%）',
  `diet_plan_count` int NOT NULL DEFAULT 0 COMMENT '膳食方案总数',
  `diet_confirm_count` int NOT NULL DEFAULT 0 COMMENT '其中营养科已接收',
  `diet_confirm_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '膳食医嘱执行率（%）',
  `consult_count` int NOT NULL DEFAULT 0 COMMENT '营养会诊单数',
  `consult_ontime_count` int NOT NULL DEFAULT 0 COMMENT '其中按时应答的条数',
  `consult_ontime_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '营养会诊及时应答率（%）',
  `meal_order_count` int NOT NULL DEFAULT 0 COMMENT '订餐明细数',
  `meal_signed_count` int NOT NULL DEFAULT 0 COMMENT '其中已签收的明细数',
  `meal_sign_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '订餐签收率（%）',
  `meal_cancel_count` int NOT NULL DEFAULT 0 COMMENT '退订明细数',
  `generate_by` varchar(64) COMMENT '生成人',
  `generate_time` datetime COMMENT '生成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nutrition_stats` (`stat_month`, `scope_type`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营养膳食月度指标';

-- biz_online_consult  线上问诊
CREATE TABLE `biz_online_consult` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `consult_no` varchar(32) NOT NULL COMMENT '问诊单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '接诊科室ID',
  `dept_name` varchar(128) COMMENT '接诊科室名称',
  `doctor_id` bigint COMMENT '接诊医生ID（员工ID）',
  `doctor_name` varchar(64) COMMENT '接诊医生姓名',
  `consult_type` tinyint NOT NULL DEFAULT 1 COMMENT '问诊方式（1-图文问诊 2-电话问诊 3-视频问诊）',
  `chief_complaint` varchar(1000) NOT NULL COMMENT '主诉/问题描述',
  `reply` varchar(1000) COMMENT '医生回复',
  `advice` varchar(500) COMMENT '处置建议',
  `need_visit` tinyint NOT NULL DEFAULT 0 COMMENT '是否建议线下就诊（0-否 1-是）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待接诊 2-接诊中 3-已完成 4-已退诊）',
  `fee` decimal(10,2) COMMENT '问诊费用',
  `apply_time` datetime COMMENT '发起时间',
  `accept_by` varchar(64) COMMENT '接诊人',
  `accept_time` datetime COMMENT '接诊时间',
  `finish_by` varchar(64) COMMENT '完成人',
  `finish_time` datetime COMMENT '完成时间',
  `reject_reason` varchar(500) COMMENT '退诊原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_online_no` (`consult_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线上问诊';

-- biz_operation_apply  手术申请单
CREATE TABLE `biz_operation_apply` (
  `id` bigint NOT NULL COMMENT '手术申请单ID（雪花）',
  `apply_no` varchar(32) NOT NULL COMMENT '手术申请单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `admission_no` varchar(32) COMMENT '入院号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(64) COMMENT '申请科室名称',
  `apply_ward_name` varchar(64) COMMENT '申请时所在病区名称',
  `apply_bed_no` varchar(32) COMMENT '申请时床号',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(64) COMMENT '申请医生姓名',
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
  `surgeon_name` varchar(64) COMMENT '主刀医师姓名',
  `assistant_name` varchar(200) COMMENT '助手姓名',
  `anesthetist_id` bigint COMMENT '麻醉医师ID（员工ID）',
  `anesthetist_name` varchar(64) COMMENT '麻醉医师姓名',
  `schedule_doctor_id` bigint COMMENT '排台操作人ID（员工ID）',
  `schedule_doctor_name` varchar(64) COMMENT '排台操作人姓名',
  `schedule_time` datetime COMMENT '排台时间',
  `schedule_remark` varchar(500) COMMENT '排台备注',
  `preop_check_items` varchar(200) COMMENT '术前核对要点码',
  `preop_note` varchar(1000) COMMENT '术前核对补充说明',
  `preop_check_doctor_id` bigint COMMENT '术前核对人ID（员工ID）',
  `preop_check_doctor_name` varchar(64) COMMENT '术前核对人姓名',
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
  `finish_doctor_name` varchar(64) COMMENT '完成录入人姓名',
  `finish_time` datetime COMMENT '手术完成时间',
  `operation_id` bigint COMMENT '回写病案首页手术明细ID',
  `record_id` bigint COMMENT '回写住院病历ID',
  `operation_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-待排期 1-已排期 2-术前核对完成 3-已完成 4-已取消）',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `cancel_doctor_id` bigint COMMENT '取消人ID（员工ID）',
  `cancel_doctor_name` varchar(64) COMMENT '取消人姓名',
  `cancel_time` datetime COMMENT '取消时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术申请单';

-- biz_operation_charge_item  手术麻醉计费明细
CREATE TABLE `biz_operation_charge_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `source_type` tinyint NOT NULL COMMENT '收费来源（预留）（1-麻醉记录 2-PACU复苏 3-手术）',
  `source_id` bigint NOT NULL COMMENT '来源单据ID',
  `source_no` varchar(32) COMMENT '来源单据号',
  `item_code` varchar(32) NOT NULL COMMENT '收费项目编码',
  `item_name` varchar(200) COMMENT '收费项目名称',
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

-- biz_operation_count  手术清点主单
CREATE TABLE `biz_operation_count` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `count_no` varchar(32) NOT NULL COMMENT '清点单号',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `operation_room` varchar(64) COMMENT '手术间',
  `planned_operation_name` varchar(200) COMMENT '手术名称',
  `instrument_nurse_id` bigint COMMENT '器械（洗手）',
  `instrument_nurse_name` varchar(64) COMMENT '器械护士姓名',
  `circulate_nurse_id` bigint COMMENT '巡回护士ID（员工ID）',
  `circulate_nurse_name` varchar(64) COMMENT '巡回护士姓名',
  `before_nurse_id` bigint COMMENT '术前清点核对人ID',
  `before_nurse_name` varchar(64) COMMENT '术前清点核对人姓名',
  `before_time` datetime COMMENT '术前清点时间',
  `before_result` tinyint COMMENT '术前清点结果（1-一致 2-不一致）',
  `closure_nurse_id` bigint COMMENT '关体前清点核对人ID（员工ID）',
  `closure_nurse_name` varchar(64) COMMENT '关体前核对人姓名',
  `closure_time` datetime COMMENT '关体前清点时间',
  `closure_result` tinyint COMMENT '关体前清点结果（1-一致 2-不一致）',
  `final_nurse_id` bigint COMMENT '关体后清点核对人ID（员工ID）',
  `final_nurse_name` varchar(64) COMMENT '关体后核对人姓名',
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

-- biz_operation_safety_check  手术安全核查单
CREATE TABLE `biz_operation_safety_check` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `check_no` varchar(32) NOT NULL COMMENT '核查单号',
  `apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) COMMENT '手术申请单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `operation_name` varchar(200) COMMENT '手术名称（快照，拟施）',
  `operation_room` varchar(64) COMMENT '手术间',
  `phase` tinyint NOT NULL COMMENT '核查时段（1-麻醉诱导前 2-手术开始前 3-患者离开手术室前）',
  `items` varchar(300) NOT NULL COMMENT '核查项码值',
  `note` varchar(1000) COMMENT '异常说明',
  `surgeon_id` bigint NOT NULL COMMENT '手术医师',
  `surgeon_name` varchar(64) COMMENT '手术医师姓名',
  `surgeon_sign_time` datetime COMMENT '手术医师签名时间',
  `anesthetist_id` bigint NOT NULL COMMENT '麻醉医师员工ID',
  `anesthetist_name` varchar(64) COMMENT '麻醉医师姓名',
  `anesthetist_sign_time` datetime COMMENT '麻醉医师签名时间',
  `nurse_id` bigint NOT NULL COMMENT '手术室护士（器械/巡回）',
  `nurse_name` varchar(64) COMMENT '手术室护士姓名',
  `nurse_sign_time` datetime COMMENT '手术室护士签名时间',
  `recorder_id` bigint COMMENT '录入人ID',
  `recorder_name` varchar(64) COMMENT '录入人姓名',
  `check_time` datetime COMMENT '核查完成时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_apply_phase` (`apply_id`, `phase`),
  UNIQUE KEY `uk_check_no` (`check_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术安全核查单';

-- biz_outp_infusion  门诊输液单
CREATE TABLE `biz_outp_infusion` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `infusion_no` varchar(32) NOT NULL COMMENT '输液单号',
  `treatment_record_id` bigint COMMENT '来源治疗记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别',
  `age` int COMMENT '年龄',
  `drug_summary` varchar(500) COMMENT '输注内容摘要',
  `seat_id` bigint COMMENT '座位ID',
  `seat_no` varchar(32) COMMENT '座位号',
  `skin_test_id` bigint COMMENT '皮试记录ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待皮试 2-待输注 3-输液中 4-已完成 5-已取消）',
  `start_time` datetime COMMENT '开始输注时间',
  `drip_rate` int COMMENT '起始滴速（滴/分）',
  `end_time` datetime COMMENT '结束时间',
  `adverse_flag` tinyint NOT NULL DEFAULT 0 COMMENT '不良反应（0-无 1-有）',
  `adverse_desc` varchar(500) COMMENT '不良反应描述',
  `nurse_id` bigint COMMENT '责任护士ID',
  `nurse_name` varchar(50) COMMENT '责任护士姓名',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_infusion_no` (`infusion_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊输液单';

-- biz_outp_infusion_round  门诊输液巡视记录
CREATE TABLE `biz_outp_infusion_round` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `infusion_id` bigint NOT NULL COMMENT '输液单ID',
  `round_time` datetime NOT NULL COMMENT '巡视时间',
  `drip_rate` int COMMENT '滴速（滴/分）',
  `remaining_volume` int COMMENT '余量（ml）',
  `nurse_id` bigint COMMENT '巡视护士ID',
  `nurse_name` varchar(50) COMMENT '巡视护士姓名',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊输液巡视记录';

-- biz_pathology_block  病理蜡块与切片
CREATE TABLE `biz_pathology_block` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '病理主单ID',
  `order_no` varchar(32) NOT NULL COMMENT '病理号',
  `block_no` varchar(32) NOT NULL COMMENT '蜡块号',
  `part_desc` varchar(200) COMMENT '取材部位描述',
  `block_count` int DEFAULT 1 COMMENT '蜡块数',
  `slice_count` int DEFAULT 0 COMMENT '切片数',
  `slide_no` varchar(50) COMMENT '切片号',
  `status` tinyint DEFAULT 1 COMMENT '状态（1-待取材 2-已取材 3-已包埋 4-已切片）',
  `sampling_by` varchar(64) COMMENT '取材人',
  `sampling_time` datetime COMMENT '取材时间',
  `embedding_by` varchar(64) COMMENT '包埋人',
  `embedding_time` datetime COMMENT '包埋时间',
  `slice_by` varchar(64) COMMENT '切片人',
  `slice_time` datetime COMMENT '切片时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病理蜡块与切片';

-- biz_pathology_order  病理检查主单
CREATE TABLE `biz_pathology_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '病理号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `visit_date` date COMMENT '就诊日期',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(100) COMMENT '申请科室',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(50) COMMENT '申请医生',
  `clinical_diagnosis` varchar(500) COMMENT '临床诊断',
  `exam_type` tinyint DEFAULT 1 COMMENT '病理检查类型（1-常规石蜡 2-术中冰冻 3-细胞学 4-免疫组化 5-疑难会诊）',
  `specimen_type` varchar(100) COMMENT '标本类型（活检/切除/穿刺/脱落细胞等）',
  `specimen_part` varchar(200) COMMENT '取材部位',
  `is_frozen` tinyint DEFAULT 0 COMMENT '是否冰冻（0-否 1-是）',
  `frozen_result` varchar(500) COMMENT '术中冰冻快速诊断结果',
  `receive_time` datetime COMMENT '标本接收时间',
  `receive_by` varchar(64) COMMENT '标本接收人',
  `sampling_time` datetime COMMENT '取材时间',
  `sampling_by` varchar(64) COMMENT '取材人',
  `embedding_time` datetime COMMENT '包埋时间',
  `embedding_by` varchar(64) COMMENT '包埋人',
  `slice_time` datetime COMMENT '制片（切片）',
  `slice_by` varchar(64) COMMENT '制片人',
  `gross_findings` text COMMENT '肉眼所见',
  `microscopy_findings` text COMMENT '镜下所见',
  `ihc_result` varchar(1000) COMMENT '免疫组化 / 特殊染色结果',
  `diagnosis` text COMMENT '病理诊断',
  `suggestion` varchar(1000) COMMENT '建议',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `report_by` varchar(64) COMMENT '初诊医师',
  `report_time` datetime COMMENT '初诊时间',
  `audit_by` varchar(64) COMMENT '审核医师',
  `audit_time` datetime COMMENT '审核时间',
  `publish_by` varchar(64) COMMENT '发布人',
  `publish_time` datetime COMMENT '发布时间',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病理检查主单';

-- biz_pathway  临床路径模板
CREATE TABLE `biz_pathway` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `pathway_code` varchar(32) NOT NULL COMMENT '路径编码',
  `pathway_name` varchar(128) NOT NULL COMMENT '路径名称',
  `dept_id` bigint COMMENT '适用科室ID',
  `dept_name` varchar(128) COMMENT '适用科室名称',
  `diagnosis` varchar(255) COMMENT '适用病种/诊断',
  `version` varchar(16) NOT NULL DEFAULT 'V1' COMMENT '版本号',
  `total_days` int NOT NULL DEFAULT 0 COMMENT '路径总日数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-草稿 2-使用中 3-已停用）',
  `publish_by` varchar(64) COMMENT '发布人',
  `publish_time` datetime COMMENT '发布时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临床路径模板';

-- biz_pathway_enroll  临床路径入径记录
CREATE TABLE `biz_pathway_enroll` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `enroll_no` varchar(32) NOT NULL COMMENT '入径单号',
  `pathway_id` bigint NOT NULL COMMENT '模板ID',
  `pathway_code` varchar(32) COMMENT '路径编码',
  `pathway_name` varchar(128) COMMENT '路径名称',
  `version` varchar(16) COMMENT '版本号',
  `total_days` int NOT NULL DEFAULT 0 COMMENT '路径总日数',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '入院科室ID',
  `dept_name` varchar(128) COMMENT '入院科室名称',
  `diagnosis` varchar(255) COMMENT '入院诊断',
  `enroll_date` date NOT NULL COMMENT '入径日期',
  `enroll_by` varchar(64) COMMENT '入径操作人',
  `enroll_time` datetime COMMENT '入径时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-在径 2-已完成 3-已退径）',
  `finish_date` date COMMENT '完成/退径日期',
  `finish_by` varchar(64) COMMENT '完成/退径操作人',
  `abort_reason` varchar(255) COMMENT '退径原因',
  `variance_count` int NOT NULL DEFAULT 0 COMMENT '变异次数',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enroll_no` (`enroll_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临床路径入径记录';

-- biz_pathway_step  临床路径步骤
CREATE TABLE `biz_pathway_step` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `pathway_id` bigint NOT NULL COMMENT '模板ID',
  `day_no` int NOT NULL COMMENT '路径日',
  `item_type` tinyint NOT NULL COMMENT '项目类型（1-诊疗 2-用药 3-手术操作 4-护理 5-病情评估 6-宣教）',
  `item_name` varchar(128) NOT NULL COMMENT '项目名称',
  `item_code` varchar(64) COMMENT '字典项目编码',
  `content` varchar(500) COMMENT '路径要求/具体内容',
  `sort_no` int NOT NULL DEFAULT 1 COMMENT '同日内的顺序',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临床路径步骤';

-- biz_pathway_variance  临床路径变异登记
CREATE TABLE `biz_pathway_variance` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `enroll_id` bigint NOT NULL COMMENT '入径记录ID',
  `day_no` int NOT NULL COMMENT '发生路径日',
  `variance_type` tinyint NOT NULL COMMENT '变异类型（1-医嘱变动 2-检查检验变动 3-手术操作变动 4-用药变动 5-出院延期 6-其他）',
  `variance_reason` varchar(255) NOT NULL COMMENT '变异原因',
  `handling` varchar(255) COMMENT '处理措施',
  `occurred_date` date NOT NULL COMMENT '变异发生日期',
  `recorder_id` bigint COMMENT '登记人（员工ID）',
  `recorder_name` varchar(64) COMMENT '登记人姓名',
  `record_time` datetime COMMENT '登记时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临床路径变异登记';

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

-- biz_patient_allergy  药物过敏史
CREATE TABLE `biz_patient_allergy` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `allergy_type` varchar(50) NOT NULL COMMENT '过敏类型（药物/食物/其他）',
  `allergen_name` varchar(200) NOT NULL COMMENT '过敏原名称',
  `allergy_severity` varchar(20) NOT NULL COMMENT '过敏严重程度（轻度/中度/重度/危及生命）',
  `allergy_symptoms` varchar(500) NOT NULL COMMENT '过敏反应表现',
  `allergy_date` date COMMENT '首次发生日期',
  `occurrence_count` int DEFAULT 1 COMMENT '发生次数',
  `treatment_given` varchar(200) COMMENT '过敏时处理措施',
  `confirmed_by` varchar(100) COMMENT '确认医生',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药物过敏史';

-- biz_patient_contact  患者联系方式
CREATE TABLE `biz_patient_contact` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `contact_name` varchar(100) NOT NULL COMMENT '联系人姓名',
  `relationship` tinyint NOT NULL COMMENT '与患者关系（如：父母、配偶、子女、朋友等）',
  `phone` varchar(20) COMMENT '联系电话',
  `is_primary` tinyint DEFAULT 0 COMMENT '是否主要联系人（0-否 1-是）',
  `address` varchar(255) COMMENT '联系地址',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者联系方式';

-- biz_patient_family_history  家族史
CREATE TABLE `biz_patient_family_history` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `relationship` varchar(50) NOT NULL COMMENT '与患者关系（父亲/母亲/兄弟/姐妹/祖父/祖母/子女）',
  `name` varchar(100) COMMENT '亲属姓名',
  `age` int COMMENT '年龄',
  `is_alive` tinyint DEFAULT 1 COMMENT '是否在世（0-已故 1-在世）',
  `cause_of_death` varchar(200) COMMENT '死亡原因',
  `health_status` varchar(500) COMMENT '健康状况描述',
  `hereditary_disease` varchar(200) COMMENT '遗传性疾病（如：高血压、糖尿病、肿瘤等）',
  `infectious_disease` varchar(200) COMMENT '传染病史',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家族史';

-- biz_patient_guardian  就诊人绑定
CREATE TABLE `biz_patient_guardian` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '登录账号ID',
  `patient_id` bigint NOT NULL COMMENT '就诊人ID',
  `relation` tinyint NOT NULL DEFAULT 99 COMMENT '与账号所有人关系（1-本人 2-配偶 3-父亲 99-其他）',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认就诊人（0-否 1-是）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_patient` (`user_id`, `patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊人绑定';

-- biz_patient_medication_history  既往用药史
CREATE TABLE `biz_patient_medication_history` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `drug_name` varchar(200) NOT NULL COMMENT '药物名称',
  `drug_type` varchar(50) COMMENT '药物类型（处方药/非处方药/中药/保健品）',
  `dosage` varchar(100) COMMENT '剂量',
  `frequency` varchar(100) COMMENT '频次',
  `route` varchar(50) COMMENT '给药途径（口服/注射/外用/吸入等）',
  `start_date` date NOT NULL COMMENT '开始用药日期',
  `end_date` date COMMENT '停药日期',
  `indications` varchar(200) COMMENT '用药指征/适应症',
  `prescriber` varchar(100) COMMENT '处方医生',
  `status` varchar(20) DEFAULT '已完成' COMMENT '用药状态（进行中/已停用/已换药/已减量）',
  `reason_stop` varchar(200) COMMENT '停药原因（疗效不佳/不良反应/患者要求/已治愈）',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='既往用药史';

-- biz_patient_merge_log  患者合并审计
CREATE TABLE `biz_patient_merge_log` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `merge_no` varchar(32) NOT NULL COMMENT '合并流水号',
  `master_id` bigint NOT NULL COMMENT '主档患者ID',
  `master_no` varchar(32) COMMENT '主档患者号',
  `master_name` varchar(50) COMMENT '主档姓名',
  `merged_id` bigint NOT NULL COMMENT '被并入的患者ID',
  `merged_no` varchar(32) COMMENT '被并患者号',
  `merged_name` varchar(50) COMMENT '被并姓名',
  `match_type` tinyint NOT NULL COMMENT '匹配置信级别(强)（1-身份证号相同 2-姓名+性别+出生日期相同 3-姓名+手机号相同 4-人工判定）',
  `match_snapshot` varchar(500) COMMENT '命中依据的字段值快照',
  `master_snapshot` varchar(1000) COMMENT '主档关键字段快照 JSON',
  `merged_snapshot` varchar(1000) COMMENT '被并档关键字段快照 JSON',
  `data_count` varchar(500) COMMENT '合并时两档各自关联业务数据量快照 JSON',
  `reason` varchar(500) NOT NULL COMMENT '合并理由',
  `operator_id` bigint COMMENT '操作人ID',
  `operator_name` varchar(64) COMMENT '操作人姓名',
  `merge_time` datetime NOT NULL COMMENT '合并时间',
  `log_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已合并 2-已撤销）',
  `revert_by` varchar(64) COMMENT '撤销人',
  `revert_time` datetime COMMENT '撤销时间',
  `revert_reason` varchar(500) COMMENT '撤销理由',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pml_no` (`merge_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者合并审计';

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

-- biz_patient_surgery_history  手术外伤史
CREATE TABLE `biz_patient_surgery_history` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `surgery_name` varchar(200) NOT NULL COMMENT '手术名称',
  `surgery_date` date NOT NULL COMMENT '手术日期',
  `surgery_type` varchar(50) COMMENT '手术类型（择期/紧急/急诊）',
  `surgeon` varchar(100) COMMENT '主刀医生',
  `anesthesia_type` varchar(100) COMMENT '麻醉方式',
  `hospital_name` varchar(200) COMMENT '手术医院',
  `postop_diagnosis` varchar(200) COMMENT '术后诊断',
  `recovery_status` varchar(50) COMMENT '恢复情况（良好/一般/差/死亡）',
  `complications` text COMMENT '术后并发症',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术外伤史';

-- biz_patient_tag_relation  患者标签关联
CREATE TABLE `biz_patient_tag_relation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `source_type` tinyint DEFAULT 1 COMMENT '标签来源（1-手动打标 2-系统自动打标）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '打标时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_tag` (`patient_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者标签关联';

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

-- biz_perf_result  科室绩效核算结果
CREATE TABLE `biz_perf_result` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `cost_month` char(7) NOT NULL COMMENT '核算月份',
  `revenue` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '科室收入',
  `drug_revenue` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '药品收入',
  `drug_ratio` decimal(6,4) COMMENT '药占比',
  `total_cost` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '成本合计',
  `surplus` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '结余 = 收入 - 成本',
  `bonus_rate` decimal(6,4) NOT NULL DEFAULT 0.0600 COMMENT '提成系数',
  `perf_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '绩效金额 = max',
  `perf_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-草稿 2-已核算 3-已发布）',
  `cost_id` bigint COMMENT '成本快照来源',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_month` (`dept_id`, `cost_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室绩效核算结果';

-- biz_pivas_batch  静配中心主单
CREATE TABLE `biz_pivas_batch` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `pivas_no` varchar(32) NOT NULL COMMENT '静配单号',
  `admix_date` date NOT NULL COMMENT '调配日期',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称',
  `dept_id` bigint COMMENT '入院科室ID',
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
  UNIQUE KEY `uk_adm_date` (`admission_id`, `admix_date`),
  UNIQUE KEY `uk_pivas_no` (`pivas_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='静配中心主单';

-- biz_pivas_item  静配中心调配明细
CREATE TABLE `biz_pivas_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `pivas_id` bigint NOT NULL COMMENT '主单ID',
  `pivas_no` varchar(32) COMMENT '静配单号（冗余）',
  `admix_date` date NOT NULL COMMENT '调配日期',
  `pivas_seq` int NOT NULL DEFAULT 1 COMMENT '重生成序号',
  `order_id` bigint NOT NULL COMMENT '住院医嘱ID',
  `order_no` varchar(32) COMMENT '医嘱号',
  `admission_id` bigint NOT NULL COMMENT '入院ID（冗余）',
  `patient_id` bigint NOT NULL COMMENT '患者ID（冗余）',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `ward_id` bigint COMMENT '病区ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_name` varchar(128) COMMENT '药品名称',
  `item_code` varchar(64) COMMENT '医嘱项目编码',
  `item_name` varchar(128) COMMENT '医嘱项目名称',
  `spec` varchar(64) COMMENT '规格',
  `unit` varchar(32) COMMENT '单位',
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

-- biz_prescription_audit_log  处方审方流水
CREATE TABLE `biz_prescription_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `prescription_id` bigint COMMENT '处方 id',
  `prescription_no` varchar(40) NOT NULL COMMENT '处方号',
  `record_id` bigint COMMENT '病历 id',
  `regist_id` bigint COMMENT '挂号 id',
  `round_no` int NOT NULL DEFAULT 1 COMMENT '第几轮',
  `action` tinyint NOT NULL COMMENT '动作',
  `auditor_id` bigint COMMENT '操作人员工 id',
  `auditor_name` varchar(64) COMMENT '操作人姓名',
  `opinion` varchar(500) COMMENT '审方意见/退回原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方审方流水';

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

-- biz_quality_control  质控检查记录
CREATE TABLE `biz_quality_control` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `qc_no` varchar(32) NOT NULL COMMENT '质控编号',
  `record_id` bigint NOT NULL COMMENT '病历ID',
  `record_source` varchar(16) NOT NULL DEFAULT 'OUTPATIENT' COMMENT '质控对象来源',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `qc_type` tinyint NOT NULL COMMENT '质控类型（0-综合 1-完整性检查 2-规范性检查 3-逻辑性检查 4-AI内涵质控）',
  `qc_content` varchar(500) NOT NULL COMMENT '检查内容',
  `qc_result` tinyint NOT NULL COMMENT '检查结果（0-不通过 1-通过）',
  `error_count` int DEFAULT 0 COMMENT '错误数量',
  `error_detail` varchar(1000) COMMENT '错误详情',
  `score` int COMMENT '质控得分',
  `severity_max` tinyint COMMENT '最高问题严重度（0-无问题 1-提示 2-重要 3-否决项）',
  `qc_status` tinyint NOT NULL DEFAULT 1 COMMENT '质控状态（1-待处理 2-已处理 3-已忽略）',
  `qc_by` varchar(64) COMMENT '质控人',
  `qc_time` datetime COMMENT '质控时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qc_no` (`qc_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质控检查记录';

-- biz_quality_control_issue  质控问题明细
CREATE TABLE `biz_quality_control_issue` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `qc_id` bigint NOT NULL COMMENT '质控单ID',
  `qc_no` varchar(32) NOT NULL COMMENT '质控单号',
  `record_source` varchar(16) NOT NULL COMMENT '病历来源',
  `record_id` bigint NOT NULL COMMENT '病历ID',
  `patient_id` bigint COMMENT '患者ID',
  `rule_code` varchar(16) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(64) NOT NULL COMMENT '规则名称',
  `dimension` tinyint NOT NULL COMMENT '维度（1-完整性 2-规范性 3-逻辑性）',
  `severity` tinyint NOT NULL COMMENT '严重度（1-提示 2-重要 3-否决）',
  `deduct` int NOT NULL DEFAULT 0 COMMENT '扣分',
  `field_name` varchar(64) COMMENT '问题字段',
  `error_detail` varchar(500) COMMENT '问题描述',
  `suggestion` varchar(500) COMMENT '整改建议',
  `evidence` varchar(200) COMMENT '病历原文证据（截断）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质控问题明细';

-- biz_queue  候诊队列
CREATE TABLE `biz_queue` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `queue_no` varchar(20) NOT NULL COMMENT '排队序号',
  `regist_id` bigint NOT NULL COMMENT '挂号ID',
  `visit_date` date COMMENT '就诊日期',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `doctor_id` bigint COMMENT '医生ID',
  `doctor_name` varchar(50) COMMENT '医生姓名',
  `queue_type` tinyint DEFAULT 1 COMMENT '队列类型（1-普通队列 2-优先队列 3-过号队列）',
  `regist_type` tinyint DEFAULT 1 COMMENT '挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）',
  `queue_status` tinyint DEFAULT 2 COMMENT '排队状态（2-候诊中 3-就诊中 4-已就诊 5-已退号 6-已过号 7-已失效）',
  `triage_status` tinyint NOT NULL DEFAULT 0 COMMENT '分诊状态（0-未分诊 1-已分诊）',
  `triage_level` tinyint COMMENT '分诊等级（1-危重 2-急症 3-亚急 4-非急）',
  `room_id` bigint COMMENT '诊室ID',
  `room_name` varchar(64) COMMENT '诊室名称',
  `sequence_no` int NOT NULL COMMENT '顺序号',
  `call_time` datetime COMMENT '叫号时间',
  `call_count` int DEFAULT 0 COMMENT '叫号次数',
  `arrive_time` datetime COMMENT '到达时间',
  `start_time` datetime COMMENT '开始就诊时间',
  `end_time` datetime COMMENT '结束就诊时间',
  `wait_duration` int DEFAULT 0 COMMENT '等待时长（分钟）',
  `is_overdue` tinyint DEFAULT 0 COMMENT '是否过号（0-否 1-是）',
  `overdue_time` datetime COMMENT '过号时间',
  `overdue_reason` varchar(200) COMMENT '过号原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候诊队列';

-- biz_radio_report_template  放射报告模板
CREATE TABLE `biz_radio_report_template` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_code` varchar(32) NOT NULL COMMENT '模板编码',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `modality` tinyint COMMENT '适用模态',
  `item_code` varchar(32) COMMENT '适用检查项目编码',
  `item_name` varchar(200) COMMENT '适用检查项目名称',
  `body_part` varchar(100) COMMENT '适用检查部位',
  `exam_method` varchar(200) COMMENT '检查方法模板',
  `finding_tpl` text COMMENT '影像所见模板',
  `impression_tpl` text COMMENT '影像诊断/印象模板',
  `suggestion_tpl` text COMMENT '建议模板',
  `is_public` tinyint NOT NULL DEFAULT 1 COMMENT '是否公用（1-科室公用 0-个人模板）',
  `doctor_id` bigint COMMENT '归属医生',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='放射报告模板';

-- biz_record_qc_flow  病历三级质控流转单
CREATE TABLE `biz_record_qc_flow` (
  `id` bigint NOT NULL COMMENT '主键',
  `flow_no` varchar(32) NOT NULL COMMENT '流转单号',
  `record_id` bigint NOT NULL COMMENT '病历ID',
  `record_source` varchar(20) NOT NULL DEFAULT 'OUTPATIENT' COMMENT '病历来源',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(64) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '病历所属科室ID',
  `dept_name` varchar(64) COMMENT '病历所属科室名称',
  `flow_status` tinyint NOT NULL DEFAULT 1 COMMENT '流转状态（1-科级待审 2-病案室待审 3-医务处待审 4-终审通过 5-整改中）',
  `current_level` tinyint NOT NULL DEFAULT 1 COMMENT '当前停留级（1-科级 2-病案室 3-医务处）',
  `return_level` tinyint COMMENT '最近一次退回发生级',
  `return_reason` varchar(500) COMMENT '最近一次退回的缺陷明细',
  `return_requirement` varchar(500) COMMENT '最近一次退回的整改要求',
  `return_deadline` date COMMENT '整改期限',
  `grade` tinyint COMMENT '终审定级（1-甲级 2-乙级 3-丙级）',
  `final_score` int COMMENT '终审评分',
  `final_opinion` varchar(500) COMMENT '终审意见',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qc_flow_no` (`flow_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历三级质控流转单';

-- biz_record_qc_flow_action  质控流转动作时间线
CREATE TABLE `biz_record_qc_flow_action` (
  `id` bigint NOT NULL COMMENT '主键',
  `flow_id` bigint NOT NULL COMMENT '流转单ID',
  `level` tinyint NOT NULL COMMENT '动作发生级（1-科级 2-病案室 3-医务处）',
  `action` tinyint NOT NULL COMMENT '动作（1-发起送审 2-审核通过 3-退回整改 4-整改提交 5-终审通过）',
  `opinion` varchar(500) COMMENT '审核意见',
  `defect_detail` varchar(1000) COMMENT '缺陷明细',
  `requirement` varchar(500) COMMENT '整改要求',
  `operator_id` bigint COMMENT '操作人员工ID',
  `operator_name` varchar(64) COMMENT '操作人姓名',
  `action_time` datetime NOT NULL COMMENT '动作时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质控流转动作时间线';

-- biz_referral  转诊
CREATE TABLE `biz_referral` (
  `referral_id` bigint NOT NULL COMMENT '转诊ID',
  `referral_no` varchar(32) NOT NULL COMMENT '转诊编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint COMMENT '就诊次ID',
  `from_dept_id` bigint NOT NULL COMMENT '转出科室ID',
  `to_dept_id` bigint COMMENT '转入科室ID',
  `to_hospital` varchar(128) COMMENT '转入医院',
  `reason` varchar(1000) COMMENT '转诊原因',
  `referral_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '转诊时间',
  `referral_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-待确认 1-已确认 2-已完成 3-已取消）',
  `remark` varchar(500) COMMENT '备注',
  `direction` tinyint DEFAULT 1 COMMENT '转诊方向（1-上转 2-下转）',
  `admission_id` bigint COMMENT '入院ID',
  `diagnosis` varchar(500) COMMENT '诊断摘要',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `audit_by` bigint COMMENT '确认人（员工ID）',
  `audit_name` varchar(50) COMMENT '确认人姓名',
  `audit_time` datetime COMMENT '确认时间',
  `audit_remark` varchar(500) COMMENT '确认意见',
  `finish_time` datetime COMMENT '完成时间',
  `finish_remark` varchar(500) COMMENT '完成备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`referral_id`),
  UNIQUE KEY `uk_referral_no` (`referral_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转诊';

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

-- biz_report  报告单
CREATE TABLE `biz_report` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `report_no` varchar(32) NOT NULL COMMENT '报告编号（唯一）',
  `report_type` tinyint NOT NULL COMMENT '报告类型（1-检查报告 2-检验报告）',
  `record_id` bigint NOT NULL COMMENT '检查/检验记录ID',
  `record_no` varchar(32) NOT NULL COMMENT '检查/检验记录号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `visit_date` date COMMENT '就诊日期',
  `item_name` varchar(200) NOT NULL COMMENT '项目名称',
  `exam_method` varchar(200) COMMENT '检查方法',
  `exam_dept_name` varchar(100) COMMENT '检查/检验科室',
  `apply_dept_name` varchar(100) COMMENT '申请科室',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(50) COMMENT '申请医生',
  `clinical_diagnosis` varchar(500) COMMENT '临床诊断',
  `report_content` text COMMENT '报告内容',
  `conclusion` text COMMENT '报告结论',
  `positive_flag` tinyint COMMENT '阴阳性（0-未判定 1-阴性 2-阳性 3-未见异常）',
  `is_critical` tinyint NOT NULL DEFAULT 0 COMMENT '是否危急（0-否 1-是）',
  `write_by` varchar(64) COMMENT '报告书写人',
  `write_by_id` bigint COMMENT '报告书写人员工ID',
  `write_time` datetime COMMENT '报告书写时间',
  `template_id` bigint COMMENT '使用的报告模板ID',
  `reject_reason` varchar(500) COMMENT '退回原因',
  `report_version` int NOT NULL DEFAULT 1 COMMENT '报告版本号',
  `film_count` int NOT NULL DEFAULT 0 COMMENT '已登记胶片张数',
  `suggestions` text COMMENT '建议',
  `report_status` tinyint DEFAULT 1 COMMENT '报告状态（1-待审核 2-初审通过 3-已审核 4-已发布 5-已作废）',
  `audit_by` varchar(64) COMMENT '审核人（初审）',
  `audit_time` datetime COMMENT '审核时间',
  `audit2_by` varchar(64) COMMENT '复审人',
  `audit2_time` datetime COMMENT '复审时间',
  `publish_time` datetime COMMENT '发布时间',
  `publish_by` varchar(64) COMMENT '发布人',
  `is_urgent` tinyint DEFAULT 0 COMMENT '是否加急（0-否 1-是）',
  `report_file_path` varchar(500) COMMENT '报告文件路径',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告单';

-- biz_revisit_fee_policy  复诊收费策略
CREATE TABLE `biz_revisit_fee_policy` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `policy_name` varchar(100) NOT NULL COMMENT '策略名称',
  `revisit_source` tinyint NOT NULL COMMENT '复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊 0-不限）',
  `same_doctor` tinyint NOT NULL DEFAULT 0 COMMENT '与原就诊医生（0-不限 1-要求同一医生 2-要求不同医生）',
  `same_dept` tinyint NOT NULL DEFAULT 0 COMMENT '与原就诊科室（0-不限 1-要求同一科室 2-要求不同科室）',
  `within_days` int COMMENT '与原就诊日最大间隔天数',
  `charge_mode` tinyint NOT NULL COMMENT '收费方式（1-全额收费 2-免挂号费 3-免挂号费+诊查费）',
  `priority` int NOT NULL DEFAULT 100 COMMENT '匹配优先级',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复诊收费策略';

-- biz_rx_doctor_talk  医师约谈记录
CREATE TABLE `biz_rx_doctor_talk` (
  `id` bigint NOT NULL COMMENT '主键',
  `talk_no` varchar(32) NOT NULL COMMENT '约谈编号',
  `doctor_id` bigint COMMENT '被约谈医师ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '被约谈医师姓名',
  `dept_name` varchar(100) COMMENT '医师所在科室',
  `talk_type` tinyint NOT NULL DEFAULT 1 COMMENT '约谈类型（1-首次约谈 2-警告约谈 3-限制处方权 4-取消处方权 5-恢复处方权）',
  `talk_time` datetime NOT NULL COMMENT '约谈时间',
  `talker_name` varchar(50) NOT NULL COMMENT '约谈人姓名',
  `talker_org` varchar(100) COMMENT '约谈部门',
  `related_count` int NOT NULL DEFAULT 0 COMMENT '关联不合理处方数',
  `related_review_ids` varchar(500) COMMENT '关联点评明细ID',
  `problem_summary` varchar(500) COMMENT '问题摘要',
  `talk_content` varchar(1000) COMMENT '约谈内容',
  `rectify_require` varchar(500) COMMENT '整改要求',
  `rectify_status` tinyint NOT NULL DEFAULT 1 COMMENT '整改状态（1-待整改 2-已整改）',
  `rectify_remark` varchar(500) COMMENT '整改情况说明',
  `doctor_confirm` tinyint NOT NULL DEFAULT 0 COMMENT '医师确认（0-未确认 1-已确认）',
  `doctor_confirm_by` varchar(50) COMMENT '医师确认人',
  `doctor_confirm_time` datetime COMMENT '医师确认时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_talk_no` (`talk_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医师约谈记录';

-- biz_rx_flow  处方流转单
CREATE TABLE `biz_rx_flow` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `flow_no` varchar(32) NOT NULL COMMENT '流转单号',
  `prescription_id` bigint NOT NULL COMMENT '处方ID',
  `prescription_no` varchar(64) NOT NULL COMMENT '处方号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `org_name` varchar(100) NOT NULL COMMENT '流向机构名称',
  `org_type` tinyint NOT NULL DEFAULT 1 COMMENT '机构类型（1-院外药店 2-基层医疗机构 3-线上药房）',
  `flow_status` tinyint NOT NULL DEFAULT 1 COMMENT '流转状态（1-已流转 2-已取药 3-已取消）',
  `flow_time` datetime COMMENT '流转时间',
  `finish_time` datetime COMMENT '完成/取消时间',
  `total_amount` decimal(10,2) COMMENT '处方总金额',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_no` (`flow_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方流转单';

-- biz_rx_review_batch  处方点评批次
CREATE TABLE `biz_rx_review_batch` (
  `id` bigint NOT NULL COMMENT '主键',
  `batch_no` varchar(32) NOT NULL COMMENT '批次号',
  `batch_name` varchar(100) NOT NULL COMMENT '批次名称',
  `review_type` tinyint NOT NULL DEFAULT 1 COMMENT '点评类型（1-常规点评 2-专项点评）',
  `specialty` varchar(100) COMMENT '专项主题',
  `date_start` date NOT NULL COMMENT '处方就诊日期起',
  `date_end` date NOT NULL COMMENT '处方就诊日期止',
  `sample_count` int NOT NULL COMMENT '抽样处方数',
  `reviewed_count` int NOT NULL DEFAULT 0 COMMENT '已点评数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '批次状态（1-进行中 2-已完成）',
  `reviewer_id` bigint COMMENT '点评人员工ID',
  `reviewer_name` varchar(50) COMMENT '点评人姓名',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方点评批次';

-- biz_rx_review_item  处方点评明细
CREATE TABLE `biz_rx_review_item` (
  `id` bigint NOT NULL COMMENT '主键',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `batch_no` varchar(32) NOT NULL COMMENT '批次号',
  `prescription_id` bigint NOT NULL COMMENT '处方ID',
  `prescription_no` varchar(32) NOT NULL COMMENT '处方号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `dept_name` varchar(100) NOT NULL COMMENT '开方科室',
  `doctor_id` bigint NOT NULL COMMENT '开方医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '开方医生姓名',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `diagnosis` varchar(500) COMMENT '诊断',
  `drug_count` int DEFAULT 0 COMMENT '药品数量',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '处方金额',
  `prescription_type` tinyint DEFAULT 1 COMMENT '处方类型（1-西药 2-中成药 3-中药饮片）',
  `prescription_source` tinyint DEFAULT 1 COMMENT '处方来源（1-门诊 2-急诊 3-住院）',
  `review_status` tinyint NOT NULL DEFAULT 0 COMMENT '点评状态（0-待点评 1-已点评）',
  `review_result` tinyint COMMENT '点评结论（1-合理 2-不规范处方 3-用药不适宜处方 4-超常处方）',
  `problem_types` varchar(500) COMMENT '问题码（11-15不规范 21-27不适宜 31-34超常）',
  `review_opinion` varchar(500) COMMENT '点评意见',
  `reviewer_id` bigint COMMENT '点评人员工ID',
  `reviewer_name` varchar(50) COMMENT '点评人姓名',
  `review_time` datetime COMMENT '点评时间',
  `publicity_status` tinyint NOT NULL DEFAULT 0 COMMENT '公示状态（0-未公示 1-已公示）',
  `publicity_by` varchar(50) COMMENT '公示操作人',
  `publicity_time` datetime COMMENT '公示时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_rx` (`batch_id`, `prescription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方点评明细';

-- biz_rx_template  处方模板
CREATE TABLE `biz_rx_template` (
  `id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `drug_count` int DEFAULT 0 COMMENT '药品数量',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '总金额',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方模板';

-- biz_rx_template_detail  处方模板明细
CREATE TABLE `biz_rx_template_detail` (
  `id` bigint NOT NULL,
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) NOT NULL COMMENT '药品编码',
  `drug_name` varchar(200) NOT NULL COMMENT '药品名称',
  `generic_name` varchar(200) COMMENT '通用名',
  `specification` varchar(100) COMMENT '规格',
  `dosage_form` varchar(50) COMMENT '剂型',
  `manufacturer` varchar(200) COMMENT '生产厂家',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `quantity` decimal(10,2) NOT NULL COMMENT '数量',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `usage_dosage` varchar(100) NOT NULL COMMENT '用法用量',
  `frequency` varchar(50) NOT NULL COMMENT '用药频次',
  `route` varchar(50) NOT NULL COMMENT '用药途径',
  `duration` int COMMENT '疗程天数',
  `single_dosage` varchar(50) COMMENT '单次剂量',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方模板明细';

-- biz_schedule  排班信息
CREATE TABLE `biz_schedule` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `week_day` tinyint NOT NULL COMMENT '星期（1-周日 2-周一 3-周二 4-周三 5-周四 6-周五 7-周六）',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `room_id` bigint COMMENT '诊室ID',
  `room_name` varchar(100) COMMENT '诊室名称',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '医生姓名',
  `staff_type` tinyint NOT NULL DEFAULT 1 COMMENT '排班对象岗位类别（2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `schedule_type` tinyint COMMENT '排班类型（1-上午 2-下午 3-全天 4-凌晨）',
  `start_time` varchar(10) NOT NULL COMMENT '开始时间',
  `end_time` varchar(10) NOT NULL COMMENT '结束时间',
  `shift_id` bigint COMMENT '标准班次ID',
  `total_source` int DEFAULT 0 COMMENT '总号源数',
  `used_source` int DEFAULT 0 COMMENT '已挂号数',
  `available_source` int DEFAULT 0 COMMENT '剩余号源数',
  `used_appointment_source` int NOT NULL DEFAULT 0 COMMENT '预约池已用号源',
  `regist_fee` decimal(10,2) DEFAULT 0.00 COMMENT '挂号费',
  `diagnosis_fee` decimal(10,2) DEFAULT 0.00 COMMENT '诊查费',
  `is_expert` tinyint DEFAULT 0 COMMENT '是否专家号（0-否 1-是）',
  `expert_fee` decimal(10,2) DEFAULT 0.00 COMMENT '专家号费用',
  `is_appointment` tinyint DEFAULT 1 COMMENT '是否可预约（0-否 1-是）',
  `appointment_source` int DEFAULT 0 COMMENT '预约号源数',
  `added_source` int NOT NULL DEFAULT 0 COMMENT '累计加号数',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停诊 1-正常 2-已满 3-已过期）',
  `consult_status` tinyint DEFAULT 0 COMMENT '就诊状态（0-待开始 1-接诊中 2-暂停）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule_window` (`dept_id`, `doctor_id`, `schedule_date`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班信息';

-- biz_schedule_slot  排班时段号源
CREATE TABLE `biz_schedule_slot` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `schedule_id` bigint NOT NULL COMMENT '排班ID',
  `seq` int NOT NULL COMMENT '段序',
  `start_time` char(5) NOT NULL COMMENT '段开始时间（HH:mm）',
  `end_time` char(5) NOT NULL COMMENT '段结束时间（HH:mm）',
  `total_source` int NOT NULL DEFAULT 0 COMMENT '段号源总数',
  `used_source` int NOT NULL DEFAULT 0 COMMENT '段已挂号数（现场+线上）',
  `available_source` int NOT NULL DEFAULT 0 COMMENT '段剩余号源',
  `added_source` int NOT NULL DEFAULT 0 COMMENT '段累计加号数',
  `appointment_source` int NOT NULL DEFAULT 0 COMMENT '段内线上预约预留',
  `used_appointment_source` int NOT NULL DEFAULT 0 COMMENT '段内线上预约已用',
  `status` tinyint DEFAULT 1 COMMENT '段状态（0-停用 1-正常）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slot` (`schedule_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班时段号源';

-- biz_schedule_slot_template  排班模板时段
CREATE TABLE `biz_schedule_slot_template` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `template_id` bigint NOT NULL COMMENT '排班模板ID',
  `seq` int NOT NULL COMMENT '段序',
  `start_time` char(5) NOT NULL COMMENT '段开始时间（HH:mm）',
  `end_time` char(5) NOT NULL COMMENT '段结束时间（HH:mm）',
  `total_source` int NOT NULL DEFAULT 0 COMMENT '段号源总数',
  `appointment_source` int NOT NULL DEFAULT 0 COMMENT '段内线上预约预留',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tpl_slot` (`template_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班模板时段';

-- biz_schedule_template  排班周模板
CREATE TABLE `biz_schedule_template` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(64) DEFAULT '' COMMENT '科室名称',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `doctor_name` varchar(64) DEFAULT '' COMMENT '医生姓名',
  `staff_type` tinyint NOT NULL DEFAULT 1 COMMENT '排班对象岗位类别（2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `week_day` tinyint NOT NULL COMMENT '星期几（1-周一 7-周日）',
  `week_parity` tinyint NOT NULL DEFAULT 0 COMMENT '单双周（0-每周 1-单周 2-双周）',
  `valid_from` date COMMENT '生效起始日期(空=不限)',
  `valid_until` date COMMENT '生效截止日期(空=不限)',
  `schedule_type` tinyint COMMENT '班次（1-上午 2-下午 3-全天 4-凌晨）',
  `start_time` char(5) DEFAULT '08:00' COMMENT '开始时间（HH:mm）',
  `end_time` char(5) DEFAULT '12:00' COMMENT '结束时间（HH:mm）',
  `shift_id` bigint COMMENT '标准班次ID',
  `total_source` int NOT NULL DEFAULT 20 COMMENT '号源总数',
  `room_id` bigint COMMENT '诊室ID',
  `room_name` varchar(64) COMMENT '诊室名称',
  `regist_fee` decimal(10,2) DEFAULT 0.00 COMMENT '挂号费',
  `diagnosis_fee` decimal(10,2) DEFAULT 0.00 COMMENT '诊疗费',
  `is_expert` tinyint DEFAULT 0 COMMENT '是否专家（0-否 1-是）',
  `expert_fee` decimal(10,2) DEFAULT 0.00 COMMENT '专家费',
  `is_appointment` tinyint DEFAULT 1 COMMENT '是否开放预约（0-否 1-是）',
  `appointment_source` int DEFAULT 0 COMMENT '预约号源数',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班周模板';

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

-- biz_settlement_diagnosis  结算清单诊断明细
CREATE TABLE `biz_settlement_diagnosis` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `settlement_id` bigint NOT NULL COMMENT '结算清单ID',
  `seq_no` int NOT NULL DEFAULT 1 COMMENT '序号',
  `diag_type` tinyint NOT NULL DEFAULT 1 COMMENT '诊断类型（1-主要诊断 2-其他诊断）',
  `icd_code` varchar(32) COMMENT 'ICD-10 编码',
  `icd_name` varchar(200) COMMENT '诊断名称',
  `admit_condition` tinyint COMMENT '入院病情（1-有 2-临床未确定 3-情况不明 4-无）',
  `cc_level` varchar(8) COMMENT '并发症合并症级别',
  `evidence_status` tinyint COMMENT '依据核对结果（1-命中 2-通过 3-不适用）',
  `evidence_note` varchar(500) COMMENT '依据核对说明',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算清单诊断明细';

-- biz_settlement_operation  结算清单手术明细
CREATE TABLE `biz_settlement_operation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `settlement_id` bigint NOT NULL COMMENT '结算清单ID',
  `seq_no` int NOT NULL DEFAULT 1 COMMENT '序号',
  `oper_code` varchar(32) COMMENT '手术操作编码',
  `oper_name` varchar(200) COMMENT '手术操作名称',
  `oper_date` date COMMENT '手术操作日期',
  `oper_level` tinyint COMMENT '手术级别',
  `is_main` tinyint NOT NULL DEFAULT 0 COMMENT '是否主要手术操作（0-否 1-是）',
  `evidence_status` tinyint COMMENT '依据核对结果（1-命中 2-通过 3-不适用）',
  `evidence_note` varchar(500) COMMENT '依据核对说明',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算清单手术明细';

-- biz_shift  班次字典
CREATE TABLE `biz_shift` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `shift_name` varchar(50) NOT NULL COMMENT '班次名称',
  `start_time` varchar(10) NOT NULL COMMENT '开始时间（HH:mm）',
  `end_time` varchar(10) NOT NULL COMMENT '结束时间（HH:mm）',
  `duration_minutes` int NOT NULL DEFAULT 0 COMMENT '时长（分钟）',
  `dept_id` bigint COMMENT '适用科室ID',
  `schedule_type` tinyint COMMENT '班次类型（1-上午 2-下午 3-全天 4-凌晨）',
  `use_scope` tinyint NOT NULL DEFAULT 1 COMMENT '班次适用域（1-门诊 2-病区护理排班）',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次字典';

-- biz_single_disease_case  单病种质控病例
CREATE TABLE `biz_single_disease_case` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `case_no` varchar(32) NOT NULL COMMENT '病例编号',
  `disease_id` bigint NOT NULL COMMENT '病种ID',
  `admission_id` bigint NOT NULL COMMENT '住院ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `main_diagnosis_code` varchar(32) COMMENT '主要诊断编码',
  `main_diagnosis_name` varchar(200) COMMENT '主要诊断名称',
  `inpatient_days` int COMMENT '住院天数',
  `total_amount` decimal(12,2) COMMENT '住院总费用',
  `is_surgery` tinyint NOT NULL DEFAULT 0 COMMENT '是否手术（0-否 1-是）',
  `death_flag` tinyint NOT NULL DEFAULT 0 COMMENT '死亡标志',
  `curative_effect` tinyint COMMENT '疗效判定（1-治愈 2-好转 3-未愈 4-死亡 5-其他）',
  `enroll_way` tinyint NOT NULL DEFAULT 1 COMMENT '纳入方式（1-自动扫描 2-手工纳入）',
  `qc_status` tinyint NOT NULL DEFAULT 0 COMMENT '质控状态（0-待质控 1-通过 2-异常）',
  `qc_issues` varchar(500) COMMENT '质控异常项',
  `report_status` tinyint NOT NULL DEFAULT 0 COMMENT '上报状态（0-未上报 1-已上报）',
  `report_time` datetime COMMENT '上报时间',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_case_no` (`case_no`),
  UNIQUE KEY `uk_disease_admission` (`disease_id`, `admission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单病种质控病例';

-- biz_skin_test  门诊皮试记录
CREATE TABLE `biz_skin_test` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `test_no` varchar(32) NOT NULL COMMENT '皮试单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `drug_name` varchar(200) NOT NULL COMMENT '皮试药物名称',
  `treatment_record_id` bigint COMMENT '来源治疗记录ID',
  `test_time` datetime NOT NULL COMMENT '皮试时间',
  `result` tinyint NOT NULL DEFAULT 0 COMMENT '判读结果（0-待判读 1-阴性 2-阳性）',
  `result_time` datetime COMMENT '判读时间',
  `nurse_id` bigint COMMENT '执行护士ID',
  `nurse_name` varchar(50) COMMENT '执行护士姓名',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_test_no` (`test_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊皮试记录';

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

-- biz_stat_report  病案统计上报台账
CREATE TABLE `biz_stat_report` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `report_no` varchar(32) NOT NULL COMMENT '上报单号',
  `report_type` tinyint NOT NULL COMMENT '报表类型（1-卫统年报 2-出院患者统计月报 3-手术工作量专项报表）',
  `period_type` tinyint NOT NULL DEFAULT 2 COMMENT '期间类型（1-月报 2-年报）',
  `period_value` varchar(10) NOT NULL COMMENT '期间值',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `title` varchar(200) NOT NULL COMMENT '报表标题',
  `discharge_count` int NOT NULL DEFAULT 0 COMMENT '摘要-出院患者例数',
  `death_count` int NOT NULL DEFAULT 0 COMMENT '摘要-死亡例数',
  `operation_count` int NOT NULL DEFAULT 0 COMMENT '摘要-手术台次',
  `level3up_count` int NOT NULL DEFAULT 0 COMMENT '摘要-三级及以上手术台次',
  `avg_los_days` decimal(6,1) NOT NULL DEFAULT 0.0 COMMENT '摘要-平均住院日',
  `total_amount` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '摘要-结算总金额',
  `payload` mediumtext NOT NULL COMMENT '上报报文',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-草稿 1-已报出 2-已作废）',
  `generate_time` datetime COMMENT '报文生成时间',
  `submit_time` datetime COMMENT '报出时间',
  `void_time` datetime COMMENT '作废时间',
  `void_reason` varchar(200) COMMENT '作废原因',
  `operator_name` varchar(50) COMMENT '生成人',
  `submit_by_name` varchar(50) COMMENT '报出人',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_report_no` (`report_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病案统计上报台账';

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
  `drug_code` varchar(32) COMMENT '药品编码',
  `drug_name` varchar(200) COMMENT '药品名称',
  `specification` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `batch_no` varchar(50) COMMENT '批号',
  `production_date` date COMMENT '生产日期',
  `expiry_date` date COMMENT '有效期',
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

-- biz_survey_answer  满意度答卷
CREATE TABLE `biz_survey_answer` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `answer_no` varchar(32) NOT NULL COMMENT '答卷编号',
  `dispatch_id` bigint NOT NULL COMMENT '发放单ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `scene` tinyint NOT NULL COMMENT '场景',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '就诊科室ID',
  `dept_name` varchar(128) COMMENT '科室名称',
  `avg_score` decimal(5,2) NOT NULL COMMENT '李克特均分',
  `score_100` decimal(6,2) NOT NULL COMMENT '百分制得分',
  `nps` tinyint COMMENT 'NPS 推荐度',
  `comment_text` varchar(1000) COMMENT '开放意见',
  `fill_source` tinyint NOT NULL DEFAULT 2 COMMENT '填报方式（1-患者自填 2-随访员代填 3-现场扫码）',
  `anonymous_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否匿名（0-否 1-是）',
  `fill_employee_id` bigint COMMENT '代填人（员工ID）',
  `fill_employee_name` varchar(64) COMMENT '代填人姓名',
  `fill_time` datetime COMMENT '提交时间',
  `answer_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-有效 2-已作废）',
  `dispute_case_id` bigint COMMENT '低分自动转出的投诉单ID',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_answer_dispatch` (`dispatch_id`),
  UNIQUE KEY `uk_survey_answer_no` (`answer_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度答卷';

-- biz_survey_answer_item  满意度逐题答案
CREATE TABLE `biz_survey_answer_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `answer_id` bigint NOT NULL COMMENT '答卷ID',
  `item_id` bigint NOT NULL COMMENT '题目ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `dimension` tinyint NOT NULL COMMENT '评价维度',
  `seq_no` int NOT NULL COMMENT '题号',
  `title` varchar(255) NOT NULL COMMENT '题干',
  `question_type` tinyint NOT NULL COMMENT '题型',
  `score` tinyint COMMENT '得分',
  `option_label` varchar(128) COMMENT '选项文本',
  `text_value` varchar(1000) COMMENT '文本题回答',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_answer_item` (`answer_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度逐题答案';

-- biz_survey_dispatch  满意度发放台账
CREATE TABLE `biz_survey_dispatch` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `dispatch_no` varchar(32) NOT NULL COMMENT '发放单号',
  `template_id` bigint NOT NULL COMMENT '问卷模板ID',
  `template_name` varchar(128) COMMENT '模板名称',
  `scene` tinyint NOT NULL COMMENT '适用场景',
  `source_type` tinyint NOT NULL COMMENT '发放来源（1-随访任务 2-出院结算 3-人工补发）',
  `source_id` bigint NOT NULL COMMENT '来源单据ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `phone` varchar(20) COMMENT '联系手机号',
  `dept_id` bigint COMMENT '就诊科室ID',
  `dept_name` varchar(128) COMMENT '科室名称',
  `channel` tinyint NOT NULL DEFAULT 1 COMMENT '回收渠道（1-电话代填 2-短信 3-微信 4-现场扫码）',
  `dispatch_status` tinyint NOT NULL DEFAULT 1 COMMENT '回收状态（1-待推送 2-已推送待回收 3-已回收 4-已过期 5-已拒答）',
  `push_time` datetime COMMENT '推送/发起时间',
  `expire_time` datetime COMMENT '回收截止时间',
  `answer_id` bigint COMMENT '回收到的答卷ID',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_dispatch_no` (`dispatch_no`),
  UNIQUE KEY `uk_survey_dispatch_source` (`source_type`, `source_id`, `template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度发放台账';

-- biz_survey_item  满意度问卷题目
CREATE TABLE `biz_survey_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `seq_no` int NOT NULL COMMENT '题号',
  `dimension` tinyint NOT NULL COMMENT '评价维度',
  `question_type` tinyint NOT NULL COMMENT '题型（1-量表 2-单选 3-多选 4-NPS推荐度 5-开放文本）',
  `title` varchar(255) NOT NULL COMMENT '题干',
  `required` tinyint NOT NULL DEFAULT 1 COMMENT '是否必答（0-否 1-是）',
  `weight` decimal(5,2) NOT NULL DEFAULT 1.00 COMMENT '权重',
  `max_score` tinyint NOT NULL DEFAULT 5 COMMENT '满分',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_item` (`template_id`, `seq_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度问卷题目';

-- biz_survey_template  满意度问卷模板
CREATE TABLE `biz_survey_template` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_no` varchar(32) NOT NULL COMMENT '模板编号',
  `template_name` varchar(128) NOT NULL COMMENT '问卷名称',
  `scene` tinyint NOT NULL COMMENT '适用场景（1-出院随访 2-门诊 3-住院在院 4-体检）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-启用 2-停用）',
  `description` varchar(500) COMMENT '说明（调查目的、口径、上报去向）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_template_no` (`template_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满意度问卷模板';

-- biz_tcm_decoct  中药代煎单
CREATE TABLE `biz_tcm_decoct` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `decoct_no` varchar(32) NOT NULL COMMENT '代煎单号',
  `prescription_id` bigint NOT NULL COMMENT '处方ID',
  `prescription_no` varchar(32) NOT NULL COMMENT '处方号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_name` varchar(100) COMMENT '开方科室',
  `doctor_name` varchar(50) COMMENT '开方医师',
  `dose_count` int NOT NULL DEFAULT 1 COMMENT '剂数',
  `herb_count` int NOT NULL DEFAULT 0 COMMENT '味数',
  `total_grams` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '全方总克数',
  `method_summary` varchar(500) COMMENT '煎法脚注汇总',
  `decoct_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待煎 2-已煎 3-已取 9-已作废）',
  `pharmacy_id` bigint COMMENT '代煎药房ID',
  `pharmacy_name` varchar(100) COMMENT '代煎药房名称',
  `operator_id` bigint COMMENT '最近一次状态操作人',
  `operator_name` varchar(64) COMMENT '最近一次状态操作人姓名',
  `decoct_time` datetime COMMENT '煎药完成时间',
  `pickup_time` datetime COMMENT '患者取走时间（终态）',
  `cancel_reason` varchar(200) COMMENT '作废原因',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prescription` (`prescription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中药代煎单';

-- biz_tech_auth_override  越权授权事后登记
CREATE TABLE `biz_tech_auth_override` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `source_type` tinyint NOT NULL COMMENT '来源单据类型（1-手术申请 2-日间手术 3-住院医嘱 4-内镜记录）',
  `source_id` bigint NOT NULL COMMENT '来源单据ID',
  `source_no` varchar(64) COMMENT '来源单据号',
  `employee_id` bigint NOT NULL COMMENT '越权操作者（员工ID）',
  `employee_name` varchar(50) NOT NULL COMMENT '越权操作者姓名',
  `auth_category` tinyint NOT NULL COMMENT '涉及授权类别',
  `required_level` tinyint NOT NULL COMMENT '该操作要求的级别',
  `held_level` tinyint COMMENT '越权者当时的授权级别上限',
  `reason` varchar(500) NOT NULL COMMENT '越权原因',
  `occur_time` datetime NOT NULL COMMENT '越权发生时间',
  `override_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待上级确认 2-已确认）',
  `supervisor_id` bigint COMMENT '上级确认人（员工ID）',
  `supervisor_name` varchar(50) COMMENT '上级确认人姓名',
  `confirm_time` datetime COMMENT '确认时间',
  `confirm_opinion` varchar(500) COMMENT '确认意见',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='越权授权事后登记';

-- biz_tele_consult  远程会诊
CREATE TABLE `biz_tele_consult` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `consult_no` varchar(32) NOT NULL COMMENT '会诊单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `admission_id` bigint COMMENT '关联住院ID',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(128) COMMENT '申请科室名称',
  `apply_doctor_id` bigint COMMENT '申请医生ID（员工ID）',
  `apply_doctor` varchar(64) COMMENT '申请医生姓名',
  `consult_type` tinyint NOT NULL DEFAULT 1 COMMENT '会诊类型（1-临床会诊 2-远程影像 3-远程心电 4-远程病理 5-其他）',
  `is_urgent` tinyint NOT NULL DEFAULT 0 COMMENT '是否急会诊（0-否 1-是）',
  `expert_hospital` varchar(128) COMMENT '受邀专家所在医院',
  `expert_dept` varchar(128) COMMENT '受邀专家科室',
  `expert_name` varchar(64) COMMENT '受邀专家姓名',
  `expert_title` varchar(32) COMMENT '受邀专家职称',
  `purpose` varchar(500) COMMENT '会诊目的',
  `diagnosis` varchar(500) COMMENT '申请方诊断/病情摘要',
  `plan_time` datetime COMMENT '计划会诊时间',
  `duration_min` int COMMENT '计划时长（分钟）',
  `platform` varchar(64) COMMENT '对接平台',
  `meet_no` varchar(64) COMMENT '接入号/会议室号（预留）',
  `opinion` varchar(1000) COMMENT '会诊意见',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待安排 2-已安排 3-已完成 4-已取消）',
  `fee` decimal(10,2) COMMENT '会诊费用',
  `arrange_by` varchar(64) COMMENT '安排人',
  `arrange_time` datetime COMMENT '安排时间',
  `complete_by` varchar(64) COMMENT '完成人',
  `complete_time` datetime COMMENT '完成时间',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tele_no` (`consult_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='远程会诊';

-- biz_transfusion_apply  输血申请单
CREATE TABLE `biz_transfusion_apply` (
  `id` bigint NOT NULL COMMENT '输血申请单ID（雪花）',
  `apply_no` varchar(32) NOT NULL COMMENT '输血申请单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `admission_no` varchar(32) COMMENT '入院号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女）',
  `age` int COMMENT '年龄',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(64) COMMENT '申请科室名称',
  `apply_ward_name` varchar(64) COMMENT '申请时所在病区名称',
  `apply_bed_no` varchar(32) COMMENT '申请时床号',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(64) COMMENT '申请医生姓名',
  `apply_time` datetime COMMENT '申请时间',
  `patient_abo` varchar(4) NOT NULL COMMENT '受血者ABO血型',
  `patient_rh` varchar(4) NOT NULL COMMENT '受血者Rh血型',
  `blood_component` tinyint NOT NULL COMMENT '血液品种（1-红细胞悬液 2-血浆 3-血小板 4-冷沉淀 5-全血 6-其他）',
  `component_spec` varchar(64) COMMENT '规格',
  `bag_count` int NOT NULL DEFAULT 1 COMMENT '申请袋数',
  `planned_amount` decimal(10,2) COMMENT '申请总量',
  `amount_ml` int COMMENT '申请量折算毫升数',
  `approve_level` tinyint NOT NULL DEFAULT 1 COMMENT '审批级别（1-上级医师 400-799ml）',
  `approve_status` tinyint NOT NULL DEFAULT 0 COMMENT '审批状态（0-待审批 1-已通过 2-已驳回 3-急诊待补审）',
  `approve_reject_reason` varchar(200) COMMENT '最近一次驳回原因',
  `approve_time` datetime COMMENT '审批通过时间',
  `approve_makeup` tinyint NOT NULL DEFAULT 0 COMMENT '是否急诊补审（0-常规审批 1-急诊后补）',
  `amount_unit` varchar(16) COMMENT '总量单位',
  `transfusion_purpose` varchar(200) COMMENT '输血目的（纠正贫血/补充凝血因子/提升血小板…）',
  `indication` varchar(500) NOT NULL COMMENT '输血指征（Hb/HCT/PLT 指标 + 临床症状，缺了就是无指征用血）',
  `pre_hb` decimal(6,2) COMMENT '输血前血红蛋白 Hb（g/L）',
  `pre_hct` decimal(5,2) COMMENT '输血前红细胞压积 HCT（%）',
  `pre_plt` int COMMENT '输血前血小板 PLT',
  `transfusion_history` varchar(500) COMMENT '既往输血史',
  `reaction_history` varchar(500) COMMENT '既往输血反应史',
  `pregnancy_history` varchar(200) COMMENT '妊娠史',
  `is_emergency` tinyint NOT NULL DEFAULT 0 COMMENT '是否紧急用血（0-否 1-是）',
  `crossmatch_status` tinyint NOT NULL DEFAULT 0 COMMENT '配血状态（0-待配血 1-配血中 3-存在配血不合）',
  `crossmatch_doctor_id` bigint COMMENT '配血人ID',
  `crossmatch_doctor_name` varchar(64) COMMENT '配血人姓名',
  `crossmatch_time` datetime COMMENT '配血完成时间',
  `issue_doctor_id` bigint COMMENT '发血人ID（员工ID）',
  `issue_doctor_name` varchar(64) COMMENT '发血人姓名',
  `issue_time` datetime COMMENT '发血时间',
  `check_items` varchar(200) COMMENT '输血前核对要点码',
  `check_note` varchar(1000) COMMENT '核对补充说明',
  `check_nurse_id` bigint COMMENT '核对护士1 ID（员工ID）',
  `check_nurse_name` varchar(64) COMMENT '核对护士1 姓名',
  `check_nurse2_id` bigint COMMENT '核对护士2 ID',
  `check_nurse2_name` varchar(64) COMMENT '核对护士2 姓名',
  `check_time` datetime COMMENT '双人核对时间',
  `infusion_nurse_id` bigint COMMENT '输注执行护士ID（员工ID）',
  `infusion_nurse_name` varchar(64) COMMENT '输注执行护士姓名',
  `infusion_start_time` datetime COMMENT '输注开始时间',
  `infusion_end_time` datetime COMMENT '输注结束时间',
  `actual_amount` decimal(10,2) COMMENT '实际输注量',
  `infusion_speed` varchar(32) COMMENT '滴速',
  `observation` varchar(1000) COMMENT '输注过程观察',
  `has_reaction` tinyint NOT NULL DEFAULT 0 COMMENT '有无输血反应（0-未上报 1-已上报有反应）',
  `reaction_type` varchar(100) COMMENT '反应类型',
  `reaction_desc` varchar(1000) COMMENT '反应描述',
  `reaction_handle` varchar(1000) COMMENT '处理措施',
  `reaction_reporter_id` bigint COMMENT '上报人ID（员工ID）',
  `reaction_reporter_name` varchar(64) COMMENT '上报人姓名',
  `reaction_time` datetime COMMENT '上报时间',
  `efficacy_eval` varchar(1000) COMMENT '输注后疗效评估',
  `post_hb` decimal(6,2) COMMENT '输血后血红蛋白 Hb（g/L）',
  `post_hct` decimal(5,2) COMMENT '输血后红细胞压积 HCT（%）',
  `post_plt` int COMMENT '输血后血小板 PLT',
  `finish_doctor_id` bigint COMMENT '完成录入人ID（员工ID）',
  `finish_doctor_name` varchar(64) COMMENT '完成录入人姓名',
  `finish_time` datetime COMMENT '完成时间',
  `record_id` bigint COMMENT '回写住院病历ID',
  `transfusion_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-待配血 1-已配血 2-已发血 3-输注中 4-已完成 5-已取消）',
  `cancel_reason` varchar(500) COMMENT '取消原因（仅待配血/已配血/已发血可取消）',
  `cancel_doctor_id` bigint COMMENT '取消人ID（员工ID）',
  `cancel_doctor_name` varchar(64) COMMENT '取消人姓名',
  `cancel_time` datetime COMMENT '取消时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='输血申请单';

-- biz_transfusion_approve  用血分级审批流水
CREATE TABLE `biz_transfusion_approve` (
  `id` bigint NOT NULL COMMENT '审批记录ID（雪花）',
  `apply_id` bigint NOT NULL COMMENT '输血申请单ID',
  `apply_no` varchar(32) COMMENT '输血申请单号',
  `approve_level` tinyint NOT NULL COMMENT '审批级别（1-上级医师 2-科主任 3-医务科）',
  `approve_result` tinyint NOT NULL COMMENT '审批结论（1-通过 2-驳回）',
  `approver_id` bigint COMMENT '审批人ID（员工ID）',
  `approver_name` varchar(64) COMMENT '审批人姓名',
  `approver_title` varchar(32) COMMENT '审批人职称',
  `opinion` varchar(200) COMMENT '审批意见',
  `is_makeup` tinyint NOT NULL DEFAULT 0 COMMENT '是否急诊补审（0-常规 1-补审）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用血分级审批流水';

-- biz_transfusion_bag  输血血袋明细
CREATE TABLE `biz_transfusion_bag` (
  `id` bigint NOT NULL COMMENT '血袋明细ID（雪花）',
  `apply_id` bigint NOT NULL COMMENT '输血申请单ID',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint COMMENT '患者ID（冗余）',
  `bag_no` varchar(64) NOT NULL COMMENT '血袋号',
  `donor_no` varchar(64) COMMENT '献血编号',
  `bag_abo` varchar(4) NOT NULL COMMENT '血袋ABO血型',
  `bag_rh` varchar(4) NOT NULL COMMENT '血袋Rh血型',
  `blood_component` tinyint COMMENT '血液品种',
  `spec` varchar(64) COMMENT '规格',
  `amount` decimal(10,2) COMMENT '血量',
  `amount_unit` varchar(16) COMMENT '血量单位',
  `source_bank` varchar(100) COMMENT '来源血站',
  `collect_date` date COMMENT '采集日期',
  `expire_date` date COMMENT '有效期至',
  `crossmatch_main` varchar(16) COMMENT '主侧配血结果（阴性=相合）',
  `crossmatch_side` varchar(16) COMMENT '次侧配血结果',
  `crossmatch_result` tinyint COMMENT '配血结论（1-相合 2-不合）',
  `crossmatch_time` datetime COMMENT '配血时间',
  `crossmatch_doctor_id` bigint COMMENT '配血人ID（员工ID）',
  `crossmatch_doctor_name` varchar(64) COMMENT '配血人姓名',
  `bag_status` tinyint NOT NULL DEFAULT 0 COMMENT '血袋状态（0-待配血 1-已配血 2-已发血 3-已输注）',
  `issue_time` datetime COMMENT '发血时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='输血血袋明细';

-- biz_treatment_apply  治疗申请单
CREATE TABLE `biz_treatment_apply` (
  `apply_id` bigint NOT NULL COMMENT '治疗申请ID',
  `apply_no` varchar(32) NOT NULL COMMENT '治疗申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint COMMENT '就诊次ID',
  `regist_id` bigint COMMENT '挂号ID',
  `doctor_id` bigint NOT NULL COMMENT '开单医生ID',
  `treatment_item_id` bigint NOT NULL COMMENT '治疗项目ID',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `execute_time` datetime COMMENT '执行时间',
  `apply_status` tinyint NOT NULL DEFAULT 0 COMMENT '申请状态（0-待执行 1-已执行 2-已取消）',
  `remark` varchar(500) COMMENT '备注',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `regist_no` varchar(32) COMMENT '挂号单号',
  `doctor_name` varchar(50) COMMENT '开单医生姓名',
  `dept_id` bigint COMMENT '开单科室ID',
  `dept_name` varchar(100) COMMENT '开单科室名称',
  `exec_dept_id` bigint COMMENT '建议执行科室ID',
  `exec_dept_name` varchar(100) COMMENT '建议执行科室名称',
  `item_code` varchar(32) COMMENT '治疗项目编码',
  `item_name` varchar(200) COMMENT '治疗项目名称',
  `item_type` tinyint COMMENT '治疗项目类别（1-注射 2-输液 3-换药 4-拆线 5-其他）',
  `price` decimal(10,2) COMMENT '项目单价',
  `total_times` int NOT NULL DEFAULT 1 COMMENT '疗程总次数',
  `done_times` int NOT NULL DEFAULT 0 COMMENT '已完成次数',
  `start_date` date COMMENT '疗程计划开始日期',
  `interval_days` int NOT NULL DEFAULT 1 COMMENT '相邻两次执行的间隔天数',
  PRIMARY KEY (`apply_id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治疗申请单';

-- biz_treatment_record  治疗执行记录
CREATE TABLE `biz_treatment_record` (
  `record_id` bigint NOT NULL COMMENT '治疗记录ID',
  `record_no` varchar(32) NOT NULL COMMENT '治疗记录编号',
  `apply_id` bigint NOT NULL COMMENT '治疗申请ID',
  `treatment_item_id` bigint NOT NULL COMMENT '治疗项目ID',
  `execute_doctor_id` bigint COMMENT '执行医生ID',
  `nurse_id` bigint COMMENT '执行护士ID',
  `execute_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  `result` varchar(1000) COMMENT '治疗结果描述',
  `record_status` tinyint NOT NULL DEFAULT 1 COMMENT '记录状态（0-异常 1-正常）',
  `remark` varchar(500) COMMENT '备注',
  `exec_seq` int NOT NULL DEFAULT 1 COMMENT '第几次执行',
  `plan_date` date COMMENT '计划执行日期',
  `exec_status` tinyint NOT NULL DEFAULT 0 COMMENT '执行状态（0-待执行 1-已执行 2-已取消）',
  `executor_name` varchar(50) COMMENT '执行人姓名',
  `charge_status` tinyint NOT NULL DEFAULT 0 COMMENT '计费状态（0-未计费 1-已计费 2-计费失败 3-无需计费）',
  `charge_time` datetime COMMENT '计费时间',
  `fee_no` varchar(32) COMMENT '记账单号',
  `fee_record_id` bigint COMMENT '记账行ID',
  `charge_amount` decimal(10,2) COMMENT '本次计费金额',
  `charge_fail_reason` varchar(500) COMMENT '未计费/失败原因',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  UNIQUE KEY `uk_treat_exec` (`apply_id`, `exec_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='治疗执行记录';

-- biz_triage_record  门诊分诊记录
CREATE TABLE `biz_triage_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `queue_id` bigint NOT NULL COMMENT '队列ID',
  `regist_id` bigint COMMENT '挂号ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `patient_no` varchar(32) COMMENT '患者号',
  `temperature` decimal(4,1) COMMENT '体温(℃)',
  `pulse` int COMMENT '脉搏(次/分)',
  `respiration` int COMMENT '呼吸(次/分)',
  `systolic_bp` int COMMENT '收缩压(mmHg)',
  `diastolic_bp` int COMMENT '舒张压(mmHg)',
  `spo2` int COMMENT '血氧饱和度(%)',
  `height` decimal(5,1) COMMENT '身高(cm)',
  `weight` decimal(5,1) COMMENT '体重(kg)',
  `bmi` decimal(4,1) COMMENT 'BMI',
  `pain_score` tinyint COMMENT '疼痛评分',
  `chief_complaint` varchar(500) COMMENT '主诉',
  `triage_level` tinyint COMMENT '分诊等级（1-危重 2-急症 3-亚急 4-非急）',
  `room_id` bigint COMMENT '分配诊室ID',
  `room_name` varchar(64) COMMENT '分配诊室名称',
  `triage_nurse_id` bigint COMMENT '分诊护士员工ID',
  `triage_nurse_name` varchar(50) COMMENT '分诊护士姓名',
  `triage_time` datetime COMMENT '分诊时刻',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊分诊记录';

-- biz_tsa_token  时间戳令牌台账
CREATE TABLE `biz_tsa_token` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `serial` varchar(64) NOT NULL COMMENT '令牌序列号',
  `digest_hex` char(64) NOT NULL COMMENT '被盖时间戳的内容摘要',
  `tsa_time` datetime NOT NULL COMMENT 'TSA 授时时刻',
  `token_value` text NOT NULL COMMENT '令牌值',
  `algo` varchar(32) NOT NULL DEFAULT 'SHA256withRSA' COMMENT '令牌签名算法',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tsa_serial` (`serial`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间戳令牌台账';

-- biz_ultrasound_measure  超声测量值
CREATE TABLE `biz_ultrasound_measure` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_id` bigint NOT NULL COMMENT '超声记录ID',
  `record_no` varchar(32) NOT NULL COMMENT '超声检查号',
  `measure_name` varchar(100) NOT NULL COMMENT '测量项',
  `measure_value` varchar(100) COMMENT '测量值',
  `unit` varchar(20) COMMENT '单位',
  `reference_range` varchar(100) COMMENT '参考范围',
  `abnormal_flag` tinyint DEFAULT 0 COMMENT '异常标志（0-正常 1-偏高 2-偏低 3-异常）',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='超声测量值';

-- biz_ultrasound_record  超声检查记录
CREATE TABLE `biz_ultrasound_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '超声检查号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `visit_date` date COMMENT '就诊日期',
  `apply_dept_id` bigint COMMENT '申请科室ID',
  `apply_dept_name` varchar(100) COMMENT '申请科室',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(50) COMMENT '申请医生',
  `clinical_diagnosis` varchar(500) COMMENT '临床诊断',
  `us_type` tinyint DEFAULT 1 COMMENT '超声类型（1-腹部 2-心脏 3-妇产 4-血管 5-浅表器官 6-肌骨 7-腔内）',
  `body_part` varchar(200) COMMENT '检查部位',
  `exam_purpose` varchar(500) COMMENT '检查目的',
  `findings` text COMMENT '超声所见',
  `conclusion` text COMMENT '超声提示（结论）',
  `suggestion` varchar(1000) COMMENT '建议',
  `sonographer` varchar(64) COMMENT '检查医师',
  `execute_time` datetime COMMENT '检查时间',
  `status` tinyint DEFAULT 1 COMMENT '状态（1-已登记 2-已签到 3-检查中 4-已出报告 5-已审核 6-已发布 7-已取消）',
  `report_by` varchar(64) COMMENT '报告医师',
  `report_time` datetime COMMENT '报告时间',
  `audit_by` varchar(64) COMMENT '审核医师',
  `audit_time` datetime COMMENT '审核时间',
  `publish_by` varchar(64) COMMENT '发布人',
  `publish_time` datetime COMMENT '发布时间',
  `cancel_time` datetime COMMENT '取消时间',
  `cancel_reason` varchar(500) COMMENT '取消原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='超声检查记录';

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

-- biz_vte_event  VTE 事件登记
CREATE TABLE `biz_vte_event` (
  `id` bigint NOT NULL COMMENT '主键',
  `event_no` varchar(32) NOT NULL COMMENT '事件编号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(100) COMMENT '病区名称',
  `event_type` tinyint NOT NULL COMMENT '事件类型（1-深静脉血栓DVT 2-肺栓塞PE 3-预防相关出血）',
  `onset_type` tinyint NOT NULL DEFAULT 1 COMMENT '发生时机（1-院内发生 2-入院时已存在）',
  `diagnose_date` date NOT NULL COMMENT '确诊日期',
  `diagnosis_basis` tinyint COMMENT '诊断依据（1-超声 2-CT肺动脉造影 3-静脉造影 4-临床诊断 5-其他）',
  `thrombus_site` varchar(100) COMMENT '血栓部位',
  `outcome` tinyint COMMENT '转归（1-好转 2-未愈 3-死亡 4-未知）',
  `drug_prevent_flag` tinyint NOT NULL DEFAULT 0 COMMENT '事件发生时是否正在药物预防（0-否 1-是）',
  `reporter_id` bigint COMMENT '登记人（员工ID）',
  `reporter_name` varchar(50) COMMENT '登记人姓名',
  `report_time` datetime COMMENT '登记时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VTE 事件登记';

-- biz_vte_prevent  VTE 预防措施记录
CREATE TABLE `biz_vte_prevent` (
  `id` bigint NOT NULL COMMENT '主键',
  `prevent_no` varchar(32) NOT NULL COMMENT '措施记录编号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(100) COMMENT '病区名称',
  `bed_no` varchar(20) COMMENT '床号',
  `assessment_id` bigint COMMENT '来源评估单ID',
  `caprini_score` int COMMENT 'Caprini 总分',
  `risk_level` tinyint COMMENT '风险等级（1-低 2-中 3-高 4-极高）',
  `measure_code` varchar(32) NOT NULL COMMENT '措施码',
  `measure_type` tinyint NOT NULL COMMENT '措施类别（1-基础预防 2-物理预防 3-药物预防）',
  `measure_name` varchar(200) COMMENT '措施名称',
  `plan_date` date COMMENT '计划执行日期',
  `execute_status` tinyint NOT NULL DEFAULT 0 COMMENT '落实状态（0-待落实 1-已落实 2-禁忌未用 3-患者拒绝）',
  `execute_time` datetime COMMENT '落实时间',
  `executor_id` bigint COMMENT '执行人（员工ID）',
  `executor_name` varchar(50) COMMENT '执行人姓名',
  `reason` varchar(500) COMMENT '未落实原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vte_prevent` (`admission_id`, `measure_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VTE 预防措施记录';

-- biz_vte_stats  VTE 防控月度指标
CREATE TABLE `biz_vte_stats` (
  `id` bigint NOT NULL COMMENT '主键',
  `stat_month` char(7) NOT NULL COMMENT '统计月份',
  `scope_type` tinyint NOT NULL DEFAULT 1 COMMENT '统计范围（1-全院 2-科室）',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(100) COMMENT '科室名称',
  `discharge_count` int NOT NULL DEFAULT 0 COMMENT '同期出院患者数',
  `assessed_count` int NOT NULL DEFAULT 0 COMMENT '其中做过 Caprini 评估的患者数',
  `assess_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT 'VTE 风险评估率（%）',
  `high_risk_count` int NOT NULL DEFAULT 0 COMMENT '其中最新评估为中高危',
  `high_risk_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '中高危占比（%）',
  `prevent_done_count` int NOT NULL DEFAULT 0 COMMENT '中高危中至少落实一条措施的患者数',
  `prevent_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '预防措施落实率（%）',
  `vte_event_count` int NOT NULL DEFAULT 0 COMMENT '院内新发 VTE 患者数',
  `vte_incidence_rate` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '院内 VTE 发生率（%）',
  `bleed_count` int NOT NULL DEFAULT 0 COMMENT '预防相关出血患者数',
  `generate_by` varchar(64) COMMENT '生成人',
  `generate_time` datetime COMMENT '生成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vte_stats` (`stat_month`, `scope_type`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VTE 防控月度指标';

-- biz_ward_dispense  住院摆药单
CREATE TABLE `biz_ward_dispense` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `dispense_no` varchar(32) NOT NULL COMMENT '摆药单号 WD+yyyyMMdd+4位',
  `dispense_date` date NOT NULL COMMENT '摆药日期',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称',
  `dept_id` bigint COMMENT '入院科室ID',
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
  `order_no` varchar(32) COMMENT '医嘱号',
  `admission_id` bigint NOT NULL COMMENT '入院ID（冗余）',
  `patient_id` bigint NOT NULL COMMENT '患者ID（冗余）',
  `patient_no` varchar(64) COMMENT '患者编号',
  `patient_name` varchar(128) COMMENT '患者姓名',
  `ward_id` bigint COMMENT '病区ID',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_name` varchar(128) COMMENT '药品名称',
  `item_code` varchar(64) COMMENT '医嘱项目编码',
  `item_name` varchar(128) COMMENT '医嘱项目名称',
  `spec` varchar(64) COMMENT '规格',
  `unit` varchar(32) COMMENT '单位',
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

-- biz_yb_catalog  国家医保目录
CREATE TABLE `biz_yb_catalog` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `catalog_type` tinyint NOT NULL COMMENT '目录类型（1-西药 2-中药饮片 3-医疗服务项目 4-医用耗材）',
  `yb_code` varchar(64) NOT NULL COMMENT '国家医保编码',
  `yb_name` varchar(200) NOT NULL COMMENT '目录名称',
  `spec` varchar(100) COMMENT '规格',
  `unit` varchar(20) COMMENT '单位',
  `dosage_form` varchar(50) COMMENT '剂型',
  `insurance_level` tinyint COMMENT '甲乙类（1-甲类 2-乙类 3-丙类）',
  `pay_ratio` decimal(5,2) COMMENT '支付比例%',
  `effective_date` date COMMENT '生效日期',
  `expire_date` date COMMENT '失效日期',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_yb_code` (`yb_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='国家医保目录';

-- biz_yb_chronic_catalog  门诊慢特病病种目录
CREATE TABLE `biz_yb_chronic_catalog` (
  `id` bigint NOT NULL COMMENT '主键',
  `disease_code` varchar(32) NOT NULL COMMENT '病种编码',
  `disease_name` varchar(200) NOT NULL COMMENT '病种名称',
  `disease_type` tinyint NOT NULL COMMENT '类别（1-慢性病 2-特殊病）',
  `icd_code` varchar(32) COMMENT '对应 ICD-10 主码',
  `default_valid_months` int COMMENT '默认有效期月数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态（1-启用 0-停用）',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chronic_catalog_code` (`disease_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊慢特病病种目录';

-- biz_yb_chronic_reg  门诊慢特病备案
CREATE TABLE `biz_yb_chronic_reg` (
  `id` bigint NOT NULL COMMENT '主键',
  `reg_no` varchar(32) NOT NULL COMMENT '备案单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名快照',
  `patient_no` varchar(32) COMMENT '患者编号快照',
  `medical_insurance_no` varchar(32) COMMENT '医保卡号快照',
  `catalog_id` bigint NOT NULL COMMENT '病种目录ID',
  `disease_code` varchar(32) NOT NULL COMMENT '病种编码快照',
  `disease_name` varchar(200) NOT NULL COMMENT '病种名称快照',
  `disease_type` tinyint NOT NULL COMMENT '病种类别快照（1-慢性 2-特殊）',
  `certify_dept_id` bigint COMMENT '诊断科室ID',
  `certify_dept_name` varchar(100) COMMENT '诊断科室名称',
  `certify_doctor_name` varchar(64) COMMENT '诊断医师姓名',
  `certify_date` date NOT NULL COMMENT '诊断日期',
  `certify_basis` varchar(500) NOT NULL COMMENT '诊断依据（病历摘要/检验结果/出院小结，必填）',
  `register_dept_id` bigint COMMENT '备案经办机构ID',
  `register_dept_name` varchar(100) COMMENT '备案经办机构名称',
  `register_emp_id` bigint COMMENT '备案经办人ID',
  `register_emp_name` varchar(64) NOT NULL COMMENT '备案经办人姓名',
  `register_date` date NOT NULL COMMENT '备案日期',
  `valid_start` date NOT NULL COMMENT '待遇生效日',
  `valid_end` date COMMENT '待遇终止日',
  `valid_end_key` date NOT NULL COMMENT '唯一键辅助列 = COALESCE',
  `reg_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-有效 2-已注销 3-已驳回）',
  `cancel_reason` varchar(500) COMMENT '注销原因',
  `cancel_by` varchar(64) COMMENT '注销经办人',
  `cancel_time` datetime COMMENT '注销时间',
  `reject_reason` varchar(500) COMMENT '驳回原因',
  `reject_by` varchar(64) COMMENT '驳回经办人',
  `reject_time` datetime COMMENT '驳回时间',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chronic_active` (`patient_id`, `disease_code`, `reg_status`, `valid_end_key`),
  UNIQUE KEY `uk_chronic_reg_no` (`reg_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊慢特病备案';

-- biz_yb_deduct_log  医保扣款处理留痕
CREATE TABLE `biz_yb_deduct_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `notice_id` bigint NOT NULL COMMENT '扣款通知ID',
  `action` tinyint NOT NULL COMMENT '动作（1-新建草稿 2-发起申诉 3-录入申诉结果 4-确认扣款并追责 5-录入缴回 6-作废）',
  `detail` varchar(1000) COMMENT '动作详情/备注',
  `amount` decimal(10,2) COMMENT '涉及金额',
  `operator` varchar(64) NOT NULL COMMENT '操作人',
  `operate_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保扣款处理留痕';

-- biz_yb_deduct_notice  医保扣款通知单
CREATE TABLE `biz_yb_deduct_notice` (
  `id` bigint NOT NULL COMMENT '主键',
  `deduct_no` varchar(32) NOT NULL COMMENT '扣款单号',
  `source_type` tinyint NOT NULL COMMENT '来源（1-飞检现场发现 2-智能审核）',
  `inspection_id` bigint COMMENT '关联飞检批次ID',
  `inspection_no` varchar(32) COMMENT '飞检批次号快照',
  `settlement_id` bigint COMMENT '关联医保结算清单ID（可选）',
  `settlement_no` varchar(32) COMMENT '结算清单号快照',
  `encounter_type` tinyint COMMENT '就诊类型（1-门诊 2-住院）',
  `encounter_id` bigint COMMENT '就诊标识',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名快照',
  `patient_no` varchar(32) COMMENT '患者编号快照',
  `dept_id` bigint COMMENT '被审科室ID',
  `dept_name` varchar(100) COMMENT '被审科室名称快照',
  `doctor_name` varchar(50) COMMENT '责任医师姓名快照',
  `violation_type` tinyint NOT NULL COMMENT '违规类型',
  `violation_desc` varchar(500) NOT NULL COMMENT '违规事实描述',
  `deduct_amount` decimal(10,2) NOT NULL COMMENT '扣款金额',
  `notice_date` date NOT NULL COMMENT '通知日期',
  `handle_deadline` date NOT NULL COMMENT '处理期限',
  `deduct_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待确认 2-申诉中 3-申诉成功 4-维持扣款待缴 5-已缴回 6-已作废）',
  `appeal_reason` varchar(500) COMMENT '申诉理由',
  `appeal_material` varchar(500) COMMENT '申诉材料说明',
  `appeal_by` varchar(64) COMMENT '申诉发起人',
  `appeal_time` datetime COMMENT '申诉时间',
  `appeal_result` tinyint COMMENT '申诉结果（1-成功 2-驳回）',
  `appeal_result_remark` varchar(500) COMMENT '申诉结果说明',
  `appeal_result_by` varchar(64) COMMENT '申诉结果录入人',
  `appeal_result_time` datetime COMMENT '申诉结果录入时间',
  `liable_dept_id` bigint COMMENT '责任科室ID',
  `liable_dept_name` varchar(100) COMMENT '责任科室名称',
  `liable_emp_name` varchar(64) COMMENT '责任人姓名',
  `loss_bear_type` tinyint COMMENT '损失承担方式（1-院方承担 2-科室承担 3-个人承担 4-科室+个人共担）',
  `bear_dept_amount` decimal(10,2) COMMENT '科室承担金额',
  `bear_emp_amount` decimal(10,2) COMMENT '个人承担金额',
  `confirm_by` varchar(64) COMMENT '确认经办人',
  `confirm_time` datetime COMMENT '确认时间',
  `paid_amount` decimal(10,2) COMMENT '实际缴回金额',
  `payback_date` date COMMENT '缴回日期',
  `payback_voucher` varchar(100) COMMENT '缴回凭证号/转账流水',
  `payback_by` varchar(64) COMMENT '缴回经办人',
  `payback_time` datetime COMMENT '缴回录入时间',
  `cancel_reason` varchar(500) COMMENT '作废原因（必填）',
  `cancel_by` varchar(64),
  `cancel_time` datetime,
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_deduct_no` (`deduct_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保扣款通知单';

-- biz_yb_inspection  医保飞检批次
CREATE TABLE `biz_yb_inspection` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `inspect_no` varchar(32) NOT NULL COMMENT '批次号',
  `inspect_type` tinyint NOT NULL COMMENT '检查类型（1-国家飞检 2-省级飞检 3-智能审核转来 4-日常驻点审核）',
  `fund_org` varchar(100) NOT NULL COMMENT '统筹区/医保局名称',
  `inspect_start_date` date NOT NULL COMMENT '审核目标期间起',
  `inspect_end_date` date NOT NULL COMMENT '审核目标期间止',
  `inspect_date` date NOT NULL COMMENT '检查组进驻/通知日期',
  `inspect_team` varchar(200) COMMENT '检查组/审核团队名称',
  `our_receiver` varchar(64) COMMENT '本院接待负责人',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-进行中 2-已结项 3-已作废）',
  `conclusion` varchar(1000) COMMENT '结项结论',
  `conclude_time` datetime COMMENT '结项时间',
  `conclude_by` varchar(64) COMMENT '结项经办人',
  `cancel_reason` varchar(500) COMMENT '作废原因（必填）',
  `cancel_by` varchar(64) COMMENT '作废经办人',
  `cancel_time` datetime COMMENT '作废时间',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inspect_no` (`inspect_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保飞检批次';

-- biz_yb_mapping  医保目录对照
CREATE TABLE `biz_yb_mapping` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `item_type` tinyint NOT NULL COMMENT '院内项目类型（1-药品 2-诊疗项目 3-检验项目 4-耗材）',
  `item_id` bigint NOT NULL COMMENT '院内项目ID',
  `item_code` varchar(32) NOT NULL COMMENT '院内项目编码',
  `item_name` varchar(200) NOT NULL COMMENT '院内项目名称',
  `catalog_id` bigint NOT NULL COMMENT '医保目录ID',
  `yb_code` varchar(64) NOT NULL COMMENT '国家医保编码',
  `yb_name` varchar(200) NOT NULL COMMENT '目录名称',
  `match_type` tinyint NOT NULL DEFAULT 2 COMMENT '对照方式（1-自动名称精确 2-人工 3-导入）',
  `mapped_by` varchar(64) COMMENT '对照人',
  `mapped_time` datetime COMMENT '对照时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item` (`item_type`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保目录对照';

-- sys_ai_call_log  AI 调用日志
CREATE TABLE `sys_ai_call_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `capability_key` varchar(64) NOT NULL COMMENT '能力标识',
  `biz_type` varchar(32) COMMENT '业务类型',
  `biz_id` bigint COMMENT '业务ID',
  `provider` varchar(32) COMMENT '提供方标识',
  `model` varchar(64) COMMENT '模型名',
  `prompt_version` varchar(32) COMMENT '提示词版本号',
  `input_digest` varchar(512) COMMENT '输入摘要',
  `output_digest` varchar(512) COMMENT '输出摘要',
  `prompt_tokens` int COMMENT '输入 token 数',
  `completion_tokens` int COMMENT '输出 token 数',
  `latency_ms` int COMMENT '调用耗时（毫秒）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-成功 2-失败 3-超时 4-降级 5-熔断（1-成功 2-失败 3-超时 4-降级 5-熔断）',
  `error_msg` varchar(500) COMMENT '失败原因',
  `operator` varchar(64) COMMENT '调用人',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '调用时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用日志';

-- sys_alert_rule  预警规则
CREATE TABLE `sys_alert_rule` (
  `rule_id` bigint NOT NULL COMMENT '规则ID',
  `rule_name` varchar(64) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(32) NOT NULL COMMENT '规则类型',
  `rule_condition` varchar(256) COMMENT '规则条件表达式',
  `threshold` int COMMENT '阈值',
  `notify_channel` varchar(32) DEFAULT 'system' COMMENT '通知渠道',
  `is_active` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用（0-停用 1-启用）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警规则';

-- sys_attachment  附件
CREATE TABLE `sys_attachment` (
  `attachment_id` bigint NOT NULL COMMENT '附件ID',
  `attachment_name` varchar(256) NOT NULL COMMENT '附件名称',
  `attachment_type` varchar(32) COMMENT '附件类型',
  `file_path` varchar(512) NOT NULL COMMENT '文件存储路径',
  `file_size` bigint DEFAULT 0 COMMENT '文件大小(字节)',
  `file_md5` varchar(32) COMMENT '文件MD5校验值',
  `biz_type` varchar(32) COMMENT '业务类型',
  `biz_id` bigint COMMENT '关联业务ID',
  `upload_user_id` bigint COMMENT '上传人ID',
  `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`attachment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件';

-- sys_audit_log  审计日志
CREATE TABLE `sys_audit_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint COMMENT '操作人ID',
  `user_name` varchar(64) COMMENT '操作人姓名',
  `module` varchar(50) COMMENT '模块名称',
  `operation` varchar(50) COMMENT '操作类型',
  `target_id` varchar(50) COMMENT '操作对象ID',
  `target_type` varchar(50) COMMENT '操作对象类型',
  `content` text COMMENT '操作内容',
  `ip` varchar(50) COMMENT 'IP地址',
  `status` tinyint DEFAULT 1 COMMENT '状态（1-成功 0-失败）',
  `error_msg` varchar(500) COMMENT '错误信息',
  `create_by` varchar(64),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';

-- sys_bed  床位
CREATE TABLE `sys_bed` (
  `bed_id` bigint NOT NULL COMMENT '床位ID',
  `bed_no` varchar(16) NOT NULL COMMENT '床位号',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `bed_type` varchar(32) COMMENT '床位类型',
  `bed_status` tinyint NOT NULL DEFAULT 1 COMMENT '床位状态（0-维修 1-空闲 2-占用 3-锁定）',
  `patient_id` bigint COMMENT '当前占用患者ID',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`bed_id`),
  UNIQUE KEY `uk_bed_no` (`ward_id`, `bed_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='床位';

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
  `update_time` datetime COMMENT '更新时间',
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
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检套餐项目';

-- sys_clinic_room  诊室
CREATE TABLE `sys_clinic_room` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '诊室名称',
  `queue_prefix` varchar(2) COMMENT '呼叫代号（队列号前缀，如A/B/C…）',
  `code` varchar(100) NOT NULL COMMENT '诊室编号（ABCD）',
  `location` varchar(200) NOT NULL COMMENT '地理位置',
  `dept_id` bigint NOT NULL COMMENT '所属科室ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '诊室状态（1-启用 0-停用）',
  `remark` varchar(255) COMMENT '备注信息',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诊室';

-- sys_config  系统参数
CREATE TABLE `sys_config` (
  `config_id` bigint NOT NULL COMMENT '配置ID',
  `config_name` varchar(64) NOT NULL COMMENT '配置名称',
  `config_key` varchar(64) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) COMMENT '配置值',
  `config_type` tinyint NOT NULL DEFAULT 0 COMMENT '类型（0-系统 1-业务）',
  `is_system` tinyint NOT NULL DEFAULT 0 COMMENT '是否系统内置（0-否 1-是）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数';

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

-- sys_dict_data  字典数据
CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dict_type` varchar(100) NOT NULL COMMENT '字典类型',
  `dict_label` varchar(100) NOT NULL COMMENT '字典标签',
  `dict_value` varchar(100) NOT NULL COMMENT '字典值',
  `dict_sort` int DEFAULT 0 COMMENT '排序号',
  `dict_class` varchar(100) COMMENT '样式属性',
  `list_class` varchar(100) COMMENT '表格回显样式',
  `is_default` tinyint DEFAULT 0 COMMENT '是否默认（0-否 1-是）',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `dict_source` tinyint DEFAULT 2 COMMENT '来源（1-系统级 2-自定义）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据';

-- sys_dict_type  字典类型
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dict_type` varchar(100) NOT NULL COMMENT '字典类型（唯一）',
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `dict_source` tinyint DEFAULT 2 COMMENT '来源（1-系统级 2-自定义）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

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

-- sys_employee_post  员工岗位（角色×科室）
CREATE TABLE `sys_employee_post` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `is_primary` tinyint DEFAULT 0 COMMENT '是否主科室（0-否 1-是）',
  `effective_date` date COMMENT '岗位生效日期',
  `expire_date` date COMMENT '岗位失效日期',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_role_dept` (`employee_id`, `role_id`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工岗位（角色×科室）';

-- sys_employee_qualification  员工资格证书
CREATE TABLE `sys_employee_qualification` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `cert_type` varchar(8) NOT NULL COMMENT '证书类型（2-医师执业证 3-护士执业证 4-药师资格证 5-技术职称聘书 9-其他）',
  `cert_no` varchar(64) NOT NULL COMMENT '证书编号',
  `issue_org` varchar(8) COMMENT '发证机关',
  `issue_date` date COMMENT '发证日期',
  `valid_until` date COMMENT '有效期至',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `remark` varchar(512) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_cert_type_no` (`employee_id`, `cert_type`, `cert_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工资格证书';

-- sys_employee_tech_auth  医疗技术授权台账
CREATE TABLE `sys_employee_tech_auth` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_name` varchar(50) NOT NULL COMMENT '员工姓名',
  `dept_id` bigint COMMENT '所属科室ID',
  `dept_name` varchar(200) COMMENT '所属科室名称',
  `title` varchar(50) COMMENT '职称',
  `auth_category` tinyint NOT NULL COMMENT '授权类别（1-手术 2-麻醉 3-内镜与介入）',
  `tech_level` tinyint NOT NULL COMMENT '可独立操作的手术级别上限',
  `item_scope` varchar(500) COMMENT '限定术式编码白名单',
  `auth_type` tinyint NOT NULL DEFAULT 1 COMMENT '授权方式（1-独立授权 2-上级指导下 3-限制授权须上级在场）',
  `auth_basis` varchar(200) COMMENT '授权依据（技术准入评价/培训考核/累计手术量，评审要看依据）',
  `valid_from` date NOT NULL COMMENT '授权生效日期',
  `valid_until` date COMMENT '授权有效期至',
  `auth_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待审批 2-已授权 3-已驳回 4-已收回）',
  `apply_by` varchar(64) COMMENT '申请（登记）',
  `apply_time` datetime COMMENT '申请时间',
  `approver_id` bigint COMMENT '审批人',
  `approver_name` varchar(50) COMMENT '审批人姓名',
  `approve_time` datetime COMMENT '审批时间',
  `approve_opinion` varchar(500) COMMENT '审批意见',
  `revoke_by` varchar(64) COMMENT '收回人',
  `revoke_time` datetime COMMENT '收回时间',
  `revoke_reason` varchar(500) COMMENT '收回原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_cat_from` (`employee_id`, `auth_category`, `valid_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗技术授权台账';

-- sys_equipment  医疗设备台账
CREATE TABLE `sys_equipment` (
  `id` bigint NOT NULL COMMENT '主键',
  `equipment_code` varchar(32) NOT NULL COMMENT '设备编码',
  `equipment_name` varchar(200) NOT NULL COMMENT '设备名称',
  `category` tinyint NOT NULL COMMENT '设备类别',
  `dept_id` bigint COMMENT '使用科室ID',
  `dept_name` varchar(100) COMMENT '使用科室名称',
  `brand` varchar(100) COMMENT '品牌',
  `model` varchar(100) COMMENT '型号',
  `purchase_date` date COMMENT '购置日期',
  `purchase_price` decimal(14,2) COMMENT '购置价格(元)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-在用 2-停用 3-维修中 4-报废）',
  `maintain_cycle_days` int DEFAULT 365 COMMENT '维保周期(天)',
  `last_maintain_date` date COMMENT '最近维保日期',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_code` (`equipment_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗设备台账';

-- sys_field_change_log  字段级修改日志
CREATE TABLE `sys_field_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_type` varchar(32) NOT NULL COMMENT '对象类型',
  `biz_id` varchar(64) NOT NULL COMMENT '对象ID',
  `biz_no` varchar(64) COMMENT '对象编号快照（患者号/工号/病历号）',
  `biz_name` varchar(128) COMMENT '对象名称快照',
  `field_name` varchar(64) NOT NULL COMMENT '字段英文名',
  `field_label` varchar(64) NOT NULL COMMENT '字段中文名',
  `old_value` varchar(500) COMMENT '变更前值',
  `new_value` varchar(500) COMMENT '变更后值',
  `change_type` varchar(16) NOT NULL DEFAULT 'UPDATE' COMMENT '变更类型',
  `batch_no` varchar(48) NOT NULL COMMENT '批次号',
  `operator_id` bigint COMMENT '操作人ID',
  `operator_name` varchar(64) COMMENT '操作人姓名',
  `dept_id` bigint COMMENT '操作人科室ID',
  `dept_name` varchar(64) COMMENT '操作人科室名称',
  `change_time` datetime NOT NULL COMMENT '变更时间',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段级修改日志';

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
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_op_code` (`op_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ICD-9-CM-3 手术编码';

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

-- sys_insurance_policy  医保政策配置
CREATE TABLE `sys_insurance_policy` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `policy_name` varchar(100) NOT NULL COMMENT '政策名称',
  `insurance_type` varchar(50) NOT NULL COMMENT '医保类型',
  `settlement_type` tinyint COMMENT '结算方式（2-城镇职工医保 3-城乡居民医保 4-公费医疗）',
  `coverage_ratio` decimal(5,2) NOT NULL COMMENT '统筹比例',
  `self_pay_ratio` decimal(5,2) DEFAULT 10.00 COMMENT '乙类药品自付比例',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime COMMENT '更新时间',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医保政策配置';

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

-- sys_login_log  登录日志
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_name` varchar(64) COMMENT '用户名',
  `user_id` bigint COMMENT '用户ID',
  `real_name` varchar(64) COMMENT '真实姓名',
  `login_ip` varchar(50) COMMENT '登录IP',
  `login_location` varchar(200) COMMENT '登录地点',
  `browser` varchar(100) COMMENT '浏览器类型',
  `os` varchar(100) COMMENT '操作系统',
  `user_agent` varchar(500) COMMENT '用户代理',
  `login_status` tinyint DEFAULT 0 COMMENT '登录状态（0-成功 1-失败）',
  `msg` varchar(200) COMMENT '提示消息',
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- sys_menu  菜单
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `menu_name` varchar(100) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父菜单ID',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `menu_type` tinyint DEFAULT 1 COMMENT '菜单类型（1-目录 2-菜单 3-按钮）',
  `path` varchar(200) COMMENT '路由地址',
  `component` varchar(200) COMMENT '组件路径',
  `menu_key` varchar(100) COMMENT '菜单标识（唯一）',
  `icon` varchar(100) COMMENT '图标',
  `permission` varchar(200) COMMENT '权限标识',
  `is_frame` tinyint DEFAULT 0 COMMENT '是否外链（0-否 1-是）',
  `is_cache` tinyint DEFAULT 0 COMMENT '是否缓存（0-否 1-是）',
  `is_visible` tinyint DEFAULT 1 COMMENT '是否可见（0-隐藏 1-显示）',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_key` (`menu_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单';

-- sys_message  消息通知
CREATE TABLE `sys_message` (
  `message_id` bigint NOT NULL COMMENT '消息ID',
  `message_no` varchar(32) NOT NULL COMMENT '消息编号',
  `channel` varchar(32) NOT NULL COMMENT '发送渠道',
  `receiver_id` bigint NOT NULL COMMENT '接收人ID',
  `receiver_name` varchar(64) COMMENT '接收人姓名',
  `title` varchar(128) COMMENT '消息标题',
  `content` varchar(2000) NOT NULL COMMENT '消息内容',
  `biz_type` varchar(32) COMMENT '业务类型',
  `biz_id` bigint COMMENT '关联业务ID',
  `severity` varchar(16) NOT NULL DEFAULT 'info' COMMENT '紧急度',
  `payload` text COMMENT '结构化负载(JSON)',
  `handle_status` tinyint COMMENT '处理状态（0-待处理 1-已处理 2-已关闭）',
  `send_status` tinyint NOT NULL DEFAULT 0 COMMENT '发送状态（0-待发送 1-已发送 2-发送失败）',
  `send_time` datetime COMMENT '发送时间',
  `error_msg` varchar(500) COMMENT '渠道发送失败原因',
  `read_status` tinyint NOT NULL DEFAULT 0 COMMENT '阅读状态（0-未读 1-已读）',
  `read_time` datetime COMMENT '阅读时间',
  PRIMARY KEY (`message_id`),
  UNIQUE KEY `uk_message_no` (`message_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';

-- sys_nursing_qc_item  护理质控检查项目录
CREATE TABLE `sys_nursing_qc_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `item_code` varchar(32) NOT NULL COMMENT '项目编码（BN/SC/SF/DC/IP + 两位序号）',
  `item_name` varchar(128) NOT NULL COMMENT '检查项目名称',
  `category` tinyint NOT NULL COMMENT '检查类别（1-基础护理 2-专科护理 3-安全管理 4-护理文书 5-院感防控）',
  `indicator_code` varchar(32) COMMENT '计入的台账指标编码',
  `standard` varchar(500) COMMENT '评价标准',
  `full_score` decimal(5,1) NOT NULL DEFAULT 0.0 COMMENT '本项应得分',
  `target_rate` decimal(5,2) COMMENT '单项目标合格率（%）',
  `key_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否重点项',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '同类别内排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qc_item_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理质控检查项目录';

-- sys_oper_log  操作日志
CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `title` varchar(100) COMMENT '操作模块',
  `business_type` tinyint DEFAULT 0 COMMENT '业务类型（0-其他 1-新增 2-修改 3-删除 4-授权 5-导出 6-导入 7-清空）',
  `method` varchar(200) COMMENT '方法名称',
  `request_method` varchar(10) COMMENT '请求方式（GET/POST/PUT/DELETE）',
  `oper_name` varchar(50) COMMENT '操作人员',
  `oper_id` bigint COMMENT '操作人员ID',
  `dept_name` varchar(100) COMMENT '部门名称',
  `dept_id` bigint COMMENT '部门ID',
  `oper_url` varchar(500) COMMENT '请求URL',
  `oper_ip` varchar(50) COMMENT '操作IP',
  `oper_location` varchar(200) COMMENT '操作地点',
  `oper_param` text COMMENT '请求参数',
  `json_result` text COMMENT '返回参数',
  `status` tinyint DEFAULT 0 COMMENT '操作状态（0-正常 1-异常）',
  `error_msg` text COMMENT '错误消息',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `cost_time` bigint DEFAULT 0 COMMENT '消耗时间（毫秒）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- sys_operation_room  手术间
CREATE TABLE `sys_operation_room` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `room_code` varchar(32) NOT NULL COMMENT '手术间编码',
  `room_name` varchar(64) NOT NULL COMMENT '手术间名称',
  `location` varchar(200) COMMENT '位置',
  `sort_order` int NOT NULL DEFAULT 1 COMMENT '总表列顺序（升序）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_code` (`room_code`),
  UNIQUE KEY `uk_room_name` (`room_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术间';

-- sys_patient_tag  患者标签
CREATE TABLE `sys_patient_tag` (
  `tag_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `short_name` varchar(2) COMMENT '标签缩写用于展示',
  `tag_color` varchar(20) COMMENT '标签颜色',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者标签';

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

-- sys_role  角色
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_code` varchar(32) NOT NULL COMMENT '角色编码（唯一）',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_type` tinyint DEFAULT 1 COMMENT '角色类型（1-系统角色 2-自定义角色）',
  `staff_type` tinyint COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `data_scope` tinyint DEFAULT 1 COMMENT '数据权限范围（1-全部数据 2-自定义数据 3-本部门数据 4-本部门及以下 5-仅本人数据）',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- sys_role_menu  角色菜单关联
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- sys_sign_cert  电子签名证书
CREATE TABLE `sys_sign_cert` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `cert_no` varchar(32) NOT NULL COMMENT '证书编号',
  `emp_id` bigint NOT NULL COMMENT '签名人员工ID',
  `emp_name` varchar(64) NOT NULL COMMENT '签名人姓名',
  `dept_id` bigint COMMENT '所属科室ID',
  `dept_name` varchar(64) COMMENT '所属科室名称',
  `key_algo` varchar(16) NOT NULL DEFAULT 'RSA2048' COMMENT '密钥算法',
  `digest_algo` varchar(16) NOT NULL DEFAULT 'SHA256' COMMENT '摘要算法',
  `sign_algo` varchar(32) NOT NULL DEFAULT 'SHA256withRSA' COMMENT '签名算法',
  `public_key` text NOT NULL COMMENT '公钥',
  `key_fingerprint` varchar(64) NOT NULL COMMENT '公钥指纹',
  `protected_private_key` text NOT NULL COMMENT '私钥密文',
  `key_salt` varchar(64) NOT NULL COMMENT '私钥派生盐',
  `key_iterations` int NOT NULL COMMENT '私钥派生迭代次数',
  `issued_mode` tinyint NOT NULL DEFAULT 1 COMMENT '签发方式（1-人工签发 2-系统自动签发）',
  `cert_status` tinyint NOT NULL DEFAULT 1 COMMENT '证书状态（1-有效 2-已吊销）',
  `valid_from` datetime NOT NULL COMMENT '生效时间',
  `valid_to` datetime NOT NULL COMMENT '失效时间',
  `revoke_reason` varchar(200) COMMENT '吊销原因',
  `revoke_time` datetime COMMENT '吊销时间',
  `revoke_by` bigint COMMENT '吊销操作人员工ID',
  `revoke_by_name` varchar(64) COMMENT '吊销操作人姓名',
  `last_used_time` datetime COMMENT '最近一次使用时间',
  `sign_count` int NOT NULL DEFAULT 0 COMMENT '累计签名次数',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cert_no` (`cert_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子签名证书';

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

-- sys_tsa_server  时间戳服务注册
CREATE TABLE `sys_tsa_server` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tsa_code` varchar(32) NOT NULL COMMENT 'TSA服务编码',
  `tsa_name` varchar(64) NOT NULL COMMENT 'TSA服务名称',
  `public_pem` text NOT NULL COMMENT 'TSA公钥',
  `key_fingerprint` varchar(64) NOT NULL COMMENT '公钥指纹',
  `protected_private_key` text NOT NULL COMMENT '私钥密文',
  `key_salt` varchar(64) NOT NULL COMMENT '私钥派生盐',
  `key_iterations` int NOT NULL COMMENT '私钥派生迭代次数',
  `tsa_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tsa_code` (`tsa_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间戳服务注册';

-- sys_user  用户
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_name` varchar(64) NOT NULL COMMENT '用户名（唯一）',
  `password` varchar(200) NOT NULL COMMENT '密码',
  `real_name` varchar(64) COMMENT '真实姓名',
  `emp_id` bigint COMMENT '关联员工ID',
  `user_type` tinyint DEFAULT 1 COMMENT '用户类型（1-系统用户 2-外部用户）',
  `patient_id` bigint COMMENT '关联患者ID',
  `openid` varchar(64) COMMENT '微信openid',
  `avatar` varchar(200) COMMENT '头像地址',
  `last_login_time` datetime COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COMMENT '最后登录IP',
  `login_count` int DEFAULT 0 COMMENT '登录次数',
  `password_update_time` datetime COMMENT '密码更新时间',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用 2-锁定）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

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

-- sys_workbench_layout  工作台个人布局
CREATE TABLE `sys_workbench_layout` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `widget_code` varchar(64) NOT NULL COMMENT '卡片编码',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '个人顺序',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '个人显隐',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_layout_user_widget` (`user_id`, `widget_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作台个人布局';

-- sys_workbench_role  角色工作台配置
CREATE TABLE `sys_workbench_role` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `widget_id` bigint NOT NULL COMMENT '卡片ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '该角色下的卡片顺序',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否展示',
  `landing_scope` tinyint NOT NULL DEFAULT 0 COMMENT '登录/切角色落点（0-默认 1-一律工作台 2-一律患者工作站）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_role_widget` (`role_id`, `widget_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色工作台配置';

-- sys_workbench_widget  工作台卡片注册表
CREATE TABLE `sys_workbench_widget` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `widget_code` varchar(64) NOT NULL COMMENT '卡片编码',
  `widget_name` varchar(64) NOT NULL COMMENT '卡片标题',
  `area` varchar(16) NOT NULL DEFAULT 'domain' COMMENT '归属区域',
  `api_key` varchar(128) NOT NULL COMMENT '取数来源标识',
  `permission` varchar(128) COMMENT '可见所需权限码',
  `default_span` int NOT NULL DEFAULT 6 COMMENT '栅格占宽',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '展示顺序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已上线可挂载 0-注册表先占位）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workbench_widget_code` (`widget_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作台卡片注册表';
-- ---------------- 参照关系（E-R 连线） ----------------
ALTER TABLE `biz_admission` ADD CONSTRAINT `fk_biz_admission_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_admission` ADD CONSTRAINT `fk_biz_admission_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_visit` (`visit_id`);
ALTER TABLE `biz_admission` ADD CONSTRAINT `fk_biz_admission_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_admission` ADD CONSTRAINT `fk_biz_admission_admission_order_id` FOREIGN KEY (`admission_order_id`) REFERENCES `biz_admission_order` (`id`);
ALTER TABLE `biz_admission` ADD CONSTRAINT `fk_biz_admission_admit_dept_id` FOREIGN KEY (`admit_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_admission` ADD CONSTRAINT `fk_biz_admission_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_admission` ADD CONSTRAINT `fk_biz_admission_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_admission` ADD CONSTRAINT `fk_biz_admission_bed_id` FOREIGN KEY (`bed_id`) REFERENCES `sys_bed` (`bed_id`);
ALTER TABLE `biz_admission` ADD CONSTRAINT `fk_biz_admission_admit_doctor_id` FOREIGN KEY (`admit_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_admission_order` ADD CONSTRAINT `fk_biz_admission_order_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_admission_order` ADD CONSTRAINT `fk_biz_admission_order_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_admission_order` ADD CONSTRAINT `fk_biz_admission_order_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_visit` (`visit_id`);
ALTER TABLE `biz_admission_order` ADD CONSTRAINT `fk_biz_admission_order_source_dept_id` FOREIGN KEY (`source_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_admission_order` ADD CONSTRAINT `fk_biz_admission_order_source_doctor_id` FOREIGN KEY (`source_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_admission_order` ADD CONSTRAINT `fk_biz_admission_order_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_admission_order` ADD CONSTRAINT `fk_biz_admission_order_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_admission_order` ADD CONSTRAINT `fk_biz_admission_order_admit_dept_id` FOREIGN KEY (`admit_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_occur_dept_id` FOREIGN KEY (`occur_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_occur_ward_id` FOREIGN KEY (`occur_ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_visit` (`visit_id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_reporter_id` FOREIGN KEY (`reporter_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_handler_id` FOREIGN KEY (`handler_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_rectify_by_id` FOREIGN KEY (`rectify_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_adverse_event` ADD CONSTRAINT `fk_biz_adverse_event_close_by_id` FOREIGN KEY (`close_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_alert` ADD CONSTRAINT `fk_biz_alert_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `sys_alert_rule` (`rule_id`);
ALTER TABLE `biz_alert` ADD CONSTRAINT `fk_biz_alert_notify_user_id` FOREIGN KEY (`notify_user_id`) REFERENCES `sys_user` (`id`);
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
ALTER TABLE `biz_antibiotic_alias` ADD CONSTRAINT `fk_biz_antibiotic_alias_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_antibiotic_auth` ADD CONSTRAINT `fk_biz_antibiotic_auth_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_antibiotic_auth` ADD CONSTRAINT `fk_biz_antibiotic_auth_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_operation_apply_id` FOREIGN KEY (`operation_apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_antibiotic_stats` ADD CONSTRAINT `fk_biz_antibiotic_stats_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_schedule_id` FOREIGN KEY (`schedule_id`) REFERENCES `biz_schedule` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_slot_id` FOREIGN KEY (`slot_id`) REFERENCES `biz_schedule_slot` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_revisit_record_id` FOREIGN KEY (`revisit_record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_create_by_id` FOREIGN KEY (`create_by_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_appoint_info` ADD CONSTRAINT `fk_biz_appoint_info_update_by_id` FOREIGN KEY (`update_by_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_archive_borrow` ADD CONSTRAINT `fk_biz_archive_borrow_archive_id` FOREIGN KEY (`archive_id`) REFERENCES `biz_medical_record_archive` (`id`);
ALTER TABLE `biz_archive_borrow` ADD CONSTRAINT `fk_biz_archive_borrow_applicant_id` FOREIGN KEY (`applicant_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_archive_borrow` ADD CONSTRAINT `fk_biz_archive_borrow_audit_by_id` FOREIGN KEY (`audit_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_archive_code_task` ADD CONSTRAINT `fk_biz_archive_code_task_archive_id` FOREIGN KEY (`archive_id`) REFERENCES `biz_medical_record_archive` (`id`);
ALTER TABLE `biz_archive_code_task` ADD CONSTRAINT `fk_biz_archive_code_task_coder_id` FOREIGN KEY (`coder_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_archive_code_task` ADD CONSTRAINT `fk_biz_archive_code_task_audit_by_id` FOREIGN KEY (`audit_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_bed_allocate` ADD CONSTRAINT `fk_biz_bed_allocate_bed_id` FOREIGN KEY (`bed_id`) REFERENCES `sys_bed` (`bed_id`);
ALTER TABLE `biz_bed_allocate` ADD CONSTRAINT `fk_biz_bed_allocate_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_bed_allocate` ADD CONSTRAINT `fk_biz_bed_allocate_own_dept_id` FOREIGN KEY (`own_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_bed_allocate` ADD CONSTRAINT `fk_biz_bed_allocate_use_dept_id` FOREIGN KEY (`use_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_bed_allocate` ADD CONSTRAINT `fk_biz_bed_allocate_wait_id` FOREIGN KEY (`wait_id`) REFERENCES `biz_bed_wait` (`id`);
ALTER TABLE `biz_bed_allocate` ADD CONSTRAINT `fk_biz_bed_allocate_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_bed_allocate` ADD CONSTRAINT `fk_biz_bed_allocate_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_bed_allocate` ADD CONSTRAINT `fk_biz_bed_allocate_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_bed_wait` ADD CONSTRAINT `fk_biz_bed_wait_admission_order_id` FOREIGN KEY (`admission_order_id`) REFERENCES `biz_admission_order` (`id`);
ALTER TABLE `biz_bed_wait` ADD CONSTRAINT `fk_biz_bed_wait_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_bed_wait` ADD CONSTRAINT `fk_biz_bed_wait_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_bed_wait` ADD CONSTRAINT `fk_biz_bed_wait_expect_ward_id` FOREIGN KEY (`expect_ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_bed_wait` ADD CONSTRAINT `fk_biz_bed_wait_assigned_bed_id` FOREIGN KEY (`assigned_bed_id`) REFERENCES `sys_bed` (`bed_id`);
ALTER TABLE `biz_bed_wait` ADD CONSTRAINT `fk_biz_bed_wait_assigned_ward_id` FOREIGN KEY (`assigned_ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_bed_wait` ADD CONSTRAINT `fk_biz_bed_wait_assigned_dept_id` FOREIGN KEY (`assigned_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_bed_wait` ADD CONSTRAINT `fk_biz_bed_wait_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_blood_crossmatch` ADD CONSTRAINT `fk_biz_blood_crossmatch_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_cashier_settlement` ADD CONSTRAINT `fk_biz_cashier_settlement_cashier_id` FOREIGN KEY (`cashier_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_cashier_settlement` ADD CONSTRAINT `fk_biz_cashier_settlement_day_settlement_id` FOREIGN KEY (`day_settlement_id`) REFERENCES `biz_day_settlement` (`id`);
ALTER TABLE `biz_checkup_record` ADD CONSTRAINT `fk_biz_checkup_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_checkup_record` ADD CONSTRAINT `fk_biz_checkup_record_package_id` FOREIGN KEY (`package_id`) REFERENCES `sys_checkup_package` (`id`);
ALTER TABLE `biz_checkup_result` ADD CONSTRAINT `fk_biz_checkup_result_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_checkup_record` (`id`);
ALTER TABLE `biz_chronic_record` ADD CONSTRAINT `fk_biz_chronic_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_chronic_record` ADD CONSTRAINT `fk_biz_chronic_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_chronic_record` ADD CONSTRAINT `fk_biz_chronic_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_clinical_rule_check` ADD CONSTRAINT `fk_biz_clinical_rule_check_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_clinical_rule_check` ADD CONSTRAINT `fk_biz_clinical_rule_check_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_compliance_audit` ADD CONSTRAINT `fk_biz_compliance_audit_settlement_id` FOREIGN KEY (`settlement_id`) REFERENCES `biz_insurance_settlement` (`id`);
ALTER TABLE `biz_compliance_audit` ADD CONSTRAINT `fk_biz_compliance_audit_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_compliance_audit_item` ADD CONSTRAINT `fk_biz_compliance_audit_item_audit_id` FOREIGN KEY (`audit_id`) REFERENCES `biz_compliance_audit` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_visit` (`visit_id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_from_dept_id` FOREIGN KEY (`from_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_to_dept_id` FOREIGN KEY (`to_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_accept_doctor_id` FOREIGN KEY (`accept_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inpatient_record` (`id`);
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
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_witness_doctor_id` FOREIGN KEY (`witness_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_critical_value` ADD CONSTRAINT `fk_biz_critical_value_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_laboratory_record` (`id`);
ALTER TABLE `biz_critical_value` ADD CONSTRAINT `fk_biz_critical_value_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_critical_value` ADD CONSTRAINT `fk_biz_critical_value_report_dept_id` FOREIGN KEY (`report_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_cssd_pack` ADD CONSTRAINT `fk_biz_cssd_pack_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_cssd_pack_template_item` ADD CONSTRAINT `fk_biz_cssd_pack_template_item_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_cssd_pack_template` (`id`);
ALTER TABLE `biz_cssd_trace` ADD CONSTRAINT `fk_biz_cssd_trace_pack_id` FOREIGN KEY (`pack_id`) REFERENCES `biz_cssd_pack` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_item_id` FOREIGN KEY (`item_id`) REFERENCES `biz_day_surgery_item` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_day_surgery_apply` ADD CONSTRAINT `fk_biz_day_surgery_apply_transfer_admission_id` FOREIGN KEY (`transfer_admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_day_surgery_follow` ADD CONSTRAINT `fk_biz_day_surgery_follow_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_day_surgery_apply` (`id`);
ALTER TABLE `biz_day_surgery_follow` ADD CONSTRAINT `fk_biz_day_surgery_follow_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_day_surgery_item` ADD CONSTRAINT `fk_biz_day_surgery_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
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
ALTER TABLE `biz_dept_cost_month` ADD CONSTRAINT `fk_biz_dept_cost_month_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_diag_template` ADD CONSTRAINT `fk_biz_diag_template_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_dialysis_patient` ADD CONSTRAINT `fk_biz_dialysis_patient_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_dialysis_prescription` ADD CONSTRAINT `fk_biz_dialysis_prescription_archive_id` FOREIGN KEY (`archive_id`) REFERENCES `biz_dialysis_patient` (`id`);
ALTER TABLE `biz_dialysis_prescription` ADD CONSTRAINT `fk_biz_dialysis_prescription_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_dialysis_session` ADD CONSTRAINT `fk_biz_dialysis_session_machine_id` FOREIGN KEY (`machine_id`) REFERENCES `biz_dialysis_machine` (`id`);
ALTER TABLE `biz_dialysis_session` ADD CONSTRAINT `fk_biz_dialysis_session_archive_id` FOREIGN KEY (`archive_id`) REFERENCES `biz_dialysis_patient` (`id`);
ALTER TABLE `biz_dialysis_session` ADD CONSTRAINT `fk_biz_dialysis_session_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_dialysis_session` ADD CONSTRAINT `fk_biz_dialysis_session_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_dialysis_prescription` (`id`);
ALTER TABLE `biz_diet_plan` ADD CONSTRAINT `fk_biz_diet_plan_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_diet_plan` ADD CONSTRAINT `fk_biz_diet_plan_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_diet_plan` ADD CONSTRAINT `fk_biz_diet_plan_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_diet_plan` ADD CONSTRAINT `fk_biz_diet_plan_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_diet_plan` ADD CONSTRAINT `fk_biz_diet_plan_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_inpatient_order` (`id`);
ALTER TABLE `biz_diet_plan` ADD CONSTRAINT `fk_biz_diet_plan_confirmer_id` FOREIGN KEY (`confirmer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_discharge` ADD CONSTRAINT `fk_biz_discharge_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_discharge` ADD CONSTRAINT `fk_biz_discharge_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_discharge` ADD CONSTRAINT `fk_biz_discharge_discharge_doctor_id` FOREIGN KEY (`discharge_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_discharge_drug` ADD CONSTRAINT `fk_biz_discharge_drug_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_discharge_drug` ADD CONSTRAINT `fk_biz_discharge_drug_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_discharge_drug` ADD CONSTRAINT `fk_biz_discharge_drug_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_discharge_drug` ADD CONSTRAINT `fk_biz_discharge_drug_dispense_by` FOREIGN KEY (`dispense_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_dispute_case` ADD CONSTRAINT `fk_biz_dispute_case_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_dispute_case` ADD CONSTRAINT `fk_biz_dispute_case_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_dispute_case` ADD CONSTRAINT `fk_biz_dispute_case_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_dispute_case` ADD CONSTRAINT `fk_biz_dispute_case_archive_id` FOREIGN KEY (`archive_id`) REFERENCES `biz_medical_record_archive` (`id`);
ALTER TABLE `biz_dispute_flow` ADD CONSTRAINT `fk_biz_dispute_flow_case_id` FOREIGN KEY (`case_id`) REFERENCES `biz_dispute_case` (`id`);
ALTER TABLE `biz_dispute_flow` ADD CONSTRAINT `fk_biz_dispute_flow_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_drg_sim_result` ADD CONSTRAINT `fk_biz_drg_sim_result_summary_id` FOREIGN KEY (`summary_id`) REFERENCES `biz_inpatient_summary` (`id`);
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
ALTER TABLE `biz_duty_log` ADD CONSTRAINT `fk_biz_duty_log_roster_id` FOREIGN KEY (`roster_id`) REFERENCES `biz_duty_roster` (`id`);
ALTER TABLE `biz_duty_log` ADD CONSTRAINT `fk_biz_duty_log_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_duty_log` ADD CONSTRAINT `fk_biz_duty_log_handover_emp_id` FOREIGN KEY (`handover_emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_duty_roster` ADD CONSTRAINT `fk_biz_duty_roster_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_duty_roster` ADD CONSTRAINT `fk_biz_duty_roster_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_duty_roster` ADD CONSTRAINT `fk_biz_duty_roster_substitute_emp_id` FOREIGN KEY (`substitute_emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_ecg_holter` ADD CONSTRAINT `fk_biz_ecg_holter_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inspection_record` (`id`);
ALTER TABLE `biz_ecg_holter` ADD CONSTRAINT `fk_biz_ecg_holter_waveform_id` FOREIGN KEY (`waveform_id`) REFERENCES `biz_ecg_waveform` (`id`);
ALTER TABLE `biz_ecg_measure` ADD CONSTRAINT `fk_biz_ecg_measure_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inspection_record` (`id`);
ALTER TABLE `biz_ecg_measure` ADD CONSTRAINT `fk_biz_ecg_measure_waveform_id` FOREIGN KEY (`waveform_id`) REFERENCES `biz_ecg_waveform` (`id`);
ALTER TABLE `biz_ecg_waveform` ADD CONSTRAINT `fk_biz_ecg_waveform_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inspection_record` (`id`);
ALTER TABLE `biz_ecg_waveform` ADD CONSTRAINT `fk_biz_ecg_waveform_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_inspection_apply` (`id`);
ALTER TABLE `biz_ecg_waveform` ADD CONSTRAINT `fk_biz_ecg_waveform_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_emergency` ADD CONSTRAINT `fk_biz_emergency_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_emergency` ADD CONSTRAINT `fk_biz_emergency_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_emergency` ADD CONSTRAINT `fk_biz_emergency_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_emergency` ADD CONSTRAINT `fk_biz_emergency_observation_ward_id` FOREIGN KEY (`observation_ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_emergency` ADD CONSTRAINT `fk_biz_emergency_observation_bed_id` FOREIGN KEY (`observation_bed_id`) REFERENCES `sys_bed` (`bed_id`);
ALTER TABLE `biz_emergency` ADD CONSTRAINT `fk_biz_emergency_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_emergency_handover` ADD CONSTRAINT `fk_biz_emergency_handover_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_emergency_handover` ADD CONSTRAINT `fk_biz_emergency_handover_from_emp_id` FOREIGN KEY (`from_emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_emergency_handover` ADD CONSTRAINT `fk_biz_emergency_handover_take_emp_id` FOREIGN KEY (`take_emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_emergency_handover_item` ADD CONSTRAINT `fk_biz_emergency_handover_item_handover_id` FOREIGN KEY (`handover_id`) REFERENCES `biz_emergency_handover` (`id`);
ALTER TABLE `biz_emergency_handover_item` ADD CONSTRAINT `fk_biz_emergency_handover_item_emergency_id` FOREIGN KEY (`emergency_id`) REFERENCES `biz_emergency` (`id`);
ALTER TABLE `biz_emergency_handover_item` ADD CONSTRAINT `fk_biz_emergency_handover_item_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_emergency_handover_item` ADD CONSTRAINT `fk_biz_emergency_handover_item_from_doctor_id` FOREIGN KEY (`from_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_emergency_handover_item` ADD CONSTRAINT `fk_biz_emergency_handover_item_take_doctor_id` FOREIGN KEY (`take_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_emr_signature` ADD CONSTRAINT `fk_biz_emr_signature_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_emr_signature` ADD CONSTRAINT `fk_biz_emr_signature_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_emr_signature` ADD CONSTRAINT `fk_biz_emr_signature_prev_sign_id` FOREIGN KEY (`prev_sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_emr_signature` ADD CONSTRAINT `fk_biz_emr_signature_signer_id` FOREIGN KEY (`signer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_emr_signature` ADD CONSTRAINT `fk_biz_emr_signature_signer_dept_id` FOREIGN KEY (`signer_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_emr_signature` ADD CONSTRAINT `fk_biz_emr_signature_cert_id` FOREIGN KEY (`cert_id`) REFERENCES `sys_sign_cert` (`id`);
ALTER TABLE `biz_emr_signature` ADD CONSTRAINT `fk_biz_emr_signature_invalid_by` FOREIGN KEY (`invalid_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_endoscopy_record` ADD CONSTRAINT `fk_biz_endoscopy_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_endoscopy_record` ADD CONSTRAINT `fk_biz_endoscopy_record_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_endoscopy_record` ADD CONSTRAINT `fk_biz_endoscopy_record_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_equipment_maintain` ADD CONSTRAINT `fk_biz_equipment_maintain_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `sys_equipment` (`id`);
ALTER TABLE `biz_equipment_metering` ADD CONSTRAINT `fk_biz_equipment_metering_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `sys_equipment` (`id`);
ALTER TABLE `biz_exam_appointment` ADD CONSTRAINT `fk_biz_exam_appointment_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_inspection_apply` (`id`);
ALTER TABLE `biz_exam_appointment` ADD CONSTRAINT `fk_biz_exam_appointment_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_exam_appointment` ADD CONSTRAINT `fk_biz_exam_appointment_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_exam_appointment` ADD CONSTRAINT `fk_biz_exam_appointment_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_exam_appointment` ADD CONSTRAINT `fk_biz_exam_appointment_item_id` FOREIGN KEY (`item_id`) REFERENCES `biz_compliance_audit_item` (`id`);
ALTER TABLE `biz_exam_appointment` ADD CONSTRAINT `fk_biz_exam_appointment_device_id` FOREIGN KEY (`device_id`) REFERENCES `biz_exam_device` (`id`);
ALTER TABLE `biz_exam_appointment` ADD CONSTRAINT `fk_biz_exam_appointment_exam_dept_id` FOREIGN KEY (`exam_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_exam_device` ADD CONSTRAINT `fk_biz_exam_device_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `sys_equipment` (`id`);
ALTER TABLE `biz_exam_device` ADD CONSTRAINT `fk_biz_exam_device_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_exam_device_item` ADD CONSTRAINT `fk_biz_exam_device_item_device_id` FOREIGN KEY (`device_id`) REFERENCES `biz_exam_device` (`id`);
ALTER TABLE `biz_exam_device_item` ADD CONSTRAINT `fk_biz_exam_device_item_item_id` FOREIGN KEY (`item_id`) REFERENCES `sys_inspection_item` (`id`);
ALTER TABLE `biz_exam_film` ADD CONSTRAINT `fk_biz_exam_film_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inspection_record` (`id`);
ALTER TABLE `biz_exam_film` ADD CONSTRAINT `fk_biz_exam_film_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_inspection_apply` (`id`);
ALTER TABLE `biz_exam_film` ADD CONSTRAINT `fk_biz_exam_film_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_exam_film` ADD CONSTRAINT `fk_biz_exam_film_spec_id` FOREIGN KEY (`spec_id`) REFERENCES `biz_film_spec` (`id`);
ALTER TABLE `biz_exam_film` ADD CONSTRAINT `fk_biz_exam_film_fee_id` FOREIGN KEY (`fee_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_exam_image` ADD CONSTRAINT `fk_biz_exam_image_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_inspection_apply` (`id`);
ALTER TABLE `biz_exam_image` ADD CONSTRAINT `fk_biz_exam_image_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inspection_record` (`id`);
ALTER TABLE `biz_exam_image` ADD CONSTRAINT `fk_biz_exam_image_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_exam_slot` ADD CONSTRAINT `fk_biz_exam_slot_device_id` FOREIGN KEY (`device_id`) REFERENCES `biz_exam_device` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_orig_fee_id` FOREIGN KEY (`orig_fee_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_book_by_id` FOREIGN KEY (`book_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_fee_record` ADD CONSTRAINT `fk_biz_fee_record_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_executor_id` FOREIGN KEY (`executor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_revisit_record_id` FOREIGN KEY (`revisit_record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_followup_task` ADD CONSTRAINT `fk_biz_followup_task_revisit_appoint_id` FOREIGN KEY (`revisit_appoint_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_fund_account` ADD CONSTRAINT `fk_biz_fund_account_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_account_id` FOREIGN KEY (`account_id`) REFERENCES `biz_fund_account` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_pay_channel_bill` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_payment_txn_id` FOREIGN KEY (`payment_txn_id`) REFERENCES `biz_payment_txn` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_fund_account_txn` ADD CONSTRAINT `fk_biz_fund_account_txn_orig_txn_id` FOREIGN KEY (`orig_txn_id`) REFERENCES `biz_payment_txn` (`id`);
ALTER TABLE `biz_hand_hygiene_obs` ADD CONSTRAINT `fk_biz_hand_hygiene_obs_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_hand_hygiene_obs` ADD CONSTRAINT `fk_biz_hand_hygiene_obs_observer_id` FOREIGN KEY (`observer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_icu_monitor` ADD CONSTRAINT `fk_biz_icu_monitor_stay_id` FOREIGN KEY (`stay_id`) REFERENCES `biz_icu_stay` (`id`);
ALTER TABLE `biz_icu_monitor` ADD CONSTRAINT `fk_biz_icu_monitor_recorder_id` FOREIGN KEY (`recorder_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_from_dept_id` FOREIGN KEY (`from_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_bed_id` FOREIGN KEY (`bed_id`) REFERENCES `sys_bed` (`bed_id`);
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
ALTER TABLE `biz_infusion_round` ADD CONSTRAINT `fk_biz_infusion_round_exec_id` FOREIGN KEY (`exec_id`) REFERENCES `biz_inpatient_order_exec` (`id`);
ALTER TABLE `biz_infusion_round` ADD CONSTRAINT `fk_biz_infusion_round_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_inpatient_order` (`id`);
ALTER TABLE `biz_infusion_round` ADD CONSTRAINT `fk_biz_infusion_round_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_infusion_round` ADD CONSTRAINT `fk_biz_infusion_round_round_nurse_id` FOREIGN KEY (`round_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_diagnosis` ADD CONSTRAINT `fk_biz_inpatient_diagnosis_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_inpatient_operation` ADD CONSTRAINT `fk_biz_inpatient_operation_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_operation` ADD CONSTRAINT `fk_biz_inpatient_operation_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_inpatient_operation` ADD CONSTRAINT `fk_biz_inpatient_operation_surgeon_id` FOREIGN KEY (`surgeon_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_order` ADD CONSTRAINT `fk_biz_inpatient_order_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_order` ADD CONSTRAINT `fk_biz_inpatient_order_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inpatient_order` ADD CONSTRAINT `fk_biz_inpatient_order_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inpatient_order` ADD CONSTRAINT `fk_biz_inpatient_order_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_inpatient_order` ADD CONSTRAINT `fk_biz_inpatient_order_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_order` ADD CONSTRAINT `fk_biz_inpatient_order_doctor_sign_id` FOREIGN KEY (`doctor_sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_inpatient_order` ADD CONSTRAINT `fk_biz_inpatient_order_verify_nurse_id` FOREIGN KEY (`verify_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_order` ADD CONSTRAINT `fk_biz_inpatient_order_nurse_sign_id` FOREIGN KEY (`nurse_sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_inpatient_order` ADD CONSTRAINT `fk_biz_inpatient_order_stop_doctor_id` FOREIGN KEY (`stop_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_order_exec` ADD CONSTRAINT `fk_biz_inpatient_order_exec_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_inpatient_order` (`id`);
ALTER TABLE `biz_inpatient_order_exec` ADD CONSTRAINT `fk_biz_inpatient_order_exec_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_order_exec` ADD CONSTRAINT `fk_biz_inpatient_order_exec_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inpatient_order_exec` ADD CONSTRAINT `fk_biz_inpatient_order_exec_exec_nurse_id` FOREIGN KEY (`exec_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_order_exec` ADD CONSTRAINT `fk_biz_inpatient_order_exec_fee_record_id` FOREIGN KEY (`fee_record_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_inpatient_order_template` ADD CONSTRAINT `fk_biz_inpatient_order_template_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_order_template` ADD CONSTRAINT `fk_biz_inpatient_order_template_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inpatient_order_template_item` ADD CONSTRAINT `fk_biz_inpatient_order_template_item_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_inpatient_order_template` (`id`);
ALTER TABLE `biz_inpatient_record` ADD CONSTRAINT `fk_biz_inpatient_record_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_record` ADD CONSTRAINT `fk_biz_inpatient_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inpatient_record` ADD CONSTRAINT `fk_biz_inpatient_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inpatient_record` ADD CONSTRAINT `fk_biz_inpatient_record_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_inpatient_record` ADD CONSTRAINT `fk_biz_inpatient_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_record` ADD CONSTRAINT `fk_biz_inpatient_record_archive_by` FOREIGN KEY (`archive_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_record` ADD CONSTRAINT `fk_biz_inpatient_record_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_inpatient_record_log` ADD CONSTRAINT `fk_biz_inpatient_record_log_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inpatient_record` (`id`);
ALTER TABLE `biz_inpatient_record_log` ADD CONSTRAINT `fk_biz_inpatient_record_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_settlement` ADD CONSTRAINT `fk_biz_inpatient_settlement_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_settlement` ADD CONSTRAINT `fk_biz_inpatient_settlement_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inpatient_settlement` ADD CONSTRAINT `fk_biz_inpatient_settlement_settle_by` FOREIGN KEY (`settle_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_summary` ADD CONSTRAINT `fk_biz_inpatient_summary_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_summary` ADD CONSTRAINT `fk_biz_inpatient_summary_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inpatient_summary` ADD CONSTRAINT `fk_biz_inpatient_summary_admit_dept_id` FOREIGN KEY (`admit_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inpatient_summary` ADD CONSTRAINT `fk_biz_inpatient_summary_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inpatient_summary` ADD CONSTRAINT `fk_biz_inpatient_summary_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_from_dept_id` FOREIGN KEY (`from_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_from_ward_id` FOREIGN KEY (`from_ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_from_bed_id` FOREIGN KEY (`from_bed_id`) REFERENCES `sys_bed` (`bed_id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_to_dept_id` FOREIGN KEY (`to_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_to_ward_id` FOREIGN KEY (`to_ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_to_bed_id` FOREIGN KEY (`to_bed_id`) REFERENCES `sys_bed` (`bed_id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_receive_doctor_id` FOREIGN KEY (`receive_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_transfer` ADD CONSTRAINT `fk_biz_inpatient_transfer_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inpatient_record` (`id`);
ALTER TABLE `biz_inspection_apply` ADD CONSTRAINT `fk_biz_inspection_apply_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inspection_apply` ADD CONSTRAINT `fk_biz_inspection_apply_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_inspection_apply` ADD CONSTRAINT `fk_biz_inspection_apply_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_inspection_apply` ADD CONSTRAINT `fk_biz_inspection_apply_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inspection_apply` ADD CONSTRAINT `fk_biz_inspection_apply_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inspection_apply` ADD CONSTRAINT `fk_biz_inspection_apply_inspection_item_id` FOREIGN KEY (`inspection_item_id`) REFERENCES `sys_inspection_item` (`id`);
ALTER TABLE `biz_inspection_apply` ADD CONSTRAINT `fk_biz_inspection_apply_inspection_dept_id` FOREIGN KEY (`inspection_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inspection_apply` ADD CONSTRAINT `fk_biz_inspection_apply_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_inspection_apply` ADD CONSTRAINT `fk_biz_inspection_apply_report_id` FOREIGN KEY (`report_id`) REFERENCES `biz_report` (`id`);
ALTER TABLE `biz_inspection_record` ADD CONSTRAINT `fk_biz_inspection_record_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_inspection_apply` (`id`);
ALTER TABLE `biz_inspection_record` ADD CONSTRAINT `fk_biz_inspection_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inspection_record` ADD CONSTRAINT `fk_biz_inspection_record_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inspection_record` ADD CONSTRAINT `fk_biz_inspection_record_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inspection_record` ADD CONSTRAINT `fk_biz_inspection_record_inspection_item_id` FOREIGN KEY (`inspection_item_id`) REFERENCES `sys_inspection_item` (`id`);
ALTER TABLE `biz_inspection_record` ADD CONSTRAINT `fk_biz_inspection_record_inspection_dept_id` FOREIGN KEY (`inspection_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inspection_record` ADD CONSTRAINT `fk_biz_inspection_record_report_sign_id` FOREIGN KEY (`report_sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_inspection_record` ADD CONSTRAINT `fk_biz_inspection_record_audit_sign_id` FOREIGN KEY (`audit_sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_inspection_template` ADD CONSTRAINT `fk_biz_inspection_template_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inspection_template` ADD CONSTRAINT `fk_biz_inspection_template_inspection_item_id` FOREIGN KEY (`inspection_item_id`) REFERENCES `sys_inspection_item` (`id`);
ALTER TABLE `biz_insurance_report` ADD CONSTRAINT `fk_biz_insurance_report_settlement_id` FOREIGN KEY (`settlement_id`) REFERENCES `biz_insurance_settlement` (`id`);
ALTER TABLE `biz_insurance_settlement` ADD CONSTRAINT `fk_biz_insurance_settlement_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_insurance_settlement` ADD CONSTRAINT `fk_biz_insurance_settlement_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_insurance_settlement` ADD CONSTRAINT `fk_biz_insurance_settlement_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_insurance_settlement` ADD CONSTRAINT `fk_biz_insurance_settlement_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_insurance_settlement` ADD CONSTRAINT `fk_biz_insurance_settlement_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_invoice` ADD CONSTRAINT `fk_biz_invoice_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_invoice` ADD CONSTRAINT `fk_biz_invoice_orig_invoice_id` FOREIGN KEY (`orig_invoice_id`) REFERENCES `biz_invoice` (`id`);
ALTER TABLE `biz_invoice` ADD CONSTRAINT `fk_biz_invoice_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_lab_result` ADD CONSTRAINT `fk_biz_lab_result_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_laboratory_record` (`id`);
ALTER TABLE `biz_lab_result` ADD CONSTRAINT `fk_biz_lab_result_laboratory_item_id` FOREIGN KEY (`laboratory_item_id`) REFERENCES `sys_laboratory_item` (`id`);
ALTER TABLE `biz_laboratory_apply` ADD CONSTRAINT `fk_biz_laboratory_apply_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_laboratory_apply` ADD CONSTRAINT `fk_biz_laboratory_apply_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_laboratory_apply` ADD CONSTRAINT `fk_biz_laboratory_apply_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_laboratory_apply` ADD CONSTRAINT `fk_biz_laboratory_apply_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_laboratory_apply` ADD CONSTRAINT `fk_biz_laboratory_apply_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_laboratory_apply` ADD CONSTRAINT `fk_biz_laboratory_apply_laboratory_item_id` FOREIGN KEY (`laboratory_item_id`) REFERENCES `sys_laboratory_item` (`id`);
ALTER TABLE `biz_laboratory_apply` ADD CONSTRAINT `fk_biz_laboratory_apply_laboratory_dept_id` FOREIGN KEY (`laboratory_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_laboratory_apply` ADD CONSTRAINT `fk_biz_laboratory_apply_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_laboratory_apply` ADD CONSTRAINT `fk_biz_laboratory_apply_report_id` FOREIGN KEY (`report_id`) REFERENCES `biz_report` (`id`);
ALTER TABLE `biz_laboratory_record` ADD CONSTRAINT `fk_biz_laboratory_record_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_laboratory_apply` (`id`);
ALTER TABLE `biz_laboratory_record` ADD CONSTRAINT `fk_biz_laboratory_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_laboratory_record` ADD CONSTRAINT `fk_biz_laboratory_record_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_laboratory_record` ADD CONSTRAINT `fk_biz_laboratory_record_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_laboratory_record` ADD CONSTRAINT `fk_biz_laboratory_record_laboratory_item_id` FOREIGN KEY (`laboratory_item_id`) REFERENCES `sys_laboratory_item` (`id`);
ALTER TABLE `biz_laboratory_record` ADD CONSTRAINT `fk_biz_laboratory_record_laboratory_dept_id` FOREIGN KEY (`laboratory_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_laboratory_record` ADD CONSTRAINT `fk_biz_laboratory_record_report_sign_id` FOREIGN KEY (`report_sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_laboratory_record` ADD CONSTRAINT `fk_biz_laboratory_record_audit_sign_id` FOREIGN KEY (`audit_sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_laboratory_template` ADD CONSTRAINT `fk_biz_laboratory_template_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_laboratory_template` ADD CONSTRAINT `fk_biz_laboratory_template_laboratory_item_id` FOREIGN KEY (`laboratory_item_id`) REFERENCES `sys_laboratory_item` (`id`);
ALTER TABLE `biz_lis_eqa_compare` ADD CONSTRAINT `fk_biz_lis_eqa_compare_plan_id` FOREIGN KEY (`plan_id`) REFERENCES `biz_lis_eqa_plan` (`id`);
ALTER TABLE `biz_lis_eqa_sample` ADD CONSTRAINT `fk_biz_lis_eqa_sample_plan_id` FOREIGN KEY (`plan_id`) REFERENCES `biz_lis_eqa_plan` (`id`);
ALTER TABLE `biz_lis_eqa_sample` ADD CONSTRAINT `fk_biz_lis_eqa_sample_item_id` FOREIGN KEY (`item_id`) REFERENCES `biz_compliance_audit_item` (`id`);
ALTER TABLE `biz_lis_qc_plan` ADD CONSTRAINT `fk_biz_lis_qc_plan_item_id` FOREIGN KEY (`item_id`) REFERENCES `biz_compliance_audit_item` (`id`);
ALTER TABLE `biz_lis_qc_record` ADD CONSTRAINT `fk_biz_lis_qc_record_plan_id` FOREIGN KEY (`plan_id`) REFERENCES `biz_lis_qc_plan` (`id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_diet_plan_id` FOREIGN KEY (`diet_plan_id`) REFERENCES `biz_diet_plan` (`id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_deliver_by_id` FOREIGN KEY (`deliver_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_medical_record_archive` ADD CONSTRAINT `fk_biz_medical_record_archive_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_medical_record_archive` ADD CONSTRAINT `fk_biz_medical_record_archive_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_medical_record_archive` ADD CONSTRAINT `fk_biz_medical_record_archive_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_medical_record_archive` ADD CONSTRAINT `fk_biz_medical_record_archive_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_medical_record_archive` ADD CONSTRAINT `fk_biz_medical_record_archive_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_medical_record_log` ADD CONSTRAINT `fk_biz_medical_record_log_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_medical_record_log` ADD CONSTRAINT `fk_biz_medical_record_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_medical_waste` ADD CONSTRAINT `fk_biz_medical_waste_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_medicaltech_execution` ADD CONSTRAINT `fk_biz_medicaltech_execution_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_medicaltech_execution` ADD CONSTRAINT `fk_biz_medicaltech_execution_item_id` FOREIGN KEY (`item_id`) REFERENCES `biz_compliance_audit_item` (`id`);
ALTER TABLE `biz_medicaltech_execution` ADD CONSTRAINT `fk_biz_medicaltech_execution_executor_id` FOREIGN KEY (`executor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_medicaltech_execution` ADD CONSTRAINT `fk_biz_medicaltech_execution_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_prescription` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_dispensing_id` FOREIGN KEY (`dispensing_id`) REFERENCES `biz_drug_dispensing` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_dispense_by_id` FOREIGN KEY (`dispense_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_checker_id` FOREIGN KEY (`checker_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_nurse_schedule` ADD CONSTRAINT `fk_biz_nurse_schedule_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_nurse_schedule` ADD CONSTRAINT `fk_biz_nurse_schedule_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_nurse_schedule` ADD CONSTRAINT `fk_biz_nurse_schedule_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_nurse_schedule` ADD CONSTRAINT `fk_biz_nurse_schedule_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_nurse_schedule_rule` ADD CONSTRAINT `fk_biz_nurse_schedule_rule_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_nurse_schedule_rule` ADD CONSTRAINT `fk_biz_nurse_schedule_rule_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_nursing_assessment` ADD CONSTRAINT `fk_biz_nursing_assessment_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_nursing_assessment` ADD CONSTRAINT `fk_biz_nursing_assessment_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_nursing_assessment` ADD CONSTRAINT `fk_biz_nursing_assessment_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_nursing_assessment` ADD CONSTRAINT `fk_biz_nursing_assessment_assess_nurse_id` FOREIGN KEY (`assess_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_nursing_qc_check` ADD CONSTRAINT `fk_biz_nursing_qc_check_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_nursing_qc_check` ADD CONSTRAINT `fk_biz_nursing_qc_check_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_nursing_qc_check` ADD CONSTRAINT `fk_biz_nursing_qc_check_inspector_id` FOREIGN KEY (`inspector_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_nursing_qc_check_item` ADD CONSTRAINT `fk_biz_nursing_qc_check_item_check_id` FOREIGN KEY (`check_id`) REFERENCES `biz_nursing_qc_check` (`id`);
ALTER TABLE `biz_nursing_qc_check_item` ADD CONSTRAINT `fk_biz_nursing_qc_check_item_item_id` FOREIGN KEY (`item_id`) REFERENCES `sys_nursing_qc_item` (`id`);
ALTER TABLE `biz_nursing_qc_indicator` ADD CONSTRAINT `fk_biz_nursing_qc_indicator_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_nursing_qc_indicator` ADD CONSTRAINT `fk_biz_nursing_qc_indicator_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_nursing_record` ADD CONSTRAINT `fk_biz_nursing_record_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_nursing_record` ADD CONSTRAINT `fk_biz_nursing_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_nursing_record` ADD CONSTRAINT `fk_biz_nursing_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_nursing_record` ADD CONSTRAINT `fk_biz_nursing_record_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_nursing_record` ADD CONSTRAINT `fk_biz_nursing_record_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_screener_id` FOREIGN KEY (`screener_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_nutrition_stats` ADD CONSTRAINT `fk_biz_nutrition_stats_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_online_consult` ADD CONSTRAINT `fk_biz_online_consult_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_online_consult` ADD CONSTRAINT `fk_biz_online_consult_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_online_consult` ADD CONSTRAINT `fk_biz_online_consult_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
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
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_treatment_record_id` FOREIGN KEY (`treatment_record_id`) REFERENCES `biz_treatment_record` (`record_id`);
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_seat_id` FOREIGN KEY (`seat_id`) REFERENCES `biz_infusion_seat` (`id`);
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_skin_test_id` FOREIGN KEY (`skin_test_id`) REFERENCES `biz_skin_test` (`id`);
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_outp_infusion_round` ADD CONSTRAINT `fk_biz_outp_infusion_round_infusion_id` FOREIGN KEY (`infusion_id`) REFERENCES `biz_outp_infusion` (`id`);
ALTER TABLE `biz_outp_infusion_round` ADD CONSTRAINT `fk_biz_outp_infusion_round_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_pathology_block` ADD CONSTRAINT `fk_biz_pathology_block_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_pathology_order` (`id`);
ALTER TABLE `biz_pathology_order` ADD CONSTRAINT `fk_biz_pathology_order_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_pathology_order` ADD CONSTRAINT `fk_biz_pathology_order_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_pathology_order` ADD CONSTRAINT `fk_biz_pathology_order_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_pathway` ADD CONSTRAINT `fk_biz_pathway_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_pathway_enroll` ADD CONSTRAINT `fk_biz_pathway_enroll_pathway_id` FOREIGN KEY (`pathway_id`) REFERENCES `biz_pathway` (`id`);
ALTER TABLE `biz_pathway_enroll` ADD CONSTRAINT `fk_biz_pathway_enroll_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_pathway_enroll` ADD CONSTRAINT `fk_biz_pathway_enroll_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_pathway_enroll` ADD CONSTRAINT `fk_biz_pathway_enroll_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_pathway_step` ADD CONSTRAINT `fk_biz_pathway_step_pathway_id` FOREIGN KEY (`pathway_id`) REFERENCES `biz_pathway` (`id`);
ALTER TABLE `biz_pathway_variance` ADD CONSTRAINT `fk_biz_pathway_variance_enroll_id` FOREIGN KEY (`enroll_id`) REFERENCES `biz_pathway_enroll` (`id`);
ALTER TABLE `biz_pathway_variance` ADD CONSTRAINT `fk_biz_pathway_variance_recorder_id` FOREIGN KEY (`recorder_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_master_id` FOREIGN KEY (`master_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_last_visit_dept` FOREIGN KEY (`last_visit_dept`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_last_visit_doctor` FOREIGN KEY (`last_visit_doctor`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_first_visit_dept_id` FOREIGN KEY (`first_visit_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_first_visit_doctor_id` FOREIGN KEY (`first_visit_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_patient_allergy` ADD CONSTRAINT `fk_biz_patient_allergy_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_contact` ADD CONSTRAINT `fk_biz_patient_contact_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_family_history` ADD CONSTRAINT `fk_biz_patient_family_history_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_guardian` ADD CONSTRAINT `fk_biz_patient_guardian_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_patient_guardian` ADD CONSTRAINT `fk_biz_patient_guardian_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_medication_history` ADD CONSTRAINT `fk_biz_patient_medication_history_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_merge_log` ADD CONSTRAINT `fk_biz_patient_merge_log_master_id` FOREIGN KEY (`master_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_merge_log` ADD CONSTRAINT `fk_biz_patient_merge_log_merged_id` FOREIGN KEY (`merged_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_merge_log` ADD CONSTRAINT `fk_biz_patient_merge_log_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_patient_past_disease` ADD CONSTRAINT `fk_biz_patient_past_disease_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_surgery_history` ADD CONSTRAINT `fk_biz_patient_surgery_history_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_tag_relation` ADD CONSTRAINT `fk_biz_patient_tag_relation_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_tag_relation` ADD CONSTRAINT `fk_biz_patient_tag_relation_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `sys_patient_tag` (`tag_id`);
ALTER TABLE `biz_pay_channel_bill` ADD CONSTRAINT `fk_biz_pay_channel_bill_local_txn_id` FOREIGN KEY (`local_txn_id`) REFERENCES `biz_payment_txn` (`id`);
ALTER TABLE `biz_pay_channel_bill` ADD CONSTRAINT `fk_biz_pay_channel_bill_matched_by_id` FOREIGN KEY (`matched_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_pay_order` ADD CONSTRAINT `fk_biz_pay_order_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_orig_txn_id` FOREIGN KEY (`orig_txn_id`) REFERENCES `biz_payment_txn` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_cashier_id` FOREIGN KEY (`cashier_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_cashier_settlement_id` FOREIGN KEY (`cashier_settlement_id`) REFERENCES `biz_cashier_settlement` (`id`);
ALTER TABLE `biz_payment_txn` ADD CONSTRAINT `fk_biz_payment_txn_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_refund_apply` (`id`);
ALTER TABLE `biz_perf_result` ADD CONSTRAINT `fk_biz_perf_result_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_perf_result` ADD CONSTRAINT `fk_biz_perf_result_cost_id` FOREIGN KEY (`cost_id`) REFERENCES `biz_dept_cost_month` (`id`);
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
ALTER TABLE `biz_prepay` ADD CONSTRAINT `fk_biz_prepay_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_prepay` ADD CONSTRAINT `fk_biz_prepay_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_prepay` ADD CONSTRAINT `fk_biz_prepay_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_prescription` ADD CONSTRAINT `fk_biz_prescription_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_prescription` ADD CONSTRAINT `fk_biz_prescription_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_prescription` ADD CONSTRAINT `fk_biz_prescription_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_prescription` ADD CONSTRAINT `fk_biz_prescription_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_prescription` ADD CONSTRAINT `fk_biz_prescription_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_prescription` ADD CONSTRAINT `fk_biz_prescription_doctor_sign_id` FOREIGN KEY (`doctor_sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_prescription` ADD CONSTRAINT `fk_biz_prescription_audit_sign_id` FOREIGN KEY (`audit_sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_prescription_audit_log` ADD CONSTRAINT `fk_biz_prescription_audit_log_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_prescription` (`id`);
ALTER TABLE `biz_prescription_audit_log` ADD CONSTRAINT `fk_biz_prescription_audit_log_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_prescription_audit_log` ADD CONSTRAINT `fk_biz_prescription_audit_log_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_prescription_audit_log` ADD CONSTRAINT `fk_biz_prescription_audit_log_auditor_id` FOREIGN KEY (`auditor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_prescription_detail` ADD CONSTRAINT `fk_biz_prescription_detail_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_prescription` (`id`);
ALTER TABLE `biz_prescription_detail` ADD CONSTRAINT `fk_biz_prescription_detail_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_public_health_report` ADD CONSTRAINT `fk_biz_public_health_report_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_public_health_report` ADD CONSTRAINT `fk_biz_public_health_report_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_purchase_order` ADD CONSTRAINT `fk_biz_purchase_order_supplier_id` FOREIGN KEY (`supplier_id`) REFERENCES `sys_supplier` (`supplier_id`);
ALTER TABLE `biz_purchase_order` ADD CONSTRAINT `fk_biz_purchase_order_approver_id` FOREIGN KEY (`approver_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_purchase_order_detail` ADD CONSTRAINT `fk_biz_purchase_order_detail_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_purchase_order` (`order_id`);
ALTER TABLE `biz_purchase_order_detail` ADD CONSTRAINT `fk_biz_purchase_order_detail_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_quality_control` ADD CONSTRAINT `fk_biz_quality_control_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inpatient_record` (`id`);
ALTER TABLE `biz_quality_control` ADD CONSTRAINT `fk_biz_quality_control_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_quality_control_issue` ADD CONSTRAINT `fk_biz_quality_control_issue_qc_id` FOREIGN KEY (`qc_id`) REFERENCES `biz_quality_control` (`id`);
ALTER TABLE `biz_quality_control_issue` ADD CONSTRAINT `fk_biz_quality_control_issue_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inpatient_record` (`id`);
ALTER TABLE `biz_quality_control_issue` ADD CONSTRAINT `fk_biz_quality_control_issue_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_radio_report_template` ADD CONSTRAINT `fk_biz_radio_report_template_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_record_qc_flow` ADD CONSTRAINT `fk_biz_record_qc_flow_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_record_qc_flow` ADD CONSTRAINT `fk_biz_record_qc_flow_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_record_qc_flow` ADD CONSTRAINT `fk_biz_record_qc_flow_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_record_qc_flow_action` ADD CONSTRAINT `fk_biz_record_qc_flow_action_flow_id` FOREIGN KEY (`flow_id`) REFERENCES `biz_record_qc_flow` (`id`);
ALTER TABLE `biz_record_qc_flow_action` ADD CONSTRAINT `fk_biz_record_qc_flow_action_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_visit` (`visit_id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_from_dept_id` FOREIGN KEY (`from_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_to_dept_id` FOREIGN KEY (`to_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_referral` ADD CONSTRAINT `fk_biz_referral_audit_by` FOREIGN KEY (`audit_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_refund_apply` ADD CONSTRAINT `fk_biz_refund_apply_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_refund_apply` ADD CONSTRAINT `fk_biz_refund_apply_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_refund_apply` ADD CONSTRAINT `fk_biz_refund_apply_auditor_id` FOREIGN KEY (`auditor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_report` ADD CONSTRAINT `fk_biz_report_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_report` ADD CONSTRAINT `fk_biz_report_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_report` ADD CONSTRAINT `fk_biz_report_write_by_id` FOREIGN KEY (`write_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_rx_doctor_talk` ADD CONSTRAINT `fk_biz_rx_doctor_talk_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_rx_flow` ADD CONSTRAINT `fk_biz_rx_flow_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_prescription` (`id`);
ALTER TABLE `biz_rx_flow` ADD CONSTRAINT `fk_biz_rx_flow_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_rx_review_batch` ADD CONSTRAINT `fk_biz_rx_review_batch_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_rx_review_item` ADD CONSTRAINT `fk_biz_rx_review_item_batch_id` FOREIGN KEY (`batch_id`) REFERENCES `biz_rx_review_batch` (`id`);
ALTER TABLE `biz_rx_review_item` ADD CONSTRAINT `fk_biz_rx_review_item_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_prescription` (`id`);
ALTER TABLE `biz_rx_review_item` ADD CONSTRAINT `fk_biz_rx_review_item_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_rx_review_item` ADD CONSTRAINT `fk_biz_rx_review_item_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_rx_template` ADD CONSTRAINT `fk_biz_rx_template_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_rx_template_detail` ADD CONSTRAINT `fk_biz_rx_template_detail_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_rx_template` (`id`);
ALTER TABLE `biz_rx_template_detail` ADD CONSTRAINT `fk_biz_rx_template_detail_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_schedule` ADD CONSTRAINT `fk_biz_schedule_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_schedule` ADD CONSTRAINT `fk_biz_schedule_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_schedule` ADD CONSTRAINT `fk_biz_schedule_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_schedule` ADD CONSTRAINT `fk_biz_schedule_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_schedule_slot` ADD CONSTRAINT `fk_biz_schedule_slot_schedule_id` FOREIGN KEY (`schedule_id`) REFERENCES `biz_schedule` (`id`);
ALTER TABLE `biz_schedule_slot_template` ADD CONSTRAINT `fk_biz_schedule_slot_template_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_schedule_template` (`id`);
ALTER TABLE `biz_schedule_template` ADD CONSTRAINT `fk_biz_schedule_template_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_schedule_template` ADD CONSTRAINT `fk_biz_schedule_template_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_schedule_template` ADD CONSTRAINT `fk_biz_schedule_template_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_schedule_template` ADD CONSTRAINT `fk_biz_schedule_template_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_settlement_bill` ADD CONSTRAINT `fk_biz_settlement_bill_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_settlement_bill` ADD CONSTRAINT `fk_biz_settlement_bill_bill_by_id` FOREIGN KEY (`bill_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_settlement_bill` ADD CONSTRAINT `fk_biz_settlement_bill_void_by_id` FOREIGN KEY (`void_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_settlement_bill` ADD CONSTRAINT `fk_biz_settlement_bill_orig_bill_id` FOREIGN KEY (`orig_bill_id`) REFERENCES `biz_pay_channel_bill` (`id`);
ALTER TABLE `biz_settlement_bill_item` ADD CONSTRAINT `fk_biz_settlement_bill_item_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_settlement_bill_item` ADD CONSTRAINT `fk_biz_settlement_bill_item_fee_record_id` FOREIGN KEY (`fee_record_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_settlement_bill_item` ADD CONSTRAINT `fk_biz_settlement_bill_item_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_settlement_bill_item` ADD CONSTRAINT `fk_biz_settlement_bill_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_settlement_diagnosis` ADD CONSTRAINT `fk_biz_settlement_diagnosis_settlement_id` FOREIGN KEY (`settlement_id`) REFERENCES `biz_insurance_settlement` (`id`);
ALTER TABLE `biz_settlement_operation` ADD CONSTRAINT `fk_biz_settlement_operation_settlement_id` FOREIGN KEY (`settlement_id`) REFERENCES `biz_insurance_settlement` (`id`);
ALTER TABLE `biz_shift` ADD CONSTRAINT `fk_biz_shift_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_single_disease_case` ADD CONSTRAINT `fk_biz_single_disease_case_disease_id` FOREIGN KEY (`disease_id`) REFERENCES `sys_single_disease` (`id`);
ALTER TABLE `biz_single_disease_case` ADD CONSTRAINT `fk_biz_single_disease_case_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_single_disease_case` ADD CONSTRAINT `fk_biz_single_disease_case_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_skin_test` ADD CONSTRAINT `fk_biz_skin_test_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_skin_test` ADD CONSTRAINT `fk_biz_skin_test_treatment_record_id` FOREIGN KEY (`treatment_record_id`) REFERENCES `biz_treatment_record` (`record_id`);
ALTER TABLE `biz_skin_test` ADD CONSTRAINT `fk_biz_skin_test_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_stat_daily` ADD CONSTRAINT `fk_biz_stat_daily_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `biz_stat_dept` (`stat_id`);
ALTER TABLE `biz_stat_dept` ADD CONSTRAINT `fk_biz_stat_dept_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_stat_report` ADD CONSTRAINT `fk_biz_stat_report_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_stocktake_item` ADD CONSTRAINT `fk_biz_stocktake_item_stocktake_id` FOREIGN KEY (`stocktake_id`) REFERENCES `biz_stocktake` (`id`);
ALTER TABLE `biz_stocktake_item` ADD CONSTRAINT `fk_biz_stocktake_item_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `biz_drug_stock` (`id`);
ALTER TABLE `biz_stocktake_item` ADD CONSTRAINT `fk_biz_stocktake_item_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_dispatch_id` FOREIGN KEY (`dispatch_id`) REFERENCES `biz_survey_dispatch` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_survey_template` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_fill_employee_id` FOREIGN KEY (`fill_employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_survey_answer` ADD CONSTRAINT `fk_biz_survey_answer_dispute_case_id` FOREIGN KEY (`dispute_case_id`) REFERENCES `biz_dispute_case` (`id`);
ALTER TABLE `biz_survey_answer_item` ADD CONSTRAINT `fk_biz_survey_answer_item_answer_id` FOREIGN KEY (`answer_id`) REFERENCES `biz_survey_answer` (`id`);
ALTER TABLE `biz_survey_answer_item` ADD CONSTRAINT `fk_biz_survey_answer_item_item_id` FOREIGN KEY (`item_id`) REFERENCES `biz_survey_item` (`id`);
ALTER TABLE `biz_survey_answer_item` ADD CONSTRAINT `fk_biz_survey_answer_item_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_survey_template` (`id`);
ALTER TABLE `biz_survey_dispatch` ADD CONSTRAINT `fk_biz_survey_dispatch_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_survey_template` (`id`);
ALTER TABLE `biz_survey_dispatch` ADD CONSTRAINT `fk_biz_survey_dispatch_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_survey_dispatch` ADD CONSTRAINT `fk_biz_survey_dispatch_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_survey_dispatch` ADD CONSTRAINT `fk_biz_survey_dispatch_answer_id` FOREIGN KEY (`answer_id`) REFERENCES `biz_survey_answer` (`id`);
ALTER TABLE `biz_survey_item` ADD CONSTRAINT `fk_biz_survey_item_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_survey_template` (`id`);
ALTER TABLE `biz_tcm_decoct` ADD CONSTRAINT `fk_biz_tcm_decoct_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_prescription` (`id`);
ALTER TABLE `biz_tcm_decoct` ADD CONSTRAINT `fk_biz_tcm_decoct_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_tcm_decoct` ADD CONSTRAINT `fk_biz_tcm_decoct_pharmacy_id` FOREIGN KEY (`pharmacy_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_tcm_decoct` ADD CONSTRAINT `fk_biz_tcm_decoct_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_tech_auth_override` ADD CONSTRAINT `fk_biz_tech_auth_override_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_tech_auth_override` ADD CONSTRAINT `fk_biz_tech_auth_override_supervisor_id` FOREIGN KEY (`supervisor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_tele_consult` ADD CONSTRAINT `fk_biz_tele_consult_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_tele_consult` ADD CONSTRAINT `fk_biz_tele_consult_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_tele_consult` ADD CONSTRAINT `fk_biz_tele_consult_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_tele_consult` ADD CONSTRAINT `fk_biz_tele_consult_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_crossmatch_doctor_id` FOREIGN KEY (`crossmatch_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_issue_doctor_id` FOREIGN KEY (`issue_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_check_nurse_id` FOREIGN KEY (`check_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_check_nurse2_id` FOREIGN KEY (`check_nurse2_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_infusion_nurse_id` FOREIGN KEY (`infusion_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_reaction_reporter_id` FOREIGN KEY (`reaction_reporter_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_finish_doctor_id` FOREIGN KEY (`finish_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inpatient_record` (`id`);
ALTER TABLE `biz_transfusion_apply` ADD CONSTRAINT `fk_biz_transfusion_apply_cancel_doctor_id` FOREIGN KEY (`cancel_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_approve` ADD CONSTRAINT `fk_biz_transfusion_approve_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_transfusion_apply` (`id`);
ALTER TABLE `biz_transfusion_approve` ADD CONSTRAINT `fk_biz_transfusion_approve_approver_id` FOREIGN KEY (`approver_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_transfusion_bag` ADD CONSTRAINT `fk_biz_transfusion_bag_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_transfusion_apply` (`id`);
ALTER TABLE `biz_transfusion_bag` ADD CONSTRAINT `fk_biz_transfusion_bag_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_transfusion_bag` ADD CONSTRAINT `fk_biz_transfusion_bag_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_transfusion_bag` ADD CONSTRAINT `fk_biz_transfusion_bag_crossmatch_doctor_id` FOREIGN KEY (`crossmatch_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_treatment_apply` ADD CONSTRAINT `fk_biz_treatment_apply_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_treatment_apply` ADD CONSTRAINT `fk_biz_treatment_apply_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_visit` (`visit_id`);
ALTER TABLE `biz_treatment_apply` ADD CONSTRAINT `fk_biz_treatment_apply_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_treatment_apply` ADD CONSTRAINT `fk_biz_treatment_apply_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_treatment_apply` ADD CONSTRAINT `fk_biz_treatment_apply_treatment_item_id` FOREIGN KEY (`treatment_item_id`) REFERENCES `sys_treatment_item` (`id`);
ALTER TABLE `biz_treatment_apply` ADD CONSTRAINT `fk_biz_treatment_apply_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_treatment_apply` ADD CONSTRAINT `fk_biz_treatment_apply_exec_dept_id` FOREIGN KEY (`exec_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_treatment_record` ADD CONSTRAINT `fk_biz_treatment_record_apply_id` FOREIGN KEY (`apply_id`) REFERENCES `biz_treatment_apply` (`apply_id`);
ALTER TABLE `biz_treatment_record` ADD CONSTRAINT `fk_biz_treatment_record_treatment_item_id` FOREIGN KEY (`treatment_item_id`) REFERENCES `sys_treatment_item` (`id`);
ALTER TABLE `biz_treatment_record` ADD CONSTRAINT `fk_biz_treatment_record_execute_doctor_id` FOREIGN KEY (`execute_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_treatment_record` ADD CONSTRAINT `fk_biz_treatment_record_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_treatment_record` ADD CONSTRAINT `fk_biz_treatment_record_fee_record_id` FOREIGN KEY (`fee_record_id`) REFERENCES `biz_fee_record` (`id`);
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_queue_id` FOREIGN KEY (`queue_id`) REFERENCES `biz_queue` (`id`);
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_triage_nurse_id` FOREIGN KEY (`triage_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_ultrasound_measure` ADD CONSTRAINT `fk_biz_ultrasound_measure_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_ultrasound_record` (`id`);
ALTER TABLE `biz_ultrasound_record` ADD CONSTRAINT `fk_biz_ultrasound_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_ultrasound_record` ADD CONSTRAINT `fk_biz_ultrasound_record_apply_dept_id` FOREIGN KEY (`apply_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_ultrasound_record` ADD CONSTRAINT `fk_biz_ultrasound_record_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_visit` ADD CONSTRAINT `fk_biz_visit_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_vte_event` ADD CONSTRAINT `fk_biz_vte_event_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_vte_event` ADD CONSTRAINT `fk_biz_vte_event_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_vte_event` ADD CONSTRAINT `fk_biz_vte_event_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_vte_event` ADD CONSTRAINT `fk_biz_vte_event_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_vte_event` ADD CONSTRAINT `fk_biz_vte_event_reporter_id` FOREIGN KEY (`reporter_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_vte_prevent` ADD CONSTRAINT `fk_biz_vte_prevent_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_vte_prevent` ADD CONSTRAINT `fk_biz_vte_prevent_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_vte_prevent` ADD CONSTRAINT `fk_biz_vte_prevent_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_vte_prevent` ADD CONSTRAINT `fk_biz_vte_prevent_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_vte_prevent` ADD CONSTRAINT `fk_biz_vte_prevent_assessment_id` FOREIGN KEY (`assessment_id`) REFERENCES `biz_nursing_assessment` (`id`);
ALTER TABLE `biz_vte_prevent` ADD CONSTRAINT `fk_biz_vte_prevent_executor_id` FOREIGN KEY (`executor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_vte_stats` ADD CONSTRAINT `fk_biz_vte_stats_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
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
ALTER TABLE `biz_yb_chronic_reg` ADD CONSTRAINT `fk_biz_yb_chronic_reg_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_yb_chronic_reg` ADD CONSTRAINT `fk_biz_yb_chronic_reg_catalog_id` FOREIGN KEY (`catalog_id`) REFERENCES `biz_yb_chronic_catalog` (`id`);
ALTER TABLE `biz_yb_chronic_reg` ADD CONSTRAINT `fk_biz_yb_chronic_reg_certify_dept_id` FOREIGN KEY (`certify_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_yb_chronic_reg` ADD CONSTRAINT `fk_biz_yb_chronic_reg_register_dept_id` FOREIGN KEY (`register_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_yb_chronic_reg` ADD CONSTRAINT `fk_biz_yb_chronic_reg_register_emp_id` FOREIGN KEY (`register_emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_yb_deduct_log` ADD CONSTRAINT `fk_biz_yb_deduct_log_notice_id` FOREIGN KEY (`notice_id`) REFERENCES `biz_yb_deduct_notice` (`id`);
ALTER TABLE `biz_yb_deduct_notice` ADD CONSTRAINT `fk_biz_yb_deduct_notice_inspection_id` FOREIGN KEY (`inspection_id`) REFERENCES `biz_yb_inspection` (`id`);
ALTER TABLE `biz_yb_deduct_notice` ADD CONSTRAINT `fk_biz_yb_deduct_notice_settlement_id` FOREIGN KEY (`settlement_id`) REFERENCES `biz_insurance_settlement` (`id`);
ALTER TABLE `biz_yb_deduct_notice` ADD CONSTRAINT `fk_biz_yb_deduct_notice_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_yb_deduct_notice` ADD CONSTRAINT `fk_biz_yb_deduct_notice_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_yb_deduct_notice` ADD CONSTRAINT `fk_biz_yb_deduct_notice_liable_dept_id` FOREIGN KEY (`liable_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_yb_mapping` ADD CONSTRAINT `fk_biz_yb_mapping_catalog_id` FOREIGN KEY (`catalog_id`) REFERENCES `biz_yb_catalog` (`id`);
ALTER TABLE `sys_attachment` ADD CONSTRAINT `fk_sys_attachment_upload_user_id` FOREIGN KEY (`upload_user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_audit_log` ADD CONSTRAINT `fk_sys_audit_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_bed` ADD CONSTRAINT `fk_sys_bed_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `sys_bed` ADD CONSTRAINT `fk_sys_bed_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_bed` ADD CONSTRAINT `fk_sys_bed_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `sys_checkup_package_item` ADD CONSTRAINT `fk_sys_checkup_package_item_package_id` FOREIGN KEY (`package_id`) REFERENCES `sys_checkup_package` (`id`);
ALTER TABLE `sys_clinic_room` ADD CONSTRAINT `fk_sys_clinic_room_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_department` ADD CONSTRAINT `fk_sys_department_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_diagnosis` ADD CONSTRAINT `fk_sys_diagnosis_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `sys_diagnosis` (`id`);
ALTER TABLE `sys_drug_price_history` ADD CONSTRAINT `fk_sys_drug_price_history_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `sys_drug_price_history` ADD CONSTRAINT `fk_sys_drug_price_history_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_employee` ADD CONSTRAINT `fk_sys_employee_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_employee_post` ADD CONSTRAINT `fk_sys_employee_post_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_employee_post` ADD CONSTRAINT `fk_sys_employee_post_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`);
ALTER TABLE `sys_employee_post` ADD CONSTRAINT `fk_sys_employee_post_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_employee_qualification` ADD CONSTRAINT `fk_sys_employee_qualification_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_employee_tech_auth` ADD CONSTRAINT `fk_sys_employee_tech_auth_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_employee_tech_auth` ADD CONSTRAINT `fk_sys_employee_tech_auth_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_employee_tech_auth` ADD CONSTRAINT `fk_sys_employee_tech_auth_approver_id` FOREIGN KEY (`approver_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_equipment` ADD CONSTRAINT `fk_sys_equipment_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_field_change_log` ADD CONSTRAINT `fk_sys_field_change_log_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_field_change_log` ADD CONSTRAINT `fk_sys_field_change_log_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_inspection_item` ADD CONSTRAINT `fk_sys_inspection_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_laboratory_item` ADD CONSTRAINT `fk_sys_laboratory_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_laboratory_item_detail` ADD CONSTRAINT `fk_sys_laboratory_item_detail_laboratory_item_id` FOREIGN KEY (`laboratory_item_id`) REFERENCES `sys_laboratory_item` (`id`);
ALTER TABLE `sys_login_log` ADD CONSTRAINT `fk_sys_login_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_menu` ADD CONSTRAINT `fk_sys_menu_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `sys_menu` (`id`);
ALTER TABLE `sys_message` ADD CONSTRAINT `fk_sys_message_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_oper_log` ADD CONSTRAINT `fk_sys_oper_log_oper_id` FOREIGN KEY (`oper_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_oper_log` ADD CONSTRAINT `fk_sys_oper_log_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_price_change_history` ADD CONSTRAINT `fk_sys_price_change_history_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_role_menu` ADD CONSTRAINT `fk_sys_role_menu_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`);
ALTER TABLE `sys_role_menu` ADD CONSTRAINT `fk_sys_role_menu_menu_id` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`);
ALTER TABLE `sys_sign_cert` ADD CONSTRAINT `fk_sys_sign_cert_emp_id` FOREIGN KEY (`emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_sign_cert` ADD CONSTRAINT `fk_sys_sign_cert_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_sign_cert` ADD CONSTRAINT `fk_sys_sign_cert_revoke_by` FOREIGN KEY (`revoke_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_treatment_item` ADD CONSTRAINT `fk_sys_treatment_item_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_user` ADD CONSTRAINT `fk_sys_user_emp_id` FOREIGN KEY (`emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_user` ADD CONSTRAINT `fk_sys_user_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `sys_ward` ADD CONSTRAINT `fk_sys_ward_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_workbench_layout` ADD CONSTRAINT `fk_sys_workbench_layout_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `sys_workbench_role` ADD CONSTRAINT `fk_sys_workbench_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`);
ALTER TABLE `sys_workbench_role` ADD CONSTRAINT `fk_sys_workbench_role_widget_id` FOREIGN KEY (`widget_id`) REFERENCES `sys_workbench_widget` (`id`);
