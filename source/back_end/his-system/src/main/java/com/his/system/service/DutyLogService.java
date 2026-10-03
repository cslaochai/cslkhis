package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.DutyLogHandoverDTO;
import com.his.system.dto.DutyLogQueryPageDTO;
import com.his.system.dto.DutyLogUpsertDTO;
import com.his.system.vo.DutyLogVO;
import java.util.List;

public interface DutyLogService {

    public static final int TYPE_EVENT = 1;

    public static final int TYPE_LEFTOVER = 2;

    public static final int TYPE_PATROL = 3;

    public static final int ST_PENDING = 0;

    public static final int ST_DONE = 1;

    public static final int ST_HANDED = 2;

    public static final int ST_ACKED = 3;

    PageResult<DutyLogVO> listPage(DutyLogQueryPageDTO q);

    List<DutyLogVO> pendingMine();

    Long upsert(DutyLogUpsertDTO dto);

    void deleteById(Long id);

    void handover(DutyLogHandoverDTO dto);

    void ack(Long id);
}
