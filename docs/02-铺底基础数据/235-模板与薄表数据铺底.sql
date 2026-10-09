INSERT INTO biz_rx_template (id, doctor_id, template_name, drug_count, total_amount, create_by, create_time, update_by, update_time, del_flag, remark)
VALUES
(2350000000000000001, 897001015, '高血压门诊随访用药（施佳明）', 0, 0.00, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000002, 897001029, '2型糖尿病规范用药（杨远航）', 0, 0.00, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000003, 897001001, '社区获得性肺炎经验用药（沈楠）', 0, 0.00, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000004, 897001008, '慢性胃炎标准用药（温昊天）', 0, 0.00, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000005, 9301, '急性上呼吸道感染用药（周远）', 0, 0.00, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000006, 897001043, '缺铁性贫血补铁方案（吕明德）', 0, 0.00, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000007, 897001016, '稳定型心绞痛二级预防（莫远航）', 0, 0.00, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000008, 897001036, '急性膀胱炎抗感染方案（郭怀瑾）', 0, 0.00, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）');

INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000101, 2350000000000000001, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 2, d.price, d.price*2, '每次30mg，每日1次', '每日一次', '口服', 28, '30mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0057';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000102, 2350000000000000001, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次80mg，每日1次', '每日一次', '口服', 28, '80mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0061';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000103, 2350000000000000002, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 2, d.price, d.price*2, '每次0.5g，每日3次', '每日三次', '口服', 30, '0.5g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0170';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000104, 2350000000000000002, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次50mg，每日3次，随第一口饭嚼服', '每日三次', '口服', 30, '50mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0175';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000105, 2350000000000000003, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次0.25g，每日3次', '每日三次', '口服', 7, '0.25g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0005';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000106, 2350000000000000003, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次10ml，每日3次', '每日三次', '口服', 7, '10ml', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0148';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000107, 2350000000000000003, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次0.5g，体温超过38.5度时服用', '按需', '口服', 3, '0.5g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0043';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000108, 2350000000000000004, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次20mg，每日1次，早餐前', '每日一次', '口服', 28, '20mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0114';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000109, 2350000000000000004, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次1.0g，症状发作时嚼服', '每日三次', '口服', 14, '1.0g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0122';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000110, 2350000000000000004, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次5mg，每日3次，餐前15分钟', '每日三次', '口服', 14, '5mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0127';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000111, 2350000000000000005, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次4粒，每日3次', '每日三次', '口服', 5, '1.4g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0303';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000112, 2350000000000000005, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次0.5g，体温超过38.5度时服用', '按需', '口服', 3, '0.5g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0043';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000113, 2350000000000000005, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次10mg，每晚1次', '每日一次', '口服', 5, '10mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0164';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000114, 2350000000000000006, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次0.1g，每日2次，餐后', '每日两次', '口服', 30, '0.1g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0255';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000115, 2350000000000000006, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次5mg，每日1次', '每日一次', '口服', 30, '5mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0256';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000116, 2350000000000000006, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次0.1g，每日3次，促进铁吸收', '每日三次', '口服', 30, '0.1g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0236';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000117, 2350000000000000007, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次100mg，每日1次', '每日一次', '口服', 30, '100mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0051';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000118, 2350000000000000007, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次20mg，每晚1次', '每日一次', '口服', 30, '20mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0093';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000119, 2350000000000000007, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次75mg，每日1次', '每日一次', '口服', 30, '75mg', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0099';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000120, 2350000000000000008, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次0.4g，每日2次', '每日两次', '口服', 7, '0.4g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0019';
INSERT INTO biz_rx_template_detail (id, template_id, drug_id, drug_code, drug_name, generic_name, specification, dosage_form, manufacturer, unit, quantity, price, amount, usage_dosage, frequency, route, duration, single_dosage, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000121, 2350000000000000008, d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.manufacturer, d.unit, 1, d.price, d.price, '每次3片，每日3次', '每日三次', '口服', 7, '0.87g', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0341';


-- ---------- B. 检查申请模板 10 个 ----------
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000301, 897001001, '胸部CT平扫（沈楠）', i.id, i.item_code, i.item_name, i.body_part, '肺部感染、占位性病变初筛', 0, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='CT002';
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000302, 897001001, '胸部正位片（沈楠）', i.id, i.item_code, i.item_name, i.body_part, '肺部感染初筛、心影评估', 0, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='XR001';
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000303, 897001022, '头颅CT平扫（倪天）', i.id, i.item_code, i.item_name, i.body_part, '脑血管意外初筛', 1, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='CT001';
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000304, 897001022, '头颅MRI平扫（倪天）', i.id, i.item_code, i.item_name, i.body_part, '脑梗死后评估、颅内病变鉴别', 0, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='MR001';
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000305, 897001008, '腹部CT平扫（温昊天）', i.id, i.item_code, i.item_name, i.body_part, '腹部占位、胰腺病变评估', 0, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='CT003';
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000306, 9301, '常规心电图（周远）', i.id, i.item_code, i.item_name, i.body_part, '心律失常、心肌缺血初筛', 1, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='ECG001';
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000307, 9301, '腹部立位DR（周远）', i.id, i.item_code, i.item_name, i.body_part, '肠梗阻、消化道穿孔排查', 1, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='DR002';
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000308, 897001036, '泌尿系彩超（郭怀瑾）', i.id, i.item_code, i.item_name, i.body_part, '泌尿系结石、肾积水评估', 0, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='US002';
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000309, 897001029, '甲状腺彩超（杨远航）', i.id, i.item_code, i.item_name, i.body_part, '甲状腺结节筛查', 0, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='US004';
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code, inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000310, 897001015, '心脏彩超（施佳明）', i.id, i.item_code, i.item_name, i.body_part, '心功能评估、瓣膜病筛查', 0, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_inspection_item i WHERE i.item_code='US005';

-- ---------- C. 检验申请模板 12 个 ----------
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000351, 897001001, '血常规（沈楠）', i.id, i.item_code, i.item_name, i.specimen_type, '感染、贫血初筛', 1, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB001';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000352, 897001001, 'C反应蛋白（沈楠）', i.id, i.item_code, i.item_name, i.specimen_type, '细菌感染与炎症评估', 1, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB018';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000353, 897001001, '降钙素原（沈楠）', i.id, i.item_code, i.item_name, i.specimen_type, '重症感染评估与抗生素指导', 1, 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB020';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000354, 897001001, '痰培养+药敏（沈楠）', i.id, i.item_code, i.item_name, i.specimen_type, '下呼吸道病原学诊断', 0, 4, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB023';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000355, 9301, '血气分析（周远）', i.id, i.item_code, i.item_name, i.specimen_type, '酸碱平衡与氧合评估', 1, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB092';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000356, 897001008, '肝功能全套（温昊天）', i.id, i.item_code, i.item_name, i.specimen_type, '肝功能评估', 0, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB004';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000357, 897001008, '幽门螺杆菌检测（温昊天）', i.id, i.item_code, i.item_name, i.specimen_type, '幽门螺杆菌感染诊断', 0, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB026';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000358, 897001029, '糖化血红蛋白（杨远航）', i.id, i.item_code, i.item_name, i.specimen_type, '近8-12周血糖控制水平评估', 0, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB007';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000359, 897001029, '甲状腺功能三项（杨远航）', i.id, i.item_code, i.item_name, i.specimen_type, '甲状腺功能评估', 0, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB010';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000360, 897001036, '肾功能三项（郭怀瑾）', i.id, i.item_code, i.item_name, i.specimen_type, '肾功能评估', 0, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB005';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000361, 897001036, '尿常规（郭怀瑾）', i.id, i.item_code, i.item_name, i.specimen_type, '泌尿系疾病筛查', 1, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB002';
INSERT INTO biz_laboratory_template (id, doctor_id, template_name, laboratory_item_id, laboratory_item_code, laboratory_item_name, sample_type, inspection_purpose, is_emergency, sort_order, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000362, 897001043, '铁蛋白（吕明德）', i.id, i.item_code, i.item_name, i.specimen_type, '铁代谢与贫血病因评估', 0, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）' FROM sys_laboratory_item i WHERE i.item_code='LB041';

-- ---------- D. 药品套餐 5 个 + 明细 14 行 ----------
INSERT INTO biz_drug_package (id, doctor_id, package_name, package_type, create_by, create_time, update_by, update_time, del_flag, remark)
VALUES
(2350000000000000401, 9301, '门诊静脉输液套餐', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000402, 897001001, '雾化吸入套餐', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000403, 897001001, '急性咽扁桃体炎口服套餐', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000404, 897001008, '消化性溃疡四联疗法套餐', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000405, 897001015, '高血压起始联合用药套餐', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）');

INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000411, 2350000000000000401, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '静滴，皮试后使用', '每日一次', '静脉滴注', 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0273';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000412, 2350000000000000401, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 2, d.price, '每次1.0g溶于0.9%氯化钠250ml中静滴', '每日一次', '静脉滴注', 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0007';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000413, 2350000000000000401, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '10ml稀释后加入输液中', '每日一次', '静脉滴注', 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0281';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000414, 2350000000000000402, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 2, d.price, '每次1支，每日2次雾化吸入', '每日两次', '雾化吸入', 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0169';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000415, 2350000000000000402, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '作为雾化稀释液使用', '每日两次', '雾化吸入', 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0273';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000416, 2350000000000000403, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '每次0.25g，每日2次', '每日两次', '口服', 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0006';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000417, 2350000000000000403, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '每次10ml，每日3次', '每日三次', '口服', 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0306';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000418, 2350000000000000403, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '每次0.5g，体温超过38.5度时服用', '按需', '口服', 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0043';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000419, 2350000000000000404, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '每次20mg，每日2次', '每日两次', '口服', 14, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0114';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000420, 2350000000000000404, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '每次0.6g，每日2次，餐前', '每日两次', '口服', 14, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0124';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000421, 2350000000000000404, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '每次1.0g，每日2次', '每日两次', '口服', 14, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0001';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000422, 2350000000000000404, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '每次0.5g，每日2次', '每日两次', '口服', 14, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0012';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000423, 2350000000000000405, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '每次30mg，每日1次', '每日一次', '口服', 28, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0057';
INSERT INTO biz_drug_package_detail (id, package_id, item_type, item_id, item_code, item_name, specification, unit, quantity, price, usage_dosage, frequency, route, duration, create_by, create_time, update_by, update_time, del_flag, remark)
SELECT 2350000000000000424, 2350000000000000405, 1, d.id, d.drug_code, d.drug_name, d.specification, d.unit, 1, d.price, '每次80mg，每日1次', '每日一次', '口服', 28, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL FROM sys_drug d WHERE d.drug_code='BP0061';

-- ---------- E. 体检套餐 6 个 + 项目 61 行（价格=明细金额合计，由 UPDATE 收口） ----------
INSERT INTO sys_checkup_package (id, package_name, package_code, gender_limit, price, description, status, create_by, create_time, update_by, update_time, del_flag, remark)
VALUES
(2350000000000000501, '健康体检基础套餐（男）', 'PK001', 1, 371.00, '覆盖常规检验、胸片与心电图，适合20-45岁男性年度体检', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000502, '健康体检基础套餐（女）', 'PK002', 2, 521.00, '基础套餐基础上增加子宫附件彩超，适合20-45岁女性年度体检', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000503, '入职体检套餐', 'PK003', 0, 263.00, '满足常规入职健康证明要求，含血常规、肝功能、胸片与心电图', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000504, '中老年关怀套餐（男）', 'PK004', 1, 971.00, '基础套餐上增加肿瘤标志物、颈动脉彩超与骨密度，适合45岁以上男性', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000505, '中老年关怀套餐（女）', 'PK005', 2, 1141.00, '女性基础套餐上增加肿瘤标志物、颈动脉彩超与骨密度，适合45岁以上女性', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000506, 'VIP尊享全面体检套餐', 'PK006', 0, 2021.00, '全面深度筛查：肿瘤标志物、甲功、头颅及胸部CT、心脏彩超、幽门螺杆菌等', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）');

INSERT INTO sys_checkup_package_item (id, package_id, item_name, item_type, ref_standard, amount, sort_order, create_by, create_time, update_by, update_time, del_flag, remark) VALUES
(2350000000000000511, 2350000000000000501, '血常规', 1, '静脉血，五分类', 25.00, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000512, 2350000000000000501, '尿常规', 1, '晨尿中段', 20.00, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000513, 2350000000000000501, '肝功能九项', 1, '空腹8小时以上采血', 68.00, 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000514, 2350000000000000501, '肾功能三项', 1, '空腹8小时以上采血', 35.00, 4, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000515, 2350000000000000501, '血脂四项', 1, '空腹12小时采血', 45.00, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000516, 2350000000000000501, '空腹血糖', 1, '空腹8小时以上采血', 8.00, 6, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000517, 2350000000000000501, '胸部DR正侧位', 2, '无', 140.00, 7, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000518, 2350000000000000501, '常规心电图', 2, '无', 30.00, 8, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000519, 2350000000000000502, '血常规', 1, '静脉血，五分类', 25.00, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000520, 2350000000000000502, '尿常规', 1, '晨尿中段', 20.00, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000521, 2350000000000000502, '肝功能九项', 1, '空腹8小时以上采血', 68.00, 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000522, 2350000000000000502, '肾功能三项', 1, '空腹8小时以上采血', 35.00, 4, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000523, 2350000000000000502, '血脂四项', 1, '空腹12小时采血', 45.00, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000524, 2350000000000000502, '空腹血糖', 1, '空腹8小时以上采血', 8.00, 6, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000525, 2350000000000000502, '胸部DR正侧位', 2, '无', 140.00, 7, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000526, 2350000000000000502, '常规心电图', 2, '无', 30.00, 8, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000527, 2350000000000000502, '子宫附件彩超', 2, '憋尿或阴式', 150.00, 9, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000528, 2350000000000000503, '血常规', 1, '静脉血，五分类', 25.00, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000529, 2350000000000000503, '肝功能九项', 1, '空腹8小时以上采血', 68.00, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000530, 2350000000000000503, '胸部DR正侧位', 2, '无', 140.00, 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000531, 2350000000000000503, '常规心电图', 2, '无', 30.00, 4, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000532, 2350000000000000504, '血常规', 1, '静脉血，五分类', 25.00, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000533, 2350000000000000504, '尿常规', 1, '晨尿中段', 20.00, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000534, 2350000000000000504, '肝功能九项', 1, '空腹8小时以上采血', 68.00, 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000535, 2350000000000000504, '肾功能三项', 1, '空腹8小时以上采血', 35.00, 4, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000536, 2350000000000000504, '血脂四项', 1, '空腹12小时采血', 45.00, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000537, 2350000000000000504, '空腹血糖', 1, '空腹8小时以上采血', 8.00, 6, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000538, 2350000000000000504, '胸部DR正侧位', 2, '无', 140.00, 7, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000539, 2350000000000000504, '常规心电图', 2, '无', 30.00, 8, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000540, 2350000000000000504, '肿瘤标志物(男)', 1, '空腹采血', 300.00, 9, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000541, 2350000000000000504, '颈动脉彩超', 2, '无', 180.00, 10, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000542, 2350000000000000504, '骨密度检测', 2, '无', 120.00, 11, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000543, 2350000000000000505, '血常规', 1, '静脉血，五分类', 25.00, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000544, 2350000000000000505, '尿常规', 1, '晨尿中段', 20.00, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000545, 2350000000000000505, '肝功能九项', 1, '空腹8小时以上采血', 68.00, 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000546, 2350000000000000505, '肾功能三项', 1, '空腹8小时以上采血', 35.00, 4, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000547, 2350000000000000505, '血脂四项', 1, '空腹12小时采血', 45.00, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000548, 2350000000000000505, '空腹血糖', 1, '空腹8小时以上采血', 8.00, 6, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000549, 2350000000000000505, '胸部DR正侧位', 2, '无', 140.00, 7, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000550, 2350000000000000505, '常规心电图', 2, '无', 30.00, 8, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000551, 2350000000000000505, '子宫附件彩超', 2, '憋尿或阴式', 150.00, 9, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000552, 2350000000000000505, '肿瘤标志物(女)', 1, '空腹采血', 320.00, 10, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000553, 2350000000000000505, '颈动脉彩超', 2, '无', 180.00, 11, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000554, 2350000000000000505, '骨密度检测', 2, '无', 120.00, 12, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000555, 2350000000000000506, '血常规', 1, '静脉血，五分类', 25.00, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000556, 2350000000000000506, '尿常规', 1, '晨尿中段', 20.00, 2, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000557, 2350000000000000506, '便常规+隐血', 1, '新鲜标本', 25.00, 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000558, 2350000000000000506, '肝功能九项', 1, '空腹8小时以上采血', 68.00, 4, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000559, 2350000000000000506, '肾功能三项', 1, '空腹8小时以上采血', 35.00, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000560, 2350000000000000506, '血脂四项', 1, '空腹12小时采血', 45.00, 6, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000561, 2350000000000000506, '空腹血糖', 1, '空腹8小时以上采血', 8.00, 7, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000562, 2350000000000000506, '甲状腺功能三项', 1, '空腹采血', 95.00, 8, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000563, 2350000000000000506, '肿瘤标志物(女)', 1, '空腹采血，男女均适用', 320.00, 9, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000564, 2350000000000000506, '幽门螺杆菌检测(呼气)', 1, '空腹或餐后2小时', 100.00, 10, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000565, 2350000000000000506, '胸部CT平扫', 2, '无', 320.00, 11, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000566, 2350000000000000506, '头颅CT平扫', 2, '无', 280.00, 12, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000567, 2350000000000000506, '心脏彩超', 2, '无', 200.00, 13, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000568, 2350000000000000506, '子宫附件彩超', 2, '憋尿或阴式', 150.00, 14, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000569, 2350000000000000506, '颈动脉彩超', 2, '无', 180.00, 15, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000570, 2350000000000000506, '骨密度检测', 2, '无', 120.00, 16, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL),
(2350000000000000571, 2350000000000000506, '常规心电图', 2, '无', 30.00, 17, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, NULL);

UPDATE sys_checkup_package p SET p.price = (SELECT COALESCE(SUM(x.amount),0) FROM sys_checkup_package_item x WHERE x.package_id=p.id AND x.del_flag=0)
WHERE p.id BETWEEN 2350000000000000501 AND 2350000000000000506;

-- ---------- F. 单病种质控目录 +15（国家单病种质控 2020 版精选） ----------
INSERT INTO sys_single_disease (id, disease_code, disease_name, icd10_prefix, create_by, create_time, update_by, update_time, del_flag, remark) VALUES
(2350000000000000601, 'SD-I21', '急性心肌梗死（STEMI，住院）', 'I21', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000602, 'SD-I50', '心力衰竭（住院）', 'I50', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000603, 'SD-J18', '社区获得性肺炎（成人，住院）', 'J18', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000604, 'SD-J44', '慢性阻塞性肺疾病急性加重（住院）', 'J44', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000605, 'SD-I63', '脑梗死（住院）', 'I63', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000606, 'SD-I61', '脑出血（住院）', 'I61', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000607, 'SD-E11', '2型糖尿病（住院）', 'E11', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000608, 'SD-K85', '急性胰腺炎（住院）', 'K85', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000609, 'SD-N18', '慢性肾脏病3-5期（住院）', 'N18', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000610, 'SD-K80', '胆囊结石伴急性胆囊炎（住院手术）', 'K80', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000611, 'SD-K35', '急性阑尾炎（住院手术）', 'K35', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000612, 'SD-O82', '择期剖宫产（住院）', 'O82', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000613, 'SD-S72', '老年髋部骨折（65岁以上，手术）', 'S72', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000614, 'SD-C73', '甲状腺癌（手术）', 'C73', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235'),
(2350000000000000615, 'SD-C50', '乳腺癌（手术）', 'C50', 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '国家单病种质控（2020版）精选，sql/235');

-- ---------- G. DRG 分组补足 +14（CHS-DRG 1.1 模拟口径，风格对齐既有 14 组） ----------
INSERT INTO sys_drg_group (id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status, create_by, create_time, update_by, update_time, del_flag, remark) VALUES
(9015, 'FR21', '急性心肌梗死，伴严重并发症或合并症', 'F', 'FR2', 1.9000, 15000.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9016, 'FR23', '急性心肌梗死，不伴并发症或合并症', 'F', 'FR2', 0.9500, 7600.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9017, 'GS21', '上消化道出血，伴严重并发症或合并症', 'G', 'GS2', 1.3000, 9800.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9018, 'GS23', '上消化道出血，不伴并发症或合并症', 'G', 'GS2', 0.7500, 5600.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9019, 'BU19', '脑血管病恢复期康复（非手术）', 'N', 'BU1', 0.8000, 6200.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9020, 'RV19', '肾功能衰竭（非手术）', 'R', 'RV1', 1.4000, 11000.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9021, 'UJ19', '泌尿系结石手术治疗', 'R', 'UJ1', 0.8500, 7000.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9022, 'KT19', '甲状腺手术治疗', 'I', 'KT1', 0.9000, 7600.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9023, 'XS19', '感染及寄生虫疾患（非手术）', 'A', 'XS1', 0.7000, 5400.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9024, 'ND19', '子宫附件手术治疗', 'N', 'ND1', 0.9000, 7400.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9025, 'OR19', '剖宫产分娩', 'O', 'OR1', 0.7000, 5200.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9026, 'PJ19', '新生儿疾患（非手术）', 'P', 'PJ1', 0.8000, 6400.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9027, 'MJ19', '精神疾患（非手术）', 'M', 'MJ1', 0.6000, 4500.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(9028, 'XJ19', '损伤中毒（非手术）', 'X', 'XJ1', 0.7500, 5800.00, 'CHS-DRG 1.1（模拟）', '1.1', 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）');

-- ---------- H. 员工资格证书 45 张（25 医师 + 20 护士） ----------
INSERT INTO sys_employee_qualification (id, employee_id, cert_type, cert_no, issue_org, issue_date, valid_until, create_by, create_time, update_by, update_time, remark) VALUES
(2350000000000000801, 9301, '2', '20171100001', '1', '2017-06-30', '2032-06-30', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000802, 897001001, '2', '20171100002', '1', '2017-09-15', '2032-09-15', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000803, 897001002, '2', '20171100003', '1', '2017-09-15', '2032-09-15', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000804, 897001003, '2', '20181100004', '1', '2018-03-20', '2033-03-20', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000805, 897001004, '2', '20201100005', '1', '2020-07-10', '2035-07-10', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000806, 897001008, '2', '20171100006', '1', '2017-12-01', '2032-12-01', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000807, 897001009, '2', '20181100007', '1', '2018-06-18', '2033-06-18', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000808, 897001010, '2', '20191100008', '1', '2019-05-22', '2034-05-22', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000809, 897001011, '2', '20201100009', '1', '2020-08-30', '2035-08-30', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000810, 897001015, '2', '20171100010', '1', '2017-04-25', '2032-04-25', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000811, 897001016, '2', '20171100011', '1', '2017-10-08', '2032-10-08', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000812, 897001017, '2', '20181100012', '1', '2018-09-12', '2033-09-12', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000813, 897001018, '2', '20201100013', '1', '2020-11-05', '2035-11-05', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000814, 897001022, '2', '20171100014', '1', '2017-05-16', '2032-05-16', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000815, 897001023, '2', '20181100015', '1', '2018-04-28', '2033-04-28', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000816, 897001024, '2', '20191100016', '1', '2019-07-19', '2034-07-19', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000817, 897001025, '2', '20201100017', '1', '2020-12-08', '2035-12-08', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000818, 897001029, '2', '20171100018', '1', '2017-11-11', '2032-11-11', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000819, 897001030, '2', '20181100019', '1', '2018-08-25', '2033-08-25', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000820, 897001031, '2', '20191100020', '1', '2019-06-06', '2034-06-06', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000821, 897001032, '2', '20201100021', '1', '2020-09-28', '2035-09-28', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000822, 897001036, '2', '20171100022', '1', '2017-08-08', '2032-08-08', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000823, 897001037, '2', '20181100023', '1', '2018-10-30', '2033-10-30', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000824, 897001038, '2', '20191100024', '1', '2019-09-14', '2034-09-14', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000825, 897001039, '2', '20201100025', '1', '2020-06-20', '2035-06-20', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000826, 897001043, '2', '20171100026', '1', '2017-03-18', '2032-03-18', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000827, 897001044, '2', '20181100027', '1', '2018-11-26', '2033-11-26', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000828, 897001005, '3', '20222200001', '1', '2022-05-20', '2027-05-20', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000829, 897001006, '3', '20222200002', '1', '2022-05-20', '2027-05-20', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000830, 897001007, '3', '20212200003', '1', '2021-04-15', '2026-04-15', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000831, 897001012, '3', '20222200004', '1', '2022-09-10', '2027-09-10', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000832, 897001013, '3', '20222200005', '1', '2022-09-10', '2027-09-10', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000833, 897001014, '3', '20232200006', '1', '2023-03-22', '2028-03-22', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000834, 897001019, '3', '20222200007', '1', '2022-11-02', '2027-11-02', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000835, 897001020, '3', '20232200008', '1', '2023-06-18', '2028-06-18', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000836, 897001021, '3', '20212200009', '1', '2021-08-09', '2026-08-09', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000837, 897001026, '3', '20222200010', '1', '2022-07-26', '2027-07-26', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000838, 897001027, '3', '20222200011', '1', '2022-07-26', '2027-07-26', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000839, 897001028, '3', '20232200012', '1', '2023-01-15', '2028-01-15', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000840, 897001033, '3', '20222200013', '1', '2022-12-08', '2027-12-08', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000841, 897001034, '3', '20222200014', '1', '2022-12-08', '2027-12-08', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000842, 897001035, '3', '20232200015', '1', '2023-05-30', '2028-05-30', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000843, 897001040, '3', '20222200016', '1', '2022-04-12', '2027-04-12', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000844, 897001041, '3', '20232200017', '1', '2023-09-25', '2028-09-25', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）'),
(2350000000000000845, 897001042, '3', '20232200018', '1', '2023-09-25', '2028-09-25', 'admin', '2026-05-08 00:00:00', NULL, NULL, '资质铺底（sql/235）');

-- ---------- I. 住院医嘱模板 12 个 + 明细 65 行（order_class：1-长期 2-临时） ----------
INSERT INTO biz_inpatient_order_template (id, doctor_id, doctor_name, dept_id, scope, template_name, order_type, item_count, create_by, create_time, update_by, update_time, del_flag, remark)
VALUES
(2350000000000000201, 897001001, '沈楠', 19580003, 1, '社区获得性肺炎常规医嘱', 1, 6, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000202, 897001015, '施佳明', 19580005, 1, '急性ST段抬高型心肌梗死急救医嘱', 1, 7, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000203, 897001029, '杨远航', 19580007, 1, '2型糖尿病入院常规医嘱', 1, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000204, 897001015, '施佳明', 19580005, 1, '慢性心力衰竭常规医嘱', 1, 7, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000205, 897001022, '倪天', 19580006, 1, '急性脑梗死入院评估医嘱', 1, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000206, 897001008, '温昊天', 19580004, 1, '上消化道出血抢救医嘱', 1, 6, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000207, 897001001, '沈楠', 19580003, 1, '慢阻肺急性加重常规医嘱', 1, 6, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000208, 897001036, '郭怀瑾', 19580008, 1, '肾病综合征常规医嘱', 1, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000209, 897001029, '杨远航', 19580007, 1, '甲状腺功能亢进常规医嘱', 1, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000210, 9302, '林小舟', 19580049, 1, '外科术前常规准备医嘱', 1, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000211, 897001008, '温昊天', 19580004, 1, '肠镜检查肠道准备医嘱', 1, 3, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(2350000000000000212, 897001043, '吕明德', 19580009, 1, '化疗前评估医嘱', 1, 5, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）');

INSERT INTO biz_inpatient_order_template_item (id, template_id, sort_no, order_class, item_code, item_name, spec, unit, dosage, dosage_unit, route, frequency, quantity, price, create_time) VALUES
(2350000000000000231, 2350000000000000201, 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次', 1.00, 12.00, '2026-05-08 00:00:00'),
(2350000000000000232, 2350000000000000201, 2, 1, NULL, '普食', NULL, NULL, NULL, NULL, NULL, NULL, 1.00, 0.00, '2026-05-08 00:00:00'),
(2350000000000000233, 2350000000000000201, 3, 1, 'BP0273', '0.9%氯化钠注射液', '250ml/袋', '袋', 250.0000, 'ml', '静脉滴注', '每日一次', 1.00, 4.20, '2026-05-08 00:00:00'),
(2350000000000000234, 2350000000000000201, 4, 1, 'BP0007', '头孢曲松钠粉针', '1.0g/支', '支', 1.0000, 'g', '静脉滴注', '每日一次', 1.00, 9.80, '2026-05-08 00:00:00'),
(2350000000000000235, 2350000000000000201, 5, 2, 'LB001', '血常规', NULL, '次', NULL, NULL, '静脉采血', '入院当天', 1.00, 25.00, '2026-05-08 00:00:00'),
(2350000000000000236, 2350000000000000201, 6, 2, 'CT002', '胸部CT平扫', NULL, '次', NULL, NULL, NULL, '入院当天', 1.00, 320.00, '2026-05-08 00:00:00'),
(2350000000000000237, 2350000000000000202, 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次', 1.00, 12.00, '2026-05-08 00:00:00'),
(2350000000000000238, 2350000000000000202, 2, 1, NULL, '心电血压监护', NULL, '日', NULL, NULL, NULL, '持续', 1.00, 20.00, '2026-05-08 00:00:00'),
(2350000000000000239, 2350000000000000202, 3, 1, NULL, '低盐低脂饮食', NULL, NULL, NULL, NULL, NULL, NULL, 1.00, 0.00, '2026-05-08 00:00:00'),
(2350000000000000240, 2350000000000000202, 4, 2, 'BP0051', '阿司匹林肠溶片', '100mg×30片', '盒', 300.0000, 'mg', '嚼服', '立即', 1.00, 12.60, '2026-05-08 00:00:00'),
(2350000000000000241, 2350000000000000202, 5, 2, 'BP0099', '硫酸氢氯吡格雷片', '75mg×7片', '盒', 300.0000, 'mg', '口服', '立即', 1.00, 78.00, '2026-05-08 00:00:00'),
(2350000000000000242, 2350000000000000202, 6, 2, 'LB060', '心肌酶谱', NULL, '次', NULL, NULL, '静脉采血', '入院当天', 1.00, 55.00, '2026-05-08 00:00:00'),
(2350000000000000243, 2350000000000000202, 7, 2, 'ECG001', '常规心电图', NULL, '次', NULL, NULL, NULL, '入院当天', 1.00, 30.00, '2026-05-08 00:00:00'),
(2350000000000000244, 2350000000000000203, 1, 1, NULL, '二级护理', NULL, '日', NULL, NULL, NULL, '每日一次', 1.00, 8.00, '2026-05-08 00:00:00'),
(2350000000000000245, 2350000000000000203, 2, 1, NULL, '糖尿病饮食', NULL, NULL, NULL, NULL, NULL, NULL, 1.00, 0.00, '2026-05-08 00:00:00'),
(2350000000000000246, 2350000000000000203, 3, 1, 'BP0170', '二甲双胍片', '0.5g×20片', '盒', 0.5000, 'g', '口服', '每日三次', 1.00, 9.80, '2026-05-08 00:00:00'),
(2350000000000000247, 2350000000000000203, 4, 2, 'LB007', '糖化血红蛋白', NULL, '次', NULL, NULL, '静脉采血', '入院当天', 1.00, 80.00, '2026-05-08 00:00:00'),
(2350000000000000248, 2350000000000000203, 5, 2, 'LB093', '尿微量白蛋白/肌酐比值', NULL, '次', NULL, NULL, '留尿', '入院当天', 1.00, 35.00, '2026-05-08 00:00:00'),
(2350000000000000249, 2350000000000000204, 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次', 1.00, 12.00, '2026-05-08 00:00:00'),
(2350000000000000250, 2350000000000000204, 2, 1, NULL, '低盐饮食', NULL, NULL, NULL, NULL, NULL, NULL, 1.00, 0.00, '2026-05-08 00:00:00'),
(2350000000000000251, 2350000000000000204, 3, 1, 'BP0086', '呋塞米片', '20mg×100片', '瓶', 20.0000, 'mg', '口服', '每日两次', 1.00, 7.50, '2026-05-08 00:00:00'),
(2350000000000000252, 2350000000000000204, 4, 1, 'BP0088', '螺内酯片', '20mg×100片', '瓶', 20.0000, 'mg', '口服', '每日一次', 1.00, 12.00, '2026-05-08 00:00:00'),
(2350000000000000253, 2350000000000000204, 5, 1, 'BP0091', '沙库巴曲缬沙坦钠片', '100mg×7片', '盒', 100.0000, 'mg', '口服', '每日两次', 1.00, 68.00, '2026-05-08 00:00:00'),
(2350000000000000254, 2350000000000000204, 6, 2, 'LB062', 'B型钠尿肽(BNP)', NULL, '次', NULL, NULL, '静脉采血', '入院当天', 1.00, 120.00, '2026-05-08 00:00:00'),
(2350000000000000255, 2350000000000000204, 7, 2, 'US005', '心脏彩超', NULL, '次', NULL, NULL, NULL, '48小时内', 1.00, 200.00, '2026-05-08 00:00:00'),
(2350000000000000256, 2350000000000000205, 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次', 1.00, 12.00, '2026-05-08 00:00:00'),
(2350000000000000257, 2350000000000000205, 2, 1, NULL, '心电血压监护', NULL, '日', NULL, NULL, NULL, '持续', 1.00, 20.00, '2026-05-08 00:00:00'),
(2350000000000000258, 2350000000000000205, 3, 2, 'MR001', '头颅MRI平扫', NULL, '次', NULL, NULL, NULL, '24小时内', 1.00, 650.00, '2026-05-08 00:00:00'),
(2350000000000000259, 2350000000000000205, 4, 2, 'LB073', '凝血功能四项+D二聚体', NULL, '次', NULL, NULL, '静脉采血', '入院当天', 1.00, 95.00, '2026-05-08 00:00:00'),
(2350000000000000260, 2350000000000000205, 5, 2, 'US006', '颈动脉彩超', NULL, '次', NULL, NULL, NULL, '48小时内', 1.00, 180.00, '2026-05-08 00:00:00'),
(2350000000000000261, 2350000000000000206, 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次', 1.00, 12.00, '2026-05-08 00:00:00'),
(2350000000000000262, 2350000000000000206, 2, 1, NULL, '禁食', NULL, NULL, NULL, NULL, NULL, NULL, 1.00, 0.00, '2026-05-08 00:00:00'),
(2350000000000000263, 2350000000000000206, 3, 2, 'BP0115', '奥美拉唑钠粉针', '40mg/支', '支', 40.0000, 'mg', '静脉滴注', '每日两次', 1.00, 28.00, '2026-05-08 00:00:00'),
(2350000000000000264, 2350000000000000206, 4, 2, 'LB031', '血型鉴定(ABO+Rh)', NULL, '次', NULL, NULL, '静脉采血', '入院当天', 1.00, 40.00, '2026-05-08 00:00:00'),
(2350000000000000265, 2350000000000000206, 5, 2, 'LB032', '交叉配血试验', NULL, '次', NULL, NULL, '静脉采血', '按需', 1.00, 60.00, '2026-05-08 00:00:00'),
(2350000000000000266, 2350000000000000206, 6, 2, 'END001', '胃镜检查', NULL, '次', NULL, NULL, NULL, '出血稳定后', 1.00, 380.00, '2026-05-08 00:00:00'),
(2350000000000000267, 2350000000000000207, 1, 1, NULL, '二级护理', NULL, '日', NULL, NULL, NULL, '每日一次', 1.00, 8.00, '2026-05-08 00:00:00'),
(2350000000000000268, 2350000000000000207, 2, 1, NULL, '低流量吸氧', NULL, '小时', NULL, NULL, '鼻导管', '持续', 1.00, 3.00, '2026-05-08 00:00:00'),
(2350000000000000269, 2350000000000000207, 3, 1, 'BP0152', '氨茶碱片', '0.1g×100片', '瓶', 0.1000, 'g', '口服', '每日三次', 1.00, 8.00, '2026-05-08 00:00:00'),
(2350000000000000270, 2350000000000000207, 4, 1, 'BP0156', '噻托溴铵粉吸入剂', '18ug×30粒', '支', 0.0180, 'mg', '吸入', '每日一次', 1.00, 286.00, '2026-05-08 00:00:00'),
(2350000000000000271, 2350000000000000207, 5, 2, 'LB092', '血气分析', NULL, '次', NULL, NULL, '动脉采血', '入院当天', 1.00, 70.00, '2026-05-08 00:00:00'),
(2350000000000000272, 2350000000000000207, 6, 2, 'OT008', '肺功能检查', NULL, '次', NULL, NULL, NULL, '3天内', 1.00, 150.00, '2026-05-08 00:00:00'),
(2350000000000000273, 2350000000000000208, 1, 1, NULL, '一级护理', NULL, '日', NULL, NULL, NULL, '每日一次', 1.00, 12.00, '2026-05-08 00:00:00'),
(2350000000000000274, 2350000000000000208, 2, 1, NULL, '低盐优质蛋白饮食', NULL, NULL, NULL, NULL, NULL, NULL, 1.00, 0.00, '2026-05-08 00:00:00'),
(2350000000000000275, 2350000000000000208, 3, 1, 'BP0261', '泼尼松片', '5mg×100片', '瓶', 5.0000, 'mg', '口服', '每日三次', 1.00, 8.00, '2026-05-08 00:00:00'),
(2350000000000000276, 2350000000000000208, 4, 2, 'LB094', '24小时尿蛋白定量', NULL, '次', NULL, NULL, '留24小时尿', '入院当天', 1.00, 30.00, '2026-05-08 00:00:00'),
(2350000000000000277, 2350000000000000208, 5, 2, 'LB008', '血脂四项', NULL, '次', NULL, NULL, '静脉采血', '入院当天', 1.00, 50.00, '2026-05-08 00:00:00'),
(2350000000000000278, 2350000000000000209, 1, 1, NULL, '二级护理', NULL, '日', NULL, NULL, NULL, '每日一次', 1.00, 8.00, '2026-05-08 00:00:00'),
(2350000000000000279, 2350000000000000209, 2, 1, NULL, '高热量高蛋白饮食', NULL, NULL, NULL, NULL, NULL, NULL, 1.00, 0.00, '2026-05-08 00:00:00'),
(2350000000000000280, 2350000000000000209, 3, 1, 'BP0190', '甲巯咪唑片', '5mg×100片', '瓶', 10.0000, 'mg', '口服', '每日三次', 1.00, 18.00, '2026-05-08 00:00:00'),
(2350000000000000281, 2350000000000000209, 4, 2, 'LB064', '甲状腺功能五项', NULL, '次', NULL, NULL, '静脉采血', '入院当天', 1.00, 150.00, '2026-05-08 00:00:00'),
(2350000000000000282, 2350000000000000209, 5, 2, 'US004', '甲状腺彩超', NULL, '次', NULL, NULL, NULL, '3天内', 1.00, 120.00, '2026-05-08 00:00:00'),
(2350000000000000283, 2350000000000000210, 1, 2, 'BP0298', '破伤风抗毒素(TAT)', '1500IU/支', '支', 1500.0000, 'IU', '肌内注射', '术前', 1.00, 18.00, '2026-05-08 00:00:00'),
(2350000000000000284, 2350000000000000210, 2, 2, 'BP0009', '头孢唑林钠粉针', '1.0g/支', '支', 1.0000, 'g', '静脉滴注', '术前30分钟', 1.00, 6.50, '2026-05-08 00:00:00'),
(2350000000000000285, 2350000000000000210, 3, 2, 'LB013', '凝血功能四项', NULL, '次', NULL, NULL, '静脉采血', '术前', 1.00, 80.00, '2026-05-08 00:00:00'),
(2350000000000000286, 2350000000000000210, 4, 2, 'ECG001', '常规心电图', NULL, '次', NULL, NULL, NULL, '术前', 1.00, 30.00, '2026-05-08 00:00:00'),
(2350000000000000287, 2350000000000000210, 5, 1, NULL, '术前禁食禁饮', NULL, NULL, NULL, NULL, NULL, '术前8小时', 1.00, 0.00, '2026-05-08 00:00:00'),
(2350000000000000288, 2350000000000000211, 1, 2, 'BP0139', '聚乙二醇4000散', '10g×10袋', '盒', 30.0000, 'g', '口服', '检查前晚', 1.00, 32.00, '2026-05-08 00:00:00'),
(2350000000000000289, 2350000000000000211, 2, 1, NULL, '少渣半流质饮食', NULL, NULL, NULL, NULL, NULL, '检查前2天', 1.00, 0.00, '2026-05-08 00:00:00'),
(2350000000000000290, 2350000000000000211, 3, 2, 'END003', '肠镜检查', NULL, '次', NULL, NULL, NULL, '预约', 1.00, 450.00, '2026-05-08 00:00:00'),
(2350000000000000291, 2350000000000000212, 1, 2, 'LB001', '血常规', NULL, '次', NULL, NULL, '静脉采血', '化疗前', 1.00, 25.00, '2026-05-08 00:00:00'),
(2350000000000000292, 2350000000000000212, 2, 2, 'LB004', '肝功能全套', NULL, '次', NULL, NULL, '静脉采血', '化疗前', 1.00, 60.00, '2026-05-08 00:00:00'),
(2350000000000000293, 2350000000000000212, 3, 2, 'LB005', '肾功能三项', NULL, '次', NULL, NULL, '静脉采血', '化疗前', 1.00, 40.00, '2026-05-08 00:00:00'),
(2350000000000000294, 2350000000000000212, 4, 2, 'US005', '心脏彩超', NULL, '次', NULL, NULL, NULL, '化疗前', 1.00, 200.00, '2026-05-08 00:00:00'),
(2350000000000000295, 2350000000000000212, 5, 2, 'LB034', '网织红细胞计数', NULL, '次', NULL, NULL, '静脉采血', '化疗前', 1.00, 30.00, '2026-05-08 00:00:00');

-- ---------- J. 放射报告模板 +4 ----------
INSERT INTO biz_radio_report_template (id, template_code, template_name, modality, item_code, item_name, body_part, exam_method, finding_tpl, impression_tpl, suggestion_tpl, is_public, doctor_id, sort_order, status, create_by, create_time, update_by, update_time, del_flag, remark)
VALUES
(210500000000000205, 'RTPL-CT-ABDOMEN', '腹部CT平扫', 1, 'CT003', '腹部CT平扫', '腹部', '腹部CT平扫，层厚5mm，范围自膈顶至耻骨联合水平。', '肝脏大小形态正常，实质密度均匀，未见异常密度灶。胆囊不大，壁不厚。脾脏不大。胰腺形态密度未见异常。双肾形态大小正常，未见积水及结石影。腹腔未见积液及肿大淋巴结。', '腹部CT平扫未见明显异常。', '结合临床及实验室检查，必要时增强扫描。', 1, NULL, 5, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(210500000000000206, 'RTPL-MR-LUMBAR', '腰椎MRI平扫', 2, 'MR003', '腰椎MRI平扫', '腰椎', '腰椎MRI平扫，行T1WI、T2WI矢状位及轴位扫描。', '腰椎生理曲度存在，椎体形态及信号未见异常。椎间盘未见明显膨出及突出。椎管未见狭窄，脊髓及圆锥信号未见异常。', '腰椎MRI平扫未见明显异常。', '结合临床，必要时复查。', 1, NULL, 6, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(210500000000000207, 'RTPL-DR-LUMBAR', '腰椎正侧位DR', 3, 'XR003', '腰椎正侧位片', '腰椎', '腰椎正侧位DR摄影，站立位或卧位。', '腰椎序列生理曲度存在，椎体骨质结构完整，未见骨折及骨质破坏。椎间隙未见明显狭窄。椎旁软组织未见异常。', '腰椎正侧位未见明显异常。', '结合临床，必要时行MRI检查。', 1, NULL, 7, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）'),
(210500000000000208, 'RTPL-MR-KNEE', '膝关节MRI平扫', 2, 'MR004', '膝关节MRI平扫', '膝关节', '膝关节MRI平扫，行矢状位、冠状位及轴位扫描。', '膝关节组成骨未见异常信号。半月板形态信号正常，未见撕裂征象。前后交叉韧带及侧副韧带连续完整。关节腔内未见明显积液。', '膝关节MRI平扫未见明显异常。', '结合临床及体格检查。', 1, NULL, 8, 1, 'admin', '2026-05-08 00:00:00', NULL, NULL, 0, '铺底（sql/235）');
