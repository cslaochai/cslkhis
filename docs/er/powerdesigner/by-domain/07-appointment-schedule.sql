-- ============================================================
-- 领域 07 预约挂号·排班·分诊·叫号（本域 8 表 + 上游参照 8 表 / 30 条关系）
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


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

-- ---------------- 参照关系（E-R 连线） ----------------
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
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_queue` ADD CONSTRAINT `fk_biz_queue_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
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
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_queue_id` FOREIGN KEY (`queue_id`) REFERENCES `biz_queue` (`id`);
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_triage_record` ADD CONSTRAINT `fk_biz_triage_record_triage_nurse_id` FOREIGN KEY (`triage_nurse_id`) REFERENCES `sys_employee` (`id`);
