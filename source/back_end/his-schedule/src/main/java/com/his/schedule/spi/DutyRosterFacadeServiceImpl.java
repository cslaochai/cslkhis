package com.his.schedule.spi;

import com.his.schedule.service.DutyRosterService;
import com.his.schedule.vo.DutyOfficerVO;
import com.his.spi.service.DutyRosterFacadeService;
import com.his.spi.vo.DutyOfficerBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 值班表端口实现：转调排班域值班表服务，只交出当班人的身份摘要。
 */
@Service
@RequiredArgsConstructor
public class DutyRosterFacadeServiceImpl implements DutyRosterFacadeService {

    private final DutyRosterService dutyRosterService;

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
}
