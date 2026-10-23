-- biz_survey_template  铺底数据：5 行（按主键序 自 dev 库 2026-10-23 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `biz_survey_template` (`id`, `template_no`, `template_name`, `scene`, `status`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `remark`, `create_by_id`, `update_by_id`)
VALUES
(895000000000000901, 'ST-SEED-IPD-FU', '住院患者出院随访满意度问卷', 1, 1, '出院后电话随访回收，百分制上报口径；维度短板自动转服务投诉', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 0, NULL, 1, 1),
(2104421452559204353, 'ST202609280008', '验证-门诊满意度短卷', 2, 1, 'verify-164 自动创建', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, NULL, 1, 1),
(2104422460689530881, 'ST202609280011', '验证-门诊满意度短卷', 2, 1, 'verify-164 自动创建', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, NULL, 1, 1),
(2104423852053491714, 'ST202609280014', '验证-门诊满意度短卷', 2, 1, 'verify-164 自动创建', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, NULL, 1, 1),
(2104424944040525825, 'ST202609280017', '验证-门诊满意度短卷', 2, 1, 'verify-164 自动创建', 'admin', '2026-05-08 00:00:00', 'admin', '2026-05-08 00:00:00', 1, NULL, 1, 1);
COMMIT;
