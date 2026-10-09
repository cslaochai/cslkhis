SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('4', 'his_id_card_type', '居民身份证', '01', 1, NULL, 'primary', 1, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('5', 'his_id_card_type', '军官证', '02', 2, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('6', 'his_id_card_type', '护照', '03', 3, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('7', 'his_id_card_type', '户口簿', '04', 4, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8', 'his_id_card_type', '出生医学证明', '05', 5, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('9', 'his_id_card_type', '港澳居民来往内地通行证', '06', 6, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10', 'his_id_card_type', '台湾居民来往大陆通行证', '07', 7, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('11', 'his_id_card_type', '外国人永久居留身份证', '08', 8, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('12', 'his_admission_status', '待入院', '0', 1, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('13', 'his_admission_status', '已入院', '1', 2, NULL, 'success', 1, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('14', 'his_admission_status', '出院结算中', '2', 3, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('15', 'his_admission_status', '已出院', '3', 4, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('16', 'his_admission_status', '转科中', '4', 5, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('17', 'his_admission_status', '死亡', '5', 6, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('18', 'his_admission_status', '自动出院', '6', 7, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('19', 'his_admission_status', '医嘱取消入院', '7', 8, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('20', 'his_admission_status', '请假', '8', 9, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('21', 'his_admission_status', '欠费停药', '9', 10, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('22', 'his_pay_type', '现金', '1', 1, NULL, 'success', 1, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('23', 'his_pay_type', '微信支付', '2', 2, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('24', 'his_pay_type', '支付宝', '3', 3, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('25', 'his_pay_type', '银联卡', '4', 4, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('26', 'his_pay_type', '城镇职工医保', '10', 5, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('27', 'his_pay_type', '城乡居民医保', '11', 6, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('28', 'his_pay_type', '公费医疗', '12', 7, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('29', 'his_pay_type', '工伤保险', '13', 8, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('30', 'his_pay_type', '生育保险', '14', 9, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('31', 'his_pay_type', '商业保险', '20', 10, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('32', 'his_pay_type', '自费', '99', 11, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('33', 'his_pay_type', '挂账', '98', 12, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('34', 'his_pay_type', '混合支付', '97', 13, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('35', 'his_pay_type', '其他', '00', 14, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('36', 'his_order_status', '开立', '1', 1, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('37', 'his_order_status', '已提交', '2', 2, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('38', 'his_order_status', '医生已审核', '3', 3, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('39', 'his_order_status', '护士已核对', '4', 4, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('40', 'his_order_status', '药房已接收', '5', 5, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('41', 'his_order_status', '药房已发药', '6', 6, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('42', 'his_order_status', '检验已接收', '7', 7, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('43', 'his_order_status', '检验已出报告', '8', 8, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('44', 'his_order_status', '执行中', '9', 9, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('45', 'his_order_status', '已完成', '10', 10, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('47', 'his_order_status', '已作废', '12', 12, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('48', 'his_order_status', '退费中', '13', 13, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('49', 'his_order_status', '已退费', '14', 14, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('50', 'his_order_status', '驳回', '15', 15, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('100', 'hospital_position', '临床科室主任', '1', 10, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('101', 'hospital_position', '临床科室副主任', '2', 11, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('102', 'hospital_position', '主任医师', '3', 12, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('103', 'hospital_position', '副主任医师', '4', 13, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('104', 'hospital_position', '主治医师', '5', 14, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('105', 'hospital_position', '住院医师', '6', 15, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('106', 'hospital_position', '护理部主任', '7', 20, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('107', 'hospital_position', '科护士长', '8', 21, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('108', 'hospital_position', '护士长', '9', 22, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('109', 'hospital_position', '主任护师', '10', 23, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('110', 'hospital_position', '副主任护师', '11', 24, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('111', 'hospital_position', '主管护师', '12', 25, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('112', 'hospital_position', '专科护士', '13', 26, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('113', 'hospital_position', '护师', '14', 27, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('114', 'hospital_position', '护士', '15', 28, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('115', 'hospital_position', '医技/药剂科室主任', '16', 30, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('116', 'hospital_position', '主任技师/药师', '17', 31, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('117', 'hospital_position', '副主任技师/药师', '18', 32, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('118', 'hospital_position', '主管技师/药师', '19', 33, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('119', 'hospital_position', '技师/药师', '20', 34, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('120', 'hospital_position', '院级领导', '21', 40, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('121', 'hospital_position', '职能部门主任', '22', 41, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('122', 'hospital_position', '职能部门副主任', '23', 42, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('123', 'hospital_position', '行政干事', '24', 43, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('124', 'hospital_position', '研究员', '25', 50, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('125', 'hospital_position', '教授/副教授', '26', 51, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('126', 'hospital_position', '讲师/助教', '27', 52, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('127', 'hospital_position', '高级技师/技师', '28', 60, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('128', 'hospital_position', '高级工/中级工', '29', 61, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('129', 'hospital_position', '普通工', '30', 62, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('130', 'hospital_position', '临床医师', '31', 70, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('131', 'hospital_position', '导诊与辅助服务岗', '32', 71, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('132', 'hospital_position', '健康管理师', '33', 72, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('133', 'hospital_position', '营养师', '34', 73, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('134', 'hospital_position', '公卫与预防保健岗', '35', 74, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('135', 'hospital_position', '专业组组长', '36', 75, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('136', 'hospital_position', '康复治疗师', '37', 76, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('137', 'hospital_position', '消毒供应岗', '38', 77, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('138', 'hospital_position', '院感管理岗', '39', 78, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('139', 'hospital_position', '收费与结算岗', '40', 79, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('140', 'hospital_position', '信息技术岗', '41', 80, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('141', 'hospital_position', '设备与医学工程岗', '42', 81, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('142', 'hospital_position', '安保与消防岗', '43', 82, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('143', 'hospital_position', '后勤维修岗', '44', 83, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('144', 'hospital_position', '病案与编码岗', '45', 84, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1001', 'medical_insurance_type', '城镇职工基本医疗保险', '城镇职工基本医疗保险', 1, 'primary', 'primary', 0, 1,
        'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1002', 'medical_insurance_type', '城镇居民基本医疗保险', '城镇居民基本医疗保险', 2, 'success', 'success', 0, 1,
        'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1003', 'medical_insurance_type', '新型农村合作医疗', '新型农村合作医疗', 3, 'warning', 'warning', 0, 1,
        'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1004', 'medical_insurance_type', '公费医疗', '公费医疗', 4, 'info', 'info', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1005', 'medical_insurance_type', '商业医疗保险', '商业医疗保险', 5, 'danger', 'danger', 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1006', 'medical_insurance_type', '离休人员医疗', '离休人员医疗', 6, 'primary', 'primary', 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1007', 'medical_insurance_type', '建国前老工人医疗', '建国前老工人医疗', 7, 'primary', 'primary', 0, 1,
        'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1008', 'medical_insurance_type', '一至六级伤残军人医疗', '一至六级伤残军人医疗', 8, 'warning', 'warning', 0, 1,
        'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1009', 'medical_insurance_type', '生育保险', '生育保险', 9, 'success', 'success', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1010', 'medical_insurance_type', '工伤保险', '工伤保险', 10, 'danger', 'danger', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10101', 'sys_hospital_title', '医士', '101', 1, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10102', 'sys_hospital_title', '医师', '102', 2, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10103', 'sys_hospital_title', '药士', '103', 3, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10104', 'sys_hospital_title', '药师', '104', 4, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10105', 'sys_hospital_title', '护士', '105', 5, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10106', 'sys_hospital_title', '护师', '106', 6, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10107', 'sys_hospital_title', '技士', '107', 7, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10108', 'sys_hospital_title', '技师', '108', 8, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10109', 'sys_hospital_title', '主治（主管）医师', '201', 9, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10110', 'sys_hospital_title', '主管药师', '202', 10, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10111', 'sys_hospital_title', '主管护师', '203', 11, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10112', 'sys_hospital_title', '主管技师', '204', 12, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10113', 'sys_hospital_title', '副主任医师', '301', 13, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10114', 'sys_hospital_title', '副主任药师', '302', 14, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10115', 'sys_hospital_title', '副主任护师', '303', 15, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10116', 'sys_hospital_title', '副主任技师', '304', 16, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10117', 'sys_hospital_title', '主任医师', '401', 17, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10118', 'sys_hospital_title', '主任药师', '402', 18, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10119', 'sys_hospital_title', '主任护师', '403', 19, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10120', 'sys_hospital_title', '主任技师', '404', 20, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10121', 'sys_hospital_title', '科员', '501', 30, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10122', 'sys_hospital_title', '工程师', '502', 31, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10123', 'sys_hospital_title', '高级工程师', '503', 32, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10124', 'sys_hospital_title', '会计师', '504', 33, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10125', 'sys_hospital_title', '高级会计师', '505', 34, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10126', 'sys_hospital_title', '经济师', '506', 35, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10127', 'sys_hospital_title', '技术员', '507', 36, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10128', 'sys_hospital_title', '营养师', '508', 37, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10129', 'sys_hospital_title', '主管营养师', '509', 38, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10130', 'sys_hospital_title', '主任营养师', '510', 39, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10131', 'sys_hospital_title', '编码员', '511', 40, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10132', 'sys_hospital_title', '质控员', '512', 41, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10133', 'sys_hospital_title', '服务员', '513', 42, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10134', 'sys_hospital_title', '专科护士', '514', 43, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10135', 'sys_hospital_title', '护士长', '515', 44, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('10136', 'sys_hospital_title', '安全员', '516', 45, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('20101', 'emp_type', '医士', '1', 1, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('20102', 'emp_type', '医师', '2', 2, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('20103', 'emp_type', '药士', '3', 3, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('20104', 'emp_type', '药师', '4', 4, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('20105', 'emp_type', '护士', '5', 5, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('20106', 'emp_type', '护师', '6', 6, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000101', 'sys_nationality', '汉族', '1', 1, NULL, 'primary', 1, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000102', 'sys_nationality', '蒙古族', '2', 2, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000103', 'sys_nationality', '回族', '3', 3, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000104', 'sys_nationality', '藏族', '4', 4, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000105', 'sys_nationality', '维吾尔族', '5', 5, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000106', 'sys_nationality', '苗族', '6', 6, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000107', 'sys_nationality', '彝族', '7', 7, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000108', 'sys_nationality', '壮族', '8', 8, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000109', 'sys_nationality', '布依族', '9', 9, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000110', 'sys_nationality', '朝鲜族', '10', 10, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000111', 'sys_nationality', '满族', '11', 11, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000112', 'sys_nationality', '侗族', '12', 12, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000113', 'sys_nationality', '瑶族', '13', 13, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000114', 'sys_nationality', '白族', '14', 14, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000115', 'sys_nationality', '土家族', '15', 15, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000116', 'sys_nationality', '哈尼族', '16', 16, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000117', 'sys_nationality', '哈萨克族', '17', 17, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000118', 'sys_nationality', '傣族', '18', 18, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000119', 'sys_nationality', '黎族', '19', 19, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000120', 'sys_nationality', '傈僳族', '20', 20, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000121', 'sys_nationality', '佤族', '21', 21, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000122', 'sys_nationality', '畲族', '22', 22, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000123', 'sys_nationality', '高山族', '23', 23, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000124', 'sys_nationality', '拉祜族', '24', 24, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000125', 'sys_nationality', '水族', '25', 25, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000126', 'sys_nationality', '东乡族', '26', 26, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000127', 'sys_nationality', '纳西族', '27', 27, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000128', 'sys_nationality', '景颇族', '28', 28, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000129', 'sys_nationality', '柯尔克孜族', '29', 29, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000130', 'sys_nationality', '土族', '30', 30, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000131', 'sys_nationality', '达斡尔族', '31', 31, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000132', 'sys_nationality', '仫佬族', '32', 32, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000133', 'sys_nationality', '羌族', '33', 33, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000134', 'sys_nationality', '布朗族', '34', 34, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000135', 'sys_nationality', '撒拉族', '35', 35, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000136', 'sys_nationality', '毛南族', '36', 36, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000137', 'sys_nationality', '仡佬族', '37', 37, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000138', 'sys_nationality', '锡伯族', '38', 38, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000139', 'sys_nationality', '阿昌族', '39', 39, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000140', 'sys_nationality', '普米族', '40', 40, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000141', 'sys_nationality', '塔吉克族', '41', 41, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000142', 'sys_nationality', '怒族', '42', 42, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000143', 'sys_nationality', '乌孜别克族', '43', 43, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000144', 'sys_nationality', '俄罗斯族', '44', 44, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000145', 'sys_nationality', '鄂温克族', '45', 45, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000146', 'sys_nationality', '德昂族', '46', 46, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000147', 'sys_nationality', '保安族', '47', 47, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000148', 'sys_nationality', '裕固族', '48', 48, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000149', 'sys_nationality', '京族', '49', 49, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000150', 'sys_nationality', '塔塔尔族', '50', 50, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000151', 'sys_nationality', '独龙族', '51', 51, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000152', 'sys_nationality', '鄂伦春族', '52', 52, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000153', 'sys_nationality', '赫哲族', '53', 53, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000154', 'sys_nationality', '门巴族', '54', 54, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000155', 'sys_nationality', '珞巴族', '55', 55, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000156', 'sys_nationality', '基诺族', '56', 56, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000201', 'sys_patient_relation', '本人', '1', 1, NULL, 'primary', 1, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000202', 'sys_patient_relation', '配偶', '2', 2, NULL, 'success', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000203', 'sys_patient_relation', '父亲', '3', 3, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000204', 'sys_patient_relation', '母亲', '4', 4, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000205', 'sys_patient_relation', '儿子', '5', 5, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000206', 'sys_patient_relation', '女儿', '6', 6, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000207', 'sys_patient_relation', '兄弟', '7', 7, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000208', 'sys_patient_relation', '姐妹', '8', 8, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000209', 'sys_patient_relation', '祖父', '9', 9, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000210', 'sys_patient_relation', '祖母', '10', 10, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000211', 'sys_patient_relation', '外祖父', '11', 11, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000212', 'sys_patient_relation', '外祖母', '12', 12, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000213', 'sys_patient_relation', '其他亲属', '13', 13, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000214', 'sys_patient_relation', '朋友', '14', 14, NULL, 'warning', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000215', 'sys_patient_relation', '同事', '15', 15, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000216', 'sys_patient_relation', '单位', '16', 16, NULL, 'default', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('1000000217', 'sys_patient_relation', '其他', '99', 99, NULL, 'danger', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('890000000000000941', 'his_ward_dispense_status', '待配药', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('890000000000000942', 'his_ward_dispense_status', '配药中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('890000000000000943', 'his_ward_dispense_status', '已配药', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('890000000000000944', 'his_ward_dispense_status', '已核对', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('890000000000000945', 'his_ward_dispense_status', '已退药', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('890000000000000951', 'his_ward_dispense_item_status', '待配药', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('890000000000000952', 'his_ward_dispense_item_status', '已配药', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('890000000000000953', 'his_ward_dispense_item_status', '已核对', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('890000000000000954', 'his_ward_dispense_item_status', '已退药', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000001', 'his_common_audit_status', '待提交', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000002', 'his_common_audit_status', '待审核', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000003', 'his_common_audit_status', '审核通过', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000004', 'his_common_audit_status', '审核驳回', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000005', 'his_enable_status', '禁用', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000006', 'his_enable_status', '启用', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000007', 'his_del_flag', '正常', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000008', 'his_del_flag', '已删除', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000009', 'his_yes_no', '否', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000010', 'his_yes_no', '是', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000013', 'his_user_type', '院内用户', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000014', 'his_user_type', '院外用户', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000015', 'his_user_type', '患者', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000016', 'his_user_type', '其他', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000020', 'his_stock_status', '正常', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000021', 'his_stock_status', '预警', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000022', 'his_stock_status', '缺货', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000023', 'his_stock_status', '过期', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000024', 'his_record_status', '草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000025', 'his_record_status', '已提交', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000026', 'his_record_status', '已归档', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000027', 'his_record_status', '已作废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000028', 'his_review_status', '待提交', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000029', 'his_review_status', '待审核', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000030', 'his_review_status', '审核通过', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000031', 'his_review_status', '审核驳回', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000032', 'his_settlement_mode', '自费', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000033', 'his_settlement_mode', '医保', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000034', 'his_settlement_type_regist', '自费', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000035', 'his_settlement_type_regist', '城镇职工医保', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000036', 'his_settlement_type_regist', '城乡居民医保', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000037', 'his_settlement_type_regist', '公费', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000038', 'his_settlement_type_regist', '商业保险', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000039', 'his_medical_insurance_type_code', '商业医疗保险', '0', 1, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000040', 'his_medical_insurance_type_code', '城镇职工医保', '2', 2, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000041', 'his_medical_insurance_type_code', '城乡居民医保', '3', 3, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000042', 'his_medical_insurance_type_code', '公费医疗', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000043', 'his_patient_type', '自费', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000044', 'his_patient_type', '城镇职工医保', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000045', 'his_patient_type', '城乡居民医保', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000046', 'his_patient_type', '公费', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000047', 'his_patient_type', '其他', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000048', 'his_card_type', '就诊卡', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000049', 'his_card_type', '身份证', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000050', 'his_card_type', '医保卡', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000051', 'his_marital_status', '未婚', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000052', 'his_marital_status', '已婚', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000053', 'his_marital_status', '离异', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000054', 'his_marital_status', '丧偶', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000055', 'his_tag_source', '手动打标', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000056', 'his_tag_source', '系统自动打标', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000057', 'his_week_day_getday', '周日', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000058', 'his_week_day_getday', '周一', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000059', 'his_week_day_getday', '周二', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000060', 'his_week_day_getday', '周三', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000061', 'his_week_day_getday', '周四', '4', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000062', 'his_week_day_getday', '周五', '5', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000063', 'his_week_day_getday', '周六', '6', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000064', 'his_week_day_iso', '周一', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000065', 'his_week_day_iso', '周二', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000066', 'his_week_day_iso', '周三', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000067', 'his_week_day_iso', '周四', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000068', 'his_week_day_iso', '周五', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000069', 'his_week_day_iso', '周六', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000070', 'his_week_day_iso', '周日', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000082', 'his_regist_status', '已挂号', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000083', 'his_regist_status', '已签到', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000084', 'his_regist_status', '已接诊', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000085', 'his_regist_status', '已就诊', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000086', 'his_regist_status', '已退号', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000087', 'his_regist_status', '已过号', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000088', 'his_regist_type', '普通号', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000089', 'his_regist_type', '专家号', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000090', 'his_regist_type', '急诊号', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000091', 'his_regist_type', '免费号', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000092', 'his_regist_source', '窗口挂号', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000093', 'his_regist_source', '自助机挂号', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000094', 'his_regist_source', '网上挂号', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000095', 'his_regist_source', '预约挂号', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000096', 'his_is_revisit', '初诊', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000097', 'his_is_revisit', '复诊', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000098', 'his_visit_type_enum', '未知', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000099', 'his_visit_type_enum', '初诊', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000100', 'his_visit_type_enum', '复诊', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000101', 'his_visit_status', '已取消', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000102', 'his_visit_status', '进行中', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000103', 'his_visit_status', '已完成', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000104', 'his_queue_status', '候诊中', '2', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000105', 'his_queue_status', '就诊中', '3', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000106', 'his_queue_status', '已就诊', '4', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000107', 'his_queue_status', '已退号', '5', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000108', 'his_queue_status', '已过号', '6', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000109', 'his_queue_status', '已失效', '7', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000110', 'his_queue_type', '普通队列', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000111', 'his_queue_type', '优先队列', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000112', 'his_queue_type', '过号队列', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000113', 'his_queue_regist_type', '普通挂号', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000114', 'his_queue_regist_type', '预约挂号', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000115', 'his_queue_regist_type', '急诊挂号', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000116', 'his_schedule_type', '上午', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000117', 'his_schedule_type', '下午', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000118', 'his_schedule_type', '全天', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000119', 'his_schedule_status', '停诊', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000120', 'his_schedule_status', '正常', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000121', 'his_schedule_status', '已满', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000122', 'his_schedule_status', '已过期', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000123', 'his_schedule_consult_status', '待开始', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000124', 'his_schedule_consult_status', '接诊中', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000125', 'his_schedule_consult_status', '暂停', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000126', 'his_appointment_source', '未划池全部现场可挂', '0', 1, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000127', 'his_appointment_source', '部分划池', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000128', 'his_appointment_source', '全部划池', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000129', 'his_clinic_room_status', '停用', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000130', 'his_clinic_room_status', '启用', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000131', 'his_prescription_type', '西药处方', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000132', 'his_prescription_type', '中成药处方', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000133', 'his_prescription_type', '中药饮片处方', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000134', 'his_prescription_source', '门诊处方', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000135', 'his_prescription_source', '急诊处方', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000136', 'his_prescription_source', '住院处方', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000137', 'his_prescription_status', '草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000138', 'his_prescription_status', '已提交', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000139', 'his_prescription_status', '已审核', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000140', 'his_prescription_status', '已发药', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000141', 'his_prescription_status', '已取消', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000142', 'his_prescription_status', '已退药', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000143', 'his_payment_status', '未缴费', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000144', 'his_payment_status', '已缴费', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000145', 'his_payment_status', '已退费', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000146', 'his_pay_method', '现金', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000147', 'his_pay_method', '微信', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000148', 'his_pay_method', '支付宝', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000149', 'his_pay_method', '医保个账', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000150', 'his_pay_method', '余额', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000151', 'his_charge_type', '挂号费', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000152', 'his_charge_type', '药品费', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000153', 'his_charge_type', '检查费', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000154', 'his_charge_type', '检验费', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000155', 'his_charge_type', '治疗费', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000156', 'his_charge_type', '综合收费', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000157', 'his_charge_status', '待收费', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000158', 'his_charge_status', '已收费', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000159', 'his_charge_status', '已退费', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000160', 'his_charge_status', '部分退费', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000161', 'his_charge_status', '已取消', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000162', 'his_charge_item_type', '挂号费', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000163', 'his_charge_item_type', '西药', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000164', 'his_charge_item_type', '中成药', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000165', 'his_charge_item_type', '中药饮片', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000166', 'his_charge_item_type', '检查', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000167', 'his_charge_item_type', '检验', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000168', 'his_charge_item_type', '治疗', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000169', 'his_skin_test_result', '阴性', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000170', 'his_skin_test_result', '阳性', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000171', 'his_refund_type', '退药', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000172', 'his_refund_type', '退检查', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000173', 'his_refund_type', '退检验', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000174', 'his_refund_type', '退治疗', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000175', 'his_refund_type', '退挂号', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000176', 'his_refund_type', '其他', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000177', 'his_refund_apply_type', '退药', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000178', 'his_refund_apply_type', '退检查', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000179', 'his_refund_apply_type', '退检验', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000180', 'his_refund_apply_type', '退治疗', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000181', 'his_refund_apply_type', '全部退费', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000182', 'his_refund_status', '待审核', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000183', 'his_refund_status', '已审核', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000184', 'his_refund_status', '已退费', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000185', 'his_refund_status', '已取消', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000186', 'his_refund_apply_status', '待审核', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000187', 'his_refund_apply_status', '审核通过', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000188', 'his_refund_apply_status', '审核驳回', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000189', 'his_refund_apply_status', '已退费', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000190', 'his_refund_method', '原路退回', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000191', 'his_refund_method', '现金退回', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000192', 'his_refund_method', '余额退回', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000193', 'his_invoice_type', '普通发票', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000194', 'his_invoice_type', '电子发票', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000195', 'his_invoice_type', '数电发票', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000196', 'his_invoice_status', '已开具', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000197', 'his_invoice_status', '已打印', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000198', 'his_invoice_status', '已作废', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000199', 'his_emergency_status', '候诊', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000200', 'his_emergency_status', '诊治中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000201', 'his_emergency_status', '留观', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000202', 'his_emergency_status', '转住院', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000203', 'his_emergency_status', '离院', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000204', 'his_emergency_status', '死亡', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000205', 'his_triage_level', 'I级(濒危)', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000206', 'his_triage_level', 'II级(危重)', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000207', 'his_triage_level', 'III级(急症)', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000208', 'his_triage_level', 'IV级(非急症)', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000209', 'his_inspection_apply_status', '已提交', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000210', 'his_inspection_apply_status', '已缴费', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000211', 'his_inspection_apply_status', '已预约', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000212', 'his_inspection_apply_status', '检查中', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000213', 'his_inspection_apply_status', '已出报告', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000214', 'his_inspection_apply_status', '已取消', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000215', 'his_laboratory_apply_status', '已提交', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000216', 'his_laboratory_apply_status', '已缴费', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000217', 'his_laboratory_apply_status', '已采样', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000218', 'his_laboratory_apply_status', '检验中', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000219', 'his_laboratory_apply_status', '已出报告', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000220', 'his_laboratory_apply_status', '已取消', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000221', 'his_treatment_apply_status', '待执行', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000222', 'his_treatment_apply_status', '已执行', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000223', 'his_treatment_apply_status', '已取消', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000224', 'his_treatment_record_status', '异常', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000225', 'his_treatment_record_status', '正常', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000226', 'his_inspection_record_status', '已登记', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000227', 'his_inspection_record_status', '已签到', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000228', 'his_inspection_record_status', '检查中', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000229', 'his_inspection_record_status', '已出结果', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000230', 'his_inspection_record_status', '已审核', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000231', 'his_inspection_record_status', '已发布', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000232', 'his_inspection_record_status', '已取消', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000233', 'his_laboratory_record_status', '已登记', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000234', 'his_laboratory_record_status', '已采样', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000235', 'his_laboratory_record_status', '已接收', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000236', 'his_laboratory_record_status', '检测中', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000237', 'his_laboratory_record_status', '已出结果', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000238', 'his_laboratory_record_status', '已审核', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000239', 'his_laboratory_record_status', '已发布', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000240', 'his_laboratory_record_status', '已取消', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000241', 'his_specimen_status', '待采集', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000242', 'his_specimen_status', '已采集', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000243', 'his_specimen_status', '已接收', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000244', 'his_specimen_status', '检测中', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000245', 'his_specimen_status', '已完成', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000246', 'his_specimen_status', '已退回', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000247', 'his_abnormal_flag', '正常', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000248', 'his_abnormal_flag', '偏高', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000249', 'his_abnormal_flag', '偏低', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000250', 'his_abnormal_flag', '异常', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000251', 'his_result_type', '定量', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000252', 'his_result_type', '定性', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000253', 'his_result_type', '文字描述', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000254', 'his_report_type', '检查报告', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000255', 'his_report_type', '检验报告', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000256', 'his_report_status', '待审核', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000257', 'his_report_status', '初审通过', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000258', 'his_report_status', '已审核', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000259', 'his_report_status', '已发布', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000260', 'his_report_status', '已作废', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000261', 'his_critical_value_status', '待接收', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000262', 'his_critical_value_status', '已接收', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000263', 'his_critical_value_status', '已处置', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000264', 'his_critical_value_status', '已作废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000265', 'his_critical_value_type', '偏低', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000266', 'his_critical_value_type', '偏高', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000267', 'his_notify_status', '未通知', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000268', 'his_notify_status', '已通知', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000269', 'his_inspection_item_type', '放射检查', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000270', 'his_inspection_item_type', '超声检查', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000271', 'his_inspection_item_type', '心电图', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000272', 'his_inspection_item_type', '内镜检查', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000273', 'his_inspection_item_type', '其他', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000274', 'his_laboratory_item_type', '血液检验', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000275', 'his_laboratory_item_type', '尿液检验', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000276', 'his_laboratory_item_type', '生化检验', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000277', 'his_laboratory_item_type', '免疫检验', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000278', 'his_laboratory_item_type', '微生物检验', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000279', 'his_laboratory_item_type', '其他', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000280', 'his_treatment_item_type', '注射', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000281', 'his_treatment_item_type', '输液', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000282', 'his_treatment_item_type', '换药', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000283', 'his_treatment_item_type', '拆线', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000284', 'his_treatment_item_type', '其他', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000285', 'his_medicaltech_apply_type', '检查', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000286', 'his_medicaltech_apply_type', '检验', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000287', 'his_medicaltech_exec_status', '待执行', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000288', 'his_medicaltech_exec_status', '执行中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000289', 'his_medicaltech_exec_status', '已完成', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000290', 'his_medicaltech_exec_status', '已审核', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000291', 'his_admit_status', '已出院', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000292', 'his_admit_status', '在院', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000293', 'his_admit_way', '门诊', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000294', 'his_admit_way', '急诊', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000295', 'his_admit_way', '转院', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000296', 'his_admit_way', '其他', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000297', 'his_discharge_way', '医嘱离院', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000298', 'his_discharge_way', '医嘱转院', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000299', 'his_discharge_way', '医嘱转社区', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000300', 'his_discharge_way', '非医嘱离院', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000301', 'his_discharge_way', '死亡', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000302', 'his_discharge_way', '其他', '9', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000303', 'his_discharge_status', '正常', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000304', 'his_discharge_status', '转科', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000305', 'his_discharge_status', '自动出院', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000306', 'his_admission_order_status', '待收治', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000307', 'his_admission_order_status', '已收治', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000308', 'his_admission_order_status', '已作废', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000309', 'his_admission_order_status', '已过期', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000310', 'his_admission_order_gender', '女', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000311', 'his_admission_order_gender', '男', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000312', 'his_death_flag', '否', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000313', 'his_death_flag', '是', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000314', 'his_order_type', '长期', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000315', 'his_order_type', '临时', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000316', 'his_order_class', '药品', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000317', 'his_order_class', '检查', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000318', 'his_order_class', '检验', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000319', 'his_order_class', '治疗', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000320', 'his_order_class', '护理', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000321', 'his_order_class', '手术', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000322', 'his_order_class', '输血', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000323', 'his_order_class', '监护', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000324', 'his_order_class', '其他', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000325', 'his_inpatient_order_status', '待校对', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000326', 'his_inpatient_order_status', '已校对', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000327', 'his_inpatient_order_status', '执行中', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000328', 'his_inpatient_order_status', '已完成', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000329', 'his_inpatient_order_status', '已停止', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000330', 'his_inpatient_order_status', '已作废', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000331', 'his_inpatient_order_status', '已退回', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000332', 'his_inpatient_order_source', '医生', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000333', 'his_inpatient_order_source', '模板', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000334', 'his_inpatient_order_source', '组套', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000335', 'his_order_exec_status', '待执行', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000336', 'his_order_exec_status', '已执行', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000337', 'his_order_exec_status', '已跳过', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000338', 'his_order_exec_status', '已退回', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000339', 'his_inpatient_record_type', '入院记录', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000340', 'his_inpatient_record_type', '首次病程', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000341', 'his_inpatient_record_type', '日常病程', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000342', 'his_inpatient_record_type', '术前小结', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000343', 'his_inpatient_record_type', '手术记录', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000344', 'his_inpatient_record_type', '术后首次病程', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000345', 'his_inpatient_record_type', '出院记录', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000346', 'his_inpatient_record_type', '死亡记录', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000347', 'his_inpatient_record_status', '草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000348', 'his_inpatient_record_status', '已提交', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000349', 'his_inpatient_record_status', '已归档', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000350', 'his_inpatient_log_doc_type', '住院病历文书', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000351', 'his_inpatient_log_doc_type', '护理文书', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000352', 'his_nursing_record_type', '三测单', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000353', 'his_nursing_record_type', '护理记录单', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000354', 'his_nursing_record_type', '生命体征监测', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000355', 'his_nursing_shift', '白班', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000356', 'his_nursing_shift', '小夜班', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000357', 'his_nursing_shift', '大夜班', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000358', 'his_nursing_level', '特级护理', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000359', 'his_nursing_level', '一级护理', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000360', 'his_nursing_level', '二级护理', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000361', 'his_nursing_level', '三级护理', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000362', 'his_age_unit', '岁', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000363', 'his_age_unit', '月', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000364', 'his_age_unit', '天', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000365', 'his_diag_type', '主要诊断', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000366', 'his_diag_type', '其他诊断', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000367', 'his_admit_condition', '有', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000368', 'his_admit_condition', '临床未确定', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000369', 'his_admit_condition', '情况不明', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000370', 'his_admit_condition', '无', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000371', 'his_operation_level', '一级手术', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000372', 'his_operation_level', '二级手术', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000373', 'his_operation_level', '三级手术', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000374', 'his_operation_level', '四级手术', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000375', 'his_incision_level', '0类(无切口)', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000376', 'his_incision_level', 'I类(清洁)', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000377', 'his_incision_level', 'II类(清洁污染)', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000378', 'his_incision_level', 'III类(污染)', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000379', 'his_anesthesia_type', '全身麻醉', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000380', 'his_anesthesia_type', '椎管内麻醉', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000381', 'his_anesthesia_type', '神经阻滞麻醉', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000382', 'his_anesthesia_type', '局部麻醉', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000383', 'his_anesthesia_type', '其他', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000384', 'his_operation_status', '待排期', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000385', 'his_operation_status', '已排期', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000386', 'his_operation_status', '术前核对完成', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000387', 'his_operation_status', '已完成', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000388', 'his_operation_status', '已取消', '4', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000389', 'his_transfer_type', '普通转科', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000390', 'his_transfer_type', '急诊转科', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000391', 'his_transfer_type', '转入ICU', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000392', 'his_transfer_type', 'ICU转出', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000393', 'his_transfer_status', '待接收', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000394', 'his_transfer_status', '已完成', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000395', 'his_transfer_status', '已取消', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000396', 'his_consult_status', '待应答', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000397', 'his_consult_status', '已完成', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000398', 'his_consult_status', '已取消', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000399', 'his_consult_status', '已应答/会诊中', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000400', 'his_consult_status_legacy', '待会诊', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000401', 'his_consult_status_legacy', '已完成', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000402', 'his_consult_status_legacy', '已取消', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000403', 'his_consult_scope', '科内会诊', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000404', 'his_consult_scope', '科间会诊', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000405', 'his_consult_scope', '全院会诊', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000406', 'his_blood_component', '红细胞悬液', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000407', 'his_blood_component', '血浆', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000408', 'his_blood_component', '血小板', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000409', 'his_blood_component', '冷沉淀', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000410', 'his_blood_component', '全血', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000411', 'his_blood_component', '其他', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000412', 'his_crossmatch_status', '待配血', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000413', 'his_crossmatch_status', '配血中', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000414', 'his_crossmatch_status', '全部相合且配齐', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000415', 'his_crossmatch_status', '存在配血不合', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000416', 'his_crossmatch_result', '相合', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000417', 'his_crossmatch_result', '不合', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000418', 'his_transfusion_status', '待配血', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000419', 'his_transfusion_status', '已配血', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000420', 'his_transfusion_status', '已发血', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000421', 'his_transfusion_status', '输注中', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000422', 'his_transfusion_status', '已完成', '4', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000423', 'his_transfusion_status', '已取消', '5', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000424', 'his_bag_status', '待配血', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000425', 'his_bag_status', '已配血', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000426', 'his_bag_status', '已发血', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000427', 'his_bag_status', '已输注', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000428', 'his_transfusion_reaction', '未上报', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000429', 'his_transfusion_reaction', '已上报有反应', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000430', 'his_prepay_type', '充值', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000431', 'his_prepay_type', '退款', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000432', 'his_prepay_pay_method', '现金', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000433', 'his_prepay_pay_method', '微信', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000434', 'his_prepay_pay_method', '支付宝', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000435', 'his_prepay_pay_method', '银行卡', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000436', 'his_prepay_pay_method', '转账', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000437', 'his_inpatient_settle_status', '已结清', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000438', 'his_inpatient_settle_status', '欠费', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000439', 'his_inpatient_settle_status', '已作废', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000440', 'his_inpatient_settle_mode', '自费', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000441', 'his_inpatient_settle_mode', '医保', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000442', 'his_bed_status', '维修', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000443', 'his_bed_status', '空闲', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000444', 'his_bed_status', '占用', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000445', 'his_bed_status', '锁定', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000446', 'his_drug_type', '西药', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000447', 'his_drug_type', '中成药', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000448', 'his_drug_type', '中药饮片', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000449', 'his_dispensing_status', '待发药', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000450', 'his_dispensing_status', '已发药', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000451', 'his_dispensing_status', '已退药', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000452', 'his_dispensing_status_legacy', '待发药', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000453', 'his_dispensing_status_legacy', '已发药', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000454', 'his_dispensing_status_legacy', '已退药', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000455', 'his_dispensing_status_legacy', '已取消', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000456', 'his_drug_inbound_type', '采购入库', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000457', 'his_drug_inbound_type', '退货入库', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000458', 'his_drug_inbound_type', '盘盈入库', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000459', 'his_drug_inbound_type', '其他入库', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000460', 'his_inbound_status', '待审核', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000461', 'his_inbound_status', '已审核', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000462', 'his_inbound_status', '已入库', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000463', 'his_inbound_status', '已取消', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000464', 'his_drug_outbound_type', '发药出库', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000465', 'his_drug_outbound_type', '报损出库', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000466', 'his_drug_outbound_type', '退药出库', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000467', 'his_drug_outbound_type', '调拨出库', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000468', 'his_drug_outbound_type', '其他出库', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000469', 'his_outbound_status', '待审核', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000470', 'his_outbound_status', '已审核', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000471', 'his_outbound_status', '已出库', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000472', 'his_outbound_status', '已取消', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000473', 'his_drug_stock_log_type', '入库', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000474', 'his_drug_stock_log_type', '发药出库', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000475', 'his_drug_stock_log_type', '退药回库', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000476', 'his_drug_stock_log_type', '其他出库', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000477', 'his_drug_stock_log_type', '盘盈', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000478', 'his_drug_stock_log_type', '盘亏', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000479', 'his_consumable_category', '卫生材料', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000480', 'his_consumable_category', '注射穿刺', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000481', 'his_consumable_category', '医用敷料', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000482', 'his_consumable_category', '防护用品', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000483', 'his_consumable_category', '其他', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000484', 'his_consumable_stock_log_type', '入库', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000485', 'his_consumable_stock_log_type', '领用出库', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000486', 'his_consumable_stock_log_type', '退回入库', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000487', 'his_consumable_stock_log_type', '其他出库', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000488', 'his_consumable_stock_log_type', '盘盈', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000489', 'his_consumable_stock_log_type', '盘亏', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000490', 'his_purchase_approval_status', '待审批', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000491', 'his_purchase_approval_status', '已通过', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000492', 'his_purchase_approval_status', '已驳回', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000493', 'his_drug_package_type', '药品套餐', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000494', 'his_drug_package_type', '检查套餐', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000495', 'his_drug_package_type', '综合套餐', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000496', 'his_package_item_type', '药品', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000497', 'his_package_item_type', '检查', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000498', 'his_package_item_type', '检验', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000499', 'his_supplier_rating', '差', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000500', 'his_supplier_rating', '一般', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000501', 'his_supplier_rating', '良好', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000502', 'his_supplier_rating', '优秀', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000503', 'his_dept_type', '门诊科室', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000504', 'his_dept_type', '医技科室', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000505', 'his_dept_type', '药房', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000506', 'his_dept_type', '住院科室', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000507', 'his_dept_type', '其他', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000508', 'his_emp_type_note', '医生', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000509', 'his_emp_type_note', '护士', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000510', 'his_emp_type_note', '收费员', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000511', 'his_emp_type_note', '药剂师', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000512', 'his_emp_type_note', '管理员', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000513', 'his_emp_type_note', '其他', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000514', 'his_role_type', '系统角色', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000515', 'his_role_type', '自定义角色', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000516', 'his_data_scope', '全部数据', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000517', 'his_data_scope', '自定义数据', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000518', 'his_data_scope', '本部门数据', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000519', 'his_data_scope', '本部门及以下', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000520', 'his_data_scope', '仅本人数据', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000521', 'his_menu_type', '目录', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000522', 'his_menu_type', '菜单', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000523', 'his_menu_type', '按钮', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000524', 'his_channel_num', '站内信', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000525', 'his_channel_num', '短信', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000526', 'his_channel_num', '微信', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000527', 'his_channel_num', '邮件', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000528', 'his_oper_business_type', '其他', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000529', 'his_oper_business_type', '新增', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000530', 'his_oper_business_type', '修改', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000531', 'his_oper_business_type', '删除', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000532', 'his_oper_business_type', '授权', '4', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000533', 'his_oper_business_type', '导出', '5', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000534', 'his_oper_business_type', '导入', '6', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000535', 'his_oper_business_type', '清空', '7', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000536', 'his_oper_status', '正常', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000537', 'his_oper_status', '异常', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000538', 'his_login_status', '成功', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000539', 'his_login_status', '失败', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000540', 'his_audit_log_status', '失败', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000541', 'his_audit_log_status', '成功', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000542', 'his_message_send_status', '待发送', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000543', 'his_message_send_status', '已发送', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000544', 'his_message_send_status', '发送失败', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000545', 'his_message_read_status', '未读', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000546', 'his_message_read_status', '已读', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000547', 'his_alert_status', '未处理', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000548', 'his_alert_status', '已处理', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000549', 'his_alert_status', '已忽略', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000550', 'his_alert_active', '停用', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000551', 'his_alert_active', '启用', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000552', 'his_config_type', '系统', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000553', 'his_config_type', '业务', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000554', 'his_referral_status', '待确认', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000555', 'his_referral_status', '已确认', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000556', 'his_referral_status', '已完成', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000557', 'his_referral_status', '已取消', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000558', 'his_followup_type', '复诊提醒', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000559', 'his_followup_type', '慢病随访', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000560', 'his_followup_type', '用药指导', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000561', 'his_followup_type', '术后随访', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000562', 'his_followup_status', '待随访', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000563', 'his_followup_status', '随访中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000564', 'his_followup_status', '已完成', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000565', 'his_followup_status', '已取消', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000566', 'his_ph_report_type', '传染病', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000567', 'his_ph_report_type', '死因监测', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000568', 'his_ph_report_type', '慢性病', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000569', 'his_ph_report_type', '其他', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000570', 'his_ph_report_status', '待审核', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000571', 'his_ph_report_status', '审核通过', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000572', 'his_ph_report_status', '审核驳回', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000573', 'his_archive_status', '待归档', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000574', 'his_archive_status', '已归档', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000575', 'his_archive_status', '已封存', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000576', 'his_qc_type', '综合', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000577', 'his_qc_type', '完整性', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000578', 'his_qc_type', '规范性', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000579', 'his_qc_type', '逻辑性', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000580', 'his_qc_type', 'AI内涵质控', '4', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000581', 'his_qc_severity', '无问题', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000582', 'his_qc_severity', '提示', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000583', 'his_qc_severity', '重要', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000584', 'his_qc_severity', '否决项', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000585', 'his_qc_dim', '完整性', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000586', 'his_qc_dim', '规范性', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000587', 'his_qc_dim', '逻辑性', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000588', 'his_qc_result', '不通过', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000589', 'his_qc_result', '通过', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000590', 'his_qc_status', '待处理', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000591', 'his_qc_status', '已处理', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000592', 'his_qc_status', '已忽略', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000593', 'his_rule_type', '配伍禁忌', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000594', 'his_rule_type', '检验诊断关联性', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000595', 'his_rule_type', '用药合理性', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000596', 'his_rule_check_result', '不通过', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000597', 'his_rule_check_result', '通过', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000598', 'his_rule_error_level', '警告', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000599', 'his_rule_error_level', '错误', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000600', 'his_rule_error_level', '严重', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000601', 'his_rule_check_status', '待处理', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000602', 'his_rule_check_status', '已处理', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000603', 'his_rule_check_status', '已忽略', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000604', 'his_audit_type', '结算前自查', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000605', 'his_audit_type', '批量筛查', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000606', 'his_audit_type', '医保反馈复核', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000607', 'his_compliance_risk_level', '未发现', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000608', 'his_compliance_risk_level', '提示', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000609', 'his_compliance_risk_level', '关注', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000610', 'his_compliance_risk_level', '高危', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000611', 'his_compliance_item_result', '命中', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000612', 'his_compliance_item_result', '通过', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000613', 'his_compliance_item_result', '不适用', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000614', 'his_compliance_target_type', '清单级', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000615', 'his_compliance_target_type', '诊断', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000616', 'his_compliance_target_type', '手术操作', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000617', 'his_ai_call_status', '成功', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000618', 'his_ai_call_status', '失败', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000619', 'his_ai_call_status', '超时', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000620', 'his_ai_call_status', '降级', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000621', 'his_ai_call_status', '熔断', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000622', 'his_sig_biz_type', '住院病历', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000623', 'his_sig_biz_type', '门诊病历', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000624', 'his_sig_biz_type', '住院医嘱', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000625', 'his_sig_biz_type', '处方', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000626', 'his_sig_biz_type', '检查报告', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000627', 'his_sig_biz_type', '检验报告', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000628', 'his_sig_scene', '提交', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000629', 'his_sig_scene', '归档', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000630', 'his_sig_scene', '开立', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000631', 'his_sig_scene', '校对', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000632', 'his_sig_scene', '补签', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000633', 'his_sig_scene', '开方', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000634', 'his_sig_scene', '审方', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000635', 'his_sig_scene', '报告签名', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000636', 'his_sig_scene', '报告审核签名', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000637', 'his_sig_status', '有效', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000638', 'his_sig_status', '已作废', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000639', 'his_sig_verify_status', '未校验', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000640', 'his_sig_verify_status', '通过', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000641', 'his_sig_verify_status', '失败', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000642', 'his_sig_object_status', '未签名', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000643', 'his_sig_object_status', '已签名', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000644', 'his_sig_object_status', '签名已失效', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000645', 'his_cert_status', '有效', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000646', 'his_cert_status', '已吊销', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000647', 'his_cert_issued_mode', '人工签发', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000648', 'his_cert_issued_mode', '系统自动签发', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000649', 'his_time_source', '本机时钟', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000650', 'his_time_source', '院内授时服务器', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000651', 'his_time_source', '第三方可信时间戳', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000652', 'his_ins_settlement_status', '待结算', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000653', 'his_ins_settlement_status', '已结算', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000654', 'his_ins_settlement_status', '已上传', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000655', 'his_ins_settlement_status', '已审核', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000656', 'his_ins_audit_status', '待审核', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000657', 'his_ins_audit_status', '审核通过', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000658', 'his_ins_audit_status', '审核驳回', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000659', 'his_merge_log_status', '已合并', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000660', 'his_merge_log_status', '已撤销', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000661', 'his_merge_match_type', '身份证号相同(强)', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000662', 'his_merge_match_type', '姓名+性别+出生日期相同', '2', 2, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000663', 'his_merge_match_type', '姓名+手机号相同', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000664', 'his_merge_match_type', '人工判定', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000665', 'his_equipment_category', '大型影像设备', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000666', 'his_equipment_category', '检验分析设备', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000667', 'his_equipment_category', '生命支持设备', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000668', 'his_equipment_category', '手术室设备', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000669', 'his_equipment_category', '监护急救设备', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000670', 'his_equipment_category', '康复理疗设备', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000671', 'his_equipment_category', '其他设备', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000672', 'his_equipment_status', '在用', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000673', 'his_equipment_status', '停用', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000674', 'his_equipment_status', '维修中', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000675', 'his_equipment_status', '报废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000680', 'his_regist_status', '爽约', '7', 7, NULL, NULL, 0, 1, 'admin', 'migrate-59', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000681', 'his_regist_status', '未就诊', '8', 8, NULL, NULL, 0, 1, 'admin', 'migrate-59', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000682', 'his_appoint_status', '爽约', '7', 7, NULL, NULL, 0, 1, 'admin', 'migrate-59', 1, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000683', 'his_appoint_status', '未就诊', '8', 8, NULL, NULL, 0, 1, 'admin', 'migrate-59', 1, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000690', 'sys_gender', '男', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000691', 'sys_gender', '女', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000692', 'sys_gender', '未知', '9', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000700', 'his_schedule_type', '凌晨', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000801', 'his_drug_special_flag', '普通药品', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000802', 'his_drug_special_flag', '麻醉药品', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000803', 'his_drug_special_flag', '第一类精神药品', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000804', 'his_drug_special_flag', '第二类精神药品', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000805', 'his_drug_special_flag', '毒性药品', '4', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000811', 'his_adverse_event_type', '药品事件', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000812', 'his_adverse_event_type', '跌倒/坠床', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000813', 'his_adverse_event_type', '压疮', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000814', 'his_adverse_event_type', '职业暴露', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000815', 'his_adverse_event_type', '手术相关', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000816', 'his_adverse_event_type', '输液/输血', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000817', 'his_adverse_event_type', '管路事件', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000818', 'his_adverse_event_type', '院感相关', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000819', 'his_adverse_event_type', '设备器械', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000820', 'his_adverse_event_type', '信息安全', '10', 10, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000821', 'his_adverse_event_type', '其他', '11', 11, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000822', 'his_adverse_event_level', 'I级 警讯事件', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000823', 'his_adverse_event_level', 'II级 不良后果', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000824', 'his_adverse_event_level', 'III级 未造成后果', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000825', 'his_adverse_event_level', 'IV级 隐患', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000826', 'his_adverse_event_status', '已上报待处理', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000827', 'his_adverse_event_status', '处理中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000828', 'his_adverse_event_status', '已整改', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000829', 'his_adverse_event_status', '已结案', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000851', 'his_archive_borrow_type', '借阅', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000852', 'his_archive_borrow_type', '复印', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000853', 'his_archive_borrow_status', '待审核', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000854', 'his_archive_borrow_status', '已借出', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000855', 'his_archive_borrow_status', '已归还', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000856', 'his_archive_borrow_status', '已拒绝', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000857', 'his_archive_borrow_status', '已复印', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000858', 'his_archive_code_status', '待编码', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000859', 'his_archive_code_status', '已提交', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000860', 'his_archive_code_status', '已完成', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000861', 'his_archive_code_status', '已退修', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000862', 'his_antibiotic_level', '非抗菌药物', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000863', 'his_antibiotic_level', '非限制使用级', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000864', 'his_antibiotic_level', '限制使用级', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000865', 'his_antibiotic_level', '特殊使用级', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000866', 'his_antibiotic_auth_status', '有效', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000867', 'his_antibiotic_auth_status', '暂停', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000868', 'his_antibiotic_auth_status', '取消', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000869', 'his_antibiotic_timing', '术前0.5~1小时', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000870', 'his_antibiotic_timing', '术前>1小时', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000871', 'his_antibiotic_timing', '术前<0.5小时', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000872', 'his_antibiotic_timing', '术中追加', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000873', 'his_antibiotic_timing', '术后才开始', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000874', 'his_antibiotic_timing', '未使用', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000875', 'his_antibiotic_incision_problem', '无预防用药指征', '41', 1, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000876', 'his_antibiotic_incision_problem', '品种选择不合理', '42', 2, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000877', 'his_antibiotic_incision_problem', '给药时机不合理', '43', 3, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000878', 'his_antibiotic_incision_problem', '疗程过长', '44', 4, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000879', 'his_antibiotic_incision_problem', '无指征联合用药', '45', 5, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000880', 'his_antibiotic_incision_problem', '剂量不合理', '46', 6, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000881', 'his_antibiotic_incision_problem', '特殊使用级无会诊', '47', 7, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000882', 'his_antibiotic_incision_problem', '术后用药起点不明', '48', 8, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000901', 'his_pathology_exam_type', '常规石蜡', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000902', 'his_pathology_exam_type', '术中冰冻', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000903', 'his_pathology_exam_type', '细胞学', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000904', 'his_pathology_exam_type', '免疫组化', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000905', 'his_pathology_exam_type', '疑难会诊', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000906', 'his_pathology_status', '已登记', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000907', 'his_pathology_status', '已接收标本', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000908', 'his_pathology_status', '已取材', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000909', 'his_pathology_status', '已制片', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000910', 'his_pathology_status', '已初诊', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000911', 'his_pathology_status', '已审核', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000912', 'his_pathology_status', '已发布', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000913', 'his_pathology_status', '已取消', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000914', 'his_pathology_block_status', '待取材', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000915', 'his_pathology_block_status', '已取材', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000916', 'his_pathology_block_status', '已包埋', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000917', 'his_pathology_block_status', '已切片', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000918', 'his_endoscopy_type', '胃镜', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000919', 'his_endoscopy_type', '肠镜', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000920', 'his_endoscopy_type', '支气管镜', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000921', 'his_endoscopy_type', '膀胱镜', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000922', 'his_endoscopy_type', '宫腔镜', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000923', 'his_endoscopy_type', '喉镜', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000924', 'his_endoscopy_type', 'ERCP', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000925', 'his_endoscopy_type', '胶囊内镜', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000926', 'his_endoscopy_anesthesia', '无麻醉', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000927', 'his_endoscopy_anesthesia', '表面麻醉', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000928', 'his_endoscopy_anesthesia', '静脉麻醉', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000929', 'his_endoscopy_anesthesia', '全身麻醉', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000930', 'his_ultrasound_type', '腹部超声', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000931', 'his_ultrasound_type', '心脏超声', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000932', 'his_ultrasound_type', '妇产超声', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000933', 'his_ultrasound_type', '血管超声', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000934', 'his_ultrasound_type', '浅表器官', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000935', 'his_ultrasound_type', '肌骨超声', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000936', 'his_ultrasound_type', '腔内超声', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000937', 'his_endous_status', '已登记', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000938', 'his_endous_status', '已签到', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000939', 'his_endous_status', '检查中', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000940', 'his_endous_status', '已出报告', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000941', 'his_endous_status', '已审核', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000942', 'his_endous_status', '已发布', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000943', 'his_endous_status', '已取消', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000944', 'his_lis_qc_level', '低值', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000945', 'his_lis_qc_level', '中值', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000946', 'his_lis_qc_level', '高值', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000947', 'his_lis_qc_status', '在控', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000948', 'his_lis_qc_status', '警告', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000949', 'his_lis_qc_status', '失控', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000950', 'his_lis_qc_handle_status', '无需处理', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000951', 'his_lis_qc_handle_status', '待处理', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000952', 'his_lis_qc_handle_status', '已处理', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000953', 'his_blood_type', 'A 型', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000954', 'his_blood_type', 'B 型', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000955', 'his_blood_type', 'O 型', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000956', 'his_blood_type', 'AB 型', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000957', 'his_blood_rh', 'Rh 阳性', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000958', 'his_blood_rh', 'Rh 阴性', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000965', 'his_blood_inventory_status', '在库', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000966', 'his_blood_inventory_status', '已预留', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000967', 'his_blood_inventory_status', '已发血', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000968', 'his_blood_inventory_status', '已报废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000969', 'his_blood_inventory_status', '已退回', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000970', 'his_blood_source_type', '血站', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000971', 'his_blood_source_type', '自体储血', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000972', 'his_blood_source_type', '互助献血', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000973', 'his_crossmatch_method', '盐水法', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000974', 'his_crossmatch_method', '凝聚胺法', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000975', 'his_crossmatch_method', '抗人球蛋白法', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000976', 'his_crossmatch_method', '微柱凝胶法', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000979', 'his_crossmatch_result', '可疑凝集', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000000983', 'his_crossmatch_status', '已作废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001001', 'his_discharge_drug_status', '待发药', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001002', 'his_discharge_drug_status', '已发药', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001011', 'his_referral_direction', '上转', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001012', 'his_referral_direction', '下转', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001051', 'his_equipment_maintain_type', '保养', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001052', 'his_equipment_maintain_type', '维修', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001053', 'his_equipment_maintain_type', '巡检', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001061', 'his_equipment_metering_type', '强检', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001062', 'his_equipment_metering_type', '校准', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001071', 'his_equipment_metering_result', '合格', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001072', 'his_equipment_metering_result', '不合格', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001081', 'his_cssd_pack_status', '已回收', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001082', 'his_cssd_pack_status', '清洗中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001083', 'his_cssd_pack_status', '已打包', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001084', 'his_cssd_pack_status', '灭菌中', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001085', 'his_cssd_pack_status', '待发放', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001086', 'his_cssd_pack_status', '已发放', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001091', 'his_cssd_node_type', '回收', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001092', 'his_cssd_node_type', '清洗', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001093', 'his_cssd_node_type', '打包', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001094', 'his_cssd_node_type', '灭菌', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001095', 'his_cssd_node_type', '储存', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001096', 'his_cssd_node_type', '发放', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001101', 'his_cssd_steril_method', '高压蒸汽', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001102', 'his_cssd_steril_method', '环氧乙烷', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001103', 'his_cssd_steril_method', '低温等离子', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001111', 'his_waste_type', '感染性废物', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001112', 'his_waste_type', '损伤性废物', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001113', 'his_waste_type', '病理性废物', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001114', 'his_waste_type', '药物性废物', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001115', 'his_waste_type', '化学性废物', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001121', 'his_waste_status', '已登记', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001122', 'his_waste_status', '已交接', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001123', 'his_waste_status', '已处置', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001200', 'his_checkup_record_status', '已登记', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001201', 'his_checkup_record_status', '检查中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001202', 'his_checkup_record_status', '已完成', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001203', 'his_checkup_record_status', '已出报告', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001204', 'his_checkup_person_type', '个人', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001205', 'his_checkup_person_type', '团体', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001206', 'his_checkup_result_flag', '正常', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001207', 'his_checkup_result_flag', '异常', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001208', 'his_checkup_result_flag', '待查', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001209', 'his_drg_sim_status', '已入组', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001210', 'his_drg_sim_status', '未入组', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001211', 'his_perf_status', '草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001212', 'his_perf_status', '已核算', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001213', 'his_perf_status', '已发布', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001301', 'his_revisit_source', '当日回诊', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001302', 'his_revisit_source', '医嘱复诊预约', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001303', 'his_revisit_source', '患者自助复诊', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001304', 'his_revisit_source', '随访计划复诊', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001311', 'his_revisit_charge_mode', '全额收费', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001312', 'his_revisit_charge_mode', '免挂号费', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000001313', 'his_revisit_charge_mode', '免挂号费+诊查费', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002101', 'his_lis_eqa_plan_status', '待收样', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002102', 'his_lis_eqa_plan_status', '检测中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002103', 'his_lis_eqa_plan_status', '已上报', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002104', 'his_lis_eqa_plan_status', '已回报', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002105', 'his_lis_eqa_plan_status', '已归档', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002111', 'his_lis_eqa_sample_status', '待检测', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002112', 'his_lis_eqa_sample_status', '已检测', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002113', 'his_lis_eqa_sample_status', '已上报', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002114', 'his_lis_eqa_sample_status', '已回报', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002121', 'his_lis_eqa_result_status', '未判定', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002122', 'his_lis_eqa_result_status', '满意', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002123', 'his_lis_eqa_result_status', '尚可', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002124', 'his_lis_eqa_result_status', '不合格', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002131', 'his_lis_eqa_judge_mode', '无法判定', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002132', 'his_lis_eqa_judge_mode', 'SDI 指数', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002133', 'his_lis_eqa_judge_mode', '允许总误差 TEa', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002134', 'his_lis_eqa_judge_mode', '可接受范围', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002141', 'his_lis_eqa_compare_status', '可接受', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000002142', 'his_lis_eqa_compare_status', '超差', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003001', 'his_exam_device_type', 'CT', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003002', 'his_exam_device_type', 'MR（磁共振）', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003003', 'his_exam_device_type', 'DR/CR', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003004', 'his_exam_device_type', '超声', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003005', 'his_exam_device_type', '心电', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003006', 'his_exam_device_type', '内镜', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003007', 'his_exam_device_type', '其他', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003011', 'his_exam_appoint_status', '已预约', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003012', 'his_exam_appoint_status', '已到检', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003013', 'his_exam_appoint_status', '已完成', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003014', 'his_exam_appoint_status', '已取消', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003015', 'his_exam_appoint_status', '爽约（未按时到检）', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003021', 'his_exam_device_status', '开放预约', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003022', 'his_exam_device_status', '暂停预约', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003031', 'his_exam_slot_status', '锁号（停用）', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003032', 'his_exam_slot_status', '正常', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003201', 'his_treatment_exec_status', '待执行', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003202', 'his_treatment_exec_status', '已执行', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003203', 'his_treatment_exec_status', '已取消', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003211', 'his_treatment_charge_status', '未计费', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003212', 'his_treatment_charge_status', '已计费', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003213', 'his_treatment_charge_status', '计费失败', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003214', 'his_treatment_charge_status', '无需计费', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003231', 'his_infectious_class', '甲类', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003232', 'his_infectious_class', '乙类', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003233', 'his_infectious_class', '丙类', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003241', 'his_infectious_report_status', '待审核', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003242', 'his_infectious_report_status', '已审核', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003243', 'his_infectious_report_status', '已直报', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003244', 'his_infectious_report_status', '已退报', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003251', 'his_infection_case_status', '待核实', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003252', 'his_infection_case_status', '已确认', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003253', 'his_infection_case_status', '已排除', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003261', 'his_infection_source', '医院感染', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003262', 'his_infection_source', '社区感染', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003271', 'his_infection_site', '下呼吸道', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003272', 'his_infection_site', '泌尿道', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003273', 'his_infection_site', '胃肠道', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003274', 'his_infection_site', '手术切口', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003275', 'his_infection_site', '血流感染', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003276', 'his_infection_site', '皮肤软组织', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003277', 'his_infection_site', '腹腔内', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003278', 'his_infection_site', '其他', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003281', 'his_infection_monitor_type', '尿管相关(CAUTI)', '1', 1, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003282', 'his_infection_monitor_type', '血管导管相关(CLABSI)', '2', 2, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003283', 'his_infection_monitor_type', '呼吸机相关(VAP)', '3', 3, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003291', 'his_infection_monitor_status', '在管', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003292', 'his_infection_monitor_status', '已拔管', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003301', 'his_hand_obs_object', '医生', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003302', 'his_hand_obs_object', '护士', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003303', 'his_hand_obs_object', '工勤/其他', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003401', 'his_prescription_audit_result', '通过', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000003402', 'his_prescription_audit_result', '退回', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009001', 'his_refund_apply_status', '已作废', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009002', 'his_refund_flow_source', '存量铺底', '0', 0, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009003', 'his_refund_flow_source', '退费申请执行', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009004', 'his_refund_flow_source', '收费处直退', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009005', 'his_refund_flow_source', '退号联动退费', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009110', 'his_encounter_type', '门诊', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009111', 'his_encounter_type', '住院', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009112', 'his_fee_status', '待结算', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009113', 'his_fee_status', '已锁定', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009114', 'his_fee_status', '已结算', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009115', 'his_fee_status', '已红冲', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009116', 'his_fee_source_type', '挂号', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009117', 'his_fee_source_type', '处方', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009118', 'his_fee_source_type', '检查申请', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009119', 'his_fee_source_type', '检验申请', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009120', 'his_fee_source_type', '治疗申请', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009121', 'his_fee_source_type', '发药/摆药', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009122', 'his_fee_source_type', '耗材使用', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009123', 'his_fee_source_type', '住院医嘱', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009124', 'his_fee_source_type', '手术', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009125', 'his_fee_source_type', '输血', '10', 10, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009126', 'his_fee_source_type', '手工补记账', '11', 11, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009127', 'his_fee_source_type', '其他', '12', 12, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009128', 'his_bill_type', '挂号费结算', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009129', 'his_bill_type', '门诊诊间结算', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009130', 'his_bill_type', '住院中途结算', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009131', 'his_bill_type', '出院结算', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009132', 'his_bill_status', '待支付', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009133', 'his_bill_status', '部分支付', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009134', 'his_bill_status', '已支付', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009135', 'his_bill_status', '已作废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009136', 'his_bill_status', '已退费', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009137', 'his_pay_direction', '收款', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009138', 'his_pay_direction', '退款', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009139', 'his_pay_txn_status', '成功', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009140', 'his_pay_txn_status', '已冲正', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009141', 'his_txn_source', '收费台收款', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009142', 'his_txn_source', '患者端支付', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009143', 'his_txn_source', '住院预交金', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009144', 'his_txn_source', '退费申请执行', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009145', 'his_txn_source', '收费处直退', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009146', 'his_txn_source', '退号联动退费', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009147', 'his_txn_source', '出院结算退差', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009148', 'his_txn_source', '账户余额抵扣', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009149', 'his_txn_source', '手工补账', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009150', 'his_account_owner_type', '患者（门诊余额）', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009151', 'his_account_owner_type', '住院就诊次（预交金）', '2', 2, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009152', 'his_account_txn_type', '住院预交金充值', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009153', 'his_account_txn_type', '预交金退款', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009154', 'his_account_txn_type', '余额支付扣减', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009155', 'his_account_txn_type', '余额退款入账', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009156', 'his_account_txn_type', '出院结算退差入账', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009157', 'his_account_txn_type', '手工调整', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009158', 'his_pay_method', '银行卡', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009159', 'his_pay_method', '转账', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009160', 'his_invoice_status', '已红冲换开', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009161', 'his_bill_status', '挂账/欠费', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009165', 'his_ins_settlement_status', '已作废', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009201', 'his_catalog_type', '自费', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009202', 'his_catalog_type', '甲类', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009203', 'his_catalog_type', '乙类', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009204', 'his_catalog_type', '丙类', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009701', 'his_consumable_stock_log_type', '使用出库', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009702', 'his_charge_item_type', '耗材材料', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000009703', 'his_consumable_category', '植入介入', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015601', 'his_drug_trace_code_type', 'GS1 码', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015602', 'his_drug_trace_code_type', '中国药品追溯码20位', '2', 2, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015603', 'his_drug_trace_code_type', '其他/未识别', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015611', 'his_drug_trace_status', '在库', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015612', 'his_drug_trace_status', '已发药核销', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015613', 'his_drug_trace_status', '已作废', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015621', 'his_drug_trace_source_type', '入库采集', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015622', 'his_drug_trace_source_type', '存量补采', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015631', 'his_drug_trace_upload_status', '待上传', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015632', 'his_drug_trace_upload_status', '已上传', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015633', 'his_drug_trace_upload_status', '上传失败', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015641', 'his_drug_trace_void_type', '退药', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015642', 'his_drug_trace_void_type', '报损', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891000000000015643', 'his_drug_trace_void_type', '召回', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000101', 'his_notice_type', '病危', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000102', 'his_notice_type', '病重', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000111', 'his_notice_status', '草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000112', 'his_notice_status', '已签发', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000113', 'his_notice_status', '已签收', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000114', 'his_notice_status', '已作废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000121', 'his_notice_consciousness', '清醒', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000122', 'his_notice_consciousness', '嗜睡', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000123', 'his_notice_consciousness', '意识模糊', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000124', 'his_notice_consciousness', '昏迷', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000129', 'his_notice_consciousness', '其他', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000131', 'his_notice_relation', '配偶', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000132', 'his_notice_relation', '父母', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000133', 'his_notice_relation', '子女', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000134', 'his_notice_relation', '兄弟姐妹', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000135', 'his_notice_relation', '配偶父母', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000136', 'his_notice_relation', '子女配偶', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000137', 'his_notice_relation', '祖父母/外祖父母', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000138', 'his_notice_relation', '孙子女/外孙子女', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000139', 'his_notice_relation', '法定代理人', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000140', 'his_notice_relation', '单位/组织负责人', '10', 10, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891610000000000149', 'his_notice_relation', '其他', '99', 99, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000101', 'his_leave_type', '临时外出（当日往返）', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000102', 'his_leave_type', '离院过夜', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000109', 'his_leave_type', '其他', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000111', 'his_leave_status', '待审批', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000112', 'his_leave_status', '已批准', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000113', 'his_leave_status', '已离院', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000114', 'his_leave_status', '已返回', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000115', 'his_leave_status', '已拒绝', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000116', 'his_leave_status', '已取消', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000121', 'his_leave_contact', '联系上并约定返回', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000122', 'his_leave_contact', '联系不上', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000123', 'his_leave_contact', '家属/随行人已知晓', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000131', 'his_leave_report', '主管医师', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000132', 'his_leave_report', '病区护士长', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891620000000000133', 'his_leave_report', '医务科/总值班', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000101', 'his_yb_inspect_type', '国家飞检', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000102', 'his_yb_inspect_type', '省级飞检', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000103', 'his_yb_inspect_type', '智能审核转来', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000104', 'his_yb_inspect_type', '日常驻点审核', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000111', 'his_yb_inspect_status', '进行中', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000112', 'his_yb_inspect_status', '已结项', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000113', 'his_yb_inspect_status', '已作废', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000121', 'his_yb_deduct_source', '飞检现场发现', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000122', 'his_yb_deduct_source', '智能审核/事后复核', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000131', 'his_yb_violation_type', '重复收费', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000132', 'his_yb_violation_type', '超适应症/超范围', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000133', 'his_yb_violation_type', '串换项目', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000134', 'his_yb_violation_type', '超标准收费', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000135', 'his_yb_violation_type', '虚假住院/虚假就诊', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000136', 'his_yb_violation_type', '无指征/无文书', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000139', 'his_yb_violation_type', '其他', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000141', 'his_yb_deduct_status', '待确认', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000142', 'his_yb_deduct_status', '申诉中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000143', 'his_yb_deduct_status', '申诉成功', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000144', 'his_yb_deduct_status', '维持扣款待缴', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000145', 'his_yb_deduct_status', '已缴回', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000146', 'his_yb_deduct_status', '已作废', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000151', 'his_yb_appeal_result', '申诉成功', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000152', 'his_yb_appeal_result', '申诉驳回', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000161', 'his_yb_loss_bear', '院方承担', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000162', 'his_yb_loss_bear', '科室承担', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000163', 'his_yb_loss_bear', '个人承担', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000164', 'his_yb_loss_bear', '科室+个人共担', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000171', 'his_yb_deduct_action', '新建草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000172', 'his_yb_deduct_action', '发起申诉', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000173', 'his_yb_deduct_action', '录入申诉结果', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000174', 'his_yb_deduct_action', '确认扣款并追责', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000175', 'his_yb_deduct_action', '录入缴回', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000176', 'his_yb_deduct_action', '作废', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000181', 'his_chronic_disease_type', '慢性病', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000182', 'his_chronic_disease_type', '特殊病', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000191', 'his_yb_chronic_status', '有效', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000192', 'his_yb_chronic_status', '已注销', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891630000000000193', 'his_yb_chronic_status', '已驳回', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000011', 'his_chronic_confirm_status', '待认定', '0', 1, NULL, 'info', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000012', 'his_chronic_confirm_status', '已认定', '1', 2, NULL, 'success', 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000013', 'his_chronic_confirm_status', '已取消', '2', 3, NULL, 'info', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000021', 'his_chronic_disease', '高血压', 'I10', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000022', 'his_chronic_disease', '2型糖尿病', 'E11', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000023', 'his_chronic_disease', '1型糖尿病', 'E10', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000024', 'his_chronic_disease', '冠状动脉粥样硬化性心脏病', 'I25.1', 4, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000025', 'his_chronic_disease', '慢性阻塞性肺疾病', 'J44.9', 5, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000026', 'his_chronic_disease', '支气管哮喘', 'J45', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000027', 'his_chronic_disease', '脑梗死后遗症', 'I69.3', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000028', 'his_chronic_disease', '慢性肾脏病', 'N18.9', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000029', 'his_chronic_disease', '恶性肿瘤化疗后', 'Z51.1', 9, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000030', 'his_chronic_disease', '高脂血症', 'E78.5', 10, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000031', 'his_chronic_disease', '慢性乙型病毒性肝炎', 'B18.1', 11, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000032', 'his_chronic_disease', '类风湿关节炎', 'M06.9', 12, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000033', 'his_chronic_disease', '骨质疏松症', 'M81.9', 13, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000034', 'his_chronic_disease', '精神分裂症', 'F20', 14, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000035', 'his_chronic_disease', '甲状腺功能减退症', 'E03.9', 15, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000036', 'his_chronic_disease', '前列腺增生', 'N40', 16, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000037', 'his_chronic_disease', '肝硬化', 'K74.6', 17, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000041', 'his_compliance_audit_type', '结算前自查', '1', 1, NULL, 'primary', 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000042', 'his_compliance_audit_type', '批量筛查', '2', 2, NULL, 'warning', 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('891650000000000043', 'his_compliance_audit_type', '医保反馈复核', '3', 3, NULL, 'danger', 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000111', 'his_stat_report_type', '卫统年报', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000112', 'his_stat_report_type', '出院患者统计月报', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000113', 'his_stat_report_type', '手术工作量专项报表', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000121', 'his_stat_period_type', '月报', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000122', 'his_stat_period_type', '年报', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000131', 'his_stat_report_status', '草稿', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000132', 'his_stat_report_status', '已报出', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000133', 'his_stat_report_status', '已作废', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000211', 'his_pivas_status', '待审方', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000212', 'his_pivas_status', '待排队', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000213', 'his_pivas_status', '待调配', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000214', 'his_pivas_status', '待核对', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000215', 'his_pivas_status', '已完成', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000216', 'his_pivas_status', '全拒配', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000221', 'his_pivas_item_status', '已拒配', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000222', 'his_pivas_item_status', '待审方', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000223', 'his_pivas_item_status', '已审方', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000224', 'his_pivas_item_status', '已排队', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000225', 'his_pivas_item_status', '已调配', '4', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000226', 'his_pivas_item_status', '已核对发放', '5', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000231', 'his_pathway_status', '草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000232', 'his_pathway_status', '使用中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000233', 'his_pathway_status', '已停用', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000241', 'his_pathway_enroll_status', '在径', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000242', 'his_pathway_enroll_status', '已完成', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000243', 'his_pathway_enroll_status', '已退径', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000251', 'his_pathway_variance_type', '医嘱变动', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000252', 'his_pathway_variance_type', '检查检验变动', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000253', 'his_pathway_variance_type', '手术操作变动', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000254', 'his_pathway_variance_type', '用药变动', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000255', 'his_pathway_variance_type', '出院延期', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000256', 'his_pathway_variance_type', '其他', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000261', 'his_pathway_item_type', '诊疗', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000262', 'his_pathway_item_type', '用药', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000263', 'his_pathway_item_type', '手术操作', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000264', 'his_pathway_item_type', '护理', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000265', 'his_pathway_item_type', '病情评估', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000266', 'his_pathway_item_type', '宣教', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000301', 'his_dispute_case_type', '服务投诉', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000302', 'his_dispute_case_type', '医疗纠纷', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000303', 'his_dispute_case_type', '医疗损害争议', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000304', 'his_dispute_case_type', '其他', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000311', 'his_dispute_source', '来电', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000312', 'his_dispute_source', '来访', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000313', 'his_dispute_source', '来信', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000314', 'his_dispute_source', '政务热线', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000315', 'his_dispute_source', '上级交办', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000316', 'his_dispute_source', '院内发现', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000317', 'his_dispute_source', '其他', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000321', 'his_dispute_status', '待受理', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000322', 'his_dispute_status', '调查中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000323', 'his_dispute_status', '处理中', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000324', 'his_dispute_status', '已结案', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000325', 'his_dispute_status', '已撤销', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000331', 'his_dispute_level', '一般', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000332', 'his_dispute_level', '较大', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000333', 'his_dispute_level', '重大', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000341', 'his_dispute_deal_type', '院内协商', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000342', 'his_dispute_deal_type', '医调委调解', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000343', 'his_dispute_deal_type', '行政调解', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000344', 'his_dispute_deal_type', '司法鉴定', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000345', 'his_dispute_deal_type', '诉讼', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000346', 'his_dispute_deal_type', '其他', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000351', 'his_dispute_duty', '无责', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000352', 'his_dispute_duty', '轻微责任', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000353', 'his_dispute_duty', '次要责任', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000354', 'his_dispute_duty', '主要责任', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000355', 'his_dispute_duty', '完全责任', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000361', 'his_dispute_relation', '本人', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000362', 'his_dispute_relation', '家属', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000363', 'his_dispute_relation', '代理人', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000364', 'his_dispute_relation', '其他', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000371', 'his_dispute_seal_status', '未申请', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000372', 'his_dispute_seal_status', '已封存', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000373', 'his_dispute_seal_status', '待归档后封存', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000401', 'his_tele_consult_type', '临床会诊', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000402', 'his_tele_consult_type', '远程影像', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000403', 'his_tele_consult_type', '远程心电', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000404', 'his_tele_consult_type', '远程病理', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000405', 'his_tele_consult_type', '其他', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000411', 'his_tele_consult_status', '待安排', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000412', 'his_tele_consult_status', '已安排', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000413', 'his_tele_consult_status', '已完成', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000414', 'his_tele_consult_status', '已取消', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000421', 'his_online_consult_type', '图文问诊', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000422', 'his_online_consult_type', '电话问诊', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000423', 'his_online_consult_type', '视频问诊', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000431', 'his_online_consult_status', '待接诊', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000432', 'his_online_consult_status', '接诊中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000433', 'his_online_consult_status', '已完成', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000434', 'his_online_consult_status', '已退诊', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000501', 'his_day_surgery_status', '待评估', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000502', 'his_day_surgery_status', '评估通过', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000503', 'his_day_surgery_status', '已安排', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000504', 'his_day_surgery_status', '术后观察', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000505', 'his_day_surgery_status', '已出院', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000506', 'his_day_surgery_status', '已取消', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000507', 'his_day_surgery_status', '已转住院', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000511', 'his_day_surgery_anesthesia', '局部麻醉', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000512', 'his_day_surgery_anesthesia', '椎管内麻醉', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000513', 'his_day_surgery_anesthesia', '全身麻醉', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000514', 'his_day_surgery_anesthesia', '神经阻滞', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000515', 'his_day_surgery_anesthesia', '其他', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000521', 'his_day_surgery_leave_type', '按时离院', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000522', 'his_day_surgery_leave_type', '转普通住院', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000523', 'his_day_surgery_leave_type', '非计划再入院', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000531', 'his_day_surgery_follow_result', '无异常', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000532', 'his_day_surgery_follow_result', '有异常已处置', '2', 2, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000533', 'his_day_surgery_follow_result', '有异常再就诊', '3', 3, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000534', 'his_day_surgery_follow_result', '失联', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000541', 'his_day_surgery_follow_type', '电话', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000542', 'his_day_surgery_follow_type', '门诊', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000543', 'his_day_surgery_follow_type', '上门', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000544', 'his_day_surgery_follow_type', '线上', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000551', 'his_day_surgery_eval_result', '通过', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000552', 'his_day_surgery_eval_result', '不通过', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000611', 'his_emp_cert_type', '医师资格证', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000612', 'his_emp_cert_type', '医师执业证', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000613', 'his_emp_cert_type', '护士执业证', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000614', 'his_emp_cert_type', '药师资格证', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000000615', 'his_emp_cert_type', '其他', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001001', 'his_dialysis_status', '在透', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001002', 'his_dialysis_status', '暂停', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001003', 'his_dialysis_status', '退出', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001011', 'his_dialysis_access', '自体内瘘', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001012', 'his_dialysis_access', '人工血管', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001013', 'his_dialysis_access', '中心静脉导管', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001014', 'his_dialysis_access', '动静脉外露', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001021', 'his_dialysis_freq', '每周1次', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001022', 'his_dialysis_freq', '每周2次', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001023', 'his_dialysis_freq', '每周3次', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001024', 'his_dialysis_freq', '每周≥4次', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001031', 'his_dialysis_dialyzer', '低通量纤维素膜', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001032', 'his_dialysis_dialyzer', '低通量合成膜', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001033', 'his_dialysis_dialyzer', '高通量合成膜', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001041', 'his_dialysis_anticoag', '普通肝素', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001042', 'his_dialysis_anticoag', '低分子肝素', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001043', 'his_dialysis_anticoag', '枸橼酸钠', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001044', 'his_dialysis_anticoag', '无肝素', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001051', 'his_dialysis_slot', '上午', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001052', 'his_dialysis_slot', '下午', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001053', 'his_dialysis_slot', '夜间', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001061', 'his_dialysis_session_status', '已排班', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001062', 'his_dialysis_session_status', '透析中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001063', 'his_dialysis_session_status', '已完成', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001064', 'his_dialysis_session_status', '已取消', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001071', 'his_dialysis_adverse', '低血压', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001072', 'his_dialysis_adverse', '肌肉痉挛', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001073', 'his_dialysis_adverse', '恶心呕吐', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001074', 'his_dialysis_adverse', '头痛头晕', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001075', 'his_dialysis_adverse', '胸痛胸闷', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001076', 'his_dialysis_adverse', '寒战发热', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001077', 'his_dialysis_adverse', '凝血', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001078', 'his_dialysis_adverse', '其他', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001081', 'his_dialysis_machine_status', '可用', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001082', 'his_dialysis_machine_status', '维修', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001083', 'his_dialysis_machine_status', '停用', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001091', 'his_icu_stay_status', '在科', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001092', 'his_icu_stay_status', '已出科', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001101', 'his_icu_care_level', '特级监护', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001102', 'his_icu_care_level', 'I级监护', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001103', 'his_icu_care_level', 'II级监护', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001111', 'his_icu_out_dest', '转普通病房', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001112', 'his_icu_out_dest', '转专科病房', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001113', 'his_icu_out_dest', '转手术室', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001114', 'his_icu_out_dest', '转院', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001115', 'his_icu_out_dest', '死亡', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001116', 'his_icu_out_dest', '自动离院', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001121', 'his_icu_vent_mode', '鼻导管/面罩', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001122', 'his_icu_vent_mode', '无创通气', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001123', 'his_icu_vent_mode', '有创通气', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001124', 'his_icu_vent_mode', '脱机', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001125', 'his_emp_cert_org', '国家卫健委', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001126', 'his_education', '博士', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001127', 'his_education', '硕士', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001128', 'his_education', '本科', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001129', 'his_education', '大专', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('892000000000001130', 'his_education', '中专', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('893000000000000311', 'his_stocktake_status', '盘点中', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('893000000000000312', 'his_stocktake_status', '待复核', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('893000000000000313', 'his_stocktake_status', '已过账', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('893000000000000314', 'his_stocktake_status', '已关单', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000000111', 'his_drug_interaction_severity', '禁忌', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000000112', 'his_drug_interaction_severity', '慎用', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000000121', 'his_dose_unit', 'g', 'g', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000000122', 'his_dose_unit', 'mg', 'mg', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000000123', 'his_dose_unit', 'ug', 'ug', 3, NULL, NULL, 0, 0, 'admin', 'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000009101', 'his_tcm_decoct_method', '水煎服', '水煎服', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000009102', 'his_tcm_decoct_method', '先煎', '先煎', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000009103', 'his_tcm_decoct_method', '后下', '后下', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000009104', 'his_tcm_decoct_method', '包煎', '包煎', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000009105', 'his_tcm_decoct_method', '另煎', '另煎', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000009106', 'his_tcm_decoct_method', '冲服', '冲服', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000009107', 'his_tcm_decoct_method', '烊化', '烊化', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000009111', 'his_tcm_decoct_flag', '代煎', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894000000000009112', 'his_tcm_decoct_flag', '自煎', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000091', 'his_dispensing_status', '已取消', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000102', 'his_stock_room', '药库', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000103', 'his_stock_room', '药房', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000112', 'his_drug_transfer_type', '药库下拨药房', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000113', 'his_drug_transfer_type', '药房退回药库', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000122', 'his_drug_transfer_status', '待发出', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000123', 'his_drug_transfer_status', '待接收', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000124', 'his_drug_transfer_status', '已完成', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000125', 'his_drug_transfer_status', '已作废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000132', 'his_supplier_return_status', '待退货', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000133', 'his_supplier_return_status', '已退货', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000134', 'his_supplier_return_status', '已作废', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000141', 'his_drug_stock_log_type', '调拨出库', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000142', 'his_drug_stock_log_type', '调拨入库', '8', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('894700000000000143', 'his_drug_stock_log_type', '退货出库', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000101', 'his_survey_scene', '出院随访', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000102', 'his_survey_scene', '门诊', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000103', 'his_survey_scene', '住院在院', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000104', 'his_survey_scene', '体检', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000111', 'his_survey_dimension', '挂号便捷', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000112', 'his_survey_dimension', '医生服务', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000113', 'his_survey_dimension', '护士服务', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000114', 'his_survey_dimension', '环境与流程', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000115', 'his_survey_dimension', '费用透明', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000116', 'his_survey_dimension', '疗效与安全感', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000117', 'his_survey_dimension', '总体印象', '7', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000121', 'his_survey_question_type', '量表（李克特5级）', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000122', 'his_survey_question_type', '单选', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000123', 'his_survey_question_type', '多选', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000124', 'his_survey_question_type', 'NPS推荐度', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000125', 'his_survey_question_type', '开放文本', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000131', 'his_survey_source', '随访任务', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000132', 'his_survey_source', '出院结算', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000133', 'his_survey_source', '人工补发', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000141', 'his_survey_channel', '电话代填', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000142', 'his_survey_channel', '短信', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000143', 'his_survey_channel', '微信/互联网', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000144', 'his_survey_channel', '现场扫码', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000151', 'his_survey_dispatch_status', '待推送', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000152', 'his_survey_dispatch_status', '已推送待回收', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000153', 'his_survey_dispatch_status', '已回收', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000154', 'his_survey_dispatch_status', '已过期', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000155', 'his_survey_dispatch_status', '已拒答', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000161', 'his_survey_answer_status', '有效', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000162', 'his_survey_answer_status', '已作废', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000171', 'his_survey_fill_source', '患者自填', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000172', 'his_survey_fill_source', '随访员代填', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000173', 'his_survey_fill_source', '现场扫码', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000181', 'his_survey_tpl_status', '启用', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895000000000000182', 'his_survey_tpl_status', '停用', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000101', 'his_tech_auth_category', '手术', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000102', 'his_tech_auth_category', '麻醉', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000103', 'his_tech_auth_category', '内镜与介入', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000111', 'his_tech_auth_type', '独立授权', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000112', 'his_tech_auth_type', '上级指导下', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000113', 'his_tech_auth_type', '限制授权', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000121', 'his_tech_auth_status', '待审批', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000122', 'his_tech_auth_status', '已授权', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000123', 'his_tech_auth_status', '已驳回', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000124', 'his_tech_auth_status', '已收回', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000131', 'his_tech_override_status', '待上级确认', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000132', 'his_tech_override_status', '已确认', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000141', 'his_tech_override_source', '手术申请', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000142', 'his_tech_override_source', '日间手术', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000143', 'his_tech_override_source', '住院医嘱', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895500000000000144', 'his_tech_override_source', '内镜记录', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000101', 'his_death_place', '医院', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000102', 'his_death_place', '来院途中', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000103', 'his_death_place', '家中', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000104', 'his_death_place', '民政管理机构', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000105', 'his_death_place', '其他机构', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000106', 'his_death_place', '未指明', '9', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000111', 'his_death_cert_status', '草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000112', 'his_death_cert_status', '已审核', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000113', 'his_death_cert_status', '已开具', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000114', 'his_death_cert_status', '已作废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000121', 'his_death_report_status', '未上报', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000122', 'his_death_report_status', '已上报', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000123', 'his_death_report_status', '上报失败', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000131', 'his_death_cause_part', 'Ⅰ部分 死因链', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000132', 'his_death_cause_part', 'Ⅱ部分 其他疾病', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000141', 'his_death_type', '疾病死亡', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000142', 'his_death_type', '非疾病死亡（外部原因）', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000143', 'his_death_type', '死因不明/待核实', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000151', 'his_body_disposal', '殡仪馆接运', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000152', 'his_body_disposal', '家属自行处理', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000153', 'his_body_disposal', '病理解剖/医学教学', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000154', 'his_body_disposal', '其他', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000161', 'his_death_cert_copy', '记录联', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000162', 'his_death_cert_copy', '户籍联', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000163', 'his_death_cert_copy', '殡葬联', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000164', 'his_death_cert_copy', '家属联', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000171', 'his_death_register_status', '草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000172', 'his_death_register_status', '已登记', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('895700000000000173', 'his_death_register_status', '已作废', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000201', 'his_shift_scope', '门诊/急诊排班', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000202', 'his_shift_scope', '病区护理排班', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000211', 'his_nurse_schedule_status', '上班', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000212', 'his_nurse_schedule_status', '休息', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000213', 'his_nurse_schedule_status', '请假', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000214', 'his_nurse_schedule_status', '培训', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000215', 'his_nurse_schedule_status', '停班', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000221', 'his_nursing_qc_category', '基础护理', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000222', 'his_nursing_qc_category', '专科护理', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000223', 'his_nursing_qc_category', '安全管理', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000224', 'his_nursing_qc_category', '护理文书', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000225', 'his_nursing_qc_category', '院感防控', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000231', 'his_nursing_indicator', '基础护理合格率', 'BASIC_NURSING', 1, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000232', 'his_nursing_indicator', '护理文书书写合格率', 'NURSING_DOC', 2, NULL, NULL, 0, 1,
        'admin', 'admin', 0, NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000233', 'his_nursing_indicator', '跌倒/坠床发生率', 'FALL_RATE', 3, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000234', 'his_nursing_indicator', '院内压力性损伤发生率', 'UPPR_RATE', 4, NULL, NULL, 0, 1,
        'admin', 'admin', 0, NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000241', 'his_nursing_qc_status', '草稿', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000242', 'his_nursing_qc_status', '已确认', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000251', 'his_nursing_qc_report', '未上报', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000252', 'his_nursing_qc_report', '已上报', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 2,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000261', 'his_adverse_acquired', '院内获得', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896610000000000262', 'his_adverse_acquired', '入院带入', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000101', 'his_shift_scope', '全院通用', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000111', 'his_org_unit_type', '科室', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000112', 'his_org_unit_type', '病区', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000113', 'his_org_unit_type', '全院', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000121', 'his_duty_status', '上班', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000122', 'his_duty_status', '休息', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000123', 'his_duty_status', '请假', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000124', 'his_duty_status', '培训', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000125', 'his_duty_status', '停班', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000131', 'his_attend_mode', '坐班', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000132', 'his_attend_mode', '听班', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000133', 'his_attend_mode', '留院值班', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000141', 'his_staff_schedule_source', '手工', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000142', 'his_staff_schedule_source', '模板', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000143', 'his_staff_schedule_source', '复制周期', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000144', 'his_staff_schedule_source', '换班', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000151', 'his_schedule_change_type', '换班', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000152', 'his_schedule_change_type', '代班', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000153', 'his_schedule_change_type', '停班', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000154', 'his_schedule_change_type', '加号', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000155', 'his_schedule_change_type', '减号', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000156', 'his_schedule_change_type', '出诊变更', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000161', 'his_duty_scope', '全院行政', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000162', 'his_duty_scope', '急诊', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000163', 'his_duty_scope', '感染', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000164', 'his_duty_scope', '总务', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000165', 'his_duty_scope', '信息', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000166', 'his_duty_scope', '临床科室', '6', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000171', 'his_duty_level', '不适用', '0', 0, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000172', 'his_duty_level', '一线', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000173', 'his_duty_level', '二线', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('896720000000000174', 'his_duty_level', '三线', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('2103859935874912258', 'his_order_freq', 'PROBE-UI途径262389', 'PROBE-UI途径262389', 15, NULL, 'primary', 0, 1,
        'admin', 'admin', 1, NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('2103860329522925570', 'his_order_freq', 'PROBE-UI途径356548', 'PROBE-UI途径356548', 15, NULL, 'primary', 0, 1,
        'admin', 'admin', 1, NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('2103860740413722625', 'his_order_freq', 'PROBE-UI途径454456', 'PROBE-UI途径454456', 15, NULL, 'primary', 0, 1,
        'admin', 'admin', 1, NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('2103861133071880194', 'his_order_freq', 'PROBE-UI途径548154', 'PROBE-UI途径548154', 15, NULL, 'primary', 0, 1,
        'admin', 'admin', 1, NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000001961', 'his_assess_type', '压疮评估（Braden）', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000001962', 'his_assess_type', '跌倒评估（Morse）', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000001963', 'his_assess_type', '疼痛评估（NRS）', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000001964', 'his_assess_type', 'VTE血栓评估（Caprini）', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000001965', 'his_assess_type', '管路滑脱评估', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000001971', 'his_assess_risk_level', '低风险', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000001972', 'his_assess_risk_level', '中风险', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000001973', 'his_assess_risk_level', '高风险', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000001974', 'his_assess_risk_level', '极高风险', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        2, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035001', 'his_order_route', '口服', '口服', 1, NULL, 'primary', 1, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035002', 'his_order_route', '静滴', '静滴', 2, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035003', 'his_order_route', '静推', '静推', 3, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035004', 'his_order_route', '肌注', '肌注', 4, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035005', 'his_order_route', '皮下注射', '皮下注射', 5, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035006', 'his_order_route', '皮内注射', '皮内注射', 6, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035007', 'his_order_route', '静脉泵入', '静脉泵入', 7, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035008', 'his_order_route', '外用', '外用', 8, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035009', 'his_order_route', '舌下含服', '舌下含服', 9, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035010', 'his_order_route', '雾化吸入', '雾化吸入', 10, NULL, 'primary', 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035011', 'his_order_route', '直肠给药', '直肠给药', 11, NULL, 'primary', 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035012', 'his_order_route', '滴眼', '滴眼', 12, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035013', 'his_order_route', '滴鼻', '滴鼻', 13, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035014', 'his_order_route', '鼻饲', '鼻饲', 14, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035015', 'his_order_route', '其他', '其他', 15, NULL, 'info', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035021', 'his_order_freq', 'qd 每日一次', 'qd', 1, NULL, 'primary', 1, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035022', 'his_order_freq', 'bid 每日两次', 'bid', 2, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035023', 'his_order_freq', 'tid 每日三次', 'tid', 3, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035024', 'his_order_freq', 'qid 每日四次', 'qid', 4, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035025', 'his_order_freq', 'q8h 每8小时', 'q8h', 5, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035026', 'his_order_freq', 'q12h 每12小时', 'q12h', 6, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035027', 'his_order_freq', 'q6h 每6小时', 'q6h', 7, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035028', 'his_order_freq', 'qod 隔日一次', 'qod', 8, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035029', 'his_order_freq', 'qw 每周一次', 'qw', 9, NULL, 'primary', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035030', 'his_order_freq', 'prn 必要时', 'prn', 10, NULL, 'warning', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035031', 'his_order_freq', 'st 立即一次', 'st', 11, NULL, 'warning', 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035032', 'his_order_freq', 'hs 睡前', 'hs', 12, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035033', 'his_order_freq', 'am 上午', 'am', 13, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035034', 'his_order_freq', 'pm 下午', 'pm', 14, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035043', 'his_dose_unit', 'μg', 'μg', 3, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035044', 'his_dose_unit', 'ml', 'ml', 4, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035045', 'his_dose_unit', 'L', 'L', 5, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035046', 'his_dose_unit', 'IU', 'IU', 6, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035047', 'his_dose_unit', 'U', 'U', 7, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035048', 'his_dose_unit', '片', '片', 8, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035049', 'his_dose_unit', '粒', '粒', 9, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035050', 'his_dose_unit', '支', '支', 10, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035051', 'his_dose_unit', '袋', '袋', 11, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035052', 'his_dose_unit', '瓶', '瓶', 12, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035053', 'his_dose_unit', '滴', '滴', 13, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035054', 'his_dose_unit', '喷', '喷', 14, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035055', 'his_dose_unit', '单位', '单位', 15, NULL, 'primary', 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035056', 'his_order_class', '临床营养', '10', 10, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035057', 'his_report_status', '草稿', '0', 0, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035058', 'his_film_status', '已登记', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035059', 'his_film_status', '已打印', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035060', 'his_film_status', '已发放', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035061', 'his_film_status', '已作废', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035062', 'his_positive_flag', '未判定', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035063', 'his_positive_flag', '阴性', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035064', 'his_positive_flag', '阳性', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000035065', 'his_positive_flag', '未见异常', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360001', 'his_vte_measure_code', '基础预防', 'BASIC', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360002', 'his_vte_measure_code', '物理预防', 'PHYSICAL', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360003', 'his_vte_measure_code', '药物预防', 'DRUG', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360004', 'his_vte_measure_type', '基础预防', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360005', 'his_vte_measure_type', '物理预防', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360006', 'his_vte_measure_type', '药物预防', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360007', 'his_vte_execute_status', '待落实', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360008', 'his_vte_execute_status', '已落实', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360009', 'his_vte_execute_status', '禁忌未用', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360010', 'his_vte_execute_status', '患者拒绝', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360011', 'his_vte_event_type', '深静脉血栓（DVT）', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360012', 'his_vte_event_type', '肺栓塞（PE）', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360013', 'his_vte_event_type', '预防相关出血', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360014', 'his_vte_onset_type', '院内发生', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360015', 'his_vte_onset_type', '入院时已存在', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360016', 'his_vte_basis', '超声', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360017', 'his_vte_basis', 'CT 肺动脉造影', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360018', 'his_vte_basis', '静脉造影', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360019', 'his_vte_basis', '临床诊断', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360020', 'his_vte_basis', '其他', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360021', 'his_vte_outcome', '好转', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360022', 'his_vte_outcome', '未愈', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360023', 'his_vte_outcome', '死亡', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000360024', 'his_vte_outcome', '未知', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361001', 'his_nutrition_screen_type', 'NRS2002 营养风险筛查', '1', 1, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361002', 'his_nutrition_screen_type', 'PG-SGA 主观整体评估', '2', 2, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361003', 'his_nutrition_screen_type', 'MNA 老年微型营养评估', '3', 3, NULL, NULL, 0, 1, 'admin',
        'admin', 0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361004', 'his_nutrition_risk_flag', '无营养风险', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361005', 'his_nutrition_risk_flag', '有营养风险', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361006', 'his_screen_source', '入院48小时内', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361007', 'his_screen_source', '病情变化复筛', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361008', 'his_screen_source', '术后复筛', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361009', 'his_screen_source', '定期复筛', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361010', 'his_diet_category', '基本饮食', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361011', 'his_diet_category', '治疗饮食', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361012', 'his_diet_category', '诊断试验饮食', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361013', 'his_diet_category', '营养支持', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361014', 'his_diet_type', '普食', 'NORMAL', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361015', 'his_diet_type', '软食', 'SOFT', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361016', 'his_diet_type', '半流质', 'HALF_LIQUID', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361017', 'his_diet_type', '流质', 'LIQUID', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361018', 'his_diet_type', '糖尿病饮食', 'DIABETES', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361019', 'his_diet_type', '低盐低脂饮食', 'LOW_SALT', 6, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361020', 'his_diet_type', '高蛋白饮食', 'HIGH_PROTEIN', 7, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361021', 'his_diet_type', '低蛋白饮食', 'LOW_PROTEIN', 8, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361022', 'his_diet_type', '肾病饮食', 'KIDNEY', 9, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361023', 'his_diet_type', '痛风饮食', 'GOUT', 10, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361024', 'his_diet_type', '少渣饮食', 'LOW_FIBER', 11, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361025', 'his_diet_type', '隐血试验饮食', 'OCCULT_BLOOD', 12, NULL, NULL, 0, 1, 'admin', 'admin',
        0, NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361026', 'his_diet_type', '胆囊造影饮食', 'CHOLYCYST', 13, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361027', 'his_diet_type', '肠内营养', 'ENT', 14, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361028', 'his_diet_type', '肠外营养', 'PN', 15, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361029', 'his_diet_type', '口服营养补充', 'ONS', 16, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361030', 'his_nutrition_route', '口服', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361031', 'his_nutrition_route', '管饲', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361032', 'his_nutrition_route', '静脉（肠外）', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361033', 'his_meal_type', '早餐', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361034', 'his_meal_type', '午餐', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361035', 'his_meal_type', '晚餐', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361036', 'his_meal_type', '加餐', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361037', 'his_meal_status', '待配餐', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361038', 'his_meal_status', '已配餐', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361039', 'his_meal_status', '已配送', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361040', 'his_meal_status', '已签收', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361041', 'his_meal_status', '已取消', '4', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361042', 'his_diet_plan_status', '执行中', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361043', 'his_diet_plan_status', '已停止', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361044', 'his_diet_plan_status', '已作废', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361045', 'his_diet_confirm_status', '待接收', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361046', 'his_diet_confirm_status', '已接收', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361047', 'his_diet_confirm_status', '已退回', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361048', 'his_consult_category', '普通科间会诊', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361049', 'his_consult_category', '营养会诊', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361050', 'his_consult_category', '药学会诊', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361051', 'his_consult_category', '其他专科会诊', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000361052', 'his_diet_type', '待指定饮食', 'TO_DETERMINE', 17, NULL, NULL, 0, 1, 'admin', 'admin', 0,
        NULL, 1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000370001', 'his_duty_shift', '白班', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000370002', 'his_duty_shift', '夜班', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000370003', 'his_duty_role', '主班', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000370004', 'his_duty_role', '副班', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1, '1',
        '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371001', 'his_duty_log_type', '值班事件', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371002', 'his_duty_log_type', '遗留事项', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371003', 'his_duty_log_type', '巡查记录', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371011', 'his_duty_log_status', '待处理', '0', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371012', 'his_duty_log_status', '已处理', '1', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371013', 'his_duty_log_status', '已交班', '2', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371014', 'his_duty_log_status', '已签收', '3', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371015', 'his_ecg_type', '常规静息心电图', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371016', 'his_ecg_type', '24小时动态心电图', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL,
        1, '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371017', 'his_ecg_rhythm', '窦性心律', '1', 1, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371018', 'his_ecg_rhythm', '窦性心动过速', '2', 2, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371019', 'his_ecg_rhythm', '窦性心动过缓', '3', 3, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371020', 'his_ecg_rhythm', '心房颤动', '4', 4, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, dict_class, list_class, is_default, status,
                           create_by, update_by, del_flag, remark, dict_source, create_by_id, update_by_id)
VALUES ('8910000000000371021', 'his_ecg_rhythm', '室性早搏', '5', 5, NULL, NULL, 0, 1, 'admin', 'admin', 0, NULL, 1,
        '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
