-- 287-候诊队列签到省略列默认值
-- 背景：分诊台签到（QueueServiceImpl.checkInByRegist）建 biz_queue 行时，
--       MyBatis-Plus 默认跳过 null 字段，生成的 INSERT 省略 call_count 等列；
--       本实例对「无 DEFAULT 子句」的列在严格模式下省略即报
--       "Field 'xxx' doesn't have a default value"，签到动作直接 500。
-- 修复：代码侧签到建队补 setCallCount(0)（新队列「还没叫过号=0」，与急诊建队同口径）；
--       此处给签到 INSERT 省略的所有无默认列补默认值做库层兜底：
--       call_count/wait_duration 初始 0、is_overdue 初始 0（0-否），
--       overdue_time/overdue_reason/remark 允许 NULL 并显式 DEFAULT NULL。
-- 已存在行不受影响（is_overdue 的 NULL 行先置 0）。
ALTER TABLE `biz_queue` MODIFY COLUMN `call_count` int NOT NULL DEFAULT 0 COMMENT '叫号次数';
ALTER TABLE `biz_queue` MODIFY COLUMN `wait_duration` int NOT NULL DEFAULT 0 COMMENT '等待时长（分钟）';
UPDATE `biz_queue` SET `is_overdue` = 0 WHERE `is_overdue` IS NULL;
ALTER TABLE `biz_queue` MODIFY COLUMN `is_overdue` tinyint NOT NULL DEFAULT 0 COMMENT '是否过号（0-否 1-是）';
ALTER TABLE `biz_queue` MODIFY COLUMN `overdue_time` datetime DEFAULT NULL COMMENT '过号时间';
ALTER TABLE `biz_queue` MODIFY COLUMN `overdue_reason` varchar(200) DEFAULT NULL COMMENT '过号原因';
ALTER TABLE `biz_queue` MODIFY COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注';


ALTER TABLE `biz_prescription`
    ADD COLUMN `doctor_signature` mediumtext COMMENT '开方医师手写笔迹（PNG dataURL，视觉落款，验签以电子证书签名为准）' AFTER `doctor_signed_time`;