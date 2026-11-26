-- 381 设备绑定来源文案同步：3 从「住院申领」改为「共享调配」
-- 背景：380 已把设备借用归位到设备科共享调配；绑定来源 3 应表达“共享调配发放”，
--       避免监控页/绑定清单继续显示“申领”，与新流程不一致。

ALTER TABLE biz_iot_device_bind
    MODIFY COLUMN bind_source TINYINT NOT NULL DEFAULT 1 COMMENT '绑定来源（1-扫码 2-手工 3-共享调配）';
