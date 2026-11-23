-- biz_opd_station  铺底数据：9 行（前 5 行自 dev 库 2026-11-20 导出，后 4 行为 353 号增量新增的门诊西药房取药窗口）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `biz_opd_station` (`id`, `station_code`, `station_name`, `station_kind`, `required_staff_type`, `shift_id`, `capacity`, `phone`, `sort_no`, `status`, `create_by`, `create_by_id`, `create_time`, `update_by`, `update_by_id`, `update_time`, `del_flag`, `remark`)
VALUES
(2770000000000000101, 'OP-GUIDE', '门诊总导诊台', 1, 2, NULL, 1, NULL, 10, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '门诊大厅总导诊/咨询'),
(2770000000000000102, 'OP-TRIAGE', '门诊分诊台', 2, 2, NULL, 1, NULL, 20, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '挂号后分诊到科室候诊'),
(2770000000000000103, 'OP-INFUSION', '门诊输液室', 3, 2, NULL, 2, NULL, 30, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '静脉输液在岗护理'),
(2770000000000000104, 'OP-INJECT', '门诊注射室', 4, 2, NULL, 1, NULL, 40, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '肌肉/皮下注射在岗护理'),
(2770000000000000105, 'OP-BLOOD', '门诊采血室', 5, 2, NULL, 1, NULL, 50, 1, 'admin', NULL, '2026-10-19 12:46:39', 'admin', NULL, '2026-10-19 12:46:39', 0, '静脉采血在岗护理'),
(3530000000000000101, 'OP-DRUG-1', '门诊西药房1号取药窗口', 6, 4, NULL, 1, NULL, 61, 1, 'admin', NULL, '2026-11-23 12:00:00', 'admin', NULL, '2026-11-23 12:00:00', 0, '患者刷证取药叫号'),
(3530000000000000102, 'OP-DRUG-2', '门诊西药房2号取药窗口', 6, 4, NULL, 1, NULL, 62, 1, 'admin', NULL, '2026-11-23 12:00:00', 'admin', NULL, '2026-11-23 12:00:00', 0, '患者刷证取药叫号'),
(3530000000000000103, 'OP-DRUG-3', '门诊西药房3号取药窗口', 6, 4, NULL, 1, NULL, 63, 1, 'admin', NULL, '2026-11-23 12:00:00', 'admin', NULL, '2026-11-23 12:00:00', 0, '患者刷证取药叫号'),
(3530000000000000104, 'OP-DRUG-4', '门诊西药房4号取药窗口', 6, 4, NULL, 1, NULL, 64, 1, 'admin', NULL, '2026-11-23 12:00:00', 'admin', NULL, '2026-11-23 12:00:00', 0, '患者刷证取药叫号');
COMMIT;
