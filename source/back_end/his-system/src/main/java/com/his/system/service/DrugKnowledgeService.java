package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.DoseLimitQueryPageDTO;
import com.his.system.dto.DoseLimitUpsertDTO;
import com.his.system.dto.DrugInteractionQueryPageDTO;
import com.his.system.dto.DrugInteractionUpsertDTO;
import com.his.system.vo.DoseLimitVO;
import com.his.system.vo.DrugInteractionVO;

/**
 * 合理用药知识库维护（药物相互作用 + 剂量上限）
 */
public interface DrugKnowledgeService {

    PageResult<DrugInteractionVO> interactionListPage(DrugInteractionQueryPageDTO queryDTO);

    /**
     * 新增/修改（id 为空=新增），回读整条 VO 返回
     * <p>
     * 不返回裸 Long：雪花主键是 19 位，JSON 里当数字给前端会丢精度（AGENTS §1 要求 id 一律字符串），
     * 而调用方真正想知道的是「归一化后的成分对顺序 + 命中了几种药」，这两件事光有一个 id 都看不出来。
     */
    DrugInteractionVO interactionUpsert(DrugInteractionUpsertDTO upsertDTO);

    /** 物理删（唯一键不含 del_flag，软删会永久占住这一对成分） */
    void interactionDeleteById(Long id);

    PageResult<DoseLimitVO> doseLimitListPage(DoseLimitQueryPageDTO queryDTO);

    /** 同 interactionUpsert，回读整条 VO */
    DoseLimitVO doseLimitUpsert(DoseLimitUpsertDTO upsertDTO);

    /** 物理删，同上 */
    void doseLimitDeleteById(Long id);
}
