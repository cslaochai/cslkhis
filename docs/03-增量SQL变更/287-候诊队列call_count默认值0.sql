-- 287-候诊队列call_count默认值0
-- 背景：biz_queue.call_count 列无默认值，签到建队（QueueServiceImpl.checkInByRegist）
--       漏 set 时被 MySQL 拒收，报 "Field 'call_count' doesn't have a default value"，
--       500 落在分诊台「签到」动作上。急诊建队已显式 setCallCount(0)。
-- 修复：代码侧签到建队补 setCallCount(0)（与新队列"还没叫过号=0"语义一致）；
--       此处给列加 DEFAULT 0 做库层兜底，已存在行不受影响，后续 UPDATE 也不会重置。
ALTER TABLE `biz_queue`
    MODIFY COLUMN `call_count` int NOT NULL DEFAULT 0 COMMENT '叫号次数';
