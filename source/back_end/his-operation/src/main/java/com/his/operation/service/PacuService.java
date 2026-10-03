package com.his.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.AnesthesiaActionDTO;
import com.his.operation.dto.PacuEnterDTO;
import com.his.operation.dto.PacuLeaveDTO;
import com.his.operation.dto.PacuQueryPageDTO;
import com.his.operation.dto.PacuScoreDTO;
import com.his.operation.vo.OperationChargeSummaryVO;
import com.his.operation.vo.PacuRecordVO;

/**
 * PACU 麻醉后监测治疗服务（入室 → Aldrete 评分 → 出室 → 计费联动）。
 */
public interface PacuService {

    IPage<PacuRecordVO> listPage(PacuQueryPageDTO query);

    PacuRecordVO getDetailById(Long pacuId);

    /** 某条麻醉记录对应的 PACU 复苏单（没有则返回 null） */
    PacuRecordVO getByRecord(Long recordId);

    /** 入 PACU 登记，返回复苏单号 */
    String enter(PacuEnterDTO dto);

    /** Aldrete 评分（服务端逐项相加求总分，不接收前端传的总分） */
    void score(PacuScoreDTO dto);

    /** 出室（自动触发 PACU 计费联动） */
    OperationChargeSummaryVO leave(PacuLeaveDTO dto);

    /** 重新计费（失败项重试） */
    OperationChargeSummaryVO charge(AnesthesiaActionDTO dto);

    /** 在室人数（复苏室床位看板用） */
    long countInRoom();
}
