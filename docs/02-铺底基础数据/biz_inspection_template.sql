SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2099346574965547009', '2098255864065458177', '脑电图维护', '48', 'ECG006', '脑电图', '奶', '1111', 1, 0,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000301', '897001001', '胸部CT平扫（沈楠）', '7', 'CT002', '胸部CT平扫', '胸部',
        '肺部感染、占位性病变初筛', 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000302', '897001001', '胸部正位片（沈楠）', '1', 'XR001', '胸部正位片', '胸部',
        '肺部感染初筛、心影评估', 0, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000303', '897001022', '头颅CT平扫（倪天）', '6', 'CT001', '头颅CT平扫', '头颅', '脑血管意外初筛', 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000304', '897001022', '头颅MRI平扫（倪天）', '11', 'MR001', '头颅MRI平扫', '头颅',
        '脑梗死后评估、颅内病变鉴别', 0, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000305', '897001008', '腹部CT平扫（温昊天）', '8', 'CT003', '腹部CT平扫', '腹部',
        '腹部占位、胰腺病变评估', 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000306', '9301', '常规心电图（周远）', '21', 'ECG001', '常规心电图', '心脏',
        '心律失常、心肌缺血初筛', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000307', '9301', '腹部立位DR（周远）', '100041', 'DR002', '腹部立位DR', '腹部',
        '肠梗阻、消化道穿孔排查', 1, 2, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000308', '897001036', '泌尿系彩超（郭怀瑾）', '17', 'US002', '泌尿系彩超', '泌尿系',
        '泌尿系结石、肾积水评估', 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000309', '897001029', '甲状腺彩超（杨远航）', '19', 'US004', '甲状腺彩超', '颈部', '甲状腺结节筛查',
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_inspection_template (id, doctor_id, template_name, inspection_item_id, inspection_item_code,
                                     inspection_item_name, body_part, inspection_purpose, is_emergency, sort_order,
                                     create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2350000000000000310', '897001015', '心脏彩超（施佳明）', '20', 'US005', '心脏彩超', '心脏',
        '心功能评估、瓣膜病筛查', 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
