SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400000', 'TR001', '肌肉注射', 1, '105', '6.00', 15, '常规注射操作', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400001', 'TR002', '皮下注射', 1, '105', '6.00', 15, '常规注射操作', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400002', 'TR003', '静脉注射', 1, '105', '8.00', 15, '常规注射操作', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400003', 'TR004', '静脉输液(每组)', 2, '105', '10.00', 15, '密闭式静脉输液', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400004', 'TR005', '静脉留置针穿刺置管', 2, '105', '45.00', 15, '留置针维护每班评估', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400005', 'TR006', 'PICC置管术', 2, '105', '650.00', 15, '超声引导,签署知情同意书', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400006', 'TR007', '中心静脉置管术(CVC)', 2, '105', '580.00', 15, '床旁无菌操作', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400007', 'TR008', '静脉切开术', 2, '105', '400.00', 15, '紧急通路建立', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400008', 'TR009', '头皮静脉穿刺', 1, '105', '10.00', 15, '儿科常规', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400009', 'TR010', '静脉采血', 1, '105', '5.00', 15, '真空采血管采集', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400010', 'TR011', '动脉采血', 1, '105', '12.00', 15, '桡动脉/股动脉穿刺', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400011', 'TR012', '换药(大)', 3, '106', '25.00', 15, '无菌换药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400012', 'TR013', '换药(中)', 3, '106', '15.00', 15, '无菌换药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400013', 'TR014', '换药(小)', 3, '106', '8.00', 15, '无菌换药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400014', 'TR015', '拆线(大)', 4, '106', '20.00', 15, '术后拆线', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400015', 'TR016', '拆线(小)', 4, '106', '10.00', 15, '术后拆线', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400016', 'TR017', '清创缝合(大)', 4, '107', '280.00', 15, '急诊清创,局麻', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400017', 'TR018', '清创缝合(中)', 4, '107', '180.00', 15, '急诊清创,局麻', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400018', 'TR019', '清创缝合(小)', 4, '107', '100.00', 15, '急诊清创,局麻', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400019', 'TR020', '脓肿切开引流术', 4, '107', '220.00', 15, '局麻下切开引流', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400020', 'TR021', '导尿术', 2, '106', '30.00', 15, '无菌导尿', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400021', 'TR022', '留置导尿', 2, '106', '45.00', 15, '气囊固定,护理记录', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400022', 'TR023', '膀胱冲洗', 2, '106', '40.00', 15, '密闭式冲洗', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400023', 'TR024', '灌肠', 2, '106', '25.00', 15, '保留/不保留灌肠', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400024', 'TR025', '胃肠减压', 2, '106', '35.00', 15, '置管接负压引流', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400025', 'TR026', '鼻饲', 2, '106', '15.00', 15, '鼻胃管喂养', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400026', 'TR027', '吸痰', 2, '106', '12.00', 15, '按需吸痰', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400027', 'TR028', '雾化吸入', 2, '106', '10.00', 15, '氧驱动/超声雾化', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400028', 'TR029', '氧气吸入(小时)', 2, '106', '3.00', 15, '鼻导管吸氧', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400029', 'TR030', '无创机械通气(小时)', 2, '108', '20.00', 15, 'NIV参数监测', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400030', 'TR031', '有创机械通气(小时)', 2, '108', '35.00', 15, '呼吸机参数监测', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400031', 'TR032', '心肺复苏', 4, '108', '300.00', 15, 'BLS/ACLS流程', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400032', 'TR033', '电除颤', 4, '108', '80.00', 15, '同步/非同步', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400033', 'TR034', '临时起搏器安置', 4, '108', '1200.00', 15, '床旁紧急起搏', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400034', 'TR035', '心包穿刺术', 4, '107', '300.00', 15, '超声定位下穿刺', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400035', 'TR036', '胸腔穿刺术', 4, '107', '180.00', 15, '超声定位下穿刺', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400036', 'TR037', '腹腔穿刺术', 4, '107', '180.00', 15, '无菌穿刺引流', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400037', 'TR038', '腰椎穿刺术', 4, '107', '220.00', 15, '测压+脑脊液送检', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400038', 'TR039', '骨髓穿刺术', 4, '107', '240.00', 15, '髂后上棘穿刺', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400039', 'TR040', '深静脉血栓物理预防', 5, '106', '40.00', 15, '气压治疗每日', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400040', 'TR041', '红外线治疗', 5, '109', '20.00', 15, '局部照射每日', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400041', 'TR042', '紫外线治疗', 5, '109', '25.00', 15, '局部照射', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400042', 'TR043', '中频脉冲电治疗', 5, '109', '30.00', 15, '每日一次', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400043', 'TR044', '低频脉冲电治疗', 5, '109', '25.00', 15, '每日一次', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400044', 'TR045', '超声波治疗', 5, '109', '30.00', 15, '局部治疗', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400045', 'TR046', '颈腰椎牵引', 5, '109', '35.00', 15, '每日一次', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400046', 'TR047', '运动疗法(次)', 5, '109', '45.00', 15, '康复师指导下', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400047', 'TR048', '作业疗法(次)', 5, '109', '45.00', 15, '康复师指导下', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400048', 'TR049', '言语训练(次)', 5, '109', '50.00', 15, '康复师指导下', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400049', 'TR050', '吞咽功能训练(次)', 5, '109', '48.00', 15, '康复师指导下', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400050', 'TR051', '平衡功能训练(次)', 5, '109', '40.00', 15, '康复师指导下', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400051', 'TR052', '气压治疗(次)', 5, '109', '35.00', 15, '上下肢气压循环', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400052', 'TR053', '中药熏洗', 5, '109', '40.00', 15, '中药熏洗桶', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400053', 'TR054', '针灸(次)', 5, '109', '30.00', 15, '辨证取穴', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400054', 'TR055', '艾灸(次)', 5, '109', '25.00', 15, '温灸', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400055', 'TR056', '拔罐(次)', 5, '109', '22.00', 15, '留罐10分钟', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400056', 'TR057', '推拿(次)', 5, '109', '60.00', 15, '全身/局部', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400057', 'TR058', '穴位注射(次)', 5, '109', '28.00', 15, '药液穴位注射', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400058', 'TR059', '刮痧(次)', 5, '109', '30.00', 15, '介质刮拭', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400059', 'TR060', '耳穴压豆(次)', 5, '109', '20.00', 15, '双侧取穴', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400060', 'TR061', '糖尿病足换药', 3, '106', '60.00', 15, '创面评估+清创', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400061', 'TR062', '压疮护理(次)', 3, '106', '50.00', 15, '分期处理', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400062', 'TR063', '造口护理(次)', 3, '106', '55.00', 15, '造口袋更换', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400063', 'TR064', 'PICC维护(次)', 3, '106', '60.00', 15, '导管冲洗敷贴更换', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400064', 'TR065', '输液港维护(次)', 3, '106', '80.00', 15, '无损伤针穿刺', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400065', 'TR066', '血糖监测(指尖)', 1, '105', '5.00', 15, '末梢血糖', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400066', 'TR067', '动态血糖监测(72h)', 1, '105', '380.00', 15, '连续监测', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400067', 'TR068', '胰岛素泵(日)', 2, '108', '120.00', 15, '持续皮下输注', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400068', 'TR069', '化疗药物输注(组)', 2, '108', '80.00', 15, '避光输注,双人核对', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400069', 'TR070', '输血护理(次)', 2, '106', '20.00', 15, '双人核对,反应观察', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400070', 'TR071', '成分输血(红细胞2U)', 2, '108', '320.00', 15, '交叉配血合格后输注', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400071', 'TR072', '成分输血(血浆200ml)', 2, '108', '240.00', 15, '交叉配血合格后输注', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400072', 'TR073', '成分输血(血小板1治疗量)', 2, '108', '1500.00', 15, '单采血小板', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400073', 'TR074', '冷沉淀(10U)', 2, '108', '380.00', 15, '凝血因子补充', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400074', 'TR075', '血液透析(次)', 2, '108', '420.00', 15, '4小时透析', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400075', 'TR076', '血液灌流(次)', 2, '108', '680.00', 15, '串联灌流器', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400076', 'TR077', '连续性血液净化(CRRT日)', 2, '108', '2600.00', 15, 'ICU床旁', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400077', 'TR078', '腹腔热灌注化疗(次)', 2, '108', '3200.00', 15, '术中/术后', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400078', 'TR079', '高压氧治疗(次)', 5, '109', '120.00', 15, '0.2MPa 60分钟', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('400079', 'TR080', '床旁心电图', 3, '103', '40.00', 15, '床旁十二导联', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('89000000000000301', 'AN001', '全身麻醉', 1, '101', '1200.00', 0, '麻醉诱导+维持+苏醒全过程管理', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('89000000000000302', 'AN002', '椎管内麻醉', 1, '101', '600.00', 0, '腰硬联合/硬膜外麻醉', 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('89000000000000303', 'AN003', '神经阻滞麻醉', 1, '101', '400.00', 0, '超声引导神经阻滞', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('89000000000000304', 'AN004', '局部麻醉', 1, '101', '200.00', 0, '局部浸润麻醉', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('89000000000000305', 'AN005', '其他麻醉', 1, '101', '300.00', 0, '其他镇静镇痛方式', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('89000000000000306', 'AN006', '麻醉监护', 1, '101', '80.00', 0, '按小时计价：心电/血压/呼吸/体温/氧合持续监护',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('89000000000000307', 'AN007', '气管插管术', 1, '101', '150.00', 0, '经口/经鼻气管插管（含可视喉镜）', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('89000000000000308', 'AN008', '麻醉后监测治疗（PACU）', 1, '101', '120.00', 0, '按小时计价：麻醉复苏室监护治疗', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009001', 'TR101', '普通针刺', 5, '19580041', '25.00', 30, '体表穴位针刺治疗', 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009002', 'TR102', '艾灸治疗', 5, '19580041', '20.00', 20, '艾条悬灸', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009003', 'TR103', '拔罐治疗', 5, '19580041', '18.00', 15, '留罐法', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009004', 'TR104', '推拿治疗(颈肩)', 5, '19580041', '45.00', 30, '手法推拿松解', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009005', 'TR105', '推拿治疗(腰背)', 5, '19580041', '50.00', 30, '手法推拿松解', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009006', 'TR106', '中频脉冲电治疗', 5, '19580041', '15.00', 20, '电极片贴敷治疗', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009007', 'TR107', '红外线治疗', 5, '19580041', '10.00', 20, '局部照射', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009008', 'TR108', '蜡疗', 5, '19580041', '30.00', 30, '蜡饼外敷', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009009', 'TR109', '颈椎牵引', 5, '19580041', '20.00', 25, '坐位牵引', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009010', 'TR110', '腰椎牵引', 5, '19580041', '25.00', 25, '仰卧位牵引', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009011', 'TR111', '运动疗法(大关节)', 5, '19580041', '35.00', 40, '主动+被动关节活动训练', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009012', 'TR112', '作业疗法', 5, '19580041', '35.00', 40, '日常生活能力训练', 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009013', 'TR113', '言语训练', 5, '19580041', '40.00', 30, '构音与表达训练', 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009014', 'TR114', '吞咽功能障碍训练', 5, '19580041', '40.00', 30, '吞咽手法+电刺激', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_treatment_item (id, item_code, item_name, item_type, dept_id, price, duration, usage_method, status,
                                create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000009015', 'TR115', '平衡功能训练', 5, '19580041', '35.00', 30, '静态+动态平衡训练', 1, 'admin',
        'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
