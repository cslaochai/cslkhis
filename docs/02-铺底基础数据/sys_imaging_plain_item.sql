SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232001', '放射', '胸部CT', '胸部CT',
        '用X光从多个角度对胸部做断层扫描，比胸片看得更细，常用于看肺、纵隔和胸壁的情况',
        '检查时需要屏住几秒钟呼吸，请配合技师口令；身上金属物品要提前取下', 1, 10, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232002', '放射', '胸部DR', '胸片（胸部X光）', '用X光给胸部拍一张平面照片，最常用于体检和初步看肺与心脏轮廓',
        '检查时脱掉带金属的内衣和项链，深吸气后屏气拍片', 1, 20, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232003', '放射', '胸部X线', '胸片（胸部X光）', '用X光给胸部拍一张平面照片，最常用于体检和初步看肺与心脏轮廓',
        '检查时脱掉带金属的内衣和项链，深吸气后屏气拍片', 1, 30, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232004', '放射', '头颅CT', '头部CT', '用X光对头部做断层扫描，常用于看颅骨和脑部结构的急症情况',
        '检查时保持不动即可，全程无痛', 1, 40, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232005', '放射', '头颅MRI', '头部核磁共振', '用磁场和无线电波给头部成像，没有X光辐射，看脑部细节比CT更清楚',
        '体内有金属植入物、心脏起搏器必须提前告知医生；检查噪音较大但无痛', 1, 50, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232006', '放射', '颈椎DR', '颈椎X光片', '用X光看颈椎的骨骼排列和椎间隙情况', '检查时按技师要求摆好体位即可', 1,
        60, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232007', '放射', '腰椎DR', '腰椎X光片', '用X光看腰椎骨骼的形态和排列', '检查前一般无需特殊准备', 1, 70, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232008', '放射', '四肢', '肢体X光片', '用X光看四肢骨骼有没有骨折、脱位等形态变化',
        '检查时保持体位不动，全程无痛', 1, 80, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232009', '超声', '腹部彩超', '腹部B超', '用超声波看腹部里肝、胆、脾、肾等器官的形态，没有辐射',
        '做肝胆B超前一般要空腹 6~8 小时，具体听检查通知', 1, 10, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232010', '超声', '肝胆胰脾', '腹部B超', '用超声波看肝、胆、胰、脾这些腹部器官的形态',
        '做检查前一般要空腹 6~8 小时，具体听检查通知', 1, 20, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232011', '超声', '泌尿系', '泌尿系B超', '用超声波看肾、输尿管、膀胱的形态，常用于结石筛查',
        '做膀胱部分通常要憋尿，具体听检查通知', 1, 30, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232012', '超声', '甲状腺', '甲状腺B超', '用超声波看甲状腺的大小和结构，没有辐射', '检查时仰卧抬下巴，全程无痛',
        1, 40, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232013', '超声', '乳腺', '乳腺B超', '用超声波看乳腺组织的结构，没有辐射',
        '检查时需要解开上衣露出胸部，请放松配合', 1, 50, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232014', '超声', '心脏彩超', '心脏彩超（超声心动图）', '用超声波看心脏的结构和泵血功能，没有辐射',
        '左侧卧位或平躺即可，全程无痛', 1, 60, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232015', '超声', '妇产科', '妇科B超', '用超声波看子宫和附件的形态',
        '经腹部检查通常要憋尿，经阴道检查要排空小便，具体听检查通知', 1, 70, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232016', '超声', '血管', '血管彩超', '用超声波看血管通不通畅、血流快慢，没有辐射', '检查前无需特殊准备', 1, 80,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232017', '心电', '心电图', '心电图', '用电极贴在胸口和手脚记录心脏的电活动，看心跳节律和供血线索',
        '检查时平躺放松、不要说话，全程无痛无创', 1, 10, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232018', '心电', '动态心电图', '24小时动态心电图',
        '随身佩戴一台小记录仪，连续记录 24 小时心跳，捕捉偶发的心律问题',
        '记录期间正常活动但避免剧烈运动和弄湿设备，按要求记活动日志', 1, 20, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('232019', '心电', '动态血压', '24小时动态血压监测', '随身佩戴血压计，自动间隔测量 24 小时血压，看全天血压波动',
        '绑袖带的手臂在测量时保持放松，按要求记活动与服药日志', 1, 30, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010002', '放射', '腰椎MRI', '腰椎磁共振', '用磁场对腰椎做多维成像，对椎间盘、神经根显示清楚，无辐射',
        '体内有起搏器、金属植入物者须提前告知；检查约15—20分钟，保持不动', 1, 20, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010003', '放射', '膝关节MRI', '膝关节磁共振', '观察膝关节软骨、半月板、韧带损伤的首选检查，无辐射',
        '检查侧肢体保持伸直不动，扫描约10—15分钟', 1, 30, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010005', '放射', '腹部立位片', '腹部X光(立位)', '立位拍摄腹部平片，主要用于排查肠梗阻与消化道穿孔',
        '需站立位拍摄，行动不便者请告知技师', 1, 50, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010007', '超声', '乳腺彩超', '乳腺彩超', '检查乳腺有无结节、囊肿及结构异常，无辐射',
        '检查上衣穿着方便解开即可，经期前后乳腺胀痛可能影响判断', 1, 70, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010008', '超声', '颈部血管彩超', '颈部血管彩超', '观察颈动脉内膜与血流，评估动脉粥样硬化斑块',
        '无需特殊准备，颈部放松即可', 1, 80, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010009', '超声', '产科彩超', '胎儿(产科)彩超', '观察胎儿发育情况与羊水、胎盘状态，无辐射',
        '孕周不同检查内容不同，请按预约时间到检', 1, 90, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010010', '超声', '下肢血管彩超', '下肢血管彩超',
        '检查下肢动静脉通畅情况与血栓，常用于腿肿、腿痛评估', '无需特殊准备，暴露下肢即可', 1, 100, NULL, 'admin', 'admin',
        0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010011', '心电', '24小时动态心电图', '动态心电图(Holter)',
        '随身携带记录仪连续记录24小时心电，捕捉阵发性心律失常', '检查期间避免剧烈运动与沾水，记录不适症状的时间点', 1,
        110, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010012', '心电', '动态血压监测', '动态血压监测',
        '随身血压计自动定时测量24小时血压，评估血压昼夜规律', '袖带侧手臂避免剧烈活动，按设定时间自动测量即可', 1, 120,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010013', '心电', '常规心电图', '心电图', '记录静息状态下的心脏电活动，排查心律失常与心肌缺血',
        '检查前静坐5分钟，四肢与胸前需贴电极', 1, 130, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010014', '内镜', '胃镜', '胃镜(上消化道内镜)', '经口进入观察食管、胃与十二指肠黏膜，可同时取活检',
        '术前空腹6小时以上；无痛胃镜需家属陪同并评估麻醉', 1, 140, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_imaging_plain_item (id, group_name, item_name, plain_name, what_it_does, notice_text, status,
                                    sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000010015', '内镜', '肠镜', '结肠镜(下消化道内镜)',
        '经肛门进入观察全结肠黏膜，是肠道病变筛查与活检的主要手段', '按医嘱提前口服清肠药至排出清水样便；检查需家属陪同',
        1, 150, NULL, 'admin', 'admin', 0, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
