SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_revisit_fee_policy (id, policy_name, revisit_source, same_doctor, same_dept, within_days, charge_mode,
                                    priority, status, create_by, update_by, del_flag, remark, create_by_id,
                                    update_by_id)
VALUES ('1210000000000000001', '当日回诊免收', 1, 0, 0, 1, 3, 10, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_revisit_fee_policy (id, policy_name, revisit_source, same_doctor, same_dept, within_days, charge_mode,
                                    priority, status, create_by, update_by, del_flag, remark, create_by_id,
                                    update_by_id)
VALUES ('2103851709045452801', 'RVFY 自助复诊同医生免挂号费', 3, 1, 0, 13, 2, 50, 1, 'admin', 'admin', 1, NULL, '1',
        '1');
INSERT INTO biz_revisit_fee_policy (id, policy_name, revisit_source, same_doctor, same_dept, within_days, charge_mode,
                                    priority, status, create_by, update_by, del_flag, remark, create_by_id,
                                    update_by_id)
VALUES ('2103851709557157890', 'RVFY 自助复诊同医生免挂号费', 3, 1, 0, 13, 2, 50, 1, 'admin', 'admin', 1, NULL, '1',
        '1');
INSERT INTO biz_revisit_fee_policy (id, policy_name, revisit_source, same_doctor, same_dept, within_days, charge_mode,
                                    priority, status, create_by, update_by, del_flag, remark, create_by_id,
                                    update_by_id)
VALUES ('2360000000000012001', '随访计划复诊同科室免挂号费', 4, 0, 1, 30, 2, 60, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_revisit_fee_policy (id, policy_name, revisit_source, same_doctor, same_dept, within_days, charge_mode,
                                    priority, status, create_by, update_by, del_flag, remark, create_by_id,
                                    update_by_id)
VALUES ('2360000000000012002', '自助复诊不同医生全额收费', 3, 2, 0, 14, 1, 90, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
