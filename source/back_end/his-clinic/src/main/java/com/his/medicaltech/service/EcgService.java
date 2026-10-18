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

    /**
     * 心电工作台分页：检查记录连带波形、测量、Holter 分析与报告状态，未采集未书写的也在工作台内。
     * 用于心电工作站列表卡的查询与分页。
     */
    PageResult<EcgListVO> listPage(EcgPageQueryDTO query);

    /**
     * 按检查记录取心电详情（含波形数据，尚未采集时同样可取）。
     * 用于心电工作站的采集弹框与书写弹框打开时回显。
     */
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

    /**
     * 保存报告草稿：只留文书内容，不做完整性校验，可反复保存。
     * 用于心电书写弹框的「存草稿」。
     */
    EcgDetailVO saveDraft(EcgReportUpsertDTO dto);

    /**
     * 提交报告进入待审核并落报告医师签名（Holter 还需先完成分析）。
     * 用于心电书写弹框的「提交审核」。
     */
    EcgDetailVO submit(EcgReportUpsertDTO dto);

    /**
     * 审核通过并落审核医师签名，审核人不得是报告人本人。
     * 用于心电工作站待审核报告的审核弹框。
     */
    EcgDetailVO audit(EcgAuditDTO dto);

    /**
     * 退回重写：结论作废、报告退回草稿并清签名。
     * 用于审核弹框选择退回时填写驳回原因后回写状态。
     */
    EcgDetailVO reject(EcgAuditDTO dto);

    /**
     * 发布已审核的报告并把结果推送给申请医师。
     * 用于心电工作站对审核通过报告做最后放行，发布后申请方与患者端才可见。
     */
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
