package com.his.operation.service;

import com.his.operation.dto.CountItemInputUpsertDTO;
import com.his.operation.dto.CountPhaseDTO;
import com.his.operation.dto.OperationCountUpsertDTO;
import com.his.operation.vo.OperationCountVO;

/**
 * 手术器械/敷料清点服务（术前 → 关体前 → 关体后三阶段双人核对）。
 */
public interface OperationCountService {

    /**
     * 某台手术的清点单（含明细；没有则返回 null）
     */
    OperationCountVO getByApply(Long applyId);

    OperationCountVO getDetailById(Long countId);

    /**
     * 建立清点单（含术前基数），返回清点单号
     */
    String create(OperationCountUpsertDTO dto);

    /**
     * 追加一行清点明细（关体后阶段开始前可加）
     */
    void addItem(Long countId, CountItemInputUpsertDTO dto);

    /**
     * 登记某一阶段的清点数量（必须逐项给全）
     */
    void countPhase(CountPhaseDTO dto);

    /**
     * 某台手术的清点是否尚未走完三轮（存在清点单但没走完）
     */
    boolean unfinished(Long applyId);

    /**
     * 某台手术是否存在"登记了但对不上"的清点
     */
    boolean hasDiscrepancy(Long applyId);
}
