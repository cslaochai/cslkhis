-- 366 冷链 GSP 留档表（补 355 未落地）
-- 355 里只写了 CREATE 语句，dev 库实际没有这张表；本文件按当前实现补齐，并增加最低/最高/点数三列，
-- 与页面「5 分钟一轮」导出对齐。写入方：ColdChainArchiveService（定时从 TDengine 聚合归档）。

CREATE TABLE IF NOT EXISTS `biz_coldchain_temp_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `device_no` varchar(64) NOT NULL COMMENT '设备编号',
  `dept_name` varchar(64) NOT NULL DEFAULT '' COMMENT '归属科室名称',
  `cabinet_name` varchar(128) NOT NULL DEFAULT '' COMMENT '柜体名称',
  `temp` decimal(6,2) NOT NULL COMMENT '窗口平均温度（℃）',
  `temp_min` decimal(6,2) DEFAULT NULL COMMENT '窗口最低温度（℃）',
  `temp_max` decimal(6,2) DEFAULT NULL COMMENT '窗口最高温度（℃）',
  `humidity` decimal(5,2) DEFAULT NULL COMMENT '窗口平均湿度（%）',
  `point_count` int NOT NULL DEFAULT 0 COMMENT '窗口内上报点数',
  `door` tinyint NOT NULL DEFAULT 0 COMMENT '柜门状态（0-关闭 1-开启）',
  `power` tinyint NOT NULL DEFAULT 1 COMMENT '供电状态（0-异常 1-正常）',
  `record_type` tinyint NOT NULL DEFAULT 1 COMMENT '记录类型（1-常规 2-超限加密）',
  `ts` datetime NOT NULL COMMENT '窗口起始时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`, `ts`),
  UNIQUE KEY `uk_coldchain_dev_ts` (`device_no`, `ts`),
  KEY `idx_coldchain_dev_ts` (`device_no`, `ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='药品冷链温湿度GSP记录'
PARTITION BY RANGE COLUMNS(`ts`) (
  PARTITION p2026 VALUES LESS THAN ('2027-01-01'),
  PARTITION p2027 VALUES LESS THAN ('2028-01-01'),
  PARTITION p2028 VALUES LESS THAN ('2029-01-01'),
  PARTITION pmax VALUES LESS THAN (MAXVALUE)
);
