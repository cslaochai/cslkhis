SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('1', '1', 'WBC', '白细胞计数', '10^9/L', '4-10', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2', '1', 'RBC', '红细胞计数', '10^12/L', '男4-5.5/女3.5-5', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('3', '1', 'HGB', '血红蛋白', 'g/L', '男120-160/女110-150', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('4', '1', 'HCT', '红细胞压积', '%', '男40-50/女35-45', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('5', '1', 'PLT', '血小板计数', '10^9/L', '100-300', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('6', '1', 'LYM%', '淋巴细胞百分比', '%', '20-40', 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('7', '1', 'MONO%', '单核细胞百分比', '%', '3-8', 7, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8', '1', 'NEUT%', '中性粒细胞百分比', '%', '50-70', 8, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('9', '1', 'EO%', '嗜酸性粒细胞百分比', '%', '0.5-5', 9, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('10', '1', 'BA%', '嗜碱性粒细胞百分比', '%', '0-1', 10, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('11', '1', 'LYM#', '淋巴细胞绝对值', '10^9/L', '0.8-4', 11, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('12', '1', 'MONO#', '单核细胞绝对值', '10^9/L', '0.1-0.8', 12, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('13', '1', 'NEUT#', '中性粒细胞绝对值', '10^9/L', '2-7', 13, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('14', '1', 'EO#', '嗜酸性粒细胞绝对值', '10^9/L', '0.02-0.5', 14, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('15', '1', 'BA#', '嗜碱性粒细胞绝对值', '10^9/L', '0-0.1', 15, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('16', '4', 'ALT', '谷丙转氨酶', 'U/L', '0-40', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('17', '4', 'AST', '谷草转氨酶', 'U/L', '0-40', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('18', '4', 'TBIL', '总胆红素', 'umol/L', '3.4-17.1', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('19', '4', 'DBIL', '直接胆红素', 'umol/L', '0-6.8', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('20', '4', 'IBIL', '间接胆红素', 'umol/L', '1.7-10.2', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21', '4', 'TP', '总蛋白', 'g/L', '60-80', 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('22', '4', 'ALB', '白蛋白', 'g/L', '35-55', 7, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('23', '4', 'GLB', '球蛋白', 'g/L', '20-30', 8, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('24', '4', 'ALP', '碱性磷酸酶', 'U/L', '40-150', 9, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('25', '4', 'GGT', '谷氨酰转肽酶', 'U/L', '男11-50/女7-32', 10, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('26', '5', 'BUN', '尿素氮', 'mmol/L', '2.8-7.2', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('27', '5', 'CR', '肌酐', 'umol/L', '44-133', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('28', '5', 'UA', '尿酸', 'umol/L', '149-416', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300000', '200000', 'CHO', '总胆固醇', 'mmol/L', '<5.2', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300001', '200000', 'TG', '甘油三酯', 'mmol/L', '<1.7', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300002', '200000', 'HDL', '高密度脂蛋白', 'mmol/L', '>1.0', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300003', '200000', 'LDL', '低密度脂蛋白', 'mmol/L', '<3.4', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300004', '200001', 'ALT', '谷丙转氨酶', 'U/L', '<40', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300005', '200001', 'AST', '谷草转氨酶', 'U/L', '<40', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300006', '200001', 'GGT', '谷氨酰转肽酶', 'U/L', '<54', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300007', '200001', 'ALP', '碱性磷酸酶', 'U/L', '<135', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300008', '200001', 'TBIL', '总胆红素', 'umol/L', '3.4-21', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300009', '200001', 'DBIL', '直接胆红素', 'umol/L', '<6.8', 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300010', '200001', 'IBIL', '间接胆红素', 'umol/L', '<17', 7, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300011', '200001', 'TP', '总蛋白', 'g/L', '65-85', 8, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300012', '200001', 'ALB', '白蛋白', 'g/L', '40-55', 9, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300013', '200002', 'CREA', '肌酐', 'umol/L', '57-97(男)/41-73(女)', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300014', '200002', 'BUN', '尿素氮', 'mmol/L', '2.8-8.2', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300015', '200002', 'UA', '尿酸', 'umol/L', '<428', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300016', '200003', 'K', '钾', 'mmol/L', '3.5-5.3', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300017', '200003', 'NA', '钠', 'mmol/L', '137-147', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300018', '200003', 'CL', '氯', 'mmol/L', '99-110', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300019', '200003', 'CA', '钙', 'mmol/L', '2.1-2.6', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300020', '200003', 'P', '磷', 'mmol/L', '0.85-1.51', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300021', '200004', 'GLU', '空腹血糖', 'mmol/L', '3.9-6.1', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300022', '200005', 'HBA1C', '糖化血红蛋白', '%', '4-6', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300023', '200006', 'OGTT0', '空腹血糖', 'mmol/L', '3.9-6.1', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300024', '200006', 'OGTT2', '餐后2小时血糖', 'mmol/L', '<7.8', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300025', '200007', 'CPEP', 'C肽', 'ng/mL', '0.8-4.2', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300026', '200008', 'INS0', '空腹胰岛素', 'uIU/mL', '2.6-24.9', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300027', '200008', 'INS2', '餐后2小时胰岛素', 'uIU/mL', '-', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300028', '200009', 'CK', '肌酸激酶', 'U/L', '38-174', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300029', '200009', 'CKMB', '肌酸激酶同工酶', 'ng/mL', '<5', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300030', '200009', 'LDH', '乳酸脱氢酶', 'U/L', '120-250', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300031', '200009', 'AST', '谷草转氨酶', 'U/L', '<40', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300032', '200010', 'CTNI', '肌钙蛋白I', 'ng/mL', '<0.04', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300033', '200011', 'BNP', 'B型钠尿肽', 'pg/mL', '<100', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300034', '200012', 'HCY', '同型半胱氨酸', 'umol/L', '<15', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300035', '200013', 'TSH', '促甲状腺激素', 'mIU/L', '0.55-4.78', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300036', '200013', 'FT3', '游离三碘甲状腺原氨酸', 'pmol/L', '3.5-6.5', 2, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300037', '200013', 'FT4', '游离甲状腺素', 'pmol/L', '11.5-22.7', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300038', '200013', 'T3', '三碘甲状腺原氨酸', 'nmol/L', '1.3-3.1', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300039', '200013', 'T4', '甲状腺素', 'nmol/L', '66-181', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300040', '200014', 'TSH', '促甲状腺激素', 'mIU/L', '0.55-4.78', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300041', '200014', 'FT3', '游离T3', 'pmol/L', '3.5-6.5', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300042', '200014', 'FT4', '游离T4', 'pmol/L', '11.5-22.7', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300043', '200015', 'TPOAB', '抗甲状腺过氧化物酶抗体', 'IU/mL', '<60', 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300044', '200016', 'FSH', '卵泡刺激素', 'mIU/mL', '依周期', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300045', '200016', 'LH', '黄体生成素', 'mIU/mL', '依周期', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300046', '200016', 'E2', '雌二醇', 'pg/mL', '依周期', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300047', '200016', 'P', '孕酮', 'ng/mL', '依周期', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300048', '200016', 'T', '睾酮', 'ng/mL', '依性别', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300049', '200016', 'PRL', '泌乳素', 'ng/mL', '3.4-25', 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300050', '200017', 'BETAHCG', 'β-HCG', 'mIU/mL', '<5(非孕)', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300051', '200018', 'AFP', '甲胎蛋白', 'ng/mL', '<7', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300052', '200018', 'CEA', '癌胚抗原', 'ng/mL', '<5', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300053', '200018', 'CA125', '糖类抗原125', 'U/mL', '<35', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300054', '200018', 'CA199', '糖类抗原19-9', 'U/mL', '<37', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300055', '200018', 'CA153', '糖类抗原15-3', 'U/mL', '<31.3', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300056', '200018', 'CA724', '糖类抗原72-4', 'U/mL', '<6.9', 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300057', '200018', 'PSA', '前列腺特异抗原', 'ng/mL', '<4', 7, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300058', '200018', 'FPSA', '游离PSA', 'ng/mL', '<1', 8, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300059', '200018', 'NSE', '神经元特异性烯醇化酶', 'ng/mL', '<16.3', 9, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300060', '200018', 'CYFRA211', '细胞角蛋白19片段', 'ng/mL', '<3.3', 10, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300061', '200018', 'SCC', '鳞状细胞癌抗原', 'ng/mL', '<1.5', 11, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300062', '200018', 'FER', '铁蛋白', 'ng/mL', '男30-400/女13-150', 12, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300063', '200019', 'AFP', '甲胎蛋白', 'ng/mL', '<7', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300064', '200020', 'CEA', '癌胚抗原', 'ng/mL', '<5', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300065', '200021', 'PSA', '总PSA', 'ng/mL', '<4', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300066', '200021', 'FPSA', '游离PSA', 'ng/mL', '<1', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300067', '200022', 'PT', '凝血酶原时间', 's', '9.4-12.5', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300068', '200022', 'APTT', '活化部分凝血活酶时间', 's', '25-36.5', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300069', '200022', 'TT', '凝血酶时间', 's', '14-21', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300070', '200022', 'FIB', '纤维蛋白原', 'g/L', '2-4', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300071', '200022', 'DDIM', 'D-二聚体', 'ug/mL', '<0.5', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300072', '200023', 'ESR', '血沉', 'mm/h', '男<15 女<20', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300073', '200024', 'CRP', 'C反应蛋白', 'mg/L', '<10', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300074', '200025', 'HSCRP', '超敏C反应蛋白', 'mg/L', '<3', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300075', '200026', 'PCT', '降钙素原', 'ng/mL', '<0.05', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300076', '200027', 'IL6', '白细胞介素6', 'pg/mL', '<7', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300077', '200028', 'FER', '铁蛋白', 'ng/mL', '男30-400 女13-150', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300078', '200029', 'VB12', '维生素B12', 'pg/mL', '197-771', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300079', '200030', 'FOL', '叶酸', 'ng/mL', '3.1-20.5', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300080', '200031', 'VD', '25羟维生素D', 'ng/mL', '30-100', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300081', '200032', 'HBSAG', '乙肝表面抗原', 'IU/mL', '<0.05', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300082', '200032', 'HBSAB', '乙肝表面抗体', 'mIU/mL', '<10 阴性', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300083', '200032', 'HBEAG', '乙肝e抗原', 'COI', '<1 阴性', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300084', '200032', 'HBEAB', '乙肝e抗体', 'COI', '>1 阴性', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300085', '200032', 'HBCAB', '乙肝核心抗体', 'COI', '>1 阴性', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300086', '200033', 'HCVAB', '丙肝抗体', 'S/CO', '<1 阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300087', '200034', 'TPAB', '梅毒螺旋体抗体', 'S/CO', '<1 阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300088', '200035', 'HIVAB', 'HIV抗体', 'S/CO', '<1 阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300089', '200036', 'HPAB', '幽门螺杆菌抗体', 'U/mL', '<15 阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300090', '200037', 'HPAG', '幽门螺杆菌抗原', '-', '阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300091', '200038', 'RF', '类风湿因子', 'IU/mL', '<20', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300092', '200039', 'ASO', '抗链球菌溶血素O', 'IU/mL', '<200', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300093', '200040', 'ANA', '抗核抗体', '滴度', '<1:100 阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300094', '200041', 'PH', '酸碱度', '-', '7.35-7.45', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300095', '200041', 'PCO2', '二氧化碳分压', 'mmHg', '35-45', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300096', '200041', 'PO2', '氧分压', 'mmHg', '80-100', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300097', '200041', 'HCO3', '碳酸氢根', 'mmol/L', '22-27', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300098', '200041', 'BE', '碱剩余', 'mmol/L', '-3~+3', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300099', '200041', 'LAC', '乳酸', 'mmol/L', '<2.0', 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300100', '200042', 'UMALB', '尿微量白蛋白', 'mg/L', '<19', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300101', '200042', 'UCREA', '尿肌酐', 'mmol/L', '-', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300102', '200042', 'ACR', '尿白蛋白肌酐比', 'mg/g', '<30', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300103', '200043', 'UPRO24', '24小时尿蛋白', 'g/24h', '<0.15', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300104', '200044', 'STOOLR', '粪便常规', '-', '见报告', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300105', '200044', 'OB', '粪便隐血', '-', '阴性', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300106', '200045', 'AFB', '抗酸杆菌涂片', '-', '阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300107', '200046', 'TBSPOT', '结核T细胞', '-', '阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300108', '200047', 'BCULT', '血培养', '-', '5天无菌生长', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300109', '200048', 'UCULT', '尿培养', 'CFU/mL', '<10^4', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300110', '200049', 'CT', '沙眼衣原体', '-', '阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300111', '200049', 'UU', '解脲支原体', 'CFU', '<10^4', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300112', '200050', 'ALLERG', '过敏原特异性IgE', '-', '阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300113', '200051', 'IGG', '免疫球蛋白G', 'g/L', '7-16', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300114', '200051', 'IGA', '免疫球蛋白A', 'g/L', '0.7-4', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300115', '200051', 'IGM', '免疫球蛋白M', 'g/L', '0.4-2.3', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300116', '200052', 'C3', '补体C3', 'g/L', '0.79-1.52', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300117', '200052', 'C4', '补体C4', 'g/L', '0.16-0.38', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300118', '200053', 'CD3', 'T细胞', 'cells/uL', '690-2540', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300119', '200053', 'CD4', '辅助T细胞', 'cells/uL', '410-1590', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300120', '200053', 'CD8', '抑制T细胞', 'cells/uL', '190-1140', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300121', '200053', 'CD4CD8', 'CD4/CD8比值', '-', '1.0-2.8', 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300122', '200054', 'LA', '狼疮抗凝物', '-', '阴性', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300123', '200055', 'VPA', '丙戊酸血药浓度', 'ug/mL', '50-100', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300124', '200056', 'DIG', '地高辛浓度', 'ng/mL', '0.8-2.0', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300125', '200057', 'CSP', '环孢素谷浓度', 'ng/mL', '100-250', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300126', '200058', 'PB', '血铅', 'ug/L', '<100', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item_detail (id, laboratory_item_id, item_code, item_name, unit, reference_range, sort_order,
                                        status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('300127', '200059', 'ALC', '血酒精浓度', 'mg/dL', '0', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
