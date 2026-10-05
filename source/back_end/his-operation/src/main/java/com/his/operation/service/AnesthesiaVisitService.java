package com.his.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.AnesthesiaVisitFinishDTO;
import com.his.operation.dto.AnesthesiaVisitQueryPageDTO;
import com.his.operation.dto.AnesthesiaVisitUpsertDTO;
import com.his.operation.vo.AnesthesiaVisitVO;

/**
 * 麻醉术前访视服务。
 *
 * <p>链上的位置：<b>麻醉记录单的前置闸门</b>。
 * 没有"可施行麻醉"结论的访视单，就不允许开立麻醉记录（急诊例外，且必须标红待补）。
 */
public interface AnesthesiaVisitService {

    IPage<AnesthesiaVisitVO> listPage(AnesthesiaVisitQueryPageDTO query);

    AnesthesiaVisitVO getDetailById(Long visitId);

    /**
     * 某台手术的访视单（没有则返回 null —— 由调用方决定是提示还是报错）
     */
    AnesthesiaVisitVO getByApply(Long applyId);

    /**
     * 保存（新增 / 修改草稿），返回访视单号
     */
    String save(AnesthesiaVisitUpsertDTO dto);

    /**
     * 完成访视（草稿 → 已完成，结论出账）
     */
    void finish(AnesthesiaVisitFinishDTO dto);

    /**
     * 该申请是否已存在"可作为麻醉依据"的访视（conclusion=1 且已完成）
     */
    boolean hasApprovedVisit(Long applyId);

    /**
     * 已完成但没有合格访视的手术台数（工作台角标：急诊超前麻醉的欠账）
     */
    long countFinishedWithoutVisit();
}
