-- 426 历史遗留清理：退役空表 biz_prepay + 机器人僵尸派单
-- 背景：AGENTS §9 已声明 biz_prepay 等旧收费表退役（sql/126），其余 4 张已不存在；
--      本表仍在库中但 0 行、后端/前端 0 引用，属纯遗留空壳，直接 DROP。
-- 另：仿真重启导致 7 张派单停在 2-已派单/3-配送中，而对应机器车遥测已 IDLE，
--      这些单永久占着 busyRobots（pickFreeRobot 会一直排除该车），按“执行态且派单超 5 分钟”清理。
-- 回滚：biz_prepay 建表语句见 docs/10-ER关系图/powerdesigner/by-domain/19-charge-four-layer.sql:540；
--      被删单据为仿真测试数据，不保留备份价值（如需复现重新建单即可）。

-- 1) 退役空表
DROP TABLE IF EXISTS `biz_prepay`;

-- 2) 僵尸派单（执行态且派单时间早于 5 分钟前）
DELETE FROM `biz_robot_dispatch_order`
WHERE `status` IN (2, 3)
  AND `dispatch_time` IS NOT NULL
  AND `dispatch_time` < DATE_SUB(NOW(), INTERVAL 5 MINUTE);
