package com.mis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mis.entity.*;
import com.mis.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务数据补洞（幂等）：补齐三类此前为空的真实表 ——
 * ① 参保人扩量（mss_insured_person，从 5 条补到约 30 条）；
 * ② 机构 2304 报盘对方账（mss_settlement + mss_message 台账，近 3 个月）；
 * ③ 月度结算（mss_monthly + 扣款/拨付明细，最近月已申报、上月已拨付、上上月已清算）。
 * 数据按固定种子生成、插入即幂等（唯一键/空表判断），重启不会重复灌。
 * HIS 真实结算后续接入：HIS 侧按 2304 报文 POST /mss/gateway/2304 即自动落 mss_settlement，
 * 月度申报会从该表聚合，无需改动本类。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BizSeedInitializer implements ApplicationRunner {

    private final JinbSettlementMapper settlementMapper;
    private final JinbMessageMapper messageMapper;
    private final JinbMonthlyMapper monthlyMapper;
    private final JinbMonthlyDeductMapper deductMapper;
    private final JinbAllocationMapper allocationMapper;
    private final JinbInsuredPersonMapper personMapper;
    private final MssBillMapper billMapper;
    private final MssAuditRuleMapper ruleMapper;
    private final MssSuspicionMapper suspicionMapper;
    private final MssInspectionMapper inspectionMapper;
    private final MssInspectionItemMapper inspectionItemMapper;
    private final MssRecoverMapper recoverMapper;
    private final MisProperties properties;
    private final ObjectMapper objectMapper;

    private long seed = 20261014L;

    private static final String[] SURNAMES = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜".split("");
    private static final String GIVEN = "伟芳娜敏静秀强磊军洋勇艳杰娟涛明超霞平刚桂英文辉建国";
    private static final String[] REGIONS = {"长沙市", "株洲市", "湘潭市"};
    private static final String[] AREAS = {"北京市", "上海市", "广州市", "深圳市", "武汉市", "成都市", "杭州市"};
    private static final String[] DEPTS = {"门诊内科", "门诊外科", "门诊儿科", "门诊骨科", "急诊科", "心血管内科", "呼吸内科"};
    private static final String[] DOCTORS = {"张建国", "李秀英", "王伟", "陈静", "刘强", "赵敏", "孙磊"};

    @Override
    public void run(ApplicationArguments args) {
        List<JinbInsuredPerson> insured = seedInsuredPersons();
        List<JinbSettlement> settlements = seedSettlements(insured);
        seedMonthly(settlements);
        seedSupervise(settlements);
        log.info("[经办平台] 数据补洞完成：参保人{} 2304报盘{} 月度结算{} 监管疑点{}",
                personMapper.selectCount(null), settlementMapper.selectCount(null), monthlyMapper.selectCount(null),
                suspicionMapper.selectCount(null));
    }

    // --------------------------------------------------------------- 参保人

    private List<JinbInsuredPerson> seedInsuredPersons() {
        List<JinbInsuredPerson> existing = personMapper.selectList(null);
        List<JinbInsuredPerson> result = new ArrayList<>(existing);
        int target = 30;
        while (result.size() < target) {
            String name = makeName();
            String idCard = makeIdCard();
            boolean resident = rnd() > 0.55;
            JinbInsuredPerson p = new JinbInsuredPerson();
            p.setIdCard(idCard);
            p.setInsuranceNo("4301" + String.format("%04d", 1000 + result.size()));
            p.setName(name);
            p.setGender(rnd() > 0.5 ? "男" : "女");
            p.setInsuranceType(resident ? "3" : "2");
            p.setPersonStatus(rnd() > 0.05 ? 1 : 0);
            p.setAccountBalance(resident ? BigDecimal.ZERO : bd(rnd() * 5000));
            p.setRegion(pick(REGIONS));
            personMapper.insert(p);
            result.add(p);
        }
        return result;
    }

    // --------------------------------------------------------------- 2304 报盘

    private List<JinbSettlement> seedSettlements(List<JinbInsuredPerson> insured) {
        if (settlementMapper.selectCount(null) != null && settlementMapper.selectCount(null) > 0) {
            return settlementMapper.selectList(null);
        }
        List<JinbSettlement> all = new ArrayList<>();
        String medinsCode = properties.getMedins().getCode();
        LocalDate today = LocalDate.now();
        // 近 3 个月：上上月 / 上月 / 本月
        YearMonth[] months = {YearMonth.now().minusMonths(2), YearMonth.now().minusMonths(1), YearMonth.now()};
        int seq = 0;
        for (YearMonth ym : months) {
            int daysInMonth = ym.lengthOfMonth();
            int lastDay = ym.equals(YearMonth.now()) ? today.getDayOfMonth() : daysInMonth;
            for (int day = 1; day <= lastDay; day++) {
                int perDay = 1 + randInt(0, 2); // 每天 1-3 条，共 60~90 条
                for (int k = 0; k < perDay; k++) {
                    seq++;
                    JinbInsuredPerson person = pick(insured);
                    LocalDate billDate = ym.atDay(day);
                    BigDecimal total = bd(600 + rnd() * 31400);
                    BigDecimal insurancePay = bd(total.doubleValue() * (0.45 + rnd() * 0.4));
                    BigDecimal personalPay = bd(total.doubleValue() * (0.05 + rnd() * 0.2));
                    BigDecimal selfPay = bd(total.subtract(insurancePay).subtract(personalPay).doubleValue());
                    String tradeNo = "JS" + billDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + String.format("%04d", seq);
                    String settlementNo = "YB" + tradeNo.substring(2);

                    JinbSettlement s = new JinbSettlement();
                    s.setTradeNo(tradeNo);
                    s.setSettlementNo(settlementNo);
                    s.setFixMedinsCode(medinsCode);
                    s.setBillNo("HIS" + tradeNo.substring(2));
                    s.setEncounterType(rnd() > 0.5 ? "门诊" : "住院");
                    s.setPatientName(person.getName());
                    s.setIdCard(person.getIdCard());
                    s.setInsuranceNo(person.getInsuranceNo());
                    s.setInsuranceType("2".equals(person.getInsuranceType()) ? "职工医保" : "居民医保");
                    s.setTotalAmount(total);
                    s.setInsurancePay(insurancePay);
                    s.setPersonalPay(personalPay);
                    s.setSelfPay(selfPay);
                    s.setPoolConfirm(bd(insurancePay.doubleValue() * (0.96 + rnd() * 0.04)));
                    s.setStatus(1);
                    s.setBillDate(billDate);
                    s.setReceiptNo("MS" + billDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + String.format("%04d", seq));
                    s.setWarnMsg(rnd() > 0.94 ? "提示：乙类项目先行自付比例偏高，请核对" : null);
                    s.setRawPayload(build2304Payload(s));
                    s.setCreateTime(billDate.atTime(randInt(8, 18), randInt(0, 59)));
                    settlementMapper.insert(s);
                    all.add(s);

                    JinbMessage msg = new JinbMessage();
                    msg.setMsgType("2304");
                    msg.setTradeNo(tradeNo);
                    msg.setSuccess(1);
                    msg.setReceiptNo(s.getReceiptNo());
                    msg.setPayload(s.getRawPayload());
                    msg.setCreateTime(s.getCreateTime());
                    messageMapper.insert(msg);
                }
            }
        }
        return all;
    }

    private String build2304Payload(JinbSettlement s) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("infcode", 0);
        node.put("msgType", "2304");
        node.put("tradeNo", s.getTradeNo());
        node.put("billNo", s.getBillNo());
        node.put("fixMedinsCode", s.getFixMedinsCode());
        node.put("patientName", s.getPatientName());
        node.put("idCard", s.getIdCard());
        node.put("insuranceType", s.getInsuranceType());
        node.put("encounterType", s.getEncounterType());
        node.put("totalAmount", s.getTotalAmount());
        node.put("insurancePay", s.getInsurancePay());
        node.put("personalPay", s.getPersonalPay());
        node.put("selfPay", s.getSelfPay());
        node.put("billDate", s.getBillDate().toString());
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }

    // --------------------------------------------------------------- 月度结算

    private void seedMonthly(List<JinbSettlement> settlements) {
        if (monthlyMapper.selectCount(null) != null && monthlyMapper.selectCount(null) > 0) {
            return;
        }
        // 按月份聚合 2304 数据，取最近 3 个有数据的账期
        Map<String, List<JinbSettlement>> byMonth = new java.util.TreeMap<>();
        for (JinbSettlement s : settlements) {
            String m = s.getBillDate().toString().substring(0, 7);
            byMonth.computeIfAbsent(m, k -> new ArrayList<>()).add(s);
        }
        List<Map.Entry<String, List<JinbSettlement>>> list = new ArrayList<>(byMonth.entrySet());
        if (list.isEmpty()) {
            return;
        }
        // 最远 → 已清算；中间 → 已拨付；最近 → 已申报
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Map.Entry<String, List<JinbSettlement>> e = list.get(i);
            String month = e.getKey();
            List<JinbSettlement> ms = e.getValue();
            int stage = i == 0 ? 4 : i == size - 1 ? 1 : 3; // 4 清算 / 1 申报 / 3 拨付
            seedOneMonth(month, ms, stage);
        }
    }

    private void seedOneMonth(String month, List<JinbSettlement> ms, int stage) {
        BigDecimal declareAmount = ms.stream()
                .map(s -> nz(s.getInsurancePay()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        declareAmount = scale(declareAmount);

        // 扣款明细：已审核及之后阶段从申报单中抽查
        List<JinbMonthlyDeduct> deducts = new ArrayList<>();
        if (stage >= 2) {
            int n = 2 + randInt(0, 3);
            for (int i = 0; i < n && i < ms.size(); i++) {
                JinbSettlement s = ms.get(i);
                boolean reject = rnd() > 0.6;
                JinbMonthlyDeduct d = new JinbMonthlyDeduct();
                d.setMonth(month);
                d.setTradeNo(s.getTradeNo());
                d.setSettlementNo(s.getSettlementNo());
                d.setDeductType(reject ? 1 : 2);
                d.setDeductAmount(reject ? s.getInsurancePay() : bd(s.getInsurancePay().doubleValue() * 0.2));
                d.setReason(reject ? "丙类项目未签署知情同意书，全额拒付" : "乙类项目先行自付比例与政策不符，按比例扣减");
                d.setCreateTime(LocalDateTime.now());
                deductMapper.insert(d);
                deducts.add(d);
            }
        }
        BigDecimal deductAmount = deducts.stream().map(JinbMonthlyDeduct::getDeductAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        deductAmount = scale(deductAmount);
        BigDecimal reserveAmount = scale(declareAmount.multiply(properties.getAudit().getReserveRatio()));
        BigDecimal payableAmount = scale(declareAmount.subtract(deductAmount).subtract(reserveAmount));

        JinbMonthly m = new JinbMonthly();
        m.setMonth(month);
        m.setFixMedinsCode(properties.getMedins().getCode());
        m.setDeclareCount(ms.size());
        m.setDeclareAmount(declareAmount);
        m.setDeductAmount(deductAmount);
        m.setReserveAmount(reserveAmount);
        m.setPayableAmount(payableAmount);
        m.setStatus(stage);
        long monthsAgo = stage == 1 ? 0L : stage == 3 ? 1L : 2L;
        LocalDateTime declareTime = LocalDateTime.now().minusMonths(monthsAgo).withDayOfMonth(5).withHour(10);
        m.setDeclareTime(declareTime);
        if (stage >= 2) {
            m.setAuditTime(declareTime.plusDays(2));
        }
        if (stage >= 3) {
            m.setAllocateTime(declareTime.plusDays(4));
        }
        if (stage >= 4) {
            m.setSettleTime(declareTime.plusDays(8));
        }
        monthlyMapper.insert(m);

        // 拨付单
        if (stage >= 3) {
            MisAllocation alloc = new MisAllocation();
            alloc.setMonth(month);
            alloc.setAllocNo("BF" + month.replace("-", "") + String.format("%03d", 1));
            alloc.setAllocType(1);
            alloc.setAmount(payableAmount);
            alloc.setRemark("月度基金拨付");
            alloc.setCreateTime(m.getAllocateTime());
            allocationMapper.insert(alloc);
            if (stage >= 4) {
                MisAllocation refund = new MisAllocation();
                refund.setMonth(month);
                refund.setAllocNo("BF" + month.replace("-", "") + String.format("%03d", 2));
                refund.setAllocType(2);
                refund.setAmount(reserveAmount);
                refund.setRemark("年度清算质保金返还");
                refund.setCreateTime(m.getSettleTime());
                allocationMapper.insert(refund);
            }
        }
    }

    // --------------------------------------------------------------- 基金监管演示数据

    /**
     * 监管侧补洞（幂等，仅表空时执行）：疑点记录（含已确认/已排除）、
     * 违规追回单（已追回/待追回各 1 张）、飞检任务（1 检查中 + 1 已完成）。
     * 一键稽核（/supervise/suspicion/run）会在其上增量扫描，种子数据不重复。
     */
    private void seedSupervise(List<JinbSettlement> settlements) {
        if (suspicionMapper.selectCount(null) != null && suspicionMapper.selectCount(null) > 0) {
            return;
        }
        List<MssAuditRule> rules = ruleMapper.selectList(null);
        List<MssBill> bills = billMapper.selectList(null);
        List<MssSuspicion> suspects = new ArrayList<>();
        String today8 = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = 1;

        // 机构报盘侧疑点（取前 4 条结算单）：2 待核实 + 1 已确认 + 1 已排除
        List<MssAuditRule> rulesSett = filterRules(rules, 2);
        for (int i = 0; i < 4 && i < settlements.size(); i++) {
            JinbSettlement s = settlements.get(i);
            MssAuditRule rule = rulesSett.isEmpty() ? null : rulesSett.get(i % rulesSett.size());
            MssSuspicion sp = new MssSuspicion();
            sp.setSuspicionNo("YZ" + today8 + String.format("%03d", seq++));
            sp.setRuleId(rule == null ? null : rule.getId());
            sp.setRuleName(rule == null ? "分解住院" : rule.getRuleName());
            sp.setRuleType(rule == null ? "split_hospitalization" : rule.getRuleType());
            sp.setBizSource(2);
            sp.setBizId(s.getId());
            sp.setBizNo(s.getTradeNo());
            sp.setPatientName(s.getPatientName());
            sp.setIdCardMask(maskIdCard(s.getIdCard()));
            sp.setInsuranceType(s.getInsuranceType());
            sp.setEncounterType(s.getEncounterType());
            sp.setBizDate(s.getBillDate());
            sp.setInvolvedAmount(s.getInsurancePay());
            sp.setReason("规则引擎扫描命中：" + (rule == null ? "分解住院" : rule.getRuleName()) + "（" + sp.getBizNo() + "）");
            if (i < 2) {
                sp.setStatus("待核实");
            } else if (i == 2) {
                sp.setStatus("已确认");
                sp.setConfirmAmount(sp.getInvolvedAmount());
                sp.setHandleUser("yb002");
                sp.setHandleTime(LocalDateTime.now().minusDays(1));
            } else {
                sp.setStatus("已排除");
                sp.setHandleUser("yb002");
                sp.setHandleTime(LocalDateTime.now().minusDays(1));
                sp.setRemark("复核票据与病程记录一致，排除疑点");
            }
            suspicionMapper.insert(sp);
            suspects.add(sp);
        }
        // 经办结算单侧疑点（取前 3 条账单）：2 待核实 + 1 已确认
        List<MssAuditRule> rulesBill = filterRules(rules, 1);
        for (int i = 0; i < 3 && i < bills.size(); i++) {
            MssBill b = bills.get(i);
            MssAuditRule rule = rulesBill.isEmpty() ? null : rulesBill.get(i % rulesBill.size());
            MssSuspicion sp = new MssSuspicion();
            sp.setSuspicionNo("YZ" + today8 + String.format("%03d", seq++));
            sp.setRuleId(rule == null ? null : rule.getId());
            sp.setRuleName(rule == null ? "重复收费" : rule.getRuleName());
            sp.setRuleType(rule == null ? "duplicate_charge" : rule.getRuleType());
            sp.setBizSource(1);
            sp.setBizId(b.getId());
            sp.setBizNo(b.getBillNo());
            sp.setPatientName(b.getPatientName());
            sp.setIdCardMask(b.getIdCardMask() != null ? b.getIdCardMask() : maskIdCard(b.getIdCard()));
            sp.setInsuranceType(b.getInsuranceType());
            sp.setEncounterType(b.getSettleType());
            sp.setDept(b.getDept());
            sp.setDoctor(b.getDoctor());
            sp.setBizDate(b.getSettleTime() == null ? null : b.getSettleTime().toLocalDate());
            sp.setInvolvedAmount(b.getInsurancePay());
            sp.setReason("规则引擎扫描命中：" + (rule == null ? "重复收费" : rule.getRuleName()) + "（" + sp.getBizNo() + "）");
            if (i == 2) {
                sp.setStatus("已确认");
                sp.setConfirmAmount(sp.getInvolvedAmount());
                sp.setHandleUser("yb002");
                sp.setHandleTime(LocalDateTime.now().minusDays(1));
            } else {
                sp.setStatus("待核实");
            }
            suspicionMapper.insert(sp);
            suspects.add(sp);
        }

        // 已确认疑点生成追回单：1 已追回 + 1 待追回
        List<MssSuspicion> confirmed = new ArrayList<>();
        for (MssSuspicion sp : suspects) {
            if ("已确认".equals(sp.getStatus())) {
                confirmed.add(sp);
            }
        }
        for (int i = 0; i < confirmed.size(); i++) {
            MssSuspicion sp = confirmed.get(i);
            MssRecover r = new MssRecover();
            r.setRecoverNo("ZJ" + today8 + String.format("%03d", i + 1));
            r.setSourceType(1);
            r.setSourceNo(sp.getSuspicionNo());
            r.setTargetOrgCode(properties.getMedins().getCode());
            r.setTargetOrgName(properties.getMedins().getName());
            r.setMonth(sp.getBizDate() == null ? null : sp.getBizDate().toString().substring(0, 7));
            r.setRecoverAmount(sp.getConfirmAmount());
            r.setReason("稽核确认违规：" + sp.getReason());
            if (i == 0) {
                r.setStatus("已追回");
                r.setRecoverTime(LocalDateTime.now().minusDays(2));
                r.setHandler("yb002");
            } else {
                r.setStatus("待追回");
            }
            recoverMapper.insert(r);
        }

        // 飞检任务：1 检查中（含 3 条问题明细）+ 1 已完成
        MssInspection ins = new MssInspection();
        ins.setInspectionNo("FJ" + today8 + "001");
        ins.setTitle("2026 年第三季度专项飞检——重复收费与超限价");
        ins.setInspectType("专项检查");
        ins.setTargetOrg(properties.getMedins().getName());
        ins.setPlanStart(LocalDate.now().minusDays(5));
        ins.setPlanEnd(LocalDate.now().plusDays(2));
        ins.setLeader("审核稽核科");
        ins.setMembers("审核稽核科");
        ins.setScopeDesc("抽查近 3 个月结算单重复收费、超限价收费、丙类项目入统筹问题");
        ins.setStatus("检查中");
        inspectionMapper.insert(ins);
        String[] pTypes = {"重复收费", "超限价收费", "丙类项目入统筹"};
        String[] pDisposes = {"追回", "整改", "移交稽核"};
        for (int i = 0; i < 3; i++) {
            MssInspectionItem it = new MssInspectionItem();
            it.setInspectionId(ins.getId());
            it.setProblemType(pTypes[i]);
            it.setProblemDesc("抽查发现「" + pTypes[i] + "」问题 " + (i + 1) + " 例");
            it.setEvidence("结算明细与目录限价比对记录");
            it.setInvolvedAmount(bd(300 + i * 220));
            it.setDispose(pDisposes[i]);
            inspectionItemMapper.insert(it);
        }
        MssInspection done = new MssInspection();
        done.setInspectionNo("FJ" + today8 + "002");
        done.setTitle("群众举报核查——虚记诊疗项目");
        done.setInspectType("举报核查");
        done.setTargetOrg(properties.getMedins().getName());
        done.setPlanStart(LocalDate.now().minusDays(20));
        done.setPlanEnd(LocalDate.now().minusDays(15));
        done.setLeader("审核稽核科");
        done.setStatus("已完成");
        done.setResultSummary("经核查，个别结算单存在虚记诊疗项目情形，已下达整改通知并移交追回。");
        inspectionMapper.insert(done);
    }

    private List<MssAuditRule> filterRules(List<MssAuditRule> rules, Integer target) {
        List<MssAuditRule> out = new ArrayList<>();
        for (MssAuditRule r : rules) {
            if (target.equals(r.getTarget())) {
                out.add(r);
            }
        }
        return out;
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() <= 8) {
            return idCard;
        }
        int stars = idCard.length() - 8;
        return idCard.substring(0, 4) + "*".repeat(stars) + idCard.substring(idCard.length() - 4);
    }

    // --------------------------------------------------------------- 工具

    private String makeName() {
        String s = pick(SURNAMES) + GIVEN.charAt(randInt(0, GIVEN.length() - 1));
        if (rnd() > 0.5) {
            s += GIVEN.charAt(randInt(0, GIVEN.length() - 1));
        }
        return s;
    }

    private String makeIdCard() {
        String region = "4301" + randInt(10, 99) + randInt(10, 99);
        return region + randInt(1960, 2003) + String.format("%02d", randInt(1, 12))
                + String.format("%02d", randInt(1, 28)) + String.format("%04d", randInt(0, 9999));
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal scale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal bd(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private double rnd() {
        seed = (seed * 1103515245L + 12345L) & 0x7fffffffL;
        return seed / (double) Integer.MAX_VALUE;
    }

    private <T> T pick(List<T> arr) {
        return arr.get((int) (rnd() * arr.size()));
    }

    private <T> T pick(T[] arr) {
        return arr[(int) (rnd() * arr.length)];
    }

    private int randInt(int a, int b) {
        return a + (int) (rnd() * (b - a + 1));
    }
}
