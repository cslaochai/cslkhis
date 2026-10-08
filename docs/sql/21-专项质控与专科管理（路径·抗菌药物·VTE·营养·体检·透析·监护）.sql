-- 领域：21-专项质控与专科管理（路径·抗菌药物·VTE·营养·体检·透析·监护）
-- 库：hn_biz_his    表数：27
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- biz_pathway  临床路径模板
-- ----------------------------
CREATE TABLE `biz_pathway` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `pathway_code` varchar(32) NOT NULL COMMENT '路径编码',
  `pathway_name` varchar(128) NOT NULL COMMENT '路径名称',
  `dept_id` bigint DEFAULT NULL COMMENT '适用科室ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '适用科室名称',
  `diagnosis` varchar(255) DEFAULT NULL COMMENT '适用病种/诊断',
  `version` varchar(16) NOT NULL DEFAULT 'V1' COMMENT '版本号',
  `total_days` int NOT NULL DEFAULT '0' COMMENT '路径总日数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-草稿 2-使用中 3-已停用）',
  `publish_by` varchar(64) DEFAULT NULL COMMENT '发布人',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_pathway_code` (`pathway_code`),
  KEY `idx_pathway_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='临床路径模板';

-- ----------------------------
-- biz_pathway_step  临床路径步骤
-- ----------------------------
CREATE TABLE `biz_pathway_step` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `pathway_id` bigint NOT NULL COMMENT '模板ID',
  `day_no` int NOT NULL COMMENT '路径日',
  `item_type` tinyint NOT NULL COMMENT '项目类型（1-诊疗 2-用药 3-手术操作 4-护理 5-病情评估 6-宣教）',
  `item_name` varchar(128) NOT NULL COMMENT '项目名称',
  `item_code` varchar(64) DEFAULT NULL COMMENT '字典项目编码',
  `content` varchar(500) DEFAULT NULL COMMENT '路径要求/具体内容',
  `sort_no` int NOT NULL DEFAULT '1' COMMENT '同日内的顺序',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_step_pathway_day` (`pathway_id`,`day_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='临床路径步骤';

-- ----------------------------
-- biz_pathway_enroll  临床路径入径记录
-- ----------------------------
CREATE TABLE `biz_pathway_enroll` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `enroll_no` varchar(32) NOT NULL COMMENT '入径单号',
  `pathway_id` bigint NOT NULL COMMENT '模板ID',
  `pathway_code` varchar(32) DEFAULT NULL COMMENT '路径编码',
  `pathway_name` varchar(128) DEFAULT NULL COMMENT '路径名称',
  `version` varchar(16) DEFAULT NULL COMMENT '版本号',
  `total_days` int NOT NULL DEFAULT '0' COMMENT '路径总日数',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '入院科室ID',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '入院科室名称',
  `diagnosis` varchar(255) DEFAULT NULL COMMENT '入院诊断',
  `enroll_date` date NOT NULL COMMENT '入径日期',
  `enroll_by` varchar(64) DEFAULT NULL COMMENT '入径操作人',
  `enroll_time` datetime DEFAULT NULL COMMENT '入径时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-在径 2-已完成 3-已退径）',
  `finish_date` date DEFAULT NULL COMMENT '完成/退径日期',
  `finish_by` varchar(64) DEFAULT NULL COMMENT '完成/退径操作人',
  `abort_reason` varchar(255) DEFAULT NULL COMMENT '退径原因',
  `variance_count` int NOT NULL DEFAULT '0' COMMENT '变异次数',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enroll_no` (`enroll_no`),
  KEY `idx_enroll_admission` (`admission_id`),
  KEY `idx_enroll_pathway` (`pathway_id`),
  KEY `idx_enroll_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='临床路径入径记录';

-- ----------------------------
-- biz_pathway_variance  临床路径变异登记
-- ----------------------------
CREATE TABLE `biz_pathway_variance` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `enroll_id` bigint NOT NULL COMMENT '入径记录ID',
  `day_no` int NOT NULL COMMENT '发生路径日',
  `variance_type` tinyint NOT NULL COMMENT '变异类型（1-医嘱变动 2-检查检验变动 3-手术操作变动 4-用药变动 5-出院延期 6-其他）',
  `variance_reason` varchar(255) NOT NULL COMMENT '变异原因',
  `handling` varchar(255) DEFAULT NULL COMMENT '处理措施',
  `occurred_date` date NOT NULL COMMENT '变异发生日期',
  `recorder_id` bigint DEFAULT NULL COMMENT '登记人（员工ID）',
  `recorder_name` varchar(64) DEFAULT NULL COMMENT '登记人姓名',
  `record_time` datetime DEFAULT NULL COMMENT '登记时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_variance_enroll` (`enroll_id`),
  KEY `idx_variance_type` (`variance_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='临床路径变异登记';

-- ----------------------------
-- biz_clinical_rule_check  临床规则校验记录
-- ----------------------------
CREATE TABLE `biz_clinical_rule_check` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `check_no` varchar(32) NOT NULL COMMENT '校验编号',
  `record_id` bigint NOT NULL COMMENT '病历ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `rule_type` tinyint NOT NULL COMMENT '规则类型（1-配伍禁忌 2-检验诊断关联性 3-用药合理性）',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_content` varchar(500) DEFAULT NULL COMMENT '规则内容',
  `check_result` tinyint NOT NULL DEFAULT '1' COMMENT '校验结果（0-不通过 1-通过）',
  `error_level` tinyint DEFAULT NULL COMMENT '错误级别（1-警告 2-错误 3-严重）',
  `error_detail` varchar(1000) DEFAULT NULL COMMENT '错误详情',
  `suggestion` varchar(500) DEFAULT NULL COMMENT '处理建议',
  `check_status` tinyint NOT NULL DEFAULT '1' COMMENT '处理状态（1-待处理 2-已处理 3-已忽略）',
  `check_by` varchar(64) DEFAULT NULL COMMENT '校验人',
  `check_time` datetime DEFAULT NULL COMMENT '校验时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_check_status` (`check_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='临床规则校验记录';

-- ----------------------------
-- biz_antibiotic_alias  抗菌药物品名别名
-- ----------------------------
CREATE TABLE `biz_antibiotic_alias` (
  `id` bigint NOT NULL COMMENT '主键',
  `drug_id` bigint NOT NULL COMMENT '药品ID',
  `drug_code` varchar(32) DEFAULT NULL COMMENT '药品编码',
  `drug_name` varchar(200) DEFAULT NULL COMMENT '药品目录名',
  `alias_name` varchar(200) NOT NULL COMMENT '别名',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_antibiotic_alias` (`alias_name`) COMMENT '一个别名只能指向一种药品；本表无 del_flag，删除走物理删',
  KEY `idx_alias_drug` (`drug_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抗菌药物品名别名';

-- ----------------------------
-- biz_antibiotic_auth  抗菌药物处方权授权
-- ----------------------------
CREATE TABLE `biz_antibiotic_auth` (
  `id` bigint NOT NULL COMMENT '主键',
  `auth_no` varchar(32) NOT NULL COMMENT '授权编号',
  `doctor_id` bigint NOT NULL COMMENT '医师ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '医师姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `title` varchar(50) DEFAULT NULL COMMENT '职称',
  `auth_level` tinyint NOT NULL COMMENT '授权级别（1-非限制使用级 2-限制使用级 3-特殊使用级）',
  `auth_basis` varchar(100) DEFAULT NULL COMMENT '授权依据',
  `auth_date` date NOT NULL COMMENT '授权日期',
  `expire_date` date NOT NULL COMMENT '有效期至',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-有效 2-暂停 3-取消）',
  `authorizer` varchar(50) DEFAULT NULL COMMENT '授权人',
  `authorize_org` varchar(100) DEFAULT NULL COMMENT '授权部门',
  `revoke_reason` varchar(500) DEFAULT NULL COMMENT '暂停/取消原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_antibiotic_auth_doctor` (`doctor_id`,`auth_level`) COMMENT '一位医师一个级别一条；本表无 del_flag，删除走物理删',
  KEY `idx_auth_doctor` (`doctor_id`),
  KEY `idx_auth_status` (`status`,`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抗菌药物处方权授权';

-- ----------------------------
-- biz_antibiotic_stats  抗菌药物使用监测指标
-- ----------------------------
CREATE TABLE `biz_antibiotic_stats` (
  `id` bigint NOT NULL COMMENT '主键',
  `stat_month` char(7) NOT NULL COMMENT '统计月份',
  `scope_type` tinyint NOT NULL DEFAULT '1' COMMENT '统计范围（1-全院 2-科室）',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `op_rx_count` int NOT NULL DEFAULT '0' COMMENT '门急诊处方总数（处方状态 3/4，源 1/2）',
  `op_abx_rx_count` int NOT NULL DEFAULT '0' COMMENT '含抗菌药物的门急诊处方数',
  `op_usage_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '门诊抗菌药物使用率（%）',
  `ip_discharge_count` int NOT NULL DEFAULT '0' COMMENT '同期出院患者数',
  `ip_abx_patient_count` int NOT NULL DEFAULT '0' COMMENT '出院患者中使用抗菌药物的人数',
  `ip_usage_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '住院抗菌药物使用率（%）',
  `patient_days` int NOT NULL DEFAULT '0' COMMENT '收治患者人天数',
  `ddds` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '抗菌药物累计 DDD 数',
  `aud` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '使用强度 AUD',
  `abx_treat_count` int NOT NULL DEFAULT '0' COMMENT '使用抗菌药物的住院患者数',
  `micro_submit_count` int NOT NULL DEFAULT '0' COMMENT '其中送检微生物标本的患者数',
  `micro_submit_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '微生物标本送检率（%）',
  `unmatched_order_count` int NOT NULL DEFAULT '0' COMMENT '未匹配到抗菌药物目录的住院药品医嘱数',
  `generate_by` varchar(64) DEFAULT NULL COMMENT '生成人',
  `generate_time` datetime DEFAULT NULL COMMENT '生成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_antibiotic_stats` (`stat_month`,`scope_type`,`dept_id`) COMMENT '一月一范围一条；重复生成覆盖同一行；本表无 del_flag，删除走物理删',
  KEY `idx_stat_month` (`stat_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抗菌药物使用监测指标';

-- ----------------------------
-- biz_antibiotic_incision_review  I 类切口预防用药点评
-- ----------------------------
CREATE TABLE `biz_antibiotic_incision_review` (
  `id` bigint NOT NULL COMMENT '主键',
  `review_no` varchar(32) NOT NULL COMMENT '点评编号',
  `operation_apply_id` bigint NOT NULL COMMENT '手术申请单ID',
  `apply_no` varchar(32) DEFAULT NULL COMMENT '手术申请单号',
  `admission_id` bigint DEFAULT NULL COMMENT '入院ID',
  `patient_id` bigint DEFAULT NULL COMMENT '患者ID',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '手术科室',
  `operation_name` varchar(200) DEFAULT NULL COMMENT '手术名称',
  `operation_code` varchar(32) DEFAULT NULL COMMENT '手术编码 ICD-9-CM-3',
  `operation_time` datetime DEFAULT NULL COMMENT '手术开始时间',
  `surgeon_name` varchar(64) DEFAULT NULL COMMENT '主刀医师',
  `incision_level` tinyint NOT NULL DEFAULT '1' COMMENT '切口等级',
  `drug_id` bigint DEFAULT NULL COMMENT '预防用药药品ID',
  `drug_name` varchar(200) DEFAULT NULL COMMENT '预防用药名称',
  `antibiotic_level` tinyint DEFAULT NULL COMMENT '预防用药分级（快照：1/2/3）',
  `indication_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否有预防用药指征（0-无 1-有）',
  `timing_type` tinyint DEFAULT NULL COMMENT '给药时机',
  `course_hours` int DEFAULT NULL COMMENT '预防用药总时长',
  `combo_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否联合用药（0-否 1-是）',
  `combo_reason` varchar(500) DEFAULT NULL COMMENT '联合用药理由',
  `consult_flag` tinyint NOT NULL DEFAULT '0' COMMENT '特殊使用级是否有抗菌药物管理工作组会诊同意（0-无 1-有）',
  `review_result` tinyint DEFAULT NULL COMMENT '点评结论（1-合理 2-不合理）',
  `problem_types` varchar(200) DEFAULT NULL COMMENT '问题码',
  `review_opinion` varchar(500) DEFAULT NULL COMMENT '点评意见',
  `reviewer_id` bigint DEFAULT NULL COMMENT '点评人员工ID',
  `reviewer_name` varchar(50) DEFAULT NULL COMMENT '点评人姓名',
  `review_time` datetime DEFAULT NULL COMMENT '点评时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_incision_apply` (`operation_apply_id`) COMMENT '一台手术一条点评；本表无 del_flag，删除走物理删',
  KEY `idx_review_result` (`review_result`),
  KEY `idx_review_time` (`review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='I 类切口预防用药点评';

-- ----------------------------
-- biz_single_disease_case  单病种质控病例
-- ----------------------------
CREATE TABLE `biz_single_disease_case` (
  `id` bigint NOT NULL COMMENT '主键',
  `case_no` varchar(32) NOT NULL COMMENT '病例编号',
  `disease_id` bigint NOT NULL COMMENT '病种ID',
  `admission_id` bigint NOT NULL COMMENT '住院ID',
  `patient_id` bigint DEFAULT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `main_diagnosis_code` varchar(32) DEFAULT NULL COMMENT '主要诊断编码',
  `main_diagnosis_name` varchar(200) DEFAULT NULL COMMENT '主要诊断名称',
  `inpatient_days` int DEFAULT NULL COMMENT '住院天数',
  `total_amount` decimal(12,2) DEFAULT NULL COMMENT '住院总费用',
  `is_surgery` tinyint NOT NULL DEFAULT '0' COMMENT '是否手术（0-否 1-是）',
  `death_flag` tinyint NOT NULL DEFAULT '0' COMMENT '死亡标志',
  `curative_effect` tinyint DEFAULT NULL COMMENT '疗效判定（1-治愈 2-好转 3-未愈 4-死亡 5-其他）',
  `enroll_way` tinyint NOT NULL DEFAULT '1' COMMENT '纳入方式（1-自动扫描 2-手工纳入）',
  `qc_status` tinyint NOT NULL DEFAULT '0' COMMENT '质控状态（0-待质控 1-通过 2-异常）',
  `qc_issues` varchar(500) DEFAULT NULL COMMENT '质控异常项',
  `report_status` tinyint NOT NULL DEFAULT '0' COMMENT '上报状态（0-未上报 1-已上报）',
  `report_time` datetime DEFAULT NULL COMMENT '上报时间',
  `create_by` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_case_no` (`case_no`),
  UNIQUE KEY `uk_disease_admission` (`disease_id`,`admission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单病种质控病例';

-- ----------------------------
-- biz_vte_stats  VTE 防控月度指标
-- ----------------------------
CREATE TABLE `biz_vte_stats` (
  `id` bigint NOT NULL COMMENT '主键',
  `stat_month` char(7) NOT NULL COMMENT '统计月份',
  `scope_type` tinyint NOT NULL DEFAULT '1' COMMENT '统计范围（1-全院 2-科室）',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `discharge_count` int NOT NULL DEFAULT '0' COMMENT '同期出院患者数',
  `assessed_count` int NOT NULL DEFAULT '0' COMMENT '其中做过 Caprini 评估的患者数',
  `assess_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT 'VTE 风险评估率（%）',
  `high_risk_count` int NOT NULL DEFAULT '0' COMMENT '其中最新评估为中高危',
  `high_risk_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '中高危占比（%）',
  `prevent_done_count` int NOT NULL DEFAULT '0' COMMENT '中高危中至少落实一条措施的患者数',
  `prevent_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '预防措施落实率（%）',
  `vte_event_count` int NOT NULL DEFAULT '0' COMMENT '院内新发 VTE 患者数',
  `vte_incidence_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '院内 VTE 发生率（%）',
  `bleed_count` int NOT NULL DEFAULT '0' COMMENT '预防相关出血患者数',
  `generate_by` varchar(64) DEFAULT NULL COMMENT '生成人',
  `generate_time` datetime DEFAULT NULL COMMENT '生成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vte_stats` (`stat_month`,`scope_type`,`dept_id`) COMMENT '一月一范围一条；重复生成覆盖同一行；本表无 del_flag，删除走物理删',
  KEY `idx_vte_stat_month` (`stat_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='VTE 防控月度指标';

-- ----------------------------
-- biz_vte_prevent  VTE 预防措施记录
-- ----------------------------
CREATE TABLE `biz_vte_prevent` (
  `id` bigint NOT NULL COMMENT '主键',
  `prevent_no` varchar(32) NOT NULL COMMENT '措施记录编号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint DEFAULT NULL COMMENT '患者ID',
  `patient_no` varchar(32) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `ward_id` bigint DEFAULT NULL COMMENT '病区ID',
  `ward_name` varchar(100) DEFAULT NULL COMMENT '病区名称',
  `bed_no` varchar(20) DEFAULT NULL COMMENT '床号',
  `assessment_id` bigint DEFAULT NULL COMMENT '来源评估单ID',
  `caprini_score` int DEFAULT NULL COMMENT 'Caprini 总分',
  `risk_level` tinyint DEFAULT NULL COMMENT '风险等级（1-低 2-中 3-高 4-极高）',
  `measure_code` varchar(32) NOT NULL COMMENT '措施码',
  `measure_type` tinyint NOT NULL COMMENT '措施类别（1-基础预防 2-物理预防 3-药物预防）',
  `measure_name` varchar(200) DEFAULT NULL COMMENT '措施名称',
  `plan_date` date DEFAULT NULL COMMENT '计划执行日期',
  `execute_status` tinyint NOT NULL DEFAULT '0' COMMENT '落实状态（0-待落实 1-已落实 2-禁忌未用 3-患者拒绝）',
  `execute_time` datetime DEFAULT NULL COMMENT '落实时间',
  `executor_id` bigint DEFAULT NULL COMMENT '执行人（员工ID）',
  `executor_name` varchar(50) DEFAULT NULL COMMENT '执行人姓名',
  `reason` varchar(500) DEFAULT NULL COMMENT '未落实原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vte_prevent` (`admission_id`,`measure_code`),
  KEY `idx_prevent_admission` (`admission_id`),
  KEY `idx_prevent_status` (`execute_status`),
  KEY `idx_prevent_exec_time` (`execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='VTE 预防措施记录';

-- ----------------------------
-- biz_vte_event  VTE 事件登记
-- ----------------------------
CREATE TABLE `biz_vte_event` (
  `id` bigint NOT NULL COMMENT '主键',
  `event_no` varchar(32) NOT NULL COMMENT '事件编号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint DEFAULT NULL COMMENT '患者ID',
  `patient_no` varchar(32) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `ward_id` bigint DEFAULT NULL COMMENT '病区ID',
  `ward_name` varchar(100) DEFAULT NULL COMMENT '病区名称',
  `event_type` tinyint NOT NULL COMMENT '事件类型（1-深静脉血栓DVT 2-肺栓塞PE 3-预防相关出血）',
  `onset_type` tinyint NOT NULL DEFAULT '1' COMMENT '发生时机（1-院内发生 2-入院时已存在）',
  `diagnose_date` date NOT NULL COMMENT '确诊日期',
  `diagnosis_basis` tinyint DEFAULT NULL COMMENT '诊断依据（1-超声 2-CT肺动脉造影 3-静脉造影 4-临床诊断 5-其他）',
  `thrombus_site` varchar(100) DEFAULT NULL COMMENT '血栓部位',
  `outcome` tinyint DEFAULT NULL COMMENT '转归（1-好转 2-未愈 3-死亡 4-未知）',
  `drug_prevent_flag` tinyint NOT NULL DEFAULT '0' COMMENT '事件发生时是否正在药物预防（0-否 1-是）',
  `reporter_id` bigint DEFAULT NULL COMMENT '登记人（员工ID）',
  `reporter_name` varchar(50) DEFAULT NULL COMMENT '登记人姓名',
  `report_time` datetime DEFAULT NULL COMMENT '登记时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_vte_event_admission` (`admission_id`),
  KEY `idx_vte_event_date` (`diagnose_date`),
  KEY `idx_vte_event_type` (`event_type`,`onset_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='VTE 事件登记';

-- ----------------------------
-- biz_nutrition_screen  营养风险筛查记录
-- ----------------------------
CREATE TABLE `biz_nutrition_screen` (
  `id` bigint NOT NULL COMMENT '主键',
  `screen_no` varchar(32) NOT NULL COMMENT '筛查编号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint DEFAULT NULL COMMENT '患者ID',
  `patient_no` varchar(32) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `ward_id` bigint DEFAULT NULL COMMENT '病区ID',
  `ward_name` varchar(100) DEFAULT NULL COMMENT '病区名称',
  `bed_no` varchar(20) DEFAULT NULL COMMENT '床号',
  `screen_type` tinyint NOT NULL DEFAULT '1' COMMENT '量表（1-NRS2002 2-PG-SGA 3-MNA）',
  `impair_score` tinyint DEFAULT NULL COMMENT 'NRS2002 营养状态受损评分 0~3（1-体重下降 2-GI手术 3-骨髓移植等）',
  `severity_score` tinyint DEFAULT NULL COMMENT 'NRS2002 疾病严重程度评分 0~3（1-髋骨骨折 2-腹部大手术 3-颅脑损伤）',
  `age_score` tinyint DEFAULT NULL COMMENT 'NRS2002 年龄评分',
  `height_cm` decimal(5,1) DEFAULT NULL COMMENT '身高 cm',
  `weight_kg` decimal(6,2) DEFAULT NULL COMMENT '体重 kg',
  `bmi` decimal(5,2) DEFAULT NULL COMMENT 'BMI',
  `weight_loss_percent` decimal(5,1) DEFAULT NULL COMMENT '近 3 个月体重下降百分比（%）',
  `total_score` int NOT NULL DEFAULT '0' COMMENT '量表总分',
  `risk_flag` tinyint NOT NULL DEFAULT '0' COMMENT '营养风险',
  `screen_source` tinyint NOT NULL DEFAULT '1' COMMENT '筛查时机（1-入院48小时内 2-病情变化复筛 3-术后复筛 4-定期复筛）',
  `next_screen_date` date DEFAULT NULL COMMENT '下次筛查日期',
  `items_json` text COMMENT '分项明细 JSON',
  `screen_time` datetime NOT NULL COMMENT '筛查时间',
  `screener_id` bigint DEFAULT NULL COMMENT '筛查人（员工ID）',
  `screener_name` varchar(50) DEFAULT NULL COMMENT '筛查人姓名',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nutrition_screen_no` (`screen_no`),
  KEY `idx_ns_admission` (`admission_id`),
  KEY `idx_ns_type_risk` (`screen_type`,`risk_flag`),
  KEY `idx_ns_next` (`next_screen_date`),
  KEY `idx_ns_time` (`screen_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='营养风险筛查记录';

-- ----------------------------
-- biz_diet_plan  膳食方案
-- ----------------------------
CREATE TABLE `biz_diet_plan` (
  `id` bigint NOT NULL COMMENT '主键',
  `diet_no` varchar(32) NOT NULL COMMENT '膳食方案编号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint DEFAULT NULL COMMENT '患者ID',
  `patient_no` varchar(32) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `ward_id` bigint DEFAULT NULL COMMENT '病区ID',
  `ward_name` varchar(100) DEFAULT NULL COMMENT '病区名称',
  `bed_no` varchar(20) DEFAULT NULL COMMENT '床号',
  `order_id` bigint DEFAULT NULL COMMENT '来源医嘱ID',
  `order_no` varchar(32) DEFAULT NULL COMMENT '来源医嘱号',
  `source` tinyint NOT NULL DEFAULT '1' COMMENT '来源（1-医嘱校对派生 2-营养师手工登记）',
  `diet_code` varchar(32) NOT NULL COMMENT '饮食类型码',
  `diet_category` tinyint NOT NULL COMMENT '饮食类别（1-基本饮食 2-治疗饮食 3-诊断试验饮食 4-营养支持）',
  `diet_name` varchar(100) NOT NULL COMMENT '饮食名称',
  `route` tinyint NOT NULL DEFAULT '1' COMMENT '给食途径（1-口服 2-管饲）',
  `feed_way` varchar(100) DEFAULT NULL COMMENT '管饲/输注方式说明',
  `calorie_target` int DEFAULT NULL COMMENT '每日热量目标 kcal',
  `protein_target` int DEFAULT NULL COMMENT '每日蛋白目标 g',
  `fluid_target` int DEFAULT NULL COMMENT '每日液体量 ml',
  `meal_types` varchar(32) DEFAULT NULL COMMENT '供应餐次',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `stop_time` datetime DEFAULT NULL COMMENT '停止时间',
  `plan_status` tinyint NOT NULL DEFAULT '1' COMMENT '方案状态（1-执行中 2-已停止 3-已作废）',
  `confirm_status` tinyint NOT NULL DEFAULT '0' COMMENT '营养科接收状态（0-待接收 1-已接收 2-已退回）',
  `confirm_time` datetime DEFAULT NULL COMMENT '接收/退回时间',
  `confirmer_id` bigint DEFAULT NULL COMMENT '接收人',
  `confirmer_name` varchar(50) DEFAULT NULL COMMENT '接收人姓名',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '退回原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_diet_plan_no` (`diet_no`),
  UNIQUE KEY `uk_diet_plan_order` (`order_id`) COMMENT '一条医嘱只派生一个方案（重复校对/重放不会多出第二条）；键**不含** del_flag → 删除必须物理删',
  KEY `idx_dp_admission` (`admission_id`),
  KEY `idx_dp_status` (`plan_status`,`confirm_status`),
  KEY `idx_dp_diet_code` (`diet_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='膳食方案';

-- ----------------------------
-- biz_meal_order  住院订餐配送
-- ----------------------------
CREATE TABLE `biz_meal_order` (
  `id` bigint NOT NULL COMMENT '主键',
  `meal_no` varchar(32) NOT NULL COMMENT '订餐单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint DEFAULT NULL COMMENT '患者ID',
  `patient_no` varchar(32) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `ward_id` bigint DEFAULT NULL COMMENT '病区ID',
  `ward_name` varchar(100) DEFAULT NULL COMMENT '病区名称',
  `bed_no` varchar(20) DEFAULT NULL COMMENT '床号',
  `diet_plan_id` bigint DEFAULT NULL COMMENT '来源膳食方案ID',
  `diet_code` varchar(32) DEFAULT NULL COMMENT '饮食类型码',
  `diet_name` varchar(100) DEFAULT NULL COMMENT '饮食名称',
  `meal_date` date NOT NULL COMMENT '就餐日期',
  `meal_type` tinyint NOT NULL COMMENT '餐次（1-早餐 2-午餐 3-晚餐 4-加餐）',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '份数',
  `dish_content` varchar(200) DEFAULT NULL COMMENT '配餐内容/食谱',
  `deliver_status` tinyint NOT NULL DEFAULT '0' COMMENT '配餐状态（0-待配餐 1-已配餐 2-已配送 3-已签收 4-已取消）',
  `prepare_time` datetime DEFAULT NULL COMMENT '配餐完成时间',
  `deliver_time` datetime DEFAULT NULL COMMENT '配送出仓时间',
  `deliver_by_id` bigint DEFAULT NULL COMMENT '配送人（员工ID）',
  `deliver_by_name` varchar(50) DEFAULT NULL COMMENT '配送人姓名',
  `sign_time` datetime DEFAULT NULL COMMENT '签收时间',
  `sign_by` varchar(50) DEFAULT NULL COMMENT '签收人（患者/家属/护士姓名）',
  `cancel_time` datetime DEFAULT NULL COMMENT '退订时间',
  `cancel_reason` varchar(500) DEFAULT NULL COMMENT '退订原因（停餐/出院/拒餐/转科等，必填）',
  `source` tinyint NOT NULL DEFAULT '1' COMMENT '来源（1-按膳食方案批量生成 2-手工加订）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meal_no` (`meal_no`),
  UNIQUE KEY `uk_meal_order` (`admission_id`,`meal_date`,`meal_type`) COMMENT '一人一天一餐一条；键**不含** del_flag → 批量重生成必须先物理清旧行',
  KEY `idx_mo_date_status` (`meal_date`,`deliver_status`),
  KEY `idx_mo_ward` (`ward_id`,`meal_date`),
  KEY `idx_mo_plan` (`diet_plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='住院订餐配送';

-- ----------------------------
-- biz_nutrition_stats  营养膳食月度指标
-- ----------------------------
CREATE TABLE `biz_nutrition_stats` (
  `id` bigint NOT NULL COMMENT '主键',
  `stat_month` char(7) NOT NULL COMMENT '统计月份',
  `scope_type` tinyint NOT NULL DEFAULT '1' COMMENT '统计范围（1-全院 2-科室）',
  `dept_id` bigint DEFAULT NULL COMMENT '科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `discharge_count` int NOT NULL DEFAULT '0' COMMENT '同期出院患者数',
  `screened_count` int NOT NULL DEFAULT '0' COMMENT '其中出院前做过 NRS2002 筛查的患者数',
  `screen_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '营养风险筛查率（%）',
  `risk_count` int NOT NULL DEFAULT '0' COMMENT '筛查阳性',
  `risk_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '筛查阳性率（%）',
  `diet_plan_count` int NOT NULL DEFAULT '0' COMMENT '膳食方案总数',
  `diet_confirm_count` int NOT NULL DEFAULT '0' COMMENT '其中营养科已接收',
  `diet_confirm_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '膳食医嘱执行率（%）',
  `consult_count` int NOT NULL DEFAULT '0' COMMENT '营养会诊单数',
  `consult_ontime_count` int NOT NULL DEFAULT '0' COMMENT '其中按时应答的条数',
  `consult_ontime_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '营养会诊及时应答率（%）',
  `meal_order_count` int NOT NULL DEFAULT '0' COMMENT '订餐明细数',
  `meal_signed_count` int NOT NULL DEFAULT '0' COMMENT '其中已签收的明细数',
  `meal_sign_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '订餐签收率（%）',
  `meal_cancel_count` int NOT NULL DEFAULT '0' COMMENT '退订明细数',
  `generate_by` varchar(64) DEFAULT NULL COMMENT '生成人',
  `generate_time` datetime DEFAULT NULL COMMENT '生成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nutrition_stats` (`stat_month`,`scope_type`,`dept_id`) COMMENT '一月一范围一条；重复生成覆盖同一行；本表无 del_flag，删除走物理删',
  KEY `idx_nts_month` (`stat_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='营养膳食月度指标';

-- ----------------------------
-- biz_checkup_record  体检登记
-- ----------------------------
CREATE TABLE `biz_checkup_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '体检编号',
  `patient_id` bigint NOT NULL COMMENT '体检人ID',
  `patient_name` varchar(50) NOT NULL COMMENT '体检人姓名',
  `gender` tinyint DEFAULT NULL COMMENT '性别（2-女 9-未知）',
  `age` int DEFAULT NULL COMMENT '年龄',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `person_type` tinyint NOT NULL DEFAULT '1' COMMENT '体检对象（1-个人 2-团体）',
  `package_id` bigint NOT NULL COMMENT '套餐ID',
  `package_name` varchar(100) NOT NULL COMMENT '套餐名称',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '应收金额',
  `checkup_date` date NOT NULL COMMENT '体检日期',
  `record_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-已登记 2-检查中 3-已完成 4-已出报告）',
  `conclusion` varchar(1000) DEFAULT NULL COMMENT '总检结论',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '总检医师',
  `report_time` datetime DEFAULT NULL COMMENT '报告时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_patient` (`patient_id`),
  KEY `idx_date` (`checkup_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体检登记';

-- ----------------------------
-- biz_checkup_result  体检结果明细
-- ----------------------------
CREATE TABLE `biz_checkup_result` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_id` bigint NOT NULL COMMENT '体检登记ID',
  `item_name` varchar(100) NOT NULL COMMENT '项目名称',
  `item_type` tinyint NOT NULL DEFAULT '1' COMMENT '项目类别（1-检验 2-检查 3-一般）',
  `ref_standard` varchar(200) DEFAULT NULL COMMENT '参考范围',
  `result_value` varchar(500) DEFAULT NULL COMMENT '结果值/所见',
  `abnormal_flag` tinyint NOT NULL DEFAULT '0' COMMENT '异常标志（0-正常 1-异常 2-待查）',
  `summary_text` varchar(500) DEFAULT NULL COMMENT '单项小结/建议',
  `checker_name` varchar(50) DEFAULT NULL COMMENT '检查/检验医师',
  `result_time` datetime DEFAULT NULL COMMENT '结果录入时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='体检结果明细';

-- ----------------------------
-- biz_dialysis_machine  透析机位台账
-- ----------------------------
CREATE TABLE `biz_dialysis_machine` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `machine_no` varchar(32) NOT NULL COMMENT '机位号',
  `room_name` varchar(64) DEFAULT NULL COMMENT '透析分区',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-可用 2-维修 3-停用）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_machine_no` (`machine_no`,`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='透析机位台账';

-- ----------------------------
-- biz_dialysis_patient  透析患者档案
-- ----------------------------
CREATE TABLE `biz_dialysis_patient` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dialysis_no` varchar(32) NOT NULL COMMENT '透析号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名',
  `phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `first_dialysis_date` date NOT NULL COMMENT '首次透析日期',
  `cause` varchar(255) DEFAULT NULL COMMENT '原发病/进入透析原因',
  `access_type` tinyint NOT NULL COMMENT '血管通路（1-自体内瘘 2-人工血管 3-中心静脉导管 4-动静脉外露）',
  `access_site` varchar(128) DEFAULT NULL COMMENT '通路部位',
  `dialysis_freq` tinyint NOT NULL DEFAULT '3' COMMENT '透析频次（1-每周1次 2-每周2次 3-每周3次 4-每周≥4次）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '档案状态（1-在透 2-暂停 3-退出）',
  `exit_reason` varchar(255) DEFAULT NULL COMMENT '暂停/退出原因（转腹透/移植/死亡/失访等，截到 200）',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dp_no` (`dialysis_no`),
  UNIQUE KEY `uk_dp_patient` (`patient_id`,`del_flag`),
  KEY `idx_dp_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='透析患者档案';

-- ----------------------------
-- biz_dialysis_prescription  透析处方
-- ----------------------------
CREATE TABLE `biz_dialysis_prescription` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `archive_id` bigint NOT NULL COMMENT '透析档案ID',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名',
  `dry_weight` decimal(6,2) NOT NULL COMMENT '干体重 kg',
  `duration_min` int NOT NULL DEFAULT '240' COMMENT '单次透析时长（分钟）',
  `blood_flow` int NOT NULL DEFAULT '220' COMMENT '血流量 mL/min',
  `dialyzer` tinyint NOT NULL DEFAULT '3' COMMENT '透析器（1-低通量纤维素膜 2-低通量合成膜 3-高通量合成膜）',
  `anticoagulant` tinyint NOT NULL DEFAULT '1' COMMENT '抗凝方式（1-普通肝素 2-低分子肝素 3-枸橼酸钠 4-无肝素）',
  `anticoag_dose` varchar(64) DEFAULT NULL COMMENT '抗凝剂量描述',
  `target_ultra_ml` decimal(8,1) DEFAULT NULL COMMENT '目标超滤量 ml',
  `start_date` date NOT NULL COMMENT '处方生效日期',
  `end_date` date DEFAULT NULL COMMENT '处方停用日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-有效 2-已停用）',
  `doctor_id` bigint DEFAULT NULL COMMENT '开立医生（员工ID）',
  `doctor_name` varchar(64) DEFAULT NULL COMMENT '开立医生姓名',
  `stop_reason` varchar(255) DEFAULT NULL COMMENT '停用原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_pre_archive` (`archive_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='透析处方';

-- ----------------------------
-- biz_dialysis_session  透析单
-- ----------------------------
CREATE TABLE `biz_dialysis_session` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `session_no` varchar(32) NOT NULL COMMENT '透析单号',
  `dialysis_date` date NOT NULL COMMENT '透析日期',
  `time_slot` tinyint NOT NULL DEFAULT '1' COMMENT '时段（1-上午 2-下午 3-夜间）',
  `machine_id` bigint NOT NULL COMMENT '机位ID',
  `machine_no` varchar(32) DEFAULT NULL COMMENT '机位号',
  `archive_id` bigint NOT NULL COMMENT '透析档案ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名',
  `prescription_id` bigint NOT NULL COMMENT '使用的透析处方ID',
  `dry_weight` decimal(6,2) DEFAULT NULL COMMENT '干体重 kg',
  `duration_min` int DEFAULT NULL COMMENT '处方透析时长分钟',
  `blood_flow` int DEFAULT NULL COMMENT '处方血流量',
  `dialyzer` tinyint DEFAULT NULL COMMENT '透析器',
  `anticoagulant` tinyint DEFAULT NULL COMMENT '抗凝方式',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-已排班 2-透析中 3-已完成 4-已取消）',
  `before_weight` decimal(6,2) DEFAULT NULL COMMENT '透前体重 kg',
  `access_check` varchar(255) DEFAULT NULL COMMENT '通路评估',
  `on_time` datetime DEFAULT NULL COMMENT '上机时间',
  `on_by` varchar(64) DEFAULT NULL COMMENT '上机人',
  `after_weight` decimal(6,2) DEFAULT NULL COMMENT '透后体重 kg',
  `actual_duration_min` int DEFAULT NULL COMMENT '实际透析时长分钟',
  `ultra_ml` decimal(8,1) DEFAULT NULL COMMENT '实际超滤量 ml =（透前-透后）',
  `off_time` datetime DEFAULT NULL COMMENT '下机时间',
  `off_by` varchar(64) DEFAULT NULL COMMENT '下机人',
  `adverse_type` tinyint DEFAULT NULL COMMENT '不良反应类型（，空=无）',
  `adverse_desc` varchar(255) DEFAULT NULL COMMENT '不良反应处置描述',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `slot_key` varchar(64) GENERATED ALWAYS AS (if(((`status` = 4) or (`del_flag` = 1)),NULL,concat(`dialysis_date`,_utf8mb4'-',`time_slot`,_utf8mb4'-',`machine_id`))) STORED COMMENT '机位时段占用键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_no` (`session_no`),
  UNIQUE KEY `uk_session_slot` (`slot_key`),
  KEY `idx_session_archive` (`archive_id`),
  KEY `idx_session_date` (`dialysis_date`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='透析单';

-- ----------------------------
-- biz_icu_stay  ICU 入出科登记
-- ----------------------------
CREATE TABLE `biz_icu_stay` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `stay_no` varchar(32) NOT NULL COMMENT '入科单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(64) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(128) DEFAULT NULL COMMENT '患者姓名',
  `from_dept_id` bigint DEFAULT NULL COMMENT '入科来源科室ID',
  `from_dept_name` varchar(128) DEFAULT NULL COMMENT '入科来源科室名称',
  `ward_id` bigint NOT NULL COMMENT 'ICU 病区ID',
  `ward_name` varchar(128) DEFAULT NULL COMMENT 'ICU 病区名称',
  `bed_id` bigint NOT NULL COMMENT 'ICU 床位ID',
  `bed_no` varchar(16) DEFAULT NULL COMMENT 'ICU 床位号',
  `care_level` tinyint NOT NULL DEFAULT '1' COMMENT '监护等级（1-特级 2-I级 3-II级）',
  `in_time` datetime NOT NULL COMMENT '入科时间',
  `in_diag` varchar(255) DEFAULT NULL COMMENT '入科诊断/原因',
  `in_gcs` int DEFAULT NULL COMMENT '入科 GCS 评分',
  `in_by` varchar(64) DEFAULT NULL COMMENT '入科登记人',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-在科 2-已出科）',
  `out_time` datetime DEFAULT NULL COMMENT '出科时间',
  `out_dest` tinyint DEFAULT NULL COMMENT '转出去向（1-普通病房 2-专科病房 3-手术室 4-转院 5-死亡 6-自动离院）',
  `out_reason` varchar(255) DEFAULT NULL COMMENT '出科情况/转归说明',
  `out_gcs` int DEFAULT NULL COMMENT '出科 GCS 评分',
  `out_by` varchar(64) DEFAULT NULL COMMENT '出科登记人',
  `monitor_count` int NOT NULL DEFAULT '0' COMMENT '监护记录条数',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_icu_stay_no` (`stay_no`),
  KEY `idx_icu_admission` (`admission_id`),
  KEY `idx_icu_bed` (`bed_id`,`status`),
  KEY `idx_icu_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ICU 入出科登记';

-- ----------------------------
-- biz_icu_monitor  ICU 监护记录单
-- ----------------------------
CREATE TABLE `biz_icu_monitor` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `stay_id` bigint NOT NULL COMMENT '入科记录ID',
  `record_time` datetime NOT NULL COMMENT '记录时刻',
  `temperature` decimal(4,1) DEFAULT NULL COMMENT '体温 ℃',
  `pulse` int DEFAULT NULL COMMENT '脉搏 次/分',
  `respiratory` int DEFAULT NULL COMMENT '呼吸 次/分',
  `sbp` int DEFAULT NULL COMMENT '收缩压 mmHg',
  `dbp` int DEFAULT NULL COMMENT '舒张压 mmHg',
  `spo2` int DEFAULT NULL COMMENT '血氧饱和度（%）',
  `gcs_eye` tinyint DEFAULT NULL COMMENT 'GCS 睁眼 1~4',
  `gcs_verbal` tinyint DEFAULT NULL COMMENT 'GCS 语言 1~5',
  `gcs_motor` tinyint DEFAULT NULL COMMENT 'GCS 运动 1~6',
  `gcs_total` int DEFAULT NULL COMMENT 'GCS 总分',
  `pupil` varchar(128) DEFAULT NULL COMMENT '瞳孔',
  `cvp` decimal(5,1) DEFAULT NULL COMMENT '中心静脉压 cmH2O',
  `vent_mode` tinyint DEFAULT NULL COMMENT '呼吸支持（1-鼻导管 2-无创 3-有创 4-脱机）',
  `fio2` int DEFAULT NULL COMMENT '吸氧浓度（%）',
  `peep` decimal(4,1) DEFAULT NULL COMMENT '呼气末正压（cmH2O）',
  `intake_ml` decimal(8,1) DEFAULT NULL COMMENT '入量 ml',
  `output_ml` decimal(8,1) DEFAULT NULL COMMENT '出量 ml',
  `fluid_balance` decimal(8,1) DEFAULT NULL COMMENT '液体平衡 ml = 入量-出量',
  `urine_ml` int DEFAULT NULL COMMENT '尿量 ml',
  `has_airway` tinyint NOT NULL DEFAULT '0' COMMENT '人工气道/气管插管 0-无 1-有（0-无 1-有）',
  `has_cvc` tinyint NOT NULL DEFAULT '0' COMMENT '中心静脉导管 0-无 1-有（0-无 1-有）',
  `has_arterial` tinyint NOT NULL DEFAULT '0' COMMENT '动脉置管 0-无 1-有（0-无 1-有）',
  `has_catheter` tinyint NOT NULL DEFAULT '0' COMMENT '导尿管 0-无 1-有（0-无 1-有）',
  `has_drain` tinyint NOT NULL DEFAULT '0' COMMENT '引流管 0-无 1-有（0-无 1-有）',
  `condition_desc` varchar(500) DEFAULT NULL COMMENT '病情观察',
  `handling` varchar(500) DEFAULT NULL COMMENT '处置/干预',
  `recorder_id` bigint DEFAULT NULL COMMENT '记录人（员工ID）',
  `recorder_name` varchar(64) DEFAULT NULL COMMENT '记录人姓名',
  `record_date` date DEFAULT NULL COMMENT '记录日期',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_icu_monitor_time` (`stay_id`,`record_time`,`del_flag`) COMMENT '同一入科记录同一时刻只能有一条监护记录',
  KEY `idx_icu_monitor_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ICU 监护记录单';

-- ----------------------------
-- biz_treatment_apply  治疗申请单
-- ----------------------------
CREATE TABLE `biz_treatment_apply` (
  `apply_id` bigint NOT NULL COMMENT '治疗申请ID',
  `apply_no` varchar(32) NOT NULL COMMENT '治疗申请单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint DEFAULT NULL COMMENT '就诊次ID',
  `regist_id` bigint DEFAULT NULL COMMENT '挂号ID',
  `doctor_id` bigint NOT NULL COMMENT '开单医生ID',
  `treatment_item_id` bigint NOT NULL COMMENT '治疗项目ID',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `execute_time` datetime DEFAULT NULL COMMENT '执行时间',
  `apply_status` tinyint NOT NULL DEFAULT '0' COMMENT '申请状态（0-待执行 1-已执行 2-已取消）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `patient_no` varchar(32) DEFAULT NULL COMMENT '患者编号',
  `patient_name` varchar(50) DEFAULT NULL COMMENT '患者姓名',
  `regist_no` varchar(32) DEFAULT NULL COMMENT '挂号单号',
  `doctor_name` varchar(50) DEFAULT NULL COMMENT '开单医生姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '开单科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '开单科室名称',
  `exec_dept_id` bigint DEFAULT NULL COMMENT '建议执行科室ID',
  `exec_dept_name` varchar(100) DEFAULT NULL COMMENT '建议执行科室名称',
  `item_code` varchar(32) DEFAULT NULL COMMENT '治疗项目编码',
  `item_name` varchar(200) DEFAULT NULL COMMENT '治疗项目名称',
  `item_type` tinyint DEFAULT NULL COMMENT '治疗项目类别（1-注射 2-输液 3-换药 4-拆线 5-其他）',
  `price` decimal(10,2) DEFAULT NULL COMMENT '项目单价',
  `total_times` int NOT NULL DEFAULT '1' COMMENT '疗程总次数',
  `done_times` int NOT NULL DEFAULT '0' COMMENT '已完成次数',
  `start_date` date DEFAULT NULL COMMENT '疗程计划开始日期',
  `interval_days` int NOT NULL DEFAULT '1' COMMENT '相邻两次执行的间隔天数',
  PRIMARY KEY (`apply_id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_visit_id` (`visit_id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_apply_status` (`apply_status`),
  KEY `idx_treat_apply_regist` (`regist_id`),
  KEY `idx_treat_apply_status` (`apply_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗申请单';

-- ----------------------------
-- biz_treatment_record  治疗执行记录
-- ----------------------------
CREATE TABLE `biz_treatment_record` (
  `record_id` bigint NOT NULL COMMENT '治疗记录ID',
  `record_no` varchar(32) NOT NULL COMMENT '治疗记录编号',
  `apply_id` bigint NOT NULL COMMENT '治疗申请ID',
  `treatment_item_id` bigint NOT NULL COMMENT '治疗项目ID',
  `execute_doctor_id` bigint DEFAULT NULL COMMENT '执行医生ID',
  `nurse_id` bigint DEFAULT NULL COMMENT '执行护士ID',
  `execute_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  `result` varchar(1000) DEFAULT NULL COMMENT '治疗结果描述',
  `record_status` tinyint NOT NULL DEFAULT '1' COMMENT '记录状态（0-异常 1-正常）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `exec_seq` int NOT NULL DEFAULT '1' COMMENT '第几次执行',
  `plan_date` date DEFAULT NULL COMMENT '计划执行日期',
  `exec_status` tinyint NOT NULL DEFAULT '0' COMMENT '执行状态（0-待执行 1-已执行 2-已取消）',
  `executor_name` varchar(50) DEFAULT NULL COMMENT '执行人姓名',
  `charge_status` tinyint NOT NULL DEFAULT '0' COMMENT '计费状态（0-未计费 1-已计费 2-计费失败 3-无需计费）',
  `charge_time` datetime DEFAULT NULL COMMENT '计费时间',
  `fee_no` varchar(32) DEFAULT NULL COMMENT '记账单号',
  `fee_record_id` bigint DEFAULT NULL COMMENT '记账行ID',
  `charge_amount` decimal(10,2) DEFAULT NULL COMMENT '本次计费金额',
  `charge_fail_reason` varchar(500) DEFAULT NULL COMMENT '未计费/失败原因',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  UNIQUE KEY `uk_treat_exec` (`apply_id`,`exec_seq`),
  KEY `idx_apply_id` (`apply_id`),
  KEY `idx_execute_time` (`execute_time`),
  KEY `idx_treat_exec_plan` (`plan_date`,`exec_status`),
  KEY `idx_treat_exec_charge` (`charge_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='治疗执行记录';
