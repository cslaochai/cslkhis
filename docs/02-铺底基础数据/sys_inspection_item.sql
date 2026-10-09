SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('1', 'XR001', '胸部正位片', 1, '101', '胸部', '80.00', 15, '去除胸部金属饰品', '孕妇慎用',
        '胸部正位片未见明显异常', 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('2', 'XR002', '颈椎正侧位片', 1, '101', '颈椎', '120.00', 15, '去除颈部金属饰品', '无', '颈椎生理曲度变直', 0,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('3', 'XR003', '腰椎正侧位片', 1, '101', '腰椎', '120.00', 15, '去除腰部金属饰品', '无', '腰椎骨质增生', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('4', 'XR004', '腹部平片', 1, '101', '腹部', '90.00', 15, '排空肠道气体', '肠梗阻患者慎用', '未见明显肠管扩张',
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('5', 'XR005', '四肢关节正位片', 1, '101', '四肢', '100.00', 15, '去除关节处金属饰品', '无', '未见明显骨折征象',
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('6', 'CT001', '头颅CT平扫', 1, '102', '头颅', '280.00', 20, '去除头部金属饰品', '孕妇禁用',
        '未见明显颅内出血及占位', 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('7', 'CT002', '胸部CT平扫', 1, '102', '胸部', '320.00', 20, '去除胸部金属饰品', '孕妇慎用', '双肺纹理清晰', 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('8', 'CT003', '腹部CT平扫', 1, '102', '腹部', '350.00', 20, '检查前禁食4小时', '无', '肝胆胰脾未见明显异常', 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('9', 'CT004', '颈椎CT平扫', 1, '102', '颈椎', '300.00', 20, '去除颈部金属饰品', '无', '颈椎间盘未见明显突出', 0,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('10', 'CT005', '盆腔CT平扫', 1, '102', '盆腔', '350.00', 20, '憋尿使膀胱充盈', '无', '盆腔未见明显占位', 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('11', 'MR001', '头颅MRI平扫', 1, '103', '头颅', '650.00', 30, '去除所有金属物品', '体内有心脏起搏器者禁用',
        '脑实质未见明显异常信号', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('12', 'MR002', '颈椎MRI平扫', 1, '103', '颈椎', '650.00', 30, '去除颈部金属物品', '体内有金属植入物者禁用',
        '颈椎间盘信号正常', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('13', 'MR003', '腰椎MRI平扫', 1, '103', '腰椎', '650.00', 30, '去除腰部金属物品', '体内有金属植入物者禁用',
        '腰椎未见明显椎间盘突出', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('14', 'MR004', '膝关节MRI平扫', 1, '103', '膝关节', '650.00', 30, '去除膝部金属物品', '体内有金属植入物者禁用',
        '半月板及韧带未见明显损伤', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('15', 'MR005', '腹部MRI平扫', 1, '103', '腹部', '700.00', 30, '检查前禁食4小时', '幽闭恐惧症患者禁用',
        '肝胆胰脾未见明显异常', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('16', 'US001', '肝胆胰脾彩超', 2, '104', '腹部', '150.00', 20, '空腹8小时以上', '无', '肝胆胰脾未见明显异常', 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('17', 'US002', '泌尿系彩超', 2, '104', '泌尿系', '150.00', 20, '憋尿使膀胱充盈', '无',
        '双肾输尿管膀胱未见明显异常', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('18', 'US003', '子宫附件彩超', 2, '104', '盆腔', '150.00', 20, '经腹需憋尿，经阴道需排空膀胱', '无',
        '子宫附件未见明显异常', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('19', 'US004', '甲状腺彩超', 2, '104', '颈部', '120.00', 15, '无需特殊准备', '无', '甲状腺未见明显结节', 0, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('20', 'US005', '心脏彩超', 2, '104', '心脏', '200.00', 25, '无需特殊准备', '无', '心脏结构及功能未见明显异常',
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('21', 'ECG001', '常规心电图', 3, '105', '心脏', '30.00', 10, '平卧放松', '无', '窦性心律，正常心电图', 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('22', 'ECG002', '24小时动态心电图', 3, '105', '心脏', '250.00', 1440, '佩戴期间避免洗澡及剧烈运动', '无',
        '待回传数据后生成报告', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('23', 'ECG003', '运动平板心电图', 3, '105', '心脏', '350.00', 40, '穿宽松衣物及运动鞋', '严重心衰、急性心梗禁用',
        '运动耐量正常，未见明显ST-T改变', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('24', 'EN001', '胃镜', 4, '106', '上消化道', '300.00', 30, '禁食8小时，禁水4小时', '严重心肺功能不全者禁用',
        '食管胃十二指肠未见明显异常', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('25', 'EN002', '肠镜', 4, '106', '下消化道', '350.00', 40, '检查前一日低渣饮食，按医嘱服用泻药',
        '肠穿孔、急性腹膜炎禁用', '结肠直肠未见明显异常', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('26', 'EN003', '支气管镜', 4, '106', '呼吸道', '400.00', 45, '禁食4小时', '严重缺氧、大咯血者慎用',
        '气管支气管未见明显异常', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('27', 'XR006', '骨盆正位片', 1, '101', '骨盆', '100.00', 15, '去除腰部金属饰品', '孕妇慎用', '骨盆未见明显骨折',
        0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('28', 'XR007', '鼻窦柯氏位片', 1, '101', '鼻窦', '90.00', 15, '去除面部金属饰品', '无', '鼻窦未见明显炎症', 0,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('29', 'CT006', '胸部CT增强', 1, '102', '胸部', '600.00', 25, '需签署增强造影知情同意书', '碘造影剂过敏者禁用',
        '增强扫描未见明显异常强化灶', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('30', 'CT007', '腹部CT增强', 1, '102', '腹部', '650.00', 25, '需签署增强造影知情同意书', '碘造影剂过敏者禁用',
        '增强扫描未见明显异常强化灶', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('31', 'MR006', '头颅MRI增强', 1, '103', '头颅', '900.00', 35, '需签署增强造影知情同意书', '钆造影剂过敏者禁用',
        '增强扫描未见明显异常强化灶', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('32', 'US006', '颈动脉彩超', 2, '104', '颈部', '180.00', 20, '无需特殊准备', '无', '双侧颈动脉未见明显斑块', 0,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('33', 'US007', '乳腺彩超', 2, '104', '乳腺', '150.00', 20, '避开月经期', '无', '乳腺未见明显结节', 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('34', 'US008', '前列腺彩超', 2, '104', '盆腔', '150.00', 20, '经腹需憋尿', '无', '前列腺未见明显增生', 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('35', 'ECG004', '食管心电图', 3, '105', '心脏', '150.00', 20, '禁食4小时', '食管静脉曲张者禁用', '未见明显异常',
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('36', 'EN004', '喉镜', 4, '106', '咽喉', '150.00', 15, '禁食2小时', '急性会厌炎慎用', '声带未见明显息肉或小结',
        0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('37', 'EN005', '膀胱镜', 4, '106', '泌尿系', '450.00', 30, '排空膀胱', '尿道狭窄、急性感染期禁用',
        '膀胱黏膜未见明显异常', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('38', 'XR008', '乳腺钼靶', 1, '101', '乳腺', '250.00', 20, '避开月经期，勿涂抹爽身粉', '孕妇及哺乳期慎用',
        '乳腺未见明显钙化或肿块', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('39', 'CT008', '头颅CTA', 1, '102', '头颅', '800.00', 25, '需签署增强造影知情同意书', '碘造影剂过敏者禁用',
        '颅内血管未见明显狭窄或动脉瘤', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('40', 'MR007', '膝关节MRI增强', 1, '103', '膝关节', '900.00', 35, '需签署增强造影知情同意书',
        '钆造影剂过敏者禁用', '增强扫描未见明显异常强化灶', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('41', 'US009', '阴式彩超', 2, '104', '盆腔', '180.00', 20, '排空膀胱', '未婚女性及经期禁用',
        '子宫附件未见明显异常', 0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('42', 'ECG005', '胎儿心电图', 3, '105', '胎儿', '80.00', 15, '孕妇平卧', '无', '胎心监护正常', 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('43', 'EN006', '十二指肠镜(ERCP)', 4, '106', '胆胰管', '1200.00', 60, '禁食8小时', '严重心肺功能不全者禁用',
        '胆胰管未见明显结石或狭窄', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('44', 'XR009', '全脊柱正侧位片', 1, '101', '脊柱', '200.00', 20, '去除躯干金属饰品', '无', '脊柱序列正常', 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('45', 'CT009', '冠脉CTA', 1, '102', '心脏', '900.00', 30, '需签署增强造影知情同意书，控制心率',
        '碘造影剂过敏、严重心律失常者禁用', '冠状动脉未见明显狭窄', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('46', 'MR008', '前列腺MRI', 1, '103', '盆腔', '700.00', 30, '检查前需排空直肠', '体内有金属植入物者禁用',
        '前列腺未见明显异常信号', 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('47', 'US010', '下肢静脉彩超', 2, '104', '下肢', '180.00', 25, '无需特殊准备', '无',
        '双下肢深浅静脉未见明显血栓', 0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('48', 'ECG006', '脑电图', 3, '105', '大脑', '150.00', 30, '检查前洗头勿用护发素', '无', '未见明显异常脑电波', 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('49', 'EN007', '阴道镜', 4, '106', '宫颈', '120.00', 20, '避开月经期，检查前3天禁性生活', '急性阴道炎禁用',
        '宫颈未见明显病变', 0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('50', 'XR010', '膝关节正侧位片', 1, '101', '膝关节', '100.00', 15, '去除膝部金属饰品', '无', '膝关节间隙正常',
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100009', 'CT010', '胸椎CT', 1, '101', '胸椎', '350.00', 15, '去除金属饰品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100010', 'CT011', '四肢关节CT', 1, '101', '四肢', '300.00', 15, '去除金属饰品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100011', 'CT012', '颌面部CT', 1, '101', '颌面部', '350.00', 15, '去除金属饰品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100012', 'CT013', '鼻窦CT', 1, '101', '鼻窦', '320.00', 15, '去除金属饰品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100013', 'CT014', '喉部CT', 1, '101', '喉部', '350.00', 15, '去除金属饰品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100014', 'CT015', '肺动脉CTA', 1, '101', '肺动脉', '900.00', 15, '碘过敏史筛查,签署知情同意书', NULL, NULL, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100015', 'CT016', '头颈部CTA', 1, '101', '头颈部', '900.00', 15, '碘过敏史筛查,签署知情同意书', NULL, NULL, 0,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100016', 'CT017', '冠脉CTA', 1, '101', '心脏', '1100.00', 15, '心率控制,碘过敏史筛查,签署知情同意书', NULL,
        NULL, 0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100017', 'CT018', '腹部CTA', 1, '101', '腹部', '950.00', 15, '禁食4小时,碘过敏史筛查', NULL, NULL, 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100018', 'CT019', '全身骨扫描前定位CT', 1, '101', '全身', '500.00', 15, '去除金属饰品', NULL, NULL, 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100019', 'CT020', '颅脑CT灌注成像', 1, '101', '头部', '1100.00', 15, '碘过敏史筛查', NULL, NULL, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100028', 'MR009', '腹部MRI平扫', 1, '101', '腹部', '950.00', 15, '禁食4小时', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100029', 'MR010', '腹部MRI增强', 1, '101', '腹部', '1400.00', 15, '禁食4小时,造影剂过敏史筛查', NULL, NULL, 0,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100030', 'MR011', '盆腔MRI', 1, '101', '盆腔', '950.00', 15, '憋尿充盈膀胱', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100031', 'MR012', '乳腺MRI', 1, '101', '乳腺', '1200.00', 15, '月经周期后1-2周检查为宜', NULL, NULL, 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100032', 'MR013', '心脏MRI', 1, '101', '心脏', '1500.00', 15, '去除金属物品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100033', 'MR014', 'MRCP(磁共振胰胆管成像)', 1, '101', '胆道', '900.00', 15, '禁食4小时', NULL, NULL, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100034', 'MR015', 'MRU(磁共振尿路成像)', 1, '101', '尿路', '900.00', 15, '禁食4小时,憋尿', NULL, NULL, 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100035', 'MR016', '头颅MRA', 1, '101', '脑血管', '900.00', 15, '去除金属物品', NULL, NULL, 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100036', 'MR017', '颈部MRA', 1, '101', '颈部血管', '900.00', 15, '去除金属物品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100037', 'MR018', '脊柱全段MRI', 1, '101', '脊柱', '1600.00', 15, '去除金属物品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100038', 'MR019', '鼻咽部MRI', 1, '101', '鼻咽', '850.00', 15, '去除金属物品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100039', 'MR020', '骶髂关节MRI', 1, '101', '骶髂关节', '850.00', 15, '去除金属物品', NULL, NULL, 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100040', 'DR001', '胸部DR正侧位', 1, '101', '胸部', '140.00', 15, '去除胸部金属饰品,深吸气屏气', NULL, NULL, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100041', 'DR002', '腹部立位DR', 1, '101', '腹部', '120.00', 15, '空腹,排除肠梗阻', NULL, NULL, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100042', 'DR003', '骨盆DR', 1, '101', '骨盆', '130.00', 15, '去除金属饰品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100043', 'DR004', '四肢DR(每部位)', 1, '101', '四肢', '100.00', 15, '去除金属饰品', NULL, NULL, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100044', 'DR005', '脊柱全长DR', 1, '101', '脊柱', '220.00', 15, '站立位,去除金属饰品', NULL, NULL, 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100045', 'DR006', '鼻骨DR', 1, '101', '鼻骨', '90.00', 15, '去除面部饰品', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100056', 'US011', '四肢血管彩超(单肢)', 2, '102', '四肢血管', '200.00', 15, '无特殊准备', NULL, NULL, 1, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100057', 'US012', '腹腔大血管彩超', 2, '102', '腹部血管', '260.00', 15, '空腹8小时', NULL, NULL, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100058', 'US013', '浅表包块彩超', 2, '102', '浅表', '130.00', 15, '无特殊准备', NULL, NULL, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100059', 'US014', '睾丸附睾彩超', 2, '102', '阴囊', '140.00', 15, '无特殊准备', NULL, NULL, 1, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100060', 'US015', '胸腔积液定位', 2, '102', '胸腔', '160.00', 15, '无特殊准备', NULL, NULL, 1, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100061', 'US016', '肝脓肿/囊肿介入定位', 2, '102', '腹部', '200.00', 15, '空腹8小时', NULL, NULL, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100067', 'END001', '胃镜检查', 4, '104', '上消化道', '380.00', 15, '空腹8小时,签署知情同意书', NULL, NULL, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100068', 'END002', '无痛胃镜检查', 4, '104', '上消化道', '880.00', 15, '空腹8小时,麻醉评估,需家属陪同', NULL,
        NULL, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100069', 'END003', '肠镜检查', 4, '104', '结肠', '450.00', 15, '肠道准备(服用泻药),签署知情同意书', NULL, NULL,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100070', 'END004', '无痛肠镜检查', 4, '104', '结肠', '950.00', 15, '肠道准备,麻醉评估,需家属陪同', NULL, NULL,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100071', 'END005', '胃镜下息肉切除术', 4, '104', '上消化道', '1500.00', 15,
        '空腹8小时,凝血功能检查,签署知情同意书', NULL, NULL, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100072', 'END006', '肠镜下息肉切除术', 4, '104', '结肠', '1800.00', 15, '肠道准备,凝血功能检查,签署知情同意书',
        NULL, NULL, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100073', 'END007', '支气管镜检查', 4, '104', '气道', '600.00', 15, '术前禁食4小时,签署知情同意书', NULL, NULL,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100074', 'END008', '喉镜检查', 4, '104', '咽喉', '200.00', 15, '检查前2小时禁食', NULL, NULL, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100075', 'END009', '膀胱镜检查', 4, '104', '膀胱', '400.00', 15, '排空膀胱后检查', NULL, NULL, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100076', 'END010', '胶囊内镜', 4, '104', '小肠', '3800.00', 15, '肠道准备,签署知情同意书', NULL, NULL, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100077', 'OT001', '钼靶乳腺X线摄影', 1, '101', '乳腺', '280.00', 15, '月经结束后1周检查为宜', NULL, NULL, 0, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100078', 'OT002', '消化道钡餐造影', 1, '101', '上消化道', '350.00', 15, '空腹8小时', NULL, NULL, 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100079', 'OT003', '钡灌肠造影', 1, '101', '结肠', '380.00', 15, '肠道准备', NULL, NULL, 0, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100080', 'OT004', '静脉肾盂造影', 1, '101', '尿路', '420.00', 15, '碘过敏史筛查,肠道准备', NULL, NULL, 0, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100081', 'OT005', '子宫输卵管造影', 1, '101', '子宫输卵管', '520.00', 15, '月经干净后3-7天,碘过敏史筛查', NULL,
        NULL, 0, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100082', 'OT006', 'TCD经颅多普勒', 5, '102', '脑血管', '180.00', 15, '无特殊准备', NULL, NULL, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100083', 'OT007', '骨密度检测', 5, '102', '骨骼', '120.00', 15, '无特殊准备', NULL, NULL, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100084', 'OT008', '肺功能检查', 5, '105', '肺', '150.00', 15, '检查前避免剧烈运动', NULL, NULL, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100085', 'OT009', '脑电图', 5, '105', '脑', '180.00', 15, '检查前洗头,勿用发油', NULL, NULL, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_inspection_item (id, item_code, item_name, item_type, dept_id, body_part, price, duration, preparation,
                                 contraindication, report_template, is_emergency, is_appointment, status, create_by,
                                 update_by, del_flag, remark, create_by_id, update_by_id)
VALUES ('100086', 'OT010', '经颅磁刺激治疗', 5, '105', '脑', '260.00', 15, '无特殊准备', NULL, NULL, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
