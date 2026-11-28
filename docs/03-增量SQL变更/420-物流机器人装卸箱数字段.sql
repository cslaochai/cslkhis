-- 物流机器人：把"载重"从一个总数细化为装卸过程可见的一组字段。
--
-- 背景：原来看板只有一个 load_kg。设备侧一单货一次性赋上去，曲线就是 0 突然跳到 10kg；
-- 现场是药房一箱一箱扫码装车、护士站一箱一箱核对签收，载重应当阶梯上升、阶梯回落，
-- 途中恒定。要做到"看得出装卸到第几箱"，必须把本单总重和箱数一起落库。
--
--   trip_load_kg  本单货物总重（装齐后的重量）
--   box_count     本单箱数
--   boxes_onboard 此刻在车上的箱数（装货递增、签收递减）
--   priority      任务优先级：急送 / 常规 / 批量补货
--
-- 适用：TDengine 3.x，iot_raw 库。已执行过 356 建库建表的现场，按本文件补列即可。
-- 新装环境直接用 356 里的完整建表语句（已同步补齐这四列）。

ALTER STABLE iot_raw.st_robot ADD COLUMN trip_load_kg DOUBLE;
ALTER STABLE iot_raw.st_robot ADD COLUMN box_count INT;
ALTER STABLE iot_raw.st_robot ADD COLUMN boxes_onboard INT;
ALTER STABLE iot_raw.st_robot ADD COLUMN priority NCHAR(16);

-- 回滚（列一旦加上不能删，只能忽略不用）：
-- 把设备侧报文里的 tripLoadKg / boxCount / boxesOnboard / priority 去掉，
-- 并把 DeviceTypeSchema 的 robot 注册表恢复为原五项即可；历史列保留不影响查询。
