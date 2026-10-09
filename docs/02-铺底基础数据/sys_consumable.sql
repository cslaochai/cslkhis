SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600000', 'HC001', '一次性使用无菌注射器(1ml)', 2, '1ml', '支', NULL, '0.45', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600001', 'HC002', '一次性使用无菌注射器(5ml)', 2, '5ml', '支', NULL, '0.65', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600002', 'HC003', '一次性使用无菌注射器(10ml)', 2, '10ml', '支', NULL, '0.85', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600003', 'HC004', '一次性使用无菌注射器(20ml)', 2, '20ml', '支', NULL, '1.20', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600004', 'HC005', '一次性使用无菌注射器(50ml)', 2, '50ml', '支', NULL, '1.80', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600005', 'HC006', '一次性使用无菌输液器', 2, '单包装', '套', NULL, '3.80', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600006', 'HC007', '一次性使用精密过滤输液器', 2, '单包装', '套', NULL, '8.50', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600007', 'HC008', '一次性使用静脉留置针(24G)', 2, '24G', '支', NULL, '12.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600008', 'HC009', '一次性使用静脉留置针(22G)', 2, '22G', '支', NULL, '12.50', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600009', 'HC010', '一次性使用静脉留置针(18G)', 2, '18G', '支', NULL, '13.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600010', 'HC011', '一次性使用采血针', 2, '单包装', '支', NULL, '0.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600011', 'HC012', '一次性使用真空采血管(EDTA紫帽)', 2, '2ml', '支', NULL, '0.80', 0, NULL, NULL, 1, 'admin',
        '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600012', 'HC013', '一次性使用真空采血管(促凝黄帽)', 2, '3.5ml', '支', NULL, '0.90', 0, NULL, NULL, 1, 'admin',
        '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600013', 'HC014', '一次性使用真空采血管(枸橼酸蓝帽)', 2, '2ml', '支', NULL, '1.00', 0, NULL, NULL, 1, 'admin',
        '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600014', 'HC015', '一次性使用真空采血管(肝素绿帽)', 2, '3ml', '支', NULL, '1.00', 0, NULL, NULL, 1, 'admin',
        '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600015', 'HC016', '一次性使用静脉切开包', 2, '单包装', '包', NULL, '35.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600016', 'HC017', '一次性使用中心静脉导管包(三腔)', 2, '7Fr', '套', NULL, '380.00', 0, NULL, NULL, 1, 'admin',
        '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600017', 'HC018', '一次性使用中心静脉导管包(双腔)', 2, '8.5Fr', '套', NULL, '320.00', 0, NULL, NULL, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600018', 'HC019', '一次性使用PICC导管(三向瓣膜)', 2, '4Fr', '套', NULL, '1280.00', 0, NULL, NULL, 1, 'admin',
        '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600019', 'HC020', '一次性使用输液接头(正压)', 2, '单包装', '个', NULL, '15.00', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600020', 'HC021', '一次性使用三通阀', 2, '单包装', '个', NULL, '4.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600021', 'HC022', '一次性使用延长管', 2, '单包装', '条', NULL, '3.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600022', 'HC023', '一次性使用雾化吸入器', 2, '单包装', '套', NULL, '6.50', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600023', 'HC024', '一次性使用吸痰管', 2, '12Fr', '支', NULL, '2.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600024', 'HC025', '一次性使用吸痰管', 2, '14Fr', '支', NULL, '2.80', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600025', 'HC026', '一次性使用鼻氧管', 2, '单包装', '根', NULL, '1.80', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600026', 'HC027', '一次性使用面罩吸氧套件', 2, '成人型', '套', NULL, '6.80', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600027', 'HC028', '一次性使用呼吸机管路', 2, '成人型', '套', NULL, '45.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600028', 'HC029', '一次性使用麻醉螺纹管', 2, '成人型', '套', NULL, '28.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600029', 'HC030', '一次性使用人工鼻(呼吸过滤器)', 2, '儿童/成人', '个', NULL, '12.00', 0, NULL, NULL, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600030', 'HC031', '一次性使用简易呼吸器', 2, '成人型', '套', NULL, '38.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600031', 'HC032', '一次性使用气管插管(普通)', 2, '7.0#', '根', NULL, '18.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600032', 'HC033', '一次性使用气管插管(加强型)', 2, '7.5#', '根', NULL, '35.00', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600033', 'HC034', '一次性使用气管切开插管', 2, '7.0#', '套', NULL, '68.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600034', 'HC035', '一次性使用导尿管(双腔)', 2, '16Fr', '根', NULL, '12.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600035', 'HC036', '一次性使用导尿管(双腔)', 2, '18Fr', '根', NULL, '12.50', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600036', 'HC037', '一次性使用导尿包', 2, '单包装', '包', NULL, '25.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600037', 'HC038', '一次性使用引流袋(抗反流)', 2, '1000ml', '个', NULL, '6.50', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600038', 'HC039', '一次性使用负压引流器', 2, '400ml', '个', NULL, '18.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600039', 'HC040', '一次性使用胃管(硅胶)', 2, '14Fr', '根', NULL, '9.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600040', 'HC041', '一次性使用鼻胃肠管', 2, '16Fr', '根', NULL, '68.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600041', 'HC042', '一次性使用肛管', 2, '22Fr', '根', NULL, '4.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL, '1',
        '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600042', 'HC043', '一次性使用灌肠包', 2, '单包装', '包', NULL, '12.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600043', 'HC044', '一次性使用阴道扩张器', 2, '透明型', '个', NULL, '1.80', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600044', 'HC045', '一次性使用产包', 2, '单包装', '包', NULL, '45.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600045', 'HC046', '一次性使用手术衣', 1, '加强型', '件', NULL, '12.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600046', 'HC047', '一次性使用手术衣', 1, '标准型', '件', NULL, '8.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600047', 'HC048', '一次性使用手术铺巾', 1, '标准型', '套', NULL, '25.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600048', 'HC049', '一次性使用无菌手术膜', 1, '10×15cm', '贴', NULL, '3.50', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600049', 'HC050', '一次性使用无菌手术膜', 1, '45×45cm', '贴', NULL, '12.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600050', 'HC051', '一次性使用帽子', 1, '无纺布', '只', NULL, '0.30', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600051', 'HC052', '一次性使用口罩', 1, '无纺布', '只', NULL, '0.25', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600052', 'HC053', '医用外科口罩', 1, '灭菌装', '只', NULL, '0.60', 0, NULL, NULL, 1, 'admin', '', 0, NULL, '1',
        '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600053', 'HC054', '医用防护口罩(N95)', 1, '头戴式', '只', NULL, '6.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600054', 'HC055', '一次性使用检查手套', 1, 'M码', '副', NULL, '0.40', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600055', 'HC056', '一次性使用无菌手套', 1, '7.5#', '副', NULL, '2.20', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600056', 'HC057', '一次性使用无菌手套', 1, '8#', '副', NULL, '2.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600057', 'HC058', '医用橡胶手套', 1, 'M码', '副', NULL, '3.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL, '1',
        '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600058', 'HC059', '一次性使用隔离衣', 1, '标准型', '件', NULL, '5.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600059', 'HC060', '一次性使用防水隔离衣', 1, '加强型', '件', NULL, '8.50', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600060', 'HC061', '一次性使用活检钳', 2, '2.3mm', '根', NULL, '168.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600061', 'HC062', '一次性内镜用活体取样钳', 2, '2.8mm', '根', NULL, '188.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600062', 'HC063', '一次性使用圈套器', 2, '25mm', '根', NULL, '198.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600063', 'HC064', '一次性使用止血夹', 2, '13mm', '个', NULL, '68.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600064', 'HC065', '一次性使用注射针', 2, '0.7×30mm', '支', NULL, '0.35', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600065', 'HC066', '一次性使用留置针敷贴', 1, '6×7cm', '贴', NULL, '3.20', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600066', 'HC067', '一次性使用无菌敷贴', 1, '9×10cm', '贴', NULL, '2.80', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600067', 'HC068', '医用纱布块', 3, '8×8cm-8层', '包', NULL, '2.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600068', 'HC069', '医用纱布绷带', 3, '5cm×6m', '卷', NULL, '2.20', 0, NULL, NULL, 1, 'admin', '', 0, NULL, '1',
        '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600069', 'HC070', '医用弹性绷带', 3, '7.5cm×4.5m', '卷', NULL, '6.80', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600070', 'HC071', '医用棉签', 3, '10cm-50支', '包', NULL, '3.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL, '1',
        '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600071', 'HC072', '医用棉球', 3, '50g', '包', NULL, '4.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600072', 'HC073', '医用脱脂棉纱布垫', 3, '25×30cm', '片', NULL, '1.50', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600073', 'HC074', '医用透明质酸钠凝胶', 3, '2.5ml', '支', NULL, '58.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600074', 'HC075', '藻酸盐敷料', 3, '10×10cm', '片', NULL, '18.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL, '1',
        '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600075', 'HC076', '泡沫敷料', 3, '10×10cm', '片', NULL, '22.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL, '1',
        '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600076', 'HC077', '水胶体敷料', 3, '10×10cm', '片', NULL, '16.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL, '1',
        '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600077', 'HC078', '银离子抗菌敷料', 3, '10×10cm', '片', NULL, '68.00', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600078', 'HC079', '医用几丁糖(防粘连)', 1, '2ml/支', '支', NULL, '286.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600079', 'HC080', '一次性使用皮肤缝合器', 4, '35W', '把', NULL, '128.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600080', 'HC081', '可吸收性外科缝线', 4, '3-0(1/2圈)', '根', NULL, '25.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600081', 'HC082', '可吸收性外科缝线', 4, '4-0(1/2圈)', '根', NULL, '28.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600082', 'HC083', '不可吸收外科缝线(丝线)', 4, '1号', '根', NULL, '3.50', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600083', 'HC084', '一次性使用皮肤吻合钉', 4, '35W', '盒', NULL, '88.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600084', 'HC085', '一次性使用引流管', 2, '硅胶18Fr', '根', NULL, '15.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600085', 'HC086', '一次性使用氧气湿化瓶', 2, '单包装', '个', NULL, '5.50', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600086', 'HC087', '一次性使用血透管路', 2, '泵前动脉壶型', '套', NULL, '68.00', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600087', 'HC088', '一次性使用透析器', 2, '1.8m²低通', '支', NULL, '128.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600088', 'HC089', '一次性使用血液灌流器', 2, '150g树脂型', '支', NULL, '268.00', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600089', 'HC090', '一次性使用穿刺针(血透)', 2, '16G', '支', NULL, '15.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600090', 'HC091', '一次性使用动态血糖传感器', 2, '探针式', '个', NULL, '386.00', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600091', 'HC092', '一次性使用胰岛素泵用管路', 2, '单包装', '套', NULL, '28.00', 0, NULL, NULL, 1, 'admin', '',
        0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600092', 'HC093', '一次性使用体外循环管路(心脏手术)', 2, '成人型', '套', NULL, '880.00', 0, NULL, NULL, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600093', 'HC094', '一次性使用膜式氧合器', 2, '成人型', '个', NULL, '1980.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600094', 'HC095', '一次性使用负压吸引连接管', 2, '2m', '根', NULL, '6.80', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600095', 'HC096', '一次性使用集痰器', 2, '单包装', '个', NULL, '3.80', 0, NULL, NULL, 1, 'admin', '', 0, NULL,
        '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600096', 'HC097', '一次性使用一次性压舌板', 2, '竹制', '支', NULL, '0.15', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600097', 'HC098', '一次性使用体温计(电子)', 2, '单包装', '支', NULL, '8.50', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600098', 'HC099', '一次性使用输液加压袋', 2, '1000ml', '个', NULL, '48.00', 0, NULL, NULL, 1, 'admin', '', 0,
        NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('600099', 'HC100', '一次性使用骨水泥(人工关节用)', 4, '40g PMMA', '套', NULL, '980.00', 0, NULL, NULL, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2101335590719987713', 'HC-VERIFY-1789832420325', '验证耗材-可删除', 5, NULL, '个', NULL, '1.00', 0, NULL, NULL,
        1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2101337998405672962', 'HC-VERIFY-1789832994395', '验证耗材-可删除', 5, NULL, '个', NULL, '1.00', 0, NULL, NULL,
        1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2101339147053572098', 'HC-VERIFY-1789833268265', '验证耗材-可删除', 5, NULL, '个', NULL, '1.00', 0, NULL, NULL,
        1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2101339725863329793', 'HC-VERIFY-1789833406258', '验证耗材-可删除', 5, NULL, '个', NULL, '1.00', 0, NULL, NULL,
        1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2101340060807864322', 'HC-VERIFY-1789833486122', '验证耗材-可删除', 5, NULL, '个', NULL, '1.00', 0, NULL, NULL,
        1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2101340474596925442', 'HC-VERIFY-1789833584764', '验证耗材-可删除', 5, NULL, '个', NULL, '1.00', 0, NULL, NULL,
        1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2101341376271622145', 'HC-VERIFY-1789833799749', '验证耗材-可删除', 5, NULL, '个', NULL, '1.00', 0, NULL, NULL,
        1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2101341520958332930', 'HC-VERIFY-1789833834230', '验证耗材-可删除', 5, NULL, '个', NULL, '1.00', 0, NULL, NULL,
        1, 'admin', 'admin', 1, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000601', 'HC-HC-001', '医用脱脂纱布块', 3, '8cm×8cm 灭菌 5片/包', '包', '振德医疗', '3.50', 0,
        NULL, NULL, 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000602', 'HC-HC-002', '医用棉签', 3, '10cm 50支/包', '包', '稳健医疗', '2.00', 0, NULL, NULL, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000603', 'HC-ZS-001', '一次性使用无菌注射器', 2, '5ml 带针', '支', '山东威高', '1.20', 0, NULL,
        NULL, 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000604', 'HC-ZS-002', '一次性使用无菌注射器', 2, '10ml 带针', '支', '山东威高', '1.50', 0, NULL,
        NULL, 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000605', 'HC-ZS-003', '静脉留置针', 2, '22G 直型密闭式', '支', 'BD碧迪', '18.00', 0, NULL, NULL,
        1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000606', 'HC-ZS-004', '一次性使用输液器', 2, '带针 双头', '套', '山东威高', '3.80', 0, NULL, NULL,
        1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000607', 'HC-FH-001', '医用外科口罩', 4, '挂耳式 17.5cm×9.5cm', '只', '稳健医疗', '0.80', 0, NULL,
        NULL, 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000608', 'HC-FH-002', '一次性使用检查手套', 4, 'PVC 无粉 M码', '副', '麦迪康', '0.60', 0, NULL,
        NULL, 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000609', 'HC-FH-003', '一次性使用无菌敷贴', 3, '6cm×7cm', '贴', '3M', '4.50', 0, NULL, NULL, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000610', 'HC-HC-003', '医用弹性绷带', 3, '7.5cm×4m', '卷', '振德医疗', '2.80', 0, NULL, NULL, 1,
        'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000611', 'HC-HC-004', '一次性使用换药碗', 1, '不锈钢 12cm', '个', '江苏华星', '5.00', 0, NULL,
        NULL, 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000612', 'HC-HC-005', '碘伏消毒棉球', 1, '0.5% 100只/瓶', '瓶', '利尔康', '8.50', 0, NULL, NULL,
        1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000801', 'GV-ZJ-001', '冠状动脉支架系统', 6, '药物洗脱 3.0mm×18mm', '个', '微创医疗器械',
        '12800.00', 1, '06941234567890', '国械注准20173660001', 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000802', 'GV-GJ-001', '人工全髋关节股骨柄', 6, '钛合金 12号', '个', '春立正达医疗', '32000.00', 1,
        '06941234567891', '国械注准20163130002', 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000803', 'GV-JT-001', '人工晶状体（非球面）', 6, '后房型 21D', '枚', '爱博诺德医疗', '5600.00', 1,
        '06941234567892', '国械注准20153220003', 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000804', 'GV-QB-001', '永久性心脏起搏器（双腔）', 6, 'DR/DDDR 1.5T', '台', '乐普医疗', '58000.00',
        1, '06941234567893', '国械注准20183120004', 1, 'admin', '', 0, NULL, '1', '1');
INSERT INTO sys_consumable (id, consumable_code, consumable_name, category, specification, unit, manufacturer,
                            retail_price, is_high_value, udi_di, reg_cert_no, status, create_by, update_by, del_flag,
                            remark, create_by_id, update_by_id)
VALUES ('2990000000000000805', 'GV-BB-001', '血管缝合闭合器', 6, '8F 缝合式', '个', '先健科技', '4800.00', 1,
        '06941234567894', '国械注准20193030005', 1, 'admin', '', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
