SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218001', '血常规', '白细胞', '免疫细胞', '看身体里对抗感染的细胞数量',
        '常见于细菌感染或发炎，也可能与应激、某些药物有关', '偏低时抵抗力下降，容易感冒或感染', 1, 10, NULL, 'admin',
        'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218002', '血常规', '白细胞计数', '免疫细胞', '看身体里对抗感染的细胞数量',
        '常见于细菌感染或发炎，也可能与应激、某些药物有关', '偏低时抵抗力下降，容易感冒或感染', 1, 20, NULL, 'admin',
        'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218003', '血常规', '红细胞', '携氧细胞', '负责把氧气送到全身各处的细胞',
        '偏少见，常见于脱水或长期缺氧，需医生判断', '偏低与贫血相关，容易头晕、乏力、脸色苍白', 1, 30, NULL, 'admin',
        'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218004', '血常规', '红细胞计数', '携氧细胞', '负责把氧气送到全身各处的细胞',
        '偏少见，常见于脱水或长期缺氧，需医生判断', '偏低与贫血相关，容易头晕、乏力、脸色苍白', 1, 40, NULL, 'admin',
        'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218005', '血常规', '血红蛋白', '血色素', '看有没有贫血，也就是血够不够用',
        '偏少见，常见于脱水、长期缺氧或吸烟，需医生判断', '偏低通常叫贫血，容易头晕、乏力、脸色苍白', 1, 50, NULL, 'admin',
        'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218006', '血常规', '红细胞压积', '血液浓稠度', '看红细胞在血液里占多少，反映血液浓稠程度',
        '常见于喝水少、腹泻后身体缺水', '偏低与贫血相关', 1, 60, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218007', '血常规', '血小板', '止血细胞', '负责止血和凝血的细胞', '偏高可能增加血栓风险，需要医生判断',
        '偏低时容易出血、淤青不容易消退', 1, 70, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218008', '血常规', '血小板计数', '止血细胞', '负责止血和凝血的细胞', '偏高可能增加血栓风险，需要医生判断',
        '偏低时容易出血、淤青不容易消退', 1, 80, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218009', '血常规', '中性粒细胞百分比', '抗炎主力细胞', '身体对抗细菌感染的主力细胞', '最常见于细菌感染或发炎',
        '偏低时抗感染能力下降', 1, 90, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218010', '血常规', '中性粒细胞绝对值', '抗炎主力细胞', '身体对抗细菌感染的主力细胞', '最常见于细菌感染或发炎',
        '偏低时抗感染能力下降', 1, 100, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218011', '血常规', '淋巴细胞百分比', '抗病毒细胞', '主要对抗病毒感染的免疫细胞', '常见于病毒感染，比如感冒',
        '偏低时需要结合其他指标一起看', 1, 110, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218012', '血常规', '淋巴细胞绝对值', '抗病毒细胞', '主要对抗病毒感染的免疫细胞', '常见于病毒感染，比如感冒',
        '偏低时需要结合其他指标一起看', 1, 120, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218013', '血常规', '单核细胞百分比', '清理细胞', '负责清理病菌和坏死组织的细胞', '可见于感染恢复期或慢性炎症',
        '单独偏低通常意义不大', 1, 130, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218014', '血常规', '单核细胞绝对值', '清理细胞', '负责清理病菌和坏死组织的细胞', '可见于感染恢复期或慢性炎症',
        '单独偏低通常意义不大', 1, 140, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218015', '血常规', '嗜酸性粒细胞百分比', '过敏相关细胞', '与过敏、寄生虫相关的免疫细胞', '常见于过敏、皮疹、哮喘',
        '单独偏低通常意义不大', 1, 150, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218016', '血常规', '嗜酸性粒细胞绝对值', '过敏相关细胞', '与过敏、寄生虫相关的免疫细胞', '常见于过敏、皮疹、哮喘',
        '单独偏低通常意义不大', 1, 160, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218017', '血常规', '嗜碱性粒细胞百分比', '过敏相关细胞', '参与过敏反应，数量很少',
        '比较少见，需要医生结合其他结果判断', '单独偏低通常意义不大', 1, 170, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218018', '血常规', '嗜碱性粒细胞绝对值', '过敏相关细胞', '参与过敏反应，数量很少',
        '比较少见，需要医生结合其他结果判断', '单独偏低通常意义不大', 1, 180, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218019', '血常规', '血常规', '血常规组合', '一次看红细胞、白细胞、血小板等基础指标', '需要看具体哪一项偏高',
        '需要看具体哪一项偏低', 1, 190, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218020', '血常规', '血常规+生化', '血生化组合', '同时看血常规和肝肾功能、血糖等代谢指标',
        '需要看具体哪一项偏高', '需要看具体哪一项偏低', 1, 200, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218021', '肝功能', 'ALT', '转氨酶', '看肝细胞有没有受损的指标', '常见于脂肪肝、肝炎、饮酒或某些药物影响',
        '偏低一般没有临床意义', 1, 210, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218022', '肝功能', 'AST', '转氨酶', '也是看肝细胞损伤的指标，同时存在于肌肉和心脏',
        '可见于肝损伤，也可能来自肌肉或心脏问题', '偏低一般没有临床意义', 1, 220, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218023', '肝功能', '总胆红素', '黄疸指标', '看皮肤或眼白发黄的原因，反映肝脏处理胆红素的能力',
        '可能出现皮肤或眼白发黄，需要医生判断原因', '偏低一般没有临床意义', 1, 230, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218024', '肾功能', '尿素氮', '代谢废物', '看肾脏排出废物的能力', '可见于肾功能下降、身体缺水或高蛋白饮食',
        '偏低可能与营养摄入不足有关，意义需医生判断', 1, 240, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218025', '肾功能', '肌酐', '代谢废物', '评价肾功能最常用的指标', '提示肾脏排泄能力可能下降',
        '偏低常见于肌肉量少的人群，一般意义不大', 1, 250, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218026', '血糖', '空腹血糖', '空腹血糖', '看空腹时血液里的糖，用于筛查糖尿病',
        '偏高需要复查，医生会结合症状判断', '偏低可能出现心慌、出汗、手抖', 1, 260, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218027', '血脂', '总胆固醇', '血脂', '血液里脂肪的总量', '长期偏高会增加血管堵塞的风险', '偏低一般意义不大', 1,
        270, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218028', '血脂', '甘油三酯', '血脂里的油脂', '血液里的油脂，受饮食影响很大', '常见于高油高糖饮食、饮酒、肥胖',
        '偏低一般意义不大', 1, 280, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218029', '血脂', 'LDL-C', '坏胆固醇', '容易沉积在血管壁上的胆固醇，俗称坏胆固醇',
        '偏高会增加心脑血管疾病的风险', '偏低一般是好的', 1, 290, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218030', '血脂', 'HDL-C', '好胆固醇', '帮忙清理血管的胆固醇，俗称好胆固醇', '偏高一般是好的',
        '偏低时血管的保护作用下降', 1, 300, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218031', '血脂', '血脂四项', '血脂组合', '一次看总胆固醇、甘油三酯、好胆固醇、坏胆固醇四项',
        '需要看具体哪一项偏高', '需要看具体哪一项偏低', 1, 310, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218032', '炎症', 'CRP', '炎症指标', '看身体里有没有炎症反应', '提示体内存在炎症或感染',
        '在正常范围说明没有明显的炎症', 1, 320, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218033', '炎症', 'C反应蛋白', '炎症指标', '看身体里有没有炎症反应', '提示体内存在炎症或感染',
        '在正常范围说明没有明显的炎症', 1, 330, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218034', '凝血', 'PT', '凝血时间', '看血液凝固需要多长时间', '凝血变慢，可能容易出血', '凝血偏快，需要医生判断',
        1, 340, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218035', '凝血', 'APTT', '凝血时间', '另一个看凝血功能的指标', '凝血变慢，可能容易出血',
        '单独偏低的意义需医生判断', 1, 350, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218036', '心肌', '肌钙蛋白', '心肌损伤指标', '看心肌有没有受损，常用于排查心脏问题',
        '提示心肌可能受损，需要尽快由医生评估', '低于检出限通常提示没有明显的心肌损伤', 1, 360, NULL, 'admin', 'admin',
        0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218037', '心肌', 'CK-MB', '心肌酶', '辅助判断心肌损伤的酶', '可见于心肌受损，也可能来自肌肉损伤',
        '偏低一般没有临床意义', 1, 370, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218038', '电解质', '血清钾', '血钾', '血液里的钾，关系心跳和肌肉功能', '偏高可能影响心跳，需要医生尽快处理',
        '偏低可能引起乏力、抽筋、心慌', 1, 380, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218039', '尿常规', '尿蛋白', '尿蛋白', '看尿里有没有漏出蛋白质，反映肾脏的过滤功能',
        '提示肾脏过滤功能可能受损，也可能因发热或剧烈运动暂时升高', '在正常范围说明没有明显的蛋白漏出', 1, 390, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218040', '尿常规', '尿糖', '尿糖', '看尿里有没有糖', '尿里有糖时，医生会结合血糖一起判断', '正常是尿里没有糖',
        1, 400, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('218041', '大便', '便常规+隐血', '大便检查', '看大便里有没有血、炎症或寄生虫',
        '隐血阳性提示消化道可能有出血，需要医生进一步判断', '在正常范围说明没有发现异常', 1, 410, NULL, 'admin', 'admin',
        0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011003', '血常规', '中性粒细胞比率', '细菌感染指示', '白细胞中应对细菌感染的主力比例',
        '升高常见于急性细菌感染、应激状态', '偏低可能见于病毒感染或某些药物影响', 1, 30, NULL, 'admin', 'admin', 0, '1',
        '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011004', '血常规', '淋巴细胞比率', '病毒感染指示', '白细胞中应对病毒感染的主力比例',
        '升高常见于病毒感染、部分慢性疾病', '偏低多为一过性，结合白细胞总数判断', 1, 40, NULL, 'admin', 'admin', 0, '1',
        '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011007', '血糖', '糖化血红蛋白', '近3个月血糖平均线', '反映近2—3个月平均血糖水平的指标',
        '偏高提示近阶段血糖控制不佳', '偏低需警惕贫血等因素干扰检测结果', 1, 70, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011008', '肝功能', '谷丙转氨酶', '肝细胞损伤指标', '反映肝细胞受损程度的常用酶学指标',
        '升高常见于脂肪肝、肝炎、饮酒或药物影响', '轻度偏低一般无临床意义', 1, 80, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011009', '肝功能', '谷草转氨酶', '肝心损伤指标', '存在于肝脏与心肌中的酶，升高提示相关细胞损伤',
        '需结合谷丙转氨酶与心脏指标综合判断', '轻度偏低一般无临床意义', 1, 90, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011011', '肾功能', '血肌酐', '肾脏滤过指标', '反映肾脏排泄代谢废物能力的核心指标',
        '持续升高提示肾功能减退，需肾内科评估', '一般无特殊临床意义，瘦体型者可偏低', 1, 110, NULL, 'admin', 'admin', 0,
        '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011013', '肾功能', '血尿酸', '痛风指标', '嘌呤代谢的最终产物，与痛风密切相关',
        '升高需控制高嘌呤饮食，反复关节痛需规范降尿酸治疗', '一般无特殊临床意义', 1, 130, NULL, 'admin', 'admin', 0, '1',
        '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011016', '血脂', '低密度脂蛋白胆固醇', '坏胆固醇', '动脉粥样硬化最主要的危险指标',
        '越高越易形成血管斑块，需按危险分层控制达标', '过低一般无临床意义', 1, 160, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011017', '血脂', '高密度脂蛋白胆固醇', '好胆固醇', '对血管有保护作用的脂蛋白',
        '一般无特殊临床意义', '偏低提示心血管保护作用减弱', 1, 170, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011018', '凝血', 'D-二聚体', '血栓指标', '反映体内血栓形成与溶解活动的敏感指标',
        '明显升高需排查血栓、感染、术后状态等', '一般无特殊临床意义', 1, 180, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_lab_plain_item (id, group_name, item_name, plain_name, what_is_it, high_text, low_text, status,
                                sort_order, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('2360000000000011020', '尿常规', '尿微量白蛋白', '早期肾损伤指标', '反映肾脏早期滤过损伤的敏感指标',
        '升高常见于糖尿病、高血压肾损害早期，建议复查', '一般无特殊临床意义', 1, 200, NULL, 'admin', 'admin', 0, '1',
        '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
