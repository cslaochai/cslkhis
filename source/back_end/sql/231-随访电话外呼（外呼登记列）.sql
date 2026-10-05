-- 231: 随访电话外呼登记列（P5-2，G-15）
-- 任务表加最小外呼闭环四列：通道、状态、最近外呼时间、累计外呼次数。
-- 外呼不另建单据表：登记/接通/未接通都是任务自身的事实，单开表等于把一句话拆两处存。

ALTER TABLE `biz_followup_task`
    ADD COLUMN `call_channel` TINYINT NULL COMMENT '外呼通道（1-人工 2-自动）' AFTER `patient_reply_time`,
    ADD COLUMN `call_status` TINYINT NOT NULL DEFAULT 0 COMMENT '外呼状态（0-未外呼 1-待外呼 2-已接通 3-未接通）' AFTER `call_channel`,
    ADD COLUMN `call_time` DATETIME NULL COMMENT '最近一次外呼登记时间' AFTER `call_status`,
    ADD COLUMN `call_attempts` INT NOT NULL DEFAULT 0 COMMENT '累计外呼登记次数' AFTER `call_time`;
