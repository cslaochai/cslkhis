-- sys_flow_definition  铺底数据：2 行（按主键序 自 dev 库 2026-11-20 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `sys_flow_definition` (`id`, `flow_code`, `flow_name`, `flow_desc`, `status`, `version`, `model_json`, `create_by`, `create_by_id`, `create_time`, `update_by`, `update_by_id`, `update_time`)
VALUES
(296430000000000001, 'PATIENT_CONSULT', '患者咨询分流', '患者在小程序里问健康/就医问题的通用入口。凡是问“怎么办/挂什么科/报告怎么看”且没有具体业务单据的，都走这条。', 1, 1, '{"nodes":["START","ROUTE","ANSWER_REPORT","ANSWER_REGISTRATION","ANSWER_GENERAL","END"]}', 'sql328', 1, '2026-11-14 17:33:17', 'sql328', 1, '2026-11-14 17:33:17'),
(296430000000000002, 'EMR_QC_REVIEW', '病历内涵复核', '规则质控命中或病历提交前的内涵复核：AI 先出问题清单，再由科室质控员确认整改。只提示不计分。', 1, 1, '{"nodes":["START","AI_CHECK","REVIEW","END"]}', 'sql328', 1, '2026-11-14 17:33:17', 'sql328', 1, '2026-11-14 17:33:17');
COMMIT;
