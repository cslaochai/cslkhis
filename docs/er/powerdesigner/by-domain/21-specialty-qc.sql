-- ============================================================
-- 领域 21 专项质控与专科管理（路径·抗菌药物·VTE·营养·体检·透析·监护）（本域 27 表 + 上游参照 17 表 / 81 条关系）
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


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
ALTER TABLE `biz_antibiotic_alias` ADD CONSTRAINT `fk_biz_antibiotic_alias_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_antibiotic_auth` ADD CONSTRAINT `fk_biz_antibiotic_auth_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_antibiotic_auth` ADD CONSTRAINT `fk_biz_antibiotic_auth_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_operation_apply_id` FOREIGN KEY (`operation_apply_id`) REFERENCES `biz_operation_apply` (`id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_antibiotic_incision_review` ADD CONSTRAINT `fk_biz_antibiotic_incision_review_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_antibiotic_stats` ADD CONSTRAINT `fk_biz_antibiotic_stats_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_checkup_record` ADD CONSTRAINT `fk_biz_checkup_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_checkup_record` ADD CONSTRAINT `fk_biz_checkup_record_package_id` FOREIGN KEY (`package_id`) REFERENCES `sys_checkup_package` (`id`);
ALTER TABLE `biz_checkup_result` ADD CONSTRAINT `fk_biz_checkup_result_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_checkup_record` (`id`);
ALTER TABLE `biz_clinical_rule_check` ADD CONSTRAINT `fk_biz_clinical_rule_check_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_clinical_rule_check` ADD CONSTRAINT `fk_biz_clinical_rule_check_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
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
ALTER TABLE `biz_icu_monitor` ADD CONSTRAINT `fk_biz_icu_monitor_stay_id` FOREIGN KEY (`stay_id`) REFERENCES `biz_icu_stay` (`id`);
ALTER TABLE `biz_icu_monitor` ADD CONSTRAINT `fk_biz_icu_monitor_recorder_id` FOREIGN KEY (`recorder_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_from_dept_id` FOREIGN KEY (`from_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_icu_stay` ADD CONSTRAINT `fk_biz_icu_stay_bed_id` FOREIGN KEY (`bed_id`) REFERENCES `sys_bed` (`bed_id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_diet_plan_id` FOREIGN KEY (`diet_plan_id`) REFERENCES `biz_diet_plan` (`id`);
ALTER TABLE `biz_meal_order` ADD CONSTRAINT `fk_biz_meal_order_deliver_by_id` FOREIGN KEY (`deliver_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `biz_nutrition_screen` ADD CONSTRAINT `fk_biz_nutrition_screen_screener_id` FOREIGN KEY (`screener_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_nutrition_stats` ADD CONSTRAINT `fk_biz_nutrition_stats_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_pathway` ADD CONSTRAINT `fk_biz_pathway_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_pathway_enroll` ADD CONSTRAINT `fk_biz_pathway_enroll_pathway_id` FOREIGN KEY (`pathway_id`) REFERENCES `biz_pathway` (`id`);
ALTER TABLE `biz_pathway_enroll` ADD CONSTRAINT `fk_biz_pathway_enroll_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_pathway_enroll` ADD CONSTRAINT `fk_biz_pathway_enroll_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_pathway_enroll` ADD CONSTRAINT `fk_biz_pathway_enroll_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_pathway_step` ADD CONSTRAINT `fk_biz_pathway_step_pathway_id` FOREIGN KEY (`pathway_id`) REFERENCES `biz_pathway` (`id`);
ALTER TABLE `biz_pathway_variance` ADD CONSTRAINT `fk_biz_pathway_variance_enroll_id` FOREIGN KEY (`enroll_id`) REFERENCES `biz_pathway_enroll` (`id`);
ALTER TABLE `biz_pathway_variance` ADD CONSTRAINT `fk_biz_pathway_variance_recorder_id` FOREIGN KEY (`recorder_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_single_disease_case` ADD CONSTRAINT `fk_biz_single_disease_case_disease_id` FOREIGN KEY (`disease_id`) REFERENCES `sys_single_disease` (`id`);
ALTER TABLE `biz_single_disease_case` ADD CONSTRAINT `fk_biz_single_disease_case_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_single_disease_case` ADD CONSTRAINT `fk_biz_single_disease_case_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
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
