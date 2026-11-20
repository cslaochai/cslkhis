-- sys_flow_node  铺底数据：10 行（按主键序 自 dev 库 2026-11-20 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `sys_flow_node` (`id`, `definition_id`, `node_code`, `node_name`, `node_type`, `permission_flag`, `node_ratio`, `ai_action`, `ai_capability_key`, `ai_template_name`, `ai_input_json`, `coordinate`, `sort_no`)
VALUES
(296431000000000001, 296430000000000001, 'START', '开始', 0, NULL, NULL, NULL, NULL, NULL, NULL, '0,0', 1),
(296431000000000002, 296430000000000001, 'ROUTE', '意图识别', 4, NULL, NULL, 'ROUTE', 'flow_intent_route', 'flow-intent-route', NULL, '200,0', 2),
(296431000000000003, 296430000000000001, 'ANSWER_REPORT', '报告解读回答', 3, NULL, NULL, 'CAPABILITY', 'flow_ai_node', 'flow-answer-report', NULL, '400,0', 3),
(296431000000000004, 296430000000000001, 'ANSWER_REGISTRATION', '挂号指引回答', 3, NULL, NULL, 'CAPABILITY', 'flow_ai_node', 'flow-answer-registration', NULL, '400,100', 4),
(296431000000000005, 296430000000000001, 'ANSWER_GENERAL', '通用回答', 3, NULL, NULL, 'CAPABILITY', 'flow_ai_node', 'flow-answer-general', NULL, '400,200', 5),
(296431000000000006, 296430000000000001, 'END', '结束', 2, NULL, NULL, NULL, NULL, NULL, NULL, '600,100', 6),
(296431000000000011, 296430000000000002, 'START', '开始', 0, NULL, NULL, NULL, NULL, NULL, NULL, '0,0', 1),
(296431000000000012, 296430000000000002, 'AI_CHECK', '内涵预检', 3, NULL, NULL, 'CAPABILITY', 'flow_ai_node', 'flow-qc-inner-check', NULL, '200,0', 2),
(296431000000000013, 296430000000000002, 'REVIEW', '科室质控员确认', 1, 'role:DOCTOR', '0', NULL, NULL, NULL, NULL, '400,0', 3),
(296431000000000014, 296430000000000002, 'END', '结束', 2, NULL, NULL, NULL, NULL, NULL, NULL, '600,0', 4);
COMMIT;
