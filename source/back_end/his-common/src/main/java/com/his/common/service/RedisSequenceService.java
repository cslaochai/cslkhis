package com.his.common.service;

/**
 * 基于 Redis 的分布式序列号 / 业务单号生成服务。
 *
 * <p>key 格式：{@code {module}:{yyyyMMdd}}，过期时间 24 小时。
 *
 * <p><b>为什么单号不走数据库自增：</b>业务单号（患者号、结算单号、发票号…）要求"当天连续、
 * 跨天归零、多实例不撞号"。数据库自增满足不了"跨天归零"，进程内 {@code AtomicInteger}
 * 满足不了"多实例不撞号"（多副本部署时两实例会发同一个号）—— 只有 Redis 的原子
 * {@code INCR} + 按天分 key 能同时满足三条。
 *
 * <p><b>两层用法的分工：</b>{@link #next(String)} 是<b>发号原语</b>，只保证"某个 key 当天递增不重号"，
 * 给非单号场景用（配置表主键、叫号队列位次）；业务单号一律走下面的 {@code generateXxxNo()}，
 * 因为一个单号同时钉死四件事——Redis key 名、前缀、日期段、序号宽度。四件事写进调用点的实参里
 * 就等于没有口径：换 key 名会让计数器归 1、与当天已发的号撞唯一索引，而这既不报错也看不出来。
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
}
