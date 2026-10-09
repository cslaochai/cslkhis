SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS=0;
START TRANSACTION;
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1001', 'myTodo', '我的待办', 'todo', 'system.myTodo', 'portal:messages:view', 24, 10, 1, NULL, 'admin',
        'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1002', 'myNotice', '最新消息', 'notice', 'system.myNotice', 'portal:messages:view', 24, 20, 1, NULL, 'admin',
        'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1003', 'quickEntry', '常用入口', 'entry', 'menu.userMenus', NULL, 24, 30, 1, NULL, 'admin', 'admin', 0, '1',
        '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1004', 'hospitalToday', '今日全院概况', 'kpi', 'report.hospitalToday', 'report:stats:list', 24, 40, 1, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1005', 'deptVisitRank', '科室就诊量排行', 'domain', 'report.deptVisitRank', 'report:stats:list', 12, 200, 1,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1006', 'weekVisitTrend', '七日到诊趋势', 'domain', 'report.weekVisitTrend', 'report:stats:list', 12, 210, 1,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1007', 'myClinicalToday', '我的今日诊疗', 'kpi', 'report.clinicalToday', 'opd:doctorWorkstation:list', 24, 50,
        1, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1008', 'wardNursingToday', '病区今日概况', 'kpi', 'report.wardNursingToday', 'ipd:nurse:list', 24, 60, 1, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1011', 'myScheduleToday', '我的今日排班', 'domain', 'appoint.myScheduleToday', 'opd:doctorWorkstation:list', 6,
        50, 0, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1012', 'waitingQueue', '候诊队列', 'domain', 'appoint.waitingQueue', 'opd:doctorWorkstation:list', 6, 60, 0,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1013', 'registToday', '今日挂号量', 'domain', 'appoint.registToday', 'opd:appointments:list', 6, 70, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1014', 'refundPending', '退号待审核', 'domain', 'appoint.refundPending', 'opd:appointments:list', 6, 80, 0,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1015', 'triagePending', '分诊待处理', 'domain', 'opd.triagePending', 'opd:triage:list', 6, 90, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1016', 'emergencyTodo', '急诊待处理', 'domain', 'emergency.emergencyTodo', 'opd:emergency:list', 6, 100, 0,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1017', 'myInpatient', '我的住院患者', 'domain', 'patient.myInpatient', 'ipd:inpatient:list', 6, 110, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1018', 'todoOrderVerify', '待校对医嘱', 'domain', 'patient.todoOrderVerify', 'ipd:order:list', 6, 120, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1019', 'todoOrderExec', '待执行医嘱', 'domain', 'patient.todoOrderExec', 'ipd:nurse:list', 6, 130, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1020', 'myConsultTodo', '待我会诊', 'domain', 'patient.myConsultTodo', 'ipd:consultation:list', 6, 140, 0,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1021', 'wardBed', '床位使用率', 'domain', 'patient.wardBed', 'ipd:nurse:list', 6, 150, 0, NULL, 'admin',
        'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1022', 'wardAdmitDischarge', '今日入出院', 'domain', 'patient.wardAdmitDischarge', 'ipd:nurse:list', 6, 160, 0,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1023', 'criticalValueTodo', '待处理危急值', 'domain', 'emr.criticalValueTodo', 'medtech:criticalValue:list', 6,
        170, 0, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1024', 'myArchivePending', '待归档病历', 'domain', 'emr.myArchivePending', 'emr:archive:list', 6, 180, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1025', 'dispenseTodo', '待发药', 'domain', 'pharmacy.dispenseTodo', 'pharmacy:dispensing:list', 6, 190, 0,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1026', 'prescriptionPending', '待审核处方', 'domain', 'pharmacy.prescriptionPending',
        'pharmacy:prescriptionAudit:list', 6, 195, 0, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1027', 'stockAlert', '近效期与缺药', 'domain', 'pharmacy.stockAlert', 'pharmacy:stock:list', 6, 198, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1028', 'wardDispenseTodo', '病区待摆药', 'domain', 'pharmacy.wardDispenseTodo', 'pharmacy:wardDispense:list',
        6, 199, 0, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1029', 'specimenTodo', '待接收标本', 'domain', 'medtech.specimenTodo', 'medtech:specimen:list', 6, 220, 0,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1030', 'labReportTodo', '检验待出报告', 'domain', 'medtech.labReportTodo',
        'medtech:laboratoryWorkstation:list', 6, 230, 0, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1031', 'insReportTodo', '检查待出报告', 'domain', 'medtech.insReportTodo',
        'medtech:inspectionWorkstation:list', 6, 240, 0, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1032', 'lisQcDue', '室内质控到期', 'domain', 'medtech.lisQcDue', 'medtech:lisQc:list', 6, 250, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1033', 'codingTask', '编码任务池', 'domain', 'emr.codingTask', 'emr:codeTask:list', 6, 260, 0, NULL, 'admin',
        'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1034', 'recordQcTodo', '待质控病历', 'domain', 'emr.recordQcTodo', 'qc:recordQc:list', 6, 270, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1035', 'archiveBorrowTodo', '借阅待审批', 'domain', 'emr.archiveBorrowTodo', 'emr:archiveBorrow:list', 6, 275,
        0, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1036', 'chargeToday', '今日收费', 'domain', 'charge.chargeToday', 'finance:cashier:list', 6, 280, 0, NULL,
        'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1037', 'arrearsPatient', '欠费患者', 'domain', 'charge.arrearsPatient', 'charge:arrearsControl:list', 6, 285,
        0, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1038', 'invoiceAbnormal', '发票异常', 'domain', 'charge.invoiceAbnormal', 'finance:invoice:list', 6, 288, 0,
        NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1039', 'insuranceSettle', '医保待结算', 'domain', 'charge.insuranceSettle', 'finance:insurance:list', 6, 290,
        0, NULL, 'admin', 'admin', 0, '1', '1');
INSERT INTO sys_workbench_widget (id, widget_code, widget_name, area, api_key, permission, default_span, sort_order,
                                  status, remark, create_by, update_by, del_flag, create_by_id, update_by_id)
VALUES ('1040', 'drgAbnormal', 'DRG分组异常', 'domain', 'report.drgAbnormal', 'report:drg:list', 6, 295, 0, NULL,
        'admin', 'admin', 0, '1', '1');
COMMIT;
SET
FOREIGN_KEY_CHECKS=1;
