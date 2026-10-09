SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO biz_radio_report_template (id, template_code, template_name, modality, item_code, item_name, body_part,
                                       exam_method, finding_tpl, impression_tpl, suggestion_tpl, is_public, doctor_id,
                                       sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                       update_by_id)
VALUES ('210500000000000201', 'RTPL-CT-CHEST', '胸部CT平扫', 1, NULL, NULL, '胸部',
        '胸部CT平扫，层厚5mm，层距5mm，范围自肺尖至肺底。',
        '胸廓对称，气管及支气管通畅，纵隔居中。双肺纹理清晰，未见实质性病变。心影大小形态正常。双侧胸腔未见积液。',
        '胸部CT平扫未见明显异常。', '必要时复查或结合临床进一步检查。', 1, NULL, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_radio_report_template (id, template_code, template_name, modality, item_code, item_name, body_part,
                                       exam_method, finding_tpl, impression_tpl, suggestion_tpl, is_public, doctor_id,
                                       sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                       update_by_id)
VALUES ('210500000000000202', 'RTPL-CT-HEAD', '头颅CT平扫', 1, NULL, NULL, '头颅',
        '头颅CT平扫，层厚5mm，层距5mm，范围自颅底至颅顶。',
        '脑实质密度均匀，灰白质分界清晰。脑室系统大小形态正常，中线结构居中。脑池及脑沟未见异常。颅骨未见骨折。',
        '头颅CT平扫未见明显异常。', '结合临床，必要时复查或行MRI检查。', 1, NULL, 2, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO biz_radio_report_template (id, template_code, template_name, modality, item_code, item_name, body_part,
                                       exam_method, finding_tpl, impression_tpl, suggestion_tpl, is_public, doctor_id,
                                       sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                       update_by_id)
VALUES ('210500000000000203', 'RTPL-MR-HEAD', '头颅MRI平扫', 2, NULL, NULL, '头颅',
        '头颅MRI平扫，行T1WI、T2WI、FLAIR及DWI序列扫描。',
        '脑实质内未见异常信号灶，灰白质分界清晰。脑室系统无扩张，中线结构居中。DWI未见明显弥散受限。',
        '头颅MRI平扫未见明显异常。', '结合临床随访。', 1, NULL, 3, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_radio_report_template (id, template_code, template_name, modality, item_code, item_name, body_part,
                                       exam_method, finding_tpl, impression_tpl, suggestion_tpl, is_public, doctor_id,
                                       sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                       update_by_id)
VALUES ('210500000000000204', 'RTPL-DR-CHEST', '胸部正位DR', 3, NULL, NULL, '胸部',
        '胸部后前位DR摄影，站立位，深吸气后屏气曝光。',
        '胸廓对称，气管居中。双肺纹理清晰，肺内未见实质性病变。心影大小形态正常，双膈面光滑，肋膈角锐利。',
        '胸部正位片未见明显异常。', '结合临床，必要时复查。', 1, NULL, 4, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_radio_report_template (id, template_code, template_name, modality, item_code, item_name, body_part,
                                       exam_method, finding_tpl, impression_tpl, suggestion_tpl, is_public, doctor_id,
                                       sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                       update_by_id)
VALUES ('210500000000000205', 'RTPL-CT-ABDOMEN', '腹部CT平扫', 1, 'CT003', '腹部CT平扫', '腹部',
        '腹部CT平扫，层厚5mm，范围自膈顶至耻骨联合水平。',
        '肝脏大小形态正常，实质密度均匀，未见异常密度灶。胆囊不大，壁不厚。脾脏不大。胰腺形态密度未见异常。双肾形态大小正常，未见积水及结石影。腹腔未见积液及肿大淋巴结。',
        '腹部CT平扫未见明显异常。', '结合临床及实验室检查，必要时增强扫描。', 1, NULL, 5, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO biz_radio_report_template (id, template_code, template_name, modality, item_code, item_name, body_part,
                                       exam_method, finding_tpl, impression_tpl, suggestion_tpl, is_public, doctor_id,
                                       sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                       update_by_id)
VALUES ('210500000000000206', 'RTPL-MR-LUMBAR', '腰椎MRI平扫', 2, 'MR003', '腰椎MRI平扫', '腰椎',
        '腰椎MRI平扫，行T1WI、T2WI矢状位及轴位扫描。',
        '腰椎生理曲度存在，椎体形态及信号未见异常。椎间盘未见明显膨出及突出。椎管未见狭窄，脊髓及圆锥信号未见异常。',
        '腰椎MRI平扫未见明显异常。', '结合临床，必要时复查。', 1, NULL, 6, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_radio_report_template (id, template_code, template_name, modality, item_code, item_name, body_part,
                                       exam_method, finding_tpl, impression_tpl, suggestion_tpl, is_public, doctor_id,
                                       sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                       update_by_id)
VALUES ('210500000000000207', 'RTPL-DR-LUMBAR', '腰椎正侧位DR', 3, 'XR003', '腰椎正侧位片', '腰椎',
        '腰椎正侧位DR摄影，站立位或卧位。',
        '腰椎序列生理曲度存在，椎体骨质结构完整，未见骨折及骨质破坏。椎间隙未见明显狭窄。椎旁软组织未见异常。',
        '腰椎正侧位未见明显异常。', '结合临床，必要时行MRI检查。', 1, NULL, 7, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO biz_radio_report_template (id, template_code, template_name, modality, item_code, item_name, body_part,
                                       exam_method, finding_tpl, impression_tpl, suggestion_tpl, is_public, doctor_id,
                                       sort_order, status, create_by, update_by, del_flag, remark, create_by_id,
                                       update_by_id)
VALUES ('210500000000000208', 'RTPL-MR-KNEE', '膝关节MRI平扫', 2, 'MR004', '膝关节MRI平扫', '膝关节',
        '膝关节MRI平扫，行矢状位、冠状位及轴位扫描。',
        '膝关节组成骨未见异常信号。半月板形态信号正常，未见撕裂征象。前后交叉韧带及侧副韧带连续完整。关节腔内未见明显积液。',
        '膝关节MRI平扫未见明显异常。', '结合临床及体格检查。', 1, NULL, 8, 1, 'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
