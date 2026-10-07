package com.his.medicaltech.service.impl;

import com.his.medicaltech.mapper.BiMapper;
import com.his.medicaltech.service.BiService;
import com.his.medicaltech.support.DrgGrouper;
import com.his.medicaltech.vo.BiBedStatRowVO;
import com.his.medicaltech.vo.BiCodedSummaryRowVO;
import com.his.medicaltech.vo.BiDayAmountRowVO;
import com.his.medicaltech.vo.BiDayCountRowVO;
import com.his.medicaltech.vo.BiDeptAmountRowVO;
import com.his.medicaltech.vo.BiDischargeWindowRowVO;
import com.his.medicaltech.vo.BiDrgWeightRowVO;
import com.his.medicaltech.vo.BiNationalVO;
import com.his.medicaltech.vo.BiOverviewVO;
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
 * 国考四指标（M5）口径见 {@link BiNationalVO} 类注释。
 */
@Service
@RequiredArgsConstructor
public class BiServiceImpl implements BiService {

    private final BiMapper biMapper;
    private final DrgGrouper drgGrouper;

    private static long nz(Long v) {
        return v == null ? 0L : v;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public BiOverviewVO overview() {
        BiOverviewVO vo = new BiOverviewVO();
        vo.setTodayAppointments(nz(biMapper.countTodayAppointments()));
        vo.setInHospitalCount(nz(biMapper.countInHospital()));
        vo.setTodayDischargeCount(nz(biMapper.countTodayDischarge()));

        BigDecimal revenue = nz(biMapper.sumTodayRevenue());
        BigDecimal drug = nz(biMapper.sumTodayDrugRevenue());
        vo.setTodayRevenue(revenue);
        vo.setTodayDrugRevenue(drug);
        vo.setDrugRatio(revenue.signum() > 0
                ? drug.divide(revenue, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        BiBedStatRowVO bed = biMapper.bedStat();
        long occupied = nz(bed.getOccupied());
        long total = nz(bed.getTotal());
        long usable = total - nz(bed.getRepair());
        vo.setBedTotal(total);
        vo.setBedOccupied(occupied);
        vo.setBedOccupancy(usable > 0
                ? BigDecimal.valueOf(occupied).divide(BigDecimal.valueOf(usable), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        vo.setAppointmentTrend(toCountTrend(biMapper.appointmentTrend()));
        vo.setRevenueTrend(toAmountTrend(biMapper.revenueTrend()));
        List<BiOverviewVO.DeptRevenue> top = new ArrayList<>();
        for (BiDeptAmountRowVO m : biMapper.deptTop()) {
            BiOverviewVO.DeptRevenue d = new BiOverviewVO.DeptRevenue();
            d.setDeptName(m.getDeptName());
            d.setAmount(nz(m.getAmount()));
            top.add(d);
        }
        vo.setDeptTop(top);
        return vo;
    }

    /**
     * 国考四指标（M5，近 30 日窗口）。分组逐条跑 {@link DrgGrouper}（与 DRG 模拟页同一分组器，单一口径），
     * 组权重一次查全后内存映射，样本量 = 近 30 日出院且已编码首页数，院内数据量下无性能问题。
     */
    public BiNationalVO nationalMetrics() {
        BiNationalVO vo = new BiNationalVO();

        // 平均住院日 / 床位周转
        BiDischargeWindowRowVO d = biMapper.dischargeWindow30d();
        long dischargeCount = nz(d.getDischargeCount());
        long bedDays = nz(d.getBedDays());
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
        BigDecimal revenue = nz(biMapper.sumWindow30dRevenue());
        BigDecimal material = nz(biMapper.sumWindow30dMaterialRevenue());
        vo.setRevenue(revenue);
        vo.setMaterialRevenue(material);
        vo.setMaterialRatio(revenue.signum() > 0
                ? material.divide(revenue, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // CMI：QY（未入组）权重按 0 计入分母，与国考口径一致
        Map<String, BigDecimal> weightByCode = new HashMap<>();
        for (BiDrgWeightRowVO g : biMapper.drgWeights()) {
            weightByCode.put(g.getDrgCode(), nz(g.getWeight()));
        }
        List<BiCodedSummaryRowVO> samples = biMapper.codedSummaries30d();
        long grouped = 0;
        BigDecimal weightSum = BigDecimal.ZERO;
        for (BiCodedSummaryRowVO s : samples) {
            DrgGrouper.GroupResult r = drgGrouper.group(
                    s.getIcdCode(),
                    Integer.valueOf(1).equals(s.getIsSurgery()),
                    s.getInpatientDays(),
                    Integer.valueOf(1).equals(s.getDeathFlag()));
            if (!DrgGrouper.QY_CODE.equals(r.drgCode())) {
                grouped++;
                weightSum = weightSum.add(weightByCode.getOrDefault(r.drgCode(), BigDecimal.ZERO));
            }
        }
        vo.setCmiSampleCount((long) samples.size());
        vo.setCmiGroupedCount(grouped);
        vo.setCmi(!samples.isEmpty()
                ? weightSum.divide(BigDecimal.valueOf(samples.size()), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return vo;
    }

    private long usableBeds() {
        BiBedStatRowVO bed = biMapper.bedStat();
        return Math.max(nz(bed.getTotal()) - nz(bed.getRepair()), 0);
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
            p.setCount(nz(m.getCnt()));
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
            p.setAmount(nz(m.getAmount()));
            list.add(p);
        }
        return list;
    }
}
