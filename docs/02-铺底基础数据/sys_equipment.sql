SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('1', 'EQ001', '超高端128排256层螺旋CT', 1, NULL, '放射科', 'GE', 'Revolution Apex', NULL, '15800000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2', 'EQ002', '64排螺旋CT', 1, NULL, '放射科', '西门子', 'SOMATOM go.Top', NULL, '6500000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('3', 'EQ003', '16排螺旋CT', 1, NULL, '急诊科', '联影', 'uCT 510', NULL, '3200000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('4', 'EQ004', '3.0T磁共振成像系统', 1, NULL, '放射科', '西门子', 'MAGNETOM Vida', NULL, '12800000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('5', 'EQ005', '1.5T磁共振成像系统', 1, NULL, '放射科', '飞利浦', 'Ingenia Ambition', NULL, '7800000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('6', 'EQ006', '数字减影血管造影机(DSA)', 1, NULL, '介入科', '飞利浦', 'Azurion 7', NULL, '15000000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('7', 'EQ007', '乳腺数字钼靶机', 1, NULL, '放射科', 'GE', 'Senographe Pristina', NULL, '2600000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8', 'EQ008', '直接数字化X射线摄影系统(DR)', 1, NULL, '放射科', '联影', 'uDR 580i', NULL, '1200000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('9', 'EQ009', '移动式数字化X射线摄影机(移动DR)', 1, NULL, '放射科', '西门子', 'Mobilett Mira', NULL,
        '850000.00', 1, 365, NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('10', 'EQ010', '床旁移动DR', 1, NULL, 'ICU', '万东', 'PLD7600', NULL, '780000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('11', 'EQ011', '数字化胃肠造影机', 1, NULL, '放射科', '东软', 'NeuViz 30D', NULL, '1800000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('12', 'EQ012', '骨科术中三维C形臂', 1, NULL, '手术室', '西门子', 'Cios Spin', NULL, '2200000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('13', 'EQ013', '高档四维彩色多普勒超声诊断仪', 2, NULL, '超声科', 'GE', 'Voluson E10', NULL, '2800000.00', 1,
        365, NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('14', 'EQ014', '全身彩色多普勒超声诊断仪', 2, NULL, '超声科', '飞利浦', 'EPIQ 7', NULL, '2200000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('15', 'EQ015', '高档心脏彩超', 2, NULL, '超声科', '西门子', 'ACUSON Sequoia', NULL, '1800000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('16', 'EQ016', '便携式彩色多普勒超声', 2, NULL, '急诊科', '迈瑞', 'M9', NULL, '280000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('17', 'EQ017', '床旁超声(掌上超声)', 2, NULL, 'ICU', '迈瑞', 'TEX20', NULL, '120000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('18', 'EQ018', '全自动生化免疫流水线', 2, NULL, '检验科', '贝克曼', 'AU5800+UniCel Dxl', NULL, '8500000.00', 1,
        365, NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('19', 'EQ019', '全自动生化分析仪', 2, NULL, '检验科', '日立', 'LABOSPECT 008AS', NULL, '1800000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('20', 'EQ020', '全自动血细胞分析流水线', 2, NULL, '检验科', '希森美康', 'XN-9000', NULL, '2800000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21', 'EQ021', '全自动血细胞分析仪', 2, NULL, '检验科', '迈瑞', 'BC-6800Plus', NULL, '380000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('22', 'EQ022', '全自动凝血分析仪', 2, NULL, '检验科', '希森美康', 'CS-5100', NULL, '850000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('23', 'EQ023', '全自动化学发光免疫分析仪', 2, NULL, '检验科', '罗氏', 'cobas e801', NULL, '2800000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('24', 'EQ024', '全自动尿液分析仪', 2, NULL, '检验科', '爱威', 'AVE-764', NULL, '180000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('25', 'EQ025', '全自动细菌鉴定药敏分析仪', 2, NULL, '检验科', '梅里埃', 'VITEK 2 Compact', NULL, '650000.00', 1,
        365, NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('26', 'EQ026', '全自动血培养仪', 2, NULL, '检验科', '梅里埃', 'BacT/ALERT 3D 120', NULL, '420000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('27', 'EQ027', '实时荧光定量PCR分析仪', 2, NULL, '检验科', 'ABI', '7500', NULL, '380000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('28', 'EQ028', '基因测序仪', 2, NULL, '检验科', '华大', 'MGISEQ-2000', NULL, '2600000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('29', 'EQ029', '全自动血气分析仪', 2, NULL, '检验科', '雷度', 'ABL90 FLEX', NULL, '280000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('30', 'EQ030', '全自动糖化血红蛋白分析仪', 2, NULL, '检验科', '伯乐', 'D-100', NULL, '380000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('31', 'EQ031', '质谱仪(微生物鉴定)', 2, NULL, '检验科', '梅里埃', 'VITEK MS', NULL, '1200000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('32', 'EQ032', '流式细胞仪', 2, NULL, '检验科', '贝克曼', 'Navios', NULL, '850000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('33', 'EQ033', '多功能呼吸机(高端)', 3, NULL, 'ICU', '德尔格', 'Evita V500', NULL, '680000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('34', 'EQ034', '多功能呼吸机', 3, NULL, 'ICU', '迈柯唯', 'Servo-u', NULL, '520000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('35', 'EQ035', '无创呼吸机', 3, NULL, '呼吸科', '飞利浦', 'V60', NULL, '180000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('36', 'EQ036', '转运呼吸机', 3, NULL, '急诊科', '德尔格', 'Oxylog 3000+', NULL, '180000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('37', 'EQ037', '体外膜肺氧合系统(ECMO)', 3, NULL, 'ICU', '迈柯唯', 'CARDIOHELP', NULL, '2800000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('38', 'EQ038', '血液透析机', 3, NULL, '血透室', '费森尤斯', '4008S', NULL, '180000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('39', 'EQ039', '血液透析滤过机', 3, NULL, '血透室', '贝朗', 'Dialog iQ', NULL, '260000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('40', 'EQ040', '连续性血液净化设备(CRRT)', 3, NULL, 'ICU', '百特', 'PrisMax', NULL, '420000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('41', 'EQ041', '麻醉机(高端)', 4, NULL, '手术室', '德尔格', 'Perseus A500', NULL, '680000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('42', 'EQ042', '麻醉机(标准)', 4, NULL, '手术室', '迈瑞', 'WATO EX-65', NULL, '280000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('43', 'EQ043', '腹腔镜系统(4K荧光)', 4, NULL, '手术室', '史赛克', '1588 AIM', NULL, '2680000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('44', 'EQ044', '宫腔镜系统', 4, NULL, '手术室', '奥林巴斯', 'OTV-S300', NULL, '880000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('45', 'EQ045', '胸腔镜系统', 4, NULL, '手术室', '奥林巴斯', 'Visera Elite III', NULL, '980000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('46', 'EQ046', '输尿管软镜系统', 4, NULL, '手术室', '奥林巴斯', 'URF-V3', NULL, '1280000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('47', 'EQ047', '超声刀系统', 4, NULL, '手术室', '强生', 'GEN11', NULL, '880000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('48', 'EQ048', '电外科能量平台', 4, NULL, '手术室', '蛇牌', 'VIO 3', NULL, '580000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('49', 'EQ049', '手术显微镜(神经外科)', 4, NULL, '手术室', '蔡司', 'KINEVO 900', NULL, '3800000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('50', 'EQ050', '手术显微镜(眼科)', 4, NULL, '手术室', '蔡司', 'ARTEVO 800', NULL, '1680000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('51', 'EQ051', 'C形臂X光机(移动)', 4, NULL, '手术室', 'GE', 'OEC 3D', NULL, '1800000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('52', 'EQ052', '手术动力系统(磨钻)', 4, NULL, '手术室', '美敦力', 'M4', NULL, '580000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('53', 'EQ053', '电刀(高频电外科系统)', 4, NULL, '手术室', '爱尔博', 'VIO 300D', NULL, '380000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('54', 'EQ054', '多参数监护仪(高端)', 5, NULL, 'ICU', '飞利浦', 'IntelliVue MX750', NULL, '180000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('55', 'EQ055', '多参数监护仪', 5, NULL, '病房', '迈瑞', 'uMEC12', NULL, '28000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('56', 'EQ056', '中央监护系统(32床)', 5, NULL, 'ICU', '迈瑞', 'BeneVision N22', NULL, '380000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('57', 'EQ057', '除颤监护仪', 5, NULL, '急诊科', '卓尔', 'ZOLL R系列', NULL, '98000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('58', 'EQ058', '自动体外除颤器(AED)', 5, NULL, '门诊大厅', '迈瑞', 'BeneHeart D1', NULL, '38000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('59', 'EQ059', '心肺复苏机', 5, NULL, '急诊科', '萨勃', 'LUCAS 3', NULL, '268000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('60', 'EQ060', '输液泵', 5, NULL, '病房', '费森尤斯', 'Agilia', NULL, '18000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('61', 'EQ061', '注射泵', 5, NULL, '病房', '费森尤斯', 'Agilia', NULL, '12000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('62', 'EQ062', '营养泵', 5, NULL, '病房', '迈睿', 'MP-100', NULL, '6800.00', 1, 365, NULL, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('63', 'EQ063', '血气分析仪(床旁)', 5, NULL, 'ICU', '雅培', 'i-STAT', NULL, '168000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('64', 'EQ064', '纤维支气管镜(床旁)', 5, NULL, 'ICU', '奥林巴斯', 'BF-P290', NULL, '280000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('65', 'EQ065', '亚低温治疗仪', 5, NULL, 'ICU', '迈松', 'MT-2000', NULL, '68000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('66', 'EQ066', '振动排痰机', 5, NULL, '呼吸科', '健合', 'G5', NULL, '38000.00', 1, 365, NULL, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('67', 'EQ067', '心电图机(十二导)', 5, NULL, '心电图室', '光电', 'ECG-2350', NULL, '38000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('68', 'EQ068', '动态心电图记录盒', 5, NULL, '心电图室', '博英', 'BI9800', NULL, '58000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('69', 'EQ069', '脑电图机(视频脑电)', 5, NULL, '神经内科', '尼高力', 'Nicolet EEG', NULL, '680000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('70', 'EQ070', '经颅多普勒(TCD)', 5, NULL, '神经内科', '德力凯', 'EMS-9PB', NULL, '168000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('71', 'EQ071', '肺功能仪', 5, NULL, '呼吸科', '耶格', 'MasterScreen', NULL, '680000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('72', 'EQ072', '睡眠呼吸监测仪(PSG)', 5, NULL, '呼吸科', '飞利浦', 'Alice 6', NULL, '380000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('73', 'EQ073', '骨密度检测仪(双能X线)', 5, NULL, '内分泌科', 'GE', 'Lunar iDXA', NULL, '1680000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('74', 'EQ074', '听觉诱发电位/耳声发射', 5, NULL, '耳鼻喉科', '尔听美', 'AccuScreen', NULL, '128000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('75', 'EQ075', '视力筛查仪', 5, NULL, '眼科', '伟伦', 'Spot', NULL, '88000.00', 1, 365, NULL, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('76', 'EQ076', '光学相干断层扫描(OCT)', 5, NULL, '眼科', '蔡司', 'Cirrus 6000', NULL, '1280000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('77', 'EQ077', '眼科手术超声乳化仪', 5, NULL, '眼科', '爱尔康', 'Centurion', NULL, '1680000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('78', 'EQ078', '激光治疗仪(钬激光)', 6, NULL, '泌尿外科', '科医人', 'Pulse 120H', NULL, '880000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('79', 'EQ079', '体外冲击波碎石机', 6, NULL, '泌尿外科', '多尼尔', 'Delta III', NULL, '680000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('80', 'EQ080', '下肢康复训练机器人', 6, NULL, '康复科', '程天', 'UGO-Exo', NULL, '980000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('81', 'EQ081', '平衡功能评估训练系统', 6, NULL, '康复科', 'Biodex', 'Balance SD', NULL, '380000.00', 1, 365,
        NULL, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('82', 'EQ082', '上肢康复机器人', 6, NULL, '康复科', '傅利叶', 'ArmMotus M2', NULL, '680000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('83', 'EQ083', '经颅磁刺激仪', 6, NULL, '精神心理科', '依瑞德', 'CCY-1A', NULL, '168000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('84', 'EQ084', '生物反馈治疗仪', 6, NULL, '康复科', '诺诚', 'NeuSenT', NULL, '88000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('85', 'EQ085', '超声波治疗仪', 6, NULL, '康复科', 'HMS', 'CH-2000', NULL, '38000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('86', 'EQ086', '中频脉冲电治疗仪', 6, NULL, '康复科', '好万家', 'ZP-100DIA', NULL, '18000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('87', 'EQ087', '高压氧舱(多人)', 6, NULL, '高压氧室', '宏远', 'HYC-1800', NULL, '1680000.00', 1, 365, NULL,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('88', 'EQ088', '中药熏蒸治疗机', 6, NULL, '中医科', '艾纳', 'XZ-IV', NULL, '68000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('89', 'EQ089', '紫外线治疗仪', 6, NULL, '皮肤科', '沃曼', 'PUVA 8000', NULL, '380000.00', 1, 365, NULL, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_equipment (id, equipment_code, equipment_name, category, dept_id, dept_name, brand, model,
                           purchase_date, purchase_price, status, maintain_cycle_days, last_maintain_date, create_by,
                           update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('90', 'EQ090', '脉冲染料激光治疗仪', 6, NULL, '皮肤科', '赛诺秀', 'Vbeam', NULL, '980000.00', 1, 365,
        '2026-09-24', 'admin', '超级管理员', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
