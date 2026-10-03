package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.DutyRosterQueryPageDTO;
import com.his.system.dto.DutyRosterUpsertDTO;
import com.his.system.dto.DutySubstituteDTO;
import com.his.system.vo.DutyOfficerVO;
import com.his.system.vo.DutyRosterVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DutyRosterService {

    DutyOfficerVO current();

    DutyOfficerVO currentAt(LocalDateTime at);

    DutyOfficerVO officerOf(LocalDate date, int shift);

    DutyOfficerVO nextOfficer(LocalDate date, int shift);

    List<DutyRosterVO> todayList();

    PageResult<DutyRosterVO> listPage(DutyRosterQueryPageDTO q);

    Long upsert(DutyRosterUpsertDTO dto);

    void substitute(DutySubstituteDTO dto);

    void cancelSubstitute(Long id);

    void deleteById(Long id);
}
