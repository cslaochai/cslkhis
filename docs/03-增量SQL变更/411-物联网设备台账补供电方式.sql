-- 411 物联网设备台账补「供电方式」
-- 背景：设备台账只有 device_type，看不出这台设备是插电的还是靠电池的；
--       护士看到"电量低"不知道该去换电池、还是该去查插座/停电。
-- 口径（与 cssimlab/docs/设备供配电方式.md 一致）：
--   mains           插电，断电即停/离线，运维动作是接回电源、报修
--   mains_backup    插电 + 后备电池（输液泵），插着电满电，拔电才用电池，接回即回充
--   battery         电池供电，靠现场换电池/换腕带恢复（资产定位信标、婴儿腕带）
--   battery_dock    电池供电，靠回充电桩充电（物流机器人）
-- 回滚：见 411-回滚.sql

ALTER TABLE biz_iot_device
    ADD COLUMN power_type VARCHAR(20) NOT NULL DEFAULT 'mains'
        COMMENT '供电方式：mains 插电 / mains_backup 插电+后备电池 / battery 电池·换电 / battery_dock 电池·回桩充电'
        AFTER device_type;

UPDATE biz_iot_device SET power_type = 'mains_backup' WHERE device_type = 'infusion';
UPDATE biz_iot_device SET power_type = 'battery'      WHERE device_type IN ('asset', 'baby');
UPDATE biz_iot_device SET power_type = 'battery_dock' WHERE device_type = 'robot';
UPDATE biz_iot_device SET power_type = 'mains'
 WHERE device_type IN ('coldchain', 'env', 'gas', 'bed', 'dialysis', 'cabinet', 'uv', 'monitor');

-- 校验
-- SELECT device_type, power_type, COUNT(*) FROM biz_iot_device WHERE del_flag = 0 GROUP BY device_type, power_type ORDER BY device_type;
