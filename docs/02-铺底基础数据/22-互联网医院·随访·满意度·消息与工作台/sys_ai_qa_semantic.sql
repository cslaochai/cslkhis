-- sys_ai_qa_semantic  铺底数据：17 行（按主键序 自 dev 库 2026-10-23 导出）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
INSERT INTO `sys_ai_qa_semantic` (`id`, `term`, `table_name`, `column_name`, `scope_desc`, `match_mode`, `priority`, `is_enabled`, `create_by`, `create_by_id`, `create_time`, `update_by`, `update_by_id`, `update_time`, `del_flag`, `remark`)
VALUES
(2450000000000000501, '收入', 'biz_settlement_bill', NULL, '账单收入口径：统计已支付账单（bill_status=3）的 paid_amount（已收合计）；统筹走 pool_amount、个账走 account_amount、自付走 self_amount，三者分列不要混用。', 1, 10, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL),
(2450000000000000502, '净收款', 'biz_payment_txn', NULL, '净收款口径：SUM(amount)，收款为正、退款为负，仅统计 txn_status=1（成功）的流水。', 1, 10, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL),
(2450000000000000503, '门诊就诊量', 'biz_visit', NULL, '门诊就诊量口径：COUNT(*) 且 visit_status=2（已完成），按 start_time 所在日期归属。注意：biz_visit 表没有科室列，不支持按科室维度分组；科室维度统计须走挂号表（当前不在白名单），不要臆造 join 列。', 1, 9, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 23:24:49', 0, NULL),
(2450000000000000504, '门诊人次', 'biz_visit', NULL, '门诊人次口径：COUNT(*) 且 visit_status=2（已完成），按 start_time 所在日期归属。注意：biz_visit 表没有科室列，不支持按科室维度分组；不要臆造 join 列。', 1, 9, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 23:24:49', 0, NULL),
(2450000000000000505, '在院', 'biz_admission', NULL, '在院人数口径：COUNT(*) 且 admit_status=1（在院）。按病区分组用 ward_id JOIN sys_ward.ward_id；按科室分组用 admit_dept_id JOIN sys_department.id（字典主键是 id，dept_code 是编码，禁止用于关联）。', 1, 9, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 23:24:49', 0, NULL),
(2450000000000000506, '入院', 'biz_admission', NULL, '入院人数口径：按 admit_time 所在日期 COUNT(*)。按科室分组用 admit_dept_id JOIN sys_department.id；按病区分组用 ward_id JOIN sys_ward.ward_id（dept_code 是编码，禁止用于关联）。', 1, 9, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 23:24:49', 0, NULL),
(2450000000000000507, '出院', 'biz_admission', NULL, '出院人数口径：按 discharge_time 所在日期 COUNT(*)，admit_status=0（已出院）。按科室分组用 admit_dept_id JOIN sys_department.id；按病区分组用 ward_id JOIN sys_ward.ward_id（dept_code 是编码，禁止用于关联）。', 1, 9, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 23:24:49', 0, NULL),
(2450000000000000508, '床位', 'sys_ward', NULL, '床位口径：总床位数 total_beds、已占床 occupied_beds；占用率=occupied_beds/total_beds。病区维度主键是 sys_ward.ward_id，业务表按各自 ward_id 与之关联。', 1, 8, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 23:24:49', 0, NULL),
(2450000000000000509, '发药', 'biz_prescription', NULL, '发药处方口径：prescription_status=4（已发药）；处方金额用 total_amount，处方量 COUNT(*)。', 1, 9, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL),
(2450000000000000510, '药品销售', 'biz_prescription_detail', NULL, '药品销售口径：SUM(amount)，明细排除 detail_status=3（已退药）。', 1, 8, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL),
(2450000000000000511, '费用', 'biz_fee_record', NULL, '费用口径：SUM(amount)，必须排除 fee_status=4（已红冲）；门诊/住院用 encounter_type（1-门诊 2-住院）区分。', 1, 9, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL),
(2450000000000000512, '统筹', 'biz_settlement_bill', NULL, '医保统筹口径：SUM(pool_amount)，仅医保结算（settlement_mode=2）的账单。', 1, 8, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL),
(2450000000000000513, '支付方式', 'biz_payment_txn', NULL, '支付方式口径：按 pay_method（1-现金 2-微信 3-支付宝 4-医保个人账户 5-院内余额 6-银行卡 7-转账）分组 SUM(amount)，仅 txn_status=1。', 1, 8, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL),
(2450000000000000514, '检验', 'biz_laboratory_record', NULL, '检验量口径：COUNT(*)，排除 record_status=8（已取消）；检验费用 SUM(price)。', 1, 8, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL),
(2450000000000000515, '手术', 'biz_operation_apply', NULL, '手术量口径：COUNT(*)，排除 operation_status=4（已取消）；级别按 operation_level（1-一级 2-二级 3-三级 4-四级）。', 1, 8, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL),
(2450000000000000516, '科室', 'sys_department', NULL, '科室字典：维度表。主键 id，dept_code 为编码（唯一但禁止用于业务关联）；业务表按各自 *_dept_id（如 admit_dept_id、apply_dept_id、laboratory_dept_id）JOIN sys_department.id。dept_type（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他）用于筛选科室类型。', 1, 5, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 23:24:49', 0, NULL),
(2450000000000000517, '处方量', 'biz_prescription', NULL, '处方量口径：COUNT(*)，排除 prescription_status=5（已取消）与 6（已退药）。', 1, 8, 1, 'admin', 1, '2026-10-12 22:56:27', 'admin', 1, '2026-10-12 22:56:27', 0, NULL);
COMMIT;
