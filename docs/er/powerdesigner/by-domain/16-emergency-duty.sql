-- ============================================================
-- 领域 16 急诊与全院总值班（本域 6 表 + 上游参照 8 表 / 23 条关系）
-- 由 workspace/_er/refresh.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


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
  `dept_name` varchar(100) NOT NULL COMMENT '交班科室名称（快照）',
  `from_emp_id` bigint NOT NULL COMMENT '交出人员工ID',
  `from_emp_name` varchar(50) NOT NULL COMMENT '交出人姓名（快照）',
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
  `emergency_no` varchar(32) NOT NULL COMMENT '急诊号（快照）',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `triage_level` tinyint COMMENT '分诊级别',
  `emergency_status` tinyint NOT NULL COMMENT '交班时该患者的急诊状态（1-候诊 2-诊治中 3-留观）',
  `from_doctor_id` bigint COMMENT '交班时的负责医生ID',
  `from_doctor_name` varchar(50) COMMENT '交班时的负责医生姓名（快照）',
  `take_doctor_id` bigint NOT NULL COMMENT '接续责任人',
  `take_doctor_name` varchar(50) NOT NULL COMMENT '接续责任人姓名（快照）',
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

-- biz_duty_roster  全院总值班排班
CREATE TABLE `biz_duty_roster` (
  `id` bigint NOT NULL COMMENT '主键',
  `duty_date` date NOT NULL COMMENT '值班日期',
  `shift_type` tinyint NOT NULL DEFAULT 1 COMMENT '班次（1-白班 2-夜班 00-次日08）',
  `role_type` tinyint NOT NULL DEFAULT 1 COMMENT '班内角色（1-主班 2-副班）',
  `employee_id` bigint NOT NULL COMMENT '值班人',
  `employee_name` varchar(50) COMMENT '值班人姓名',
  `dept_id` bigint COMMENT '值班人原属科室ID',
  `dept_name` varchar(100) COMMENT '值班人原属科室名称（快照）',
  `phone` varchar(32) COMMENT '值班联系电话',
  `start_time` varchar(5) COMMENT '班次开始时间（HH:mm）',
  `end_time` varchar(5) COMMENT '班次结束时间（HH:mm）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-有效 0-停用）',
  `substitute_emp_id` bigint COMMENT '临时换班后的实际值班人',
  `substitute_emp_name` varchar(50) COMMENT '换班后实际值班人姓名（快照）',
  `substitute_time` datetime COMMENT '换班时间',
  `substitute_reason` varchar(200) COMMENT '换班原因',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `shift_id` bigint COMMENT '标准班次ID',
  `post_id` bigint COMMENT '值班点位ID',
  `staff_schedule_id` bigint COMMENT '员工排班ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_duty_post_date` (`post_id`, `duty_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全院总值班排班';

-- biz_duty_log  总值班值班日志
CREATE TABLE `biz_duty_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `duty_date` date NOT NULL COMMENT '值班日期',
  `shift_type` tinyint NOT NULL COMMENT '班次 1-白班 2-夜班（1-白班 2-夜班）',
  `roster_id` bigint COMMENT '所属排班行 biz_duty_roster.id',
  `employee_id` bigint NOT NULL COMMENT '值班人',
  `employee_name` varchar(64) COMMENT '值班人姓名（快照）',
  `log_type` tinyint NOT NULL DEFAULT 1 COMMENT '记录类型 1-值班事件 2-遗留事项 3-巡查记录（1-值班事件 2-遗留事项 3-巡查记录）',
  `happen_time` datetime COMMENT '事件发生时间',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` varchar(2000) COMMENT '事件经过',
  `handle_result` varchar(1000) COMMENT '处理情况',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0-待处理 1-已处理 2-已交班（0-待处理 1-已处理 2-已交班）',
  `handover_emp_id` bigint COMMENT '接班人',
  `handover_emp_name` varchar(64) COMMENT '接班人姓名（快照）',
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

-- biz_duty_post  值班点位
CREATE TABLE `biz_duty_post` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `post_code` varchar(32) NOT NULL COMMENT '点位编码',
  `post_name` varchar(64) NOT NULL COMMENT '点位名称',
  `duty_scope` tinyint NOT NULL DEFAULT 1 COMMENT '责任范围（1-全院行政 2-急诊 3-感染 4-总务 5-信息）',
  `org_type` tinyint NOT NULL DEFAULT 3 COMMENT '排班单元类型（1-科室 2-病区 3-全院）',
  `org_id` bigint NOT NULL DEFAULT 0 COMMENT '排班单元ID（全院级为0）',
  `role_type` tinyint NOT NULL DEFAULT 1 COMMENT '班内角色（1-主班 2-副班）',
  `duty_level` tinyint NOT NULL DEFAULT 0 COMMENT '值班层级（0-不适用 1-一线 2-二线 3-三线）',
  `attend_mode` tinyint NOT NULL DEFAULT 3 COMMENT '响应形态（1-坐班 2-听班 3-留院值班）',
  `shift_id` bigint NOT NULL COMMENT '标准班次ID',
  `required_staff_type` tinyint COMMENT '应到岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他，空-不限）',
  `phone` varchar(32) COMMENT '点位值班电话',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_duty_post_code` (`post_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值班点位';

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

-- biz_staff_schedule  员工排班
CREATE TABLE `biz_staff_schedule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `week_day` tinyint NOT NULL COMMENT '星期（1-周一 7-周日）',
  `org_type` tinyint NOT NULL DEFAULT 1 COMMENT '排班单元类型（1-科室 2-病区 3-全院）',
  `org_id` bigint NOT NULL DEFAULT 0 COMMENT '排班单元ID（全院级为0）',
  `org_name` varchar(128) NOT NULL DEFAULT '' COMMENT '排班单元名称（快照）',
  `dept_id` bigint NOT NULL DEFAULT 0 COMMENT '科室ID（全院级为0）',
  `dept_name` varchar(128) NOT NULL DEFAULT '' COMMENT '科室名称（快照）',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `emp_code` varchar(32) COMMENT '工号（快照）',
  `employee_name` varchar(50) NOT NULL DEFAULT '' COMMENT '姓名（快照）',
  `employee_post_id` bigint COMMENT '员工岗位ID（人 × 科室 × 角色）',
  `staff_type` tinyint NOT NULL DEFAULT 1 COMMENT '岗位类别（1-医生 2-护理 3-医技 4-药学 5-收费 6-行政其他）',
  `shift_id` bigint NOT NULL DEFAULT 0 COMMENT '标准班次ID（0-无班次）',
  `start_time` varchar(5) COMMENT '开始时间（HH:mm，班次快照）',
  `end_time` varchar(5) COMMENT '结束时间（HH:mm，班次快照，早于开始时间属次日）',
  `duty_status` tinyint NOT NULL DEFAULT 1 COMMENT '出勤状态（1-上班 2-休息 3-请假 4-培训 5-停班）',
  `attend_mode` tinyint NOT NULL DEFAULT 1 COMMENT '响应形态（1-坐班 2-听班 3-留院值班）',
  `clinic_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否出诊（0-否 1-是）',
  `work_minutes` int NOT NULL DEFAULT 0 COMMENT '工时（分钟）',
  `schedule_source` tinyint NOT NULL DEFAULT 1 COMMENT '生成来源（1-手工 2-模板 3-复制周期 4-换班）',
  `template_id` bigint COMMENT '来源排班周模板ID',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_date_shift` (`employee_id`, `schedule_date`, `shift_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工排班';

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
ALTER TABLE `biz_duty_log` ADD CONSTRAINT `fk_biz_duty_log_roster_id` FOREIGN KEY (`roster_id`) REFERENCES `biz_duty_roster` (`id`);
ALTER TABLE `biz_duty_log` ADD CONSTRAINT `fk_biz_duty_log_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_duty_log` ADD CONSTRAINT `fk_biz_duty_log_handover_emp_id` FOREIGN KEY (`handover_emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_duty_roster` ADD CONSTRAINT `fk_biz_duty_roster_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_duty_roster` ADD CONSTRAINT `fk_biz_duty_roster_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_duty_roster` ADD CONSTRAINT `fk_biz_duty_roster_substitute_emp_id` FOREIGN KEY (`substitute_emp_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_duty_roster` ADD CONSTRAINT `fk_biz_duty_roster_shift_id` FOREIGN KEY (`shift_id`) REFERENCES `biz_shift` (`id`);
ALTER TABLE `biz_duty_roster` ADD CONSTRAINT `fk_biz_duty_roster_post_id` FOREIGN KEY (`post_id`) REFERENCES `biz_duty_post` (`id`);
ALTER TABLE `biz_duty_roster` ADD CONSTRAINT `fk_biz_duty_roster_staff_schedule_id` FOREIGN KEY (`staff_schedule_id`) REFERENCES `biz_staff_schedule` (`id`);
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
