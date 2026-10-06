package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.*;
import com.his.medicaltech.vo.EcgDetailVO;
import com.his.medicaltech.vo.EcgListVO;
import com.his.medicaltech.vo.EcgTemplateVO;

import java.util.List;

/**
 * 心电工作站（sql/173）：签到 / 波形采集 / 测量 / Holter 分析 / 报告书写 / 审核 / 发布。
 */
public interface EcgService {

    PageResult<EcgListVO> listPage(EcgQueryPageDTO query);

    EcgDetailVO getDetailByRecordId(Long recordId);

    /**
     * 签到（1已登记 → 2已签到）
     */
    EcgDetailVO checkIn(Long recordId);

    /**
     * 波形采集（设备推送路径）：waveData JSON 落库，记录置 4-已出结果
     */
    EcgDetailVO collectWave(EcgCollectWaveDTO dto);

    /**
     * 模拟采集（联调/演示路径）：服务端合成 12 导联波形并落库
     */
    EcgDetailVO simulateWave(EcgSimulateDTO dto);

    /**
     * 保存测量参数（upsert）
     */
    EcgDetailVO saveMeasure(EcgMeasureUpsertDTO dto);

    /**
     * 保存 Holter 分析（upsert）
     */
    EcgDetailVO saveHolter(EcgHolterUpsertDTO dto);

    EcgDetailVO saveDraft(EcgReportUpsertDTO dto);

    EcgDetailVO submit(EcgReportUpsertDTO dto);

    EcgDetailVO audit(EcgAuditDTO dto);

    EcgDetailVO reject(EcgAuditDTO dto);

    EcgDetailVO publish(Long reportId);

    /**
     * 模板下拉（按心电类型过滤，通用模板任何类型都能选）
     */
    List<EcgTemplateVO> templateSelectList(Integer ecgType);

    /**
     * 模板列表（维护用，含停用）
     */
    List<EcgTemplateVO> templateList();

    /**
     * 新增/修改模板
     */
    EcgTemplateVO templateUpsert(EcgTemplateUpsertDTO dto);

    /**
     * 删除模板；返回 false 表示模板不存在或已被删除
     */
    boolean templateDeleteById(Long id);
}
