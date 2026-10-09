SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600101', 'HEADACHE', '头痛头晕', '头痛、头疼、偏头痛、头晕、眩晕、脑袋疼、头昏', '19580006', '神经内科', 80, 0,
        '突发剧烈头痛或伴呕吐、意识改变请直接到急诊', 1, 10, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600102', 'COUGH', '咳嗽咳痰', '咳嗽、咳痰、气喘、喘不上气、胸闷伴咳、久咳', '19580003', '呼吸内科', 80, 0, '', 1,
        20, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600103', 'CHEST_PAIN', '胸痛心悸', '胸痛、胸闷、心慌、心悸、胸口疼、心口疼', '19580005', '心血管内科', 90, 0,
        '持续胸痛超过15分钟请立即到急诊', 1, 30, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600104', 'ABDO_PAIN', '腹痛腹泻', '腹痛、肚子疼、腹泻、拉肚子、反酸、烧心、恶心、呕吐、便秘', '19580004', '消化内科',
        80, 0, '', 1, 40, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600105', 'FEVER', '发热', '发热、发烧、体温高、发冷、寒战', '19580068', '发热门诊', 70, 0, '', 1, 50, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600106', 'FEVER', '发热', '低烧、午后低热', '19580086', '全科医学科', 50, 0, '', 1, 51, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600107', 'GLUCOSE', '血糖异常', '血糖高、糖尿病、多饮、多尿、消瘦、糖耐量', '19580007', '内分泌科', 80, 0, '', 1,
        60, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600108', 'THYROID', '甲状腺乳腺', '甲状腺、脖子肿、乳腺、乳房肿块、乳房疼', '19580087', '甲乳外科', 80, 0, '', 1,
        70, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600109', 'URINE', '泌尿不适', '尿频、尿急、尿痛、血尿、排尿困难、肾结石、腰痛伴尿血', '19580017', '泌尿外科', 80,
        0, '', 1, 80, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600110', 'BONE_JOINT', '骨关节痛', '骨折、摔伤、关节痛、腰腿痛、脖子疼、肩膀疼、扭伤、骨质疏松', '19580014', '骨科',
        80, 0, '', 1, 90, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600111', 'SKIN', '皮肤问题', '皮疹、瘙痒、长痘、湿疹、荨麻疹、皮肤红肿、脱发、痣', '19580033', '皮肤科', 80, 0, '',
        1, 100, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600112', 'EYE', '眼部不适', '眼睛红、视力下降、眼睛疼、眼干、眼屎多、看不清', '19580030', '眼科', 80, 0, '', 1,
        110, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600113', 'ENT', '耳鼻喉不适', '鼻塞、流鼻涕、耳鸣、耳朵疼、听力下降、咽痛、嗓子疼、打鼾', '19580031', '耳鼻喉科',
        80, 0, '', 1, 120, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600114', 'TOOTH', '口腔牙痛', '牙痛、牙疼、牙龈肿、补牙、拔牙、口腔溃疡', '19580032', '口腔科', 80, 0, '', 1, 130,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600115', 'GYNE', '妇科不适', '月经不调、白带异常、外阴瘙痒、妇科检查、阴道、宫颈', '19580022', '妇科', 80, 0, '',
        1, 140, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600116', 'PREGNANT', '怀孕产检', '怀孕、产检、孕期、胎动、备孕', '19580023', '产科', 80, 0, '', 1, 150, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600117', 'CHILD', '儿童不适', '孩子发烧、小孩咳嗽、宝宝腹泻、儿童、小儿、婴幼儿', '19580026', '小儿内科', 80, 0,
        '', 1, 160, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600118', 'MOOD', '情绪睡眠', '失眠、睡不着、焦虑、抑郁、情绪低落、压力大、心理咨询', '19580065', '临床心理科', 70,
        0, '', 1, 170, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600119', 'MOOD', '情绪睡眠', '打鼾、睡眠呼吸暂停、嗜睡', '19580066', '睡眠医学中心', 60, 0, '', 1, 171,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600120', 'WEAK', '乏力贫血', '乏力、没劲、贫血、面色苍白、头晕伴乏力、体重下降', '19580086', '全科医学科', 60, 0,
        '', 1, 180, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600121', 'LIVER', '肝病', '乙肝、肝功异常、转氨酶高、肝区不适、黄疸、脂肪肝', '19580070', '肝病科', 80, 0, '', 1,
        190, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600122', 'ANUS', '肛肠不适', '便血、痔疮、肛门疼、肛门瘙痒、排便困难', '19580088', '肛肠外科', 80, 0, '', 1, 200,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600123', 'CHECKUP', '体检', '体检、健康查体、入职体检、年度体检', '19580071', '健康管理中心', 70, 0, '', 1, 210,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600124', 'TCM', '中医调理', '中医、调理、推拿、针灸、颈肩痛、腰肌劳损、体质调理', '19580055', '针灸推拿科', 60, 0,
        '', 1, 220, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600125', 'INFECT', '传染腹泻', '腹泻伴发热、诺如、手足口、传染、腮腺炎', '19580069', '肠道门诊', 70, 0, '', 1,
        230, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600201', 'EMERG_CHEST', '急性胸痛', '胸痛剧烈、压榨性胸痛、胸痛大汗、呼吸困难、喘不上气、窒息', '19580048',
        '急诊内科', 100, 1, '疑似心脑血管急症，请立即前往急诊或拨打120', 1, 10, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600202', 'EMERG_NEURO', '中风信号', '嘴歪、半身无力、说话不清、偏瘫、意识不清、昏迷、抽搐、晕倒', '19580048',
        '急诊内科', 100, 1, '疑似卒中，请立即前往急诊或拨打120', 1, 11, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600203', 'EMERG_BLEED', '大出血', '大出血、呕血、咯血、便血不止、外伤出血不止', '19580049', '急诊外科', 100, 1,
        '请立即前往急诊或拨打120', 1, 12, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600204', 'EMERG_TRAUMA', '急性外伤', '摔伤出血、车祸、高处坠落、开放性伤口、骨折畸形', '19580049', '急诊外科',
        100, 1, '请立即前往急诊', 1, 13, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_triage_rule (id, symptom_code, symptom_name, keywords, dept_id, dept_name, weight, urgent_flag, advice,
                             status, sort_order, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21600205', 'EMERG_CHILD', '儿童急症', '高热惊厥、小孩抽搐、孩子抽搐、宝宝抽搐、孩子喘不上气', '19580051',
        '急诊儿科', 100, 1, '儿童急症请立即前往急诊儿科', 1, 14, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
