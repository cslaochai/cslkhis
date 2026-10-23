-- biz_dip_aux  铺底数据：4 行（按主键序 自 dev 库 2026-10-23 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `biz_dip_aux` (`id`, `disease_code`, `aux_type`, `aux_code`, `aux_name`, `age_min`, `age_max`, `coeff`, `source`, `version`, `status`, `create_by`, `create_by_id`, `create_time`, `update_by`, `update_by_id`, `update_time`, `del_flag`, `remark`)
VALUES
(2760000000000000201, NULL, 1, NULL, '70 岁及以上', 70, 150, '1.0500', '演示样例（非官方）', '2.0', 1, 'admin', 1, '2026-10-19 13:40:08', 'admin', 1, '2026-10-19 13:40:08', 0, '演示数据：高龄上浮'),
(2760000000000000202, NULL, 1, NULL, '6 岁以下', 0, 5, '1.1000', '演示样例（非官方）', '2.0', 1, 'admin', 1, '2026-10-19 13:40:08', 'admin', 1, '2026-10-19 13:40:08', 0, '演示数据：低龄上浮'),
(2760000000000000203, NULL, 2, 'E11.9', '2型糖尿病不伴并发症（合并症）', NULL, NULL, '1.1000', '演示样例（非官方）', '2.0', 1, 'admin', 1, '2026-10-19 13:40:08', 'admin', 1, '2026-10-19 13:40:08', 0, '演示数据：合并症系数'),
(2760000000000000204, 'DIP2026C509001', 3, 'C50.9', '乳房恶性肿瘤（肿瘤分级细分）', NULL, NULL, '1.2000', '演示样例（非官方）', '2.0', 1, 'admin', 1, '2026-10-19 13:40:08', 'admin', 1, '2026-10-19 13:40:08', 0, '演示数据：病种绑定肿瘤分级系数');
COMMIT;
