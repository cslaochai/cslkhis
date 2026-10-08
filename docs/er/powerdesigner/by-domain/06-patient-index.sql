-- ============================================================
-- 领域 06 患者主索引与健康档案（本域 12 表 + 上游参照 4 表 / 22 条关系）
-- 由 workspace/_er/emit.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


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

-- biz_patient_contact  患者联系方式
CREATE TABLE `biz_patient_contact` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `contact_name` varchar(100) NOT NULL COMMENT '联系人姓名',
  `relationship` tinyint NOT NULL COMMENT '与患者关系（如：父母、配偶、子女、朋友等）',
  `phone` varchar(20) COMMENT '联系电话',
  `is_primary` tinyint DEFAULT 0 COMMENT '是否主要联系人（0-否 1-是）',
  `address` varchar(255) COMMENT '联系地址',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者联系方式';

-- biz_patient_allergy  药物过敏史
CREATE TABLE `biz_patient_allergy` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `allergy_type` varchar(50) NOT NULL COMMENT '过敏类型（药物/食物/其他）',
  `allergen_name` varchar(200) NOT NULL COMMENT '过敏原名称',
  `allergy_severity` varchar(20) NOT NULL COMMENT '过敏严重程度（轻度/中度/重度/危及生命）',
  `allergy_symptoms` varchar(500) NOT NULL COMMENT '过敏反应表现',
  `allergy_date` date COMMENT '首次发生日期',
  `occurrence_count` int DEFAULT 1 COMMENT '发生次数',
  `treatment_given` varchar(200) COMMENT '过敏时处理措施',
  `confirmed_by` varchar(100) COMMENT '确认医生',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药物过敏史';

-- biz_patient_past_disease  既往疾病史
CREATE TABLE `biz_patient_past_disease` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `disease_name` varchar(200) NOT NULL COMMENT '疾病名称',
  `disease_code` varchar(50) COMMENT '疾病编码',
  `diagnosis_date` date COMMENT '诊断日期',
  `diagnosis_dept` varchar(100) COMMENT '诊断科室',
  `treatment_plan` text COMMENT '治疗方案',
  `current_status` varchar(50) COMMENT '当前控制情况（已治愈/控制良好/未控制/随访中）',
  `relapse_count` int DEFAULT 0 COMMENT '复发次数',
  `last_followup_date` date COMMENT '最近随访日期',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='既往疾病史';

-- biz_patient_family_history  家族史
CREATE TABLE `biz_patient_family_history` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `relationship` varchar(50) NOT NULL COMMENT '与患者关系（父亲/母亲/兄弟/姐妹/祖父/祖母/子女）',
  `name` varchar(100) COMMENT '亲属姓名',
  `age` int COMMENT '年龄',
  `is_alive` tinyint DEFAULT 1 COMMENT '是否在世（0-已故 1-在世）',
  `cause_of_death` varchar(200) COMMENT '死亡原因',
  `health_status` varchar(500) COMMENT '健康状况描述',
  `hereditary_disease` varchar(200) COMMENT '遗传性疾病（如：高血压、糖尿病、肿瘤等）',
  `infectious_disease` varchar(200) COMMENT '传染病史',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家族史';

-- biz_patient_surgery_history  手术外伤史
CREATE TABLE `biz_patient_surgery_history` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `surgery_name` varchar(200) NOT NULL COMMENT '手术名称',
  `surgery_date` date NOT NULL COMMENT '手术日期',
  `surgery_type` varchar(50) COMMENT '手术类型（择期/紧急/急诊）',
  `surgeon` varchar(100) COMMENT '主刀医生',
  `anesthesia_type` varchar(100) COMMENT '麻醉方式',
  `hospital_name` varchar(200) COMMENT '手术医院',
  `postop_diagnosis` varchar(200) COMMENT '术后诊断',
  `recovery_status` varchar(50) COMMENT '恢复情况（良好/一般/差/死亡）',
  `complications` text COMMENT '术后并发症',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术外伤史';

-- biz_patient_medication_history  既往用药史
CREATE TABLE `biz_patient_medication_history` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `drug_name` varchar(200) NOT NULL COMMENT '药物名称',
  `drug_type` varchar(50) COMMENT '药物类型（处方药/非处方药/中药/保健品）',
  `dosage` varchar(100) COMMENT '剂量',
  `frequency` varchar(100) COMMENT '频次',
  `route` varchar(50) COMMENT '给药途径（口服/注射/外用/吸入等）',
  `start_date` date NOT NULL COMMENT '开始用药日期',
  `end_date` date COMMENT '停药日期',
  `indications` varchar(200) COMMENT '用药指征/适应症',
  `prescriber` varchar(100) COMMENT '处方医生',
  `status` varchar(20) DEFAULT '已完成' COMMENT '用药状态（进行中/已停用/已换药/已减量）',
  `reason_stop` varchar(200) COMMENT '停药原因（疗效不佳/不良反应/患者要求/已治愈）',
  `remark` text COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='既往用药史';

-- biz_patient_tag_relation  患者标签关联
CREATE TABLE `biz_patient_tag_relation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `source_type` tinyint DEFAULT 1 COMMENT '标签来源（1-手动打标 2-系统自动打标）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '打标时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_tag` (`patient_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者标签关联';

-- biz_patient_guardian  就诊人绑定
CREATE TABLE `biz_patient_guardian` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '登录账号ID',
  `patient_id` bigint NOT NULL COMMENT '就诊人ID',
  `relation` tinyint NOT NULL DEFAULT 99 COMMENT '与账号所有人关系（1-本人 2-配偶 3-父亲 99-其他）',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认就诊人（0-否 1-是）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-停用 1-启用）',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_patient` (`user_id`, `patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊人绑定';

-- biz_patient_merge_log  患者合并审计
CREATE TABLE `biz_patient_merge_log` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `merge_no` varchar(32) NOT NULL COMMENT '合并流水号',
  `master_id` bigint NOT NULL COMMENT '主档患者ID',
  `master_no` varchar(32) COMMENT '主档患者号',
  `master_name` varchar(50) COMMENT '主档姓名',
  `merged_id` bigint NOT NULL COMMENT '被并入的患者ID',
  `merged_no` varchar(32) COMMENT '被并患者号',
  `merged_name` varchar(50) COMMENT '被并姓名',
  `match_type` tinyint NOT NULL COMMENT '匹配置信级别(强)（1-身份证号相同 2-姓名+性别+出生日期相同 3-姓名+手机号相同 4-人工判定）',
  `match_snapshot` varchar(500) COMMENT '命中依据的字段值快照',
  `master_snapshot` varchar(1000) COMMENT '主档关键字段快照 JSON',
  `merged_snapshot` varchar(1000) COMMENT '被并档关键字段快照 JSON',
  `data_count` varchar(500) COMMENT '合并时两档各自关联业务数据量快照 JSON',
  `reason` varchar(500) NOT NULL COMMENT '合并理由',
  `operator_id` bigint COMMENT '操作人ID',
  `operator_name` varchar(64) COMMENT '操作人姓名',
  `merge_time` datetime NOT NULL COMMENT '合并时间',
  `log_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-已合并 2-已撤销）',
  `revert_by` varchar(64) COMMENT '撤销人',
  `revert_time` datetime COMMENT '撤销时间',
  `revert_reason` varchar(500) COMMENT '撤销理由',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pml_no` (`merge_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者合并审计';

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

-- biz_chronic_record  慢病建档
CREATE TABLE `biz_chronic_record` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `record_no` varchar(32) NOT NULL COMMENT '档案编号',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `patient_no` varchar(32) NOT NULL COMMENT '患者号',
  `patient_name` varchar(50) NOT NULL COMMENT '患者姓名',
  `disease_code` varchar(32) NOT NULL COMMENT '慢病编码',
  `disease_name` varchar(100) NOT NULL COMMENT '慢病名称',
  `doctor_id` bigint NOT NULL COMMENT '认定医生ID',
  `doctor_name` varchar(50) NOT NULL COMMENT '认定医生姓名',
  `dept_id` bigint COMMENT '认定科室ID',
  `dept_name` varchar(100) COMMENT '认定科室名称',
  `confirm_status` tinyint NOT NULL DEFAULT 0 COMMENT '认定状态（0-待认定 1-已认定 2-已取消）',
  `confirm_time` datetime COMMENT '认定时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='慢病建档';

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

-- sys_patient_tag  患者标签
CREATE TABLE `sys_patient_tag` (
  `tag_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `short_name` varchar(2) COMMENT '标签缩写用于展示',
  `tag_color` varchar(20) COMMENT '标签颜色',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者标签';

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
ALTER TABLE `biz_chronic_record` ADD CONSTRAINT `fk_biz_chronic_record_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_chronic_record` ADD CONSTRAINT `fk_biz_chronic_record_doctor_id` FOREIGN KEY (`doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_chronic_record` ADD CONSTRAINT `fk_biz_chronic_record_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_master_id` FOREIGN KEY (`master_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_last_visit_dept` FOREIGN KEY (`last_visit_dept`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_last_visit_doctor` FOREIGN KEY (`last_visit_doctor`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_first_visit_dept_id` FOREIGN KEY (`first_visit_dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `biz_patient` ADD CONSTRAINT `fk_biz_patient_first_visit_doctor_id` FOREIGN KEY (`first_visit_doctor_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_patient_allergy` ADD CONSTRAINT `fk_biz_patient_allergy_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_contact` ADD CONSTRAINT `fk_biz_patient_contact_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_family_history` ADD CONSTRAINT `fk_biz_patient_family_history_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_guardian` ADD CONSTRAINT `fk_biz_patient_guardian_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);
ALTER TABLE `biz_patient_guardian` ADD CONSTRAINT `fk_biz_patient_guardian_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_medication_history` ADD CONSTRAINT `fk_biz_patient_medication_history_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_merge_log` ADD CONSTRAINT `fk_biz_patient_merge_log_master_id` FOREIGN KEY (`master_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_merge_log` ADD CONSTRAINT `fk_biz_patient_merge_log_merged_id` FOREIGN KEY (`merged_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_merge_log` ADD CONSTRAINT `fk_biz_patient_merge_log_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_employee` (`id`);
ALTER TABLE `biz_patient_past_disease` ADD CONSTRAINT `fk_biz_patient_past_disease_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_surgery_history` ADD CONSTRAINT `fk_biz_patient_surgery_history_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_tag_relation` ADD CONSTRAINT `fk_biz_patient_tag_relation_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `biz_patient_tag_relation` ADD CONSTRAINT `fk_biz_patient_tag_relation_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `sys_patient_tag` (`tag_id`);
ALTER TABLE `biz_visit` ADD CONSTRAINT `fk_biz_visit_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
