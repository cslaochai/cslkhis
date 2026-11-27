package com.his.clinical.service;

import com.his.clinical.dto.EmrCorrectionApplyDTO;
import com.his.clinical.dto.EmrCorrectionOpinionDTO;
import com.his.clinical.dto.EmrCorrectionPageQueryDTO;
import com.his.clinical.entity.BizEmrCorrectionRequest;
import com.his.clinical.vo.EmrCorrectionVO;
import com.his.common.base.PageResult;

/**
 * 病历更正申请服务：已提交/归档病历的更正审批与回写留痕。
 */
public interface EmrCorrectionService {

    /**
     * 发起更正申请（仅已提交/归档病历；同一病历在途单唯一）。
     */
    EmrCorrectionVO apply(EmrCorrectionApplyDTO dto);

    /**
     * 科室初审：通过 → 转医务审批；驳回 → 终态驳回。
     */
    void deptApprove(EmrCorrectionOpinionDTO dto);

    /**
     * 医务终审：通过 → 已批准待修改；驳回 → 终态驳回。
     */
    void medicalApprove(EmrCorrectionOpinionDTO dto);

    /**
     * 申请人撤回（仅待科室审核 / 待医务审批可撤）。
     */
    void revoke(Long id);

    /**
     * 台账分页（按数据权限收口科室）。
     */
    PageResult<EmrCorrectionVO> page(EmrCorrectionPageQueryDTO query);

    /**
     * 申请单详情。
     */
    EmrCorrectionVO getDetail(Long id);

    /**
     * 取病历已批准待修改的更正申请（病历回写授权闸用）；无则返回 null。
     */
    BizEmrCorrectionRequest findApprovedByRecord(Long recordId);

    /**
     * 病历回写完成闭环：置已完成，记实际修改人与时间，并写病历日志留痕。
     */
    void markCompleted(Long recordId, Long modifierId, String modifierName);
}
