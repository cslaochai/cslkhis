SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000231', '2350000000000000201', 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次',
        '1.00', '12.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000232', '2350000000000000201', 2, 1, NULL, '普食', NULL, NULL, NULL, NULL, NULL, NULL, '1.00',
        '0.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000233', '2350000000000000201', 3, 1, 'BP0273', '0.9%氯化钠注射液', '250ml/袋', '袋', '250.0000',
        'ml', '静脉滴注', '每日一次', '1.00', '4.20', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000234', '2350000000000000201', 4, 1, 'BP0007', '头孢曲松钠粉针', '1.0g/支', '支', '1.0000', 'g',
        '静脉滴注', '每日一次', '1.00', '9.80', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000235', '2350000000000000201', 5, 2, 'LB001', '血常规', NULL, '次', NULL, NULL, '静脉采血',
        '入院当天', '1.00', '25.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000236', '2350000000000000201', 6, 2, 'CT002', '胸部CT平扫', NULL, '次', NULL, NULL, NULL,
        '入院当天', '1.00', '320.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000237', '2350000000000000202', 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次',
        '1.00', '12.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000238', '2350000000000000202', 2, 1, NULL, '心电血压监护', NULL, '日', NULL, NULL, NULL, '持续',
        '1.00', '20.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000239', '2350000000000000202', 3, 1, NULL, '低盐低脂饮食', NULL, NULL, NULL, NULL, NULL, NULL,
        '1.00', '0.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000240', '2350000000000000202', 4, 2, 'BP0051', '阿司匹林肠溶片', '100mg×30片', '盒', '300.0000',
        'mg', '嚼服', '立即', '1.00', '12.60', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000241', '2350000000000000202', 5, 2, 'BP0099', '硫酸氢氯吡格雷片', '75mg×7片', '盒', '300.0000',
        'mg', '口服', '立即', '1.00', '78.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000242', '2350000000000000202', 6, 2, 'LB060', '心肌酶谱', NULL, '次', NULL, NULL, '静脉采血',
        '入院当天', '1.00', '55.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000243', '2350000000000000202', 7, 2, 'ECG001', '常规心电图', NULL, '次', NULL, NULL, NULL,
        '入院当天', '1.00', '30.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000244', '2350000000000000203', 1, 1, NULL, '二级护理', NULL, '日', NULL, NULL, NULL, '每日一次',
        '1.00', '8.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000245', '2350000000000000203', 2, 1, NULL, '糖尿病饮食', NULL, NULL, NULL, NULL, NULL, NULL,
        '1.00', '0.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000246', '2350000000000000203', 3, 1, 'BP0170', '二甲双胍片', '0.5g×20片', '盒', '0.5000', 'g',
        '口服', '每日三次', '1.00', '9.80', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000247', '2350000000000000203', 4, 2, 'LB007', '糖化血红蛋白', NULL, '次', NULL, NULL, '静脉采血',
        '入院当天', '1.00', '80.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000248', '2350000000000000203', 5, 2, 'LB093', '尿微量白蛋白/肌酐比值', NULL, '次', NULL, NULL,
        '留尿', '入院当天', '1.00', '35.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000249', '2350000000000000204', 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次',
        '1.00', '12.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000250', '2350000000000000204', 2, 1, NULL, '低盐饮食', NULL, NULL, NULL, NULL, NULL, NULL,
        '1.00', '0.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000251', '2350000000000000204', 3, 1, 'BP0086', '呋塞米片', '20mg×100片', '瓶', '20.0000', 'mg',
        '口服', '每日两次', '1.00', '7.50', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000252', '2350000000000000204', 4, 1, 'BP0088', '螺内酯片', '20mg×100片', '瓶', '20.0000', 'mg',
        '口服', '每日一次', '1.00', '12.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000253', '2350000000000000204', 5, 1, 'BP0091', '沙库巴曲缬沙坦钠片', '100mg×7片', '盒',
        '100.0000', 'mg', '口服', '每日两次', '1.00', '68.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000254', '2350000000000000204', 6, 2, 'LB062', 'B型钠尿肽(BNP)', NULL, '次', NULL, NULL,
        '静脉采血', '入院当天', '1.00', '120.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000255', '2350000000000000204', 7, 2, 'US005', '心脏彩超', NULL, '次', NULL, NULL, NULL,
        '48小时内', '1.00', '200.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000256', '2350000000000000205', 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次',
        '1.00', '12.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000257', '2350000000000000205', 2, 1, NULL, '心电血压监护', NULL, '日', NULL, NULL, NULL, '持续',
        '1.00', '20.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000258', '2350000000000000205', 3, 2, 'MR001', '头颅MRI平扫', NULL, '次', NULL, NULL, NULL,
        '24小时内', '1.00', '650.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000259', '2350000000000000205', 4, 2, 'LB073', '凝血功能四项+D二聚体', NULL, '次', NULL, NULL,
        '静脉采血', '入院当天', '1.00', '95.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000260', '2350000000000000205', 5, 2, 'US006', '颈动脉彩超', NULL, '次', NULL, NULL, NULL,
        '48小时内', '1.00', '180.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000261', '2350000000000000206', 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次',
        '1.00', '12.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000262', '2350000000000000206', 2, 1, NULL, '禁食', NULL, NULL, NULL, NULL, NULL, NULL, '1.00',
        '0.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000263', '2350000000000000206', 3, 2, 'BP0115', '奥美拉唑钠粉针', '40mg/支', '支', '40.0000',
        'mg', '静脉滴注', '每日两次', '1.00', '28.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000264', '2350000000000000206', 4, 2, 'LB031', '血型鉴定(ABO+Rh)', NULL, '次', NULL, NULL,
        '静脉采血', '入院当天', '1.00', '40.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000265', '2350000000000000206', 5, 2, 'LB032', '交叉配血试验', NULL, '次', NULL, NULL, '静脉采血',
        '按需', '1.00', '60.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000266', '2350000000000000206', 6, 2, 'END001', '胃镜检查', NULL, '次', NULL, NULL, NULL,
        '出血稳定后', '1.00', '380.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000267', '2350000000000000207', 1, 1, NULL, '二级护理', NULL, '日', NULL, NULL, NULL, '每日一次',
        '1.00', '8.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000268', '2350000000000000207', 2, 1, NULL, '低流量吸氧', NULL, '小时', NULL, NULL, '鼻导管',
        '持续', '1.00', '3.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000269', '2350000000000000207', 3, 1, 'BP0152', '氨茶碱片', '0.1g×100片', '瓶', '0.1000', 'g',
        '口服', '每日三次', '1.00', '8.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000270', '2350000000000000207', 4, 1, 'BP0156', '噻托溴铵粉吸入剂', '18ug×30粒', '支', '0.0180',
        'mg', '吸入', '每日一次', '1.00', '286.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000271', '2350000000000000207', 5, 2, 'LB092', '血气分析', NULL, '次', NULL, NULL, '动脉采血',
        '入院当天', '1.00', '70.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000272', '2350000000000000207', 6, 2, 'OT008', '肺功能检查', NULL, '次', NULL, NULL, NULL,
        '3天内', '1.00', '150.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000273', '2350000000000000208', 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次',
        '1.00', '12.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000274', '2350000000000000208', 2, 1, NULL, '低盐优质蛋白饮食', NULL, NULL, NULL, NULL, NULL,
        NULL, '1.00', '0.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000275', '2350000000000000208', 3, 1, 'BP0261', '泼尼松片', '5mg×100片', '瓶', '5.0000', 'mg',
        '口服', '每日三次', '1.00', '8.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000276', '2350000000000000208', 4, 2, 'LB094', '24小时尿蛋白定量', NULL, '次', NULL, NULL,
        '留24小时尿', '入院当天', '1.00', '30.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000277', '2350000000000000208', 5, 2, 'LB008', '血脂四项', NULL, '次', NULL, NULL, '静脉采血',
        '入院当天', '1.00', '50.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000278', '2350000000000000209', 1, 1, NULL, '二级护理', NULL, '日', NULL, NULL, NULL, '每日一次',
        '1.00', '8.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000279', '2350000000000000209', 2, 1, NULL, '高热量高蛋白饮食', NULL, NULL, NULL, NULL, NULL,
        NULL, '1.00', '0.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000280', '2350000000000000209', 3, 1, 'BP0190', '甲巯咪唑片', '5mg×100片', '瓶', '10.0000', 'mg',
        '口服', '每日三次', '1.00', '18.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000281', '2350000000000000209', 4, 2, 'LB064', '甲状腺功能五项', NULL, '次', NULL, NULL,
        '静脉采血', '入院当天', '1.00', '150.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000282', '2350000000000000209', 5, 2, 'US004', '甲状腺彩超', NULL, '次', NULL, NULL, NULL,
        '3天内', '1.00', '120.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000283', '2350000000000000210', 1, 2, 'BP0298', '破伤风抗毒素(TAT)', '1500IU/支', '支',
        '1500.0000', 'IU', '肌内注射', '术前', '1.00', '18.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000284', '2350000000000000210', 2, 2, 'BP0009', '头孢唑林钠粉针', '1.0g/支', '支', '1.0000', 'g',
        '静脉滴注', '术前30分钟', '1.00', '6.50', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000285', '2350000000000000210', 3, 2, 'LB013', '凝血功能四项', NULL, '次', NULL, NULL, '静脉采血',
        '术前', '1.00', '80.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000286', '2350000000000000210', 4, 2, 'ECG001', '常规心电图', NULL, '次', NULL, NULL, NULL,
        '术前', '1.00', '30.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000287', '2350000000000000210', 5, 1, NULL, '术前禁食禁饮', NULL, NULL, NULL, NULL, NULL,
        '术前8小时', '1.00', '0.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000288', '2350000000000000211', 1, 2, 'BP0139', '聚乙二醇4000散', '10g×10袋', '盒', '30.0000',
        'g', '口服', '检查前晚', '1.00', '32.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000289', '2350000000000000211', 2, 1, NULL, '少渣半流质饮食', NULL, NULL, NULL, NULL, NULL,
        '检查前2天', '1.00', '0.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000290', '2350000000000000211', 3, 2, 'END003', '肠镜检查', NULL, '次', NULL, NULL, NULL, '预约',
        '1.00', '450.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000291', '2350000000000000212', 1, 2, 'LB001', '血常规', NULL, '次', NULL, NULL, '静脉采血',
        '化疗前', '1.00', '25.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000292', '2350000000000000212', 2, 2, 'LB004', '肝功能全套', NULL, '次', NULL, NULL, '静脉采血',
        '化疗前', '1.00', '60.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000293', '2350000000000000212', 3, 2, 'LB005', '肾功能三项', NULL, '次', NULL, NULL, '静脉采血',
        '化疗前', '1.00', '40.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000294', '2350000000000000212', 4, 2, 'US005', '心脏彩超', NULL, '次', NULL, NULL, NULL, '化疗前',
        '1.00', '200.00', 'admin', 'admin', '1', '1');
INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit,
                                               dosage, dosage_unit, route, frequency, quantity, price, create_by,
                                               update_by, create_by_id, update_by_id)
VALUES ('2350000000000000295', '2350000000000000212', 5, 2, 'LB034', '网织红细胞计数', NULL, '次', NULL, NULL,
        '静脉采血', '化疗前', '1.00', '30.00', 'admin', 'admin', '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
