SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('100', '门户', '0', 10, 1, '/portal', NULL, 'portal', 'House', NULL, 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('101', '工作台', '100', 1, 2, '/', 'dashboard/WorkbenchView', 'portal.workbench', 'House',
        'portal:workbench:view', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('102', '消息待办', '100', 2, 2, '/messages', 'messages/MessagesView', 'portal.messages', 'Bell',
        'portal:messages:view', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('200', '门诊业务', '0', 20, 1, '/opd', NULL, 'opd', 'FirstAidKit', NULL, 0, 0, 1, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('201', '挂号预约', '200', 1, 2, '/appointments', 'appointments/AppointmentsView', 'opd.appointments', 'Ticket',
        'opd:appointments:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('202', '分诊工作站', '200', 2, 2, '/triage', 'triage/TriageView', 'opd.triage', 'Filter', 'opd:triage:list', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('203', '医生工作站', '200', 3, 2, '/doctor-workstation', 'doctor-workstation/DoctorWorkstationView',
        'opd.doctorWorkstation', 'FirstAidKit', 'opd:doctorWorkstation:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('204', '就诊总览', '200', 4, 2, '/today-visits', 'today-visits/TodayVisitsView', 'opd.todayVisits', 'Notebook',
        'opd:todayVisits:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('205', '急诊中心', '200', 5, 2, '/emergency', 'emergency/EmergencyView', 'opd.emergency', 'Phone',
        'opd:emergency:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('206', '门诊治疗站', '200', 6, 2, '/treatment-station', 'treatment/TreatmentStationView',
        'opd.treatmentStation', 'FirstAidKit', 'opd:treatmentStation:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('207', '门诊日志', '200', 21, 2, '/outpatient-log', 'outpatient-log/OutpatientLogView', 'opd.outpatientLog',
        'Document', 'opd:outpatientLog:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('208', '慢病建档与长处方认定', '200', 20, 2, '/chronic-record', 'chronic/ChronicRecordView',
        'opd.chronicRecord', 'Files', 'opd:chronicRecord:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('300', '住院业务', '0', 30, 1, '/ipd', NULL, 'ipd', 'Suitcase', NULL, 0, 0, 1, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('301', '入出院管理', '300', 1, 2, '/inpatient', 'inpatient/InpatientView', 'ipd.inpatient', 'Suitcase',
        'ipd:inpatient:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('302', '住院医生站', '300', 2, 2, '/inpatient-order', 'inpatient/InpatientOrderView', 'ipd.order', 'Memo',
        'ipd:order:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('303', '护士工作站', '300', 3, 2, '/nurse', 'nurse/NurseView', 'ipd.nurse', 'Watch', 'ipd:nurse:list', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('304', '住院病历', '300', 4, 2, '/inpatient-record', 'inpatient/InpatientRecordView', 'ipd.record',
        'DocumentCopy', 'ipd:record:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('305', '住院结算', '1000', 2, 2, '/inpatient-settlement', 'inpatient/InpatientSettlementView', 'ipd.settlement',
        'Coin', 'ipd:settlement:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('306', '手术排期', '300', 5, 2, '/surgery', 'surgery/SurgeryView', 'ipd.surgery', 'Scissor', 'ipd:surgery:list',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('307', '会诊管理', '300', 8, 2, '/consultation', 'consultation/ConsultationView', 'ipd.consultation', 'User',
        'ipd:consultation:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('308', '转科管理', '300', 9, 2, '/transfer', 'transfer/TransferView', 'ipd.transfer', 'Sort',
        'ipd:transfer:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('309', '麻醉工作站', '300', 7, 2, '/anesthesia', 'anesthesia/AnesthesiaView', 'ipd.anesthesia', 'Aim',
        'ipd:anesthesia:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('310', '出院带药', '300', 11, 2, '/discharge-drug', 'dischargedrug/DischargeDrugView',
        'inpatient.dischargeDrug', 'Box', 'inpatient:dischargeDrug:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('311', '随访回访', '300', 27, 2, '/followup', 'followup/FollowupView', 'inpatient.followup', 'Phone',
        'inpatient:followup:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('312', '双向转诊', '300', 28, 2, '/referral', 'referral/ReferralView', 'inpatient.referral', 'Position',
        'inpatient:referral:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('313', '欠费管控', '1000', 44, 2, '/arrears-control', 'arrears/ArrearsControlView', 'charge.arrearsControl',
        'Warning', 'charge:arrearsControl:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('314', 'ICU 专科监护', '300', 20, 2, '/icu', 'icu/IcuView', 'ipd.icu', 'HeartPulse', 'ipd:icu:list', 0, 0, 1, 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('315', '远程会诊', '1300', 1, 2, '/teleconsult', 'teleconsult/TeleConsultView', 'ipd.teleconsult', 'Connection',
        'ipd:teleconsult:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('316', '日间手术', '300', 21, 2, '/day-surgery', 'day-surgery/DaySurgeryView', 'ipd.daySurgery', 'Scissor',
        'ipd:daySurgery:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('317', '床位管理', '300', 10, 2, '/bed-center', 'bedcenter/BedCenterView', 'ipd.bedCenter', 'Grid',
        'ipd:bedCenter:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('318', '死亡证明与登记', '300', 14, 2, '/death-certificate', 'death/DeathCertificateView',
        'ipd.deathCertificate', 'FileWarning', 'ipd:deathCertificate:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('319', '病危重通知', '300', 12, 2, '/critical-notice', 'critical/CriticalNoticeView', 'ipd.criticalNotice',
        'Warning', 'ipd:criticalNotice:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('320', '患者请假离院', '300', 13, 2, '/inpatient-leave', 'inpatientLeave/InpatientLeaveView', 'ipd.leave',
        'AlarmClock', 'ipd:leave:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('330', '护理管理', '0', 40, 1, '/nursing-group', NULL, 'nursing', 'Watch', NULL, 0, 0, 1, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('331', '病区护理排班', '2945', 4, 2, '/nurse-schedule', 'nurse/NurseScheduleView', 'nursing.schedule',
        'Calendar', 'nursing:schedule:list', 0, 0, 1, 1, 'admin', 'sql230', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('332', 'VTE 防控与监测', '330', 2, 2, '/vte-prevent', 'nurse/VtePreventView', 'nursing.vtePrevent',
        'FirstAidKit', 'nursing:vte:prevent', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('333', '院内 VTE 监测', '330', 90, 2, '/vte-monitor', 'nurse/VteMonitorView', 'nursing.vteMonitor',
        'TrendCharts', 'nursing:vte:monitor', 0, 0, 0, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('334', '护理质控', '330', 3, 2, '/nursing-qc', 'nurse/NursingQcView', 'nursing.qc', 'Aim', 'nursing:qc:list', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('400', '医技医辅', '0', 50, 1, '/medtech', NULL, 'medtech', 'Monitor', NULL, 0, 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('401', '检查工作站', '400', 1, 2, '/inspection-workstation', 'lab/InspectionView',
        'medtech.inspectionWorkstation', 'Monitor', 'medtech:inspectionWorkstation:list', 0, 0, 1, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('402', '检验工作站', '400', 2, 2, '/laboratory-workstation', 'lab/LaboratoryView',
        'medtech.laboratoryWorkstation', 'Aim', 'medtech:laboratoryWorkstation:list', 0, 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('403', '标本管理', '400', 3, 2, '/specimen', 'specimen/SpecimenView', 'medtech.specimen', 'Position',
        'medtech:specimen:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('404', '检查项目字典', '400', 40, 2, '/inspection-items', 'medical-items/InspectionItemsView',
        'medtech.inspectionItems', 'Files', 'medtech:inspectionItems:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('405', '检验项目字典', '400', 41, 2, '/laboratory-items', 'medical-items/LaboratoryItemsView',
        'medtech.laboratoryItems', 'List', 'medtech:laboratoryItems:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('406', '输血管理', '400', 11, 2, '/transfusion', 'transfusion/TransfusionView', 'medtech.transfusion',
        'Pouring', 'medtech:transfusion:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('407', '病理工作站', '400', 7, 2, '/pathology', 'pathology/PathologyView', 'medtech.pathology', 'Microscope',
        'medtech:pathology:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('408', '内镜工作站', '400', 6, 2, '/endoscopy', 'endoscopy/EndoscopyView', 'medtech.endoscopy', 'View',
        'medtech:endoscopy:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('409', '超声工作站', '400', 5, 2, '/ultrasound', 'ultrasound/UltrasoundView', 'medtech.ultrasound', 'Odometer',
        'medtech:ultrasound:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('410', '检验质控', '400', 20, 2, '/lis-qc', 'lisqc/LisQcView', 'medtech.lisQc', 'DataAnalysis',
        'medtech:lisQc:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('411', '血库管理', '400', 12, 2, '/blood-bank', 'bloodbank/BloodBankView', 'medtech.bloodBank', 'Pouring',
        'medtech:bloodBank:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('412', '检查预约中心', '400', 4, 2, '/exam-appointment', 'exam-appointment/ExamAppointmentView',
        'medtech.examAppoint', 'Calendar', 'medtech:examAppoint:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('413', '血液净化中心', '400', 13, 2, '/dialysis', 'dialysis/DialysisView', 'medtech.dialysis', 'Droplets',
        'medtech:dialysis:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('414', '放射诊断工作站', '400', 8, 2, '/radio-diagnosis', 'lab/RadioDiagnosisView', 'medtech.radioDiagnosis',
        'EditPen', 'medtech:radioDiagnosis:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('415', '胶片量方与发放', '400', 9, 2, '/exam-film', 'lab/ExamFilmView', 'medtech.examFilm', 'Film',
        'medtech:examFilm:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('416', '室间质评', '400', 90, 2, '/lis-eqa', 'lisqc/LisEqaView', 'medtech.lisEqa', 'TrendCharts',
        'medtech:lisEqa:list', 0, 0, 0, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('417', '心电工作站', '400', 10, 2, '/ecg', 'lab/EcgWorkstationView', 'medtech.ecg', 'Monitor',
        'medtech:ecg:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('424', '营养风险筛查', '300', 22, 2, '/nutrition-screen', 'nutrition/NutritionScreenView',
        'ipd.nutritionScreen', 'Apple', 'ipd:nutrition:screen', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('425', '膳食医嘱执行', '300', 23, 2, '/diet-plan', 'nutrition/DietPlanView', 'ipd.dietPlan', 'Food',
        'ipd:diet:plan', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('426', '订餐配送', '300', 24, 2, '/meal-order', 'nutrition/MealOrderView', 'ipd.mealOrder', 'Dish',
        'ipd:meal:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('427', '营养会诊', '300', 25, 2, '/nutrition-consult', 'nutrition/NutritionConsultView', 'ipd.nutritionConsult',
        'FirstAidKit', 'ipd:nutrition:consult', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('428', '营养指标监测', '300', 26, 2, '/nutrition-stats', 'nutrition/NutritionStatsView', 'ipd.nutritionStats',
        'TrendCharts', 'ipd:nutrition:stats', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('500', '药事管理', '0', 60, 1, '/pharmacy-group', NULL, 'pharmacyGroup', 'Box', NULL, 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('501', '药品库存', '500', 7, 2, '/pharmacy', 'pharmacy/PharmacyView', 'pharmacy.stock', 'Goods',
        'pharmacy:stock:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('502', '药房发药', '500', 1, 2, '/pharmacy-window', 'pharmacy-window/PharmacyWindowView', 'pharmacy.dispensing',
        'Box', 'pharmacy:dispensing:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('503', '处方审核', '500', 20, 2, '/prescription-audit', 'pharmacy-window/PrescriptionAuditView',
        'pharmacy.prescriptionAudit', 'Stamp', 'pharmacy:prescriptionAudit:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('504', '麻精药品专册', '500', 6, 2, '/narcotic-register', 'pharmacy-window/NarcoticRegisterView',
        'pharmacy.narcoticRegister', 'Lock', 'pharmacy:narcotic:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('505', '采购订单', '500', 8, 2, '/purchase-order', 'pharmacy/PurchaseOrderView', 'pharmacy.purchaseOrder',
        'ShoppingCart', 'pharmacy:purchase:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('506', '入库单', '500', 9, 2, '/drug-inbound', 'pharmacy/DrugInboundView', 'pharmacy.drugInbound', 'Download',
        'pharmacy:inbound:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('507', '供应商管理', '500', 40, 2, '/supplier', 'pharmacy/SupplierView', 'pharmacy.supplier', 'OfficeBuilding',
        'pharmacy:supplier:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('508', '住院摆药', '500', 2, 2, '/ward-dispense', 'pharmacy/WardDispenseView', 'pharmacy.wardDispense', 'Box',
        'pharmacy:wardDispense:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('509', '静配中心', '500', 3, 2, '/pivas', 'pharmacy/PivasView', 'pharmacy.pivas', 'Syringe',
        'pharmacy:pivas:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('510', '药房盘点', '500', 4, 2, '/stocktake', 'pharmacy/StocktakeView', 'pharmacy.stocktake', 'Clipboard',
        'pharmacy:stocktake:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('511', '中药代煎', '500', 5, 2, '/tcm-decoct', 'pharmacy/TcmDecoctView', 'pharmacy.tcmDecoct', 'Coffee',
        'pharmacy:tcmDecoct:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('512', '药品调拨', '500', 10, 2, '/drug-transfer', 'pharmacy/DrugTransferView', 'pharmacy.drugTransfer', 'Sort',
        'pharmacy:drugTransfer:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('513', '供应商退货', '500', 11, 2, '/supplier-return', 'pharmacy/SupplierReturnView', 'pharmacy.supplierReturn',
        'Reply', 'pharmacy:supplierReturn:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('514', '药品追溯码', '500', 12, 2, '/drug-trace', 'pharmacy/DrugTraceView', 'pharmacy.drugTrace', 'Ticket',
        'pharmacy:drugTrace:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('515', '处方点评', '500', 21, 2, '/rx-review', 'pharmacy/RxReviewView', 'pharmacy.rxReview', 'Finished',
        'pharmacy:rxReview:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('516', '处方公示', '500', 90, 2, '/rx-publicity', 'pharmacy/RxPublicityView', 'pharmacy.rxPublicity', 'View',
        'pharmacy:rxPublicity:list', 0, 0, 0, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('517', '抗菌药物分级目录', '500', 41, 2, '/antibiotic-catalog', 'pharmacy/AntibioticCatalogView',
        'pharmacy.antibioticCatalog', 'Collection', 'pharmacy:antibiotic:catalog', 0, 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('518', '抗菌药物使用监测', '500', 23, 2, '/antibiotic-monitor', 'pharmacy/AntibioticMonitorView',
        'pharmacy.antibioticMonitor', 'DataLine', 'pharmacy:antibiotic:monitor', 0, 0, 1, 1, 'admin', 'system', 0, NULL,
        '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('519', 'I类切口预防用药点评', '500', 22, 2, '/antibiotic-incision', 'pharmacy/AntibioticIncisionView',
        'pharmacy.antibioticIncision', 'Scissors', 'pharmacy:antibiotic:incision', 0, 0, 1, 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('600', '病历与质量安全', '0', 70, 1, '/emr-qc', NULL, 'emrQc', 'DocumentChecked', NULL, 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('602', '门诊病历', '600', 1, 2, '/medical-record', 'medical-record/MedicalRecordView', 'emr.medicalRecord',
        'Postcard', 'emr:medicalRecord:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('603', '病历审核', '600', 2, 2, '/medical-review', 'medical-record/MedicalReviewView', 'emr.medicalReview',
        'DocumentChecked', 'emr:medicalReview:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('604', '病案质控工作台', '600', 20, 2, '/medical-record-qc', 'quality/MedicalRecordQcView', 'qc.recordQc',
        'DataAnalysis', 'qc:recordQc:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('605', '全院数据质量监测', '600', 21, 2, '/data-quality', 'quality/DataQualityView', 'qc.dataQuality',
        'Histogram', 'qc:dataQuality:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('606', '电子签名与时间戳', '1100', 43, 2, '/signature-center', 'sign/SignatureCenterView', 'sign.center',
        'Stamp', 'sign:center:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('607', '临床路径', '600', 22, 2, '/clinical-path', 'clinical-path/ClinicalPathView', 'qc.clinicalPath', 'Guide',
        'qc:clinicalPath:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('608', '危急值管理', '600', 30, 2, '/critical-value', 'critical-value/CriticalValueView',
        'medtech.criticalValue', 'Warning', 'medtech:criticalValue:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('609', '病案归档', '600', 3, 2, '/medical-record-archive', 'medical-record/MedicalRecordArchiveView',
        'emr.archive', 'FolderChecked', 'emr:archive:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('610', '不良事件上报', '600', 31, 2, '/adverse-event', 'adverse-event/AdverseEventView', 'emr.adverseEvent',
        'WarningFilled', 'emr:adverseEvent:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('611', '病案借阅复印', '600', 4, 2, '/archive-borrow', 'medical-record/ArchiveBorrowView', 'emr.archiveBorrow',
        'Notebook', 'emr:archiveBorrow:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('612', '编码任务池', '600', 5, 2, '/coding-task', 'medical-record/CodingTaskView', 'emr.codeTask', 'Collection',
        'emr:codeTask:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('613', '传染病报告卡', '600', 34, 2, '/infectious-report', 'infectious-report/InfectiousReportView',
        'emr.infectiousReport', 'Warning', 'emr:infectiousReport:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('614', '院感监测', '600', 32, 2, '/infection-monitor', 'infection-monitor/InfectionMonitorView',
        'emr.infectionMonitor', 'DataBoard', 'emr:infectionMonitor:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('615', '医疗纠纷与投诉', '600', 33, 2, '/dispute', 'dispute/DisputeView', 'qc.dispute', 'WarningFilled',
        'qc:dispute:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('616', '满意度评价', '600', 36, 2, '/survey', 'survey/SurveyView', 'qc.survey', 'Star', 'qc:survey:list', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('617', '手卫生依从性', '600', 14, 2, '/hand-hygiene', 'infection-monitor/HandHygieneView', 'emr.handHygiene',
        'Aim', 'emr:infectionMonitor:list', 0, 0, 1, 1, 'admin', 'system', 1, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('618', '公卫上报', '600', 35, 2, '/public-health', 'public-health/PublicHealthReportView', 'emr.publicHealth',
        'Notification', 'emr:publicHealth:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('700', '患者中心', '0', 90, 1, '/patient-center', NULL, 'patientCenter', 'User', NULL, 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('701', '患者管理', '700', 2, 2, '/patients', 'patients/PatientsView', 'patient.list', 'User', 'patient:list', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('702', '患者主索引', '700', 40, 2, '/empi', 'empi/PatientIndexView', 'patient.empi', 'Coordinate',
        'patient:empi:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('703', '患者 360 视图', '700', 1, 2, '/cdr', 'cdr/PatientCdrView', 'patient.cdr', 'DataLine',
        'patient:cdr:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('704', '患者标签', '700', 41, 2, '/system/patientTag', 'system/tag/PatientTagView', 'patient.tag', 'PriceTag',
        'patient:tag:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('705', '健康档案', '700', 3, 2, '/health-record', 'health-record/HealthRecordView', 'patient.profile',
        'FirstAidKit', 'patient:profile:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('800', '组织与资源', '0', 100, 1, '/org', NULL, 'org', 'OfficeBuilding', NULL, 0, 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('801', '科室管理', '800', 40, 2, '/departments', 'departments/DepartmentsView', 'org.dept', 'OfficeBuilding',
        'org:dept:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('802', '诊室管理', '800', 41, 2, '/system/clinicRoom', 'system/clinicRoom/ClinicRoomView', 'org.clinicRoom',
        'Shop', 'org:clinicRoom:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('803', '员工管理', '800', 4, 2, '/system/employee', 'system/employee/EmployeeView', 'org.employee',
        'UserFilled', 'org:employee:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('804', '排班管理', '2945', 2, 2, '/schedule', 'schedule/ScheduleView', 'org.schedule', 'Calendar',
        'org:schedule:list', 0, 0, 1, 1, 'admin', 'sql230', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('805', '技术授权', '800', 5, 2, '/system/techAuth', 'system/techAuth/TechAuthView', 'org.techAuth', 'Stamp',
        'org:techAuth:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('806', '总值班排班', '2945', 6, 2, '/duty-roster', 'duty/DutyRosterView', 'org.dutyRoster', 'AlarmClock',
        'org:duty:list', 0, 0, 1, 1, 'admin', 'sql230', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('900', '物资设备', '0', 110, 1, '/asset', NULL, 'asset', 'Tools', NULL, 0, 0, 1, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('901', '物资耗材', '900', 1, 2, '/supplies', 'supplies/SuppliesView', 'asset.supplies', 'Box',
        'asset:supplies:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('902', '设备管理', '900', 2, 2, '/equipment', 'equipment/EquipmentView', 'asset.equipment', 'Monitor',
        'asset:equipment:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('903', '消毒供应CSSD', '900', 3, 2, '/cssd', 'cssd/CssdView', 'asset.cssd', 'Box', 'asset:cssd:list', 0, 0, 1,
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('904', '医废管理', '900', 4, 2, '/waste', 'waste/WasteView', 'asset.waste', 'Delete', 'asset:waste:list', 0, 0,
        1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('905', '体检管理', '1400', 1, 2, '/checkup', 'checkup/CheckupView', 'patient.checkup', 'icon-tijian',
        'checkup:manage:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('906', 'BI 驾驶舱', '1200', 1, 2, '/bi', 'bi/BiDashboard', 'report.bi', 'icon-dashboard', 'report:bi:list', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('907', 'DRG-DIP 分组模拟', '1200', 20, 2, '/drgsim', 'drg/DrgSimView', 'report.drgsim', 'icon-suanfa',
        'report:drg:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('908', '绩效与成本核算', '1200', 4, 2, '/perf', 'performance/PerformanceView', 'finance.performance',
        'icon-jixiao', 'report:perf:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1000', '财务结算', '0', 80, 1, '/finance-group', NULL, 'financeGroup', 'Wallet', NULL, 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1001', '收费结算窗口', '1000', 1, 2, '/cashier', 'cashier/CashierView', 'finance.cashier', 'Wallet',
        'finance:cashier:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1003', '医保结算清单', '1000', 10, 2, '/insurance', 'insurance/InsuranceView', 'finance.insurance', 'Discount',
        'finance:insurance:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1004', '发票管理', '1000', 4, 2, '/invoice', 'invoice/InvoiceView', 'finance.invoice', 'Tickets',
        'finance:invoice:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1005', '退费申请', '1000', 3, 2, '/refund', 'refund/RefundView', 'finance.refund', 'RefreshLeft',
        'finance:refund:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1006', '医保政策配置', '1000', 40, 2, '/insurance-policy', 'insurance-policy/InsurancePolicyView',
        'finance.insurancePolicy', 'Guide', 'finance:insurancePolicy:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1007', '价格管理', '1000', 26, 2, '/price', 'price/PriceView', 'finance.price', 'PriceTag',
        'finance:price:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1008', '财务日结', '1000', 5, 2, '/finance', 'finance/FinanceView', 'finance.settlement', 'Wallet',
        'finance:settlement:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1009', '医保目录对照', '1000', 41, 2, '/insurance-mapping', 'insurance/InsuranceMappingView',
        'finance.insuranceMapping', 'Connection', 'finance:insuranceMapping:list', 0, 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1010', '医保扣款与飞检处理', '1000', 13, 2, '/insurance-deduct', 'insurance/InsuranceDeductView',
        'finance.insuranceDeduct', 'Warning', 'finance:insuranceDeduct:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1011', '慢特病医保备案', '1000', 11, 2, '/insurance-chronic', 'insurance/InsuranceChronicView',
        'finance.insuranceChronic', 'Stamp', 'finance:insuranceChronic:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL,
        '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1012', '医保合规审核台账', '1000', 12, 2, '/compliance-audit', 'insurance/ComplianceAuditView',
        'finance.complianceAudit', 'CircleCheck', 'finance:complianceAudit:list', 0, 0, 1, 1, 'admin', 'system', 0,
        NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1100', '系统管理', '0', 150, 1, '/system', NULL, 'system', 'Setting', NULL, 0, 0, 1, 1, 'admin', 'admin', 0,
        NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1101', '用户管理', '1100', 1, 2, '/system/user', 'system/user/UserView', 'system.user', 'UserFilled',
        'system:user:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1102', '角色管理', '1100', 2, 2, '/system/role', 'system/role/RoleView', 'system.role', 'Key',
        'system:role:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1103', '菜单管理', '1100', 3, 2, '/system/menu', 'system/menu/MenuView', 'system.menu', 'Menu',
        'system:menu:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1104', '字典管理', '1100', 40, 2, '/system/dict', 'system/dict/DictView', 'system.dict', 'Collection',
        'system:dict:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1105', '参数设置', '1100', 41, 2, '/system/config', 'system/config/ConfigView', 'system.config', 'Setting',
        'system:config:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1106', '工作台配置', '1100', 42, 2, '/system/workbench', 'system/workbench/WorkbenchConfigView',
        'system.workbench', 'Operation', 'system:workbench:config', 0, 1, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1107', '合理用药知识库', '500', 42, 2, '/system/drugKnowledge', 'system/DrugKnowledgeView',
        'system.drugKnowledge', 'Notebook', 'system:drugKnowledge:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1108', '日志审计', '1100', 4, 2, '/system/log', 'system/LogView', 'system.log', 'Document', 'system:log:list',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1109', '部门管理', '1100', 5, 2, '/system/dept', 'system/dept/DeptView', 'system.dept', 'Share',
        'org:dept:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1130', '患者常见问题', '700', 42, 2, '/system/patientFaq', 'system/faq/FaqView', 'patient.faq', 'Notebook',
        'patient:faq:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1131', '新增或修改', '1130', 1, 3, '', '', 'patient.faq.upsert', '', 'patient:faq:upsert', 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1132', '删除', '1130', 2, 3, '', '', 'patient.faq.delete', '', 'patient:faq:delete', 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1133', '检验项目白话词典', '700', 43, 2, '/system/labPlain', 'system/faq/LabPlainItemView', 'lab.plain',
        'Reading', 'lab:plain:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1134', '新增或修改', '1133', 1, 3, '', '', 'lab.plain.upsert', '', 'lab:plain:upsert', 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1135', '删除', '1133', 2, 3, '', '', 'lab.plain.delete', '', 'lab:plain:delete', 0, 0, 1, 1, 'admin', 'admin',
        0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1136', '工单受理', '700', 44, 2, '/system/serviceTicket', 'system/service/ServiceTicketView', 'service.ticket',
        'Service', 'service:ticket:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1137', '受理与处理', '1136', 1, 3, '', '', 'service.ticket.handle', '', 'service:ticket:handle', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1200', '报表统计', '0', 140, 1, '/report-group', NULL, 'reportGroup', 'Histogram', NULL, 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1201', '运营统计报表', '1200', 2, 2, '/reports', 'reports/ReportsView', 'report.stats', 'Histogram',
        'report:stats:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1300', '远程医疗', '0', 120, 1, '/telehealth', NULL, 'telehealth', 'Connection', NULL, 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('1400', '健康管理（体检）', '0', 130, 1, '/checkup-group', NULL, 'checkupGroup', 'CircleCheck', NULL, 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2001', '消息待办-修改', '102', 2, 3, NULL, NULL, 'portal.messages:edit', NULL, 'portal:messages:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2002', '挂号预约-新增', '201', 1, 3, NULL, NULL, 'opd.appointments:add', NULL, 'opd:appointments:add', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2003', '挂号预约-修改', '201', 2, 3, NULL, NULL, 'opd.appointments:edit', NULL, 'opd:appointments:edit', 0, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2004', '挂号预约-删除', '201', 3, 3, NULL, NULL, 'opd.appointments:delete', NULL, 'opd:appointments:delete', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2005', '分诊工作站-新增', '202', 1, 3, NULL, NULL, 'opd.triage:add', NULL, 'opd:triage:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2006', '分诊工作站-修改', '202', 2, 3, NULL, NULL, 'opd.triage:edit', NULL, 'opd:triage:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2007', '医生工作站-新增', '203', 1, 3, NULL, NULL, 'opd.doctorWorkstation:add', NULL,
        'opd:doctorWorkstation:add', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2008', '医生工作站-修改', '203', 2, 3, NULL, NULL, 'opd.doctorWorkstation:edit', NULL,
        'opd:doctorWorkstation:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2009', '医生工作站-删除', '203', 3, 3, NULL, NULL, 'opd.doctorWorkstation:delete', NULL,
        'opd:doctorWorkstation:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2010', '急诊中心-新增', '205', 1, 3, NULL, NULL, 'opd.emergency:add', NULL, 'opd:emergency:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2011', '急诊中心-修改', '205', 2, 3, NULL, NULL, 'opd.emergency:edit', NULL, 'opd:emergency:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2012', '门诊治疗站-新增', '206', 1, 3, NULL, NULL, 'opd.treatmentStation:add', NULL,
        'opd:treatmentStation:add', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2013', '门诊治疗站-修改', '206', 2, 3, NULL, NULL, 'opd.treatmentStation:edit', NULL,
        'opd:treatmentStation:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2014', '门诊治疗站-删除', '206', 3, 3, NULL, NULL, 'opd.treatmentStation:delete', NULL,
        'opd:treatmentStation:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2015', '入出院管理-新增', '301', 1, 3, NULL, NULL, 'ipd.inpatient:add', NULL, 'ipd:inpatient:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2016', '入出院管理-修改', '301', 2, 3, NULL, NULL, 'ipd.inpatient:edit', NULL, 'ipd:inpatient:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2017', '住院医生站-新增', '302', 1, 3, NULL, NULL, 'ipd.order:add', NULL, 'ipd:order:add', 0, 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2018', '住院医生站-修改', '302', 2, 3, NULL, NULL, 'ipd.order:edit', NULL, 'ipd:order:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2019', '住院医生站-删除', '302', 3, 3, NULL, NULL, 'ipd.order:delete', NULL, 'ipd:order:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2020', '护士工作站-新增', '303', 1, 3, NULL, NULL, 'ipd.nurse:add', NULL, 'ipd:nurse:add', 0, 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2021', '护士工作站-修改', '303', 2, 3, NULL, NULL, 'ipd.nurse:edit', NULL, 'ipd:nurse:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2022', '住院病历-新增', '304', 1, 3, NULL, NULL, 'ipd.record:add', NULL, 'ipd:record:add', 0, 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2023', '住院病历-修改', '304', 2, 3, NULL, NULL, 'ipd.record:edit', NULL, 'ipd:record:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2024', '手术排期-新增', '306', 1, 3, NULL, NULL, 'ipd.surgery:add', NULL, 'ipd:surgery:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2025', '手术排期-修改', '306', 2, 3, NULL, NULL, 'ipd.surgery:edit', NULL, 'ipd:surgery:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2026', '手术排期-删除', '306', 3, 3, NULL, NULL, 'ipd.surgery:delete', NULL, 'ipd:surgery:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2027', '会诊管理-新增', '307', 1, 3, NULL, NULL, 'ipd.consultation:add', NULL, 'ipd:consultation:add', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2028', '会诊管理-修改', '307', 2, 3, NULL, NULL, 'ipd.consultation:edit', NULL, 'ipd:consultation:edit', 0, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2029', '会诊管理-删除', '307', 3, 3, NULL, NULL, 'ipd.consultation:delete', NULL, 'ipd:consultation:delete', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2030', '转科管理-新增', '308', 1, 3, NULL, NULL, 'ipd.transfer:add', NULL, 'ipd:transfer:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2031', '转科管理-修改', '308', 2, 3, NULL, NULL, 'ipd.transfer:edit', NULL, 'ipd:transfer:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2032', '转科管理-删除', '308', 3, 3, NULL, NULL, 'ipd.transfer:delete', NULL, 'ipd:transfer:delete', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2033', '麻醉工作站-新增', '309', 1, 3, NULL, NULL, 'ipd.anesthesia:add', NULL, 'ipd:anesthesia:add', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2034', '麻醉工作站-修改', '309', 2, 3, NULL, NULL, 'ipd.anesthesia:edit', NULL, 'ipd:anesthesia:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2035', '出院带药-新增', '310', 1, 3, NULL, NULL, 'inpatient.dischargeDrug:add', NULL,
        'inpatient:dischargeDrug:add', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2036', '出院带药-修改', '310', 2, 3, NULL, NULL, 'inpatient.dischargeDrug:edit', NULL,
        'inpatient:dischargeDrug:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2037', '出院带药-删除', '310', 3, 3, NULL, NULL, 'inpatient.dischargeDrug:delete', NULL,
        'inpatient:dischargeDrug:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2038', '随访回访-新增', '311', 1, 3, NULL, NULL, 'inpatient.followup:add', NULL, 'inpatient:followup:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2039', '随访回访-修改', '311', 2, 3, NULL, NULL, 'inpatient.followup:edit', NULL, 'inpatient:followup:edit', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2040', '随访回访-删除', '311', 3, 3, NULL, NULL, 'inpatient.followup:delete', NULL,
        'inpatient:followup:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2041', '双向转诊-新增', '312', 1, 3, NULL, NULL, 'inpatient.referral:add', NULL, 'inpatient:referral:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2042', '双向转诊-修改', '312', 2, 3, NULL, NULL, 'inpatient.referral:edit', NULL, 'inpatient:referral:edit', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2043', '双向转诊-删除', '312', 3, 3, NULL, NULL, 'inpatient.referral:delete', NULL,
        'inpatient:referral:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2044', '欠费管控-新增', '313', 1, 3, NULL, NULL, 'charge.arrearsControl:add', NULL,
        'charge:arrearsControl:add', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2045', '检验工作站-修改', '402', 2, 3, NULL, NULL, 'medtech.laboratoryWorkstation:edit', NULL,
        'medtech:laboratoryWorkstation:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2046', '标本管理-修改', '403', 2, 3, NULL, NULL, 'medtech.specimen:edit', NULL, 'medtech:specimen:edit', 0, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2047', '检查项目字典-新增', '404', 1, 3, NULL, NULL, 'medtech.inspectionItems:add', NULL,
        'medtech:inspectionItems:add', 0, 1, 0, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2048', '检查项目字典-删除', '404', 3, 3, NULL, NULL, 'medtech.inspectionItems:delete', NULL,
        'medtech:inspectionItems:delete', 0, 1, 0, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2049', '输血管理-新增', '406', 1, 3, NULL, NULL, 'medtech.transfusion:add', NULL, 'medtech:transfusion:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2050', '输血管理-修改', '406', 2, 3, NULL, NULL, 'medtech.transfusion:edit', NULL, 'medtech:transfusion:edit',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2051', '输血管理-删除', '406', 3, 3, NULL, NULL, 'medtech.transfusion:delete', NULL,
        'medtech:transfusion:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2052', '病理工作站-新增', '407', 1, 3, NULL, NULL, 'medtech.pathology:add', NULL, 'medtech:pathology:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2053', '病理工作站-修改', '407', 2, 3, NULL, NULL, 'medtech.pathology:edit', NULL, 'medtech:pathology:edit', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2054', '病理工作站-删除', '407', 3, 3, NULL, NULL, 'medtech.pathology:delete', NULL,
        'medtech:pathology:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2055', '内镜工作站-新增', '408', 1, 3, NULL, NULL, 'medtech.endoscopy:add', NULL, 'medtech:endoscopy:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2056', '内镜工作站-修改', '408', 2, 3, NULL, NULL, 'medtech.endoscopy:edit', NULL, 'medtech:endoscopy:edit', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2057', '内镜工作站-删除', '408', 3, 3, NULL, NULL, 'medtech.endoscopy:delete', NULL,
        'medtech:endoscopy:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2058', '超声工作站-新增', '409', 1, 3, NULL, NULL, 'medtech.ultrasound:add', NULL, 'medtech:ultrasound:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2059', '超声工作站-修改', '409', 2, 3, NULL, NULL, 'medtech.ultrasound:edit', NULL, 'medtech:ultrasound:edit',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2060', '超声工作站-删除', '409', 3, 3, NULL, NULL, 'medtech.ultrasound:delete', NULL,
        'medtech:ultrasound:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2061', '室内质控-新增', '410', 1, 3, NULL, NULL, 'medtech.lisQc:add', NULL, 'medtech:lisQc:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2062', '室内质控-修改', '410', 2, 3, NULL, NULL, 'medtech.lisQc:edit', NULL, 'medtech:lisQc:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2063', '血库管理-新增', '411', 1, 3, NULL, NULL, 'medtech.bloodBank:add', NULL, 'medtech:bloodBank:add', 0, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2064', '血库管理-修改', '411', 2, 3, NULL, NULL, 'medtech.bloodBank:edit', NULL, 'medtech:bloodBank:edit', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2065', '血库管理-删除', '411', 3, 3, NULL, NULL, 'medtech.bloodBank:delete', NULL, 'medtech:bloodBank:delete',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2066', '检查预约中心-新增', '412', 1, 3, NULL, NULL, 'medtech.examAppoint:add', NULL,
        'medtech:examAppoint:add', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2067', '检查预约中心-修改', '412', 2, 3, NULL, NULL, 'medtech.examAppoint:edit', NULL,
        'medtech:examAppoint:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2068', '检查预约中心-删除', '412', 3, 3, NULL, NULL, 'medtech.examAppoint:delete', NULL,
        'medtech:examAppoint:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2069', '药品库存-新增', '501', 1, 3, NULL, NULL, 'pharmacy.stock:add', NULL, 'pharmacy:stock:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2070', '药品库存-修改', '501', 2, 3, NULL, NULL, 'pharmacy.stock:edit', NULL, 'pharmacy:stock:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2071', '药品库存-删除', '501', 3, 3, NULL, NULL, 'pharmacy.stock:delete', NULL, 'pharmacy:stock:delete', 0, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2072', '药房发药-修改', '502', 2, 3, NULL, NULL, 'pharmacy.dispensing:edit', NULL, 'pharmacy:dispensing:edit',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2073', '处方审核-修改', '503', 2, 3, NULL, NULL, 'pharmacy.prescriptionAudit:edit', NULL,
        'pharmacy:prescriptionAudit:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2074', '麻精药品专册-修改', '504', 2, 3, NULL, NULL, 'pharmacy.narcoticRegister:edit', NULL,
        'pharmacy:narcotic:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2075', '采购订单-新增', '505', 1, 3, NULL, NULL, 'pharmacy.purchaseOrder:add', NULL, 'pharmacy:purchase:add',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2076', '采购订单-修改', '505', 2, 3, NULL, NULL, 'pharmacy.purchaseOrder:edit', NULL, 'pharmacy:purchase:edit',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2077', '采购订单-删除', '505', 3, 3, NULL, NULL, 'pharmacy.purchaseOrder:delete', NULL,
        'pharmacy:purchase:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2078', '入库单-修改', '506', 2, 3, NULL, NULL, 'pharmacy.drugInbound:edit', NULL, 'pharmacy:inbound:edit', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2079', '入库单-删除', '506', 3, 3, NULL, NULL, 'pharmacy.drugInbound:delete', NULL, 'pharmacy:inbound:delete',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2080', '供应商管理-新增', '507', 1, 3, NULL, NULL, 'pharmacy.supplier:add', NULL, 'pharmacy:supplier:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2081', '供应商管理-删除', '507', 3, 3, NULL, NULL, 'pharmacy.supplier:delete', NULL,
        'pharmacy:supplier:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2082', '住院摆药-新增', '508', 1, 3, NULL, NULL, 'pharmacy.wardDispense:add', NULL,
        'pharmacy:wardDispense:add', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2083', '住院摆药-修改', '508', 2, 3, NULL, NULL, 'pharmacy.wardDispense:edit', NULL,
        'pharmacy:wardDispense:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2084', '门诊病历-修改', '602', 2, 3, NULL, NULL, 'emr.medicalRecord:edit', NULL, 'emr:medicalRecord:edit', 0,
        1, 0, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2085', '病历审核-修改', '603', 2, 3, NULL, NULL, 'emr.medicalReview:edit', NULL, 'emr:medicalReview:edit', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2086', '病案质控工作台-新增', '604', 1, 3, NULL, NULL, 'qc.recordQc:add', NULL, 'qc:recordQc:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2087', '病案质控工作台-修改', '604', 2, 3, NULL, NULL, 'qc.recordQc:edit', NULL, 'qc:recordQc:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2088', '电子签名与时间戳-修改', '606', 2, 3, NULL, NULL, 'sign.center:edit', NULL, 'sign:center:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2089', '危急值管理-修改', '608', 2, 3, NULL, NULL, 'medtech.criticalValue:edit', NULL,
        'medtech:criticalValue:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2090', '危急值管理-删除', '608', 3, 3, NULL, NULL, 'medtech.criticalValue:delete', NULL,
        'medtech:criticalValue:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2091', '病案归档-修改', '609', 2, 3, NULL, NULL, 'emr.archive:edit', NULL, 'emr:archive:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2092', '不良事件上报-新增', '610', 1, 3, NULL, NULL, 'emr.adverseEvent:add', NULL, 'emr:adverseEvent:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2093', '不良事件上报-修改', '610', 2, 3, NULL, NULL, 'emr.adverseEvent:edit', NULL, 'emr:adverseEvent:edit', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2094', '不良事件上报-删除', '610', 3, 3, NULL, NULL, 'emr.adverseEvent:delete', NULL,
        'emr:adverseEvent:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2095', '病案借阅复印-新增', '611', 1, 3, NULL, NULL, 'emr.archiveBorrow:add', NULL, 'emr:archiveBorrow:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2096', '病案借阅复印-修改', '611', 2, 3, NULL, NULL, 'emr.archiveBorrow:edit', NULL, 'emr:archiveBorrow:edit',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2097', '病案借阅复印-删除', '611', 3, 3, NULL, NULL, 'emr.archiveBorrow:delete', NULL,
        'emr:archiveBorrow:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2098', '编码任务池-新增', '612', 1, 3, NULL, NULL, 'emr.codeTask:add', NULL, 'emr:codeTask:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2099', '编码任务池-修改', '612', 2, 3, NULL, NULL, 'emr.codeTask:edit', NULL, 'emr:codeTask:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2100', '传染病报告卡-新增', '613', 1, 3, NULL, NULL, 'emr.infectiousReport:add', NULL,
        'emr:infectiousReport:add', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2101', '传染病报告卡-修改', '613', 2, 3, NULL, NULL, 'emr.infectiousReport:edit', NULL,
        'emr:infectiousReport:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2102', '院感监测-新增', '614', 1, 3, NULL, NULL, 'emr.infectionMonitor:add', NULL, 'emr:infectionMonitor:add',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2103', '院感监测-修改', '614', 2, 3, NULL, NULL, 'emr.infectionMonitor:edit', NULL,
        'emr:infectionMonitor:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2104', '院感监测-删除', '614', 3, 3, NULL, NULL, 'emr.infectionMonitor:delete', NULL,
        'emr:infectionMonitor:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2105', '患者管理-新增', '701', 1, 3, NULL, NULL, 'patient.list:add', NULL, 'patient:add', 0, 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2106', '患者管理-修改', '701', 2, 3, NULL, NULL, 'patient.list:edit', NULL, 'patient:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2107', '患者主索引-修改', '702', 2, 3, NULL, NULL, 'patient.empi:edit', NULL, 'patient:empi:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2108', '患者主索引-删除', '702', 3, 3, NULL, NULL, 'patient.empi:delete', NULL, 'patient:empi:delete', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2109', '患者标签-新增', '704', 1, 3, NULL, NULL, 'patient.tag:add', NULL, 'patient:tag:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2110', '患者标签-删除', '704', 3, 3, NULL, NULL, 'patient.tag:delete', NULL, 'patient:tag:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2111', '健康档案-新增', '705', 1, 3, NULL, NULL, 'patient.profile:add', NULL, 'patient:profile:add', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2112', '健康档案-修改', '705', 2, 3, NULL, NULL, 'patient.profile:edit', NULL, 'patient:profile:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2113', '健康档案-删除', '705', 3, 3, NULL, NULL, 'patient.profile:delete', NULL, 'patient:profile:delete', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2114', '科室管理-新增', '801', 1, 3, NULL, NULL, 'org.dept:add', NULL, 'org:dept:add', 0, 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2115', '科室管理-删除', '801', 3, 3, NULL, NULL, 'org.dept:delete', NULL, 'org:dept:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2116', '诊室管理-新增', '802', 1, 3, NULL, NULL, 'org.clinicRoom:add', NULL, 'org:clinicRoom:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2117', '诊室管理-删除', '802', 3, 3, NULL, NULL, 'org.clinicRoom:delete', NULL, 'org:clinicRoom:delete', 0, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2118', '员工管理-新增', '803', 1, 3, NULL, NULL, 'org.employee:add', NULL, 'org:employee:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2119', '员工管理-删除', '803', 3, 3, NULL, NULL, 'org.employee:delete', NULL, 'org:employee:delete', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2120', '排班管理-新增', '804', 1, 3, NULL, NULL, 'org.schedule:add', NULL, 'org:schedule:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2121', '排班管理-修改', '804', 2, 3, NULL, NULL, 'org.schedule:edit', NULL, 'org:schedule:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2122', '排班管理-删除', '804', 3, 3, NULL, NULL, 'org.schedule:delete', NULL, 'org:schedule:delete', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2123', '物资耗材-新增', '901', 1, 3, NULL, NULL, 'asset.supplies:add', NULL, 'asset:supplies:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2124', '物资耗材-修改', '901', 2, 3, NULL, NULL, 'asset.supplies:edit', NULL, 'asset:supplies:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2125', '设备管理-新增', '902', 1, 3, NULL, NULL, 'asset.equipment:add', NULL, 'asset:equipment:add', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2126', '设备管理-删除', '902', 3, 3, NULL, NULL, 'asset.equipment:delete', NULL, 'asset:equipment:delete', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2127', '消毒供应CSSD-修改', '903', 2, 3, NULL, NULL, 'asset.cssd:edit', NULL, 'asset:cssd:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2128', '医废管理-新增', '904', 1, 3, NULL, NULL, 'asset.waste:add', NULL, 'asset:waste:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2129', '医废管理-修改', '904', 2, 3, NULL, NULL, 'asset.waste:edit', NULL, 'asset:waste:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2130', '医废管理-删除', '904', 3, 3, NULL, NULL, 'asset.waste:delete', NULL, 'asset:waste:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2131', '体检管理-新增', '905', 1, 3, NULL, NULL, 'patient.checkup:add', NULL, 'checkup:manage:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2132', '体检管理-修改', '905', 2, 3, NULL, NULL, 'patient.checkup:edit', NULL, 'checkup:manage:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2133', '体检管理-删除', '905', 3, 3, NULL, NULL, 'patient.checkup:delete', NULL, 'checkup:manage:delete', 0, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2134', '绩效与成本核算-新增', '908', 1, 3, NULL, NULL, 'finance.performance:add', NULL, 'report:perf:add', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2135', '收费结算窗口-新增', '1001', 1, 3, NULL, NULL, 'finance.cashier:add', NULL, 'finance:cashier:add', 0, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2136', '收费结算窗口-修改', '1001', 2, 3, NULL, NULL, 'finance.cashier:edit', NULL, 'finance:cashier:edit', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2137', '医保结算清单-新增', '1003', 1, 3, NULL, NULL, 'finance.insurance:add', NULL, 'finance:insurance:add',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2138', '医保结算清单-修改', '1003', 2, 3, NULL, NULL, 'finance.insurance:edit', NULL, 'finance:insurance:edit',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2139', '医保结算清单-删除', '1003', 3, 3, NULL, NULL, 'finance.insurance:delete', NULL,
        'finance:insurance:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2140', '发票管理-删除', '1004', 3, 3, NULL, NULL, 'finance.invoice:delete', NULL, 'finance:invoice:delete', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2141', '发票管理-导出', '1004', 4, 3, NULL, NULL, 'finance.invoice:export', NULL, 'finance:invoice:export', 0,
        1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2142', '发票管理-开具', '1004', 1, 3, NULL, NULL, 'finance.refund:add', NULL, 'finance:invoice:add', 0, 1, 0,
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2143', '退费申请-修改', '1005', 2, 3, NULL, NULL, 'finance.refund:edit', NULL, 'finance:refund:edit', 0, 1, 0,
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2144', '退费申请-发起', '1005', 1, 3, NULL, NULL, 'finance.insurancePolicy:add', NULL, 'finance:refund:add', 0,
        1, 0, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2145', '医保政策配置-删除', '1006', 3, 3, NULL, NULL, 'finance.insurancePolicy:delete', NULL,
        'finance:insurancePolicy:delete', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2146', '价格管理-修改', '1007', 2, 3, NULL, NULL, 'finance.price:edit', NULL, 'finance:price:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2147', '财务日结-修改', '1008', 2, 3, NULL, NULL, 'finance.settlement:edit', NULL, 'finance:settlement:edit',
        0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2148', '用户管理-新增', '1101', 1, 3, NULL, NULL, 'system.user:add', NULL, 'system:user:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2149', '用户管理-修改', '1101', 2, 3, NULL, NULL, 'system.user:edit', NULL, 'system:user:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2150', '用户管理-删除', '1101', 3, 3, NULL, NULL, 'system.user:delete', NULL, 'system:user:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2151', '角色管理-新增', '1102', 1, 3, NULL, NULL, 'system.role:add', NULL, 'system:role:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2152', '角色管理-删除', '1102', 3, 3, NULL, NULL, 'system.role:delete', NULL, 'system:role:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2153', '菜单管理-新增', '1103', 1, 3, NULL, NULL, 'system.menu:add', NULL, 'system:menu:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2154', '菜单管理-删除', '1103', 3, 3, NULL, NULL, 'system.menu:delete', NULL, 'system:menu:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2155', '字典管理-新增', '1104', 1, 3, NULL, NULL, 'system.dict:add', NULL, 'system:dict:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2156', '字典管理-修改', '1104', 2, 3, NULL, NULL, 'system.dict:edit', NULL, 'system:dict:edit', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2157', '字典管理-删除', '1104', 3, 3, NULL, NULL, 'system.dict:delete', NULL, 'system:dict:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2158', '参数设置-新增', '1105', 1, 3, NULL, NULL, 'system.config:add', NULL, 'system:config:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2159', '病案统计上报', '1200', 3, 2, '/statreport', 'statreport/StatReportView', 'report.statReport',
        'icon-file-text', 'report:statReport:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2160', '病案统计上报-生成报文', '2159', 1, 3, NULL, NULL, 'report.statReport.add', NULL,
        'report:statReport:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2161', '病案统计上报-报出', '2159', 2, 3, NULL, NULL, 'report.statReport.submit', NULL,
        'report:statReport:submit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2162', '病案统计上报-作废', '2159', 3, 3, NULL, NULL, 'report.statReport.void', NULL, 'report:statReport:void',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2163', '静配中心-生成静配单', '509', 1, 3, NULL, NULL, 'pharmacy.pivas.add', NULL, 'pharmacy:pivas:add', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2164', '静配中心-审方/排队/调配/核对', '509', 2, 3, NULL, NULL, 'pharmacy.pivas.edit', NULL,
        'pharmacy:pivas:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2165', '临床路径-模板维护与发布', '607', 1, 3, NULL, NULL, 'qc.clinicalPath.add', NULL, 'qc:clinicalPath:add',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2166', '临床路径-入径/变异/完成/退径', '607', 2, 3, NULL, NULL, 'qc.clinicalPath.edit', NULL,
        'qc:clinicalPath:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2167', '血液净化-档案/处方/机位维护', '413', 1, 3, NULL, NULL, 'medtech.dialysis.add', NULL,
        'medtech:dialysis:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2168', '血液净化-排班/上机/下机/取消', '413', 2, 3, NULL, NULL, 'medtech.dialysis.edit', NULL,
        'medtech:dialysis:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2169', 'ICU-入科/出科登记', '314', 1, 3, NULL, NULL, 'ipd.icu.add', NULL, 'ipd:icu:add', 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2170', 'ICU-监护记录单', '314', 2, 3, NULL, NULL, 'ipd.icu.edit', NULL, 'ipd:icu:edit', 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2171', '医疗纠纷-登记与修改', '615', 1, 3, NULL, NULL, 'qc.dispute.add', NULL, 'qc:dispute:add', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2172', '医疗纠纷-受理/调查/处理/结案/撤销', '615', 2, 3, NULL, NULL, 'qc.dispute.edit', NULL,
        'qc:dispute:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2173', '远程会诊-申请与维护', '315', 1, 3, NULL, NULL, 'ipd.teleconsult.add', NULL, 'ipd:teleconsult:add', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2174', '远程会诊-安排/完成/取消、问诊接诊与回复', '315', 2, 3, NULL, NULL, 'ipd.teleconsult.edit', NULL,
        'ipd:teleconsult:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2175', '日间手术-目录维护与预约登记', '316', 1, 3, NULL, NULL, 'ipd.daySurgery.add', NULL,
        'ipd:daySurgery:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2176', '日间手术-评估/安排/完成/出院/转住院/随访', '316', 2, 3, NULL, NULL, 'ipd.daySurgery.edit', NULL,
        'ipd:daySurgery:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2180', '门诊输液站', '200', 7, 2, '/infusionRoom', 'infusion/InfusionRoomView', 'medtech.infusion', NULL,
        'medtech:infusion:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2181', '门诊输液站-座位维护', '2180', 0, 3, NULL, NULL, 'medtech.infusion.add', NULL, 'medtech:infusion:add',
        0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2182', '门诊输液站-入座/皮试/输注操作', '2180', 0, 3, NULL, NULL, 'medtech.infusion.edit', NULL,
        'medtech:infusion:edit', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2183', '单病种质控', '600', 23, 2, '/singleDisease', 'quality/SingleDiseaseView', 'qc.singleDisease', NULL,
        'qc:singleDisease:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2184', '单病种质控-目录维护', '2183', 0, 3, NULL, NULL, 'qc.singleDisease.add', NULL, 'qc:singleDisease:add',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2185', '单病种质控-纳入/质控/上报', '2183', 0, 3, NULL, NULL, 'qc.singleDisease.edit', NULL,
        'qc:singleDisease:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2186', '药房盘点-建单/改范围/录实盘', '510', 1, 3, NULL, NULL, 'pharmacy.stocktake.add', NULL,
        'pharmacy:stocktake:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2187', '药房盘点-提交/复核过账', '510', 2, 3, NULL, NULL, 'pharmacy.stocktake.edit', NULL,
        'pharmacy:stocktake:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2188', '药房盘点-删除', '510', 3, 3, NULL, NULL, 'pharmacy.stocktake.delete', NULL,
        'pharmacy:stocktake:delete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2320', '支付渠道对账', '1000', 25, 2, '/pay-channel', 'finance/PayChannelView', 'finance.payChannel', NULL,
        'finance:payChannel:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2321', '支付渠道对账-拉取账单/手工登记', '2320', 0, 3, NULL, NULL, 'finance.payChannel:import', NULL,
        'finance:payChannel:import', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2322', '支付渠道对账-勾对', '2320', 0, 3, NULL, NULL, 'finance.payChannel:match', NULL,
        'finance:payChannel:match', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2323', '支付渠道对账-长款短款处理', '2320', 0, 3, NULL, NULL, 'finance.payChannel:diff', NULL,
        'finance:payChannel:diff', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2340', '复诊收费策略', '1000', 43, 2, '/revisit-policy', 'revisit-policy/RevisitFeePolicyView',
        'opd.revisitPolicy', NULL, 'opd:revisitPolicy:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2341', '复诊收费策略-新增/修改', '2340', 0, 3, NULL, NULL, 'opd.revisitPolicy:add', NULL,
        'opd:revisitPolicy:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2342', '复诊收费策略-删除', '2340', 0, 3, NULL, NULL, 'opd.revisitPolicy:delete', NULL,
        'opd:revisitPolicy:delete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2350', '退费流水', '1000', 23, 2, '/refund-flow', 'refund-flow/RefundFlowView', 'finance.refundFlow', NULL,
        'finance:refundFlow:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2360', '费用记账', '1000', 20, 2, '/fee-record', 'fee-record/FeeRecordView', 'finance.feeRecord', NULL,
        'finance:feeRecord:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2361', '费用记账-补记账', '2360', 0, 3, NULL, NULL, 'finance.feeRecord:add', NULL, 'finance:feeRecord:add', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2362', '费用记账-红冲', '2360', 0, 3, NULL, NULL, 'finance.feeRecord:edit', NULL, 'finance:feeRecord:edit', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2363', '结算账单查询', '1000', 21, 2, '/settlement-bill', 'settlement-bill/SettlementBillView', 'finance.bill',
        NULL, 'finance:bill:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2364', '结算账单-结算/作废', '2363', 0, 3, NULL, NULL, 'finance.bill:edit', NULL, 'finance:bill:edit', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2365', '支付流水', '1000', 22, 2, '/payment-txn', 'payment-txn/PaymentTxnView', 'finance.payTxn', NULL,
        'finance:payTxn:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2366', '支付流水-冲正', '2365', 0, 3, NULL, NULL, 'finance.payTxn:edit', NULL, 'finance:payTxn:edit', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2367', '资金账户', '1000', 24, 2, '/fund-account', 'fund-account/FundAccountView', 'finance.fundAccount', NULL,
        'finance:fundAccount:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2368', '资金账户-充值/退款', '2367', 0, 3, NULL, NULL, 'finance.fundAccount:add', NULL,
        'finance:fundAccount:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2369', '知识库-新增/修改/启停', '1107', 1, 3, NULL, NULL, 'system.drugKnowledge.add', NULL,
        'system:drugKnowledge:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2370', '知识库-删除', '1107', 2, 3, NULL, NULL, 'system.drugKnowledge.delete', NULL,
        'system:drugKnowledge:delete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2371', '检查影像-上传/导入', '401', 1, 3, NULL, NULL, 'medtech.inspectionWorkstation.imageAdd', NULL,
        'medtech:inspectionWorkstation:imageAdd', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2372', '检查影像-删除', '401', 2, 3, NULL, NULL, 'medtech.inspectionWorkstation.imageDelete', NULL,
        'medtech:inspectionWorkstation:imageDelete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2373', '放射报告-书写/提交', '414', 1, 3, NULL, NULL, 'medtech.radioDiagnosis.write', NULL,
        'medtech:radioDiagnosis:write', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2374', '放射报告-审核/退回', '414', 2, 3, NULL, NULL, 'medtech.radioDiagnosis.audit', NULL,
        'medtech:radioDiagnosis:audit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2375', '放射报告-发布', '414', 3, 3, NULL, NULL, 'medtech.radioDiagnosis.publish', NULL,
        'medtech:radioDiagnosis:publish', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2376', '放射报告-模板维护', '414', 4, 3, NULL, NULL, 'medtech.radioDiagnosis.tplEdit', NULL,
        'medtech:radioDiagnosis:tplEdit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2377', '胶片-登记/修改', '415', 1, 3, NULL, NULL, 'medtech.examFilm.add', NULL, 'medtech:examFilm:add', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2378', '胶片-记账', '415', 2, 3, NULL, NULL, 'medtech.examFilm.charge', NULL, 'medtech:examFilm:charge', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2379', '胶片-打印/发放', '415', 3, 3, NULL, NULL, 'medtech.examFilm.deliver', NULL, 'medtech:examFilm:deliver',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2380', '代煎-状态推进', '511', 1, 3, NULL, NULL, 'pharmacy.tcmDecoct.edit', NULL, 'pharmacy:tcmDecoct:edit', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2381', '代煎-作废', '511', 2, 3, NULL, NULL, 'pharmacy.tcmDecoct.cancel', NULL, 'pharmacy:tcmDecoct:cancel', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2382', '代煎-打印回执', '511', 3, 3, NULL, NULL, 'pharmacy.tcmDecoct.print', NULL, 'pharmacy:tcmDecoct:print',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2383', '医嘱基础字典', '300', 40, 2, '/order-base-dict', 'inpatient/OrderBaseDictView', 'ipd.orderDict',
        'Collection', 'ipd:orderDict:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2384', '医嘱基础字典-新增', '2383', 1, 3, NULL, NULL, 'ipd.orderDict:add', NULL, 'ipd:orderDict:add', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2385', '医嘱基础字典-修改', '2383', 2, 3, NULL, NULL, 'ipd.orderDict:edit', NULL, 'ipd:orderDict:edit', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2386', '医嘱基础字典-删除', '2383', 3, 3, NULL, NULL, 'ipd.orderDict:delete', NULL, 'ipd:orderDict:delete', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2387', '医嘱组套（套餐）维护', '300', 41, 2, '/order-set', 'inpatient/OrderSetTemplateView', 'ipd.orderSet',
        'Tickets', 'ipd:orderSet:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2388', '医嘱组套-新增', '2387', 1, 3, NULL, NULL, 'ipd.orderSet:add', NULL, 'ipd:orderSet:add', 0, 0, 1, 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2389', '医嘱组套-修改', '2387', 2, 3, NULL, NULL, 'ipd.orderSet:edit', NULL, 'ipd:orderSet:edit', 0, 0, 1, 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2390', '医嘱组套-删除', '2387', 3, 3, NULL, NULL, 'ipd.orderSet:delete', NULL, 'ipd:orderSet:delete', 0, 0, 1,
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2391', '医保目录对照-目录维护', '1009', 1, 3, '', '', 'finance.insuranceMapping:add', '',
        'finance:insuranceMapping:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2392', '医保目录对照-对照维护', '1009', 2, 3, '', '', 'finance.insuranceMapping:edit', '',
        'finance:insuranceMapping:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2393', '胶片-作废', '415', 4, 3, NULL, NULL, 'medtech.examFilm.delete', NULL, 'medtech:examFilm:delete', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2395', '心电-签到/波形采集', '417', 1, 3, NULL, NULL, 'medtech.ecg.collect', NULL, 'medtech:ecg:collect', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2396', '心电报告-书写/提交', '417', 2, 3, NULL, NULL, 'medtech.ecg.write', NULL, 'medtech:ecg:write', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2397', '心电报告-审核/退回', '417', 3, 3, NULL, NULL, 'medtech.ecg.audit', NULL, 'medtech:ecg:audit', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2398', '心电报告-发布', '417', 4, 3, NULL, NULL, 'medtech.ecg.publish', NULL, 'medtech:ecg:publish', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2399', '心电-模板维护', '417', 5, 3, NULL, NULL, 'medtech.ecg.tplEdit', NULL, 'medtech:ecg:tplEdit', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2401', '床位管理-登记排队', '317', 1, 3, '', '', 'ipd.bedCenter:add', '', 'ipd:bedCenter:add', 0, 0, 1, 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2402', '床位管理-安排床位', '317', 2, 3, '', '', 'ipd.bedCenter:assign', '', 'ipd:bedCenter:assign', 0, 0, 1,
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2403', '床位管理-释放床位', '317', 3, 3, '', '', 'ipd.bedCenter:release', '', 'ipd:bedCenter:release', 0, 0, 1,
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2404', '床位管理-办理入院', '317', 4, 3, '', '', 'ipd:bedCenter:admit', '', 'ipd:bedCenter:admit', 0, 0, 1, 1,
        'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2405', '床位管理-取消排队', '317', 5, 3, '', '', 'ipd.bedCenter:cancel', '', 'ipd:bedCenter:cancel', 0, 0, 1,
        1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2500', '药品调拨-建单/改明细', '512', 1, 3, NULL, NULL, 'pharmacy.drugTransfer.add', NULL,
        'pharmacy:drugTransfer:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2501', '药品调拨-发出/接收/作废', '512', 2, 3, NULL, NULL, 'pharmacy.drugTransfer.edit', NULL,
        'pharmacy:drugTransfer:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2502', '药品调拨-删除', '512', 3, 3, NULL, NULL, 'pharmacy.drugTransfer.delete', NULL,
        'pharmacy:drugTransfer:delete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2503', '供应商退货-建单/改明细', '513', 1, 3, NULL, NULL, 'pharmacy.supplierReturn.add', NULL,
        'pharmacy:supplierReturn:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2504', '供应商退货-确认退货/作废', '513', 2, 3, NULL, NULL, 'pharmacy.supplierReturn.edit', NULL,
        'pharmacy:supplierReturn:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2505', '供应商退货-删除', '513', 3, 3, NULL, NULL, 'pharmacy.supplierReturn.delete', NULL,
        'pharmacy:supplierReturn:delete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2506', '药品追溯码-采集/补采', '514', 1, 3, NULL, NULL, 'pharmacy.drugTrace.add', NULL,
        'pharmacy:drugTrace:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2507', '药品追溯码-发药核销/作废', '514', 2, 3, NULL, NULL, 'pharmacy.drugTrace.edit', NULL,
        'pharmacy:drugTrace:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2508', '药品追溯码-上传', '514', 3, 3, NULL, NULL, 'pharmacy.drugTrace.export', NULL,
        'pharmacy:drugTrace:export', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2509', '药品追溯码-删除', '514', 4, 3, NULL, NULL, 'pharmacy.drugTrace.delete', NULL,
        'pharmacy:drugTrace:delete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2510', '日志审计-导出', '1108', 1, 3, NULL, NULL, 'system.log.export', NULL, 'system:log:export', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2601', '急诊中心-交班清零', '205', 3, 3, '', '', 'opd.emergency:handover', '', 'opd:emergency:handover', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2602', '处方点评-建批/补录/点评', '515', 1, 3, NULL, NULL, 'pharmacy.rxReview.add', NULL,
        'pharmacy:rxReview:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2603', '处方点评-公示', '515', 2, 3, NULL, NULL, 'pharmacy.rxReview.publicity', NULL,
        'pharmacy:rxReview:publicity', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2604', '处方点评-删除', '515', 3, 3, NULL, NULL, 'pharmacy.rxReview.delete', NULL, 'pharmacy:rxReview:delete',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2605', '处方点评-导出', '515', 4, 3, NULL, NULL, 'pharmacy.rxReview.export', NULL, 'pharmacy:rxReview:export',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2606', '处方点评-约谈', '515', 5, 3, NULL, NULL, 'pharmacy.rxReview.talk', NULL, 'pharmacy:rxReview:talk', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2610', '技术授权-新增/修改', '805', 1, 3, NULL, NULL, 'org.techAuth.add', NULL, 'org:techAuth:add', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2611', '技术授权-审批/驳回/收回', '805', 2, 3, NULL, NULL, 'org.techAuth.edit', NULL, 'org:techAuth:edit', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2612', '技术授权-删除', '805', 3, 3, NULL, NULL, 'org.techAuth.delete', NULL, 'org:techAuth:delete', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2613', '技术授权-越权确认', '805', 4, 3, NULL, NULL, 'org.techAuth.override', NULL, 'org:techAuth:override', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2614', '死亡证明-填写/修改', '318', 1, 3, NULL, NULL, 'ipd.deathCertificate.add', NULL,
        'ipd:deathCertificate:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2615', '死亡证明-审核/签发/作废', '318', 2, 3, NULL, NULL, 'ipd.deathCertificate.edit', NULL,
        'ipd:deathCertificate:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2616', '死亡证明-死因监测上报', '318', 3, 3, NULL, NULL, 'ipd.deathCertificate.report', NULL,
        'ipd:deathCertificate:report', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2617', '死亡证明-打印', '318', 4, 3, NULL, NULL, 'ipd.deathCertificate.print', NULL,
        'ipd:deathCertificate:print', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2618', '死亡登记-填写/修改', '318', 5, 3, NULL, NULL, 'ipd.deathRegister.add', NULL, 'ipd:deathRegister:add',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2619', '死亡登记-确认/作废', '318', 6, 3, NULL, NULL, 'ipd.deathRegister.edit', NULL, 'ipd:deathRegister:edit',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2620', '病危重通知-填写/修改', '319', 1, 3, NULL, NULL, 'ipd.criticalNotice.add', NULL,
        'ipd:criticalNotice:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2621', '病危重通知-签发/签收/作废', '319', 2, 3, NULL, NULL, 'ipd.criticalNotice.edit', NULL,
        'ipd:criticalNotice:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2622', '病危重通知-打印回执', '319', 3, 3, NULL, NULL, 'ipd.criticalNotice.print', NULL,
        'ipd:criticalNotice:print', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2623', '请假单-填写/修改', '320', 1, 3, NULL, NULL, 'ipd.leave.add', NULL, 'ipd:leave:add', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2624', '请假单-审批/离院/销假/取消', '320', 2, 3, NULL, NULL, 'ipd.leave.edit', NULL, 'ipd:leave:edit', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2625', '请假单-打印承诺书', '320', 3, 3, NULL, NULL, 'ipd.leave.print', NULL, 'ipd:leave:print', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2626', '扣款/飞检-新建/修改', '1010', 1, 3, NULL, NULL, 'finance.insuranceDeduct:add', NULL,
        'finance:insuranceDeduct:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2627', '扣款-申诉/确认/缴回/作废', '1010', 2, 3, NULL, NULL, 'finance.insuranceDeduct:edit', NULL,
        'finance:insuranceDeduct:edit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2628', '慢特病医保备案-新增/修改', '1011', 1, 3, NULL, NULL, 'finance.insuranceChronic:add', NULL,
        'finance:insuranceChronic:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2629', '慢特病医保备案-注销/驳回', '1011', 2, 3, NULL, NULL, 'finance.insuranceChronic:cancel', NULL,
        'finance:insuranceChronic:cancel', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2630', '满意度-模板与发放', '616', 1, 3, NULL, NULL, 'qc.survey.add', NULL, 'qc:survey:add', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2631', '满意度-答卷录入与作废', '616', 2, 3, NULL, NULL, 'qc.survey.edit', NULL, 'qc:survey:edit', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2640', '公卫上报-提交', '618', 1, 3, NULL, NULL, 'emr.publicHealth.add', NULL, 'emr:publicHealth:add', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2641', '公卫上报-审核', '618', 2, 3, NULL, NULL, 'emr.publicHealth.edit', NULL, 'emr:publicHealth:edit', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2642', '慢病建档与认定-建档认定', '208', 1, 3, NULL, NULL, 'opd.chronicRecord.add', NULL,
        'opd:chronicRecord:add', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2643', '慢病建档与认定-作废', '208', 2, 3, NULL, NULL, 'opd.chronicRecord.cancel', NULL,
        'opd:chronicRecord:cancel', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2644', '抗菌药物目录-分级维护', '517', 1, 3, NULL, NULL, 'pharmacy.antibioticCatalog.edit', NULL,
        'pharmacy:antibiotic:catalogEdit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2645', '抗菌药物-处方权授权', '2922', 1, 3, NULL, NULL, 'pharmacy.antibiotic.authEdit', NULL,
        'pharmacy:antibiotic:authEdit', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2646', '抗菌药物监测-生成统计', '518', 1, 3, NULL, NULL, 'pharmacy.antibiotic.statGenerate', NULL,
        'pharmacy:antibiotic:statGenerate', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2647', '抗菌药物监测-导出', '518', 2, 3, NULL, NULL, 'pharmacy.antibiotic.statExport', NULL,
        'pharmacy:antibiotic:statExport', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2648', 'I类切口点评-提交结论', '519', 1, 3, NULL, NULL, 'pharmacy.antibiotic.incisionReview', NULL,
        'pharmacy:antibiotic:incisionReview', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2650', '病区护理排班-排班维护', '331', 1, 3, '', '', 'nursing.schedule.add', '', 'nursing:schedule:add', 0, 0,
        1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2651', '病区护理排班-人力标准维护', '331', 2, 3, '', '', 'nursing.schedule.edit', '', 'nursing:schedule:edit',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2652', '病区护理排班-删除排班行', '331', 3, 3, '', '', 'nursing.schedule.delete', '',
        'nursing:schedule:delete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2653', 'VTE防控-措施登记落实', '332', 1, 3, NULL, NULL, 'nursing.vtePrevent.edit', NULL,
        'nursing:vte:preventEdit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2654', 'VTE防控-删除措施记录', '332', 2, 3, NULL, NULL, 'nursing.vtePrevent.delete', NULL,
        'nursing:vte:preventDelete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2655', 'VTE监测-事件登记', '333', 1, 3, NULL, NULL, 'nursing.vteMonitor.eventEdit', NULL,
        'nursing:vte:eventEdit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2656', 'VTE监测-生成统计', '333', 2, 3, NULL, NULL, 'nursing.vteMonitor.statGenerate', NULL,
        'nursing:vte:statGenerate', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2657', 'VTE监测-导出', '333', 3, 3, NULL, NULL, 'nursing.vteMonitor.statExport', NULL,
        'nursing:vte:statExport', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2658', '营养筛查-登记/修改', '424', 1, 3, NULL, NULL, 'ipd.nutritionScreen.edit', NULL,
        'ipd:nutrition:screenEdit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2659', '营养筛查-删除', '424', 2, 3, NULL, NULL, 'ipd.nutritionScreen.delete', NULL,
        'ipd:nutrition:screenDelete', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2660', '营养筛查-发起营养会诊', '424', 3, 3, NULL, NULL, 'ipd.nutritionScreen.consult', NULL,
        'ipd:nutrition:consultApply', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2661', '膳食方案-登记/修改', '425', 1, 3, NULL, NULL, 'ipd.dietPlan.edit', NULL, 'ipd:diet:edit', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2662', '膳食方案-营养科接收/退回', '425', 2, 3, NULL, NULL, 'ipd.dietPlan.confirm', NULL, 'ipd:diet:confirm',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2663', '订餐-批量生成', '426', 1, 3, NULL, NULL, 'ipd.mealOrder.generate', NULL, 'ipd:meal:generate', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2664', '订餐-配餐/配送/签收/退订', '426', 2, 3, NULL, NULL, 'ipd.mealOrder.status', NULL, 'ipd:meal:status', 0,
        0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2665', '营养会诊-应答/完成/取消', '427', 1, 3, NULL, NULL, 'ipd.nutritionConsult.edit', NULL,
        'ipd:nutrition:consultEdit', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2666', '营养指标-生成快照', '428', 1, 3, NULL, NULL, 'ipd.nutritionStats.generate', NULL,
        'ipd:nutrition:statGenerate', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2667', '营养指标-导出', '428', 2, 3, NULL, NULL, 'ipd.nutritionStats.export', NULL, 'ipd:nutrition:statExport',
        0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2668', '总值班排班-登记/修改', '806', 1, 3, NULL, NULL, 'org.dutyRoster.edit', NULL, 'org:duty:edit', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2669', '总值班排班-删除', '806', 2, 3, NULL, NULL, 'org.dutyRoster.delete', NULL, 'org:duty:delete', 0, 0, 1,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2670', '总值班排班-临时换班', '806', 3, 3, NULL, NULL, 'org.dutyRoster.substitute', NULL,
        'org:duty:substitute', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2671', '值班日志-登记/修改', '2920', 1, 3, NULL, NULL, 'org.dutyRoster.log.edit', NULL, 'org:duty:log:edit', 0,
        0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2672', '值班日志-删除', '2920', 2, 3, NULL, NULL, 'org.dutyRoster.log.delete', NULL, 'org:duty:log:delete', 0,
        0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2673', '值班日志-交班/签收', '2920', 3, 3, NULL, NULL, 'org.dutyRoster.log.handover', NULL,
        'org:duty:log:handover', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2901', '室间质评-新增', '416', 1, 3, NULL, NULL, 'medtech.lisEqa:add', NULL, 'medtech:lisEqa:add', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2902', '室间质评-修改', '416', 2, 3, NULL, NULL, 'medtech.lisEqa:edit', NULL, 'medtech:lisEqa:edit', 0, 1, 0,
        1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2903', '室间质评-删除', '416', 3, 3, NULL, NULL, 'medtech.lisEqa:delete', NULL, 'medtech:lisEqa:delete', 0, 1,
        0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2911', '护理质控-检查单维护', '334', 1, 3, '', '', 'nursing.qc.edit', '', 'nursing:qc:edit', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2912', '护理质控-台账重算与上报', '334', 2, 3, '', '', 'nursing.qc.calc', '', 'nursing:qc:calc', 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2913', '护理质控-删除', '334', 3, 3, '', '', 'nursing.qc.delete', '', 'nursing:qc:delete', 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2920', '总值班日志（交班本）', '800', 3, 2, '/duty-log', 'duty/DutyLogView', 'org.dutyRoster.log', 'Notebook',
        'org:duty:log:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2921', '耗材字典', '900', 40, 2, '/consumable-dict', 'supplies/ConsumableDictView', 'asset.consumableDict',
        'Goods', 'asset:consumableDict:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2922', '抗菌药物处方权管理', '500', 24, 2, '/antibiotic-auth', 'pharmacy/AntibioticAuthView',
        'pharmacy.antibioticAuth', 'Stamp', 'pharmacy:antibiotic:auth:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2923', '门诊慢特病病种目录', '1000', 42, 2, '/chronic-catalog', 'insurance/ChronicCatalogView',
        'finance.chronicCatalog', 'Collection', 'finance:chronicCatalog:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL,
        '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2924', '手术器械清点', '300', 6, 2, '/operation-count', 'surgery/OperationCountView', 'ipd.operationCount',
        'List', 'ipd:operationCount:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2925', '检查设备与号源', '400', 42, 2, '/exam-device-slot', 'exam-appointment/ExamDeviceSlotView',
        'medtech.examDeviceSlot', 'Tickets', 'medtech:examAppoint:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1',
        '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2926', '手术器械清点-建立清点单', '2924', 1, 3, NULL, NULL, 'ipd.operationCount:add', NULL,
        'ipd:operationCount:add', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2927', '手术器械清点-三阶段登记', '2924', 2, 3, NULL, NULL, 'ipd.operationCount:edit', NULL,
        'ipd:operationCount:edit', 0, 1, 0, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2928', '日间手术准入目录', '300', 42, 2, '/day-surgery-item', 'day-surgery/DaySurgeryItemView',
        'ipd.daySurgeryItem', 'Notebook', 'ipd:daySurgery:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2929', '线上问诊', '1300', 2, 2, '/online-consult', 'teleconsult/OnlineConsultView', 'ipd.onlineConsult',
        'Tickets', 'ipd:teleconsult:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2930', '体检套餐', '1400', 40, 2, '/checkup-package', 'checkup/CheckupPackageView', 'checkup.checkupPackage',
        'Goods', 'checkup:manage:list', 0, 0, 1, 1, 'admin', 'system', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2931', '血液净化机位管理', '400', 43, 2, '/dialysis-machine', 'dialysis/DialysisMachineView',
        'medtech.dialysisMachine', 'Grid', 'medtech:dialysis:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2932', '器械包模板', '900', 41, 2, '/cssd-template', 'cssd/CssdTemplateView', 'asset.cssdTemplate', 'Files',
        'asset:cssd:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2933', '部门管理-新增', '1109', 1, 3, NULL, NULL, 'system.dept:add', NULL, 'org:dept:add', 0, 1, 0, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2934', '部门管理-删除', '1109', 2, 3, NULL, NULL, 'system.dept:delete', NULL, 'org:dept:delete', 0, 1, 0, 1,
        'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2935', '全院岗位排班', '2945', 3, 2, '/staff-schedule', 'views/schedule/StaffScheduleView.vue',
        'org:staffSchedule', 'Grid', 'org:schedule:list', 0, 0, 1, 1, 'admin', 'sql230', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2936', '值守点位', '2945', 7, 2, '/duty-post', 'views/duty/DutyPostView.vue', 'org:dutyPost', 'Aim',
        'org:duty:list', 0, 0, 1, 1, 'admin', 'sql230', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2937', '人力配置标准', '2945', 5, 2, '/staff-plan-rule', 'views/schedule/StaffPlanRuleView.vue',
        'org:staffPlanRule', 'Tools', 'org:schedule:list', 0, 0, 1, 1, 'admin', 'sql230', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2938', '排班总览', '2945', 1, 2, '/schedule-overview', 'views/schedule/ScheduleOverview.vue',
        'org:scheduleOverview', 'TrendCharts', 'org:schedule:list', 0, 0, 1, 1, 'admin', 'sql230', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2940', 'AI 运营问数', '1200', 46, 2, '/ai-operation-qa', 'views/ai/OperationQaView.vue', 'ai.operationQa',
        'DataAnalysis', 'ai:operationQa:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2941', '问数查询', '1200', 1, 3, '', '', 'ai.operationQa.ask', '', 'ai:operationQa:ask', 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2942', 'AI 管理台', '1100', 44, 2, '/ai-admin', 'views/ai/AiAdminView.vue', 'ai.admin', 'Monitor',
        'ai:admin:list', 0, 0, 1, 1, 'admin', 'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2943', '知识库维护', '2942', 1, 3, '', '', 'ai.knowledge', '', 'ai:knowledge:manage', 0, 0, 1, 1, 'admin',
        'admin', 0, NULL, '1', '1');
INSERT INTO sys_menu (id, menu_name, parent_id, sort_order, menu_type, path, component, menu_key, icon, permission,
                      is_frame, is_cache, is_visible, status, create_by, update_by, del_flag, remark, create_by_id,
                      update_by_id)
VALUES ('2945', '排班中心', '0', 45, 1, '/schedule-center', NULL, 'org:scheduleCenter', 'Calendar', NULL, 0, 0, 1, 1,
        'admin', 'admin', 0, NULL, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
