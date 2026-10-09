SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_drug_package (id, doctor_id, package_name, package_type, create_by, update_by, del_flag, remark,
                              create_by_id, update_by_id)
VALUES ('2099290933110611970', '2098255864065458177', '脑梗专用套餐', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_package (id, doctor_id, package_name, package_type, create_by, update_by, del_flag, remark,
                              create_by_id, update_by_id)
VALUES ('2350000000000000401', '9301', '门诊静脉输液套餐', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_package (id, doctor_id, package_name, package_type, create_by, update_by, del_flag, remark,
                              create_by_id, update_by_id)
VALUES ('2350000000000000402', '897001001', '雾化吸入套餐', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_package (id, doctor_id, package_name, package_type, create_by, update_by, del_flag, remark,
                              create_by_id, update_by_id)
VALUES ('2350000000000000403', '897001001', '急性咽扁桃体炎口服套餐', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_package (id, doctor_id, package_name, package_type, create_by, update_by, del_flag, remark,
                              create_by_id, update_by_id)
VALUES ('2350000000000000404', '897001008', '消化性溃疡四联疗法套餐', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_drug_package (id, doctor_id, package_name, package_type, create_by, update_by, del_flag, remark,
                              create_by_id, update_by_id)
VALUES ('2350000000000000405', '897001015', '高血压起始联合用药套餐', 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
