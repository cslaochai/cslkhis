-- 领域：10-护理（文书·评估·排班·质控）
-- 库：hn_biz_his    表数：8
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- biz_nursing_record  护理文书
-- ----------------------------
CREATE TABLE `biz_nursing_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '护理文书号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) DEFAULT NULL COMMENT '患者编号（快照）',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名（快照）',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(64) DEFAULT NULL COMMENT '科室名称（快照）',
  `ward_id` bigint DEFAULT NULL COMMENT '病区ID',
  `ward_name` varchar(64) DEFAULT NULL COMMENT '病区名称（快照）',
  `bed_no` varchar(32) DEFAULT NULL COMMENT '床号（快照）',
  `nursing_type` tinyint NOT NULL COMMENT '文书类型（1-三测单 2-护理记录单 3-生命体征监测）',
  `measure_time` datetime NOT NULL COMMENT '测量/记录时间',
  `shift` tinyint DEFAULT NULL COMMENT '班次（1-白班 2-小夜班 3-大夜班）',
  `temperature` decimal(4,1) DEFAULT NULL COMMENT '体温（℃）',
  `pulse` int DEFAULT NULL COMMENT '脉搏（次/分）',
  `respiration` int DEFAULT NULL COMMENT '呼吸（次/分）',
  `systolic_pressure` int DEFAULT NULL COMMENT '收缩压（mmHg）',
  `diastolic_pressure` int DEFAULT NULL COMMENT '舒张压（mmHg）',
  `spo2` int DEFAULT NULL COMMENT '血氧饱和度（%）',
  `stool_count` int DEFAULT NULL COMMENT '大便次数（次/日）',
  `urine_volume` int DEFAULT NULL COMMENT '尿量（ml）',
  `intake_volume` int DEFAULT NULL COMMENT '入量（ml）',
  `output_volume` int DEFAULT NULL COMMENT '出量（ml）',
  `nursing_level` tinyint DEFAULT NULL COMMENT '护理级别（1-特级护理 2-一级护理 3-二级护理 4-三级护理）',
  `nursing_content` text COMMENT '护理措施与病情观察记录正文',
  `nurse_id` bigint DEFAULT NULL COMMENT '记录护士ID（员工ID）',
  `nurse_name` varchar(64) DEFAULT NULL COMMENT '记录护士姓名',
  `record_status` tinyint NOT NULL DEFAULT '1' COMMENT '文书状态（1-草稿 2-已提交 3-已归档）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nr_admission_type_time` (`admission_id`,`nursing_type`,`measure_time`),
  KEY `idx_nr_admission` (`admission_id`,`measure_time`),
  KEY `idx_nr_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理文书';

-- ----------------------------
-- biz_nursing_assessment  护理评估单
-- ----------------------------
CREATE TABLE `biz_nursing_assessment` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `assess_no` varchar(32) NOT NULL COMMENT '评估单号 AS+yyyyMMdd+4位',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) DEFAULT NULL COMMENT '患者编号（快照）',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名（快照）',
  `ward_id` bigint DEFAULT NULL COMMENT '病区ID（快照）',
  `ward_name` varchar(128) DEFAULT NULL COMMENT '病区名称（快照）',
  `bed_no` varchar(32) DEFAULT NULL COMMENT '床号（快照）',
  `assess_type` tinyint NOT NULL COMMENT '评估类型（1-压疮Braden 2-跌倒Morse 3-疼痛NRS）',
  `total_score` int NOT NULL COMMENT '总分',
  `risk_level` tinyint NOT NULL COMMENT '风险等级（1-低风险 2-中风险 3-高风险 4-极高风险）',
  `items_json` text COMMENT '评分明细 JSON',
  `assess_time` datetime NOT NULL COMMENT '评估时间',
  `assess_nurse_id` bigint DEFAULT NULL COMMENT '评估护士ID（员工ID）',
  `assess_nurse_name` varchar(64) DEFAULT NULL COMMENT '评估护士姓名',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assess_no` (`assess_no`),
  KEY `idx_adm` (`admission_id`),
  KEY `idx_type_time` (`assess_type`,`assess_time`),
  KEY `idx_ward` (`ward_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理评估单';

-- ----------------------------
-- biz_nurse_schedule_rule  护理人力配置标准
-- ----------------------------
CREATE TABLE `biz_nurse_schedule_rule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) DEFAULT NULL COMMENT '病区名称（快照）',
  `shift_id` bigint NOT NULL DEFAULT '0' COMMENT '班次ID',
  `shift_name` varchar(50) DEFAULT NULL COMMENT '班次名称',
  `min_staff` tinyint NOT NULL DEFAULT '0' COMMENT '最低在岗人数',
  `max_staff` tinyint NOT NULL DEFAULT '0' COMMENT '最高在岗人数',
  `max_week_hours` decimal(5,1) DEFAULT NULL COMMENT '单周工时上限',
  `max_consecutive_night_days` tinyint DEFAULT NULL COMMENT '连续夜班天数上限',
  `max_consecutive_work_days` tinyint DEFAULT NULL COMMENT '连续上班天数上限',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `deprecated` tinyint NOT NULL DEFAULT '0' COMMENT '1=已并入 biz_staff_plan_rule（sql/206 起只读，勿再写入）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ward_shift_rule` (`ward_id`,`shift_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理人力配置标准';

-- ----------------------------
-- biz_nurse_schedule  病区护理排班
-- ----------------------------
CREATE TABLE `biz_nurse_schedule` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) DEFAULT NULL COMMENT '病区名称（快照）',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '科室名称（快照）',
  `unit_type` tinyint NOT NULL DEFAULT '1' COMMENT '排班单元类型（1-病区 2-门诊科室）',
  `unit_id` bigint DEFAULT NULL COMMENT '排班单元ID（unit_type=1 取 sys_ward.ward_id，=2 取 sys_department.id）',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `week_day` tinyint NOT NULL COMMENT '星期（1-周一 7-周日）',
  `employee_id` bigint NOT NULL COMMENT '护士ID',
  `emp_code` varchar(32) DEFAULT NULL COMMENT '工号（快照）',
  `nurse_name` varchar(50) DEFAULT NULL COMMENT '护士姓名（快照）',
  `nurse_title` varchar(50) DEFAULT NULL COMMENT '职称',
  `shift_id` bigint DEFAULT NULL COMMENT '班次ID',
  `shift_name` varchar(50) DEFAULT NULL COMMENT '班次名称（快照）',
  `start_time` varchar(10) DEFAULT NULL COMMENT '开始时间 HH（快照）',
  `end_time` varchar(10) DEFAULT NULL COMMENT '结束时间 HH（快照）',
  `work_minutes` int NOT NULL DEFAULT '0' COMMENT '工时',
  `schedule_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-上班 2-休息 3-请假 4-培训 5-停班）',
  `schedule_source` tinyint NOT NULL DEFAULT '1' COMMENT '生成来源（1-手工 2-模板 3-复制周期 4-换班）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `staff_schedule_id` bigint DEFAULT NULL COMMENT '关联的出勤事实 biz_staff_schedule.id（护理格子→底座的指路牌）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nurse_date` (`employee_id`,`schedule_date`),
  KEY `idx_ward_date` (`ward_id`,`schedule_date`),
  KEY `idx_date_status` (`schedule_date`,`schedule_status`),
  KEY `idx_staff_schedule` (`staff_schedule_id`),
  KEY `idx_unit_date` (`unit_type`,`unit_id`,`schedule_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病区护理排班';

-- ----------------------------
-- sys_nursing_qc_item  护理质控检查项目录
-- ----------------------------
CREATE TABLE `sys_nursing_qc_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `item_code` varchar(32) NOT NULL COMMENT '项目编码（BN/SC/SF/DC/IP + 两位序号）',
  `item_name` varchar(128) NOT NULL COMMENT '检查项目名称',
  `category` tinyint NOT NULL COMMENT '检查类别（1-基础护理 2-专科护理 3-安全管理 4-护理文书 5-院感防控）',
  `indicator_code` varchar(32) DEFAULT NULL COMMENT '计入的台账指标编码',
  `standard` varchar(500) DEFAULT NULL COMMENT '评价标准',
  `full_score` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '本项应得分',
  `target_rate` decimal(5,2) DEFAULT NULL COMMENT '单项目标合格率（%）',
  `key_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否重点项',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '同类别内排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qc_item_code` (`item_code`),
  KEY `idx_qc_item_cat` (`category`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理质控检查项目录';

-- ----------------------------
-- biz_nursing_qc_check  护理质量检查单
-- ----------------------------
CREATE TABLE `biz_nursing_qc_check` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `check_no` varchar(32) NOT NULL COMMENT '检查单号 QC+yyyyMM+病区序号+类别',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) DEFAULT NULL COMMENT '病区名称（快照）',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '科室名称（快照）',
  `check_month` char(7) NOT NULL COMMENT '检查月份 yyyy-MM',
  `check_date` date NOT NULL COMMENT '现场检查日期',
  `category` tinyint NOT NULL COMMENT '检查类别',
  `inspector_id` bigint DEFAULT NULL COMMENT '检查人员工ID',
  `inspector_name` varchar(50) DEFAULT NULL COMMENT '检查人姓名（快照）',
  `sample_count` int NOT NULL DEFAULT '0' COMMENT '抽查总例数',
  `qualified_count` int NOT NULL DEFAULT '0' COMMENT '合格总例数',
  `qualified_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '合格率%=合格例数/抽查例数*100',
  `full_score` decimal(7,1) NOT NULL DEFAULT '0.0' COMMENT '应得分',
  `total_score` decimal(7,1) NOT NULL DEFAULT '0.0' COMMENT '实得分',
  `score_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '得分率%=实得分/应得分*100',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-草稿 2-已确认）',
  `summary` varchar(500) DEFAULT NULL COMMENT '本轮小结',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_ward_month_cat` (`ward_id`,`check_month`,`category`),
  KEY `idx_check_month` (`check_month`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理质量检查单';

-- ----------------------------
-- biz_nursing_qc_check_item  护理质量检查明细
-- ----------------------------
CREATE TABLE `biz_nursing_qc_check_item` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `check_id` bigint NOT NULL COMMENT '检查单ID',
  `item_id` bigint NOT NULL COMMENT '检查项ID',
  `item_code` varchar(32) DEFAULT NULL COMMENT '项目编码（快照）',
  `item_name` varchar(128) DEFAULT NULL COMMENT '项目名称',
  `category` tinyint NOT NULL COMMENT '检查类别',
  `checked_num` int NOT NULL DEFAULT '0' COMMENT '抽查例数',
  `qualified_num` int NOT NULL DEFAULT '0' COMMENT '合格例数',
  `full_score` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '本项应得分（快照）',
  `score` decimal(5,1) NOT NULL DEFAULT '0.0' COMMENT '本项实得分=应得分*合格/抽查',
  `problem` varchar(500) DEFAULT NULL COMMENT '存在问题',
  `cause_analysis` varchar(500) DEFAULT NULL COMMENT '原因分析',
  `rectify_measure` varchar(500) DEFAULT NULL COMMENT '整改措施',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_item` (`check_id`,`item_id`),
  KEY `idx_qc_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理质量检查明细';

-- ----------------------------
-- biz_nursing_qc_indicator  护理质控指标台账
-- ----------------------------
CREATE TABLE `biz_nursing_qc_indicator` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_name` varchar(128) DEFAULT NULL COMMENT '病区名称（快照）',
  `dept_id` bigint NOT NULL COMMENT '科室ID（快照）',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '科室名称（快照）',
  `stat_month` char(7) NOT NULL COMMENT '统计月份 yyyy-MM',
  `indicator_code` varchar(32) NOT NULL COMMENT '指标编码',
  `indicator_name` varchar(64) NOT NULL COMMENT '指标名称（快照）',
  `unit` varchar(16) NOT NULL COMMENT '单位',
  `numerator` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '分子',
  `denominator` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '分母',
  `rate_value` decimal(10,4) DEFAULT NULL COMMENT '指标值',
  `target_value` decimal(10,4) DEFAULT NULL COMMENT '目标值',
  `reached_flag` tinyint DEFAULT NULL COMMENT '是否达标（1-达标 0-未达标）',
  `source_type` tinyint NOT NULL COMMENT '事实来源（1-检查表 2-不良事件+住院事实）',
  `report_status` tinyint NOT NULL DEFAULT '1' COMMENT '上报状态（1-未上报 2-已上报）',
  `calc_time` datetime DEFAULT NULL COMMENT '最近一次重算时间',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_indicator` (`ward_id`,`stat_month`,`indicator_code`),
  KEY `idx_indicator_month` (`stat_month`,`indicator_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='护理质控指标台账';
