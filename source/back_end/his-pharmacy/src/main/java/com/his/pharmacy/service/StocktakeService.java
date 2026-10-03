package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.StocktakeAuditDTO;
import com.his.pharmacy.dto.StocktakeCountUpsertDTO;
import com.his.pharmacy.dto.StocktakeIdDTO;
import com.his.pharmacy.dto.StocktakeQueryPageDTO;
import com.his.pharmacy.dto.StocktakeUpsertDTO;
import com.his.pharmacy.vo.StocktakeVO;

/**
 * 药房盘点服务
 *
 * <p>单据流：建单（抓账面快照）【盘点中】→（录实盘，可反复）→（提交）
 * 有差异【待复核】/ 无差异【已关单】→（复核通过）【已过账，差异落库存流水】，
 * 复核不通过退回【盘点中】。
 */
public interface StocktakeService {

    PageResult<StocktakeVO> listPage(StocktakeQueryPageDTO query);

    /** 详情（含明细与本次过账流水） */
    StocktakeVO getDetailById(Long id);

    /** 建单（抓快照）/ 改主题与范围（仅盘点中，改范围重抓快照并清空实盘数） */
    StocktakeVO upsert(StocktakeUpsertDTO dto);

    /** 录入实盘数（仅盘点中，可反复保存） */
    StocktakeVO saveCount(StocktakeCountUpsertDTO dto);

    /** 提交：全部批次已录实盘数才放行；无差异直接关单，有差异转待复核 */
    StocktakeVO submit(StocktakeIdDTO dto);

    /** 复核：通过则差异过账（盘盈/盘亏落流水并调批次余额），不通过退回盘点中 */
    StocktakeVO audit(StocktakeAuditDTO dto);

    /** 删除（仅盘点中；已过账是账，删不得） */
    void deleteById(Long id);
}
