-- biz_arrears_policy  铺底数据：1 行（按主键序 自 dev 库 2026-10-23 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `biz_arrears_policy` (`id`, `warn_line`, `stop_line`, `stop_enabled`, `stop_classes`, `remark`, `update_by`, `update_time`, `create_by`, `create_time`, `create_by_id`, `update_by_id`)
VALUES
(1, '1000.00', '3000.00', 0, '2,3,4', NULL, 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, 1);
COMMIT;
