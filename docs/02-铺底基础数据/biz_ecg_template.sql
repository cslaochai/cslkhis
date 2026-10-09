SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_ecg_template (id, template_code, template_name, ecg_type, finding_tpl, conclusion_tpl, suggestion_tpl,
                              sort_order, status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('211000000000000101', 'ETPL-NORMAL', '正常心电图', NULL,
        '窦性心律，P波顺序出现，PR间期 0.12~0.20s，QRS波群时限正常，ST-T未见异常偏移。', '正常心电图。',
        '无需特殊处理，定期体检复查。', 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_ecg_template (id, template_code, template_name, ecg_type, finding_tpl, conclusion_tpl, suggestion_tpl,
                              sort_order, status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('211000000000000102', 'ETPL-TACHO', '窦性心动过速', 1,
        '窦性心律，心率 >100 次/分，P波形态正常，PR间期正常，QRS时限正常。', '窦性心动过速。',
        '结合临床查找原因（发热/贫血/甲亢/情绪等），对症处理后复查。', 2, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_ecg_template (id, template_code, template_name, ecg_type, finding_tpl, conclusion_tpl, suggestion_tpl,
                              sort_order, status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('211000000000000103', 'ETPL-BRADO', '窦性心动过缓', 1,
        '窦性心律，心率 <60 次/分，P波形态正常，未见传导阻滞及停搏。', '窦性心动过缓。',
        '如无症状可观察；伴头晕/黑矇者建议行动态心电图进一步评估。', 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_ecg_template (id, template_code, template_name, ecg_type, finding_tpl, conclusion_tpl, suggestion_tpl,
                              sort_order, status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('211000000000000104', 'ETPL-AF', '心房颤动', 1, 'P波消失，代之以f波，RR间期绝对不齐，QRS波群呈室上性。',
        '心房颤动。', '结合临床评估抗凝指征，控制心室率，建议查心脏超声及甲功。', 4, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_ecg_template (id, template_code, template_name, ecg_type, finding_tpl, conclusion_tpl, suggestion_tpl,
                              sort_order, status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('211000000000000105', 'ETPL-STT', 'ST-T改变', 1, '窦性心律，部分导联 ST 段压低，T 波低平/倒置。',
        'ST-T改变，请结合临床。', '建议结合病史与心肌酶/冠脉评估，必要时复查。', 5, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_ecg_template (id, template_code, template_name, ecg_type, finding_tpl, conclusion_tpl, suggestion_tpl,
                              sort_order, status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('211000000000000106', 'ETPL-HOLTER-NORMAL', 'Holter正常', 2,
        '窦性心律，平均心率 xx 次/分，最快 xx 次/分，最慢 xx 次/分；总心搏 xx 次。未见恶性心律失常及长间歇。',
        '动态心电图（24小时）未见明显异常。', '正常生活作息，不适随诊。', 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_ecg_template (id, template_code, template_name, ecg_type, finding_tpl, conclusion_tpl, suggestion_tpl,
                              sort_order, status, create_by, update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('211000000000000107', 'ETPL-HOLTER-AF', 'Holter房颤伴长间歇', 2,
        '基础心律为心房颤动，全天平均心室率 xx 次/分；共检出长间歇 xx 次，最长 xx ms（发生于 xx 时）。',
        '持续性心房颤动伴长间歇。', '建议心内科评估起搏指征及抗凝方案。', 7, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
