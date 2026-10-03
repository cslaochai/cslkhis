-- ============================================================
-- 领域 09 住院与医嘱（入出转·会诊·床位调度）（本域 14 表 + 上游参照 11 表 / 88 条关系）
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

-- biz_admission_order  入院通知单
CREATE TABLE `biz_admission_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(32) NOT NULL COMMENT '住院证号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄（快照）',
  `phone` varchar(20) COMMENT '联系电话（快照）',
  `id_card` varchar(18) COMMENT '身份证号（快照）',
  `regist_id` bigint COMMENT '来源挂号ID',
  `regist_no` varchar(32) COMMENT '来源挂号号',
  `visit_id` bigint COMMENT '来源就诊次ID',
  `source_dept_id` bigint COMMENT '开证科室ID',
  `source_dept_name` varchar(100) COMMENT '开证科室名称（快照）',
  `source_doctor_id` bigint COMMENT '开证医生ID',
  `source_doctor_name` varchar(50) COMMENT '开证医生姓名（快照）',
  `apply_dept_id` bigint COMMENT '拟收治科室ID',
  `apply_dept_name` varchar(100) COMMENT '拟收治科室名称（快照）',
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

-- biz_inpatient_order_template  医嘱模板
CREATE TABLE `biz_inpatient_order_template` (
  `id` bigint NOT NULL COMMENT '模板ID（雪花）',
  `doctor_id` bigint COMMENT '归属医生',
  `doctor_name` varchar(64) COMMENT '医生姓名（快照）',
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

-- biz_inpatient_transfer  住院转科轨迹
CREATE TABLE `biz_inpatient_transfer` (
  `id` bigint NOT NULL COMMENT '转科记录ID（雪花）',
  `transfer_no` varchar(32) NOT NULL COMMENT '转科单号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `admission_no` varchar(32) COMMENT '入院号（快照）',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `from_dept_id` bigint COMMENT '转出科室ID',
  `from_dept_name` varchar(64) COMMENT '转出科室名称（快照）',
  `from_ward_id` bigint COMMENT '转出病区ID',
  `from_ward_name` varchar(64) COMMENT '转出病区名称（快照）',
  `from_bed_id` bigint COMMENT '转出床位ID',
  `from_bed_no` varchar(32) COMMENT '转出床位号（快照）',
  `to_dept_id` bigint NOT NULL COMMENT '转入科室ID',
  `to_dept_name` varchar(64) COMMENT '转入科室名称（快照）',
  `to_ward_id` bigint NOT NULL COMMENT '转入病区ID',
  `to_ward_name` varchar(64) COMMENT '转入病区名称（快照）',
  `to_bed_id` bigint NOT NULL COMMENT '转入床位ID',
  `to_bed_no` varchar(32) COMMENT '转入床位号（快照）',
  `transfer_type` tinyint NOT NULL DEFAULT 1 COMMENT '转科类型（1-普通转科 2-急诊转科 3-转入ICU 4-ICU转出）',
  `transfer_reason` varchar(500) NOT NULL COMMENT '转科原因',
  `hospital_days` int COMMENT '发起转科时该次住院的已住院天数',
  `stop_orders_count` int NOT NULL DEFAULT 0 COMMENT '接收时随之停止的长期医嘱条数',
  `order_remark` varchar(500) COMMENT '医嘱处置说明',
  `apply_doctor_id` bigint COMMENT '转出方发起医生ID',
  `apply_doctor_name` varchar(64) COMMENT '转出方发起医生姓名（快照）',
  `receive_doctor_id` bigint COMMENT '转入方接收医生ID（员工ID）',
  `receive_doctor_name` varchar(64) COMMENT '转入方接收医生姓名（快照）',
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

-- biz_inpatient_leave  住院请假登记
CREATE TABLE `biz_inpatient_leave` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `leave_no` varchar(32) NOT NULL COMMENT '请假单号',
  `admission_id` bigint NOT NULL COMMENT '住院记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄',
  `dept_id` bigint COMMENT '申请时点所在科室ID',
  `dept_name` varchar(100) COMMENT '科室名称（快照）',
  `ward_name` varchar(64) COMMENT '病区名称（快照）',
  `bed_no` varchar(16) COMMENT '床位号（快照）',
  `admission_no` varchar(32) COMMENT '住院号（快照）',
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

-- biz_critical_notice  病危重通知回执
CREATE TABLE `biz_critical_notice` (
  `id` bigint NOT NULL COMMENT '主键（雪花ID）',
  `notice_no` varchar(32) NOT NULL COMMENT '通知单号',
  `admission_id` bigint NOT NULL COMMENT '住院记录ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `gender` tinyint COMMENT '性别（1-男 2-女 3-未知）',
  `age` int COMMENT '年龄',
  `dept_id` bigint COMMENT '开单科室ID',
  `dept_name` varchar(100) COMMENT '开单科室名称（快照）',
  `ward_name` varchar(64) COMMENT '病区名称（快照）',
  `bed_no` varchar(16) COMMENT '床位号（快照）',
  `admission_no` varchar(32) COMMENT '住院号（快照）',
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

-- biz_consultation  会诊申请记录
CREATE TABLE `biz_consultation` (
  `consultation_id` bigint NOT NULL COMMENT '会诊ID',
  `consultation_no` varchar(32) NOT NULL COMMENT '会诊编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `visit_id` bigint COMMENT '就诊次ID',
  `admission_id` bigint COMMENT '入院ID',
  `from_dept_id` bigint NOT NULL COMMENT '申请科室ID',
  `apply_doctor_id` bigint COMMENT '申请医生ID',
  `apply_doctor_name` varchar(64) COMMENT '申请医生姓名（快照）',
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
  `accept_doctor_name` varchar(64) COMMENT '接诊医生姓名（快照）',
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

-- biz_bed_wait  等床队列
CREATE TABLE `biz_bed_wait` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `wait_no` varchar(32) NOT NULL COMMENT '等待号',
  `admission_order_id` bigint COMMENT '来源住院证ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `gender` tinyint COMMENT '性别（1-男 2-女 9-未知）',
  `age` int COMMENT '年龄（快照）',
  `phone` varchar(20) COMMENT '联系电话（快照）',
  `apply_dept_id` bigint COMMENT '拟收治科室ID',
  `apply_dept_name` varchar(100) COMMENT '拟收治科室名称（快照）',
  `expect_ward_id` bigint COMMENT '期望病区ID',
  `bed_type` varchar(32) NOT NULL DEFAULT 'normal' COMMENT '需求床型',
  `priority` tinyint NOT NULL DEFAULT 1 COMMENT '优先级（1-普通 2-急 3-危重）',
  `gender_limit` tinyint NOT NULL DEFAULT 0 COMMENT '性别限制（0-不限 1-限男床 2-限女床）',
  `isolation_flag` tinyint NOT NULL DEFAULT 0 COMMENT '隔离需求（0-否 1-是）',
  `expect_admit_date` date COMMENT '预计入院日期',
  `diagnosis_name` varchar(200) COMMENT '拟诊名称（快照）',
  `wait_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-等待中 1-已安排床位 2-已收治 3-已取消）',
  `register_time` datetime NOT NULL COMMENT '登记排队时间',
  `assigned_bed_id` bigint COMMENT '已安排的床位ID',
  `assigned_bed_no` varchar(16) COMMENT '已安排床位号（快照）',
  `assigned_ward_id` bigint COMMENT '已安排床位所在病区ID',
  `assigned_ward_name` varchar(64) COMMENT '已安排病区名称（快照）',
  `assigned_dept_id` bigint COMMENT '已安排床位所属科室ID',
  `assigned_dept_name` varchar(100) COMMENT '已安排床位所属科室名称（快照）',
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

-- biz_bed_allocate  床位调配台账
CREATE TABLE `biz_bed_allocate` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `allocate_no` varchar(32) NOT NULL COMMENT '调配单号',
  `bed_id` bigint NOT NULL COMMENT '床位ID',
  `bed_no` varchar(16) COMMENT '床位号（快照）',
  `ward_id` bigint COMMENT '病区ID（快照）',
  `ward_name` varchar(64) COMMENT '病区名称（快照）',
  `own_dept_id` bigint COMMENT '床位归属科室ID',
  `own_dept_name` varchar(100) COMMENT '床位归属科室名称（快照）',
  `use_dept_id` bigint COMMENT '实际使用科室ID',
  `use_dept_name` varchar(100) COMMENT '实际使用科室名称（快照）',
  `wait_id` bigint COMMENT '来源等床记录ID',
  `patient_id` bigint COMMENT '患者ID',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `alloc_type` tinyint NOT NULL DEFAULT 1 COMMENT '调配类型（1-本科室预留 2-跨科调配 3-急诊占床）',
  `alloc_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已预留 2-已转入院 3-已释放 4-已作废）',
  `operator_id` bigint COMMENT '操作人ID',
  `operator_name` varchar(50) COMMENT '操作人姓名（快照）',
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

-- biz_fee_record  费用记账流水
CREATE TABLE `biz_fee_record` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `fee_no` varchar(32) NOT NULL COMMENT '记账流水号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者号（快照）',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名（快照）',
  `encounter_type` tinyint NOT NULL COMMENT '就诊类型（1-门诊 2-住院）',
  `encounter_id` bigint NOT NULL COMMENT '就诊标识',
  `encounter_no` varchar(32) COMMENT '就诊标识单号',
  `dept_id` bigint COMMENT '费用归属科室',
  `dept_name` varchar(100) COMMENT '科室名称（快照）',
  `doctor_id` bigint COMMENT '开单/执行人员工ID',
  `doctor_name` varchar(50) COMMENT '开单人姓名（快照）',
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
  `book_by_name` varchar(64) COMMENT '记账人姓名（快照）',
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

-- biz_inpatient_record  住院病历文书
CREATE TABLE `biz_inpatient_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `record_no` varchar(32) NOT NULL COMMENT '病历文书号',
  `admission_id` bigint NOT NULL COMMENT '入院ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) COMMENT '患者编号（快照）',
  `patient_name` varchar(50) COMMENT '患者姓名（快照）',
  `gender` tinyint COMMENT '性别（快照）（1-男 2-女）',
  `age` int COMMENT '年龄（快照）',
  `age_unit` tinyint COMMENT '年龄单位（快照）（1-岁 2-月 3-天）',
  `dept_id` bigint COMMENT '科室ID',
  `dept_name` varchar(64) COMMENT '科室名称（快照）',
  `ward_id` bigint COMMENT '病区ID',
  `ward_name` varchar(64) COMMENT '病区名称（快照）',
  `bed_no` varchar(32) COMMENT '床号（快照）',
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
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_visit_id` FOREIGN KEY (`visit_id`) REFERENCES `biz_visit` (`visit_id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_from_dept_id` FOREIGN KEY (`from_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_apply_doctor_id` FOREIGN KEY (`apply_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_to_dept_id` FOREIGN KEY (`to_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_accept_doctor_id` FOREIGN KEY (`accept_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_consultation` ADD CONSTRAINT `fk_biz_consultation_record_id` FOREIGN KEY (`record_id`) REFERENCES `biz_inpatient_record` (`id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_witness_doctor_id` FOREIGN KEY (`witness_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_critical_notice` ADD CONSTRAINT `fk_biz_critical_notice_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
ALTER TABLE `biz_discharge` ADD CONSTRAINT `fk_biz_discharge_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_discharge` ADD CONSTRAINT `fk_biz_discharge_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_discharge` ADD CONSTRAINT `fk_biz_discharge_discharge_doctor_id` FOREIGN KEY (`discharge_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_discharge_drug` ADD CONSTRAINT `fk_biz_discharge_drug_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_discharge_drug` ADD CONSTRAINT `fk_biz_discharge_drug_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_discharge_drug` ADD CONSTRAINT `fk_biz_discharge_drug_drug_id` FOREIGN KEY (`drug_id`) REFERENCES `sys_drug` (`id`);
ALTER TABLE `biz_discharge_drug` ADD CONSTRAINT `fk_biz_discharge_drug_dispense_by` FOREIGN KEY (`dispense_by`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_admission_id` FOREIGN KEY (`admission_id`) REFERENCES `biz_admission` (`admission_id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_inpatient_leave` ADD CONSTRAINT `fk_biz_inpatient_leave_sign_id` FOREIGN KEY (`sign_id`) REFERENCES `biz_emr_signature` (`id`);
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
