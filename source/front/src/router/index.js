import {createRouter, createWebHistory} from 'vue-router';

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/LoginView.vue'),
    },
    {
        path: '/',
        component: () => import('@/components/his/DashboardLayout.vue'),
        children: [
            {path: '', name: 'Workbench', component: () => import('@/views/dashboard/WorkbenchView.vue')},
            {path: 'messages', name: 'Messages', component: () => import('@/views/messages/MessagesView.vue')},
            {
                path: 'appointments',
                name: 'Appointments',
                component: () => import('@/views/appointments/AppointmentsView.vue')
            },
            {path: 'triage', name: 'Triage', component: () => import('@/views/triage/TriageView.vue')},
            {
                path: 'doctor-workstation',
                name: 'DoctorWorkstation',
                component: () => import('@/views/doctor-workstation/DoctorWorkstationView.vue'),
                meta: {patientWorkspace: true}
            },
            {
                path: 'records',
                name: 'Records',
                component: () => import('@/views/records/RecordsView.vue'),
                meta: {title: '门诊病历查询（已下线）', standalone: true}
            },
            {
                path: 'today-visits',
                name: 'TodayVisits',
                component: () => import('@/views/today-visits/TodayVisitsView.vue')
            },
            {
                path: 'outpatient-log',
                name: 'OutpatientLog',
                component: () => import('@/views/outpatient-log/OutpatientLogView.vue'),
                meta: {title: '门诊日志'}
            },
            {
                path: 'my-unreported',
                name: 'MyUnreported',
                component: () => import('@/views/outpatient-log/MyUnreportedView.vue'),
                meta: {title: '我的未报'}
            },
            {
                path: 'emergency',
                name: 'Emergency',
                component: () => import('@/views/emergency/EmergencyView.vue'),
                meta: {title: '急诊中心'}
            },
            {path: 'inpatient', name: 'Inpatient', component: () => import('@/views/inpatient/InpatientView.vue')},
            {
                path: 'inpatient-order',
                name: 'InpatientOrder',
                component: () => import('@/views/inpatient/InpatientOrderView.vue')
            },
            {
                path: 'inpatient-record',
                name: 'InpatientRecord',
                component: () => import('@/views/inpatient/InpatientRecordView.vue')
            },
            {
                path: 'inpatient-settlement',
                name: 'InpatientSettlement',
                component: () => import('@/views/inpatient/InpatientSettlementView.vue')
            },
            {
                path: 'order-base-dict',
                name: 'OrderBaseDict',
                component: () => import('@/views/inpatient/OrderBaseDictView.vue'),
                meta: {title: '医嘱基础字典'}
            },
            {
                path: 'order-set',
                name: 'OrderSetTemplate',
                component: () => import('@/views/inpatient/OrderSetTemplateView.vue'),
                meta: {title: '医嘱组套（套餐）维护'}
            },
            {path: 'nurse', name: 'Nurse', component: () => import('@/views/nurse/NurseView.vue')},
            {
                path: 'nurse-schedule',
                name: 'NurseSchedule',
                component: () => import('@/views/nurse/NurseScheduleView.vue'),
                meta: {title: '病区护理排班'}
            },
            {
                path: 'nursing-qc',
                name: 'NursingQc',
                component: () => import('@/views/nurse/NursingQcView.vue'),
                meta: {title: '护理质控'}
            },
            {path: 'surgery', name: 'Surgery', component: () => import('@/views/surgery/SurgeryView.vue')},
            {
                path: 'anesthesia',
                name: 'Anesthesia',
                component: () => import('@/views/anesthesia/AnesthesiaView.vue'),
                meta: {title: '麻醉工作站'}
            },
            {
                path: 'operation-count',
                name: 'OperationCount',
                component: () => import('@/views/surgery/OperationCountView.vue'),
                meta: {title: '手术器械清点'}
            },
            {
                path: 'infectious-report',
                name: 'InfectiousReport',
                component: () => import('@/views/infectious-report/InfectiousReportView.vue'),
                meta: {title: '传染病报告卡'}
            },
            {
                path: 'infection-monitor',
                name: 'InfectionMonitor',
                component: () => import('@/views/infection-monitor/InfectionMonitorView.vue'),
                meta: {title: '院感监测'}
            },
            {
                path: 'hand-hygiene',
                name: 'HandHygiene',
                component: () => import('@/views/infection-monitor/HandHygieneView.vue'),
                meta: {standalone: true, title: '手卫生依从性（已并入院感监测）'}
            },
            {
                path: 'public-health',
                name: 'PublicHealth',
                component: () => import('@/views/public-health/PublicHealthReportView.vue'),
                meta: {title: '公卫上报'}
            },
            {
                path: 'consultation',
                name: 'Consultation',
                component: () => import('@/views/consultation/ConsultationView.vue')
            },
            {
                path: 'transfer',
                name: 'Transfer',
                component: () => import('@/views/transfer/TransferView.vue'),
                meta: {title: '转科管理'}
            },
            {
                path: 'bed-center',
                name: 'BedCenter',
                component: () => import('@/views/bedcenter/BedCenterView.vue'),
                meta: {title: '床位管理'}
            },
            {
                path: 'death-certificate',
                name: 'DeathCertificate',
                component: () => import('@/views/death/DeathCertificateView.vue'),
                meta: {title: '死亡证明与登记'}
            },
            {
                path: 'inpatient-leave',
                name: 'InpatientLeave',
                component: () => import('@/views/inpatientLeave/InpatientLeaveView.vue'),
                meta: {title: '患者请假离院'}
            },
            {
                path: 'critical-notice',
                name: 'CriticalNotice',
                component: () => import('@/views/critical/CriticalNoticeView.vue'),
                meta: {title: '病危重通知'}
            },
            {
                path: 'discharge-drug',
                name: 'DischargeDrug',
                component: () => import('@/views/dischargedrug/DischargeDrugView.vue')
            },
            {path: 'followup', name: 'Followup', component: () => import('@/views/followup/FollowupView.vue')},
            {path: 'referral', name: 'Referral', component: () => import('@/views/referral/ReferralView.vue')},
            {
                path: 'arrears-control',
                name: 'ArrearsControl',
                component: () => import('@/views/arrears/ArrearsControlView.vue')
            },
            {
                path: 'inspection-workstation',
                name: 'InspectionWorkstation',
                component: () => import('@/views/lab/InspectionView.vue')
            },
            {
                path: 'radio-diagnosis',
                name: 'RadioDiagnosis',
                component: () => import('@/views/lab/RadioDiagnosisView.vue'),
                meta: {title: '放射诊断工作站'}
            },
            {
                path: 'exam-film',
                name: 'ExamFilm',
                component: () => import('@/views/lab/ExamFilmView.vue'),
                meta: {title: '胶片量方与发放'}
            },
            {
                path: 'ecg',
                name: 'Ecg',
                component: () => import('@/views/lab/EcgWorkstationView.vue'),
                meta: {title: '心电工作站'}
            },
            {
                path: 'laboratory-workstation',
                name: 'LaboratoryWorkstation',
                component: () => import('@/views/lab/LaboratoryView.vue')
            },
            {path: 'specimen', name: 'Specimen', component: () => import('@/views/specimen/SpecimenView.vue')},
            {path: 'pathology', name: 'Pathology', component: () => import('@/views/pathology/PathologyView.vue')},
            {path: 'endoscopy', name: 'Endoscopy', component: () => import('@/views/endoscopy/EndoscopyView.vue')},
            {path: 'ultrasound', name: 'Ultrasound', component: () => import('@/views/ultrasound/UltrasoundView.vue')},
            {path: 'lis-qc', name: 'LisQc', component: () => import('@/views/lisqc/LisQcView.vue')},
            {path: 'lis-eqa', name: 'LisEqa', component: () => import('@/views/lisqc/LisEqaView.vue')},
            {path: 'blood-bank', name: 'BloodBank', component: () => import('@/views/bloodbank/BloodBankView.vue')},
            {
                path: 'exam-appointment',
                name: 'ExamAppointment',
                component: () => import('@/views/exam-appointment/ExamAppointmentView.vue')
            },
            {
                path: 'exam-device-slot',
                name: 'ExamDeviceSlot',
                component: () => import('@/views/exam-appointment/ExamDeviceSlotView.vue'),
                meta: {title: '检查设备与号源'}
            },
            {
                path: 'treatment-station',
                name: 'TreatmentStation',
                component: () => import('@/views/treatment/TreatmentStationView.vue'),
                meta: {title: '门诊治疗站'}
            },
            {
                path: 'chronic-record',
                name: 'ChronicRecord',
                component: () => import('@/views/chronic/ChronicRecordView.vue'),
                meta: {title: '慢病建档与认定'}
            },
            {path: 'pharmacy', name: 'Pharmacy', component: () => import('@/views/pharmacy/PharmacyView.vue')},
            {
                path: 'pharmacy-window',
                name: 'PharmacyWindow',
                component: () => import('@/views/pharmacy-window/PharmacyWindowView.vue')
            },
            {
                path: 'prescription-audit',
                name: 'PrescriptionAudit',
                component: () => import('@/views/pharmacy-window/PrescriptionAuditView.vue')
            },
            {
                path: 'narcotic-register',
                name: 'NarcoticRegister',
                component: () => import('@/views/pharmacy-window/NarcoticRegisterView.vue')
            },
            {
                path: 'purchase-order',
                name: 'PurchaseOrder',
                component: () => import('@/views/pharmacy/PurchaseOrderView.vue')
            },
            {
                path: 'drug-inbound',
                name: 'DrugInbound',
                component: () => import('@/views/pharmacy/DrugInboundView.vue')
            },
            {path: 'supplier', name: 'Supplier', component: () => import('@/views/pharmacy/SupplierView.vue')},
            {path: 'rx-review', name: 'RxReview', component: () => import('@/views/pharmacy/RxReviewView.vue')},
            {
                path: 'rx-publicity',
                name: 'RxPublicity',
                component: () => import('@/views/pharmacy/RxPublicityView.vue')
            },
            {
                path: 'antibiotic-catalog',
                name: 'AntibioticCatalog',
                component: () => import('@/views/pharmacy/AntibioticCatalogView.vue'),
                meta: {title: '抗菌药物分级目录'}
            },
            {
                path: 'antibiotic-auth',
                name: 'AntibioticAuth',
                component: () => import('@/views/pharmacy/AntibioticAuthView.vue'),
                meta: {title: '抗菌药物处方权管理'}
            },
            {
                path: 'antibiotic-monitor',
                name: 'AntibioticMonitor',
                component: () => import('@/views/pharmacy/AntibioticMonitorView.vue'),
                meta: {title: '抗菌药物使用监测'}
            },
            {
                path: 'antibiotic-incision',
                name: 'AntibioticIncision',
                component: () => import('@/views/pharmacy/AntibioticIncisionView.vue'),
                meta: {title: 'I类切口预防用药点评'}
            },
            {
                path: 'vte-prevent',
                name: 'VtePrevent',
                component: () => import('@/views/nurse/VtePreventView.vue'),
                meta: {title: 'VTE 风险防控'}
            },
            {
                path: 'vte-monitor',
                name: 'VteMonitor',
                component: () => import('@/views/nurse/VteMonitorView.vue'),
                meta: {title: '院内 VTE 监测'}
            },
            {
                path: 'nutrition-screen',
                name: 'NutritionScreen',
                component: () => import('@/views/nutrition/NutritionScreenView.vue'),
                meta: {title: '营养风险筛查'}
            },
            {
                path: 'diet-plan',
                name: 'DietPlan',
                component: () => import('@/views/nutrition/DietPlanView.vue'),
                meta: {title: '膳食医嘱执行'}
            },
            {
                path: 'meal-order',
                name: 'MealOrder',
                component: () => import('@/views/nutrition/MealOrderView.vue'),
                meta: {title: '订餐配送'}
            },
            {
                path: 'nutrition-consult',
                name: 'NutritionConsult',
                component: () => import('@/views/nutrition/NutritionConsultView.vue'),
                meta: {title: '营养会诊'}
            },
            {
                path: 'nutrition-stats',
                name: 'NutritionStats',
                component: () => import('@/views/nutrition/NutritionStatsView.vue'),
                meta: {title: '营养指标监测'}
            },
            {path: 'cashier', name: 'Cashier', component: () => import('@/views/cashier/CashierView.vue')},
            {
                path: 'finance',
                name: 'Finance',
                component: () => import('@/views/finance/FinanceView.vue'),
                meta: {title: '财务管理', standalone: true}
            },
            {path: 'insurance', name: 'Insurance', component: () => import('@/views/insurance/InsuranceView.vue')},
            {
                path: 'insurance-mapping',
                name: 'InsuranceMapping',
                component: () => import('@/views/insurance/InsuranceMappingView.vue')
            },
            {
                path: 'insurance-deduct',
                name: 'InsuranceDeduct',
                component: () => import('@/views/insurance/InsuranceDeductView.vue')
            },
            {
                path: 'insurance-chronic',
                name: 'InsuranceChronic',
                component: () => import('@/views/insurance/InsuranceChronicView.vue')
            },
            {
                path: 'chronic-catalog',
                name: 'ChronicCatalog',
                component: () => import('@/views/insurance/ChronicCatalogView.vue'),
                meta: {title: '门诊慢特病病种目录'}
            },
            {
                path: 'compliance-audit',
                name: 'ComplianceAudit',
                component: () => import('@/views/insurance/ComplianceAuditView.vue'),
                meta: {title: '医保合规审核台账'}
            },
            {path: 'invoice', name: 'Invoice', component: () => import('@/views/invoice/InvoiceView.vue')},
            {path: 'refund', name: 'Refund', component: () => import('@/views/refund/RefundView.vue')},
            {
                path: 'refund-flow',
                name: 'RefundFlow',
                component: () => import('@/views/refund-flow/RefundFlowView.vue')
            },
            {path: 'fee-record', name: 'FeeRecord', component: () => import('@/views/fee-record/FeeRecordView.vue')},
            {
                path: 'settlement-bill',
                name: 'SettlementBill',
                component: () => import('@/views/settlement-bill/SettlementBillView.vue')
            },
            {
                path: 'payment-txn',
                name: 'PaymentTxn',
                component: () => import('@/views/payment-txn/PaymentTxnView.vue')
            },
            {
                path: 'fund-account',
                name: 'FundAccount',
                component: () => import('@/views/fund-account/FundAccountView.vue')
            },
            {
                path: 'insurance-policy',
                name: 'InsurancePolicy',
                component: () => import('@/views/insurance-policy/InsurancePolicyView.vue')
            },
            {
                path: 'revisit-policy',
                name: 'RevisitPolicy',
                component: () => import('@/views/revisit-policy/RevisitFeePolicyView.vue')
            },
            {path: 'price', name: 'Price', component: () => import('@/views/price/PriceView.vue')},
            {path: 'pay-channel', name: 'PayChannel', component: () => import('@/views/finance/PayChannelView.vue')},
            {
                path: 'medical-record',
                name: 'MedicalRecord',
                component: () => import('@/views/medical-record/MedicalRecordView.vue')
            },
            {
                path: 'medical-review',
                name: 'MedicalReview',
                component: () => import('@/views/medical-record/MedicalReviewView.vue')
            },
            {
                path: 'critical-value',
                name: 'CriticalValue',
                component: () => import('@/views/critical-value/CriticalValueView.vue')
            },
            {
                path: 'adverse-event',
                name: 'AdverseEvent',
                component: () => import('@/views/adverse-event/AdverseEventView.vue'),
                meta: {title: '不良事件', standalone: true}
            },
            {
                path: 'transfusion',
                name: 'Transfusion',
                component: () => import('@/views/transfusion/TransfusionView.vue')
            },
            {path: 'patients', name: 'Patients', component: () => import('@/views/patients/PatientsView.vue')},
            {path: 'empi', name: 'PatientIndex', component: () => import('@/views/empi/PatientIndexView.vue')},
            {path: 'cdr', name: 'PatientCdr', component: () => import('@/views/cdr/PatientCdrView.vue')},
            {path: 'data-quality', name: 'DataQuality', component: () => import('@/views/quality/DataQualityView.vue')},
            {
                path: 'medical-record-qc',
                name: 'MedicalRecordQc',
                component: () => import('@/views/quality/MedicalRecordQcView.vue')
            },
            {
                path: 'medical-record-archive',
                name: 'MedicalRecordArchive',
                component: () => import('@/views/medical-record/MedicalRecordArchiveView.vue')
            },
            {
                path: 'archive-borrow',
                name: 'ArchiveBorrow',
                component: () => import('@/views/medical-record/ArchiveBorrowView.vue'),
                meta: {title: '病案借阅复印'}
            },
            {
                path: 'coding-task',
                name: 'CodingTask',
                component: () => import('@/views/medical-record/CodingTaskView.vue'),
                meta: {title: '编码任务池'}
            },
            {
                path: 'ward-dispense',
                name: 'WardDispense',
                component: () => import('@/views/pharmacy/WardDispenseView.vue'),
                meta: {title: '住院摆药'}
            },
            {
                path: 'pivas',
                name: 'Pivas',
                component: () => import('@/views/pharmacy/PivasView.vue'),
                meta: {title: '静配中心'}
            },
            {
                path: 'stocktake',
                name: 'Stocktake',
                component: () => import('@/views/pharmacy/StocktakeView.vue'),
                meta: {title: '药房盘点'}
            },
            {
                path: 'tcm-decoct',
                name: 'TcmDecoct',
                component: () => import('@/views/pharmacy/TcmDecoctView.vue'),
                meta: {title: '中药代煎'}
            },
            {
                path: 'drug-transfer',
                name: 'DrugTransfer',
                component: () => import('@/views/pharmacy/DrugTransferView.vue'),
                meta: {title: '药品调拨'}
            },
            {
                path: 'supplier-return',
                name: 'SupplierReturn',
                component: () => import('@/views/pharmacy/SupplierReturnView.vue'),
                meta: {title: '供应商退货'}
            },
            {
                path: 'drug-trace',
                name: 'DrugTrace',
                component: () => import('@/views/pharmacy/DrugTraceView.vue'),
                meta: {title: '药品追溯码'}
            },
            {
                path: 'clinical-path',
                name: 'ClinicalPath',
                component: () => import('@/views/clinical-path/ClinicalPathView.vue'),
                meta: {title: '临床路径'}
            },
            {
                path: 'dialysis',
                name: 'Dialysis',
                component: () => import('@/views/dialysis/DialysisView.vue'),
                meta: {title: '血液净化中心'}
            },
            {
                path: 'dialysis-machine',
                name: 'DialysisMachine',
                component: () => import('@/views/dialysis/DialysisMachineView.vue'),
                meta: {title: '血液净化机位管理'}
            },
            {
                path: 'icu',
                name: 'Icu',
                component: () => import('@/views/icu/IcuView.vue'),
                meta: {title: 'ICU 专科监护'}
            },
            {
                path: 'teleconsult',
                name: 'TeleConsult',
                component: () => import('@/views/teleconsult/TeleConsultView.vue'),
                meta: {title: '远程会诊'}
            },
            {
                path: 'online-consult',
                name: 'OnlineConsult',
                component: () => import('@/views/teleconsult/OnlineConsultView.vue'),
                meta: {title: '线上问诊'}
            },
            {
                path: 'day-surgery',
                name: 'DaySurgery',
                component: () => import('@/views/day-surgery/DaySurgeryView.vue'),
                meta: {title: '日间手术'}
            },
            {
                path: 'day-surgery-item',
                name: 'DaySurgeryItem',
                component: () => import('@/views/day-surgery/DaySurgeryItemView.vue'),
                meta: {title: '日间手术准入目录'}
            },
            {
                path: 'dispute',
                name: 'Dispute',
                component: () => import('@/views/dispute/DisputeView.vue'),
                meta: {title: '医疗纠纷与投诉'}
            },
            {
                path: 'survey',
                name: 'Survey',
                component: () => import('@/views/survey/SurveyView.vue'),
                meta: {title: '满意度评价'}
            },
            {
                path: 'infusionRoom',
                name: 'InfusionRoom',
                component: () => import('@/views/infusion/InfusionRoomView.vue'),
                meta: {title: '门诊输液站'}
            },
            {
                path: 'singleDisease',
                name: 'SingleDisease',
                component: () => import('@/views/quality/SingleDiseaseView.vue'),
                meta: {title: '单病种质控'}
            },
            {
                path: 'signature-center',
                name: 'SignatureCenter',
                component: () => import('@/views/sign/SignatureCenterView.vue')
            },
            {
                path: 'health-record',
                name: 'HealthRecord',
                component: () => import('@/views/health-record/HealthRecordView.vue'),
                meta: {title: '健康档案'}
            },
            {
                path: 'doctors',
                name: 'Doctors',
                component: () => import('@/views/doctors/DoctorsView.vue'),
                meta: {title: '医生管理'}
            },
            {path: 'schedule', name: 'Schedule', component: () => import('@/views/schedule/ScheduleView.vue')},
            {
                path: 'staff-schedule',
                name: 'StaffSchedule',
                component: () => import('@/views/schedule/StaffScheduleView.vue'),
                meta: {title: '全院岗位排班'}
            },
            {
                path: 'schedule-overview',
                name: 'ScheduleOverview',
                component: () => import('@/views/schedule/ScheduleOverview.vue'),
                meta: {title: '排班总览'}
            },
            {
                path: 'staff-plan-rule',
                name: 'StaffPlanRule',
                component: () => import('@/views/schedule/StaffPlanRuleView.vue'),
                meta: {title: '人力配置标准'}
            },
            {
                path: 'perf',
                name: 'Performance',
                component: () => import('@/views/performance/PerformanceView.vue'),
                meta: {title: '绩效与成本核算'}
            },
            {
                path: 'checkup',
                name: 'Checkup',
                component: () => import('@/views/checkup/CheckupView.vue'),
                meta: {title: '体检管理'}
            },
            {
                path: 'checkup-package',
                name: 'CheckupPackage',
                component: () => import('@/views/checkup/CheckupPackageView.vue'),
                meta: {title: '体检套餐'}
            },
            {
                path: 'bi',
                name: 'BiDashboard',
                component: () => import('@/views/bi/BiDashboard.vue'),
                meta: {title: 'BI 驾驶舱'}
            },
            {
                path: 'drgsim',
                name: 'DrgSim',
                component: () => import('@/views/drg/DrgSimView.vue'),
                meta: {title: 'DRG-DIP 分组模拟'}
            },
            {
                path: 'statreport',
                name: 'StatReport',
                component: () => import('@/views/statreport/StatReportView.vue'),
                meta: {title: '病案统计上报'}
            },
            {
                path: 'duty-roster',
                name: 'DutyRoster',
                component: () => import('@/views/duty/DutyRosterView.vue'),
                meta: {title: '总值班排班'}
            },
            {
                path: 'duty-log',
                name: 'DutyLog',
                component: () => import('@/views/duty/DutyLogView.vue'),
                meta: {title: '总值班日志（交班本）'}
            },
            {
                path: 'duty-post',
                name: 'DutyPost',
                component: () => import('@/views/duty/DutyPostView.vue'),
                meta: {title: '值守点位'}
            },
            {
                path: 'departments',
                name: 'Departments',
                component: () => import('@/views/departments/DepartmentsView.vue')
            },
            {
                path: 'inspection-items',
                name: 'InspectionItems',
                component: () => import('@/views/medical-items/InspectionItemsView.vue')
            },
            {
                path: 'laboratory-items',
                name: 'LaboratoryItems',
                component: () => import('@/views/medical-items/LaboratoryItemsView.vue')
            },
            {
                path: 'equipment',
                name: 'Equipment',
                component: () => import('@/views/equipment/EquipmentView.vue'),
                meta: {title: '设备管理'}
            },
            {path: 'supplies', name: 'Supplies', component: () => import('@/views/supplies/SuppliesView.vue')},
            {
                path: 'consumable-dict',
                name: 'ConsumableDict',
                component: () => import('@/views/supplies/ConsumableDictView.vue'),
                meta: {title: '耗材字典'}
            },
            {
                path: 'cssd',
                name: 'Cssd',
                component: () => import('@/views/cssd/CssdView.vue'),
                meta: {title: '消毒供应 CSSD'}
            },
            {
                path: 'cssd-template',
                name: 'CssdTemplate',
                component: () => import('@/views/cssd/CssdTemplateView.vue'),
                meta: {title: '器械包模板'}
            },
            {
                path: 'waste',
                name: 'Waste',
                component: () => import('@/views/waste/WasteView.vue'),
                meta: {title: '医废管理'}
            },
            {path: 'system/user', name: 'SystemUser', component: () => import('@/views/system/user/UserView.vue')},
            {path: 'system/role', name: 'SystemRole', component: () => import('@/views/system/role/RoleView.vue')},
            {path: 'system/menu', name: 'SystemMenu', component: () => import('@/views/system/menu/MenuView.vue')},
            {
                path: 'system/dept',
                name: 'SystemDept',
                component: () => import('@/views/system/dept/DeptView.vue'),
                meta: {title: '部门管理'}
            },
            {
                path: 'system/employee',
                name: 'SystemEmployee',
                component: () => import('@/views/system/employee/EmployeeView.vue')
            },
            {
                path: 'system/techAuth',
                name: 'SystemTechAuth',
                component: () => import('@/views/system/techAuth/TechAuthView.vue')
            },
            {
                path: 'system/clinicRoom',
                name: 'SystemClinicRoom',
                component: () => import('@/views/system/clinicRoom/ClinicRoomView.vue')
            },
            {path: 'system/dict', name: 'SystemDict', component: () => import('@/views/system/dict/DictView.vue')},
            {
                path: 'system/config',
                name: 'SystemConfig',
                component: () => import('@/views/system/config/ConfigView.vue')
            },
            {
                path: 'system/patientTag',
                name: 'SystemPatientTag',
                component: () => import('@/views/system/tag/PatientTagView.vue')
            },
            {
                path: 'system/patientFaq',
                name: 'SystemPatientFaq',
                component: () => import('@/views/system/faq/FaqView.vue'),
                meta: {title: '患者常见问题'}
            },
            {
                path: 'system/labPlain',
                name: 'SystemLabPlain',
                component: () => import('@/views/system/faq/LabPlainItemView.vue'),
                meta: {title: '检验项目白话词典'}
            },
            {
                path: 'system/serviceTicket',
                name: 'SystemServiceTicket',
                component: () => import('@/views/system/service/ServiceTicketView.vue'),
                meta: {title: '工单受理'}
            },
            {
                path: 'system/workbench',
                name: 'SystemWorkbench',
                component: () => import('@/views/system/workbench/WorkbenchConfigView.vue')
            },
            {
                path: 'system/drugKnowledge',
                name: 'SystemDrugKnowledge',
                component: () => import('@/views/system/DrugKnowledgeView.vue')
            },
            {path: 'system/log', name: 'SystemLog', component: () => import('@/views/system/LogView.vue')},
            {
                path: 'reports',
                name: 'Reports',
                component: () => import('@/views/reports/ReportsView.vue'),
                meta: {title: '运营统计报表'}
            },
            {
                path: 'ai-operation-qa',
                name: 'AiOperationQa',
                component: () => import('@/views/ai/OperationQaView.vue'),
                meta: {title: 'AI 运营问数'}
            },
            {
                path: 'ai-admin',
                name: 'AiAdmin',
                component: () => import('@/views/ai/AiAdminView.vue'),
                meta: {title: 'AI 管理台'}
            },
        ],
    },
];
const router = createRouter({
    history: createWebHistory(),
    routes,
});
export default router;