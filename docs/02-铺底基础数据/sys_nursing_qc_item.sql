SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000001', 'BN01', '入院护理评估及时完整', 1, 'BASIC_NURSING',
        '入院 8 小时内完成 Braden/Morse/NRS 三项评估且记录完整', '12.5', '95.00', 1, 1, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000002', 'BN02', '分级护理巡视到位', 1, 'BASIC_NURSING',
        '按护理级别巡视并在记录单留痕，巡视间隔不超限', '12.5', '95.00', 1, 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000003', 'BN03', '晨晚间护理落实', 1, 'BASIC_NURSING', '晨晚间护理项目齐全，床单位清洁平整', '12.5',
        '90.00', 0, 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000004', 'BN04', '口腔护理规范', 1, 'BASIC_NURSING', '禁食/昏迷/插管患者按医嘱执行口腔护理',
        '12.5', '90.00', 0, 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000005', 'BN05', '卧位与舒适管理', 1, 'BASIC_NURSING', '体位符合病情，疼痛按 NRS 追踪处理', '12.5',
        '90.00', 0, 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000006', 'BN06', '排泄与会阴护理', 1, 'BASIC_NURSING', '失禁患者皮肤清洁有防护措施，导尿管护理到位',
        '12.5', '90.00', 0, 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000007', 'BN07', '生活自理能力指导', 1, 'BASIC_NURSING',
        '按 Barthel 分级给予相应生活护理与活动指导', '12.5', '90.00', 0, 7, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000008', 'BN08', '营养与饮食护理', 1, 'BASIC_NURSING', '治疗饮食宣教到位，进食体位与呛咳防范落实',
        '12.5', '90.00', 0, 8, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000009', 'SC01', '管道护理（固定/标识/通畅）', 2, NULL, '各类导管固定妥贴、标识清楚、通畅无扭曲脱出',
        '20.0', '95.00', 1, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000010', 'SC02', '伤口造口护理', 2, NULL, '换药无菌操作规范，造口周围皮肤无失禁性皮炎', '20.0',
        '90.00', 0, 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000011', 'SC03', '给药正确（三查八对）', 2, NULL, '遵医嘱给药，三查八对执行有记录，高危药品双人核对',
        '20.0', '100.00', 1, 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000012', 'SC04', '静脉治疗规范（留置针/PICC）', 2, NULL,
        '穿刺点无红肿渗出，敷料在位且在更换周期内，冲封管规范', '20.0', '95.00', 1, 4, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000013', 'SC05', '术前术后护理', 2, NULL, '术前准备与术后交接、观察要点落实', '20.0', '90.00', 0, 5,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000014', 'SF01', '跌倒/坠床防范措施落实', 3, NULL,
        '高危患者有警示标识、床栏到位、呼叫器可及、防滑措施齐', '20.0', '95.00', 1, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000015', 'SF02', '压力性损伤防范措施落实', 3, NULL, 'Braden 高危有翻身卡与减压用具，交接班查看皮肤',
        '20.0', '95.00', 1, 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000016', 'SF03', '管路滑脱防范', 3, NULL, '高危管路有二次固定与固定交接，患者/家属知晓防脱宣教',
        '20.0', '95.00', 0, 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000017', 'SF04', '患者身份识别与查对', 3, NULL, '两种及以上方式识别身份，操作前查对腕带', '20.0',
        '100.00', 1, 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000018', 'SF05', '保护性约束规范', 3, NULL, '约束有医嘱与知情同意，定时松解与皮肤观察有记录',
        '20.0', '95.00', 0, 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000019', 'DC01', '护理记录书写规范', 4, 'NURSING_DOC',
        '客观及时准确完整，与医疗记录不矛盾，签名与时间齐全', '25.0', '95.00', 1, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000020', 'DC02', '三测单绘制准确', 4, 'NURSING_DOC',
        '体温/脉搏/呼吸按频次测量并绘制，跳过与复测有说明', '25.0', '95.00', 0, 2, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000021', 'DC03', '交接班记录完整', 4, 'NURSING_DOC',
        '危重/手术/特殊患者交接项目齐全，重点内容无遗漏', '25.0', '95.00', 0, 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000022', 'DC04', '健康教育与知情记录', 4, 'NURSING_DOC', '入院/用药/术前/出院宣教有记录且患者知晓',
        '25.0', '90.00', 0, 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000023', 'IP01', '手卫生依从', 5, NULL, '五个时刻执行手卫生，速干手消毒剂可及', '25.0', '95.00', 1,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000024', 'IP02', '医疗废物分类处置', 5, NULL, '感染性/损伤性废物分类投放，交接登记完整', '25.0',
        '95.00', 0, 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000025', 'IP03', '多重耐药菌隔离措施', 5, NULL, '接触隔离标识、专人护理、床旁消毒落实', '25.0',
        '95.00', 0, 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_nursing_qc_item (id, item_code, item_name, category, indicator_code, standard, full_score, target_rate,
                                 key_flag, sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                 update_by_id)
VALUES ('896801000000000026', 'IP04', '病室环境清洁消毒', 5, NULL, '物表清洁频次与紫外线/空气消毒登记符合规范', '25.0',
        '90.00', 0, 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
