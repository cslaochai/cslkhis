SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010001', '华法林', '阿司匹林', '华法林&阿司匹林', 1,
        '抗凝药与抗血小板药叠加，消化道及颅内出血风险显著升高',
        '避免联用；确有抗栓指征须由抗凝门诊评估，缩短 INR 复测间隔并加用质子泵抑制剂', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010002', '华法林', '胺碘酮', '华法林&胺碘酮', 1,
        '胺碘酮抑制 CYP2C9 与 P-糖蛋白，华法林血药浓度及 INR 明显升高',
        '联用时华法林减量 30%~50%，3~7 天内复测 INR；停用胺碘酮后须再上调', 1, 'admin', '超级管理员', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010003', '华法林', '氟康唑', '华法林&氟康唑', 1,
        '三唑类抗真菌药强抑制 CYP2C9，INR 骤升可致严重出血',
        '联用需华法林减量并加密监测 INR，或换用对 CYP2C9 影响小的抗真菌药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010004', '华法林', '甲硝唑', '华法林&甲硝唑', 1, '甲硝唑抑制华法林代谢，抗凝作用增强',
        '联用期间复测 INR，酌情减量华法林', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010005', '华法林', '维生素K1', '华法林&维生素K1', 1,
        '维生素K1 直接拮抗华法林抗凝作用，INR 下降致抗凝失败、血栓风险',
        '非出血抢救需要不得联用；已使用须重估抗凝方案并复测 INR', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010006', '华法林', '苯妥英钠', '华法林&苯妥英钠', 1,
        '蛋白结合置换与酶诱导并存，INR 双向剧烈波动，抗凝不可控', '换用不受酶诱导影响的抗癫痫药，或改用低分子肝素', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010007', '华法林', '利福平', '利福平&华法林', 1,
        '利福平强诱导肝药酶，华法林代谢加速致抗凝作用消失、血栓风险',
        '避免联用；必须抗结核治疗时全程严密监测 INR 并大幅调整剂量', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010008', '硝酸', '西地那非', '硝酸&西地那非', 1,
        '硝酸酯类与 5 型磷酸二酯酶抑制剂联用致严重低血压、晕厥甚至心肌梗死',
        '24 小时内（用他达拉非者 48 小时）禁止联用；胸痛急救须主动告知医师已服用 PDE5 抑制剂', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010009', '沙库巴曲缬沙坦', '普利', '普利&沙库巴曲缬沙坦', 1,
        '脑啡肽酶抑制剂与 ACEI 联用使缓激肽积聚，血管性水肿风险升高且降压叠加',
        '停用 ACEI 后至少间隔 36 小时方可启动沙库巴曲缬沙坦，禁止两药同日服用', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010010', '利奈唑胺', '帕罗西汀', '利奈唑胺&帕罗西汀', 1,
        '利奈唑胺具弱单胺氧化酶抑制作用，与 SSRI 联用可致 5-羟色胺综合征（高热、肌阵挛、意识障碍）',
        '避免联用；必须抗感染时暂停 SSRI 并观察至停药后 2 周', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010011', '利奈唑胺', '氟西汀', '利奈唑胺&氟西汀', 1,
        '利奈唑胺与 SSRI 联用可致 5-羟色胺综合征，氟西汀半衰期长风险持续更久',
        '避免联用；换用不具 MAOI 作用的抗革兰阳性菌药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010012', '司来吉兰', '氟西汀', '司来吉兰&氟西汀', 1,
        'MAO-B 抑制剂与 SSRI 联用致 5-羟色胺综合征、高热、惊厥', '两药切换须间隔 5 周以上（氟西汀洗脱期长），禁止同期服用',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010013', '司来吉兰', '右美沙芬', '右美沙芬&司来吉兰', 1,
        'MAO-B 抑制剂与右美沙芬联用可致精神症状、呼吸抑制', '镇咳换用氨溴索等不含 5-羟色胺能作用的药物', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010014', '可待因', '地西泮', '可待因&地西泮', 1,
        '阿片类与苯二氮类叠加致深度镇静、呼吸抑制，已有致死报道',
        '避免联用；必须联用时取两药最小剂量、最短疗程并监测呼吸与意识', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010015', '吗啡', '地西泮', '吗啡&地西泮', 1, '阿片类与苯二氮类注射剂联用呼吸抑制风险极高',
        '避免联用；术前镇静须由麻醉医师调整剂量并在监护下给药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010016', '吗啡', '咪达唑仑', '吗啡&咪达唑仑', 1, '阿片类与苯二氮类叠加致呼吸抑制、低血压',
        '禁止在无监护条件下同时静脉给药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010017', '克拉霉素', '辛伐他汀', '克拉霉素&辛伐他汀', 1,
        '克拉霉素强抑制 CYP3A4，辛伐他汀血药浓度骤升可致横纹肌溶解、急性肾衰竭',
        '抗感染疗程内停用辛伐他汀，或降脂药换瑞舒伐他汀/依折麦布、抗菌药换阿奇霉素', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010018', '克拉霉素', '阿托伐他汀', '克拉霉素&阿托伐他汀', 1,
        'CYP3A4 受抑使他汀蓄积，肌病与横纹肌溶解风险升高', '联用期间暂停他汀，或他汀减至低剂量并嘱肌痛即报', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010019', '伊曲康唑', '辛伐他汀', '伊曲康唑&辛伐他汀', 1,
        '唑类抗真菌药强抑制 CYP3A4，他汀暴露量显著升高致横纹肌溶解', '联用期间停用辛伐他汀', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010020', '克拉霉素', '秋水仙碱', '克拉霉素&秋水仙碱', 1,
        '克拉霉素抑制 P-糖蛋白与 CYP3A4，秋水仙碱蓄积致骨髓抑制、肌病，老年及肾功能减退者可致死',
        '避免联用；必须抗感染时秋水仙碱大幅减量并缩短疗程，密切查血常规与肌酶', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010021', '头孢曲松', '葡萄糖酸钙', '头孢曲松&葡萄糖酸钙', 1,
        '头孢曲松与含钙溶液形成不溶性头孢曲松钙沉淀，可在肺、肾沉积（新生儿禁忌最严）',
        '禁止经同一输液通路同时给药；新生儿 48 小时内不得先后使用两药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010022', '头孢曲松', '碳酸钙', '头孢曲松&碳酸钙', 1,
        '头孢曲松与钙离子形成不溶性沉淀，存在器官沉积风险', '静脉用头孢曲松期间口服钙剂须错开给药时间窗并复评', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010023', '万古霉素', '呋塞米', '万古霉素&呋塞米', 1,
        '两药均有耳毒性与肾毒性，叠加可致不可逆听力损害', '避免联用；必须联用时监测听力、血药浓度与肾功能，呋塞米缓慢静推',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010024', '硫唑嘌呤', '别嘌醇', '别嘌醇&硫唑嘌呤', 1,
        '别嘌醇抑制黄嘌呤氧化酶，硫唑嘌呤代谢受阻致严重骨髓抑制、全血细胞减少',
        '禁止常规联用；确需联用须将硫唑嘌呤减至原剂量 1/3 以下并密切查血常规，或换用其他降尿酸药', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010025', '甲氨蝶呤', '磺胺', '甲氨蝶呤&磺胺', 1,
        '磺胺类置换蛋白结合并叠加叶酸代谢抑制，可致全血细胞减少、口腔黏膜炎',
        '避免联用；需抗感染时换用非磺胺类，联用期间每周查血常规', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010026', '甲氧氯普胺', '氯丙嗪', '氯丙嗪&甲氧氯普胺', 1,
        '多巴胺受体阻断作用叠加，锥体外系反应与迟发性运动障碍风险显著升高', '止吐换用昂丹司琼等 5-HT3 拮抗剂', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010027', '胺碘酮', '氟哌啶醇', '氟哌啶醇&胺碘酮', 1, '两药均延长 QT 间期，叠加可致尖端扭转型室速',
        '避免联用；必须时持续心电监测并先纠正低钾低镁', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010028', '红霉素', '卡马西平', '卡马西平&红霉素', 1,
        '红霉素抑制 CYP3A4，卡马西平血药浓度升高致中毒（眩晕、复视、共济失调）',
        '抗菌药换为阿奇霉素，或卡马西平减量并测血药浓度', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010029', '克拉霉素', '卡马西平', '克拉霉素&卡马西平', 1, '强 CYP3A4 抑制使卡马西平蓄积中毒',
        '避免联用，换用阿奇霉素等不抑制 CYP3A4 的大环内酯类', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010030', '氟哌啶醇', '西酞普兰', '氟哌啶醇&西酞普兰', 1,
        '两药均延长 QT 间期，且 SSRI 抑制 CYP2D6 使氟哌啶醇浓度升高', '避免联用；必须时查基线心电与电解质并加密复查', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010041', '奥美拉唑', '氯吡格雷', '奥美拉唑&氯吡格雷', 2,
        '奥美拉唑抑制 CYP2C19，氯吡格雷活性代谢物生成减少、抗血小板作用下降', '需抑酸时换用泮托拉唑或雷贝拉唑', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010042', '布洛芬', '阿司匹林', '布洛芬&阿司匹林', 2,
        '布洛芬竞争 COX-1 结合位点削弱阿司匹林抗血小板作用，且消化道损伤叠加',
        '避免长期联用；必须时段开服用（阿司匹林晨服，布洛芬间隔 6~8 小时）并监测消化道症状', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010043', '氢氯噻嗪', '地高辛', '地高辛&氢氯噻嗪', 2, '利尿致低血钾，低钾显著增加地高辛中毒风险',
        '补钾并监测血钾、心率与地高辛血药浓度', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010044', '呋塞米', '地高辛', '呋塞米&地高辛', 2, '强效利尿致低钾，增加洋地黄中毒与心律失常风险',
        '注意补钾并复查电解质', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010045', '胺碘酮', '地高辛', '地高辛&胺碘酮', 2,
        '胺碘酮抑制 P-糖蛋白，地高辛血药浓度可升高约 70% 致中毒', '地高辛减量约 50% 并监测浓度、心率与传导', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010046', '胺碘酮', '美托洛尔', '美托洛尔&胺碘酮', 2,
        '负性频率与负性传导作用叠加，严重心动过缓、传导阻滞风险升高',
        'β受体阻滞剂减量并监测心率，心率低于 50 次/分须复核方案', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010047', '胺碘酮', '沙星', '沙星&胺碘酮', 2, '胺碘酮与喹诺酮类均延长 QT 间期，叠加致室性心律失常',
        '换用不延长 QT 的抗菌药，或查心电并纠正电解质', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010048', '缬沙坦', '螺内酯', '缬沙坦&螺内酯', 2,
        'ARB 与保钾利尿剂叠加致高钾血症，肾功能不全者风险更高', '查血钾与肌酐，避免同时使用补钾制剂', 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010049', '氯化钾', '螺内酯', '氯化钾&螺内酯', 2, '补钾与保钾利尿剂同用易致高钾血症',
        '监测血钾与肾功能，肌酐清除率下降时应停口服补钾', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010050', '沙星', '碳酸钙', '沙星&碳酸钙', 2, '多价阳离子与喹诺酮类螯合，抗菌药吸收明显下降',
        '两药服用间隔 2 小时以上，或治疗期间暂停钙剂', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010051', '沙星', '铝碳酸镁', '沙星&铝碳酸镁', 2, '铝镁离子与喹诺酮螯合致血药浓度不足，治疗失败风险',
        '间隔 2 小时以上服用，或抗感染期间改用其他胃药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010052', '蒙脱石', '地高辛', '地高辛&蒙脱石', 2, '蒙脱石吸附作用降低地高辛吸收，血药浓度不足',
        '两药间隔 2 小时以上，并注意洋地黄疗效变化', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010053', '格列美脲', '磺胺', '格列美脲&磺胺', 2,
        '磺胺类置换蛋白结合并抑制代谢，磺脲类降糖作用增强致低血糖', '监测血糖，酌情下调降糖药剂量', 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010054', '格列美脲', '氟康唑', '格列美脲&氟康唑', 2,
        '氟康唑抑制 CYP2C9，磺脲类蓄积可致持续低血糖（老年患者尤甚）', '监测血糖并减量，必要时换用不依赖 CYP2C9 的降糖药',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010055', '泼尼松', '二甲双胍', '二甲双胍&泼尼松', 2, '糖皮质激素升高血糖，降糖方案相对不足',
        '加强血糖监测，必要时增加降糖药或改用胰岛素', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010056', '地塞米松', '二甲双胍', '二甲双胍&地塞米松', 2, '糖皮质激素的升糖作用拮抗降糖疗效',
        '激素疗程内加密监测空腹与餐后血糖', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010057', '碳酸钙', '左甲状腺素钠', '左甲状腺素钠&碳酸钙', 2,
        '钙盐与左甲状腺素在胃肠道螯合，吸收下降致甲功控制不佳', '两药间隔 4 小时以上服用，6~8 周后复查 TSH', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010058', '骨化三醇', '碳酸钙', '碳酸钙&骨化三醇', 2,
        '活性维生素D与钙剂叠加可致高钙血症（口渴、多尿、便秘、心律失常）', '监测血钙与尿钙，出现症状即停钙剂', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010059', '奥美拉唑', '硫酸亚铁', '奥美拉唑&硫酸亚铁', 2,
        '胃酸分泌减少使铁剂还原吸收受阻，补铁疗效下降', '延长疗程或改用注射铁剂，复查血常规与铁蛋白评估疗效', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010060', '茶碱', '沙星', '沙星&茶碱', 2,
        '喹诺酮类抑制 CYP1A2，茶碱类清除率下降致中毒（恶心、心律失常、惊厥）',
        '茶碱减量约 1/3 并测血药浓度，或换用对茶碱影响小的抗菌药（多索茶碱受影响较小仍须复核）', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010061', '丹参', '华法林', '丹参&华法林', 2, '丹参（含复方丹参滴丸）增强华法林抗凝作用，INR 升高',
        '加密监测 INR，中成药加减时按调整抗凝药对待', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010062', '银杏叶', '华法林', '华法林&银杏叶', 2,
        '银杏叶抑制血小板活化因子，与抗凝药叠加增加出血风险', '避免长期同用，监测出血征象与 INR', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010063', '他克莫司', '氟康唑', '他克莫司&氟康唑', 2,
        '唑类抑制 CYP3A4，他克莫司浓度骤升致肾毒性、震颤', '他克莫司大幅减量并按血药浓度调整，监测肌酐与尿量', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010064', '别嘌醇', '普利', '别嘌醇&普利', 2,
        'ACEI 与别嘌醇合用，过敏及粒细胞减少风险升高（肾功能不全者更明显）', '换用其他降压药，或定期查血常规并监测肾功能',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010065', '戊酸雌二醇', '利福平', '利福平&戊酸雌二醇', 2,
        '酶诱导加速雌激素代谢，避孕效果下降并出现突破性出血', '加用屏障避孕法，或换用不受酶诱导影响的避孕方案', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010066', '洛芬', '甲氨蝶呤', '洛芬&甲氨蝶呤', 2,
        '非甾体抗炎药减少甲氨蝶呤肾排泄，血药浓度升高致骨髓抑制与肾损伤',
        '监测血常规与肾功能，大剂量甲氨蝶呤期间避免联用', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010067', '沙星', '地塞米松', '地塞米松&沙星', 2,
        '喹诺酮类与糖皮质激素合用增加肌腱炎、肌腱断裂风险（老年及肾功能不全者尤甚）',
        '告知患者跟腱疼痛立即停药，尽量避免联用', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010068', '丙戊酸钠', '卡马西平', '丙戊酸钠&卡马西平', 2,
        '两药相互诱导代谢，各自有效浓度下降且肝毒性叠加', '监测两药血药浓度与肝功能，按浓度调整剂量', 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010069', '曲马多', '帕罗西汀', '帕罗西汀&曲马多', 2, '5-羟色胺能作用叠加并可降低癫痫阈值',
        '避免联用；镇痛换用其他方案', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010070', '吡格列酮', '胰岛素', '吡格列酮&胰岛素', 2,
        '噻唑烷二酮与胰岛素联用增加水钠潴留、水肿与心力衰竭风险', '监测体重与心功能，心功能Ⅲ级及以上避免联用', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010071', '氢氯噻嗪', '别嘌醇', '别嘌醇&氢氯噻嗪', 2, '噻嗪类利尿剂升高血尿酸，拮抗降尿酸疗效',
        '按血尿酸调整别嘌醇剂量，或换用其他降压药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010072', '氯丙嗪', '地西泮', '地西泮&氯丙嗪', 2, '中枢抑制叠加，过度镇静、低血压与呼吸抑制风险升高',
        '减量并避免同时静脉给药，老年患者尤须监测', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010073', '铝碳酸镁', '左甲状腺素钠', '左甲状腺素钠&铝碳酸镁', 2,
        '铝盐与氢氧化镁影响左甲状腺素吸收，甲功波动', '间隔 4 小时以上服用，复查 TSH 评估剂量', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010074', '地高辛', '铝碳酸镁', '地高辛&铝碳酸镁', 2, '抗酸剂降低地高辛吸收，疗效不足',
        '间隔 2 小时以上服药，必要时测地高辛浓度', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010075', '辛伐他汀', '非诺贝特', '辛伐他汀&非诺贝特', 2,
        '他汀与贝特类联用肌病与横纹肌溶解风险升高', '分开时间给药、他汀取低剂量，嘱肌痛乏力即报并查肌酶', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010076', '瑞舒伐他汀', '非诺贝特', '瑞舒伐他汀&非诺贝特', 2,
        '联用增加肌病风险，且相互作用使他汀浓度升高', '他汀从低剂量起始并限制增量，定期查肌酶', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010077', '伏立康唑', '利伐沙班', '伏立康唑&利伐沙班', 2,
        'CYP3A4 与 P-糖蛋白受抑使利伐沙班暴露量升高，出血风险增加', '加强出血征象监测，老年与肾功能减退者考虑换方案', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010078', '螺内酯', '普利', '普利&螺内酯', 2, '保钾利尿剂与 ACEI 叠加致高钾血症',
        '监测血钾与肌酐，避免同用补钾制剂或非甾体抗炎药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010079', '氢氯噻嗪', '普利', '普利&氢氯噻嗪', 2, '利尿剂致容量不足，起始 ACEI 时首剂低血压风险升高',
        'ACEI 从低剂量起始、睡前服，或停利尿剂 2~3 天后再启用', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010080', '阿托品', '地高辛', '地高辛&阿托品', 2,
        '阿托品改变胃肠动力使地高辛吸收波动，且两者对心脏传导的作用相互干扰', '监测地高辛浓度与心率，注意洋地黄中毒表现',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010081', '酮替芬', '地西泮', '地西泮&酮替芬', 2, '镇静作用叠加，日间嗜睡与跌倒风险升高（老年患者）',
        '睡前给药并提醒避免驾驶，必要时换用无镇静作用的抗组胺药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010082', '蒙脱石', '沙星', '沙星&蒙脱石', 2, '蒙脱石的吸附作用降低喹诺酮类吸收，抗菌疗效不足',
        '两药间隔 1~2 小时以上服用', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000010083', '氯丙嗪', '沙星', '氯丙嗪&沙星', 2, '两药均可延长 QT 间期，叠加致室性心律失常',
        '查基线心电与电解质，出现心悸晕厥立即停药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006001', '地高辛', '呋塞米', '地高辛&呋塞米', 2, '袢利尿剂致低钾血症，增加地高辛中毒风险',
        '监测血钾与地高辛浓度，常规补钾或联用保钾利尿剂', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006003', '阿托伐他汀', '克拉霉素', '阿托伐他汀&克拉霉素', 1, 'CYP3A4强抑制，横纹肌溶解风险升高',
        '克拉霉素期间暂停他汀或换用瑞舒伐他汀', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006004', '辛伐他汀', '胺碘酮', '辛伐他汀&胺碘酮', 1, '辛伐他汀日剂量超20mg时肌病风险显著升高',
        '限辛伐他汀≤20mg/日，监测CK', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006005', '二甲双胍', '碘对比剂', '二甲双胍&碘对比剂', 2, '对比剂肾病风险叠加乳酸酸中毒风险',
        '增强检查当日停用，48小时后复查肾功能正常再恢复', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006006', '依那普利', '螺内酯', '依那普利&螺内酯', 2, 'ACEI与保钾利尿剂叠加致高钾血症',
        '基线及1周内复测血钾，避免再联用补钾剂', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006007', '氯吡格雷', '奥美拉唑', '氯吡格雷&奥美拉唑', 2, 'CYP2C19抑制削弱氯吡格雷活化',
        '换用泮托拉唑或雷贝拉唑', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006008', '硝苯地平', '利福平', '硝苯地平&利福平', 1, '利福平强诱导CYP3A4，降压作用显著减弱',
        '换用不受诱导影响的降压药并加强血压监测', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006009', '华法林', '左氧氟沙星', '华法林&左氧氟沙星', 2, '肠道菌群破坏与CYP抑制，INR升高出血风险',
        '联用期间3天复测INR，必要时减量', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006010', '茶碱', '环丙沙星', '茶碱&环丙沙星', 1, '环丙沙星抑制茶碱代谢致心悸、抽搐风险',
        '茶碱减量1/3~1/2，有条件监测血药浓度', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006011', '甲氨蝶呤', '布洛芬', '甲氨蝶呤&布洛芬', 1, 'NSAID减少甲氨蝶呤肾排泄，骨髓抑制风险',
        '避免大剂量联用，监测血常规', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006012', '碳酸锂', '氢氯噻嗪', '碳酸锂&氢氯噻嗪', 1, '噻嗪类升锂浓度，锂中毒风险',
        '锂剂量下调，规律监测血锂', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006013', '西沙必利', '红霉素', '西沙必利&红霉素', 1, '叠加QT延长，尖端扭转型室速风险',
        '禁止联用，换用其他胃动力药', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006014', '格列本脲', '环丙沙星', '格列本脲&环丙沙星', 2, '氟喹诺酮增强磺脲类降糖，低血糖风险',
        '加强血糖监测，告知低血糖识别与处理', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006015', '缬沙坦', '氯化钾缓释片', '缬沙坦&氯化钾缓释片', 2, 'ARB类减少钾排泄，高钾血症风险',
        '一般不常规补钾，联用须监测血钾', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006016', '泼尼松', '布洛芬', '泼尼松&布洛芬', 2, '糖皮质激素与NSAID叠加消化道溃疡出血风险',
        '加用质子泵抑制剂保护胃黏膜', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006017', '曲马多', '舍曲林', '曲马多&舍曲林', 1, '5-HT综合征与癫痫阈值降低风险',
        '避免联用，必要时减量并密切观察', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006018', '环孢素', '瑞舒伐他汀', '环孢素&瑞舒伐他汀', 1, '环孢素抑制他汀转运，肌病风险升高',
        '瑞舒伐他汀≤10mg/日，监测CK', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_interaction (id, component_a, component_b, pair_key, severity, interaction_desc, suggestion,
                                  status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2360000000000006019', '苯妥英钠', '华法林', '苯妥英钠&华法林', 2, '苯妥英置换蛋白结合并诱导代谢，效应不稳',
        '加密INR监测，稳定后固定剂量', 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
