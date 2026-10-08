package com.his.common.base;

/**
 * 两类"前缀"是一对，但落在完全不同的地方
 */
public interface BizCodeConstants {

    /**
     * 患者档案号
     */
    String PATIENT_NO_KEY_PREFIX = "PATIENT";
    /**
     * 门诊挂号单号
     */
    String APPOINT_NO_KEY_PREFIX = "APPOINT";
    /**
     * 员工档案号
     */
    String EMPLOYEE_NO_KEY_PREFIX = "EMPLOYEE";
    /**
     * 药品编码。<b>本对常量已无引用</b>：药品编码是药品主档里的业务输入（按本位码/厂家编码录入），
     * 从来不走 Redis 发号器。
     */
    String DRUG_NO_KEY_PREFIX = "DRUG";
    /**
     * 候诊排队号。<b>本对常量已无引用</b>：排队号实发的是「诊室呼叫代号 + 当日该诊室序号」
     * （按诊室分号段，见叫号 service），既不用 Q 前缀也不走这个 key。
     */
    String QUEUE_NO_KEY_PREFIX = "QUEUE";
    /**
     * 角色编码
     */
    String ROLE_NO_KEY_PREFIX = "ROLE";
    /**
     * 急诊单号
     */
    String EMERGENCY_NO_KEY_PREFIX = "EMERGENCY";
    /**
     * 药品采购订单号
     */
    String PURCHASE_NO_KEY_PREFIX = "PURCHASE";
    /**
     * 药品入库单号
     */
    String INBOUND_NO_KEY_PREFIX = "INBOUND";
    /**
     * 药品不良反应上报单号
     */
    String ADVERSE_EVENT_NO_KEY_PREFIX = "ADVERSE_EVENT";
    /**
     * 病案借阅单号
     */
    String ARCHIVE_BORROW_NO_KEY_PREFIX = "ARCHIVE_BORROW";
    /**
     * 病案编码任务单号（ICD 编码流水线：待编码 → 已提交 → 已完成/已退修）
     */
    String CODE_TASK_NO_KEY_PREFIX = "CODE_TASK";
    /**
     * 病区摆药单号
     */
    String WARD_DISPENSE_NO_KEY_PREFIX = "WARD_DISPENSE";
    /**
     * 静配中心冲配批次号（PIVAS = Pharmacy Intravenous Admixture Services）
     */
    String PIVAS_NO_KEY_PREFIX = "PIVAS";
    /**
     * 药房盘点单号
     */
    String STOCKTAKE_NO_KEY_PREFIX = "STOCKTAKE";
    /**
     * 药品调拨单
     */
    String DRUG_TRANSFER_NO_KEY_PREFIX = "DRUG_TRANSFER";

    /**
     * 供应商退货单
     */
    String SUPPLIER_RETURN_NO_KEY_PREFIX = "SUPPLIER_RETURN";
    /**
     * 中药代煎单号
     */
    String TCM_DECOCT_NO_KEY_PREFIX = "TCM_DECOCT";
    /**
     * 三级质控流转单
     */
    String RECORD_QC_FLOW_NO_KEY_PREFIX = "RECORD_QC_FLOW";
    /**
     * 检查预约单号（医技时段占用）
     */
    String EXAM_APPOINT_NO_KEY_PREFIX = "EXAM_APPOINT";
    /**
     * 治疗申请单号
     */
    String TREATMENT_APPLY_NO_KEY_PREFIX = "TREATMENT_APPLY";
    /**
     * 待发药记录号（药房处方流转位）
     */
    String DISPENSING_NO_KEY_PREFIX = "DISPENSING";
    /**
     * 临床路径入径单号
     */
    String PATHWAY_NO_KEY_PREFIX = "PATHWAY";
    /**
     * 透析患者档案号
     */
    String DIALYSIS_PATIENT_NO_KEY_PREFIX = "DIALYSIS_PATIENT";
    /**
     * 透析治疗单号
     */
    String DIALYSIS_SESSION_NO_KEY_PREFIX = "DIALYSIS_SESSION";
    /**
     * ICU 入科单号
     */
    String ICU_STAY_NO_KEY_PREFIX = "ICU_STAY";

    /**
     * 记账行号（临床单据产生的应收流水）
     */
    String FEE_NO_KEY_PREFIX = "FEE";
    /**
     * 结算账单号（一批应收合并出账的合计凭证）
     */
    String BILL_NO_KEY_PREFIX = "BILL";
    /**
     * 收款流水号
     */
    String PAY_TXN_NO_KEY_PREFIX = "PAY_TXN";
    /**
     * 退款流水号（与收款分号段，退这笔钱不复用收款序号）
     */
    String REFUND_TXN_NO_KEY_PREFIX = "REFUND_TXN";
    /**
     * 账户（余额）变动流水号
     */
    String FUND_TXN_NO_KEY_PREFIX = "FUND_TXN";
    /**
     * 发票号（L4 票据，票号是财政序列）
     */
    String INVOICE_NO_KEY_PREFIX = "INVOICE";
    /**
     * 医保结算清单号（sql/136 起由 L2 出账生成，不再用时间戳+随机数：同秒两张单会撞号，报盘后医保侧无从追溯）。
     * key 名是全称 INSURANCE_SETTLEMENT，与常量名 ISB_* 不同名，属历史 key，值不能改。
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

    /**
     * 传染病报告卡编号（网络直报送出的卡号）
     */
    String INFECTIOUS_REPORT_NO_KEY_PREFIX = "INFECTIOUS_REPORT";
    /**
     * 院感病例编号
     */
    String INFECTION_CASE_NO_KEY_PREFIX = "INFECTION_CASE";
    /**
     * 院感目标性监测编号
     */
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
     * 住院请假单号（sql/162）。key 名是 INPATIENT_LEAVE，与常量名 LEAVE_RECORD_* 不同名，属历史 key，值不能改。
     */
    String LEAVE_RECORD_NO_KEY_PREFIX = "INPATIENT_LEAVE";

    /**
     * 临床规则校验流水号（用药禁忌/合理性校验留痕）
     */
    String RULE_CHECK_NO_KEY_PREFIX = "RULE_CHECK";
    /**
     * 慢病建档记录号
     */
    String CHRONIC_RECORD_NO_KEY_PREFIX = "CHRONIC_RECORD";
    /**
     * 退费申请单号（审批单，不是资金流水）
     */
    String REFUND_APPLY_NO_KEY_PREFIX = "REFUND_APPLY";
    /**
     * 处方流转单号（开方到配药/发药之间的流转记录）
     */
    String RX_FLOW_NO_KEY_PREFIX = "RX_FLOW";
    /**
     * 公共卫生报告单号
     */
    String PUBLIC_HEALTH_REPORT_NO_KEY_PREFIX = "PUBLIC_HEALTH_REPORT";
    /**
     * 处方号
     */
    String PRESCRIPTION_NO_KEY_PREFIX = "PRESCRIPTION";
    /**
     * 检查申请单号（影像/心电等医技申请）
     */
    String INSPECTION_APPLY_NO_KEY_PREFIX = "INSPECTION_APPLY";
    /**
     * 检验申请单号（标本申请）
     */
    String LABORATORY_APPLY_NO_KEY_PREFIX = "LABORATORY_APPLY";
    /**
     * 病历号（住院病历标识）
     */
    String MEDICAL_RECORD_NO_KEY_PREFIX = "MEDICAL_RECORD";
    /**
     * 随访任务号
     */
    String FOLLOWUP_TASK_NO_KEY_PREFIX = "FOLLOWUP_TASK";
    /**
     * 病案归档编号
     */
    String MEDICAL_RECORD_ARCHIVE_NO_KEY_PREFIX = "MEDICAL_RECORD_ARCHIVE";
    /**
     * 检查记录号（一次检查执行）
     */
    String INSPECTION_RECORD_NO_KEY_PREFIX = "INSPECTION_RECORD";
    /**
     * 检验记录号（一次标本检验）
     */
    String LABORATORY_RECORD_NO_KEY_PREFIX = "LABORATORY_RECORD";
    /**
     * 医技报告号
     */
    String REPORT_NO_KEY_PREFIX = "REPORT";
    /**
     * 心电波形记录号（一次采样的波形数据）
     */
    String ECG_WAVE_NO_KEY_PREFIX = "ECG_WAVE";
    /**
     * 站内消息号
     */
    String MESSAGE_NO_KEY_PREFIX = "MESSAGE";
    /**
     * 统计报表号
     */
    String STAT_REPORT_NO_KEY_PREFIX = "STAT_REPORT";
    /**
     * AI 药审的校验流水（与人工校验分号段，见 RULE_CHECK_AI_NO_PREFIX）
     */
    String RULE_CHECK_AI_NO_KEY_PREFIX = "RULE_CHECK_AI";

    /**
     * ST问卷卷号
     */
    String SURVEY_TEMPLATE_NO_KEY_PREFIX = "SURVEY_TEMPLATE";

    /**
     * SD发放单号
     */
    String SURVEY_DISPATCH_NO_KEY_PREFIX = "SURVEY_DISPATCH";

    /**
     * SV答卷号
     */
    String SURVEY_ANSWER_NO_KEY_PREFIX = "SURVEY_ANSWER";

    /**
     * --------------------------------------------------------------------------------------编号前缀---------------------------------------------------------------------------------------------
     * 患者档案号前缀
     */
    String PATIENT_NO_PREFIX = "PA";
    /**
     * 挂号单号前缀
     */
    String APPOINT_NO_PREFIX = "AP";
    /**
     * 药品编码前缀
     */
    String DRUG_NO_PREFIX = "D";
    /**
     * 排队号前缀
     */
    String QUEUE_NO_PREFIX = "Q";
    /**
     * 角色编码前缀
     */
    String ROLE_NO_PREFIX = "R";
    /**
     * 员工档案号前缀
     */
    String EMPLOYEE_NO_PREFIX = "E";
    /**
     * 急诊单号前缀（JZ = 急诊）
     */
    String EMERGENCY_NO_PREFIX = "JZ";
    /**
     * 采购订单号前缀（CG = 采购）
     */
    String PURCHASE_NO_PREFIX = "CG";
    /**
     * 入库单号前缀
     */
    String INBOUND_NO_PREFIX = "IN";
    /**
     * 不良反应单号前缀（AE = Adverse Event）
     */
    String ADVERSE_EVENT_NO_PREFIX = "AE";
    /**
     * 病案借阅单号前缀（BR = Borrow）
     */
    String ARCHIVE_BORROW_NO_PREFIX = "BR";
    /**
     * 病案编码任务单号前缀（CT = Code Task）
     */
    String CODE_TASK_NO_PREFIX = "CT";
    /**
     * 病区摆药单号前缀（WD）
     */
    String WARD_DISPENSE_NO_PREFIX = "WD";
    /**
     * 静配批次号前缀（PV = PIVAS）
     */
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
    /**
     * 三级质控流转单号前缀（QCF）
     */
    String RECORD_QC_FLOW_NO_PREFIX = "QCF";

    /**
     * 死亡证明编号前缀
     */
    String DEATH_CERT_NO_PREFIX = "DC";
    /**
     * 死亡登记号前缀
     */
    String DEATH_REGISTER_NO_PREFIX = "RG";
    /**
     * 病危重通知单号前缀
     */
    String CRITICAL_NOTICE_NO_PREFIX = "BT";
    /**
     * 住院请假单号前缀
     */
    String LEAVE_RECORD_NO_PREFIX = "LV";
    /**
     * SV答卷号前缀
     */
    String SURVEY_TEMPLATE_NO_PREFIX = "ST";

    /**
     * SD发放单号前缀
     */
    String SURVEY_DISPATCH_NO_PREFIX = "SD";
    /**
     * SV答卷号前缀
     */
    String SURVEY_ANSWER_NO_PREFIX = "SV";

    /**
     * 记账行号前缀：FR + yyyyMMdd + 5 位
     */
    String FEE_NO_PREFIX = "FR";
    /**
     * 账单号前缀：SB + yyyyMMdd + 5 位
     */
    String BILL_NO_PREFIX = "SB";
    /**
     * 收款流水号前缀
     */
    String PAY_TXN_NO_PREFIX = "PT";
    /**
     * 退款流水号前缀
     */
    String REFUND_TXN_NO_PREFIX = "RT";
    /**
     * 账户余额流水号前缀
     */
    String FUND_TXN_NO_PREFIX = "AT";
    /**
     * 发票号前缀
     */
    String INVOICE_NO_PREFIX = "IV";
    /**
     * 医保结算清单号前缀
     */
    String ISB_NO_PREFIX = "IS";
    /**
     * 飞检批次号前缀
     */
    String YB_INSPECT_NO_PREFIX = "FI";
    /**
     * 扣款通知单号前缀
     */
    String YB_DEDUCT_NO_PREFIX = "DK";
    /**
     * 慢特病备案单号前缀
     */
    String CHRONIC_REG_NO_PREFIX = "MT";

    /**
     * 检查预约单号前缀
     */
    String EXAM_APPOINT_NO_PREFIX = "YY";

    /**
     * 治疗申请单号前缀
     */
    String TREATMENT_APPLY_NO_PREFIX = "TAPPLY";
    /**
     * 待发药记录号前缀
     */
    String DISPENSING_NO_PREFIX = "DSP";
    /**
     * 临床路径入径单号前缀
     */
    String PATHWAY_NO_PREFIX = "LP";
    /**
     * 透析号前缀
     */
    String DIALYSIS_PATIENT_NO_PREFIX = "DP";
    /**
     * 透析单号前缀
     */
    String DIALYSIS_SESSION_NO_PREFIX = "HD";
    /**
     * ICU 入科单号前缀
     */
    String ICU_STAY_NO_PREFIX = "ICU";

    /**
     * 传染病报告卡编号前缀
     */
    String INFECTIOUS_REPORT_NO_PREFIX = "INF";

    /**
     * 院感病例编号前缀
     */
    String INFECTION_CASE_NO_PREFIX = "ICASE";

    /**
     * 目标性监测编号前缀
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
     * 临床规则校验单号前缀（人工校验）
     */
    String RULE_CHECK_NO_PREFIX = "RC";
    /**
     * 慢病建档记录号前缀（CHR = Chronic Record）
     */
    String CHRONIC_RECORD_NO_PREFIX = "CHR";
    /**
     * 退费申请单号前缀（RA）
     */
    String REFUND_APPLY_NO_PREFIX = "RA";
    /**
     * 处方流转单号前缀（RXF，与处方号 RX 分两条流水）
     */
    String RX_FLOW_NO_PREFIX = "RXF";
    /**
     * 公共卫生报告单号前缀（PH = Public Health）
     */
    String PUBLIC_HEALTH_REPORT_NO_PREFIX = "PH";
    /**
     * 处方号前缀（RX = 处方）
     */
    String PRESCRIPTION_NO_PREFIX = "RX";
    /**
     * 检查申请单号前缀（INS = Inspection）
     */
    String INSPECTION_APPLY_NO_PREFIX = "INS";
    /**
     * 检验申请单号前缀（LAB = Laboratory）
     */
    String LABORATORY_APPLY_NO_PREFIX = "LAB";
    /**
     * 病历号前缀（MR = Medical Record）
     */
    String MEDICAL_RECORD_NO_PREFIX = "MR";
    /**
     * 随访任务号前缀（FT = Followup Task）
     */
    String FOLLOWUP_TASK_NO_PREFIX = "FT";
    /**
     * 归档编号前缀：MA + yyyyMMdd + 4 位。原先两条归档路径一个写 MA 一个写 ARC，
     * 同一张表的同一列长出两种格式，按号检索要猜前缀。统一成 MA。
     */
    String MEDICAL_RECORD_ARCHIVE_NO_PREFIX = "MA";
    /**
     * 检查记录号前缀（IR = Inspection Record）
     */
    String INSPECTION_RECORD_NO_PREFIX = "IR";
    /**
     * 检验记录号前缀（LR = Laboratory Record）
     */
    String LABORATORY_RECORD_NO_PREFIX = "LR";
    /**
     * 医技报告号前缀（RPT = Report）
     */
    String REPORT_NO_PREFIX = "RPT";
    /**
     * 心电波形记录号前缀（ECG，一次采样一条波形数据）
     */
    String ECG_WAVE_NO_PREFIX = "ECG";
    /**
     * 站内消息号前缀（MSG）
     */
    String MESSAGE_NO_PREFIX = "MSG";
    /**
     * 统计报表号前缀（TJ = 统计）
     */
    String STAT_REPORT_NO_PREFIX = "TJ";
    /**
     * AI 药审写进校验表的单号：与人工校验的 RC 分前缀也分号段，
     * 便于按前缀统计 AI 命中，且两张号段互不占用当天的序号
     */
    String RULE_CHECK_AI_NO_PREFIX = "RCAI";
}
