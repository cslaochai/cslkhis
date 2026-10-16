package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.PivasActionDTO;
import com.his.pharmacy.dto.PivasAuditDTO;
import com.his.pharmacy.dto.PivasGenerateDTO;
import com.his.pharmacy.dto.PivasQueryPageDTO;
import com.his.pharmacy.vo.PivasCandidateVO;
import com.his.pharmacy.vo.PivasStatsVO;
import com.his.pharmacy.vo.PivasVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 静脉用药调配中心（PIVAS）服务：审方 → 排队（打标签取号）→ 调配 → 核对发放。
 */
public interface PivasService {

    /**
     * 可静配医嘱候选（生成前预览）
     */
    List<PivasCandidateVO> candidates(Long wardId, Long admissionId, LocalDate admixDate);

    /**
     * 生成静配单（同入院同日复用主单、明细追加，幂等；新明细一律待审方）
     */
    PivasVO generate(PivasGenerateDTO dto);

    /**
     * 主单分页
     */
    PageResult<PivasVO> listPage(PivasQueryPageDTO dto);

    /**
     * 主单详情（含明细）
     */
    PivasVO getDetailById(Long id);

    /**
     * 审方：1→2 通过 / 1→0 退回（原因必填，退回终态、医嘱可重新入单）
     */
    PivasVO auditItem(PivasAuditDTO dto);

    /**
     * 打标签排队：主单内全部已审方明细 2→3 并取排队号（标签打印预留：只取号盖时间）
     */
    PivasVO labelBatch(PivasActionDTO dto);

    /**
     * 调配：3→4
     */
    PivasVO compoundItem(PivasActionDTO dto);

    /**
     * 核对发放：4→5（终态，成品配送病区）
     */
    PivasVO verifyItem(PivasActionDTO dto);

    /**
     * 统计（按调配日期 + 可选病区）
     */
    PivasStatsVO stats(LocalDate admixDate, Long wardId);
}
