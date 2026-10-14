package com.mis.service;

import com.mis.common.base.PageVO;
import com.mis.dto.InspectionFinishDTO;
import com.mis.dto.InspectionItemUpsertDTO;
import com.mis.dto.InspectionQueryDTO;
import com.mis.dto.InspectionUpsertDTO;
import com.mis.entity.MssInspection;
import com.mis.vo.ActionVO;
import com.mis.vo.InspectionDetailVO;

/**
 * 飞检任务
 */
public interface InspectionService {

    PageVO<MssInspection> inspectionPage(InspectionQueryDTO query);

    ActionVO upsert(InspectionUpsertDTO dto);

    /** 开始检查：草稿/待开展 → 检查中 */
    ActionVO start(Long id);

    /** 办结任务：检查中 → 已完成 */
    ActionVO finish(InspectionFinishDTO dto);

    InspectionDetailVO getDetail(Long id);

    /** 记录发现问题 */
    ActionVO addItem(InspectionItemUpsertDTO dto);

    ActionVO deleteItem(Long id);

    /** 从问题明细生成追回单（合计涉及金额） */
    ActionVO toRecover(Long id);
}
