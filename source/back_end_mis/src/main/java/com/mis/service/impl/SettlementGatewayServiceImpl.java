package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageVO;
import com.mis.config.MisProperties;
import com.mis.dto.CancelSettlementDTO;
import com.mis.dto.UploadSettlementDTO;
import com.mis.engine.PolicyEngine;
import com.mis.entity.JinbInsuredPerson;
import com.mis.entity.JinbMessage;
import com.mis.entity.JinbSettlement;
import com.mis.mapper.JinbInsuredPersonMapper;
import com.mis.mapper.JinbMessageMapper;
import com.mis.mapper.JinbSettlementMapper;
import com.mis.service.SettlementGatewayService;
import com.mis.vo.DayBillItemVO;
import com.mis.vo.ReceiptVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SettlementGatewayServiceImpl implements SettlementGatewayService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JinbSettlementMapper settlementMapper;
    private final JinbMessageMapper messageMapper;
    private final JinbInsuredPersonMapper personMapper;
    private final PolicyEngine policyEngine;
    private final MisProperties properties;

    public SettlementGatewayServiceImpl(JinbSettlementMapper settlementMapper,
                                        JinbMessageMapper messageMapper,
                                        JinbInsuredPersonMapper personMapper,
                                        PolicyEngine policyEngine,
                                        MisProperties properties) {
        this.settlementMapper = settlementMapper;
        this.messageMapper = messageMapper;
        this.personMapper = personMapper;
        this.policyEngine = policyEngine;
        this.properties = properties;
    }

    @Override
    public ReceiptVO receive2304(UploadSettlementDTO dto, String rawPayload) {
        String replyTime = LocalDateTime.now().format(DT);
        String settlementNo = dto.getSettlementNo();
        BigDecimal total = dto.getFees() == null ? null : dto.getFees().getTotal();

        // 1) 报文合法性校验（与原 mock 前置机同口径）
        if (!StringUtils.hasText(settlementNo)) {
            return fail("2304 报文缺少结算清单号 settlementNo", dto, replyTime, rawPayload);
        }
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return fail("总费用必须大于 0，实际: " + (dto.getFees() == null ? "null" : dto.getFees().getTotal()),
                    dto, replyTime, rawPayload);
        }

        // 2) 幂等：同一 tradeNo 已成功接收过，直接回原回执
        JinbSettlement exist = byTradeNo(dto.getTradeNo());
        if (exist != null) {
            return successReceipt(dto, exist.getReceiptNo(), settlementNo, exist.getPoolConfirm(),
                    "重复上传，已按原回执幂等返回", replyTime);
        }

        // 3) 参保校验（非阻断告警）
        String warn = null;
        String idCard = dto.getPatient() == null ? null : dto.getPatient().getIdCard();
        String insuranceType = dto.getPatient() == null ? null : dto.getPatient().getInsuranceType();
        String encounterType = dto.getVisit() == null ? null : dto.getVisit().getEncounterType();
        if (StringUtils.hasText(idCard)) {
            JinbInsuredPerson person = personMapper.selectOne(new LambdaQueryWrapper<JinbInsuredPerson>()
                    .eq(JinbInsuredPerson::getIdCard, idCard).last("limit 1"));
            if (person == null) {
                warn = "参保人未在本统筹区登记，按报文中险种兜底接收";
            } else if (person.getPersonStatus() != null && person.getPersonStatus() == 0) {
                warn = "参保人当前为停保状态，已标记告警（演示不阻断）";
            }
        }

        // 4) 平台独立核定统筹
        BigDecimal poolConfirm = policyEngine.confirmPool(insuranceType, encounterType, total);

        // 5) 落平台侧结算单
        JinbSettlement s = new JinbSettlement();
        s.setTradeNo(dto.getTradeNo());
        s.setSettlementNo(settlementNo);
        s.setFixMedinsCode(StringUtils.hasText(dto.getFixMedinsCode())
                ? dto.getFixMedinsCode() : properties.getMedins().getCode());
        s.setBillNo(dto.getVisit() == null ? null : dto.getVisit().getBillNo());
        s.setEncounterType(encounterType);
        s.setPatientName(dto.getPatient() == null ? null : dto.getPatient().getPatientName());
        s.setIdCard(idCard);
        s.setInsuranceNo(dto.getPatient() == null ? null : dto.getPatient().getInsuranceNo());
        s.setInsuranceType(insuranceType);
        s.setTotalAmount(scale(total));
        s.setInsurancePay(dto.getFund() == null || dto.getFund().getInsurancePay() == null
                ? BigDecimal.ZERO : scale(dto.getFund().getInsurancePay()));
        s.setPersonalPay(dto.getFund() == null || dto.getFund().getPersonalPay() == null
                ? BigDecimal.ZERO : scale(dto.getFund().getPersonalPay()));
        s.setSelfPay(dto.getFund() == null || dto.getFund().getSelfPay() == null
                ? BigDecimal.ZERO : scale(dto.getFund().getSelfPay()));
        s.setPoolConfirm(poolConfirm);
        s.setStatus(1);
        s.setBillDate(LocalDate.now());
        s.setWarnMsg(warn);
        s.setRawPayload(rawPayload);
        settlementMapper.insert(s);

        String receiptNo = "YBZ" + LocalDate.now().format(DAY) + String.format("%06d", s.getId());
        s.setReceiptNo(receiptNo);
        settlementMapper.updateById(s);

        // 6) 报文台账
        saveMessage("2304", dto.getTradeNo(), null, 1, receiptNo, null, rawPayload);

        return successReceipt(dto, receiptNo, settlementNo, poolConfirm,
                "医保结算经办平台已接收结算信息（模拟医保局核心系统回执）" + (warn == null ? "" : "；告警：" + warn),
                replyTime);
    }

    @Override
    public ReceiptVO receive2305(CancelSettlementDTO dto, String rawPayload) {
        String replyTime = LocalDateTime.now().format(DT);
        if (!StringUtils.hasText(dto.getOrigTradeNo())) {
            return failCancel("撤销报文缺少被撤销单据 origTradeNo", dto, replyTime, rawPayload);
        }

        JinbSettlement original = settlementMapper.selectOne(new LambdaQueryWrapper<JinbSettlement>()
                .eq(JinbSettlement::getTradeNo, dto.getOrigTradeNo())
                .orderByDesc(JinbSettlement::getId).last("limit 1"));
        if (original == null) {
            return failCancel("原上传流水不存在：" + dto.getOrigTradeNo(), dto, replyTime, rawPayload);
        }

        // 已撤销：幂等成功
        if (original.getStatus() != null && original.getStatus() == 2) {
            return successReceipt2305(dto, original.getReceiptNo(), "原结算单已撤销，幂等返回", replyTime);
        }

        original.setStatus(2);
        original.setCancelTime(LocalDateTime.now());
        settlementMapper.updateById(original);

        saveMessage("2305", dto.getTradeNo(), dto.getOrigTradeNo(), 1, original.getReceiptNo(), null, rawPayload);
        return successReceipt2305(dto, original.getReceiptNo(), "医保结算经办平台已撤销该笔结算并冲正", replyTime);
    }

    @Override
    public List<DayBillItemVO> dayBill(LocalDate billDate) {
        List<JinbSettlement> list = settlementMapper.selectList(new LambdaQueryWrapper<JinbSettlement>()
                .eq(JinbSettlement::getBillDate, billDate)
                .eq(JinbSettlement::getStatus, 1)
                .orderByAsc(JinbSettlement::getId));
        List<DayBillItemVO> result = new ArrayList<>(list.size());
        for (JinbSettlement s : list) {
            DayBillItemVO item = new DayBillItemVO();
            item.setTradeNo(s.getTradeNo());
            item.setSettlementNo(s.getSettlementNo());
            item.setTotalAmount(s.getTotalAmount());
            item.setInsurancePay(s.getInsurancePay());
            item.setReceiptNo(s.getReceiptNo());
            result.add(item);
        }
        return result;
    }

    @Override
    public PageVO<JinbSettlement> settlementPage(String month, Integer status, String patientName,
                                                 String settlementNo, String billNo, String tradeNo,
                                                 int pageNum, int pageSize) {
        LambdaQueryWrapper<JinbSettlement> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(month)) {
            LocalDate start = LocalDate.parse(month + "-01");
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            wrapper.between(JinbSettlement::getBillDate, start, end);
        }
        wrapper.eq(status != null, JinbSettlement::getStatus, status);
        wrapper.like(StringUtils.hasText(patientName), JinbSettlement::getPatientName, patientName);
        wrapper.like(StringUtils.hasText(settlementNo), JinbSettlement::getSettlementNo, settlementNo);
        wrapper.like(StringUtils.hasText(billNo), JinbSettlement::getBillNo, billNo);
        wrapper.like(StringUtils.hasText(tradeNo), JinbSettlement::getTradeNo, tradeNo);
        wrapper.orderByDesc(JinbSettlement::getId);
        Page<JinbSettlement> page = settlementMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        // 列表不带原始报文，明细接口再返回，避免分页体积过大
        page.getRecords().forEach(s -> s.setRawPayload(null));
        return PageVO.of(page.getRecords(), page.getTotal());
    }

    @Override
    public JinbSettlement getById(Long id) {
        if (id == null) {
            return null;
        }
        return settlementMapper.selectById(id);
    }

    // ===== internal =====

    private JinbSettlement byTradeNo(String tradeNo) {
        if (!StringUtils.hasText(tradeNo)) {
            return null;
        }
        return settlementMapper.selectOne(new LambdaQueryWrapper<JinbSettlement>()
                .eq(JinbSettlement::getTradeNo, tradeNo).last("limit 1"));
    }

    private void saveMessage(String msgType, String tradeNo, String origTradeNo, int success,
                             String receiptNo, String errMsg, String payload) {
        JinbMessage msg = new JinbMessage();
        msg.setMsgType(msgType);
        msg.setTradeNo(tradeNo);
        msg.setOrigTradeNo(origTradeNo);
        msg.setSuccess(success);
        msg.setReceiptNo(receiptNo);
        msg.setErrMsg(errMsg);
        msg.setPayload(payload);
        messageMapper.insert(msg);
    }

    private ReceiptVO successReceipt(UploadSettlementDTO dto, String receiptNo, String settlementNo,
                                     BigDecimal poolConfirm, String note, String replyTime) {
        ReceiptVO vo = new ReceiptVO();
        vo.setInfcode(0);
        vo.setWarnMsg("");
        vo.setErrMsg("");
        vo.setMsgType("2304");
        vo.setTradeNo(dto.getTradeNo());
        vo.setReceiptNo(receiptNo);
        vo.setSettlementNo(settlementNo);
        vo.setReplyTime(replyTime);
        vo.setPoolConfirm(poolConfirm);
        vo.setNote(note);
        return vo;
    }

    private ReceiptVO successReceipt2305(CancelSettlementDTO dto, String receiptNo, String note, String replyTime) {
        ReceiptVO vo = new ReceiptVO();
        vo.setInfcode(0);
        vo.setWarnMsg("");
        vo.setErrMsg("");
        vo.setMsgType("2305");
        vo.setTradeNo(dto.getTradeNo());
        vo.setReceiptNo(receiptNo);
        vo.setSettlementNo(dto.getSettlementNo());
        vo.setReplyTime(replyTime);
        vo.setNote(note);
        return vo;
    }

    private ReceiptVO fail(String errMsg, UploadSettlementDTO dto, String replyTime, String rawPayload) {
        saveMessage("2304", dto.getTradeNo(), null, 0, null, errMsg, rawPayload);
        ReceiptVO vo = new ReceiptVO();
        vo.setInfcode(-1);
        vo.setErrMsg(errMsg);
        vo.setMsgType("2304");
        vo.setTradeNo(dto.getTradeNo());
        vo.setSettlementNo(dto.getSettlementNo());
        vo.setReplyTime(replyTime);
        return vo;
    }

    private ReceiptVO failCancel(String errMsg, CancelSettlementDTO dto, String replyTime, String rawPayload) {
        saveMessage("2305", dto.getTradeNo(), dto.getOrigTradeNo(), 0, null, errMsg, rawPayload);
        ReceiptVO vo = new ReceiptVO();
        vo.setInfcode(-1);
        vo.setErrMsg(errMsg);
        vo.setMsgType("2305");
        vo.setTradeNo(dto.getTradeNo());
        vo.setSettlementNo(dto.getSettlementNo());
        vo.setReplyTime(replyTime);
        return vo;
    }

    private BigDecimal scale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
