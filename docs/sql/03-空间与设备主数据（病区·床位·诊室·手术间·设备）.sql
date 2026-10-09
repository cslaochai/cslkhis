-- 领域：03-空间与设备主数据（病区·床位·诊室·手术间·设备）
-- 库：hn_biz_his    表数：8
-- 说明：DDL 快照（由线上库 SHOW CREATE TABLE 导出，无 DROP / 无数据）。建表语句彼此独立，不含外键约束。

-- ----------------------------
-- sys_ward  病区
-- ----------------------------
CREATE TABLE `sys_ward` (
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `ward_code` varchar(32) NOT NULL COMMENT '病区编码',
  `ward_name` varchar(64) NOT NULL COMMENT '病区名称',
  `dept_id` bigint NOT NULL COMMENT '所属科室ID',
  `total_beds` int NOT NULL DEFAULT '0' COMMENT '总床位数',
  `occupied_beds` int NOT NULL DEFAULT '0' COMMENT '已占用床位数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-停用 1-正常）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`ward_id`),
  UNIQUE KEY `uk_ward_code` (`ward_code`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='病区';

-- ----------------------------
-- sys_bed  床位
-- ----------------------------
CREATE TABLE `sys_bed` (
  `bed_id` bigint NOT NULL COMMENT '床位ID',
  `bed_no` varchar(16) NOT NULL COMMENT '床位号',
  `ward_id` bigint NOT NULL COMMENT '病区ID',
  `dept_id` bigint NOT NULL COMMENT '科室ID',
  `bed_type` varchar(32) DEFAULT NULL COMMENT '床位类型',
  `bed_status` tinyint NOT NULL DEFAULT '1' COMMENT '床位状态（0-维修 1-空闲 2-占用 3-锁定）',
  `patient_id` bigint DEFAULT NULL COMMENT '当前占用患者ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`bed_id`),
  UNIQUE KEY `uk_bed_no` (`ward_id`,`bed_no`),
  KEY `idx_ward_id` (`ward_id`),
  KEY `idx_bed_status` (`bed_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='床位';

-- ----------------------------
-- sys_clinic_room  诊室
-- ----------------------------
CREATE TABLE `sys_clinic_room` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '诊室名称',
  `queue_prefix` varchar(2) DEFAULT NULL COMMENT '呼叫代号（队列号前缀，如A/B/C…）',
  `code` varchar(100) NOT NULL COMMENT '诊室编号（ABCD）',
  `location` varchar(200) NOT NULL COMMENT '地理位置',
  `dept_id` bigint NOT NULL COMMENT '所属科室ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '诊室状态（1-启用 0-停用）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='诊室';

-- ----------------------------
-- sys_operation_room  手术间
-- ----------------------------
CREATE TABLE `sys_operation_room` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花）',
  `room_code` varchar(32) NOT NULL COMMENT '手术间编码',
  `room_name` varchar(64) NOT NULL COMMENT '手术间名称',
  `location` varchar(200) DEFAULT NULL COMMENT '位置',
  `sort_order` int NOT NULL DEFAULT '1' COMMENT '总表列顺序（升序）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-启用 0-停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_code` (`room_code`),
  UNIQUE KEY `uk_room_name` (`room_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='手术间';

-- ----------------------------
-- sys_equipment  医疗设备台账
-- ----------------------------
CREATE TABLE `sys_equipment` (
  `id` bigint NOT NULL COMMENT '主键',
  `equipment_code` varchar(32) NOT NULL COMMENT '设备编码',
  `equipment_name` varchar(200) NOT NULL COMMENT '设备名称',
  `category` tinyint NOT NULL COMMENT '设备类别',
  `dept_id` bigint DEFAULT NULL COMMENT '使用科室ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '使用科室名称',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `model` varchar(100) DEFAULT NULL COMMENT '型号',
  `purchase_date` date DEFAULT NULL COMMENT '购置日期',
  `purchase_price` decimal(14,2) DEFAULT NULL COMMENT '购置价格(元)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-在用 2-停用 3-维修中 4-报废）',
  `maintain_cycle_days` int DEFAULT '365' COMMENT '维保周期(天)',
  `last_maintain_date` date DEFAULT NULL COMMENT '最近维保日期',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_code` (`equipment_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医疗设备台账';

-- ----------------------------
-- biz_equipment_maintain  设备维保记录
-- ----------------------------
CREATE TABLE `biz_equipment_maintain` (
  `id` bigint NOT NULL COMMENT '维保记录ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_code` varchar(32) DEFAULT NULL COMMENT '设备编码（快照）',
  `equipment_name` varchar(200) DEFAULT NULL COMMENT '设备名称（快照）',
  `maintain_type` tinyint NOT NULL COMMENT '维保类型（1-保养 2-维修 3-巡检）',
  `maintain_date` date NOT NULL COMMENT '维保日期',
  `next_maintain_date` date DEFAULT NULL COMMENT '下次维保日期',
  `cost` decimal(12,2) DEFAULT NULL COMMENT '费用（元）',
  `fault_desc` varchar(500) DEFAULT NULL COMMENT '故障描述',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `maintain_result` tinyint NOT NULL DEFAULT '1' COMMENT '维保结果（1-正常 2-异常）',
  `handler_name` varchar(50) DEFAULT NULL COMMENT '维保人',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  KEY `idx_bem_equipment` (`equipment_id`),
  KEY `idx_bem_date` (`maintain_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备维保记录';

-- ----------------------------
-- biz_equipment_metering  设备计量记录
-- ----------------------------
CREATE TABLE `biz_equipment_metering` (
  `id` bigint NOT NULL COMMENT '计量记录ID',
  `equipment_id` bigint NOT NULL COMMENT '设备ID',
  `equipment_code` varchar(32) DEFAULT NULL COMMENT '设备编码（快照）',
  `equipment_name` varchar(200) DEFAULT NULL COMMENT '设备名称（快照）',
  `metering_type` tinyint NOT NULL COMMENT '计量类型（1-强检 2-校准）',
  `metering_date` date NOT NULL COMMENT '计量日期',
  `valid_until` date NOT NULL COMMENT '有效期至',
  `metering_result` tinyint NOT NULL DEFAULT '1' COMMENT '计量结果（1-合格 2-不合格）',
  `cert_no` varchar(64) DEFAULT NULL COMMENT '证书编号',
  `agency` varchar(128) DEFAULT NULL COMMENT '检定/校准机构',
  `create_by` varchar(64) NOT NULL DEFAULT '' COMMENT '创建人',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL DEFAULT '' COMMENT '更新人',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  PRIMARY KEY (`id`),
  KEY `idx_bemeter_equipment` (`equipment_id`),
  KEY `idx_bemeter_valid` (`valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备计量记录';

-- ----------------------------
-- biz_infusion_seat  输液室座位
-- ----------------------------
CREATE TABLE `biz_infusion_seat` (
  `id` bigint NOT NULL COMMENT '主键（雪花）',
  `seat_no` varchar(32) NOT NULL COMMENT '座位号',
  `area` varchar(50) NOT NULL DEFAULT '普通区' COMMENT '区域（成人区/儿童区/隔离区等）',
  `seat_status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1-空闲 2-占用 3-停用）',
  `create_by` varchar(64) NOT NULL DEFAULT '',
  `create_by_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) NOT NULL DEFAULT '',
  `update_by_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `del_flag` tinyint NOT NULL DEFAULT '0',
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seat_no` (`seat_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='输液室座位';
