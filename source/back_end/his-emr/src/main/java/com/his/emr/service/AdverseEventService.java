package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.AdverseEventActionDTO;
import com.his.emr.dto.AdverseEventQueryPageDTO;
import com.his.emr.dto.AdverseEventUpsertDTO;
import com.his.emr.vo.AdverseEventStatsVO;
import com.his.emr.vo.AdverseEventVO;

/**
 * 不良事件服务
 */
public interface AdverseEventService {

    /** 分页查询 */
    PageResult<AdverseEventVO> page(AdverseEventQueryPageDTO queryDTO);

    /** 详情 */
    AdverseEventVO getDetailById(Long id);

    /** 上报（新增）/ 修改（仅状态=1 且本人上报的单） */
    Long upsert(AdverseEventUpsertDTO dto);

    /** 处理（1→2，D） */
    void handle(AdverseEventActionDTO dto);

    /** 整改（2→3，C） */
    void rectify(AdverseEventActionDTO dto);

    /** 结案（3→4，A，不可逆） */
    void close(AdverseEventActionDTO dto);

    /** 删除（仅状态=1 且本人上报的单，软删） */
    void deleteById(Long id);

    /** 工作台统计（本月口径） */
    AdverseEventStatsVO monthStats();
}
