package com.his.medicaltech.service.impl;

import com.his.medicaltech.service.LisEqaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.dto.LisEqaDTO;
import com.his.medicaltech.entity.BizLisEqaCompare;
import com.his.medicaltech.entity.BizLisEqaPlan;
import com.his.medicaltech.entity.BizLisEqaSample;
import com.his.medicaltech.mapper.BizLisEqaCompareMapper;
import com.his.medicaltech.mapper.BizLisEqaPlanMapper;
import com.his.medicaltech.mapper.BizLisEqaSampleMapper;
import com.his.medicaltech.support.EqaJudgeEngine;
import com.his.medicaltech.vo.LisEqaVO;
import com.his.medicaltech.support.SubDictText;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 室间质评（EQA）服务
 *
 * <p>一句话定位：室内质控回答"我的机器今天稳不稳"，EQA 回答"我的数跟外面几百家比准不准"。
 * 这条链的关键是 <b>盲样</b> —— 不知道靶值的样品，按常规标本一样上机做，
 * 结果报给组织方，等各家数据汇总、对方把靶值回报回来，才知道自己偏了多少。
 * 全程不依赖外部系统接口：台账、上报、回报、判定、整改都在本系统闭环。
 *
 * <p>三条硬规则：
 * <ol>
 *   <li><b>判定只在服务端</b>：SDI / 偏倚 / 满意还是不合格，全部 {@link EqaJudgeEngine} 算完落库。
 *       室间质评的成绩是给卫健委评审看的，"前端判合格"这种写法等于可以凭空造证据。</li>
 *   <li><b>不合格必须闭环才准归档</b>：判定不合格的行自动要求填原因 + 纠正措施 + 第二人复核；
 *       没闭环就不让归档 —— 归档意味着"这次考核结案了"，带着没整改的不合格项结案，台账是假的。</li>
 *   <li><b>互差按整机 Yeah 落事实表</b>：每次成绩回报后重算本批次互差（物理删后重插），
 *       归档后就是不可变的快照。事后改仪器/改方法，改不掉当年的可比性结论。</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class LisEqaServiceImpl implements LisEqaService {

    private static final String DICT_PLAN_STATUS = "his_lis_eqa_plan_status";
    private static final String DICT_SAMPLE_STATUS = "his_lis_eqa_sample_status";
    private static final String DICT_RESULT_STATUS = "his_lis_eqa_result_status";
    private static final String DICT_JUDGE_MODE = "his_lis_eqa_judge_mode";
    private static final String DICT_COMPARE_STATUS = "his_lis_eqa_compare_status";

    /** 批次状态：3-已上报 4-已回报 5-已归档（服务端单向推进） */
    private static final int PLAN_REPORTED = 3;
    private static final int PLAN_RETURNED = 4;
    private static final int PLAN_ARCHIVED = 5;

    /** 盲样流转：1-已检测 2-已上报 3-已回报 */
    private static final int SAMPLE_TESTED = 1;
    private static final int SAMPLE_REPORTED = 2;
    private static final int SAMPLE_RETURNED = 3;

    private final BizLisEqaPlanMapper planMapper;
    private final BizLisEqaSampleMapper sampleMapper;
    private final BizLisEqaCompareMapper compareMapper;
    private final SubDictText dictText;

    // 批次台账

    public PageResult<LisEqaVO.PlanVO> planPage(LisEqaDTO.PlanQuery q) {
        LambdaQueryWrapper<BizLisEqaPlan> w = new LambdaQueryWrapper<>();
        w.eq(q.getPlanYear() != null, BizLisEqaPlan::getPlanYear, q.getPlanYear())
                .eq(q.getBatchNo() != null, BizLisEqaPlan::getBatchNo, q.getBatchNo())
                .like(StringUtils.hasText(q.getOrgName()), BizLisEqaPlan::getOrgName, tr(q.getOrgName()))
                .eq(q.getStatus() != null, BizLisEqaPlan::getStatus, q.getStatus())
                .orderByDesc(BizLisEqaPlan::getPlanYear)
                .orderByDesc(BizLisEqaPlan::getBatchNo)
                .orderByDesc(BizLisEqaPlan::getId);
        Page<BizLisEqaPlan> page = planMapper.selectPage(page(q.getPageNum(), q.getPageSize()), w);
        List<LisEqaVO.PlanVO> vos = new ArrayList<>();
        for (BizLisEqaPlan p : page.getRecords()) {
            vos.add(toPlanVo(p));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    private LisEqaVO.PlanVO toPlanVo(BizLisEqaPlan p) {
        LisEqaVO.PlanVO vo = new LisEqaVO.PlanVO();
        BeanUtils.copyProperties(p, vo);
        vo.setStatusText(dictText.text(DICT_PLAN_STATUS, p.getStatus()));
        vo.setPassFlagText(p.getPassFlag() == null ? "未出成绩"
                : (p.getPassFlag() == 1 ? "合格" : "不合格"));
        vo.setPendingCount(sampleMapper.selectCount(new LambdaQueryWrapper<BizLisEqaSample>()
                .eq(BizLisEqaSample::getPlanId, p.getId())
                .lt(BizLisEqaSample::getStatus, SAMPLE_RETURNED)).intValue());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public String planUpsert(LisEqaDTO.PlanUpsert dto) {
        BizLisEqaPlan p;
        if (dto.getId() == null) {
            long dup = planMapper.selectCount(new LambdaQueryWrapper<BizLisEqaPlan>()
                    .eq(BizLisEqaPlan::getPlanYear, dto.getPlanYear())
                    .eq(BizLisEqaPlan::getBatchNo, dto.getBatchNo())
                    .eq(BizLisEqaPlan::getOrgName, tr(dto.getOrgName())));
            if (dup > 0) {
                throw new BusinessException(dto.getPlanYear() + " 年第 " + dto.getBatchNo()
                        + " 批（" + tr(dto.getOrgName()) + "）已登记，请勿重复建批次");
            }
            p = new BizLisEqaPlan();
            BeanUtils.copyProperties(dto, p);
            p.setId(null);
            p.setOrgName(tr(dto.getOrgName()));
            p.setStatus(1);
            p.setPlanNo(nextPlanNo(dto.getPlanYear(), dto.getBatchNo()));
            planMapper.insert(p);
        } else {
            p = requirePlan(dto.getId());
            if (p.getStatus() != null && p.getStatus() == PLAN_ARCHIVED) {
                throw new BusinessException("批次已归档（" + p.getPlanNo() + "），不可再修改");
            }
            BeanUtils.copyProperties(dto, p);
            p.setId(dto.getId());
            p.setOrgName(tr(dto.getOrgName()));
            planMapper.updateById(p);
        }
        return p.getPlanNo();
    }

    /**
     * 归档：本次质评结案。
     *
     * <p>前置条件卡两件事 —— ① 成绩必须回报完（否则 PT 得分是个假的暂定值）；
     * ② 不合格项必须整改且复核完（带着没闭环的不合格项归档，等于台账造假）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void archive(LisEqaDTO.PlanArchive dto) {
        BizLisEqaPlan p = requirePlan(dto.getPlanId());
        if (p.getStatus() != null && p.getStatus() == PLAN_ARCHIVED) {
            throw new BusinessException("批次已归档（" + p.getPlanNo() + "），无需重复归档");
        }
        if (p.getStatus() == null || p.getStatus() < PLAN_RETURNED) {
            throw new BusinessException("批次当前为「" + dictText.text(DICT_PLAN_STATUS, p.getStatus())
                    + "」，须等成绩全部回报后才能归档");
        }
        long todo = sampleMapper.selectCount(new LambdaQueryWrapper<BizLisEqaSample>()
                .eq(BizLisEqaSample::getPlanId, p.getId())
                .eq(BizLisEqaSample::getHandleStatus, 1));
        if (todo > 0) {
            throw new BusinessException("还有 " + todo + " 项不合格未完成整改，不允许归档");
        }
        long toReview = sampleMapper.selectCount(new LambdaQueryWrapper<BizLisEqaSample>()
                .eq(BizLisEqaSample::getPlanId, p.getId())
                .eq(BizLisEqaSample::getHandleStatus, 2)
                .isNull(BizLisEqaSample::getReviewBy));
        if (toReview > 0) {
            throw new BusinessException("还有 " + toReview + " 项整改未完成复核，不允许归档");
        }
        p.setStatus(PLAN_ARCHIVED);
        p.setArchiveBy(currentName());
        p.setArchiveTime(LocalDateTime.now().withNano(0));
        planMapper.updateById(p);
    }

    // 盲样台账

    public PageResult<LisEqaVO.SampleVO> samplePage(LisEqaDTO.SampleQuery q) {
        LambdaQueryWrapper<BizLisEqaSample> w = new LambdaQueryWrapper<>();
        w.eq(q.getPlanId() != null, BizLisEqaSample::getPlanId, q.getPlanId())
                .eq(StringUtils.hasText(q.getPlanNo()), BizLisEqaSample::getPlanNo, tr(q.getPlanNo()))
                .like(StringUtils.hasText(q.getSampleNo()), BizLisEqaSample::getSampleNo, tr(q.getSampleNo()))
                .eq(q.getSampleSeq() != null, BizLisEqaSample::getSampleSeq, q.getSampleSeq())
                .like(StringUtils.hasText(q.getItemName()), BizLisEqaSample::getItemName, tr(q.getItemName()))
                .like(StringUtils.hasText(q.getInstrumentName()), BizLisEqaSample::getInstrumentName, tr(q.getInstrumentName()))
                .eq(q.getStatus() != null, BizLisEqaSample::getStatus, q.getStatus())
                .eq(q.getResultStatus() != null, BizLisEqaSample::getResultStatus, q.getResultStatus())
                .eq(q.getHandleStatus() != null, BizLisEqaSample::getHandleStatus, q.getHandleStatus())
                .orderByAsc(BizLisEqaSample::getSampleSeq)
                .orderByAsc(BizLisEqaSample::getItemCode)
                .orderByAsc(BizLisEqaSample::getId);
        Page<BizLisEqaSample> page = sampleMapper.selectPage(page(q.getPageNum(), q.getPageSize()), w);
        List<LisEqaVO.SampleVO> vos = new ArrayList<>();
        for (BizLisEqaSample s : page.getRecords()) {
            vos.add(toSampleVo(s));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    private LisEqaVO.SampleVO toSampleVo(BizLisEqaSample s) {
        LisEqaVO.SampleVO vo = new LisEqaVO.SampleVO();
        BeanUtils.copyProperties(s, vo);
        vo.setStatusText(dictText.text(DICT_SAMPLE_STATUS, s.getStatus()));
        vo.setResultStatusText(s.getResultStatus() == null || s.getResultStatus() == 0
                ? "未判定" : dictText.text(DICT_RESULT_STATUS, s.getResultStatus()));
        vo.setJudgeModeText(s.getJudgeMode() == null || s.getJudgeMode() == 0
                ? "—" : dictText.text(DICT_JUDGE_MODE, s.getJudgeMode()));
        vo.setHandleStatusText(handleStatusText(s.getHandleStatus(), s.getReviewBy()));
        return vo;
    }

    private String handleStatusText(Integer handleStatus, String reviewBy) {
        String txt;
        if (handleStatus == null || handleStatus == 0) {
            txt = "无需整改";
        } else if (handleStatus == 1) {
            txt = "待整改";
        } else {
            txt = "已整改";
        }
        return StringUtils.hasText(reviewBy) ? txt + "·已复核" : txt;
    }

    @Transactional(rollbackFor = Exception.class)
    public String sampleUpsert(LisEqaDTO.SampleUpsert dto) {
        BizLisEqaPlan plan = requirePlan(dto.getPlanId());
        assertEditable(plan);
        BizLisEqaSample s;
        if (dto.getId() == null) {
            String instrument = instrumentOf(dto.getInstrumentName());
            long dup = sampleMapper.selectCount(new LambdaQueryWrapper<BizLisEqaSample>()
                    .eq(BizLisEqaSample::getPlanId, dto.getPlanId())
                    .eq(BizLisEqaSample::getSampleSeq, dto.getSampleSeq())
                    .eq(BizLisEqaSample::getItemCode, tr(dto.getItemCode()))
                    .eq(BizLisEqaSample::getInstrumentName, instrument));
            if (dup > 0) {
                throw new BusinessException("该批次第 " + dto.getSampleSeq() + " 号样品的「"
                        + tr(dto.getItemName()) + "」在仪器「" + (instrument.isEmpty() ? "未指定" : instrument)
                        + "」上已登记");
            }
            s = new BizLisEqaSample();
            BeanUtils.copyProperties(dto, s);
            s.setId(null);
            s.setItemCode(tr(dto.getItemCode()));
            s.setInstrumentName(instrument);
            s.setPlanNo(plan.getPlanNo());
            s.setStatus(0);
            s.setResultStatus(0);
            s.setHandleStatus(0);
            s.setRemark(clip(dto.getRemark()));
            sampleMapper.insert(s);
            // 登记了盲样就开始算，批次状态推进到「检测中」
            if (plan.getStatus() == null || plan.getStatus() == 1) {
                plan.setStatus(2);
                planMapper.updateById(plan);
            }
        } else {
            s = requireSample(dto.getId());
            if (s.getStatus() != null && s.getStatus() >= SAMPLE_REPORTED) {
                throw new BusinessException("盲样已上报/回报（" + s.getSampleNo() + "），不允许修改台账");
            }
            BeanUtils.copyProperties(dto, s);
            s.setId(dto.getId());
            s.setItemCode(tr(dto.getItemCode()));
            s.setInstrumentName(instrumentOf(dto.getInstrumentName()));
            s.setPlanNo(plan.getPlanNo());
            s.setRemark(clip(dto.getRemark()));
            sampleMapper.updateById(s);
        }
        return s.getSampleNo();
    }

    /**
     * 批量生成盲样台账骨架（样品序号 × 项目 × 仪器）。
     *
     * @return 实际新增行数（已存在的组合跳过，不覆盖 —— 覆盖会把已录的检测值抹掉）
     */
    @Transactional(rollbackFor = Exception.class)
    public int sampleGenerate(LisEqaDTO.SampleGenerate dto) {
        BizLisEqaPlan plan = requirePlan(dto.getPlanId());
        assertEditable(plan);
        List<String> instruments = new ArrayList<>();
        Set<String> seenInstrument = new HashSet<>();
        if (dto.getInstruments() != null) {
            for (String s : dto.getInstruments()) {
                String v = instrumentOf(s);
                if (seenInstrument.add(v)) {
                    instruments.add(v);
                }
            }
        }
        if (instruments.isEmpty()) {
            instruments.add("");
        }
        Set<Integer> seqs = new HashSet<>(dto.getSampleSeqs());
        int created = 0;
        for (Integer seq : seqs) {
            for (LisEqaDTO.ItemRow item : dto.getItems()) {
                for (String instr : instruments) {
                    long dup = sampleMapper.selectCount(new LambdaQueryWrapper<BizLisEqaSample>()
                            .eq(BizLisEqaSample::getPlanId, plan.getId())
                            .eq(BizLisEqaSample::getSampleSeq, seq)
                            .eq(BizLisEqaSample::getItemCode, tr(item.getItemCode()))
                            .eq(BizLisEqaSample::getInstrumentName, instr));
                    if (dup > 0) {
                        continue;
                    }
                    BizLisEqaSample s = new BizLisEqaSample();
                    s.setPlanId(plan.getId());
                    s.setPlanNo(plan.getPlanNo());
                    s.setSampleNo(dto.getSampleNoPrefix().trim() + "-" + seq);
                    s.setSampleSeq(seq);
                    s.setItemCode(tr(item.getItemCode()));
                    s.setItemName(item.getItemName().trim());
                    s.setInstrumentName(instr);
                    s.setMethodName(StringUtils.hasText(dto.getMethodName()) ? dto.getMethodName().trim() : null);
                    s.setReceiveDate(dto.getReceiveDate());
                    s.setReceiveBy(StringUtils.hasText(dto.getReceiveBy()) ? dto.getReceiveBy().trim() : currentName());
                    s.setStatus(0);
                    s.setResultStatus(0);
                    s.setHandleStatus(0);
                    sampleMapper.insert(s);
                    created++;
                }
            }
        }
        if (created > 0 && (plan.getStatus() == null || plan.getStatus() == 1)) {
            plan.setStatus(2);
            planMapper.updateById(plan);
        }
        return created;
    }

    @Transactional(rollbackFor = Exception.class)
    public void sampleDeleteById(Long id) {
        BizLisEqaSample s = requireSample(id);
        if (s.getStatus() != null && s.getStatus() >= SAMPLE_REPORTED) {
            throw new BusinessException("盲样已上报/回报（" + s.getSampleNo() + "），不允许删除");
        }
        sampleMapper.purgeById(id);
    }

    // 本室检测 / 上报 / 成绩回报

    @Transactional(rollbackFor = Exception.class)
    public LisEqaVO.SampleVO test(LisEqaDTO.TestInput dto) {
        BizLisEqaSample s = requireSample(dto.getSampleId());
        BizLisEqaPlan plan = requirePlan(s.getPlanId());
        assertEditable(plan);
        if (s.getStatus() != null && s.getStatus() >= SAMPLE_RETURNED) {
            throw new BusinessException("该盲样成绩已回报（" + s.getSampleNo() + "），检测结果不允许再改");
        }
        s.setTestValue(dto.getTestValue());
        s.setTestBy(currentName());
        s.setTestTime(LocalDateTime.now().withNano(0));
        if (StringUtils.hasText(dto.getRemark())) {
            s.setRemark(clip(dto.getRemark()));
        }
        s.setStatus(SAMPLE_TESTED);
        sampleMapper.updateById(s);
        return toSampleVo(s);
    }

    /**
     * 向组织方上报结果。
     *
     * <p>过了截止日不拦 —— 数据该报还是得报，但会落 overdue_flag 并计入台账,
     * 评审查的是"是否按期"，把逾期记录偷偷吞掉才是事故。
     */
    @Transactional(rollbackFor = Exception.class)
    public String report(LisEqaDTO.ReportInput dto) {
        BizLisEqaPlan plan = requirePlan(dto.getPlanId());
        if (plan.getStatus() != null && plan.getStatus() >= PLAN_RETURNED) {
            throw new BusinessException("批次已回报成绩（" + plan.getPlanNo() + "），不可再上报");
        }
        LocalDate deadline = plan.getReportDeadline();
        int n = 0;
        int overdue = 0;
        for (Long id : dto.getSampleIds()) {
            BizLisEqaSample s = requireSample(id);
            if (s.getPlanId() == null || !plan.getId().equals(s.getPlanId())) {
                throw new BusinessException("盲样 " + s.getSampleNo() + " 不属于批次 " + plan.getPlanNo());
            }
            if (s.getStatus() != null && s.getStatus() >= SAMPLE_REPORTED) {
                continue; // 幂等：重复上报跳过
            }
            if (s.getTestValue() == null) {
                throw new BusinessException("盲样 " + s.getSampleNo() + "（" + s.getItemName()
                        + "）尚未录入检测结果，不能上报");
            }
            boolean od = deadline != null && LocalDate.now().isAfter(deadline);
            s.setStatus(SAMPLE_REPORTED);
            s.setOverdueFlag(od ? 1 : 0);
            sampleMapper.updateById(s);
            n++;
            if (od) {
                overdue++;
            }
        }
        long remain = sampleMapper.selectCount(new LambdaQueryWrapper<BizLisEqaSample>()
                .eq(BizLisEqaSample::getPlanId, plan.getId())
                .lt(BizLisEqaSample::getStatus, SAMPLE_REPORTED));
        if (remain == 0 && plan.getStatus() != null && plan.getStatus() < PLAN_REPORTED) {
            plan.setStatus(PLAN_REPORTED);
            planMapper.updateById(plan);
        }
        return n + " 项已上报" + (overdue > 0 ? "，其中 " + overdue + " 项逾期（截止日 " + deadline + "）" : "");
    }

    /**
     * 成绩回报（组织方把靶值发回来）→ 服务端判定 + 重算 PT 得分 + 重算仪器间互差。
     */
    @Transactional(rollbackFor = Exception.class)
    public LisEqaVO.JudgeVO returnScore(LisEqaDTO.ScoreReturn dto) {
        BizLisEqaPlan plan = requirePlan(dto.getPlanId());
        if (plan.getStatus() != null && plan.getStatus() == PLAN_ARCHIVED) {
            throw new BusinessException("批次已归档（" + plan.getPlanNo() + "），不可再回报成绩");
        }
        for (LisEqaDTO.ScoreRow row : dto.getRows()) {
            BizLisEqaSample s = requireSample(row.getSampleId());
            if (s.getPlanId() == null || !plan.getId().equals(s.getPlanId())) {
                throw new BusinessException("盲样 " + s.getSampleNo() + " 不属于批次 " + plan.getPlanNo());
            }
            EqaJudgeEngine.Verdict v = EqaJudgeEngine.judge(s.getTestValue(), row.getTargetValue(),
                    row.getGroupSd(), row.getTea(), row.getTargetMin(), row.getTargetMax());
            s.setTargetValue(row.getTargetValue());
            s.setGroupSd(row.getGroupSd());
            s.setTea(row.getTea());
            s.setTargetMin(row.getTargetMin());
            s.setTargetMax(row.getTargetMax());
            s.setSdi(v.getSdi());
            s.setBiasRate(v.getBiasRate());
            s.setJudgeMode(v.getJudgeMode());
            s.setResultStatus(v.getResultStatus());
            s.setStatus(SAMPLE_RETURNED);
            if (v.getResultStatus() == EqaJudgeEngine.FAILED) {
                s.setHandleStatus(1);
            } else if (s.getHandleStatus() == null || s.getHandleStatus() == 1) {
                // 回报数据修正后转合格的：撤销整改要求；已经整改过的记录留着当档案
                s.setHandleStatus(0);
            }
            sampleMapper.updateById(s);
        }
        plan.setReturnDate(dto.getReturnDate() == null ? LocalDate.now() : dto.getReturnDate());
        recalcPlanStat(plan);
        long remain = sampleMapper.selectCount(new LambdaQueryWrapper<BizLisEqaSample>()
                .eq(BizLisEqaSample::getPlanId, plan.getId())
                .lt(BizLisEqaSample::getStatus, SAMPLE_RETURNED));
        if (remain == 0) {
            plan.setStatus(PLAN_RETURNED);
        } else if (plan.getStatus() == null || plan.getStatus() < PLAN_REPORTED) {
            plan.setStatus(PLAN_REPORTED);
        }
        planMapper.updateById(plan);

        rebuildCompare(plan.getId());

        LisEqaVO.JudgeVO vo = new LisEqaVO.JudgeVO();
        vo.setPlanNo(plan.getPlanNo());
        vo.setReturnedCount(dto.getRows().size());
        vo.setPassCount(passCountOf(plan.getId()));
        vo.setFailCount(plan.getFailCount() == null ? 0 : plan.getFailCount());
        vo.setPtScore(plan.getPtScore());
        vo.setPassFlag(plan.getPassFlag());
        vo.setPassFlagText(plan.getPassFlag() == null ? "未出成绩" : (plan.getPassFlag() == 1 ? "合格" : "不合格"));
        vo.setCompareCount(compareMapper.selectCount(new LambdaQueryWrapper<BizLisEqaCompare>()
                .eq(BizLisEqaCompare::getPlanId, plan.getId())).intValue());
        vo.setCompareFailedCount(compareMapper.selectCount(new LambdaQueryWrapper<BizLisEqaCompare>()
                .eq(BizLisEqaCompare::getPlanId, plan.getId())
                .eq(BizLisEqaCompare::getStatus, 2)).intValue());
        return vo;
    }

    /** 重算批次的科目数 / 样品数 / 不合格数 / PT 得分 / 合格标志 */
    private void recalcPlanStat(BizLisEqaPlan plan) {
        List<BizLisEqaSample> all = sampleMapper.selectList(new LambdaQueryWrapper<BizLisEqaSample>()
                .eq(BizLisEqaSample::getPlanId, plan.getId()));
        int judged = 0;
        int pass = 0;
        int fail = 0;
        Set<String> items = new HashSet<>();
        int maxSeq = 0;
        int pending = 0;
        for (BizLisEqaSample s : all) {
            if (StringUtils.hasText(s.getItemCode())) {
                items.add(s.getItemCode());
            }
            if (s.getSampleSeq() != null && s.getSampleSeq() > maxSeq) {
                maxSeq = s.getSampleSeq();
            }
            if (s.getStatus() == null || s.getStatus() < SAMPLE_RETURNED) {
                pending++;
            }
            Integer rs = s.getResultStatus();
            if (rs == null || rs == 0) {
                continue;
            }
            judged++;
            if (rs == EqaJudgeEngine.SATISFACTORY || rs == EqaJudgeEngine.ACCEPTABLE) {
                pass++;
            } else {
                fail++;
            }
        }
        plan.setItemCount(items.size());
        plan.setSampleCount(maxSeq);
        plan.setFailCount(fail);
        plan.setPtScore(EqaJudgeEngine.ptScore(judged, pass));
        plan.setPassFlag(judged > 0 ? (EqaJudgeEngine.ptPass(plan.getPtScore()) ? 1 : 0) : null);
    }

    private int passCountOf(Long planId) {
        List<BizLisEqaSample> all = sampleMapper.selectList(new LambdaQueryWrapper<BizLisEqaSample>()
                .eq(BizLisEqaSample::getPlanId, planId));
        int pass = 0;
        for (BizLisEqaSample s : all) {
            Integer rs = s.getResultStatus();
            if (rs != null && (rs == EqaJudgeEngine.SATISFACTORY || rs == EqaJudgeEngine.ACCEPTABLE)) {
                pass++;
            }
        }
        return pass;
    }

    // 室间差（仪器间比对）

    /**
     * 重算本批次的仪器间互差：物理删后按「同项目 + 同样品」两两重插。
     *
     * <p>为什么删了重插而不是增量更新：(批次, 样品序号, 项目, 仪器A, 仪器B) 唯一键不含 del_flag，
     * 软删会留下占位行导致下一次重算撞键；而且已经不存在于台账的组合本来就不该留在这个表里。
     */
    @Transactional(rollbackFor = Exception.class)
    public void rebuildCompare(Long planId) {
        BizLisEqaPlan plan = requirePlan(planId);
        compareMapper.purgeByPlanId(planId);
        LambdaQueryWrapper<BizLisEqaSample> cw = new LambdaQueryWrapper<>();
        cw.eq(BizLisEqaSample::getPlanId, planId)
                .ge(BizLisEqaSample::getStatus, SAMPLE_RETURNED)
                .isNotNull(BizLisEqaSample::getTestValue);
        List<BizLisEqaSample> rows = sampleMapper.selectList(cw);
        Map<String, List<BizLisEqaSample>> groups = new LinkedHashMap<>();
        for (BizLisEqaSample s : rows) {
            groups.computeIfAbsent(s.getItemCode() + "#" + s.getSampleSeq(), k -> new ArrayList<>()).add(s);
        }
        for (Map.Entry<String, List<BizLisEqaSample>> e : groups.entrySet()) {
            List<BizLisEqaSample> g = e.getValue();
            for (int i = 0; i < g.size(); i++) {
                for (int j = i + 1; j < g.size(); j++) {
                    insertCompare(plan, g.get(i), g.get(j));
                }
            }
        }
    }

    private void insertCompare(BizLisEqaPlan plan, BizLisEqaSample a, BizLisEqaSample b) {
        BigDecimal rate = EqaJudgeEngine.diffRate(a.getTestValue(), b.getTestValue());
        BigDecimal tea = a.getTea() != null ? a.getTea() : b.getTea();
        Integer source = tea != null && tea.compareTo(BigDecimal.ZERO) > 0 ? 1 : 2;
        BigDecimal allow = EqaJudgeEngine.allowRate(tea);
        int status = (rate == null || rate.compareTo(allow) > 0) ? 2 : 1;

        BizLisEqaCompare c = new BizLisEqaCompare();
        c.setPlanId(plan.getId());
        c.setPlanNo(plan.getPlanNo());
        c.setItemCode(a.getItemCode());
        c.setItemName(a.getItemName());
        c.setSampleSeq(a.getSampleSeq());
        c.setSampleNo(a.getSampleNo());
        c.setInstrumentA(a.getInstrumentName());
        c.setMethodA(a.getMethodName());
        c.setValueA(a.getTestValue());
        c.setInstrumentB(b.getInstrumentName());
        c.setMethodB(b.getMethodName());
        c.setValueB(b.getTestValue());
        c.setDiffValue(a.getTestValue().subtract(b.getTestValue()).abs().setScale(4, RoundingMode.HALF_UP));
        c.setDiffRate(rate);
        c.setAllowRate(allow);
        c.setAllowSource(source);
        c.setStatus(status);
        compareMapper.insert(c);
    }

    public PageResult<LisEqaVO.CompareVO> comparePage(LisEqaDTO.CompareQuery q) {
        LambdaQueryWrapper<BizLisEqaCompare> w = new LambdaQueryWrapper<>();
        w.eq(q.getPlanId() != null, BizLisEqaCompare::getPlanId, q.getPlanId())
                .like(StringUtils.hasText(q.getItemName()), BizLisEqaCompare::getItemName, tr(q.getItemName()))
                .eq(q.getStatus() != null, BizLisEqaCompare::getStatus, q.getStatus())
                .orderByDesc(BizLisEqaCompare::getId);
        Page<BizLisEqaCompare> page = compareMapper.selectPage(page(q.getPageNum(), q.getPageSize()), w);
        List<LisEqaVO.CompareVO> vos = new ArrayList<>();
        for (BizLisEqaCompare c : page.getRecords()) {
            LisEqaVO.CompareVO vo = new LisEqaVO.CompareVO();
            BeanUtils.copyProperties(c, vo);
            vo.setStatusText(dictText.text(DICT_COMPARE_STATUS, c.getStatus()));
            vo.setAllowSourceText(c.getAllowSource() != null && c.getAllowSource() == 1
                    ? "TEa 折半" : "默认值 " + EqaJudgeEngine.DEFAULT_COMPARE_ALLOW + "%");
            vos.add(vo);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    // 不合格整改闭环

    @Transactional(rollbackFor = Exception.class)
    public void rectify(LisEqaDTO.Rectify dto) {
        BizLisEqaSample s = requireSample(dto.getSampleId());
        if (s.getResultStatus() == null || s.getResultStatus() != EqaJudgeEngine.FAILED) {
            throw new BusinessException("仅「不合格」项需要整改（当前："
                    + dictText.text(DICT_RESULT_STATUS, s.getResultStatus()) + "）");
        }
        if (s.getHandleStatus() != null && s.getHandleStatus() == 2) {
            throw new BusinessException("该项已整改，不可重复提交");
        }
        s.setHandleStatus(2);
        s.setHandleCause(clip(dto.getHandleCause()));
        s.setHandleMeasure(clip(dto.getHandleMeasure()));
        s.setHandleBy(currentName());
        s.setHandleTime(LocalDateTime.now().withNano(0));
        sampleMapper.updateById(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public void rectifyReview(LisEqaDTO.RectifyReview dto) {
        BizLisEqaSample s = requireSample(dto.getSampleId());
        if (s.getResultStatus() == null || s.getResultStatus() != EqaJudgeEngine.FAILED) {
            throw new BusinessException("仅「不合格」项需要复核");
        }
        if (s.getHandleStatus() == null || s.getHandleStatus() != 2) {
            throw new BusinessException("复核前必须先完成整改（当前：" + handleStatusText(s.getHandleStatus(), s.getReviewBy()) + "）");
        }
        String who = currentName();
        if (who.equals(s.getHandleBy())) {
            throw new BusinessException("复核人不得是整改人本人（" + who + "）——纠正是否到位必须第二人确认");
        }
        s.setReviewBy(who);
        s.setReviewTime(LocalDateTime.now().withNano(0));
        sampleMapper.updateById(s);
    }

    // 统计

    public LisEqaVO.StatsVO stats() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        LisEqaVO.StatsVO vo = new LisEqaVO.StatsVO();
        vo.setThisYear(year);
        vo.setActivePlanCount(planMapper.selectCount(new LambdaQueryWrapper<BizLisEqaPlan>()
                .lt(BizLisEqaPlan::getStatus, PLAN_ARCHIVED)));
        vo.setPendingReturnCount(planMapper.selectCount(new LambdaQueryWrapper<BizLisEqaPlan>()
                .eq(BizLisEqaPlan::getStatus, PLAN_REPORTED)));
        vo.setPendingRectifyCount(sampleMapper.selectCount(new LambdaQueryWrapper<BizLisEqaSample>()
                .eq(BizLisEqaSample::getHandleStatus, 1)));
        vo.setDueSoonCount(planMapper.selectCount(new LambdaQueryWrapper<BizLisEqaPlan>()
                .in(BizLisEqaPlan::getStatus, 1, 2)
                .isNotNull(BizLisEqaPlan::getReportDeadline)
                .le(BizLisEqaPlan::getReportDeadline, today.plusDays(7))));

        List<BizLisEqaPlan> yearPlans = planMapper.selectList(new LambdaQueryWrapper<BizLisEqaPlan>()
                .eq(BizLisEqaPlan::getPlanYear, year));
        List<Long> planIds = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        int scored = 0;
        for (BizLisEqaPlan p : yearPlans) {
            planIds.add(p.getId());
            if (p.getPtScore() != null) {
                sum = sum.add(p.getPtScore());
                scored++;
            }
        }
        vo.setYearScoredCount(scored);
        vo.setYearAvgScore(scored == 0 ? null : sum.divide(BigDecimal.valueOf(scored), 1, RoundingMode.HALF_UP));
        if (planIds.isEmpty()) {
            vo.setYearFailCount(0);
        } else {
            vo.setYearFailCount(sampleMapper.selectCount(new LambdaQueryWrapper<BizLisEqaSample>()
                    .in(BizLisEqaSample::getPlanId, planIds)
                    .eq(BizLisEqaSample::getResultStatus, EqaJudgeEngine.FAILED)));
        }
        return vo;
    }

    // 内部

    @SuppressWarnings("unchecked")
    private <T> Page<T> page(Integer pageNum, Integer pageSize) {
        return (Page<T>) new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 20 : pageSize);
    }

    private BizLisEqaPlan requirePlan(Long id) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (id == null) {
            throw new BusinessException("质评批次ID不能为空");
        }
        BizLisEqaPlan p = planMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("质评批次不存在：" + id);
        }
        return p;
    }

    private BizLisEqaSample requireSample(Long id) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (id == null) {
            throw new BusinessException("盲样台账ID不能为空");
        }
        BizLisEqaSample s = sampleMapper.selectById(id);
        if (s == null) {
            throw new BusinessException("盲样台账不存在：" + id);
        }
        return s;
    }

    /** 归档后整条链都不许动：台账的价值就在于它是当时事实，可以改的台账没人信 */
    private void assertEditable(BizLisEqaPlan plan) {
        if (plan.getStatus() != null && plan.getStatus() >= PLAN_RETURNED) {
            throw new BusinessException("批次「" + plan.getPlanNo() + "」当前为「"
                    + dictText.text(DICT_PLAN_STATUS, plan.getStatus()) + "」，盲样台账不可再变更");
        }
    }

    /** 仪器名统一：不填就是空串（列上 NOT NULL DEFAULT ''，让唯一键能生效） */
    private String instrumentOf(String raw) {
        return StringUtils.hasText(raw) ? raw.trim() : "";
    }

    private String nextPlanNo(Integer year, Integer batch) {
        String base = "EQA" + year + "-" + (batch == null ? "0" : String.format("%02d", batch));
        for (int i = 0; i < 200; i++) {
            String no = i == 0 ? base : base + "-" + (i + 1);
            Long c = planMapper.selectCount(new LambdaQueryWrapper<BizLisEqaPlan>()
                    .eq(BizLisEqaPlan::getPlanNo, no));
            if (c == null || c == 0) {
                return no;
            }
        }
        return base + "-" + System.currentTimeMillis() % 100000;
    }

    private String currentName() {
        String n = UserUtils.getCurrentEmployeeName();
        return StringUtils.hasText(n) ? n : "未知操作人";
    }

    private String clip(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 480 ? s.substring(0, 480) : s;
    }

    private String tr(String s) {
        return s == null ? null : s.trim();
    }
}
