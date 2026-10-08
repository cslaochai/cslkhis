-- 领域：16-急诊与全院总值班
-- 库：hn_biz_his    表数：5
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- biz_emergency  急诊记录
-- ----------------------------
CREATE TABLE `biz_emergency` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `emergency_no` varchar(32) NOT NULL COMMENT '急诊号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint DEFAULT NULL COMMENT '性别（1-男 2-女 9-未知）',
  `age` int DEFAULT '0' COMMENT '年龄',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `chief_complaint` text COMMENT '主诉',
  `triage_level` tinyint NOT NULL DEFAULT '3' COMMENT '分诊级别（1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症）',
  `zone` varchar(20) DEFAULT '绿区' COMMENT '区域（红区/黄区/绿区）',
  `green_channel` varchar(50) DEFAULT NULL COMMENT '绿色通道（胸痛中心/卒中中心/创伤中心/无）',
  `dept_id` bigint DEFAULT NULL COMMENT '接诊科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '接诊科室',
  `doctor_id` bigint DEFAULT NULL COMMENT '接诊医生ID',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '接诊医生',
  `assign_type` tinyint NOT NULL DEFAULT '0' COMMENT '派单方式',
  `unassigned_reason` varchar(200) DEFAULT NULL COMMENT '未派单原因',
  `target_see_minutes` int DEFAULT NULL COMMENT '该分诊级别的应接诊时限',
  `vital_signs` text COMMENT '生命体征',
  `diagnosis` text COMMENT '初步诊断',
  `treatment` text COMMENT '处理措施',
  `emergency_status` tinyint DEFAULT '1' COMMENT '急诊状态（1-候诊 2-诊治中 3-留观 4-转住院 5-离院 6-死亡）',
  `observation_bed` varchar(20) DEFAULT NULL COMMENT '留观床位号',
  `observation_ward_id` bigint DEFAULT NULL COMMENT '留观病区ID',
  `observation_bed_id` bigint DEFAULT NULL COMMENT '留观床位ID',
  `observation_start_time` datetime DEFAULT NULL COMMENT '开始留观时间',
  `observation_end_time` datetime DEFAULT NULL COMMENT '结束留观时间（转住院/离院/死亡时写入）',
  `admission_id` bigint DEFAULT NULL COMMENT '转住院产生的入院记录ID',
  `admission_time` datetime DEFAULT NULL COMMENT '入急诊时间',
  `diagnosis_time` datetime DEFAULT NULL COMMENT '开始诊治时间',
  `finish_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_by` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `del_flag` tinyint DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emergency_no` (`emergency_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_triage_level` (`triage_level`),
  KEY `idx_emergency_status` (`emergency_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_observation_bed_id` (`observation_bed_id`),
  KEY `idx_admission_id` (`admission_id`),
  KEY `idx_emg_status_admit` (`emergency_status`,`admission_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='急诊记录';

-- ----------------------------
-- biz_emergency_handover  急诊交班单
-- ----------------------------
CREATE TABLE `biz_emergency_handover` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `handover_no` varchar(32) NOT NULL COMMENT '交班单号',
  `dept_id` bigint NOT NULL COMMENT '交班科室ID',
  `dept_name` varchar(100) NOT NULL COMMENT '交班科室名称',
  `from_emp_id` bigint NOT NULL COMMENT '交出人员工ID',
  `from_emp_name` varchar(50) NOT NULL COMMENT '交出人姓名',
  `take_emp_id` bigint NOT NULL COMMENT '接班人员工ID',
  `take_emp_name` varchar(50) NOT NULL COMMENT '接班人姓名',
  `shift_name` varchar(32) DEFAULT NULL COMMENT '班次名',
  `period_begin` datetime NOT NULL COMMENT '本班区间起',
  `period_end` datetime NOT NULL COMMENT '本班区间止',
  `pending_count` int NOT NULL DEFAULT '0' COMMENT '本次移交未闭环人数（定格）',
  `pool_count` int NOT NULL DEFAULT '0' COMMENT '其中交班前无人指派的条数',
  `overdue_count` int NOT NULL DEFAULT '0' COMMENT '其中候诊已超时的条数（定格）',
  `observation_count` int NOT NULL DEFAULT '0' COMMENT '其中留观中的条数（定格）',
  `obs_over_limit_count` int NOT NULL DEFAULT '0' COMMENT '其中留观已超时限的条数（定格）',
  `remark` varchar(500) DEFAULT NULL COMMENT '整单交代备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记（0-未删除 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_handover_no` (`handover_no`),
  KEY `idx_ho_dept_time` (`dept_id`,`period_end`),
  KEY `idx_ho_from` (`from_emp_id`,`period_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='急诊交班单';

-- ----------------------------
-- biz_emergency_handover_item  急诊交班明细
-- ----------------------------
CREATE TABLE `biz_emergency_handover_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `handover_id` bigint NOT NULL COMMENT '交班单ID',
  `emergency_id` bigint NOT NULL COMMENT '急诊记录ID',
  `emergency_no` varchar(32) NOT NULL COMMENT '急诊号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `triage_level` tinyint DEFAULT NULL COMMENT '分诊级别',
  `emergency_status` tinyint NOT NULL COMMENT '交班时该患者的急诊状态（1-候诊 2-诊治中 3-留观）',
  `from_doctor_id` bigint DEFAULT NULL COMMENT '交班时的负责医生ID',
  `from_doctor_name` varchar(50) DEFAULT NULL COMMENT '交班时的负责医生姓名',
  `take_doctor_id` bigint NOT NULL COMMENT '接续责任人',
  `take_doctor_name` varchar(50) NOT NULL COMMENT '接续责任人姓名',
  `disposition` varchar(100) NOT NULL COMMENT '去向/处置交代',
  `handover_note` varchar(300) DEFAULT NULL COMMENT '逐条补充交代（过敏史/管路/家属联系方式等，截到 300）',
  `wait_minutes` bigint DEFAULT NULL COMMENT '候诊已等多久',
  `obs_hours` int DEFAULT NULL COMMENT '已留观小时数',
  `overdue_level` tinyint NOT NULL DEFAULT '0' COMMENT '超时档位定格（0-未超时 1-超时 2-严重超时）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记（0-未删除 1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_handover_item` (`handover_id`,`emergency_id`),
  KEY `idx_hi_emergency` (`emergency_id`),
  KEY `idx_hi_take` (`take_doctor_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='急诊交班明细';

-- ----------------------------
-- biz_duty_roster  全院总值班排班
-- ----------------------------
CREATE TABLE `biz_duty_roster` (
  `id` bigint NOT NULL COMMENT '主键',
  `duty_date` date NOT NULL COMMENT '值班日期',
  `shift_type` tinyint NOT NULL DEFAULT '1' COMMENT '班次（1-白班 2-夜班 00-次日08）',
  `role_type` tinyint NOT NULL DEFAULT '1' COMMENT '班内角色（1-主班 2-副班）',
  `employee_id` bigint NOT NULL COMMENT '值班人',
  `employee_name` varchar(50) DEFAULT NULL COMMENT '值班人姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '值班人原属科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '值班人原属科室名称',
  `phone` varchar(32) DEFAULT NULL COMMENT '值班联系电话',
  `start_time` varchar(5) DEFAULT NULL COMMENT '班次开始时间（HH:mm）',
  `end_time` varchar(5) DEFAULT NULL COMMENT '班次结束时间（HH:mm）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-有效 0-停用）',
  `substitute_emp_id` bigint DEFAULT NULL COMMENT '临时换班后的实际值班人',
  `substitute_emp_name` varchar(50) DEFAULT NULL COMMENT '换班后实际值班人姓名',
  `substitute_time` datetime DEFAULT NULL COMMENT '换班时间',
  `substitute_reason` varchar(200) DEFAULT NULL COMMENT '换班原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_duty_date_shift_role` (`duty_date`,`shift_type`,`role_type`),
  KEY `idx_duty_date` (`duty_date`),
  KEY `idx_duty_emp` (`employee_id`),
  KEY `idx_duty_sub` (`substitute_emp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全院总值班排班';

-- ----------------------------
-- biz_duty_log  总值班值班日志
-- ----------------------------
CREATE TABLE `biz_duty_log` (
  `id` bigint NOT NULL COMMENT '主键',
  `duty_date` date NOT NULL COMMENT '值班日期',
  `shift_type` tinyint NOT NULL COMMENT '班次 1-白班 2-夜班（1-白班 2-夜班）',
  `roster_id` bigint DEFAULT NULL COMMENT '所属排班行 biz_duty_roster.id',
  `employee_id` bigint NOT NULL COMMENT '值班人',
  `employee_name` varchar(64) DEFAULT NULL COMMENT '值班人姓名',
  `log_type` tinyint NOT NULL DEFAULT '1' COMMENT '记录类型 1-值班事件 2-遗留事项 3-巡查记录（1-值班事件 2-遗留事项 3-巡查记录）',
  `happen_time` datetime DEFAULT NULL COMMENT '事件发生时间',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` varchar(2000) DEFAULT NULL COMMENT '事件经过',
  `handle_result` varchar(1000) DEFAULT NULL COMMENT '处理情况',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-待处理 1-已处理 2-已交班（0-待处理 1-已处理 2-已交班）',
  `handover_emp_id` bigint DEFAULT NULL COMMENT '接班人',
  `handover_emp_name` varchar(64) DEFAULT NULL COMMENT '接班人姓名',
  `handover_time` datetime DEFAULT NULL COMMENT '交班时间',
  `ack_time` datetime DEFAULT NULL COMMENT '接班人签收时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '记录人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_log_duty` (`duty_date`,`shift_type`),
  KEY `idx_log_emp` (`employee_id`),
  KEY `idx_log_handover` (`handover_emp_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='总值班值班日志';
