package com.his.system.service;

/**
 * 基于 Redis 的分布式序列号 / 业务单号生成服务。
 */
public interface RedisSequenceService {

    /**
     * 获取下一个序列号
     *
     * @param module 模块标识，如 PATIENT、QUEUE
     * @return 自增后的序列号（从 1 开始）
     */
    long next(String module);

    /**
     * 生成患者号：P + 年月日 + 5位流水号（Redis 递增，24小时过期）
     */
    String generatePatientNo();

    /**
     * 生成角色编号
     * 格式：R + 年月日 + 4位流水号，（Redis 递增，24小时过期）
     */
    String generateRoleCode();

    /**
     * 生成挂号单号：A + 年月日 + 5位流水号
     */
    String generateAppointNo();

    /**
     * 生成员工工号：E + 年月日 + 5位流水号
     */
    String generateEmployeeNo();

    /**
     * 生成急诊号：JZ + 年月日 + 5位流水号
     */
    String generateEmergencyNo();

    /**
     * 生成采购订单号：CG + 年月日 + 4位流水号
     * 单号一经生成即固化（订单表 order_no 有唯一索引），不随订单编辑而变。
     */
    String generatePurchaseNo();

    /**
     * 生成药品入库单号：IN + 年月日 + 3位流水号
     * 与 sql/7 铺底单号（IN20240101001）同格式，避免新旧单号看不出是一类单据。
     */
    String generateInboundNo();

    /**
     * 生成不良事件编号：AE + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    String generateAdverseEventNo();

    /**
     * 生成病案借阅/复印单号：BR + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    String generateArchiveBorrowNo();

    /**
     * 生成编码任务号：CT + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    String generateCodeTaskNo();

    /**
     * 生成住院摆药单号：WD + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    String generateWardDispenseNo();

    /**
     * 生成静配单号：PV + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    String generatePivasNo();

    /**
     * 生成药房盘点单号：PD + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    String generateStocktakeNo();

    /**
     * 生成药品调拨单号：TB + 年月日 + 4位流水号（Redis 递增，24小时过期，sql/154）
     */
    String generateDrugTransferNo();

    /**
     * 生成药品供应商退货单号：TG + 年月日 + 4位流水号（Redis 递增，24小时过期，sql/154）
     */
    String generateSupplierReturnNo();

    /**
     * 生成中药代煎单号：TCMD + 年月日 + 4位流水号（Redis 递增，24小时过期，sql/139）
     */
    String generateTcmDecoctNo();

    /**
     * 生成三级质控流转单号：QCF + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    String generateRecordQcFlowNo();

    /**
     * 生成检查预约单号：YY + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    String generateExamAppointNo();

    /**
     * 生成门诊治疗申请单号：TAPPLY + 年月日 + 4位流水号（Redis 递增，24小时过期）
     */
    String generateTreatmentApplyNo();

    /**
     * 待发药记录编号：DSP + yyyyMMdd + 4 位序号（原先是「DP + 时间戳 + 进程内 AtomicInteger」，多实例会撞号）
     */
    String generateDispensingNo();

    /**
     * 传染病报卡编号：INF + yyyyMMdd + 4 位序号
     */
    String generateInfectiousReportNo();

    /**
     * 院感病例编号：ICASE + yyyyMMdd + 4 位序号
     */
    String generateInfectionCaseNo();

    /**
     * 死亡证明编号：DC + yyyyMMdd + 4 位序号（sql/157）
     */
    String generateDeathCertNo();

    /**
     * 死亡登记号：RG + yyyyMMdd + 4 位序号（sql/157）
     */
    String generateDeathRegisterNo();

    /**
     * 病危重通知单号：BT + yyyyMMdd + 4 位序号（sql/161）
     */
    String generateCriticalNoticeNo();

    /**
     * 住院请假单号：LV + yyyyMMdd + 4 位序号（sql/162）
     */
    String generateInpatientLeaveNo();

    /**
     * 临床路径入径单号：LP + yyyyMMdd + 4 位序号
     */
    String generatePathwayNo();

    /**
     * 透析号：DP + yyyyMMdd + 4 位序号
     */
    String generateDialysisPatientNo();

    /**
     * 透析单号：HD + yyyyMMdd + 4 位序号
     */
    String generateDialysisSessionNo();

    /**
     * ICU 入科单号：ICU + yyyyMMdd + 4 位序号
     */
    String generateIcuStayNo();

    /**
     * 目标性监测编号：IMON + yyyyMMdd + 4 位序号
     */
    String generateInfectionMonitorNo();

    /**
     * 费用记账流水号：FR + yyyyMMdd + 5 位序号
     */
    String generateFeeNo();

    /**
     * 结算账单号：SB + yyyyMMdd + 5 位序号
     */
    String generateBillNo();

    /**
     * 收款流水号：PT + yyyyMMdd + 5 位序号
     */
    String generatePayTxnNo();

    /**
     * 退款流水号：RT + yyyyMMdd + 5 位序号
     */
    String generateRefundTxnNo();

    /**
     * 资金账户流水号：AT + yyyyMMdd + 5 位序号
     */
    String generateFundTxnNo();

    /**
     * 发票号：IV + yyyyMMdd + 5 位序号（旧口径用时间戳+随机数，同秒并发会撞号且对账页无法判重）
     */
    String generateInvoiceNo();

    /**
     * 医保结算清单号：IS + yyyyMMdd + 5 位序号（与发票号同因：旧的时间戳+随机数会撞号）
     */
    String generateInsuranceSettlementNo();

    /**
     * 飞检批次号：FI + yyyyMMdd + 4 位序号
     */
    String generateYbInspectNo();

    /**
     * 扣款通知单号：DK + yyyyMMdd + 4 位序号
     */
    String generateYbDeductNo();

    /**
     * 慢特病备案单号：MT + yyyyMMdd + 4 位序号
     */
    String generateChronicRegNo();

    /**
     * 临床规则校验编号：RC + yyyyMMdd + 4 位序号
     */
    String generateRuleCheckNo();

    /**
     * AI 药审校验编号：RCAI + yyyyMMdd + 4 位序号（与人工 RC 独立号段，原先是时间戳+6 位随机数）
     */
    String generateRuleCheckAiNo();

    /**
     * 慢病建档/认定编号：CHR + yyyyMMdd + 4 位序号
     */
    String generateChronicRecordNo();

    /**
     * 退费申请单号：RA + yyyyMMdd + 4 位序号
     */
    String generateRefundApplyNo();

    /**
     * 处方流转单号：RXF + yyyyMMdd + 4 位序号
     */
    String generateRxFlowNo();

    /**
     * 公卫上报单号：PH + yyyyMMdd + 4 位序号
     */
    String generatePublicHealthReportNo();

    /**
     * 处方号：RX + yyyyMMdd + 5 位序号（门诊处方日均上千，4 位一天能发满）
     */
    String generatePrescriptionNo();

    /**
     * 检查申请单号：INS + yyyyMMdd + 5 位序号
     */
    String generateInspectionApplyNo();

    /**
     * 检验申请单号：LAB + yyyyMMdd + 5 位序号
     */
    String generateLaboratoryApplyNo();

    /**
     * 门诊病历号：MR + yyyyMMdd + 5 位序号
     */
    String generateMedicalRecordNo();

    /**
     * 随访任务号：FT + yyyyMMdd + 4 位序号
     */
    String generateFollowupTaskNo();

    /**
     * 病案归档编号：MA + yyyyMMdd + 4 位序号（ARC 前缀已并入此口径）
     */
    String generateMedicalRecordArchiveNo();

    /**
     * 检查执行记录号：IR + yyyyMMdd + 5 位序号
     */
    String generateInspectionRecordNo();

    /**
     * 检验执行记录号：LR + yyyyMMdd + 5 位序号
     */
    String generateLaboratoryRecordNo();

    /**
     * 报告单号：RPT + yyyyMMdd + 5 位序号（检查/检验/心电/放射共用同一张报告表，共用一个号段）
     */
    String generateReportNo();

    /**
     * 心电波形号：ECG + yyyyMMdd + 4 位序号
     */
    String generateEcgWaveNo();

    /**
     * 消息编号：MSG + yyyyMMdd + 5 位序号（广播一次写多人，一人一号，量按人次算）
     */
    String generateMessageNo();

    /**
     * 病案统计上报单号：TJ + yyyyMMdd + 4 位序号（原先是时间戳+3 位随机数，同秒并发会撞号）
     */
    String generateStatReportNo();

    /**
     * 生成输血申请单号：SX + yyyyMMdd + 4 位序号
     */
    String generateTransfusionApplyNo();

    /**
     * 生成危急值编号：WJ + yyyyMMdd + 4 位序号
     */
    String generateCriticalValueNo();

    /**
     * 生成 AI 病历质控编号：QCAI + yyyyMMdd + 4 位序号
     */
    String generateEmrQcNo();

    /**
     * 生成字段变更批次号：FC + yyyyMMdd + 4 位序号
     */
    String generateFieldChangeBatchNo();

    /**
     * 生成医保合规审计号：CA + yyyyMMdd + 4 位序号
     */
    String generateComplianceAuditNo();

    /**
     * 生成药品追溯码：DR + yyyyMMdd + 4 位序号
     */
    String generateDrugTraceNo();

    /**
     * 生成药品追溯上传批次号：UP + yyyyMMdd + 4 位序号
     */
    String generateDrugUploadBatchNo();

    /**
     * 生成高值耗材追溯码：HV + yyyyMMdd + 4 位序号
     */
    String generateHighValueTraceNo();

    /**
     * 生成耗材出入库流水号：LC + yyyyMMdd + 4 位序号
     */
    String generateConsumableStockLogNo();

    /**
     * 生成手术申请单号：SS + yyyyMMdd + 4 位序号
     */
    String generateOperationApplyNo();

    /**
     * 生成手术清点单号：QD + yyyyMMdd + 4 位序号
     */
    String generateOperationCountNo();

    /**
     * 生成麻醉访视单号：MF + yyyyMMdd + 4 位序号
     */
    String generateAnesthesiaVisitNo();

    /**
     * 生成麻醉记录单号：MZ + yyyyMMdd + 4 位序号
     */
    String generateAnesthesiaRecordNo();

    /**
     * 生成麻醉随访单号：MS + yyyyMMdd + 4 位序号
     */
    String generateAnesthesiaFollowupNo();

    /**
     * 生成复苏室（PACU）记录单号：FS + yyyyMMdd + 4 位序号
     */
    String generatePacuNo();

    /**
     * 生成手术安全核查单号：HC + yyyyMMdd + 4 位序号
     */
    String generateOperationSafetyCheckNo();

    /**
     * 生成住院医嘱单号：RZ + yyyyMMdd + 3 位序号
     */
    String generateAdmissionOrderNo();

    /**
     * 生成待床登记号：DC + yyyyMMdd + 3 位序号
     */
    String generateBedWaitNo();

    /**
     * 生成床位分配号：TP + yyyyMMdd + 3 位序号
     */
    String generateBedAllocateNo();

    /**
     * 生成会诊单号：HZ + yyyyMMdd + 4 位序号
     */
    String generateConsultationNo();

    /**
     * 生成住院病历记录号：BL + yyyyMMdd + 4 位序号（住院病历/会诊记录/转诊记录共用一个 BL 号段）
     */
    String generateInpatientRecordNo();

    /**
     * 生成护理评估单号：AS + yyyyMMdd + 4 位序号
     */
    String generateNursingAssessNo();

    /**
     * 生成护理记录单号：HL + yyyyMMdd + 4 位序号
     */
    String generateNursingRecordNo();

    /**
     * 生成入院单号：ADM + yyyyMMdd + 3 位序号
     */
    String generateAdmissionNo();

    /**
     * 生成出院单号：DIS + yyyyMMdd + 3 位序号
     */
    String generateDischargeNo();

    /**
     * 生成就诊次号：VISIT + yyyyMMdd + 3 位序号
     */
    String generateVisitNo();

    /**
     * 生成住院医嘱单号：YZ + yyyyMMdd + 4 位序号
     */
    String generateInpatientOrderNo();

    /**
     * 生成医嘱分组号：G + yyyyMMdd + 4 位序号
     */
    String generateOrderGroupNo();

    /**
     * 生成转诊/转科单号：ZK + yyyyMMdd + 4 位序号
     */
    String generateTransferNo();

    /**
     * 生成患者合并号：HB + yyyyMMdd + 4 位序号
     */
    String generatePatientMergeNo();

    /**
     * 生成转诊单号：REF + yyyyMMdd + 4 位序号
     */
    String generateReferralNo();

    /**
     * 生成出院带药单号：DDA + yyyyMMdd + 4 位序号
     */
    String generateDischargeDrugNo();

    /**
     * 生成 VTE 预防措施单号：VP + yyyyMMdd + 4 位序号
     */
    String generateVtePreventNo();

    /**
     * 生成 VTE 事件单号：VE + yyyyMMdd + 4 位序号
     */
    String generateVteEventNo();

    /**
     * 生成膳食方案单号：DP + yyyyMMdd + 4 位序号
     */
    String generateDietPlanNo();

    /**
     * 生成营养风险筛查单号：NS + yyyyMMdd + 4 位序号
     */
    String generateNutritionScreenNo();

    /**
     * 生成订餐单号：MO + yyyyMMdd + 4 位序号
     */
    String generateMealOrderNo();

    /**
     * 生成体检登记记录号：CU + yyyyMMdd + 4 位序号
     */
    String generateCheckupRecordNo();

    /**
     * 生成抗菌药物处方权授权单号：KJ + yyyyMMdd + 4 位序号
     */
    String generateAntibioticAuthNo();

    /**
     * 生成抗菌药物 I 类切口点评单号：KQI + yyyyMMdd + 4 位序号
     */
    String generateAntibioticReviewNo();

    /**
     * 生成质控单号：QC + yyyyMMdd + 4 位序号
     */
    String generateQcStoreNo();

    /**
     * 生成处方点评批次号：RXRB + yyyyMMdd + 4 位序号
     */
    String generateRxReviewBatchNo();

    /**
     * 生成药师约谈编号：YT + yyyyMMdd + 4 位序号
     */
    String generateRxDoctorTalkNo();

    /**
     * 生成麻精药品专册登记号：NZ + yyyyMMdd + 4 位序号
     */
    String generateNarcoticRegisterNo();

}
