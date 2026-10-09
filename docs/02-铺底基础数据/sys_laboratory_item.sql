SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('1', 'LB001', '血常规', 1, '201', '静脉血', '25.00', 2,
        'WBC:4-10, RBC:男4-5.5/女3.5-5, HGB:男120-160/女110-150', '10^9/L, 10^12/L, g/L', 1, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('2', 'LB002', '尿常规', 2, '201', '尿液', '20.00', 2, 'PRO:阴性, GLU:阴性, WBC:0-5/HP', '-', 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('3', 'LB003', '便常规+隐血', 3, '201', '粪便', '25.00', 3, 'OB:阴性, WBC:0-1/HP', '-', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('4', 'LB004', '肝功能全套', 3, '202', '静脉血', '60.00', 4, 'ALT:0-40, AST:0-40, TBIL:3.4-17.1', 'U/L, umol/L',
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('5', 'LB005', '肾功能三项', 3, '202', '静脉血', '40.00', 4, 'BUN:2.8-7.2, Cr:44-133, UA:149-416',
        'mmol/L, umol/L', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('6', 'LB006', '空腹血糖', 3, '202', '静脉血', '10.00', 2, '3.9-6.1', 'mmol/L', 1, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('7', 'LB007', '糖化血红蛋白', 3, '202', '静脉血', '80.00', 6, '4.0-6.0', '%', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('8', 'LB008', '血脂四项', 3, '202', '静脉血', '50.00', 4, 'TC:<5.2, TG:<1.7, HDL-C:>1.0, LDL-C:<3.4', 'mmol/L',
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('9', 'LB009', '乙肝两对半', 4, '203', '静脉血', '60.00', 8, 'HBsAg:阴性, HBsAb:阴性/阳性', '-', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('10', 'LB010', '甲功三项', 4, '203', '静脉血', '120.00', 8, 'TSH:0.27-4.2, FT3:3.1-6.8, FT4:12-22',
        'mIU/L, pmol/L', 0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('11', 'LB011', '肿瘤标志物(男)', 4, '203', '静脉血', '300.00', 12, 'AFP:<7, CEA:<5, PSA:<4', 'ng/mL', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('12', 'LB012', '肿瘤标志物(女)', 4, '203', '静脉血', '320.00', 12, 'AFP:<7, CEA:<5, CA125:<35, CA153:<25',
        'ng/mL, U/mL', 0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('13', 'LB013', '凝血功能四项', 4, '203', '静脉血', '80.00', 4, 'PT:11-14, APTT:28-43, FIB:2-4, TT:14-21',
        's, s, g/L, s', 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('14', 'LB014', 'D-二聚体', 4, '203', '静脉血', '60.00', 4, '<0.5', 'mg/L', 1, 0, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('15', 'LB015', 'HIV抗体初筛', 4, '203', '静脉血', '50.00', 12, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('16', 'LB016', '梅毒螺旋体抗体', 4, '203', '静脉血', '40.00', 12, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('17', 'LB017', '丙肝抗体', 4, '203', '静脉血', '40.00', 12, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('18', 'LB018', 'C反应蛋白(CRP)', 4, '203', '静脉血', '30.00', 2, '<10', 'mg/L', 1, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('19', 'LB019', '超敏C反应蛋白(hs-CRP)', 4, '203', '静脉血', '40.00', 4, '<3', 'mg/L', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('20', 'LB020', '降钙素原(PCT)', 4, '203', '静脉血', '120.00', 4, '<0.05', 'ng/mL', 1, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('21', 'LB021', '血培养(需氧+厌氧)', 5, '204', '静脉血', '150.00', 120, '阴性', '-', 1, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('22', 'LB022', '尿培养+药敏', 5, '204', '中段尿', '120.00', 96, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('23', 'LB023', '痰培养+药敏', 5, '204', '痰液', '120.00', 96, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('24', 'LB024', '粪便培养', 5, '204', '粪便', '100.00', 96, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('25', 'LB025', '分泌物培养+药敏', 5, '204', '拭子', '120.00', 96, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('26', 'LB026', '幽门螺杆菌检测(呼气)', 5, '204', '呼气', '100.00', 1, '<100', 'dpm', 0, 1, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('27', 'LB027', '肺炎支原体抗体', 4, '203', '静脉血', '60.00', 8, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('28', 'LB028', '肺炎衣原体抗体', 4, '203', '静脉血', '60.00', 8, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('29', 'LB029', '呼吸道病毒抗原检测', 4, '203', '鼻咽拭子', '150.00', 4, '阴性', '-', 1, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('30', 'LB030', '流感病毒抗原检测', 4, '203', '鼻咽拭子', '120.00', 4, '阴性', '-', 1, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('31', 'LB031', '血型鉴定(ABO+Rh)', 1, '201', '静脉血', '40.00', 2, 'A/B/AB/O型, Rh阳性/阴性', '-', 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('32', 'LB032', '交叉配血试验', 1, '201', '静脉血', '60.00', 4, '相合', '-', 1, 0, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('33', 'LB033', '不规则抗体筛查', 1, '201', '静脉血', '50.00', 4, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('34', 'LB034', '网织红细胞计数', 1, '201', '静脉血', '30.00', 4, '0.5-1.5', '%', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('35', 'LB035', '红细胞沉降率(血沉)', 1, '201', '静脉血', '20.00', 2, '男:0-15, 女:0-20', 'mm/h', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('36', 'LB036', '糖耐量试验(OGTT)', 3, '202', '静脉血', '60.00', 6, '空腹<6.1, 2h<7.8', 'mmol/L', 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('37', 'LB037', '胰岛素释放试验', 4, '203', '静脉血', '150.00', 8, '根据实验室标准', 'mU/L', 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('38', 'LB038', 'C肽释放试验', 4, '203', '静脉血', '150.00', 8, '根据实验室标准', 'ng/mL', 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('39', 'LB039', '同型半胱氨酸', 3, '202', '静脉血', '80.00', 6, '<15', 'umol/L', 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('40', 'LB040', '维生素D测定', 4, '203', '静脉血', '150.00', 8, '>30', 'ng/mL', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('41', 'LB041', '铁蛋白', 4, '203', '静脉血', '60.00', 6, '男:30-400, 女:13-150', 'ng/mL', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('42', 'LB042', '叶酸测定', 4, '203', '静脉血', '80.00', 6, '>3', 'ng/mL', 0, 0, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('43', 'LB043', '维生素B12测定', 4, '203', '静脉血', '80.00', 6, '180-914', 'pg/mL', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('44', 'LB044', '皮质醇测定', 4, '203', '静脉血', '100.00', 8, '早晨:50-280', 'nmol/L', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('45', 'LB045', '促肾上腺皮质激素(ACTH)', 4, '203', '静脉血', '120.00', 8, '早晨:5-78', 'pg/mL', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('46', 'LB046', '生长激素(HGH)', 4, '203', '静脉血', '100.00', 8, '男:<5, 女:<10', 'ng/mL', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('47', 'LB047', '性激素六项', 4, '203', '静脉血', '250.00', 8, '根据性别及生理期不同', '-', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('48', 'LB048', '抗核抗体谱(ANA)', 4, '203', '静脉血', '200.00', 12, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('49', 'LB049', '类风湿因子(RF)', 4, '203', '静脉血', '40.00', 4, '<20', 'IU/mL', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('50', 'LB050', '抗链球菌溶血素O(ASO)', 4, '203', '静脉血', '30.00', 4, '<200', 'IU/mL', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200000', 'LB051', '血脂四项', 3, '201', '静脉血', '45.00', 4, 'TC<5.2, TG<1.7, HDL>1.0, LDL<3.4', 'mmol/L', 1,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200001', 'LB052', '肝功能九项', 3, '201', '静脉血', '68.00', 4, 'ALT<40, AST<40, TBIL<21', 'U/L, umol/L', 1, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200002', 'LB053', '肾功能三项', 3, '201', '静脉血', '35.00', 4, 'CREA<106, BUN<8.2, UA<428', 'umol/L, mmol/L',
        1, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200003', 'LB054', '电解质五项', 3, '201', '静脉血', '38.00', 4, 'K 3.5-5.3, Na 137-147', 'mmol/L', 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200004', 'LB055', '空腹血糖', 3, '201', '静脉血', '8.00', 4, '3.9-6.1', 'mmol/L', 1, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200005', 'LB056', '糖化血红蛋白', 3, '201', '静脉血', '60.00', 4, '4-6', '% HbA1c', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200006', 'LB057', '口服葡萄糖耐量试验(OGTT)', 3, '201', '静脉血', '45.00', 4, '<7.8(2h)', 'mmol/L', 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200007', 'LB058', 'C肽测定', 3, '201', '静脉血', '45.00', 4, '0.8-4.2', 'ng/mL', 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200008', 'LB059', '胰岛素释放试验', 3, '201', '静脉血', '50.00', 4, '2.6-24.9(空腹)', 'uIU/mL', 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200009', 'LB060', '心肌酶谱', 3, '201', '静脉血', '55.00', 4, 'CK<190, CKMB<25', 'U/L, ng/mL', 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200010', 'LB061', '肌钙蛋白', 3, '201', '静脉血', '80.00', 4, '<0.04', 'ng/mL', 1, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200011', 'LB062', 'B型钠尿肽(BNP)', 3, '201', '静脉血', '120.00', 4, '<100', 'pg/mL', 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200012', 'LB063', '同型半胱氨酸', 3, '201', '静脉血', '55.00', 4, '<15', 'umol/L', 0, 1, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200013', 'LB064', '甲状腺功能五项', 4, '201', '静脉血', '150.00', 4, 'TSH 0.55-4.78', 'mIU/L', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200014', 'LB065', '甲状腺功能三项', 4, '201', '静脉血', '95.00', 4, 'TSH 0.55-4.78', 'mIU/L', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200015', 'LB066', '甲状腺过氧化物酶抗体', 4, '201', '静脉血', '45.00', 4, '<60', 'IU/mL', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200016', 'LB067', '性激素六项', 4, '201', '静脉血', '180.00', 4, '依性别及月经周期', '- ng/mL mIU/mL', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200017', 'LB068', '人绒毛膜促性腺激素(HCG)', 4, '201', '静脉血', '50.00', 4, '<5(非孕)', 'mIU/mL', 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200018', 'LB069', '肿瘤标志物十二项', 4, '201', '静脉血', '420.00', 4, 'AFP<7, CEA<5', 'ng/mL', 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200019', 'LB070', '甲胎蛋白(AFP)', 4, '201', '静脉血', '40.00', 4, '<7', 'ng/mL', 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200020', 'LB071', '癌胚抗原(CEA)', 4, '201', '静脉血', '40.00', 4, '<5', 'ng/mL', 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200021', 'LB072', '前列腺特异抗原(PSA)两项', 4, '201', '静脉血', '80.00', 4, '<4', 'ng/mL', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200022', 'LB073', '凝血功能四项+D二聚体', 3, '201', '静脉血', '95.00', 4, 'PT 9.4-12.5s', 's, g/L, ug/mL', 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200023', 'LB074', '血沉', 1, '201', '静脉血', '15.00', 4, '男<15 女<20', 'mm/h', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200024', 'LB075', 'C反应蛋白(CRP)', 1, '201', '静脉血', '25.00', 4, '<10', 'mg/L', 1, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200025', 'LB076', '超敏C反应蛋白', 1, '201', '静脉血', '35.00', 4, '<3', 'mg/L', 1, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200026', 'LB077', '降钙素原(PCT)', 1, '201', '静脉血', '120.00', 4, '<0.05', 'ng/mL', 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200027', 'LB078', '白细胞介素6', 1, '201', '静脉血', '70.00', 4, '<7', 'pg/mL', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200028', 'LB079', '铁蛋白', 4, '201', '静脉血', '40.00', 4, '男30-400 女13-150', 'ng/mL', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200029', 'LB080', '维生素B12', 4, '201', '静脉血', '45.00', 4, '197-771', 'pg/mL', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200030', 'LB081', '叶酸', 4, '201', '静脉血', '45.00', 4, '3.1-20.5', 'ng/mL', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200031', 'LB082', '25羟维生素D', 4, '201', '静脉血', '90.00', 4, '30-100', 'ng/mL', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200032', 'LB083', '乙肝五项(定量)', 4, '201', '静脉血', '95.00', 4, 'HBsAg<0.05', 'IU/mL COI', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200033', 'LB084', '丙肝抗体', 4, '201', '静脉血', '40.00', 4, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200034', 'LB085', '梅毒螺旋体抗体', 4, '201', '静脉血', '30.00', 4, '阴性', '-', 1, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200035', 'LB086', '人类免疫缺陷病毒抗体(HIV)', 4, '201', '静脉血', '40.00', 4, '阴性', '-', 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200036', 'LB087', '幽门螺杆菌抗体', 4, '201', '静脉血', '40.00', 4, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200037', 'LB088', '幽门螺杆菌粪便抗原', 4, '201', '粪便', '45.00', 4, '阴性', '-', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200038', 'LB089', '类风湿因子', 4, '201', '静脉血', '25.00', 4, '<20', 'IU/mL', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200039', 'LB090', '抗链球菌溶血素O', 4, '201', '静脉血', '30.00', 4, '<200', 'IU/mL', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200040', 'LB091', '抗核抗体(ANA)', 4, '201', '静脉血', '60.00', 4, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200041', 'LB092', '血气分析', 3, '201', '动脉血', '70.00', 4, 'pH 7.35-7.45', '-', 1, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200042', 'LB093', '尿微量白蛋白/肌酐比值', 2, '201', '尿液', '35.00', 4, '<30', 'mg/g', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200043', 'LB094', '24小时尿蛋白定量', 2, '201', '24h尿液', '30.00', 4, '<0.15', 'g/24h', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200044', 'LB095', '粪便常规+隐血', 2, '201', '粪便', '25.00', 4, '隐血阴性', '-', 1, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200045', 'LB096', '痰涂片找抗酸杆菌', 5, '201', '痰液', '25.00', 4, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200046', 'LB097', '结核感染T细胞检测', 5, '201', '静脉血', '280.00', 4, '阴性', '-', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200047', 'LB098', '血培养(需氧+厌氧)', 5, '201', '血液', '160.00', 4, '无菌生长', '-', 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200048', 'LB099', '中段尿培养+药敏', 5, '201', '中段尿', '80.00', 4, '无菌生长或菌落<10^4', 'CFU/mL', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200049', 'LB100', '衣原体/支原体培养+药敏', 5, '201', '分泌物', '120.00', 4, '阴性', '-', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200050', 'LB101', '过敏原筛查(吸入+食入)', 4, '201', '静脉血', '220.00', 4, '阴性', '-', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200051', 'LB102', '免疫球蛋白三项', 4, '201', '静脉血', '60.00', 4, 'IgG 7-16', 'g/L', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200052', 'LB103', '补体两项', 4, '201', '静脉血', '50.00', 4, 'C3 0.79-1.52', 'g/L', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200053', 'LB104', '淋巴细胞亚群', 4, '201', '静脉血', '180.00', 4, '见报告', 'cells/uL', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200054', 'LB105', '狼疮抗凝物', 4, '201', '静脉血', '80.00', 4, '阴性', '-', 0, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200055', 'LB106', '药物浓度监测(丙戊酸)', 4, '201', '静脉血', '70.00', 4, '50-100', 'ug/mL', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200056', 'LB107', '地高辛浓度', 4, '201', '静脉血', '60.00', 4, '0.8-2.0', 'ng/mL', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200057', 'LB108', '环孢素浓度', 4, '201', '静脉血', '80.00', 4, '100-250', 'ng/mL', 0, 0, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200058', 'LB109', '撒网式重金属检测(血铅)', 4, '201', '静脉血', '55.00', 4, '<100', 'ug/L', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_laboratory_item (id, item_code, item_name, item_type, dept_id, specimen_type, price, duration,
                                 reference_value, unit, is_emergency, is_fasting, status, create_by, update_by,
                                 del_flag, remark, create_by_id, update_by_id)
VALUES ('200059', 'LB110', '酒精浓度(血)', 4, '201', '静脉血', '30.00', 4, '0', 'mg/dL', 1, 0, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
