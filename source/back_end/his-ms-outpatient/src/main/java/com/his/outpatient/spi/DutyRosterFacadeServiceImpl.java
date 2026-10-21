package com.his.outpatient.spi;

import com.his.common.enums.AttendModeEnum;
import com.his.outpatient.service.BizClinicSourceService;
import com.his.outpatient.service.DutyRosterService;
import com.his.outpatient.service.StaffScheduleService;
import com.his.outpatient.vo.DutyOfficerVO;
import com.his.outpatient.vo.OnDutyStaffVO;
import com.his.outpatient.vo.StaffScheduleVO;
import com.his.spi.service.DutyRosterFacadeService;
import com.his.spi.vo.DutyOfficerBriefVO;
import com.his.spi.vo.DutyStaffBriefVO;
import com.his.spi.vo.StaffScheduleBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 值班表端口实现：转调排班域值班表服务，只交出当班人的身份摘要。
 */
@Service
@RequiredArgsConstructor
public class DutyRosterFacadeServiceImpl implements DutyRosterFacadeService {

    private final DutyRosterService dutyRosterService;
    private final StaffScheduleService staffScheduleService;
    private final BizClinicSourceService bizClinicSourceService;

    @Override
    public DutyOfficerBriefVO current() {
        DutyOfficerVO officer = dutyRosterService.current();
        if (officer == null) {
            return null;
        }
        DutyOfficerBriefVO brief = new DutyOfficerBriefVO();
        brief.setEmployeeId(officer.getEmployeeId());
        brief.setEmployeeName(officer.getEmployeeName());
        return brief;
    }

    @Override
    public List<StaffScheduleBriefVO> listOnDutyStaff(LocalDateTime at, Integer orgType, Long orgId, Integer staffType) {
        LocalDateTime moment = at != null ? at : LocalDateTime.now();
        List<StaffScheduleVO> rows = staffScheduleService.onDutyAt(moment, orgType, orgId, staffType);
        List<StaffScheduleBriefVO> result = new ArrayList<>(rows.size());
        for (StaffScheduleVO row : rows) {
            // 过滤听班 ON_CALL：待命响应、呼叫到岗，不计入此刻在岗派单名单
            Integer attendMode = row.getAttendMode();
            if (attendMode != null && attendMode == AttendModeEnum.ON_CALL.getCode()) {
                continue;
            }
            StaffScheduleBriefVO brief = new StaffScheduleBriefVO();
            brief.setId(row.getId());
            brief.setScheduleDate(row.getScheduleDate());
            brief.setEmployeeId(row.getEmployeeId());
            brief.setEmployeeName(row.getEmployeeName());
            brief.setShiftId(row.getShiftId());
            brief.setStartTime(row.getStartTime());
            brief.setEndTime(row.getEndTime());
            brief.setStaffType(row.getStaffType());
            brief.setDutyStatus(row.getDutyStatus());
            brief.setAttendMode(row.getAttendMode());
            brief.setClinicFlag(row.getClinicFlag());
            brief.setActualOnDuty(row.getActualOnDuty());
            result.add(brief);
        }
        return result;
    }

    @Override
    public DutyStaffBriefVO pickDutyStaff(Long deptId, Integer staffType, Long preferEmpId) {
        OnDutyStaffVO staff = bizClinicSourceService.pickDutyStaff(deptId, staffType, preferEmpId);
        if (staff == null) {
            return null;
        }
        DutyStaffBriefVO brief = new DutyStaffBriefVO();
        brief.setStaffId(staff.getStaffId());
        brief.setStaffName(staff.getStaffName());
        return brief;
    }
}
