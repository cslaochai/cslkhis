SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2103871750117908481', 2, '8950000000000075201', '8900000000000900099', 'PWXDEMO001', '微信演示患者', '500.00',
        '0', '500.00', '0.00', 1, '2026-09-26 23:38:08', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2104492187864657922', 2, '8920092500000000401', '8900000000000900099', 'PWXDEMO001', '微信演示患者', '66.66',
        '1', '66.66', '0.00', 1, '2026-09-28 16:43:32', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2108026695318515714', 2, '2108026692843876354', '2104150908102373377', 'P2026092700008',
        '字段变更探针03644255改', '500.00', '1', '500.00', '0.00', 1, '2026-10-08 10:48:24', 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2108027806179930115', 2, '2108027803285860353', '2104148440035360770', 'P2026092700006',
        '字段变更探针03055930改', '500.00', '1', '500.00', '0.00', 1, '2026-10-08 10:52:49', 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2108028903036563458', 2, '2108028900255739906', '2104148166432522242', 'P2026092700005',
        '字段变更探针02990695改', '500.00', '1', '500.00', '0.00', 1, '2026-10-08 10:57:10', 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2108029204284059649', 2, '2108029201440321539', '2104147887607775234', 'P2026092700004',
        '字段变更探针02924023', '500.00', '1', '500.00', '0.00', 1, '2026-10-08 10:58:22', 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2108030335374602243', 2, '2108030332442783747', '8450000000000000303', 'BC20260927003', '等床验证丙', '500.00',
        '1', '500.00', '0.00', 1, '2026-10-08 11:02:52', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2108030636693401603', 2, '2108030633962909698', '8450000000000000301', 'BC20260927001', '等床验证甲', '500.00',
        '1', '500.00', '0.00', 1, '2026-10-08 11:04:04', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2108031000385695747', 2, '2108030997231579139', '2101714574737715202', 'P2026092100001', 'E2E建档患者881',
        '0.00', '2', '500.00', '500.00', 1, '2026-10-08 11:05:31', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_fund_account (id, owner_type, owner_id, patient_id, patient_no, patient_name, balance, version,
                              total_recharge, total_consume, account_status, last_txn_time, create_by, update_by,
                              del_flag, remark, create_by_id, update_by_id)
VALUES ('2108031001484603393', 1, '2101714574737715202', '2101714574737715202', 'P2026092100001', 'E2E建档患者881',
        '500.00', '1', '500.00', '0.00', 1, '2026-10-08 11:05:31', 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
