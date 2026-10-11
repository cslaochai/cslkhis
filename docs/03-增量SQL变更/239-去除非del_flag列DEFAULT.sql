-- ============================================================
-- 增量变更：去除所有 NOT NULL/NULLABLE 列上的 DEFAULT（del_flag 除外）
-- 依据 AGENTS.md §25 硬规矩：业务层强控，DB 不兜底
-- 目标库：hn_biz_his  @ 192.168.88.132:3306
-- 影响列数：1649（del_flag 已豁免）
-- 说明：DROP DEFAULT 仅改元数据，不触数据；可逆（如需回滚按列 ALTER ... SET DEFAULT 原值）
-- ============================================================

USE `hn_biz_his`;

-- biz_admission.nursing_level_source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_admission` ALTER COLUMN `nursing_level_source` DROP DEFAULT;

-- biz_admission.admit_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_admission` ALTER COLUMN `admit_time` DROP DEFAULT;

-- biz_admission.admit_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_admission` ALTER COLUMN `admit_status` DROP DEFAULT;

-- biz_admission.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_admission` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_admission.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_admission` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_admission_order.order_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_admission_order` ALTER COLUMN `order_status` DROP DEFAULT;

-- biz_admission_order.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_admission_order` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_admission_order.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_admission_order` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_adverse_event.acquired_flag  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_adverse_event` ALTER COLUMN `acquired_flag` DROP DEFAULT;

-- biz_adverse_event.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_adverse_event` ALTER COLUMN `status` DROP DEFAULT;

-- biz_adverse_event.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_adverse_event` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_adverse_event.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_adverse_event` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ai_draft_diff.patient_no  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `biz_ai_draft_diff` ALTER COLUMN `patient_no` DROP DEFAULT;

-- biz_ai_draft_diff.patient_name  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `biz_ai_draft_diff` ALTER COLUMN `patient_name` DROP DEFAULT;

-- biz_ai_draft_diff.dept_name  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `biz_ai_draft_diff` ALTER COLUMN `dept_name` DROP DEFAULT;

-- biz_ai_draft_diff.doctor_name  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `biz_ai_draft_diff` ALTER COLUMN `doctor_name` DROP DEFAULT;

-- biz_ai_draft_diff.changed  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_ai_draft_diff` ALTER COLUMN `changed` DROP DEFAULT;

-- biz_ai_draft_diff.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ai_draft_diff` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_ai_draft_diff.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ai_draft_diff` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ai_draft_diff.remark  (varchar(500), YES)  原 DEFAULT = ''
ALTER TABLE `biz_ai_draft_diff` ALTER COLUMN `remark` DROP DEFAULT;

-- biz_alert.alert_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_alert` ALTER COLUMN `alert_status` DROP DEFAULT;

-- biz_alert.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_alert` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_alert.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_alert` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_anesthesia_followup.round_no  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_anesthesia_followup` ALTER COLUMN `round_no` DROP DEFAULT;

-- biz_anesthesia_followup.followup_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_followup` ALTER COLUMN `followup_status` DROP DEFAULT;

-- biz_anesthesia_followup.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_followup` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_anesthesia_followup.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_followup` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_anesthesia_med.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_med` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_anesthesia_med.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_med` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_anesthesia_pacu.complication_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_pacu` ALTER COLUMN `complication_flag` DROP DEFAULT;

-- biz_anesthesia_pacu.status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_pacu` ALTER COLUMN `status` DROP DEFAULT;

-- biz_anesthesia_pacu.charge_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_pacu` ALTER COLUMN `charge_status` DROP DEFAULT;

-- biz_anesthesia_pacu.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_pacu` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_anesthesia_pacu.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_pacu` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_anesthesia_record.adverse_event_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_record` ALTER COLUMN `adverse_event_flag` DROP DEFAULT;

-- biz_anesthesia_record.record_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_record` ALTER COLUMN `record_status` DROP DEFAULT;

-- biz_anesthesia_record.charge_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_record` ALTER COLUMN `charge_status` DROP DEFAULT;

-- biz_anesthesia_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_anesthesia_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_anesthesia_visit.is_emergency  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_visit` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- biz_anesthesia_visit.asa_emergency  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_visit` ALTER COLUMN `asa_emergency` DROP DEFAULT;

-- biz_anesthesia_visit.difficult_airway  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_visit` ALTER COLUMN `difficult_airway` DROP DEFAULT;

-- biz_anesthesia_visit.visit_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_anesthesia_visit` ALTER COLUMN `visit_status` DROP DEFAULT;

-- biz_anesthesia_visit.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_visit` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_anesthesia_visit.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_visit` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_anesthesia_vital.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_vital` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_anesthesia_vital.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_anesthesia_vital` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_antibiotic_alias.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_antibiotic_alias` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_antibiotic_alias.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_antibiotic_alias` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_antibiotic_auth.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_antibiotic_auth` ALTER COLUMN `status` DROP DEFAULT;

-- biz_antibiotic_auth.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_antibiotic_auth` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_antibiotic_auth.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_antibiotic_auth` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_antibiotic_incision_review.incision_level  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_antibiotic_incision_review` ALTER COLUMN `incision_level` DROP DEFAULT;

-- biz_antibiotic_incision_review.indication_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_incision_review` ALTER COLUMN `indication_flag` DROP DEFAULT;

-- biz_antibiotic_incision_review.combo_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_incision_review` ALTER COLUMN `combo_flag` DROP DEFAULT;

-- biz_antibiotic_incision_review.consult_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_incision_review` ALTER COLUMN `consult_flag` DROP DEFAULT;

-- biz_antibiotic_incision_review.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_antibiotic_incision_review` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_antibiotic_incision_review.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_antibiotic_incision_review` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_antibiotic_stats.scope_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `scope_type` DROP DEFAULT;

-- biz_antibiotic_stats.op_rx_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `op_rx_count` DROP DEFAULT;

-- biz_antibiotic_stats.op_abx_rx_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `op_abx_rx_count` DROP DEFAULT;

-- biz_antibiotic_stats.op_usage_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `op_usage_rate` DROP DEFAULT;

-- biz_antibiotic_stats.ip_discharge_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `ip_discharge_count` DROP DEFAULT;

-- biz_antibiotic_stats.ip_abx_patient_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `ip_abx_patient_count` DROP DEFAULT;

-- biz_antibiotic_stats.ip_usage_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `ip_usage_rate` DROP DEFAULT;

-- biz_antibiotic_stats.patient_days  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `patient_days` DROP DEFAULT;

-- biz_antibiotic_stats.ddds  (decimal(14,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `ddds` DROP DEFAULT;

-- biz_antibiotic_stats.aud  (decimal(8,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `aud` DROP DEFAULT;

-- biz_antibiotic_stats.abx_treat_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `abx_treat_count` DROP DEFAULT;

-- biz_antibiotic_stats.micro_submit_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `micro_submit_count` DROP DEFAULT;

-- biz_antibiotic_stats.micro_submit_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `micro_submit_rate` DROP DEFAULT;

-- biz_antibiotic_stats.unmatched_order_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `unmatched_order_count` DROP DEFAULT;

-- biz_antibiotic_stats.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_antibiotic_stats.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_antibiotic_stats` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_appoint_info.regist_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_appoint_info` ALTER COLUMN `regist_type` DROP DEFAULT;

-- biz_appoint_info.regist_source  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_appoint_info` ALTER COLUMN `regist_source` DROP DEFAULT;

-- biz_appoint_info.regist_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_appoint_info` ALTER COLUMN `regist_time` DROP DEFAULT;

-- biz_appoint_info.settlement_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_appoint_info` ALTER COLUMN `settlement_type` DROP DEFAULT;

-- biz_appoint_info.regist_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_appoint_info` ALTER COLUMN `regist_status` DROP DEFAULT;

-- biz_appoint_info.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_appoint_info` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_appoint_info.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_appoint_info` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_archive_borrow.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_archive_borrow` ALTER COLUMN `status` DROP DEFAULT;

-- biz_archive_borrow.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_archive_borrow` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_archive_borrow.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_archive_borrow` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_archive_code_task.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_archive_code_task` ALTER COLUMN `status` DROP DEFAULT;

-- biz_archive_code_task.return_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_archive_code_task` ALTER COLUMN `return_count` DROP DEFAULT;

-- biz_archive_code_task.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_archive_code_task` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_archive_code_task.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_archive_code_task` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_arrears_policy.stop_enabled  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_arrears_policy` ALTER COLUMN `stop_enabled` DROP DEFAULT;

-- biz_arrears_policy.stop_classes  (varchar(64), NO)  原 DEFAULT = '2,3,4'
ALTER TABLE `biz_arrears_policy` ALTER COLUMN `stop_classes` DROP DEFAULT;

-- biz_arrears_policy.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_arrears_policy` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_arrears_policy.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_arrears_policy` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_attending_relation.patient_name  (varchar(50), NO)  原 DEFAULT = ''
ALTER TABLE `biz_attending_relation` ALTER COLUMN `patient_name` DROP DEFAULT;

-- biz_attending_relation.employee_name  (varchar(50), NO)  原 DEFAULT = ''
ALTER TABLE `biz_attending_relation` ALTER COLUMN `employee_name` DROP DEFAULT;

-- biz_attending_relation.dept_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_attending_relation` ALTER COLUMN `dept_id` DROP DEFAULT;

-- biz_attending_relation.dept_name  (varchar(100), NO)  原 DEFAULT = ''
ALTER TABLE `biz_attending_relation` ALTER COLUMN `dept_name` DROP DEFAULT;

-- biz_attending_relation.ward_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_attending_relation` ALTER COLUMN `ward_id` DROP DEFAULT;

-- biz_attending_relation.bed_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_attending_relation` ALTER COLUMN `bed_id` DROP DEFAULT;

-- biz_attending_relation.relation_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_attending_relation` ALTER COLUMN `relation_type` DROP DEFAULT;

-- biz_attending_relation.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_attending_relation` ALTER COLUMN `status` DROP DEFAULT;

-- biz_attending_relation.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_attending_relation` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_attending_relation.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_attending_relation` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_bed_allocate.alloc_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_bed_allocate` ALTER COLUMN `alloc_type` DROP DEFAULT;

-- biz_bed_allocate.alloc_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_bed_allocate` ALTER COLUMN `alloc_status` DROP DEFAULT;

-- biz_bed_allocate.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_bed_allocate` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_bed_allocate.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_bed_allocate` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_bed_wait.bed_type  (varchar(32), NO)  原 DEFAULT = 'normal'
ALTER TABLE `biz_bed_wait` ALTER COLUMN `bed_type` DROP DEFAULT;

-- biz_bed_wait.priority  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_bed_wait` ALTER COLUMN `priority` DROP DEFAULT;

-- biz_bed_wait.gender_limit  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_bed_wait` ALTER COLUMN `gender_limit` DROP DEFAULT;

-- biz_bed_wait.isolation_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_bed_wait` ALTER COLUMN `isolation_flag` DROP DEFAULT;

-- biz_bed_wait.wait_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_bed_wait` ALTER COLUMN `wait_status` DROP DEFAULT;

-- biz_bed_wait.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_bed_wait` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_bed_wait.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_bed_wait` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_blood_crossmatch.volume  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_blood_crossmatch` ALTER COLUMN `volume` DROP DEFAULT;

-- biz_blood_crossmatch.method  (tinyint, YES)  原 DEFAULT = '2'
ALTER TABLE `biz_blood_crossmatch` ALTER COLUMN `method` DROP DEFAULT;

-- biz_blood_crossmatch.result  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_blood_crossmatch` ALTER COLUMN `result` DROP DEFAULT;

-- biz_blood_crossmatch.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_blood_crossmatch` ALTER COLUMN `status` DROP DEFAULT;

-- biz_blood_crossmatch.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_blood_crossmatch` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_blood_crossmatch.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_blood_crossmatch` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_blood_inventory.rh_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_blood_inventory` ALTER COLUMN `rh_type` DROP DEFAULT;

-- biz_blood_inventory.volume  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_blood_inventory` ALTER COLUMN `volume` DROP DEFAULT;

-- biz_blood_inventory.unit_amount  (decimal(6,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_blood_inventory` ALTER COLUMN `unit_amount` DROP DEFAULT;

-- biz_blood_inventory.source_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_blood_inventory` ALTER COLUMN `source_type` DROP DEFAULT;

-- biz_blood_inventory.abo_verify  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_blood_inventory` ALTER COLUMN `abo_verify` DROP DEFAULT;

-- biz_blood_inventory.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_blood_inventory` ALTER COLUMN `status` DROP DEFAULT;

-- biz_blood_inventory.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_blood_inventory` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_blood_inventory.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_blood_inventory` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_blood_stock_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_blood_stock_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_blood_stock_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_blood_stock_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_cashier_settlement.shift_type  (tinyint, NO)  原 DEFAULT = '3'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `shift_type` DROP DEFAULT;

-- biz_cashier_settlement.charge_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `charge_count` DROP DEFAULT;

-- biz_cashier_settlement.charge_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `charge_amount` DROP DEFAULT;

-- biz_cashier_settlement.refund_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `refund_count` DROP DEFAULT;

-- biz_cashier_settlement.refund_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `refund_amount` DROP DEFAULT;

-- biz_cashier_settlement.net_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `net_amount` DROP DEFAULT;

-- biz_cashier_settlement.cash_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `cash_amount` DROP DEFAULT;

-- biz_cashier_settlement.wechat_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `wechat_amount` DROP DEFAULT;

-- biz_cashier_settlement.alipay_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `alipay_amount` DROP DEFAULT;

-- biz_cashier_settlement.insurance_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `insurance_amount` DROP DEFAULT;

-- biz_cashier_settlement.balance_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `balance_amount` DROP DEFAULT;

-- biz_cashier_settlement.unknown_pay_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `unknown_pay_amount` DROP DEFAULT;

-- biz_cashier_settlement.pool_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `pool_amount` DROP DEFAULT;

-- biz_cashier_settlement.invoice_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `invoice_count` DROP DEFAULT;

-- biz_cashier_settlement.invoice_void_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `invoice_void_count` DROP DEFAULT;

-- biz_cashier_settlement.settle_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `settle_status` DROP DEFAULT;

-- biz_cashier_settlement.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_cashier_settlement.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cashier_settlement` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_checkup_record.person_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_checkup_record` ALTER COLUMN `person_type` DROP DEFAULT;

-- biz_checkup_record.total_amount  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_checkup_record` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_checkup_record.record_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_checkup_record` ALTER COLUMN `record_status` DROP DEFAULT;

-- biz_checkup_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_checkup_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_checkup_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_checkup_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_checkup_result.item_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_checkup_result` ALTER COLUMN `item_type` DROP DEFAULT;

-- biz_checkup_result.abnormal_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_checkup_result` ALTER COLUMN `abnormal_flag` DROP DEFAULT;

-- biz_checkup_result.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_checkup_result` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_checkup_result.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_checkup_result` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_chronic_record.confirm_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_chronic_record` ALTER COLUMN `confirm_status` DROP DEFAULT;

-- biz_chronic_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_chronic_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_chronic_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_chronic_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_clinic_source.staff_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `staff_type` DROP DEFAULT;

-- biz_clinic_source.total_source  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `total_source` DROP DEFAULT;

-- biz_clinic_source.used_source  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `used_source` DROP DEFAULT;

-- biz_clinic_source.available_source  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `available_source` DROP DEFAULT;

-- biz_clinic_source.used_appointment_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `used_appointment_source` DROP DEFAULT;

-- biz_clinic_source.regist_fee  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `regist_fee` DROP DEFAULT;

-- biz_clinic_source.diagnosis_fee  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `diagnosis_fee` DROP DEFAULT;

-- biz_clinic_source.is_expert  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `is_expert` DROP DEFAULT;

-- biz_clinic_source.expert_fee  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `expert_fee` DROP DEFAULT;

-- biz_clinic_source.is_appointment  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `is_appointment` DROP DEFAULT;

-- biz_clinic_source.appointment_source  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `appointment_source` DROP DEFAULT;

-- biz_clinic_source.added_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `added_source` DROP DEFAULT;

-- biz_clinic_source.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `status` DROP DEFAULT;

-- biz_clinic_source.consult_status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `consult_status` DROP DEFAULT;

-- biz_clinic_source.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_clinic_source.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_clinic_source_change_log.occur_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source_change_log` ALTER COLUMN `occur_time` DROP DEFAULT;

-- biz_clinic_source_change_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source_change_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_clinic_source_change_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source_change_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_clinic_source_slot.total_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_slot` ALTER COLUMN `total_source` DROP DEFAULT;

-- biz_clinic_source_slot.used_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_slot` ALTER COLUMN `used_source` DROP DEFAULT;

-- biz_clinic_source_slot.available_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_slot` ALTER COLUMN `available_source` DROP DEFAULT;

-- biz_clinic_source_slot.added_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_slot` ALTER COLUMN `added_source` DROP DEFAULT;

-- biz_clinic_source_slot.appointment_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_slot` ALTER COLUMN `appointment_source` DROP DEFAULT;

-- biz_clinic_source_slot.used_appointment_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_slot` ALTER COLUMN `used_appointment_source` DROP DEFAULT;

-- biz_clinic_source_slot.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_clinic_source_slot` ALTER COLUMN `status` DROP DEFAULT;

-- biz_clinic_source_slot.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source_slot` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_clinic_source_slot.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source_slot` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_clinic_source_slot_template.total_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_slot_template` ALTER COLUMN `total_source` DROP DEFAULT;

-- biz_clinic_source_slot_template.appointment_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_slot_template` ALTER COLUMN `appointment_source` DROP DEFAULT;

-- biz_clinic_source_slot_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source_slot_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_clinic_source_slot_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source_slot_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_clinic_source_template.dept_name  (varchar(64), YES)  原 DEFAULT = ''
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `dept_name` DROP DEFAULT;

-- biz_clinic_source_template.doctor_name  (varchar(64), YES)  原 DEFAULT = ''
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `doctor_name` DROP DEFAULT;

-- biz_clinic_source_template.staff_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `staff_type` DROP DEFAULT;

-- biz_clinic_source_template.week_parity  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `week_parity` DROP DEFAULT;

-- biz_clinic_source_template.start_time  (char(5), YES)  原 DEFAULT = '08:00'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `start_time` DROP DEFAULT;

-- biz_clinic_source_template.end_time  (char(5), YES)  原 DEFAULT = '12:00'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `end_time` DROP DEFAULT;

-- biz_clinic_source_template.total_source  (int, NO)  原 DEFAULT = '20'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `total_source` DROP DEFAULT;

-- biz_clinic_source_template.regist_fee  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `regist_fee` DROP DEFAULT;

-- biz_clinic_source_template.diagnosis_fee  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `diagnosis_fee` DROP DEFAULT;

-- biz_clinic_source_template.is_expert  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `is_expert` DROP DEFAULT;

-- biz_clinic_source_template.expert_fee  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `expert_fee` DROP DEFAULT;

-- biz_clinic_source_template.is_appointment  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `is_appointment` DROP DEFAULT;

-- biz_clinic_source_template.appointment_source  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `appointment_source` DROP DEFAULT;

-- biz_clinic_source_template.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `status` DROP DEFAULT;

-- biz_clinic_source_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_clinic_source_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinic_source_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_clinical_rule_check.check_result  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_clinical_rule_check` ALTER COLUMN `check_result` DROP DEFAULT;

-- biz_clinical_rule_check.check_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_clinical_rule_check` ALTER COLUMN `check_status` DROP DEFAULT;

-- biz_clinical_rule_check.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinical_rule_check` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_clinical_rule_check.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_clinical_rule_check` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_compliance_audit.audit_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_compliance_audit` ALTER COLUMN `audit_type` DROP DEFAULT;

-- biz_compliance_audit.risk_level  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_compliance_audit` ALTER COLUMN `risk_level` DROP DEFAULT;

-- biz_compliance_audit.risk_score  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_compliance_audit` ALTER COLUMN `risk_score` DROP DEFAULT;

-- biz_compliance_audit.hit_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_compliance_audit` ALTER COLUMN `hit_count` DROP DEFAULT;

-- biz_compliance_audit.pass_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_compliance_audit` ALTER COLUMN `pass_count` DROP DEFAULT;

-- biz_compliance_audit.na_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_compliance_audit` ALTER COLUMN `na_count` DROP DEFAULT;

-- biz_compliance_audit.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_compliance_audit` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_compliance_audit.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_compliance_audit` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_compliance_audit_item.risk_level  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_compliance_audit_item` ALTER COLUMN `risk_level` DROP DEFAULT;

-- biz_compliance_audit_item.target_type  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_compliance_audit_item` ALTER COLUMN `target_type` DROP DEFAULT;

-- biz_compliance_audit_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_compliance_audit_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_compliance_audit_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_compliance_audit_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_consultation.consult_type  (tinyint, NO)  原 DEFAULT = '2'
ALTER TABLE `biz_consultation` ALTER COLUMN `consult_type` DROP DEFAULT;

-- biz_consultation.consult_category  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_consultation` ALTER COLUMN `consult_category` DROP DEFAULT;

-- biz_consultation.is_urgent  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_consultation` ALTER COLUMN `is_urgent` DROP DEFAULT;

-- biz_consultation.apply_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consultation` ALTER COLUMN `apply_time` DROP DEFAULT;

-- biz_consultation.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consultation` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_consultation.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consultation` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_consumable_consume.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consumable_consume` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_consumable_consume.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consumable_consume` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_consumable_stock.quantity  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_consumable_stock` ALTER COLUMN `quantity` DROP DEFAULT;

-- biz_consumable_stock.cost_price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_consumable_stock` ALTER COLUMN `cost_price` DROP DEFAULT;

-- biz_consumable_stock.total_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_consumable_stock` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_consumable_stock.stock_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_consumable_stock` ALTER COLUMN `stock_status` DROP DEFAULT;

-- biz_consumable_stock.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consumable_stock` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_consumable_stock.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consumable_stock` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_consumable_stock_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consumable_stock_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_consumable_stock_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consumable_stock_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_consumable_trace.retail_price  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_consumable_trace` ALTER COLUMN `retail_price` DROP DEFAULT;

-- biz_consumable_trace.visit_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_consumable_trace` ALTER COLUMN `visit_type` DROP DEFAULT;

-- biz_consumable_trace.charge_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_consumable_trace` ALTER COLUMN `charge_status` DROP DEFAULT;

-- biz_consumable_trace.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_consumable_trace` ALTER COLUMN `status` DROP DEFAULT;

-- biz_consumable_trace.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consumable_trace` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_consumable_trace.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_consumable_trace` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_critical_notice.consciousness_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_critical_notice` ALTER COLUMN `consciousness_status` DROP DEFAULT;

-- biz_critical_notice.notice_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_critical_notice` ALTER COLUMN `notice_status` DROP DEFAULT;

-- biz_critical_notice.print_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_critical_notice` ALTER COLUMN `print_count` DROP DEFAULT;

-- biz_critical_notice.sign_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_critical_notice` ALTER COLUMN `sign_status` DROP DEFAULT;

-- biz_critical_notice.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_critical_notice` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_critical_notice.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_critical_notice` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_critical_value.notify_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_critical_value` ALTER COLUMN `notify_status` DROP DEFAULT;

-- biz_critical_value.escalate_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_critical_value` ALTER COLUMN `escalate_status` DROP DEFAULT;

-- biz_critical_value.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_critical_value` ALTER COLUMN `status` DROP DEFAULT;

-- biz_critical_value.source  (varchar(16), NO)  原 DEFAULT = 'RULE'
ALTER TABLE `biz_critical_value` ALTER COLUMN `source` DROP DEFAULT;

-- biz_critical_value.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_critical_value` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_critical_value.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_critical_value` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_cssd_pack.sterilize_method  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_cssd_pack` ALTER COLUMN `sterilize_method` DROP DEFAULT;

-- biz_cssd_pack.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_cssd_pack` ALTER COLUMN `status` DROP DEFAULT;

-- biz_cssd_pack.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cssd_pack` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_cssd_pack.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cssd_pack` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_cssd_pack_template.sterilize_method  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_cssd_pack_template` ALTER COLUMN `sterilize_method` DROP DEFAULT;

-- biz_cssd_pack_template.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_cssd_pack_template` ALTER COLUMN `status` DROP DEFAULT;

-- biz_cssd_pack_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cssd_pack_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_cssd_pack_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cssd_pack_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_cssd_pack_template_item.unit  (varchar(16), NO)  原 DEFAULT = '件'
ALTER TABLE `biz_cssd_pack_template_item` ALTER COLUMN `unit` DROP DEFAULT;

-- biz_cssd_pack_template_item.sort_no  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_cssd_pack_template_item` ALTER COLUMN `sort_no` DROP DEFAULT;

-- biz_cssd_pack_template_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cssd_pack_template_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_cssd_pack_template_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cssd_pack_template_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_cssd_trace.result  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_cssd_trace` ALTER COLUMN `result` DROP DEFAULT;

-- biz_cssd_trace.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cssd_trace` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_cssd_trace.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_cssd_trace` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_day_settlement.shift_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `shift_count` DROP DEFAULT;

-- biz_day_settlement.charge_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `charge_count` DROP DEFAULT;

-- biz_day_settlement.bill_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `bill_count` DROP DEFAULT;

-- biz_day_settlement.charge_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `charge_amount` DROP DEFAULT;

-- biz_day_settlement.refund_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `refund_count` DROP DEFAULT;

-- biz_day_settlement.refund_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `refund_amount` DROP DEFAULT;

-- biz_day_settlement.net_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `net_amount` DROP DEFAULT;

-- biz_day_settlement.cash_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `cash_amount` DROP DEFAULT;

-- biz_day_settlement.wechat_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `wechat_amount` DROP DEFAULT;

-- biz_day_settlement.alipay_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `alipay_amount` DROP DEFAULT;

-- biz_day_settlement.insurance_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `insurance_amount` DROP DEFAULT;

-- biz_day_settlement.balance_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `balance_amount` DROP DEFAULT;

-- biz_day_settlement.unknown_pay_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `unknown_pay_amount` DROP DEFAULT;

-- biz_day_settlement.pool_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `pool_amount` DROP DEFAULT;

-- biz_day_settlement.invoice_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `invoice_count` DROP DEFAULT;

-- biz_day_settlement.invoice_void_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `invoice_void_count` DROP DEFAULT;

-- biz_day_settlement.detail_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `detail_amount` DROP DEFAULT;

-- biz_day_settlement.dept_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `dept_count` DROP DEFAULT;

-- biz_day_settlement.dept_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `dept_amount` DROP DEFAULT;

-- biz_day_settlement.unattributed_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `unattributed_amount` DROP DEFAULT;

-- biz_day_settlement.unassigned_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `unassigned_count` DROP DEFAULT;

-- biz_day_settlement.unassigned_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `unassigned_amount` DROP DEFAULT;

-- biz_day_settlement.reconcile_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `reconcile_status` DROP DEFAULT;

-- biz_day_settlement.diff_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `diff_amount` DROP DEFAULT;

-- biz_day_settlement.settle_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `settle_status` DROP DEFAULT;

-- biz_day_settlement.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_day_settlement.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_day_settlement` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_day_surgery_apply.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_day_surgery_apply` ALTER COLUMN `status` DROP DEFAULT;

-- biz_day_surgery_apply.follow_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_day_surgery_apply` ALTER COLUMN `follow_count` DROP DEFAULT;

-- biz_day_surgery_apply.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_day_surgery_apply` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_day_surgery_apply.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_day_surgery_apply` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_day_surgery_follow.follow_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_day_surgery_follow` ALTER COLUMN `follow_type` DROP DEFAULT;

-- biz_day_surgery_follow.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_day_surgery_follow` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_day_surgery_follow.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_day_surgery_follow` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_day_surgery_item.max_stay_hours  (int, NO)  原 DEFAULT = '48'
ALTER TABLE `biz_day_surgery_item` ALTER COLUMN `max_stay_hours` DROP DEFAULT;

-- biz_day_surgery_item.operation_level  (tinyint, NO)  原 DEFAULT = '2'
ALTER TABLE `biz_day_surgery_item` ALTER COLUMN `operation_level` DROP DEFAULT;

-- biz_day_surgery_item.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_day_surgery_item` ALTER COLUMN `status` DROP DEFAULT;

-- biz_day_surgery_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_day_surgery_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_day_surgery_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_day_surgery_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_death_certificate.autopsy_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_death_certificate` ALTER COLUMN `autopsy_flag` DROP DEFAULT;

-- biz_death_certificate.cert_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_death_certificate` ALTER COLUMN `cert_status` DROP DEFAULT;

-- biz_death_certificate.print_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_death_certificate` ALTER COLUMN `print_count` DROP DEFAULT;

-- biz_death_certificate.report_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_death_certificate` ALTER COLUMN `report_status` DROP DEFAULT;

-- biz_death_certificate.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_death_certificate` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_death_certificate.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_death_certificate` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_death_certificate_cause.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_death_certificate_cause` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_death_certificate_cause.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_death_certificate_cause` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_death_registration.police_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_death_registration` ALTER COLUMN `police_flag` DROP DEFAULT;

-- biz_death_registration.forensic_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_death_registration` ALTER COLUMN `forensic_flag` DROP DEFAULT;

-- biz_death_registration.dispute_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_death_registration` ALTER COLUMN `dispute_flag` DROP DEFAULT;

-- biz_death_registration.register_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_death_registration` ALTER COLUMN `register_status` DROP DEFAULT;

-- biz_death_registration.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_death_registration` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_death_registration.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_death_registration` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_dept_cost_month.labor_cost  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_dept_cost_month` ALTER COLUMN `labor_cost` DROP DEFAULT;

-- biz_dept_cost_month.drug_cost  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_dept_cost_month` ALTER COLUMN `drug_cost` DROP DEFAULT;

-- biz_dept_cost_month.material_cost  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_dept_cost_month` ALTER COLUMN `material_cost` DROP DEFAULT;

-- biz_dept_cost_month.depreciation  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_dept_cost_month` ALTER COLUMN `depreciation` DROP DEFAULT;

-- biz_dept_cost_month.other_cost  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_dept_cost_month` ALTER COLUMN `other_cost` DROP DEFAULT;

-- biz_dept_cost_month.total_cost  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_dept_cost_month` ALTER COLUMN `total_cost` DROP DEFAULT;

-- biz_dept_cost_month.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dept_cost_month` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_dept_cost_month.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dept_cost_month` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_diag_template.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_diag_template` ALTER COLUMN `sort_order` DROP DEFAULT;

-- biz_diag_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_diag_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_diag_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_diag_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_dialysis_machine.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_dialysis_machine` ALTER COLUMN `status` DROP DEFAULT;

-- biz_dialysis_machine.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dialysis_machine` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_dialysis_machine.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dialysis_machine` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_dialysis_patient.dialysis_freq  (tinyint, NO)  原 DEFAULT = '3'
ALTER TABLE `biz_dialysis_patient` ALTER COLUMN `dialysis_freq` DROP DEFAULT;

-- biz_dialysis_patient.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_dialysis_patient` ALTER COLUMN `status` DROP DEFAULT;

-- biz_dialysis_patient.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dialysis_patient` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_dialysis_patient.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dialysis_patient` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_dialysis_prescription.duration_min  (int, NO)  原 DEFAULT = '240'
ALTER TABLE `biz_dialysis_prescription` ALTER COLUMN `duration_min` DROP DEFAULT;

-- biz_dialysis_prescription.blood_flow  (int, NO)  原 DEFAULT = '220'
ALTER TABLE `biz_dialysis_prescription` ALTER COLUMN `blood_flow` DROP DEFAULT;

-- biz_dialysis_prescription.dialyzer  (tinyint, NO)  原 DEFAULT = '3'
ALTER TABLE `biz_dialysis_prescription` ALTER COLUMN `dialyzer` DROP DEFAULT;

-- biz_dialysis_prescription.anticoagulant  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_dialysis_prescription` ALTER COLUMN `anticoagulant` DROP DEFAULT;

-- biz_dialysis_prescription.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_dialysis_prescription` ALTER COLUMN `status` DROP DEFAULT;

-- biz_dialysis_prescription.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dialysis_prescription` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_dialysis_prescription.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dialysis_prescription` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_dialysis_session.time_slot  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_dialysis_session` ALTER COLUMN `time_slot` DROP DEFAULT;

-- biz_dialysis_session.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_dialysis_session` ALTER COLUMN `status` DROP DEFAULT;

-- biz_dialysis_session.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dialysis_session` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_dialysis_session.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dialysis_session` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_diet_plan.source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_diet_plan` ALTER COLUMN `source` DROP DEFAULT;

-- biz_diet_plan.route  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_diet_plan` ALTER COLUMN `route` DROP DEFAULT;

-- biz_diet_plan.plan_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_diet_plan` ALTER COLUMN `plan_status` DROP DEFAULT;

-- biz_diet_plan.confirm_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_diet_plan` ALTER COLUMN `confirm_status` DROP DEFAULT;

-- biz_diet_plan.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_diet_plan` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_diet_plan.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_diet_plan` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_discharge.discharge_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_discharge` ALTER COLUMN `discharge_time` DROP DEFAULT;

-- biz_discharge.discharge_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_discharge` ALTER COLUMN `discharge_status` DROP DEFAULT;

-- biz_discharge.death_flag  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_discharge` ALTER COLUMN `death_flag` DROP DEFAULT;

-- biz_discharge.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_discharge` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_discharge.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_discharge` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_discharge_drug.dispense_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_discharge_drug` ALTER COLUMN `dispense_status` DROP DEFAULT;

-- biz_discharge_drug.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_discharge_drug` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_discharge_drug.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_discharge_drug` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_dispute_case.level  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_dispute_case` ALTER COLUMN `level` DROP DEFAULT;

-- biz_dispute_case.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_dispute_case` ALTER COLUMN `status` DROP DEFAULT;

-- biz_dispute_case.need_seal  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_dispute_case` ALTER COLUMN `need_seal` DROP DEFAULT;

-- biz_dispute_case.seal_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_dispute_case` ALTER COLUMN `seal_status` DROP DEFAULT;

-- biz_dispute_case.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dispute_case` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_dispute_case.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dispute_case` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_dispute_flow.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dispute_flow` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_dispute_flow.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_dispute_flow` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drg_sim_result.is_surgery  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_drg_sim_result` ALTER COLUMN `is_surgery` DROP DEFAULT;

-- biz_drg_sim_result.sim_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drg_sim_result` ALTER COLUMN `sim_status` DROP DEFAULT;

-- biz_drg_sim_result.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drg_sim_result` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drg_sim_result.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drg_sim_result` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_dispensing.dispensing_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_dispensing` ALTER COLUMN `dispensing_status` DROP DEFAULT;

-- biz_drug_dispensing.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_dispensing` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_dispensing.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_dispensing` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_inbound.inbound_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_inbound` ALTER COLUMN `inbound_type` DROP DEFAULT;

-- biz_drug_inbound.total_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_inbound` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_drug_inbound.total_quantity  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_inbound` ALTER COLUMN `total_quantity` DROP DEFAULT;

-- biz_drug_inbound.inbound_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_inbound` ALTER COLUMN `inbound_status` DROP DEFAULT;

-- biz_drug_inbound.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_inbound` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_inbound.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_inbound` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_inbound_detail.detail_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_inbound_detail` ALTER COLUMN `detail_status` DROP DEFAULT;

-- biz_drug_inbound_detail.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_inbound_detail` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_inbound_detail.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_inbound_detail` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_outbound.outbound_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_outbound` ALTER COLUMN `outbound_type` DROP DEFAULT;

-- biz_drug_outbound.total_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_outbound` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_drug_outbound.total_quantity  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_outbound` ALTER COLUMN `total_quantity` DROP DEFAULT;

-- biz_drug_outbound.outbound_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_outbound` ALTER COLUMN `outbound_status` DROP DEFAULT;

-- biz_drug_outbound.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_outbound` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_outbound.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_outbound` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_outbound_detail.detail_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_outbound_detail` ALTER COLUMN `detail_status` DROP DEFAULT;

-- biz_drug_outbound_detail.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_outbound_detail` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_outbound_detail.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_outbound_detail` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_package.package_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_package` ALTER COLUMN `package_type` DROP DEFAULT;

-- biz_drug_package.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_package` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_package.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_package` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_package_detail.quantity  (decimal(10,2), YES)  原 DEFAULT = '1.00'
ALTER TABLE `biz_drug_package_detail` ALTER COLUMN `quantity` DROP DEFAULT;

-- biz_drug_package_detail.price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_package_detail` ALTER COLUMN `price` DROP DEFAULT;

-- biz_drug_package_detail.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_package_detail` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_package_detail.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_package_detail` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_stock.quantity  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_stock` ALTER COLUMN `quantity` DROP DEFAULT;

-- biz_drug_stock.locked_quantity  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_stock` ALTER COLUMN `locked_quantity` DROP DEFAULT;

-- biz_drug_stock.available_quantity  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_stock` ALTER COLUMN `available_quantity` DROP DEFAULT;

-- biz_drug_stock.cost_price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_stock` ALTER COLUMN `cost_price` DROP DEFAULT;

-- biz_drug_stock.total_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_stock` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_drug_stock.stock_room  (tinyint, NO)  原 DEFAULT = '2'
ALTER TABLE `biz_drug_stock` ALTER COLUMN `stock_room` DROP DEFAULT;

-- biz_drug_stock.stock_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_stock` ALTER COLUMN `stock_status` DROP DEFAULT;

-- biz_drug_stock.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_stock` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_stock.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_stock` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_stock_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_stock_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_stock_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_stock_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_supplier_return.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_supplier_return` ALTER COLUMN `status` DROP DEFAULT;

-- biz_drug_supplier_return.total_items  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_drug_supplier_return` ALTER COLUMN `total_items` DROP DEFAULT;

-- biz_drug_supplier_return.total_quantity  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_supplier_return` ALTER COLUMN `total_quantity` DROP DEFAULT;

-- biz_drug_supplier_return.total_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_supplier_return` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_drug_supplier_return.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_supplier_return` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_supplier_return.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_supplier_return` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_supplier_return_item.stock_room  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_supplier_return_item` ALTER COLUMN `stock_room` DROP DEFAULT;

-- biz_drug_supplier_return_item.cost_price  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_supplier_return_item` ALTER COLUMN `cost_price` DROP DEFAULT;

-- biz_drug_supplier_return_item.amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_supplier_return_item` ALTER COLUMN `amount` DROP DEFAULT;

-- biz_drug_supplier_return_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_supplier_return_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_supplier_return_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_supplier_return_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_trace.code_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_trace` ALTER COLUMN `code_type` DROP DEFAULT;

-- biz_drug_trace.source_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_trace` ALTER COLUMN `source_type` DROP DEFAULT;

-- biz_drug_trace.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_trace` ALTER COLUMN `status` DROP DEFAULT;

-- biz_drug_trace.upload_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_drug_trace` ALTER COLUMN `upload_status` DROP DEFAULT;

-- biz_drug_trace.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_trace` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_trace.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_trace` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_transfer.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_drug_transfer` ALTER COLUMN `status` DROP DEFAULT;

-- biz_drug_transfer.total_items  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_drug_transfer` ALTER COLUMN `total_items` DROP DEFAULT;

-- biz_drug_transfer.total_quantity  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_transfer` ALTER COLUMN `total_quantity` DROP DEFAULT;

-- biz_drug_transfer.out_quantity  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_transfer` ALTER COLUMN `out_quantity` DROP DEFAULT;

-- biz_drug_transfer.in_quantity  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_transfer` ALTER COLUMN `in_quantity` DROP DEFAULT;

-- biz_drug_transfer.total_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_transfer` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_drug_transfer.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_transfer` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_transfer.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_transfer` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_drug_transfer_item.cost_price  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_transfer_item` ALTER COLUMN `cost_price` DROP DEFAULT;

-- biz_drug_transfer_item.locked_quantity  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_drug_transfer_item` ALTER COLUMN `locked_quantity` DROP DEFAULT;

-- biz_drug_transfer_item.out_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_drug_transfer_item` ALTER COLUMN `out_flag` DROP DEFAULT;

-- biz_drug_transfer_item.in_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_drug_transfer_item` ALTER COLUMN `in_flag` DROP DEFAULT;

-- biz_drug_transfer_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_transfer_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_drug_transfer_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_drug_transfer_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_duty_log.log_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_duty_log` ALTER COLUMN `log_type` DROP DEFAULT;

-- biz_duty_log.status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_duty_log` ALTER COLUMN `status` DROP DEFAULT;

-- biz_duty_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_duty_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_duty_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_duty_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_duty_post.duty_scope  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_duty_post` ALTER COLUMN `duty_scope` DROP DEFAULT;

-- biz_duty_post.org_type  (tinyint, NO)  原 DEFAULT = '3'
ALTER TABLE `biz_duty_post` ALTER COLUMN `org_type` DROP DEFAULT;

-- biz_duty_post.org_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_duty_post` ALTER COLUMN `org_id` DROP DEFAULT;

-- biz_duty_post.role_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_duty_post` ALTER COLUMN `role_type` DROP DEFAULT;

-- biz_duty_post.duty_level  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_duty_post` ALTER COLUMN `duty_level` DROP DEFAULT;

-- biz_duty_post.attend_mode  (tinyint, NO)  原 DEFAULT = '3'
ALTER TABLE `biz_duty_post` ALTER COLUMN `attend_mode` DROP DEFAULT;

-- biz_duty_post.sort_no  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_duty_post` ALTER COLUMN `sort_no` DROP DEFAULT;

-- biz_duty_post.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_duty_post` ALTER COLUMN `status` DROP DEFAULT;

-- biz_duty_post.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_duty_post` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_duty_post.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_duty_post` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_duty_roster.shift_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_duty_roster` ALTER COLUMN `shift_type` DROP DEFAULT;

-- biz_duty_roster.role_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_duty_roster` ALTER COLUMN `role_type` DROP DEFAULT;

-- biz_duty_roster.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_duty_roster` ALTER COLUMN `status` DROP DEFAULT;

-- biz_duty_roster.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_duty_roster` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_duty_roster.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_duty_roster` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ecg_holter.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ecg_holter` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_ecg_holter.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ecg_holter` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ecg_measure.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ecg_measure` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_ecg_measure.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ecg_measure` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ecg_template.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_ecg_template` ALTER COLUMN `sort_order` DROP DEFAULT;

-- biz_ecg_template.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_ecg_template` ALTER COLUMN `status` DROP DEFAULT;

-- biz_ecg_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ecg_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_ecg_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ecg_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ecg_waveform.ecg_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_ecg_waveform` ALTER COLUMN `ecg_type` DROP DEFAULT;

-- biz_ecg_waveform.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ecg_waveform` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_ecg_waveform.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ecg_waveform` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_emergency.age  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_emergency` ALTER COLUMN `age` DROP DEFAULT;

-- biz_emergency.triage_level  (tinyint, NO)  原 DEFAULT = '3'
ALTER TABLE `biz_emergency` ALTER COLUMN `triage_level` DROP DEFAULT;

-- biz_emergency.zone  (varchar(20), YES)  原 DEFAULT = '绿区'
ALTER TABLE `biz_emergency` ALTER COLUMN `zone` DROP DEFAULT;

-- biz_emergency.assign_type  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_emergency` ALTER COLUMN `assign_type` DROP DEFAULT;

-- biz_emergency.emergency_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_emergency` ALTER COLUMN `emergency_status` DROP DEFAULT;

-- biz_emergency.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_emergency` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_emergency.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_emergency` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_emergency_handover.pending_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_emergency_handover` ALTER COLUMN `pending_count` DROP DEFAULT;

-- biz_emergency_handover.pool_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_emergency_handover` ALTER COLUMN `pool_count` DROP DEFAULT;

-- biz_emergency_handover.overdue_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_emergency_handover` ALTER COLUMN `overdue_count` DROP DEFAULT;

-- biz_emergency_handover.observation_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_emergency_handover` ALTER COLUMN `observation_count` DROP DEFAULT;

-- biz_emergency_handover.obs_over_limit_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_emergency_handover` ALTER COLUMN `obs_over_limit_count` DROP DEFAULT;

-- biz_emergency_handover.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_emergency_handover` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_emergency_handover.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_emergency_handover` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_emergency_handover_item.overdue_level  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_emergency_handover_item` ALTER COLUMN `overdue_level` DROP DEFAULT;

-- biz_emergency_handover_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_emergency_handover_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_emergency_handover_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_emergency_handover_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_emr_signature.chain_no  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_emr_signature` ALTER COLUMN `chain_no` DROP DEFAULT;

-- biz_emr_signature.time_source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_emr_signature` ALTER COLUMN `time_source` DROP DEFAULT;

-- biz_emr_signature.sign_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_emr_signature` ALTER COLUMN `sign_status` DROP DEFAULT;

-- biz_emr_signature.verify_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_emr_signature` ALTER COLUMN `verify_status` DROP DEFAULT;

-- biz_emr_signature.verify_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_emr_signature` ALTER COLUMN `verify_count` DROP DEFAULT;

-- biz_emr_signature.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_emr_signature` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_emr_signature.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_emr_signature` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_endoscopy_record.endo_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_endoscopy_record` ALTER COLUMN `endo_type` DROP DEFAULT;

-- biz_endoscopy_record.anesthesia_method  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_endoscopy_record` ALTER COLUMN `anesthesia_method` DROP DEFAULT;

-- biz_endoscopy_record.hp_result  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_endoscopy_record` ALTER COLUMN `hp_result` DROP DEFAULT;

-- biz_endoscopy_record.biopsy_flag  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_endoscopy_record` ALTER COLUMN `biopsy_flag` DROP DEFAULT;

-- biz_endoscopy_record.biopsy_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_endoscopy_record` ALTER COLUMN `biopsy_count` DROP DEFAULT;

-- biz_endoscopy_record.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_endoscopy_record` ALTER COLUMN `status` DROP DEFAULT;

-- biz_endoscopy_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_endoscopy_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_endoscopy_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_endoscopy_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_equipment_maintain.maintain_result  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_equipment_maintain` ALTER COLUMN `maintain_result` DROP DEFAULT;

-- biz_equipment_maintain.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_equipment_maintain` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_equipment_maintain.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_equipment_maintain` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_equipment_metering.metering_result  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_equipment_metering` ALTER COLUMN `metering_result` DROP DEFAULT;

-- biz_equipment_metering.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_equipment_metering` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_equipment_metering.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_equipment_metering` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_exam_appointment.active_flag  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_appointment` ALTER COLUMN `active_flag` DROP DEFAULT;

-- biz_exam_appointment.is_emergency  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_exam_appointment` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- biz_exam_appointment.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_appointment` ALTER COLUMN `status` DROP DEFAULT;

-- biz_exam_appointment.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_appointment` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_exam_appointment.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_appointment` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_exam_device.am_start  (char(5), NO)  原 DEFAULT = '08:00'
ALTER TABLE `biz_exam_device` ALTER COLUMN `am_start` DROP DEFAULT;

-- biz_exam_device.am_end  (char(5), NO)  原 DEFAULT = '12:00'
ALTER TABLE `biz_exam_device` ALTER COLUMN `am_end` DROP DEFAULT;

-- biz_exam_device.slot_minutes  (int, NO)  原 DEFAULT = '30'
ALTER TABLE `biz_exam_device` ALTER COLUMN `slot_minutes` DROP DEFAULT;

-- biz_exam_device.parallel_count  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_device` ALTER COLUMN `parallel_count` DROP DEFAULT;

-- biz_exam_device.ahead_days  (int, NO)  原 DEFAULT = '7'
ALTER TABLE `biz_exam_device` ALTER COLUMN `ahead_days` DROP DEFAULT;

-- biz_exam_device.max_slot_minutes  (int, NO)  原 DEFAULT = '240'
ALTER TABLE `biz_exam_device` ALTER COLUMN `max_slot_minutes` DROP DEFAULT;

-- biz_exam_device.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_device` ALTER COLUMN `status` DROP DEFAULT;

-- biz_exam_device.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_device` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_exam_device.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_device` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_exam_device_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_device_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_exam_device_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_device_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_exam_film.film_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_film` ALTER COLUMN `film_status` DROP DEFAULT;

-- biz_exam_film.charge_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_exam_film` ALTER COLUMN `charge_flag` DROP DEFAULT;

-- biz_exam_film.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_film` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_exam_film.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_film` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_exam_image.biz_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_image` ALTER COLUMN `biz_type` DROP DEFAULT;

-- biz_exam_image.seq  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_image` ALTER COLUMN `seq` DROP DEFAULT;

-- biz_exam_image.source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_image` ALTER COLUMN `source` DROP DEFAULT;

-- biz_exam_image.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_image` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_exam_image.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_image` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_exam_slot.total_source  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_slot` ALTER COLUMN `total_source` DROP DEFAULT;

-- biz_exam_slot.used_source  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_exam_slot` ALTER COLUMN `used_source` DROP DEFAULT;

-- biz_exam_slot.available_source  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_slot` ALTER COLUMN `available_source` DROP DEFAULT;

-- biz_exam_slot.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_exam_slot` ALTER COLUMN `status` DROP DEFAULT;

-- biz_exam_slot.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_slot` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_exam_slot.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_exam_slot` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_fee_record.fee_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_fee_record` ALTER COLUMN `fee_status` DROP DEFAULT;

-- biz_fee_record.refunded_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_fee_record` ALTER COLUMN `refunded_amount` DROP DEFAULT;

-- biz_fee_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_fee_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_fee_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_fee_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_film_spec.unit_price  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_film_spec` ALTER COLUMN `unit_price` DROP DEFAULT;

-- biz_film_spec.unit  (varchar(20), NO)  原 DEFAULT = '张'
ALTER TABLE `biz_film_spec` ALTER COLUMN `unit` DROP DEFAULT;

-- biz_film_spec.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_film_spec` ALTER COLUMN `sort_order` DROP DEFAULT;

-- biz_film_spec.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_film_spec` ALTER COLUMN `status` DROP DEFAULT;

-- biz_film_spec.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_film_spec` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_film_spec.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_film_spec` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_followup_task.followup_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_followup_task` ALTER COLUMN `followup_status` DROP DEFAULT;

-- biz_followup_task.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_followup_task` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_followup_task.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_followup_task` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_followup_task.call_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_followup_task` ALTER COLUMN `call_status` DROP DEFAULT;

-- biz_followup_task.call_attempts  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_followup_task` ALTER COLUMN `call_attempts` DROP DEFAULT;

-- biz_fund_account.balance  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_fund_account` ALTER COLUMN `balance` DROP DEFAULT;

-- biz_fund_account.version  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_fund_account` ALTER COLUMN `version` DROP DEFAULT;

-- biz_fund_account.total_recharge  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_fund_account` ALTER COLUMN `total_recharge` DROP DEFAULT;

-- biz_fund_account.total_consume  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_fund_account` ALTER COLUMN `total_consume` DROP DEFAULT;

-- biz_fund_account.account_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_fund_account` ALTER COLUMN `account_status` DROP DEFAULT;

-- biz_fund_account.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_fund_account` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_fund_account.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_fund_account` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_fund_account_txn.txn_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_fund_account_txn` ALTER COLUMN `txn_status` DROP DEFAULT;

-- biz_fund_account_txn.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_fund_account_txn` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_fund_account_txn.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_fund_account_txn` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_hand_hygiene_obs.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_hand_hygiene_obs` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_hand_hygiene_obs.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_hand_hygiene_obs` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_icu_monitor.has_airway  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_icu_monitor` ALTER COLUMN `has_airway` DROP DEFAULT;

-- biz_icu_monitor.has_cvc  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_icu_monitor` ALTER COLUMN `has_cvc` DROP DEFAULT;

-- biz_icu_monitor.has_arterial  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_icu_monitor` ALTER COLUMN `has_arterial` DROP DEFAULT;

-- biz_icu_monitor.has_catheter  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_icu_monitor` ALTER COLUMN `has_catheter` DROP DEFAULT;

-- biz_icu_monitor.has_drain  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_icu_monitor` ALTER COLUMN `has_drain` DROP DEFAULT;

-- biz_icu_monitor.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_icu_monitor` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_icu_monitor.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_icu_monitor` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_icu_stay.care_level  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_icu_stay` ALTER COLUMN `care_level` DROP DEFAULT;

-- biz_icu_stay.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_icu_stay` ALTER COLUMN `status` DROP DEFAULT;

-- biz_icu_stay.monitor_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_icu_stay` ALTER COLUMN `monitor_count` DROP DEFAULT;

-- biz_icu_stay.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_icu_stay` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_icu_stay.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_icu_stay` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_infection_case.case_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_infection_case` ALTER COLUMN `case_status` DROP DEFAULT;

-- biz_infection_case.leak_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_infection_case` ALTER COLUMN `leak_flag` DROP DEFAULT;

-- biz_infection_case.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infection_case` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_infection_case.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infection_case` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_infection_monitor.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_infection_monitor` ALTER COLUMN `status` DROP DEFAULT;

-- biz_infection_monitor.infection_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_infection_monitor` ALTER COLUMN `infection_flag` DROP DEFAULT;

-- biz_infection_monitor.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infection_monitor` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_infection_monitor.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infection_monitor` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_infection_monitor_daily.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infection_monitor_daily` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_infection_monitor_daily.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infection_monitor_daily` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_infectious_report.report_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_infectious_report` ALTER COLUMN `report_status` DROP DEFAULT;

-- biz_infectious_report.report_count  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_infectious_report` ALTER COLUMN `report_count` DROP DEFAULT;

-- biz_infectious_report.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infectious_report` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_infectious_report.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infectious_report` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_infusion_round.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infusion_round` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_infusion_round.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infusion_round` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_infusion_seat.area  (varchar(50), NO)  原 DEFAULT = '普通区'
ALTER TABLE `biz_infusion_seat` ALTER COLUMN `area` DROP DEFAULT;

-- biz_infusion_seat.seat_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_infusion_seat` ALTER COLUMN `seat_status` DROP DEFAULT;

-- biz_infusion_seat.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infusion_seat` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_infusion_seat.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_infusion_seat` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_diagnosis.seq_no  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_diagnosis` ALTER COLUMN `seq_no` DROP DEFAULT;

-- biz_inpatient_diagnosis.diag_type  (tinyint, NO)  原 DEFAULT = '2'
ALTER TABLE `biz_inpatient_diagnosis` ALTER COLUMN `diag_type` DROP DEFAULT;

-- biz_inpatient_diagnosis.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_diagnosis` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_diagnosis.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_diagnosis` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_leave.leave_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_leave` ALTER COLUMN `leave_status` DROP DEFAULT;

-- biz_inpatient_leave.print_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_leave` ALTER COLUMN `print_count` DROP DEFAULT;

-- biz_inpatient_leave.sign_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_leave` ALTER COLUMN `sign_status` DROP DEFAULT;

-- biz_inpatient_leave.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_leave` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_leave.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_leave` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_operation.seq_no  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_operation` ALTER COLUMN `seq_no` DROP DEFAULT;

-- biz_inpatient_operation.is_main  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_operation` ALTER COLUMN `is_main` DROP DEFAULT;

-- biz_inpatient_operation.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_operation` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_operation.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_operation` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_order.quantity  (decimal(10,2), NO)  原 DEFAULT = '1.00'
ALTER TABLE `biz_inpatient_order` ALTER COLUMN `quantity` DROP DEFAULT;

-- biz_inpatient_order.price  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inpatient_order` ALTER COLUMN `price` DROP DEFAULT;

-- biz_inpatient_order.amount  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inpatient_order` ALTER COLUMN `amount` DROP DEFAULT;

-- biz_inpatient_order.order_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_order` ALTER COLUMN `order_status` DROP DEFAULT;

-- biz_inpatient_order.is_urgent  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_order` ALTER COLUMN `is_urgent` DROP DEFAULT;

-- biz_inpatient_order.source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_order` ALTER COLUMN `source` DROP DEFAULT;

-- biz_inpatient_order.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_order` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_order.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_order` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_order_exec.exec_seq  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_order_exec` ALTER COLUMN `exec_seq` DROP DEFAULT;

-- biz_inpatient_order_exec.exec_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_order_exec` ALTER COLUMN `exec_status` DROP DEFAULT;

-- biz_inpatient_order_exec.adverse_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_order_exec` ALTER COLUMN `adverse_flag` DROP DEFAULT;

-- biz_inpatient_order_exec.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_order_exec` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_order_exec.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_order_exec` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_order_template.scope  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_order_template` ALTER COLUMN `scope` DROP DEFAULT;

-- biz_inpatient_order_template.order_type  (tinyint, NO)  原 DEFAULT = '2'
ALTER TABLE `biz_inpatient_order_template` ALTER COLUMN `order_type` DROP DEFAULT;

-- biz_inpatient_order_template.item_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_order_template` ALTER COLUMN `item_count` DROP DEFAULT;

-- biz_inpatient_order_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_order_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_order_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_order_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_order_template_item.sort_no  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_order_template_item` ALTER COLUMN `sort_no` DROP DEFAULT;

-- biz_inpatient_order_template_item.quantity  (decimal(12,2), NO)  原 DEFAULT = '1.00'
ALTER TABLE `biz_inpatient_order_template_item` ALTER COLUMN `quantity` DROP DEFAULT;

-- biz_inpatient_order_template_item.price  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inpatient_order_template_item` ALTER COLUMN `price` DROP DEFAULT;

-- biz_inpatient_order_template_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_order_template_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_order_template_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_order_template_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_record.record_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_record` ALTER COLUMN `record_status` DROP DEFAULT;

-- biz_inpatient_record.sign_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_record` ALTER COLUMN `sign_status` DROP DEFAULT;

-- biz_inpatient_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_record_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_record_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_record_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_record_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_settlement.charge_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `charge_count` DROP DEFAULT;

-- biz_inpatient_settlement.total_amount  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_inpatient_settlement.insurance_amount  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `insurance_amount` DROP DEFAULT;

-- biz_inpatient_settlement.patient_pay_amount  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `patient_pay_amount` DROP DEFAULT;

-- biz_inpatient_settlement.prepay_balance  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `prepay_balance` DROP DEFAULT;

-- biz_inpatient_settlement.refund_amount  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `refund_amount` DROP DEFAULT;

-- biz_inpatient_settlement.arrears_amount  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `arrears_amount` DROP DEFAULT;

-- biz_inpatient_settlement.settle_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `settle_status` DROP DEFAULT;

-- biz_inpatient_settlement.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_settlement.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_settlement` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_summary.death_flag  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_summary` ALTER COLUMN `death_flag` DROP DEFAULT;

-- biz_inpatient_summary.autopsy_flag  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_summary` ALTER COLUMN `autopsy_flag` DROP DEFAULT;

-- biz_inpatient_summary.is_surgery  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_summary` ALTER COLUMN `is_surgery` DROP DEFAULT;

-- biz_inpatient_summary.is_transfusion  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_summary` ALTER COLUMN `is_transfusion` DROP DEFAULT;

-- biz_inpatient_summary.is_rescue  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_summary` ALTER COLUMN `is_rescue` DROP DEFAULT;

-- biz_inpatient_summary.is_critical  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_summary` ALTER COLUMN `is_critical` DROP DEFAULT;

-- biz_inpatient_summary.summary_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_summary` ALTER COLUMN `summary_status` DROP DEFAULT;

-- biz_inpatient_summary.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_summary` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_summary.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_summary` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inpatient_transfer.transfer_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_inpatient_transfer` ALTER COLUMN `transfer_type` DROP DEFAULT;

-- biz_inpatient_transfer.stop_orders_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_transfer` ALTER COLUMN `stop_orders_count` DROP DEFAULT;

-- biz_inpatient_transfer.transfer_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inpatient_transfer` ALTER COLUMN `transfer_status` DROP DEFAULT;

-- biz_inpatient_transfer.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_transfer` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inpatient_transfer.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inpatient_transfer` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inspection_apply.is_emergency  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inspection_apply` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- biz_inspection_apply.price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inspection_apply` ALTER COLUMN `price` DROP DEFAULT;

-- biz_inspection_apply.apply_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_inspection_apply` ALTER COLUMN `apply_status` DROP DEFAULT;

-- biz_inspection_apply.sign_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_inspection_apply` ALTER COLUMN `sign_status` DROP DEFAULT;

-- biz_inspection_apply.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inspection_apply` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inspection_apply.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inspection_apply` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inspection_record.price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_inspection_record` ALTER COLUMN `price` DROP DEFAULT;

-- biz_inspection_record.record_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_inspection_record` ALTER COLUMN `record_status` DROP DEFAULT;

-- biz_inspection_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inspection_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inspection_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inspection_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_inspection_template.is_emergency  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inspection_template` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- biz_inspection_template.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_inspection_template` ALTER COLUMN `sort_order` DROP DEFAULT;

-- biz_inspection_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inspection_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_inspection_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_inspection_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_insurance_catalog_rule.self_pay_ratio  (decimal(5,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_catalog_rule` ALTER COLUMN `self_pay_ratio` DROP DEFAULT;

-- biz_insurance_catalog_rule.deductible  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_catalog_rule` ALTER COLUMN `deductible` DROP DEFAULT;

-- biz_insurance_catalog_rule.ceiling  (decimal(10,2), NO)  原 DEFAULT = '999999.99'
ALTER TABLE `biz_insurance_catalog_rule` ALTER COLUMN `ceiling` DROP DEFAULT;

-- biz_insurance_catalog_rule.pool_ratio  (decimal(5,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_catalog_rule` ALTER COLUMN `pool_ratio` DROP DEFAULT;

-- biz_insurance_catalog_rule.limit_flags  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_insurance_catalog_rule` ALTER COLUMN `limit_flags` DROP DEFAULT;

-- biz_insurance_catalog_rule.priority  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_insurance_catalog_rule` ALTER COLUMN `priority` DROP DEFAULT;

-- biz_insurance_catalog_rule.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_insurance_catalog_rule` ALTER COLUMN `status` DROP DEFAULT;

-- biz_insurance_catalog_rule.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_insurance_catalog_rule` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_insurance_catalog_rule.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_insurance_catalog_rule` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_insurance_report.status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_insurance_report` ALTER COLUMN `status` DROP DEFAULT;

-- biz_insurance_report.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_insurance_report` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_insurance_report.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_insurance_report` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_insurance_settlement.total_amount  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_insurance_settlement.drug_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `drug_amount` DROP DEFAULT;

-- biz_insurance_settlement.inspection_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `inspection_amount` DROP DEFAULT;

-- biz_insurance_settlement.laboratory_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `laboratory_amount` DROP DEFAULT;

-- biz_insurance_settlement.treatment_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `treatment_amount` DROP DEFAULT;

-- biz_insurance_settlement.material_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `material_amount` DROP DEFAULT;

-- biz_insurance_settlement.other_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `other_amount` DROP DEFAULT;

-- biz_insurance_settlement.insurance_pay  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `insurance_pay` DROP DEFAULT;

-- biz_insurance_settlement.personal_pay  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `personal_pay` DROP DEFAULT;

-- biz_insurance_settlement.self_pay  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `self_pay` DROP DEFAULT;

-- biz_insurance_settlement.settlement_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `settlement_status` DROP DEFAULT;

-- biz_insurance_settlement.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_insurance_settlement.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_insurance_settlement` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_invoice.invoice_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_invoice` ALTER COLUMN `invoice_type` DROP DEFAULT;

-- biz_invoice.invoice_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_invoice` ALTER COLUMN `invoice_status` DROP DEFAULT;

-- biz_invoice.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_invoice` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_invoice.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_invoice` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_lab_result.abnormal_flag  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lab_result` ALTER COLUMN `abnormal_flag` DROP DEFAULT;

-- biz_lab_result.result_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_lab_result` ALTER COLUMN `result_type` DROP DEFAULT;

-- biz_lab_result.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lab_result` ALTER COLUMN `sort_order` DROP DEFAULT;

-- biz_lab_result.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lab_result` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_lab_result.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lab_result` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_laboratory_apply.is_fasting  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_laboratory_apply` ALTER COLUMN `is_fasting` DROP DEFAULT;

-- biz_laboratory_apply.is_emergency  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_laboratory_apply` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- biz_laboratory_apply.price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_laboratory_apply` ALTER COLUMN `price` DROP DEFAULT;

-- biz_laboratory_apply.apply_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_laboratory_apply` ALTER COLUMN `apply_status` DROP DEFAULT;

-- biz_laboratory_apply.sign_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_laboratory_apply` ALTER COLUMN `sign_status` DROP DEFAULT;

-- biz_laboratory_apply.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_laboratory_apply` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_laboratory_apply.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_laboratory_apply` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_laboratory_record.specimen_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_laboratory_record` ALTER COLUMN `specimen_status` DROP DEFAULT;

-- biz_laboratory_record.price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_laboratory_record` ALTER COLUMN `price` DROP DEFAULT;

-- biz_laboratory_record.record_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_laboratory_record` ALTER COLUMN `record_status` DROP DEFAULT;

-- biz_laboratory_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_laboratory_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_laboratory_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_laboratory_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_laboratory_template.is_emergency  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_laboratory_template` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- biz_laboratory_template.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_laboratory_template` ALTER COLUMN `sort_order` DROP DEFAULT;

-- biz_laboratory_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_laboratory_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_laboratory_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_laboratory_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_lis_eqa_compare.allow_source  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_lis_eqa_compare` ALTER COLUMN `allow_source` DROP DEFAULT;

-- biz_lis_eqa_compare.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_lis_eqa_compare` ALTER COLUMN `status` DROP DEFAULT;

-- biz_lis_eqa_compare.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_eqa_compare` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_lis_eqa_compare.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_eqa_compare` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_lis_eqa_plan.item_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lis_eqa_plan` ALTER COLUMN `item_count` DROP DEFAULT;

-- biz_lis_eqa_plan.sample_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lis_eqa_plan` ALTER COLUMN `sample_count` DROP DEFAULT;

-- biz_lis_eqa_plan.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_lis_eqa_plan` ALTER COLUMN `status` DROP DEFAULT;

-- biz_lis_eqa_plan.fail_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lis_eqa_plan` ALTER COLUMN `fail_count` DROP DEFAULT;

-- biz_lis_eqa_plan.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_eqa_plan` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_lis_eqa_plan.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_eqa_plan` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_lis_eqa_sample.instrument_name  (varchar(100), NO)  原 DEFAULT = ''
ALTER TABLE `biz_lis_eqa_sample` ALTER COLUMN `instrument_name` DROP DEFAULT;

-- biz_lis_eqa_sample.overdue_flag  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lis_eqa_sample` ALTER COLUMN `overdue_flag` DROP DEFAULT;

-- biz_lis_eqa_sample.judge_mode  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lis_eqa_sample` ALTER COLUMN `judge_mode` DROP DEFAULT;

-- biz_lis_eqa_sample.result_status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lis_eqa_sample` ALTER COLUMN `result_status` DROP DEFAULT;

-- biz_lis_eqa_sample.status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lis_eqa_sample` ALTER COLUMN `status` DROP DEFAULT;

-- biz_lis_eqa_sample.handle_status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lis_eqa_sample` ALTER COLUMN `handle_status` DROP DEFAULT;

-- biz_lis_eqa_sample.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_eqa_sample` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_lis_eqa_sample.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_eqa_sample` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_lis_qc_plan.qc_level  (tinyint, YES)  原 DEFAULT = '2'
ALTER TABLE `biz_lis_qc_plan` ALTER COLUMN `qc_level` DROP DEFAULT;

-- biz_lis_qc_plan.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_lis_qc_plan` ALTER COLUMN `status` DROP DEFAULT;

-- biz_lis_qc_plan.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_qc_plan` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_lis_qc_plan.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_qc_plan` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_lis_qc_record.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_lis_qc_record` ALTER COLUMN `status` DROP DEFAULT;

-- biz_lis_qc_record.handle_status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_lis_qc_record` ALTER COLUMN `handle_status` DROP DEFAULT;

-- biz_lis_qc_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_qc_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_lis_qc_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_lis_qc_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_meal_order.quantity  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_meal_order` ALTER COLUMN `quantity` DROP DEFAULT;

-- biz_meal_order.deliver_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_meal_order` ALTER COLUMN `deliver_status` DROP DEFAULT;

-- biz_meal_order.source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_meal_order` ALTER COLUMN `source` DROP DEFAULT;

-- biz_meal_order.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_meal_order` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_meal_order.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_meal_order` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_medical_record.visit_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_medical_record` ALTER COLUMN `visit_type` DROP DEFAULT;

-- biz_medical_record.record_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_medical_record` ALTER COLUMN `record_status` DROP DEFAULT;

-- biz_medical_record.review_status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_medical_record` ALTER COLUMN `review_status` DROP DEFAULT;

-- biz_medical_record.sign_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_medical_record` ALTER COLUMN `sign_status` DROP DEFAULT;

-- biz_medical_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medical_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_medical_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medical_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_medical_record_archive.archive_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_medical_record_archive` ALTER COLUMN `archive_status` DROP DEFAULT;

-- biz_medical_record_archive.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medical_record_archive` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_medical_record_archive.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medical_record_archive` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_medical_record_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medical_record_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_medical_record_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medical_record_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_medical_waste.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_medical_waste` ALTER COLUMN `status` DROP DEFAULT;

-- biz_medical_waste.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medical_waste` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_medical_waste.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medical_waste` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_medicaltech_execution.execution_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_medicaltech_execution` ALTER COLUMN `execution_status` DROP DEFAULT;

-- biz_medicaltech_execution.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medicaltech_execution` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_medicaltech_execution.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_medicaltech_execution` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_narcotic_register.ampoule_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_narcotic_register` ALTER COLUMN `ampoule_status` DROP DEFAULT;

-- biz_narcotic_register.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_narcotic_register` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_narcotic_register.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_narcotic_register` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_nurse_schedule.unit_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_nurse_schedule` ALTER COLUMN `unit_type` DROP DEFAULT;

-- biz_nurse_schedule.work_minutes  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nurse_schedule` ALTER COLUMN `work_minutes` DROP DEFAULT;

-- biz_nurse_schedule.schedule_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_nurse_schedule` ALTER COLUMN `schedule_status` DROP DEFAULT;

-- biz_nurse_schedule.schedule_source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_nurse_schedule` ALTER COLUMN `schedule_source` DROP DEFAULT;

-- biz_nurse_schedule.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nurse_schedule` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_nurse_schedule.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nurse_schedule` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_nursing_assessment.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_assessment` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_nursing_assessment.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_assessment` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_nursing_qc_check.sample_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nursing_qc_check` ALTER COLUMN `sample_count` DROP DEFAULT;

-- biz_nursing_qc_check.qualified_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nursing_qc_check` ALTER COLUMN `qualified_count` DROP DEFAULT;

-- biz_nursing_qc_check.qualified_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_nursing_qc_check` ALTER COLUMN `qualified_rate` DROP DEFAULT;

-- biz_nursing_qc_check.full_score  (decimal(7,1), NO)  原 DEFAULT = '0.0'
ALTER TABLE `biz_nursing_qc_check` ALTER COLUMN `full_score` DROP DEFAULT;

-- biz_nursing_qc_check.total_score  (decimal(7,1), NO)  原 DEFAULT = '0.0'
ALTER TABLE `biz_nursing_qc_check` ALTER COLUMN `total_score` DROP DEFAULT;

-- biz_nursing_qc_check.score_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_nursing_qc_check` ALTER COLUMN `score_rate` DROP DEFAULT;

-- biz_nursing_qc_check.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_nursing_qc_check` ALTER COLUMN `status` DROP DEFAULT;

-- biz_nursing_qc_check.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_qc_check` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_nursing_qc_check.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_qc_check` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_nursing_qc_check_item.checked_num  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nursing_qc_check_item` ALTER COLUMN `checked_num` DROP DEFAULT;

-- biz_nursing_qc_check_item.qualified_num  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nursing_qc_check_item` ALTER COLUMN `qualified_num` DROP DEFAULT;

-- biz_nursing_qc_check_item.full_score  (decimal(5,1), NO)  原 DEFAULT = '0.0'
ALTER TABLE `biz_nursing_qc_check_item` ALTER COLUMN `full_score` DROP DEFAULT;

-- biz_nursing_qc_check_item.score  (decimal(5,1), NO)  原 DEFAULT = '0.0'
ALTER TABLE `biz_nursing_qc_check_item` ALTER COLUMN `score` DROP DEFAULT;

-- biz_nursing_qc_check_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_qc_check_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_nursing_qc_check_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_qc_check_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_nursing_qc_indicator.numerator  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_nursing_qc_indicator` ALTER COLUMN `numerator` DROP DEFAULT;

-- biz_nursing_qc_indicator.denominator  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_nursing_qc_indicator` ALTER COLUMN `denominator` DROP DEFAULT;

-- biz_nursing_qc_indicator.report_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_nursing_qc_indicator` ALTER COLUMN `report_status` DROP DEFAULT;

-- biz_nursing_qc_indicator.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_qc_indicator` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_nursing_qc_indicator.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_qc_indicator` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_nursing_record.record_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_nursing_record` ALTER COLUMN `record_status` DROP DEFAULT;

-- biz_nursing_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_nursing_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nursing_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_nutrition_screen.screen_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_nutrition_screen` ALTER COLUMN `screen_type` DROP DEFAULT;

-- biz_nutrition_screen.total_score  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_screen` ALTER COLUMN `total_score` DROP DEFAULT;

-- biz_nutrition_screen.risk_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_screen` ALTER COLUMN `risk_flag` DROP DEFAULT;

-- biz_nutrition_screen.screen_source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_nutrition_screen` ALTER COLUMN `screen_source` DROP DEFAULT;

-- biz_nutrition_screen.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nutrition_screen` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_nutrition_screen.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nutrition_screen` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_nutrition_stats.scope_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `scope_type` DROP DEFAULT;

-- biz_nutrition_stats.discharge_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `discharge_count` DROP DEFAULT;

-- biz_nutrition_stats.screened_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `screened_count` DROP DEFAULT;

-- biz_nutrition_stats.screen_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `screen_rate` DROP DEFAULT;

-- biz_nutrition_stats.risk_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `risk_count` DROP DEFAULT;

-- biz_nutrition_stats.risk_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `risk_rate` DROP DEFAULT;

-- biz_nutrition_stats.diet_plan_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `diet_plan_count` DROP DEFAULT;

-- biz_nutrition_stats.diet_confirm_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `diet_confirm_count` DROP DEFAULT;

-- biz_nutrition_stats.diet_confirm_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `diet_confirm_rate` DROP DEFAULT;

-- biz_nutrition_stats.consult_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `consult_count` DROP DEFAULT;

-- biz_nutrition_stats.consult_ontime_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `consult_ontime_count` DROP DEFAULT;

-- biz_nutrition_stats.consult_ontime_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `consult_ontime_rate` DROP DEFAULT;

-- biz_nutrition_stats.meal_order_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `meal_order_count` DROP DEFAULT;

-- biz_nutrition_stats.meal_signed_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `meal_signed_count` DROP DEFAULT;

-- biz_nutrition_stats.meal_sign_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `meal_sign_rate` DROP DEFAULT;

-- biz_nutrition_stats.meal_cancel_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `meal_cancel_count` DROP DEFAULT;

-- biz_nutrition_stats.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_nutrition_stats.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_nutrition_stats` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_online_consult.consult_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_online_consult` ALTER COLUMN `consult_type` DROP DEFAULT;

-- biz_online_consult.need_visit  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_online_consult` ALTER COLUMN `need_visit` DROP DEFAULT;

-- biz_online_consult.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_online_consult` ALTER COLUMN `status` DROP DEFAULT;

-- biz_online_consult.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_online_consult` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_online_consult.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_online_consult` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_operation_apply.is_emergency  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_operation_apply` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- biz_operation_apply.is_main  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_operation_apply` ALTER COLUMN `is_main` DROP DEFAULT;

-- biz_operation_apply.operation_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_operation_apply` ALTER COLUMN `operation_status` DROP DEFAULT;

-- biz_operation_apply.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_apply` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_operation_apply.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_apply` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_operation_charge_item.charge_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_operation_charge_item` ALTER COLUMN `charge_status` DROP DEFAULT;

-- biz_operation_charge_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_charge_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_operation_charge_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_charge_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_operation_count.phase  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_operation_count` ALTER COLUMN `phase` DROP DEFAULT;

-- biz_operation_count.status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_operation_count` ALTER COLUMN `status` DROP DEFAULT;

-- biz_operation_count.discrepancy_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_operation_count` ALTER COLUMN `discrepancy_flag` DROP DEFAULT;

-- biz_operation_count.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_count` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_operation_count.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_count` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_operation_count_item.seq_no  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_operation_count_item` ALTER COLUMN `seq_no` DROP DEFAULT;

-- biz_operation_count_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_count_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_operation_count_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_count_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_operation_safety_check.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_safety_check` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_operation_safety_check.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_operation_safety_check` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_outp_infusion.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_outp_infusion` ALTER COLUMN `status` DROP DEFAULT;

-- biz_outp_infusion.adverse_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_outp_infusion` ALTER COLUMN `adverse_flag` DROP DEFAULT;

-- biz_outp_infusion.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_outp_infusion` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_outp_infusion.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_outp_infusion` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_outp_infusion_round.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_outp_infusion_round` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_outp_infusion_round.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_outp_infusion_round` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pathology_block.block_count  (int, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_pathology_block` ALTER COLUMN `block_count` DROP DEFAULT;

-- biz_pathology_block.slice_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_pathology_block` ALTER COLUMN `slice_count` DROP DEFAULT;

-- biz_pathology_block.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_pathology_block` ALTER COLUMN `status` DROP DEFAULT;

-- biz_pathology_block.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathology_block` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pathology_block.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathology_block` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pathology_order.exam_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_pathology_order` ALTER COLUMN `exam_type` DROP DEFAULT;

-- biz_pathology_order.is_frozen  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_pathology_order` ALTER COLUMN `is_frozen` DROP DEFAULT;

-- biz_pathology_order.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_pathology_order` ALTER COLUMN `status` DROP DEFAULT;

-- biz_pathology_order.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathology_order` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pathology_order.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathology_order` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pathway.version  (varchar(16), NO)  原 DEFAULT = 'V1'
ALTER TABLE `biz_pathway` ALTER COLUMN `version` DROP DEFAULT;

-- biz_pathway.total_days  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_pathway` ALTER COLUMN `total_days` DROP DEFAULT;

-- biz_pathway.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_pathway` ALTER COLUMN `status` DROP DEFAULT;

-- biz_pathway.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathway` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pathway.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathway` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pathway_enroll.total_days  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_pathway_enroll` ALTER COLUMN `total_days` DROP DEFAULT;

-- biz_pathway_enroll.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_pathway_enroll` ALTER COLUMN `status` DROP DEFAULT;

-- biz_pathway_enroll.variance_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_pathway_enroll` ALTER COLUMN `variance_count` DROP DEFAULT;

-- biz_pathway_enroll.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathway_enroll` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pathway_enroll.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathway_enroll` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pathway_step.sort_no  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_pathway_step` ALTER COLUMN `sort_no` DROP DEFAULT;

-- biz_pathway_step.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathway_step` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pathway_step.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathway_step` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pathway_variance.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathway_variance` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pathway_variance.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pathway_variance` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient.merge_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_patient` ALTER COLUMN `merge_status` DROP DEFAULT;

-- biz_patient.marital_status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_patient` ALTER COLUMN `marital_status` DROP DEFAULT;

-- biz_patient.patient_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_patient` ALTER COLUMN `patient_type` DROP DEFAULT;

-- biz_patient.card_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_patient` ALTER COLUMN `card_type` DROP DEFAULT;

-- biz_patient.balance  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_patient` ALTER COLUMN `balance` DROP DEFAULT;

-- biz_patient.total_expense  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_patient` ALTER COLUMN `total_expense` DROP DEFAULT;

-- biz_patient.visit_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_patient` ALTER COLUMN `visit_count` DROP DEFAULT;

-- biz_patient.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_patient` ALTER COLUMN `status` DROP DEFAULT;

-- biz_patient.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient_allergy.occurrence_count  (int, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_patient_allergy` ALTER COLUMN `occurrence_count` DROP DEFAULT;

-- biz_patient_allergy.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_allergy` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient_allergy.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_allergy` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient_contact.is_primary  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_patient_contact` ALTER COLUMN `is_primary` DROP DEFAULT;

-- biz_patient_contact.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_patient_contact` ALTER COLUMN `status` DROP DEFAULT;

-- biz_patient_contact.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_contact` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient_contact.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_contact` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient_family_history.is_alive  (tinyint(1), YES)  原 DEFAULT = '1'
ALTER TABLE `biz_patient_family_history` ALTER COLUMN `is_alive` DROP DEFAULT;

-- biz_patient_family_history.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_family_history` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient_family_history.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_family_history` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient_guardian.relation  (tinyint, NO)  原 DEFAULT = '99'
ALTER TABLE `biz_patient_guardian` ALTER COLUMN `relation` DROP DEFAULT;

-- biz_patient_guardian.is_default  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_patient_guardian` ALTER COLUMN `is_default` DROP DEFAULT;

-- biz_patient_guardian.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_patient_guardian` ALTER COLUMN `status` DROP DEFAULT;

-- biz_patient_guardian.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_guardian` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient_guardian.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_guardian` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient_medication_history.status  (varchar(20), YES)  原 DEFAULT = '已完成'
ALTER TABLE `biz_patient_medication_history` ALTER COLUMN `status` DROP DEFAULT;

-- biz_patient_medication_history.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_medication_history` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient_medication_history.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_medication_history` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient_merge_log.log_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_patient_merge_log` ALTER COLUMN `log_status` DROP DEFAULT;

-- biz_patient_merge_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_merge_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient_merge_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_merge_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient_past_disease.relapse_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_patient_past_disease` ALTER COLUMN `relapse_count` DROP DEFAULT;

-- biz_patient_past_disease.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_past_disease` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient_past_disease.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_past_disease` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient_surgery_history.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_surgery_history` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient_surgery_history.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_surgery_history` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_patient_tag_relation.source_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_patient_tag_relation` ALTER COLUMN `source_type` DROP DEFAULT;

-- biz_patient_tag_relation.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_tag_relation` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_patient_tag_relation.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_patient_tag_relation` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pay_channel_bill.import_way  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_pay_channel_bill` ALTER COLUMN `import_way` DROP DEFAULT;

-- biz_pay_channel_bill.match_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_pay_channel_bill` ALTER COLUMN `match_status` DROP DEFAULT;

-- biz_pay_channel_bill.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pay_channel_bill` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pay_channel_bill.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pay_channel_bill` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pay_order.channel  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_pay_order` ALTER COLUMN `channel` DROP DEFAULT;

-- biz_pay_order.pay_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_pay_order` ALTER COLUMN `pay_status` DROP DEFAULT;

-- biz_pay_order.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pay_order` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pay_order.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pay_order` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_payment_txn.direction  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_payment_txn` ALTER COLUMN `direction` DROP DEFAULT;

-- biz_payment_txn.txn_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_payment_txn` ALTER COLUMN `txn_status` DROP DEFAULT;

-- biz_payment_txn.insurance_cancelled  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_payment_txn` ALTER COLUMN `insurance_cancelled` DROP DEFAULT;

-- biz_payment_txn.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_payment_txn` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_payment_txn.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_payment_txn` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_perf_result.revenue  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_perf_result` ALTER COLUMN `revenue` DROP DEFAULT;

-- biz_perf_result.drug_revenue  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_perf_result` ALTER COLUMN `drug_revenue` DROP DEFAULT;

-- biz_perf_result.total_cost  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_perf_result` ALTER COLUMN `total_cost` DROP DEFAULT;

-- biz_perf_result.surplus  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_perf_result` ALTER COLUMN `surplus` DROP DEFAULT;

-- biz_perf_result.bonus_rate  (decimal(6,4), NO)  原 DEFAULT = '0.0600'
ALTER TABLE `biz_perf_result` ALTER COLUMN `bonus_rate` DROP DEFAULT;

-- biz_perf_result.perf_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_perf_result` ALTER COLUMN `perf_amount` DROP DEFAULT;

-- biz_perf_result.perf_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_perf_result` ALTER COLUMN `perf_status` DROP DEFAULT;

-- biz_perf_result.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_perf_result` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_perf_result.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_perf_result` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pivas_batch.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_pivas_batch` ALTER COLUMN `status` DROP DEFAULT;

-- biz_pivas_batch.item_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_pivas_batch` ALTER COLUMN `item_count` DROP DEFAULT;

-- biz_pivas_batch.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pivas_batch` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pivas_batch.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pivas_batch` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_pivas_item.pivas_seq  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_pivas_item` ALTER COLUMN `pivas_seq` DROP DEFAULT;

-- biz_pivas_item.price  (decimal(12,4), NO)  原 DEFAULT = '0.0000'
ALTER TABLE `biz_pivas_item` ALTER COLUMN `price` DROP DEFAULT;

-- biz_pivas_item.amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_pivas_item` ALTER COLUMN `amount` DROP DEFAULT;

-- biz_pivas_item.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_pivas_item` ALTER COLUMN `status` DROP DEFAULT;

-- biz_pivas_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pivas_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_pivas_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_pivas_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_prepay.pay_method  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_prepay` ALTER COLUMN `pay_method` DROP DEFAULT;

-- biz_prepay.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_prepay` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_prepay.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_prepay` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_prescription.prescription_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_prescription` ALTER COLUMN `prescription_type` DROP DEFAULT;

-- biz_prescription.prescription_source  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_prescription` ALTER COLUMN `prescription_source` DROP DEFAULT;

-- biz_prescription.total_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_prescription` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_prescription.drug_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription` ALTER COLUMN `drug_count` DROP DEFAULT;

-- biz_prescription.prescription_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_prescription` ALTER COLUMN `prescription_status` DROP DEFAULT;

-- biz_prescription.payment_status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription` ALTER COLUMN `payment_status` DROP DEFAULT;

-- biz_prescription.is_long_prescription  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription` ALTER COLUMN `is_long_prescription` DROP DEFAULT;

-- biz_prescription.pay_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_prescription` ALTER COLUMN `pay_amount` DROP DEFAULT;

-- biz_prescription.return_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription` ALTER COLUMN `return_count` DROP DEFAULT;

-- biz_prescription.is_urgent  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription` ALTER COLUMN `is_urgent` DROP DEFAULT;

-- biz_prescription.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_prescription` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_prescription.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_prescription` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_prescription_audit_log.round_no  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_prescription_audit_log` ALTER COLUMN `round_no` DROP DEFAULT;

-- biz_prescription_audit_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_prescription_audit_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_prescription_audit_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_prescription_audit_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_prescription_detail.is_skin_test  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription_detail` ALTER COLUMN `is_skin_test` DROP DEFAULT;

-- biz_prescription_detail.is_allergy  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription_detail` ALTER COLUMN `is_allergy` DROP DEFAULT;

-- biz_prescription_detail.is_combo  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription_detail` ALTER COLUMN `is_combo` DROP DEFAULT;

-- biz_prescription_detail.is_special  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription_detail` ALTER COLUMN `is_special` DROP DEFAULT;

-- biz_prescription_detail.detail_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_prescription_detail` ALTER COLUMN `detail_status` DROP DEFAULT;

-- biz_prescription_detail.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_prescription_detail` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_prescription_detail.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_prescription_detail` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_prescription_detail.payment_status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_prescription_detail` ALTER COLUMN `payment_status` DROP DEFAULT;

-- biz_previsit_record.patient_no  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `biz_previsit_record` ALTER COLUMN `patient_no` DROP DEFAULT;

-- biz_previsit_record.patient_name  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `biz_previsit_record` ALTER COLUMN `patient_name` DROP DEFAULT;

-- biz_previsit_record.dept_name  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `biz_previsit_record` ALTER COLUMN `dept_name` DROP DEFAULT;

-- biz_previsit_record.main_symptom  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `biz_previsit_record` ALTER COLUMN `main_symptom` DROP DEFAULT;

-- biz_previsit_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_previsit_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_previsit_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_previsit_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_previsit_record.remark  (varchar(500), YES)  原 DEFAULT = ''
ALTER TABLE `biz_previsit_record` ALTER COLUMN `remark` DROP DEFAULT;

-- biz_public_health_report.report_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_public_health_report` ALTER COLUMN `report_status` DROP DEFAULT;

-- biz_public_health_report.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_public_health_report` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_public_health_report.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_public_health_report` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_purchase_order.order_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_purchase_order` ALTER COLUMN `order_time` DROP DEFAULT;

-- biz_purchase_order.total_amount  (decimal(12,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_purchase_order` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_purchase_order.approval_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_purchase_order` ALTER COLUMN `approval_status` DROP DEFAULT;

-- biz_purchase_order.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_purchase_order` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_purchase_order.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_purchase_order` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_purchase_order_detail.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_purchase_order_detail` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_purchase_order_detail.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_purchase_order_detail` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_quality_control.record_source  (varchar(16), NO)  原 DEFAULT = 'OUTPATIENT'
ALTER TABLE `biz_quality_control` ALTER COLUMN `record_source` DROP DEFAULT;

-- biz_quality_control.error_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_quality_control` ALTER COLUMN `error_count` DROP DEFAULT;

-- biz_quality_control.qc_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_quality_control` ALTER COLUMN `qc_status` DROP DEFAULT;

-- biz_quality_control.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_quality_control` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_quality_control.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_quality_control` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_quality_control_issue.deduct  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_quality_control_issue` ALTER COLUMN `deduct` DROP DEFAULT;

-- biz_quality_control_issue.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_quality_control_issue` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_quality_control_issue.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_quality_control_issue` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_queue.queue_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_queue` ALTER COLUMN `queue_type` DROP DEFAULT;

-- biz_queue.regist_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_queue` ALTER COLUMN `regist_type` DROP DEFAULT;

-- biz_queue.queue_status  (tinyint, YES)  原 DEFAULT = '2'
ALTER TABLE `biz_queue` ALTER COLUMN `queue_status` DROP DEFAULT;

-- biz_queue.triage_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_queue` ALTER COLUMN `triage_status` DROP DEFAULT;

-- biz_queue.call_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_queue` ALTER COLUMN `call_count` DROP DEFAULT;

-- biz_queue.wait_duration  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_queue` ALTER COLUMN `wait_duration` DROP DEFAULT;

-- biz_queue.is_overdue  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_queue` ALTER COLUMN `is_overdue` DROP DEFAULT;

-- biz_queue.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_queue` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_queue.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_queue` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_radio_report_template.is_public  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_radio_report_template` ALTER COLUMN `is_public` DROP DEFAULT;

-- biz_radio_report_template.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_radio_report_template` ALTER COLUMN `sort_order` DROP DEFAULT;

-- biz_radio_report_template.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_radio_report_template` ALTER COLUMN `status` DROP DEFAULT;

-- biz_radio_report_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_radio_report_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_radio_report_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_radio_report_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_record_qc_flow.record_source  (varchar(20), NO)  原 DEFAULT = 'OUTPATIENT'
ALTER TABLE `biz_record_qc_flow` ALTER COLUMN `record_source` DROP DEFAULT;

-- biz_record_qc_flow.flow_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_record_qc_flow` ALTER COLUMN `flow_status` DROP DEFAULT;

-- biz_record_qc_flow.current_level  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_record_qc_flow` ALTER COLUMN `current_level` DROP DEFAULT;

-- biz_record_qc_flow.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_record_qc_flow` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_record_qc_flow.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_record_qc_flow` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_record_qc_flow_action.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_record_qc_flow_action` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_record_qc_flow_action.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_record_qc_flow_action` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_referral.referral_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_referral` ALTER COLUMN `referral_time` DROP DEFAULT;

-- biz_referral.referral_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_referral` ALTER COLUMN `referral_status` DROP DEFAULT;

-- biz_referral.direction  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_referral` ALTER COLUMN `direction` DROP DEFAULT;

-- biz_referral.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_referral` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_referral.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_referral` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_refund_apply.apply_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_refund_apply` ALTER COLUMN `apply_status` DROP DEFAULT;

-- biz_refund_apply.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_refund_apply` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_refund_apply.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_refund_apply` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_report.is_critical  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_report` ALTER COLUMN `is_critical` DROP DEFAULT;

-- biz_report.report_version  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_report` ALTER COLUMN `report_version` DROP DEFAULT;

-- biz_report.film_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_report` ALTER COLUMN `film_count` DROP DEFAULT;

-- biz_report.report_status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_report` ALTER COLUMN `report_status` DROP DEFAULT;

-- biz_report.is_urgent  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_report` ALTER COLUMN `is_urgent` DROP DEFAULT;

-- biz_report.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_report` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_report.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_report` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_revisit_fee_policy.same_doctor  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_revisit_fee_policy` ALTER COLUMN `same_doctor` DROP DEFAULT;

-- biz_revisit_fee_policy.same_dept  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_revisit_fee_policy` ALTER COLUMN `same_dept` DROP DEFAULT;

-- biz_revisit_fee_policy.priority  (int, NO)  原 DEFAULT = '100'
ALTER TABLE `biz_revisit_fee_policy` ALTER COLUMN `priority` DROP DEFAULT;

-- biz_revisit_fee_policy.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_revisit_fee_policy` ALTER COLUMN `status` DROP DEFAULT;

-- biz_revisit_fee_policy.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_revisit_fee_policy` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_revisit_fee_policy.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_revisit_fee_policy` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_rx_doctor_talk.talk_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_rx_doctor_talk` ALTER COLUMN `talk_type` DROP DEFAULT;

-- biz_rx_doctor_talk.related_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_rx_doctor_talk` ALTER COLUMN `related_count` DROP DEFAULT;

-- biz_rx_doctor_talk.rectify_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_rx_doctor_talk` ALTER COLUMN `rectify_status` DROP DEFAULT;

-- biz_rx_doctor_talk.doctor_confirm  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_rx_doctor_talk` ALTER COLUMN `doctor_confirm` DROP DEFAULT;

-- biz_rx_doctor_talk.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_doctor_talk` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_rx_doctor_talk.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_doctor_talk` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_rx_flow.org_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_rx_flow` ALTER COLUMN `org_type` DROP DEFAULT;

-- biz_rx_flow.flow_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_rx_flow` ALTER COLUMN `flow_status` DROP DEFAULT;

-- biz_rx_flow.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_flow` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_rx_flow.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_flow` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_rx_review_batch.review_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_rx_review_batch` ALTER COLUMN `review_type` DROP DEFAULT;

-- biz_rx_review_batch.reviewed_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_rx_review_batch` ALTER COLUMN `reviewed_count` DROP DEFAULT;

-- biz_rx_review_batch.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_rx_review_batch` ALTER COLUMN `status` DROP DEFAULT;

-- biz_rx_review_batch.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_review_batch` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_rx_review_batch.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_review_batch` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_rx_review_item.drug_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_rx_review_item` ALTER COLUMN `drug_count` DROP DEFAULT;

-- biz_rx_review_item.total_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_rx_review_item` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_rx_review_item.prescription_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_rx_review_item` ALTER COLUMN `prescription_type` DROP DEFAULT;

-- biz_rx_review_item.prescription_source  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_rx_review_item` ALTER COLUMN `prescription_source` DROP DEFAULT;

-- biz_rx_review_item.review_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_rx_review_item` ALTER COLUMN `review_status` DROP DEFAULT;

-- biz_rx_review_item.publicity_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_rx_review_item` ALTER COLUMN `publicity_status` DROP DEFAULT;

-- biz_rx_review_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_review_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_rx_review_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_review_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_rx_template.drug_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_rx_template` ALTER COLUMN `drug_count` DROP DEFAULT;

-- biz_rx_template.total_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_rx_template` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_rx_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_rx_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_rx_template_detail.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_template_detail` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_rx_template_detail.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_rx_template_detail` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_schedule.org_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_schedule` ALTER COLUMN `org_type` DROP DEFAULT;

-- biz_schedule.org_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_schedule` ALTER COLUMN `org_id` DROP DEFAULT;

-- biz_schedule.org_name  (varchar(128), NO)  原 DEFAULT = ''
ALTER TABLE `biz_schedule` ALTER COLUMN `org_name` DROP DEFAULT;

-- biz_schedule.dept_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_schedule` ALTER COLUMN `dept_id` DROP DEFAULT;

-- biz_schedule.dept_name  (varchar(128), NO)  原 DEFAULT = ''
ALTER TABLE `biz_schedule` ALTER COLUMN `dept_name` DROP DEFAULT;

-- biz_schedule.employee_name  (varchar(50), NO)  原 DEFAULT = ''
ALTER TABLE `biz_schedule` ALTER COLUMN `employee_name` DROP DEFAULT;

-- biz_schedule.staff_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_schedule` ALTER COLUMN `staff_type` DROP DEFAULT;

-- biz_schedule.shift_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_schedule` ALTER COLUMN `shift_id` DROP DEFAULT;

-- biz_schedule.duty_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_schedule` ALTER COLUMN `duty_status` DROP DEFAULT;

-- biz_schedule.attend_mode  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_schedule` ALTER COLUMN `attend_mode` DROP DEFAULT;

-- biz_schedule.clinic_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_schedule` ALTER COLUMN `clinic_flag` DROP DEFAULT;

-- biz_schedule.work_minutes  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_schedule` ALTER COLUMN `work_minutes` DROP DEFAULT;

-- biz_schedule.schedule_source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_schedule` ALTER COLUMN `schedule_source` DROP DEFAULT;

-- biz_schedule.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_schedule` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_schedule.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_schedule` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_service_message.status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_service_message` ALTER COLUMN `status` DROP DEFAULT;

-- biz_service_message.priority  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_service_message` ALTER COLUMN `priority` DROP DEFAULT;

-- biz_service_message.reply_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_service_message` ALTER COLUMN `reply_count` DROP DEFAULT;

-- biz_service_message.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_service_message` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_service_message.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_service_message` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_service_message_bak_20261010.status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_service_message_bak_20261010` ALTER COLUMN `status` DROP DEFAULT;

-- biz_service_message_bak_20261010.priority  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_service_message_bak_20261010` ALTER COLUMN `priority` DROP DEFAULT;

-- biz_service_message_bak_20261010.reply_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_service_message_bak_20261010` ALTER COLUMN `reply_count` DROP DEFAULT;

-- biz_service_message_bak_20261010.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_service_message_bak_20261010` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_service_message_bak_20261010.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_service_message_bak_20261010` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_service_ticket_log.visible_to_patient  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_service_ticket_log` ALTER COLUMN `visible_to_patient` DROP DEFAULT;

-- biz_service_ticket_log.operator_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_service_ticket_log` ALTER COLUMN `operator_type` DROP DEFAULT;

-- biz_service_ticket_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_service_ticket_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_service_ticket_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_service_ticket_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_service_ticket_log_bak_20261010.visible_to_patient  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_service_ticket_log_bak_20261010` ALTER COLUMN `visible_to_patient` DROP DEFAULT;

-- biz_service_ticket_log_bak_20261010.operator_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_service_ticket_log_bak_20261010` ALTER COLUMN `operator_type` DROP DEFAULT;

-- biz_service_ticket_log_bak_20261010.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_service_ticket_log_bak_20261010` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_service_ticket_log_bak_20261010.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_service_ticket_log_bak_20261010` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_settlement_bill.bill_type  (tinyint, NO)  原 DEFAULT = '2'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `bill_type` DROP DEFAULT;

-- biz_settlement_bill.fee_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `fee_count` DROP DEFAULT;

-- biz_settlement_bill.total_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_settlement_bill.discount_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `discount_amount` DROP DEFAULT;

-- biz_settlement_bill.settlement_mode  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `settlement_mode` DROP DEFAULT;

-- biz_settlement_bill.pool_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `pool_amount` DROP DEFAULT;

-- biz_settlement_bill.account_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `account_amount` DROP DEFAULT;

-- biz_settlement_bill.self_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `self_amount` DROP DEFAULT;

-- biz_settlement_bill.payable_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `payable_amount` DROP DEFAULT;

-- biz_settlement_bill.paid_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `paid_amount` DROP DEFAULT;

-- biz_settlement_bill.refund_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `refund_amount` DROP DEFAULT;

-- biz_settlement_bill.bill_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `bill_status` DROP DEFAULT;

-- biz_settlement_bill.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_settlement_bill.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_settlement_bill` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_settlement_bill_item.discount_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill_item` ALTER COLUMN `discount_amount` DROP DEFAULT;

-- biz_settlement_bill_item.pool_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill_item` ALTER COLUMN `pool_amount` DROP DEFAULT;

-- biz_settlement_bill_item.account_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill_item` ALTER COLUMN `account_amount` DROP DEFAULT;

-- biz_settlement_bill_item.self_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_settlement_bill_item` ALTER COLUMN `self_amount` DROP DEFAULT;

-- biz_settlement_bill_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_settlement_bill_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_settlement_bill_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_settlement_bill_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_settlement_diagnosis.seq_no  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_settlement_diagnosis` ALTER COLUMN `seq_no` DROP DEFAULT;

-- biz_settlement_diagnosis.diag_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_settlement_diagnosis` ALTER COLUMN `diag_type` DROP DEFAULT;

-- biz_settlement_diagnosis.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_settlement_diagnosis` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_settlement_diagnosis.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_settlement_diagnosis` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_settlement_operation.seq_no  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_settlement_operation` ALTER COLUMN `seq_no` DROP DEFAULT;

-- biz_settlement_operation.is_main  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_settlement_operation` ALTER COLUMN `is_main` DROP DEFAULT;

-- biz_settlement_operation.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_settlement_operation` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_settlement_operation.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_settlement_operation` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_shift.cross_day  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_shift` ALTER COLUMN `cross_day` DROP DEFAULT;

-- biz_shift.is_night  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_shift` ALTER COLUMN `is_night` DROP DEFAULT;

-- biz_shift.need_rest_hours  (decimal(4,1), NO)  原 DEFAULT = '0.0'
ALTER TABLE `biz_shift` ALTER COLUMN `need_rest_hours` DROP DEFAULT;

-- biz_shift.late_grace_minutes  (int, NO)  原 DEFAULT = '15'
ALTER TABLE `biz_shift` ALTER COLUMN `late_grace_minutes` DROP DEFAULT;

-- biz_shift.duration_minutes  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_shift` ALTER COLUMN `duration_minutes` DROP DEFAULT;

-- biz_shift.use_scope  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_shift` ALTER COLUMN `use_scope` DROP DEFAULT;

-- biz_shift.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_shift` ALTER COLUMN `status` DROP DEFAULT;

-- biz_shift.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_shift` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_shift.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_shift` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_single_disease_case.is_surgery  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_single_disease_case` ALTER COLUMN `is_surgery` DROP DEFAULT;

-- biz_single_disease_case.death_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_single_disease_case` ALTER COLUMN `death_flag` DROP DEFAULT;

-- biz_single_disease_case.enroll_way  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_single_disease_case` ALTER COLUMN `enroll_way` DROP DEFAULT;

-- biz_single_disease_case.qc_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_single_disease_case` ALTER COLUMN `qc_status` DROP DEFAULT;

-- biz_single_disease_case.report_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_single_disease_case` ALTER COLUMN `report_status` DROP DEFAULT;

-- biz_single_disease_case.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_single_disease_case` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_single_disease_case.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_single_disease_case` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_skin_test.result  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_skin_test` ALTER COLUMN `result` DROP DEFAULT;

-- biz_skin_test.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_skin_test` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_skin_test.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_skin_test` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_staff_attendance.org_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `org_id` DROP DEFAULT;

-- biz_staff_attendance.shift_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `shift_id` DROP DEFAULT;

-- biz_staff_attendance.planned_minutes  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `planned_minutes` DROP DEFAULT;

-- biz_staff_attendance.overtime_minutes  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `overtime_minutes` DROP DEFAULT;

-- biz_staff_attendance.attendance_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `attendance_status` DROP DEFAULT;

-- biz_staff_attendance.confirm_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `confirm_status` DROP DEFAULT;

-- biz_staff_attendance.data_source  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `data_source` DROP DEFAULT;

-- biz_staff_attendance.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `status` DROP DEFAULT;

-- biz_staff_attendance.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_staff_attendance.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_staff_attendance` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_staff_demand.org_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_demand` ALTER COLUMN `org_id` DROP DEFAULT;

-- biz_staff_demand.period_code  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_demand` ALTER COLUMN `period_code` DROP DEFAULT;

-- biz_staff_demand.shift_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_demand` ALTER COLUMN `shift_id` DROP DEFAULT;

-- biz_staff_demand.required_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_demand` ALTER COLUMN `required_count` DROP DEFAULT;

-- biz_staff_demand.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_staff_demand` ALTER COLUMN `status` DROP DEFAULT;

-- biz_staff_demand.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_staff_demand` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_staff_demand.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_staff_demand` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_staff_plan_rule.org_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_staff_plan_rule` ALTER COLUMN `org_type` DROP DEFAULT;

-- biz_staff_plan_rule.org_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_plan_rule` ALTER COLUMN `org_id` DROP DEFAULT;

-- biz_staff_plan_rule.shift_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_plan_rule` ALTER COLUMN `shift_id` DROP DEFAULT;

-- biz_staff_plan_rule.min_staff  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_plan_rule` ALTER COLUMN `min_staff` DROP DEFAULT;

-- biz_staff_plan_rule.max_staff  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_staff_plan_rule` ALTER COLUMN `max_staff` DROP DEFAULT;

-- biz_staff_plan_rule.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_staff_plan_rule` ALTER COLUMN `status` DROP DEFAULT;

-- biz_staff_plan_rule.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_staff_plan_rule` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_staff_plan_rule.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_staff_plan_rule` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_stat_daily.visit_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stat_daily` ALTER COLUMN `visit_count` DROP DEFAULT;

-- biz_stat_daily.charge_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stat_daily` ALTER COLUMN `charge_amount` DROP DEFAULT;

-- biz_stat_daily.prescription_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stat_daily` ALTER COLUMN `prescription_count` DROP DEFAULT;

-- biz_stat_daily.refund_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stat_daily` ALTER COLUMN `refund_amount` DROP DEFAULT;

-- biz_stat_daily.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stat_daily` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_stat_daily.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stat_daily` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_stat_dept.visit_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stat_dept` ALTER COLUMN `visit_count` DROP DEFAULT;

-- biz_stat_dept.charge_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stat_dept` ALTER COLUMN `charge_amount` DROP DEFAULT;

-- biz_stat_dept.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stat_dept` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_stat_dept.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stat_dept` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_stat_report.period_type  (tinyint, NO)  原 DEFAULT = '2'
ALTER TABLE `biz_stat_report` ALTER COLUMN `period_type` DROP DEFAULT;

-- biz_stat_report.discharge_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stat_report` ALTER COLUMN `discharge_count` DROP DEFAULT;

-- biz_stat_report.death_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stat_report` ALTER COLUMN `death_count` DROP DEFAULT;

-- biz_stat_report.operation_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stat_report` ALTER COLUMN `operation_count` DROP DEFAULT;

-- biz_stat_report.level3up_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stat_report` ALTER COLUMN `level3up_count` DROP DEFAULT;

-- biz_stat_report.avg_los_days  (decimal(6,1), NO)  原 DEFAULT = '0.0'
ALTER TABLE `biz_stat_report` ALTER COLUMN `avg_los_days` DROP DEFAULT;

-- biz_stat_report.total_amount  (decimal(14,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stat_report` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_stat_report.status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stat_report` ALTER COLUMN `status` DROP DEFAULT;

-- biz_stat_report.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stat_report` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_stat_report.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stat_report` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_stocktake.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_stocktake` ALTER COLUMN `status` DROP DEFAULT;

-- biz_stocktake.total_items  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stocktake` ALTER COLUMN `total_items` DROP DEFAULT;

-- biz_stocktake.counted_items  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stocktake` ALTER COLUMN `counted_items` DROP DEFAULT;

-- biz_stocktake.diff_items  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stocktake` ALTER COLUMN `diff_items` DROP DEFAULT;

-- biz_stocktake.profit_items  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stocktake` ALTER COLUMN `profit_items` DROP DEFAULT;

-- biz_stocktake.loss_items  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stocktake` ALTER COLUMN `loss_items` DROP DEFAULT;

-- biz_stocktake.diff_quantity  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stocktake` ALTER COLUMN `diff_quantity` DROP DEFAULT;

-- biz_stocktake.diff_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stocktake` ALTER COLUMN `diff_amount` DROP DEFAULT;

-- biz_stocktake.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stocktake` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_stocktake.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stocktake` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_stocktake_item.cost_price  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stocktake_item` ALTER COLUMN `cost_price` DROP DEFAULT;

-- biz_stocktake_item.locked_quantity  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stocktake_item` ALTER COLUMN `locked_quantity` DROP DEFAULT;

-- biz_stocktake_item.diff_quantity  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stocktake_item` ALTER COLUMN `diff_quantity` DROP DEFAULT;

-- biz_stocktake_item.diff_amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_stocktake_item` ALTER COLUMN `diff_amount` DROP DEFAULT;

-- biz_stocktake_item.posted  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_stocktake_item` ALTER COLUMN `posted` DROP DEFAULT;

-- biz_stocktake_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stocktake_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_stocktake_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_stocktake_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_survey_answer.fill_source  (tinyint, NO)  原 DEFAULT = '2'
ALTER TABLE `biz_survey_answer` ALTER COLUMN `fill_source` DROP DEFAULT;

-- biz_survey_answer.anonymous_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_survey_answer` ALTER COLUMN `anonymous_flag` DROP DEFAULT;

-- biz_survey_answer.answer_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_survey_answer` ALTER COLUMN `answer_status` DROP DEFAULT;

-- biz_survey_answer.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_answer` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_survey_answer.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_answer` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_survey_answer_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_answer_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_survey_answer_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_answer_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_survey_dispatch.channel  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_survey_dispatch` ALTER COLUMN `channel` DROP DEFAULT;

-- biz_survey_dispatch.dispatch_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_survey_dispatch` ALTER COLUMN `dispatch_status` DROP DEFAULT;

-- biz_survey_dispatch.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_dispatch` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_survey_dispatch.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_dispatch` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_survey_item.required  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_survey_item` ALTER COLUMN `required` DROP DEFAULT;

-- biz_survey_item.weight  (decimal(5,2), NO)  原 DEFAULT = '1.00'
ALTER TABLE `biz_survey_item` ALTER COLUMN `weight` DROP DEFAULT;

-- biz_survey_item.max_score  (tinyint, NO)  原 DEFAULT = '5'
ALTER TABLE `biz_survey_item` ALTER COLUMN `max_score` DROP DEFAULT;

-- biz_survey_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_survey_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_survey_template.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_survey_template` ALTER COLUMN `status` DROP DEFAULT;

-- biz_survey_template.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_template` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_survey_template.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_survey_template` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_tcm_decoct.dose_count  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_tcm_decoct` ALTER COLUMN `dose_count` DROP DEFAULT;

-- biz_tcm_decoct.herb_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_tcm_decoct` ALTER COLUMN `herb_count` DROP DEFAULT;

-- biz_tcm_decoct.total_grams  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_tcm_decoct` ALTER COLUMN `total_grams` DROP DEFAULT;

-- biz_tcm_decoct.decoct_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_tcm_decoct` ALTER COLUMN `decoct_status` DROP DEFAULT;

-- biz_tcm_decoct.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_tcm_decoct` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_tcm_decoct.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_tcm_decoct` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_tech_auth_override.override_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_tech_auth_override` ALTER COLUMN `override_status` DROP DEFAULT;

-- biz_tech_auth_override.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_tech_auth_override` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_tech_auth_override.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_tech_auth_override` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_tele_consult.consult_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_tele_consult` ALTER COLUMN `consult_type` DROP DEFAULT;

-- biz_tele_consult.is_urgent  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_tele_consult` ALTER COLUMN `is_urgent` DROP DEFAULT;

-- biz_tele_consult.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_tele_consult` ALTER COLUMN `status` DROP DEFAULT;

-- biz_tele_consult.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_tele_consult` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_tele_consult.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_tele_consult` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_transfusion_apply.bag_count  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `bag_count` DROP DEFAULT;

-- biz_transfusion_apply.approve_level  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `approve_level` DROP DEFAULT;

-- biz_transfusion_apply.approve_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `approve_status` DROP DEFAULT;

-- biz_transfusion_apply.approve_makeup  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `approve_makeup` DROP DEFAULT;

-- biz_transfusion_apply.is_emergency  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- biz_transfusion_apply.crossmatch_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `crossmatch_status` DROP DEFAULT;

-- biz_transfusion_apply.has_reaction  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `has_reaction` DROP DEFAULT;

-- biz_transfusion_apply.transfusion_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `transfusion_status` DROP DEFAULT;

-- biz_transfusion_apply.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_transfusion_apply.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_transfusion_apply` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_transfusion_approve.is_makeup  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_transfusion_approve` ALTER COLUMN `is_makeup` DROP DEFAULT;

-- biz_transfusion_approve.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_transfusion_approve` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_transfusion_approve.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_transfusion_approve` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_transfusion_bag.bag_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_transfusion_bag` ALTER COLUMN `bag_status` DROP DEFAULT;

-- biz_transfusion_bag.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_transfusion_bag` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_transfusion_bag.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_transfusion_bag` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_treatment_apply.apply_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_treatment_apply` ALTER COLUMN `apply_time` DROP DEFAULT;

-- biz_treatment_apply.apply_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_treatment_apply` ALTER COLUMN `apply_status` DROP DEFAULT;

-- biz_treatment_apply.total_times  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_treatment_apply` ALTER COLUMN `total_times` DROP DEFAULT;

-- biz_treatment_apply.done_times  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_treatment_apply` ALTER COLUMN `done_times` DROP DEFAULT;

-- biz_treatment_apply.interval_days  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_treatment_apply` ALTER COLUMN `interval_days` DROP DEFAULT;

-- biz_treatment_apply.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_treatment_apply` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_treatment_apply.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_treatment_apply` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_treatment_record.execute_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_treatment_record` ALTER COLUMN `execute_time` DROP DEFAULT;

-- biz_treatment_record.record_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_treatment_record` ALTER COLUMN `record_status` DROP DEFAULT;

-- biz_treatment_record.exec_seq  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_treatment_record` ALTER COLUMN `exec_seq` DROP DEFAULT;

-- biz_treatment_record.exec_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_treatment_record` ALTER COLUMN `exec_status` DROP DEFAULT;

-- biz_treatment_record.charge_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_treatment_record` ALTER COLUMN `charge_status` DROP DEFAULT;

-- biz_treatment_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_treatment_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_treatment_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_treatment_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_triage_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_triage_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_triage_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_triage_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_triage_rule.weight  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_triage_rule` ALTER COLUMN `weight` DROP DEFAULT;

-- biz_triage_rule.urgent_flag  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_triage_rule` ALTER COLUMN `urgent_flag` DROP DEFAULT;

-- biz_triage_rule.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_triage_rule` ALTER COLUMN `status` DROP DEFAULT;

-- biz_triage_rule.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_triage_rule` ALTER COLUMN `sort_order` DROP DEFAULT;

-- biz_triage_rule.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_triage_rule` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_triage_rule.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_triage_rule` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_tsa_token.algo  (varchar(32), NO)  原 DEFAULT = 'SHA256withRSA'
ALTER TABLE `biz_tsa_token` ALTER COLUMN `algo` DROP DEFAULT;

-- biz_tsa_token.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_tsa_token` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_tsa_token.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_tsa_token` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ultrasound_measure.abnormal_flag  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_ultrasound_measure` ALTER COLUMN `abnormal_flag` DROP DEFAULT;

-- biz_ultrasound_measure.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `biz_ultrasound_measure` ALTER COLUMN `sort_order` DROP DEFAULT;

-- biz_ultrasound_measure.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ultrasound_measure` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_ultrasound_measure.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ultrasound_measure` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ultrasound_record.us_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_ultrasound_record` ALTER COLUMN `us_type` DROP DEFAULT;

-- biz_ultrasound_record.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `biz_ultrasound_record` ALTER COLUMN `status` DROP DEFAULT;

-- biz_ultrasound_record.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ultrasound_record` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_ultrasound_record.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ultrasound_record` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_visit.total_amount  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `biz_visit` ALTER COLUMN `total_amount` DROP DEFAULT;

-- biz_visit.visit_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_visit` ALTER COLUMN `visit_status` DROP DEFAULT;

-- biz_visit.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_visit` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_visit.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_visit` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_vte_event.onset_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_vte_event` ALTER COLUMN `onset_type` DROP DEFAULT;

-- biz_vte_event.drug_prevent_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_vte_event` ALTER COLUMN `drug_prevent_flag` DROP DEFAULT;

-- biz_vte_event.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_vte_event` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_vte_event.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_vte_event` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_vte_prevent.execute_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_vte_prevent` ALTER COLUMN `execute_status` DROP DEFAULT;

-- biz_vte_prevent.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_vte_prevent` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_vte_prevent.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_vte_prevent` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_vte_stats.scope_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `scope_type` DROP DEFAULT;

-- biz_vte_stats.discharge_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `discharge_count` DROP DEFAULT;

-- biz_vte_stats.assessed_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `assessed_count` DROP DEFAULT;

-- biz_vte_stats.assess_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `assess_rate` DROP DEFAULT;

-- biz_vte_stats.high_risk_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `high_risk_count` DROP DEFAULT;

-- biz_vte_stats.high_risk_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `high_risk_rate` DROP DEFAULT;

-- biz_vte_stats.prevent_done_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `prevent_done_count` DROP DEFAULT;

-- biz_vte_stats.prevent_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `prevent_rate` DROP DEFAULT;

-- biz_vte_stats.vte_event_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `vte_event_count` DROP DEFAULT;

-- biz_vte_stats.vte_incidence_rate  (decimal(6,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `vte_incidence_rate` DROP DEFAULT;

-- biz_vte_stats.bleed_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `bleed_count` DROP DEFAULT;

-- biz_vte_stats.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_vte_stats.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_vte_stats` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ward_dispense.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_ward_dispense` ALTER COLUMN `status` DROP DEFAULT;

-- biz_ward_dispense.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ward_dispense` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_ward_dispense.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ward_dispense` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_ward_dispense_item.dispense_seq  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_ward_dispense_item` ALTER COLUMN `dispense_seq` DROP DEFAULT;

-- biz_ward_dispense_item.price  (decimal(12,4), NO)  原 DEFAULT = '0.0000'
ALTER TABLE `biz_ward_dispense_item` ALTER COLUMN `price` DROP DEFAULT;

-- biz_ward_dispense_item.amount  (decimal(12,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `biz_ward_dispense_item` ALTER COLUMN `amount` DROP DEFAULT;

-- biz_ward_dispense_item.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_ward_dispense_item` ALTER COLUMN `status` DROP DEFAULT;

-- biz_ward_dispense_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ward_dispense_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_ward_dispense_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_ward_dispense_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_yb_catalog.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_yb_catalog` ALTER COLUMN `status` DROP DEFAULT;

-- biz_yb_catalog.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_catalog` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_yb_catalog.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_catalog` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_yb_chronic_catalog.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_yb_chronic_catalog` ALTER COLUMN `status` DROP DEFAULT;

-- biz_yb_chronic_catalog.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_chronic_catalog` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_yb_chronic_catalog.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_chronic_catalog` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_yb_chronic_reg.reg_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_yb_chronic_reg` ALTER COLUMN `reg_status` DROP DEFAULT;

-- biz_yb_chronic_reg.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_chronic_reg` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_yb_chronic_reg.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_chronic_reg` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_yb_deduct_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_deduct_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_yb_deduct_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_deduct_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_yb_deduct_notice.deduct_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_yb_deduct_notice` ALTER COLUMN `deduct_status` DROP DEFAULT;

-- biz_yb_deduct_notice.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_deduct_notice` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_yb_deduct_notice.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_deduct_notice` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_yb_inspection.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `biz_yb_inspection` ALTER COLUMN `status` DROP DEFAULT;

-- biz_yb_inspection.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_inspection` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_yb_inspection.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_inspection` ALTER COLUMN `update_time` DROP DEFAULT;

-- biz_yb_mapping.match_type  (tinyint, NO)  原 DEFAULT = '2'
ALTER TABLE `biz_yb_mapping` ALTER COLUMN `match_type` DROP DEFAULT;

-- biz_yb_mapping.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_mapping` ALTER COLUMN `create_time` DROP DEFAULT;

-- biz_yb_mapping.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `biz_yb_mapping` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_ai_call_log.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_ai_call_log` ALTER COLUMN `status` DROP DEFAULT;

-- sys_ai_call_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_ai_call_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_ai_call_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_ai_call_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_alert_rule.notify_channel  (varchar(32), YES)  原 DEFAULT = 'system'
ALTER TABLE `sys_alert_rule` ALTER COLUMN `notify_channel` DROP DEFAULT;

-- sys_alert_rule.is_active  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_alert_rule` ALTER COLUMN `is_active` DROP DEFAULT;

-- sys_alert_rule.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_alert_rule` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_alert_rule.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_alert_rule` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_attachment.file_size  (bigint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_attachment` ALTER COLUMN `file_size` DROP DEFAULT;

-- sys_attachment.upload_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_attachment` ALTER COLUMN `upload_time` DROP DEFAULT;

-- sys_attachment.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_attachment` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_attachment.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_attachment` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_audit_log.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_audit_log` ALTER COLUMN `status` DROP DEFAULT;

-- sys_audit_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_audit_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_audit_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_audit_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_bed.bed_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_bed` ALTER COLUMN `bed_status` DROP DEFAULT;

-- sys_bed.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_bed` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_bed.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_bed` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_checkup_package.gender_limit  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_checkup_package` ALTER COLUMN `gender_limit` DROP DEFAULT;

-- sys_checkup_package.price  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `sys_checkup_package` ALTER COLUMN `price` DROP DEFAULT;

-- sys_checkup_package.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_checkup_package` ALTER COLUMN `status` DROP DEFAULT;

-- sys_checkup_package.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_checkup_package` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_checkup_package.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_checkup_package` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_checkup_package_item.item_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_checkup_package_item` ALTER COLUMN `item_type` DROP DEFAULT;

-- sys_checkup_package_item.amount  (decimal(10,2), NO)  原 DEFAULT = '0.00'
ALTER TABLE `sys_checkup_package_item` ALTER COLUMN `amount` DROP DEFAULT;

-- sys_checkup_package_item.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_checkup_package_item` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_checkup_package_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_checkup_package_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_checkup_package_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_checkup_package_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_clinic_room.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_clinic_room` ALTER COLUMN `status` DROP DEFAULT;

-- sys_clinic_room.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_clinic_room` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_clinic_room.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_clinic_room` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_config.config_type  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_config` ALTER COLUMN `config_type` DROP DEFAULT;

-- sys_config.is_system  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_config` ALTER COLUMN `is_system` DROP DEFAULT;

-- sys_config.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_config` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_config.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_config` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_consumable.category  (tinyint, YES)  原 DEFAULT = '5'
ALTER TABLE `sys_consumable` ALTER COLUMN `category` DROP DEFAULT;

-- sys_consumable.retail_price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `sys_consumable` ALTER COLUMN `retail_price` DROP DEFAULT;

-- sys_consumable.is_high_value  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_consumable` ALTER COLUMN `is_high_value` DROP DEFAULT;

-- sys_consumable.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_consumable` ALTER COLUMN `status` DROP DEFAULT;

-- sys_consumable.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_consumable` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_consumable.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_consumable` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_department.parent_id  (bigint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_department` ALTER COLUMN `parent_id` DROP DEFAULT;

-- sys_department.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_department` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_department.is_open  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_department` ALTER COLUMN `is_open` DROP DEFAULT;

-- sys_department.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_department` ALTER COLUMN `status` DROP DEFAULT;

-- sys_department.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_department` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_department.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_department` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_diagnosis.diagnosis_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_diagnosis` ALTER COLUMN `diagnosis_type` DROP DEFAULT;

-- sys_diagnosis.parent_id  (bigint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_diagnosis` ALTER COLUMN `parent_id` DROP DEFAULT;

-- sys_diagnosis.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_diagnosis` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_diagnosis.is_common  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_diagnosis` ALTER COLUMN `is_common` DROP DEFAULT;

-- sys_diagnosis.is_notifiable  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_diagnosis` ALTER COLUMN `is_notifiable` DROP DEFAULT;

-- sys_diagnosis.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_diagnosis` ALTER COLUMN `status` DROP DEFAULT;

-- sys_diagnosis.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_diagnosis` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_diagnosis.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_diagnosis` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_dict_data.dict_sort  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_dict_data` ALTER COLUMN `dict_sort` DROP DEFAULT;

-- sys_dict_data.is_default  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_dict_data` ALTER COLUMN `is_default` DROP DEFAULT;

-- sys_dict_data.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_dict_data` ALTER COLUMN `status` DROP DEFAULT;

-- sys_dict_data.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_dict_data` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_dict_data.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_dict_data` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_dict_data.dict_source  (tinyint, YES)  原 DEFAULT = '2'
ALTER TABLE `sys_dict_data` ALTER COLUMN `dict_source` DROP DEFAULT;

-- sys_dict_type.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_dict_type` ALTER COLUMN `status` DROP DEFAULT;

-- sys_dict_type.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_dict_type` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_dict_type.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_dict_type` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_dict_type.dict_source  (tinyint, YES)  原 DEFAULT = '2'
ALTER TABLE `sys_dict_type` ALTER COLUMN `dict_source` DROP DEFAULT;

-- sys_drg_adrg.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_drg_adrg` ALTER COLUMN `status` DROP DEFAULT;

-- sys_drg_ccmcc.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_drg_ccmcc` ALTER COLUMN `status` DROP DEFAULT;

-- sys_drg_group.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_drg_group` ALTER COLUMN `status` DROP DEFAULT;

-- sys_drg_group.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drg_group` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_drg_group.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drg_group` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_drg_mdc.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_drg_mdc` ALTER COLUMN `status` DROP DEFAULT;

-- sys_drg_set.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_drg_set` ALTER COLUMN `status` DROP DEFAULT;

-- sys_drug.drug_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_drug` ALTER COLUMN `drug_type` DROP DEFAULT;

-- sys_drug.is_trace_required  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_drug` ALTER COLUMN `is_trace_required` DROP DEFAULT;

-- sys_drug.price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `sys_drug` ALTER COLUMN `price` DROP DEFAULT;

-- sys_drug.cost_price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `sys_drug` ALTER COLUMN `cost_price` DROP DEFAULT;

-- sys_drug.retail_price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `sys_drug` ALTER COLUMN `retail_price` DROP DEFAULT;

-- sys_drug.is_medical_insurance  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_drug` ALTER COLUMN `is_medical_insurance` DROP DEFAULT;

-- sys_drug.shelf_life  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_drug` ALTER COLUMN `shelf_life` DROP DEFAULT;

-- sys_drug.is_skin_test  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_drug` ALTER COLUMN `is_skin_test` DROP DEFAULT;

-- sys_drug.is_cold_chain  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_drug` ALTER COLUMN `is_cold_chain` DROP DEFAULT;

-- sys_drug.special_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_drug` ALTER COLUMN `special_flag` DROP DEFAULT;

-- sys_drug.antibiotic_level  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_drug` ALTER COLUMN `antibiotic_level` DROP DEFAULT;

-- sys_drug.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_drug` ALTER COLUMN `status` DROP DEFAULT;

-- sys_drug.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drug` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_drug.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drug` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_drug_dose_limit.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_drug_dose_limit` ALTER COLUMN `status` DROP DEFAULT;

-- sys_drug_dose_limit.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drug_dose_limit` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_drug_dose_limit.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drug_dose_limit` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_drug_interaction.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_drug_interaction` ALTER COLUMN `status` DROP DEFAULT;

-- sys_drug_interaction.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drug_interaction` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_drug_interaction.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drug_interaction` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_drug_price_history.change_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drug_price_history` ALTER COLUMN `change_time` DROP DEFAULT;

-- sys_drug_price_history.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drug_price_history` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_drug_price_history.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_drug_price_history` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_employee.emp_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_employee` ALTER COLUMN `emp_type` DROP DEFAULT;

-- sys_employee.gender  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_employee` ALTER COLUMN `gender` DROP DEFAULT;

-- sys_employee.is_expert  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_employee` ALTER COLUMN `is_expert` DROP DEFAULT;

-- sys_employee.expert_price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `sys_employee` ALTER COLUMN `expert_price` DROP DEFAULT;

-- sys_employee.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_employee` ALTER COLUMN `status` DROP DEFAULT;

-- sys_employee.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_employee` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_employee.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_employee` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_employee_post.is_primary  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_employee_post` ALTER COLUMN `is_primary` DROP DEFAULT;

-- sys_employee_post.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_employee_post` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_employee_post.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_employee_post` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_employee_qualification.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_employee_qualification` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_employee_qualification.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_employee_qualification` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_employee_tech_auth.auth_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_employee_tech_auth` ALTER COLUMN `auth_type` DROP DEFAULT;

-- sys_employee_tech_auth.auth_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_employee_tech_auth` ALTER COLUMN `auth_status` DROP DEFAULT;

-- sys_employee_tech_auth.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_employee_tech_auth` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_employee_tech_auth.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_employee_tech_auth` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_equipment.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_equipment` ALTER COLUMN `status` DROP DEFAULT;

-- sys_equipment.maintain_cycle_days  (int, YES)  原 DEFAULT = '365'
ALTER TABLE `sys_equipment` ALTER COLUMN `maintain_cycle_days` DROP DEFAULT;

-- sys_equipment.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_equipment` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_equipment.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_equipment` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_faq.hot_flag  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_faq` ALTER COLUMN `hot_flag` DROP DEFAULT;

-- sys_faq.view_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_faq` ALTER COLUMN `view_count` DROP DEFAULT;

-- sys_faq.helpful_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_faq` ALTER COLUMN `helpful_count` DROP DEFAULT;

-- sys_faq.useless_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_faq` ALTER COLUMN `useless_count` DROP DEFAULT;

-- sys_faq.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_faq` ALTER COLUMN `status` DROP DEFAULT;

-- sys_faq.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_faq` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_faq.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_faq` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_faq.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_faq` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_field_change_log.change_type  (varchar(16), NO)  原 DEFAULT = 'UPDATE'
ALTER TABLE `sys_field_change_log` ALTER COLUMN `change_type` DROP DEFAULT;

-- sys_field_change_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_field_change_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_field_change_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_field_change_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_icd10.code_std  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_icd10` ALTER COLUMN `code_std` DROP DEFAULT;

-- sys_icd10.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_icd10` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_icd10.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_icd10` ALTER COLUMN `status` DROP DEFAULT;

-- sys_icd10.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_icd10` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_icd10.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_icd10` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_icd9cm3.code_std  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_icd9cm3` ALTER COLUMN `code_std` DROP DEFAULT;

-- sys_icd9cm3.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_icd9cm3` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_icd9cm3.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_icd9cm3` ALTER COLUMN `status` DROP DEFAULT;

-- sys_icd9cm3.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_icd9cm3` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_icd9cm3.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_icd9cm3` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_imaging_plain_item.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_imaging_plain_item` ALTER COLUMN `status` DROP DEFAULT;

-- sys_imaging_plain_item.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_imaging_plain_item` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_imaging_plain_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_imaging_plain_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_imaging_plain_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_imaging_plain_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_infectious_disease.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_infectious_disease` ALTER COLUMN `status` DROP DEFAULT;

-- sys_infectious_disease.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_infectious_disease` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_infectious_disease.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_infectious_disease` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_inspection_item.item_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_inspection_item` ALTER COLUMN `item_type` DROP DEFAULT;

-- sys_inspection_item.price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `sys_inspection_item` ALTER COLUMN `price` DROP DEFAULT;

-- sys_inspection_item.duration  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_inspection_item` ALTER COLUMN `duration` DROP DEFAULT;

-- sys_inspection_item.is_emergency  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_inspection_item` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- sys_inspection_item.is_appointment  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_inspection_item` ALTER COLUMN `is_appointment` DROP DEFAULT;

-- sys_inspection_item.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_inspection_item` ALTER COLUMN `status` DROP DEFAULT;

-- sys_inspection_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_inspection_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_inspection_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_inspection_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_insurance_policy.self_pay_ratio  (decimal(5,2), YES)  原 DEFAULT = '10.00'
ALTER TABLE `sys_insurance_policy` ALTER COLUMN `self_pay_ratio` DROP DEFAULT;

-- sys_insurance_policy.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_insurance_policy` ALTER COLUMN `status` DROP DEFAULT;

-- sys_insurance_policy.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_insurance_policy` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_insurance_policy.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_insurance_policy` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_knowledge_chunk.doc_title  (varchar(200), YES)  原 DEFAULT = ''
ALTER TABLE `sys_knowledge_chunk` ALTER COLUMN `doc_title` DROP DEFAULT;

-- sys_knowledge_chunk.category  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `sys_knowledge_chunk` ALTER COLUMN `category` DROP DEFAULT;

-- sys_knowledge_chunk.chunk_index  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_knowledge_chunk` ALTER COLUMN `chunk_index` DROP DEFAULT;

-- sys_knowledge_chunk.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_knowledge_chunk` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_knowledge_chunk.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_knowledge_chunk` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_knowledge_chunk.remark  (varchar(500), YES)  原 DEFAULT = ''
ALTER TABLE `sys_knowledge_chunk` ALTER COLUMN `remark` DROP DEFAULT;

-- sys_knowledge_doc.category  (varchar(50), YES)  原 DEFAULT = ''
ALTER TABLE `sys_knowledge_doc` ALTER COLUMN `category` DROP DEFAULT;

-- sys_knowledge_doc.source_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_knowledge_doc` ALTER COLUMN `source_type` DROP DEFAULT;

-- sys_knowledge_doc.chunk_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_knowledge_doc` ALTER COLUMN `chunk_count` DROP DEFAULT;

-- sys_knowledge_doc.status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_knowledge_doc` ALTER COLUMN `status` DROP DEFAULT;

-- sys_knowledge_doc.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_knowledge_doc` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_knowledge_doc.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_knowledge_doc` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_knowledge_doc.remark  (varchar(500), YES)  原 DEFAULT = ''
ALTER TABLE `sys_knowledge_doc` ALTER COLUMN `remark` DROP DEFAULT;

-- sys_lab_plain_item.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_lab_plain_item` ALTER COLUMN `status` DROP DEFAULT;

-- sys_lab_plain_item.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_lab_plain_item` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_lab_plain_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_lab_plain_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_lab_plain_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_lab_plain_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_laboratory_item.item_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_laboratory_item` ALTER COLUMN `item_type` DROP DEFAULT;

-- sys_laboratory_item.price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `sys_laboratory_item` ALTER COLUMN `price` DROP DEFAULT;

-- sys_laboratory_item.duration  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_laboratory_item` ALTER COLUMN `duration` DROP DEFAULT;

-- sys_laboratory_item.is_emergency  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_laboratory_item` ALTER COLUMN `is_emergency` DROP DEFAULT;

-- sys_laboratory_item.is_fasting  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_laboratory_item` ALTER COLUMN `is_fasting` DROP DEFAULT;

-- sys_laboratory_item.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_laboratory_item` ALTER COLUMN `status` DROP DEFAULT;

-- sys_laboratory_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_laboratory_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_laboratory_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_laboratory_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_laboratory_item_detail.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_laboratory_item_detail` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_laboratory_item_detail.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_laboratory_item_detail` ALTER COLUMN `status` DROP DEFAULT;

-- sys_laboratory_item_detail.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_laboratory_item_detail` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_laboratory_item_detail.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_laboratory_item_detail` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_login_log.login_status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_login_log` ALTER COLUMN `login_status` DROP DEFAULT;

-- sys_login_log.login_time  (datetime, YES)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_login_log` ALTER COLUMN `login_time` DROP DEFAULT;

-- sys_login_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_login_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_login_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_login_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_menu.parent_id  (bigint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_menu` ALTER COLUMN `parent_id` DROP DEFAULT;

-- sys_menu.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_menu` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_menu.menu_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_menu` ALTER COLUMN `menu_type` DROP DEFAULT;

-- sys_menu.is_frame  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_menu` ALTER COLUMN `is_frame` DROP DEFAULT;

-- sys_menu.is_cache  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_menu` ALTER COLUMN `is_cache` DROP DEFAULT;

-- sys_menu.is_visible  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_menu` ALTER COLUMN `is_visible` DROP DEFAULT;

-- sys_menu.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_menu` ALTER COLUMN `status` DROP DEFAULT;

-- sys_menu.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_menu` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_menu.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_menu` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_message.severity  (varchar(16), NO)  原 DEFAULT = 'info'
ALTER TABLE `sys_message` ALTER COLUMN `severity` DROP DEFAULT;

-- sys_message.send_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_message` ALTER COLUMN `send_status` DROP DEFAULT;

-- sys_message.read_status  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_message` ALTER COLUMN `read_status` DROP DEFAULT;

-- sys_message.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_message` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_message.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_message` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_nursing_qc_item.full_score  (decimal(5,1), NO)  原 DEFAULT = '0.0'
ALTER TABLE `sys_nursing_qc_item` ALTER COLUMN `full_score` DROP DEFAULT;

-- sys_nursing_qc_item.key_flag  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_nursing_qc_item` ALTER COLUMN `key_flag` DROP DEFAULT;

-- sys_nursing_qc_item.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_nursing_qc_item` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_nursing_qc_item.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_nursing_qc_item` ALTER COLUMN `status` DROP DEFAULT;

-- sys_nursing_qc_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_nursing_qc_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_nursing_qc_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_nursing_qc_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_oper_log.business_type  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_oper_log` ALTER COLUMN `business_type` DROP DEFAULT;

-- sys_oper_log.status  (tinyint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_oper_log` ALTER COLUMN `status` DROP DEFAULT;

-- sys_oper_log.oper_time  (datetime, YES)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_oper_log` ALTER COLUMN `oper_time` DROP DEFAULT;

-- sys_oper_log.cost_time  (bigint, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_oper_log` ALTER COLUMN `cost_time` DROP DEFAULT;

-- sys_oper_log.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_oper_log` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_oper_log.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_oper_log` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_operation_room.sort_order  (int, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_operation_room` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_operation_room.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_operation_room` ALTER COLUMN `status` DROP DEFAULT;

-- sys_operation_room.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_operation_room` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_operation_room.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_operation_room` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_patient_tag.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_patient_tag` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_patient_tag.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_patient_tag` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_price_change_history.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_price_change_history` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_price_change_history.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_price_change_history` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_role.role_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_role` ALTER COLUMN `role_type` DROP DEFAULT;

-- sys_role.data_scope  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_role` ALTER COLUMN `data_scope` DROP DEFAULT;

-- sys_role.sort_order  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_role` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_role.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_role` ALTER COLUMN `status` DROP DEFAULT;

-- sys_role.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_role` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_role.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_role` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_role_menu.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_role_menu` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_role_menu.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_role_menu` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_role_menu_bak_20261010_full.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_role_menu_bak_20261010_full` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_role_menu_bak_20261010_full.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_role_menu_bak_20261010_full` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_service_trace.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_service_trace` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_service_trace.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_service_trace` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_sign_cert.key_algo  (varchar(16), NO)  原 DEFAULT = 'RSA2048'
ALTER TABLE `sys_sign_cert` ALTER COLUMN `key_algo` DROP DEFAULT;

-- sys_sign_cert.digest_algo  (varchar(16), NO)  原 DEFAULT = 'SHA256'
ALTER TABLE `sys_sign_cert` ALTER COLUMN `digest_algo` DROP DEFAULT;

-- sys_sign_cert.sign_algo  (varchar(32), NO)  原 DEFAULT = 'SHA256withRSA'
ALTER TABLE `sys_sign_cert` ALTER COLUMN `sign_algo` DROP DEFAULT;

-- sys_sign_cert.issued_mode  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_sign_cert` ALTER COLUMN `issued_mode` DROP DEFAULT;

-- sys_sign_cert.cert_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_sign_cert` ALTER COLUMN `cert_status` DROP DEFAULT;

-- sys_sign_cert.sign_count  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_sign_cert` ALTER COLUMN `sign_count` DROP DEFAULT;

-- sys_sign_cert.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_sign_cert` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_sign_cert.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_sign_cert` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_single_disease.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_single_disease` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_single_disease.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_single_disease` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_supplier.rating  (tinyint, YES)  原 DEFAULT = '3'
ALTER TABLE `sys_supplier` ALTER COLUMN `rating` DROP DEFAULT;

-- sys_supplier.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_supplier` ALTER COLUMN `status` DROP DEFAULT;

-- sys_supplier.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_supplier` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_supplier.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_supplier` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_treatment_item.item_type  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_treatment_item` ALTER COLUMN `item_type` DROP DEFAULT;

-- sys_treatment_item.price  (decimal(10,2), YES)  原 DEFAULT = '0.00'
ALTER TABLE `sys_treatment_item` ALTER COLUMN `price` DROP DEFAULT;

-- sys_treatment_item.duration  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_treatment_item` ALTER COLUMN `duration` DROP DEFAULT;

-- sys_treatment_item.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_treatment_item` ALTER COLUMN `status` DROP DEFAULT;

-- sys_treatment_item.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_treatment_item` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_treatment_item.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_treatment_item` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_tsa_server.tsa_status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_tsa_server` ALTER COLUMN `tsa_status` DROP DEFAULT;

-- sys_tsa_server.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_tsa_server` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_tsa_server.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_tsa_server` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_user.user_type  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_user` ALTER COLUMN `user_type` DROP DEFAULT;

-- sys_user.login_count  (int, YES)  原 DEFAULT = '0'
ALTER TABLE `sys_user` ALTER COLUMN `login_count` DROP DEFAULT;

-- sys_user.status  (tinyint, YES)  原 DEFAULT = '1'
ALTER TABLE `sys_user` ALTER COLUMN `status` DROP DEFAULT;

-- sys_user.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_user` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_user.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_user` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_ward.total_beds  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_ward` ALTER COLUMN `total_beds` DROP DEFAULT;

-- sys_ward.occupied_beds  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_ward` ALTER COLUMN `occupied_beds` DROP DEFAULT;

-- sys_ward.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_ward` ALTER COLUMN `status` DROP DEFAULT;

-- sys_ward.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_ward` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_ward.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_ward` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_workbench_layout.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_workbench_layout` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_workbench_layout.visible  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_workbench_layout` ALTER COLUMN `visible` DROP DEFAULT;

-- sys_workbench_layout.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_workbench_layout` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_workbench_layout.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_workbench_layout` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_workbench_role.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_workbench_role` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_workbench_role.visible  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_workbench_role` ALTER COLUMN `visible` DROP DEFAULT;

-- sys_workbench_role.landing_scope  (tinyint, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_workbench_role` ALTER COLUMN `landing_scope` DROP DEFAULT;

-- sys_workbench_role.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_workbench_role` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_workbench_role.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_workbench_role` ALTER COLUMN `update_time` DROP DEFAULT;

-- sys_workbench_widget.area  (varchar(16), NO)  原 DEFAULT = 'domain'
ALTER TABLE `sys_workbench_widget` ALTER COLUMN `area` DROP DEFAULT;

-- sys_workbench_widget.default_span  (int, NO)  原 DEFAULT = '6'
ALTER TABLE `sys_workbench_widget` ALTER COLUMN `default_span` DROP DEFAULT;

-- sys_workbench_widget.sort_order  (int, NO)  原 DEFAULT = '0'
ALTER TABLE `sys_workbench_widget` ALTER COLUMN `sort_order` DROP DEFAULT;

-- sys_workbench_widget.status  (tinyint, NO)  原 DEFAULT = '1'
ALTER TABLE `sys_workbench_widget` ALTER COLUMN `status` DROP DEFAULT;

-- sys_workbench_widget.create_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_workbench_widget` ALTER COLUMN `create_time` DROP DEFAULT;

-- sys_workbench_widget.update_time  (datetime, NO)  原 DEFAULT = 'CURRENT_TIMESTAMP'
ALTER TABLE `sys_workbench_widget` ALTER COLUMN `update_time` DROP DEFAULT;
