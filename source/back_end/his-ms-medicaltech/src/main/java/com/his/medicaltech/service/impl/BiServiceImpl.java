package com.his.medicaltech.service.impl;

import com.his.common.util.NumUtil;
import com.his.medicaltech.mapper.BiMapper;
import com.his.medicaltech.service.BiService;
import com.his.spi.dto.DrgGroupFactsDTO;
import com.his.spi.service.DrgGroupFacadeService;
import com.his.spi.vo.DrgGroupResultVO;
import com.his.medicaltech.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BI 驾驶舱服务：只读聚合，口径注释在 SQL 上（药费=item_type 2/3/4；净额=收入-退款抵扣）。
 */
@Service
@RequiredArgsConstructor
public class BiServiceImpl implements BiService {

    private final BiMapper biMapper;
    private final DrgGroupFacadeService drgGroupFacadeService;

    public BiOverviewVO overview() {
        BiOverviewVO vo = new BiOverviewVO();
        vo.setTodayAppointments(NumUtil.orZero(biMapper.countTodayAppointments()));
        vo.setTodayAdmissions(NumUtil.orZero(biMapper.countTodayAdmissions()));
        vo.setInHospitalCount(NumUtil.orZero(biMapper.countInHospital()));
        vo.setTodayDischargeCount(NumUtil.orZero(biMapper.countTodayDischarge()));
        vo.setTodayOperations(NumUtil.orZero(biMapper.countTodayOperations()));

        BigDecimal revenue = NumUtil.orZero(biMapper.sumTodayRevenue());
        BigDecimal drug = NumUtil.orZero(biMapper.sumTodayDrugRevenue());
        vo.setTodayRevenue(revenue);
        vo.setDrugRatio(revenue.signum() > 0
                ? drug.divide(revenue, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 昨日对照（环比用）；昨日药占比与今日同口径现算
        vo.setYesterdayAppointments(NumUtil.orZero(biMapper.countYesterdayAppointments()));
        vo.setYesterdayAdmissions(NumUtil.orZero(biMapper.countYesterdayAdmissions()));
        vo.setYesterdayDischargeCount(NumUtil.orZero(biMapper.countYesterdayDischarge()));
        vo.setYesterdayOperations(NumUtil.orZero(biMapper.countYesterdayOperations()));
        BigDecimal yesterdayRevenue = NumUtil.orZero(biMapper.sumYesterdayRevenue());
        BigDecimal yesterdayDrug = NumUtil.orZero(biMapper.sumYesterdayDrugRevenue());
        vo.setYesterdayRevenue(yesterdayRevenue);
        vo.setYesterdayDrugRatio(yesterdayRevenue.signum() > 0
                ? yesterdayDrug.divide(yesterdayRevenue, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        BiBedStatRowVO bed = biMapper.bedStat();
        long occupied = NumUtil.orZero(bed.getOccupied());
        long total = NumUtil.orZero(bed.getTotal());
        long repair = NumUtil.orZero(bed.getRepair());
        long usable = total - repair;
        vo.setBedTotal(total);
        vo.setBedOccupied(occupied);
        vo.setBedOccupancy(usable > 0
                ? BigDecimal.valueOf(occupied).divide(BigDecimal.valueOf(usable), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setEmptyBeds(Math.max(total - occupied - repair, 0));
        vo.setPendingAdmissionCount(NumUtil.orZero(biMapper.countPendingAdmission()));

        vo.setAppointmentTrend(toCountTrend(biMapper.appointmentTrend()));
        vo.setRevenueTrend(toAmountTrend(biMapper.revenueTrend()));
        List<BiOverviewVO.DeptRevenue> top = new ArrayList<>();
        for (BiDeptAmountRowVO m : biMapper.deptTop()) {
            BiOverviewVO.DeptRevenue d = new BiOverviewVO.DeptRevenue();
            d.setDeptId(m.getDeptId());
            d.setDeptName(m.getDeptName());
            d.setAmount(NumUtil.orZero(m.getAmount()));
            top.add(d);
        }
        vo.setDeptTop(top);
        List<BiOverviewVO.DeptWorkload> workload = new ArrayList<>();
        for (BiDeptWorkloadRowVO m : biMapper.deptWorkloadTop()) {
            BiOverviewVO.DeptWorkload w = new BiOverviewVO.DeptWorkload();
            w.setDeptId(m.getDeptId());
            w.setDeptName(m.getDeptName());
            w.setCount(NumUtil.orZero(m.getCount()));
            workload.add(w);
        }
        vo.setDeptWorkloadTop(workload);
        vo.setRevenueStructure(biMapper.revenueStructure30d());
        return vo;
    }

    /**
     * 国考四指标（M5，近 30 日窗口）。样本一次查全（含手术/其他诊断明细拼接串），
     * 批量交给 {@link DrgGroupFacadeService} 入组（分组算法与方案版本留在分组域，整批同一份快照），
     * 院内数据量下无性能问题。
     */
    public BiNationalVO nationalMetrics() {
        BiNationalVO vo = new BiNationalVO();

        // 平均住院日 / 床位周转
        BiDischargeWindowRowVO d = biMapper.dischargeWindow30d();
        long dischargeCount = NumUtil.orZero(d.getDischargeCount());
        long bedDays = NumUtil.orZero(d.getBedDays());
        vo.setDischargeCount(dischargeCount);
        vo.setTotalBedDays(bedDays);
        vo.setAvgLengthOfStay(dischargeCount > 0
                ? BigDecimal.valueOf(bedDays).divide(BigDecimal.valueOf(dischargeCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        long usable = usableBeds();
        vo.setUsableBeds(usable);
        vo.setBedTurnover(usable > 0
                ? BigDecimal.valueOf(dischargeCount).divide(BigDecimal.valueOf(usable), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        // 耗占比
        BigDecimal revenue = NumUtil.orZero(biMapper.sumWindow30dRevenue());
        BigDecimal material = NumUtil.orZero(biMapper.sumWindow30dMaterialRevenue());
        vo.setRevenue(revenue);
        vo.setMaterialRevenue(material);
        vo.setMaterialRatio(revenue.signum() > 0
                ? material.divide(revenue, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // CMI：QY（未入组）权重按 0 计入分母，与国考口径一致
        Map<String, BigDecimal> weightByCode = new HashMap<>();
        for (BiDrgWeightRowVO g : biMapper.drgWeights()) {
            weightByCode.put(g.getDrgCode(), g.getWeight());
        }
        List<BiCodedSummaryRowVO> samples = biMapper.codedSummaries30d();
        List<DrgGroupFactsDTO> factsList = new ArrayList<>(samples.size());
        for (BiCodedSummaryRowVO s : samples) {
            DrgGroupFactsDTO facts = new DrgGroupFactsDTO();
            facts.setMainDiag(s.getIcdCode());
            // 编码串按首页原样传，归一与拆分由分组侧完成，调用方不各自抄一份比码口径
            facts.setMainOperCodes(s.getMainOperCodes());
            facts.setOtherDiagCodes(s.getOtherDiagCodes());
            facts.setOtherOperCodes(s.getOtherOperCodes());
            facts.setGender(s.getGender());
            facts.setAge(s.getAge());
            facts.setAgeUnit(s.getAgeUnit());
            facts.setAdmissionWeightG(s.getAdmissionWeightG());
            factsList.add(facts);
        }
        List<DrgGroupResultVO> groupedResults = drgGroupFacadeService.group(factsList);
        long grouped = 0;
        long weightMissing = 0;
        BigDecimal weightSum = BigDecimal.ZERO;
        for (DrgGroupResultVO r : groupedResults) {
            if (!r.isGrouped()) {
                continue;
            }
            grouped++;
            BigDecimal weight = weightByCode.get(r.getDrgCode());
            if (weight == null) {
                weightMissing++;
            } else {
                weightSum = weightSum.add(weight);
            }
        }
        vo.setCmiSampleCount((long) samples.size());
        vo.setCmiGroupedCount(grouped);
        // 只要有一份权重复不上就整指标返回 null：官方包只下发目录，权重由统筹区医保局另行制定，
        // 把缺的按 0 加进分子会得到一个「看着正常但偏低」的 CMI，比空着更坏（同 §7 不把统筹塞进优惠列）
        vo.setCmi(samples.isEmpty() || weightMissing > 0 ? null
                : weightSum.divide(BigDecimal.valueOf(samples.size()), 4, RoundingMode.HALF_UP));

        // 次均费用：门诊=门诊收入/门诊人次；住院=住院收入/窗口出院人次；分母为 0 置 0
        BiWindowRevenueRowVO window = biMapper.windowRevenue30d();
        BigDecimal outRevenue = NumUtil.orZero(window.getOutpatient());
        BigDecimal inRevenue = NumUtil.orZero(window.getInpatient());
        long outVisits = NumUtil.orZero(biMapper.countOutpatientVisits30d());
        vo.setOutpatientRevenue(outRevenue);
        vo.setOutpatientVisits(outVisits);
        vo.setOutpatientAvgCost(outVisits > 0
                ? outRevenue.divide(BigDecimal.valueOf(outVisits), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        vo.setInpatientRevenue(inRevenue);
        vo.setInpatientAvgCost(dischargeCount > 0
                ? inRevenue.divide(BigDecimal.valueOf(dischargeCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 三四级手术占比（近 30 日主手术）
        BiOpLevelRowVO op = biMapper.operationLevel30d();
        long opTotal = NumUtil.orZero(op.getTotal());
        long opLevel34 = NumUtil.orZero(op.getLevel34());
        vo.setOperationTotal(opTotal);
        vo.setOperationLevel34(opLevel34);
        vo.setLevel34Ratio(opTotal > 0
                ? BigDecimal.valueOf(opLevel34).divide(BigDecimal.valueOf(opTotal), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        // 医保收入占比（近 30 日已结算非作废账单；分母=费用总额）
        BiInsuranceRowVO ins = biMapper.insuranceSettle30d();
        BigDecimal insurance = NumUtil.orZero(ins.getInsurance());
        BigDecimal insTotal = NumUtil.orZero(ins.getTotal());
        vo.setInsuranceRevenue(insurance);
        vo.setSettleRevenue(insTotal);
        vo.setInsuranceRatio(insTotal.signum() > 0
                ? insurance.divide(insTotal, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // 质量安全：不良事件上报、院内感染已确认
        vo.setAdverseEventCount(NumUtil.orZero(biMapper.countAdverseEvent30d()));
        vo.setInfectionCount(NumUtil.orZero(biMapper.countInfection30d()));

        // 满意度：无有效答卷时均分与满意率整指标置 null（前端 —），不按 0 凑
        BiSurveyRowVO survey = biMapper.survey30d();
        long surveyCount = NumUtil.orZero(survey.getCount());
        long satisfied = NumUtil.orZero(survey.getSatisfied());
        vo.setSurveyCount(surveyCount);
        vo.setAvgScore(surveyCount > 0 ? NumUtil.orZero(survey.getAvgScore()) : null);
        vo.setSatisfiedRatio(surveyCount > 0
                ? BigDecimal.valueOf(satisfied).divide(BigDecimal.valueOf(surveyCount), 4, RoundingMode.HALF_UP)
                : null);
        return vo;
    }

    private long usableBeds() {
        BiBedStatRowVO bed = biMapper.bedStat();
        return Math.max(NumUtil.orZero(bed.getTotal()) - NumUtil.orZero(bed.getRepair()), 0);
    }

    /**
     * 挂号趋势。缺日不补零：SQL 只对有数据的日期分组，补零要知道日历起止，
     * 而 BI 总览与工作台折线图的口径本就不同（前者7 天窗口、后者含今日 7 天），
     * 交给各自调用方按需处理。
     */
    private List<BiOverviewVO.TrendPoint> toCountTrend(List<BiDayCountRowVO> rows) {
        List<BiOverviewVO.TrendPoint> list = new ArrayList<>();
        for (BiDayCountRowVO m : rows) {
            BiOverviewVO.TrendPoint p = new BiOverviewVO.TrendPoint();
            p.setDate(m.getStatDate());
            p.setCount(NumUtil.orZero(m.getCnt()));
            p.setAmount(BigDecimal.ZERO);
            list.add(p);
        }
        return list;
    }

    /**
     * 收入趋势。count 固定 0：这条曲线前端只读 amount，给个真实计数反而会让人误读成"当天笔数"。
     */
    private List<BiOverviewVO.TrendPoint> toAmountTrend(List<BiDayAmountRowVO> rows) {
        List<BiOverviewVO.TrendPoint> list = new ArrayList<>();
        for (BiDayAmountRowVO m : rows) {
            BiOverviewVO.TrendPoint p = new BiOverviewVO.TrendPoint();
            p.setDate(m.getStatDate());
            p.setCount(0L);
            p.setAmount(NumUtil.orZero(m.getAmount()));
            p.setOutpatientAmount(NumUtil.orZero(m.getOutpatientAmount()));
            p.setInpatientAmount(NumUtil.orZero(m.getInpatientAmount()));
            list.add(p);
        }
        return list;
    }
}
