SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('1', '近效期预警', 'expiry', 'expiry_date <= NOW() + INTERVAL ? DAY', 30, 'system', 1, NULL, 'admin', 'admin',
        '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('2', '低库存预警', 'low_stock', 'quantity < ?', 50, 'system', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('3', '高库存预警', 'high_stock', 'quantity > ?', 5000, 'system', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('4', '近效期短信预警', 'expiry', 'expiry_date <= NOW() + INTERVAL ? DAY', 7, 'sms', 1, NULL, 'admin', 'admin',
        '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('5', '低库存邮件预警', 'low_stock', 'quantity < ?', 20, 'email', 0, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('6', '检验危急值-血钾', 'critical_value', 'result_value OUT OF (2.5,6.5) mmol/L', NULL, 'system+sms', 1, NULL,
        'admin', 'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('7', '检验危急值-血糖', 'critical_value', 'result_value OUT OF (2.8,22.2) mmol/L', NULL, 'system+sms', 1, NULL,
        'admin', 'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('8', '检验危急值-血小板', 'critical_value', 'result_value < 30 ×10^9/L', 30, 'system+sms', 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('9', '检验危急值-血红蛋白', 'critical_value', 'result_value < 50 g/L', 50, 'system+sms', 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('10', '住院欠费预警', 'arrears', 'prepay_balance - daily_cost < ?', 1000, 'system', 1, NULL, 'admin', 'admin',
        '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('11', '门诊候诊超时', 'queue_wait', 'waiting_time > ? MINUTE', 30, 'system', 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('12', '药品近效期', 'expiry', 'expiry_date <= NOW() + INTERVAL ? DAY', 90, 'system', 1, NULL, 'admin', 'admin',
        '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('13', '药品呆滞库存', 'slow_moving', 'last_dispense_date < NOW() - INTERVAL ? DAY', 180, 'system', 1, NULL,
        'admin', 'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('14', '耗材库存下限', 'low_stock', 'quantity <= safety_stock + ?', 0, 'system', 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('15', '号源紧张', 'slot_shortage', 'remain_slots / total_slots < 0.1', 10, 'system', 1, NULL, 'admin', 'admin',
        '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('16', '退号异常', 'refund_anomaly', 'refund_count_today > ? per_account', 5, 'system+audit', 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('17', '设备状态离线', 'device_offline', 'heartbeat_gap > ? MINUTE', 30, 'system', 1, NULL, 'admin', 'admin',
        '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('18', '危急值处理超时', 'critical_timeout', 'ack_elapsed > ? MINUTE', 15, 'system+sms', 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('19', '抗菌药使用强度', 'antibiotic_intensity', 'ddds_per_100_bed_days > ?', 40, 'system', 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('20', 'I类切口预防用药超时长', 'incision_prophylaxis', 'prophylaxis_duration > ? HOUR', 24, 'system', 1, NULL,
        'admin', 'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('21', '住院超30天', 'long_stay', 'length_of_stay > ? DAY', 30, 'system', 1, NULL, 'admin', 'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('22', '再入院预警', 'readmission', 'readmit_within_days <= ?', 31, 'system', 1, NULL, 'admin', 'admin', '1',
        '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('23', '医保拒付预警', 'insurance_reject', 'reject_amount_month > ? YUAN', 50000, 'system', 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('24', '手术间超时占用', 'or_overtime', 'scheduled_duration_actual_ratio > 1.3', 130, 'system', 1, NULL, 'admin',
        'admin', '1', '1');
INSERT INTO sys_alert_rule (rule_id, rule_name, rule_type, rule_condition, threshold, notify_channel, is_active, remark,
                            create_by, update_by, create_by_id, update_by_id)
VALUES ('25', '输血反应上报', 'transfusion_reaction', 'reaction_reported = 1', NULL, 'system+sms', 1, NULL, 'admin',
        'admin', '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
