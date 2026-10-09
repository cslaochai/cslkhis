SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('1', '城镇职工医保-在职', '在职职工', 2, '85.00', '10.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('2', '城镇职工医保-退休', '退休职工', 2, '92.00', '5.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('3', '城乡居民医保-成人', '城乡居民', 3, '70.00', '15.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('4', '城乡居民医保-学生', '学生儿童', 3, '80.00', '10.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('5', '城乡居民医保-老人', '老年居民', 3, '75.00', '12.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('6', '公费医疗', '公费医疗', 4, '100.00', '0.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('7', '城乡居民医保-成年居民', '城乡居民', 3, '65.00', '15.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('8', '城乡居民医保-学生儿童', '城乡居民', 3, '75.00', '10.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('9', '城乡居民医保-大病保险', '城乡居民', 3, '70.00', '10.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('10', '城镇职工医保-门诊统筹', '在职职工', 2, '60.00', '10.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('11', '城镇职工医保-住院统筹', '在职职工', 2, '88.00', '10.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('12', '公费医疗-在职', '公费医疗', 4, '90.00', '5.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('13', '公费医疗-离退休', '公费医疗', 4, '95.00', '5.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('14', '异地就医-职工直接结算', '在职职工', 2, '80.00', '10.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('15', '异地就医-居民直接结算', '城乡居民', 3, '58.00', '15.00', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_insurance_policy (id, policy_name, insurance_type, settlement_type, coverage_ratio, self_pay_ratio,
                                  status, remark, create_by, update_by, create_by_id, update_by_id)
VALUES ('16', '生育保险-产前检查', '在职职工', 2, '85.00', '0.00', 1, NULL, 'admin', 'admin', '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
