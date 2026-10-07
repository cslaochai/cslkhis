package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.charge.api.AppointGateway;
import com.his.charge.api.EmrGateway;
import com.his.charge.api.PatientGateway;
import com.his.charge.entity.*;
import com.his.charge.enums.InsuranceReportStatusEnum;
import com.his.charge.enums.InsuranceReportTypeEnum;
import com.his.charge.mapper.*;
import com.his.charge.service.InsuranceChannelService;
import com.his.charge.service.InsuranceSettlementService;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.SensitiveMaskUtil;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 医保结算清单实现（L2 账单的报盘出口）。
 *
 * <p>清单的三个数各有唯一来源，谁都不许估：
 * 统筹抄账单统筹金额（L2 逐行 split 的结果），个账与自付从本账单的
 * <b>成功收款流水</b>现算（{@code pay_method=4} 是刷参保人卡的真钱，其余是现金类）。
 * 旧实现在这里写死了「自付超过 100 就当他刷了 100 个账」，报出去的数与金库无关。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceSettlementServiceImpl
        extends ServiceImpl<BizInsuranceSettlementMapper, BizInsuranceSettlement>
        implements InsuranceSettlementService {

    private static final int AMOUNT_SCALE = 2;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final PatientGateway patientGateway;
    private final AppointGateway appointGateway;
    private final BizSettlementBillMapper bizSettlementBillMapper;
    private final BizSettlementBillItemMapper bizSettlementBillItemMapper;
    private final BizPaymentTxnMapper bizPaymentTxnMapper;
    private final BizInvoiceMapper bizInvoiceMapper;
    private final BizInsuranceReportMapper bizInsuranceReportMapper;
    private final InsuranceChannelService insuranceChannelService;
    private final RedisSequenceService redisSequenceService;
    private final ObjectMapper objectMapper;
    private final EmrGateway emrGateway;

    // 查询

    @Override
    public PageResult<BizInsuranceSettlementVO> selectSettlementPage(Long patientId, String patientName,
                                                                     Integer settlementStatus,
                                                                     int pageNum, int pageSize) {
        LambdaQueryWrapper<BizInsuranceSettlement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizInsuranceSettlement::getPatientId, patientId)
                .like(StringUtils.hasText(patientName), BizInsuranceSettlement::getPatientName, patientName)
                .eq(settlementStatus != null, BizInsuranceSettlement::getSettlementStatus, settlementStatus)
                .orderByDesc(BizInsuranceSettlement::getCreateTime);

        Page<BizInsuranceSettlement> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<BizInsuranceSettlementVO> records = new ArrayList<>(page.getRecords().size());
        for (BizInsuranceSettlement row : page.getRecords()) {
            records.add(toListVO(row));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public BizInsuranceSettlement getSettlementDetail(Long settlementId) {
        return this.getById(settlementId);
    }

    @Override
    public BizInsuranceSettlementVO getSettlementVO(Long settlementId) {
        return toListVO(getSettlementDetail(settlementId));
    }

    // 出账与账单联动

    @Override
    public InsuranceSettlementDetailVO getSettlementDetailVO(Long settlementId) {
        BizInsuranceSettlement settlement = requireSettlement(settlementId);
        InsuranceSettlementDetailVO vo = new InsuranceSettlementDetailVO();
        BeanUtils.copyProperties(settlement, vo);

        // 1. 患者档案补齐性别/年龄/证号/医保号（清单只是快照，允许有空）
        PatientBriefVO patient = settlement.getPatientId() == null ? null
                : patientGateway.findPatient(settlement.getPatientId());
        if (patient != null) {
            if (!StringUtils.hasText(vo.getPatientName())) {
                vo.setPatientName(patient.getPatientName());
            }
            if (vo.getGender() == null) {
                vo.setGender(patient.getGender());
            }
            if (vo.getAge() == null) {
                vo.setAge(patient.getAge());
            }
            if (!StringUtils.hasText(vo.getIdCard())) {
                vo.setIdCard(patient.getIdCard());
            }
            if (!StringUtils.hasText(vo.getMedicalInsuranceNo())) {
                vo.setMedicalInsuranceNo(patient.getMedicalInsuranceNo());
            }
            vo.setPhone(patient.getPhone());
            vo.setPatientType(patient.getPatientType());
            if (!StringUtils.hasText(vo.getInsuranceType())) {
                vo.setInsuranceType(patient.getMedicalInsuranceType());
            }
        }

        // 2. 挂号（门诊）/入院（住院）：科室、医生、就诊日期
        RegistBriefVO regist = settlement.getRegistId() == null ? null
                : appointGateway.findRegist(settlement.getRegistId());
        if (regist != null) {
            vo.setRegistNo(regist.getRegistNo());
            if (vo.getVisitDate() == null) {
                vo.setVisitDate(regist.getVisitDate());
            }
            if (!StringUtils.hasText(vo.getDeptName())) {
                vo.setDeptName(regist.getDeptName());
            }
            if (!StringUtils.hasText(vo.getDoctorName())) {
                vo.setDoctorName(regist.getDoctorName());
            }
            if (!StringUtils.hasText(vo.getMedicalInsuranceNo())) {
                vo.setMedicalInsuranceNo(regist.getMedicalInsuranceNo());
            }
            if (!StringUtils.hasText(vo.getInsuranceType())) {
                vo.setInsuranceType(regist.getMedicalInsuranceType());
            }
            if (!StringUtils.hasText(vo.getVisitType())) {
                vo.setVisitType(visitTypeText(regist.getVisitType()));
            }
        } else if (EncounterTypeEnum.INPATIENT.getCode().equals(settlement.getEncounterType())
                && settlement.getEncounterId() != null) {
            AdmissionBriefVO admission = patientGateway.findAdmission(settlement.getEncounterId());
            if (admission != null) {
                vo.setAdmissionNo(admission.getAdmissionNo());
                vo.setVisitDate(admission.getAdmitTime() == null ? null : admission.getAdmitTime().toLocalDate());
                if (!StringUtils.hasText(vo.getDiagnosis())) {
                    vo.setDiagnosis(admission.getDiagnosis());
                }
            }
        }

        // 3. 诊断：清单上没有就取该患者最近一次病历（合规审核找的是同一份诊断依据）
        if (!StringUtils.hasText(vo.getDiagnosisName()) || !StringUtils.hasText(vo.getDiagnosis())) {
            MedicalRecordBriefVO record = settlement.getPatientId() == null ? null
                    : latestMedicalRecord(settlement.getPatientId());
            if (record != null) {
                if (!StringUtils.hasText(vo.getDiagnosis())) {
                    vo.setDiagnosis(record.getDiagnosis());
                }
                if (!StringUtils.hasText(vo.getDiagnosisCode())) {
                    vo.setDiagnosisCode(record.getDiagnosisCode());
                }
                if (!StringUtils.hasText(vo.getDiagnosisName())) {
                    vo.setDiagnosisName(record.getDiagnosisName());
                }
            }
        }

        // 4. 关联账单与账单行：应收/优惠/统筹/应缴/已收各来自哪一列，页面上要能一句话对上
        BizSettlementBill bill = settlement.getBillId() == null ? null
                : bizSettlementBillMapper.selectById(settlement.getBillId());
        if (bill != null) {
            vo.setBillId(bill.getId());
            vo.setBillNo(bill.getBillNo());
            vo.setBillStatus(bill.getBillStatus());
            vo.setBillTotalAmount(NumUtil.orZero(bill.getTotalAmount()));
            vo.setDiscountAmount(NumUtil.orZero(bill.getDiscountAmount()));
            vo.setPayableAmount(NumUtil.orZero(bill.getPayableAmount()));
            vo.setPaidAmount(NumUtil.orZero(bill.getPaidAmount()));
            vo.setRefundAmount(NumUtil.orZero(bill.getRefundAmount()));
            vo.setBillDate(bill.getBillDate());
            vo.setPayTime(bill.getPayTime());
            vo.setBillByName(bill.getBillByName());
            vo.setInvoiceNo(latestInvoiceNo(bill.getId()));
            List<BizSettlementBillItem> items = bizSettlementBillItemMapper.selectByBill(bill.getId());
            vo.setItems(items);
        }
        // 5. 脱敏：清单详情只用于看，没有编辑回显，所以明文一律不出响应体
        //    （2304 报文另有取材路径：buildUploadPayload 直接读实体，不受这里影响）
        vo.setIdCardMasked(SensitiveMaskUtil.maskIdCard(vo.getIdCard()));
        vo.setIdCard(null);
        vo.setMedicalInsuranceNoMasked(SensitiveMaskUtil.maskCardNo(vo.getMedicalInsuranceNo()));
        vo.setMedicalInsuranceNo(null);
        vo.setPhoneMasked(SensitiveMaskUtil.maskPhone(vo.getPhone()));
        vo.setPhone(null);
        return vo;
    }

    @Override
    public BizInsuranceSettlementVO toListVO(BizInsuranceSettlement entity) {
        if (entity == null) {
            return null;
        }
        BizInsuranceSettlementVO vo = new BizInsuranceSettlementVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setIdCardMasked(SensitiveMaskUtil.maskIdCard(entity.getIdCard()));
        vo.setIdCard(null);
        vo.setMedicalInsuranceNoMasked(SensitiveMaskUtil.maskCardNo(entity.getMedicalInsuranceNo()));
        vo.setMedicalInsuranceNo(null);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizInsuranceSettlement generateFromBill(BizSettlementBill bill, List<BizSettlementBillItem> items,
                                                   BigDecimal coverageRatio) {
        if (bill == null || bill.getId() == null) {
            throw new BusinessException("缺少结算账单，无法生成医保清单");
        }
        BigDecimal pool = NumUtil.scale(NumUtil.orZero(bill.getPoolAmount()), AMOUNT_SCALE);
        if (pool.signum() <= 0) {
            // 没有统筹记账就没有要报给医保局的钱：出一张 0 元清单等于让医保替你归档自费单据
            return null;
        }
        BizInsuranceSettlement existing = findByBill(bill.getId());
        BizInsuranceSettlement settlement = existing == null ? new BizInsuranceSettlement() : existing;

        settlement.setBillId(bill.getId());
        settlement.setBillNo(bill.getBillNo());
        settlement.setEncounterType(bill.getEncounterType());
        settlement.setEncounterId(bill.getEncounterId());
        settlement.setPatientId(bill.getPatientId());
        settlement.setPatientNo(bill.getPatientNo());
        settlement.setPatientName(bill.getPatientName());
        // 门诊的就诊标识就是挂号ID：科室/医生/费别从挂号抄，住院留给账单行上的归属科室
        Long registId = EncounterTypeEnum.OUTPATIENT.getCode().equals(bill.getEncounterType())
                ? bill.getEncounterId() : null;
        settlement.setRegistId(registId);
        RegistBriefVO regist = registId == null ? null : appointGateway.findRegist(registId);
        settlement.setDeptId(regist != null ? regist.getDeptId() : firstDeptId(items));
        settlement.setDeptName(regist != null ? regist.getDeptName() : firstDeptName(items));
        settlement.setDoctorId(regist == null ? null : regist.getDoctorId());
        settlement.setDoctorName(regist == null ? null : regist.getDoctorName());
        settlement.setVisitType(regist == null ? null : visitTypeText(regist.getVisitType()));
        settlement.setSettlementType(bill.getSettlementMode());
        settlement.setInsuranceType(bill.getInsuranceType());
        settlement.setCoverageRatio(coverageRatio);
        fillPatientIdentity(settlement, bill.getPatientId() == null ? null : patientGateway.findPatient(bill.getPatientId()));
        fillDiagnosis(settlement);

        BigDecimal net = NumUtil.scale(NumUtil.orZero(bill.getTotalAmount()).subtract(NumUtil.orZero(bill.getDiscountAmount())), AMOUNT_SCALE);
        settlement.setTotalAmount(net);
        fillCategoryAmounts(settlement, items);
        // 三个数里只有统筹此刻已成事实（L2 逐行 split 的结果）；个账与自付是「钱怎么进来的」，
        // 要等 L3 收齐后由 settle() 回读，这里一律置 0，避免把预估数当成结算数报上去。
        settlement.setInsurancePay(pool);
        settlement.setPersonalPay(BigDecimal.ZERO);
        settlement.setSelfPay(BigDecimal.ZERO);

        if (existing == null) {
            settlement.setSettlementNo(redisSequenceService.generateInsuranceSettlementNo());
            settlement.setSettlementStatus(InsuranceSettlementStatusEnum.PENDING.getCode());
            settlement.setAuditStatus(0);
            this.save(settlement);
            log.info("[医保清单] 账单 {} 出清单 {}：费用 ¥{} 统筹 ¥{}", bill.getBillNo(),
                    settlement.getSettlementNo(), net.toPlainString(), pool.toPlainString());
        } else {
            this.updateById(settlement);
            log.info("[医保清单] 账单 {} 的清单 {} 按最新账单刷新", bill.getBillNo(), settlement.getSettlementNo());
        }
        return settlement;
    }

    @Override
    public BizInsuranceSettlement findReportedOfBill(Long billId) {
        if (billId == null) {
            return null;
        }
        List<BizInsuranceSettlement> hits = this.list(new LambdaQueryWrapper<BizInsuranceSettlement>()
                .eq(BizInsuranceSettlement::getBillId, billId)
                .eq(BizInsuranceSettlement::getDelFlag, 0)
                .in(BizInsuranceSettlement::getSettlementStatus, reportedStatuses())
                .orderByDesc(BizInsuranceSettlement::getId));
        return CollectionUtils.isEmpty(hits) ? null : hits.get(0);
    }

    @Override
    public void assertBillRefundable(Long billId, boolean wholeBill) {
        if (wholeBill) {
            return;
        }
        BizInsuranceSettlement reported = findReportedOfBill(billId);
        if (reported != null) {
            throw new BusinessException("该账单的医保结算清单「" + reported.getSettlementNo()
                    + "」已报盘上传，医保侧只有整单冲正（2305）、不能部分退费；"
                    + "请整单退费，或先在「医保结算」页撤销报盘后重新发起");
        }
    }

    // 结算与报盘

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidByBill(Long billId, String reason) {
        BizInsuranceSettlement settlement = findByBill(billId);
        if (settlement == null || InsuranceSettlementStatusEnum.VOIDED.getCode()
                .equals(settlement.getSettlementStatus())) {
            return;
        }
        if (isReported(settlement)) {
            // 撤销是外发：医保拒了就让调用方整笔回滚，绝不留下「账单没了、医保还记着这笔费用」
            sendCancel(settlement, "账单 " + settlement.getBillNo() + " 作废联动撤销：" + reason);
        }
        settlement.setSettlementStatus(InsuranceSettlementStatusEnum.VOIDED.getCode());
        this.updateById(settlement);
        log.info("[医保清单] 清单 {} 随账单 {} 作废，原因：{}", settlement.getSettlementNo(),
                settlement.getBillNo(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetByBill(Long billId, String reason) {
        BizInsuranceSettlement settlement = findByBill(billId);
        if (settlement == null
                || InsuranceSettlementStatusEnum.VOIDED.getCode().equals(settlement.getSettlementStatus())
                || isReported(settlement)) {
            // 已报盘的走不到这里（assertBillRefundable 已经把部分退回绝了）；
            // 万一状态在两步之间被人改动了，也只是不重置，不会把已报盘的清单悄悄抹掉
            return;
        }
        if (InsuranceSettlementStatusEnum.PENDING.getCode().equals(settlement.getSettlementStatus())) {
            return;
        }
        settlement.setSettlementStatus(InsuranceSettlementStatusEnum.PENDING.getCode());
        settlement.setPersonalPay(BigDecimal.ZERO);
        settlement.setSelfPay(BigDecimal.ZERO);
        settlement.setAuditStatus(0);
        settlement.setAuditRemark(TextUtil.cut("账单发生部分退费，医保结算作废重做：" + reason, 500));
        this.updateById(settlement);
        // updateById 跳过 null 字段：审核时间要显式清空，否则待结算的清单上还挂着一次审核记录
        this.update(new LambdaUpdateWrapper<BizInsuranceSettlement>()
                .eq(BizInsuranceSettlement::getId, settlement.getId())
                .set(BizInsuranceSettlement::getAuditTime, null));
        log.info("[医保清单] 清单 {} 因账单 {} 部分退费退回待结算", settlement.getSettlementNo(), settlement.getBillNo());
    }

    @Override
    public PreSettlementVO preSettlement(Long settlementId) {
        BizInsuranceSettlement settlement = requireSettlement(settlementId);
        FundSplit fund = computeFund(settlement);

        PreSettlementVO result = new PreSettlementVO();
        result.setSettlementId(settlementId);
        result.setTotalAmount(fund.total());
        result.setInsurancePay(fund.pool());
        // 预结算口径的「自付」= 个人应缴合计（个账 + 现金类），与医保局 2304 试算的字段含义一致
        result.setSelfPay(NumUtil.scale(fund.account().add(fund.cash()), AMOUNT_SCALE));
        result.setCoverageRatio(settlement.getCoverageRatio());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SettlementResultVO settle(Long settlementId) {
        BizInsuranceSettlement settlement = requireSettlement(settlementId);
        if (!InsuranceSettlementStatusEnum.PENDING.getCode().equals(settlement.getSettlementStatus())) {
            throw new BusinessException("当前状态是「"
                    + InsuranceSettlementStatusEnum.descOf(settlement.getSettlementStatus()) + "」，不允许重复结算");
        }
        FundSplit fund = computeFund(settlement);
        // 费用分类每次结算重算：账单行才是分类的事实来源，清单上的数只是快照
        List<BizSettlementBillItem> items = bizSettlementBillItemMapper.selectByBill(settlement.getBillId());
        fillCategoryAmounts(settlement, items);

        settlement.setTotalAmount(fund.total());
        settlement.setInsurancePay(fund.pool());
        settlement.setPersonalPay(fund.account());
        settlement.setSelfPay(fund.cash());
        settlement.setSettlementStatus(InsuranceSettlementStatusEnum.SETTLED.getCode());
        this.updateById(settlement);

        SettlementResultVO result = new SettlementResultVO();
        result.setSettlementId(settlementId);
        result.setTotalAmount(fund.total());
        result.setInsurancePay(fund.pool());
        result.setPersonalPay(fund.account());
        result.setSelfPay(fund.cash());
        log.info("[医保结算] 清单 {} 结算完成：费用 ¥{} = 统筹 ¥{} + 个账 ¥{} + 自付 ¥{}",
                settlement.getSettlementNo(), fund.total().toPlainString(), fund.pool().toPlainString(),
                fund.account().toPlainString(), fund.cash().toPlainString());
        return result;
    }

    /**
     * G7 报盘上传：清单 2-已结算 → 组 2304 报文 → 台账落一条待发记录 →
     * {@link InsuranceChannelService#send} → 回执写回台账 → 回执成功才推到 3-已上传。
     *
     * <p>刻意不加 {@code @Transactional}：失败的报文记录必须留痕（对账与追溯依据），
     * 不能被业务异常回滚掉。
     */
    @Override
    public boolean uploadSettlement(Long settlementId) {
        BizInsuranceSettlement settlement = requireSettlement(settlementId);
        if (!InsuranceSettlementStatusEnum.SETTLED.getCode().equals(settlement.getSettlementStatus())) {
            throw new BusinessException("当前状态是「"
                    + InsuranceSettlementStatusEnum.descOf(settlement.getSettlementStatus()) + "」，只有已结算的清单才能报盘");
        }
        BizSettlementBill bill = bizSettlementBillMapper.selectById(settlement.getBillId());
        if (bill == null) {
            throw new BusinessException("清单关联的结算账单不存在，无法报盘");
        }
        List<BizSettlementBillItem> items = bizSettlementBillItemMapper.selectByBill(bill.getId());

        String tradeNo = nextTradeNo();
        BizInsuranceReport report = newReport(settlement, InsuranceReportTypeEnum.UPLOAD.getCode(), "2304", tradeNo);
        report.setPayload(buildUploadPayload(settlement, bill, items, tradeNo));
        report.setTotalAmount(settlement.getTotalAmount());
        report.setInsurancePay(settlement.getInsurancePay());
        report.setPersonalPay(settlement.getPersonalPay());
        report.setSelfPay(settlement.getSelfPay());
        bizInsuranceReportMapper.insert(report);

        InsuranceChannelService.Receipt receipt = insuranceChannelService.send(
                new InsuranceChannelService.OutboundMessage("2304", tradeNo, report.getPayload()));
        applyReceipt(report, receipt);
        if (!receipt.isSuccess()) {
            throw new BusinessException("医保报盘被拒：" + TextUtil.cut(receipt.getErrMsg(), 200));
        }
        settlement.setSettlementStatus(InsuranceSettlementStatusEnum.UPLOADED.getCode());
        settlement.setUploadTime(LocalDateTime.now());
        return this.updateById(settlement);
    }

    // 报文台账与日对账

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelUpload(Long settlementId, String reason) {
        BizInsuranceSettlement settlement = requireSettlement(settlementId);
        if (!isReported(settlement)) {
            throw new BusinessException("只有已上传/已审核的清单才能撤销");
        }
        sendCancel(settlement, reason);
        settlement.setSettlementStatus(InsuranceSettlementStatusEnum.SETTLED.getCode());
        this.updateById(settlement);
        // updateById 跳过 null 字段：上传时间必须显式清空，否则清单上永远挂着一次已经不成立的上传
        return this.update(new LambdaUpdateWrapper<BizInsuranceSettlement>()
                .eq(BizInsuranceSettlement::getId, settlement.getId())
                .set(BizInsuranceSettlement::getUploadTime, null));
    }

    /**
     * 发 2305 撤销：找本次清单最近一张回执成功的上传报文，撤销它、并把它标成「已被撤销」。
     *
     * <p>供收费员点「撤销报盘」与账单作废联动两处共用 —— 后者没有第二个入口可点。
     */
    private void sendCancel(BizInsuranceSettlement settlement, String reason) {
        List<BizInsuranceReport> uploads = bizInsuranceReportMapper.selectList(new LambdaQueryWrapper<BizInsuranceReport>()
                .eq(BizInsuranceReport::getSettlementId, settlement.getId())
                .eq(BizInsuranceReport::getReportType, InsuranceReportTypeEnum.UPLOAD.getCode())
                .eq(BizInsuranceReport::getStatus, InsuranceReportStatusEnum.SUCCESS.getCode())
                .orderByDesc(BizInsuranceReport::getId));
        if (CollectionUtils.isEmpty(uploads)) {
            throw new BusinessException("未找到回执成功的上传报文，无法撤销");
        }
        BizInsuranceReport original = uploads.get(0);
        String cutReason = StringUtils.hasText(reason) ? reason : "收费员冲正";

        String tradeNo = nextTradeNo();
        InsuranceCancelPayloadVO payload = new InsuranceCancelPayloadVO();
        payload.setMsgType("2305");
        payload.setTradeNo(tradeNo);
        payload.setOrigTradeNo(original.getTradeNo());
        payload.setSettlementNo(settlement.getSettlementNo());
        payload.setBillNo(settlement.getBillNo());
        payload.setPatientName(settlement.getPatientName());
        payload.setCancelReason(cutReason);
        payload.setSendTime(LocalDateTime.now().format(DateFormats.DATETIME));
        payload.setNote("撤销报文：正式环境按医保前置机 2305 规范做字段映射");

        BizInsuranceReport cancel = newReport(settlement, InsuranceReportTypeEnum.CANCEL.getCode(), "2305", tradeNo);
        cancel.setOrigTradeNo(original.getTradeNo());
        cancel.setPayload(toPrettyJson(payload));
        cancel.setRemark(TextUtil.cut(cutReason, 490));
        cancel.setTotalAmount(settlement.getTotalAmount());
        cancel.setInsurancePay(settlement.getInsurancePay());
        cancel.setPersonalPay(settlement.getPersonalPay());
        cancel.setSelfPay(settlement.getSelfPay());
        bizInsuranceReportMapper.insert(cancel);

        InsuranceChannelService.Receipt receipt = insuranceChannelService.send(
                new InsuranceChannelService.OutboundMessage("2305", tradeNo, cancel.getPayload()));
        applyReceipt(cancel, receipt);
        if (!receipt.isSuccess()) {
            throw new BusinessException("医保撤销被拒：" + TextUtil.cut(receipt.getErrMsg(), 200));
        }
        original.setStatus(InsuranceReportStatusEnum.CANCELLED.getCode());
        bizInsuranceReportMapper.updateById(original);
    }

    @Override
    public PageResult<BizInsuranceReportVO> selectReportPage(Long settlementId, Integer reportType, Integer status,
                                                             int pageNum, int pageSize) {
        LambdaQueryWrapper<BizInsuranceReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(settlementId != null, BizInsuranceReport::getSettlementId, settlementId)
                .eq(reportType != null, BizInsuranceReport::getReportType, reportType)
                .eq(status != null, BizInsuranceReport::getStatus, status)
                .select(BizInsuranceReport.class, fi ->
                        !"payload".equals(fi.getProperty()) && !"replyPayload".equals(fi.getProperty()))
                .orderByDesc(BizInsuranceReport::getId);
        Page<BizInsuranceReport> page = bizInsuranceReportMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<BizInsuranceReportVO> records = new ArrayList<>(page.getRecords().size());
        for (BizInsuranceReport row : page.getRecords()) {
            BizInsuranceReportVO vo = new BizInsuranceReportVO();
            BeanUtils.copyProperties(row, vo);
            records.add(vo);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public BizInsuranceReportVO getReportDetail(Long reportId) {
        BizInsuranceReport report = bizInsuranceReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报文记录不存在");
        }
        BizInsuranceReportVO vo = new BizInsuranceReportVO();
        BeanUtils.copyProperties(report, vo);
        return vo;
    }

    // 内部：账单与流水口径

    @Override
    public ReconcileResultVO reconcile(LocalDate billDate) {
        // 本地口径：当日上传成功（3-已上传 / 4-已审核）的结算清单
        List<BizInsuranceSettlement> locals = this.list(new LambdaQueryWrapper<BizInsuranceSettlement>()
                .apply("DATE(upload_time) = {0}", billDate.toString())
                .in(BizInsuranceSettlement::getSettlementStatus, reportedStatuses()));
        // 医保侧口径：走网关拉当日账单（Mock 实现取报文台账中回执成功且未被撤销的上传记录）
        List<InsuranceChannelService.RemoteSettlement> remotes = insuranceChannelService.queryDayBill(billDate);

        ReconcileResultVO vo = new ReconcileResultVO();
        vo.setBillDate(billDate.toString());
        vo.setLocalCount(locals.size());
        vo.setLocalTotal(sum(locals, BizInsuranceSettlement::getTotalAmount));
        vo.setLocalInsurancePay(sum(locals, BizInsuranceSettlement::getInsurancePay));
        vo.setRemoteCount(remotes.size());
        vo.setRemoteTotal(sum(remotes, InsuranceChannelService.RemoteSettlement::getTotalAmount));
        vo.setRemoteInsurancePay(sum(remotes, InsuranceChannelService.RemoteSettlement::getInsurancePay));

        List<BizInsuranceReport> dayReports = bizInsuranceReportMapper.selectList(new LambdaQueryWrapper<BizInsuranceReport>()
                .eq(BizInsuranceReport::getBillDate, billDate)
                .select(BizInsuranceReport.class, fi ->
                        !"payload".equals(fi.getProperty()) && !"replyPayload".equals(fi.getProperty())));
        vo.setUploadSuccess((int) dayReports.stream().filter(r -> InsuranceReportTypeEnum.UPLOAD.getCode().equals(r.getReportType()) && InsuranceReportStatusEnum.SUCCESS.getCode().equals(r.getStatus())).count());
        vo.setUploadFail((int) dayReports.stream().filter(r -> InsuranceReportTypeEnum.UPLOAD.getCode().equals(r.getReportType()) && InsuranceReportStatusEnum.FAIL.getCode().equals(r.getStatus())).count());
        vo.setUploadCancelled((int) dayReports.stream().filter(r -> InsuranceReportTypeEnum.UPLOAD.getCode().equals(r.getReportType()) && InsuranceReportStatusEnum.CANCELLED.getCode().equals(r.getStatus())).count());
        vo.setCancelSent((int) dayReports.stream().filter(r -> InsuranceReportTypeEnum.CANCEL.getCode().equals(r.getReportType()) && InsuranceReportStatusEnum.SUCCESS.getCode().equals(r.getStatus())).count());

        Map<String, InsuranceChannelService.RemoteSettlement> remoteByNo = remotes.stream()
                .collect(Collectors.toMap(InsuranceChannelService.RemoteSettlement::getSettlementNo, Function.identity(), (a, b) -> a));
        List<ReconcileResultVO.Diff> diffs = new ArrayList<>();
        for (BizInsuranceSettlement local : locals) {
            InsuranceChannelService.RemoteSettlement remote = remoteByNo.remove(local.getSettlementNo());
            if (remote == null) {
                diffs.add(new ReconcileResultVO.Diff(local.getSettlementNo(), null,
                        "本地已上传但医保侧无回执（可能已撤销或丢单）", local.getTotalAmount(), null));
            } else if (remote.getTotalAmount() == null || local.getTotalAmount() == null
                    || remote.getTotalAmount().compareTo(local.getTotalAmount()) != 0) {
                diffs.add(new ReconcileResultVO.Diff(local.getSettlementNo(), remote.getTradeNo(),
                        "金额不符", local.getTotalAmount(), remote.getTotalAmount()));
            }
        }
        remoteByNo.values().forEach(remote -> diffs.add(new ReconcileResultVO.Diff(remote.getSettlementNo(),
                remote.getTradeNo(), "医保侧多出差笔（本地清单不存在或已回滚）", null, remote.getTotalAmount())));
        vo.setDiffs(diffs);
        vo.setMatched(diffs.isEmpty() && vo.getLocalCount().equals(vo.getRemoteCount()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditSettlement(Long settlementId, boolean approved, String remark) {
        BizInsuranceSettlement settlement = requireSettlement(settlementId);
        if (!InsuranceSettlementStatusEnum.UPLOADED.getCode().equals(settlement.getSettlementStatus())) {
            throw new BusinessException("当前状态是「"
                    + InsuranceSettlementStatusEnum.descOf(settlement.getSettlementStatus()) + "」，只有已上传的清单才能审核");
        }
        settlement.setAuditStatus(approved ? 1 : 2);
        settlement.setAuditTime(LocalDateTime.now());
        settlement.setAuditRemark(TextUtil.cut(remark, 500));
        if (approved) {
            settlement.setSettlementStatus(InsuranceSettlementStatusEnum.AUDITED.getCode());
        }
        return this.updateById(settlement);
    }

    /**
     * 清单的三个数现算：统筹来自账单，个账与自付来自账单名下的成功收款流水。
     *
     * <p>门禁是「账单已付清」：只有收齐时费用 = 统筹 + 个账 + 自付才成立，
     * 没收清就来结算，报给医保的自付会比患者实际掏的钱少，日结必然差一块。
     */
    private FundSplit computeFund(BizInsuranceSettlement settlement) {
        BizSettlementBill bill = bizSettlementBillMapper.selectById(settlement.getBillId());
        if (bill == null) {
            throw new BusinessException("清单关联的结算账单不存在");
        }
        BillStatusEnum billStatus = BillStatusEnum.fromCode(bill.getBillStatus());
        if (billStatus == BillStatusEnum.VOIDED) {
            throw new BusinessException("结算账单 " + bill.getBillNo() + " 已作废，清单不能结算");
        }
        BigDecimal payable = NumUtil.scale(NumUtil.orZero(bill.getPayableAmount()), AMOUNT_SCALE);
        BigDecimal account = BigDecimal.ZERO;
        BigDecimal cash = BigDecimal.ZERO;
        for (BizPaymentTxn txn : bizPaymentTxnMapper.selectByBill(bill.getId())) {
            if (!PayTxnStatusEnum.SUCCESS.getCode().equals(txn.getTxnStatus())) {
                continue;
            }
            // 流水带符号（收款正、退款负），直接累加即为净实付
            if (PaymentMethodEnum.INSURANCE_ACCOUNT.getCode().equals(txn.getPayMethod())) {
                account = account.add(NumUtil.orZero(txn.getAmount()));
            } else {
                cash = cash.add(NumUtil.orZero(txn.getAmount()));
            }
        }
        account = NumUtil.scale(account, AMOUNT_SCALE);
        cash = NumUtil.scale(cash, AMOUNT_SCALE);
        BigDecimal paid = NumUtil.scale(account.add(cash), AMOUNT_SCALE);
        if (paid.compareTo(payable) < 0) {
            throw new BusinessException("结算账单 " + bill.getBillNo() + " 尚未收清（已收 ¥"
                    + paid.toPlainString() + " / 应缴 ¥" + payable.toPlainString() + "），医保结算要等钱收齐后再做");
        }
        return new FundSplit(NumUtil.scale(NumUtil.orZero(bill.getTotalAmount()).subtract(NumUtil.orZero(bill.getDiscountAmount())), AMOUNT_SCALE),
                NumUtil.scale(NumUtil.orZero(bill.getPoolAmount()), AMOUNT_SCALE), account, cash);
    }

    /**
     * 按账单行汇总费用分类。
     *
     * <p>取「行金额 - 行优惠」而不是行金额：分类合计必须等于清单总费用，
     * 否则医保前置机的 2304 校验第一步就过不去（总费用与明细对不上）。
     */
    private void fillCategoryAmounts(BizInsuranceSettlement settlement, List<BizSettlementBillItem> items) {
        Map<PaymentItemTypeEnum, BigDecimal> buckets = new EnumMap<>(PaymentItemTypeEnum.class);
        for (BizSettlementBillItem item : items == null ? List.<BizSettlementBillItem>of() : items) {
            PaymentItemTypeEnum type = PaymentItemTypeEnum.getByCode(item.getItemType());
            if (type == null) {
                type = PaymentItemTypeEnum.TREATMENT;
            }
            BigDecimal net = NumUtil.scale(NumUtil.orZero(item.getAmount()).subtract(NumUtil.orZero(item.getDiscountAmount())), AMOUNT_SCALE);
            buckets.merge(type, net, BigDecimal::add);
        }
        settlement.setDrugAmount(NumUtil.scale(bucket(buckets, PaymentItemTypeEnum.WESTERN_MEDICINE,
                PaymentItemTypeEnum.CHINESE_PATENT_MEDICINE, PaymentItemTypeEnum.CHINESE_HERBAL_MEDICINE), AMOUNT_SCALE));
        settlement.setInspectionAmount(NumUtil.scale(bucket(buckets, PaymentItemTypeEnum.EXAMINATION), AMOUNT_SCALE));
        settlement.setLaboratoryAmount(NumUtil.scale(bucket(buckets, PaymentItemTypeEnum.LABORATORY_TEST), AMOUNT_SCALE));
        settlement.setTreatmentAmount(NumUtil.scale(bucket(buckets, PaymentItemTypeEnum.TREATMENT), AMOUNT_SCALE));
        settlement.setMaterialAmount(NumUtil.scale(bucket(buckets, PaymentItemTypeEnum.CONSUMABLE), AMOUNT_SCALE));
        settlement.setOtherAmount(NumUtil.scale(bucket(buckets, PaymentItemTypeEnum.REGISTRATION_FEE), AMOUNT_SCALE));
    }

    private BigDecimal bucket(Map<PaymentItemTypeEnum, BigDecimal> buckets, PaymentItemTypeEnum... types) {
        BigDecimal total = BigDecimal.ZERO;
        for (PaymentItemTypeEnum type : types) {
            total = total.add(buckets.getOrDefault(type, BigDecimal.ZERO));
        }
        return total;
    }

    /**
     * 参保人身份在出账时快照进清单。
     *
     * <p>2304 报文直接读实体（{@code buildUploadPayload}），医保局靠身份证号匹配参保人 ——
     * 只在详情接口里从档案回补，报出去的报文就是一张没有人的清单。
     */
    private void fillPatientIdentity(BizInsuranceSettlement settlement, PatientBriefVO patient) {
        if (patient == null) {
            return;
        }
        settlement.setIdCard(patient.getIdCard());
        settlement.setMedicalInsuranceNo(patient.getMedicalInsuranceNo());
        if (settlement.getGender() == null) {
            settlement.setGender(patient.getGender());
        }
        if (settlement.getAge() == null) {
            settlement.setAge(patient.getAge());
        }
        if (!StringUtils.hasText(settlement.getInsuranceType())) {
            settlement.setInsuranceType(patient.getMedicalInsuranceType());
        }
    }

    private void fillDiagnosis(BizInsuranceSettlement settlement) {
        if (settlement.getRegistId() == null) {
            return;
        }
        MedicalRecordBriefVO record = emrGateway.findLatestMedicalRecordByRegist(settlement.getRegistId());
        if (record == null) {
            return;
        }
        settlement.setGender(record.getGender());
        settlement.setAge(record.getAge());
        settlement.setDiagnosis(record.getDiagnosis());
        settlement.setDiagnosisCode(record.getDiagnosisCode());
        settlement.setDiagnosisName(record.getDiagnosisName());
    }

    /**
     * 组装 2304 上传报文：账单头 + 逐行 split（医保要按项目核费用，整单一个数报不出去）
     */
    private String buildUploadPayload(BizInsuranceSettlement s, BizSettlementBill bill,
                                      List<BizSettlementBillItem> items, String tradeNo) {
        InsuranceUploadPayloadVO.Patient patient = new InsuranceUploadPayloadVO.Patient();
        patient.setPatientNo(s.getPatientNo());
        patient.setPatientName(s.getPatientName());
        patient.setGender(s.getGender());
        patient.setAge(s.getAge());
        patient.setIdCard(s.getIdCard());
        patient.setInsuranceNo(s.getMedicalInsuranceNo());
        patient.setInsuranceType(s.getInsuranceType());

        InsuranceUploadPayloadVO.Visit visit = new InsuranceUploadPayloadVO.Visit();
        visit.setBillNo(bill.getBillNo());
        visit.setEncounterType(EncounterTypeEnum.descOf(bill.getEncounterType()));
        visit.setVisitType(s.getVisitType());
        visit.setDeptName(s.getDeptName());
        visit.setDoctorName(s.getDoctorName());
        visit.setDiagnosisCode(s.getDiagnosisCode());
        visit.setDiagnosisName(StringUtils.hasText(s.getDiagnosisName()) ? s.getDiagnosisName() : s.getDiagnosis());

        InsuranceUploadPayloadVO.Fees fees = new InsuranceUploadPayloadVO.Fees();
        fees.setTotal(s.getTotalAmount());
        fees.setDrug(s.getDrugAmount());
        fees.setInspection(s.getInspectionAmount());
        fees.setLaboratory(s.getLaboratoryAmount());
        fees.setTreatment(s.getTreatmentAmount());
        fees.setMaterial(s.getMaterialAmount());
        fees.setOther(s.getOtherAmount());

        InsuranceUploadPayloadVO.Fund fund = new InsuranceUploadPayloadVO.Fund();
        fund.setCoverageRatio(s.getCoverageRatio());
        fund.setInsurancePay(s.getInsurancePay());
        fund.setPersonalPay(s.getPersonalPay());
        fund.setSelfPay(s.getSelfPay());

        List<InsuranceUploadPayloadVO.Item> lines = new ArrayList<>(items.size());
        for (BizSettlementBillItem item : items) {
            InsuranceUploadPayloadVO.Item line = new InsuranceUploadPayloadVO.Item();
            line.setItemType(item.getItemType());
            line.setItemTypeName(itemTypeText(item.getItemType()));
            line.setItemCode(item.getItemCode());
            line.setItemName(item.getItemName());
            line.setSpecification(item.getSpecification());
            line.setUnit(item.getUnit());
            line.setQuantity(item.getQuantity());
            line.setPrice(item.getPrice());
            line.setAmount(item.getAmount());
            line.setDiscount(item.getDiscountAmount());
            line.setCatalogType(item.getCatalogType());
            line.setPool(item.getPoolAmount());
            line.setSelf(item.getSelfAmount());
            lines.add(line);
        }

        InsuranceUploadPayloadVO payload = new InsuranceUploadPayloadVO();
        payload.setMsgType("2304");
        payload.setTradeNo(tradeNo);
        payload.setFixMedinsCode("H4301000001");
        payload.setFixMedinsName("长沙市麓康医院");
        payload.setSettlementNo(s.getSettlementNo());
        payload.setSendTime(LocalDateTime.now().format(DateFormats.DATETIME));
        payload.setPatient(patient);
        payload.setVisit(visit);
        payload.setFees(fees);
        payload.setFund(fund);
        payload.setItems(lines);
        payload.setNote("G7 报盘样例报文：真实对接时按医保前置机规范做字段映射，替换 InsuranceChannelService 即可");
        return toPrettyJson(payload);
    }

    // 内部：杂项

    private BizInsuranceReport newReport(BizInsuranceSettlement settlement, int reportType, String msgType,
                                         String tradeNo) {
        BizInsuranceReport report = new BizInsuranceReport();
        report.setSettlementId(settlement.getId());
        report.setSettlementNo(settlement.getSettlementNo());
        report.setReportType(reportType);
        report.setMsgType(msgType);
        report.setTradeNo(tradeNo);
        report.setStatus(InsuranceReportStatusEnum.INIT.getCode());
        report.setSendTime(LocalDateTime.now());
        report.setBillDate(LocalDate.now());
        return report;
    }

    private void applyReceipt(BizInsuranceReport report, InsuranceChannelService.Receipt receipt) {
        report.setStatus(receipt.isSuccess() ? InsuranceReportStatusEnum.SUCCESS.getCode() : InsuranceReportStatusEnum.FAIL.getCode());
        report.setReceiptNo(receipt.getReceiptNo());
        report.setReplyPayload(receipt.getReplyPayload());
        report.setErrMsg(TextUtil.cut(receipt.getErrMsg(), 500));
        report.setReplyTime(LocalDateTime.now());
        bizInsuranceReportMapper.updateById(report);
    }

    private BizInsuranceSettlement requireSettlement(Long settlementId) {
        BizInsuranceSettlement settlement = settlementId == null ? null : this.getById(settlementId);
        if (settlement == null) {
            throw new BusinessException("结算清单不存在");
        }
        return settlement;
    }

    private BizInsuranceSettlement findByBill(Long billId) {
        if (billId == null) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<BizInsuranceSettlement>()
                .eq(BizInsuranceSettlement::getBillId, billId)
                .eq(BizInsuranceSettlement::getDelFlag, 0)
                .orderByDesc(BizInsuranceSettlement::getId), false);
    }

    private boolean isReported(BizInsuranceSettlement settlement) {
        InsuranceSettlementStatusEnum status = InsuranceSettlementStatusEnum.fromCode(settlement.getSettlementStatus());
        return status != null && status.uploaded();
    }

    private List<Integer> reportedStatuses() {
        return List.of(InsuranceSettlementStatusEnum.UPLOADED.getCode(), InsuranceSettlementStatusEnum.AUDITED.getCode());
    }

    private String latestInvoiceNo(Long billId) {
        List<BizInvoice> invoices = bizInvoiceMapper.selectList(new LambdaQueryWrapper<BizInvoice>()
                .eq(BizInvoice::getBillId, billId)
                .orderByDesc(BizInvoice::getId));
        return CollectionUtils.isEmpty(invoices) ? null : invoices.get(0).getInvoiceNo();
    }

    private MedicalRecordBriefVO latestMedicalRecord(Long patientId) {
        return emrGateway.findLatestMedicalRecordByPatient(patientId);
    }

    private Long firstDeptId(List<BizSettlementBillItem> items) {
        return CollectionUtils.isEmpty(items) ? null : items.get(0).getDeptId();
    }

    private String firstDeptName(List<BizSettlementBillItem> items) {
        return CollectionUtils.isEmpty(items) ? null : items.get(0).getDeptName();
    }

    private String visitTypeText(Integer visitType) {
        if (visitType == null) {
            return null;
        }
        return visitType == 1 ? "初诊" : visitType == 2 ? "复诊" : null;
    }

    private String itemTypeText(Integer itemType) {
        PaymentItemTypeEnum type = PaymentItemTypeEnum.getByCode(itemType);
        return type == null ? null : type.getDesc();
    }

    private String nextTradeNo() {
        return "HIS" + LocalDateTime.now().format(DateFormats.COMPACT_DATETIME_MS) + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private String toPrettyJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception e) {
            throw new BusinessException("报文序列化失败：" + e.getMessage());
        }
    }

    /**
     * 医保工作台统计（首屏四个数）。
     *
     * <p>「今日」按创建时间归集：清单是出账时生成的，报盘/审核是后续动作，
     * 按状态变更时间算会让同一张清单在两天的报表里各出现一次。
     */
    @Override
    public InsuranceStatsVO stats() {
        List<BizInsuranceSettlement> today = this.list(new LambdaQueryWrapper<BizInsuranceSettlement>()
                .ge(BizInsuranceSettlement::getCreateTime, LocalDate.now().atStartOfDay()));
        InsuranceStatsVO vo = new InsuranceStatsVO();
        vo.setTodayTotal(NumUtil.scale(sum(today, BizInsuranceSettlement::getTotalAmount), AMOUNT_SCALE));
        vo.setTodayInsurancePay(NumUtil.scale(sum(today, BizInsuranceSettlement::getInsurancePay), AMOUNT_SCALE));
        vo.setTodayCount(today.size());
        vo.setPendingCount(this.count(new LambdaQueryWrapper<BizInsuranceSettlement>()
                .eq(BizInsuranceSettlement::getSettlementStatus, InsuranceSettlementStatusEnum.PENDING.getCode())));
        vo.setSettledCount(this.count(new LambdaQueryWrapper<BizInsuranceSettlement>()
                .eq(BizInsuranceSettlement::getSettlementStatus, InsuranceSettlementStatusEnum.SETTLED.getCode())));
        return vo;
    }

    private <T> BigDecimal sum(List<T> list, Function<T, BigDecimal> getter) {
        return list.stream().map(getter).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 清单三个数 + 总费用的现算结果：{@code total = pool + account + cash}（仅在账单已付清时成立）
     */
    private record FundSplit(BigDecimal total, BigDecimal pool, BigDecimal account, BigDecimal cash) {
    }
}
