-- biz_opd_station  铺底数据：5 行（按主键序 自 dev 库 2026-10-23 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `biz_opd_station` (`id`, `station_code`, `station_name`, `station_kind`, `required_staff_type`, `shift_id`, `capacity`, `phone`, `sort_no`, `status`, `create_by`, `create_by_id`, `create_time`, `update_by`, `update_by_id`, `update_time`, `del_flag`, `remark`)
VALUES
(2770000000000000101, 'OP-GUIDE', '门诊总导诊台', 1, 2, NULL, 1, NULL, 10, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '门诊大厅总导诊/咨询'),
(2770000000000000102, 'OP-TRIAGE', '门诊分诊台', 2, 2, NULL, 1, NULL, 20, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '挂号后分诊到科室候诊'),
(2770000000000000103, 'OP-INFUSION', '门诊输液室', 3, 2, NULL, 2, NULL, 30, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '静脉输液在岗护理'),
(2770000000000000104, 'OP-INJECT', '门诊注射室', 4, 2, NULL, 1, NULL, 40, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '肌肉/皮下注射在岗护理'),
(2770000000000000105, 'OP-BLOOD', '门诊采血室', 5, 2, NULL, 1, NULL, 50, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '静脉采血在岗护理');
COMMIT;
