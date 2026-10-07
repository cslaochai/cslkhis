package com.his.common.base;

public interface Constants {

    /**
     * <h2>两类"前缀"是一对，但落在完全不同的地方</h2>
     *
     * <ul>
     *   <li>{@code XXX_NO_KEY_PREFIX} —— <b>Redis 计数器的 key 名</b>，只进 {@code next(module)}，
     *       真实 key 是 {@code {KEY_PREFIX}:{yyyyMMdd}}（如 {@code EMPLOYEE:20261007}）。
     *       它<em>不会</em>出现在任何单号里、也不落库。</li>
     *   <li>{@code XXX_NO_PREFIX} —— <b>单号正文的头几个字母</b>，落进 {@code xxx_no} 列给人看
     *       （如 {@code PATIENT_NO_PREFIX = "P"} → {@code P2026100700001}）。</li>
     * </ul>
     *
     * 一对两个是因为「给人看的短前缀」和「机器用的长名字」诉求相反：单号要短要好认（P/AE/ICASE），
     * key 要能一眼看出是哪个业务的号段（PATIENT/ADVERSE_EVENT/INFECTION_CASE）。
     *
     * <p><b>⚠ key 名的值是运行事实，不能改：</b>换 key 名 = 当天计数器从 1 重启 = 与当天已发出的号
     * 撞 {@code uk_xxx_no} 唯一索引，而且零报错（现象是「保存失败/系统内部错误」，真因查不出来）。
     * 要新号段就新增常量，别动老常量的值。
     */
    String PATIENT_NO_KEY_PREFIX = "PATIENT";
    String APPOINT_NO_KEY_PREFIX = "APPOINT";
    String EMPLOYEE_NO_KEY_PREFIX = "EMPLOYEE";
    String DRUG_NO_KEY_PREFIX = "DRUG";
    String QUEUE_NO_KEY_PREFIX = "QUEUE";
    String ROLE_NO_KEY_PREFIX = "ROLE";
    String EMERGENCY_NO_KEY_PREFIX = "EMERGENCY";
    String PURCHASE_NO_KEY_PREFIX = "PURCHASE";
    String INBOUND_NO_KEY_PREFIX = "INBOUND";
    String ADVERSE_EVENT_NO_KEY_PREFIX = "ADVERSE_EVENT";
    String ARCHIVE_BORROW_NO_KEY_PREFIX = "ARCHIVE_BORROW";
    String CODE_TASK_NO_KEY_PREFIX = "CODE_TASK";
    String WARD_DISPENSE_NO_KEY_PREFIX = "WARD_DISPENSE";
    String PIVAS_NO_KEY_PREFIX = "PIVAS";
    String STOCKTAKE_NO_KEY_PREFIX = "STOCKTAKE";
    /**
     * 药品调拨单 / 供应商退货单（sql/154）
     */
    String DRUG_TRANSFER_NO_KEY_PREFIX = "DRUG_TRANSFER";
    String SUPPLIER_RETURN_NO_KEY_PREFIX = "SUPPLIER_RETURN";
    /**
     * 中药代煎单号（sql/139）
     */
    String TCM_DECOCT_NO_KEY_PREFIX = "TCM_DECOCT";
    /**
     * 三级质控流转单：本常量声明的 key 是 RECORD_QC_FLOW，但历史上这张单是按 "QCF"（前缀常量）取号的。
     * 值不能就地纠正 —— 换 key = 当天序号归 1 = 与当天已发的 QCF 号撞唯一索引，见 generateRecordQcFlowNo
     */
    String RECORD_QC_FLOW_NO_KEY_PREFIX = "RECORD_QC_FLOW";
    String EXAM_APPOINT_NO_KEY_PREFIX = "EXAM_APPOINT";
    String TREATMENT_APPLY_NO_KEY_PREFIX = "TREATMENT_APPLY";
    String DISPENSING_NO_KEY_PREFIX = "DISPENSING";
    String PATHWAY_NO_KEY_PREFIX = "PATHWAY";
    String DIALYSIS_PATIENT_NO_KEY_PREFIX = "DIALYSIS_PATIENT";
    String DIALYSIS_SESSION_NO_KEY_PREFIX = "DIALYSIS_SESSION";
    String ICU_STAY_NO_KEY_PREFIX = "ICU_STAY";

    /**
     * 收费四层（sql/125）：记账行 / 结算账单 / 支付流水（收、退各自独立序号） / 账户流水
     */
    String FEE_NO_KEY_PREFIX = "FEE";
    String BILL_NO_KEY_PREFIX = "BILL";
    String PAY_TXN_NO_KEY_PREFIX = "PAY_TXN";
    String REFUND_TXN_NO_KEY_PREFIX = "REFUND_TXN";
    String FUND_TXN_NO_KEY_PREFIX = "FUND_TXN";
    String INVOICE_NO_KEY_PREFIX = "INVOICE";
    /**
     * 医保结算清单号（sql/136 起由 L2 出账生成，不再用时间戳+随机数：同秒两张单会撞号，报盘后医保侧无从追溯）
     */
    String ISB_NO_KEY_PREFIX = "INSURANCE_SETTLEMENT";
    /**
     * 医保飞检批次号（sql/163：FI + yyyyMMdd + 4 位）
     */
    String YB_INSPECT_NO_KEY_PREFIX = "YB_INSPECT";
    /**
     * 医保扣款通知单号（sql/163：DK + yyyyMMdd + 4 位）
     */
    String YB_DEDUCT_NO_KEY_PREFIX = "YB_DEDUCT";
    /**
     * 慢特病备案单号（sql/163：MT + yyyyMMdd + 4 位）
     */
    String CHRONIC_REG_NO_KEY_PREFIX = "CHRONIC_REG";

    String INFECTIOUS_REPORT_NO_KEY_PREFIX = "INFECTIOUS_REPORT";

    String INFECTION_CASE_NO_KEY_PREFIX = "INFECTION_CASE";
    String INFECTION_MONITOR_NO_KEY_PREFIX = "INFECTION_MONITOR";

    /**
     * 死亡证明编号（sql/157）
     */
    String DEATH_CERT_NO_KEY_PREFIX = "DEATH_CERT";
    /**
     * 死亡登记号（sql/157）
     */
    String DEATH_REGISTER_NO_KEY_PREFIX = "DEATH_REGISTER";
    /**
     * 病危重通知单号（sql/161）
     */
    String CRITICAL_NOTICE_NO_KEY_PREFIX = "CRITICAL_NOTICE";
    /**
     * 住院请假单号（sql/162）
     */
    String LEAVE_RECORD_NO_KEY_PREFIX = "INPATIENT_LEAVE";

    /**
     * 医生工作站/医技/消息侧单号（原先在各自 service 里用进程内 AtomicInteger + 秒级时间戳发号，
     * 重启归零、多实例并存必撞各表的 xxx_no 唯一索引）
     */
    String RULE_CHECK_NO_KEY_PREFIX = "RULE_CHECK";
    String CHRONIC_RECORD_NO_KEY_PREFIX = "CHRONIC_RECORD";
    String REFUND_APPLY_NO_KEY_PREFIX = "REFUND_APPLY";
    String RX_FLOW_NO_KEY_PREFIX = "RX_FLOW";
    String PUBLIC_HEALTH_REPORT_NO_KEY_PREFIX = "PUBLIC_HEALTH_REPORT";
    String PRESCRIPTION_NO_KEY_PREFIX = "PRESCRIPTION";
    String INSPECTION_APPLY_NO_KEY_PREFIX = "INSPECTION_APPLY";
    String LABORATORY_APPLY_NO_KEY_PREFIX = "LABORATORY_APPLY";
    String MEDICAL_RECORD_NO_KEY_PREFIX = "MEDICAL_RECORD";
    String FOLLOWUP_TASK_NO_KEY_PREFIX = "FOLLOWUP_TASK";
    String MEDICAL_RECORD_ARCHIVE_NO_KEY_PREFIX = "MEDICAL_RECORD_ARCHIVE";
    String INSPECTION_RECORD_NO_KEY_PREFIX = "INSPECTION_RECORD";
    String LABORATORY_RECORD_NO_KEY_PREFIX = "LABORATORY_RECORD";
    String REPORT_NO_KEY_PREFIX = "REPORT";
    String ECG_WAVE_NO_KEY_PREFIX = "ECG_WAVE";
    String MESSAGE_NO_KEY_PREFIX = "MESSAGE";
    String STAT_REPORT_NO_KEY_PREFIX = "STAT_REPORT";
    String RULE_CHECK_AI_NO_KEY_PREFIX = "RULE_CHECK_AI";

    /**
     * 单号正文前缀（落库给人看的那几个字母），与上面每组 KEY_PREFIX 一一配对，分工见类注释
     */
    String PATIENT_NO_PREFIX = "P";
    String APPOINT_NO_PREFIX = "A";
    String DRUG_NO_PREFIX = "D";
    String QUEUE_NO_PREFIX = "Q";
    String ROLE_NO_PREFIX = "R";
    String EMPLOYEE_NO_PREFIX = "E";
    String EMERGENCY_NO_PREFIX = "JZ";
    String PURCHASE_NO_PREFIX = "CG";
    String INBOUND_NO_PREFIX = "IN";
    String ADVERSE_EVENT_NO_PREFIX = "AE";
    String ARCHIVE_BORROW_NO_PREFIX = "BR";
    String CODE_TASK_NO_PREFIX = "CT";
    String WARD_DISPENSE_NO_PREFIX = "WD";
    String PIVAS_NO_PREFIX = "PV";
    /**
     * 药房盘点单号前缀（PD = 盘点）
     */
    String STOCKTAKE_NO_PREFIX = "PD";
    /**
     * 药品调拨单号前缀（TB = 调拨，sql/154）
     */
    String DRUG_TRANSFER_NO_PREFIX = "TB";
    /**
     * 药品供应商退货单号前缀（TG = 退供，sql/154）
     */
    String SUPPLIER_RETURN_NO_PREFIX = "TG";
    /**
     * 中药代煎单号前缀（TCMD = 中药代煎）
     */
    String TCM_DECOCT_NO_PREFIX = "TCMD";
    String RECORD_QC_FLOW_NO_PREFIX = "QCF";

    /**
     * 死亡证明编号前缀（DC = Death Certificate，sql/157；区域死因监测系统另发编号，落 report_no）
     */
    String DEATH_CERT_NO_PREFIX = "DC";
    /**
     * 死亡登记号前缀（RG = ReGiSter，sql/157）
     */
    String DEATH_REGISTER_NO_PREFIX = "RG";
    /**
     * 病危重通知单号前缀（BT = 病重/病危通知「危」字拼音首音节，避让死亡证明 DC，sql/161）
     */
    String CRITICAL_NOTICE_NO_PREFIX = "BT";
    /**
     * 住院请假单号前缀（LV = Leave，避让 BT病危/DC死亡证明/RG死亡登记，sql/162）
     */
    String LEAVE_RECORD_NO_PREFIX = "LV";

    /**
     * 满意度三件套单号（sql/164）：ST问卷卷号 / SD发放单号 / SV答卷号
     */
    String SURVEY_TEMPLATE_NO_KEY_PREFIX = "SURVEY_TEMPLATE";
    String SURVEY_DISPATCH_NO_KEY_PREFIX = "SURVEY_DISPATCH";
    String SURVEY_ANSWER_NO_KEY_PREFIX = "SURVEY_ANSWER";
    String SURVEY_TEMPLATE_NO_PREFIX = "ST";
    String SURVEY_DISPATCH_NO_PREFIX = "SD";
    String SURVEY_ANSWER_NO_PREFIX = "SV";

    /**
     * 收费四层单号前缀。收/退用不同前缀（PT/RT）是刻意的：
     * 流水号在渠道对账里会被人和渠道账单逐行比对，看号就知道这笔钱的方向，
     * 少一次"还得回去查 direction 字段"。
     */
    String FEE_NO_PREFIX = "FR";
    String BILL_NO_PREFIX = "SB";
    String PAY_TXN_NO_PREFIX = "PT";
    String REFUND_TXN_NO_PREFIX = "RT";
    String FUND_TXN_NO_PREFIX = "AT";
    /**
     * 发票号前缀：IV + yyyyMMdd + 5 位。票号是财政序列，一天一号，绝不带秒级随机数
     */
    String INVOICE_NO_PREFIX = "IV";
    /**
     * 医保结算清单号前缀：IS + yyyyMMdd + 5 位（报盘的唯一业务键，医保侧按它追这笔结算）
     */
    String ISB_NO_PREFIX = "IS";
    /**
     * 飞检批次号前缀：FI + yyyyMMdd + 4 位（Flying Inspection）
     */
    String YB_INSPECT_NO_PREFIX = "FI";
    /**
     * 扣款通知单号前缀：DK + yyyyMMdd + 4 位（Deduct）
     */
    String YB_DEDUCT_NO_PREFIX = "DK";
    /**
     * 慢特病备案单号前缀：MT + yyyyMMdd + 4 位（MenTe 门特）
     */
    String CHRONIC_REG_NO_PREFIX = "MT";

    /**
     * 检查预约单号前缀：YY + yyyyMMdd + 4 位（与挂号预约的 A 前缀区分开）
     */
    String EXAM_APPOINT_NO_PREFIX = "YY";

    /**
     * 治疗申请单号前缀：与 sql/11 铺底单号（TAPPLY20240101001）同族，新旧单号一眼是一类单据
     */
    String TREATMENT_APPLY_NO_PREFIX = "TAPPLY";
    /**
     * 待发药记录号前缀：DSP + yyyyMMdd + 4 位（DP 已被透析号占用，两个 DP 在单号检索里会互相命中）
     */
    String DISPENSING_NO_PREFIX = "DSP";
    /**
     * 临床路径入径单号前缀：LP + yyyyMMdd + 4 位
     */
    String PATHWAY_NO_PREFIX = "LP";
    /**
     * 透析号前缀：DP + yyyyMMdd + 4 位（透析室患者档案号，与住院号/门诊号无关）
     */
    String DIALYSIS_PATIENT_NO_PREFIX = "DP";
    /**
     * 透析单号前缀：HD + yyyyMMdd + 4 位（一次治疗单元一单）
     */
    String DIALYSIS_SESSION_NO_PREFIX = "HD";
    /**
     * ICU 入科单号前缀：ICU + yyyyMMdd + 4 位（与院感 ICASE 前缀区分开）
     */
    String ICU_STAY_NO_PREFIX = "ICU";

    String INFECTIOUS_REPORT_NO_PREFIX = "INF";

    /**
     * 院感病例编号前缀：ICASE + yyyyMMdd + 4 位
     */
    String INFECTION_CASE_NO_PREFIX = "ICASE";

    /**
     * 目标性监测编号前缀：IMON + yyyyMMdd + 4 位
     */
    String INFECTION_MONITOR_NO_PREFIX = "IMON";

    /**
     * 医疗纠纷/投诉单号前缀：DS + yyyyMMdd + 4 位（Dispute）
     */
    String DISPUTE_NO_PREFIX = "DS";
    /**
     * 远程会诊单号前缀：TC + yyyyMMdd + 4 位（Tele-consultation）
     */
    String TELE_CONSULT_NO_PREFIX = "TC";
    /**
     * 互联网线上问诊单号前缀：OC + yyyyMMdd + 4 位（Online-consultation）
     */
    String ONLINE_CONSULT_NO_PREFIX = "OC";
    /**
     * 日间手术登记单号前缀：DA + yyyyMMdd + 4 位（Day-surgery Appointment）
     */
    String DAY_SURGERY_NO_PREFIX = "DA";

    /**
     * 单号宽度口径：日均量能上千的（处方、病历号、检查/检验申请与记录、报告、消息）用 5 位，
     * 其余低频单据用 4 位。4 位一旦当天发满，第 10001 单会撞唯一索引（发号器不会自己报错）。
     */
    String RULE_CHECK_NO_PREFIX = "RC";
    String CHRONIC_RECORD_NO_PREFIX = "CHR";
    String REFUND_APPLY_NO_PREFIX = "RA";
    String RX_FLOW_NO_PREFIX = "RXF";
    String PUBLIC_HEALTH_REPORT_NO_PREFIX = "PH";
    String PRESCRIPTION_NO_PREFIX = "RX";
    String INSPECTION_APPLY_NO_PREFIX = "INS";
    String LABORATORY_APPLY_NO_PREFIX = "LAB";
    String MEDICAL_RECORD_NO_PREFIX = "MR";
    String FOLLOWUP_TASK_NO_PREFIX = "FT";
    /**
     * 归档编号前缀：MA + yyyyMMdd + 4 位。原先两条归档路径一个写 MA 一个写 ARC，
     * 同一张表的同一列长出两种格式，按号检索要猜前缀。统一成 MA。
     */
    String MEDICAL_RECORD_ARCHIVE_NO_PREFIX = "MA";
    String INSPECTION_RECORD_NO_PREFIX = "IR";
    String LABORATORY_RECORD_NO_PREFIX = "LR";
    String REPORT_NO_PREFIX = "RPT";
    String ECG_WAVE_NO_PREFIX = "ECG";
    String MESSAGE_NO_PREFIX = "MSG";
    String STAT_REPORT_NO_PREFIX = "TJ";
    /**
     * AI 药审写进校验表的单号：与人工校验的 RC 分前缀也分号段，
     * 便于按前缀统计 AI 命中，且两张号段互不占用当天的序号
     */
    String RULE_CHECK_AI_NO_PREFIX = "RCAI";
}
