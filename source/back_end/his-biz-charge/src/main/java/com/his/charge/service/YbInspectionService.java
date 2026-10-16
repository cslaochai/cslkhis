package com.his.charge.service;


import com.his.charge.dto.YbCancelDTO;
import com.his.charge.dto.YbInspectConcludeDTO;
import com.his.charge.dto.YbInspectionQueryPageDTO;
import com.his.charge.dto.YbInspectionUpsertDTO;
import com.his.charge.vo.YbInspectionListVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 医保飞检/专项审核批次服务（任务头台账，扣款通知挂在批次下）。
 */
public interface YbInspectionService {

    /**
     * 分页查询（附名下扣款单数与金额合计）
     */
    PageResult<YbInspectionListVO> listPage(YbInspectionQueryPageDTO queryDTO);

    /**
     * 详情
     */
    YbInspectionListVO getById(Long id);

    /**
     * 进行中批次下拉（新建扣款通知时挂批次用，只取 status=1）
     */
    List<YbInspectionListVO> selectRunningList();

    /**
     * 新增/修改（单号服务端生成；仅「进行中」可改）
     */
    YbInspectionListVO upsert(YbInspectionUpsertDTO dto);

    /**
     * 结项（1-进行中 → 2-已结项，结论必填，结项后内容不可再改）
     */
    void conclude(YbInspectConcludeDTO dto);

    /**
     * 作废（1-进行中 → 3-已作废；名下有扣款通知时禁止）
     */
    void cancel(YbCancelDTO dto);
}
