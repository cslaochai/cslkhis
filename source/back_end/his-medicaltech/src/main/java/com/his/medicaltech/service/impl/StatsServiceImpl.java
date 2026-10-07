package com.his.medicaltech.service.impl;

import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.medicaltech.mapper.StatsMapper;
import com.his.medicaltech.service.StatsService;
import com.his.medicaltech.vo.StatsOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * 报表统计服务：全部聚合下推 SQL，服务层只做窗口兜底与比率现算。
 *
 * <p>窗口缺省 = 截至今天的近 30 天；单边缺省按另一边对齐。
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final int DEFAULT_WINDOW_DAYS = 30;
    private static final int MAX_WINDOW_DAYS = 366;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final StatsMapper statsMapper;

    /**
     * 分子/分母 → 百分比（1位小数），分母为 0 出 0
     */
    private static BigDecimal rate(Number numerator, Number denominator) {
        BigDecimal d = new BigDecimal(String.valueOf(denominator));
        if (d.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(numerator))
                .multiply(HUNDRED).divide(d, 1, RoundingMode.HALF_UP);
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), DateFormats.DATE);
        } catch (Exception ex) {
            throw new BusinessException("日期格式应为 yyyy-MM-dd: " + text);
        }
    }

    private static long z(Long v) {
        return v == null ? 0L : v;
    }

    @Override
    public StatsOverviewVO overview(String startDate, String endDate) {
        LocalDate end = parseDate(endDate);
        if (end == null) {
            end = LocalDate.now();
        }
        LocalDate start = parseDate(startDate);
        if (start == null) {
            start = end.minusDays(DEFAULT_WINDOW_DAYS - 1L);
        }
        if (start.isAfter(end)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (start.isBefore(end.minusDays(MAX_WINDOW_DAYS - 1L))) {
            throw new BusinessException("统计窗口最长支持 " + MAX_WINDOW_DAYS + " 天");
        }
        String s = start.format(DateFormats.DATE);
        String e = end.format(DateFormats.DATE);

        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setStartDate(s);
        vo.setEndDate(e);

        fillOutpatient(vo, s, e);
        fillInpatient(vo, s, e);
        fillRevenue(vo, s, e);
        fillPharmacy(vo, s, e);
        return vo;
    }

    private void fillOutpatient(StatsOverviewVO vo, String s, String e) {
        vo.setOpVisitTotal(z(statsMapper.countVisit(s, e)));
        vo.setOpRegistTotal(z(statsMapper.countRegistTotal(s, e)));
        vo.setOpRefundCount(z(statsMapper.countCancel(s, e)));
        vo.setOpFirstVisitCount(z(statsMapper.countFirstVisit(s, e)));
        vo.setOpRevisitCount(z(statsMapper.countRevisit(s, e)));
        vo.setOpInsuranceCount(z(statsMapper.countInsurance(s, e)));
        vo.setOpTrend(statsMapper.opTrend(s, e));
        vo.setOpDeptTop(statsMapper.opDeptTop(s, e));
        vo.setOpTypeDist(statsMapper.opTypeDist(s, e));
        vo.setOpSourceDist(statsMapper.opSourceDist(s, e));
        vo.setOpSettleDist(statsMapper.opSettleDist(s, e));
        vo.setOpAgeDist(statsMapper.opAgeDist(s, e));
    }

    private void fillInpatient(StatsOverviewVO vo, String s, String e) {
        vo.setIpAdmitCount(z(statsMapper.countAdmit(s, e)));
        long discharges = z(statsMapper.countDischarge(s, e));
        vo.setIpDischargeCount(discharges);
        BigDecimal bedDays = NumUtil.orZero(statsMapper.sumBedDays(s, e));
        vo.setIpBedDays(bedDays.setScale(0, RoundingMode.HALF_UP).longValue());
        vo.setIpAvgLosDays(discharges > 0
                ? bedDays.divide(BigDecimal.valueOf(discharges), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setIpInCur(z(statsMapper.countInHospitalAt(s, e)));

        StatsOverviewVO.BedStat bed = statsMapper.bedStat();
        if (bed != null) {
            long occupied = bed.getOccupied() == null ? 0L : bed.getOccupied();
            long total = bed.getTotal() == null ? 0L : bed.getTotal();
            vo.setBedOccupied(occupied);
            vo.setBedTotal(total);
            vo.setBedUseRate(rate(occupied, total));
        }

        vo.setIpTrend(statsMapper.ipTrend(s, e));
        vo.setIpDeptDist(statsMapper.ipDischargeDeptTop(s, e));
        vo.setIpSettle(statsMapper.ipSettle(s, e));
        vo.setIpInsuranceDist(statsMapper.ipInsuranceDist(s, e));
    }

    private void fillRevenue(StatsOverviewVO vo, String s, String e) {
        BigDecimal total = NumUtil.orZero(statsMapper.sumRevenue(s, e));
        BigDecimal drug = NumUtil.orZero(statsMapper.sumDrugRevenue(s, e));
        vo.setRevTotal(total);
        vo.setRevDrug(drug);
        vo.setDrugRatio(rate(drug, total));
        vo.setRevMaterial(NumUtil.orZero(statsMapper.sumMaterialRevenue(s, e)));
        vo.setRevOutpatient(NumUtil.orZero(statsMapper.sumOutpatientRevenue(s, e)));
        vo.setRevInpatient(NumUtil.orZero(statsMapper.sumInpatientRevenue(s, e)));
        vo.setRevTrend(statsMapper.revTrend(s, e));
        vo.setRevTypeDist(statsMapper.revTypeDist(s, e));
        vo.setRevDeptTop(statsMapper.revDeptTop(s, e));
        vo.setRevPayDist(statsMapper.revPayDist(s, e));
        vo.setRevRefundAmount(NumUtil.orZero(statsMapper.sumRefundAmount(s, e)));
        vo.setRevRefundCount(z(statsMapper.countRefundBill(s, e)));
    }

    private void fillPharmacy(StatsOverviewVO vo, String s, String e) {
        vo.setPhPrescCount(z(statsMapper.countPresc(s, e)));
        vo.setPhPrescOutpatient(z(statsMapper.countPrescBySource(s, e, 1)));
        vo.setPhPrescEmergency(z(statsMapper.countPrescBySource(s, e, 2)));
        vo.setPhPrescInpatient(z(statsMapper.countPrescBySource(s, e, 3)));
        vo.setPhPrescTypeDist(statsMapper.phPrescTypeDist(s, e));
        vo.setPhAuditReturnCount(z(statsMapper.sumPrescReturnCount(s, e)));
        vo.setPhDrugTop(statsMapper.phDrugTop(s, e));
        vo.setPhDispTrend(statsMapper.phDispTrend(s, e));
        StatsOverviewVO.ReturnStat ret = statsMapper.phReturnStat(s, e);
        if (ret != null) {
            vo.setPhReturnCount(ret.getCnt() == null ? 0L : ret.getCnt());
            vo.setPhReturnAmount(NumUtil.orZero(ret.getAmt()));
        }
    }
}
