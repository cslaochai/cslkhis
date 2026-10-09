-- ============================================================
-- 领域 03 空间与设备主数据（病区·床位·诊室·手术间·设备）（本域 8 表 + 上游参照 2 表 / 8 条关系）
-- 由 workspace/_er/refresh.mjs 从 dev 库 information_schema 反向生成，只用于建模，禁止在业务库执行。
-- 关系 = *_id 列命名推断 + 真实数据覆盖率验证，逐条证据见 docs/er/relationships.csv。
-- PowerDesigner：File → Reverse Engineer → Database → 模板选 MySQL 8.0 → 勾选 Script file 指向本文件。
-- ============================================================


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

-- sys_clinic_room  诊室
CREATE TABLE `sys_clinic_room` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '诊室名称',
  `queue_prefix` varchar(2) COMMENT '呼叫代号（队列号前缀，如A/B/C…）',
  `code` varchar(100) NOT NULL COMMENT '诊室编号（ABCD）',
  `location` varchar(200) NOT NULL COMMENT '地理位置',
  `dept_id` bigint NOT NULL COMMENT '所属科室ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '诊室状态（1-启用 0-停用）',
  `remark` varchar(255) COMMENT '备注信息',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诊室';

-- sys_operation_room  手术间
CREATE TABLE `sys_operation_room` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `room_code` varchar(32) NOT NULL COMMENT '手术间编码',
  `room_name` varchar(64) NOT NULL COMMENT '手术间名称',
  `location` varchar(200) COMMENT '位置',
  `sort_order` int NOT NULL DEFAULT 1 COMMENT '总表列顺序（升序）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-启用 0-停用）',
  `remark` varchar(500) COMMENT '备注',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_code` (`room_code`),
  UNIQUE KEY `uk_room_name` (`room_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手术间';

-- sys_equipment  医疗设备台账
CREATE TABLE `sys_equipment` (
  `id` bigint NOT NULL COMMENT '主键',
  `equipment_code` varchar(32) NOT NULL COMMENT '设备编码',
  `equipment_name` varchar(200) NOT NULL COMMENT '设备名称',
  `category` tinyint NOT NULL COMMENT '设备类别',
  `dept_id` bigint COMMENT '使用科室ID',
  `dept_name` varchar(100) COMMENT '使用科室名称',
  `brand` varchar(100) COMMENT '品牌',
  `model` varchar(100) COMMENT '型号',
  `purchase_date` date COMMENT '购置日期',
  `purchase_price` decimal(14,2) COMMENT '购置价格(元)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1-在用 2-停用 3-维修中 4-报废）',
  `maintain_cycle_days` int DEFAULT 365 COMMENT '维保周期(天)',
  `last_maintain_date` date COMMENT '最近维保日期',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_code` (`equipment_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医疗设备台账';

-- biz_equipment_maintain  设备维保记录
CREATE TABLE `biz_equipment_maintain` (
  `id` bigint NOT NULL COMMENT '维保记录ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_code` varchar(32) COMMENT '设备编码（快照）',
  `equipment_name` varchar(200) COMMENT '设备名称（快照）',
  `maintain_type` tinyint NOT NULL COMMENT '维保类型（1-保养 2-维修 3-巡检）',
  `maintain_date` date NOT NULL COMMENT '维保日期',
  `next_maintain_date` date COMMENT '下次维保日期',
  `cost` decimal(12,2) COMMENT '费用（元）',
  `fault_desc` varchar(500) COMMENT '故障描述',
  `handle_result` varchar(500) COMMENT '处理结果',
  `maintain_result` tinyint NOT NULL DEFAULT 1 COMMENT '维保结果（1-正常 2-异常）',
  `handler_name` varchar(50) COMMENT '维保人',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维保记录';

-- biz_equipment_metering  设备计量记录
CREATE TABLE `biz_equipment_metering` (
  `id` bigint NOT NULL COMMENT '计量记录ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_code` varchar(32) COMMENT '设备编码（快照）',
  `equipment_name` varchar(200) COMMENT '设备名称（快照）',
  `metering_type` tinyint NOT NULL COMMENT '计量类型（1-强检 2-校准）',
  `metering_date` date NOT NULL COMMENT '计量日期',
  `valid_until` date NOT NULL COMMENT '有效期至',
  `metering_result` tinyint NOT NULL DEFAULT 1 COMMENT '计量结果（1-合格 2-不合格）',
  `cert_no` varchar(64) COMMENT '证书编号',
  `agency` varchar(128) COMMENT '检定/校准机构',
  `create_by` varchar(64) COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备计量记录';

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
-- ---------------- 参照关系（E-R 连线） ----------------
ALTER TABLE `biz_equipment_maintain` ADD CONSTRAINT `fk_biz_equipment_maintain_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `sys_equipment` (`id`);
ALTER TABLE `biz_equipment_metering` ADD CONSTRAINT `fk_biz_equipment_metering_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `sys_equipment` (`id`);
ALTER TABLE `sys_bed` ADD CONSTRAINT `fk_sys_bed_ward_id` FOREIGN KEY (`ward_id`) REFERENCES `sys_ward` (`ward_id`);
ALTER TABLE `sys_bed` ADD CONSTRAINT `fk_sys_bed_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_bed` ADD CONSTRAINT `fk_sys_bed_patient_id` FOREIGN KEY (`patient_id`) REFERENCES `biz_patient` (`id`);
ALTER TABLE `sys_clinic_room` ADD CONSTRAINT `fk_sys_clinic_room_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_equipment` ADD CONSTRAINT `fk_sys_equipment_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
ALTER TABLE `sys_ward` ADD CONSTRAINT `fk_sys_ward_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`);
