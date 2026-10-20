package com.his.drgdip.settle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.entity.CurrentUser;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.drgdip.mapper.DipSimMapper;
import com.his.drgdip.mapper.DrgSimMapper;
import com.his.drgdip.settle.dto.MedpaySettleDTO;
import com.his.drgdip.settle.entity.BizCaseAppeal;
import com.his.drgdip.settle.entity.BizMedpaySettle;
import com.his.drgdip.settle.mapper.CaseAppealMapper;
import com.his.drgdip.settle.mapper.MedpaySettleMapper;
import com.his.drgdip.settle.service.MedpaySettleService;
import com.his.drgdip.settle.vo.MedpaySettleVO;
import com.his.drgdip.support.DipFacts;
import com.his.drgdip.support.DipGrouper;
import com.his.drgdip.support.DipSchemeCache;
import com.his.drgdip.support.DrgFacts;
import com.his.drgdip.support.DrgGrouper;
import com.his.drgdip.support.DrgSchemeCache;
import com.his.system.service.SysConfigService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 按病种付费结算服务实现（P4 结算层）。
 *
 * <p>金额链路（与模拟器同口径，参数未下发放 NULL 不编造）：
 * DRG 应付 = 权重 × 费率（drg.rate）；DIP 应付 = 分值（基准分值 × 系数）× 点值（dip.point_value）；
 * 盈亏 = 应付 - 实际费用。批量默认取最近 50 张出院结算账单，非出院/未关联首页的账单跳过计数。
 */
@Service
@RequiredArgsConstructor
public class MedpaySettleServiceImpl implements MedpaySettleService {

    private static final DateTimeFormatter APPEAL_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final MedpaySettleMapper medpaySettleMapper;
    private final CaseAppealMapper caseAppealMapper;
    private final DrgGrouper drgGrouper;
    private final DipGrouper dipGrouper;
    private final DrgSchemeCache drgSchemeCache;
    private final DipSchemeCache dipSchemeCache;
    private final DrgSimMapper drgSimMapper;
    private final DipSimMapper dipSimMapper;
    private final SysConfigService sysConfigService;

    private static BigDecimal money(BigDecimal v) {
        return v == null ? null : v.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal score(BigDecimal base, BigDecimal coeff) {
        if (base == null || coeff == null) {
            return null;
        }
        return base.multiply(coeff).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 主付费方式：medpay.mode，默认 DRG（一主一备，按统筹区切换）。
     */
    private String medpayMode() {
        String mode = sysConfigService.getConfigValue(SystemConfigKeyConst.MEDPAY_MODE, "DRG");
        return "DIP".equalsIgnoreCase(mode) ? "DIP" : "DRG";
    }

    /**
     * DRG 费率（元/权重）：未配置或非法返回 null（结算金额不计算）。
     */
    private BigDecimal drgRate() {
        String v = sysConfigService.getConfigValue(SystemConfigKeyConst.DRG_RATE, null);
        if (!TextUtil.hasText(v)) {
            return null;
        }
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * DIP 点值（元/分值）：未配置或非法返回 null。
     */
    private BigDecimal dipPoint() {
        String v = sysConfigService.getConfigValue(SystemConfigKeyConst.DIP_POINT_VALUE, null);
        if (!TextUtil.hasText(v)) {
            return null;
        }
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String currentUserName() {
        CurrentUser user = UserUtils.getCurrentUser();
        return user == null || !TextUtil.hasText(user.getRealName()) ? "system" : user.getRealName();
    }

    private static Long currentUserId() {
        CurrentUser user = UserUtils.getCurrentUser();
        return user == null ? null : user.getUserId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedpaySettleVO.SettleRow settleByBill(Long billId) {
        if (billId == null) {
            throw new BusinessException("结算账单ID不能为空");
        }
        MedpaySettleVO.BillRow row = medpaySettleMapper.selectSettleBillRow(billId);
        if (row == null) {
            throw new BusinessException("结算账单不存在");
        }
        if (row.getEncounterType() == null || row.getEncounterType() != 2 || row.getBillType() == null || row.getBillType() != 4) {
            throw new BusinessException("仅支持住院出院结算账单（encounter_type=2 且 bill_type=4）");
        }

        String mode = medpayMode();
        String version;
        boolean grouped;
        BigDecimal weight = null;
        BigDecimal rate = null;
        BigDecimal baseScore = null;
        BigDecimal coeff = null;
        BigDecimal sc = null;
        BigDecimal point = null;
        Integer groupType = null;
        String groupCode;
        String groupName;
        String ruleNote;

        if ("DIP".equals(mode)) {
            version = dipSchemeCache.getDefaultVersion();
            List<String> mainOpers = row.getAdmissionId() == null ? List.of()
                    : dipSimMapper.selectMainOperCodes(row.getAdmissionId());
            List<String> otherDiags = row.getAdmissionId() == null ? List.of()
                    : dipSimMapper.selectOtherDiagCodes(row.getAdmissionId());
            DipGrouper.GroupResult g = dipGrouper.group(
                    new DipFacts(row.getMainDiagnosisCode(), mainOpers, otherDiags,
                            row.getAge(), row.getAgeUnit()), version);
            grouped = g.grouped();
            groupType = g.diseaseType();
            baseScore = g.baseScore();
            coeff = g.coeff();
            sc = score(baseScore, coeff);
            point = dipPoint();
            groupCode = grouped ? g.dipCode() : null;
            groupName = g.dipName();
            ruleNote = g.ruleNote();
        } else {
            version = drgSchemeCache.getDefaultVersion();
            List<String> mainOpers = row.getAdmissionId() == null ? List.of()
                    : drgSimMapper.selectMainOperCodes(row.getAdmissionId());
            List<String> otherOpers = row.getAdmissionId() == null ? List.of()
                    : drgSimMapper.selectOtherOperCodes(row.getAdmissionId());
            List<String> otherDiags = row.getAdmissionId() == null ? List.of()
                    : drgSimMapper.selectOtherDiagCodes(row.getAdmissionId());
            DrgGrouper.GroupResult g = drgGrouper.group(
                    new DrgFacts(row.getMainDiagnosisCode(), mainOpers, otherDiags, otherOpers,
                            row.getGender(), row.getAge(), row.getAgeUnit(), row.getBirthWeight()), version);
            grouped = g.grouped();
            weight = grouped ? g.weight() : null;
            rate = drgRate();
            groupCode = grouped ? g.drgCode() : null;
            groupName = g.drgName();
            ruleNote = g.ruleNote();
        }

        BigDecimal actual = money(row.getTotalAmount());
        // 应付：任一侧未下发（未入组 / 权重分值为空 / 费率点值为空）一律 NULL，不拿 0 减实际费用冒充结余
        BigDecimal insurance = grouped && "DIP".equals(mode) && sc != null && point != null
                ? money(sc.multiply(point))
                : (grouped && !"DIP".equals(mode) && weight != null && rate != null
                        ? money(weight.multiply(rate)) : null);
        BigDecimal profit = insurance != null && actual != null ? money(insurance.subtract(actual)) : null;

        // upsert：每结算账单每方式每版本一条，重算覆盖
        BizMedpaySettle r = medpaySettleMapper.selectOne(new LambdaQueryWrapper<BizMedpaySettle>()
                .eq(BizMedpaySettle::getSettleBillId, billId)
                .eq(BizMedpaySettle::getPayMode, mode)
                .eq(BizMedpaySettle::getVersion, version)
                .last("LIMIT 1"));
        boolean create = r == null;
        if (create) {
            r = new BizMedpaySettle();
            r.setSettleBillId(billId);
            r.setCreateBy(currentUserName());
            r.setCreateById(currentUserId());
        }
        r.setBillNo(row.getBillNo());
        r.setSummaryId(row.getSummaryId());
        r.setAdmissionId(row.getAdmissionId());
        r.setPatientName(row.getPatientName());
        r.setPayMode(mode);
        r.setVersion(version);
        r.setGroupCode(groupCode);
        r.setGroupName(groupName);
        r.setGroupType(groupType);
        r.setWeight(weight);
        r.setRate(rate);
        r.setBaseScore(baseScore);
        r.setCoeff(coeff);
        r.setScore(sc);
        r.setPointValue(point);
        r.setInsuranceAmount(insurance);
        r.setActualAmount(actual);
        r.setProfitAmount(profit);
        r.setCalcStatus(grouped ? 1 : 2);
        r.setRuleNote(ruleNote);
        r.setSettleTime(LocalDateTime.now());
        r.setUpdateBy(currentUserName());
        r.setUpdateById(currentUserId());
        if (create) {
            try {
                medpaySettleMapper.insert(r);
            } catch (DuplicateKeyException e) {
                // 并发兜底：uk_medpay_settle 拦截同键并发插入 → 改为按 (bill_id, pay_mode, version) 覆盖
                int rows = medpaySettleMapper.update(r, new LambdaUpdateWrapper<BizMedpaySettle>()
                        .eq(BizMedpaySettle::getSettleBillId, billId)
                        .eq(BizMedpaySettle::getPayMode, mode)
                        .eq(BizMedpaySettle::getVersion, version));
                if (rows == 0) {
                    throw e;
                }
            }
        } else {
            medpaySettleMapper.updateById(r);
        }
        return toSettleRow(r);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MedpaySettleVO.BatchVO settleBatch(MedpaySettleDTO.SettleBatch dto) {
        List<Long> ids = dto == null ? null : dto.getBillIds();
        List<Long> billIds;
        if (ids != null && !ids.isEmpty()) {
            billIds = ids;
        } else {
            billIds = medpaySettleMapper.selectRecentSettleBillIds(50);
        }
        MedpaySettleVO.BatchVO vo = new MedpaySettleVO.BatchVO();
        List<String> skippedNotes = new ArrayList<>();
        for (Long billId : billIds) {
            try {
                settleByBill(billId);
                vo.setGrouped(vo.getGrouped() + 1);
            } catch (BusinessException e) {
                vo.setSkipped(vo.getSkipped() + 1);
                if (skippedNotes.size() < 20) {
                    skippedNotes.add("账单" + billId + "：" + e.getMessage());
                }
            }
        }
        vo.setTotal(billIds.size());
        vo.setSkippedNote(skippedNotes.isEmpty() ? "" : String.join("；", skippedNotes));
        return vo;
    }

    @Override
    public IPage<MedpaySettleVO.SettleRow> resultPage(MedpaySettleDTO.SettleQuery dto) {
        LambdaQueryWrapper<BizMedpaySettle> qw = new LambdaQueryWrapper<BizMedpaySettle>()
                .eq(dto.getPayMode() != null, BizMedpaySettle::getPayMode, dto.getPayMode())
                .eq(dto.getCalcStatus() != null, BizMedpaySettle::getCalcStatus, dto.getCalcStatus())
                .and(TextUtil.hasText(dto.getKeyword()), w -> w
                        .like(BizMedpaySettle::getPatientName, TextUtil.trimToEmpty(dto.getKeyword()))
                        .or().like(BizMedpaySettle::getGroupCode, TextUtil.trimToEmpty(dto.getKeyword()))
                        .or().like(BizMedpaySettle::getGroupName, TextUtil.trimToEmpty(dto.getKeyword())))
                .orderByDesc(BizMedpaySettle::getSettleTime)
                .orderByDesc(BizMedpaySettle::getId);
        IPage<BizMedpaySettle> page = medpaySettleMapper.selectPage(
                Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toSettleRow);
    }

    @Override
    public MedpaySettleVO.ModeVO mode() {
        MedpaySettleVO.ModeVO vo = new MedpaySettleVO.ModeVO();
        vo.setMode(medpayMode());
        vo.setDrgVersion(drgSchemeCache.getDefaultVersion());
        vo.setDipVersion(dipSchemeCache.getDefaultVersion());
        vo.setDrgRate(sysConfigService.getConfigValue(SystemConfigKeyConst.DRG_RATE, null));
        vo.setPointValue(sysConfigService.getConfigValue(SystemConfigKeyConst.DIP_POINT_VALUE, null));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizCaseAppeal createAppeal(MedpaySettleDTO.AppealCreate dto) {
        BizMedpaySettle settle = medpaySettleMapper.selectById(dto.getSettleId());
        if (settle == null) {
            throw new BusinessException("结算结果不存在");
        }
        if (dto.getReasonType() == null || dto.getReasonType() < 1 || dto.getReasonType() > 5) {
            throw new BusinessException("申请类型仅支持 1-费用极高 2-重大新技术 3-危急重症 4-罕见病 5-其他");
        }
        Long active = caseAppealMapper.selectCount(new LambdaQueryWrapper<BizCaseAppeal>()
                .eq(BizCaseAppeal::getSettleId, dto.getSettleId())
                .in(BizCaseAppeal::getStatus, 1, 2));
        if (active != null && active > 0) {
            throw new BusinessException("该结算已有进行中的特例单议申请");
        }
        BizCaseAppeal appeal = new BizCaseAppeal();
        appeal.setAppealNo("AL" + APPEAL_NO_FMT.format(LocalDateTime.now()));
        appeal.setSettleId(settle.getId());
        appeal.setSettleBillId(settle.getSettleBillId());
        appeal.setSummaryId(settle.getSummaryId());
        appeal.setPatientName(settle.getPatientName());
        appeal.setPayMode(settle.getPayMode());
        appeal.setGroupCode(settle.getGroupCode());
        appeal.setGroupName(settle.getGroupName());
        appeal.setReasonType(dto.getReasonType());
        appeal.setReasonDesc(dto.getReasonDesc());
        appeal.setProfitAmount(settle.getProfitAmount());
        appeal.setStatus(1);
        appeal.setCreateBy(currentUserName());
        appeal.setCreateById(currentUserId());
        appeal.setUpdateBy(currentUserName());
        appeal.setUpdateById(currentUserId());
        caseAppealMapper.insert(appeal);
        return appeal;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAppeal(Long id) {
        BizCaseAppeal appeal = requireAppeal(id);
        if (appeal.getStatus() == null || appeal.getStatus() != 1) {
            throw new BusinessException("仅草稿（1）状态的申请单可提交");
        }
        appeal.setStatus(2);
        appeal.setApplyBy(currentUserName());
        appeal.setApplyById(currentUserId());
        appeal.setApplyTime(LocalDateTime.now());
        appeal.setUpdateBy(currentUserName());
        appeal.setUpdateById(currentUserId());
        caseAppealMapper.updateById(appeal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewAppeal(MedpaySettleDTO.AppealReview dto) {
        BizCaseAppeal appeal = requireAppeal(dto.getId());
        if (appeal.getStatus() == null || appeal.getStatus() != 2) {
            throw new BusinessException("仅已提交（2）状态的申请单可审核");
        }
        appeal.setStatus(Boolean.TRUE.equals(dto.getApproved()) ? 3 : 4);
        appeal.setReviewBy(currentUserName());
        appeal.setReviewById(currentUserId());
        appeal.setReviewTime(LocalDateTime.now());
        appeal.setReviewRemark(dto.getReviewRemark());
        appeal.setUpdateBy(currentUserName());
        appeal.setUpdateById(currentUserId());
        caseAppealMapper.updateById(appeal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawAppeal(Long id) {
        BizCaseAppeal appeal = requireAppeal(id);
        if (appeal.getStatus() == null || appeal.getStatus() != 1) {
            throw new BusinessException("仅草稿（1）状态的申请单可撤回");
        }
        appeal.setStatus(5);
        appeal.setUpdateBy(currentUserName());
        appeal.setUpdateById(currentUserId());
        caseAppealMapper.updateById(appeal);
    }

    @Override
    public IPage<MedpaySettleVO.AppealRow> appealPage(MedpaySettleDTO.AppealQuery dto) {
        LambdaQueryWrapper<BizCaseAppeal> qw = new LambdaQueryWrapper<BizCaseAppeal>()
                .eq(dto.getPayMode() != null, BizCaseAppeal::getPayMode, dto.getPayMode())
                .eq(dto.getStatus() != null, BizCaseAppeal::getStatus, dto.getStatus())
                .and(TextUtil.hasText(dto.getKeyword()), w -> w
                        .like(BizCaseAppeal::getPatientName, TextUtil.trimToEmpty(dto.getKeyword()))
                        .or().like(BizCaseAppeal::getAppealNo, TextUtil.trimToEmpty(dto.getKeyword())))
                .orderByDesc(BizCaseAppeal::getCreateTime)
                .orderByDesc(BizCaseAppeal::getId);
        IPage<BizCaseAppeal> page = caseAppealMapper.selectPage(
                Page.of(dto.getPageNum(), dto.getPageSize()), qw);
        return page.convert(this::toAppealRow);
    }

    private BizCaseAppeal requireAppeal(Long id) {
        if (id == null) {
            throw new BusinessException("申请单ID不能为空");
        }
        BizCaseAppeal appeal = caseAppealMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException("特例单议申请不存在");
        }
        return appeal;
    }

    private MedpaySettleVO.SettleRow toSettleRow(BizMedpaySettle r) {
        MedpaySettleVO.SettleRow vo = new MedpaySettleVO.SettleRow();
        vo.setId(r.getId());
        vo.setSettleBillId(r.getSettleBillId());
        vo.setBillNo(r.getBillNo());
        vo.setSummaryId(r.getSummaryId());
        vo.setAdmissionId(r.getAdmissionId());
        vo.setPatientName(r.getPatientName());
        vo.setPayMode(r.getPayMode());
        vo.setVersion(r.getVersion());
        vo.setGroupCode(r.getGroupCode());
        vo.setGroupName(r.getGroupName());
        vo.setGroupType(r.getGroupType());
        vo.setWeight(r.getWeight());
        vo.setRate(r.getRate());
        vo.setBaseScore(r.getBaseScore());
        vo.setCoeff(r.getCoeff());
        vo.setScore(r.getScore());
        vo.setPointValue(r.getPointValue());
        vo.setInsuranceAmount(r.getInsuranceAmount());
        vo.setActualAmount(r.getActualAmount());
        vo.setProfitAmount(r.getProfitAmount());
        vo.setCalcStatus(r.getCalcStatus());
        vo.setRuleNote(r.getRuleNote());
        vo.setSettleTime(r.getSettleTime());
        return vo;
    }

    private MedpaySettleVO.AppealRow toAppealRow(BizCaseAppeal a) {
        MedpaySettleVO.AppealRow vo = new MedpaySettleVO.AppealRow();
        vo.setId(a.getId());
        vo.setAppealNo(a.getAppealNo());
        vo.setSettleId(a.getSettleId());
        vo.setSettleBillId(a.getSettleBillId());
        vo.setSummaryId(a.getSummaryId());
        vo.setPatientName(a.getPatientName());
        vo.setPayMode(a.getPayMode());
        vo.setGroupCode(a.getGroupCode());
        vo.setGroupName(a.getGroupName());
        vo.setReasonType(a.getReasonType());
        vo.setReasonDesc(a.getReasonDesc());
        vo.setProfitAmount(a.getProfitAmount());
        vo.setStatus(a.getStatus());
        vo.setApplyBy(a.getApplyBy());
        vo.setApplyTime(a.getApplyTime());
        vo.setReviewBy(a.getReviewBy());
        vo.setReviewTime(a.getReviewTime());
        vo.setReviewRemark(a.getReviewRemark());
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }
}
