-- 376 资产定位历史告警归档（联调噪声忽略留痕）
-- 背景：设备平台旧版资产造数把 12 个信标每台每小时随机跳区（asset-move-per-hour=3），
--       1.5 小时累计 28 条 OUT_OF_AREA，全部压在未处理队列，且授权区与设备实际科室不一致。
-- 现版本已按设备实际科室重设授权区（住院楼3F/4F/5F/6F、急诊楼1F），移动频率降为 0.3 次/小时/台，
--       信标电量掉电速度改为约 1.1%/天。此前事件统一忽略留痕，不做物理删除。
-- 截止时间 18:46:00 = 新版设备平台重启时刻。

UPDATE biz_iot_event
SET handled_flag = 2,
    handle_by = '设备与供应室管理员',
    handle_time = NOW(),
    handle_remark = '设备平台联调期旧授权区产生的位置告警，信标已按设备实际科室归位，统一忽略'
WHERE device_type = 'asset'
  AND handled_flag = 0
  AND event_code = 'OUT_OF_AREA'
  AND ts < '2026-11-25 18:46:00';
