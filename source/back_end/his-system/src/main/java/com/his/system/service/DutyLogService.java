package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.DutyLogHandoverDTO;
import com.his.system.dto.DutyLogQueryPageDTO;
import com.his.system.dto.DutyLogUpsertDTO;
import com.his.system.vo.DutyLogVO;
import java.util.List;

public interface DutyLogService {

    PageResult<DutyLogVO> listPage(DutyLogQueryPageDTO q);

    List<DutyLogVO> pendingMine();

    Long upsert(DutyLogUpsertDTO dto);

    void deleteById(Long id);

    void handover(DutyLogHandoverDTO dto);

    void ack(Long id);
}
