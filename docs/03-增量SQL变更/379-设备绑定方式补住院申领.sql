-- 379 绑定方式补「住院申领」【已被 381 覆盖，留存历史】
-- 绑定动作从监控大屏迁到「住院管理 · 床旁设备申领」后，来源需要能和设备科现场扫码区分开。
-- 380 改共享调配后，3 的含义改为「共享调配」，381 已把列注释同步为最新口径。
ALTER TABLE biz_iot_device_bind
    MODIFY COLUMN bind_source TINYINT NOT NULL DEFAULT 1 COMMENT '绑定方式（1-扫码 2-手工 3-住院申领）';

-- 历史门诊/住院绑定行不追溯改码值：1/2 的含义保持不变。
