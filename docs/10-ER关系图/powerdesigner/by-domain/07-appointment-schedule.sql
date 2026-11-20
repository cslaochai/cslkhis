-- ============================================================
-- 领域 07 预约挂号·排班·分诊·叫号（本域 24 表 + 上游参照 10 表 / 83 条关系）
-- 由 docs/01-初始化DDL 的结构基线反向生成，只用于建模，禁止在业务库执行。
-- ============================================================


-- biz_appoint_info  挂号信息
CREATE TABLE `biz_appoint_info` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `regist_no` varchar(32) NOT NULL COMMENT '挂号单号（唯一）',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL DEFAULT '' COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '性别（1-男 2-女 3-未知）',
  `age` int COMMENT '年龄',
  `phone` varchar(20) COMMENT '手机号码',
  `dept_id` bigint NOT NULL DEFAULT '0' COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `room_id` bigint COMMENT '诊室ID',
  `room_name` varchar(100) COMMENT '诊室名称',
  `doctor_id` bigint COMMENT '医生ID',
  `doctor_name` varchar(50) COMMENT '医生姓名',
  `schedule_id` bigint COMMENT '排班ID',
  `slot_id` bigint COMMENT '排班时间片段ID',
  `slot_start` char(5) COMMENT '就诊时段开始快照（HH:mm）',
  `slot_end` char(5) COMMENT '就诊时段结束快照（HH:mm）',
  `regist_type` tinyint COMMENT '挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）',
  `regist_source` tinyint COMMENT '挂号来源（1-窗口挂号 2-自助机挂号 3-网上挂号 4-预约挂号）',
  `regist_time` datetime NOT NULL DEFAULT (now()) COMMENT '挂号时间',
  `visit_date` date NOT NULL DEFAULT (curdate()) COMMENT '就诊日期',
  `arrive_time` datetime COMMENT '到达时间',
  `schedule_type` tinyint COMMENT '时间段（1-上午 2-下午 3-全天 4-凌晨）',
  `slot_time` varchar(8) COMMENT '就诊时段',
  `visit_type` tinyint NOT NULL DEFAULT '0' COMMENT '就诊类型（号别）（1-初诊 2-复诊）',
  `revisit_source` tinyint COMMENT '复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊）',
  `settlement_type` tinyint COMMENT '结算方式（1-全自费 2-医保联网）',
  `medical_insurance_type` varchar(50) COMMENT '费别快照（取自患者主档：自费/城镇职工医保/城乡居民医保/公费医疗/商业医疗保险）',
  `medical_insurance_no` varchar(50) COMMENT '医保卡号（快照自患者主档）',
  `regist_status` tinyint COMMENT '挂号状态（1-已挂号 2-已签到 3-已接诊 4-已就诊 5-已退号 6-已过号 7-爽约 8-未就诊）',
  `revisit_type` tinyint COMMENT '【已废置】改用 visit_type',
  `revisit_record_id` bigint COMMENT '复诊关联的病历ID',
  `refund_time` datetime COMMENT '退号时间',
  `refund_reason` varchar(200) COMMENT '退号原因',
  `bill_id` bigint COMMENT '挂号费结算账单ID',
  `bill_no` varchar(32) COMMENT '账单号（快照）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_by_id` bigint COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_by_id` bigint COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_appoint_info_regist_no` (`regist_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号信息';

-- biz_clinic_source  门诊号源表
CREATE TABLE `biz_clinic_source` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `staff_schedule_id` bigint COMMENT '员工排班ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `week_day` tinyint NOT NULL DEFAULT '0' COMMENT '星期（1-周一 2-周二 3-周三 4-周四 5-周五 6-周六 7-周日）',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `room_id` bigint COMMENT '诊室ID',
  `room_name` varchar(100) COMMENT '诊室名称',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '医生姓名',
  `staff_type` tinyint NOT NULL DEFAULT '1' COMMENT '排班对象岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `schedule_type` tinyint COMMENT '排班类型（1-上午 2-下午 3-全天 4-凌晨）',
  `start_time` varchar(10) NOT NULL COMMENT '开始时间',
  `end_time` varchar(10) NOT NULL COMMENT '结束时间',
  `shift_id` bigint COMMENT '标准班次ID',
  `total_source` int COMMENT '总号源数',
  `used_source` int COMMENT '已挂号数',
  `available_source` int COMMENT '剩余号源数',
  `used_appointment_source` int NOT NULL DEFAULT '0' COMMENT '预约池已用号源',
  `regist_fee` decimal(10,2) COMMENT '挂号费',
  `diagnosis_fee` decimal(10,2) COMMENT '诊查费',
  `diagnosis_tier` tinyint COMMENT '门诊诊查费职级加收档位：1普通 2副主任医师 3主任医师 4知名专家',
  `is_expert` tinyint COMMENT '是否专家号（0-否 1-是）',
  `expert_fee` decimal(10,2) COMMENT '专家号费用',
  `is_appointment` tinyint COMMENT '是否可预约（0-否 1-是）',
  `appointment_source` int COMMENT '预约号源数',
  `added_source` int NOT NULL DEFAULT '0' COMMENT '累计加号数',
  `status` tinyint COMMENT '状态（0-停诊 1-正常 2-已满 3-已过期）',
  `publish_status` tinyint NOT NULL DEFAULT '0' COMMENT '发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）',
  `consult_status` tinyint COMMENT '就诊状态（0-待开始 1-接诊中 2-暂停）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_clinic_source_dept_id_doctor_id_schedule_date_start_time_end_time` (`dept_id`, `doctor_id`, `schedule_date`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊号源表';

-- biz_clinic_source_change_log  排班变更记录
CREATE TABLE `biz_clinic_source_change_log` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `staff_schedule_id` bigint COMMENT '员工排班ID（整周级审批记录为空）',
  `action_type` tinyint NOT NULL DEFAULT '0' COMMENT '变更类型（1-换班 2-代班 3-停班 4-加号 5-减号 6-出诊变更 7-提交审核 8-审核发布 9-驳回 10-整周提交 11-整周审核发布 12-撤回 13-进入调班）',
  `from_employee_id` bigint COMMENT '原值班人',
  `to_employee_id` bigint COMMENT '实际值班人',
  `from_shift_id` bigint COMMENT '原班次ID',
  `to_shift_id` bigint COMMENT '新班次ID',
  `amount` int COMMENT '变更数量',
  `reason` varchar(200) COMMENT '变更原因',
  `occur_time` datetime NOT NULL DEFAULT (now()) COMMENT '变更时间',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班变更记录';

-- biz_clinic_source_log  门诊排班变更日志
CREATE TABLE `biz_clinic_source_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `schedule_id` bigint NOT NULL DEFAULT '0' COMMENT '门诊排班ID',
  `action_type` tinyint NOT NULL DEFAULT '0' COMMENT '变更动作（1-新增排班 2-修改排班 3-删除排班 4-停诊 5-启用 6-加号 7-批量退号 8-段级编辑 9-复刻）',
  `amount` int COMMENT '变更数量（加号数/退号数/批量或复刻条数）',
  `reason` varchar(200) COMMENT '变更原因',
  `occur_time` datetime NOT NULL DEFAULT (now()) COMMENT '发生时间',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint COMMENT '创建人ID',
  `update_by_id` bigint COMMENT '更新人ID',
  `dept_name` varchar(64) COMMENT '科室名称快照',
  `doctor_name` varchar(64) COMMENT '排班人员姓名快照',
  `schedule_date` date COMMENT '排班日期快照',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊排班变更日志';

-- biz_clinic_source_slot  排班时段号源
CREATE TABLE `biz_clinic_source_slot` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `schedule_id` bigint NOT NULL COMMENT '排班ID',
  `seq` int NOT NULL DEFAULT '0' COMMENT '段序',
  `start_time` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '段开始时间（HH:mm）',
  `end_time` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '段结束时间（HH:mm）',
  `total_source` int NOT NULL DEFAULT '0' COMMENT '段号源总数',
  `used_source` int NOT NULL DEFAULT '0' COMMENT '段已挂号数（现场+线上）',
  `available_source` int NOT NULL DEFAULT '0' COMMENT '段剩余号源',
  `added_source` int NOT NULL DEFAULT '0' COMMENT '段累计加号数',
  `appointment_source` int NOT NULL DEFAULT '0' COMMENT '段内线上预约预留',
  `used_appointment_source` int NOT NULL DEFAULT '0' COMMENT '段内线上预约已用',
  `status` tinyint COMMENT '段状态（0-停用 1-正常）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COLLATE utf8mb4_general_ci COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_clinic_source_slot_schedule_id_start_time` (`schedule_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班时段号源';

-- biz_clinic_source_slot_template  排班模板时段
CREATE TABLE `biz_clinic_source_slot_template` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `template_id` bigint NOT NULL COMMENT '排班模板ID',
  `seq` int NOT NULL DEFAULT '0' COMMENT '段序',
  `start_time` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '段开始时间（HH:mm）',
  `end_time` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '段结束时间（HH:mm）',
  `total_source` int NOT NULL DEFAULT '0' COMMENT '段号源总数',
  `appointment_source` int NOT NULL DEFAULT '0' COMMENT '段内线上预约预留',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COLLATE utf8mb4_general_ci COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_clinic_source_slot_template_template_id_start_time` (`template_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班模板时段';

-- biz_clinic_source_template  排班周模板
CREATE TABLE `biz_clinic_source_template` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_id` bigint NOT NULL DEFAULT '0' COMMENT '科室ID',
  `dept_name` varchar(64) COLLATE utf8mb4_general_ci COMMENT '科室名称',
  `doctor_id` bigint NOT NULL DEFAULT '0' COMMENT '医生ID',
  `doctor_name` varchar(64) COLLATE utf8mb4_general_ci COMMENT '医生姓名',
  `staff_type` tinyint NOT NULL DEFAULT '1' COMMENT '排班对象岗位类别（2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `week_day` tinyint NOT NULL DEFAULT '0' COMMENT '星期几（1-周一 7-周日）',
  `week_parity` tinyint NOT NULL DEFAULT '0' COMMENT '单双周（0-每周 1-单周 2-双周）',
  `valid_from` date COMMENT '生效起始日期(空=不限)',
  `valid_until` date COMMENT '生效截止日期(空=不限)',
  `schedule_type` tinyint COMMENT '班次（1-上午 2-下午 3-全天 4-凌晨）',
  `start_time` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '开始时间（HH:mm）',
  `end_time` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '结束时间（HH:mm）',
  `shift_id` bigint COMMENT '标准班次ID',
  `total_source` int NOT NULL DEFAULT '20' COMMENT '号源总数',
  `room_id` bigint COMMENT '诊室ID',
  `room_name` varchar(64) COLLATE utf8mb4_general_ci COMMENT '诊室名称',
  `regist_fee` decimal(10,2) COMMENT '挂号费',
  `diagnosis_fee` decimal(10,2) COMMENT '诊疗费',
  `diagnosis_tier` tinyint COMMENT '门诊诊查费职级加收档位：1普通 2副主任医师 3主任医师 4知名专家',
  `is_expert` tinyint COMMENT '是否专家（0-否 1-是）',
  `expert_fee` decimal(10,2) COMMENT '专家费',
  `is_appointment` tinyint COMMENT '是否开放预约（0-否 1-是）',
  `appointment_source` int COMMENT '预约号源数',
  `status` tinyint COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COLLATE utf8mb4_general_ci COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班周模板';

-- biz_medical_record  门诊病历
CREATE TABLE `biz_medical_record` (
  `id` bigint NOT NULL,
  `record_no` varchar(32) NOT NULL COMMENT '病历号',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL DEFAULT '' COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '性别',
  `age` int COMMENT '年龄',
  `regist_id` bigint NOT NULL DEFAULT '0' COMMENT '挂号ID',
  `regist_no` varchar(32) NOT NULL DEFAULT '' COMMENT '挂号单号',
  `visit_date` date NOT NULL DEFAULT (curdate()) COMMENT '就诊日期',
  `visit_type` tinyint COMMENT '就诊类型（1-初诊 2-复诊）',
  `dept_id` bigint NOT NULL DEFAULT '0' COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `doctor_id` bigint NOT NULL DEFAULT '0' COMMENT '医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '医生姓名',
  `chief_complaint` text COMMENT '主诉',
  `present_illness` text COMMENT '现病史',
  `past_history` text COMMENT '既往史',
  `personal_history` text COMMENT '个人史',
  `family_history` text COMMENT '家族史',
  `marriage_history` text COMMENT '婚育史',
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
  `record_status` tinyint COMMENT '病历状态（1-草稿 2-已提交 3-已归档 4-已作废）',
  `review_status` tinyint COMMENT '审核状态（0-待提交 1-待审核 2-审核通过 3-审核驳回）',
  `review_by` varchar(64) COMMENT '审核人',
  `review_time` datetime COMMENT '审核时间',
  `review_remark` varchar(500) COMMENT '审核意见',
  `guide_pdf_path` varchar(512) COMMENT '患者引导单文件路径',
  `rx_pdf_path` varchar(300) COMMENT '处方签PDF路径（结诊时生成）',
  `submit_time` datetime COMMENT '提交时间',
  `archive_time` datetime COMMENT '归档时间',
  `sign_status` tinyint NOT NULL DEFAULT '0' COMMENT '签名状态（0-未签名 1-已签名 2-签名已失效）',
  `sign_id` bigint COMMENT '当前有效签名ID',
  `signed_time` datetime COMMENT '最近一次签名时刻',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_medical_record_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊病历';

-- biz_nurse_schedule_filing  护理排班周备案
CREATE TABLE `biz_nurse_schedule_filing` (
  `id` bigint NOT NULL COMMENT '主键',
  `unit_type` tinyint NOT NULL COMMENT '排班单元类型（1-病区 2-门诊科室）',
  `ward_id` bigint NOT NULL COMMENT '护理单元ID',
  `ward_name` varchar(128) COMMENT '护理单元名称（快照）',
  `dept_id` bigint COMMENT '所属科室ID',
  `dept_name` varchar(128) COMMENT '所属科室名称（快照）',
  `week_start` date NOT NULL COMMENT '周起始日（周一）',
  `week_end` date NOT NULL COMMENT '周结束日（周日）',
  `filing_status` tinyint NOT NULL DEFAULT '0' COMMENT '备案态（0-待备案 1-已备案 2-已退回）',
  `filer_id` bigint COMMENT '备案人ID',
  `filer_name` varchar(64) COMMENT '备案人姓名（快照）',
  `filing_time` datetime COMMENT '备案时间',
  `opinion` varchar(200) COMMENT '备案意见/退回原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `create_by_id` bigint COMMENT '创建人ID',
  `update_by_id` bigint COMMENT '更新人ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_nurse_schedule_filing_unit_type_ward_id_week_start` (`unit_type`, `ward_id`, `week_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理排班周备案';

-- biz_nurse_schedule_log  护理排班审批与变更留痕
CREATE TABLE `biz_nurse_schedule_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `schedule_id` bigint COMMENT '排班行ID（整周提交/发布/复制等无单行的动作可为空）',
  `action_type` tinyint NOT NULL DEFAULT '0' COMMENT '动作（1-新增排班 2-修改/调班 3-删除排班 4-提交审核 5-审核发布 6-撤回 7-进入调班 8-整周提交 9-整周发布 10-复制上周）',
  `amount` int COMMENT '变更数量（批量铺/复制/整周发布条数）',
  `reason` varchar(200) COMMENT '变更/审批原因或意见',
  `occur_time` datetime NOT NULL DEFAULT (now()) COMMENT '发生时间',
  `schedule_date` date COMMENT '排班日期快照',
  `org_name` varchar(64) COMMENT '排班单元名称快照（病区/门诊科室）',
  `dept_name` varchar(64) COMMENT '科室名称快照',
  `nurse_name` varchar(64) COMMENT '护士姓名快照',
  `from_snapshot` varchar(200) COMMENT '变更前快照（班次/状态文本，调班/改格用）',
  `to_snapshot` varchar(200) COMMENT '变更后快照（班次/状态文本）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `create_by_id` bigint COMMENT '创建人ID',
  `update_by_id` bigint COMMENT '更新人ID',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除；审计只写不删）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理排班审批与变更留痕';

-- biz_nurse_schedule_template  护理排班模板（周模板：护士×星期几的固定出勤）
CREATE TABLE `biz_nurse_schedule_template` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `name` varchar(100) NOT NULL COMMENT '模板名称（如「内科一组周模板」）',
  `unit_type` tinyint NOT NULL DEFAULT '1' COMMENT '排班单元类型（1-病区 2-门诊科室；空按1-病区）',
  `ward_id` bigint NOT NULL DEFAULT '0' COMMENT '排班单元ID（unit_type=1 取病区ID，=2 取门诊科室ID）',
  `ward_name` varchar(128) NOT NULL COMMENT '排班单元名称（快照）',
  `week_parity` tinyint NOT NULL DEFAULT '0' COMMENT '单双周（0-每周 1-单周 2-双周，按 ISO 周号奇偶）',
  `valid_from` date COMMENT '生效起始日期（空=不限）',
  `valid_until` date COMMENT '生效截止日期（空=不限）',
  `scope` tinyint NOT NULL DEFAULT '0' COMMENT '共享范围（0-个人 1-权限组共享）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除；手动维护，便于已删除统计）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理排班模板（周模板：护士×星期几的固定出勤）';

-- biz_nurse_schedule_template_apply  护理排班模板套用记录（审计，只写不删）
CREATE TABLE `biz_nurse_schedule_template_apply` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_id` bigint COMMENT '套用模板ID',
  `template_name` varchar(128) COMMENT '套用模板名称（快照）',
  `unit_type` tinyint COMMENT '排班单元类型（1-病区 2-门诊科室）',
  `ward_id` bigint COMMENT '排班单元ID',
  `ward_name` varchar(128) COMMENT '排班单元名称（快照）',
  `apply_week_start` date COMMENT '套用目标周起始（周一）',
  `apply_week_end` date COMMENT '套用目标周结束（周日）',
  `applied_by_id` bigint COMMENT '操作人ID',
  `applied_by_name` varchar(50) COMMENT '操作人姓名',
  `generated_count` int NOT NULL DEFAULT '0' COMMENT '实际生成排班格数',
  `skipped_count` int NOT NULL DEFAULT '0' COMMENT '跳过格数（已排/日期已过/单双周不匹配/不在生效期/班次失效等）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-成功 1-部分跳过）',
  `remark` varchar(500) COMMENT '备注（含套用汇总说明）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理排班模板套用记录（审计，只写不删）';

-- biz_nurse_schedule_template_item  护理排班模板明细（护士×星期几×出勤/班次）
CREATE TABLE `biz_nurse_schedule_template_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `template_id` bigint NOT NULL COMMENT '所属模板ID',
  `employee_id` bigint NOT NULL COMMENT '护士ID',
  `employee_name` varchar(50) NOT NULL COMMENT '护士姓名（快照，报错点名用）',
  `week_day` tinyint NOT NULL COMMENT '星期几（1-周一 … 7-周日）',
  `schedule_status` tinyint NOT NULL COMMENT '出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）',
  `shift_id` bigint COMMENT '班次ID（仅状态=1-上班时有效；非上班由服务端强制置空）',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除；手动维护）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理排班模板明细（护士×星期几×出勤/班次）';

-- biz_nurse_schedule_template_rule  护理排班模板规则（班次实用排班模式：APN/8-8/小组）
CREATE TABLE `biz_nurse_schedule_template_rule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `rule_name` varchar(100) NOT NULL COMMENT '模式名称（如「APN 三班倒」）',
  `rule_code` varchar(50) NOT NULL COMMENT '模式编码（APN / 8-8 / GROUP，业务约定）',
  `pattern_detail` text COMMENT '班次模式明细（JSON：由哪些班次组成、排班结构说明；前端只读）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除；手动维护）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理排班模板规则（班次实用排班模式：APN/8-8/小组）';

-- biz_opd_station  门诊公共工位字典
CREATE TABLE `biz_opd_station` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `station_code` varchar(32) NOT NULL COMMENT '工位编码（全局唯一）',
  `station_name` varchar(64) NOT NULL COMMENT '工位名称（如：门诊总导诊台）',
  `station_kind` tinyint NOT NULL DEFAULT '0' COMMENT '工位类别（1-总导诊 2-门诊分诊台 3-输液室 4-注射室 5-采血室 9-其他）',
  `required_staff_type` tinyint NOT NULL DEFAULT '2' COMMENT '应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他 7-护工，默认2护理）',
  `shift_id` bigint COMMENT '标准班次ID（可空，派岗时再选班次）',
  `capacity` int NOT NULL DEFAULT '1' COMMENT '建议同时在岗人数',
  `phone` varchar(32) COMMENT '工位电话',
  `sort_no` int NOT NULL DEFAULT '0' COMMENT '排序号（小的在前）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_by_id` bigint COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_by_id` bigint COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_opd_station_station_code` (`station_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊公共工位字典';

-- biz_opd_station_assignment  门诊工位派岗记录
CREATE TABLE `biz_opd_station_assignment` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `assign_date` date NOT NULL COMMENT '上岗日期',
  `station_id` bigint NOT NULL COMMENT '工位ID（biz_opd_station.id）',
  `station_name` varchar(64) NOT NULL COMMENT '工位名称快照',
  `station_kind` tinyint NOT NULL DEFAULT '0' COMMENT '工位类别快照（1总导诊 2分诊台 3输液室 4注射室 5采血室 9其他）',
  `employee_id` bigint NOT NULL COMMENT '上岗员工ID',
  `employee_name` varchar(50) NOT NULL COMMENT '上岗员工姓名快照',
  `emp_code` varchar(32) COMMENT '员工工号快照',
  `staff_type` tinyint NOT NULL DEFAULT '2' COMMENT '上岗人岗位类别快照（默认2护理）',
  `shift_id` bigint COMMENT '班次ID',
  `start_time` varchar(5) COMMENT '班次开始时间快照 HH:mm',
  `end_time` varchar(5) COMMENT '班次结束时间快照 HH:mm',
  `is_loan` tinyint NOT NULL DEFAULT '0' COMMENT '是否跨单元借调（0-否 1-是）',
  `source_org_type` tinyint COMMENT '借出方单元类型（借调填：1科室 2病区 3全院）',
  `source_org_id` bigint COMMENT '借出方单元ID（借调填）',
  `source_org_name` varchar(100) COMMENT '借出方单元名称快照（借调填）',
  `assign_mode` tinyint NOT NULL DEFAULT '1' COMMENT '派岗方式（1-排班员指定）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-在岗 0-撤销）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_by_id` bigint COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_by_id` bigint COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_opd_station_assignment_assign_date_station_id_shift_id_employee_id` (`assign_date`, `station_id`, `shift_id`, `employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊工位派岗记录';

-- biz_patient  患者基本信息
CREATE TABLE `biz_patient` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `master_id` bigint COMMENT '主索引',
  `merge_status` tinyint NOT NULL DEFAULT '0' COMMENT '主索引状态（0-正常 1-已并入主档）',
  `merge_time` datetime COMMENT '并入主档的时间',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '性别（1-男 2-女 9-未知）',
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
  `marital_status` tinyint COMMENT '婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）',
  `blood_type` varchar(10) COMMENT '血型（A/B/O/AB）',
  `allergy_history` text COMMENT '过敏史',
  `medical_history` text COMMENT '既往病史',
  `medical_insurance_no` varchar(50) COMMENT '医保卡号/医保编号（费别为医保类时填写）',
  `medical_insurance_type` varchar(50) COMMENT '费别（自费/城镇职工医保/城乡居民医保/公费医疗/商业医疗保险）',
  `card_type` tinyint COMMENT '卡片类型（1-就诊卡 2-身份证 3-医保卡）',
  `card_no` varchar(50) COMMENT '卡片号码',
  `balance` decimal(10,2) COMMENT '账户余额',
  `total_expense` decimal(10,2) COMMENT '累计消费金额',
  `visit_count` int COMMENT '就诊次数',
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
  `status` tinyint COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_patient_patient_no` (`patient_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者基本信息';

-- biz_previsit_record  患者端预问诊记录（挂号后病史采集）
CREATE TABLE `biz_previsit_record` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `regist_id` bigint NOT NULL COMMENT '挂号ID（一次挂号一份问卷）',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(50) COMMENT '患者号',
  `patient_name` varchar(50) COMMENT '患者姓名',
  `dept_id` bigint COMMENT '就诊科室ID',
  `dept_name` varchar(50) COMMENT '就诊科室名称',
  `main_symptom` varchar(50) COMMENT '主症状',
  `answers_json` text COMMENT '问答明细JSON（题目与作答回显）',
  `free_text` text COMMENT '患者补充描述',
  `summary_ai` text COMMENT '病史摘要（模型凝练或规则模板）',
  `summary_source` tinyint COMMENT '摘要来源（1-模型 2-规则）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_previsit_record_regist_id_del_flag` (`regist_id`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者端预问诊记录（挂号后病史采集）';

-- biz_queue  候诊队列
CREATE TABLE `biz_queue` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `queue_no` varchar(20) NOT NULL DEFAULT '' COMMENT '排队序号',
  `regist_id` bigint NOT NULL DEFAULT '0' COMMENT '挂号ID',
  `visit_date` date COMMENT '就诊日期',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL DEFAULT '' COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `dept_id` bigint NOT NULL DEFAULT '0' COMMENT '科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `doctor_id` bigint COMMENT '医生ID',
  `doctor_name` varchar(50) COMMENT '医生姓名',
  `queue_type` tinyint COMMENT '队列类型（1-普通队列 2-优先队列 3-过号队列）',
  `regist_type` tinyint COMMENT '挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）',
  `queue_status` tinyint COMMENT '排队状态（2-候诊中 3-就诊中 4-已就诊 5-已退号 6-已过号 7-已失效）',
  `triage_status` tinyint NOT NULL DEFAULT '0' COMMENT '分诊状态（0-未分诊 1-已分诊）',
  `triage_level` tinyint COMMENT '分诊等级（1-危重 2-急症 3-亚急 4-非急）',
  `room_id` bigint COMMENT '诊室ID',
  `room_name` varchar(64) COMMENT '诊室名称',
  `sequence_no` int NOT NULL DEFAULT '0' COMMENT '顺序号',
  `call_time` datetime COMMENT '叫号时间',
  `call_count` int NOT NULL DEFAULT '0' COMMENT '叫号次数',
  `arrive_time` datetime COMMENT '到达时间',
  `start_time` datetime COMMENT '开始就诊时间',
  `end_time` datetime COMMENT '结束就诊时间',
  `wait_duration` int NOT NULL DEFAULT '0' COMMENT '等待时长（分钟）',
  `is_overdue` tinyint NOT NULL DEFAULT '0' COMMENT '是否过号（0-否 1-是）',
  `overdue_time` datetime COMMENT '过号时间',
  `overdue_reason` varchar(200) COMMENT '过号原因',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候诊队列';

-- biz_revisit_fee_policy  复诊收费策略
CREATE TABLE `biz_revisit_fee_policy` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `policy_name` varchar(100) NOT NULL COMMENT '策略名称',
  `revisit_source` tinyint NOT NULL DEFAULT '0' COMMENT '复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊 0-不限）',
  `same_doctor` tinyint NOT NULL DEFAULT '0' COMMENT '与原就诊医生（0-不限 1-要求同一医生 2-要求不同医生）',
  `same_dept` tinyint NOT NULL DEFAULT '0' COMMENT '与原就诊科室（0-不限 1-要求同一科室 2-要求不同科室）',
  `within_days` int COMMENT '与原就诊日最大间隔天数',
  `charge_mode` tinyint NOT NULL DEFAULT '0' COMMENT '收费方式（1-全额收费 2-免挂号费 3-免挂号费+诊查费）',
  `priority` int NOT NULL DEFAULT '100' COMMENT '匹配优先级',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()),
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()),
  `del_flag` tinyint NOT NULL DEFAULT '0',
  `remark` varchar(500),
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复诊收费策略';

-- biz_schedule  员工排班
CREATE TABLE `biz_schedule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `week_day` tinyint NOT NULL DEFAULT '0' COMMENT '星期（1-周一 7-周日）',
  `org_type` tinyint NOT NULL DEFAULT '1' COMMENT '排班单元类型（1-科室 2-病区 3-全院）',
  `org_id` bigint NOT NULL DEFAULT '0' COMMENT '排班单元ID（全院级为0）',
  `org_name` varchar(128) NOT NULL COMMENT '排班单元名称（快照）',
  `dept_id` bigint NOT NULL DEFAULT '0' COMMENT '科室ID（全院级为0）',
  `dept_name` varchar(128) NOT NULL COMMENT '科室名称（快照）',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `emp_code` varchar(32) COMMENT '工号（快照）',
  `employee_name` varchar(50) NOT NULL COMMENT '姓名（快照）',
  `employee_post_id` bigint COMMENT '员工岗位ID（人 × 科室 × 角色）',
  `staff_type` tinyint NOT NULL DEFAULT '1' COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `shift_id` bigint NOT NULL DEFAULT '0' COMMENT '标准班次ID（0-无班次）',
  `start_time` varchar(5) COMMENT '开始时间（HH:mm，班次快照）',
  `end_time` varchar(5) COMMENT '结束时间（HH:mm，班次快照，早于开始时间属次日）',
  `duty_status` tinyint NOT NULL DEFAULT '1' COMMENT '出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）',
  `publish_status` tinyint NOT NULL DEFAULT '0' COMMENT '发布态（0-草稿 1-待审核 2-已发布 3-调班中 4-已变更）',
  `attend_mode` tinyint NOT NULL DEFAULT '1' COMMENT '响应形态（1-坐班 2-听班 3-留院值班）',
  `clinic_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否出诊（0-否 1-是）',
  `work_minutes` int NOT NULL DEFAULT '0' COMMENT '工时（分钟）',
  `schedule_source` tinyint NOT NULL DEFAULT '1' COMMENT '生成来源（1-手工 2-模板 3-复制周期 4-换班）',
  `template_id` bigint COMMENT '来源排班周模板ID',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_schedule_employee_id_schedule_date_shift_id` (`employee_id`, `schedule_date`, `shift_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工排班';

-- biz_settlement_bill  结算账单
CREATE TABLE `biz_settlement_bill` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `bill_no` varchar(32) NOT NULL COMMENT '账单号',
  `patient_id` bigint NOT NULL DEFAULT '0' COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `encounter_type` tinyint NOT NULL DEFAULT '0' COMMENT '就诊类型（1-门诊 2-住院）',
  `encounter_id` bigint NOT NULL DEFAULT '0' COMMENT '就诊标识',
  `encounter_no` varchar(32) COMMENT '就诊标识单号（快照）',
  `bill_type` tinyint NOT NULL DEFAULT '2' COMMENT '账单类型（1-挂号费结算 2-门诊诊间结算 3-住院中途结算 4-出院结算）',
  `fee_count` int NOT NULL DEFAULT '0' COMMENT '纳入本账单的记账行数',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '应收合计',
  `discount_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '院内优惠/抹零',
  `settlement_mode` tinyint NOT NULL DEFAULT '1' COMMENT '结算方式（1-自费 2-医保）',
  `insurance_type` varchar(32) COMMENT '医保类型',
  `pool_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '医保统筹支付',
  `account_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '医保个人账户支付',
  `self_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '个人自费',
  `payable_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '患者应缴 = total - discount - pool - account',
  `paid_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已收合计',
  `refund_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '本账单已退合计',
  `bill_status` tinyint NOT NULL DEFAULT '1' COMMENT '账单状态（1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费）',
  `bill_date` date NOT NULL DEFAULT (curdate()) COMMENT '账务归属日',
  `bill_time` datetime COMMENT '结算生成时间',
  `bill_by_id` bigint COMMENT '结算人员工ID',
  `bill_by_name` varchar(64) COMMENT '结算人姓名（快照）',
  `pay_time` datetime COMMENT '收讫时间',
  `void_by_id` bigint COMMENT '作废操作人',
  `void_by_name` varchar(64) COMMENT '作废操作人姓名（快照）',
  `void_time` datetime COMMENT '作废时间',
  `void_reason` varchar(200) COMMENT '作废原因（必填）',
  `orig_bill_id` bigint COMMENT '红冲指针',
  `close_reason` varchar(200) COMMENT '结清说明',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()),
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()),
  `del_flag` tinyint NOT NULL DEFAULT '0',
  `remark` varchar(500),
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_settlement_bill_bill_no` (`bill_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算账单';

-- biz_shift  班次字典
CREATE TABLE `biz_shift` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `shift_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班次名称',
  `start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '开始时间（HH:mm）',
  `end_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '结束时间（HH:mm）',
  `cross_day` tinyint NOT NULL DEFAULT '0' COMMENT '是否跨零点（0-不跨 1-次日收）',
  `is_night` tinyint NOT NULL DEFAULT '0' COMMENT '是否夜班（1-夜班 0-白班）：夜班流入判定与连续夜班上限的唯一依据',
  `need_rest_hours` decimal(4,1) NOT NULL DEFAULT '0.0' COMMENT '下此班后最短休息小时数（0-不限制；夜班通例取16）',
  `late_grace_minutes` int NOT NULL DEFAULT '15' COMMENT '迟到宽限（分钟）：签到晚于班次开始超过这个数才算迟到',
  `duration_minutes` int NOT NULL DEFAULT '0' COMMENT '时长（分钟）',
  `dept_id` bigint COMMENT '适用科室ID',
  `schedule_type` tinyint COMMENT '班次类型（1-上午 2-下午 3-全天 4-凌晨）',
  `use_scope` tinyint NOT NULL DEFAULT '1' COMMENT '班次适用域（1-门诊 2-病区护理排班）',
  `apply_staff_type` tinyint COMMENT '适用岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-全部岗位通用）',
  `status` tinyint COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  `shift_code` varchar(16) COMMENT '班次业务短码（AM/PM/N/E/R，对接考勤机/薪资用）',
  `short_name` varchar(4) COMMENT '排班图显示缩写（如 夜/休/听）',
  `color` varchar(7) COMMENT '排班图颜色HEX（如 #F56C6C）',
  `count_work_hours` tinyint NOT NULL DEFAULT '1' COMMENT '是否计工时（1-是 0-否）',
  `count_night_allowance` tinyint NOT NULL DEFAULT '0' COMMENT '是否计夜班津贴（1-是 0-否）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次字典';

-- biz_staff_attendance  实际出勤（闭环第3步：计划 vs 实际的对照落点）
CREATE TABLE `biz_staff_attendance` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `staff_schedule_id` bigint COMMENT '关联的排班事实ID（biz_staff_schedule.id）；空=无计划的出勤（加班/支援/替班）',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_name` varchar(50) COMMENT '姓名（快照）',
  `emp_code` varchar(32) COMMENT '工号（快照）',
  `schedule_date` date NOT NULL COMMENT '出勤日期（归属哪一天；夜班签退跨到次日也算这天）',
  `org_type` tinyint NOT NULL COMMENT '实际出勤单元类型（1-科室 2-病区 3-全院）',
  `org_id` bigint NOT NULL DEFAULT '0' COMMENT '实际出勤单元ID（全院级为0）',
  `org_name` varchar(128) COMMENT '单元名称（快照）',
  `shift_id` bigint NOT NULL DEFAULT '0' COMMENT '班次ID（0-无班次，如自由工时的加班）',
  `staff_type` tinyint COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政）',
  `check_in` datetime COMMENT '签到时间（NULL=没签到：缺勤确认或手工登记的工时）',
  `check_out` datetime COMMENT '签退时间（NULL=还没签退或缺勤）',
  `actual_minutes` int COMMENT '实际工时（分钟）：打卡则算，无打卡由科室确认后手工填',
  `planned_minutes` int NOT NULL DEFAULT '0' COMMENT '计划工时（分钟）：biz_staff_schedule.work_minutes 的快照',
  `overtime_minutes` int NOT NULL DEFAULT '0' COMMENT '超时工时（分钟）：GREATEST(0, 实际-计划)',
  `attendance_status` tinyint NOT NULL DEFAULT '1' COMMENT '出勤状态（1-正常 2-迟到 3-早退 4-缺勤 5-替班 6-加班 7-支援）',
  `substitute_for` bigint COMMENT '替了谁的班（employee_id）',
  `confirm_status` tinyint NOT NULL DEFAULT '0' COMMENT '科室确认（0-待确认 1-已确认 2-有异议）',
  `confirm_by` varchar(64) COMMENT '确认人',
  `confirm_time` datetime COMMENT '确认时间',
  `data_source` tinyint NOT NULL DEFAULT '1' COMMENT '数据来源（1-人工登记 2-考勤机导入 3-系统判定）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-生效）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_staff_attendance_employee_id_schedule_date_org_type_org_id_shift_id` (`employee_id`, `schedule_date`, `org_type`, `org_id`, `shift_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实际出勤（闭环第3步：计划 vs 实际的对照落点）';

-- biz_staff_demand  人力需求（需求层：排班的驱动源与分母）
CREATE TABLE `biz_staff_demand` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `demand_date` date NOT NULL COMMENT '需求日期',
  `org_type` tinyint NOT NULL COMMENT '排班单元类型（1-科室 2-病区 3-全院）',
  `org_id` bigint NOT NULL DEFAULT '0' COMMENT '排班单元ID（全院级为0）',
  `org_name` varchar(128) COMMENT '排班单元名称（快照）',
  `period_code` tinyint NOT NULL DEFAULT '0' COMMENT '时段（0-全天 1-上午 2-下午 3-夜间）',
  `shift_id` bigint NOT NULL DEFAULT '0' COMMENT '班次ID（0-不限班次）',
  `staff_type` tinyint NOT NULL COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政）',
  `required_count` int NOT NULL DEFAULT '0' COMMENT '需求人数',
  `required_level` tinyint COMMENT '能级下限（0-不限；依赖 G-01，未做前一律 NULL）',
  `demand_source` tinyint NOT NULL DEFAULT '0' COMMENT '来源（1-门诊出诊派生 2-住院患者派生 3-手工调整）',
  `source_biz_id` bigint COMMENT '来源业务ID（出诊计划ID/病区ID）',
  `calc_basis` varchar(500) COMMENT '测算依据（怎么算出来的，写给人看的）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-生效）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()),
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()),
  `del_flag` tinyint NOT NULL DEFAULT '0',
  `remark` varchar(500),
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_staff_demand_demand_date_org_type_org_id_period_code_shift_id_staff_type` (`demand_date`, `org_type`, `org_id`, `period_code`, `shift_id`, `staff_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人力需求（需求层：排班的驱动源与分母）';

-- biz_staff_plan_rule  人力配置标准
CREATE TABLE `biz_staff_plan_rule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `org_type` tinyint NOT NULL DEFAULT '1' COMMENT '排班单元类型（1-科室 2-病区 3-全院）',
  `org_id` bigint NOT NULL DEFAULT '0' COMMENT '排班单元ID（全院级为0）',
  `org_name` varchar(128) COMMENT '排班单元名称（快照）',
  `shift_id` bigint NOT NULL DEFAULT '0' COMMENT '标准班次ID（0-该单元全部班次）',
  `staff_type` tinyint NOT NULL COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `min_staff` tinyint NOT NULL DEFAULT '0' COMMENT '最低在岗人数',
  `max_staff` tinyint NOT NULL DEFAULT '0' COMMENT '最高在岗人数',
  `max_week_hours` decimal(5,1) COMMENT '单周工时上限',
  `max_consecutive_night_days` tinyint COMMENT '连续夜班天数上限',
  `max_consecutive_work_days` tinyint COMMENT '连续上班天数上限',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_staff_plan_rule_org_type_org_id_shift_id_staff_type` (`org_type`, `org_id`, `shift_id`, `staff_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人力配置标准';

-- biz_triage_record  门诊分诊记录
CREATE TABLE `biz_triage_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `queue_id` bigint NOT NULL DEFAULT '0' COMMENT '队列ID',
  `regist_id` bigint COMMENT '挂号ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `patient_no` varchar(32) COMMENT '患者号（快照）',
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
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊分诊记录';

-- biz_triage_rule  智能导诊症状科室映射
CREATE TABLE `biz_triage_rule` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `symptom_code` varchar(32) NOT NULL COMMENT '症状编码',
  `symptom_name` varchar(100) NOT NULL COMMENT '症状名称',
  `keywords` varchar(500) COMMENT '匹配关键词（顿号分隔）',
  `dept_id` bigint NOT NULL COMMENT '推荐科室ID',
  `dept_name` varchar(100) COMMENT '推荐科室名称（快照）',
  `weight` int COMMENT '推荐权重（越大越靠前）',
  `urgent_flag` tinyint COMMENT '急症信号（0-否 1-是）',
  `advice` varchar(500) COMMENT '就诊提示',
  `status` tinyint COMMENT '状态（0-停用 1-启用）',
  `sort_order` int COMMENT '排序号',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_triage_rule_symptom_code_dept_id` (`symptom_code`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能导诊症状科室映射';

-- sys_clinic_room  诊室
CREATE TABLE `sys_clinic_room` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '诊室名称',
  `queue_prefix` varchar(2) COMMENT '呼叫代号（队列号前缀，如A/B/C…）',
  `code` varchar(100) NOT NULL DEFAULT '' COMMENT '诊室编号（ABCD）',
  `location` varchar(200) NOT NULL DEFAULT '' COMMENT '地理位置',
  `dept_id` bigint NOT NULL DEFAULT '0' COMMENT '所属科室ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '诊室状态（1-启用 0-停用）',
  `remark` varchar(255) COMMENT '备注信息',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诊室';

-- sys_department  科室
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dept_code` varchar(32) NOT NULL COMMENT '科室编码（唯一）',
  `dept_name` varchar(100) NOT NULL COMMENT '科室名称',
  `dept_type` varchar(20) NOT NULL DEFAULT '' COMMENT '科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父科室ID',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `dept_icon` varchar(200) COMMENT '科室图标',
  `dept_desc` varchar(500) COMMENT '科室描述',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `location` varchar(200) COMMENT '科室位置',
  `dept_leader_id` bigint COMMENT '科室负责人（sys_employee.id)',
  `is_open` tinyint COMMENT '是否开诊（0-否 1-是）',
  `status` tinyint COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  `outpatient_auditor_id` bigint COMMENT '门诊排班审核人 sys_employee.id',
  `nursing_auditor_id` bigint COMMENT '护理排班审核人 sys_employee.id',
  `schedule_auditor_id` bigint COMMENT '全院岗位排班审核人（sys_employee.id）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_department_dept_code` (`dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室';

-- sys_employee  员工
CREATE TABLE `sys_employee` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `emp_code` varchar(32) NOT NULL COMMENT '员工编号（唯一）',
  `emp_name` varchar(50) NOT NULL COMMENT '员工姓名',
  `emp_type` tinyint NOT NULL DEFAULT '1' COMMENT '员工类型（1-医生 2-护士 3-收费员 4-药剂师 5-管理员 6-其他）',
  `gender` tinyint COMMENT '性别',
  `birth_date` date COMMENT '出生日期',
  `hire_date` date COMMENT '入职日期',
  `id_card` varchar(18) COMMENT '身份证号',
  `phone` varchar(20) COMMENT '手机号码',
  `email` varchar(100) COMMENT '电子邮箱',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(200) COMMENT '科室ID',
  `ward_id` bigint COMMENT '病区ID（护理人员用；医生为空，医生归属科室+医疗组）',
  `group_id` bigint COMMENT '医疗组ID（医生用；sys_med_group.id）',
  `title` varchar(50) COMMENT '职称',
  `position` varchar(50) COMMENT '职位',
  `specialty` varchar(200) COMMENT '专业特长',
  `education` varchar(50) COMMENT '学历',
  `avatar` varchar(200) COMMENT '头像地址',
  `sign_image` longtext COMMENT '签名笔迹（base64 PNG，结诊/报告审核签名引用）',
  `is_expert` tinyint COMMENT '是否专家（0-否 1-是）',
  `is_registered` tinyint COMMENT '本机构注册执业（0-否 1-是 NULL-未登记；非本机构注册执业不得单独值班）',
  `is_fixed` tinyint COMMENT '是否固定医师（0-轮转 1-固定 NULL-未登记；急诊固定医师≥75%评审口径）',
  `expert_price` decimal(10,2) COMMENT '专家号价格',
  `status` tinyint COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_employee_emp_code` (`emp_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工';

-- sys_employee_post  员工岗位（角色×科室）
CREATE TABLE `sys_employee_post` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `is_primary` tinyint COMMENT '是否主科室（0-否 1-是）',
  `effective_date` date COMMENT '岗位生效日期',
  `expire_date` date COMMENT '岗位失效日期',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_employee_post_employee_id_role_id_dept_id` (`employee_id`, `role_id`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工岗位（角色×科室）';

-- sys_user  用户
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_name` varchar(64) NOT NULL COMMENT '用户名（唯一）',
  `password` varchar(200) NOT NULL DEFAULT '' COMMENT '密码',
  `real_name` varchar(64) COMMENT '真实姓名',
  `emp_id` bigint COMMENT '关联员工ID',
  `user_type` tinyint COMMENT '用户类型（1-系统用户 2-外部用户）',
  `patient_id` bigint COMMENT '关联患者ID',
  `openid` varchar(64) COMMENT '微信openid',
  `avatar` varchar(200) COMMENT '头像地址',
  `last_login_time` datetime COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COMMENT '最后登录IP',
  `login_count` int COMMENT '登录次数',
  `password_update_time` datetime COMMENT '密码更新时间',
  `status` tinyint COMMENT '状态（0-停用 1-启用 2-锁定）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_user_name` (`user_name`),
  UNIQUE KEY `uk_sys_user_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- sys_ward  病区
CREATE TABLE `sys_ward` (
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_code` varchar(32) NOT NULL COMMENT '病区编码',
  `ward_name` varchar(64) NOT NULL COMMENT '病区名称',
  `dept_id` bigint NOT NULL DEFAULT '0' COMMENT '所属科室ID',
  `total_beds` int NOT NULL DEFAULT '0' COMMENT '总床位数',
  `occupied_beds` int NOT NULL DEFAULT '0' COMMENT '已占用床位数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-正常）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`ward_id`),
  UNIQUE KEY `uk_sys_ward_ward_code` (`ward_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病区';

ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_bill_id` FOREIGN KEY (`bill_id`) REFERENCES `biz_settlement_bill` (`id`);
ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_create_by_id` FOREIGN KEY (`create_by_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_revisit_record_id` FOREIGN KEY (`revisit_record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_schedule_id` FOREIGN KEY (`schedule_id`) REFERENCES `biz_clinic_source` (`id`);
ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_slot_id` FOREIGN KEY (`slot_id`) REFERENCES `biz_clinic_source_slot` (`id`);
ALTER TABLE `biz_appoint_info`
    ADD CONSTRAINT `fk_biz_appoint_info_update_by_id` FOREIGN KEY (`update_by_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_clinic_source`
    ADD CONSTRAINT `fk_biz_clinic_source_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_clinic_source`
    ADD CONSTRAINT `fk_biz_clinic_source_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_clinic_source`
    ADD CONSTRAINT `fk_biz_clinic_source_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_clinic_source`
    ADD CONSTRAINT `fk_biz_clinic_source_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_clinic_source`
    ADD CONSTRAINT `fk_biz_clinic_source_staff_schedule_id` FOREIGN KEY (`staff_schedule_id`) REFERENCES `biz_schedule` (`id`);
ALTER TABLE `biz_clinic_source_change_log`
    ADD CONSTRAINT `fk_biz_clinic_source_change_log_from_employee_id` FOREIGN KEY (`from_employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_clinic_source_change_log`
    ADD CONSTRAINT `fk_biz_clinic_source_change_log_from_shift_id` FOREIGN KEY (`from_shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_clinic_source_change_log`
    ADD CONSTRAINT `fk_biz_clinic_source_change_log_staff_schedule_id` FOREIGN KEY (`staff_schedule_id`) REFERENCES `biz_schedule` (`id`);
ALTER TABLE `biz_clinic_source_change_log`
    ADD CONSTRAINT `fk_biz_clinic_source_change_log_to_employee_id` FOREIGN KEY (`to_employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_clinic_source_change_log`
    ADD CONSTRAINT `fk_biz_clinic_source_change_log_to_shift_id` FOREIGN KEY (`to_shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_clinic_source_log`
    ADD CONSTRAINT `fk_biz_clinic_source_log_schedule_id` FOREIGN KEY (`schedule_id`) REFERENCES `biz_clinic_source` (`id`);
ALTER TABLE `biz_clinic_source_slot`
    ADD CONSTRAINT `fk_biz_clinic_source_slot_schedule_id` FOREIGN KEY (`schedule_id`) REFERENCES `biz_clinic_source` (`id`);
ALTER TABLE `biz_clinic_source_slot_template`
    ADD CONSTRAINT `fk_biz_clinic_source_slot_template_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_clinic_source_template` (`id`);
ALTER TABLE `biz_clinic_source_template`
    ADD CONSTRAINT `fk_biz_clinic_source_template_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_clinic_source_template`
    ADD CONSTRAINT `fk_biz_clinic_source_template_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_clinic_source_template`
    ADD CONSTRAINT `fk_biz_clinic_source_template_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_clinic_source_template`
    ADD CONSTRAINT `fk_biz_clinic_source_template_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_medical_record`
    ADD CONSTRAINT `fk_biz_medical_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_medical_record`
    ADD CONSTRAINT `fk_biz_medical_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_medical_record`
    ADD CONSTRAINT `fk_biz_medical_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_medical_record`
    ADD CONSTRAINT `fk_biz_medical_record_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_medical_record`
    ADD CONSTRAINT `fk_biz_medical_record_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_nurse_schedule_filing`
    ADD CONSTRAINT `fk_biz_nurse_schedule_filing_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_nurse_schedule_filing`
    ADD CONSTRAINT `fk_biz_nurse_schedule_filing_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_nurse_schedule_template_apply`
    ADD CONSTRAINT `fk_biz_nurse_schedule_template_apply_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_nurse_schedule_template` (`id`);
ALTER TABLE `biz_opd_station_assignment`
    ADD CONSTRAINT `fk_biz_opd_station_assignment_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_opd_station_assignment`
    ADD CONSTRAINT `fk_biz_opd_station_assignment_station_id` FOREIGN KEY (`station_id`) REFERENCES `biz_opd_station` (`id`);
ALTER TABLE `biz_patient`
    ADD CONSTRAINT `fk_biz_patient_first_visit_dept_id` FOREIGN KEY (`first_visit_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_patient`
    ADD CONSTRAINT `fk_biz_patient_first_visit_doctor_id` FOREIGN KEY (`first_visit_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_patient`
    ADD CONSTRAINT `fk_biz_patient_last_visit_dept` FOREIGN KEY (`last_visit_dept`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_patient`
    ADD CONSTRAINT `fk_biz_patient_last_visit_doctor` FOREIGN KEY (`last_visit_doctor`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_patient`
    ADD CONSTRAINT `fk_biz_patient_master_id` FOREIGN KEY (`master_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_previsit_record`
    ADD CONSTRAINT `fk_biz_previsit_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_previsit_record`
    ADD CONSTRAINT `fk_biz_previsit_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_previsit_record`
    ADD CONSTRAINT `fk_biz_previsit_record_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_queue`
    ADD CONSTRAINT `fk_biz_queue_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_queue`
    ADD CONSTRAINT `fk_biz_queue_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_queue`
    ADD CONSTRAINT `fk_biz_queue_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_queue`
    ADD CONSTRAINT `fk_biz_queue_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_queue`
    ADD CONSTRAINT `fk_biz_queue_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_schedule`
    ADD CONSTRAINT `fk_biz_schedule_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_schedule`
    ADD CONSTRAINT `fk_biz_schedule_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_schedule`
    ADD CONSTRAINT `fk_biz_schedule_employee_post_id` FOREIGN KEY (`employee_post_id`) REFERENCES `sys_employee_post` (`id`);
ALTER TABLE `biz_schedule`
    ADD CONSTRAINT `fk_biz_schedule_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_schedule`
    ADD CONSTRAINT `fk_biz_schedule_template_id` FOREIGN KEY (`template_id`) REFERENCES `biz_clinic_source_template` (`id`);
ALTER TABLE `biz_settlement_bill`
    ADD CONSTRAINT `fk_biz_settlement_bill_bill_by_id` FOREIGN KEY (`bill_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_settlement_bill`
    ADD CONSTRAINT `fk_biz_settlement_bill_orig_bill_id` FOREIGN KEY (`orig_bill_id`) REFERENCES `biz_pay_channel_bill` (`id`);
ALTER TABLE `biz_settlement_bill`
    ADD CONSTRAINT `fk_biz_settlement_bill_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_settlement_bill`
    ADD CONSTRAINT `fk_biz_settlement_bill_void_by_id` FOREIGN KEY (`void_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_shift`
    ADD CONSTRAINT `fk_biz_shift_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_staff_attendance`
    ADD CONSTRAINT `fk_biz_staff_attendance_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_staff_attendance`
    ADD CONSTRAINT `fk_biz_staff_attendance_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_staff_attendance`
    ADD CONSTRAINT `fk_biz_staff_attendance_staff_schedule_id` FOREIGN KEY (`staff_schedule_id`) REFERENCES `biz_schedule` (`id`);
ALTER TABLE `biz_staff_attendance`
    ADD CONSTRAINT `fk_biz_staff_attendance_substitute_for` FOREIGN KEY (`substitute_for`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_staff_demand`
    ADD CONSTRAINT `fk_biz_staff_demand_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_staff_plan_rule`
    ADD CONSTRAINT `fk_biz_staff_plan_rule_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_triage_record`
    ADD CONSTRAINT `fk_biz_triage_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_triage_record`
    ADD CONSTRAINT `fk_biz_triage_record_queue_id` FOREIGN KEY (`queue_id`) REFERENCES `biz_queue` (`id`);
ALTER TABLE `biz_triage_record`
    ADD CONSTRAINT `fk_biz_triage_record_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_triage_record`
    ADD CONSTRAINT `fk_biz_triage_record_room_id` FOREIGN KEY (`room_id`) REFERENCES `sys_clinic_room` (`id`);
ALTER TABLE `biz_triage_record`
    ADD CONSTRAINT `fk_biz_triage_record_triage_nurse_id` FOREIGN KEY (`triage_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_triage_rule`
    ADD CONSTRAINT `fk_biz_triage_rule_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_clinic_room`
    ADD CONSTRAINT `fk_sys_clinic_room_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_department`
    ADD CONSTRAINT `fk_sys_department_dept_leader_id` FOREIGN KEY (`dept_leader_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_department`
    ADD CONSTRAINT `fk_sys_department_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_employee`
    ADD CONSTRAINT `fk_sys_employee_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_employee`
    ADD CONSTRAINT `fk_sys_employee_group_id` FOREIGN KEY (`group_id`) REFERENCES `sys_med_group` (`id`);
ALTER TABLE `sys_employee_post`
    ADD CONSTRAINT `fk_sys_employee_post_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_employee_post`
    ADD CONSTRAINT `fk_sys_employee_post_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_employee_post`
    ADD CONSTRAINT `fk_sys_employee_post_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`);
ALTER TABLE `sys_user`
    ADD CONSTRAINT `fk_sys_user_emp_id` FOREIGN KEY (`emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `sys_user`
    ADD CONSTRAINT `fk_sys_user_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `sys_ward`
    ADD CONSTRAINT `fk_sys_ward_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
