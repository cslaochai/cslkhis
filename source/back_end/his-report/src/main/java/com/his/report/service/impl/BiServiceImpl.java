package com.his.report.service.impl;

import com.his.report.service.BiService;
import com.his.report.mapper.BiMapper;
import com.his.report.vo.BiNationalVO;
import com.his.report.vo.BiOverviewVO;
import com.his.report.support.DrgGrouper;
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

        Map<String, Object> bed = biMapper.bedStat();
        long repair = bed.get("repair") == null ? 0 : ((Number) bed.get("repair")).longValue();
        long occupied = bed.get("occupied") == null ? 0 : ((Number) bed.get("occupied")).longValue();
        long total = bed.get("total") == null ? 0 : ((Number) bed.get("total")).longValue();
        long usable = total - repair;
        vo.setBedTotal(total);
        vo.setBedOccupied(occupied);
        vo.setBedOccupancy(usable > 0
                ? BigDecimal.valueOf(occupied).divide(BigDecimal.valueOf(usable), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        vo.setAppointmentTrend(toTrend(biMapper.appointmentTrend(), "n", null));
        vo.setRevenueTrend(toTrend(biMapper.revenueTrend(), "amt", "amt"));
        List<BiOverviewVO.DeptRevenue> top = new ArrayList<>();
        for (Map<String, Object> m : biMapper.deptTop()) {
            BiOverviewVO.DeptRevenue d = new BiOverviewVO.DeptRevenue();
            d.setDeptName(String.valueOf(m.get("dept_name")));
            d.setAmount(m.get("amt") == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(m.get("amt"))));
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
        Map<String, Object> d = biMapper.dischargeWindow30d();
        long dischargeCount = d.get("discharge_count") == null ? 0 : ((Number) d.get("discharge_count")).longValue();
        long bedDays = d.get("bed_days") == null ? 0 : new BigDecimal(String.valueOf(d.get("bed_days"))).longValue();
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
        for (Map<String, Object> g : biMapper.drgWeights()) {
            weightByCode.put(String.valueOf(g.get("drg_code")),
                    g.get("weight") == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(g.get("weight"))));
        }
        List<Map<String, Object>> samples = biMapper.codedSummaries30d();
        long grouped = 0;
        BigDecimal weightSum = BigDecimal.ZERO;
        for (Map<String, Object> s : samples) {
            DrgGrouper.GroupResult r = drgGrouper.group(
                    String.valueOf(s.get("icd_code")),
                    asInt(s.get("is_surgery")) == 1,
                    asInt(s.get("inpatient_days")),
                    asInt(s.get("death_flag")) == 1);
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
        Map<String, Object> bed = biMapper.bedStat();
        long repair = bed.get("repair") == null ? 0 : ((Number) bed.get("repair")).longValue();
        long total = bed.get("total") == null ? 0 : ((Number) bed.get("total")).longValue();
        return Math.max(total - repair, 0);
    }

    private static int asInt(Object v) {
        if (v == null) {
            return 0;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<BiOverviewVO.TrendPoint> toTrend(List<Map<String, Object>> rows, String countKey, String amountKey) {
        List<BiOverviewVO.TrendPoint> list = new ArrayList<>();
        for (Map<String, Object> m : rows) {
            BiOverviewVO.TrendPoint p = new BiOverviewVO.TrendPoint();
            p.setDate(String.valueOf(m.get("d")));
            p.setCount(m.get(countKey) == null ? 0L : new BigDecimal(String.valueOf(m.get(countKey))).longValue());
            p.setAmount(amountKey != null && m.get("amt") != null
                    ? new BigDecimal(String.valueOf(m.get("amt"))) : BigDecimal.ZERO);
            list.add(p);
        }
        return list;
    }

    private static long nz(Long v) {
        return v == null ? 0L : v;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
