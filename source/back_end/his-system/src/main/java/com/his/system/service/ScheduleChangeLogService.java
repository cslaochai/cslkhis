package com.his.system.service;

import com.his.common.enums.ScheduleChangeTypeEnum;
import com.his.system.vo.ScheduleChangeLogVO;

import java.util.List;

/**
 * 排班变更留痕服务（只写不改，读侧按排班捞）。
 */
public interface ScheduleChangeLogService {

    /**
     * 写一条变更事实。被换班、加减号、停班这些动作各自在完成后调它留痕，
     * 调用方不需要关心创建人与时间（由服务端现取）。
     *
     * <p>②非 web 入口入参：由排班 service 用实体字段直接调用，不经请求体绑定，注解拦不到。
     *
     * @return 留痕行主键
     */
    Long record(Long staffScheduleId, ScheduleChangeTypeEnum actionType,
                Long fromEmployeeId, Long toEmployeeId,
                Long fromShiftId, Long toShiftId, Integer amount, String reason);

    /**
     * 某条排班的变更记录（按变更时间倒序，最新在前）。
     */
    List<ScheduleChangeLogVO> listBySchedule(Long staffScheduleId);
}
