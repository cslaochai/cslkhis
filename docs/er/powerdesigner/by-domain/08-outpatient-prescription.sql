-- ============================================================
-- 领域 08 门诊病历与处方（本域 18 表 + 上游参照 13 表 / 58 条关系）
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


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
  `total_amount` decimal(10,2) COMMENT '处方总金额（快照）',
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
  `prescription_no` varchar(32) NOT NULL COMMENT '处方号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `dept_name` varchar(100) NOT NULL COMMENT '开方科室（快照）',
  `doctor_id` bigint NOT NULL COMMENT '开方医生ID（快照）',
  `doctor_name` varchar(50) NOT NULL COMMENT '开方医生姓名（快照）',
  `visit_date` date NOT NULL COMMENT '就诊日期',
  `diagnosis` varchar(500) COMMENT '诊断（快照）',
  `drug_count` int DEFAULT 0 COMMENT '药品数量（快照）',
  `total_amount` decimal(10,2) DEFAULT 0.00 COMMENT '处方金额（快照）',
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

-- biz_rx_doctor_talk  医师约谈记录
CREATE TABLE `biz_rx_doctor_talk` (
  `id` bigint NOT NULL COMMENT '主键',
  `talk_no` varchar(32) NOT NULL COMMENT '约谈编号',
  `doctor_id` bigint COMMENT '被约谈医师ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '被约谈医师姓名',
  `dept_name` varchar(100) COMMENT '医师所在科室（快照）',
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

-- biz_skin_test  门诊皮试记录
CREATE TABLE `biz_skin_test` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `test_no` varchar(32) NOT NULL COMMENT '皮试单号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `drug_name` varchar(200) NOT NULL COMMENT '皮试药物名称',
  `treatment_record_id` bigint COMMENT '来源治疗记录ID',
  `test_time` datetime NOT NULL COMMENT '皮试时间',
  `result` tinyint NOT NULL DEFAULT 0 COMMENT '判读结果（0-待判读 1-阴性 2-阳性）',
  `result_time` datetime COMMENT '判读时间',
  `nurse_id` bigint COMMENT '执行护士ID',
  `nurse_name` varchar(50) COMMENT '执行护士姓名（快照）',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_test_no` (`test_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊皮试记录';

-- biz_tcm_decoct  中药代煎单
CREATE TABLE `biz_tcm_decoct` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `decoct_no` varchar(32) NOT NULL COMMENT '代煎单号',
  `prescription_id` bigint NOT NULL COMMENT '处方ID',
  `prescription_no` varchar(32) NOT NULL COMMENT '处方号',
  `patient_id` bigint NOT NULL COMMENT '患者ID（快照）',
  `patient_no` varchar(32) COMMENT '患者号（快照）',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `dept_name` varchar(100) COMMENT '开方科室',
  `doctor_name` varchar(50) COMMENT '开方医师（快照）',
  `dose_count` int NOT NULL DEFAULT 1 COMMENT '剂数',
  `herb_count` int NOT NULL DEFAULT 0 COMMENT '味数',
  `total_grams` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '全方总克数',
  `method_summary` varchar(500) COMMENT '煎法脚注汇总',
  `decoct_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待煎 2-已煎 3-已取 9-已作废）',
  `pharmacy_id` bigint COMMENT '代煎药房ID',
  `pharmacy_name` varchar(100) COMMENT '代煎药房名称（快照）',
  `operator_id` bigint COMMENT '最近一次状态操作人',
  `operator_name` varchar(64) COMMENT '最近一次状态操作人姓名（快照）',
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

-- biz_outp_infusion  门诊输液单
CREATE TABLE `biz_outp_infusion` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `infusion_no` varchar(32) NOT NULL COMMENT '输液单号',
  `treatment_record_id` bigint COMMENT '来源治疗记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别（快照）',
  `age` int COMMENT '年龄（快照）',
  `drug_summary` varchar(500) COMMENT '输注内容摘要',
  `seat_id` bigint COMMENT '座位ID',
  `seat_no` varchar(32) COMMENT '座位号（快照）',
  `skin_test_id` bigint COMMENT '皮试记录ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-待皮试 2-待输注 3-输液中 4-已完成 5-已取消）',
  `start_time` datetime COMMENT '开始输注时间',
  `drip_rate` int COMMENT '起始滴速（滴/分）',
  `end_time` datetime COMMENT '结束时间',
  `adverse_flag` tinyint NOT NULL DEFAULT 0 COMMENT '不良反应（0-无 1-有）',
  `adverse_desc` varchar(500) COMMENT '不良反应描述',
  `nurse_id` bigint COMMENT '责任护士ID',
  `nurse_name` varchar(50) COMMENT '责任护士姓名（快照）',
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
  `nurse_name` varchar(50) COMMENT '巡视护士姓名（快照）',
  `create_by` varchar(64),
  `create_time` datetime,
  `update_by` varchar(64),
  `update_time` datetime,
  `del_flag` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(500),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门诊输液巡视记录';

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
  `bill_no` varchar(32) COMMENT '账单号（快照）',
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

-- biz_emr_signature  电子签名证据
CREATE TABLE `biz_emr_signature` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `sign_no` varchar(32) NOT NULL COMMENT '签名流水号',
  `biz_type` tinyint NOT NULL COMMENT '签名对象类型',
  `biz_id` bigint NOT NULL COMMENT '签名对象ID',
  `biz_no` varchar(64) COMMENT '对象单号（快照）',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `dept_id` bigint COMMENT '对象所属科室ID（快照）',
  `dept_name` varchar(64) COMMENT '对象所属科室名称（快照）',
  `sign_scene` tinyint NOT NULL COMMENT '签名场景',
  `chain_no` int NOT NULL DEFAULT 1 COMMENT '同对象第几次签名',
  `prev_sign_id` bigint COMMENT '前一次签名ID',
  `prev_digest` varchar(128) COMMENT '前一次签名摘要',
  `signer_id` bigint NOT NULL COMMENT '签名人员工ID',
  `signer_name` varchar(64) NOT NULL COMMENT '签名人姓名（快照）',
  `signer_dept_id` bigint COMMENT '签名人科室ID（快照）',
  `signer_dept_name` varchar(64) COMMENT '签名人科室名称（快照）',
  `signer_title` varchar(64) COMMENT '签名人职称',
  `cert_id` bigint NOT NULL COMMENT '所用证书ID',
  `cert_no` varchar(32) NOT NULL COMMENT '所用证书编号（快照）',
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

-- biz_inpatient_order  住院医嘱主表
CREATE TABLE `biz_inpatient_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '医嘱号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `dept_id` bigint COMMENT '开立科室ID',
  `dept_name` varchar(64) COMMENT '开立科室名称（快照）',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称（快照）',
  `bed_no` varchar(32) COMMENT '床号（快照）',
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
ALTER TABLE `biz_diag_template` ADD CONSTRAINT `fk_biz_diag_template_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_infusion_round` ADD CONSTRAINT `fk_biz_infusion_round_exec_id` FOREIGN KEY (`exec_id`) REFERENCES `biz_inpatient_order_exec` (`id`);
ALTER TABLE `biz_infusion_round` ADD CONSTRAINT `fk_biz_infusion_round_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_inpatient_order` (`id`);
ALTER TABLE `biz_infusion_round` ADD CONSTRAINT `fk_biz_infusion_round_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_infusion_round` ADD CONSTRAINT `fk_biz_infusion_round_round_nurse_id` FOREIGN KEY (`round_nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_regist_id` FOREIGN KEY (`regist_id`) REFERENCES `biz_appoint_info` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_medical_record` ADD CONSTRAINT `fk_biz_medical_record_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_medical_record_log` ADD CONSTRAINT `fk_biz_medical_record_log_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_medical_record` (`id`);
ALTER TABLE `biz_medical_record_log` ADD CONSTRAINT `fk_biz_medical_record_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_prescription` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_dispensing_id` FOREIGN KEY (`dispensing_id`) REFERENCES `biz_drug_dispensing` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_dispense_by_id` FOREIGN KEY (`dispense_by_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_narcotic_register` ADD CONSTRAINT `fk_biz_narcotic_register_checker_id` FOREIGN KEY (`checker_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_treatment_record_id` FOREIGN KEY (`treatment_record_id`) REFERENCES `biz_treatment_record` (`record_id`);
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_seat_id` FOREIGN KEY (`seat_id`) REFERENCES `biz_infusion_seat` (`id`);
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_skin_test_id` FOREIGN KEY (`skin_test_id`) REFERENCES `biz_skin_test` (`id`);
ALTER TABLE `biz_outp_infusion` ADD CONSTRAINT `fk_biz_outp_infusion_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_outp_infusion_round` ADD CONSTRAINT `fk_biz_outp_infusion_round_infusion_id` FOREIGN KEY (`infusion_id`) REFERENCES `biz_outp_infusion` (`id`);
ALTER TABLE `biz_outp_infusion_round` ADD CONSTRAINT `fk_biz_outp_infusion_round_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
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
ALTER TABLE `biz_skin_test` ADD CONSTRAINT `fk_biz_skin_test_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_skin_test` ADD CONSTRAINT `fk_biz_skin_test_treatment_record_id` FOREIGN KEY (`treatment_record_id`) REFERENCES `biz_treatment_record` (`record_id`);
ALTER TABLE `biz_skin_test` ADD CONSTRAINT `fk_biz_skin_test_nurse_id` FOREIGN KEY (`nurse_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_tcm_decoct` ADD CONSTRAINT `fk_biz_tcm_decoct_prescription_id` FOREIGN KEY (`prescription_id`) REFERENCES `biz_prescription` (`id`);
ALTER TABLE `biz_tcm_decoct` ADD CONSTRAINT `fk_biz_tcm_decoct_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_tcm_decoct` ADD CONSTRAINT `fk_biz_tcm_decoct_pharmacy_id` FOREIGN KEY (`pharmacy_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_tcm_decoct` ADD CONSTRAINT `fk_biz_tcm_decoct_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
