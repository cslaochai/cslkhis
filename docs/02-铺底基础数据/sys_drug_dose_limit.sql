SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020001', '对乙酰氨基酚', 'g', '0.5000', '2.0000', '成人解热镇痛口径；肝功能不全及饮酒者减半', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020002', '布洛芬', 'g', '0.4000', '2.4000', '退热镇痛成人极量；儿童按体重另算，本表不承担儿科剂量',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020003', '阿司匹林', 'mg', '500.0000', '1500.0000',
        '按解热镇痛口径定；抗血小板维持量 100mg/日远低于本上限，不会误报', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020004', '双氯芬酸钠', 'mg', '75.0000', '150.0000', '口服制剂每日不超过 150mg', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020005', '二甲双胍', 'g', '1.0000', '2.5500', '普通片极量；加量须按耐受性，eGFR 低于 45 应减量', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020006', '格列美脲', 'mg', '4.0000', '6.0000',
        '磺脲类极量；老年与肾功能减退者起始 1mg，远低于本上限', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020007', '硝苯地平', 'mg', '30.0000', '60.0000', '按控释片整片口径（不可掰分）；普通片降压剂量另计',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020008', '氨氯地平', 'mg', '10.0000', '10.0000', '每日一次给药，单次即极量', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020009', '缬沙坦', 'mg', '320.0000', '320.0000', '每日一次给药极量', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020010', '厄贝沙坦', 'mg', '300.0000', '300.0000', '每日一次给药极量', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020011', '美托洛尔', 'mg', '190.0000', '200.0000',
        '缓释片整片口径（47.5mg×4）；普通片单次不超过 100mg，心率低于 55 次/分须减量而非按极量放行', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020012', '氢氯噻嗪', 'mg', '50.0000', '100.0000', '按说明书极量；降压常用不超过 25mg/日', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020013', '螺内酯', 'mg', '200.0000', '200.0000', '利尿极量；心衰治疗量（20mg/日）远低于本上限', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020014', '氯化钾', 'g', '1.5000', '6.0000',
        '口服单次不超过 1.5g；静滴另有浓度与速度限制（≤0.3%、禁静推），不在本表口径内', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020015', '地高辛', 'mg', '0.2500', '0.5000', '治疗窗窄；老年与肾功能减退者维持量常需低于本上限', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020016', '胺碘酮', 'g', '0.2000', '0.6000', '负荷期剂量接近本上限属方案需要，命中后须按方案复核', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020017', '辛伐他汀', 'mg', '40.0000', '40.0000', '80mg 因肌病风险已不再推荐，故按 40mg 收口', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020018', '阿托伐他汀', 'mg', '80.0000', '80.0000', '每日一次给药极量', 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020019', '瑞舒伐他汀', 'mg', '20.0000', '20.0000', '亚洲人群推荐起始 5~10mg，20mg 为极量', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020020', '氯雷他定', 'mg', '10.0000', '10.0000', '每日一次给药极量', 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020021', '西替利嗪', 'mg', '10.0000', '10.0000', '每日一次给药极量；肾功能不全须减量', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020022', '奥美拉唑', 'mg', '40.0000', '80.0000',
        '常规抑酸 20~40mg/日，80mg 为特殊方案（如卓-艾综合征）上限', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020023', '左氧氟沙星', 'g', '0.5000', '0.7500', '复杂感染 0.5~0.75g/日；老年须按肌酐清除率减量', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020024', '阿奇霉素', 'g', '0.5000', '0.5000', '首日 0.5g、后续 0.25g 的疗程口径，单次即极量', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020025', '奥司他韦', 'mg', '75.0000', '150.0000', '治疗口径 75mg 每日两次；预防 75mg 每日一次', 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020026', '卡马西平', 'g', '0.4000', '1.2000', '按说明书极量；须按血药浓度与耐受性滴定', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020027', '丙戊酸钠', 'g', '0.6000', '1.2000', '缓释片整片口径；按浓度与肝功能调整', 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020028', '地西泮', 'mg', '10.0000', '40.0000', '口服镇静口径；注射剂单次不超过 10mg 且需缓慢推注',
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020029', '秋水仙碱', 'mg', '1.0000', '2.0000',
        '急性痛风新口径（首剂 1mg，1 小时后 0.5mg）；不再沿用日极量 4~6mg 的老写法', 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020030', '别嘌醇', 'mg', '300.0000', '800.0000',
        '小剂量起始按血尿酸调整；肾功能不全者须按肌酐清除率下调本上限', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020031', '氨茶碱', 'mg', '200.0000', '500.0000',
        '茶碱类治疗窗窄，命中后须按血药浓度与吸烟/合并用药史复核', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020032', '维生素C', 'g', '0.5000', '1.0000',
        '大剂量长期服用可致腹泻与尿路结石；日常补充量远低于本上限', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020033', '呋塞米', 'mg', '200.0000', '600.0000',
        '按水肿状态的极量；利尿治疗常远低于本上限，命中须核对是否急性肺水肿方案', 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_drug_dose_limit (id, component, dose_unit, max_single_dose, max_daily_dose, note, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('894000000000020034', '吗替麦考酚酯', 'g', '1.5000', '3.0000', '口服极量（移植方案）；感染或骨髓抑制时须减量', 1,
        'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
