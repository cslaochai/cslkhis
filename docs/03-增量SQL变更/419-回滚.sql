-- ============================================================
-- 419 回滚：撤掉床旁监护仪接入
-- 日期：2026-11-27
-- 说明：删除 6 台 monitor 台账行；TDengine 超级表 st_monitor 一并 DROP（历史点位随之清除）。
--       波形只在内存缓冲，服务重启即清空，无需额外清理。
-- ============================================================
SET NAMES utf8mb4;

DELETE FROM `biz_iot_device`
WHERE `device_type` = 'monitor' AND `device_no` IN ('MON-01','MON-02','MON-03','MON-04','MON-05','MON-06');

-- TDengine 侧（在 TDengine 执行，非 MySQL）：
-- DROP STABLE IF EXISTS iot_raw.st_monitor;
