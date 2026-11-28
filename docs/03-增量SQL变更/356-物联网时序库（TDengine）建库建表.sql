-- 356 物联网时序库（TDengine 3.3）建库建表
-- 执行环境：TDengine 3.3.6.13（192.168.88.132:6030 / REST 6041），**不是 MySQL**
-- 分工：MySQL 的 355 存设备档案、事件、指令审计；本文件存遥测原始点与 5 分钟聚合。
-- 口径：一类设备一张超级表（stable），一台设备由 `USING ... TAGS(...)` 自动建子表；
--       子表名 = 设备编号小写（如 pump_01），查询永远写超级表 + 标签过滤。

-- ---------- 账号 ----------
ALTER USER root PASS 'HisTsdb@2026';

-- ---------- 库 ----------
-- iot_raw：原始点位，保留 90 天（查询"最近 24h 秒级曲线"用这里）
CREATE DATABASE IF NOT EXISTS iot_raw KEEP 90 DURATION 10 PRECISION 'ms' WAL_LEVEL 1;
-- iot_agg：5 分钟聚合，保留 730 天（趋势与月度对比用这里）
CREATE DATABASE IF NOT EXISTS iot_agg KEEP 730 DURATION 30 PRECISION 'ms' WAL_LEVEL 1;

-- ---------- 原始点位：按设备类型分超级表 ----------
-- 通过 REST 执行时 USE 不跨请求生效，下表名一律带库前缀

CREATE STABLE IF NOT EXISTS iot_raw.st_coldchain (
  ts TIMESTAMP,
  temp DOUBLE,
  humidity DOUBLE,
  door BOOL,
  power BOOL
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_infusion (
  ts TIMESTAMP,
  rate DOUBLE,
  remaining_ml DOUBLE,
  battery INT,
  occluded BOOL
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_asset (
  ts TIMESTAMP,
  area NCHAR(64),
  rssi INT,
  battery INT
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_env (
  ts TIMESTAMP,
  temp DOUBLE,
  humidity DOUBLE,
  pressure_pa DOUBLE,
  door BOOL,
  air_velocity DOUBLE
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_gas (
  ts TIMESTAMP,
  o2_kpa DOUBLE,
  vacuum_kpa DOUBLE,
  air_kpa DOUBLE,
  flow_lpm DOUBLE
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_bed (
  ts TIMESTAMP,
  in_bed BOOL,
  heart_rate INT,
  resp_rate INT,
  turns INT,
  body_movement DOUBLE
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_dialysis (
  ts TIMESTAMP,
  blood_flow_ml_min INT,
  dialysate_temp_c DOUBLE,
  conductivity_ms_cm DOUBLE,
  tmp_mmhg INT,
  elapsed_min INT,
  remaining_min INT,
  ultrafiltration_ml INT
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_cabinet (
  ts TIMESTAMP,
  temp DOUBLE,
  humidity DOUBLE,
  door BOOL,
  stock_pct INT
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_baby (
  ts TIMESTAMP,
  area NCHAR(64),
  rssi INT,
  battery INT,
  tamper BOOL
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_uv (
  ts TIMESTAMP,
  running BOOL,
  lamp_hours DOUBLE,
  filter_pct INT,
  occupied BOOL
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

CREATE STABLE IF NOT EXISTS iot_raw.st_robot (
  ts TIMESTAMP,
  task_state NCHAR(32),
  location NCHAR(64),
  battery INT,
  load_kg DOUBLE,
  trip_load_kg DOUBLE,
  box_count INT,
  boxes_onboard INT,
  priority NCHAR(16),
  odometer_m DOUBLE
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

-- ---------- 5 分钟聚合（跨类型共用一张） ----------
-- 聚合库表名同样带前缀

CREATE STABLE IF NOT EXISTS iot_agg.st_metric_5m (
  ts TIMESTAMP,
  temp DOUBLE,
  rate DOUBLE,
  battery INT,
  sample_count INT,
  metrics NCHAR(1024)
) TAGS (device_no NCHAR(64), device_type NCHAR(32), dept_id BIGINT);

