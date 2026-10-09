SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001001', 'I10.x00', '原发性高血压', 1, '心血管系统', '0', 10, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001002', 'I25.103', '冠状动脉粥样硬化性心脏病', 1, '心血管系统', '0', 20, 1, 0, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001003', 'I21.900', '急性心肌梗死', 1, '心血管系统', '0', 30, 1, 0, '急性期', 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001004', 'I48.x00', '心房颤动', 1, '心血管系统', '0', 40, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001005', 'I50.900', '心力衰竭', 1, '心血管系统', '0', 50, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001006', 'I20.000', '稳定型心绞痛', 1, '心血管系统', '0', 60, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001007', 'I63.900', '脑梗死', 1, '神经系统', '0', 70, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001008', 'I61.902', '脑出血', 1, '神经系统', '0', 80, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001009', 'G43.900', '偏头痛', 1, '神经系统', '0', 90, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001010', 'G47.100', '发作性睡病', 1, '神经系统', '0', 95, 0, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001011', 'E11.900', '2型糖尿病', 1, '内分泌系统', '0', 100, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001012', 'E10.900', '1型糖尿病', 1, '内分泌系统', '0', 110, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001013', 'E04.900', '非毒性结节性甲状腺肿', 1, '内分泌系统', '0', 120, 1, 0, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001014', 'E05.900', '甲状腺功能亢进', 1, '内分泌系统', '0', 130, 1, 0, NULL, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001015', 'E03.900', '甲状腺功能减退', 1, '内分泌系统', '0', 140, 1, 0, NULL, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001016', 'E78.500', '高脂血症', 1, '内分泌系统', '0', 150, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001017', 'E66.900', '肥胖症', 1, '内分泌系统', '0', 160, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001018', 'E28.209', '多囊卵巢综合征', 1, '内分泌系统', '0', 165, 1, 0, NULL, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001019', 'J06.900', '急性上呼吸道感染', 1, '呼吸系统', '0', 170, 1, 0, NULL, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001020', 'J18.900', '肺炎', 1, '呼吸系统', '0', 180, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001021', 'J44.100', '慢性阻塞性肺病急性加重', 1, '呼吸系统', '0', 190, 1, 0, '急性加重期', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001022', 'J44.800', '慢性阻塞性肺病', 1, '呼吸系统', '0', 200, 1, 0, '稳定期', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001023', 'J45.900', '支气管哮喘', 1, '呼吸系统', '0', 210, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001024', 'J42.x00', '慢性支气管炎', 1, '呼吸系统', '0', 220, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001025', 'J15.700', '支原体肺炎', 1, '呼吸系统', '0', 230, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001026', 'A15.000', '肺结核', 1, '呼吸系统', '0', 240, 1, 1, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001027', 'K29.700', '慢性胃炎', 1, '消化系统', '0', 250, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001028', 'K29.300', '慢性浅表性胃炎', 1, '消化系统', '0', 260, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001029', 'K25.700', '胃溃疡', 1, '消化系统', '0', 270, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001030', 'K26.700', '十二指肠溃疡', 1, '消化系统', '0', 280, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001031', 'K35.900', '急性阑尾炎', 1, '消化系统', '0', 290, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001032', 'K80.200', '胆囊结石', 1, '消化系统', '0', 300, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001033', 'K85.900', '急性胰腺炎', 1, '消化系统', '0', 310, 1, 0, '水肿型', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001034', 'K74.600', '肝硬化', 1, '消化系统', '0', 320, 1, 0, '代偿期', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001035', 'K52.904', '急性胃肠炎', 1, '消化系统', '0', 330, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001036', 'K59.900', '功能性肠病', 1, '消化系统', '0', 340, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001037', 'B18.100', '慢性乙型病毒性肝炎', 1, '消化系统', '0', 350, 1, 0, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001038', 'N18.900', '慢性肾脏病', 1, '泌尿系统', '0', 360, 1, 0, '3期', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001039', 'N39.000', '尿路感染', 1, '泌尿系统', '0', 370, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001040', 'N20.000', '肾结石', 1, '泌尿系统', '0', 380, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001041', 'N04.900', '肾病综合征', 1, '泌尿系统', '0', 390, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001042', 'D64.900', '贫血', 1, '血液系统', '0', 400, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001043', 'D50.900', '缺铁性贫血', 1, '血液系统', '0', 410, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001044', 'D69.900', '血小板减少症', 1, '血液系统', '0', 420, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001045', 'D70.900', '白细胞减少症', 1, '血液系统', '0', 430, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001046', 'M54.500', '腰痛', 1, '骨骼肌肉系统', '0', 440, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001047', 'M17.900', '膝骨关节病', 1, '骨骼肌肉系统', '0', 450, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001048', 'M51.202', '腰椎间盘突出症', 1, '骨骼肌肉系统', '0', 460, 1, 0, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001049', 'M10.900', '痛风', 1, '骨骼肌肉系统', '0', 470, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001050', 'M81.900', '骨质疏松', 1, '骨骼肌肉系统', '0', 480, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001051', 'M79.100', '肌痛', 1, '骨骼肌肉系统', '0', 485, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001052', 'S72.001', '股骨颈骨折', 1, '骨骼肌肉系统', '0', 490, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001053', 'S82.201', '胫骨干骨折', 1, '骨骼肌肉系统', '0', 495, 0, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001054', 'L50.900', '荨麻疹', 1, '皮肤与附属器', '0', 500, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001055', 'L20.900', '特应性皮炎', 1, '皮肤与附属器', '0', 510, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001056', 'L03.900', '蜂窝织炎', 1, '皮肤与附属器', '0', 515, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001057', 'L21.900', '脂溢性皮炎', 1, '皮肤与附属器', '0', 517, 0, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001058', 'H10.900', '结膜炎', 1, '眼与附器', '0', 520, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001059', 'H66.900', '中耳炎', 1, '耳与乳突', '0', 530, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001060', 'J02.900', '急性咽炎', 1, '呼吸系统', '0', 540, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001061', 'J03.900', '急性扁桃体炎', 1, '呼吸系统', '0', 550, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001062', 'J31.200', '慢性鼻炎', 1, '呼吸系统', '0', 555, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001063', 'K04.000', '龋齿', 1, '口腔', '0', 560, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001064', 'K05.100', '慢性牙龈炎', 1, '口腔', '0', 565, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001065', 'F32.900', '抑郁发作', 1, '精神与行为', '0', 570, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001066', 'F41.100', '广泛性焦虑障碍', 1, '精神与行为', '0', 580, 1, 0, NULL, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001067', 'G47.000', '入睡和维持睡眠障碍', 1, '神经系统', '0', 590, 1, 0, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001068', 'O82.000', '单胎顺产头位分娩', 1, '妊娠与产科', '0', 600, 1, 0, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001069', 'O24.900', '妊娠期糖尿病', 1, '妊娠与产科', '0', 610, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001070', 'O26.900', '妊娠期其他情况', 1, '妊娠与产科', '0', 615, 0, 0, NULL, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001071', 'P59.900', '新生儿黄疸', 1, '围产儿', '0', 620, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001072', 'P23.900', '新生儿肺炎', 1, '围产儿', '0', 630, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001073', 'J21.900', '急性毛细支气管炎', 1, '呼吸系统', '0', 635, 1, 0, NULL, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001074', 'A09.900', '感染性腹泻', 1, '消化系统', '0', 640, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001075', 'R50.900', '发热待查', 1, '症状与体征', '0', 650, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001076', 'R10.400', '其他和未明确的腹痛', 1, '症状与体征', '0', 660, 1, 0, NULL, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001077', 'R51.x00', '头痛', 1, '症状与体征', '0', 670, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001078', 'R05.x00', '咳嗽', 1, '症状与体征', '0', 680, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001079', 'C34.900', '支气管和肺恶性肿瘤', 1, '肿瘤', '0', 690, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001080', 'C50.900', '乳房恶性肿瘤', 1, '肿瘤', '0', 700, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001081', 'C73.900', '甲状腺恶性肿瘤', 1, '肿瘤', '0', 710, 1, 0, NULL, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_diagnosis (id, diagnosis_code, diagnosis_name, diagnosis_type, category_name, parent_id, sort_order,
                           is_common, is_notifiable, disease_stage, status, create_by, update_by, del_flag, remark,
                           create_by_id, update_by_id)
VALUES ('2360000000000001082', 'C16.900', '胃恶性肿瘤', 1, '肿瘤', '0', 720, 1, 0, NULL, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
