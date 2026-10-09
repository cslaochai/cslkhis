SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2099348817903099906', '2098255864065458177', '术前血常规', '1', 'LB001', '血常规', '', '', 0, 0, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000351', '897001001', '血常规（沈楠）', '1', 'LB001', '血常规', '静脉血', '感染、贫血初筛', 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000352', '897001001', 'C反应蛋白（沈楠）', '18', 'LB018', 'C反应蛋白(CRP)', '静脉血',
        '细菌感染与炎症评估', 1, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000353', '897001001', '降钙素原（沈楠）', '20', 'LB020', '降钙素原(PCT)', '静脉血',
        '重症感染评估与抗生素指导', 1, 3, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000354', '897001001', '痰培养+药敏（沈楠）', '23', 'LB023', '痰培养+药敏', '痰液',
        '下呼吸道病原学诊断', 0, 4, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000355', '9301', '血气分析（周远）', '200041', 'LB092', '血气分析', '动脉血', '酸碱平衡与氧合评估',
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000356', '897001008', '肝功能全套（温昊天）', '4', 'LB004', '肝功能全套', '静脉血', '肝功能评估', 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000357', '897001008', '幽门螺杆菌检测（温昊天）', '26', 'LB026', '幽门螺杆菌检测(呼气)', '呼气',
        '幽门螺杆菌感染诊断', 0, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000358', '897001029', '糖化血红蛋白（杨远航）', '7', 'LB007', '糖化血红蛋白', '静脉血',
        '近8-12周血糖控制水平评估', 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000359', '897001029', '甲状腺功能三项（杨远航）', '10', 'LB010', '甲功三项', '静脉血',
        '甲状腺功能评估', 0, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000360', '897001036', '肾功能三项（郭怀瑾）', '5', 'LB005', '肾功能三项', '静脉血', '肾功能评估', 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000361', '897001036', '尿常规（郭怀瑾）', '2', 'LB002', '尿常规', '尿液', '泌尿系疾病筛查', 1, 2,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code,
                                     laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000362', '897001043', '铁蛋白（吕明德）', '41', 'LB041', '铁蛋白', '静脉血', '铁代谢与贫血病因评估',
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
