package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.DoseLimitPageQueryDTO;
import com.his.pharmacy.dto.DoseLimitUpsertDTO;
import com.his.pharmacy.dto.DrugInteractionPageQueryDTO;
import com.his.pharmacy.dto.DrugInteractionUpsertDTO;
import com.his.pharmacy.vo.DoseLimitVO;
import com.his.pharmacy.vo.DrugInteractionVO;

/**
 * 合理用药知识库维护（药物相互作用 + 剂量上限）
 */
public interface DrugKnowledgeService {

    /**
     * 药物相互作用分页：按成分对、严重度、启停状态筛选。
     * 用于合理用药知识库页「药物相互作用」标签页的表格与查询卡。
     */
    PageResult<DrugInteractionVO> interactionListPage(DrugInteractionPageQueryDTO queryDTO);

    /**
     * 新增/修改（id 为空=新增），回读整条 VO 返回
     * <p>
     * 不返回裸 Long：雪花主键是 19 位，JSON 里当数字给前端会丢精度（AGENTS §1 要求 id 一律字符串），
     * 而调用方真正想知道的是「归一化后的成分对顺序 + 命中了几种药」，这两件事光有一个 id 都看不出来。
     */
    DrugInteractionVO interactionUpsert(DrugInteractionUpsertDTO upsertDTO);

    /**
     * 物理删（唯一键不含 del_flag，软删会永久占住这一对成分）
     */
    void interactionDeleteById(Long id);

    /**
     * 剂量上限分页：按成分、药品关键字与启停状态筛选。
     * 用于合理用药知识库页「剂量上限」标签页的表格与查询卡。
     */
    PageResult<DoseLimitVO> doseLimitListPage(DoseLimitPageQueryDTO queryDTO);

    /**
     * 同 interactionUpsert，回读整条 VO
     */
    DoseLimitVO doseLimitUpsert(DoseLimitUpsertDTO upsertDTO);

    /**
     * 物理删，同上
     */
    void doseLimitDeleteById(Long id);
}
