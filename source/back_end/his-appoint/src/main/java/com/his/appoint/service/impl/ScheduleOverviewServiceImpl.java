package com.his.appoint.service.impl;

import com.his.appoint.mapper.BizScheduleMapper;
import com.his.appoint.service.ScheduleOverviewService;
import com.his.appoint.vo.*;
import com.his.common.enums.DutyShiftTypeEnum;
import com.his.system.service.DutyRosterService;
import com.his.system.service.StaffPlanRuleService;
import com.his.system.service.StaffScheduleService;
import com.his.system.vo.StaffShortfallVO;
import com.his.system.vo.UnitDayWorkingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 排班周总览实现：跨模块只读 his-system 的 service（事实层 / 人力标准 / 总值班），
 * 号源聚合读本模块表。总览不写任何事实，也不在此处做范围收口以外的业务判断。
 */
@Service
@RequiredArgsConstructor
public class ScheduleOverviewServiceImpl implements ScheduleOverviewService {

    private static final int WEEK_DAYS = 7;

    private final StaffScheduleService staffScheduleService;
    private final StaffPlanRuleService staffPlanRuleService;
    private final DutyRosterService dutyRosterService;
    private final BizScheduleMapper scheduleMapper;

    @Override
    public ScheduleOverviewVO overviewWeek(LocalDate beginDate) {
        // 任意一天归一到 ISO 周一：with(DayOfWeek.MONDAY) 在 ISO 周制下就是回到本周一
        LocalDate begin = (beginDate != null ? beginDate : LocalDate.now()).with(DayOfWeek.MONDAY);
        LocalDate end = begin.plusDays(WEEK_DAYS - 1L);

        ScheduleOverviewVO vo = new ScheduleOverviewVO();
        vo.setBeginDate(begin);
        vo.setDays(WEEK_DAYS);
        vo.setStaffTypeDays(toStaffTypeDays(staffScheduleService.listStaffTypeDayWorking(begin, end)));
        vo.setUnitDays(toUnitDays(staffScheduleService.listUnitDayWorking(begin, end)));
        vo.setClinicDays(toClinicDays(scheduleMapper.summaryByDay(begin, end)));
        vo.setShortfalls(toShortfalls(staffPlanRuleService.listShortfalls(begin, end)));
        vo.setDutyDays(dutyDays(begin));
        return vo;
    }

    private List<OverviewStaffTypeDayVO> toStaffTypeDays(List<com.his.system.vo.StaffTypeDayWorkingVO> list) {
        List<OverviewStaffTypeDayVO> vos = new ArrayList<>(list.size());
        for (com.his.system.vo.StaffTypeDayWorkingVO row : list) {
            OverviewStaffTypeDayVO vo = new OverviewStaffTypeDayVO();
            vo.setStaffType(row.getStaffType());
            vo.setScheduleDate(row.getScheduleDate());
            vo.setWorkingCount(row.getWorkingCount());
            vos.add(vo);
        }
        return vos;
    }

    private List<OverviewUnitDayVO> toUnitDays(List<UnitDayWorkingVO> list) {
        List<OverviewUnitDayVO> vos = new ArrayList<>(list.size());
        for (UnitDayWorkingVO row : list) {
            OverviewUnitDayVO vo = new OverviewUnitDayVO();
            vo.setOrgType(row.getOrgType());
            vo.setOrgId(row.getOrgId());
            vo.setOrgName(row.getOrgName());
            vo.setScheduleDate(row.getScheduleDate());
            vo.setWorkingCount(row.getWorkingCount());
            vos.add(vo);
        }
        return vos;
    }

    private List<OverviewClinicDayVO> toClinicDays(List<Map<String, Object>> rows) {
        List<OverviewClinicDayVO> vos = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            OverviewClinicDayVO vo = new OverviewClinicDayVO();
            vo.setScheduleDate(LocalDate.parse(String.valueOf(row.get("scheduleDate"))));
            vo.setShiftCount(asLong(row.get("shiftCount")));
            vo.setTotalSource(asDecimal(row.get("totalSource")));
            vo.setUsedSource(asDecimal(row.get("usedSource")));
            vo.setStoppedCount(asLong(row.get("stoppedCount")));
            vos.add(vo);
        }
        return vos;
    }

    private List<OverviewShortfallVO> toShortfalls(List<StaffShortfallVO> list) {
        List<OverviewShortfallVO> vos = new ArrayList<>(list.size());
        for (StaffShortfallVO row : list) {
            OverviewShortfallVO vo = new OverviewShortfallVO();
            vo.setScheduleDate(row.getScheduleDate());
            vo.setOrgType(row.getOrgType());
            vo.setOrgId(row.getOrgId());
            vo.setOrgName(row.getOrgName());
            vo.setShiftName(row.getShiftName());
            vo.setStaffTypeName(com.his.common.enums.StaffTypeEnum.getText(row.getStaffType()));
            vo.setMinStaff(row.getMinStaff());
            vo.setActualCount(row.getActualCount());
            vo.setShortfall(row.getShortfall());
            vos.add(vo);
        }
        vos.sort(Comparator.comparing(OverviewShortfallVO::getScheduleDate)
                .thenComparing(OverviewShortfallVO::getOrgType)
                .thenComparing(OverviewShortfallVO::getOrgId));
        return vos;
    }

    /**
     * 本周每日总值班（白班/夜班，主班优先副班顶上）。
     * 复用 {@link DutyRosterService#officerOf} 的解析口径：漏排返回 found=0，
     * 总览如实展示「哪天没人值班」，不替业务兜底。
     */
    private List<OverviewDutyDayVO> dutyDays(LocalDate begin) {
        List<OverviewDutyDayVO> vos = new ArrayList<>(WEEK_DAYS * DutyShiftTypeEnum.values().length);
        for (int i = 0; i < WEEK_DAYS; i++) {
            LocalDate date = begin.plusDays(i);
            for (DutyShiftTypeEnum shift : DutyShiftTypeEnum.values()) {
                com.his.system.vo.DutyOfficerVO officer = dutyRosterService.officerOf(date, shift.getCode());
                OverviewDutyDayVO vo = new OverviewDutyDayVO();
                vo.setDutyDate(date);
                vo.setShiftType(shift.getCode());
                vo.setShiftTypeText(shift.getLabel());
                vo.setFound(officer.getFound());
                // officerOf 的解析口径本身就是「换班优先」：employeeName 已是实际值班人
                vo.setActualEmpName(officer.getEmployeeName());
                vo.setEmptyReason(officer.getEmptyReason());
                vos.add(vo);
            }
        }
        return vos;
    }

    private Long asLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private BigDecimal asDecimal(Object value) {
        if (value == null) {
            return null;
        }
        return value instanceof BigDecimal ? (BigDecimal) value : BigDecimal.valueOf(((Number) value).longValue());
    }
}
