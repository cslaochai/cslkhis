import { createRouter, createWebHistory } from 'vue-router';
// 说明：
//  1. 侧边栏与面包屑都从后端 sys_menu 表取菜单（见 source/back_end/sql/52-菜单表重建.sql），
//     路由本身**不再重复维护菜单层级**。
//  2. `meta.title` 只给「没有挂菜单」的页面兜底面包屑（假壳页、已合并入口等）：
//     菜单里有的页面，面包屑一律以菜单为准，这里的 title 不生效。
//  3. 新增页面如果要出现在侧边栏，除了在这里注册路由，还要在「系统管理 → 菜单管理」里加一条菜单。
//
//  4. `meta.standalone: true` = **不参与当前角色菜单判权**，见 `router/guard.js`。
//     打这个标记的只有两类：
//       a) 已下线/兼容保留的页面（假壳页、`records` / `hand-hygiene` 旧链接）——它们本来就不挂菜单，
//          挂不上菜单≠这个角色没权限，拦掉是误伤；
//       b) 页面内部的自有子路由（若将来出现），权限由父级页面自行判定。
//     新增业务页面**不要**打这个标记 —— 打上就等于绕过了角色菜单判权。
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
            // 门户工作台：所有角色共用的唯一首页，画哪几张卡由 /workbench/config 按角色算
            // （菜单 101 的 component 已改为 dashboard/WorkbenchView，见 sql/105）
            { path: '', name: 'Workbench', component: () => import('@/views/dashboard/WorkbenchView.vue') },
            { path: 'messages', name: 'Messages', component: () => import('@/views/messages/MessagesView.vue') },
            { path: 'appointments', name: 'Appointments', component: () => import('@/views/appointments/AppointmentsView.vue') },
            { path: 'triage', name: 'Triage', component: () => import('@/views/triage/TriageView.vue') },
            { path: 'doctor-workstation', name: 'DoctorWorkstation', component: () => import('@/views/doctor-workstation/DoctorWorkstationView.vue'), meta: { patientWorkspace: true } },
            // 601「门诊电子病历查询」已于 2026-09-23 摘除菜单（见 sql/74-摘除门诊电子病历查询菜单.sql）：
            // 它与 602「门诊病案首页」同表同接口同检索、信息量更少，且没有任何岗位的工作流会走到它。
            // 页面文件保留不删（同 /emr、/billing 等已下线页的惯例），故打 standalone 保住直接访问能力。
            { path: 'records', name: 'Records', component: () => import('@/views/records/RecordsView.vue'), meta: { title: '门诊病历查询（已下线）', standalone: true } },
            { path: 'today-visits', name: 'TodayVisits', component: () => import('@/views/today-visits/TodayVisitsView.vue') },
            // 门诊日志（法规台账，菜单 207 / sql/131）：病历口径 + 诊断 + 应报未报，只读
            { path: 'outpatient-log', name: 'OutpatientLog', component: () => import('@/views/outpatient-log/OutpatientLogView.vue'), meta: { title: '门诊日志' } },
            // 急诊中心（菜单 205，sql/188 由「急诊 / 绿色通道」更名）：绿色通道只是本页内的标记，不是并列流程
            { path: 'emergency', name: 'Emergency', component: () => import('@/views/emergency/EmergencyView.vue'), meta: { title: '急诊中心' } },
            // 以下为已下线菜单的假壳页，仅保留直接访问能力（演示壳组件已随壳页清理删除）
            // standalone：这些页面从 sys_menu 摘掉了，不能按「菜单里没有」判成无权
            { path: 'inpatient', name: 'Inpatient', component: () => import('@/views/inpatient/InpatientView.vue') },
            { path: 'inpatient-order', name: 'InpatientOrder', component: () => import('@/views/inpatient/InpatientOrderView.vue') },
            { path: 'inpatient-record', name: 'InpatientRecord', component: () => import('@/views/inpatient/InpatientRecordView.vue') },
            { path: 'inpatient-settlement', name: 'InpatientSettlement', component: () => import('@/views/inpatient/InpatientSettlementView.vue') },
            // sql/142：医嘱基础字典（菜单 2383）与全院组套模板（菜单 2387）。
            // 前端路由是静态表、菜单只管可见性 —— 漏在这里就是「菜单能点、路由 404 白屏」。
            { path: 'order-base-dict', name: 'OrderBaseDict', component: () => import('@/views/inpatient/OrderBaseDictView.vue'), meta: { title: '医嘱基础字典' } },
            { path: 'order-set', name: 'OrderSetTemplate', component: () => import('@/views/inpatient/OrderSetTemplateView.vue'), meta: { title: '医嘱组套（套餐）维护' } },
            { path: 'nurse', name: 'Nurse', component: () => import('@/views/nurse/NurseView.vue') },
            // 病区护理排班：菜单 331（sql/166），路径与 menu.path（/nurse-schedule）一字不差。
            // 与 /schedule（门诊医生排班 org.schedule）是两套班次册，互不通用
            { path: 'nurse-schedule', name: 'NurseSchedule', component: () => import('@/views/nurse/NurseScheduleView.vue'), meta: { title: '病区护理排班' } },
            // 护理质控：菜单 334（sql/168），路径与 menu.path（/nursing-qc）一字不差。
            // 与病案质控（评单份病历书写）不是一张账，这里评「病区 × 月」的护理质量
            { path: 'nursing-qc', name: 'NursingQc', component: () => import('@/views/nurse/NursingQcView.vue'), meta: { title: '护理质控' } },
            { path: 'surgery', name: 'Surgery', component: () => import('@/views/surgery/SurgeryView.vue') },
            // G15：麻醉工作站正式挂菜单 309（sql/83-手术麻醉链.sql §10），不再是「已下线假壳页」，
            // 故去掉 standalone —— 打上它等于绕过角色菜单判权（见 guard.js 与文件头注释）。
            { path: 'anesthesia', name: 'Anesthesia', component: () => import('@/views/anesthesia/AnesthesiaView.vue'), meta: { title: '麻醉工作站' } },
            // 手术器械清点独立为二级菜单 2924（300 住院业务，sql/180）：原为麻醉工作站的页签，
            // 清点是手术室护士（器械/巡回护士）的核对文书，与麻醉记录不是同一岗位同一份文书。
            { path: 'operation-count', name: 'OperationCount', component: () => import('@/views/surgery/OperationCountView.vue'), meta: { title: '手术器械清点' } },
            { path: 'infectious-report', name: 'InfectiousReport', component: () => import('@/views/infectious-report/InfectiousReportView.vue'), meta: { title: '传染病报告卡' } },
            // L10：院感监测（菜单 614，sql/92）
            { path: 'infection-monitor', name: 'InfectionMonitor', component: () => import('@/views/infection-monitor/InfectionMonitorView.vue'), meta: { title: '院感监测' } },
            // 手卫生依从性已并入「院感监测」（菜单 614）的同名 tab（sql/175），菜单 617 已下线。
            // 路由保留为 standalone：旧收藏 / 旧链接仍能打开，不被路由守卫弹回工作台。
            { path: 'hand-hygiene', name: 'HandHygiene', component: () => import('@/views/infection-monitor/HandHygieneView.vue'), meta: { standalone: true, title: '手卫生依从性（已并入院感监测）' } },
            // 公卫上报（菜单 618，sql/165）：此前是空表，原因就是没有录入口。
            { path: 'public-health', name: 'PublicHealth', component: () => import('@/views/public-health/PublicHealthReportView.vue'), meta: { title: '公卫上报' } },
            { path: 'consultation', name: 'Consultation', component: () => import('@/views/consultation/ConsultationView.vue') },
            // 转科管理（菜单 308，sql/188 由「转科 / 交接班」更名）：本页只做跨科转科，医护交接班走总值班交班本
            { path: 'transfer', name: 'Transfer', component: () => import('@/views/transfer/TransferView.vue'), meta: { title: '转科管理' } },
            // 床位管理（菜单 317，sql/144；sql/188 前叫「床位服务中心」）：等床队列 + 全院床位池与跨科调配。
            // 前端路由是静态表、菜单只管可见性 —— 漏在这里就是「菜单能点、路由 404 白屏」。
            { path: 'bed-center', name: 'BedCenter', component: () => import('@/views/bedcenter/BedCenterView.vue'), meta: { title: '床位管理' } },
            // 死亡证明与登记（菜单 318，sql/157）：路径与 menu.path 必须一字不差。
            { path: 'death-certificate', name: 'DeathCertificate', component: () => import('@/views/death/DeathCertificateView.vue'), meta: { title: '死亡证明与登记' } },
            // 患者请假离院（菜单 320，sql/162）：医师审批 + 患方承诺签署 + 超期处置，路径与 menu.path 一字不差。
            { path: 'inpatient-leave', name: 'InpatientLeave', component: () => import('@/views/inpatientLeave/InpatientLeaveView.vue'), meta: { title: '患者请假离院' } },
            // 病危/病重通知与告知书签收回执（菜单 319，sql/161）
            { path: 'critical-notice', name: 'CriticalNotice', component: () => import('@/views/critical/CriticalNoticeView.vue'), meta: { title: '病危重通知' } },
            // G20：出院带药 / 随访回访 / 双向转诊 / 欠费管控（菜单 310~313，sql/85）
            { path: 'discharge-drug', name: 'DischargeDrug', component: () => import('@/views/dischargedrug/DischargeDrugView.vue') },
            { path: 'followup', name: 'Followup', component: () => import('@/views/followup/FollowupView.vue') },
            { path: 'referral', name: 'Referral', component: () => import('@/views/referral/ReferralView.vue') },
            { path: 'arrears-control', name: 'ArrearsControl', component: () => import('@/views/arrears/ArrearsControlView.vue') },
            { path: 'inspection-workstation', name: 'InspectionWorkstation', component: () => import('@/views/lab/InspectionView.vue') },
            // 放射诊断工作站（菜单 414，sql/138）：报告书写台。拍片在 401、胶片在 415，本页只下诊断结论。
            { path: 'radio-diagnosis', name: 'RadioDiagnosis', component: () => import('@/views/lab/RadioDiagnosisView.vue'), meta: { title: '放射诊断工作站' } },
            // 胶片量方与发放（菜单 415，sql/138）：技师岗，登记用量→记账→打印→发放。
            { path: 'exam-film', name: 'ExamFilm', component: () => import('@/views/lab/ExamFilmView.vue'), meta: { title: '胶片量方与发放' } },
            // 心电工作站（菜单 417，sql/173）：签到→波形采集→测量/Holter 分析→报告书写→审核→发布。
            // 路径与 menu.path（/ecg）一字不差 —— 前端路由是静态表，菜单只管可见性。
            { path: 'ecg', name: 'Ecg', component: () => import('@/views/lab/EcgWorkstationView.vue'), meta: { title: '心电工作站' } },
            { path: 'laboratory-workstation', name: 'LaboratoryWorkstation', component: () => import('@/views/lab/LaboratoryView.vue') },
            { path: 'specimen', name: 'Specimen', component: () => import('@/views/specimen/SpecimenView.vue') },
            // G17 医技亚专业：菜单 407~411（sql/84 §7）。前端路由是静态表，
            // 菜单只管可见性 —— 漏掉这里就是「菜单能点、路由 404 白屏」。
            { path: 'pathology', name: 'Pathology', component: () => import('@/views/pathology/PathologyView.vue') },
            { path: 'endoscopy', name: 'Endoscopy', component: () => import('@/views/endoscopy/EndoscopyView.vue') },
            { path: 'ultrasound', name: 'Ultrasound', component: () => import('@/views/ultrasound/UltrasoundView.vue') },
            { path: 'lis-qc', name: 'LisQc', component: () => import('@/views/lisqc/LisQcView.vue') },
            // 室间质评 EQA：菜单 416（sql/172），与室内质控并列 —— 前者跟外面几百家比，后者看本室机器稳不稳。
            { path: 'lis-eqa', name: 'LisEqa', component: () => import('@/views/lisqc/LisEqaView.vue') },
            { path: 'blood-bank', name: 'BloodBank', component: () => import('@/views/bloodbank/BloodBankView.vue') },
            // G21 检查预约中心：菜单 412（sql/85 §7），路径与 menu.path 必须一字不差 ——
            // 前端路由是静态表，菜单只管可见性，漏在这里就是「菜单能点、路由 404 白屏」。
            { path: 'exam-appointment', name: 'ExamAppointment', component: () => import('@/views/exam-appointment/ExamAppointmentView.vue') },
            // 检查设备与号源独立为二级菜单 2925（400 医技医辅，sql/181）：原为检查预约中心的第 3 个页签，
            // 预约中心是逐患者受理预约单，这里维护设备档位（开放时段/粒度/并行）与分时段号源，岗位与频率都不同。
            { path: 'exam-device-slot', name: 'ExamDeviceSlot', component: () => import('@/views/exam-appointment/ExamDeviceSlotView.vue'), meta: { title: '检查设备与号源' } },
            // G19 门诊治疗站：菜单 206（sql/88 §9），路径与 menu.path 必须一字不差。
            // 原 /infusion 假壳页随本条替换删除。
            { path: 'treatment-station', name: 'TreatmentStation', component: () => import('@/views/treatment/TreatmentStationView.vue'), meta: { title: '门诊治疗站' } },
            // 慢病建档与认定（菜单 2051，sql/165）：长处方开方资格的唯一依据，
            // 此前只有小程序 M1 在用，桌面端查不到。路径与 menu.path 必须逐字一致。
            { path: 'chronic-record', name: 'ChronicRecord', component: () => import('@/views/chronic/ChronicRecordView.vue'), meta: { title: '慢病建档与认定' } },
            { path: 'pharmacy', name: 'Pharmacy', component: () => import('@/views/pharmacy/PharmacyView.vue') },
            { path: 'pharmacy-window', name: 'PharmacyWindow', component: () => import('@/views/pharmacy-window/PharmacyWindowView.vue') },
            { path: 'prescription-audit', name: 'PrescriptionAudit', component: () => import('@/views/pharmacy-window/PrescriptionAuditView.vue') },
            { path: 'narcotic-register', name: 'NarcoticRegister', component: () => import('@/views/pharmacy-window/NarcoticRegisterView.vue') },
            // G9 药品采购链：菜单 505/506/507（药事管理组）。前端路由是静态表，
            // 菜单只管可见性 —— 漏掉这里就是「菜单能点、路由 404 白屏」。
            { path: 'purchase-order', name: 'PurchaseOrder', component: () => import('@/views/pharmacy/PurchaseOrderView.vue') },
            { path: 'drug-inbound', name: 'DrugInbound', component: () => import('@/views/pharmacy/DrugInboundView.vue') },
            { path: 'supplier', name: 'Supplier', component: () => import('@/views/pharmacy/SupplierView.vue') },
            // 处方点评与公示（菜单 510/511，sql/160）：路径与 menu.path 必须一字不差。
            { path: 'rx-review', name: 'RxReview', component: () => import('@/views/pharmacy/RxReviewView.vue') },
            { path: 'rx-publicity', name: 'RxPublicity', component: () => import('@/views/pharmacy/RxPublicityView.vue') },
            // 抗菌药物管理（菜单 517/518/519，sql/161）：分级目录与处方权授权、使用监测、I类切口预防用药点评。
            // 路径与 menu.path 必须一字不差（/antibiotic-catalog / /antibiotic-monitor / /antibiotic-incision）
            { path: 'antibiotic-catalog', name: 'AntibioticCatalog', component: () => import('@/views/pharmacy/AntibioticCatalogView.vue'), meta: { title: '抗菌药物分级目录' } },
            // 抗菌药物处方权管理（菜单 2922，sql/178）：原为 517「抗菌药物分级目录」的第二个页签。
            // 目录管的是「药品属哪一级」，处方权管的是「医师能开到哪一级」，实体与维护岗位都不同，故拆开。
            { path: 'antibiotic-auth', name: 'AntibioticAuth', component: () => import('@/views/pharmacy/AntibioticAuthView.vue'), meta: { title: '抗菌药物处方权管理' } },
            { path: 'antibiotic-monitor', name: 'AntibioticMonitor', component: () => import('@/views/pharmacy/AntibioticMonitorView.vue'), meta: { title: '抗菌药物使用监测' } },
            { path: 'antibiotic-incision', name: 'AntibioticIncision', component: () => import('@/views/pharmacy/AntibioticIncisionView.vue'), meta: { title: 'I类切口预防用药点评' } },
            // VTE 防控（菜单 332/333，sql/167）：中高危名单与措施落实、院内 VTE 发生率监测。
            // 路径与 menu.path 必须一字不差（/vte-prevent / /vte-monitor）
            { path: 'vte-prevent', name: 'VtePrevent', component: () => import('@/views/nurse/VtePreventView.vue'), meta: { title: 'VTE 风险防控' } },
            { path: 'vte-monitor', name: 'VteMonitor', component: () => import('@/views/nurse/VteMonitorView.vue'), meta: { title: '院内 VTE 监测' } },
            // 营养膳食管理（菜单 424~428，sql/168）：NRS2002 筛查、膳食医嘱接单、订餐配送、营养会诊、月度指标。
            // 路径与 menu.path 必须一字不差；膳食方案的主来源是 orderClass=10 医嘱校对派生，筛查页跳 /diet-plan 带 admissionId。
            { path: 'nutrition-screen', name: 'NutritionScreen', component: () => import('@/views/nutrition/NutritionScreenView.vue'), meta: { title: '营养风险筛查' } },
            { path: 'diet-plan', name: 'DietPlan', component: () => import('@/views/nutrition/DietPlanView.vue'), meta: { title: '膳食医嘱执行' } },
            { path: 'meal-order', name: 'MealOrder', component: () => import('@/views/nutrition/MealOrderView.vue'), meta: { title: '订餐配送' } },
            { path: 'nutrition-consult', name: 'NutritionConsult', component: () => import('@/views/nutrition/NutritionConsultView.vue'), meta: { title: '营养会诊' } },
            { path: 'nutrition-stats', name: 'NutritionStats', component: () => import('@/views/nutrition/NutritionStatsView.vue'), meta: { title: '营养指标监测' } },
            { path: 'cashier', name: 'Cashier', component: () => import('@/views/cashier/CashierView.vue') },
            // 已摘除：/billing「收费管理」作为收费查询是 /cashier 的坏子集 ——
            // 把 chargeNo 当 chargeId 传给 /charge/getById（详情必然 400 且静默）、
            // 统计卡只算 pageSize=100 的前 100 条、筛选是前端切片。页面文件保留未删，
            // 待重做成 /cashier 的查询 tab 后再放开。
            // { path: 'billing', name: 'Billing', component: () => import('@/views/billing/BillingView.vue') },
            { path: 'finance', name: 'Finance', component: () => import('@/views/finance/FinanceView.vue'), meta: { title: '财务管理', standalone: true } },
            { path: 'insurance', name: 'Insurance', component: () => import('@/views/insurance/InsuranceView.vue') },
            // 医保目录对照（菜单 1009，sql/143）：院内项目 ↔ 国家医保编码
            { path: 'insurance-mapping', name: 'InsuranceMapping', component: () => import('@/views/insurance/InsuranceMappingView.vue') },
            // 医保扣款与飞检处理（菜单 1010，sql/163）：路径必须与 menu.path（/insurance-deduct）逐字一致
            { path: 'insurance-deduct', name: 'InsuranceDeduct', component: () => import('@/views/insurance/InsuranceDeductView.vue') },
            // 慢特病医保备案（菜单 1011，sql/163；sql/188 由「慢特病人员备案」更名，加「医保」二字钉归属）：同上，/insurance-chronic
            { path: 'insurance-chronic', name: 'InsuranceChronic', component: () => import('@/views/insurance/InsuranceChronicView.vue') },
            // 门诊慢特病病种目录（菜单 2923，sql/179）：原为 1011「慢特病医保备案」的第二个页签。
            // 目录是待遇配置（一个病种一行，随上级目录调整），备案是逐患者经办，故拆开。
            { path: 'chronic-catalog', name: 'ChronicCatalog', component: () => import('@/views/insurance/ChronicCatalogView.vue'), meta: { title: '门诊慢特病病种目录' } },
            // 医保合规审核台账（菜单 1012，sql/165）：回看已跑过的审核结果与 72 行规则明细，
            // 执行审核仍在 /insurance。路径与 menu.path 必须逐字一致。
            { path: 'compliance-audit', name: 'ComplianceAudit', component: () => import('@/views/insurance/ComplianceAuditView.vue'), meta: { title: '医保合规审核台账' } },
            { path: 'invoice', name: 'Invoice', component: () => import('@/views/invoice/InvoiceView.vue') },
            { path: 'refund', name: 'Refund', component: () => import('@/views/refund/RefundView.vue') },
            // 退费流水（菜单 2350，sql/124）：路径必须与 menu.path（/refund-flow）逐字一致，否则点菜单白屏
            { path: 'refund-flow', name: 'RefundFlow', component: () => import('@/views/refund-flow/RefundFlowView.vue') },
            // 收费四层台账（菜单 2360~2368，sql/125）：路径必须与 menu.path 逐字一致，否则点菜单白屏。
            // 出账/收款主战场在 /cashier；这四页是 L1~L3 各层的台账 + 动作入口。
            { path: 'fee-record', name: 'FeeRecord', component: () => import('@/views/fee-record/FeeRecordView.vue') },
            { path: 'settlement-bill', name: 'SettlementBill', component: () => import('@/views/settlement-bill/SettlementBillView.vue') },
            { path: 'payment-txn', name: 'PaymentTxn', component: () => import('@/views/payment-txn/PaymentTxnView.vue') },
            { path: 'fund-account', name: 'FundAccount', component: () => import('@/views/fund-account/FundAccountView.vue') },
            { path: 'insurance-policy', name: 'InsurancePolicy', component: () => import('@/views/insurance-policy/InsurancePolicyView.vue') },
            // 复诊收费策略（菜单 2340，sql/121）：复诊号收不收钱由这张表判定，
            // 挂在门诊目录下，路由路径与菜单 path 必须逐字一致，否则点菜单白屏。
            { path: 'revisit-policy', name: 'RevisitPolicy', component: () => import('@/views/revisit-policy/RevisitFeePolicyView.vue') },
            { path: 'price', name: 'Price', component: () => import('@/views/price/PriceView.vue') },
            { path: 'pay-channel', name: 'PayChannel', component: () => import('@/views/finance/PayChannelView.vue') },
            { path: 'medical-record', name: 'MedicalRecord', component: () => import('@/views/medical-record/MedicalRecordView.vue') },
            { path: 'medical-review', name: 'MedicalReview', component: () => import('@/views/medical-record/MedicalReviewView.vue') },
            { path: 'critical-value', name: 'CriticalValue', component: () => import('@/views/critical-value/CriticalValueView.vue') },
            { path: 'adverse-event', name: 'AdverseEvent', component: () => import('@/views/adverse-event/AdverseEventView.vue'), meta: { title: '不良事件', standalone: true } },
            { path: 'transfusion', name: 'Transfusion', component: () => import('@/views/transfusion/TransfusionView.vue') },
            { path: 'patients', name: 'Patients', component: () => import('@/views/patients/PatientsView.vue') },
            { path: 'empi', name: 'PatientIndex', component: () => import('@/views/empi/PatientIndexView.vue') },
            { path: 'cdr', name: 'PatientCdr', component: () => import('@/views/cdr/PatientCdrView.vue') },
            { path: 'data-quality', name: 'DataQuality', component: () => import('@/views/quality/DataQualityView.vue') },
            { path: 'medical-record-qc', name: 'MedicalRecordQc', component: () => import('@/views/quality/MedicalRecordQcView.vue') },
            { path: 'medical-record-archive', name: 'MedicalRecordArchive', component: () => import('@/views/medical-record/MedicalRecordArchiveView.vue') },
            { path: 'archive-borrow', name: 'ArchiveBorrow', component: () => import('@/views/medical-record/ArchiveBorrowView.vue'), meta: { title: '病案借阅复印' } },
            { path: 'coding-task', name: 'CodingTask', component: () => import('@/views/medical-record/CodingTaskView.vue'), meta: { title: '编码任务池' } },
            { path: 'ward-dispense', name: 'WardDispense', component: () => import('@/views/pharmacy/WardDispenseView.vue'), meta: { title: '住院摆药' } },
            // 静配中心 PIVAS：菜单 509（sql/104），路径与 menu.path（/pivas）一字不差
            { path: 'pivas', name: 'Pivas', component: () => import('@/views/pharmacy/PivasView.vue'), meta: { title: '静配中心' } },
            // 药房盘点：菜单 510（sql/127），路径与 menu.path（/stocktake）一字不差
            { path: 'stocktake', name: 'Stocktake', component: () => import('@/views/pharmacy/StocktakeView.vue'), meta: { title: '药房盘点' } },
            // 中药代煎台账：菜单 511（sql/139），路径与 menu.path（/tcm-decoct）一字不差
            { path: 'tcm-decoct', name: 'TcmDecoct', component: () => import('@/views/pharmacy/TcmDecoctView.vue'), meta: { title: '中药代煎' } },
            // 药品调拨（药库↔药房）：菜单 512（sql/154），路径与 menu.path（/drug-transfer）一字不差
            { path: 'drug-transfer', name: 'DrugTransfer', component: () => import('@/views/pharmacy/DrugTransferView.vue'), meta: { title: '药品调拨' } },
            // 药品供应商退货：菜单 513（sql/154），路径与 menu.path（/supplier-return）一字不差
            { path: 'supplier-return', name: 'SupplierReturn', component: () => import('@/views/pharmacy/SupplierReturnView.vue'), meta: { title: '供应商退货' } },
            // 药品追溯码采集与核对（菜单 514，sql/156）：路径与 menu.path（/drug-trace）一字不差
            { path: 'drug-trace', name: 'DrugTrace', component: () => import('@/views/pharmacy/DrugTraceView.vue'), meta: { title: '药品追溯码' } },
            // 临床路径：菜单 607 复活（sql/106），路径与 menu.path（/clinical-path）一字不差
            { path: 'clinical-path', name: 'ClinicalPath', component: () => import('@/views/clinical-path/ClinicalPathView.vue'), meta: { title: '临床路径' } },
            // 血液净化中心：菜单 413（sql/108），路径与 menu.path（/dialysis）一字不差
            { path: 'dialysis', name: 'Dialysis', component: () => import('@/views/dialysis/DialysisView.vue'), meta: { title: '血液净化中心' } },
            // 血液净化机位管理：菜单 2931（sql/186），路径与 menu.path（/dialysis-machine）一字不差。
            // 机位是建档（装机/报废低频），排班与上下机是每日动作，岗位与频率都不同，故拆开。
            { path: 'dialysis-machine', name: 'DialysisMachine', component: () => import('@/views/dialysis/DialysisMachineView.vue'), meta: { title: '血液净化机位管理' } },
            // ICU 专科监护：菜单 314（sql/108），路径与 menu.path（/icu）一字不差
            { path: 'icu', name: 'Icu', component: () => import('@/views/icu/IcuView.vue'), meta: { title: 'ICU 专科监护' } },
            // 远程会诊：菜单 315（sql/110 建卡，sql/184 由「互联网医院 / 远程会诊」更名），路径与 menu.path（/teleconsult）一字不差
            { path: 'teleconsult', name: 'TeleConsult', component: () => import('@/views/teleconsult/TeleConsultView.vue'), meta: { title: '远程会诊' } },
            // 线上问诊：菜单 2929（sql/184），路径与 menu.path（/online-consult）一字不差
            { path: 'online-consult', name: 'OnlineConsult', component: () => import('@/views/teleconsult/OnlineConsultView.vue'), meta: { title: '线上问诊' } },
            // 日间手术：菜单 316（sql/111），路径与 menu.path（/day-surgery）一字不差
            { path: 'day-surgery', name: 'DaySurgery', component: () => import('@/views/day-surgery/DaySurgeryView.vue'), meta: { title: '日间手术' } },
            // 日间手术准入目录：菜单 2928（sql/183），路径与 menu.path（/day-surgery-item）一字不差
            { path: 'day-surgery-item', name: 'DaySurgeryItem', component: () => import('@/views/day-surgery/DaySurgeryItemView.vue'), meta: { title: '日间手术准入目录' } },
            // 医疗纠纷与投诉：菜单 615（sql/109），路径与 menu.path（/dispute）一字不差
            { path: 'dispute', name: 'Dispute', component: () => import('@/views/dispute/DisputeView.vue'), meta: { title: '医疗纠纷与投诉' } },
            // 满意度评价：菜单 616（sql/164），路径与 menu.path（/survey）一字不差
            { path: 'survey', name: 'Survey', component: () => import('@/views/survey/SurveyView.vue'), meta: { title: '满意度评价' } },
            // 门诊输液室：菜单 2180（sql/117），路径与 menu.path（/infusionRoom）一字不差
            { path: 'infusionRoom', name: 'InfusionRoom', component: () => import('@/views/infusion/InfusionRoomView.vue'), meta: { title: '门诊输液站' } },
            // 单病种质控：菜单 2183（sql/117），路径与 menu.path（/singleDisease）一字不差
            { path: 'singleDisease', name: 'SingleDisease', component: () => import('@/views/quality/SingleDiseaseView.vue'), meta: { title: '单病种质控' } },
            { path: 'signature-center', name: 'SignatureCenter', component: () => import('@/views/sign/SignatureCenterView.vue') },
            { path: 'health-record', name: 'HealthRecord', component: () => import('@/views/health-record/HealthRecordView.vue'), meta: { title: '健康档案' } },
            { path: 'doctors', name: 'Doctors', component: () => import('@/views/doctors/DoctorsView.vue'), meta: { title: '医生管理' } },
            { path: 'schedule', name: 'Schedule', component: () => import('@/views/schedule/ScheduleView.vue') },
            // 全院岗位排班（菜单 2935，sql/200 核心表）：回答「谁哪天在哪个单元上什么班」。
            // 与 /schedule（门诊出诊计划）是两件事：出诊计划挂号源，这张核心表管出勤 ——
            // 同一个人以前可以在三张排班表里各排一次且时间重叠都不报错，收敛到这里之后事实只有一条。
            { path: 'staff-schedule', name: 'StaffSchedule', component: () => import('@/views/schedule/StaffScheduleView.vue'), meta: { title: '全院岗位排班' } },
            // 人力配置标准（菜单 2937，sql/200）：一个单元×一个班次×一个岗位该配多少人。
            // 它是排班保存的闸门（低于最低在岗会拦），不是报表，所以单独成页而不是塞进排班页的页签。
            { path: 'staff-plan-rule', name: 'StaffPlanRule', component: () => import('@/views/schedule/StaffPlanRuleView.vue'), meta: { title: '人力配置标准' } },
            // G23 绩效与成本核算：菜单 908（sql/87），路径与 menu.path（perf）一字不差
            { path: 'perf', name: 'Performance', component: () => import('@/views/performance/PerformanceView.vue'), meta: { title: '绩效与成本核算' } },
            // G23 体检管理：菜单 905（sql/87）
            { path: 'checkup', name: 'Checkup', component: () => import('@/views/checkup/CheckupView.vue'), meta: { title: '体检管理' } },
            // 体检套餐：菜单 2930（sql/185），路径与 menu.path（/checkup-package）一字不差。
            // 套餐是建档（一次性维护+明细），登记是每日动作，岗位与频率都不同，故拆开。
            { path: 'checkup-package', name: 'CheckupPackage', component: () => import('@/views/checkup/CheckupPackageView.vue'), meta: { title: '体检套餐' } },
            // G23 BI 驾驶舱：菜单 906（sql/87）
            { path: 'bi', name: 'BiDashboard', component: () => import('@/views/bi/BiDashboard.vue'), meta: { title: 'BI 驾驶舱' } },
            // G23 DRG-DIP 分组模拟：菜单 907（sql/87）
            { path: 'drgsim', name: 'DrgSim', component: () => import('@/views/drg/DrgSimView.vue'), meta: { title: 'DRG-DIP 分组模拟' } },
            // L9 病案统计上报（打印预留）：菜单 2159（sql/101），路径与 menu.path 一字不差
            { path: 'statreport', name: 'StatReport', component: () => import('@/views/statreport/StatReportView.vue'), meta: { title: '病案统计上报' } },
            // 全院总值班排班（菜单 806，sql/169）：路径必须与 menu.path（/duty-roster）一字不差。
            // 急诊升级 / 床位跨科调配 / 双向转诊三条链路的兜底收口人就在这张表里。
            { path: 'duty-roster', name: 'DutyRoster', component: () => import('@/views/duty/DutyRosterView.vue'), meta: { title: '总值班排班' } },
            // 总值班日志 / 交班本（菜单 2920，sql/176）：原为 806 的第二个页签，现独立挂菜单。
            // 排班是院办按周编，交班本当班的人每班都要记与签收 —— 岗位与频率都不同，故拆开。
            { path: 'duty-log', name: 'DutyLog', component: () => import('@/views/duty/DutyLogView.vue'), meta: { title: '总值班日志（交班本）' } },
            // 值守点位（菜单 2936，sql/200）：把「位」从「人」里剥出来 ——
            // 位先定义存在（医院今天就该有这个班），排班只是把人写进位里。位可以空着等排。
            { path: 'duty-post', name: 'DutyPost', component: () => import('@/views/duty/DutyPostView.vue'), meta: { title: '值守点位' } },
            { path: 'departments', name: 'Departments', component: () => import('@/views/departments/DepartmentsView.vue') },
            { path: 'inspection-items', name: 'InspectionItems', component: () => import('@/views/medical-items/InspectionItemsView.vue') },
            { path: 'laboratory-items', name: 'LaboratoryItems', component: () => import('@/views/medical-items/LaboratoryItemsView.vue') },
            { path: 'equipment', name: 'Equipment', component: () => import('@/views/equipment/EquipmentView.vue'), meta: { title: '设备管理' } },
            { path: 'supplies', name: 'Supplies', component: () => import('@/views/supplies/SuppliesView.vue') },
            // 耗材字典（菜单 2921，sql/177）：原为 901「物资耗材」的页签，现独立挂菜单。
            // 字典是建档（新耗材进院录一次），出入库是每日动作，岗位与频率都不同，故拆开。
            { path: 'consumable-dict', name: 'ConsumableDict', component: () => import('@/views/supplies/ConsumableDictView.vue'), meta: { title: '耗材字典' } },
            { path: 'cssd', name: 'Cssd', component: () => import('@/views/cssd/CssdView.vue'), meta: { title: '消毒供应 CSSD' } },
            // 器械包模板：菜单 2932（sql/187），路径与 menu.path（/cssd-template）一字不差。
            // 模板是建档（包编码/组成明细一次性维护），回收-灭菌-发放是每日流转动作，岗位与频率都不同，故拆开。
            { path: 'cssd-template', name: 'CssdTemplate', component: () => import('@/views/cssd/CssdTemplateView.vue'), meta: { title: '器械包模板' } },
            { path: 'waste', name: 'Waste', component: () => import('@/views/waste/WasteView.vue'), meta: { title: '医废管理' } },
            // 系统管理
            { path: 'system/user', name: 'SystemUser', component: () => import('@/views/system/user/UserView.vue') },
            { path: 'system/role', name: 'SystemRole', component: () => import('@/views/system/role/RoleView.vue') },
            { path: 'system/menu', name: 'SystemMenu', component: () => import('@/views/system/menu/MenuView.vue') },
            // 部门管理（菜单 1109，见 sql/197）：科室树的**树形**视图。
            // 与「组织与资源 → 科室管理」(/departments，列表视图) 是同一张 sys_department 的两种视图，老王 2026-09-29 拍板两个入口并存。
            { path: 'system/dept', name: 'SystemDept', component: () => import('@/views/system/dept/DeptView.vue'), meta: { title: '部门管理' } },
            { path: 'system/employee', name: 'SystemEmployee', component: () => import('@/views/system/employee/EmployeeView.vue') },
            { path: 'system/techAuth', name: 'SystemTechAuth', component: () => import('@/views/system/techAuth/TechAuthView.vue') },
            { path: 'system/clinicRoom', name: 'SystemClinicRoom', component: () => import('@/views/system/clinicRoom/ClinicRoomView.vue') },
            { path: 'system/dict', name: 'SystemDict', component: () => import('@/views/system/dict/DictView.vue') },
            { path: 'system/config', name: 'SystemConfig', component: () => import('@/views/system/config/ConfigView.vue') },
            { path: 'system/patientTag', name: 'SystemPatientTag', component: () => import('@/views/system/tag/PatientTagView.vue') },
            // 患者端常见问题维护（菜单 1108，sql/217）：path 必须与 sys_menu.path 一字不差
            { path: 'system/patientFaq', name: 'SystemPatientFaq', component: () => import('@/views/system/faq/FaqView.vue'), meta: { title: '患者常见问题' } },
            { path: 'system/labPlain', name: 'SystemLabPlain', component: () => import('@/views/system/faq/LabPlainItemView.vue'), meta: { title: '检验项目白话词典' } },
            { path: 'system/serviceTicket', name: 'SystemServiceTicket', component: () => import('@/views/system/service/ServiceTicketView.vue'), meta: { title: '工单受理' } },
            // 工作台配置（菜单 1106，见 sql/105）：path 必须与 sys_menu.path 一字不差
            { path: 'system/workbench', name: 'SystemWorkbench', component: () => import('@/views/system/workbench/WorkbenchConfigView.vue') },
            // 合理用药知识库（菜单 1107，见 sql/130）：path 与 menu.path 一字不差
            { path: 'system/drugKnowledge', name: 'SystemDrugKnowledge', component: () => import('@/views/system/DrugKnowledgeView.vue') },
            // 日志审计（菜单 1108，见 sql/158）：操作/登录/审计三本账，path 与 menu.path 一字不差
            { path: 'system/log', name: 'SystemLog', component: () => import('@/views/system/LogView.vue') },
            // 运营统计报表（菜单 1201，sql/188 由「报表统计」更名；上级目录 1200 同步由「报表与审计」改为「报表统计」）
            { path: 'reports', name: 'Reports', component: () => import('@/views/reports/ReportsView.vue'), meta: { title: '运营统计报表' } },
        ],
    },
];
const router = createRouter({
    history: createWebHistory(),
    routes,
});
export default router;