package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.RadioReportAuditDTO;
import com.his.medicaltech.dto.RadioReportQueryPageDTO;
import com.his.medicaltech.dto.RadioReportUpsertDTO;
import com.his.medicaltech.dto.RadioTemplateUpsertDTO;
import com.his.medicaltech.vo.RadioReportDetailVO;
import com.his.medicaltech.vo.RadioReportListVO;
import com.his.medicaltech.vo.RadioReportTemplateVO;

import java.util.List;

/**
 * 放射诊断报告书写台（sql/138）。
 *
 * <p>报告状态机（报告单的报告状态 + 检查记录的记录状态联动）：
 * <pre>
 *   拍片完成（技师）        → record 4 已出结果，报告还不存在
 *   保存草稿（医师）        → report 0 草稿
 *   提交审核（医师）        → report 1 待审核 + 报告医师签名
 *   审核通过（上级医师）    → report 3 已审核 + 审核医师签名，record 5 已审核
 *   退回重写（上级医师）    → report 0 草稿 + version+1 + 清签名指针，record 退回 4
 *   发布（医师）            → report 4 已发布，record 6 已发布
 * </pre>
 */
public interface RadiologyReportService {

    /** 工作台分页列表（检查记录 LEFT JOIN 报告） */
    PageResult<RadioReportListVO> listPage(RadioReportQueryPageDTO query);

    /** 按检查记录取详情（还没写报告时也能取，images 有值、报告字段为 null） */
    RadioReportDetailVO getDetailByRecordId(Long recordId);

    /** 按报告ID取详情 */
    RadioReportDetailVO getDetailByReportId(Long reportId);

    /** 保存草稿（0-草稿；不签名、不通知） */
    RadioReportDetailVO saveDraft(RadioReportUpsertDTO dto);

    /** 提交审核（→1 待审核 + 报告医师签名） */
    RadioReportDetailVO submit(RadioReportUpsertDTO dto);

    /** 审核通过（→3 已审核 + 审核医师签名） */
    RadioReportDetailVO audit(RadioReportAuditDTO dto);

    /** 退回重写（→0 草稿 + 版本号+1 + 清签名指针） */
    RadioReportDetailVO reject(RadioReportAuditDTO dto);

    /** 发布（→4 已发布，通知开单医生） */
    RadioReportDetailVO publish(Long reportId);

    /** 模板下拉（按模态过滤；未登录员工ID时只返回公用模板） */
    List<RadioReportTemplateVO> templateSelectList(Integer modality);

    /** 模板列表（维护用，含停用） */
    List<RadioReportTemplateVO> templateList();

    /** 新增/修改模板 */
    RadioReportTemplateVO templateUpsert(RadioTemplateUpsertDTO dto);

    /** 删除模板；返回 false 表示模板不存在或已被删除 */
    boolean templateDeleteById(Long id);
}
