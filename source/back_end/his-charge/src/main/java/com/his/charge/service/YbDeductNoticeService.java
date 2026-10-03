package com.his.charge.service;

import com.his.charge.dto.*;
import com.his.charge.vo.DeductNoticeDetailVO;
import com.his.charge.vo.DeductNoticeListVO;
import com.his.charge.vo.DeductSummaryVO;
import com.his.common.base.PageResult;

/**
 * 医保扣款通知服务：申诉 → 确认追责 → 缴回的闭环台账，每个动作都写留痕。
 */
public interface YbDeductNoticeService {

    /**
     * 分页查询（附超期展示态）
     */
    PageResult<DeductNoticeListVO> listPage(DeductNoticeQueryPageDTO queryDTO);

    /**
     * 详情（含全过程留痕，正序）
     */
    DeductNoticeDetailVO getDetailById(Long id);

    /**
     * 台账汇总（卡片数字来自 SQL 聚合）
     */
    DeductSummaryVO summary();

    /**
     * 新增/修改草稿（单号服务端生成；仅「待确认」可改）
     */
    DeductNoticeListVO upsert(DeductNoticeUpsertDTO dto);

    /**
     * 发起申诉（待确认 → 申诉中）
     */
    void appeal(DeductAppealDTO dto);

    /**
     * 录入申诉结果（申诉中 → 申诉成功 / 维持扣款待缴）
     */
    void appealResult(DeductAppealResultDTO dto);

    /**
     * 确认扣款并追责（待确认 → 待缴；待缴状态下可补/改追责三要素）
     */
    void confirm(DeductConfirmDTO dto);

    /**
     * 录入缴回（待缴 → 已缴回，金额必须等于扣款金额）
     */
    void payback(DeductPaybackDTO dto);

    /**
     * 作废（仅待确认可作废，理由必填）
     */
    void cancel(YbCancelDTO dto);
}
