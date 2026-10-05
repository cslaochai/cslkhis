package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.RxFlowActionDTO;
import com.his.emr.dto.RxFlowQueryPageDTO;
import com.his.emr.dto.RxFlowUpsertDTO;
import com.his.emr.vo.RxFlowListVO;

/**
 * 处方流转单（院外取药口子）。
 */
public interface RxFlowService {

    /**
     * 创建流转单（处方 → 院外机构）
     */
    RxFlowListVO createFlow(RxFlowUpsertDTO dto);

    /**
     * 取药完成回写（1→2，终态不可逆）
     */
    void finish(RxFlowActionDTO dto);

    /**
     * 取消流转（1→3，终态不可逆）
     */
    void cancel(RxFlowActionDTO dto);

    /**
     * 流转单分页
     */
    PageResult<RxFlowListVO> listPage(RxFlowQueryPageDTO dto);
}
