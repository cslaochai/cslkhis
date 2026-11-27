-- 419 床旁监护仪（monitor）接入：时序超级表 + 设备台账对齐在院床位
-- 背景：病区大屏已接输液泵（按就诊绑定）与智能床垫（按床号对齐），床旁监护仪此前未接入。
--       本脚本把 monitor 纳入同一条链路：数值（心率/血氧/血压/呼吸/体温/心律）落 TDengine，
--       ECG/脉搏波/呼吸高频曲线只在 HIS 内存缓冲取窗，不建历史表。
-- 分工：上半段在 TDengine（iot_raw 库）建超级表；下半段在 MySQL（hn_biz_his）铺设备台账。
-- 口径：监护仪固定装在床旁、跟床走不跟患者（同床垫），对齐字段为 biz_iot_device.bed_no。

-- =====================================================================
-- 一、TDengine：iot_raw.st_monitor（原始点位，保留 90 天，与 356 其余超级表同口径）
-- 执行环境：TDengine 3.3.6.x（192.168.88.132:6030 / REST 6041），不是 MySQL
-- =====================================================================

CREATE STABLE IF NOT EXISTS iot_raw.st_monitor (
  ts TIMESTAMP,
  heart_rate INT,
  spo2 INT,
  sbp INT,
  dbp INT,
  resp_rate INT,
  temp_c DOUBLE,
  rhythm NCHAR(32),
  rhythm_label NCHAR(32),
  resp_pattern NCHAR(32)
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

-- =====================================================================
-- 二、MySQL：hn_biz_his.biz_iot_device 台账铺底（6 台，对齐当前在院且未挂床垫的床）
-- 设备编号与 SimLab 设备平台的 MON-01..06 一一对应；bed_no 决定大屏挂到哪张床。
-- 若后续床位调整，只改 bed_no/location/dept_id，不动遥测与前端。
-- =====================================================================

INSERT INTO `biz_iot_device`
(`id`, `device_no`, `device_type`, `power_type`, `device_name`, `dept_id`, `dept_name`,
 `location`, `bed_no`, `online`, `last_state`, `alarm_flag`, `enabled`,
 `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `create_by_id`, `update_by_id`)
VALUES
(419000000000000001, 'MON-01', 'monitor', 'mains', '14床床旁监护仪', 19580004, '消化内科', '消化内科病区 14床', '14', 0, '', 0, 1, 'admin', now(), 'admin', now(), 0, 0, 0),
(419000000000000002, 'MON-02', 'monitor', 'mains', '15床床旁监护仪', 19580004, '消化内科', '消化内科病区 15床', '15', 0, '', 0, 1, 'admin', now(), 'admin', now(), 0, 0, 0),
(419000000000000003, 'MON-03', 'monitor', 'mains', '16床床旁监护仪', 19580004, '消化内科', '消化内科病区 16床', '16', 0, '', 0, 1, 'admin', now(), 'admin', now(), 0, 0, 0),
(419000000000000004, 'MON-04', 'monitor', 'mains', '01床床旁监护仪', 19580003, '呼吸内科', '呼吸内科病区 01床', '01', 0, '', 0, 1, 'admin', now(), 'admin', now(), 0, 0, 0),
(419000000000000005, 'MON-05', 'monitor', 'mains', '02床床旁监护仪', 19580003, '呼吸内科', '呼吸内科病区 02床', '02', 0, '', 0, 1, 'admin', now(), 'admin', now(), 0, 0, 0),
(419000000000000006, 'MON-06', 'monitor', 'mains', '12床床旁监护仪', 19580005, '心血管内科', '心血管内科病区 12床', '12', 0, '', 0, 1, 'admin', now(), 'admin', now(), 0, 0, 0);
