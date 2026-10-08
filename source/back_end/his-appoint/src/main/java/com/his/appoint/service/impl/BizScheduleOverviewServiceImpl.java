package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.mapper.BizScheduleMapper;
import com.his.appoint.service.BizScheduleOverviewService;
import com.his.appoint.vo.*;
import com.his.common.enums.DutyShiftTypeEnum;
import com.his.system.service.DutyRosterService;
import com.his.system.service.StaffPlanRuleService;
import com.his.system.service.StaffScheduleService;
import com.his.system.vo.StaffShortfallVO;
import com.his.system.vo.UnitDayWorkingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 排班周总览实现：跨模块只读 his-system 的 service（事实层 / 人力标准 / 总值班），
 */
@Service
@RequiredArgsConstructor
public class BizScheduleOverviewServiceImpl extends ServiceImpl<BizScheduleMapper, BizSchedule> implements BizScheduleOverviewService {

    private static final int WEEK_DAYS = 7;

    private final StaffScheduleService staffScheduleService;

    private final StaffPlanRuleService staffPlanRuleService;

    private final DutyRosterService dutyRosterService;

    private final BizScheduleMapper bizScheduleMapper;

    @Override
    public ScheduleOverviewVO overviewWeek(LocalDate beginDate) {
        LocalDate begin = (beginDate != null ? beginDate : LocalDate.now()).with(DayOfWeek.MONDAY);
        LocalDate end = begin.plusDays(WEEK_DAYS - 1L);

        ScheduleOverviewVO vo = new ScheduleOverviewVO();
        vo.setBeginDate(begin);
        vo.setDays(WEEK_DAYS);
        vo.setStaffTypeDays(toStaffTypeDays(staffScheduleService.listStaffTypeDayWorking(begin, end)));
        vo.setUnitDays(toUnitDays(staffScheduleService.listUnitDayWorking(begin, end)));
        vo.setClinicDays(bizScheduleMapper.summaryByDay(begin, end));
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
                vo.setActualEmpName(officer.getEmployeeName());
                vo.setEmptyReason(officer.getEmptyReason());
                vos.add(vo);
            }
        }
        return vos;
    }
}
