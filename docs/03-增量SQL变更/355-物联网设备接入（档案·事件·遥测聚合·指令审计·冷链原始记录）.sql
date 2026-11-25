-- 355 物联网设备接入（档案·事件·遥测聚合·指令审计·冷链原始记录）
-- 库：hn_biz_his
-- 说明：MQTT 设备上报数据的落库结构。原始遥测不落库（只留 Redis 最新值 + 环形缓存），
--       事件全存、遥测按 5 分钟聚合、冷链温度按 GSP 留记录（常规 30 分钟一条、超限 2 分钟一条，保存 5 年）。
-- 口径：device_no 即 MQTT 主题里的 {id}；事件幂等键 (device_no, seq, ts)；
--       大表按 ts / window_start 月分区，过期 DROP PARTITION，禁止 DELETE 清理。

-- ----------------------------
-- biz_iot_device  物联网设备档案
-- ----------------------------
CREATE TABLE `biz_iot_device` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_no` varchar(64) NOT NULL COMMENT '设备编号（主题中的设备标识）',
  `device_type` varchar(32) NOT NULL COMMENT '设备类型（coldchain-药品冷藏 infusion-输液泵 asset-定位信标 env-环境监测 gas-医用气体 bed-智能床垫 dialysis-透析机 cabinet-智能药柜 baby-婴儿腕带 uv-空气消毒机 robot-送药机器人）',
  `device_name` varchar(128) NOT NULL DEFAULT '' COMMENT '设备名称',
  `dept_id` bigint DEFAULT NULL COMMENT '归属科室ID',
  `dept_name` varchar(64) NOT NULL DEFAULT '' COMMENT '归属科室名称',
  `location` varchar(128) NOT NULL DEFAULT '' COMMENT '安装位置',
  `bind_type` varchar(32) DEFAULT NULL COMMENT '绑定业务对象类型（bed-床位 infusion-输液单 cabinet-药柜 none-未绑定）',
  `bind_id` bigint DEFAULT NULL COMMENT '绑定业务对象ID',
  `fw_version` varchar(64) DEFAULT NULL COMMENT '固件版本',
  `online` tinyint NOT NULL DEFAULT '0' COMMENT '在线状态（0-离线 1-在线）',
  `last_seen` datetime DEFAULT NULL COMMENT '最后上报时间',
  `last_state` varchar(32) NOT NULL DEFAULT '' COMMENT '最后状态码',
  `alarm_flag` tinyint NOT NULL DEFAULT '0' COMMENT '告警标志（0-正常 1-告警）',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '启用状态（0-停用 1-启用）',
  `create_by` varchar(64) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  `update_by` varchar(64) NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT (now()) COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by_id` bigint NOT NULL COMMENT '创建人ID',
  `update_by_id` bigint NOT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_device_no` (`device_no`,`del_flag`),
  KEY `idx_iot_device_dept` (`dept_id`),
  KEY `idx_iot_device_type_online` (`device_type`,`online`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联网设备档案';

-- ----------------------------
-- biz_iot_event  物联网设备事件（全存）
-- ----------------------------
CREATE TABLE `biz_iot_event` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_no` varchar(64) NOT NULL COMMENT '设备编号',
  `device_type` varchar(32) NOT NULL COMMENT '设备类型',
  `dept_id` bigint DEFAULT NULL COMMENT '归属科室ID',
  `event_code` varchar(64) NOT NULL COMMENT '事件码',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '事件级别（1-提示 2-警告 3-严重）',
  `detail` varchar(500) NOT NULL DEFAULT '' COMMENT '事件描述',
  `data_json` json DEFAULT NULL COMMENT '事件明细数据',
  `seq` bigint NOT NULL DEFAULT '0' COMMENT '设备侧报文序号',
  `ts` datetime NOT NULL COMMENT '事件发生时间',
  `handled_flag` tinyint NOT NULL DEFAULT '0' COMMENT '处理标志（0-未处理 1-已处理 2-已忽略）',
  `handle_by_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handle_by` varchar(64) DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理说明',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  PRIMARY KEY (`id`,`ts`),
  UNIQUE KEY `uk_iot_event_idem` (`device_no`,`seq`,`ts`),
  KEY `idx_iot_event_dev_ts` (`device_no`,`ts`),
  KEY `idx_iot_event_code_ts` (`event_code`,`ts`),
  KEY `idx_iot_event_handle_ts` (`handled_flag`,`ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物联网设备事件'
PARTITION BY RANGE COLUMNS(`ts`) (
  PARTITION p202611 VALUES LESS THAN ('2026-12-01'),
  PARTITION p202612 VALUES LESS THAN ('2027-01-01'),
  PARTITION p202701 VALUES LESS THAN ('2027-02-01'),
  PARTITION pmax VALUES LESS THAN (MAXVALUE)
);

-- ----------------------------
-- biz_iot_metric_5m  遥测 5 分钟聚合
-- ----------------------------
CREATE TABLE `biz_iot_metric_5m` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_no` varchar(64) NOT NULL COMMENT '设备编号',
  `device_type` varchar(32) NOT NULL COMMENT '设备类型',
  `window_start` datetime NOT NULL COMMENT '统计窗口起点（5 分钟对齐）',
  `sample_count` int NOT NULL DEFAULT '1' COMMENT '窗口内采样点数量',
  `temp` decimal(5,2) DEFAULT NULL COMMENT '温度（℃）',
  `rate` decimal(8,2) DEFAULT NULL COMMENT '速率（输液泵 ml/h）',
  `battery` tinyint DEFAULT NULL COMMENT '电量（%）',
  `metrics_json` json DEFAULT NULL COMMENT '窗口指标均值与极值',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  PRIMARY KEY (`id`,`window_start`),
  UNIQUE KEY `uk_iot_metric_win` (`device_no`,`window_start`),
  KEY `idx_iot_metric_win` (`window_start`),
  KEY `idx_iot_metric_dev_win` (`device_no`,`window_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='遥测5分钟聚合'
PARTITION BY RANGE COLUMNS(`window_start`) (
  PARTITION p202611 VALUES LESS THAN ('2026-12-01'),
  PARTITION p202612 VALUES LESS THAN ('2027-01-01'),
  PARTITION p202701 VALUES LESS THAN ('2027-02-01'),
  PARTITION pmax VALUES LESS THAN (MAXVALUE)
);

-- ----------------------------
-- biz_iot_cmd_log  设备指令下发审计
-- ----------------------------
CREATE TABLE `biz_iot_cmd_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_no` varchar(64) NOT NULL COMMENT '设备编号',
  `request_id` varchar(64) NOT NULL COMMENT '指令请求号',
  `cmd` varchar(32) NOT NULL COMMENT '指令码',
  `cmd_value` varchar(64) DEFAULT NULL COMMENT '指令参数',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) NOT NULL COMMENT '操作人',
  `reason` varchar(200) NOT NULL COMMENT '下发原因',
  `send_time` datetime NOT NULL COMMENT '下发时间',
  `ack_ok` tinyint DEFAULT NULL COMMENT '回执结果（0-失败 1-成功 NULL-未回执）',
  `ack_message` varchar(200) DEFAULT NULL COMMENT '回执说明',
  `ack_time` datetime DEFAULT NULL COMMENT '回执时间',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iot_cmd_req` (`device_no`,`request_id`),
  KEY `idx_iot_cmd_time` (`send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备指令下发审计';

-- ----------------------------
-- biz_coldchain_temp_log  药品冷链温度原始记录（1 分钟）
-- ----------------------------
CREATE TABLE `biz_coldchain_temp_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_no` varchar(64) NOT NULL COMMENT '设备编号',
  `dept_name` varchar(64) NOT NULL DEFAULT '' COMMENT '归属科室名称',
  `cabinet_name` varchar(128) NOT NULL DEFAULT '' COMMENT '柜体名称',
  `temp` decimal(5,2) NOT NULL COMMENT '温度（℃）',
  `humidity` tinyint DEFAULT NULL COMMENT '湿度（%）',
  `door` tinyint NOT NULL DEFAULT '0' COMMENT '柜门状态（0-关闭 1-开启）',
  `power` tinyint NOT NULL DEFAULT '1' COMMENT '供电状态（0-异常 1-正常）',
  `record_type` tinyint NOT NULL DEFAULT '1' COMMENT '记录类型（1-常规 30 分钟 2-超限加密 2 分钟）',
  `ts` datetime NOT NULL COMMENT '采样时间',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '创建时间',
  PRIMARY KEY (`id`,`ts`),
  UNIQUE KEY `uk_coldchain_dev_ts` (`device_no`,`ts`),
  KEY `idx_coldchain_dev_ts` (`device_no`,`ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品冷链温度记录'
PARTITION BY RANGE COLUMNS(`ts`) (
  PARTITION p2026 VALUES LESS THAN ('2027-01-01'),
  PARTITION p2027 VALUES LESS THAN ('2028-01-01'),
  PARTITION pmax VALUES LESS THAN (MAXVALUE)
);
