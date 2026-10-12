-- ============================================================
-- 增量变更：del_flag 统一为 NOT NULL DEFAULT '0'（补 NOT NULL）
-- 依据 AGENTS.md §25：唯一允许带 DEFAULT 的 NOT NULL 列就是 del_flag，且必须是 NOT NULL
-- 目标库：hn_biz_his @ 192.168.88.132:3306
-- 影响表数：97（执行前已实测 del_flag IS NULL 的行数 = 0，不触数据）
-- ============================================================

-- biz_ai_draft_diff.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_ai_draft_diff` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_appoint_info.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_appoint_info` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_blood_crossmatch.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_blood_crossmatch` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_blood_inventory.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_blood_inventory` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_blood_stock_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_blood_stock_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_clinic_source.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_clinic_source` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_clinic_source_slot.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_clinic_source_slot` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_clinic_source_slot_template.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_clinic_source_slot_template` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_clinic_source_template.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_clinic_source_template` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_consumable_consume.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_consumable_consume` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_consumable_stock.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_consumable_stock` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_consumable_stock_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_consumable_stock_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_diag_template.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_diag_template` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_drug_inbound.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_drug_inbound` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_drug_inbound_detail.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_drug_inbound_detail` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_drug_outbound.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_drug_outbound` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_drug_outbound_detail.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_drug_outbound_detail` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_drug_package.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_drug_package` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_drug_package_detail.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_drug_package_detail` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_drug_stock.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_drug_stock` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_drug_stock_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_drug_stock_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_duty_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_duty_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_emergency.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_emergency` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_endoscopy_record.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_endoscopy_record` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_exam_appointment.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_exam_appointment` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_exam_device.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_exam_device` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_exam_device_item.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_exam_device_item` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_exam_slot.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_exam_slot` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_inspection_apply.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_inspection_apply` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_inspection_record.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_inspection_record` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_inspection_template.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_inspection_template` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_lab_result.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_lab_result` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_laboratory_apply.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_laboratory_apply` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_laboratory_record.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_laboratory_record` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_laboratory_template.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_laboratory_template` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_lis_eqa_compare.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_lis_eqa_compare` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_lis_eqa_plan.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_lis_eqa_plan` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_lis_eqa_sample.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_lis_eqa_sample` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_lis_qc_plan.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_lis_qc_plan` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_lis_qc_record.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_lis_qc_record` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_medical_record.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_medical_record` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_medical_record_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_medical_record_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_narcotic_register.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_narcotic_register` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_pathology_block.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_pathology_block` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_pathology_order.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_pathology_order` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_patient.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_patient` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_patient_allergy.del_flag  原 tinyint(1) NULL DEFAULT '0'
ALTER TABLE `biz_patient_allergy` MODIFY COLUMN `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_patient_contact.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_patient_contact` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_patient_family_history.del_flag  原 tinyint(1) NULL DEFAULT '0'
ALTER TABLE `biz_patient_family_history` MODIFY COLUMN `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_patient_guardian.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_patient_guardian` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_patient_medication_history.del_flag  原 tinyint(1) NULL DEFAULT '0'
ALTER TABLE `biz_patient_medication_history` MODIFY COLUMN `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_patient_past_disease.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_patient_past_disease` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_patient_surgery_history.del_flag  原 tinyint(1) NULL DEFAULT '0'
ALTER TABLE `biz_patient_surgery_history` MODIFY COLUMN `del_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_prescription.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_prescription` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_prescription_detail.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_prescription_detail` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_previsit_record.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_previsit_record` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_purchase_order.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_purchase_order` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_queue.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_queue` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_report.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_report` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_rx_template.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_rx_template` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_rx_template_detail.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_rx_template_detail` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_service_message.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_service_message` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_service_message_bak_20261010.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_service_message_bak_20261010` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_service_ticket_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_service_ticket_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_service_ticket_log_bak_20261010.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_service_ticket_log_bak_20261010` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_shift.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_shift` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_triage_record.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_triage_record` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_triage_rule.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_triage_rule` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_ultrasound_measure.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_ultrasound_measure` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- biz_ultrasound_record.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `biz_ultrasound_record` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_audit_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_audit_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_clinic_room.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_clinic_room` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_consumable.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_consumable` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_department.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_department` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_diagnosis.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_diagnosis` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_dict_data.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_dict_data` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_dict_type.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_dict_type` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_drug.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_drug` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_employee.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_employee` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_faq.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_faq` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_field_change_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_field_change_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_icd10.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_icd10` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_imaging_plain_item.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_imaging_plain_item` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_inspection_item.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_inspection_item` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_knowledge_chunk.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_knowledge_chunk` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_knowledge_doc.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_knowledge_doc` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_lab_plain_item.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_lab_plain_item` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_laboratory_item.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_laboratory_item` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_laboratory_item_detail.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_laboratory_item_detail` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_login_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_login_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_menu.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_menu` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_oper_log.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_oper_log` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_role.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_role` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_service_trace.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_service_trace` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_supplier.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_supplier` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_treatment_item.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_treatment_item` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';

-- sys_user.del_flag  原 tinyint NULL DEFAULT '0'
ALTER TABLE `sys_user` MODIFY COLUMN `del_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志（0-正常 1-删除）';
