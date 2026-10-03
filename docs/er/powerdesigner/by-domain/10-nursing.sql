-- ============================================================
-- 领域 10 护理（文书·评估·排班·质控）（本域 8 表 + 上游参照 6 表 / 22 条关系）
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


-- biz_nursing_record  护理文书
CREATE TABLE `biz_nursing_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '护理文书号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(64) COMMENT '科室名称（快照）',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称（快照）',
  `bed_no` varchar(32) COMMENT '床号（快照）',
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

-- biz_nursing_assessment  护理评估单
CREATE TABLE `biz_nursing_assessment` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `assess_no` varchar(32) NOT NULL COMMENT '评估单号 AS+yyyyMMdd+4位',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) COMMENT '患者编号（快照）',
  `patient_name` varchar(128) COMMENT '患者姓名（快照）',
  `ward_id` bigint COMMENT '病区ID（快照）',
  `ward_name` varchar(128) COMMENT '病区名称（快照）',
  `bed_no` varchar(32) COMMENT '床号（快照）',
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

-- biz_nurse_schedule_rule  护理人力配置标准
CREATE TABLE `biz_nurse_schedule_rule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称（快照）',
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

-- biz_nurse_schedule  病区护理排班
CREATE TABLE `biz_nurse_schedule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称（快照）',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(128) COMMENT '科室名称（快照）',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `week_day` tinyint NOT NULL COMMENT '星期（1-周一 7-周日）',
  `employee_id` bigint NOT NULL COMMENT '护士ID',
  `emp_code` varchar(32) COMMENT '工号（快照）',
  `nurse_name` varchar(50) COMMENT '护士姓名（快照）',
  `nurse_title` varchar(50) COMMENT '职称',
  `shift_id` bigint COMMENT '班次ID',
  `shift_name` varchar(50) COMMENT '班次名称（快照）',
  `start_time` varchar(10) COMMENT '开始时间 HH（快照）',
  `end_time` varchar(10) COMMENT '结束时间 HH（快照）',
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

-- biz_nursing_qc_check  护理质量检查单
CREATE TABLE `biz_nursing_qc_check` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `check_no` varchar(32) NOT NULL COMMENT '检查单号 QC+yyyyMM+病区序号+类别',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) COMMENT '病区名称（快照）',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(128) COMMENT '科室名称（快照）',
  `check_month` char(7) NOT NULL COMMENT '检查月份 yyyy-MM',
  `check_date` date NOT NULL COMMENT '现场检查日期',
  `category` tinyint NOT NULL COMMENT '检查类别',
  `inspector_id` bigint COMMENT '检查人员工ID',
  `inspector_name` varchar(50) COMMENT '检查人姓名（快照）',
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
  `item_code` varchar(32) COMMENT '项目编码（快照）',
  `item_name` varchar(128) COMMENT '项目名称',
  `category` tinyint NOT NULL COMMENT '检查类别',
  `checked_num` int NOT NULL DEFAULT 0 COMMENT '抽查例数',
  `qualified_num` int NOT NULL DEFAULT 0 COMMENT '合格例数',
  `full_score` decimal(5,1) NOT NULL DEFAULT 0.0 COMMENT '本项应得分（快照）',
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
  `ward_name` varchar(128) COMMENT '病区名称（快照）',
  `dept_id` bigint NOT NULL COMMENT '科室ID（快照）',
  `dept_name` varchar(128) COMMENT '科室名称（快照）',
  `stat_month` char(7) NOT NULL COMMENT '统计月份 yyyy-MM',
  `indicator_code` varchar(32) NOT NULL COMMENT '指标编码',
  `indicator_name` varchar(64) NOT NULL COMMENT '指标名称（快照）',
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
