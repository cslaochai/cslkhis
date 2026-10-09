SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2098977881689427969', '2098255864065458177', '复方甘草x10', 1, '150.00', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2350000000000000001', '897001015', '高血压门诊随访用药（施佳明）', 2, '90.00', 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2350000000000000002', '897001029', '2型糖尿病规范用药（杨远航）', 2, '87.60', 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2350000000000000003', '897001001', '社区获得性肺炎经验用药（沈楠）', 3, '44.70', 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2350000000000000004', '897001008', '慢性胃炎标准用药（温昊天）', 3, '64.60', 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2350000000000000005', '9301', '急性上呼吸道感染用药（周远）', 3, '34.10', 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2350000000000000006', '897001043', '缺铁性贫血补铁方案（吕明德）', 3, '39.00', 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2350000000000000007', '897001016', '稳定型心绞痛二级预防（莫远航）', 3, '117.40', 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, update_by, del_flag,
                             remark, create_by_id, update_by_id)
VALUES ('2350000000000000008', '897001036', '急性膀胱炎抗感染方案（郭怀瑾）', 2, '36.40', 'admin', 'admin', 0, NULL, '1',
        '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
