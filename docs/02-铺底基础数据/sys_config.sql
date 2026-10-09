SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('1', '医院名称', 'hospital.name', '湖南长沙市麓康医院', 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('2', '挂号费标准', 'regist.fee', '15.00', 1, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('3', '处方有效期', 'prescription.expiry', '3', 1, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('4', '药品加成比例', 'drug.markup_rate', '0.15', 1, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('5', '医保结算比例', 'insurance.rate', '0.70', 1, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('6', '最小库存预警值', 'stock.min_alert', '50', 1, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('7', '近效期预警天数', 'stock.expiry_alert_days', '30', 1, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('8', '系统维护模式', 'system.maintenance', '0', 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('9', '每日最大预约数', 'appointment.max_daily', '30', 1, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('10', '数据备份周期', 'system.backup_days', '7', 0, 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('120', '危急值处置时限', 'lab.critical_value_deadline_minutes', '30', 0, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('125', '危急值兜底接收人', 'lab.critical_value_fallback_receiver', 'admin', 0, 0, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('130', '住院证有效期', 'admission_order.valid_days', '7', 0, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('890000000000000501', '等床催总值班时限', 'duty.coord.bed_wait_hours', '24', 1, 0, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('890000000000000502', '转诊待确认催总值班时限', 'duty.coord.referral_pending_hours', '2', 1, 0, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('891450000000000001', '急诊候诊时限-Ⅰ级濒危(分钟)', 'emergency.wait_deadline_level1', '0', 1, 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('891450000000000002', '急诊候诊时限-Ⅱ级危重(分钟)', 'emergency.wait_deadline_level2', '10', 1, 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('891450000000000003', '急诊候诊时限-Ⅲ级急症(分钟)', 'emergency.wait_deadline_level3', '30', 1, 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('891450000000000004', '急诊候诊时限-Ⅳ级非急症(分钟)', 'emergency.wait_deadline_level4', '120', 1, 1, NULL,
        'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('891450000000000005', '急诊候诊超时-兜底接收人', 'emergency.wait_fallback_receiver', 'admin', 2, 1, NULL,
        'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('891530000000000001', '急诊留观预警时限(小时)', 'emergency.observation_warn_hours', '48', 1, 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('891530000000000002', '急诊留观上限时限(小时)', 'emergency.observation_max_hours', '72', 1, 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('891620000000000001', '住院请假时长上限', 'inpatient.leave.max_hours', '72', 0, 0, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('2103057512377991169', '医院地址', 'hospital.address', '湖南省长沙市岳麓区金州大道10086号麓康门诊', 0, 1, NULL,
        'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('2103057512377991170', '联系电话', 'hospital.phone', '0731888888888888', 0, 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('2103057512411545601', '医院邮箱', 'hospital.email', '18888888888@cslk.com', 0, 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('9000000000000000001', '签名证书默认有效期（天）', 'sign.cert.valid_days', '365', 1, 0, NULL, 'admin', 'admin',
        '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('9000000000000000002', '缺证书时自动签发', 'sign.cert.auto_issue', '1', 1, 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, is_system, remark, create_by,
                        update_by, create_by_id, update_by_id)
VALUES ('9000000000000000003', '签名时间来源', 'sign.time_source', '1', 1, 0, NULL, 'admin', 'admin', '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
