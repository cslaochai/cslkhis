package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.his.charge.entity.BizInsuranceReport;
import com.his.charge.entity.BizInsuredPerson;
import com.his.charge.enums.InsuranceReportStatusEnum;
import com.his.charge.enums.InsuranceReportTypeEnum;
import com.his.charge.mapper.BizInsuranceReportMapper;
import com.his.charge.mapper.BizInsuredPersonMapper;
import com.his.charge.service.InsuranceChannelService;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 医保前置机出口（G7）：本系统是报文的生产方，结算清单确认后在这里把报文发出去、拿回执、按账期取对账数据。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "his.insurance.channel.mode", havingValue = "mock", matchIfMissing = true)
public class InsuranceChannelServiceImpl extends ServiceImpl<BizInsuranceReportMapper, BizInsuranceReport> implements InsuranceChannelService {

    private final BizInsuranceReportMapper bizInsuranceReportMapper;
    private final BizInsuredPersonMapper bizInsuredPersonMapper;
    private final ObjectMapper objectMapper;

    /**
     * 发送一条报文并同步拿回执（真实前置机为「发送-等待回执」一次往返）
     */
    public Receipt send(OutboundMessage message) {
        // —— M9 留口子：这一段打印就是"向医保前置机外发报文"的占位，真前置机接入后整块替换 ——
        log.info("[M9医保外发口子] ===== 报文外发 → 医保前置机 ===== msgType={} tradeNo={}", message.getMsgType(), message.getTradeNo());
        log.info("[M9医保外发口子] 报文全文：\n{}", message.getPayload());
        String replyTime = LocalDateTime.now().format(DateFormats.DATETIME);
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String settlementNo = text(root, "settlementNo");
            BigDecimal totalAmount = root.path("fees").path("total").isNumber()
                    ? root.path("fees").path("total").decimalValue() : null;

            if (!TextUtil.hasText(settlementNo)) {
                return fail("2304/2305 报文缺少结算清单号 settlementNo", replyTime, message);
            }
            if ("2304".equals(message.getMsgType())
                    && (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0)) {
                // 费用校验只属于上传报文：2305 是撤销冲正，报文里本就不带 fees，
                // 统一校验会把撤销链路全部拒掉（G7 验证抓出）
                return fail("总费用必须大于 0，实际: " + root.path("fees").path("total").asText(), replyTime, message);
            }
            if ("2305".equals(message.getMsgType()) && !TextUtil.hasText(text(root, "origTradeNo"))) {
                return fail("撤销报文缺少被撤销单据 origTradeNo", replyTime, message);
            }

            String receiptNo = "MOCK" + message.getTradeNo();
            String reply = objectMapper.writeValueAsString(Map.of(
                    "infcode", 0,
                    "warn_msg", "",
                    "err_msg", "",
                    "msgType", message.getMsgType(),
                    "tradeNo", message.getTradeNo(),
                    "receiptNo", receiptNo,
                    "settlementNo", settlementNo,
                    "replyTime", replyTime,
                    "note", "Mock 前置机回执：真实环境由医保局核心系统返回"));
            return new Receipt(true, receiptNo, reply, null);
        } catch (Exception e) {
            log.warn("Mock 医保前置机处理报文失败 tradeNo={}", message.getTradeNo(), e);
            return fail("报文解析失败: " + e.getMessage(), replyTime, message);
        }
    }

    /**
     * 拉取指定账期日医保侧的结算明细（对账的"对方账"）
     */
    public List<RemoteSettlement> queryDayBill(LocalDate billDate) {
        // —— M9 留口子：对账"拉取医保侧账单"动作的占位打印 ——
        log.info("[M9医保外发口子] ===== 模拟从医保前置机拉取当日账单 ===== billDate={}（真实接入=调前置机对账接口）", billDate);
        List<BizInsuranceReport> records = bizInsuranceReportMapper.selectList(new LambdaQueryWrapper<BizInsuranceReport>()
                .eq(BizInsuranceReport::getBillDate, billDate)
                .eq(BizInsuranceReport::getReportType, InsuranceReportTypeEnum.UPLOAD.getCode())
                .eq(BizInsuranceReport::getStatus, InsuranceReportStatusEnum.SUCCESS.getCode())
                .orderByAsc(BizInsuranceReport::getId));
        return records.stream()
                .map(r -> new RemoteSettlement(r.getTradeNo(), r.getSettlementNo(),
                        r.getTotalAmount(), r.getInsurancePay(), r.getReceiptNo()))
                .toList();
    }

    /**
     * 参保核验（mock）：查 HIS 本地参保台账（医保局参保库的院内镜像）。
     * 真实环境 = 调医保前置机 2201/2202 人员参保信息查询；台账内容靠参保变更同步维护。
     * <p>防冒名：台账命中后比对患者主档身份证（金标准）/姓名，不一致判「身份不符」拒医保。
     */
    public InsuredStatus checkInsured(String insuranceNo, String idCard, String name) {
        if (!TextUtil.hasText(insuranceNo)) {
            return new InsuredStatus(false, false, null, null, "医保卡号为空");
        }
        BizInsuredPerson person = bizInsuredPersonMapper.selectOne(new LambdaQueryWrapper<BizInsuredPerson>()
                .eq(BizInsuredPerson::getInsuranceNo, insuranceNo)
                .last("LIMIT 1"));
        log.info("参保核验: insuranceNo={} idCard={} name={} found={} status={}", insuranceNo,
                idCard, name, person != null, person == null ? null : person.getPersonStatus());
        if (person == null) {
            return new InsuredStatus(false, false, null, null,
                    "参保库查无此卡号（" + insuranceNo + "），未参保登记");
        }
        // 防冒名：主档有身份证比身份证（金标准），主档无身份证退姓名兜底
        if (TextUtil.hasText(idCard)) {
            if (!idCard.equals(person.getIdCard())) {
                return new InsuredStatus(false, false, null, null,
                        "参保人身份与患者档案不符（身份证不一致），请核对医保卡号或改按自费结算");
            }
        } else if (TextUtil.hasText(name) && !name.equals(person.getName())) {
            return new InsuredStatus(false, false, null, null,
                    "参保人身份与患者档案不符（姓名不一致），请核对医保卡号或改按自费结算");
        }
        boolean active = Integer.valueOf(1).equals(person.getPersonStatus());
        return new InsuredStatus(true, active, person.getInsuranceType(), person.getName(),
                active ? null : "参保状态为停保（" + insuranceNo + "）");
    }

    private Receipt fail(String errMsg, String replyTime, OutboundMessage message) {
        try {
            String reply = objectMapper.writeValueAsString(Map.of(
                    "infcode", -1,
                    "err_msg", errMsg,
                    "msgType", message.getMsgType(),
                    "tradeNo", message.getTradeNo(),
                    "replyTime", replyTime));
            return new Receipt(false, null, reply, errMsg);
        } catch (Exception e) {
            return new Receipt(false, null, null, errMsg);
        }
    }

    private String text(JsonNode node, String field) {
        return node.path(field).isTextual() ? node.path(field).asText() : null;
    }
}
