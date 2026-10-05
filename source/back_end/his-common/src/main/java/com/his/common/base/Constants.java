package com.his.common.base;

public interface Constants {

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
}
