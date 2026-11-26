-- 369 医用气体历史告警归档（联调噪声，忽略留痕，不做删除）
-- 背景：医用气体页面 2026-11-25 晚联调时，设备平台按 fault-scale=60 加速造故障，
--       6 个终端在 1.5 小时内累计 67 条 PRESSURE_LOW / REGULATOR_FAULT，全部压在未处理队列里。
--       现场压力实测正常（450/-55/460 kPa），这些属于联调加速产生的历史事件，不是真实故障。
-- 处理：handled_flag 置 2（忽略），保留原始事件与备注，审计可查；不做物理删除。
-- 同时设备平台已把气体故障率降为 0.5 次/小时/点位，并改为迟滞阈值，后续不再刷屏；
-- 截止时间取 18:23:00（新版本设备平台重启时刻），此前的加速事件一并归档。

UPDATE biz_iot_event
SET handled_flag = 2,
    handle_by = '系统管理员',
    handle_time = NOW(),
    handle_remark = '设备平台联调期加速造故障产生的历史告警，现场压力复核正常，统一忽略'
WHERE device_type = 'gas'
  AND handled_flag = 0
  AND event_code IN ('PRESSURE_LOW', 'REGULATOR_FAULT')
  AND ts < '2026-11-25 18:23:00';
