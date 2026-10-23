-- biz_diag_template  铺底数据：4 行（按主键序 自 dev 库 2026-10-23 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `biz_diag_template` (`id`, `doctor_id`, `icd_code`, `icd_name`, `sort_order`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `remark`, `create_by_id`, `update_by_id`)
VALUES
(2099349085923319810, 2098255864065458177, 'I10', '原发性高血压', 0, 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 0, NULL, 1, 1),
(2099349085923319811, 2098255864065458177, 'I21.9', '急性心肌梗死', 1, 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 0, NULL, 1, 1),
(2099349085923319812, 2098255864065458177, 'C34.9', '肺癌', 2, 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 0, NULL, 1, 1),
(2099349085923319813, 2098255864065458177, 'I50.9', '心力衰竭', 3, 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 0, NULL, 1, 1);
COMMIT;
